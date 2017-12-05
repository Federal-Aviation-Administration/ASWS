package gov.faa.services.airport.status.response;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION;
import gov.faa.airportstatus.schema.asi.Airport;
import gov.faa.airportstatus.schema.asi.AirportClosureList;
import gov.faa.airportstatus.schema.asi.ArrivalDeparture;
import gov.faa.airportstatus.schema.asi.DelayType;

import gov.faa.airportstatus.schema.asi.GroundDelayList;
import gov.faa.airportstatus.schema.asi.GroundStopList;
import gov.faa.airportstatus.schema.asi.ArrivalDepartureDelayList;
import gov.faa.airportstatus.schema.asi.Delay;
import gov.faa.airportstatus.schema.asi.Program;
import gov.faa.airportstatus.schema.delays.ObjectFactory;
import gov.faa.airportstatus.schema.delays.Status;
import gov.faa.airportstatus.schema.delays.ArriveDepartDelays;
import gov.faa.airportstatus.schema.delays.Closures;
import gov.faa.airportstatus.schema.delays.Delays;
import gov.faa.airportstatus.schema.delays.GroundDelays;
import gov.faa.airportstatus.schema.delays.GroundStops;
import gov.faa.airportstatus.schema.asi.GroundDelay;

/**
 * Generate the airport delay summary information based upon fly.faa.gov. This is all delays across all major US airports
 * @author Martin CTR Hile
 *
 */
public class GenerateDelaySummary {
	final static Logger logger = LoggerFactory.getLogger(GenerateDelaySummary.class);
	private ObjectFactory factory;
	private AIRPORTSTATUSINFORMATION asi = null;
	private Delays delayResponse;
	
	// Provide a protected null constructor so no other objects attempt to create a generator without airport status information
	@SuppressWarnings("unused")
	private GenerateDelaySummary () { }
	
	/*
	 * The public constructor accepts the AIRPORTSTATUSINFORMATION as serialized by the JAXB objects representing the
	 * XML DTD file. 
	 */
	public GenerateDelaySummary(AIRPORTSTATUSINFORMATION asi) {
		this.asi = asi;
		this.factory = new ObjectFactory();
		this.delayResponse = factory.createDelays();
		initResponse ();
	}
	
	/*
	 * The initResponse method constructs the status object and sets the status on the response object. It also sets 
	 * the target airport IATA and ICAO in the response object.
	 */
	public Delays getResponse () {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Starting response generation for delay summary");
		}
		List <DelayType> asiDelays = asi.getDelayType();
				
		for (DelayType asiDelay : asiDelays) {
			delayResponse.getClosures().getClosure().addAll(processClosures(asiDelay.getAirportClosureList()).getClosure());
			delayResponse.getGroundDelays().getGroundDelay().addAll(processGroundDelays(asiDelay.getGroundDelayList()).getGroundDelay());
			delayResponse.getGroundStops().getGroundStop().addAll(processGroundStops(asiDelay.getGroundStopList()).getGroundStop());
			delayResponse.getArriveDepartDelays().getArriveDepart().addAll(processArriveDepartDelays(asiDelay.getArrivalDepartureDelayList()).getArriveDepart());
		}
		setStatus(delayResponse);
		
		if (logger.isDebugEnabled())
			logger.debug("Processed a total of "+delayResponse.getStatus().getCount()+" delays for major US airports");
		
