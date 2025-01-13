package org.loudermilk.tempmon.venstar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenRefreshRequest {

	@JsonProperty("refresh_token")
	private String refreshToken;

	public TokenRefreshRequest(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

}
