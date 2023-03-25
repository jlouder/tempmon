package org.loudermilk.tempmon.weathercloud;

public class DeviceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -7741335541922507551L;

	public DeviceNotFoundException(String message) {
		super(message);
	}

}
