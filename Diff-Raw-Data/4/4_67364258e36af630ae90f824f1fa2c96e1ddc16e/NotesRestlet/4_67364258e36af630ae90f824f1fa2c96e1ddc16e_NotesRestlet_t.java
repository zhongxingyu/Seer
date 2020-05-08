 package org.iucn.sis.server.extensions.notes;
 
 import java.util.Date;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.FieldIO;
 import org.iucn.sis.server.api.io.NoteIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 
 public class NotesRestlet extends BaseServiceRestlet {
 
 	public NotesRestlet(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/notes/{type}/{id}");
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		final String type = getType(request);
 		final Integer id = getID(request);
 		
 		final NoteIO noteIO = new NoteIO(session);
 		final FieldIO fieldIO = new FieldIO(session);
 		final TaxonIO taxonIO = new TaxonIO(session);
 		
 		if (type.equalsIgnoreCase("note")) {
 			Notes note = noteIO.get(id);
 			if (note != null) {
 				if (noteIO.delete(note))
 					response.setStatus(Status.SUCCESS_OK);
 				else
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 			
 		} else if (type.equalsIgnoreCase("field")) {
 			Field field = fieldIO.get(id);
 			if (field != null) {
 				for (Notes note : field.getNotes()) {
 					if (!noteIO.delete(note)){
 						response.setStatus(Status.SERVER_ERROR_INTERNAL);
 						return;
 					}
 				}
 				response.setStatus(Status.SUCCESS_OK);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 		} else if (type.equalsIgnoreCase("taxon")) {
 			Taxon taxon = taxonIO.getTaxon(id);
 			
 			if (taxon != null) {
 				for (Notes note : taxon.getNotes()) {
 					if (!noteIO.delete(note)){
 						response.setStatus(Status.SERVER_ERROR_INTERNAL);
 						return;
 					}
 				}
 				response.setStatus(Status.SUCCESS_OK);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 			
 		} else 
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		final String type = getType(request);
 		final Integer id = getID(request);
 		
 		final NoteIO noteIO = new NoteIO(session);
 		final FieldIO fieldIO = new FieldIO(session);
 		final TaxonIO taxonIO = new TaxonIO(session);
 		final AssessmentIO assessmentIO = new AssessmentIO(session);
 		
 		if (type.equalsIgnoreCase("note")) {
 			Notes note = noteIO.get(id);
 			if (note != null) {
 				return new StringRepresentation(note.toXML(), MediaType.TEXT_XML);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 		} else if (type.equalsIgnoreCase("field")) {
 			Field field = fieldIO.get(id);
 			if (field != null) {
 				StringBuilder xml = new StringBuilder("<xml>");
 				for (Notes note : field.getNotes())
 					xml.append(note.toXML());
 				xml.append("</xml>");
 				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 		} else if (type.equalsIgnoreCase("taxon")) {
 			Taxon taxon = taxonIO.getTaxon(id);
 			if (taxon != null) {
 				StringBuilder xml = new StringBuilder("<xml>");
 				for (Notes note : taxon.getNotes())
 					xml.append(note.toXML());
 				xml.append("</xml>");
 				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 		} else if (type.equalsIgnoreCase("assessment")) {
 			Assessment assessment = assessmentIO.getAssessment(id);
 			if (assessment != null) {
 				StringBuilder xml = new StringBuilder();
 				xml.append("<xml>");
 				/**
 				 * FIXME: a hibernate SQL query that searched 
 				 * the notes table would be nice here...
 				 */
 				if (assessment.getField() != null)
 					for (Field field : assessment.getField()) {
 						appendNotes(fieldIO, field, xml);
 					}
 				xml.append("</xml>");
 				return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 		} else
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
 	}
 	
 	private void appendNotes(FieldIO fieldIO, Field field, StringBuilder xml) {
 		Field full = fieldIO.get(field.getId());
 		if (full.getNotes() != null && !full.getNotes().isEmpty()) {
 			xml.append("<field name=\"" + full.getName() + ":" + full.getId() + "\">");
 			for (Notes note : full.getNotes()) 
 				xml.append(note.toXML());
 			xml.append("</field>");
 		}
 		for (Field subfield : field.getFields())
 			appendNotes(fieldIO, subfield, xml);
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		/**
 		 * FIXME: why are there multiple targets to do the same thing?
 		 * Shouldn't POST operations throw not found exceptions when 
 		 * trying to edit something that doesn't exist?
 		 */
 		handlePost(entity, request, response, session);
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final String type = getType(request);
 		final Integer id = getID(request);
 		
 		if (request.getResourceRef().getQueryAsForm().getFirstValue("option") != null
 				&& request.getResourceRef().getQueryAsForm().getFirstValue("option").equals("remove"))
 			handleDelete(request, response, session);
 		else {
 			NativeDocument document = new JavaNativeDocument();
 			document.parse(request.getEntityAsText());
 			
 			Notes note = Notes.fromXML(document.getDocumentElement());
 			
 			if (note.getValue() == null)
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No note provided.");
 			
 			Edit edit = new Edit();
 			edit.setUser(getUser(request, session));
 			edit.setCreatedDate(new Date());
 			edit.getNotes().add(note);
 			
 			note.getEdits().clear();
 			note.getEdits().add(edit);
 			
 			final NoteIO noteIO = new NoteIO(session);
 			final FieldIO fieldIO = new FieldIO(session);
 			final TaxonIO taxonIO = new TaxonIO(session);
 			final AssessmentIO assessmentIO = new AssessmentIO(session);
 			
 			if (type.equalsIgnoreCase("field")) {
 				Field field = fieldIO.get(id);
 				if (field != null) {
 					field.getNotes().add(note);
 					note.getFields().add(field);				
 					if (noteIO.save(note)) {
 						try {
 							SIS.get().getManager().saveObject(session, field);
 						} catch (PersistentException e) {
 							throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not save field", e); 
 						}
 						
 						response.setStatus(Status.SUCCESS_OK);
 						response.setEntity(note.toXML(), MediaType.TEXT_XML);
 					} else {
 						throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 					}
 				} else {
 					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 				}
 				
 			} else if (type.equalsIgnoreCase("taxon")) {
 				Taxon taxon = taxonIO.getTaxon(id);
 				if (taxon != null) {
 					taxon.getEdits().add(edit);
 					edit.getTaxon().add(taxon);
 					taxon.getNotes().add(note);
 					note.getTaxa().add(taxon);
 					if (noteIO.save(note)) {					
 						response.setStatus(Status.SUCCESS_OK);
 						response.setEntity(note.toXML(), MediaType.TEXT_XML);
 					} else {
 						throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 					}
 				} else {
 					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 				}
 			} else if (type.equals("synonym")) {
 				org.iucn.sis.shared.api.models.Synonym synonym;
 				try {
 					synonym = SIS.get().getManager().getObject(session, org.iucn.sis.shared.api.models.Synonym.class, id);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 				if (synonym == null)
 					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 				
 				try {
 					note.setSynonym(synonym);
 					synonym.getNotes().add(note);
 					
 					SIS.get().getManager().saveObject(session, note);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 				
 				synonym.getTaxon().toXML();
 				
 				response.setStatus(Status.SUCCESS_OK);
 				response.setEntity(note.toXML(), MediaType.TEXT_XML);
 			} else if (type.equalsIgnoreCase("commonName")) {
 				CommonName commonName;
 				try {
 					commonName = SIS.get().getManager().getObject(session, CommonName.class, id);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 				if (commonName == null)
 					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No " + type + " found for " + id);
 				
 				try {
 					note.setCommonName(commonName);
 					commonName.getNotes().add(note);
 					
 					SIS.get().getManager().saveObject(session, note);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 				
 				commonName.getTaxon().toXML();
 				
 				response.setStatus(Status.SUCCESS_OK);
 				response.setEntity(note.getId() + "", MediaType.TEXT_PLAIN);
 			} else 
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified: " + type);
 		}		
 	}
 	
 	private Integer getID(Request request) throws ResourceException {
 		try {
 			return Integer.valueOf((String) request.getAttributes().get("id"));
 		} catch (NullPointerException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify an ID", e);
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a valid numeric ID", e);
 		}
 	}
 	
 	private String getType(Request request) throws ResourceException {
 		String value = (String)request.getAttributes().get("type");
 		if (value == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please specify a type.");
 		
 		return value;
 	}
 }
