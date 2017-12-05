package gov.faa.airport.status.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import gov.faa.airportstatus.schema.delays.Delays;
import gov.faa.services.airport.status.api.AirportStatus;

public class DelaysTest {

	@Test
	public void defaultTest () {

		try {
			AirportStatus restApi = new AirportStatus();
			Delays d = (Delays) restApi.getDelays().getEntity();
			assertEquals(200, d.getStatus().getCode());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail (ex.getMessage());
		}
	}
}
