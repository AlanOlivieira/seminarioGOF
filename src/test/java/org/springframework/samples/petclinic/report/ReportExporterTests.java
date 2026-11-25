package org.springframework.samples.petclinic.report;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExporterTests {

	@Test
	void shouldGenerateCsvWithReportData() {
		ReportExporter exporter = new CsvReportExporter();

		String result = exporter.export(reportWith("Vacinação"));

		assertThat(exporter.format()).isEqualTo("csv");
		assertThat(exporter.contentType()).isEqualTo("text/csv;charset=UTF-8");

		assertThat(result).containsSubsequence("Tutor,\"Alan\"", "Animal,\"Mel\"", "Data,Descrição",
				"\"2026-11-23\",\"Vacinação\"", "Total de visitas,1");
	}

	@Test
	void shouldGenerateHtmlWithReportData() {
		ReportExporter exporter = new HtmlReportExporter();

		String result = exporter.export(reportWith("Vacinação"));

		assertThat(exporter.format()).isEqualTo("html");
		assertThat(exporter.contentType()).isEqualTo("text/html;charset=UTF-8");

		assertThat(result).containsSubsequence("<h1>Relatório de visitas</h1>", "<strong>Tutor:</strong> Alan",
				"<strong>Animal:</strong> Mel", "2026-11-23", "Vacinação", "<strong>Total de visitas:</strong> 1");

		assertThat(result).contains("</body>", "</html>");
	}

	@Test
	void shouldEscapeQuotesAndPreserveLineBreaksInCsv() {
		String result = new CsvReportExporter().export(reportWith("Vacina, com \"reforço\"\nRetorno"));

		assertThat(result).contains("\"Vacina, com \"\"reforço\"\"\nRetorno\"");
	}

	@Test
	void shouldEscapeHtmlInVisitDescription() {
		String result = new HtmlReportExporter().export(reportWith("<b>Vacina</b> & retorno"));

		assertThat(result).contains("&lt;b&gt;Vacina&lt;/b&gt; &amp; retorno").doesNotContain("<b>Vacina</b>");
	}

	@Test
	void shouldHandleReportWithoutVisits() {
		VisitReportData report = new VisitReportData("Alan", "Mel", List.of());

		String csv = new CsvReportExporter().export(report);
		String html = new HtmlReportExporter().export(report);

		assertThat(csv).contains("Data,Descrição", "Total de visitas,0");
		assertThat(html).contains("Nenhuma visita registrada.", "<strong>Total de visitas:</strong> 0");
	}

	@Test
	void shouldPrefixFormulaLikeCsvValues() {
		for (String description : List.of("=1+1", "+1+1", "-1+1", "@SUM(A1)", "  =1+1")) {
			String result = new CsvReportExporter().export(reportWith(description));

			assertThat(result).contains("\"'" + description + "\"");
		}
	}

	@Test
	void shouldHandleMissingDateAndDescription() {
		VisitReportData report = new VisitReportData("Alan", "Mel",
				List.of(new VisitReportData.VisitEntry(null, null)));

		String csv = new CsvReportExporter().export(report);
		String html = new HtmlReportExporter().export(report);

		assertThat(csv).contains("\"\",\"\"\r\n");
		assertThat(html).contains("<tr><td></td><td></td></tr>");
	}

	private VisitReportData reportWith(String description) {
		return new VisitReportData("Alan", "Mel",
				List.of(new VisitReportData.VisitEntry(LocalDate.of(2026, 11, 23), description)));
	}

}