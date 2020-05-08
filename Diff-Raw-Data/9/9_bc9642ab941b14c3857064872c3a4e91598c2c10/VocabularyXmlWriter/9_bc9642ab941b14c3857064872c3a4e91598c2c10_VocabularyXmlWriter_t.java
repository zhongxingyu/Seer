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
 
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.ServiceException;
 import eionet.meta.service.data.SiteCode;
 
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
     private static final String DD_SCHEMA_NS = "http://dd.eionet.europa.eu/schema.rdf#";
 
     /** Characters that aren't allowed in IRIs. */
     private static final String[] BAD_IRI_CHARS = {" ", "{", "}", "<", ">", "\"", "|", "\\", "^", "`"};
     /** Replacements for characters that aren't allowed in IRIs. */
     private static final String[] BAD_IRI_CHARS_ESCAPES = {"%20", "%7B", "%7D", "%3C", "%3E", "%22", "%7C", "%5C", "%5E", "%60"};
 
     /** The base URI of the concept. It must end with a slash (/). */
     private String contextRoot;
 
     /**
      * XMLWriter to write XML to.
      */
     private XMLStreamWriter writer = null;
 
     /** Objects to write to output. */
     private VocabularyFolder vocabularyFolder;
     private List<? extends VocabularyConcept> vocabularyConcepts;
 
     /**
      * Class constructor.
      *
      * @param out
      * @param contextRoot
      * @param vocabularyService
      * @throws XMLStreamException
      */
     public VocabularyXmlWriter(OutputStream out, String contextRoot, VocabularyFolder vocabularyFolder,
             List<? extends VocabularyConcept> vocabularyConcepts) throws XMLStreamException {
         writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
         this.contextRoot = contextRoot;
         this.vocabularyFolder = vocabularyFolder;
         this.vocabularyConcepts = vocabularyConcepts;
     }
 
     /**
      * Escapes IRI's reserved characters in the given URL string.
      *
      * @param url
      *            is a string.
      * @return escaped URI
      */
     public static String escapeIRI(String url) {
 
         return url == null ? null : StringUtils.replaceEach(url, BAD_IRI_CHARS, BAD_IRI_CHARS_ESCAPES);
     }
 
     /**
      * Writes rdf output to stream.
      *
      * @throws XMLStreamException
      * @throws ServiceException
      */
     public void writeManifestXml() throws XMLStreamException, ServiceException {
 
         writer.writeStartDocument(ENCODING, "1.0");
 
         writer.setPrefix("rdf", RDF_NS);
         writer.setPrefix("rdfs", RDFS_NS);
         writer.setPrefix("skos", SKOS_NS);
         if (vocabularyFolder.isSiteCodeType()) {
             writer.setPrefix("dd", DD_SCHEMA_NS);
         }
 
         writer.writeStartElement("rdf", "RDF", RDF_NS);
         writer.writeNamespace("rdf", RDF_NS);
         writer.writeNamespace("rdfs", RDFS_NS);
         writer.writeNamespace("skos", SKOS_NS);
         if (vocabularyFolder.isSiteCodeType()) {
             writer.writeNamespace("dd", DD_SCHEMA_NS);
         }
         writer.writeAttribute("xml", XML_NS, "base", escapeIRI(contextRoot));
 
         writer.writeCharacters("\n");
         writer.writeStartElement(SKOS_NS, "ConceptScheme");
         writer.writeAttribute("rdf", RDF_NS, "about", escapeIRI(contextRoot));
 
         writer.writeCharacters("\n");
         writer.writeStartElement(RDFS_NS, "label");
         writer.writeCharacters(vocabularyFolder.getLabel());
         writer.writeEndElement();
 
         writer.writeCharacters("\n");
         writer.writeEndElement(); // End ConceptScheme
 
         for (VocabularyConcept vc : vocabularyConcepts) {
             writer.writeCharacters("\n");
             writer.writeStartElement(SKOS_NS, "Concept");
             writer.writeAttribute("rdf", RDF_NS, "about", escapeIRI(contextRoot + vc.getIdentifier()));
 
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
 
             writer.writeCharacters("\n");
             writer.writeEmptyElement(SKOS_NS, "inScheme");
             writer.writeAttribute("rdf", RDF_NS, "resource", escapeIRI(contextRoot));
 
             if (vocabularyFolder.isSiteCodeType()) {
                 writeSiteCodeData((SiteCode) vc);
             }
 
             writer.writeCharacters("\n");
             writer.writeEndElement();
         }
 
         writer.writeCharacters("\n");
         writer.writeEndElement(); // End rdf:RDF
         writer.writeCharacters("\n");
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
 
         if (sc.getDateCreated() != null){
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
