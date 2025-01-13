package org.loudermilk.tempmon.venstar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

	@JsonProperty("_id")
	private String id;
	
	private Values live;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Values getLive() {
		return live;
	}

	public void setLive(Values live) {
		this.live = live;
	}

	public class Values {
		
		private int spaceTemp;
		
		private String status;

		public int getSpaceTemp() {
			return spaceTemp;
		}

		public void setSpaceTemp(int spaceTemp) {
			this.spaceTemp = spaceTemp;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}
}
