package org.loudermilk.tempmon.weathercloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Values {

	private long epoch;
	
	@JsonProperty("temp")
	private int outdoorTemperature;
	
	@JsonProperty("tempin")
	private int indoorTemperature;

	public long getEpoch() {
		return epoch;
	}

	public void setEpoch(long epoch) {
		this.epoch = epoch;
	}

	public int getOutdoorTemperature() {
		return outdoorTemperature;
	}

	public void setOutdoorTemperature(int outdoorTemperature) {
		this.outdoorTemperature = outdoorTemperature;
	}

	public int getIndoorTemperature() {
		return indoorTemperature;
	}

	public void setIndoorTemperature(int indoorTemperature) {
		this.indoorTemperature = indoorTemperature;
	}

	@Override
	public String toString() {
		return "Values [epoch=" + epoch + ", outdoorTemperature=" + outdoorTemperature + ", indoorTemperature="
				+ indoorTemperature + "]";
	}
}
