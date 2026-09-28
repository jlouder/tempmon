package org.loudermilk.tempmon.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name="monitor.notifier", havingValue="ntfy")
public class NtfyNotifier implements Notifier {
	
	private static Logger logger = LoggerFactory.getLogger(NtfyNotifier.class);
	
	@Value("${ntfy.topic}")
	private String topicName;
	
	@Value("${ntfy.priority:high}")
	private String priority;
	
	private final RestClient restClient = RestClient.create();
	
	public void notify(MonitoringState oldState, MonitoringState newState) {
		// See if this state change requires notification
		if (oldState.getCode() == newState.getCode()) {
			// nothing changed
			return;
		}
		if (oldState.getCode() == MonitoringState.Code.UNKNOWN &&
				newState.getCode() == MonitoringState.Code.OK) {
			// first check, and temp is okay
			return;
		}

		logger.info("old state: {}", oldState);
		logger.info("new state: {}", newState);
		logger.info("notifying topic: {}", topicName);

		String tag;
		if (newState.getCode() == MonitoringState.Code.OK) {
			tag = "green_circle";
		} else {
			tag = "red_circle";
		}
		
		String message = "Temperature is " + newState + " (was: " + oldState.getCode() + ")";
		
		String responseBody = restClient.post()
			.uri("https://ntfy.sh/" + topicName)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.header("Title", "Temperature alert")
			.header("Priority", priority)
			.header("Tags", tag)
			.body(message)
			.retrieve()
			.body(String.class);
		
		logger.info("Response: {}", responseBody);
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

}
