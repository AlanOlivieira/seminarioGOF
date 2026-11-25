package org.springframework.samples.petclinic.report;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

	private final OwnerRepository owners;

	private final Map<String, ReportExporter> exporters;

	public ReportService(OwnerRepository owners, List<ReportExporter> exporters) {
		this.owners = owners;
		this.exporters = exporters.stream()
			.collect(Collectors.toUnmodifiableMap(ReportExporter::format, Function.identity()));
	}

	@Transactional(readOnly = true)
	public GeneratedReport generate(int ownerId, int petId, String format) {
		String selectedFormat = format == null ? "" : format.strip().toLowerCase(Locale.ROOT);

		ReportExporter exporter = this.exporters.get(selectedFormat);

		if (exporter == null) {
			throw new IllegalArgumentException("Formato de relatório não suportado.");
		}

		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new NoSuchElementException("Tutor não encontrado."));

		Pet pet = owner.getPet(petId);

		if (pet == null) {
			throw new NoSuchElementException("Animal não encontrado para este tutor.");
		}

		List<VisitReportData.VisitEntry> visits = pet.getVisits()
			.stream()
			.map(visit -> new VisitReportData.VisitEntry(visit.getDate(), visit.getDescription()))
			.toList();

		String ownerName = owner.getFirstName() + " " + owner.getLastName();

		VisitReportData data = new VisitReportData(ownerName, pet.getName(), visits);

		String filename = "visitas-animal-" + petId + "." + exporter.format();

		return new GeneratedReport(exporter.export(data), exporter.contentType(), filename);
	}

}