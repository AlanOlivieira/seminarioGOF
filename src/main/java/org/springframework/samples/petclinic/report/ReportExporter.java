package org.springframework.samples.petclinic.report;

public interface ReportExporter {

	String format();

	String contentType();

	String export(VisitReportData report);

}