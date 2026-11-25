package org.springframework.samples.petclinic.report;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ReportController {

	private final ReportService reports;

	public ReportController(ReportService reports) {
		this.reports = reports;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/report")
	public ResponseEntity<byte[]> generate(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@RequestParam(name = "format", defaultValue = "html") String format) {

		GeneratedReport report;

		try {
			report = this.reports.generate(ownerId, petId, format);
		}
		catch (NoSuchElementException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
		}
		catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		}

		MediaType contentType = MediaType.parseMediaType(report.contentType());

		ContentDisposition.Builder disposition = MediaType.TEXT_HTML.isCompatibleWith(contentType)
				? ContentDisposition.inline() : ContentDisposition.attachment();

		return ResponseEntity.ok()
			.contentType(contentType)
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.filename(report.filename()).build().toString())
			.cacheControl(CacheControl.noStore())
			.body(report.content().getBytes(StandardCharsets.UTF_8));
	}

}