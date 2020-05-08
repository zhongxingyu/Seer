 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.rest;
 
 import com.elasticgrid.cluster.ClusterManager;
 import com.elasticgrid.model.Cluster;
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.model.S3Object;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.ext.wadl.DocumentationInfo;
 import org.restlet.ext.wadl.MethodInfo;
 import org.restlet.ext.wadl.RepresentationInfo;
 import org.restlet.ext.wadl.WadlResource;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 import org.rioproject.core.OperationalString;
 import org.rioproject.core.OperationalStringManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ApplicationsResource extends WadlResource {
     private String clusterName;
     private String dropBucket;
 
 //    @Autowired
     private Configuration config;
 
     @Autowired
     private S3Service s3;
 
     @Autowired
     private ClusterManager clusterManager;
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         // Allow modifications of this resource via POST requests
         setModifiable(true);
         // Declare the kind of representations supported by this resource
         getVariants().add(new Variant(MediaType.APPLICATION_XML));
         // Extract URI variables
         clusterName = (String) request.getAttributes().get("clusterName");
     }
 
     /**
      * Handle GET requests: describe all applications.
      */
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         try {
             List<OperationalStringManager> opstringMgrs = RestJSB.getOperationalStringManagers();
             for (OperationalStringManager opstringMgr : opstringMgrs) {
                 OperationalString opstring = opstringMgr.getOperationalString();
                 Logger.getLogger(getClass().getName()).info(opstring.getName());
             }
 
             logger.log(Level.INFO, "Requested variant {0}", variant.getMediaType());
             if (MediaType.TEXT_HTML.equals(variant.getMediaType())
                     || MediaType.APPLICATION_XML.equals(variant.getMediaType())) {
                 Cluster cluster = clusterManager.cluster(clusterName);
                 if (cluster == null)
                     throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find cluster " + clusterName);
                 logger.log(Level.INFO, "Found cluster {0}", cluster);
                 config = new Configuration();
                 config.setTemplateLoader(new ClassTemplateLoader(getClass(), "/com/elasticgrid/rest"));
                 Map<String, Object> model = new HashMap<String, Object>();
                 model.put("cluster", cluster);
                 return new TemplateRepresentation("applications.ftl", config, model, MediaType.TEXT_HTML);
             } else {
                 return new StringRepresentation("so???");
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
         }
     }
 
     /**
      * Handle POST requests: provision a new application.
      */
     @Override
     public void storeRepresentation(Representation entity) throws ResourceException {
         super.acceptRepresentation(entity);
         if (MediaType.MULTIPART_ALL.equals(entity.getMediaType())
                 || MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType())) {
             try {
                 System.out.println("trace0");
                 DiskFileItemFactory factory = new DiskFileItemFactory();
                 factory.setSizeThreshold(1000240);
                 RestletFileUpload upload = new RestletFileUpload(factory);
                 List<FileItem> files = upload.parseRequest(getRequest());
 
                 logger.log(Level.INFO, "Found {0} items", files.size());
                 for (FileItem fi : files) {
                     if ("oar".equals(fi.getFieldName())) {
                         // download it as a temp file
                         File file = File.createTempFile("elastic-grid", "oar");
                         fi.write(file);
                         // upload it to S3
                         logger.log(Level.INFO, "Uploading OAR '{0}' to S3 bucket '{1}'",
                                 new Object[]{fi.getName(), dropBucket});
                         S3Object object = new S3Object(fi.getName());
                         object.setDataInputFile(file);
                         s3.putObject(dropBucket, object);
                     }
                 }
 
                 // Set the status of the response.
                 logger.info("Redirecting to " + getRequest().getOriginalRef());
                 getResponse().setLocationRef(getRequest().getOriginalRef().addSegment("??"));  // todo: figure out the proper URL
             } catch (FileUploadException e) {
                 e.printStackTrace();
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
             }
         } else if (new MediaType("application/oar").equals(entity.getMediaType())) {
             System.out.println("trace1");
             try {
                 // extract filename information
                 Form form = (Form) getRequest().getAttributes().get("org.restlet.http.headers");
                 // upload it to S3
                 String fileName = form.getFirstValue("x-filename");
                 logger.log(Level.INFO, "Uploading OAR '{0}' to S3 bucket '{1}'",
                         new Object[]{fileName, dropBucket});
                 S3Object object = new S3Object(fileName);
                 object.setDataInputStream(entity.getStream());
                 s3.putObject(dropBucket, object);
                 // Set the status of the response
                 logger.info("Redirecting to " + getRequest().getOriginalRef());
                 getResponse().setLocationRef(getRequest().getOriginalRef().addSegment("??"));  // todo: figure out the proper URL
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
             }
         } else {
             throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
         }
     }
 
     @Override
     protected void describeGet(MethodInfo info) {
         super.describeGet(info);
         info.setDocumentation("Describe all applications running on the cluster {clusterName}.");
         info.getResponse().setDocumentation("The cluster.");
         RepresentationInfo representation = new RepresentationInfo();
         representation.setDocumentation("This resource exposes applications running on cluster {clusterName}.");
         representation.getDocumentations().get(0).setTitle("applications");
         representation.setMediaType(MediaType.APPLICATION_XML);
         representation.getDocumentations().addAll(Arrays.asList(
                 new DocumentationInfo("Example of output:<pre><![CDATA[" +
                         "<applications xmlns=\"urn:elastic-grid:eg\">\n" +
                         "  <application name=\"myapp\" cluster=\"cluster2\">\n" +
                         "    <service name=\"My First Service\">\n" +
                         "      <provisioning planned=\"1\" deployed=\"1\" pending=\"0\"/>\n" +
                         "    </service>\n" +
                         "    <service name=\"My Second Service\">\n" +
                         "      <provisioning planned=\"1\" deployed=\"1\" pending=\"0\"/>\n" +
                         "    </service>\n" +
                         "  </application>\n" +
                         "</applications>" +
                         "]]></pre>")
         ));
         representation.setXmlElement("eg:applications");
         info.getResponse().setRepresentations(Arrays.asList(representation));
     }
 
     @Override
     protected void describePost(MethodInfo info) {
         super.describePost(info);
         info.setDocumentation("Provision a new application on {clusterName}.");
         info.getRequest().setDocumentation("The application to provision packaged as an OAR.");
         RepresentationInfo formRepresentation = new RepresentationInfo();
         formRepresentation.setDocumentation("HTML form with file uploads.");
         formRepresentation.setMediaType(MediaType.MULTIPART_FORM_DATA);
         info.getRequest().setRepresentations(Arrays.asList(formRepresentation));
     }
 
     @Override
     public boolean allowPut() {
         return false;
     }
 
     @Override
     public boolean allowDelete() {
         return false;
     }
 
     @Required
     public void setDropBucket(String dropBucket) {
         this.dropBucket = dropBucket;
     }
 }
