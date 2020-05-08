 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Resource which lists the existing artifacts in PODD.
  * <p>
  * TODO: list based on authorization, group projects. list project title, description, PI and lead
  * institution
  * 
  * @author kutila
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class ListArtifactsResourceImpl extends AbstractPoddResourceImpl
 {
     public static final String LIST_PAGE_TITLE_TEXT = "PODD Artifact Listing";
     
     private Map<String, List<InferredOWLOntologyID>> getArtifactsInternal() throws ResourceException
     {
         final Map<String, List<InferredOWLOntologyID>> results = new HashMap<String, List<InferredOWLOntologyID>>();
         
         final String publishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_PUBLISHED, true);
         final String unpublishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_UNPUBLISHED, true);
         
         // default to both published and unpublished to start with
         boolean published = true;
         boolean unpublished = false;
         
         if(publishedString != null)
         {
             published = Boolean.parseBoolean(publishedString);
         }
         
         // If the user is authenticated, set unpublished to true before checking the query
         // parameters
         // if(this.getClientInfo().isAuthenticated())
         // {
         // this.log.info("User was logged in");
         // unpublished = true;
         // }
         
         if(unpublishedString != null)
         {
             unpublished = Boolean.parseBoolean(unpublishedString);
         }
         else
         {
             // Default to showing unpublished artifacts to authenticated users if they did not
             // specify anything in their query parameters
             unpublished = this.getRequest().getClientInfo().isAuthenticated();
         }
         
         if(published)
         {
             this.log.info("Including published artifacts");
         }
         
         if(unpublished)
         {
             this.log.info("Including unpublished artifacts");
         }
         
         if(!published && !unpublished)
         {
             this.log.error("Both published and unpublished artifacts were disabled in query");
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "Both published and unpublished artifacts were disabled in query");
         }
         
         try
         {
             if(published)
             {
                 final List<InferredOWLOntologyID> publishedResults = new ArrayList<InferredOWLOntologyID>();
                 
                 final List<InferredOWLOntologyID> publishedArtifacts =
                         this.getPoddArtifactManager().listPublishedArtifacts();
                 
                 for(final InferredOWLOntologyID nextPublishedArtifact : publishedArtifacts)
                 {
                     if(this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ, nextPublishedArtifact
                             .getOntologyIRI().toOpenRDFURI(), false))
                     {
                         // If the authentication succeeded add the artifact
                         publishedResults.add(nextPublishedArtifact);
                     }
                 }
                 
                 results.put(PoddWebConstants.KEY_PUBLISHED, publishedArtifacts);
             }
             
            if(unpublished && this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_LIST, null, false))
             {
                 this.log.info("About to check for authentication to look at unpublished artifacts");
                 this.log.info("Is authenticated: {}", this.getRequest().getClientInfo().isAuthenticated());
                 this.log.info("Current user: {}", this.getRequest().getClientInfo().getUser());
                 
                 final List<InferredOWLOntologyID> unpublishedResults = new ArrayList<InferredOWLOntologyID>();
                 
                 final List<InferredOWLOntologyID> unpublishedArtifacts =
                         this.getPoddArtifactManager().listUnpublishedArtifacts();
                 
                 for(final InferredOWLOntologyID nextUnpublishedArtifact : unpublishedArtifacts)
                 {
                     try
                     {
                         if(this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, nextUnpublishedArtifact
                                 .getOntologyIRI().toOpenRDFURI(), false))
                         {
                             // If the authentication succeeded add the artifact
                             unpublishedResults.add(nextUnpublishedArtifact);
                         }
                     }
                     catch(final ResourceException e)
                     {
                         // Ignore this as it should not happen with the throwExceptionOnFailure
                         // parameter set to false.
                     }
                 }
                 results.put(PoddWebConstants.KEY_UNPUBLISHED, unpublishedResults);
             }
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Database exception", e);
         }
         
         return results;
     }
     
     /**
      * Handle http GET request to serve the list artifacts page.
      */
     @Get(":html")
     public Representation getListArtifactsPage(final Representation entity) throws ResourceException
     {
         this.log.info("@Get listArtifacts Page");
         
         final Map<String, List<InferredOWLOntologyID>> artifactsInternal = this.getArtifactsInternal();
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "projects.html.ftl");
         dataModel.put("pageTitle", ListArtifactsResourceImpl.LIST_PAGE_TITLE_TEXT);
         
         // Disable currently unimplemented features
         dataModel.put("canFilter", Boolean.FALSE);
         dataModel.put("hasFilter", Boolean.FALSE);
         
         
         if (this.checkAuthentication(PoddAction.ARTIFACT_CREATE, null, false))
         {
             dataModel.put("userCanCreate", Boolean.TRUE);    
         }
         else
         {
             dataModel.put("userCanCreate", Boolean.FALSE);
         }
         
         this.log.info("artifacts: {}", artifactsInternal);
         
         for(final String nextKey : artifactsInternal.keySet())
         {
             try
             {
                 final List<PoddObjectLabel> results =
                         this.getPoddArtifactManager().getTopObjectLabels(artifactsInternal.get(nextKey));
                 dataModel.put(nextKey + "ArtifactsList", results);
             }
             catch(final OpenRDFException e)
             {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find labels for " + nextKey
                         + " artifacts", e);
             }
         }
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     @Get(":rdf|rj|json|ttl")
     public Representation getListArtifactsRdf(final Representation entity, final Variant variant)
         throws ResourceException
     {
         final Map<String, List<InferredOWLOntologyID>> artifactsInternal = this.getArtifactsInternal();
         
         final RDFFormat resultFormat = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName());
         
         if(resultFormat == null)
         {
             this.log.error("Could not find an RDF serialiser matching the requested mime-type: "
                     + variant.getMediaType().getName());
             throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                     "Could not find an RDF serialiser matching the requested mime-type: "
                             + variant.getMediaType().getName());
         }
         
         final MediaType resultMediaType = MediaType.valueOf(resultFormat.getDefaultMIMEType());
         
         final ByteArrayOutputStream out = new ByteArrayOutputStream(8096);
         final RDFWriter writer = Rio.createWriter(resultFormat, out);
         
         try
         {
             writer.startRDF();
             for(final String nextKey : artifactsInternal.keySet())
             {
                 // log.info("nextArtifact: {}", nextKey);
                 OntologyUtils.ontologyIDsToHandler(artifactsInternal.get(nextKey), writer);
             }
             writer.endRDF();
         }
         catch(final RDFHandlerException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "Could not generate RDF output due to an exception in the writer", e);
         }
         
         this.log.info(new String(out.toByteArray(), StandardCharsets.UTF_8));
         
         final ByteArrayRepresentation result = new ByteArrayRepresentation(out.toByteArray(), resultMediaType);
         
         return result;
     }
     
 }
