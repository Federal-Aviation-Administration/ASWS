package gov.faa.services.airport.data;

public class AirportInformation {

	private String iata;
	private String name;
	private String city;
	private String state;
	
	public AirportInformation (String code) {
		this.iata = code;
	}
	
	public AirportInformation(String code, String name, String city, String state) {
		this.iata = code;
		this.name = name;
		this.city = city;
		this.state = state;
	}

	public String getIATA () {
		return iata;
	}
	
	public void setIATA(String code) {
		this.iata = code;
	}
	
	public String getName () {
		return name;
	}
	
	public void setName (String airportName) {
		this.name = airportName; 
	}
	
	public String getCity () {
		return city;
	}
	
	public void setCity (String airportCity) {
		this.city = airportCity;
	}
	
	public String getState () {
		return state;
	}
	
	public void setState (String airportState) {
		this.city = airportState;
	}
}
