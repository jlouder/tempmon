package org.loudermilk.tempmon.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.loudermilk.tempmon.weathercloud.WeathercloudClient;
import org.loudermilk.tempmon.weathercloud.model.Device;
import org.loudermilk.tempmon.weathercloud.model.Values;

@Component
public class MonitoringService {
	
	private static Logger logger = LoggerFactory.getLogger(MonitoringService.class);

	@Autowired
	private WeathercloudClient client;
	
	@Value("${weathercloud.device.latitude}")
	private double deviceLatitude;
	
	@Value("${weathercloud.device.longitude}")
	private double deviceLongitude;
	
	@Value("${weathercloud.device.code}")
	private String deviceCode;
	
	@Value("${monitor.minimumTemperature}")
	private double minimumTemperature;
	
	@Value("${monitor.alertEmailAddresses")
	private String[] alertEmailAddresses;
	
	private MonitoringState currentState = new MonitoringState(MonitoringState.Code.UNKNOWN, "not monitored yet");
	
	private long lastStateChangeTimestamp = System.currentTimeMillis();
	
	@Scheduled(cron = "${monitor.cron}")
	void monitorTemperature() {
		logger.debug("fetching current temperature");
		MonitoringState newState;
		try {
			Device device = client.findDevice(deviceLatitude, deviceLongitude, deviceCode);
			Values values = device.getValues();
			double currentTemperature = (double) values.getIndoorTemperature() / 10 * 9/5 + 32;
			if (currentTemperature < minimumTemperature) {
				newState = new MonitoringState(MonitoringState.Code.BELOW_THRESHOLD, currentTemperature);
			} else {
				newState = new MonitoringState(MonitoringState.Code.OK, currentTemperature);
			}
		} catch (Exception e) {
			logger.error("error fetching current temperature", e);
			newState = new MonitoringState(MonitoringState.Code.ERROR, e.toString());
		}
		
		if (currentState.getCode() != newState.getCode()) {
			lastStateChangeTimestamp = System.currentTimeMillis();
			notify(currentState, newState, lastStateChangeTimestamp);
		}
		
		currentState = newState;
		logger.debug("current state: {}", currentState);
	}

	void notify(MonitoringState oldState, MonitoringState newState, long lastStateChangeTimestamp) {
		// See if this state change requires notification
		if (oldState.getCode() == newState.getCode()) {
			// nothing changed
			return;
		}
		if (oldState.getCode() == MonitoringState.Code.UNKNOWN &&
				newState.getCode() == MonitoringState.Code.OK) {
			// first check, and temp is okay
			return;
		}
		// TODO send email
		List<String> emailAddresses = Arrays.stream(alertEmailAddresses).collect(Collectors.toList());
		logger.info("old state: {}", oldState);
		logger.info("new state: {}", newState);
		logger.info("send email to: {}", emailAddresses);
	}
	
	@PostConstruct
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

	public String[] getAlertEmailAddresses() {
		return alertEmailAddresses;
	}

	public void setAlertEmailAddresses(String[] alertEmailAddresses) {
		this.alertEmailAddresses = alertEmailAddresses;
	}

	public MonitoringState getCurrentState() {
		return currentState;
	}

	public long getLastStateChangeTimestamp() {
		return lastStateChangeTimestamp;
	}
}
