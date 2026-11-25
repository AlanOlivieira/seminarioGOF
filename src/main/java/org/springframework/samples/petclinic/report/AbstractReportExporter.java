package org.springframework.samples.petclinic.report;

import java.util.Objects;

public abstract class AbstractReportExporter implements ReportExporter {

	@Override
	public final String export(VisitReportData report) {
		Objects.requireNonNull(report, "Report data must not be null");

		StringBuilder output = new StringBuilder();

		writeHeader(output, report);

		for (VisitReportData.VisitEntry visit : report.visits()) {
			writeVisit(output, visit);
		}

		writeFooter(output, report);

		return output.toString();
	}

	protected abstract void writeHeader(StringBuilder output, VisitReportData report);

	protected abstract void writeVisit(StringBuilder output, VisitReportData.VisitEntry visit);

	protected abstract void writeFooter(StringBuilder output, VisitReportData report);

}