package org.loudermilk.tempmon.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Disabled("this test sends email")
public class TestNotificationService {

	private NotificationService service;
	
	@BeforeEach
	public void beforeEachTest() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp");
		mailSender.setPort(25);
		service = new NotificationService();
		service.setMailSender(mailSender);
		service.setEmailRecipients(new String[]{"joel@loudermilk.org"});
	}
	
	@Test
	public void testNotify() {
		MonitoringState oldState = new MonitoringState(MonitoringState.Code.OK, 75);
		MonitoringState newState = new MonitoringState(MonitoringState.Code.ERROR, "UNIT TESTING!!!");
		service.notify(oldState, newState);
	}
}
