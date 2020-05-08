 /*
  * $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The DOMS project.
  * Copyright (C) 2007-2010  The State and University Library
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 
 package dk.statsbiblioteket.doms.central;
 
 
 import com.sun.xml.ws.developer.servlet.HttpSessionScope;
 import dk.statsbiblioteket.doms.central.connectors.*;
 import dk.statsbiblioteket.doms.central.connectors.authchecker.AuthChecker;
 
 
 import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
 import dk.statsbiblioteket.doms.central.connectors.updatetracker.UpdateTracker;
 import dk.statsbiblioteket.doms.central.connectors.updatetracker.UpdateTrackerRecord;
 import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
 import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import dk.statsbiblioteket.doms.webservices.configuration.ConfigCollection;
 import dk.statsbiblioteket.util.xml.DOM;
 import net.sf.json.JSONObject;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.namespace.QName;
 import javax.xml.ws.Holder;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.lang.String;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Aug 18, 2010
  * Time: 2:01:51 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebService(endpointInterface = "dk.statsbiblioteket.doms.central.CentralWebservice")
 @HttpSessionScope
 public class CentralWebserviceImpl implements CentralWebservice {
 
     @Resource
     WebServiceContext context;
 
     private static Log log = LogFactory.getLog(
             CentralWebserviceImpl.class);
     private static Lock lock = new Lock();
 
     private final String fedoraLocation;
     private final String updateTrackerLocation;
     private final String authCheckerLocation;
     private final String pidgeneratorLocation;
     private final String summaLocation;
 
 
 
     public CentralWebserviceImpl() {
         pidgeneratorLocation = ConfigCollection.getProperties()
                 .getProperty("dk.statsbiblioteket.doms.ecm.pidGeneratorLocation");
         fedoraLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.fedoraLocation");
         updateTrackerLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.updateTrackerLocation");
         authCheckerLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.authCheckerLocation");
         summaLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.summaWSDL");
 
     }
 
     private EnhancedFedora fedora;
 
     @PostConstruct
    private void initialise(Credentials creds) throws MalformedURLException, PIDGeneratorException {
        creds = getCredentials();
         fedora = new EnhancedFedoraImpl(creds, fedoraLocation, pidgeneratorLocation);
     }
 
 
     @Override
     public String newObject(@WebParam(name = "pid", targetNamespace = "") String pid,
                             @WebParam(name = "oldID", targetNamespace = "") List<String> oldID,
                             @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace(
                     "Entering newObject with params pid=" + pid + " and oldIDs="
                     + oldID.toString());
             return fedora.cloneTemplate(pid,oldID,comment);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
     }
 
     @Override
     public ObjectProfile getObjectProfile(@WebParam(name = "pid", targetNamespace = "") String pid)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         try {
             log.trace("Entering getObjectProfile with params pid='" + pid+"'");
             dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile fprofile = fedora.getObjectProfile(pid);
             ObjectProfile wprofile = new ObjectProfile();
             wprofile.setTitle(fprofile.getLabel());
             wprofile.setPid(fprofile.getPid());
             wprofile.setState(fprofile.getState());
             wprofile.setCreatedDate(fprofile.getObjectCreatedDate().getTime());
             wprofile.setModifiedDate(fprofile.getObjectLastModifiedDate().getTime());
             wprofile.getContentmodels().addAll(fprofile.getContentModels());
             switch (fprofile.getType()){
                 case CONTENT_MODEL:
                     wprofile.setType("ContentModel");
                     break;
                 case DATA_OBJECT:
                     wprofile.setType("DataObject");
                     break;
                 case TEMPLATE:
                     wprofile.setType("TemplateObject");
                     break;
                 case COLLECTION:
                     wprofile.setType("CollectionObject");
                     break;
                 case FILE:
                     wprofile.setType("FileObject");
                     break;
             }
 
             //Datastreams
             List<DatastreamProfile> datastreams = wprofile.getDatastreams();
             for (dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile datastreamProfile : fprofile
                     .getDatastreams()) {
                 DatastreamProfile wdprofile = new DatastreamProfile();
                 wdprofile.setId(datastreamProfile.getID());
                 wdprofile.setLabel(datastreamProfile.getLabel());
                 wdprofile.setChecksum(new Checksum());
                 wdprofile.getChecksum().setType(datastreamProfile.getChecksumType());
                 wdprofile.getChecksum().setValue(datastreamProfile.getChecksum());
                 wdprofile.setMimeType(datastreamProfile.getMimeType());
                 wdprofile.setFormatUri(datastreamProfile.getFormatURI());
                 wdprofile.setInternal(datastreamProfile.isInternal());
                 if (!wdprofile.isInternal()){
                     wdprofile.setUrl(datastreamProfile.getUrl());
                 }
                 datastreams.add(wdprofile);
             }
 
             //Relations
             List<Relation> wrelations = wprofile.getRelations();
             wrelations.addAll(convertRelations(fprofile.getRelations()));
 
             return wprofile;
 
 
 
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     @Override
     public void setObjectLabel(@WebParam(name = "pid", targetNamespace = "") String pid,
                                @WebParam(name = "name", targetNamespace = "") String name,
                                @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering setObjectLabel with params pid=" + pid
                       + " and name=" + name);
             fedora.modifyObjectLabel(pid, name, comment);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
 
     }
 
 
     @Override
     public void deleteObject(@WebParam(name = "pids", targetNamespace = "") List<String> pids,
                              @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering deleteObject with params pid=" + pids);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_DELETED, comment);
             }
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
 
     @Override
     public void markPublishedObject(@WebParam(name = "pids", targetNamespace = "") List<String> pids,
                                     @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         List<String> activated = new ArrayList<String>();
         try {
             log.trace("Entering markPublishedObject with params pids=" + pids);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_ACTIVE, comment);
                 activated.add(pid);
             }
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             //rollback
             comment = comment + ": Publishing failed, marking back to InProgress";
             markInProgressObject(activated, comment);
             throw new MethodFailedException("Method failed to execute: "+e.getMessage(),
                                             "Method failed to execute: "+e.getMessage(),
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             comment = comment + ": Publishing failed, marking back to InProgress";
             markInProgressObject(activated, comment);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         }  catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
 
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             comment = comment + ": Publishing failed, marking back to InProgress";
             markInProgressObject(activated, comment);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
 
     }
 
 
     @Override
     public void markInProgressObject(@WebParam(name = "pids", targetNamespace = "") List<String> pids,
                                      @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering markInProgressObject with params pids=" + pids);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_INACTIVE, comment);
             }
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
 
     @Override
     public void modifyDatastream(@WebParam(name = "pid", targetNamespace = "") String pid,
                                  @WebParam(name = "datastream", targetNamespace = "") String datastream,
                                  @WebParam(name = "contents", targetNamespace = "") String contents,
                                  @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering modifyDatastream with params pid=" + pid
                       + " and datastream=" + datastream + " and contents="
                       + contents);
 
             fedora.modifyDatastreamByValue(pid, datastream, contents, comment);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
 
     public String getDatastreamContents(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "datastream", targetNamespace = "")
             String datastream)
             throws MethodFailedException, InvalidCredentialsException, InvalidResourceException {
         try {
             log.trace("Entering getDatastreamContents with params pid=" + pid
                       + " and datastream=" + datastream);
             return fedora.getXMLDatastreamContents(pid, datastream);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     @Override
     public void addFileFromPermanentURL(@WebParam(name = "pid", targetNamespace = "") String pid,
                                         @WebParam(name = "filename", targetNamespace = "") String filename,
                                         @WebParam(name = "md5sum", targetNamespace = "") String md5Sum,
                                         @WebParam(name = "permanentURL", targetNamespace = "") String permanentURL,
                                         @WebParam(name = "formatURI", targetNamespace = "") String formatURI,
                                         @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
 
 
         try {
             log.trace("Entering addFileFromPermamentURL with params pid=" + pid
                       + " and filename=" + filename + " and md5sum=" + md5Sum
                       + " and permanentURL=" + permanentURL + " and formatURI="
                       + formatURI);
 
 
             String existingObject = getFileObjectWithURL(permanentURL);
             if (existingObject != null) {
                 log.warn("Attempt to add a permament url that already exists"
                          + "in DOMS");
                 throw new MethodFailedException(
                         "This permanent url has already "
                         + "been added to the object '" +
                         existingObject + "'",
                         "This permanent url has already "
                         + "been added to the object '" +
                         existingObject + "'");
             }
             fedora.addExternalDatastream(pid,"CONTENTS",filename,permanentURL,formatURI,"application/octet-stream",comment);
             setObjectLabel(pid,permanentURL,comment);
 
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
 
     public String getFileObjectWithURL(
             @WebParam(name = "URL", targetNamespace = "") String url)
             throws MethodFailedException, InvalidCredentialsException, InvalidResourceException {
         try {
             log.trace("Entering getFileObjectWithURL with param url=" + url);
             List<String> objects = fedora.listObjectsWithThisLabel(url);
 
             if (objects != null && !objects.isEmpty()) {
                 return objects.get(0);
             } else {
                 return null;
             }
 
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     @Override
     public void addRelation(@WebParam(name = "pid", targetNamespace = "") String pid,
                             @WebParam(name = "relation", targetNamespace = "") Relation relation,
                             @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering addRelation with params pid=" + pid
                       + " and subject=" + relation.getSubject() + " and predicate="
                       + relation.getPredicate() + " and object=" + relation.getObject());
             fedora.addRelation(pid, relation.subject, relation.predicate, relation.object, relation.literal, comment);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
     @Override
     public List<Relation> getRelations(@WebParam(name = "pid", targetNamespace = "") String pid)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
 
         log.trace("Entering getRelations with params pid='" + pid + "'");
         return getNamedRelations(pid, null);
     }
 
     @Override
     public List<Relation> getNamedRelations(@WebParam(name = "pid", targetNamespace = "") String pid,
                                             @WebParam(name = "predicate", targetNamespace = "") String predicate)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         try {
             log.trace("Entering getNamedRelations with params pid='" + pid + "'");
             List<FedoraRelation> fedorarels = fedora.getNamedRelations(pid, predicate);
             return convertRelations(fedorarels);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
     }
 
     @Override
     public List<Relation> getInverseRelations(@WebParam(name = "pid", targetNamespace = "") String pid)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         try {
             log.trace("Entering getInverseRelations with params pid='" + pid + "'");
             List<FedoraRelation> fedorarels = fedora.getInverseRelations(pid, null);
             return convertRelations(fedorarels);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     @Override
     public List<Relation> getInverseRelationsWithPredicate(@WebParam(name = "pid", targetNamespace = "") String pid,
                                                            @WebParam(name = "predicate", targetNamespace = "")
                                                            String predicate)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         try {
             log.trace("Entering getInverseRelations with params pid='" + pid + "'");
             List<FedoraRelation> fedorarels = fedora.getInverseRelations(pid, predicate);
             return convertRelations(fedorarels);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
 
     @Override
     public void deleteRelation(@WebParam(name = "pid", targetNamespace = "") String pid,
                                @WebParam(name = "relation", targetNamespace = "") Relation relation,
                                @WebParam(name = "comment", targetNamespace = "") String comment)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         long token = lock.getReadAndWritePerm();
         try {
             log.trace("Entering deleteRelation with params pid=" + pid
                       + " and subject=" + relation.subject + " and predicate="
                       + relation.predicate + " and object=" + relation.object);
             fedora.deleteRelation(pid, relation.subject, relation.predicate, relation.object, relation.literal, comment);
         }  catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         } finally {
             lock.releaseReadAndWritePerm(token);
         }
 
     }
 
 
     public ViewBundle getViewBundle(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "name", targetNamespace = "")
             String viewAngle)
             throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
         log.trace("Entering getViewBundle with params pid=" + pid
                   + " and viewAngle=" + viewAngle);
         /*
         * Pseudo kode here
         * We need to figure two things out
         * the bundle
         * the type
         * ECM generates the bundle
         * The type is the entry content model of the origin pid
         * */
 
 
         try {
 /*
             List<String> types = ecm.getEntryContentModelsForObject(pid,
                                                                     viewAngle);
             if (types.isEmpty()) {
                 throw new BackendInvalidResourceException("Pid '"+pid+"'is not an entry object for angle '"+viewAngle+"'");
             }
 */
             Document bundleContents = fedora.createBundle(pid, viewAngle);
             String bundleContentsString = DOM.domToString(bundleContents);
 
             ViewBundle viewBundle = new ViewBundle();
             viewBundle.setId(pid);
             viewBundle.setContents(bundleContentsString);
             return viewBundle;
 
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidResourceException("Invalid Resource Requested",
                                                "Invalid Resource Requested",
                                                e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public List<RecordDescription> getIDsModified(
             @WebParam(name = "since", targetNamespace = "") long since,
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle,
             @WebParam(name = "state", targetNamespace = "") String state,
             @WebParam(name = "offset", targetNamespace = "") Integer offset,
             @WebParam(name = "limit", targetNamespace = "") Integer limit)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             logEntering("getIDsModified",
                         since + "",
                         collectionPid,
                         viewAngle,
                         state,
                         offset + "",
                         limit + "");
             Credentials creds = getCredentials();
             UpdateTracker tracker = new UpdateTracker(creds,
                                                       updateTrackerLocation);
             if (state == null || state.isEmpty()) {
                 state = "Published";
             }
             List<UpdateTrackerRecord> modifieds
                     = tracker.listObjectsChangedSince(
                     collectionPid,
                     viewAngle,
                     since,
                     state,
                     offset,
                     limit);
             return transform(modifieds);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
 
     }
 
 
     public long getLatestModified(
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle,
             @WebParam(name = "state", targetNamespace = "")
             String state)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             logEntering("getLatestModified", collectionPid, viewAngle, state);
             Credentials creds = getCredentials();
             UpdateTracker tracker = new UpdateTracker(creds,
                                                       updateTrackerLocation);
             return tracker.getLatestModification(collectionPid,
                                                  viewAngle,
                                                  state);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
     }
 
     @Override
     public List<String> findObjectFromDCIdentifier(@WebParam(name = "string", targetNamespace = "") String string)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             log.trace("Entering findObjectFromDCIdentifier with param string=" + string);
             List<String> objects = fedora.findObjectFromDCIdentifier(string);
 
 
             return objects;
 /*
             if (objects != null && !objects.isEmpty()) {
                 return objects.get(0);
             } else {
                 return null;
             }
 */
 
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
     }
 
     @Override
     public List<SearchResult> findObjects(@WebParam(name = "query", targetNamespace = "") String query,
                                           @WebParam(name = "offset", targetNamespace = "") int offset,
                                           @WebParam(name = "pageSize", targetNamespace = "") int pageSize)
             throws MethodFailedException {
         try {
             log.trace("Entering findObjects with param query=" + query + ", offset="+offset+", pageSize="+pageSize);
             SearchWS summaSearch = new SearchWSService(new java.net.URL(summaLocation),
                                                        new QName("http://statsbiblioteket.dk/summa/search", "SearchWSService")).getSearchWS();
 
             JSONObject jsonQuery = new JSONObject();
             jsonQuery.put("search.document.resultfields", "domsshortrecord");
             jsonQuery.put("search.document.query", query);
             jsonQuery.put("search.document.startindex", offset);
             jsonQuery.put("search.document.maxrecords", pageSize);
 
             String searchResultString = summaSearch.directJSON(jsonQuery.toString());
 
             Document searchResultDOM = DOM.stringToDOM(searchResultString);
             XPath xPath = XPathFactory.newInstance().newXPath();
 
             List<SearchResult> searchResults = new LinkedList<SearchResult>();
 
             NodeList nodeList = (NodeList) xPath.evaluate(
                     "//responsecollection/response/documentresult/record/field/shortrecord",
                     searchResultDOM.getDocumentElement(), XPathConstants.NODESET);
 
             for (int i=0; i<nodeList.getLength(); ++i) {
                 Node node = nodeList.item(i);
 
                 SearchResult searchResult = new SearchResult();
 
                 searchResult.setPid(xPath.evaluate("pid", node));
                 searchResult.setTitle(xPath.evaluate("title", node));
                 searchResult.setState(xPath.evaluate("state", node));
 
                 Calendar createdDate = DatatypeConverter.parseDateTime(xPath.evaluate("createdDate", node));
                 Calendar modifiedDate = DatatypeConverter.parseDateTime(xPath.evaluate("modifiedDate", node));
 
                 searchResult.setCreatedDate(createdDate.getTimeInMillis());
                 searchResult.setModifiedDate(modifiedDate.getTimeInMillis());
 
                 searchResults.add(searchResult);
             }
 
             return searchResults;
 
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (XPathExpressionException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                              e);
         }
     }
 
 
     @Override
     public void lockForWriting() throws InvalidCredentialsException, MethodFailedException {
 
 
         lock.lockForWriting(); //DO the lock
 
 
         try { //Execute a command to flush the unflushed triple changes.
             fedora.flushTripples();
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
 
     }
 
     @Override
     public void unlockForWriting() throws InvalidCredentialsException, MethodFailedException {
         lock.unlockForWriting();
     }
 
     @Override
     public User createTempAdminUser(@WebParam(name = "username", targetNamespace = "") String username,
                                     @WebParam(name = "roles", targetNamespace = "") List<String> roles)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             Credentials creds = getCredentials();//TODO perhaps we should check something here, against context.xml?
             AuthChecker auth = new AuthChecker(authCheckerLocation);
             dk.statsbiblioteket.doms.authchecker.user.User auser = auth.createTempAdminUser(username, roles);
             User user = new User();
             user.setUsername(auser.getUsername());
             user.setPassword(auser.getPassword());
             return user;
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     @Override
     public List<String> getObjectsInCollection(
             @WebParam(name = "collectionPid", targetNamespace = "") String collectionPid,
             @WebParam(name = "contentModelPid", targetNamespace = "") String contentModelPid)
             throws InvalidCredentialsException, InvalidResourceException, MethodFailedException {
         try {
             log.trace("Entering getObjectsInCollection with param collectionPid=" + collectionPid + " and contentModelPid="+contentModelPid);
             List<String> objects = fedora.getObjectsInCollection(collectionPid,contentModelPid);
             return objects;
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
 
     }
 
     private List<RecordDescription> transform(List<UpdateTrackerRecord> input) {
         List<RecordDescription> output = new ArrayList<RecordDescription>();
         for (UpdateTrackerRecord updateTrackerRecord : input) {
             output.add(transform(updateTrackerRecord));
         }
         return output;
     }
 
     private RecordDescription transform(UpdateTrackerRecord updateTrackerRecord) {
         RecordDescription a = new RecordDescription();
         a.setCollectionPid(updateTrackerRecord.getCollectionPid());
         a.setEntryContentModelPid(updateTrackerRecord.getEntryContentModelPid());
         a.setPid(updateTrackerRecord.getPid());
         a.setDate(updateTrackerRecord.getDate().getTime());
         return a;
     }
 
     private Credentials getCredentials() {
         HttpServletRequest request = (HttpServletRequest) context
                 .getMessageContext()
                 .get(MessageContext.SERVLET_REQUEST);
         Credentials creds = (Credentials) request.getAttribute("Credentials");
         if (creds == null) {
             log.warn("Attempted call at Central without credentials");
             creds = new Credentials("", "");
         }
         return creds;
 
     }
 
     private void logEntering(String method, String... params) {
         if (log.isTraceEnabled()) {
             String command = method + "(";
             for (String param : params) {
                 command = command + " " + param + ",";
             }
             command = command.substring(0, command.length() - 1) + ")";
             log.trace("Entering " + command);
         }
     }
 
     private static List<Relation> convertRelations(List<FedoraRelation> fedorarels) {
         List<Relation> outrealtions = new ArrayList<Relation>();
         for (FedoraRelation fedorarel : fedorarels) {
             Relation outrel = new Relation();
             outrel.setSubject(fedorarel.getSubject());
             outrel.setPredicate(fedorarel.getPredicate());
             outrel.setObject(fedorarel.getObject());
             outrel.setLiteral(fedorarel.isLiteral());
             outrealtions.add(outrel);
         }
         return outrealtions;
     }
 
 
 }
