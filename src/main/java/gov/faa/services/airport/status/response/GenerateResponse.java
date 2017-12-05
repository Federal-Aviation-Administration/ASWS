package gov.faa.services.airport.status.response;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import gov.faa.airportstatus.schema.response.ObjectFactory;

import gov.faa.airportstatus.schema.response.Meta;
import gov.faa.airportstatus.schema.response.Weather;
import gov.faa.airportstatus.schema.response.AirportStatus;
import gov.faa.airportstatus.schema.response.Status;

import gov.faa.airportstatus.schema.wx.CurrentObservation;
import gov.faa.services.airport.data.AirportData;
import gov.faa.services.airport.data.AirportInformation;
import gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION;
import gov.faa.airportstatus.schema.asi.Airport;
import gov.faa.airportstatus.schema.asi.AirportClosureList;
import gov.faa.airportstatus.schema.asi.ArrivalDeparture;
import gov.faa.airportstatus.schema.asi.ArrivalDepartureDelayList;
import gov.faa.airportstatus.schema.asi.Delay;
import gov.faa.airportstatus.schema.asi.DelayType;
import gov.faa.airportstatus.schema.asi.GroundDelay;
import gov.faa.airportstatus.schema.asi.GroundDelayList;
import gov.faa.airportstatus.schema.asi.GroundStopList;
import gov.faa.airportstatus.schema.asi.Program;

/**
 * This class builds the XML response to an airport status call. The target airport and the stream of XML from the
 * airport status data call to fly.faa.gov are passed as parameters. The XML is processed and the requested airport 
 * status is constructed and returned. 
 * @author FAA
 *
 */
public class GenerateResponse {
	final static Logger logger = LoggerFactory.getLogger(GenerateResponse.class);
	ObjectFactory factory = new ObjectFactory();
	boolean knownAirport = false;
	
	String airport = new String();
	AIRPORTSTATUSINFORMATION asi = null;
	CurrentObservation wxobs = null;
	AirportStatus response = factory.createAirportStatus();

	/*
	 * The public constructor accepts the AIRPORTSTATUSINFORMATION as serialized by the JAXB objects representing the
	 * XML DTD file. The target airport is obtained from the REST service call context path. Both information are passed to
	 * this constructor to setup the response automatically.
	 */
	public GenerateResponse(AIRPORTSTATUSINFORMATION asi, CurrentObservation wxobs, String targetAirport) {
		this.asi = asi;
		this.airport = targetAirport;
		this.wxobs = wxobs; 
		initResponse();
	}

	/*
	 * The initResponse method constructs the status object and sets the status on the response object. It also sets 
	 * the target airport IATA and ICAO in the response object.
	 */
	private void initResponse () {
		
		AirportInformation airportData = AirportData.getData(airport);
		response = factory.createAirportStatus();
		Status baseStatus = factory.createStatus();
		/*
		 * If we cannot find the airport in our map, then setup a basic airport and
		 * flag it as unknown. Else we build the full status and keep chugging.
		 */
		if (airportData == null) {
			logger.warn("Client provided an unknown airport code "+airport+". Status is only available for major US airports.");
			response.setIATA(airport);
			baseStatus.setReason("Status is only available for major airports in the United States");
			response.setCity("UNKNOWN");
			response.setState("UNKNOWN");
			response.setName("UNKNOWN");
			response.getStatus().add(baseStatus);
			response.setDelay(false);
			response.setSupportedAirport(false);
			knownAirport = false;
		}

		else {
			response.setIATA(airport);
			response.setICAO("K"+airport);
			response.setName(airportData.getName());
			response.setCity(airportData.getCity());
			response.setState(airportData.getState());
			response.setSupportedAirport(true);

			knownAirport = true;
		}
		
	}

	/**
	 * The getResponse object builds the XML response of JAXB objects which can be marshalled as XML 
	 * for the REST response. 
	 * @return AirportStatus which is the root JAXB object for the XML response
	 */
	public AirportStatus getResponse () {
		long startTime = System.currentTimeMillis();
		
		logger.info("Start response processing for request of airport "+airport);
		// if we don't have the airport info nor the weather, return an empty
		// response
		if (asi == null && wxobs == null) {
			return response;
		}
		// here we have no airport info, but we did get the weather data, so we'll
		// set the weather and return the response
		else if (asi == null && wxobs != null) {
			setWeather();
			return response;
		}
		
		// we have airport info and weather, so process the delays we see

		processDelays();
		
		logger.info("End of response processing. Response is ready to return for airport "+airport+" and took "+(System.currentTimeMillis()-startTime)+" ms.");
		return response;
	}
	
