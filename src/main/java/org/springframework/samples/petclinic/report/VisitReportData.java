package org.springframework.samples.petclinic.report;

import java.time.LocalDate;
import java.util.List;

public record VisitReportData(String ownerName, String petName, List<VisitEntry> visits) {

	public VisitReportData {
		visits = List.copyOf(visits);
	}

	public record VisitEntry(LocalDate date, String description) {

	}

}