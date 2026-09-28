package org.loudermilk.tempmon.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

@Disabled("this test sends email")
class TestNtfyNotifier {

	private NtfyNotifier service;
	
	@BeforeEach
	public void beforeEachTest() {
		service = new NtfyNotifier();
//		service.setTopicName("my-topic-name");
		service.setPriority("high");
	}
	
	@Test
	void testNotifyError() {
		MonitoringState oldState = new MonitoringState(MonitoringState.Code.OK, 75);
		MonitoringState newState = new MonitoringState(MonitoringState.Code.ERROR, "UNIT TESTING!!!");
		service.notify(oldState, newState);
	}

	@Test
	void testNotifyOk() {
		MonitoringState oldState = new MonitoringState(MonitoringState.Code.ERROR, "too low!");
		MonitoringState newState = new MonitoringState(MonitoringState.Code.OK, "UNIT TESTING!!!");
		service.notify(oldState, newState);
	}
}
