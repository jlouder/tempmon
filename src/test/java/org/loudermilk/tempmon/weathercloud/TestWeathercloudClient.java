package org.loudermilk.tempmon.weathercloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.loudermilk.tempmon.weathercloud.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class TestWeathercloudClient {
	
	private static Logger logger = LoggerFactory.getLogger(TestWeathercloudClient.class);
	
	private WeathercloudClient client;
	
	private static MockWebServer mockWebServer;
	
	private String responseString = "{\"devices\":[{\"type\":\"device\",\"code\":\"5703705358\",\"name\":\"Altamonte\",\"city\":\"Altamonte Springs\",\"latitude\":\"28.681533300065336\",\"longitude\":\"-81.38532400131226\",\"elevation\":\"230\",\"image\":\"/images/map/sidebox/device-default-background.png\",\"account\":0,\"isFavorite\":false,\"update\":454,\"values\":{\"epoch\":\"1679765923\",\"tempin\":\"232\",\"temp\":\"307\",\"dew\":\"221\",\"chill\":\"307\",\"heat\":\"342\",\"humin\":\"45\",\"hum\":\"60\",\"bar\":\"10152\",\"wdir\":\"215\",\"wspd\":\"2\",\"wspdavg\":\"2\",\"wspdhi\":\"10\",\"rainrate\":\"0\",\"rain\":\"0\",\"solarrad\":\"7521\",\"uvi\":\"70\",\"wdiravg\":\"215\"},\"data\":\"1.33\"},{\"type\":\"device\",\"code\":\"1614024241\",\"name\":\"Logia Weather Station\",\"city\":\"Altamonte Springs\",\"latitude\":\"28.68129121282701\",\"longitude\":\"-81.38039458670391\",\"elevation\":\"271\",\"image\":\"/images/map/sidebox/device-default-background.png\",\"account\":0,\"isFavorite\":false,\"update\":483,\"values\":{\"epoch\":\"1679765894\",\"bar\":\"10141\",\"wdir\":\"186\",\"wspd\":\"10\",\"wspdhi\":\"10\",\"rainrate\":\"0\",\"rain\":\"0\",\"wspdavg\":\"9\",\"wdiravg\":\"186\",\"temp\":\"302\",\"chill\":\"302\",\"hum\":\"56\",\"heat\":\"323\",\"dew\":\"205\",\"tempin\":\"267\",\"humin\":\"52\"},\"data\":\"1.55\"}]}";
	private double longitude = -81.390358;
	private double latitude = 28.670401;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
	
	@BeforeEach
	public void beforeEachTest() {
		client = new WeathercloudClient();
		client.setNearbyDevicesUrl(String.format(
				"http://localhost:%d/page/coordinates/latitude/{latitude}/longitude/{longitude}/distance/{distance}",
				mockWebServer.getPort()));
	}

	@Test
	void testFindNearbyDevices() {
		mockWebServer.enqueue(new MockResponse().setBody(responseString).addHeader("Content-Type", "application/json"));
		List<Device> devices = client.findNearbyDevices(latitude, longitude, 1);
		devices.forEach(d -> logger.info("found device: {}", d));
		assertThat(devices).hasSize(2);
		assertThat(devices.stream().map(Device::getCode).toList()).containsExactlyInAnyOrder("5703705358", "1614024241");
	}
	
	@Test
	void testFindDeviceThatExists() {
		mockWebServer.enqueue(new MockResponse().setBody(responseString).addHeader("Content-Type", "application/json"));
		Device device = client.findDevice(latitude, longitude, "1614024241");
		assertThat(device).hasFieldOrPropertyWithValue("code", "1614024241");
	}

	@Test
	void testFindDeviceThatDoesNotExist() {
		mockWebServer.enqueue(new MockResponse().setBody(responseString).addHeader("Content-Type", "application/json"));
		assertThatExceptionOfType(DeviceNotFoundException.class).isThrownBy(() -> {
			client.findDevice(latitude, longitude, "99999999");
		});
	}
}
