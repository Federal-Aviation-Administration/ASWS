package gov.faa.services.airport.status.external;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress; 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
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

import gov.faa.airportstatus.schema.wx.CurrentObservation;
import gov.faa.services.airport.status.api.Config;

public class WxObservation {
	final static Logger logger = LoggerFactory.getLogger(WxObservation.class);
	private static Map<Class<?>, JAXBContext> contextStore = new ConcurrentHashMap<Class<?>, JAXBContext>();

	private String airportCode = new String();
	private String icao = new String();
	private CurrentObservation xmlDoc = null;
	
	URL noaa = null;
	
	public WxObservation() { 
		if (logger.isDebugEnabled())
			logger.debug("Constructed weather observation controller with no airport code assigned");
		this.setAirportCodes(airportCode.toUpperCase());
		this.buildURL();
	}
	
	public WxObservation(String code) {
		
		if (code == null) {	
			code = new String();
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Constructed weather observation controller using airport code "+code);
		this.airportCode = code.toUpperCase();

		this.setAirportCodes(code);
		this.buildURL();
	}
	
	public String getAirportCode() {
		return airportCode;
	}

	public void setAirportCodes(String code) {
		
		this.airportCode = code.toUpperCase();
		
		switch (airportCode.length()) {
		case 4:
			this.icao = new String (airportCode);
			break;
		case 3:
			this.icao = "K"+airportCode;
			break;
		default:
			this.icao = new String("UNK");
			break;
			
		}
			
		if (logger.isDebugEnabled())
			logger.debug ("Weather observation controller set airport code as "+icao);
		
	}
	
	private void buildURL() {
		
		 //String baseUrl = AirportStatusConfig.config.getProperty("gov.faa.wx.base.url");
		 try {
			 this.noaa = new URL(Config.getWXBaseURL()+icao+".xml");
			 //this.noaa = new URL(baseUrl+"K"+airportCode+".xml");
		 }
		 catch (MalformedURLException emalformed) {
			 noaa = null;
			 emalformed.printStackTrace();
		 }
		 if (logger.isInfoEnabled())
			 logger.info("Using URL "+noaa.toExternalForm()+" to retrieve weather observation.");
	}
	
	public CurrentObservation getObservation() throws WeatherException {
		HttpURLConnection myURLConnection = null;
		int httpStatus = 200;
		Proxy proxy = null;
		long startTime = System.currentTimeMillis();
	
		logger.info("Starting weather observation retrieval for airport "+icao);
		
		if (airportCode == null) {
			logger.warn("Airport code is null. Unable to retrieve weather observation.");
			throw new WeatherException ("Airport not specified.");
		}
		
		// Setup the proxy first
		try {
			if (Config.getFAADMZProxyHost() == null || Config.getFAADMZProxyHost().equals("NONE")) {
				logger.info("No proxy detected. Using direct connection to NOAA");
				proxy = null;
			}
			else {
				if (logger.isInfoEnabled())
					logger.info("Connecting to the proxy using "+Config.getFAADMZProxyHost()+":"+Config.getFAADMZProxyPort());
				InetSocketAddress proxySocket = new InetSocketAddress(Config.getFAADMZProxyHost(),Config.getFAADMZProxyPort());
				proxy = new Proxy (Proxy.Type.HTTP, proxySocket);
			}
		}
		catch (NullPointerException enullProxy) {
			proxy = null;
		}
		catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to resolve the proxy or illegal argument on proxy port number. Attempted to use "+Config.getFAADMZProxyHost()+":"+Config.getFAADMZProxyPort());
				logger.warn("Not using proxy. Attempting to use direct connection instead.");
			}
			proxy = null;
		}		
		
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://apache.org/xml/features/validation/schema", false);
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			JAXBContext jc = getContextInstance(gov.faa.airportstatus.schema.wx.CurrentObservation.class);
			Unmarshaller u = jc.createUnmarshaller();
			if (proxy != null) {
				// use proxy host
				if (logger.isDebugEnabled())
					logger.debug("Using proxy host to retrieve weather data from weather.gov.");
				 myURLConnection = (HttpURLConnection) noaa.openConnection(proxy);
			}
			else {
				// direct connection
				if (logger.isDebugEnabled())
					logger.debug("No proxy deteced. Direct connect to weather.gov");
				 myURLConnection = (HttpURLConnection) noaa.openConnection();				
			}
		    myURLConnection.connect();	
		    if (logger.isInfoEnabled())
		    	logger.info("Connected to weather.gov and attempting to parse weather observation.");
		    
