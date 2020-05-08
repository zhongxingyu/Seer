 /*******************************************************************************
  * Copyright 2012 Pearson Education
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.semantictools.uml.api;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.semantictools.context.renderer.URLRewriter;
 import org.semantictools.context.renderer.model.BaseDocumentMetadata;
 import org.semantictools.context.renderer.model.ContextProperties;
 import org.semantictools.context.renderer.model.GlobalProperties;
 import org.semantictools.context.renderer.model.ReferenceManager;
 import org.semantictools.context.renderer.model.ServiceDocumentation;
 import org.semantictools.context.view.Caption;
 import org.semantictools.context.view.CaptionType;
 import org.semantictools.context.view.DefaultDocumentPrinter;
 import org.semantictools.context.view.DocumentPrinter;
 import org.semantictools.context.view.DocumentPrinterFactory;
 import org.semantictools.context.view.Heading;
 import org.semantictools.context.view.HtmlPrinter;
 import org.semantictools.frame.api.LinkManager;
 import org.semantictools.frame.api.TypeManager;
 import org.semantictools.frame.model.Datatype;
 import org.semantictools.frame.model.Field;
 import org.semantictools.frame.model.Frame;
 import org.semantictools.frame.model.NamedIndividual;
 import org.semantictools.frame.model.OntologyInfo;
 import org.semantictools.frame.model.RdfType;
 import org.semantictools.index.api.LinkedDataIndex;
 import org.semantictools.uml.graphics.ClassDiagram;
 import org.semantictools.uml.model.UmlAssociation;
 import org.semantictools.uml.model.UmlAssociationEnd;
 import org.semantictools.uml.model.UmlClass;
 import org.semantictools.uml.model.UmlManager;
 import org.semantictools.web.upload.AppspotUploadClient;
 
 import com.hp.hpl.jena.rdf.model.Property;
 
 
 public class UmlPrinter extends HtmlPrinter {
   
   private static final String UML_CSS = "uml.css";
 
   private UmlManager umlManager; 
   private String ontologyTitle;
   private String ontologyURI;
   private UmlFileManager linkManager;
   private LinkedDataIndex mediaTypeOracle;
   private AppspotUploadClient uploadClient;
   private LinkManager uploadLinkManager;
   private GlobalProperties global;
   private BaseDocumentMetadata metadata;
   private DocumentPrinter printer;
   
   public UmlPrinter(
       GlobalProperties global,
       URLRewriter rewriter, 
       UmlManager umlManager, 
       UmlFileManager linkManager, 
       LinkedDataIndex oracle) {
     super(rewriter);
     this.global = global==null ? new GlobalProperties() : global;
     this.umlManager = umlManager;
     this.linkManager = linkManager;
     this.mediaTypeOracle = oracle;
   }
 
   public AppspotUploadClient getUploadClient() {
     return uploadClient;
   }
 
   public void setUploadClient(AppspotUploadClient uploadClient) {
     this.uploadClient = uploadClient;
     
     File baseDir = linkManager.getRootDir().getParentFile();
     String baseURI = baseDir.getPath().replace('\\', '/')+'/';
     uploadLinkManager = new LinkManager();
     uploadLinkManager.setBaseURI(baseURI);
    
   }
 
 
 
   public void printAll() throws IOException {
     Iterator<OntologyInfo> sequence = umlManager.getTypeManager().listOntologies().iterator();
     while (sequence.hasNext()) {
       String ontURI = sequence.next().getUri();
       if (global.isIgnoredOntology(ontURI)) continue;
       if (umlManager.getTypeManager().isStandard(ontURI)) continue;
       OntologyInfo info = umlManager.getTypeManager().getOntologyByUri(ontURI);
       if (info == null) continue;
       init(info);
       switch (info.getType()) {
       case RDF: 
         if (info.hasClasses()) {
           printOntology(ontURI); 
         }
         break;
       case XSD: printXmlSchema(info); break;
       }
       
     }
     copyStyleSheet();
   }
   
   private void init(OntologyInfo info) {
 
     this.init();
     this.ontologyURI = info.getUri();
     linkManager.setOntology(ontologyURI);
     
   }
 
   private void printXmlSchema(OntologyInfo info) throws IOException {
 
     String templateName = global.getTemplateName();
     metadata = new BaseDocumentMetadata(global);
     printer = DocumentPrinterFactory.getDefaultFactory().createPrinter(templateName);
     printer.setMetadata(metadata);
     setPrintContext(printer.getPrintContext());
     setCss();
     setXmlSchemaTitle(info);
     printer.beginHTML();
     printer.printTitlePage();
     printer.printTableOfContentsMarker();
     println("<hr/>");
     printDatatypes(info);
     printer.printFooter();
     printer.endHTML();
     
     printer.insertTableOfContents();
     
     writeFile();
     
     
   }
 
   private void printDatatypes(OntologyInfo info) {
 
     List<Datatype> types = listDataTypes(info.getUri());
     for (Datatype type : types) {
       printDatatype(type);
     }
     
   }
 
   private void setXmlSchemaTitle(OntologyInfo info) {
     
     
     ontologyTitle = info.getLabel();
     if (ontologyTitle==null) {
       ontologyTitle = info.getPrefix().toUpperCase() + " XML Schema";
     }
     
     metadata.setTitle(ontologyTitle);
     
   }
 
   private List<Datatype> listDataTypes(String uri) {
     List<Datatype> list = new ArrayList<Datatype>();
     TypeManager typeManager = umlManager.getTypeManager();
     Collection<Datatype> source = typeManager.listDatatypes();
     for (Datatype d : source) {
       String typeURI = d.getUri();
       if (typeURI.startsWith(uri)) {
         list.add(d);
       }
     }
     
     Collections.sort(list, new DatatypeComparator());
     
     return list;
   }
 
   private void printDatatype(Datatype type) {
       
     Heading heading = printer.createHeading(type.getLocalName());
     
     
     heading.setClassName("rdfType");
     print(heading);
     
     String baseURI = type.getBase().getUri();
    
     printer.beginTable("propertiesTable");
     
     printer.beginRow();
     printer.printTH("Restriction&nbsp;Base");
     printer.printTD(baseURI);
     printer.endRow();
     
     printStringFacet("pattern", type.getPattern());
     printNumberFacet("length", type.getLength());
     printNumberFacet("minLength", type.getMinLength());
     printNumberFacet("maxLength", type.getMaxLength());
     printNumberFacet("minInclusive", type.getMinInclusive());
     printNumberFacet("maxInclusive", type.getMaxInclusive());
     printNumberFacet("minExclusive", type.getMinExclusive());
     printNumberFacet("maxExclusive", type.getMaxExclusive());
     printNumberFacet("totalDigits", type.getTotalDigits());
     printNumberFacet("fractionDigits", type.getFractionDigits());
     
     printer.endTable();
 //    
 //    String captionText = "Facets of " + termName;
 //    Caption caption = new Caption(CaptionType.Table, captionText, termName, type.getUri());
 //    assignNumber(caption);
 //    printCaption(caption);
     
   
     
   }
 
   private void printNumberFacet(String name, Number value) {
     
     if (value == null) return;
     printer.beginRow();
     printer.printTH(name);
     printer.printTD(value.toString());
     printer.endRow();
     
   }
 
   private void printStringFacet(String name, String value) {
     if (value == null) return;
     printer.beginRow();
     printer.printTH(name);
     printer.printTD(value);
     printer.endRow();
     
   }
   
   static class DatatypeComparator implements Comparator<Datatype> {
 
     @Override
     public int compare(Datatype a, Datatype b) {
       return a.getLocalName().compareTo(b.getLocalName());
     }
     
   }
 
   private void copyStyleSheet() throws IOException {
     InputStream stream = getClass().getClassLoader().getResourceAsStream(UML_CSS);
     if (stream != null) {
       File cssFile = new File(linkManager.getRootDir(), UML_CSS);
       copyFile(stream, cssFile);
       if (uploadClient != null) {
         uploadClient.upload("text/css", "uml/" + UML_CSS, cssFile);
       }
     }
     
   }
   
 
   private void copyFile(InputStream stream, File cssFile) throws IOException  {
    
     File parent = cssFile.getParentFile();
     parent.mkdirs();
     FileOutputStream out = new FileOutputStream(cssFile);
     try {
       byte[] buffer = new byte[1024];
       
       int len;
       while ( (len = stream.read(buffer)) > 0) {
         out.write(buffer, 0, len);
       }
       
     } finally {
       out.close();
     }
     
   }
 
   
   public void printOntology(String ontologyURI) throws IOException {
     // TODO: use a proper reference map.
     
     String templateName = global.getTemplateName();
     metadata = new BaseDocumentMetadata(global);
     printer = DocumentPrinterFactory.getDefaultFactory().createPrinter(templateName);
     printer.setMetadata(metadata);
     setPrintContext(printer.getPrintContext());
     setCss();
     setTitle();
     printer.beginHTML();
     printer.printTitlePage();
     printer.printTableOfContentsMarker();
     printDataModel();
     printer.printFooter();
     printer.endHTML();
     
     printer.insertTableOfContents();
     
     writeFile();
   }
 
   private void setCss() {
     // TODO: should delegate to UmlFileManager
     
     String ontoPath = linkManager.getOntologyDir().getAbsolutePath();
     String basePath = linkManager.getRootDir().getAbsolutePath();
     
     String relativePath = ontoPath.substring(basePath.length());
     relativePath = relativePath.replace('\\', '/');
     int count = dirCount(relativePath);
     StringBuilder builder = new StringBuilder();
     for (int i=0; i<count; i++) {
       builder.append("../");
     }
     builder.append(UML_CSS);
     
     String css = builder.toString();
     metadata.setCss(css);
   }
 
   private void setTitle() {
     OntologyInfo info = umlManager.getTypeManager().getOntologyByUri(ontologyURI);
     ontologyTitle =  ontologyURI;
     if (info != null && info.getLabel()!=null) {
       ontologyTitle = info.getLabel();
     }
     
     metadata.setTitle(ontologyTitle);
     
     
   }
 
   private void writeFile() throws IOException {
     
     File file = getOntologyIndexFile();
     file.getParentFile().mkdirs();
     String text = printer.popText();
     
     FileWriter writer = new FileWriter(file);
     try {
       writer.write(text);
       writer.flush();
     } catch (IOException oops) {
       throw oops;
     } finally {
       writer.close();
     }
     
     uploadFile("text/html", file);
     
     
   }
 
   private void uploadFile(String contentType, File file) throws IOException {
     if (uploadClient == null) return;
     String path = file.getPath().replace("\\", "/");
     path = uploadLinkManager.relativize(path);    
     uploadClient.upload(contentType, path, file);
     
   }
 
   private File getOntologyIndexFile() {
     return linkManager.getOntologyAllFile();
   }
 
 
   private void printTitle() {
     
     
     print("<H1>");
     print(ontologyTitle);
     println("</H1>");
     
   }
 
   private void printDataModel() throws IOException {
     
     List<UmlClass> list = getClassList();
     for (UmlClass umlClass : list) {
       String uri = umlClass.getURI();
       if (!uri.startsWith(ontologyURI)) continue;
       printUmlClass(umlClass);
     }
     
   }
   
   private List<UmlClass> getClassList() {
 
     List<UmlClass> list = new ArrayList<UmlClass>();
     for (UmlClass umlClass : umlManager.listUmlClasses()) {
       String uri = umlClass.getURI();
       if (!uri.startsWith(ontologyURI)) continue;
       list.add(umlClass);
     }
     Collections.sort(list, new UmlClassComparator());
     return list;
   }
   
   static class UmlClassComparator implements Comparator<UmlClass> {
 
     @Override
     public int compare(UmlClass a, UmlClass b) {
       String aName = a.getLocalName();
       String bName = b.getLocalName();
       return aName.compareTo(bName);
     }
     
   }
 
   private boolean exists(String value) {
     return value != null && value.length()>0;
   }
 
   private void printUmlClass(UmlClass umlClass) throws IOException {
     String localName = umlClass.getLocalName();
     
     println("<HR/>");
     
     Heading heading = printer.createHeading(localName, linkManager.getTypeId(umlClass.getType()));
     print(heading);
     pushIndent();
     
     printClassDiagram(umlClass);
     printSupertypes(umlClass);
     printSubtypes(umlClass);
     printClassUses(umlClass);
     printMediaTypes(umlClass);
     printDescription(umlClass);
     printPropertyTable(umlClass);
     printInheritedProperties(umlClass);
     printNamedInstances(umlClass);
     
     popIndent();
   }
 
 
   private void printClassUses(UmlClass umlClass) {
     
     List<UmlAssociation> list = umlClass.getParentList();
     if (list == null || list.isEmpty()) return;
     
     printer.beginDiv("list-heading");
     print("Known Uses:");    
     printer.endDiv();
     indent();
     println("<UL>");
     pushIndent();
     for (UmlAssociation a : list) {
       UmlAssociationEnd end = a.getOtherEnd(umlClass);
       String classHref = linkManager.getTypeHref(end.getParticipant().getType());
       String className = end.getParticipant().getLocalName();
       
       Field field = a.getSelfEnd(umlClass).getField();
       
       indent();
       print("<LI>");
       printer.printAnchor(classHref, className);
       if (field != null) {
         Property property = field.getProperty();
         String fieldName = field.getLocalName();
         String fieldHref = linkManager.getPropertyHref(end.getParticipant(), property);
         print(".");
         printer.printAnchor(fieldHref, fieldName);
       }
       
     }
     
     popIndent();
     indent();
     println("</UL>");
     
   }
 
   private void printMediaTypes(UmlClass umlClass) {
     if (mediaTypeOracle == null) return;
     
     
     List<ContextProperties> mediaTypeList = mediaTypeOracle.listMediaTypesForClass(umlClass.getURI());
     List<ServiceDocumentation> list = mediaTypeOracle.getServiceDocumentationForClass(umlClass.getURI());
     if (list == null) {
       list = new ArrayList<ServiceDocumentation>();
     }
     
     int count = mediaTypeList.size() + list.size();
     if (count==0) return;
 
 
     printer.beginDiv("list-heading");
     print("See Also:");
     printer.endDiv();
     println("<UL>");
     pushIndent();
     
     for (int i=0; i<list.size(); i++) {
       
       ServiceDocumentation doc = list.get(i);
       String title = doc.getTitle().replace("<br>", " ").replace("<br/>", " ");
 
       String serviceHref = linkManager.relativize(doc.getServiceDocumentationFile());
       indent();
       print("<LI>");
       printer.printAnchor(serviceHref, title);
     }
     for (ContextProperties context : mediaTypeList) {
       String mediaType = context.getMediaType();
       String href = linkManager.relativize(context.getMediaTypeDocFile());
       indent();
       print("<LI>");
       printer.printAnchor(href, mediaType);
     }
     popIndent();    
     println("</UL>");
     
   }
 
 
   private void printDescription(UmlClass umlClass) {
 
     String description = umlClass.getDescription();
     if (exists(description)) {
       printer.printParagraph(description);
     }
 
     
   }
 
   private void printSupertypes(UmlClass umlClass) {
     List<UmlClass> superList = umlClass.getSupertypeList();
     if (superList == null || superList.isEmpty()) return;
     
     printer.beginDiv("list-heading");
     print("Direct Known Supertypes:");
     printer.endDiv();
     printer.beginDiv("running-list");
     String comma = "";
     for (UmlClass supertype : superList) {
       print(comma);
       print(linkManager.getTypeLink(supertype.getType()));
       comma = ", ";
     }
     
     printer.endDiv();
     
   }
 
   private void printSubtypes(UmlClass umlClass) {
     List<UmlClass> subList = umlClass.getSubtypeList();
     if (subList == null || subList.isEmpty()) return;
     
     printer.beginDiv("list-heading");
     print("Direct Known Subtypes:");
     printer.endDiv();
     printer.beginDiv("running-list");
     String comma = "";
     for (UmlClass subtype : subList) {
       print(comma);
       print(linkManager.getTypeLink(subtype.getType()));
       comma = ", ";
     }
     
     printer.endDiv();
     
   }
 
   private void printInheritedProperties(UmlClass umlClass) {
     
     List<UmlClass> list = umlClass.getSupertypeList();
     if (list.isEmpty()) return;
     
     list = new ArrayList<UmlClass>(list);
     Collections.sort(list, new UmlClassComparator());
     
     for (UmlClass superclass : list) {
       printInheritedProperties(umlClass, superclass);
     }
     
     
     
   }
 
   private void printInheritedProperties(UmlClass umlClass, UmlClass superclass) {
     
     RdfType superType = superclass.getType();
     if (!superType.canAsFrame()) return;
     
     List<Field> list = superType.asFrame().getDeclaredFields();
     if (list==null || list.isEmpty()) return;
     
     String title = "Properties inherited from " + superclass.getLocalName();
     printer.beginTable("propertiesTable");
     printer.beginRow();
     printer.printTH(title);
     printer.endRow();
     printer.beginRow();
     indent();
     print("<TD>");
     
     String comma = "";
     for (Field field : list) {
       print(comma);
       String id = null;
       id = linkManager.getPropertyHref(superclass, field.getProperty());
       if (id != null) {
         print("<a href=\"");
         print(id);
         print("\">");
       }
       String name = field.getLocalName();
       print(name);
       if (id != null) {
         print("</a>");
       }
       comma = ", ";
     }
     println();
     indent();
     println("</TD>");
     
     
     printer.endRow();
     
     printer.endTable();
     
     
   }
 
   private void printClassDiagram(UmlClass umlClass) throws IOException {
     
     
     File file = linkManager.getUmlClassImageFile(umlClass);
     ClassDiagram diagram = new ClassDiagram(umlClass);
     BufferedImage image = diagram.getImage();
     ImageIO.write(image, "png", file);
     uploadFile("image/png", file);
     
     String src = "images/" + file.getName();
 //    String captionText = umlClass.getLocalName() + " Class";
 //    String id = file.getName();
 //    Caption caption = new Caption(CaptionType.Figure, captionText, id, null);
 //    
 //    assignNumber(caption);
     
     printer.printFigure(src, null);
     
   }
 
   private void printNamedInstances(UmlClass umlClass) {
     
     
     List<NamedIndividual> list = umlClass.listInstances(false);
     if (list.isEmpty()) return;
     
     Collections.sort(list, new Comparator<NamedIndividual>() {
 
       @Override
       public int compare(NamedIndividual a, NamedIndividual b) {
        return
            (a.getLocalName() != null && b.getLocalName()!=null) ? a.getLocalName().compareTo(b.getLocalName()) :
            0;
       }
     });
     
     printer.beginDiv("list-heading");
     print("Known Instances:");
     printer.endDiv();
     printer.beginTable("propertiesTable");
     printer.beginRow();
     println("<TH>Simple Name</TH><TH> Description / URI</TH>");
     printer.endRow();
     for (NamedIndividual n : list) {
       String comment = n.getComment();
       printer.beginRow();
       print("<TD>");
       print(n.getLocalName());
       print("</TD>");
       print("<TD>");
       if (comment != null) {
         print("<P>");
         print(comment);
         print("</P>");
       }
       print("<code>");
       print(n.getUri());
       print("</code>");
       print("</TD>");
       printer.endRow();
     }
     printer.endTable();
     
   }
 
   private void printPropertyTable(UmlClass umlClass) {
     
     RdfType type = umlClass.getType();
     if (!type.canAsFrame()) return;
     
     Frame frame = type.asFrame();
     List<Field> list = frame.getDeclaredFields();
     
     if (list.isEmpty()) return;
     
     String tableId = umlClass.getLocalName() + ".properties";
     String captionText = format("{0} Properties", umlClass.getLocalName());
     Caption caption = new Caption(CaptionType.Table, captionText, tableId, null);
     
     printer.assignNumber(caption);
     
     String title = format("Properties of class {0}", umlClass.getLocalName());
     
     
     printer.beginTable("propertiesTable");
 
     printer.beginRow();
     println("<TH colspan=\"3\">" + title + "</TH>");
     printer.endRow();
     
     TypeManager typeManager = umlManager.getTypeManager();
     
     // TODO: print anchor tag for Type.
     for (Field field : list) {
       
       RdfType fieldType = field.getRdfType();
       
       if (fieldType.canAsListType()) {
         fieldType = fieldType.asListType().getElementType();
       }
       
       String link = typeManager.isStandard(fieldType.getNamespace()) ? fieldType.getLocalName() :
         linkManager.getTypeLink(fieldType);
       
       String propertyId = linkManager.getPropertyId(umlClass, field.getProperty());
       
       printer.beginRow();
       indent();
       print("<TD>");
       printer.beginDiv("propertyName", propertyId);
       indent();
       print(field.getLocalName());
       printer.endDiv();
       printer.beginDiv("description");
       indent();
       print(field.getComment());
       printer.endDiv();
       print("</TD>");
       printer.printTD("propertyType", link);
       printer.printTD("multiplicity", field.getMultiplicity());
       printer.endRow();
     }
     
 //    beginRow();
 //    printTH("Name");
 //    printTH("Mult");
 //    printTH("Description");
 //    printTH("Type");
 //    endRow();
 //    
 //    TypeManager typeManager = umlManager.getTypeManager();
 //    
 //    // TODO: print anchor tag for Type.
 //    for (UmlProperty p : list) {
 //      
 //      RdfType fieldType = p.getField().getRdfType();
 //      
 //      String link = typeManager.isStandard(fieldType.getNamespace()) ? fieldType.getLocalName() :
 //        linkManager.getTypeLink(fieldType);
 //      
 //      beginRow();
 //      printTD(p.getLocalName());
 //      printTD(p.getMultiplicity());
 //      printTD(p.getDescription());
 //      printTD(link);
 //      endRow();
 //    }
     
     printer.endTable();
     
   }
 
 
   private int dirCount(String relativePath) {
     int count = 0;
     int index = -1;
     
     while ( (index = relativePath.indexOf('/', index+1)) >= 0) {
       count++;
     }
     
     return count;
   }
 
 }
