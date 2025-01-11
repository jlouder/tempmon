package org.loudermilk.tempmon.monitoring;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {
	
	private static Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${email.recipients}")
	private String[] emailRecipients;
	
	void notify(MonitoringState oldState, MonitoringState newState) {
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
		List<String> emailAddresses = Arrays.stream(emailRecipients).toList();
		logger.info("old state: {}", oldState);
		logger.info("new state: {}", newState);
		logger.info("sending email to: {}", emailAddresses);
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(emailRecipients);
		message.setSubject("Temperature alert");
		message.setText("Temperature is " + newState + " (was: " + oldState.getCode() + ")");
		try {
			mailSender.send(message);
		} catch (MailException e) {
			logger.error("failed to send mail", e);
		}
		logger.info("sent email to: {}", emailAddresses);
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public String[] getEmailRecipients() {
		return emailRecipients;
	}

	public void setEmailRecipients(String[] emailRecipients) {
		this.emailRecipients = emailRecipients;
	}

}
