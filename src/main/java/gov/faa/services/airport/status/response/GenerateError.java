package gov.faa.services.airport.status.response;

import gov.faa.airportstatus.schema.response.AirportStatus;
import gov.faa.airportstatus.schema.response.ObjectFactory;
import gov.faa.airportstatus.schema.response.Status;
import gov.faa.services.airport.data.AirportData;
import gov.faa.services.airport.data.AirportInformation;

public class GenerateError {
	String airport = "UNK";
	ObjectFactory of = new ObjectFactory();
	AirportStatus response = of.createAirportStatus();
	String reason = "UNKNOWN";
	int code = 404;
	
	public GenerateError (String message, String airport) {
		this.reason = message;
		this.airport = airport;
	}
	
	public GenerateError (int code, String message) {
		this.reason = message;
		this.code = code;
	}
	
	public AirportStatus getResponse () {
		
		AirportInformation airportData = AirportData.getData(airport);
		response = of.createAirportStatus();
		Status status = of.createStatus();
	
		if (airportData == null) {
			response.setIATA(airport);
			response.setCity("UNKNOWN");
			response.setState("UNKNOWN");
			response.setName("UNKNOWN");
			response.setSupportedAirport(false);
			Status unknown = of.createStatus();
			unknown.setReason("Delays are available only for major United States airports");
			response.getStatus().add(unknown);
			status.setReason(code+" "+reason);
			response.setDelay(false);
			response.setDelayCount(0);
			response.getStatus().add(status);
		}
		else {
			response.setIATA(airport);
			response.setICAO("K"+airport);
			response.setName(airportData.getName());
			response.setCity(airportData.getCity());
			response.setState(airportData.getState());
			status.setAvgDelay("");
			status.setClosureBegin("");
			status.setClosureEnd("");
			status.setEndTime("");
			status.setMaxDelay("");
			status.setMinDelay("");
			status.setTrend("");
			status.setType("");
			response.getStatus().add(status);
		}
		
		return response;
	}
}
