package pl.edu.icm.rds.extension.citation;

import edu.harvard.iq.dataverse.citation.AbstractCitationFormatsConverter;
import edu.harvard.iq.dataverse.citation.CitationConstants;
import edu.harvard.iq.dataverse.citation.CitationData;
import edu.harvard.iq.dataverse.common.BundleUtil;
import edu.harvard.iq.dataverse.persistence.GlobalId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ejb.EJBException;
import javax.enterprise.inject.Alternative;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Alternative @Priority(10)
public class RdsCitationFormatsConverter extends AbstractCitationFormatsConverter {
    private static final Logger logger = LoggerFactory.getLogger(RdsCitationFormatsConverter.class);

    // -------------------- LOGIC --------------------

    @Override
    public String toString(CitationData data, Locale locale, boolean escapeHtml) {
        CitationBuilder citation = new CitationBuilder(escapeHtml);

        citation.value(data.getAuthorsString()).endPart(": ");
        if (data.getFileTitle() != null && data.isDirect()) {
            citation.add("\"").value(data.getFileTitle()).endPart("\", ")
                    .add("<i>").value(data.getTitle()).add("</i>");
        } else {
            citation.add("\"").value(data.getTitle()).add("\"");
        }
        citation.add(getConstant(CitationConstants.DATA, locale)).endPart(". ");

        if (!data.getProducers().isEmpty()) {
            citation.value(joinProducers(data, locale)).endPart(StringUtils.EMPTY)
                    .add(", ").value(data.getProductionPlace()).endPart(StringUtils.EMPTY)
                    .add(", ").value(data.getProductionDate()).endPart(StringUtils.EMPTY)
                    .endPart(". ");
        }
        citation.value(data.getOtherIds().stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "))).endPart(". ")
                .value(joinDistributors(data, locale)).endPart()
                .value(data.getRootDataverseName())
                    .add(getConstant(CitationConstants.PUBLISHER, locale)).endPart()
                .rawValue(data.getReleaseYear()).endPart(". ");
        String pid = extractPersistentIdUrl(data);
        citation.urlValue(pid, pid).endPart()
                .rawValue(data.getVersion()).endPart(StringUtils.EMPTY);
        return citation.toString();
    }

    @Override
    public String toBibtexString(CitationData data, Locale locale) {
        GlobalId pid = data.getPersistentId();
        BibTeXCitationBuilder bibtex = new BibTeXCitationBuilder()
                .add(data.getFileTitle() != null && data.isDirect() ? "@incollection{" : "@misc{")
                .add(pid.getIdentifier() + "_" + data.getYear() + ",\r\n")
                .line("author", String.join(" and ", data.getAuthors()))
                .line("doi", pid.getAuthority() + "/" + pid.getIdentifier())
                .line("edition", data.getVersion());
        if (!data.getKeywords().isEmpty()) {
            bibtex.line("keywords", String.join(", ", data.getKeywords()));
        }
        if (!data.getLanguages().isEmpty()) {
            bibtex.line("language", data.getLanguages().get(0));
        }
        bibtex.line("publisher", createPublishingData(data, locale, true));
        if (data.getFileTitle() != null && data.isDirect()) {
            bibtex.line("title", data.getFileTitle())
                    .line("booktitle", data.getTitle(),
                            s -> bibtex.mapValue(s, "{",
                                    getConstant(CitationConstants.DATA, locale) + "}, "));
        } else {
            bibtex.line("title",
                    data.getTitle()
                            .replaceFirst("\"", "``")
                            .replaceFirst("\"", "''"),
                    s -> bibtex.mapValue(s, "\"{",
                            getConstant(CitationConstants.DATA, locale) + "}\","));
        }
        if (data.getUNF() != null) {
            bibtex.line("UNF", data.getUNF());
        }
        bibtex.line("url", pid.toURL().toString(), s -> bibtex.mapValue(s, "{", "}"))
                .line("year", data.getYear())
                .line("note", "Edition: " + data.getVersion())
                .add("}\r\n");
        return bibtex.toString();
    }

    @Override
    public String toRISString(CitationData data, Locale locale) {
        RISCitationBuilder ris = new RISCitationBuilder()
                .line("TY  - DATA")
                .lines("AU", data.getAuthors());
        if ((data.getFileTitle() != null) && data.isDirect()) {
            ris.line("T1", data.getFileTitle())
                    .line("T2", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        } else {
            ris.line("T1", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        }
        if (data.getSeriesTitle() != null) {
            ris.line("T3", data.getSeriesTitle());
        }
        boolean productionDateAvailable = isNotBlank(data.getProductionDate());
        String productionDate = productionDateAvailable
                ? data.getProductionDate() : data.getReleaseYear();
        ris.line("PY", productionDate);
        GlobalId pid = data.getPersistentId();
        if (pid != null) {
            ris.line("DO", pid.toString())
                .line("UR", extractPersistentIdUrl(data));
        }
        ris.line("ET", data.getVersion())
                .line("PB", createPublishingData(data, locale, productionDateAvailable));
        if (data.getFileTitle() != null) {
            if (!data.isDirect()) {
                ris.line("C1", data.getFileTitle());
            }
            if (data.getUNF() != null) {
                ris.line("C2", data.getUNF());
            }
        }
        ris.line("ER", ""); // closing element
        return ris.toString();
    }

    @Override
    public String toEndNoteString(CitationData data, Locale locale) {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlw = null;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            xmlw = xmlOutputFactory.createXMLStreamWriter(buffer);
            createEndNoteXML(data, locale, xmlw);
            return buffer.toString();
        } catch (XMLStreamException | IOException e) {
            logger.error("", e);
            throw new EJBException("Error occurred during creating endnote xml.", e);
        } finally {
            try {
                if (xmlw != null) {
                    xmlw.close();
                }
            } catch (XMLStreamException xse) {
                logger.warn("Exception while closing XMLStreamWriter", xse);
            }
        }
    }

    // -------------------- PRIVATE --------------------

    private void createEndNoteXML(CitationData data, Locale locale, XMLStreamWriter xmlw) throws XMLStreamException {
        EndNoteCitationBuilder xml = new EndNoteCitationBuilder(xmlw);
        xml.start()
                .startTag("xml")
                .startTag("records")
                .startTag("record")
                .startTag("ref-type")
                .addAttribute("name", "Dataset")
                .addValue("59")
                .endTag() // ref-type
                .startTag("contributors")
                .addTagCollection("authors", "author", data.getAuthors())
                .endTag(); // contributors

        xml.startTag("titles");
        if ((data.getFileTitle() != null) && data.isDirect()) {
            xml.addTagWithValue("title", data.getFileTitle())
                    .addTagWithValue("secondary-title", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        } else {
            xml.addTagWithValue("title", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        }
        if (data.getSeriesTitle() != null) {
            xml.addTagWithValue("tertiary-title", data.getSeriesTitle());
        }
        xml.endTag() // titles
                .addTagCollection("keywords", "keyword", data.getKeywords())
                .startTag("dates")
                .addTagWithValue("year", data.getYear())
                .endTag() // dates
                .addTagWithValue("publisher", createPublishingData(data, locale, true))
                .addTagWithValue("edition", data.getVersion());

        if (!data.getLanguages().isEmpty()) {
            xml.addTagWithValue( "language", data.getLanguages().get(0));
        }
        GlobalId pid = data.getPersistentId();
        if (pid != null) {
            xml.startTag("urls")
                    .startTag("web-urls")
                    .addTagWithValue("url", extractPersistentIdUrl(data))
                    .endTag() // web-urls
                    .endTag(); // urls
        }
        if (data.getFileTitle() != null) {
            xml.addTagWithValue("custom1", data.getFileTitle());
            if (data.getUNF() != null) {
                xml.addTagWithValue("custom2", data.getUNF());
            }
        }
        if (pid != null) {
            xml.addTagWithValue("electronic-resource-num",
                    pid.getProtocol() + "/" + pid.getAuthority() + "/" + pid.getIdentifier());
        }
        xml.endTag() // record
                .endTag() // records
                .endTag() // xml
                .end();
    }

    private String joinDistributors(CitationData data, Locale locale) {
        return data.getDistributors().stream()
                .map(d -> d + getConstant(CitationConstants.DISTRIBUTOR, locale))
                .collect(Collectors.joining(", "));
    }

    private String joinProducers(CitationData data, Locale locale) {
        return data.getProducers().stream()
                .map(p -> isNotBlank(p.getAffiliation())
                        ? p.getName() + ", " + p.getAffiliation()
                        : p.getName())
                .map(p -> p + getConstant(CitationConstants.PRODUCER, locale))
                .collect(Collectors.joining(", "));
    }

    private String createPublishingData(CitationData data, Locale locale, boolean useReleaseDate) {
        String producers = !data.getProducers().isEmpty()
                ? Stream.of(joinProducers(data, locale), data.getProductionPlace())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", ")) + ". "
                : "";
        String citationEnd = Stream.of(joinDistributors(data, locale),
                data.getRootDataverseName() + getConstant(CitationConstants.PUBLISHER, locale),
                useReleaseDate ? data.getReleaseYear() : "")
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
        return producers + citationEnd;
    }

    private String extractPersistentIdUrl(CitationData data) {
        return Optional.ofNullable(data.getPersistentId())
                .map(GlobalId::toURL)
                .map(URL::toString)
                .orElse(StringUtils.EMPTY);
    }

    private String getConstant(CitationConstants constant, Locale locale) {
        return " [" + BundleUtil.getStringFromBundleWithLocale(constant.getKey(), locale) + "]";
    }
}
