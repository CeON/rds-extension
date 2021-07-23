package pl.edu.icm.rds.extension.citation;

import edu.harvard.iq.dataverse.citation.CitationData;
import edu.harvard.iq.dataverse.persistence.GlobalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;


class RdsCitationFormatsConverterTest {

    private RdsCitationFormatsConverter converter = new RdsCitationFormatsConverter();

    private static final Locale TEST_LOCALE = Locale.ENGLISH;

    // -------------------- TESTS --------------------

    @Test
    @DisplayName("Should create BibTeX citation")
    void toBibtexString() {

        // given
        CitationData citationData = createFullCitationData();

        // when
        String bibtex = converter.toBibtexString(citationData, TEST_LOCALE);

        // then
        assertThat(bibtex).isEqualTo("@misc{ZENON_2019,\r\n" +
                "author = {Author, The First and Author, The Second},\r\n" +
                "doi = {10.18150/ZENON},\r\n" +
                "edition = {V1},\r\n" +
                "keywords = {Keyword I, Keyword II},\r\n" +
                "publisher = {Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], Dataverse [publisher], 2021},\r\n" +
                "title = {Title [data]},\r\n" +
                "url = {https://doi.org/10.18150/ZENON},\r\n" +
                "year = {2001},\r\n" +
                "note = {Edition: V1}\r\n" +
                "}\r\n");
    }

    @Test
    @DisplayName("Should create BibTeX citation for harvested data")
    void toBibtexString_harvested() {

        // given
        CitationData citationData = createFullCitationDataForHarvested();

        // when
        String bibtex = converter.toBibtexString(citationData, TEST_LOCALE);

        // then
        assertThat(bibtex).isEqualTo("@misc{ZENON_2019,\r\n" +
                "author = {Author, The First and Author, The Second},\r\n" +
                "doi = {10.18150/ZENON},\r\n" +
                "title = {Title [data]},\r\n" +
                "url = {https://doi.org/10.18150/ZENON},\r\n" +
                "year = {2019}\r\n" +
                "}\r\n");
    }

    @Test
    @DisplayName("Should create BibTeX citation for file")
    void toBibtexString__file() {

        // given
        CitationData citationData = createFullCitationDataForFile();

        // when
        String bibtex = converter.toBibtexString(citationData, TEST_LOCALE);

        // then
        assertThat(bibtex).isEqualTo("@misc{ZENON_2019,\r\n" +
                "author = {Author, The First and Author, The Second},\r\n" +
                "doi = {10.18150/ZENON},\r\n" +
                "edition = {V1},\r\n" +
                "keywords = {Keyword I, Keyword II},\r\n" +
                "publisher = {Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], Dataverse [publisher], 2021},\r\n" +
                "title = {Title [data]},\r\n" +
                "url = {https://doi.org/10.18150/ZENON},\r\n" +
                "year = {2001},\r\n" +
                "note = {Edition: V1; File Name [file name], doi:10.18150/ZENON_F}\r\n" +
                "}\r\n");
    }

    @Test
    @DisplayName("Should create BibTeX citation for harvested file")
    void toBibtexString__harvested_file() {

        // given
        CitationData citationData = createFullCitationDataForHarvestedFile();

        // when
        String bibtex = converter.toBibtexString(citationData, TEST_LOCALE);

        // then
        assertThat(bibtex).isEqualTo("@misc{ZENON_2019,\r\n" +
                "author = {Author, The First and Author, The Second},\r\n" +
                "doi = {10.18150/ZENON},\r\n" +
                "title = {Title [data]},\r\n" +
                "url = {https://doi.org/10.18150/ZENON},\r\n" +
                "year = {2019},\r\n" +
                "note = {File Name [file name], doi:10.18150/ZENON_F}\r\n" +
                "}\r\n");
    }

    @Test
    @DisplayName("Should create RIS citation")
    void toRISString() {

        // given
        CitationData citationData = createFullCitationData();


        // when
        String ris = converter.toRISString(citationData, TEST_LOCALE);

        // then
        assertThat(ris).isEqualTo("TY  - DATA\r\n" +
                "AU  - Author, The First\r\n" +
                "AU  - Author, The Second\r\n" +
                "T1  - Title [data]\r\n" +
                "LA  - polish\r\n" +
                "LA  - italian\r\n" +
                "PY  - 2001///\r\n" +
                "DO  - 10.18150/ZENON\r\n" +
                "UR  - https://doi.org/10.18150/ZENON\r\n" +
                "ET  - V1\r\n" +
                "PB  - Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], Dataverse [publisher], 2021\r\n" +
                "ER  - ");
    }

