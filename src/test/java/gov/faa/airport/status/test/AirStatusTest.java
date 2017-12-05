package gov.faa.airport.status.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import gov.faa.services.airport.status.api.AirportStatus;

//import gov.faa.airportstatus.schema.AIRPORTSTATUSINFORMATION;
//import gov.faa.services.airport.status.external.FlyFAAAccess;

public class AirStatusTest {
	@Test
	public void retrieveUnknown () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("XYZ");
			assertNotNull(status);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void retrieveDulles () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("IAD");
			assertNotNull(status);
			assertEquals(status.getIATA(), "IAD");
			assertEquals(status.getICAO(), "KIAD");
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void retrieveReagan () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("DCA");
			assertNotNull(status);
			assertEquals(status.getIATA(), "DCA");
			assertEquals(status.getICAO(), "KDCA");
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void retrieveJFK () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("JFK");
			assertNotNull(status);
			assertEquals(status.getIATA(), "JFK");
			assertEquals(status.getICAO(), "KJFK");
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void retrieveLouisville () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("SDF");
			assertNotNull(status);
			assertEquals(status.getIATA(), "SDF");
			assertEquals(status.getStatus().get(0).getReason(), "No known delays for this airport");
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void retrieveTeterboro () {

		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = restApi.getAirportStatus("TEB");
			assertNotNull(status);
			assertEquals(status.getIATA(), "TEB");
			assertEquals(status.getICAO(), "KTEB");
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void getAllAirports () {
		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = null;
			status = restApi.getAirportStatus("ATL");
			assertNotNull(status);
			status = restApi.getAirportStatus("BWI");
			assertNotNull(status);
			status = restApi.getAirportStatus("BOS");
			assertNotNull(status);
			status = restApi.getAirportStatus("CLT");
			assertNotNull(status);
			status = restApi.getAirportStatus("MDW");
			assertNotNull(status);
			status = restApi.getAirportStatus("ORD");
			assertNotNull(status);
			status = restApi.getAirportStatus("CVG");
			assertNotNull(status);
			status = restApi.getAirportStatus("CLE");
			assertNotNull(status);
			status = restApi.getAirportStatus("DFW");
			assertNotNull(status);
			status = restApi.getAirportStatus("DEN");
			assertNotNull(status);
			status = restApi.getAirportStatus("DTW");
			assertNotNull(status);
			status = restApi.getAirportStatus("FLL");
			assertNotNull(status);
			status = restApi.getAirportStatus("IAH");
			assertNotNull(status);
			status = restApi.getAirportStatus("IND");
			assertNotNull(status);
			status = restApi.getAirportStatus("LAS");
			assertNotNull(status);
			status = restApi.getAirportStatus("MCI");
			assertNotNull(status);
			status = restApi.getAirportStatus("LAX");
			assertNotNull(status);
			status = restApi.getAirportStatus("MEM");
			assertNotNull(status);
			status = restApi.getAirportStatus("MIA");
			assertNotNull(status);
			status = restApi.getAirportStatus("MSP");
			assertNotNull(status);
			status = restApi.getAirportStatus("BNA");
			assertNotNull(status);
			status = restApi.getAirportStatus("EWR");
			assertNotNull(status);
			status = restApi.getAirportStatus("JFK");
			assertNotNull(status);
			status = restApi.getAirportStatus("LGA");
			assertNotNull(status);
			status = restApi.getAirportStatus("MCO");
			assertNotNull(status);
			status = restApi.getAirportStatus("PHL");
			assertNotNull(status);
			status = restApi.getAirportStatus("PHX");
			assertNotNull(status);
			status = restApi.getAirportStatus("PIT");
			assertNotNull(status);
			status = restApi.getAirportStatus("PDX");
			assertNotNull(status);
			status = restApi.getAirportStatus("RDU");
			assertNotNull(status);
			status = restApi.getAirportStatus("SLC");
			assertNotNull(status);
			status = restApi.getAirportStatus("SAN");
			assertNotNull(status);
			status = restApi.getAirportStatus("SFO");
			assertNotNull(status);
			status = restApi.getAirportStatus("SJC");
			assertNotNull(status);
			status = restApi.getAirportStatus("SEA");
			assertNotNull(status);
			status = restApi.getAirportStatus("STL");
			assertNotNull(status);
			status = restApi.getAirportStatus("TPA");
			assertNotNull(status);
			status = restApi.getAirportStatus("TEB");
			assertNotNull(status);
			status = restApi.getAirportStatus("IAD");
			assertNotNull(status);
			status = restApi.getAirportStatus("DCA");
			assertNotNull(status);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void fuzz1 () {
		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = null;
			status = restApi.getAirportStatus("IAD ");
			assertNotNull(status);
			assertNotNull(status.getWeather());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}

	@Test
	public void fuzz2 () {
		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = null;
			status = restApi.getAirportStatus("FUBAR");
			assertNotNull(status);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void fuzz3 () {
		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = null;
			status = restApi.getAirportStatus("F@#");
			assertNotNull(status);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
	
	@Test
	public void fuzz4 () {
		try {
			AirportStatus restApi = new AirportStatus();
			gov.faa.airportstatus.schema.response.AirportStatus status = null;
			status = restApi.getAirportStatus(null);
			assertNotNull(status);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
}
