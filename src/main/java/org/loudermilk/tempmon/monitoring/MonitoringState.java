package org.loudermilk.tempmon.monitoring;

public class MonitoringState {
	
	private Code code;
	
	private String message;
	
	private Double temperature;
	
	private long timestamp;
	
	public enum Code {
		UNKNOWN, ERROR, OK, TOO_LOW
	}

	public MonitoringState(Code code, String message) {
		super();
		this.code = code;
		this.message = message;
		timestamp = System.currentTimeMillis();
	}

	public MonitoringState(Code code, double temperature) {
		super();
		this.code = code;
		this.temperature = temperature;
		timestamp = System.currentTimeMillis();
	}

	public MonitoringState(Code code, String message, double temperature) {
		super();
		this.code = code;
		this.message = message;
		this.temperature = temperature;
		timestamp = System.currentTimeMillis();
	}

	public Code getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Double getTemperature() {
		return temperature;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(code.toString());
		sb.append(": ");
		if (message == null) {
			sb.append(String.format("%.1f degrees", temperature));
		} else if (temperature == null) {
			sb.append(message);
		} else {
			sb.append(String.format("%.1f degrees", temperature));
			sb.append(", ");
			sb.append(message);
		}
		return sb.toString();
	}
	
}