    @Test
    @DisplayName("Should create RIS citation for harvested data")
    void toRISString_harvested() {

        // given
        CitationData citationData = createFullCitationDataForHarvested();


        // when
        String ris = converter.toRISString(citationData, TEST_LOCALE);

        // then
        assertThat(ris).isEqualTo("TY  - DATA\r\n" +
                "AU  - Author, The First\r\n" +
                "AU  - Author, The Second\r\n" +
                "T1  - Title [data]\r\n" +
                "PY  - 2019///\r\n" +
                "DO  - 10.18150/ZENON\r\n" +
                "UR  - https://doi.org/10.18150/ZENON\r\n" +
                "ER  - ");
    }

    @Test
    @DisplayName("Should create RIS citation for file")
    void toRISString__file() {

        // given
        CitationData citationData = createFullCitationDataForFile();


        // when
        String ris = converter.toRISString(citationData, TEST_LOCALE);

        // then
        assertThat(ris).isEqualTo("TY  - DATA\r\n" +
                "AU  - Author, The First\r\n" +
                "AU  - Author, The Second\r\n" +
                "T1  - Title [data]\r\n" +
                "T2  - File Name\r\n" +
                "LA  - polish\r\n" +
                "LA  - italian\r\n" +
                "PY  - 2001///\r\n" +
                "DO  - 10.18150/ZENON\r\n" +
                "UR  - https://doi.org/10.18150/ZENON\r\n" +
                "ET  - V1\r\n" +
                "PB  - Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], Dataverse [publisher], 2021\r\n" +
                "ER  - ");
    }

    @Test
    @DisplayName("Should create RIS citation for harvested file")
    void toRISString__harvested_file() {

        // given
        CitationData citationData = createFullCitationDataForHarvestedFile();


        // when
        String ris = converter.toRISString(citationData, TEST_LOCALE);

        // then
        assertThat(ris).isEqualTo("TY  - DATA\r\n" +
                "AU  - Author, The First\r\n" +
                "AU  - Author, The Second\r\n" +
                "T1  - Title [data]\r\n" +
                "T2  - File Name\r\n" +
                "PY  - 2019///\r\n" +
                "DO  - 10.18150/ZENON\r\n" +
                "UR  - https://doi.org/10.18150/ZENON\r\n" +
                "ER  - ");
    }

    @Test
    @DisplayName("Should create EndNote citation")
    void toEndNoteString() {

        // given
        CitationData citationData = createFullCitationData();


        // when
        String endNote = converter.toEndNoteString(citationData, TEST_LOCALE);

        // then
        assertThat(endNote).isEqualTo("<?xml version='1.0' encoding='UTF-8'?>" +
                        "<xml>" +
                          "<records>" +
                            "<record>" +
                              "<ref-type name=\"Dataset\">59</ref-type>" +
                              "<contributors>" +
                                "<authors>" +
                                  "<author>Author, The First</author>" +
                                  "<author>Author, The Second</author>" +
                                "</authors>" +
                              "</contributors>" +
                              "<titles><title>Title [data]</title></titles>" +
                              "<keywords>" +
                                "<keyword>Keyword I</keyword>" +
                                "<keyword>Keyword II</keyword>" +
                              "</keywords>" +
                              "<dates><year>2001</year></dates>" +
                              "<publisher>Producer 1, ABC [producer], Producer 2, BCD [producer], " +
                                "Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], " +
                                "Dataverse [publisher], 2021" +
                              "</publisher>" +
                              "<edition>V1</edition>" +
                              "<language>polish</language>" +
                              "<language>italian</language>" +
                              "<urls><web-urls><url>https://doi.org/10.18150/ZENON</url></web-urls></urls>" +
                              "<electronic-resource-num>doi/10.18150/ZENON</electronic-resource-num>" +
                            "</record>" +
                          "</records>" +
                        "</xml>");
    }

