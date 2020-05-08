 /**
  * 
  */
 package com.github.podd.resources;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.OWLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.exception.PoddException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.PoddWebServiceApplication;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * Resource which allows uploading an artifact in the form of an RDF file.
  * 
  * @author kutila
  * 
  */
 public class UploadArtifactResourceImpl extends AbstractPoddResourceImpl
 {
     
     private static final String UPLOAD_PAGE_TITLE_TEXT = "PODD Upload New Artifact";
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private final Path tempDirectory;
     
     /**
      * Constructor: prepare temp directory
      */
     public UploadArtifactResourceImpl()
     {
         super();
         
         try
         {
             this.tempDirectory = Files.createTempDirectory("podd-ontologymanageruploads");
         }
         catch(final IOException e)
         {
             throw new RuntimeException("Could not create temporary directory", e);
         }
     }
     
     private InferredOWLOntologyID doUpload(final Representation entity) throws ResourceException
     {
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.info("authenticated user: {}", user);
         
         if(entity == null)
         {
             // POST request with no entity.
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not submit anything");
         }
         
         this.log.info("media-type: {}", entity.getMediaType());
         
         InferredOWLOntologyID artifactMap;
         
         if(MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true))
         {
             // - extract file from incoming Representation and load artifact to PODD
             artifactMap = this.uploadFileAndLoadArtifactIntoPodd(entity);
         }
         else
         {
             
             String formatString = this.getQuery().getFirstValue("format");
             
             if(formatString == null)
             {
                 // Use the media type that was attached to the entity as a fallback if they did not
                 // specify it as a query parameter
                 formatString = entity.getMediaType().getName();
             }
             
             final RDFFormat format = Rio.getParserFormatForMIMEType(formatString, RDFFormat.RDFXML);
             
             InputStream inputStream = null;
             
             try
             {
                 inputStream = entity.getStream();
             }
             catch(final IOException e)
             {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
             }
             artifactMap = this.uploadFileAndLoadArtifactIntoPodd(inputStream, format);
         }
         
         return artifactMap;
     }
     
     /**
      * Handle http GET request to serve the new artifact upload page.
      */
     @Get
     public Representation getUploadArtifactPage(final Representation entity) throws ResourceException
     {
         // even though this only does a page READ, we're checking authorization for CREATE since the
         // page
         // is for creating a new artifact via a file upload
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
         
         this.log.info("@Get UploadArtifactFile Page");
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "artifact_upload.html.ftl");
         dataModel.put("pageTitle", UploadArtifactResourceImpl.UPLOAD_PAGE_TITLE_TEXT);
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     /**
      * Handle http POST submitting a new artifact file
      */
     @Post(":html")
     public Representation uploadArtifactFileHtml(final Representation entity) throws ResourceException
     {
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
         
         this.log.info("@Post UploadArtifactFile Page");
         
         final InferredOWLOntologyID artifactMap = this.doUpload(entity);
         
         this.log.info("Successfully loaded artifact {}", artifactMap);
         
         // TODO - create and write to a template informing success
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "artifact_upload.html.ftl");
         dataModel.put("pageTitle", UploadArtifactResourceImpl.UPLOAD_PAGE_TITLE_TEXT);
         // This is now an InferredOWLOntologyID
         dataModel.put("artifact", artifactMap);
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
    @Post(":rdf|rj|json|ttl")
     public Representation uploadArtifactToRdf(final Representation entity, final Variant variant)
         throws ResourceException
     {
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
         
        this.log.info("@Post uploadArtifactFile RDF ({})", variant.getMediaType().getName());
         
         final InferredOWLOntologyID artifactMap = this.doUpload(entity);
         
         this.log.info("Successfully loaded artifact {}", artifactMap);
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         
         final RDFWriter writer =
                 Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                         output);
         
         try
         {
             writer.startRDF();
             OntologyUtils.ontologyIDsToHandler(Arrays.asList(artifactMap), writer);
             writer.endRDF();
         }
         catch(final RDFHandlerException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
         }
         
         return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(writer.getRDFFormat()
                 .getDefaultMIMEType()));
     }
     
     /**
      * Handle http POST submitting a new artifact file Returns a text String containing the added
      * artifact's Ontology IRI.
      * 
      */
     @Post(":txt")
     public Representation uploadArtifactToText(final Representation entity, final Variant variant)
         throws ResourceException
     {
         this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
         
         this.log.info("@Post uploadArtifactFile ({})", variant.getMediaType().getName());
         
         final InferredOWLOntologyID artifactMap = this.doUpload(entity);
         
         this.log.info("Successfully loaded artifact {}", artifactMap.getOntologyIRI().toString());
         
         return new StringRepresentation(artifactMap.getOntologyIRI().toString());
     }
     
     /**
      * 
      * @param inputStream
      *            The input stream containing an RDF document in the given format that is to be
      *            uploaded.
      * @param format
      *            The determined, or at least specified, format for the serialised RDF triples in
      *            the input.
      * @return
      * @throws ResourceException
      */
     private InferredOWLOntologyID uploadFileAndLoadArtifactIntoPodd(final InputStream inputStream,
             final RDFFormat format) throws ResourceException
     {
         /*
          * Guess mime type using any23 final MIMEType parsedContentType =
          * MIMEType.parse(contentType);
          * 
          * final MIMEType guessedMIMEType = new TikaMIMETypeDetector().guessMIMEType(filename,
          * inputStream, parsedContentType); final MediaType finalMimeType =
          * MediaType.valueOf(guessedMIMEType.toString());
          */
         final PoddArtifactManager artifactManager =
                 ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
         try
         {
             if(artifactManager != null)
             {
                 final InferredOWLOntologyID loadedArtifact = artifactManager.loadArtifact(inputStream, format);
                 // resultsMap.put("iri", loadedArtifact.getOntologyIRI().toString());
                 // resultsMap.put("versionIri", loadedArtifact.getVersionIRI().toString());
                 // resultsMap.put("inferredIri",
                 // loadedArtifact.getInferredOntologyIRI().toString());
                 return loadedArtifact;
             }
             else
             {
                 throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                         "Could not find PODD Artifact Manager");
             }
         }
         catch(OpenRDFException | PoddException | IOException | OWLException e)
         {
             this.log.error("Failed to load artifact {}", e);
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error loading artifact to PODD", e);
         }
         
     }
     
     private InferredOWLOntologyID uploadFileAndLoadArtifactIntoPodd(final Representation entity)
         throws ResourceException
     {
         List<FileItem> items;
         Path file = null;
         String contentType = null;
         
         // 1: Create a factory for disk-based file items
         final DiskFileItemFactory factory = new DiskFileItemFactory(1000240, this.tempDirectory.toFile());
         
         // 2: Create a new file upload handler
         final RestletFileUpload upload = new RestletFileUpload(factory);
         final Map<String, String> props = new HashMap<String, String>();
         try
         {
             // 3: Request is parsed by the handler which generates a list of FileItems
             items = upload.parseRequest(this.getRequest());
             
             for(final FileItem fi : items)
             {
                 final String name = fi.getName();
                 
                 if(name == null)
                 {
                     props.put(fi.getFieldName(), new String(fi.get(), StandardCharsets.UTF_8));
                 }
                 else
                 {
                     // FIXME: Strip everything up to the last . out of the filename so that
                     // the filename can be used for content type determination where
                     // possible.
                     // Note: These are Java-7 APIs
                     file = Files.createTempFile(this.tempDirectory, "ontologyupload-", name);
                     
                     contentType = fi.getContentType();
                     props.put("Content-Type", fi.getContentType());
                     // InputStream uploadedFileInputStream = fi.getInputStream();
                     try
                     {
                         fi.write(file.toFile());
                     }
                     catch(final IOException ioe)
                     {
                         throw ioe;
                     }
                     catch(final Exception e)
                     {
                         // avoid throwing a generic exception just because the apache
                         // commons library throws Exception
                         throw new IOException(e);
                     }
                 }
             }
         }
         catch(final IOException | FileUploadException e)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
         }
         
         this.log.info("props={}", props.toString());
         
         if(file == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not submit a valid file and filename");
         }
         
         this.log.info("filename={}", file.toAbsolutePath().toString());
         this.log.info("contentType={}", contentType);
         
         RDFFormat format = null;
         
         // If the content type was application/octet-stream then use the file name instead
         // Browsers attach this content type when they are not sure what the real type is
         if(MediaType.APPLICATION_OCTET_STREAM.getName().equals(contentType))
         {
             format = Rio.getParserFormatForFileName(file.getFileName().toString());
             
             this.log.info("octet-stream contentType filename format={}", format);
         }
         // Otherwise use the content type directly in preference to using the filename
         else if(contentType != null)
         {
             format = Rio.getParserFormatForMIMEType(contentType);
             
             this.log.info("non-octet-stream contentType format={}", format);
         }
         
         // If the content type choices failed to resolve the type, then try the filename
         if(format == null)
         {
             format = Rio.getParserFormatForFileName(file.getFileName().toString());
             
             this.log.info("non-content-type filename format={}", format);
         }
         
         // Or fallback to RDF/XML which at minimum is able to detect when the document is
         // structurally invalid
         if(format == null)
         {
             this.log.warn("Could not determine RDF format from request so falling back to RDF/XML");
             format = RDFFormat.RDFXML;
         }
         
         InputStream inputStream = null;
         try
         {
             inputStream = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ));
         }
         catch(final IOException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "File IO error occurred", e);
         }
         
         return this.uploadFileAndLoadArtifactIntoPodd(inputStream, format);
     }
     
 }
