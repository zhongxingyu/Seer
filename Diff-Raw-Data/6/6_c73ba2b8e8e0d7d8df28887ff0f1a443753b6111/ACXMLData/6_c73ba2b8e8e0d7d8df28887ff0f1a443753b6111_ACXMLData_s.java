 // Copyright (c) 2007 ScenPro, Inc.
 
// $Header: /share/content/gforge/sentinel/sentinel/src/gov/nih/nci/cadsr/sentinel/tool/ACXMLData.java,v 1.5 2008-01-14 18:56:22 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.sentinel.tool;
 
 import gov.nih.nci.cadsr.sentinel.database.DBAlert;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Timestamp;
 import java.util.*;
 
 
 import org.apache.log4j.Logger;
 import org.jdom.DocType;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Attribute;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.stream.XMLStreamException;
 
 /**
  * This is the working class for the collection, assimilation and processing of caDSR data by the Sentinel Alert Auto Process server and generating an XML file.
  * Jdom parser is used to build the tree structure. Date: Sep 8, 2007 Time: 7:37:14 PM
  */
 public class ACXMLData
 {
     /** **/
     public static final String _elementCadsr = "cadsr";
     /** **/
     public static final String _attrDocType = "cadsrsentinel-modified-1.0.dtd";
     /** **/
     public static final String _elementAlertReport = "alertReport";
     /** **/
     public static final String _attrSoftwareName = "softwareName";
     /** **/
     public static final String _attrSoftwareVersion =  "softwareVersion";
     /** **/
     public static final String _attrVersion =  "version";
     /** **/
     public static final String _elementDatabase = "database";
     /** **/
     public static final String _attrServer = "server";
     /** **/
     public static final String _attrName = "name";
     /** **/
     public static final String _attrRAI = "rai";
     /** **/
     public static final String _elementDefinition = "definition";
     /** **/
     public static final String _elementName = "name";
     /** **/
     public static final String _attrValue = "value";
     /** **/
     public static final String _elementId = "id";
     /** **/
     public static final String _elementIntro = "intro";
     /** **/
     public static final String _elementCreatedBy = "createdBy";
     /** **/
     public static final String _attrUser = "user";
     /** **/
     public static final String _attrEmail = "email";
     /** **/
     public static final String _elementRecipient = "recipient";
     /** **/
     public static final String _attrUrl = "url";
     /** **/
     public static final String _elementSummary = "summary";
     /** **/
     public static final String _elementLastAutoRun = "lastAutoRun";
     /** **/
     public static final String _attrTime = "time";
     /** **/
     public static final String _elementFrequency = "frequency";
     /** **/
     public static final String _attrUnit = "unit";
     /** **/
     public static final String _elementStatus = "status";
     /** **/
     public static final String _attrCode = "code";
     /** **/
     public static final String _attrBeginDate = "beginDate";
     /** **/
     public static final String _attrEndDate = "endDate";
     /** **/
     public static final String _elementLevel = "level";
     /** **/
     public static final String _attrDepth = "depth";
     /** **/
     public static final String _elementStart = "start";
     /** **/
     public static final String _elementEnd = "end";
     /** **/
     public static final String _elementCreatedOn = "createdOn";
     /** **/
     public static final String _attrDate = "date";
     /** **/
     public static final String _elementGroup = "group";
     /** **/
     public static final String _elementAssociateItem = "associateItem";
     /** **/
     public static final String _elementChangedItem = "changedItem";
     /** **/
     public static final String _elementChange = "change";
 
     private OutputStream _out;
 
     private AlertRec _rec;
 
     private DBAlert _db;
 
     private Stack<RepRows> _save;
 
     private String _dbName;
 
     private String _cemail;
 
     private String _version;
 
     private Timestamp _startDate;
 
     private Timestamp _endDate;
 
     private static final Logger _logger = Logger.getLogger(ACXMLData.class.getName());
 
     /**
      * Constructor
      *
      * @param out_
      * @param rec_
      * @param db_
      * @param save_
      * @param dbName_
      * @param cemail_
      * @param version_
      * @param start_
      * @param end_
      */
     public ACXMLData(OutputStream out_, AlertRec rec_, DBAlert db_, Stack<RepRows> save_, String dbName_, String cemail_, String version_, Timestamp start_,
                      Timestamp end_)
     {
         _out = out_;
         _rec = rec_;
         _db = db_;
         _save = save_;
         _dbName = dbName_;
         _cemail = (cemail_ == null) ? "" : cemail_;
         _version = version_;
         _startDate = start_;
         _endDate = end_;
     }
 
     /**
      * Write the XML output file
      * @param dtd_ The dtd file URL prefix
      */
     public void writeXMLWithJDOM(String dtd_)
     {
         ResourceBundle props = PropertyResourceBundle.getBundle("gov.nih.nci.cadsr.sentinel.DSRAlert");
         String attrDocType = (props != null) ? dtd_ + props.getString("cadsr.sentinel.dtd") : "Error loading Property file.";
         String attrSoftwareName = (props != null) ? props.getString("cadsr.softwarename") : "Error loading Property file.";
 
 
         Element root = new Element(_elementCadsr);
         // replace the dtd with the appropriate dtd URL
         //TODO the Document Type should come from a property(?)
         DocType type = new DocType(_elementCadsr, attrDocType);
         // replace
         Document doc = new Document(root, type);
 
         try
         {
             XMLOutputter serializer = new XMLOutputter();
             // serialize with two space indents and extra line breaks
             serializer.setFormat(Format.getPrettyFormat());
 
             // create element alertreport
             // <!ELEMENT alertReport (database | definition | changedItem* | associateItem* | group*)>
             Element alertReport = new Element(_elementAlertReport);
             //TODO get software name from a property(?)
             alertReport.setAttribute(_attrSoftwareName, attrSoftwareName);
 
             String[] parts = _version.split(";");
             alertReport.setAttribute(_attrSoftwareVersion, parts[parts.length - 1]);
             alertReport.setAttribute(_attrVersion, "1.0");
 
             // Add element database
             Element database = new Element(_elementDatabase);
             // add attributes to the database element
 
             database.setAttribute(_attrServer, "");
             database.setAttribute(_attrName, _dbName);
             database.setAttribute(_attrRAI, _db.getDatabaseRAI());
             alertReport.addContent(database);
 
             // Add element definition
             // <!ELEMENT definition (name | id | intro? | createdBy | recipient* | summary? | criteria+ |
             // monitor+ | lastAutoRun? | frequency | status | level | start | end | createdOn)>
             Element definition = new Element(_elementDefinition);
             // add elements to the definition element
             // add name to definition
             Element name = new Element(_elementName);
             name.setAttribute(_attrValue, _rec.getName());
             definition.addContent(name);
 
             // add id to definition
             Element id = new Element(_elementId);
             id.addContent(_rec.getAlertRecNum());
             definition.addContent(id);
 
             // add intro to definition
             Element intro = new Element(_elementIntro);
             intro.addContent(_rec.getIntro(false));
             definition.addContent(intro);
 
             // Add element createdBy to definition
             Element createdBy = new Element(_elementCreatedBy);
             // add attributes to the _createdBy element
             createdBy.setAttribute(_attrUser, _rec.getCreator());
             createdBy.setAttribute(_attrName, _rec.getCreatorName());
             createdBy.setAttribute(_attrEmail, _cemail);
             definition.addContent(createdBy);
 
             // Add recipient list
             // Add element recipient to definition, should be atleast 1 or more
             // String recipientNames= _db.selectRecipientNames(_rec.getRecipients());
 
             parts = _rec.getRecipients();
             for (int rc = 0; rc < parts.length; rc++)
             {
                 Element recipient = new Element(_elementRecipient);
 
                 // A process URL recipient
                 if (parts[rc].startsWith("http://") || (parts[rc].startsWith("https://")))
                 {
                     recipient.setAttribute(_attrUrl, parts[rc]);
                     definition.addContent(recipient);
                 }
 
                 // An email address recipient
                 else if (parts[rc].indexOf("@") != -1)
                 {
                     recipient.setAttribute(_attrEmail, parts[rc]);
                     definition.addContent(recipient);
                 }
 
                 // A Context Curator Group
                 else if (parts[rc].indexOf('/') == 0)
                 {
                     String[] temp = _db.selectEmailsFromConte(parts[rc]);
                     if(temp!=null && temp.length>0)
                     {
                         for(int cc=0; cc<temp.length;cc++){
                             if(temp[cc]!=null){
                                 recipient = new Element(_elementRecipient);
                                 recipient.setAttribute(_attrEmail, temp[cc]);
                                 definition.addContent(recipient);
                             }
                         }
                     }
 
                 }
 
                 // A User
                 else
                 {
 
                     String temp[] = new String[1];
                     temp[0] = parts[rc];
                     recipient.setAttribute(_attrUser, parts[rc]);
                     recipient.setAttribute(_attrName, ACData.convertNullString(_db.selectRecipientNames(temp)));
                     temp[0] = _db.selectEmailFromUser(parts[rc]);
                     recipient.setAttribute(_attrEmail, (temp[0] == null) ? "" : temp[0]);
                     definition.addContent(recipient);
                    _logger.error(_db.getError());
                 }
 
 
             }
 
             // add summary to definition can be 0 or 1
             if (_rec.getSummary(true) != null)
             {
                 Element summary = new Element(_elementSummary);
                 summary.addContent(_rec.getSummary(true));
                 definition.addContent(summary);
             }
 
             /*
              * Use this piece of code to display the criteria and monitor //Add criteria to definition, should be atleast 1 or more Element _criteria = new
              * Element("criteria"); //add attributes to criteria _criteria.setAttribute("type", ""); _criteria.setAttribute("value", ""); //add element value to
              * _criteria can be 0 or more Element _criteriaValue = new Element("value"); _criteriaValue.addContent(""); _criteria.addContent(_criteriaValue);
              * _definition.addContent(_criteria); //Add monitor to definition, should be atleast 1 or more Element _monitor = new Element("monitor"); //add
              * attributes to _monitor _monitor.setAttribute("type", ""); _monitor.setAttribute("value", ""); _definition.addContent(_monitor);
              */
 
             // Add _lastAutoRun to definition, can be 0 or 1
             Element lastAutoRun = new Element(_elementLastAutoRun);
             // add attributes to _lastAutoRun
             if (_rec.getAdate() != null)
                 lastAutoRun.setAttribute(_attrTime, _rec.getAdate().toString());
             else
                 lastAutoRun.setAttribute(_attrTime, "");
 
             definition.addContent(lastAutoRun);
 
             // Add frequency to definition, should be there once must be present
 
             Element frequency = new Element(_elementFrequency);
             // add attributes to _frequency
             if (_rec.getFreqString().equals("D"))
                 frequency.setAttribute(_attrUnit, "Day");
             else if (_rec.getFreqString().equals("W"))
             {
                 frequency.setAttribute(_attrUnit, "Week");
                 frequency.setAttribute(_attrValue, _rec.getFreq(false));
             }
             else
             {
                 frequency.setAttribute(_attrUnit, "Month");
 
                 // check this method for week & month
                 frequency.setAttribute(_attrValue, _rec.getFreq(false));
             }
             definition.addContent(frequency);
 
             // Add status to definition, should be there once must be present
 
             Element status = new Element(_elementStatus);
             // add attributes to _status
             if (_rec.isActive())
                 status.setAttribute(_attrCode, "Active");
             else if (_rec.isInactive())
                 status.setAttribute(_attrCode, "Inactive");
             else if (_rec.isActiveOnce())
                 status.setAttribute(_attrCode, "Once");
             else if (_rec.isActiveDates())
             {
                 status.setAttribute(_attrCode, "Range");
                 // add beginDate and endDate only if the code is Range
 
                 status.setAttribute(_attrBeginDate, _rec.getStart().toString().substring(0, 10));
                 status.setAttribute(_attrEndDate, _rec.getEnd().toString().substring(0, 10));
             }
             definition.addContent(status);
 
             // Add level to definition, should be there once, must be present
 
             Element level = new Element(_elementLevel);
             // add attributes to _level
             level.setAttribute(_attrDepth, new StringBuffer().append(_rec.getIAssocLvl()).toString());
             definition.addContent(level);
 
             // Add start to definition, should be there once, must be present
 
             Element start = new Element(_elementStart);
             // add attributes to _start
             start.setAttribute(_attrDate, _startDate.toString().substring(0, 10));
             definition.addContent(start);
 
             // Add end to definition, should be there once, must be present
 
             Element end = new Element(_elementEnd);
             // add attributes to _start
             end.setAttribute(_attrDate, _endDate.toString().substring(0, 10));
             definition.addContent(end);
 
             // Add createdOn to definition, should be there once, must be present
 
             Element createdOn = new Element(_elementCreatedOn);
             // add attributes to _start
             createdOn.setAttribute(_attrTime, new Timestamp(System.currentTimeMillis()).toString());
             definition.addContent(createdOn);
 
             // Add definition to the alert report
             alertReport.addContent(definition);
 
             // Work on changed item now, changedItem can be 0 or more, so check for changedItem
             // Dump the stack.
 
             List<Element> changeList = getChangedItems(_db, _save, _rec.getIAssocLvl());
 
 
             if(changeList!=null  && changeList.size()>0)
             {
             // add the changed items or associated items or groups to the report.
             // Add group to alertReport can be 0 or more, if there is a change then there is a group
             for (int j = 0; j < changeList.size(); j++)
             {
 
                 {
                     if ((changeList.get(j)).getName().equalsIgnoreCase(_elementChangedItem))
                         alertReport.addContent(changeList.get(j));
                 }
             }
 
             // Add associateItem to alertReport can be 0 or more
             Map<String, Element> associateItemMap = new HashMap<String, Element>();
             for (int j = 0; j < changeList.size(); j++)
             {
 
                 Element aItem = changeList.get(j);
                 if ((changeList.get(j)).getName().equalsIgnoreCase(_elementAssociateItem))
                 {
                     // check to see if the associateItem already appears in the Jdom tree, ignore if already there
                     associateItemMap.put(aItem.getAttributeValue("id"), aItem);
 
                 }
             }
 
             // All unique values are in the Map - now process the map
             if (associateItemMap != null && associateItemMap.size() > 0)
             {
                 Iterator<Element> it = associateItemMap.values().iterator();
                 while (it.hasNext())
                 {
                     alertReport.addContent(it.next());
                 }
 
             }
 
             // Add groups to alertReport can be 0 or more
 
             for (int j = 0; j < changeList.size(); j++)
             {
                 if ((changeList.get(j)).getName().equalsIgnoreCase(_elementGroup))
                     alertReport.addContent(changeList.get(j));
             }
             }
 
             root.addContent(alertReport);
 
             serializer.output(doc, _out);
         }
         catch (IOException e)
         {
             _logger.error("Error writing to the xml file");
         }
 
     }
 
 
      /**
      * Write the XML output file
      * @param dtd_ The dtd file URL prefix
      */
     public void writeXMLWithSTAX(String dtd_)
     {
         ResourceBundle props = PropertyResourceBundle.getBundle("gov.nih.nci.cadsr.sentinel.DSRAlert");
         String attrDocType = (props != null) ? dtd_ + props.getString("cadsr.sentinel.dtd") : "Error loading Property file.";
         String attrSoftwareName = (props != null) ? props.getString("cadsr.softwarename") : "Error loading Property file.";
 
          XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
           // Create an XML stream writer
         XMLStreamWriter xmlw = null;
 
 
 
         try
         {
 
            xmlw = xmlof.createXMLStreamWriter(_out);
             // Write XML prologue
             xmlw.writeStartDocument();
 
 
             // Set the namespace definitions to the root element
             // Declare the DTD in the root element
             xmlw.writeDTD("<!DOCTYPE "+_elementCadsr+" SYSTEM \""+attrDocType+"\">");
 
             xmlw.flush();
             // Now start with root element
             xmlw.writeStartElement(_elementCadsr);
 
 
          // create element alertreport
             // <!ELEMENT alertReport (database | definition | changedItem* | associateItem* | group*)>
 
             xmlw.writeStartElement(_elementAlertReport);
             // Writing a few attributes
             xmlw.writeAttribute(_attrSoftwareName, attrSoftwareName);
 
             String[] parts = _version.split(";");
 
             xmlw.writeAttribute(_attrSoftwareVersion, parts[parts.length - 1]);
 
             xmlw.writeAttribute(_attrVersion, "1.0");
             xmlw.flush();
 
             // Add element database
             xmlw.writeEmptyElement(_elementDatabase);
 
             // add attributes to the database element
 
             xmlw.writeAttribute(_attrServer, "");
             xmlw.writeAttribute(_attrName, _dbName);
             xmlw.writeAttribute(_attrRAI, _db.getDatabaseRAI());
             //End element database
 
             xmlw.flush();
 
             // Add element definition
             // <!ELEMENT definition (name | id | intro? | createdBy | recipient* | summary? | criteria+ |
             // monitor+ | lastAutoRun? | frequency | status | level | start | end | createdOn)>
 
             xmlw.writeStartElement(_elementDefinition);
              // add elements to the definition element
             // add name to definition
             xmlw.writeEmptyElement(_elementName);
             xmlw.writeAttribute(_attrValue, _rec.getName());
             //xmlw.writeEndElement();
 
             // add id to definition
             xmlw.writeStartElement(_elementId);
             xmlw.writeCharacters(_rec.getAlertRecNum());
             xmlw.writeEndElement();
 
             // add intro to definition
             xmlw.writeStartElement(_elementIntro);
             xmlw.writeCharacters(_rec.getIntro(false));
             xmlw.writeEndElement();
 
             // Add element createdBy to definition
             xmlw.writeEmptyElement(_elementCreatedBy);
             // add attributes to the _createdBy element
             xmlw.writeAttribute(_attrUser, _rec.getCreator());
             xmlw.writeAttribute(_attrName, _rec.getCreatorName());
             xmlw.writeAttribute(_attrEmail, _cemail);
             //xmlw.writeEndElement();
             xmlw.flush();
             // Add recipient list
             // Add element recipient to definition, should be atleast 1 or more
             // String recipientNames= _db.selectRecipientNames(_rec.getRecipients());
 
             parts = _rec.getRecipients();
             for (int rc = 0; rc < parts.length; rc++)
             {
                 xmlw.writeEmptyElement(_elementRecipient);
 
                 // A process URL recipient
                 if (parts[rc].startsWith("http://") || (parts[rc].startsWith("https://")))
                 {
                     xmlw.writeAttribute(_attrUrl, parts[rc]);
                     //xmlw.writeEndElement();
                 }
 
                 // An email address recipient
                 else if (parts[rc].indexOf("@") != -1)
                 {
                     xmlw.writeAttribute(_attrEmail, parts[rc]);
                     //xmlw.writeEndElement();
                 }
 
                 // A Context Curator Group
                 else if (parts[rc].indexOf('/') == 0)
                 {
                     String[] temp = _db.selectEmailsFromConte(parts[rc]);
                     if(temp!=null && temp.length>0)
                     {
                         for(int cc=0; cc<temp.length;cc++){
                             if(temp[cc]!=null){
                                 xmlw.writeEmptyElement(_elementRecipient);
                                 xmlw.writeAttribute(_attrEmail, temp[cc]);
                                 //xmlw.writeEndElement();
                             }
                         }
                     }
 
                 }
 
                 // A User
                 else
                 {
 
                     String temp[] = new String[1];
                     temp[0] = parts[rc];
                     xmlw.writeAttribute(_attrUser, parts[rc]);
                     xmlw.writeAttribute(_attrName, ACData.convertNullString(_db.selectRecipientNames(temp)));
                     temp[0] = _db.selectEmailFromUser(parts[rc]);
                     xmlw.writeAttribute(_attrEmail, (temp[0] == null) ? "" : temp[0]);
                     //xmlw.writeEndElement();
                    _logger.error(_db.getError());
                 }
                 xmlw.flush();
 
             }
 
             // add summary to definition can be 0 or 1
             if (_rec.getSummary(true) != null)
             {
                 xmlw.writeStartElement(_elementSummary);
                 xmlw.writeCharacters(_rec.getSummary(true));
                 xmlw.writeEndElement();
             }
 
 
 
             // Add _lastAutoRun to definition, can be 0 or 1
             xmlw.writeEmptyElement(_elementLastAutoRun);
             // add attributes to _lastAutoRun
             if (_rec.getAdate() != null)
                 xmlw.writeAttribute(_attrTime, _rec.getAdate().toString());
             else
                 xmlw.writeAttribute(_attrTime, "");
 
             //xmlw.writeEndElement();
             xmlw.flush();
             // Add frequency to definition, should be there once must be present
 
             xmlw.writeEmptyElement(_elementFrequency);
             // add attributes to _frequency
             if (_rec.getFreqString().equals("D"))
                 xmlw.writeAttribute(_attrUnit, "Day");
             else if (_rec.getFreqString().equals("W"))
             {
                 xmlw.writeAttribute(_attrUnit, "Week");
                 xmlw.writeAttribute(_attrValue, _rec.getFreq(false));
             }
             else
             {
                 xmlw.writeAttribute(_attrUnit, "Month");
 
                 // check this method for week & month
                 xmlw.writeAttribute(_attrValue, _rec.getFreq(false));
             }
             //xmlw.writeEndElement();
             xmlw.flush();
             // Add status to definition, should be there once must be present
 
             xmlw.writeEmptyElement(_elementStatus);
             // add attributes to _status
             if (_rec.isActive())
                 xmlw.writeAttribute(_attrCode, "Active");
             else if (_rec.isInactive())
                 xmlw.writeAttribute(_attrCode, "Inactive");
             else if (_rec.isActiveOnce())
                 xmlw.writeAttribute(_attrCode, "Once");
             else if (_rec.isActiveDates())
             {
                 xmlw.writeAttribute(_attrCode, "Range");
                 // add beginDate and endDate only if the code is Range
 
                 xmlw.writeAttribute(_attrBeginDate, _rec.getStart().toString().substring(0, 10));
                 xmlw.writeAttribute(_attrEndDate, _rec.getEnd().toString().substring(0, 10));
             }
             //xmlw.writeEndElement();
             xmlw.flush();
             // Add level to definition, should be there once, must be present
 
             xmlw.writeEmptyElement(_elementLevel);
             // add attributes to _level
             xmlw.writeAttribute(_attrDepth, new StringBuffer().append(_rec.getIAssocLvl()).toString());
             //xmlw.writeEndElement();
 
             // Add start to definition, should be there once, must be present
 
             xmlw.writeEmptyElement(_elementStart);
             // add attributes to _start
             xmlw.writeAttribute(_attrDate, _startDate.toString().substring(0, 10));
             //xmlw.writeEndElement();
 
             // Add end to definition, should be there once, must be present
 
             xmlw.writeEmptyElement(_elementEnd);
             // add attributes to _start
             xmlw.writeAttribute(_attrDate, _endDate.toString().substring(0, 10));
             //xmlw.writeEndElement();
 
             // Add createdOn to definition, should be there once, must be present
 
             xmlw.writeEmptyElement(_elementCreatedOn);
             // add attributes to _start
             xmlw.writeAttribute(_attrTime, new Timestamp(System.currentTimeMillis()).toString());
             //xmlw.writeEndElement();
 
             // End Definition element
             xmlw.writeEndElement();
             xmlw.flush();
 
             // Work on changed item now, changedItem can be 0 or more, so check for changedItem
             // Dump the stack.
 
             List<Element> changeList = getAssociatedAndGroupItems(_db, _save, _rec.getIAssocLvl(), xmlw);
 
 
             if(changeList!=null  && changeList.size()>0)
             {
 
 
             // Add associateItem to alertReport can be 0 or more
             Map<String, Element> associateItemMap = new HashMap<String, Element>();
             for (int j = 0; j < changeList.size(); j++)
             {
 
                 Element aItem = changeList.get(j);
                 if ((changeList.get(j)).getName().equalsIgnoreCase(_elementAssociateItem))
                 {
                     // check to see if the associateItem already appears in the Jdom tree, ignore if already there
                     associateItemMap.put(aItem.getAttributeValue("id"), aItem);
 
                 }
             }
 
             // All unique values are in the Map - now process the map
             if (associateItemMap != null && associateItemMap.size() > 0)
             {
                 Iterator<Element> it = associateItemMap.values().iterator();
                 while (it.hasNext())
                 {
                     Element associateItem = it.next();
                     xmlw.writeStartElement(associateItem.getName());
                     for(int i=0; i < associateItem.getAttributes().size(); i++)
                     {
                        Attribute aItem =  (Attribute)associateItem.getAttributes().get(i);
                         xmlw.writeAttribute(aItem.getName(),aItem.getValue());
                     }
                     xmlw.writeEndElement();
                     xmlw.flush();
                 }
 
             }
 
             // Add groups to alertReport can be 0 or more
 
             for (int j = 0; j < changeList.size(); j++)
             {
                 if ((changeList.get(j)).getName().equalsIgnoreCase(_elementGroup))
                 {
                     Element group = changeList.get(j);
                     xmlw.writeStartElement(group.getName());
                     xmlw.writeAttribute("changedItemId",group.getAttributeValue("changedItemId"));
                     if(group.getChildren("associate")!=null)
                     {
                         for(int k=0; k < group.getChildren("associate").size(); k++)
                         {
                             Element associate = (Element)group.getChildren("associate").get(k);
                             xmlw.writeStartElement(associate.getName());
                             xmlw.writeAttribute("childItemId",associate.getAttributeValue("childItemId"));
                             xmlw.writeAttribute("parentItemId",associate.getAttributeValue("parentItemId"));
                             xmlw.writeEndElement();
                             xmlw.flush();
                         }
                     }
                     xmlw.writeEndElement();
                     xmlw.flush();
                 }
 
             }
             }
 
 
             //End Alert Report
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             // Close the writer to flush the output
             xmlw.close();
         }
          catch (XMLStreamException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     /**
      * Populate the common changedItem and associateItem attributes.
      *
      * @param elm_ the XML Element
      * @param xml_ the XML data
      */
     private void itemHeader(Element elm_, ACDataXML xml_)
     {
 
         elm_.setAttribute("type", xml_._type);
         elm_.setAttribute("name", xml_._name);
         elm_.setAttribute("id", xml_._id);
         if (xml_._publicId != null)
             elm_.setAttribute("publicId", xml_._publicId);
          if (xml_._version != null)
         elm_.setAttribute("version", xml_._version);
          if (xml_._modifiedByUser != null)
         elm_.setAttribute("modifiedByUser", xml_._modifiedByUser);
          if (xml_._modifiedByName != null)
         elm_.setAttribute("modifiedByName", xml_._modifiedByName);
         if (xml_._modifiedTime != null)
             elm_.setAttribute("modifiedTime", xml_._modifiedTime);
         elm_.setAttribute("createdByUser", xml_._createdByUser);
         elm_.setAttribute("createdByName", xml_._createdByName);
         elm_.setAttribute("createdTime", xml_._createdTime);
         elm_.setAttribute("changeNote", xml_._changeNote);
     }
 
 
      /**
      * Populate the common changedItem and associateItem attributes.
      *
      * @param xmlw_ the XMLStreamWriter Element
      * @param xml_ the XML data
      */
     private void printItemHeader(ACDataXML xml_, XMLStreamWriter xmlw_) throws XMLStreamException {
 
          xmlw_.writeAttribute("type", xml_._type);
          xmlw_.writeAttribute("name", xml_._name);
          xmlw_.writeAttribute("id", xml_._id);
          if (xml_._publicId != null)
              xmlw_.writeAttribute("publicId", xml_._publicId);
           if (xml_._version != null)
          xmlw_.writeAttribute("version", xml_._version);
           if (xml_._modifiedByUser != null)
          xmlw_.writeAttribute("modifiedByUser", xml_._modifiedByUser);
           if (xml_._modifiedByName != null)
          xmlw_.writeAttribute("modifiedByName", xml_._modifiedByName);
          if (xml_._modifiedTime != null)
              xmlw_.writeAttribute("modifiedTime", xml_._modifiedTime);
          xmlw_.writeAttribute("createdByUser", xml_._createdByUser);
          xmlw_.writeAttribute("createdByName", xml_._createdByName);
          xmlw_.writeAttribute("createdTime", xml_._createdTime);
          xmlw_.writeAttribute("changeNote", xml_._changeNote);
      }
 
     /**
      * Dump the changed items  into the XML tree.
      *
      * @param db_
      *        A database object for an open connection to a caDSR.
      * @param save_
      *        A stack containing the report results. (Note this is a LIFO
      *        stack.)
      * @param depth depth of the associated items to traverse
      * @return  a List of changed items  (changedItem, associatedItem, group)
      */
     public List<Element> getChangedItems(DBAlert db_, Stack<RepRows> save_, int depth)
     {
         List<Element> changeList = new ArrayList<Element>();
         List<Element> groupList = new ArrayList<Element>();
         int count = -1;
 
         // Get only rows of interest dep
         Stack<RepRows> report = ACData.dumpTrim(save_, depth);
         while (!report.empty())
         {
             RepRows val = report.pop();
             val._rec.resolveNames(db_);
             ACDataXML xml = val._rec.getXML();
             if (val._rec.isPrimary())
             {
                 Element changedItem = new Element("changedItem");
                 // add attributes to changedItem
 
                 itemHeader(changedItem, xml);
 
                 // can have 0 or more details so add logic to see if required
                 if (xml._changes != null)
                 {
                     for (int i = 0; i < xml._changes.length; i++)
                     {
                         ACDataChangesXML chg = xml._changes[i];
                         Element details = new Element("details");
                         // Add attributes to _details element
                         details.setAttribute("modifiedByUser", chg._modifiedByUser);
                         details.setAttribute("modifiedByName", chg._modifiedByName);
                         details.setAttribute("time", chg._time);
                         // Conver the meaningful names in the changes to internal codes for xml
                         String[] changekeyNames = db_.getKeyNames(chg._attributes);
                         // should have atleast one or more change elements
                         for (int chgcnt = 0; chgcnt < changekeyNames.length; chgcnt++)
                         {
                             Element change = new Element(_elementChange);
                             change.setAttribute("attribute", changekeyNames[chgcnt]);
                             change.setAttribute("oldValue", chg._oldValues[chgcnt]);
                             change.setAttribute("newValue", chg._newValues[chgcnt]);
                             details.addContent(change);
                         }
                         changedItem.addContent(details);
                     }
                 }
                 changeList.add(changedItem);
 
                 // Add group to alertReport can be 0 or more, if there is a change then there is a group
                 // for each changedItem add a group
                 Element group = new Element("group");
                 // add attribute changedItemId to _group which is referencint the id of the changedItem
                 group.setAttribute("changedItemId", xml._id);
 
                 groupList.add(group);
                 count++;
             }
             else
             {
 
                 Element associateItem = new Element("associateItem");
                 // add attributes to changedItem
 
                 itemHeader(associateItem, xml);
 
                 changeList.add(associateItem);
                 // Associate this item to the group
 
                 Element group = (Element) groupList.get(count);
 
                 Element associate = new Element("associate");
                 associate.setAttribute("childItemId", xml._id);
                 associate.setAttribute("parentItemId", xml._relatedId);
                 groupList.remove(count);
                 group.addContent(associate);
                 groupList.add(count, group);
 
             }
         }
 
         // add the grouplist to changeList
         for (int k = 0; k < groupList.size(); k++)
         {
             changeList.add(groupList.get(k));
         }
         return changeList;
     }
 
     /**
      * Dump the changed items  into the XML file and return a list of associations and group.
      *
      * @param db_
      *        A database object for an open connection to a caDSR.
      * @param save_
      *        A stack containing the report results. (Note this is a LIFO
      *        stack.)
      * @param depth depth of the associated items to traverse
      * @param xmlw_ 
      * @return  a List of changed items  (changedItem, associatedItem, group)
      * @throws XMLStreamException 
      */
     public List<Element> getAssociatedAndGroupItems(DBAlert db_, Stack<RepRows> save_, int depth, XMLStreamWriter xmlw_) throws XMLStreamException {
         List<Element> changeList = new ArrayList<Element>();
         List<Element> groupList = new ArrayList<Element>();
         int count = -1;
 
         // Get only rows of interest dep
         Stack<RepRows> report = ACData.dumpTrim(save_, depth);
         while (!report.empty())
         {
             RepRows val = report.pop();
             val._rec.resolveNames(db_);
             ACDataXML xml = val._rec.getXML();
             if (val._rec.isPrimary())
             {
                 xmlw_.writeStartElement("changedItem");
                 // add attributes to changedItem
 
                 printItemHeader(xml, xmlw_);
 
                 // can have 0 or more details so add logic to see if required
                 if (xml._changes != null)
                 {
                     for (int i = 0; i < xml._changes.length; i++)
                     {
                         ACDataChangesXML chg = xml._changes[i];
                         xmlw_.writeStartElement("details");
                         // Add attributes to _details element
                         xmlw_.writeAttribute("modifiedByUser", chg._modifiedByUser);
                         xmlw_.writeAttribute("modifiedByName", chg._modifiedByName);
                         xmlw_.writeAttribute("time", chg._time);
                         // Conver the meaningful names in the changes to internal codes for xml
                         String[] changekeyNames = db_.getKeyNames(chg._attributes);
                         // should have atleast one or more change elements
                         for (int chgcnt = 0; chgcnt < changekeyNames.length; chgcnt++)
                         {
                             xmlw_.writeEmptyElement(_elementChange);
                             xmlw_.writeAttribute("attribute", changekeyNames[chgcnt]);
                             xmlw_.writeAttribute("oldValue", chg._oldValues[chgcnt]);
                             xmlw_.writeAttribute("newValue", chg._newValues[chgcnt]);
                             //xmlw_.writeEndElement();
                         }
                         xmlw_.writeEndElement();
                         xmlw_.flush();
                     }
                 }
                 xmlw_.writeEndElement();
                 xmlw_.flush();
                 //changeList.add(changedItem);
 
                 // Add group to alertReport can be 0 or more, if there is a change then there is a group
                 // for each changedItem add a group
                 Element group = new Element("group");
                 // add attribute changedItemId to _group which is referencint the id of the changedItem
                 group.setAttribute("changedItemId", xml._id);
 
                 groupList.add(group);
                 count++;
             }
             else
             {
 
                 Element associateItem = new Element("associateItem");
                 // add attributes to changedItem
 
                 itemHeader(associateItem, xml);
 
                 changeList.add(associateItem);
                 // Associate this item to the group
 
                 Element group = (Element) groupList.get(count);
 
                 Element associate = new Element("associate");
                 associate.setAttribute("childItemId", xml._id);
                 associate.setAttribute("parentItemId", xml._relatedId);
                 groupList.remove(count);
                 group.addContent(associate);
                 groupList.add(count, group);
 
             }
         }
 
         // add the grouplist to changeList
         for (int k = 0; k < groupList.size(); k++)
         {
             changeList.add(groupList.get(k));
         }
         return changeList;
     }
 }
