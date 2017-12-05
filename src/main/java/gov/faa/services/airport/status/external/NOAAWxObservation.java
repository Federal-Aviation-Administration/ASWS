package gov.faa.services.airport.status.external;


import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader; 

public class NOAAWxObservation {
	private String airportCode = new String();
	private AIRPORTSTATUSINFORMATION xmlDoc = null;
	final static Logger logger = LoggerFactory.getLogger(NOAAWxObservation.class);

	public NOAAWxObservation() { }
	
	public NOAAWxObservation(String airportCode) {
		this.airportCode = airportCode;
	}
	
	public String getAirportCode() {
		return airportCode;
	}

	public void setAirportCode(String airportCode) {
		this.airportCode = airportCode;
	}
	
	public Object getStatus(String url)  {
		long startTime = 0;
		
		if (airportCode == null || airportCode.equals("")) {
			if (logger.isWarnEnabled()) {
				logger.warn("Null or missing airport code. Unabled to retrieve weather observation from NOAA");
			}
			return null;
		}
		
		try {
			if (logger.isInfoEnabled()) {
				logger.info("Attempting to get weather observation for "+airportCode);
			}
			
			startTime = System.currentTimeMillis();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://apache.org/xml/features/validation/schema", false);
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			JAXBContext jc = JAXBContext.newInstance(gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION.class);
			Unmarshaller u = jc.createUnmarshaller();
			URL myUrl = new URL(url);
			URLConnection myURLConnection = myUrl.openConnection();
		    myURLConnection.connect();	
		    InputSource is = new InputSource(myURLConnection.getInputStream());
		    SAXSource source = new SAXSource (xmlReader, is);
		    xmlDoc = (AIRPORTSTATUSINFORMATION) u.unmarshal(source);
		}
		catch (JAXBException ejaxb) {
			logger.error(ejaxb.getMessage(), ejaxb);
			return new String ("Error: JAXB barfed.");
		}
		catch (MalformedURLException emalformed) {
			logger.error(emalformed.getMessage(), emalformed);
			return new String ("Error: URL"+url+" is malformed.");
		}
		catch (IOException eio) {
			logger.error(eio.getMessage(), eio);
			return new String("Error: I/O error accessing URL "+url);
		} catch (SAXNotRecognizedException e) {
			logger.error(e.getMessage(), e);
			return null; 
		} catch (SAXNotSupportedException e) {
			logger.error(e.getMessage(), e);
			return null; 
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage(), e);
			return null; 
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
			return null; 
		}
		
		long duration = System.currentTimeMillis() - startTime;

		if (logger.isInfoEnabled()) {
			logger.info("Retrieved weather observation for "+airportCode+" in "+duration+" ms");
		}
		return this.xmlDoc;
	}
	
	public String getStatusAsString () {
		try {
			JAXBContext jc = JAXBContext.newInstance(gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			m.marshal(xmlDoc,  boas);
			return boas.toString();
		}
		catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return "";
	}
	
}
