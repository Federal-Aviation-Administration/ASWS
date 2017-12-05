package gov.faa.services.airport.status.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.jaxrs.config.BeanConfig;

//import gov.faa.services.airport.status.external.WxObservation;
@ApplicationPath ("/asws")
public class AirportStatusApp extends Application {
	final static Logger logger = LoggerFactory.getLogger(AirportStatusApp.class);
	
	/**
	 * Default constructor of the airport status web service app
	 */
	public AirportStatusApp () {
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setTitle("FAA Airport Status API");
		beanConfig.setVersion("1.0.0");
		beanConfig.setSchemes(new String [] {"https"});
		beanConfig.setHost("soa.smext.faa.gov");
		beanConfig.setBasePath("/asws");
		beanConfig.setResourcePackage("io.swagger.resources");
		beanConfig.setLicense("US Public Domain");
		beanConfig.setLicenseUrl("http://www.usa.gov/publicdomain/label/1.0/");
		beanConfig.setScan(true);
	}
	
	@Override
	/**
	 * Get the classes that are used as resources for this application.
	 */
	public Set<Class<?>> getClasses() {
		if (logger.isDebugEnabled())
			logger.debug("Resource classes being retrieved from AirportStatus jersey application.");
		Set<Class<?>> s = new HashSet <Class<?>>();
		s.add(gov.faa.services.airport.status.api.AirportStatus.class);
		s.add(gov.faa.services.airport.status.api.AirportStatusApplicationListener.class);
		s.add(io.swagger.jaxrs.listing.ApiListingResource.class);
		s.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
		
		return s;
	}	
}