		return delayResponse;
	}
	
	// Initialize the jaxb bindings that hold the delay results. This creates the top level structures 
	// that are populated by the processing
	private void initResponse () {
		delayResponse.setArriveDepartDelays(factory.createArriveDepartDelays());
		delayResponse.setClosures(factory.createClosures());
		delayResponse.setGroundDelays(factory.createGroundDelays());
		delayResponse.setGroundStops(factory.createGroundStops());
	}
	
	// process the ground delays found in the airport status information
	private GroundDelays processGroundDelays (GroundDelayList delayList) {
		
		if (logger.isDebugEnabled())
			logger.debug("Processing ground delays for delay summary");
		
		if (delayList == null) {
			GroundDelays delays = factory.createGroundDelays();
			delays.setCount(0);
			return delays;
		}
		
		List<gov.faa.airportstatus.schema.asi.GroundDelay> groundDelays = delayList.getGroundDelay();
		gov.faa.airportstatus.schema.delays.GroundDelays delays = factory.createGroundDelays();
		
		// if we have ground delays, we loop through those looking for our airport. If our airport is found, set
		// the information about the delay in our response object
		for (GroundDelay groundDelay : groundDelays) {		
			gov.faa.airportstatus.schema.delays.GroundDelay summaryDelay = factory.createGroundDelay();
			summaryDelay.setAirport(groundDelay.getARPT().getContent());
			summaryDelay.setAvgTime(groundDelay.getAvg().getContent());
			summaryDelay.setReason(groundDelay.getReason().getContent());
			delays.getGroundDelay().add(summaryDelay);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Processed a total of "+delays.getGroundDelay().size()+" ground delays for major airports");
		
		
		return delays;
	}
	
	// process the ground stops found in the airport status information
	private GroundStops processGroundStops (GroundStopList stopList) {
		
		if (logger.isDebugEnabled())
			logger.debug("Processing ground stop programs for delay summary");
		
		if (stopList == null) {
			GroundStops stops = factory.createGroundStops();
			stops.setCount(0);
			return stops;
		}
		
		List<gov.faa.airportstatus.schema.asi.Program> programList = stopList.getProgram();
		gov.faa.airportstatus.schema.delays.GroundStops stops = factory.createGroundStops();

		for (Program program : programList) {
			gov.faa.airportstatus.schema.delays.GroundStop summaryStop = factory.createGroundStop();
			summaryStop.setAirport(program.getARPT().getContent());
			summaryStop.setEndTime(program.getEndTime().getContent());
			summaryStop.setReason(program.getReason().getContent());
			stops.getGroundStop().add(summaryStop);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Processed a total of "+stops.getGroundStop().size()+" ground stops for major airports");
		
		return stops;
	}
	
	// process the arrival and departure delays found in the status information
	private ArriveDepartDelays processArriveDepartDelays (ArrivalDepartureDelayList adList) {
		
		if (logger.isDebugEnabled())
			logger.debug("Processing arrival and departure delays for delay summary");
		
		if (adList == null) {
			ArriveDepartDelays delays = factory.createArriveDepartDelays();
			delays.setCount(0);
			return delays;
		}
		
		List<gov.faa.airportstatus.schema.asi.Delay> delayList = adList.getDelay();
		gov.faa.airportstatus.schema.delays.ArriveDepartDelays summaryDelays = factory.createArriveDepartDelays();

		for (Delay delay : delayList) {

			List <ArrivalDeparture> arriveDepartList = delay.getArrivalDeparture();
			
			if (arriveDepartList == null || arriveDepartList.isEmpty()) {
				gov.faa.airportstatus.schema.delays.ArriveDepart arriveDepartDelay = factory.createArriveDepart();
				arriveDepartDelay.setAirport(delay.getARPT().getContent());
				arriveDepartDelay.setReason(delay.getReason().getContent());
				summaryDelays.getArriveDepart().add(arriveDepartDelay);
				continue;
			}
			
			for (ArrivalDeparture ad : arriveDepartList) {				
				gov.faa.airportstatus.schema.delays.ArriveDepart arriveDepartDelay = factory.createArriveDepart();
				arriveDepartDelay.setAirport(delay.getARPT().getContent());
				arriveDepartDelay.setReason(delay.getReason().getContent());
				arriveDepartDelay.setMaxTime(ad.getMax().getContent());
				arriveDepartDelay.setMinTime(ad.getMin().getContent());
				summaryDelays.getArriveDepart().add(arriveDepartDelay);
			}
			
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Processed a total of "+summaryDelays.getArriveDepart().size()+" arrival and departure delays for major airports");

		return summaryDelays;
	}
	
	// process the airport closures found in the status information
	private Closures processClosures (AirportClosureList closureList) {

		if (logger.isDebugEnabled())
			logger.debug("Processing airport closures for delay summary");

		if (closureList == null) {
			Closures closures = factory.createClosures();
			closures.setCount(0);
			return closures;
		}
		
		List <Airport> airportClosures = closureList.getAirport();
		gov.faa.airportstatus.schema.delays.Closures closures = factory.createClosures();
		
		for (Airport airportClosure : airportClosures) {
			gov.faa.airportstatus.schema.delays.Closure closure = factory.createClosure();
			closure.setAirport(airportClosure.getARPT().getContent());
			closure.setReopen(airportClosure.getReopen().getContent());
			closure.setReason(airportClosure.getReason().getContent());
			closures.getClosure().add(closure);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Processed a total of "+closures.getClosure().size()+" airport closures for major airports");
		
		return closures;
	}
	
	// After all processing is done, we set the counts for each delay type and the overall number of delays across the country
	private void setStatus (Delays delays) {
	
		Status status = factory.createStatus();
		status.setCode(200);
		status.setInfo("OK");
		
		int adCount = delays.getArriveDepartDelays().getArriveDepart().size();
		delays.getArriveDepartDelays().setCount(adCount);
		
		int closeCount = delays.getClosures().getClosure().size();
		delays.getClosures().setCount(closeCount);
		
		int stopCount = delays.getGroundStops().getGroundStop().size();
		delays.getGroundStops().setCount(stopCount);
		
		int groundCount = delays.getGroundDelays().getGroundDelay().size();
		delays.getGroundDelays().setCount(groundCount);
		
		status.setCount(adCount+closeCount+stopCount+groundCount);
		delays.setStatus(status);
	}
}
