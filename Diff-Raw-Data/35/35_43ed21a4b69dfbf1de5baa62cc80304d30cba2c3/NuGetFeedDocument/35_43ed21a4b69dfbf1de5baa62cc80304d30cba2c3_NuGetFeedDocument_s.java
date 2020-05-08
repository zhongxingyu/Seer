 package com.tw.go.plugin.nuget;
 
 import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
 import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.util.Date;
 
 public class NuGetFeedDocument {
     public static final String SCHEMA_ADO_DATASERVICES = "http://schemas.microsoft.com/ado/2007/08/dataservices";
     public static final String SCHEMA_ADO_DATASERVICES_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
     private final Document xmlFeed;
 
     public NuGetFeedDocument(Document xmlDocument) {
         this.xmlFeed = xmlDocument;
     }
 
     String getPackageLocation() {
         return firstOf(xmlFeed.getElementsByTagName("content")).getAttributes().getNamedItem("src").getTextContent();
     }
 
     String getAuthor() {
         return xmlFeed.getElementsByTagName("name").item(0).getTextContent();
     }
 
     Date getPublishedDate() {
         return javax.xml.bind.DatatypeConverter.parseDateTime(getProperty(getProperties(), "Published")).getTime();
     }
 
     private String getProperty(NodeList properties, String name) {
         return firstOf(properties).getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES, name).item(0).getTextContent();
     }
 
     private NodeList getProperties() {
         return xmlFeed.getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES_METADATA, "properties");
     }
 
     String getEntryTitle() {
         NodeList titles = firstOf(getEntries()).getElementsByTagName("title");
         return titles.item(0).getTextContent();
     }
 
     NodeList getEntries() {
         return xmlFeed.getElementsByTagName("entry");
     }
 
     private Element firstOf(NodeList nodeList) {
         return ((Element) nodeList.item(0));
     }
 
     public String getPackageVersion() {
         return getProperty(getProperties(), "Version");
     }
 
     public PackageRevision getPackageRevision(boolean lastVersionKnown) {
         if (getEntries().getLength() == 0){
             if(lastVersionKnown) return null;
             else throw new NuGetException("No such package found");
         }
         if (getEntries().getLength() > 1)
             throw new NuGetException(String.format("Multiple entries in feed for %s %s", getEntryTitle(), getPackageVersion()));
         PackageRevision result = new PackageRevision(getPackageLabel(), getPublishedDate(), getAuthor(), getReleaseNotes(), getProjectUrl());
         result.addData(NuGetPackageConfig.PACKAGE_LOCATION, getPackageLocation());
         result.addData(NuGetPackageConfig.PACKAGE_DESCRIPTION, getDescriptionSummary());
         result.addData(NuGetPackageConfig.PACKAGE_VERSION, getPackageVersion());
         return result;
     }
 
     private String getReleaseNotes() {
         String releaseNotes = getProperty(getProperties(), "ReleaseNotes");
         if(releaseNotes == null || releaseNotes.trim().isEmpty()) return null;
         return firstNonEmptyLine(releaseNotes);
     }
 
     private String firstNonEmptyLine(String s) {
         String[] lines = s.split("\n");
         for(String line : lines){
             if(!line.trim().isEmpty())
                 return line.trim();
         }
         return null;
     }
 
     private String getProjectUrl() {
         String projectUrl = getProperty(getProperties(), "ProjectUrl");
         if(projectUrl == null || projectUrl.trim().isEmpty()) return null;
         return firstNonEmptyLine(projectUrl);
     }
 
     private String getDescriptionSummary() {
         String summary = getEntrySummary();
         if(summary == null || summary.trim().isEmpty()) return getFirstLineOfDescription();
         return summary;
     }
 
     private String getFirstLineOfDescription() {
         String description = getProperty(getProperties(), "Description");
         return firstNonEmptyLine(description);
     }
 
     private String getEntrySummary() {
         NodeList summary = firstOf(getEntries()).getElementsByTagName("summary");
         return summary.item(0).getTextContent();
     }
 
     private String getPackageLabel() {
         return getEntryTitle() + "-" + getPackageVersion();
     }
 }
