package org.springframework.samples.petclinic.report;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTests {

	@Mock
	private OwnerRepository owners;

	private ReportService service;

	@BeforeEach
	void setup() {
		this.service = new ReportService(this.owners, List.of(new CsvReportExporter(), new HtmlReportExporter()));
	}

	@Test
	void shouldGenerateCsvWithOwnerAndPetData() {
		when(this.owners.findById(1)).thenReturn(Optional.of(ownerWithPet()));

		GeneratedReport result = this.service.generate(1, 7, "csv");

		assertThat(result.filename()).isEqualTo("visitas-animal-7.csv");
		assertThat(result.contentType()).isEqualTo("text/csv;charset=UTF-8");
		assertThat(result.content()).contains("Tutor,\"Alan Oliveira\"", "Animal,\"Mel\"",
				"\"2026-11-23\",\"Vacinação\"");
	}

	@Test
	void shouldGenerateHtmlWithOwnerAndPetData() {
		when(this.owners.findById(1)).thenReturn(Optional.of(ownerWithPet()));

		GeneratedReport result = this.service.generate(1, 7, "html");

		assertThat(result.filename()).isEqualTo("visitas-animal-7.html");
		assertThat(result.contentType()).isEqualTo("text/html;charset=UTF-8");
		assertThat(result.content()).contains("<h1>Relatório de visitas</h1>", "Alan Oliveira", "Mel", "Vacinação");
	}

	@Test
	void shouldNormalizeRequestedFormat() {
		when(this.owners.findById(1)).thenReturn(Optional.of(ownerWithPet()));

		GeneratedReport result = this.service.generate(1, 7, "  CsV  ");

		assertThat(result.filename()).isEqualTo("visitas-animal-7.csv");
		assertThat(result.contentType()).isEqualTo("text/csv;charset=UTF-8");
	}

	@Test
	void shouldRejectUnsupportedFormatBeforeQueryingRepository() {
		assertThatThrownBy(() -> this.service.generate(1, 7, "pdf")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Formato de relatório não suportado.");

		verifyNoInteractions(this.owners);
	}

	@Test
	void shouldRejectMissingOwner() {
		when(this.owners.findById(1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.service.generate(1, 7, "html")).isInstanceOf(NoSuchElementException.class)
			.hasMessage("Tutor não encontrado.");
	}

	@Test
	void shouldRejectPetNotFoundForOwner() {
		when(this.owners.findById(1)).thenReturn(Optional.of(ownerWithPet()));

		assertThatThrownBy(() -> this.service.generate(1, 99, "csv")).isInstanceOf(NoSuchElementException.class)
			.hasMessage("Animal não encontrado para este tutor.");
	}

	@Test
	void shouldGenerateReportForPetWithoutVisits() {
		Owner owner = ownerWithPet();
		owner.getPet(7).getVisits().clear();

		when(this.owners.findById(1)).thenReturn(Optional.of(owner));

		GeneratedReport csv = this.service.generate(1, 7, "csv");
		GeneratedReport html = this.service.generate(1, 7, "html");

		assertThat(csv.content()).contains("Total de visitas,0");
		assertThat(html.content()).contains("Nenhuma visita registrada.");
	}

	private Owner ownerWithPet() {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("Alan");
		owner.setLastName("Oliveira");

		Pet pet = new Pet();
		pet.setId(7);
		pet.setName("Mel");

		Visit visit = new Visit();
		visit.setId(10);
		visit.setDate(LocalDate.of(2026, 11, 23));
		visit.setDescription("Vacinação");

		pet.addVisit(visit);
		owner.addPet(pet);

		return owner;
	}

}