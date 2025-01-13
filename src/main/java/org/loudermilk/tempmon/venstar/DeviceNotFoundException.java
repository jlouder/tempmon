package org.loudermilk.tempmon.venstar;

public class DeviceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 839239690569584312L;

	public DeviceNotFoundException(String message) {
		super(message);
	}

}
