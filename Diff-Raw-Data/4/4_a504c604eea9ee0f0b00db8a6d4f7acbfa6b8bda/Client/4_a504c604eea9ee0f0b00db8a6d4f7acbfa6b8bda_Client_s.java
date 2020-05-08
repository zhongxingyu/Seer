 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.common.fedora;
 
 import java.io.UnsupportedEncodingException;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.apache.axis.types.NonNegativeInteger;
 
 import de.escidoc.core.common.exceptions.remote.system.FedoraSystemException;
 import de.escidoc.core.test.common.client.servlet.HttpHelper;
 import de.escidoc.core.test.common.logger.AppLogger;
 import de.escidoc.core.test.common.resources.PropertiesProvider;
 import org.fcrepo.client.FedoraClient;
 import org.fcrepo.server.access.FedoraAPIA;
 import org.fcrepo.server.management.FedoraAPIM;
 import org.fcrepo.server.types.gen.Datastream;
 import org.fcrepo.server.types.gen.MIMETypedStream;
 import org.fcrepo.server.types.gen.ObjectProfile;
 
 /**
  * An utility class for Fedora requests.
  * 
  * @spring.bean id="business.FedoraUtility"
  * @author ROF
  * @om
  * 
  */
 public class Client {
 
     private static AppLogger log = new AppLogger(Client.class.getName());
 
     private FedoraAPIM apim;
 
     private FedoraAPIA apia;
 
     private FedoraClient fc;
 
     /**
      * Fedora Client (with configuration from escidoc.properties).
      * 
      * @throws Exception
      *             Thrown if getting instance of FedoraClient failed.
      */
     public Client() throws Exception {
 
         PropertiesProvider p = new PropertiesProvider();
 
         try {
             fc =
                 new FedoraClient(p.getProperty(PropertiesProvider.FEDORA_URL),
                     p.getProperty(PropertiesProvider.FEDORA_USER), p
                         .getProperty(PropertiesProvider.FEDORA_PASSWORD));
             apia = fc.getAPIA();
             apim = fc.getAPIM();
 
         }
         catch (Exception e) {
             throw new FedoraSystemException();
         }
     }
 
     /**
      * Fedora Client (with given parameters).
      * 
      * @param fedoraBaseUrl
      *            Base URL of Fedora
      * @param fedoraUser
      *            Name of Fedora user account
      * @param fedoraPasswd
      *            Password of Fedora user account
      * @throws FedoraSystemException
      *             Thrown if getting instance of FedoraClient failed.
      */
     public Client(final String fedoraBaseUrl, final String fedoraUser,
         final String fedoraPasswd) throws FedoraSystemException {
 
         try {
             fc = new FedoraClient(fedoraBaseUrl, fedoraUser, fedoraPasswd);
             apia = fc.getAPIA();
             apim = fc.getAPIM();
 
         }
         catch (Exception e) {
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method fetches content of the datastream with provided id from
      * Fedora-object with provided id via Fedora APIA-Webservice
      * getDatastreamDissemination()and converts content of the datastream to
      * string.
      * 
      * @param datastreamId
      *            id of the datastream to fetch
      * @param pid
      *            id of the Fedora-object
      * @return content of the datastream as string
      * @throws FedoraSystemException
      *             Thrown if getting Content from Fedora failed.
      * 
      */
     public String getDatastreamContent(
         final String datastreamId, final String pid)
         throws FedoraSystemException {
 
         MIMETypedStream datastream = null;
         String content = null;
         // get content of data stream with provided ID from
         // Fedora object with provided id
         try {
             datastream =
                 apia.getDatastreamDissemination(pid, datastreamId, null);
 
             byte[] streamContent = datastream.getStream();
             // convert to String
             content =
                 new String(streamContent, HttpHelper.HTTP_DEFAULT_CHARSET);
 
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
         return content;
     }
 
     /**
      * The method purges the Object with the given pid.
      * 
      * @param pid
      *            id of the Object
      * @param logMessage
      *            log Message
      * @throws FedoraSystemException
      *             Thrown if purging object in Fedora failed.
      */
     public void purgeObject(final String pid, final String logMessage)
         throws FedoraSystemException {
         try {
             apim.purgeObject(pid, logMessage, false);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method returns the content of the datastream with provided id as byte
      * [] and additional information about this datastream.
      * 
      * @param pid
      *            pid of the fedora object
      * @param dataStreamId
      *            id of the datastream
      * @return MIMETypedStream datastream content
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public MIMETypedStream getDatastreamDissemination(
         final String dataStreamId, final String pid)
         throws FedoraSystemException {
         MIMETypedStream datastream = null;
         try {
             datastream =
                 apia.getDatastreamDissemination(pid, dataStreamId, null);
         }
         catch (RemoteException e) {
 
             throw new FedoraSystemException();
         }
         return datastream;
     }
 
     /**
      * 
      * @param dataStreamId
      *            Id of Datastream
      * @param pid
      *            Fedora object id.
      * @return Map with stream and MIME type.
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public HashMap<String, Object> getDatastreamWithMimeType(
         final String dataStreamId, final String pid)
         throws FedoraSystemException {
         HashMap<String, Object> datastream = new HashMap<String, Object>();
         MIMETypedStream stream = null;
         try {
             stream = apia.getDatastreamDissemination(pid, dataStreamId, null);
             datastream.put("content", stream.getStream());
             datastream.put("mimeType", stream.getMIMEType());
 
         }
         catch (RemoteException e) {
 
             throw new FedoraSystemException();
         }
         return datastream;
     }
 
     /**
      * The method modifies the named xml datastream of the Object with the given
      * Pid. New Datastream-content is the given byte[] datastream
      * 
      * @param pid
      *            id of the Object
      * @param datastreamName
      *            datastreamName
      * @param datastreamLabel
      *            datastreamLabel
      * @param datastream
      *            datastream
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void modifyDatastream(
         final String pid, final String datastreamName,
         final String datastreamLabel, final byte[] datastream)
         throws FedoraSystemException {
         try {
             apim.modifyDatastreamByValue(pid, datastreamName, new String[0],
                 datastreamLabel, "text/xml", null, datastream, "A", null,
                 "eSciDoc test environment", true);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method modifies the named datastream of the Object with the given
      * Pid. New Datastream-content is the given byte[] datastream
      * 
      * @param pid
      *            id of the Object
      * @param datastreamName
      *            datastreamName
      * @param datastreamLabel
      *            datastreamLabel
      * @param mimeType
      *            mimeType
      * @param datastream
      *            datastream
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void modifyDatastream(
         final String pid, final String datastreamName,
         final String datastreamLabel, final String mimeType,
         final byte[] datastream) throws FedoraSystemException {
         try {
             apim.modifyDatastreamByValue(pid, datastreamName, new String[0],
                 datastreamLabel, mimeType, null, datastream, "A", null,
                 "eSciDoc test environment", true);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method modifies the named datastream of the Object with the given
      * Pid. New Datastream-content is the given byte[] datastream
      * 
      * @param pid
      *            id of the Object
      * @param datastreamName
      *            datastreamName
      * @param datastreamLabel
      *            datastreamLabel
      * @param mimeType
      *            mimeType
      * @param url
      *            url
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void modifyDatastream(
         final String pid, final String datastreamName,
         final String datastreamLabel, final String mimeType, final String url)
         throws FedoraSystemException {
         try {
             apim.modifyDatastreamByReference(pid, datastreamName,
                 new String[0], datastreamLabel, mimeType, null, url, "A", null,
                 "eSciDoc test environment", true);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method modifies the named xml datastream of the Object with the given
      * Pid. New Datastream-content is the given byte[] datastream
      * 
      * @param pid
      *            id of the Object
      * @param datastreamName
      *            datastreamName
      * @param datastreamLabel
      *            datastreamLabel
      * @param alternateIDs
      *            String array of alternateIDs of the datastream
      * @param datastream
      *            datastream
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void modifyDatastream(
         final String pid, final String datastreamName,
         final String datastreamLabel, final String[] alternateIDs,
         final byte[] datastream) throws FedoraSystemException {
         try {
             apim.modifyDatastreamByValue(pid, datastreamName, alternateIDs,
                 datastreamLabel, "text/xml", null, datastream, "A", null,
                 "eSciDoc test environment", true);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method retrieves all Datastreams of the Object with the given Pid.
      * 
      * @param pid
      *            id of the Object
      * @return Datastream[] datastreams
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public Datastream[] getDatastreams(final String pid)
         throws FedoraSystemException {
         try {
             return apim.getDatastreams(pid, null, "A");
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * The method retrieves the creation Date of the Object with the given Pid.
      * 
      * @param pid
      *            id of the Object
      * @return Calendar creationDate
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public Calendar fetchFedoraCreationDate(final String pid)
         throws FedoraSystemException {
 
         ObjectProfile objecrprof = null;
 
         try {
             objecrprof = apia.getObjectProfile(pid, null);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
 
         }
 
         String creatDate = objecrprof.getObjCreateDate();
 
         SimpleDateFormat sdfInput =
             new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
         creatDate = creatDate.replace('T', '-');
         creatDate = creatDate.trim();
 
         Calendar cDate =
             CalendarUtility.TransformStringToCalendar(creatDate, sdfInput);
 
         return cDate;
 
     }
 
     /**
      * The method retrieves the last modification Date of the Object with the
      * given Pid.
      * 
      * @param pid
      *            id of the Object
      * @return Calendar creationDate
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public Calendar fetchFedoraLastModificationDate(final String pid)
         throws FedoraSystemException {
 
         ObjectProfile objecrprof = null;
 
         try {
             objecrprof = apia.getObjectProfile(pid, null);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
 
         String lastModDate = objecrprof.getObjLastModDate();
 
         SimpleDateFormat sdfInput =
             new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
         lastModDate = lastModDate.replace('T', '-');
         lastModDate = lastModDate.trim();
 
         Calendar cDate =
             CalendarUtility.TransformStringToCalendar(lastModDate, sdfInput);
 
         return cDate;
 
     }
 
     /**
      * Fedora Export method.
      * 
      * @param pid
      *            Fedora objid.
      * @param format
      *            Select export format.
      * @return Export of Fedora resource.
      */
     public String export(final String pid, final String format) {
         byte[] contentStream = null;
         String contentString = null;
         try {
             contentStream = apim.export(pid, format, "public");
             // convert to String
 
             contentString =
                 new String(contentStream, HttpHelper.HTTP_DEFAULT_CHARSET);
         }
         catch (UnsupportedEncodingException e) {
             log.error(e);
         }
         catch (RemoteException e) {
             log.error(e);
         }
         return contentString;
     }
 
     /**
      * 
      * @param pid
      * @param format
      * @return
      */
     public byte[] exportStream(final String pid, final String format) {
         byte[] contentStream = null;
 
         try {
             contentStream = apim.export(pid, format, "public");
             // convert to String
 
         }
         catch (RemoteException e) {
             // TODO Auto-generated catch block
             log.error(e);
         }
         return contentStream;
     }
 
     /**
      * 
      * @param foxml
      * @return
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public String storeObjectInFedora(final String foxml)
         throws FedoraSystemException {
 
         String createdPID;
         try {
             createdPID =
                 apim.ingest(foxml.getBytes(HttpHelper.HTTP_DEFAULT_CHARSET),
                     "foxml1.0", "test");
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
 
         return createdPID;
     }
 
     public String[] getNextPid(final int pidNumber)
         throws FedoraSystemException {
         String[] pids = null;
         NonNegativeInteger number =
             new NonNegativeInteger(String.valueOf(pidNumber));
         try {
             pids = apim.getNextPID(number, "escidoc");
         }
         catch (Exception e) {
             throw new FedoraSystemException();
         }
         return pids;
     }
 
     /**
      * The method retrieves metadata for a datastream of the fedora object with
      * provided id.
      * 
      * @param pid
      *            Id of Fedora object
      * @param dsId
      *            Id of datastream
      * @return Datastream
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public Datastream getDatastreamInformation(
         final String pid, final String dsId) throws FedoraSystemException {
         Datastream datastreamInfos = null;
         try {
            datastreamInfos = apim.getDatastream(pid, dsId, "A");
         }
         catch (Exception e) {

             throw new FedoraSystemException();
         }
         return datastreamInfos;
     }
 
     /**
      * The method retrieves metadata for all datastreams of the fedora object
      * with provided id as Array.
      * 
      * @param pid
      *            provided id
      * @return Fedora Datastream array with information about all datastreams of
      *         the Fedora object.
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public Datastream[] getDatastreamsInformation(final String pid)
         throws FedoraSystemException {
         Datastream[] datastreamInfos = null;
         try {
             datastreamInfos = apim.getDatastreams(pid, null, "A");
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
         return datastreamInfos;
     }
 
     /**
      * Getter for apia.
      * 
      * @return FedoraAPIA apia
      */
     public FedoraAPIA getApia() {
         return apia;
     }
 
     /**
      * Setter for apia.
      * 
      * @param apia
      *            apia
      */
     public void setApia(final FedoraAPIA apia) {
         this.apia = apia;
     }
 
     /**
      * Getter for apim.
      * 
      * @return FedoraAPIM apim
      * @return
      */
     public FedoraAPIM getApim() {
         return apim;
     }
 
     /**
      * Setter for apim.
      * 
      * @param apim
      *            apim
      */
     public void setApim(final FedoraAPIM apim) {
         this.apim = apim;
     }
 
     /**
      * Getter for fc.
      * 
      * @return fc
      */
     public FedoraClient getFc() {
         return fc;
     }
 
     /**
      * Setter for fc.
      * 
      * @param fc
      *            fc
      */
     public void setFc(final FedoraClient fc) {
         this.fc = fc;
     }
 
     /**
      * 
      * @param pid
      * @param name
      * @param altIDs
      * @param label
      * @param stream
      * @return
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public String addDatastream(
         final String pid, String name, String[] altIDs, String label,
         byte[] stream) throws FedoraSystemException {
         long start = System.currentTimeMillis();
         String result =
             addDatastream(pid, name, altIDs, label, true, "text/xml", null,
                 "http://localhost:8082/fop/images/dummy.xml", "X", "A",
                 "created");
         long end = System.currentTimeMillis();
         if (log.isDebugEnabled()) {
             log.debug("addDatastream: addDatastream(dummy): " + (end - start)
                 + "ms.");
         }
         start = System.currentTimeMillis();
         modifyDatastream(pid, name, label, altIDs, stream);
         end = System.currentTimeMillis();
         if (log.isDebugEnabled()) {
             log.debug("addDatastream: modifyDatastream: " + (end - start)
                 + "ms.");
         }
         return result;
     }
 
     /**
      * @deprecated (for Fedora <2.2)
      * 
      * @param pid
      * @param dsID
      * @param altIDs
      * @param dsLabel
      * @param versionable
      * @param MIMEType
      * @param formatURI
      * @param dsLocation
      * @param controlGroup
      * @param dsState
      * @param logMessage
      * @return
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     @Deprecated
     public String addDatastream(
         final String pid, final String dsID, final String[] altIDs,
         final String dsLabel, final boolean versionable, final String MIMEType,
         final String formatURI, final String dsLocation,
         final String controlGroup, final String dsState, final String logMessage)
         throws FedoraSystemException {
         return (addDatastream(pid, dsID, altIDs, dsLabel, versionable,
             MIMEType, formatURI, dsLocation, controlGroup, dsState, null, null,
             logMessage));
     }
 
     /**
      * Add Datastream to Fedora (Fedora >= 2.2).
      * 
      * @param pid
      * @param dsID
      * @param altIDs
      * @param dsLabel
      * @param versionable
      * @param MIMEType
      * @param formatURI
      * @param dsLocation
      * @param controlGroup
      * @param dsState
      * @param checksumType
      * @param checksum
      * @param logMessage
      * @return id of datastream
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public String addDatastream(
         final String pid, final String dsID, final String[] altIDs,
         final String dsLabel, final boolean versionable, final String MIMEType,
         final String formatURI, final String dsLocation,
         final String controlGroup, final String dsState,
         final String checksumType, final String checksum,
         final String logMessage) throws FedoraSystemException {
 
         String datastreamID = null;
         try {
             datastreamID =
                 apim.addDatastream(pid, dsID, altIDs, dsLabel, versionable,
                     MIMEType, formatURI, dsLocation, controlGroup, dsState,
                     checksumType, checksum, logMessage);
         }
         catch (Exception e) {
 
             throw new FedoraSystemException();
         }
 
         return datastreamID;
     }
 
     /**
      * Touch Fedora object (give object a new timestamp).
      * 
      * @param pid
      *            Fedora object id.
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void touchObject(final String pid) throws FedoraSystemException {
         try {
             apim.modifyObject(pid, null, null, null, "touched");
         }
         catch (RemoteException e) {
 
             throw new FedoraSystemException();
         }
     }
 
     /**
      * Delete Fedora object.
      * 
      * @param objid
      *            Fedora object id.
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      */
     public void deleteObject(final String objid) throws FedoraSystemException {
         String msg = "Deleted object " + objid + ".";
         try {
             apim.purgeObject(objid, msg, false);
         }
         catch (RemoteException e) {
             throw new FedoraSystemException();
         }
     }
 }
