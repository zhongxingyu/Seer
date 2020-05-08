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
 import java.util.List;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import eionet.meta.dao.domain.Schema;
 import eionet.meta.dao.domain.SchemaSet;
 import eionet.meta.service.ISchemaService;
 import eionet.meta.service.ServiceException;
 import eionet.meta.service.data.SchemaFilter;
 import eionet.meta.service.data.SchemaSetFilter;
 import eionet.meta.service.data.SchemaSetsResult;
 import eionet.meta.service.data.SchemasResult;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 
 /**
  * Schemas RDF manifest xml writer.
  *
  * @author Juhan Voolaid
  */
 public class SchemasManifestXmlWriter {
 
     private static final String ENCODING = "UTF-8";
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
     private static final String DCT_NS = "http://purl.org/dc/terms/";
     private static final String VOID_NS = "http://rdfs.org/ns/void#";
     private static final String DD_NS = "http://dd.eionet.europa.eu/schema.rdf#";
     private static final String CR_NS = "http://cr.eionet.europa.eu/ontologies/contreg.rdf#";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
 
     private String contextRoot;
 
     /**
      * XMLWriter to write XML to.
      */
     private XMLStreamWriter writer = null;
 
     private ISchemaService schemaService;
 
     /**
      * Class constructor.
      *
      * @param out
      * @param contextRoot
      * @throws XMLStreamException
      */
     public SchemasManifestXmlWriter(OutputStream out, String contextRoot, ISchemaService schemaService) throws XMLStreamException {
         writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
         this.contextRoot = contextRoot;
         this.schemaService = schemaService;
     }
 
     /**
      * Writes manifest xml of schema sets data.
      *
      * @param schemaSets
      * @throws XMLStreamException
      * @throws ServiceException
      */
     public void writeManifestXml() throws XMLStreamException, ServiceException {
         SchemaSetFilter schemaSetFilter = new SchemaSetFilter();
         SchemaSetsResult schemaSetsResult = schemaService.searchSchemaSets(schemaSetFilter);
         String webAppUrl = Props.getRequiredProperty(PropsIF.DD_URL);
 
         writer.writeStartDocument(ENCODING, "1.0");
 
         writer.setPrefix("rdf", RDF_NS);
         writer.setPrefix("rdfs", RDFS_NS);
         writer.setPrefix("dd", DD_NS);
         writer.setPrefix("cr", CR_NS);
 
         writer.writeStartElement("rdf", "RDF", RDF_NS);
         writer.writeNamespace("rdf", RDF_NS);
         writer.writeNamespace("rdfs", RDFS_NS);
         writer.writeNamespace("dd", DD_NS);
         writer.writeNamespace("cr", CR_NS);
         writer.writeAttribute("xml", XML_NS, "base", webAppUrl);
 
         for (SchemaSet ss : schemaSetsResult.getList()) {
 
             writer.writeStartElement("dd", "SchemaSet", DD_NS);
            writer.writeAttribute("rdf", RDF_NS, "about", "schemaset/" + ss.getIdentifier());
 
             writer.writeStartElement(RDFS_NS, "label");
             writer.writeCharacters(ss.getNameAttribute());
             writer.writeEndElement();
 
             List<Schema> schemas = schemaService.listSchemaSetSchemas(ss.getId());
 
             for (Schema s : schemas) {
                 writer.writeStartElement("dd", "hasSchema", DD_NS);
                writer.writeAttribute("rdf", RDF_NS, "resource", "schemas/" + ss.getIdentifier() + "/" + s.getFileName());
                 writer.writeEndElement();
             }
 
             writer.writeEndElement();
 
         }
 
         SchemaFilter schemaFilter = new SchemaFilter();
         SchemasResult schemaResult = schemaService.searchSchemas(schemaFilter);
 
         for (Schema s : schemaResult.getList()) {
             String schemaUri = null;
             if (s.getSchemaSetIdentifier() == null) {
                 schemaUri = "schema/" + SchemaSet.ROOT_IDENTIFIER + "/" + s.getFileName();
             } else {
                 schemaUri = "schema/" + s.getSchemaSetIdentifier() + "/" + s.getFileName();
             }
             writer.writeStartElement("cr", "XMLSchema", CR_NS);
             writer.writeAttribute("rdf", RDF_NS, "about", schemaUri);
 
             writer.writeStartElement(RDFS_NS, "label");
             writer.writeCharacters(s.getNameAttribute());
             writer.writeEndElement();
 
             writer.writeEndElement();
         }
 
         writer.writeEndDocument();
     }
 }
