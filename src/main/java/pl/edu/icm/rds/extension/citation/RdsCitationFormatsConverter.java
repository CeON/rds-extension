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
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Alternative @Priority(10)
public class RdsCitationFormatsConverter extends AbstractCitationFormatsConverter {
    private static final Logger logger = LoggerFactory.getLogger(RdsCitationFormatsConverter.class);

    // -------------------- LOGIC --------------------

    @Override
    public String toString(CitationData data, Locale locale, boolean escapeHtml) {
        CitationBuilder citation = new CitationBuilder(escapeHtml)
                .value(data.getAuthorsString()).endPart(": ")
                .value(data.getTitle())
                .add(getConstant(CitationConstants.DATA, locale)).endPart(". ");
        if (!data.getProducers().isEmpty()) {
            citation.value(joinProducers(data, locale)).endPartEmpty()
                    .add(", ").value(data.getProductionPlace()).endPartEmpty()
                    .add(", ").value(data.getProductionDate()).endPartEmpty()
                    .endPart(". ");
        }
        citation.value(data.getOtherIds().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(joining(", "))).endPart(". ")
                .value(joinDistributors(data, locale)).endPart()
                .value(data.getRootDataverseName())
                    .add(getConstant(CitationConstants.PUBLISHER, locale)).endPart()
                .rawValue(data.getYear()).endPart(". ");
        String pid = extractPIDUrl(data.getPersistentId());

        citation.urlValue(pid, pid).endPartEmpty()
                .add(", ").rawValue(data.getVersion()).endPartEmpty();

        if (shouldAddFileName(data)) {
            citation.add(", ").value(data.getFileTitle()).add(getConstant(CitationConstants.FILE_NAME, locale))
                    .endPartEmpty();
        }
        return citation.toString();
    }