    @Test
    @DisplayName("Should create EndNote citation for harvested data")
    void toEndNoteString_harvested() {

        // given
        CitationData citationData = createFullCitationDataForHarvested();


        // when
        String endNote = converter.toEndNoteString(citationData, TEST_LOCALE);

        // then
        assertThat(endNote).isEqualTo("<?xml version='1.0' encoding='UTF-8'?>" +
                "<xml>" +
                "<records>" +
                "<record>" +
                "<ref-type name=\"Dataset\">59</ref-type>" +
                "<contributors>" +
                "<authors>" +
                "<author>Author, The First</author>" +
                "<author>Author, The Second</author>" +
                "</authors>" +
                "</contributors>" +
                "<titles><title>Title [data]</title></titles>" +
                "<dates><year>2019</year></dates>" +
                "<urls><web-urls><url>https://doi.org/10.18150/ZENON</url></web-urls></urls>" +
                "<electronic-resource-num>doi/10.18150/ZENON</electronic-resource-num>" +
                "</record>" +
                "</records>" +
                "</xml>");
    }

    @Test
    @DisplayName("Should create EndNote citation for file")
    void toEndNoteString__file() {

        // given
        CitationData citationData = createFullCitationDataForFile();


        // when
        String endNote = converter.toEndNoteString(citationData, TEST_LOCALE);

        // then
        assertThat(endNote).isEqualTo("<?xml version='1.0' encoding='UTF-8'?>" +
                        "<xml>" +
                          "<records>" +
                            "<record>" +
                              "<ref-type name=\"Dataset\">59</ref-type>" +
                              "<contributors>" +
                                "<authors>" +
                                  "<author>Author, The First</author>" +
                                  "<author>Author, The Second</author>" +
                                "</authors>" +
                              "</contributors>" +
                              "<titles>" +
                                "<title>Title [data]</title>" +
                                "<secondary-title>File Name [file name]</secondary-title>" +
                              "</titles>" +
                              "<keywords>" +
                                "<keyword>Keyword I</keyword>" +
                                "<keyword>Keyword II</keyword>" +
                              "</keywords>" +
                              "<dates><year>2001</year></dates>" +
                              "<publisher>Producer 1, ABC [producer], Producer 2, BCD [producer], " +
                                "Warsaw. Distributor 1 [distributor], Distributor 2 [distributor], " +
                                "Dataverse [publisher], 2021" +
                              "</publisher>" +
                              "<edition>V1</edition>" +
                              "<language>polish</language>" +
                              "<language>italian</language>" +
                              "<urls><web-urls><url>https://doi.org/10.18150/ZENON</url></web-urls></urls>" +
                              "<electronic-resource-num>doi/10.18150/ZENON</electronic-resource-num>" +
                            "</record>" +
                          "</records>" +
                        "</xml>");
    }

    @Test
    @DisplayName("Should create EndNote citation for harvested file")
    void toEndNoteString__harvested_file() {

        // given
        CitationData citationData = createFullCitationDataForHarvestedFile();


        // when
        String endNote = converter.toEndNoteString(citationData, TEST_LOCALE);

        // then
        assertThat(endNote).isEqualTo("<?xml version='1.0' encoding='UTF-8'?>" +
                "<xml>" +
                "<records>" +
                "<record>" +
                "<ref-type name=\"Dataset\">59</ref-type>" +
                "<contributors>" +
                "<authors>" +
                "<author>Author, The First</author>" +
                "<author>Author, The Second</author>" +
                "</authors>" +
                "</contributors>" +
                "<titles>" +
                "<title>Title [data]</title>" +
                "<secondary-title>File Name [file name]</secondary-title>" +
                "</titles>" +
                "<dates><year>2019</year></dates>" +
                "<urls><web-urls><url>https://doi.org/10.18150/ZENON</url></web-urls></urls>" +
                "<electronic-resource-num>doi/10.18150/ZENON</electronic-resource-num>" +
                "</record>" +
                "</records>" +
                "</xml>");
    }

    @Test
    @DisplayName("Should create citation")
    void toString__() {

        // given
        CitationData citationData = createFullCitationData();


        // when
        String citation = converter.toString(citationData, TEST_LOCALE, false);

        // then
        assertThat(citation)
                .isEqualTo("Author, The First; Author, The Second: Title [data]. " +
                        "Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw, 2001. " +
                        "OtherId1, OtherId2, OtherId3. Distributor 1 [distributor], Distributor 2 [distributor], " +
                        "Dataverse [publisher], 2019. https://doi.org/10.18150/ZENON, V1");
    }