	/*
	 * This method processes the 4 delay types that may be provided in the XML stream used as input to this object.
	 */
	private void processDelays () {
		int delayCount = 0;
		
		logger.info("Start of delay processing for airport "+airport);
		
		if (logger.isDebugEnabled()) 
			logger.debug("Processing ground delays for airport "+airport);
		delayCount = setGroundDelay();
		if (logger.isDebugEnabled())
			logger.debug("Processing ground stops for airport "+airport);
		delayCount = delayCount + setGroundStop();
		if (logger.isDebugEnabled())
			logger.debug("Processing Arrival/Departure delays for airport "+airport);
		delayCount = delayCount + setArriveDepartDelay();
		if (logger.isDebugEnabled())
			logger.debug("Processing airport closures for airport "+airport);
		delayCount = delayCount + setAirportClosure();
		
		if (delayCount == 0) {
			if (logger.isDebugEnabled())
				logger.debug("No delays found for airport "+airport);
			response.getStatus().add(new Status());
			response.getStatus().get(0).setReason("No known delays for this airport");
			response.setDelay(false);
			response.setDelayCount(0);
		}
		else {
			response.setDelay(true);
			response.setDelayCount(delayCount);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Processing weather information for airport "+airport);
		setWeather();
		
		logger.info("End of delay and weather processing for airport "+airport+". Processed "+delayCount+" delays.");
	}
	
	/*
	 * The ground delays are processed first to determine if any ground delays are present. We only process ground
	 * delays if the target airport has a delay. The XML stream will contain delays for any airport
	 */
	private int setGroundDelay () {
		int delayCount = 0;
		List <DelayType> delays = asi.getDelayType();
		
		if (delays == null) 
			return 0;
		
		// first loop through the delays
		for (DelayType delay : delays) {
			if (delay == null) 
				continue;
			if (logger.isDebugEnabled()) {
				logger.debug("Delay is "+delay.getName().getContent());
				logger.debug("Delay toString: "+delay.toString());
			}
			GroundDelayList delayList = delay.getGroundDelayList();
			if (delayList == null) {
				if (logger.isDebugEnabled())
					logger.debug("Ground delay list was null");
				continue;
			}
			
			List <GroundDelay> groundDelays = delayList.getGroundDelay();
			if (groundDelays == null) {
				if (logger.isDebugEnabled())
					logger.debug("Ground delays are null");
				continue;
			}
			
			// if we have ground delays, we loop through those looking for our airport. If our airport is found, set
			// the information about the delay in our response object
			for (GroundDelay groundDelay : groundDelays) {
				if (logger.isDebugEnabled())
					logger.debug("Processing delay for airport "+groundDelay.getARPT().getContent()+" against target airport "+airport);
				if (groundDelay.getARPT().getContent().equals(airport)) {
					Status groundDelayStatus = factory.createStatus();
					//response.setDelay(true);
					groundDelayStatus.setAvgDelay(groundDelay.getAvg().getContent());
					response.setName(groundDelay.getARPT().getContent());
					groundDelayStatus.setType("Ground Delay");
					groundDelayStatus.setReason(groundDelay.getReason().getContent());
					response.getStatus().add(groundDelayStatus);
					delayCount++;
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Found "+delayCount+" delays for airport "+airport);
		return delayCount;
	}

	/*
	 * Ground stop programs are processed after delays. There may be 0..N ground stops in the input XML stream, but
	 * only 0..1 ground stops for the target airport are included. 
	 */
	private int setGroundStop () {
		int delayCount = 0;
		List <DelayType> delays = asi.getDelayType();
		
		if (delays == null) 
			return 0;
		
		// first loop through all delays
		for (DelayType delay : delays) {
			GroundStopList stopList = delay.getGroundStopList();
			if (stopList == null)
				continue;
			
			List <Program> stops = stopList.getProgram();
			if (stops == null)
				continue;
			
			// now we have the stops list (if any). loop through the stops list and see if there is a ground
			// stop program for our target airport. If so, setup the response with the ground stop
			for (Program stop : stops) {
				
				if (stop.getARPT().getContent().equals(airport)) {
					
					if (logger.isDebugEnabled())
						logger.debug("Processing ground stop for airport "+stop.getARPT().getContent()+" against target airport "+airport);
					Status groundStopStatus = factory.createStatus();
					groundStopStatus.setReason(stop.getReason().getContent());
					groundStopStatus.setEndTime(stop.getEndTime().getContent());
					response.setIATA(airport);
					response.setICAO("K"+airport);
					response.getStatus().add(groundStopStatus);
					delayCount++;
				}
			}
		}
		return delayCount;
	}
	
	/*
	 * Arrival and Departure delays are processed after ground stops. There may be 0..N arrival or departure delays in the
	 * input XML stream, but only 0..1 delays for the target airport.
	 */
	private int setArriveDepartDelay () {
		int delayCount = 0;
		List <DelayType> delays = asi.getDelayType();
		
		if (delays == null) 
			return 0;
		// First we loop through all delays
		for (DelayType delay : delays) {
			ArrivalDepartureDelayList adDelayList = delay.getArrivalDepartureDelayList();
			if (adDelayList == null)
				continue;
			
			List <Delay> adDelays = adDelayList.getDelay();
			if (adDelays == null) 
				continue;
			
			// within the delays, there could be 0..N arrival and departure delays, so we grab that list next
			for (Delay adDelay : adDelays) {
				// If a delay is associated with our target airport, we set the delay to true and grab the 
				// arrival / departure delay list
				if (adDelay.getARPT().getContent().equals(airport) ) {
					if (logger.isDebugEnabled())
						logger.debug("Processing arrival/departure delay for airport "+adDelay.getARPT().getContent()+" against target airport "+airport);
					delayCount++;
					//response.setDelay(true);
					List <ArrivalDeparture> adList = adDelay.getArrivalDeparture();
					// Since we know the delay is for our airport, we loop through the delays and add info to the response
					// object
						for (ArrivalDeparture ad : adList) {
							Status adDelayStatus = factory.createStatus();
							adDelayStatus.setType(ad.getType());
							//response.setDelay(true);
							adDelayStatus.setReason(adDelay.getReason().getContent());
							adDelayStatus.setMaxDelay(ad.getMax().getContent());
							adDelayStatus.setMinDelay(ad.getMin().getContent());
							adDelayStatus.setTrend(ad.getTrend().getContent());
							response.getStatus().add(adDelayStatus);
					}
				}
			}
		}
		return delayCount;
	}
	
	/*
	 * Finally, airport closures are processed and the time to reopen the airport is set. Only the target airport is processed.
	 */
	private int setAirportClosure () {
		int delayCount = 0;
		List <DelayType> delays = asi.getDelayType();
		if (delays == null) 
			return 0;
		
		// first get the list of all delays
		for (DelayType delay : delays) {
			// now get the closure list (if any) and loop through the list of closures
			AirportClosureList closureList = delay.getAirportClosureList();
			if (closureList == null)
				continue;
			
			List <Airport> closures = closureList.getAirport();
			if (closures == null)
				continue;
			for (Airport airport : closures) {
				// if there is a closure for the target airport, we add the status to the response object
				if (airport.getARPT().getContent().equals(this.airport)) {
					if (logger.isDebugEnabled())
						logger.debug("Processing closure for airport "+airport.getARPT().getContent()+" against target airport "+airport);
					Status closureStatus = factory.createStatus();
					delayCount++;
					closureStatus.setReason(airport.getReason().getContent());
					closureStatus.setClosureEnd(airport.getReopen().getContent());
					response.getStatus().add(closureStatus);
				}
			}
		}
		return delayCount;
	}
	
	private void setWeather () {
		Weather wx = factory.createWeather();
		Weather wxString = factory.createWeather();
		if (wxobs == null) {
			logger.warn("Weather observation was null for "+this.airport);
			wxString.getContent().add("Unable to retrieve weather data");
			wx.getContent().add(wxString);
			response.setWeather(wx);
			return;
		}
				
		wxString.getContent().add(wxobs.getWeather());
		wx.getContent().add(wxString);
		wx.getContent().add(factory.createVisibility(wxobs.getVisibilityMi()));
		

		Meta meta = factory.createMeta();
		meta.setCredit(wxobs.getCredit());
		meta.setUpdated(wxobs.getObservationTime());
		meta.setUrl(wxobs.getCreditURL());
		
		wx.getContent().add(meta);
		wx.getContent().add(factory.createTemp(wxobs.getTemperatureString()));
		wx.getContent().add(factory.createWind(wxobs.getWindDir()+" at "+wxobs.getWindMph()));
		
		response.setWeather(wx);
	}
}
