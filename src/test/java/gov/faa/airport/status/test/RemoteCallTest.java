package gov.faa.airport.status.test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;

import org.junit.Test;

// This is a simple test class that shows how to use the JAX-RS API to call the Airport Status service
public class RemoteCallTest {

	
	@Test
	public void getDullesProd () {
		String response = new String("Empty"); 
		
		try {
			
			String token = getAuthorization();
			
			assertNotNull(token);
				
			Client client = ClientBuilder.newClient();
			
			// The target for external services is soa.smext.faa.gov. The URI path depends on the service being called, obviously
			WebTarget webTarget = client.target("https://soa.smext.faa.gov/api/airport/status");
			WebTarget dulles = webTarget.path("IAD");
		
			// The media type is optional. Here we specify JSON, but could have also specified APPLICATION_XML instead
			Invocation.Builder invoke = dulles.request(MediaType.APPLICATION_JSON);
			
			// The Authorization header must be obtained from the OAuth authentication / identity provider.
			// This is for illustrative purposes only.
			invoke.header("Authorization", "Bearer "+token);
			response = invoke.get(String.class); 
			System.out.println(response);
		}
		catch (Exception exGet) {
			exGet.printStackTrace();
			System.out.println(response);
		}
	}
	
	public String getAuthorization () {
		String token = new String ();
		String emptyJson = new String ("{}");
		Client client = ClientBuilder.newClient();
		
		// Setup the target of the API gateway server that issues tokens
		WebTarget base = client.target("https://soa.smext.faa.gov/auth/oauth/v2/token");
		// Here we supply the client identifier and secret as provided by the 
		// API registrar
		WebTarget oauth = base.queryParam("client_id", "ADE320@faa.gov")
				.queryParam("client_secret", "770260e9-5584-4270-905f-efea7713c1b5")
				.queryParam("grant_type", "client_credentials");
		
		
		Invocation.Builder invoke = oauth.request(MediaType.APPLICATION_JSON);
		
		// Have to make sure the request is a POST as the GET verb is not supported
		String response = invoke.post(Entity.entity(emptyJson, MediaType.APPLICATION_JSON), String.class);

		JsonReader jr = Json.createReader(new StringReader(response.toString()));
		JsonObject jobject = jr.readObject();
		
		// Finally get the token out of the JSON response string
		token = jobject.getString("access_token");
	
		return token;		
	}
}
