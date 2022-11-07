package org.loudermilk.tempmon.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.loudermilk.tempmon.monitoring.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoringController {
	
	@Autowired
	private MonitoringService service;

	@GetMapping("/")
	public String getStatus(Model model) {
		model.addAttribute("state", service.getCurrentState());
		model.addAttribute("previousState", service.getPreviousState());
		model.addAttribute("alertTemperature", String.format("%.1f", service.getMinimumTemperature()));
		Double temperature = service.getCurrentState().getTemperature();
		if (temperature == null) {
			model.addAttribute("temperature", "--.-");
		} else {
			model.addAttribute("temperature", String.format("%.1f", temperature));
		}
		model.addAttribute("lastMonitoredAge",
				friendlyTimeDifference(System.currentTimeMillis() - service.getCurrentState().getTimestamp()));
		model.addAttribute("lastMonitoredTime", friendlyTimestamp(service.getCurrentState().getTimestamp()));
		model.addAttribute("lastStateChangeAge",
				friendlyTimeDifference(System.currentTimeMillis() - service.getLastStateChangeTimestamp()));
		model.addAttribute("lastStateChangeTime", friendlyTimestamp(service.getLastStateChangeTimestamp()));
		return "status";
	}
	
	String friendlyTimeDifference(long milliseconds) {
		if (milliseconds > (1000L * 60 * 60 * 24 * 30)) {
			return String.format("%.1f months ago", (double) milliseconds / (1000 * 60 * 60 * 24 * 30));
		} else if (milliseconds > (1000 * 60 * 60 * 24)) {
			return String.format("%.1f days ago", (double) milliseconds / (1000 * 60 * 60 * 24));
		} else if (milliseconds > (1000 * 60 * 60)) {
			return String.format("%.1f hours ago", (double) milliseconds / (1000 * 60 * 60));
		} else if (milliseconds > (1000 * 60)) {
			return String.format("%.1f minutes ago", (double) milliseconds / (1000 * 60));
		} else {
			return String.format("%d seconds ago", milliseconds / 1000);
		}
	}
	
	String friendlyTimestamp(long milliseconds) {
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd h:mm aa zzz");
		Date date = new Date(milliseconds);
		return dateFormat.format(date);
	}
}
