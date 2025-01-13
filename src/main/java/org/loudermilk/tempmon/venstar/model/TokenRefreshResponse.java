package org.loudermilk.tempmon.venstar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenRefreshResponse {

	@JsonProperty("AccessToken")
	private String accessToken;
	
	@JsonProperty("IdToken")
	private String idToken;
	
	@JsonProperty("ExpiresIn")
	private int expiresIn;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
}
