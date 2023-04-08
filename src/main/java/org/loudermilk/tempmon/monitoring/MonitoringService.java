package org.loudermilk.tempmon.monitoring;

import org.loudermilk.tempmon.weathercloud.WeathercloudClient;
import org.loudermilk.tempmon.weathercloud.model.Device;
import org.loudermilk.tempmon.weathercloud.model.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitoringService {
	
	private static Logger logger = LoggerFactory.getLogger(MonitoringService.class);

	@Autowired
	private WeathercloudClient client;
	
	@Autowired
	private NotificationService notificationService;
	
	@Value("${weathercloud.device.latitude}")
	private double deviceLatitude;
	
	@Value("${weathercloud.device.longitude}")
	private double deviceLongitude;
	
	@Value("${weathercloud.device.code}")
	private String deviceCode;
	
	@Value("${monitor.minimumTemperature}")
	private double minimumTemperature;
	
	@Value("${monitor.maximumAgeSeconds}")
	private int maximumAgeSeconds;
	
	private MonitoringState currentState = new MonitoringState(MonitoringState.Code.UNKNOWN, "not monitored yet");
	
	private MonitoringState previousState;
	
	private long lastStateChangeTimestamp = System.currentTimeMillis();
	
	@Scheduled(cron = "${monitor.cron}")
	void monitorTemperature() {
		logger.debug("fetching current temperature");
		MonitoringState newState;
		try {
			Device device = client.findDevice(deviceLatitude, deviceLongitude, deviceCode);
			Values values = device.getValues();
			double currentTemperature = (double) values.getIndoorTemperature() / 10 * 9/5 + 32;
			if (values.getSecondsSinceUpdate() > maximumAgeSeconds) {
				String message = String.format("temperature was last uploaded %d seconds ago", values.getSecondsSinceUpdate());
				newState = new MonitoringState(MonitoringState.Code.TOO_OLD, message, currentTemperature);
			} else if (currentTemperature < minimumTemperature) {
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
	
	@EventListener(ApplicationReadyEvent.class)
	void onStartup() {
		monitorTemperature();
	}

	public WeathercloudClient getClient() {
		return client;
	}

	public void setClient(WeathercloudClient client) {
		this.client = client;
	}

	public double getDeviceLatitude() {
		return deviceLatitude;
	}

	public void setDeviceLatitude(double deviceLatitude) {
		this.deviceLatitude = deviceLatitude;
	}

	public double getDeviceLongitude() {
		return deviceLongitude;
	}

	public void setDeviceLongitude(double deviceLongitude) {
		this.deviceLongitude = deviceLongitude;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
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

	public int getMaximumAgeSeconds() {
		return maximumAgeSeconds;
	}

	public void setMaximumAgeSeconds(int maximumAgeSeconds) {
		this.maximumAgeSeconds = maximumAgeSeconds;
	}
}