		    InputSource is = new InputSource(myURLConnection.getInputStream());
		    SAXSource source = new SAXSource (xmlReader, is);
		    JAXBElement <CurrentObservation> root = u.unmarshal(source, CurrentObservation.class);
		    
		    xmlDoc = root.getValue();
		    if (logger.isInfoEnabled())
		    	logger.info("Weather observation parsing complete. Retrieved root element of weather document.");
		    
		    httpStatus = myURLConnection.getResponseCode();
		    
		    if (logger.isDebugEnabled()) 
		    	logger.debug("Disconnecting from www.weather.gov");
		    myURLConnection.disconnect();
		}
		catch (JAXBException ejaxb) {
			logger.error(ejaxb.getMessage());
			if (logger.isErrorEnabled())
				logger.error("Error occurred while parsing weather response for airport "+icao, ejaxb);
			throw new WeatherException ("Error occurred while parsing weather response for airport "+icao, ejaxb);
		}
		catch (MalformedURLException emalformed) {
			logger.error(emalformed.getMessage());
			if (logger.isErrorEnabled())
				logger.error("The weather observation URL is malformed for airport "+icao, emalformed);
			throw new WeatherException ("The weather observation URL is malformed for airport "+icao, emalformed);
		}
		catch (IOException eio) {
			if (logger.isErrorEnabled())
				logger.error("I/O Error occurred while retrieving weather observation for airport "+icao, eio);
			throw new WeatherException ("I/O Error occurred while retrieving weather observation for airport "+icao, eio);
		} catch (SAXNotRecognizedException e) {
			if (logger.isErrorEnabled())
				logger.error("Error occurred while parsing weather response for airport "+icao, e);
			throw new WeatherException ("Error occurred while parsing weather response for airport "+icao, e);
		} catch (SAXNotSupportedException esaxnotsupported) {
			if (logger.isErrorEnabled())
				logger.error("Error occurred while parsing weather response for airport "+icao, esaxnotsupported);
			throw new WeatherException ("Error occurred while parsing weather response for airport "+icao, esaxnotsupported);
		} catch (ParserConfigurationException eparse) {
			if (logger.isErrorEnabled())
				logger.error("Error occurred while parsing weather response for airport "+icao, eparse);
			throw new WeatherException ("Error occurred while parsing weather response for airport "+icao, eparse);
		} catch (SAXException esax) {
			if (logger.isErrorEnabled())
				logger.error("SAX Error occurred while parsing weather response for airport "+icao, esax);
			throw new WeatherException ("SAX Error occurred while parsing weather response for airport "+icao, esax); 
		}
		
		long duration = System.currentTimeMillis() - startTime;
		
		if (logger.isDebugEnabled())
			logger.debug("URL "+noaa.toExternalForm()+" returned status code "+httpStatus);
		
		logger.info("Retrieved weather observation with HTTP status "+httpStatus+" for airport "+icao+" in "+duration+" ms.");
		return this.xmlDoc;
	}
	
	public String getObservationAsString () throws WeatherException {
		
		if (xmlDoc == null) {
			logger.warn("xmlDocument for weather observation is null. Unable to process weather.");
			throw new WeatherException ("Failed to retrieve weather and cannot convert to a String. Weather is null");
		}
		
		try {
			JAXBContext jc = getContextInstance(gov.faa.airportstatus.schema.wx.CurrentObservation.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			m.marshal(xmlDoc,  boas);
			return boas.toString();
		}
		catch (Exception ex) {
			logger.error("Parse of weather observation failed. Likely cause was a null response from weather.gov", ex);
			throw new WeatherException ("Unable to convert the weather to a string", ex);
		}
		
	}

	protected static JAXBContext getContextInstance(Class<?> objectClass) throws JAXBException {
		  JAXBContext context = contextStore.get(objectClass);
		  if (context==null){
		    context = JAXBContext.newInstance(objectClass);
		    contextStore.put(objectClass, context);
		  }
		  return context;
	}
}
