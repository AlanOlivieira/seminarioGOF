package org.springframework.samples.petclinic.report;

import org.springframework.stereotype.Component;

@Component
public class CsvReportExporter extends AbstractReportExporter {

	@Override
	public String format() {
		return "csv";
	}

	@Override
	public String contentType() {
		return "text/csv;charset=UTF-8";
	}

	@Override
	protected void writeHeader(StringBuilder output, VisitReportData report) {
		output.append("Tutor,").append(csvCell(report.ownerName())).append("\r\n");
		output.append("Animal,").append(csvCell(report.petName())).append("\r\n");
		output.append("Data,Descrição\r\n");
	}

	@Override
	protected void writeVisit(StringBuilder output, VisitReportData.VisitEntry visit) {
		String date = visit.date() == null ? "" : visit.date().toString();

		output.append(csvCell(date)).append(",").append(csvCell(visit.description())).append("\r\n");
	}

	@Override
	protected void writeFooter(StringBuilder output, VisitReportData report) {
		output.append("Total de visitas,").append(report.visits().size()).append("\r\n");
	}

	private String csvCell(String value) {
		String text = value == null ? "" : value;
		String beginning = text.stripLeading();

		boolean startsWithControl = !text.isEmpty() && Character.isISOControl(text.charAt(0));
		boolean startsWithFormula = !beginning.isEmpty() && "=+-@＝＋－＠".indexOf(beginning.charAt(0)) >= 0;

		if (startsWithControl || startsWithFormula) {
			text = "'" + text;
		}

		return "\"" + text.replace("\"", "\"\"") + "\"";
	}

}