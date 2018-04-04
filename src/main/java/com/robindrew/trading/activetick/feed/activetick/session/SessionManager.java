package com.robindrew.trading.activetick.feed.activetick.session;

import com.robindrew.trading.provider.activetick.platform.AtCredentials;

public class SessionManager implements SessionManagerMBean {

	private final AtCredentials credentials;

	public SessionManager(AtCredentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public String getUsername() {
		return credentials.getUsername();
	}

	@Override
	public String getApiKey() {
		return credentials.getApiKey();
	}

}
