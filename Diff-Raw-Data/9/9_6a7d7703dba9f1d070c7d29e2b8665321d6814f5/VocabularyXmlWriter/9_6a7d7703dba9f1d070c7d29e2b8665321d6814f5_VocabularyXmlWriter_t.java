 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.exports.rdf;
 
 import java.io.OutputStream;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.StringUtils;
 
 import eionet.meta.dao.domain.Folder;
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyConceptAttribute;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.data.SiteCode;
 import eionet.util.StringEncoder;
 
 /**
  * Vocabulary RDF-XML writer.
  *
  * @author Juhan Voolaid
  */
 public class VocabularyXmlWriter {
 
     /** RDF write constants. */
     private static final String ENCODING = "UTF-8";
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     private static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
     private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
     private static final String DCTYPE_NS = "http://purl.org/dc/dcmitype";
     private static final String DCTERMS_NS = "http://purl.org/dc/terms";
 
     private static final String DD_SCHEMA_NS = "http://dd.eionet.europa.eu/schema.rdf#";
 
     /**
      * XMLWriter to write XML to.
      */
     private XMLStreamWriter writer = null;
 
     /**
      * Class constructor.
      *
      * @param out
      * @param contextRoot
      * @param vocabularyService
      * @throws XMLStreamException
      */
     public VocabularyXmlWriter(OutputStream out) throws XMLStreamException {
         writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
     }
 
     /**
      * Escapes IRI's reserved characters in the given URL string.
      *
      * @param url
      *            is a string.
      * @return escaped URI
      */
     public static String escapeIRI(String url) {
 
         return StringEncoder.encodeToIRI(url);
     }
 
     /**
      * Writes rdf output to stream for one vocabulary folder.
      *
      * @param folderContextRoot
      *            IRI for folder
      * @param contextRoot
      *            folder context IRI
      * @param vocabularyFolder
      *            vocabulary
      * @param vocabularyConcepts
      *            concepts in the vocabulary
      * @throws XMLStreamException
      *             if streaming fails
      */
     public void writeRDFXml(String folderContextRoot, String contextRoot, VocabularyFolder vocabularyFolder,
             List<? extends VocabularyConcept> vocabularyConcepts) throws XMLStreamException {
 
         writeXmlStart(vocabularyFolder.isSiteCodeType(), contextRoot);
 
         writeVocabularyFolderXml(folderContextRoot, contextRoot, vocabularyFolder, vocabularyConcepts);
 
         writeXmlEnd();
     }
 
     /**
      * Writes start of XML.
      *
      * @param siteCodeType
      * @param contextRoot IRI for context
      * @throws XMLStreamException
      */
     public void writeXmlStart(boolean siteCodeType, String contextRoot) throws XMLStreamException {
         writer.writeStartDocument(ENCODING, "1.0");
         writer.writeCharacters("\n");
 
         writer.setPrefix("rdf", RDF_NS);
         writer.setPrefix("rdfs", RDFS_NS);
         writer.setPrefix("skos", SKOS_NS);
         writer.setPrefix("owl", OWL_NS);
         if (siteCodeType) {
             writer.setPrefix("dd", DD_SCHEMA_NS);
         }
 
         writer.writeStartElement("rdf", "RDF", RDF_NS);
         writer.writeNamespace("rdf", RDF_NS);
         writer.writeNamespace("rdfs", RDFS_NS);
         writer.writeNamespace("skos", SKOS_NS);
         writer.writeNamespace("owl", OWL_NS);
         writer.writeNamespace("dctype", DCTYPE_NS);
         writer.writeNamespace("dcterms", DCTERMS_NS);
 
         if (siteCodeType) {
             writer.writeNamespace("dd", DD_SCHEMA_NS);
         }
         writer.writeAttribute("xml", XML_NS, "base", escapeIRI(contextRoot));
 
     }
 
     /**
      * Writes dcterms:Collection resource of the folder.
      *
      * @param folderContext Folder context IRI
      * @param folder Folder for the vocabularies
      * @param vocabularies concepts collection in the vocabulary
      * @throws XMLStreamException if rdf output fails
      */
     public void writeFolderXml(String folderContext, Folder folder, List<? extends VocabularyFolder> vocabularies)
             throws XMLStreamException {
         // dctype:Collection for Folder:
         writer.writeCharacters("\n");
         writer.writeStartElement(DCTYPE_NS, "Collection");
         writer.writeAttribute("rdf", RDF_NS, "about", escapeIRI(folderContext));
 
         writer.writeCharacters("\n");
         writer.writeStartElement(RDFS_NS, "label");
         writer.writeCharacters(folder.getLabel());
         writer.writeEndElement();
 
         // hasPart relations of concepts:
         for (VocabularyFolder v : vocabularies) {
             writer.writeCharacters("\n");
             writer.writeEmptyElement(DCTERMS_NS, "hasPart");
            writer.writeAttribute("rdf", RDF_NS, "resource", escapeIRI(folderContext + v.getIdentifier() + "/"));
         }
         writer.writeCharacters("\n");
         writer.writeEndElement(); // End Collection
 
     }
 
     /**
      * Writes closing tags of XML.
      *
      * @throws XMLStreamException
      */
     public void writeXmlEnd() throws XMLStreamException {
         writer.writeCharacters("\n");
         writer.writeEndElement(); // End rdf:RDF
         writer.writeCharacters("\n");
     }
 
     /**
      * Writes vocabulary folder XML.
      *
      * @param folderContextRoot
      * @param vocabularyContextRoot
      * @param vocabularyFolder
      * @param vocabularyConcepts
      * @throws XMLStreamException
      */
     public void writeVocabularyFolderXml(String folderContextRoot, String vocabularyContextRoot,
             VocabularyFolder vocabularyFolder, List<? extends VocabularyConcept> vocabularyConcepts) throws XMLStreamException {
         writer.writeCharacters("\n");
         writer.writeStartElement(SKOS_NS, "ConceptScheme");
         writer.writeAttribute("rdf", RDF_NS, "about", escapeIRI(vocabularyContextRoot));
 
         writer.writeCharacters("\n");
         writer.writeStartElement(RDFS_NS, "label");
         writer.writeCharacters(vocabularyFolder.getLabel());
         writer.writeEndElement();
 
         writer.writeCharacters("\n");
 
         writer.writeEmptyElement(DCTERMS_NS, "isPartOf");
         writer.writeAttribute("rdf", RDF_NS, "resource", escapeIRI(folderContextRoot));
 
         writer.writeCharacters("\n");
         writer.writeEndElement(); // End ConceptScheme
 
         for (VocabularyConcept vc : vocabularyConcepts) {
             writer.writeCharacters("\n");
             writer.writeStartElement(SKOS_NS, "Concept");
             writer.writeAttribute("rdf", RDF_NS, "about", escapeIRI(vocabularyContextRoot + vc.getIdentifier()));
 
             if (StringUtils.isNotEmpty(vc.getNotation())) {
                 writer.writeCharacters("\n");
                 writer.writeStartElement(SKOS_NS, "notation");
                 writer.writeCharacters(vc.getNotation());
                 writer.writeEndElement();
             }
 
             writer.writeCharacters("\n");
             writer.writeStartElement(SKOS_NS, "prefLabel");
             writer.writeCharacters(vc.getLabel());
             writer.writeEndElement();
 
             if (StringUtils.isNotEmpty(vc.getDefinition())) {
                 writer.writeCharacters("\n");
                 writer.writeStartElement(SKOS_NS, "definition");
                 writer.writeCharacters(vc.getDefinition());
                 writer.writeEndElement();
             }
 
             writer.writeCharacters("\n");
             writer.writeEmptyElement(SKOS_NS, "inScheme");
             writer.writeAttribute("rdf", RDF_NS, "resource", escapeIRI(vocabularyContextRoot));
 
             if (vocabularyFolder.isSiteCodeType()) {
                 writeSiteCodeData((SiteCode) vc);
             } else {
                 writeAdditionalAttributes(vocabularyContextRoot, vc.getAttributes());
             }
 
             writer.writeCharacters("\n");
             writer.writeEndElement();
         }
     }
 
     /**
      * Writes additional attributes for vocabulary concepts.
      *
      * @param contextRoot
      * @param attributes
      * @throws XMLStreamException
      */
     // Old implementation that will be replaced by data element attributes. See #14721.
     @Deprecated
     private void writeAdditionalAttributes(String contextRoot, List<List<VocabularyConceptAttribute>> attributes)
             throws XMLStreamException {
         if (attributes != null) {
             for (List<VocabularyConceptAttribute> attrs : attributes) {
                 if (attrs != null) {
                     for (VocabularyConceptAttribute attr : attrs) {
                         if (StringUtils.isNotEmpty(attr.getValue()) && StringUtils.isNotEmpty(attr.getRdfPropertyName())) {
                             writer.writeCharacters("\n");
                             if (attr.getDataType().equalsIgnoreCase("reference")) {
                                 writer.writeEmptyElement(attr.getRdfPropertyUri(), attr.getRdfPropertyName());
                                 writer.writeAttribute("rdf", RDF_NS, "resource", attr.getValue());
                             } else {
                                 writer.writeStartElement(attr.getRdfPropertyUri(), attr.getRdfPropertyName());
                                 if (StringUtils.isNotEmpty(attr.getLanguage())) {
                                     writer.writeAttribute("xml", XML_NS, "lang", attr.getLanguage());
                                 }
                                 if (StringUtils.isNotEmpty(attr.getDataType())
                                         && !(attr.getDataType().equalsIgnoreCase("string"))) {
                                     writer.writeAttribute("rdf", RDF_NS, "datatype", Rdf.getXmlType(attr.getDataType()));
                                 }
                                 writer.writeCharacters(attr.getValue());
                                 writer.writeEndElement();
                             }
                         } else if (StringUtils.isNotEmpty(attr.getRelatedIdentifier())
                                 && StringUtils.isNotEmpty(attr.getRdfPropertyName())) {
                             writer.writeCharacters("\n");
                             writer.writeEmptyElement(attr.getRdfPropertyUri(), attr.getRdfPropertyName());
                             writer.writeAttribute("rdf", RDF_NS, "resource", escapeIRI(contextRoot + attr.getRelatedIdentifier()));
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Writes site code specific properties to RDF output.
      *
      * @param sc
      * @throws XMLStreamException
      */
     private void writeSiteCodeData(SiteCode sc) throws XMLStreamException {
         writer.writeCharacters("\n");
         writer.writeStartElement(DD_SCHEMA_NS, "siteCode");
         writer.writeAttribute("rdf", RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#int");
         writer.writeCharacters(sc.getIdentifier());
         writer.writeEndElement();
 
         writer.writeCharacters("\n");
         writer.writeStartElement(DD_SCHEMA_NS, "siteName");
         writer.writeCharacters(sc.getLabel());
         writer.writeEndElement();
 
         writer.writeCharacters("\n");
         writer.writeStartElement(DD_SCHEMA_NS, "status");
         writer.writeCharacters(sc.getStatus().name());
         writer.writeEndElement();
 
         if (StringUtils.isNotEmpty(sc.getCountryCode())) {
             writer.writeCharacters("\n");
             writer.writeEmptyElement(DD_SCHEMA_NS, "countryAllocated");
             writer.writeAttribute("rdf", RDF_NS, "resource",
                     "http://rdfdata.eionet.europa.eu/eea/countries/" + sc.getCountryCode());
         }
 
         if (sc.getDateCreated() != null) {
             Calendar created = Calendar.getInstance();
             created.setTime(sc.getDateCreated());
             writer.writeCharacters("\n");
             writer.writeStartElement(DD_SCHEMA_NS, "yearCreated");
             writer.writeAttribute("rdf", RDF_NS, "datatype", "http://www.w3.org/2001/XMLSchema#gYear");
             writer.writeCharacters(Integer.toString(created.get(Calendar.YEAR)));
             writer.writeEndElement();
         }
 
         if (StringUtils.isNotEmpty(sc.getYearsDeleted())) {
             writer.writeCharacters("\n");
             writer.writeStartElement(DD_SCHEMA_NS, "yearsDeleted");
             writer.writeCharacters(sc.getYearsDeleted());
             writer.writeEndElement();
         }
 
         if (StringUtils.isNotEmpty(sc.getYearsDisappeared())) {
             writer.writeCharacters("\n");
             writer.writeStartElement(DD_SCHEMA_NS, "yearsDisappeared");
             writer.writeCharacters(sc.getYearsDisappeared());
             writer.writeEndElement();
         }
 
         writer.writeCharacters("\n");
         writer.writeEmptyElement(RDF_NS, "type");
         writer.writeAttribute("rdf", RDF_NS, "resource", "http://dd.eionet.europa.eu/schema.rdf#SiteCode");
 
     }
 }
