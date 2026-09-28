package org.loudermilk.tempmon.monitoring;

public interface Notifier {
	
	public void notify(MonitoringState oldState, MonitoringState newState);

}
