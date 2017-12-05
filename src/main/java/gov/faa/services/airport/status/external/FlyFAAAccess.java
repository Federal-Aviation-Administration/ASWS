package gov.faa.services.airport.status.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
//import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION;
import gov.faa.services.airport.status.api.Config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader; 

/**
 * This class obtains the current airport status from the Fly FAA public web pages as an XML stream. The XML is bound to JAXB objects
 * which are returned through the @see getStatus method call. It is up to the caller to tell us which airport to use. If an airport
 * is not specified, this may fail.
 * @author FAA
 *
 */

public class FlyFAAAccess {
	
	final static Logger logger = LoggerFactory.getLogger(FlyFAAAccess.class);
	private static Map<Class<?>, JAXBContext> contextStore = new ConcurrentHashMap <Class<?>, JAXBContext> ();
	private URL flyFAA = null;
	private AIRPORTSTATUSINFORMATION asi = null;
	private JAXBElement <AIRPORTSTATUSINFORMATION> root = null;
	private String iata = new String();
	private String icao = new String();
	
	public FlyFAAAccess() { 
		if (logger.isDebugEnabled())
			logger.debug("Obtaining access to the FLY FAA xml airport status.");
		buildURL();
	}
	
	public FlyFAAAccess(String airportCode) {
		if (logger.isDebugEnabled())
			logger.debug("Obtaining access to the FLY FAA xml airport status.");
		setAirportCodes(airportCode);
		buildURL();
	}
	
	public String getIATA() {
		return iata;
	}

	public void setIATA(String iata) {
		this.iata = iata.toUpperCase();
	}
	
	public String getICAO() {
		return icao;
	}

	public void setICAO(String icao) {
		this.icao = icao.toUpperCase();
	}
	
	public void setAirportCodes(String airportCode) {
		
		if (airportCode == null) {
			airportCode = new String();
		}
		
		if (airportCode.startsWith("K") && airportCode.length() == 4) {
			this.icao = new String (airportCode);
			this.iata = new String (icao.substring(1));
		}
		else {
			this.iata = new String (airportCode);
			this.icao = new String ("K"+airportCode);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Set airport IATA code to "+iata);
			logger.debug("Set airport ICAO code to "+icao);
		}
	}
	
	/*
	 * Build the URL we will use to get the airport status. This is based up a static string FQDN, context path, and the IATA code
	 * The IATA code is extracted from the servlet path and is the final element of the path
	 */
	
	// http://www.fly.faa.gov/flyfaa/xmlAirportStatus.jsp
	private void buildURL() {
		 try {
		 	this.flyFAA = new URL(Config.getFlyFAABaseURL()+"?ARPT="+iata);
		 	if (logger.isDebugEnabled()) {
		 		logger.debug("Accessing airport status using "+flyFAA.toExternalForm());
		 	}
		 }
		 catch (MalformedURLException emalformed) {
			 flyFAA = null;
			 logger.error(emalformed.getMessage());
			 if (logger.isWarnEnabled())
				 logger.warn("Error encountered when constructing the URL to call the FLy FAA airport status page.", emalformed);
		}
		 
		if (logger.isDebugEnabled()) 
			logger.debug("Constructed URL "+flyFAA.toExternalForm()+" to call for airport status");
		 
	}
	
