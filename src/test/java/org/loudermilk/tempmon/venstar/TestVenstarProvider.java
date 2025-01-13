package org.loudermilk.tempmon.venstar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class TestVenstarProvider {
	
	private VenstarProvider provider;
	
	private static MockWebServer mockWebServer;
	
	private String loginResponseString = """
			{ "AccessToken": "myaccesstoken", "RefreshToken": "myrefreshtoken", "ExpiresIn": 3600 }
			""";
	private String deviceResponseString = """
			[
{
"_id": "123456789",
"fwVer": "XPM-T2000-5.65-VEN",
"name": "THERMOSTAT",
"locationID": "66551637d901d60008f9c8ba",
"locationName": "Cabin",
"writable": true,
"external": false,
"live": {
  "activeAlerts": 0,
  "availableModes": 0,
  "away": 0,
  "cool1": 0,
  "cool2": 0,
  "coolSP": 131,
  "coolToDehum": 0,
  "dataSequence": 4601,
  "dayPart": 0,
  "dehumSp": 80,
  "emHeat": 0,
  "eqStatus": 0,
  "equipment_energizeFanOnHeat": false,
  "equipment_heatpump": false,
  "equipment_reversingValve": "O",
  "fanMode": 0,
  "fanStatus": 0,
  "fwVer": "XPM-T2000-5.65-VEN",
  "gasElecJp": 1,
  "heat1": 48,
  "heat2": 0,
  "heat3": 0,
  "heatSP": 102,
  "hpJp": 1,
  "humHi": 255,
  "humLo": 255,
  "humSp": 25,
  "humidity": 255,
  "internalHi": 103,
  "internalLo": 100,
  "internalTemp": 101,
  "maxOvercool": 4,
  "mode": 1,
  "name": "THERMOSTAT",
  "onHoliday": 0,
  "progOn": 0,
  "progType": 3,
  "recoveryActive": 0,
  "revValveJp": 1,
  "security": 0,
  "spDelta": 2,
  "spMax": 135,
  "spMin": 118,
  "spaceTemp": 101,
  "spaceTempMax": 103,
  "spaceTempMin": 100,
  "status": "connected",
  "units": 0,
  "updated": 1735918649710,
  "wiredTempHi": 255,
  "wiredTempLo": 255
}
}
]
			""";

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
		provider = new VenstarProvider();
		provider.setBaseUrl(String.format("http://localhost:%d/", mockWebServer.getPort()));
		provider.setEmailAddress("user@example.org");
		provider.setPassword("password");
		provider.setDeviceId("123456789");
	}

	@Test
	void testLogin() {
		mockWebServer.enqueue(new MockResponse().setBody(loginResponseString).addHeader("Content-Type", "text/plain")); // venstar responds with this
		mockWebServer.enqueue(new MockResponse().setBody(deviceResponseString).addHeader("Content-Type", "application/json"));
		provider.getTemperature();
		assertThat(provider.accessToken, is("myaccesstoken"));
		assertThat(provider.refreshToken, is("myrefreshtoken"));
	}
	
	@Test
	void testTokenRefresh() {
		// we're logged in, but the token expire time is in the past
		provider.accessToken = "accesstoken1";
		provider.refreshToken = "myrefreshtoken";
		provider.tokenExpireTime = 0L;
		String tokenRefreshResponseString = """
				{ "AccessToken": "accesstoken2", "ExpiresIn": 3600, "IdToken": "idtoken" }
				""";
		mockWebServer.enqueue(new MockResponse().setBody(tokenRefreshResponseString).addHeader("Content-Type", "application/json"));
		mockWebServer.enqueue(new MockResponse().setBody(deviceResponseString).addHeader("Content-Type", "application/json"));
		provider.getTemperature();
		assertThat(provider.accessToken, is("accesstoken2"));
	}
	
	@Test
	void testGetTemperature() {
		// we're logged in and the token hasn't expired
		provider.accessToken = "accesstoken1";
		provider.tokenExpireTime = Long.MAX_VALUE;
		mockWebServer.enqueue(new MockResponse().setBody(deviceResponseString).addHeader("Content-Type", "application/json"));
		assertThat(provider.getTemperature(), is((double) 51));
	}
}
