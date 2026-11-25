# Spring PetClinic — relatório de visitas por animal

**Antes (`818c413`):** a tela do tutor já mostrava a data e a descrição das visitas de cada animal, mas não gerava um relatório separado nem permitia baixá-lo em CSV.

**Depois (`c6c8b9b`, branch `seminario-gof`):** acrescentamos dois botões em [ownerDetails.html](src/main/resources/templates/owners/ownerDetails.html#L76): **Ver relatório** abre o HTML em outra aba; **Baixar CSV** baixa os mesmos dados. Ambos mostram tutor, animal, visitas e total. Os padrões abaixo foram usados para implementar essa funcionalidade nova.

## 1. Strategy — escolher o formato

[ReportExporter.java](src/main/java/org/springframework/samples/petclinic/report/ReportExporter.java#L3) define o contrato comum: `format()`, `contentType()` e `export(...)`. O Spring entrega os exportadores ao [construtor de ReportService](src/main/java/org/springframework/samples/petclinic/report/ReportService.java#L23), que os guarda em um mapa por formato. Em [`generate()`](src/main/java/org/springframework/samples/petclinic/report/ReportService.java#L30), o serviço escolhe um deles e delega a geração:

```java
ReportExporter exporter = this.exporters.get(selectedFormat);
// ... verifica o formato, busca as visitas e prepara os dados e o nome do arquivo ...
return new GeneratedReport(exporter.export(data), exporter.contentType(), filename);
```

**Por que é Strategy?** Se o pedido traz `html`, quem escreve é `HtmlReportExporter`; se traz `csv`, é `CsvReportExporter`. A implementação muda em tempo de execução, mas a chamada do serviço é sempre a mesma: `exporter.export(data)`. Assim, um novo formato não exige mudar a lógica de seleção no serviço.

## 2. Template Method — fixar a ordem de montagem

Os dois exportadores herdam de [AbstractReportExporter.java](src/main/java/org/springframework/samples/petclinic/report/AbstractReportExporter.java#L5), que implementa `ReportExporter`. Nessa classe, `export()` é `final`: as subclasses não podem substituir o método que estabelece esta ordem:

```java
writeHeader(output, report);
for (VisitReportData.VisitEntry visit : report.visits()) {
    writeVisit(output, visit);
}
writeFooter(output, report);
```

**Por que é Template Method?** A classe abstrata determina **quando** escrever o cabeçalho, cada visita e o rodapé. As subclasses implementam os três métodos `protected abstract` e determinam **como** escrever cada parte. Por exemplo, em `writeVisit()`, [HtmlReportExporter.java](src/main/java/org/springframework/samples/petclinic/report/HtmlReportExporter.java#L54) cria uma linha de tabela (`<tr>`); [CsvReportExporter.java](src/main/java/org/springframework/samples/petclinic/report/CsvReportExporter.java#L26) escreve a data e a descrição separadas por vírgula. Nenhum dos dois precisa repetir o laço das visitas.

## Como isso se conecta à tela

Os botões chamam `GET /owners/{ownerId}/pets/{petId}/report` com `format=html` ou `format=csv`. O [ReportController](src/main/java/org/springframework/samples/petclinic/report/ReportController.java#L27) recebe o pedido. O serviço consulta o `OwnerRepository` existente, localiza o animal daquele tutor e reúne as visitas em [VisitReportData](src/main/java/org/springframework/samples/petclinic/report/VisitReportData.java#L6). O resultado volta em [GeneratedReport](src/main/java/org/springframework/samples/petclinic/report/GeneratedReport.java#L3), com conteúdo, tipo e nome do arquivo. Não foi criada uma tabela de relatórios.

Os **rótulos dos botões** reaproveitam a tradução que o PetClinic já possuía ([mensagens em português](src/main/resources/messages/messages_pt.properties#L54)). Isso é internacionalização, não Adapter. Os rótulos dentro do relatório permanecem em português.

## Como rodar e conferir

Com a versão da equipe e **JDK 17 ou superior**, abra o terminal na pasta do `pom.xml`. O banco padrão é o H2 em memória; não é preciso instalar um banco separado.

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS: `./mvnw spring-boot:run`.

Com o terminal aberto, acesse [Jean Coleman — Animais e visitas](http://localhost:8080/owners/6?lang=pt). Na seção da **Samantha**, experimente os dois botões. Com os dados iniciais, tanto o HTML quanto o CSV mostram **duas visitas**. Para encerrar, pressione `Ctrl+C`.

Para testar exportadores, serviço, controlador e chaves de tradução no PowerShell (23 testes na versão indicada):

```powershell
.\mvnw.cmd "-Dtest=ReportExporterTests,ReportServiceTests,ReportControllerTests,I18nPropertiesSyncTest" test
```
