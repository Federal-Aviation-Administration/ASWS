package gov.faa.services.airport.status.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static final Logger logger  = LoggerFactory.getLogger(Config.class);
	private static final Properties cfg = new Properties();
	private static final String FAA_DMZ_PROXY_HOST=null;
	private static final String FAA_DMZ_PROXY_PORT="8080";	
	private static final String WX_BASE_URL = "http://w1.weather.gov/xml/current_obs/";
	// http://www.fly.faa.gov/flyfaa/xmlAirportStatus.jsp
	private static final String FLY_FAA_BASE_URL = "http://www.fly.faa.gov/flyfaa/xmlAirportStatus.jsp";

	private Config () { }
	
	static {
		Config.loadConfig();
	}
	
	/**
	 * Load the configuration properties from the standard location on disk. The location of the properties file is controlled through the Ansible deploy of APRA.
	 * Ansible guarantees that the file is created and will exist with appropriate values per environment. 
	 */
	public static void loadConfig () {
		if (logger.isDebugEnabled())
			logger.debug("Setting configuration of properties for proxy server and URLs");
		
		if (logger.isDebugEnabled())
			logger.debug("Attempting load of properties resources");
		
		try {
			FileInputStream fis = new FileInputStream (new File("/opt/asws/conf/config.properties"));
			cfg.load(fis);	
			logger.info("Configuration properties loaded successfully from /opt/asws/conf/config.properties");
		}
		catch (IOException eio) {
			logger.warn("Using default configuration properties. File /opt/asws/conf/config.properties not found.", eio);
		}
	}
	
	/**
	 * Get the DMZ proxy host name. 
	 * @return FQDN of the proxy server
	 */
	public static String getFAADMZProxyHost () {
		return cfg.getProperty("gov.faa.dmz.proxy.host", FAA_DMZ_PROXY_HOST);
	}
	
	/**
	 * Get the DMZ proxy port number
	 * @return proxy server port
	 */
	public static int getFAADMZProxyPort () {
		String intValue = cfg.getProperty("gov.faa.dmz.proxy.port", FAA_DMZ_PROXY_PORT);
		return Integer.valueOf(intValue);
	}
	
	public static String getWXBaseURL () {
		return cfg.getProperty("gov.wx.base.url", WX_BASE_URL);
	}
	
	public static String getFlyFAABaseURL () {
		return cfg.getProperty("gov.faa.fly.base.url", FLY_FAA_BASE_URL);
	}
	
}
