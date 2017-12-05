package gov.faa.services.airport.status.external;

public class AirportStatusException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public AirportStatusException () {
		super();
	}
	
	public AirportStatusException (String message) {
		super (message);
	}
	
	public AirportStatusException (String message, Throwable cause) {
		super (message, cause);
	}
	
	public AirportStatusException (Throwable cause) {
		this ("Error occurred retrieving weather observation", cause);
	}
}