    @Test
    @DisplayName("Should create citation for harvested data")
    void toString__harvested() {

        // given
        CitationData citationData = createFullCitationDataForHarvested();


        // when
        String citation = converter.toString(citationData, TEST_LOCALE, false);

        // then
        assertThat(citation)
                .isEqualTo("Author, The First; Author, The Second: Title [data]. " +
                        "2019. https://doi.org/10.18150/ZENON");
    }

    @Test
    @DisplayName("Should create citation with translated constants for different locale")
    void toString__different_locale() {

        // given
        CitationData citationData = createFullCitationData();


        // when
        String citation = converter.toString(citationData, Locale.forLanguageTag("pl"), false);

        // then
        assertThat(citation)
                .isEqualTo("Author, The First; Author, The Second: Title [dane]. " +
                        "Producer 1, ABC [producent], Producer 2, BCD [producent], Warsaw, 2001. " +
                        "OtherId1, OtherId2, OtherId3. Distributor 1 [dystrybutor], Distributor 2 [dystrybutor], " +
                        "Dataverse [wydawca], 2019. https://doi.org/10.18150/ZENON, V1");
    }

    @Test
    @DisplayName("Should create citation for file")
    void toString__file() {

        // given
        CitationData citationData = createFullCitationDataForFile();


        // when
        String citation = converter.toString(citationData, TEST_LOCALE, false);

        // then
        assertThat(citation)
                .isEqualTo("Author, The First; Author, The Second: Title [data]. " +
                        "Producer 1, ABC [producer], Producer 2, BCD [producer], Warsaw, 2001. " +
                        "OtherId1, OtherId2, OtherId3. Distributor 1 [distributor], Distributor 2 [distributor], " +
                        "Dataverse [publisher], 2019. https://doi.org/10.18150/ZENON, V1, File Name [file name]");
    }

    @Test
    @DisplayName("Should create citation for harvested file")
    void toString__harvested_file() {

        // given
        CitationData citationData = createFullCitationDataForHarvestedFile();


        // when
        String citation = converter.toString(citationData, TEST_LOCALE, false);

        // then
        assertThat(citation)
                .isEqualTo("Author, The First; Author, The Second: Title [data]. " +
                        "2019. https://doi.org/10.18150/ZENON, File Name [file name]");
    }

    // -------------------- PRIVATE --------------------

    private CitationData createFullCitationData() {
        CitationData data = new CitationData();
        data.getAuthors().addAll(Arrays.asList("Author, The First", "Author, The Second"));
        data.getProducers().addAll(Arrays.asList(
                new CitationData.Producer("Producer 1", "ABC"),
                new CitationData.Producer("Producer 2", "BCD")));
        data.getDistributors().addAll(Arrays.asList("Distributor 1", "Distributor 2"));
        data.getOtherIds().addAll(Arrays.asList("OtherId1", "OtherId2", "OtherId3"));
        data.getKeywords().addAll(Arrays.asList("Keyword I", "Keyword II"));
        data.getLanguages().addAll(Arrays.asList("polish", "italian"));
        GlobalId globalId = new GlobalId("doi:10.18150/ZENON");
        data.setTitle("Title")
                .setProductionPlace("Warsaw")
                .setProductionDate("2001")
                .setRootDataverseName("Dataverse")
                .setReleaseYear("2021")
                .setYear("2019")
                .setPersistentId(globalId)
                .setPidOfDataset(globalId)
                .setVersion("V1");
        return data;
    }

    private CitationData createFullCitationDataForFile() {
        return createFullCitationData()
                .setDirect(true)
                .setFileTitle("File Name")
                .setPidOfFile(new GlobalId("doi:10.18150/ZENON_F"));
    }

    private CitationData createFullCitationDataForHarvested() {
        CitationData data = new CitationData();
        data.getAuthors().addAll(Arrays.asList("Author, The First", "Author, The Second"));
        GlobalId globalId = new GlobalId("doi:10.18150/ZENON");

        data.setTitle("Title")
                .setYear("2019")
                .setPidOfDataset(globalId)
                .setPersistentId(globalId);
        return data;
    }

    private CitationData createFullCitationDataForHarvestedFile() {
        return createFullCitationDataForHarvested()
                .setDirect(true)
                .setFileTitle("File Name")
                .setPidOfFile(new GlobalId("doi:10.18150/ZENON_F"));
    }

}
