package org.loudermilk.tempmon.weathercloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

	private String code;
	
	private String name;
	
	@JsonProperty("update")
	private int secondsSinceUpdate;
	
	private Values values;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSecondsSinceUpdate() {
		return secondsSinceUpdate;
	}

	public void setSecondsSinceUpdate(int secondsSinceUpdate) {
		this.secondsSinceUpdate = secondsSinceUpdate;
	}

	public Values getValues() {
		return values;
	}

	public void setValues(Values values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "Device [code=" + code + ", name=" + name + ", secondsSinceUpdate=" + secondsSinceUpdate + ", values="
				+ values + "]";
	}

}
