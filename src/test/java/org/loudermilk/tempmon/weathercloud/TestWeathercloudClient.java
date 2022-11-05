package org.loudermilk.tempmon.weathercloud;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.loudermilk.tempmon.weathercloud.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWeathercloudClient {
	
	private static Logger logger = LoggerFactory.getLogger(TestWeathercloudClient.class);
	
	private WeathercloudClient client;
	
	@BeforeEach
	public void beforeEachTest() {
		client = new WeathercloudClient();
		client.setNearbyDevicesUrl("https://app.weathercloud.net/page/coordinates/latitude/{latitude}/longitude/{longitude}/distance/{distance}");
	}

	@Test
	public void testFindNearbyDevices() {
		double latitude = -105.5119818;
		double longitude = 40.3690963;
		List<Device> devices = client.findNearbyDevices(latitude, longitude, 1);
		devices.forEach(d -> logger.info("found device: {}", d));
	}
}