	/*
	 * Due to some issues with JAXB, we are forced to strip the DOCTYPE XML directive from the page that is returned by the 
	 * Fly FAA status request. The DOCTYPE is filtered from the response and the filtered response is placed in an output
	 * buffer for use by JAXB. By doing this, we are temporarily holding the response in memory so we don't have to call
	 * the URL twice. 
	 */
	private byte [] stripDoctype () {
		
		InputStream is = null;
		ByteArrayOutputStream os = null;
		Proxy proxy = null;
		HttpURLConnection myURLConnection = null;
				
		if (flyFAA == null) { 
			return null;
		}
		
		// Setup the proxy first
		try {
			if (Config.getFAADMZProxyHost() == null || Config.getFAADMZProxyHost().equals("NONE")) {
				proxy = null;
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Using proxy server "+Config.getFAADMZProxyHost()+":"+Config.getFAADMZProxyPort()+" to call "+flyFAA.toExternalForm());
				}
				InetSocketAddress proxySocket = new InetSocketAddress(Config.getFAADMZProxyHost(), Config.getFAADMZProxyPort());
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
		
		// Now try our connection to the URL and get an output stream we can use to pull the page response
		try {
			if (proxy != null) {
				// use proxy host
				if (logger.isDebugEnabled())
					logger.debug("Using proxy host to retrieve airport data from flyfaa.faa.gov.");
				 myURLConnection = (HttpURLConnection) flyFAA.openConnection(proxy);
			}
			else {
				// direct connection
				if (logger.isDebugEnabled())
					logger.debug("No proxy deteced. Direct connect to flyfaa.faa.gov");
				 myURLConnection = (HttpURLConnection) flyFAA.openConnection();				
			}

			// Start the clock on getting the HTTP response from fly.faa.gov. Intentionally not including the proxy setup here.
			long startTime = System.currentTimeMillis();

			myURLConnection.connect();
			is = myURLConnection.getInputStream();
			os = new ByteArrayOutputStream();
			int httpStatus = myURLConnection.getResponseCode();
			
			if (logger.isInfoEnabled())
				logger.info("Obtained response from www.fly.faa.gov with HTTP status "+httpStatus+" in "+(System.currentTimeMillis()-startTime)+" ms.");

		}
		catch (Exception ex) {
			logger.error("Error when attempting to get information from flyfaa at URL "+flyFAA.toString());
			logger.error(ex.getMessage());
			if (logger.isDebugEnabled()) 
				logger.debug("Attempt to get the Fly FAA xml page response failed.", ex);
			return null;
		}
		
		// Assuming we have gotten this far, we attempt to filter the page response using the XML streams API. Essentially, we
		// discard the DOCTYPE event and do not write it to the buffer output using a custom filter (static class) embedded in this
		// class
		try {
			if (logger.isDebugEnabled())
				logger.debug("Attempting to filter Fly FAA xml page response.");
			XMLInputFactory inFactory = XMLInputFactory.newFactory();
			XMLOutputFactory outFactory = XMLOutputFactory.newFactory();
			inFactory.setProperty(XMLInputFactory.IS_VALIDATING,Boolean.FALSE);
			inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			inFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
			
			if (logger.isDebugEnabled())
				logger.debug("Obtained XML factory objects to create filters.");
			XMLEventReader input = inFactory.createXMLEventReader(is);
			if (logger.isDebugEnabled())
				logger.debug("Created new XML event reader for Fly FAA input using HTTP input stream. About to create filtered XML event reader.");
			XMLEventReader filtered = inFactory.createFilteredReader(input, new DTDFilter());
			if (logger.isDebugEnabled())
				logger.debug("Created new XML event reader with a filter to remove the DTD declaration from the Fly FAA xml response. Now creating the output writer.");
			XMLEventWriter output = outFactory.createXMLEventWriter(os);
			if (logger.isDebugEnabled())
				logger.debug("XML output event writer created. Next step is to add the filter to the output stream.");
			
			output.add(filtered);
			if (logger.isDebugEnabled())
				logger.debug("Filter added to output event writer. About to flush the output writer.");
			output.flush();
			if (logger.isDebugEnabled())
				logger.debug("Ouptut writer flushed. Now returning a byte array from the output stream.");
			return os.toByteArray();
		}
		catch (Exception ex2) {
			logger.error(ex2.getMessage());
			if (logger.isDebugEnabled())
				logger.debug ("Error occurred when filtering XML airport status.", ex2);
			return null;
		}
		
	}
	
	/**
	 * Get the airport status information from the Fly FAA url on the FAA network. This method unmarshals the response into
	 * a set of JAXB objects that match the DTD/schema of the Fly FAA URL.
	 * @return AIRPORTSTATUSINFORMATION including latest airport status from across the country
	 */
	public AIRPORTSTATUSINFORMATION getStatus() throws AirportStatusException {
		AIRPORTSTATUSINFORMATION xmlDoc = null;
		byte [] xdoc = null;
		long startTime = System.currentTimeMillis();
		
		if (logger.isDebugEnabled())
			logger.debug("Starting to get airport status information for airport "+icao);
		
		// check the flyFAA response. If it is null, then we return a null response and move on. Buyer beware here. 
		//TODO consider changing this to throw an exception so the caller doesn't attempt a null pointer access
		if (flyFAA == null) {
			return null;
		}
		
		// strip that pesky DOCTYPE directive from the response and store the buffer into xdoc
		xdoc = stripDoctype();
		
		//TODO here again decide if we should toss an exception rather than returning a null to prevent a NPE 
		if (xdoc == null) {		
			return null;
		}
		
		// The big try-catch block with tons of stuff that can go wrong at any moment. 
		// We are using the underlying SAX parser that is included in the API packages for Java. Most likely xerces
		// Get the XML reader and construct an input source using the array buffer of clean XML that has been stripped
		// of the offensive DOCTYPE directive. Finally, get the root element from JAXB and return it to the caller
		// so the caller may walk the XML tree
		try {
			if (logger.isDebugEnabled())
				logger.debug("Attempting to parse response from fly.faa.gov");
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://apache.org/xml/features/validation/schema", false);
			spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
			JAXBContext jc = getContextInstance(gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION.class);
			//JAXBContext jc = JAXBContext.newInstance(gov.faa.airportstatus.schema.AIRPORTSTATUSINFORMATION.class);
			Unmarshaller u = jc.createUnmarshaller();
	
		    InputSource is = new InputSource(new ByteArrayInputStream(xdoc));
		    SAXSource source = new SAXSource (xmlReader, is);
		    
		    if (logger.isDebugEnabled())
		    	logger.debug("All unmarshaller objects created. Ready to unmarshal the XML response from fly.faa.gov");
		    
		    root = u.unmarshal(source, AIRPORTSTATUSINFORMATION.class);
		 
		    xmlDoc = root.getValue();
		}
		catch (JAXBException ejaxb) {
			logger.error(ejaxb.getMessage());
			if (logger.isWarnEnabled()) 
				logger.warn("Error occurred during airport status retrieval and parsing.", ejaxb);
			throw new AirportStatusException ("Could not understand Fly FAA airport status response");
		} catch (SAXNotRecognizedException esaxnotfound) {
			logger.error(esaxnotfound.getMessage());
			if (logger.isWarnEnabled()) 
				logger.warn("Error occurred during airport status retrieval and parsing.", esaxnotfound);
			throw new AirportStatusException ("An error occurred during airport status retrieval and parsing.");
		} catch (SAXNotSupportedException esax) {
			logger.error(esax.getMessage());
			if (logger.isWarnEnabled()) 
				logger.warn("Error occurred during airport status retrieval and parsing.", esax);
			throw new AirportStatusException ("An error occurred during airport status retrieval and parsing.");
		} catch (ParserConfigurationException eparse) {
			logger.error(eparse.getMessage());
			if (logger.isWarnEnabled()) 
				logger.warn("Error occurred during airport status retrieval and parsing.", eparse);
			throw new AirportStatusException ("An error occurred during airport status retrieval and parsing.");
		} catch (SAXException esax) {
			logger.error(esax.getMessage());
			if (logger.isWarnEnabled()) 
				logger.warn("Error occurred during airport status retrieval and parsing.", esax);
			throw new AirportStatusException ("An error occurred during airport status retrieval and parsing.");
		}
		this.asi = xmlDoc;
		long duration = System.currentTimeMillis() - startTime;
		if (logger.isInfoEnabled())
			logger.info("Retrieved airport status information for airport "+icao+" in "+duration+" ms.");
		return asi;
	}
	
	
	public String getStatusAsString () {
		String response = new String();
		try {
			if (asi == null) {
				getStatus();
			}
			
			JAXBContext jc = getContextInstance(gov.faa.airportstatus.schema.asi.AIRPORTSTATUSINFORMATION.class);
			//JAXBContext jc = JAXBContext.newInstance(gov.faa.airportstatus.schema.AIRPORTSTATUSINFORMATION.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			m.marshal(root,  boas);
			response = boas.toString();
		}
		catch (Exception ex) {
			response = ex.getMessage();
			logger.error(ex.getMessage());
			if (logger.isDebugEnabled()) 
				logger.debug("Error occurred while attempting to marshal response status to string", ex);
		}
		
		return response;
	}
	

	/*
	 * Here is our special XML filter designed specifically to throw out the bad DOCTYPE directive. Would be nice if 
	 * I could figure out how to get the SAX parser to ignore the directive. I attempted to use the various parser features, but
	 * it didn't seem to want to work. 
	 */
	static class DTDFilter implements EventFilter {
		@Override
		public boolean accept (XMLEvent event) {
			
			if (event.getEventType() == XMLStreamConstants.DTD) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found DOCTYPE declaration for removal: "+event.toString());
				}
			}
			
			return event.getEventType() != XMLStreamConstants.DTD;
		}
	}
	
	protected static JAXBContext getContextInstance(Class<?> objectClass) throws JAXBException{
		  JAXBContext context = contextStore.get(objectClass);
		  if (context==null){
		    context = JAXBContext.newInstance(objectClass);
		    contextStore.put(objectClass, context);
		  }
		  return context;
	}
}
