package org.loudermilk.tempmon.weathercloud;

public class DataTooOldException extends RuntimeException {

	private static final long serialVersionUID = -2209223551314111653L;

	public DataTooOldException(String message) {
		super(message);
	}
}
