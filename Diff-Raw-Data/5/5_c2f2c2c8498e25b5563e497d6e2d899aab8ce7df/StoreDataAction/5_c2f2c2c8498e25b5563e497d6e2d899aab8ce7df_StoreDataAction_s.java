 package com.turbulence.core.actions;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import java.util.UUID;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.lang.UnhandledException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.hp.hpl.jena.rdf.arp.ALiteral;
 import com.hp.hpl.jena.rdf.arp.AResource;
 import com.hp.hpl.jena.rdf.arp.ARP;
 import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
 import com.hp.hpl.jena.rdf.arp.ARPEventHandler;
 import com.hp.hpl.jena.rdf.arp.NamespaceHandler;
 import com.hp.hpl.jena.rdf.arp.StatementHandler;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFWriter;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 import com.hp.hpl.jena.vocabulary.RDF;
 
 import com.turbulence.core.TurbulenceDriver;
 
 import me.prettyprint.cassandra.model.*;
 import me.prettyprint.cassandra.serializers.*;
 import me.prettyprint.cassandra.service.*;
 import me.prettyprint.cassandra.service.template.*;
 import me.prettyprint.hector.api.*;
 import me.prettyprint.hector.api.ddl.*;
 import me.prettyprint.hector.api.exceptions.*;
 import me.prettyprint.hector.api.factory.HFactory;
 
 public class StoreDataAction implements Action, ARPEventHandler, ErrorHandler {
     private static final Log logger = LogFactory.getLog(StoreDataAction.class);
 
     private InputStream input;
     private ARP rdfParser;
     private XMLOutputFactory outputFactory;
 
     Model currentModel;
     Resource currentSubject = null;
 
     ColumnFamilyTemplate<String, String> conceptsTemplate;
     ColumnFamilyTemplate<String, String> conceptsInstanceDataTemplate;
     SuperCfTemplate<String, String, String> triplesTemplate;
     protected StoreDataAction(InputStream in) {
         input = in;
         rdfParser = new ARP();
         rdfParser.getHandlers().setErrorHandler(this);
         rdfParser.getHandlers().setExtendedHandler(this);
         rdfParser.getHandlers().setNamespaceHandler(this);
         rdfParser.getHandlers().setStatementHandler(this);
         //rdfParser.getOptions().setLaxErrorMode();
 
         conceptsTemplate = TurbulenceDriver.getConceptsTemplate();
         conceptsInstanceDataTemplate = TurbulenceDriver.getConceptsInstanceDataTemplate();
         triplesTemplate = TurbulenceDriver.getTriplesTemplate();
 
         currentModel = ModelFactory.createDefaultModel();
     }
 
     public Result perform() {
         Result r = new Result();
 
         try {
             rdfParser.load(input);
             r.success = true;
         } catch (SAXException e) {
             r.success = false;
             r.error = TurbulenceError.BAD_XML_DATA;
             r.message = e.getMessage();
         } catch (IOException e) {
             r.success = false;
             r.error = TurbulenceError.IO_ERROR;
             r.message = e.getMessage();
         }
         return r;
     }
 
     public void error(SAXParseException exception) {
     }
     public void fatalError(SAXParseException exception) {
     }
     public void warning(SAXParseException exception) {
     }
 
     public void startPrefixMapping(String prefix, String uri) {
         currentModel.setNsPrefix(prefix, uri);
     }
 
     public void endPrefixMapping(String prefix) {
         currentModel.removeNsPrefix(prefix);
     }
 
     public void statement(AResource subject, AResource predicate, AResource object) {
         if (currentSubject == null || !currentSubject.getURI().equals(subject.getURI())) {
             dumpRDFInstance();
             resetRDFInstance(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI());
         }
 
         currentSubject.addProperty(currentModel.createProperty(predicate.getURI()), currentModel.createResource(object.getURI()));
 
         if (predicate.getURI().equals(RDF.type.getURI())) {
             //logger.warn("Saving new entry of type " + object.getURI());
             ColumnFamilyUpdater<String, String> updater = conceptsTemplate.createUpdater(object.getURI());
 
             if (subject.isAnonymous())
                 updater.setString(DigestUtils.md5Hex(subject.getAnonymousID()), subject.getAnonymousID());
             else
                 updater.setString(DigestUtils.md5Hex(subject.getURI()), subject.getURI());
 
             try {
                 conceptsTemplate.update(updater);
             } catch (HectorException e) {
                 throw new UnhandledException(e);
             }
         }
         else {
             saveTriple(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI(), predicate.getURI(), object.isAnonymous() ? object.getAnonymousID() : object.getURI());
         }
         //logger.warn("statementAResource: " + subject + " " + predicate + " " + object);
     }
 
     public void statement(AResource subject, AResource predicate, ALiteral object) {
         if (currentSubject == null || !currentSubject.getURI().equals(subject.getURI())) {
             dumpRDFInstance();
             resetRDFInstance(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI());
         }
         currentSubject.addProperty(currentModel.createProperty(predicate.getURI()), object.toString());
         saveTriple(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI(), predicate.getURI(), object.toString());
         //logger.warn("statementALiteral: " + subject + " " + predicate + " " + object);
     }
 
     private void saveTriple(String subject, String predicate, String object) {
         //logger.warn("Saving triple with row key " + subject + " SCol: " + predicate + " colName: " + DigestUtils.md5Hex(object) + " colValue: " + object);
         SuperCfUpdater<String, String, String> updater = triplesTemplate.createUpdater(subject, predicate);
         updater.setString(DigestUtils.md5Hex(object), object);
         try {
             triplesTemplate.update(updater);
         } catch (HectorException e) {
             throw new UnhandledException(e);
         }
     }
 
     private void dumpRDFInstance() {
         if (currentSubject == null || currentSubject.getPropertyResourceValue(RDF.type) == null)
             return;
         StringWriter w = new StringWriter();
         RDFWriter writer = currentModel.getWriter();
         writer.setProperty("allowBadURIs", true);
         writer.write(currentModel, w, null);
         //logger.warn("Will save dump " + w.toString());
         //logger.warn("to row " + currentSubject.getPropertyResourceValue(RDF.type).getURI() + " column " + currentSubject.getURI());
         ColumnFamilyUpdater<String, String> updater = conceptsInstanceDataTemplate.createUpdater(currentSubject.getPropertyResourceValue(RDF.type).getURI());
 
         if (currentSubject.isAnon())
             updater.setString(currentSubject.getId().toString(), w.toString());
         else
             updater.setString(currentSubject.getURI(), w.toString());
         try {
             conceptsInstanceDataTemplate.update(updater);
         } catch (HectorException e) {
             throw new UnhandledException(e);
         }
     }
 
     private void resetRDFInstance(String subject) {
         currentModel.removeAll();
         currentSubject = currentModel.createResource(subject);
     }
 
     public void endBNodeScope(AResource bnode)
     {
     }
 
     public boolean discardNodesWithNodeID()
     {
         return false;
     }
 
     public void startRDF()
     {
     }
 
     public void endRDF()
     {
         // take care of the last subject
         dumpRDFInstance();
     }
 }
