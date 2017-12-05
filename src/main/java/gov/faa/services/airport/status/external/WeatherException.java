package gov.faa.services.airport.status.external;

public class WeatherException extends Exception {
	
	public WeatherException () {
		super();
	}
	
	public WeatherException (String message) {
		super (message);
	}
	
	public WeatherException (String message, Throwable cause) {
		super (message, cause);
	}
	
	public WeatherException (Throwable cause) {
		this ("Error occurred retrieving weather observation", cause);
	}
	
	/*
	public int getErrorCode () {
		return errorCode;
	}
	
	public void setErrorCode(int code) {
		this.errorCode = code;
	}
	
	public void setStatusCode (int code) {
		this.statusCode = code;
	}
	
	public int getStatusCode () {
		return this.statusCode;
	}
	*/
}
