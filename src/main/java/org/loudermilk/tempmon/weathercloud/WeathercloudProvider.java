package org.loudermilk.tempmon.weathercloud;

import java.net.URI;
import java.util.List;

import org.loudermilk.tempmon.monitoring.TemperatureProvider;
import org.loudermilk.tempmon.weathercloud.model.Device;
import org.loudermilk.tempmon.weathercloud.model.DeviceList;
import org.loudermilk.tempmon.weathercloud.model.Values;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name="monitor.temperatureProvider", havingValue="weathercloud")
public class WeathercloudProvider implements TemperatureProvider {

	@Value("${weathercloud.nearbyDevicesUrl}")
	private String nearbyDevicesUrl;
	
	@Value("${weathercloud.device.latitude}")
	private double deviceLatitude;
	
	@Value("${weathercloud.device.longitude}")
	private double deviceLongitude;
	
	@Value("${weathercloud.device.code}")
	private String deviceCode;
	
	@Value("${weathercloud.maximumAgeSeconds}")
	private int maximumAgeSeconds;
	
	private List<Device> findNearbyDevices(double latitude, double longitude, int withinMiles) {
		URI uri = UriComponentsBuilder.fromUriString(nearbyDevicesUrl)
		.build(latitude, longitude, withinMiles);
		String response = WebClient.create()
			.get()
			.uri(uri)
			.accept(MediaType.APPLICATION_JSON)
			.header("X-Requested-With", "XMLHttpRequest")
			.retrieve()
			.bodyToMono(String.class)
			.block();
		DeviceList deviceList;
		try {
			deviceList = new ObjectMapper().readValue(response, DeviceList.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return deviceList.getDevices();
	}
	
	private Device findDevice(double latitude, double longitude, String deviceCode) {
		for (Device device : findNearbyDevices(latitude, longitude, 1)) {
			if (device.getCode().equals(deviceCode)) {
				return device;
			}
		}
		throw new DeviceNotFoundException("device " + deviceCode + " not found");
	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5000, multiplier = 2))
	@Override
	public double getTemperature() {
		Device device = findDevice(deviceLatitude, deviceLongitude, deviceCode);
		Values values = device.getValues();
		if (values.getSecondsSinceUpdate() > maximumAgeSeconds) {
			throw new DataTooOldException(
					"temperature not updated since " + values.getSecondsSinceUpdate() + " seconds ago");
		}
		return (double) values.getIndoorTemperature() / 10 * 9/5 + 32;
	}

	public String getNearbyDevicesUrl() {
		return nearbyDevicesUrl;
	}

	public void setNearbyDevicesUrl(String nearbyDevicesUrl) {
		this.nearbyDevicesUrl = nearbyDevicesUrl;
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

	public int getMaximumAgeSeconds() {
		return maximumAgeSeconds;
	}

	public void setMaximumAgeSeconds(int maximumAgeSeconds) {
		this.maximumAgeSeconds = maximumAgeSeconds;
	}
}