    @Override
    public String toBibtexString(CitationData data, Locale locale) {
        GlobalId pid = data.getPidOfDataset() != null
                ? data.getPidOfDataset()
                : new GlobalId(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        BibTeXCitationBuilder bibtex = new BibTeXCitationBuilder()
                .add("@misc{")
                .add(pid.getIdentifier() + "_" + data.getYear() + ",\r\n")
                .line("author", String.join(" and ", data.getAuthors()))
                .line("doi", pid.getAuthority() + "/" + pid.getIdentifier());

        if (data.getVersion() != null) {
            bibtex.line("edition", data.getVersion());
        }

        if (!data.getKeywords().isEmpty()) {
            bibtex.line("keywords", String.join(", ", data.getKeywords()));
        }

        String publishingData = createPublishingData(data, locale);
        if (StringUtils.isNotBlank(publishingData)) {
            bibtex.line("publisher", publishingData);
        }

        bibtex.line("title", data.getTitle(),
                    s -> bibtex.mapValue(s, "{", getConstant(CitationConstants.DATA, locale) + "},"));

        String pidUrl = pid.toURL() != null ? pid.toURL().toString() : StringUtils.EMPTY;
        String filePid = shouldAddFileName(data) && data.getPidOfFile() != null
                ? ", " + data.getPidOfFile().asString() : StringUtils.EMPTY;

        bibtex.line("url", pidUrl, s -> bibtex.mapValue(s, "{", "},"))
                .line("year", getMainProductionYear(data) != null ? getMainProductionYear(data) : data.getYear());

        String fileName = shouldAddFileName(data)
                ? data.getFileTitle() + getConstant(CitationConstants.FILE_NAME, locale)
                : StringUtils.EMPTY;
        String noteEditionPart = data.getVersion() !=  null ? "Edition: " + data.getVersion() : StringUtils.EMPTY;
        String noteFilePart = fileName + filePid;
        String note = Stream.of(noteEditionPart, noteFilePart).filter(s -> !s.isEmpty()).collect(joining("; "));
        if (!note.isEmpty()) {
            bibtex.line("note", note);
        }

        bibtex.removeLastDelimiter(",\r\n").add("\r\n").add("}\r\n");
        return bibtex.toString();
    }

    @Override
    public String toRISString(CitationData data, Locale locale) {
        RISCitationBuilder ris = new RISCitationBuilder()
                .line("TY  - DATA")
                .lines("AU", data.getAuthors())
                .line("T1", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        if (shouldAddFileName(data)) {
            ris.line("T2", data.getFileTitle());
        }
        ris.lines("LA", data.getLanguages());

        ris.line("PY", (getMainProductionYear(data) != null ? getMainProductionYear(data) : data.getYear()) + "///");
        GlobalId pid = data.getPidOfDataset();
        if (pid != null) {
            ris.line("DO", pid.getAuthority() + "/" + pid.getIdentifier())
                .line("UR", extractPIDUrl(data.getPidOfDataset()));
        }
        if (data.getVersion() != null) {
            ris.line("ET", data.getVersion());
        }

        if (!data.getProducers().isEmpty() || !data.getDistributors().isEmpty()) {
            ris.line("PB", createPublishingData(data, locale));
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
        xml.addTagWithValue("title", data.getTitle() + getConstant(CitationConstants.DATA, locale));
        if (shouldAddFileName(data)) {
            xml.addTagWithValue("secondary-title", data.getFileTitle() + getConstant(CitationConstants.FILE_NAME, locale));
        }
        xml.endTag() // titles
                .addTagCollection("keywords", "keyword", data.getKeywords())
                .startTag("dates")
                .addTagWithValue("year", getMainProductionYear(data) != null ? getMainProductionYear(data) : data.getYear())
                .endTag();// dates

        if (!data.getProducers().isEmpty() || !data.getDistributors().isEmpty()) {
            xml.addTagWithValue("publisher", createPublishingData(data, locale));
        }

        if (data.getVersion() != null) {
            xml.addTagWithValue("edition", data.getVersion());
        }

        xml.addTagCollection("", "language", data.getLanguages());
        GlobalId pid = data.getPidOfDataset();
        if (pid != null) {
            xml.startTag("urls")
                    .startTag("web-urls")
                    .addTagWithValue("url", extractPIDUrl(data.getPidOfDataset()))
                    .endTag() // web-urls
                    .endTag(); // urls
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

    private boolean shouldAddFileName(CitationData data) {
        return /* data.isDirect() && */ isNotBlank(data.getFileTitle());
    }

    private String joinDistributors(CitationData data, Locale locale) {
        return data.getDistributors().stream()
                .map(d -> d + getConstant(CitationConstants.DISTRIBUTOR, locale))
                .collect(joining(", "));
    }

    private String joinProducers(CitationData data, Locale locale) {
        return data.getProducers().stream()
                .map(p -> isNotBlank(p.getAffiliation())
                        ? p.getName() + ", " + p.getAffiliation()
                        : p.getName())
                .map(p -> p + getConstant(CitationConstants.PRODUCER, locale))
                .collect(joining(", "));
    }

    private String createPublishingData(CitationData data, Locale locale) {
        String producers = !data.getProducers().isEmpty()
                ? Stream.of(joinProducers(data, locale), data.getProductionPlace())
                .filter(StringUtils::isNotBlank)
                .collect(joining(", ")) + ". "
                : "";
        String distributors = Stream.of(joinDistributors(data, locale))
                .filter(StringUtils::isNotBlank)
                .collect(joining(", "));

        String rootDvName = StringUtils.isNotBlank(data.getRootDataverseName()) ?
                ", " + data.getRootDataverseName() + getConstant(CitationConstants.PUBLISHER, locale) : StringUtils.EMPTY;

        String productionYear = StringUtils.isNotBlank(getAuxiliaryProductionYear(data))
                ? ", " + getAuxiliaryProductionYear(data) : EMPTY;

        if(StringUtils.isNotBlank(producers) || StringUtils.isNotBlank(distributors) || StringUtils.isNotBlank(rootDvName)) {
            return producers + distributors + rootDvName + productionYear;
        }

        return EMPTY;
    }

    private String extractPIDUrl(GlobalId globalId) {
        return Optional.ofNullable(globalId)
                .map(GlobalId::toURL)
                .map(URL::toString)
                .orElse(StringUtils.EMPTY);
    }

    private String getConstant(CitationConstants constant, Locale locale) {
        return " [" + BundleUtil.getStringFromBundleWithLocale(constant.getKey(), locale) + "]";
    }

    private String getMainProductionYear(CitationData data) {
        return isNotBlank(data.getProductionDate())
                ? data.getProductionDate() : data.getReleaseYear();
    }

    private String getAuxiliaryProductionYear(CitationData data) {
        return isNotBlank(data.getProductionDate())
                ? data.getReleaseYear() : StringUtils.EMPTY;
    }
}
