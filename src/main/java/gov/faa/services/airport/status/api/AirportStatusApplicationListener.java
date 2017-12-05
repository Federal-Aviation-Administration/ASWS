package gov.faa.services.airport.status.api;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirportStatusApplicationListener implements ApplicationEventListener {
	final static Logger logger = LoggerFactory.getLogger(AirportStatusApplicationListener.class);
	private volatile int requestCount = 0;
	@Override
	public void onEvent (ApplicationEvent appEvent) {
		switch (appEvent.getType()) {
			case INITIALIZATION_FINISHED:
				logger.info("AirportStatus web service (ASWS) started. Ready to service requests.");
				break;
			default:
				break;
		}
	}
	
	@Override
	public RequestEventListener onRequest (RequestEvent requestEvent) {
		requestCount++;
		return new AirportStatusEventListener(requestCount);
	}
	
	public static class AirportStatusEventListener implements RequestEventListener {
		
		private final int requestNumber;
		private final long startTime;
		final static Logger logger = LoggerFactory.getLogger(AirportStatusApplicationListener.class);
		
		public AirportStatusEventListener (int requestNumber) {
			this.requestNumber = requestNumber;
			this.startTime = System.currentTimeMillis();
		}
		
		@Override
		public void onEvent (RequestEvent event) {
			switch (event.getType()) {
			case RESOURCE_METHOD_START:
				ThreadContext.put("ID", "ASWS-"+Integer.toString(requestNumber));
				logger.info("Resource method "+event.getUriInfo().getMatchedResourceMethod().getHttpMethod()+" started for request "+requestNumber);
				break;
			case FINISHED:
				logger.info("Request ASWS-"+requestNumber+" finished. Processing time "+ (System.currentTimeMillis() - startTime) + " ms.");
				ThreadContext.clearAll();
				break;
			default:
				break;
			}
		}
	
	}

}
