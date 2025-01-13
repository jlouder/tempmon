package org.loudermilk.tempmon.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitoringService {
	
	private static Logger logger = LoggerFactory.getLogger(MonitoringService.class);

	@Autowired
	private TemperatureProvider provider;
	
	@Autowired
	private NotificationService notificationService;
	
	@Value("${monitor.minimumTemperature}")
	private double minimumTemperature;
	
	private MonitoringState currentState = new MonitoringState(MonitoringState.Code.UNKNOWN, "not monitored yet");
	
	private MonitoringState previousState;
	
	private long lastStateChangeTimestamp = System.currentTimeMillis();
	
	@Scheduled(fixedRateString = "${monitor.rate}")
	void monitorTemperature() {
		logger.debug("fetching current temperature");
		MonitoringState newState;
		try {
			double currentTemperature = provider.getTemperature();
			if (currentTemperature < minimumTemperature) {
				newState = new MonitoringState(MonitoringState.Code.TOO_LOW, currentTemperature);
			} else {
				newState = new MonitoringState(MonitoringState.Code.OK, currentTemperature);
			}
		} catch (Exception e) {
			logger.error("error fetching current temperature", e);
			newState = new MonitoringState(MonitoringState.Code.ERROR, e.toString());
		}
		
		if (currentState.getCode() != newState.getCode()) {
			previousState = currentState;
			lastStateChangeTimestamp = System.currentTimeMillis();
			notificationService.notify(currentState, newState);
		}
		
		currentState = newState;
		logger.debug("current state: {}", currentState);
	}
	
	public TemperatureProvider getProvider() {
		return provider;
	}

	public void setProvider(TemperatureProvider provider) {
		this.provider = provider;
	}

	public double getMinimumTemperature() {
		return minimumTemperature;
	}

	public void setMinimumTemperature(double minimumTemperature) {
		this.minimumTemperature = minimumTemperature;
	}

	public MonitoringState getCurrentState() {
		return currentState;
	}
	
	public MonitoringState getPreviousState() {
		return previousState;
	}

	public long getLastStateChangeTimestamp() {
		return lastStateChangeTimestamp;
	}
}
