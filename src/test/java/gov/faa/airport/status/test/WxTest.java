package gov.faa.airport.status.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import gov.faa.services.airport.status.external.WxObservation;
import gov.faa.airportstatus.schema.wx.CurrentObservation;

public class WxTest {

	@Test
	public void retrieveWeatherObservation () {
		WxObservation wxobs = new WxObservation("KIAD");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherTeterboro () {
		WxObservation wxobs = new WxObservation("KTEB");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherLA () {
		WxObservation wxobs = new WxObservation("KLAX");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherDulles () {
		WxObservation wxobs = new WxObservation("IAD");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherLaguardia () {
		WxObservation wxobs = new WxObservation("LAG");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherChicago () {
		WxObservation wxobs = new WxObservation("ORD");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void retrieveWeatherMinneapolis () {
		WxObservation wxobs = new WxObservation("MSP");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void testEquality () {
		WxObservation wxIATA = new WxObservation("MSP");
		WxObservation wxICAO = new WxObservation("KMSP");
		try {
			wxIATA.getObservation();
			wxICAO.getObservation();
			
			byte [] array1 = wxIATA.getObservationAsString().getBytes();
			byte [] array2 = wxICAO.getObservationAsString().getBytes();
			
			assertTrue(Arrays.equals(array1, array2));
			
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void getAllAirports () {
		Object obj;
		try {
			obj = new WxObservation("ATL").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("BWI").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("BOS").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("CLT").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MDW").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("ORD").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("CVG").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("CLE").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("DFW").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("DEN").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("DTW").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("FLL").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("IAH").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("IND").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("LAS").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MCI").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("LAX").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MEM").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MIA").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MSP").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("BNA").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("EWR").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("JFK").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("LGA").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("MCO").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("PHL").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("PHX").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("PIT").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("PDX").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("RDU").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("SLC").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("SAN").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("SFO").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("SJC").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("SEA").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("STL").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("TPA").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("TEB").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("IAD").getObservation();
			assertNotNull(obj);
			obj = new WxObservation("DCA").getObservation();
			assertNotNull(obj);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}	
	
	@Test
	public void fuzz1 () {
		WxObservation wxobs = new WxObservation("F@TS");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void fuzz2 () {
		WxObservation wxobs = new WxObservation(" IAD ");
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
	
	@Test
	public void fuzz3 () {
		WxObservation wxobs = new WxObservation(null);
		try {
			CurrentObservation obs = wxobs.getObservation();
			assertNotNull(obs);
		}
		catch (Exception ex) {
			return;
		}
	}
}
