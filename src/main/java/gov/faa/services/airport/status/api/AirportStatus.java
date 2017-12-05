package gov.faa.services.airport.status.api;

import gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION;
import gov.faa.airportstatus.schema.delays.Delays;
import gov.faa.airportstatus.schema.delays.ObjectFactory;
import gov.faa.airportstatus.schema.delays.Status;
import gov.faa.airportstatus.schema.wx.CurrentObservation;


import gov.faa.services.airport.status.external.AirportStatusException;
import gov.faa.services.airport.status.external.FlyFAAAccess;
import gov.faa.services.airport.status.external.WeatherException;
import gov.faa.services.airport.status.external.WxObservation;
import gov.faa.services.airport.status.response.GenerateResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import gov.faa.services.airport.status.response.GenerateDelaySummary;
import gov.faa.services.airport.status.response.GenerateError;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;

/**
 * Root resource (exposed at "airport" path)
 */
@Path("/airport")
@Api(value="FAA Airport Status Service")

public class AirportStatus {
	final static Logger logger = LoggerFactory.getLogger(AirportStatus.class);

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/xml" or "application/json" media type.
     * The response is determined by what the calling application requests through the 
     * HTTP Accept: header. The default response is application/xml unless the client
     * sends the Accept: application/json header in the HTTP request. 
     *
     * @return String that will be returned as a text/plain response.
     */

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/status/{airportCode}")
    @ApiOperation(value="Get airport status based on path parameter provided on the API call. The path parameter is an IATA airport code.", 
	notes="The airport status is retrieved from fly.faa.gov and is provided in a format that can be used by applications that require airport status data."
			+ "Available airports include the following: BOS, LGA, TEB, EWR, JFK, PHL, PIT, IAD, BWI, DCA, RDU, CLT, ATL, MCO, TPA, "
			+ "MCO, FLL, MIA, DTW, CLE, MDW, ORD, IND, CVG, BNA, MEM, STL, MCI, MSP, DFW, IAH, "
			+ "DEN, SLC, PHX, LAS, SAN, LAX, SJC, SFO, PDX, SEA")
    
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Delays are available only for major United States airports")})

    public gov.faa.airportstatus.schema.response.AirportStatus getAirportStatus(@PathParam("airportCode") String airportCode) {
		
    	gov.faa.airportstatus.schema.response.AirportStatus response = null;
    	AIRPORTSTATUSINFORMATION asi = null;
    	CurrentObservation wx = null;
    	
    	
    	try { 
    		if (airportCode != null) {
    			airportCode = airportCode.trim();
    			airportCode = airportCode.toUpperCase();
    		}
 
    		if (logger.isInfoEnabled())
    			logger.info("Getting status for airport: "+airportCode);
    		asi = new FlyFAAAccess(airportCode).getStatus();
    	}
    	catch (AirportStatusException eStatus) {
    		logger.error(eStatus.getMessage());
    		asi = null;
    		GenerateError error = new GenerateError (eStatus.getMessage(), airportCode);
    		response = error.getResponse();
    		return response;
    	}
    	
    	try {
    		if (logger.isInfoEnabled())
    			logger.info("Getting weather observation for airport: "+airportCode);		
      		wx = new WxObservation(airportCode).getObservation();
    	}
    	catch (WeatherException ex) {
    		logger.error("Failed to get weather for airport: "+airportCode);
    		logger.error(ex.getMessage());
    		wx = null;   		
    		GenerateError errorResponse = new GenerateError(ex.getMessage(), airportCode);
    		return errorResponse.getResponse();
    	}
    	
		GenerateResponse gen = new GenerateResponse(asi, wx, airportCode);
		response = gen.getResponse();
    	
    	return response;
    }

    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/xml" or "application/json" media type.
     * The response is determined by what the calling application requests through the 
     * HTTP Accept: header. The default response is application/xml unless the client
     * sends the Accept: application/json header in the HTTP request. 
     *
     * @return String that will be returned as a text/plain response.
     */

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/delays")
    @ApiOperation(value="Get airport delay summary for major US national airports.", 
	notes="The airport status is retrieved from fly.faa.gov and is provided in a format that can be used by applications that require airport status data."
			+ "Delays will be returned for the following airports: BOS, LGA, TEB, EWR, JFK, PHL, PIT, IAD, BWI, DCA, RDU, CLT, ATL, MCO, TPA, "
			+ "MCO, FLL, MIA, DTW, CLE, MDW, ORD, IND, CVG, BNA, MEM, STL, MCI, MSP, DFW, IAH, "
			+ "DEN, SLC, PHX, LAS, SAN, LAX, SJC, SFO, PDX, SEA")
    
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Airport delays from http://www.fly.faa.gov are unavailable"),
		@ApiResponse(code = 500, message = "Airport information from http://www.fly.faa.gov returned an error response"),
		})

    public Response getDelays() {
    	ObjectFactory factory = new ObjectFactory();
    	AIRPORTSTATUSINFORMATION asi = null;   	
    	
    	try { 
    		if (logger.isInfoEnabled())
    			logger.info("Getting delays summary report.");
    		asi = new FlyFAAAccess().getStatus();
    		
    		if (asi == null) {
    			logger.error("Unable to obtain fly.faa.gov airport status");
    			Delays delays = factory.createDelays();
    			Status status = factory.createStatus();
    			status.setCode(404);
    			status.setCount(0);
    			status.setInfo("Airport delays from http://www.fly.faa.gov are unavailable");
    			delays.setStatus(status);
    	    	return Response.status(delays.getStatus().getCode()).entity(delays).build();
    		}
    	}
    	catch (AirportStatusException eStatus) {
    		logger.error(eStatus.getMessage());
    		asi = null;
			Delays delays = factory.createDelays();
			Status status = factory.createStatus();
			status.setCode(500);
			status.setCount(0);
			status.setInfo("Airport information from http://www.fly.faa.gov returned an error response");
			delays.setStatus(status);
	    	return Response.status(delays.getStatus().getCode()).entity(delays).build();
    	}
    	
		GenerateDelaySummary gen = new GenerateDelaySummary(asi);

		Delays delays = gen.getResponse();
		
		if (logger.isInfoEnabled())
			logger.info("Retrieved delays and returning response to request.");
    	return Response.status(delays.getStatus().getCode()).entity(delays).build();

    }

}