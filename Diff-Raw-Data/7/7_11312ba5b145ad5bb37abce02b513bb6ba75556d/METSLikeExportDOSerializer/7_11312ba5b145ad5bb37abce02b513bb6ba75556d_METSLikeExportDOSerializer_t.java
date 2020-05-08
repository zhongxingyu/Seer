 package fedora.server.storage.translation;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.StreamWriteException;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamContent;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DSBinding;
 import fedora.server.utilities.StreamUtility;
 
 /**
  *
  * <p><b>Title:</b> METSLikeExportDOSerializer.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class METSLikeExportDOSerializer
         implements DOSerializer {
 
     public static final String FEDORA_AUDIT_NS="http://fedora.comm.nsdlib.org/audit";
     public static final String METS_PREFIX="METS";
     public static final String METS_NS="http://www.loc.gov/METS/";
     public static final String METS_XSD_LOCATION="http://www.fedora.info/definitions/1/0/mets-fedora-ext.xsd";
     public static final String METS_XLINK_NS="http://www.w3.org/TR/xlink";
     public static final String REAL_XLINK_NS="http://www.w3.org/TR/xlink";
     //public static final String REAL_XLINK_NS="http://www.w3.org/1999/xlink";
     public static final String XSI_NS="http://www.w3.org/2001/XMLSchema-instance";
 
     private String m_XLinkPrefix="xlink";
     private String m_fedoraAuditPrefix="fedora-auditing";
     private SimpleDateFormat m_formatter=
             new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
     private static String s_localServerDissemUrlStart; // "http://actual.hostname:8080/fedora/get/"
 
     public METSLikeExportDOSerializer() {
     }
 
     public DOSerializer getInstance() {
         return new METSLikeExportDOSerializer();
     }
 
     public void serialize(DigitalObject obj, OutputStream out, String encoding)
             throws ObjectIntegrityException, StreamIOException,
             UnsupportedEncodingException {
         // get the host info in a static var so search/replaces are quicker later
         if (s_localServerDissemUrlStart==null) {
             String fedoraHome=System.getProperty("fedora.home");
             String fedoraServerHost=null;
             String fedoraServerPort=null;
             if (fedoraHome==null || fedoraHome.equals("")) {
                 // if fedora.home is undefined or empty, assume we're testing,
                 // in which case the host and port will be taken from system
                 // properties
                 fedoraServerHost=System.getProperty("fedoraServerHost");
                 fedoraServerPort=System.getProperty("fedoraServerPort");
             } else {
                 try {
                     Server s=Server.getInstance(new File(fedoraHome));
                     fedoraServerHost=s.getParameter("fedoraServerHost");
                     fedoraServerPort=s.getParameter("fedoraServerPort");
                 } catch (InitializationException ie) {
                     // can only possibly happen during failed testing, in which 
                     // case it's ok to do a System.exit
                     System.err.println("STARTUP ERROR: " + ie.getMessage());
                     System.exit(1);
                 }
             }
             s_localServerDissemUrlStart="http://" + fedoraServerHost + ":" 
                     + fedoraServerPort + "/fedora/get/";
         }
         // now do serialization stuff
         StringBuffer buf=new StringBuffer();
         appendXMLDeclaration(obj, encoding, buf);
         appendRootElementStart(obj, buf);
         appendHdr(obj, buf);
         appendDescriptiveMD(obj, buf, encoding);
         appendAuditRecordAdminMD(obj, buf);
         appendOtherAdminMD(obj, buf, encoding);
         appendFileSecs(obj, buf);
         appendStructMaps(obj, buf);
         appendDisseminators(obj, buf);
         appendRootElementEnd(buf);
         writeToStream(buf, out, encoding, true);
     }
 
     private void appendXMLDeclaration(DigitalObject obj, String encoding,
             StringBuffer buf) {
         buf.append("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n");
     }
 
     private void appendRootElementStart(DigitalObject obj, StringBuffer buf)
             throws ObjectIntegrityException {
         buf.append("<" + METS_PREFIX + ":mets xmlns:" + METS_PREFIX + "=\""
                 + StreamUtility.enc(METS_NS) + "\"\n");
         String indent="           ";
         // make sure XSI_NS is mapped...
         String xsiPrefix=(String) obj.getNamespaceMapping().get(XSI_NS);
         if (xsiPrefix==null) {
             xsiPrefix="fedoraxsi";
             obj.getNamespaceMapping().put(XSI_NS, "fedoraxsi"); // 99.999999999% chance this is unique
         }
         appendNamespaceDeclarations(indent,obj.getNamespaceMapping(),buf);
         // hardcode xsi:schemaLocation to definitive location for such.
         buf.append(indent + xsiPrefix + ":schemaLocation=\"" + StreamUtility.enc(METS_NS) + " http://www.fedora.info/definitions/1/0/mets-fedora-ext.xsd\"\n");
         if (obj.getPid()==null) {
             throw new ObjectIntegrityException("Object must have a pid.");
         }
         buf.append(indent + "OBJID=\"" + obj.getPid() + "\" TYPE=\""
                 + getTypeAttribute(obj) + "\"");
         if (obj.getLabel()!=null) {
             buf.append("\n" + indent + "LABEL=\"" + StreamUtility.enc(
                     obj.getLabel()) + "\"");
         }
         if (obj.getContentModelId()!=null) {
             buf.append("\n" + indent + "PROFILE=\"" + StreamUtility.enc(
                     obj.getContentModelId()) + "\"");
         }
         buf.append(">\n");
     }
 
     private void appendNamespaceDeclarations(String prepend, Map URIToPrefix,
             StringBuffer buf) {
         Iterator iter=URIToPrefix.keySet().iterator();
         while (iter.hasNext()) {
             String URI=(String) iter.next();
             String prefix=(String) URIToPrefix.get(URI);
             if ( (URI.equals(METS_XLINK_NS)) || (URI.equals(REAL_XLINK_NS)) ) {
                 m_XLinkPrefix=prefix;
             } else if (URI.equals(FEDORA_AUDIT_NS)) {
                 m_fedoraAuditPrefix=prefix;
             } else if (!URI.equals(METS_NS)) {
                 buf.append(prepend + "xmlns:" + prefix + "=\""
                         + StreamUtility.enc(URI) + "\"\n");
             }
         }
         buf.append(prepend + "xmlns:" + m_XLinkPrefix + "=\""
                 + REAL_XLINK_NS + "\"\n");
         buf.append(prepend + "xmlns:" + m_fedoraAuditPrefix + "=\""
                 + FEDORA_AUDIT_NS + "\"\n");
     }
 
     private String getTypeAttribute(DigitalObject obj)
             throws ObjectIntegrityException {
         int t=obj.getFedoraObjectType();
         if (t==DigitalObject.FEDORA_BDEF_OBJECT) {
             return "FedoraBDefObject";
         } else if (t==DigitalObject.FEDORA_BMECH_OBJECT) {
             return "FedoraBMechObject";
         } else if (t==DigitalObject.FEDORA_OBJECT) {
             return "FedoraObject";
         } else {
             throw new ObjectIntegrityException("Object must have a FedoraObjectType.");
         }
     }
 
     private void appendHdr(DigitalObject obj, StringBuffer buf) {
         buf.append("  <" + METS_PREFIX + ":metsHdr");
         Date cDate=obj.getCreateDate();
         if (cDate!=null) {
             buf.append(" CREATEDATE=\"");
             buf.append(m_formatter.format(cDate));
             buf.append("\"");
         }
         Date mDate=obj.getLastModDate();
         if (mDate!=null) {
             buf.append(" LASTMODDATE=\"");
             buf.append(m_formatter.format(mDate) + "\"");
         }
         String state=obj.getState();
         if (state!=null) {
             buf.append(" RECORDSTATUS=\"");
             buf.append(state + "\"");
         }
         buf.append("/>\n");
     }
 
     private void appendDescriptiveMD(DigitalObject obj, StringBuffer buf,
             String encoding)
             throws ObjectIntegrityException, UnsupportedEncodingException,
             StreamIOException {
         Iterator iter=obj.datastreamIdIterator();
         while (iter.hasNext()) {
             String id=(String) iter.next();
             Datastream firstDS=(Datastream) obj.datastreams(id).get(0);
             if ((firstDS.DSControlGrp.equals("X"))
                     && (((DatastreamXMLMetadata) firstDS).DSMDClass==
                     DatastreamXMLMetadata.DESCRIPTIVE)) {
                 appendMDSec(obj, "dmdSecFedora", "descMD", obj.datastreams(id),
                         buf, encoding);
             }
         }
     }
 
     private void appendMDSec(DigitalObject obj, String outerName,
             String innerName, List XMLMetadata, StringBuffer buf, String encoding)
             throws ObjectIntegrityException, UnsupportedEncodingException,
             StreamIOException {
         DatastreamXMLMetadata first=(DatastreamXMLMetadata) XMLMetadata.get(0);
         if (first.DatastreamID==null) {
             throw new ObjectIntegrityException("Datastream must have an id.");
         }
         if (first.DSState==null) {
             throw new ObjectIntegrityException("Datastream must have a state.");
         }
         buf.append("  <" + METS_PREFIX + ":" + outerName + " ID=\""
                 + first.DatastreamID + "\" STATUS=\"" + first.DSState + "\">\n");
         for (int i=0; i<XMLMetadata.size(); i++) {
             DatastreamXMLMetadata ds=(DatastreamXMLMetadata) XMLMetadata.get(i);
             if (ds.DSVersionID==null) {
                 throw new ObjectIntegrityException("Datastream must have a version id.");
             }
             if (ds.DSCreateDT==null) {
                 throw new ObjectIntegrityException("Datastream must have a creation date.");
             }
             buf.append("    <" + METS_PREFIX + ":" + innerName + " ID=\""
                     + ds.DSVersionID + "\" CREATED=\"" + m_formatter.format(
                     ds.DSCreateDT) + "\">\n");
             if (ds.DSMIME==null) {
                 ds.DSMIME="text/html";
             }
             if (ds.DSInfoType==null || ds.DSInfoType.equals("")
                     || ds.DSInfoType.equalsIgnoreCase("OTHER") ) {
                 ds.DSInfoType="UNSPECIFIED";
             }
             String mdType=ds.DSInfoType;
             String otherString="";
             if ( !mdType.equals("MARC") && !mdType.equals("EAD")
                     && !mdType.equals("DC") && !mdType.equals("NISOIMG")
                     && !mdType.equals("LC-AV") && !mdType.equals("VRA")
                     && !mdType.equals("TEIHDR") && !mdType.equals("DDI")
                     && !mdType.equals("FGDC") ) {
                 mdType="OTHER";
                 otherString=" OTHERMDTYPE=\"" + StreamUtility.enc(ds.DSInfoType)
                         + "\" ";
             }
             String labelString="";
             if ( ds.DSLabel!=null && !ds.DSLabel.equals("") ) {
                 labelString=" LABEL=\"" + StreamUtility.enc(ds.DSLabel) + "\"";
             }
             buf.append("      <" + METS_PREFIX + ":mdWrap MIMETYPE=\"" + ds.DSMIME
                     + "\" MDTYPE=\"" + mdType + "\"" + otherString
                     + labelString + ">\n");
             buf.append("        <" + METS_PREFIX + ":xmlData>\n");
             appendStream(ds.getContentStream(), buf, encoding);
             buf.append("        </" + METS_PREFIX + ":xmlData>");
             buf.append("      </" + METS_PREFIX + ":mdWrap>\n");
             buf.append("    </" + METS_PREFIX + ":" + innerName + ">\n");
         }
         buf.append("  </" + METS_PREFIX + ":" + outerName + ">\n");
     }
 
     private void appendStream(InputStream in, StringBuffer buf, String encoding)
             throws ObjectIntegrityException, UnsupportedEncodingException,
             StreamIOException {
         if (in==null) {
             throw new ObjectIntegrityException("Object's inline descriptive "
                     + "metadata stream cannot be null.");
         }
         try {
             byte[] byteBuf = new byte[4096];
             int len;
             while ( ( len = in.read( byteBuf ) ) != -1 ) {
                 buf.append(new String(byteBuf, 0, len, encoding));
             }
         } catch (UnsupportedEncodingException uee) {
             throw uee;
         } catch (IOException ioe) {
             throw new StreamIOException("Error reading from inline datastream.");
         } finally {
             try {
                 in.close();
             } catch (IOException closeProb) {
                 throw new StreamIOException("Error closing read stream.");
             }
         }
     }
 
     private void appendAuditRecordAdminMD(DigitalObject obj, StringBuffer buf)
             throws ObjectIntegrityException {
         if (obj.getAuditRecords().size()>0) {
             buf.append("  <" + METS_PREFIX + ":amdSec ID=\"FEDORA-AUDITTRAIL\">\n");
             for (int i=0; i<obj.getAuditRecords().size(); i++) {
                 AuditRecord audit=(AuditRecord) obj.getAuditRecords().get(i);
                 if (audit.id==null) {
                     throw new ObjectIntegrityException("Audit record must have id.");
                 }
                 if (audit.date==null) {
                     throw new ObjectIntegrityException("Audit record must have date.");
                 }
                 if (audit.processType==null) {
                     throw new ObjectIntegrityException("Audit record must have processType.");
                 }
                 if (audit.action==null) {
                     throw new ObjectIntegrityException("Audit record must have action.");
                 }
                 if (audit.responsibility==null) {
                     throw new ObjectIntegrityException("Audit record must have responsibility.");
                 }
                 if (audit.justification==null) {
                     throw new ObjectIntegrityException("Audit record must have justification.");
                 }
                 buf.append("    <" + METS_PREFIX + ":digiprovMD ID=\"" + audit.id
                         + "\" CREATED=\"" + m_formatter.format(audit.date)
                         + "\" STATUS=\"A\">\n");
                 buf.append("      <" + METS_PREFIX + ":mdWrap MIMETYPE=\"text/xml\" "
                         + "MDTYPE=\"OTHER\" OTHERMDTYPE=\"FEDORA-AUDITTRAIL\""
                         + " LABEL=\"Audit record for '"
                         + StreamUtility.enc(audit.action) + "' action by "
                         + StreamUtility.enc(audit.responsibility) + " at "
                         + m_formatter.format(audit.date) + "\">\n");
                 buf.append("        <" + METS_PREFIX + ":xmlData>\n");
                 buf.append("          <" + m_fedoraAuditPrefix + ":record>\n");
                 buf.append("            <" + m_fedoraAuditPrefix + ":process type=\""
                         + StreamUtility.enc(audit.processType) + "\"/>\n");
                 buf.append("            <" + m_fedoraAuditPrefix + ":action>"
                         + StreamUtility.enc(audit.action)
                         + "</" + m_fedoraAuditPrefix + ":action>\n");
                 buf.append("            <" + m_fedoraAuditPrefix + ":responsibility>"
                         + StreamUtility.enc(audit.responsibility)
                         + "</" + m_fedoraAuditPrefix + ":responsibility>\n");
                 buf.append("            <" + m_fedoraAuditPrefix + ":date>"
                         + m_formatter.format(audit.date)
                         + "</" + m_fedoraAuditPrefix + ":date>\n");
                 buf.append("            <" + m_fedoraAuditPrefix + ":justification>"
                         + StreamUtility.enc(audit.justification)
                         + "</" + m_fedoraAuditPrefix + ":justification>\n");
                 buf.append("          </" + m_fedoraAuditPrefix + ":record>\n");
                 buf.append("        </" + METS_PREFIX + ":xmlData>\n");
                 buf.append("      </" + METS_PREFIX + ":mdWrap>\n");
                 buf.append("    </" + METS_PREFIX + ":digiprovMD>\n");
             }
             buf.append("  </" + METS_PREFIX + ":amdSec>\n");
         }
     }
 
     private void appendOtherAdminMD(DigitalObject obj, StringBuffer buf,
             String encoding)
             throws ObjectIntegrityException, UnsupportedEncodingException,
             StreamIOException {
         Iterator iter=obj.datastreamIdIterator();
         while (iter.hasNext()) {
             String id=(String) iter.next();
             Datastream firstDS=(Datastream) obj.datastreams(id).get(0);
             if ((firstDS.DSControlGrp.equals("X"))
                     && (((DatastreamXMLMetadata) firstDS).DSMDClass!=
                     DatastreamXMLMetadata.DESCRIPTIVE)) {
                 DatastreamXMLMetadata md=(DatastreamXMLMetadata) firstDS;
                 String mdClass=null;
                 if (md.DSMDClass==DatastreamXMLMetadata.TECHNICAL) {
                     mdClass="techMD";
                 } else if (md.DSMDClass==DatastreamXMLMetadata.SOURCE) {
                     mdClass="sourceMD";
                 } else if (md.DSMDClass==DatastreamXMLMetadata.RIGHTS) {
                     mdClass="rightsMD";
                 } else if (md.DSMDClass==DatastreamXMLMetadata.DIGIPROV) {
                     mdClass="digiprovMD";
                 } else {
                     throw new ObjectIntegrityException("Object's inline XML datastream must have a class.");
                 }
                 appendMDSec(obj, "amdSec", mdClass, obj.datastreams(id),
                         buf, encoding);
             }
         }
     }
 
     private void appendFileSecs(DigitalObject obj, StringBuffer buf)
             throws ObjectIntegrityException {
         Iterator iter=obj.datastreamIdIterator();
         boolean didFileSec=false;
         while (iter.hasNext()) {
             Datastream ds=(Datastream) obj.datastreams((String) iter.next()).get(0);
             if (!ds.DSControlGrp.equals("X")) {
                 if (!didFileSec) {
                     didFileSec=true;
                     buf.append("  <" + METS_PREFIX + ":fileSec>\n");
                     buf.append("    <" + METS_PREFIX + ":fileGrp ID=\"DATASTREAMS\">\n");
                 }
                 if (ds.DatastreamID==null || ds.DatastreamID.equals("")) {
                     throw new ObjectIntegrityException("Object's content datastream must have an id.");
                 }
                 if (ds.DSState==null) ds.DSState="";
                 buf.append("      <" + METS_PREFIX + ":fileGrp ID=\""
                         + ds.DatastreamID + "\" STATUS=\"" + ds.DSState + "\">\n");
                 Iterator contentIter=obj.datastreams(ds.DatastreamID).iterator();
                 while (contentIter.hasNext()) {
                     DatastreamContent dsc=(DatastreamContent) contentIter.next();
                     if (dsc.DSVersionID==null || dsc.DSVersionID.equals("")) {
                         throw new ObjectIntegrityException("Object's content datastream must have a version id.");
                     }
                     if (dsc.DSCreateDT==null) {
                         throw new ObjectIntegrityException("Object's content datastream must have a create date.");
                     }
                     if (dsc.DSMIME==null || dsc.DSMIME.equals("")) {
                         dsc.DSMIME="text/xml";
                     }
                     String labelString="";
                     if (dsc.DSLabel!=null && !dsc.DSLabel.equals("")) {
                         labelString=" " + m_XLinkPrefix + ":title=\""
                                 + StreamUtility.enc(dsc.DSLabel) + "\"";
                     }
                     if (dsc.DSLocation==null || dsc.DSLocation.equals("")) {
                         throw new ObjectIntegrityException("Object's content datastream must have a location.");
                     }
                     String sizeString=" SIZE=\"" + dsc.DSSize + "\"";
                     String admIDString=getIdString(obj, dsc, true);
                     String dmdIDString=getIdString(obj, dsc, false);
                     if (dsc.DSControlGrp==null || dsc.DSControlGrp.equals("")) {
                         throw new ObjectIntegrityException("Object's content datastream must have a control group.");
                     }
                     if (dsc.DSControlGrp.equals("M")) {
                         // translate the url to the default disseminator's
                         // url for the datastream
                         dsc.DSLocation=s_localServerDissemUrlStart 
                                 + obj.getPid() 
                                + "/fedora-system:3/getItem/"
                                + m_formatter.format(dsc.DSCreateDT)
                                + "?itemID=" + dsc.DatastreamID;
                     }
                     buf.append("        <" + METS_PREFIX + ":file ID=\""
                             + dsc.DSVersionID + "\" CREATED=\"" + m_formatter.format(dsc.DSCreateDT)
                             + "\" MIMETYPE=\"" + dsc.DSMIME + "\"" + sizeString
                             + admIDString + dmdIDString + " OWNERID=\"" + dsc.DSControlGrp + "\">\n");
                     buf.append("          <" + METS_PREFIX + ":FLocat" + labelString
                             + " LOCTYPE=\"URL\" " + m_XLinkPrefix
                             + ":href=\"" + StreamUtility.enc(dsc.DSLocation) + "\"/>\n");
                     buf.append("        </" + METS_PREFIX + ":file>\n");
                 }
                 buf.append("      </" + METS_PREFIX + ":fileGrp>\n");
             }
         }
         if (didFileSec) {
             buf.append("    </" + METS_PREFIX + ":fileGrp>\n");
             buf.append("  </" + METS_PREFIX + ":fileSec>\n");
         }
     }
 
     private String getIdString(DigitalObject obj, DatastreamContent content,
             boolean adm)
             throws ObjectIntegrityException {
         ArrayList ret;
         if (adm) {
             ret=new ArrayList(content.auditRecordIdList());
         } else {
             ret=new ArrayList();
         }
         Iterator mdIdIter=content.metadataIdList().iterator();
         while (mdIdIter.hasNext()) {
             String mdId=(String) mdIdIter.next();
             List datastreams=obj.datastreams(mdId);
             if (datastreams.size()==0) {
                 throw new ObjectIntegrityException("Object's content datastream"
                         + " points to an invalid inline metadata datastream id.");
             }
             Datastream ds=(Datastream) datastreams.get(0);
             if (ds.DSControlGrp.equalsIgnoreCase("X")) {
                 DatastreamXMLMetadata mds=(DatastreamXMLMetadata) ds;
                 if (mds.DSMDClass == DatastreamXMLMetadata.DESCRIPTIVE) {
                     if (!adm) ret.add(mdId);
                 }
                 else {
                     if (adm) ret.add(mdId);
                 }
             }
         }
         StringBuffer out=new StringBuffer();
         for (int i=0; i<ret.size(); i++) {
             if (i>0) {
                 out.append(' ');
             } else {
                 if (adm) {
                     out.append(" ADMID=\"");
                 } else {
                     out.append(" DMDID=\"");
                 }
             }
             out.append((String) ret.get(i));
             if (i==ret.size()-1) {
                 out.append("\"");
             }
         }
         return out.toString();
     }
 
     private void appendStructMaps(DigitalObject obj, StringBuffer buf)
             throws ObjectIntegrityException {
         Iterator dissIdIter=obj.disseminatorIdIterator();
         while (dissIdIter.hasNext()) {
             String did=(String) dissIdIter.next();
             Iterator dissIter=obj.disseminators(did).iterator();
             while (dissIter.hasNext()) {
                 Disseminator diss=(Disseminator) dissIter.next();
                 if (diss.dsBindMapID==null) {
                     throw new ObjectIntegrityException("Object's disseminator must have a binding map id.");
                 }
                 if (diss.bMechID==null) {
                     throw new ObjectIntegrityException("Object's disseminator must have a bmech id.");
                 }
                 if (diss.dsBindMap==null) {
                     throw new ObjectIntegrityException("Object's disseminator must have a binding map.");
                 }
                 String labelString="";
                 if ( diss.dsBindMap.dsBindMapLabel!=null
                         && !diss.dsBindMap.dsBindMapLabel.equals("") ) {
                     labelString=" LABEL=\"" + StreamUtility.enc(diss.dsBindMap.dsBindMapLabel) + "\"";
                 }
                 buf.append("  <" + METS_PREFIX + ":structMap ID=\""
                         + diss.dsBindMapID + "\" TYPE=\"fedora:dsBindingMap\">\n");
                 buf.append("    <" + METS_PREFIX + ":div TYPE=\"" + diss.bMechID
                         + "\"" + labelString + ">\n");
                 DSBinding[] bindings=diss.dsBindMap.dsBindings;
                 for (int i=0; i<bindings.length; i++) {
                     if (bindings[i].bindKeyName==null
                             || bindings[i].bindKeyName.equals("")) {
                         throw new ObjectIntegrityException("Object's disseminator binding map binding must have a binding key name.");
                     }
                     buf.append("      <" + METS_PREFIX + ":div TYPE=\"");
                     buf.append(bindings[i].bindKeyName);
                     if (bindings[i].bindLabel!=null
                             && !bindings[i].bindLabel.equals("")) {
                         buf.append("\" LABEL=\"");
                         buf.append(StreamUtility.enc(bindings[i].bindLabel));
                     }
                     if (bindings[i].seqNo!=null) {
                         buf.append("\" ORDER=\"");
                         buf.append(bindings[i].seqNo);
                     }
                     if (bindings[i].datastreamID==null
                             || bindings[i].datastreamID.equals("")) {
                         throw new ObjectIntegrityException("Object's disseminator binding map binding must point to a datastream.");
                     }
                     buf.append("\">\n        <" + METS_PREFIX + ":fptr FILEID=\""
                             + bindings[i].datastreamID + "\"/>\n" + "      </"
                             + METS_PREFIX + ":div>\n");
                 }
                 buf.append("    </" + METS_PREFIX + ":div>\n");
                 buf.append("  </" + METS_PREFIX + ":structMap>\n");
             }
         }
     }
 
     private void appendDisseminators(DigitalObject obj, StringBuffer buf)
             throws ObjectIntegrityException {
         Iterator dissIdIter=obj.disseminatorIdIterator();
         while (dissIdIter.hasNext()) {
             String did=(String) dissIdIter.next();
             Iterator dissIter=obj.disseminators(did).iterator();
             Disseminator diss=(Disseminator) obj.disseminators(did).get(0);
             if (diss.dissState==null || diss.dissState.equals("")) {
                 throw new ObjectIntegrityException("Object's disseminator must have a state.");
             }
             buf.append("  <" + METS_PREFIX + ":behaviorSec ID=\"" + did
                     + "\" STATUS=\"" + diss.dissState + "\">\n");
             for (int i=0; i<obj.disseminators(did).size(); i++) {
                 diss=(Disseminator) obj.disseminators(did).get(i);
                 if (diss.dissVersionID==null || diss.dissVersionID.equals("")) {
                     throw new ObjectIntegrityException("Object's disseminator must have a version id.");
                 }
                 if (diss.bDefID==null || diss.bDefID.equals("")) {
                     throw new ObjectIntegrityException("Object's disseminator must have a bdef id.");
                 }
                 if (diss.dissCreateDT==null) {
                     throw new ObjectIntegrityException("Object's disseminator must have a create date.");
                 }
                 if (diss.dissState==null || diss.dissState.equals("")) {
                     throw new ObjectIntegrityException("Object's disseminator must have a state.");
                 }
                 String dissLabelString="";
                 if (diss.dissLabel!=null && !diss.dissLabel.equals("")) {
                     dissLabelString=" LABEL=\"" + StreamUtility.enc(diss.dissLabel) + "\"";
                 }
                 String bDefLabelString="";
                 if (diss.bDefLabel!=null && !diss.bDefLabel.equals("")) {
                     bDefLabelString=" LABEL=\"" + StreamUtility.enc(diss.bDefLabel) + "\"";
                 }
                 String bMechLabelString="";
                 if (diss.bMechLabel!=null && !diss.bMechLabel.equals("")) {
                     bMechLabelString=" LABEL=\"" + StreamUtility.enc(diss.bMechLabel) + "\"";
                 }
                 buf.append("    <" + METS_PREFIX + ":serviceBinding ID=\""
                         + diss.dissVersionID + "\" STRUCTID=\"" + diss.dsBindMapID
                         + "\" BTYPE=\"" + diss.bDefID + "\" CREATED=\""
                         + m_formatter.format(diss.dissCreateDT) + "\""
                         + dissLabelString + ">\n");
                 buf.append("      <" + METS_PREFIX + ":interfaceMD" + bDefLabelString
                         + " LOCTYPE=\"URN\" " + m_XLinkPrefix + ":href=\""
                         + diss.bDefID + "\"/>\n");
                 buf.append("      <" + METS_PREFIX + ":serviceBindMD" + bMechLabelString
                         + " LOCTYPE=\"URN\" " + m_XLinkPrefix + ":href=\""
                         + diss.bMechID + "\"/>\n");
 
                 buf.append("    </" + METS_PREFIX + ":serviceBinding>\n");
             }
             buf.append("  </" + METS_PREFIX + ":behaviorSec>\n");
         }
     }
 
     private void appendRootElementEnd(StringBuffer buf) {
         buf.append("</" + METS_PREFIX + ":mets>");
     }
 
     private void writeToStream(StringBuffer buf, OutputStream out,
             String encoding, boolean closeWhenFinished)
             throws StreamIOException, UnsupportedEncodingException {
         try {
             out.write(buf.toString().getBytes(encoding));
             out.flush();
         } catch (IOException ioe) {
             throw new StreamWriteException("Problem serializing to METS: "
                     + ioe.getMessage());
         } finally {
             if (closeWhenFinished) {
                 try {
                     out.close();
                 } catch (IOException ioe2) {
                     throw new StreamWriteException("Problem closing stream after "
                             + " serializing to METS: " + ioe2.getMessage());
                 }
             }
         }
     }
 }
