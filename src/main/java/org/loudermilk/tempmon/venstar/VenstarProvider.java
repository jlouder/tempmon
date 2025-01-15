package org.loudermilk.tempmon.venstar;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.loudermilk.tempmon.monitoring.TemperatureProvider;
import org.loudermilk.tempmon.venstar.model.Device;
import org.loudermilk.tempmon.venstar.model.LoginRequest;
import org.loudermilk.tempmon.venstar.model.LoginResponse;
import org.loudermilk.tempmon.venstar.model.TokenRefreshRequest;
import org.loudermilk.tempmon.venstar.model.TokenRefreshResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
@ConditionalOnProperty(name="monitor.temperatureProvider", havingValue="venstar")
public class VenstarProvider implements TemperatureProvider {
	
	Logger logger = LoggerFactory.getLogger(VenstarProvider.class);

	@Value("${venstar.baseUrl}")
	private String baseUrl;
	
	@Value("${venstar.emailAddress}")
	private String emailAddress;
	
	@Value("${venstar.password}")
	private String password;
	
	@Value("${venstar.deviceId}")
	private String deviceId;
	
	@Value("${venstar.userAgent:Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0}")
	private String userAgent;
	
	private WebClient webClient;
	String accessToken;
	String  refreshToken;
	long tokenExpireTime;
	
	public VenstarProvider() {
		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		webClient = WebClient.builder()
				.filter(forceJsonContentType())
				.filter(logRequest())
				.filter(logResponse())
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
	
	private void login() {
		LoginRequest loginRequest = new LoginRequest(emailAddress, password);
		URI uri = UriComponentsBuilder.fromUriString(baseUrl).pathSegment("users", "auth", "login").build().toUri();
		LoginResponse loginResponse = webClient
			.post()
			.uri(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.USER_AGENT, userAgent)
			.bodyValue(loginRequest)
			.retrieve()
			.bodyToMono(LoginResponse.class)
			.block();
		Assert.notNull(loginResponse, "login response must not be null");
		accessToken = loginResponse.getAccessToken();
		refreshToken = loginResponse.getRefreshToken();
		tokenExpireTime = System.currentTimeMillis() + loginResponse.getExpiresIn() * 1000L;
	}
	
	private void refreshAccessToken() {
		TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(refreshToken);
		URI uri = UriComponentsBuilder.fromUriString(baseUrl).pathSegment("users", "auth", "token").build().toUri();
		TokenRefreshResponse tokenRefreshResponse = webClient
				.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.USER_AGENT, userAgent)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.bodyValue(tokenRefreshRequest)
				.retrieve()
				.bodyToMono(TokenRefreshResponse.class)
				.block();
		Assert.notNull(tokenRefreshResponse, "token refresh response must not be null");
		accessToken = tokenRefreshResponse.getAccessToken();
		tokenExpireTime = System.currentTimeMillis() + tokenRefreshResponse.getExpiresIn() * 1000L;
	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5000, multiplier = 2))
	@Override
	public double getTemperature() {
		if (accessToken == null) {
			login();
		} else if (System.currentTimeMillis() > tokenExpireTime) {
			refreshAccessToken();
		}
		URI uri = UriComponentsBuilder.fromUriString(baseUrl).pathSegment("devices")
				.queryParam("owner", "false")
				.queryParam("includeLiveData", "true")
				.build().toUri();
		List<Device> devices = webClient
			.get()
			.uri(uri)
			.accept(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.USER_AGENT, userAgent)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.bodyToFlux(Device.class)
			.collectList()
			.block();
		Assert.notNull(devices, "device list must not be null");
		Map<String, Device> devicesById = devices.stream()
			.collect(Collectors.toMap(Device::getId, Function.identity()));
		Device device = devicesById.get(deviceId);
		if (device == null) {
			throw new DeviceNotFoundException("device " + deviceId + " not found in response");
		}
		return (double) device.getLive().getSpaceTemp() - 50;
	}
	
	private ExchangeFilterFunction forceJsonContentType() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> 
        Mono.just(clientResponse.mutate()
            .headers(headers -> headers.remove(HttpHeaders.CONTENT_TYPE))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()));
	}
	
	private ExchangeFilterFunction logRequest() {
	    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
	    	logger.debug("{} {}", clientRequest.method(), clientRequest.url());
	        return Mono.just(clientRequest);
	    });
	}

	private ExchangeFilterFunction logResponse() {
	    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
	    	logger.debug("{}", clientResponse.statusCode());
	        return Mono.just(clientResponse);
	    });
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

}
