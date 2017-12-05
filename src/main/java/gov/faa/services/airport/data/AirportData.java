package gov.faa.services.airport.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import gov.faa.services.airport.data.AirportInformation;

public class AirportData {

	private static final Map<String, AirportInformation> airportMap;
	
	static {
		AirportInformation ap;
		airportMap = new ConcurrentHashMap <String, AirportInformation> ();
		ap = new AirportInformation ("ATL", "Hartsfield-Jackson International", "Atlanta", "Georgia");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("BWI", "Baltimore-Washington International", "Baltimore", "Maryland");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("BOS", "Boston-Logan International", "Boston", "Massachuttsets");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("CLT", "Charlotte/Douglas International", "Charlotte", "North Carolina");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MDW", "Chicago Midway", "Chicago", "Illinois");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("ORD", "Chicago O'Hare International", "Chicago", "Illinois");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("CVG", "Cincinnati/Northern Kentucky International", "Cincinnati", "Ohio");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("CLE", "Cleveland-Hopkins International", "Cleveland", "Ohio");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("DFW", "Dallas/Fort Worth International", "Dallas", "Texas");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("DEN", "Denver International", "Denver", "Colorado");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("DTW", "Detroit Metropolitan Wayne County", "Detroit", "Michigan");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("FLL", "Fort Lauderdale/Hollywood International", "Fort Lauderdale", "Florida");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("IAH", "Houston George Bush Intercontinental", "Houston", "Texas");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("IND", "Indianapolis International", "Indianapolis", "Indiana");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("LAS", "Las Vegas McCarran International", "Las Vegas", "Nevada");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MCI", "Kansas City International", "Kansas City", "Missouri");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("LAX", "Los Angeles International", "Los Angeles", "California");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MEM", "Memphis International", "Memphis", "Tennessee");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MIA", "Miami International", "Miami", "Florida");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MSP", "Minneapolis-St. Paul International", "Minneapolis", "Minnesota");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("BNA", "Nashville International", "Nashville", "Tennessee");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("EWR", "Newark International", "Newark", "New Jersey");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("JFK", "New York John F Kennedy International", "New York", "New York");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("LGA", "New York Laguardia International", "New York", "New York");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("MCO", "Orlando International", "Orlando", "Florida");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("PHL", "Philadelphia International", "Philadelphia", "Pennsylvania");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("PHX", "Phoenix Sky Harbor International", "Phoenix", "Arizona");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("PIT", "Pittsburgh International", "Pittsburgh", "Pennsylvania");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("PDX", "Portland International", "Portland", "Oregon");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("RDU", "Raliegh-Durham International", "Morrisville", "North Carolina");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("SLC", "Salt Lake City International", "Salt Lake", "Utah");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("SAN", "San Diego International Lindbergh Field", "San Diego", "California");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("SFO", "San Francisco International", "San Francisco", "California");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("SJC", "San Jose International", "San Jose", "California");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("SEA", "Seattle Tacoma International", "Seattle", "Washington");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("STL", "St Louis Lambert-International", "St. Louis", "Missouri");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("TPA", "Tampa International", "Tampa", "Florida");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("TEB", "Teterboro International", "Teterboro", "New Jersey");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("IAD", "Washington Dulles International", "Washington", "District of Columbia");
		airportMap.put(ap.getIATA(), ap);
		ap = new AirportInformation ("DCA", "Washington National Reagan International", "Washington", "District of Columbia");
		airportMap.put(ap.getIATA(), ap);

	}
	
	public static AirportInformation getData (String code) {
		if (code == null) 
			return new AirportInformation ("UNK", "UNKNOWN", "UNKNOWN", "UNKNOWN");
		
		return airportMap.get(code);
	}
}
