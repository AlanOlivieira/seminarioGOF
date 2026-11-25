package org.springframework.samples.petclinic.report;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class HtmlReportExporter extends AbstractReportExporter {

	@Override
	public String format() {
		return "html";
	}

	@Override
	public String contentType() {
		return "text/html;charset=UTF-8";
	}

	@Override
	protected void writeHeader(StringBuilder output, VisitReportData report) {
		output.append("""
				<!DOCTYPE html>
				<html lang="pt-BR">
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1">
				    <title>Relatório de visitas</title>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 32px; color: #333; }
				        table { width: 100%; border-collapse: collapse; }
				        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
				        th { background-color: #f2f2f2; }
				        td { white-space: pre-wrap; overflow-wrap: anywhere; }
				    </style>
				</head>
				<body>
				    <h1>Relatório de visitas</h1>
				""");

		output.append("<p><strong>Tutor:</strong> ").append(escape(report.ownerName())).append("</p>\n");

		output.append("<p><strong>Animal:</strong> ").append(escape(report.petName())).append("</p>\n");

		output.append("""
				<table>
				    <thead>
				        <tr><th>Data</th><th>Descrição</th></tr>
				    </thead>
				    <tbody>
				""");
	}

	@Override
	protected void writeVisit(StringBuilder output, VisitReportData.VisitEntry visit) {
		String date = visit.date() == null ? "" : visit.date().toString();

		output.append("<tr><td>")
			.append(escape(date))
			.append("</td><td>")
			.append(escape(visit.description()))
			.append("</td></tr>\n");
	}

	@Override
	protected void writeFooter(StringBuilder output, VisitReportData report) {
		if (report.visits().isEmpty()) {
			output.append("<tr><td colspan=\"2\">Nenhuma visita registrada.</td></tr>\n");
		}

		output.append("""
				    </tbody>
				</table>
				""");

		output.append("<p><strong>Total de visitas:</strong> ").append(report.visits().size()).append("</p>\n");

		output.append("</body>\n</html>\n");
	}

	private String escape(String value) {
		return HtmlUtils.htmlEscape(value == null ? "" : value, "UTF-8");
	}

}