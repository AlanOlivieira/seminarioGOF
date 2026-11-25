package org.springframework.samples.petclinic.report;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTests {

	private static final String REPORT_URL = "/owners/{ownerId}/pets/{petId}/report";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReportService reports;

	@Test
	void shouldDisplayHtmlReport() throws Exception {
		String body = "<h1>Relatório de visitas</h1>";

		when(this.reports.generate(6, 7, "html"))
			.thenReturn(new GeneratedReport(body, "text/html;charset=UTF-8", "visitas-animal-7.html"));

		this.mockMvc.perform(get(REPORT_URL, 6, 7).param("format", "html"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/html;charset=UTF-8"))
			.andExpect(content().encoding("UTF-8"))
			.andExpect(content().string(body))
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"visitas-animal-7.html\""))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
	}

	@Test
	void shouldDownloadCsvReport() throws Exception {
		String body = "Data,Descrição\r\n2026-11-23,Vacinação\r\n";

		when(this.reports.generate(6, 7, "csv"))
			.thenReturn(new GeneratedReport(body, "text/csv;charset=UTF-8", "visitas-animal-7.csv"));

		this.mockMvc.perform(get(REPORT_URL, 6, 7).param("format", "csv"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/csv;charset=UTF-8"))
			.andExpect(content().encoding("UTF-8"))
			.andExpect(content().string(body))
			.andExpect(
					header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"visitas-animal-7.csv\""))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
	}

	@Test
	void shouldDefaultToHtml() throws Exception {
		when(this.reports.generate(6, 7, "html"))
			.thenReturn(new GeneratedReport("<h1>Relatório</h1>", "text/html;charset=UTF-8", "visitas-animal-7.html"));

		this.mockMvc.perform(get(REPORT_URL, 6, 7))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/html;charset=UTF-8"));

		verify(this.reports).generate(6, 7, "html");
	}

	@Test
	void shouldReturnBadRequestForUnsupportedFormat() throws Exception {
		when(this.reports.generate(6, 7, "pdf"))
			.thenThrow(new IllegalArgumentException("Formato de relatório não suportado."));

		this.mockMvc.perform(get(REPORT_URL, 6, 7).param("format", "pdf")).andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnNotFoundForMissingOwner() throws Exception {
		when(this.reports.generate(999, 7, "html")).thenThrow(new NoSuchElementException("Tutor não encontrado."));

		this.mockMvc.perform(get(REPORT_URL, 999, 7).param("format", "html")).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundForPetOutsideOwner() throws Exception {
		when(this.reports.generate(6, 999, "csv"))
			.thenThrow(new NoSuchElementException("Animal não encontrado para este tutor."));

		this.mockMvc.perform(get(REPORT_URL, 6, 999).param("format", "csv")).andExpect(status().isNotFound());
	}

	@Test
	void shouldRejectNonNumericOwnerId() throws Exception {
		this.mockMvc.perform(get(REPORT_URL, "abc", 7)).andExpect(status().isBadRequest());

		verifyNoInteractions(this.reports);
	}

}