package org.loudermilk.tempmon.weathercloud;

import java.net.URI;
import java.util.List;

import org.loudermilk.tempmon.weathercloud.model.Device;
import org.loudermilk.tempmon.weathercloud.model.DeviceList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WeathercloudClient {

	@Value("${weathercloud.nearbyDevicesUrl}")
	private String nearbyDevicesUrl;
	
	public List<Device> findNearbyDevices(double latitude, double longitude, int withinMiles) {
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
	
	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5000, multiplier = 2))
	public Device findDevice(double latitude, double longitude, String deviceCode) {
		for (Device device : findNearbyDevices(latitude, longitude, 1)) {
			if (device.getCode().equals(deviceCode)) {
				return device;
			}
		}
		throw new DeviceNotFoundException("device " + deviceCode + " not found");
	}

	public String getNearbyDevicesUrl() {
		return nearbyDevicesUrl;
	}

	public void setNearbyDevicesUrl(String nearbyDevicesUrl) {
		this.nearbyDevicesUrl = nearbyDevicesUrl;
	}
}
