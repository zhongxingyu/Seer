 package org.sortdc.sortdc.resources;
 
 import com.sun.jersey.api.NotFoundException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.sortdc.sortdc.Classifier;
 import org.sortdc.sortdc.Log;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.database.ObjectNotFoundException;
 import org.sortdc.sortdc.resources.dto.CategoryDTO;
 import org.sortdc.sortdc.resources.dto.DocumentDTO;
 import org.sortdc.sortdc.resources.dto.DocumentsDTO;
 
 @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 public class DocumentsResource {
 
     private Classifier classifier;
     private Category category;
 
     public DocumentsResource(Classifier classifier) {
         this.classifier = classifier;
     }
 
     public DocumentsResource(Classifier classifier, Category category) {
         this.classifier = classifier;
         this.category = category;
     }
 
     /**
      * Retrieves list of all documents
      * 
      * @return
      */
     @GET
     public DocumentsDTO get() {
         List<Document> documents;
         DocumentsDTO documents_dto;
         String classifier_id = this.classifier.getId();
         try {
             if (this.category == null) {
                 documents = this.classifier.findAllDocuments();
                 documents_dto = new DocumentsDTO(classifier_id);
 
                 Map<String, CategoryDTO> categories_dto = new HashMap<String, CategoryDTO>();
                 for (Document document : documents) {
                    DocumentDTO document_dto = new DocumentDTO(classifier_id, document.getId());
                     String category_id = document.getCategoryId();
                     CategoryDTO category_dto;
                     if (!categories_dto.containsKey(category_id)) {
                         category_dto = new CategoryDTO(classifier_id, category_id);
                         categories_dto.put(category_id, category_dto);
                     } else {
                         category_dto = categories_dto.get(category_id);
                     }
                     document_dto.category = category_dto;
                     documents_dto.add(document_dto);
                 }
             } else {
                 documents = this.classifier.findDocumentsByCategoryId(this.category.getId());
                 documents_dto = new DocumentsDTO(classifier_id, this.category.getId());
 
                 for (Document document : documents) {
                    DocumentDTO document_dto = new DocumentDTO(classifier_id, document.getId());
                     documents_dto.add(document_dto);
                 }
             }
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return documents_dto;
     }
 
     /**
      * Adds a new document
      * 
      * @return
      */
     @POST
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Response post(DocumentDTO request) {
         if (request == null) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
 
         String category_id;
         if (request.category != null && request.category.id != null) {
             if (!request.category.id.matches("^[a-zA-Z0-9_-]+$")) {
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             category_id = request.category.id;
         } else if (this.category != null) {
             category_id = this.category.getId();
         } else {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
 
         Document document = new Document();
         if (request.id != null) {
             if (!request.id.matches("^[a-zA-Z0-9_-]+$")) {
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             document.setId(request.id);
         }
         document.setCategoryId(category_id);
 
         Map<String, Integer> tokens;
         try {
             tokens = this.classifier.extractTokens(request.text, request.html, request.getTokens());
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         if (tokens.isEmpty()) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         try {
             document.setTokensOccurrences(tokens);
             this.classifier.saveDocument(document);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         DocumentDTO document_dto = this.getDocumentDTO(document);
         URI uri = URI.create(document_dto.getHref());
         return Response.created(uri).entity(document_dto).build();
     }
 
     /**
      * Adds a new document
      * 
      * @return
      */
     @POST
     @Consumes(MediaType.TEXT_PLAIN)
     public Response postText(String text) {
         if (this.category == null) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
 
         Document document = new Document();
         document.setCategoryId(this.category.getId());
 
         Map<String, Integer> tokens;
         try {
             tokens = this.classifier.extractTokens(text, null, null);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         if (tokens.isEmpty()) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         try {
             document.setTokensOccurrences(tokens);
             this.classifier.saveDocument(document);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         DocumentDTO document_dto = this.getDocumentDTO(document);
         URI uri = URI.create(document_dto.getHref());
         return Response.created(uri).entity(document_dto).build();
     }
 
     /**
      * Generates a DocumentDTO from a Document
      * 
      * @param document
      * @return 
      */
     public DocumentDTO getDocumentDTO(Document document) {
         String classifier_id = this.classifier.getId();
         DocumentDTO document_dto = new DocumentDTO(classifier_id, document.getId());
         document_dto.category = new CategoryDTO(classifier_id, document.getCategoryId());
         document_dto.setTokens(document.getTokensOccurrences());
         return document_dto;
     }
 
     /**
      * Deletes all classifier's categories and documents
      * 
      * @return
      */
     @DELETE
     public Response delete() {
         try {
             if (this.category == null) {
                 this.classifier.empty();
             } else {
                 this.classifier.deleteCategoryById(this.category.getId());
             }
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return Response.ok().build();
     }
 
     /**
      * Returns a new document resource
      * 
      * @param document_id
      * @return
      */
     @Path("/{document: [a-zA-Z0-9_-]+}")
     public DocumentResource getDocumentResource(@PathParam("document") String document_id) {
         if (this.category != null) {
             throw new NotFoundException();
         }
         Document document;
         try {
             document = this.classifier.findDocumentById(document_id);
         } catch (ObjectNotFoundException e) {
             throw new NotFoundException();
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         try {
             this.category = this.classifier.getCategory(document.getCategoryId());
         } catch (ObjectNotFoundException e) {
             throw new NotFoundException();
         }
         return new DocumentResource(this.classifier, this.category, document);
     }
 }
