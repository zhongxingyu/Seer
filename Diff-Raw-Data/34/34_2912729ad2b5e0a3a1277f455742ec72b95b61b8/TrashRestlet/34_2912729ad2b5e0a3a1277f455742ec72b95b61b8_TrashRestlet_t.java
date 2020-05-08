 package org.iucn.sis.server.restlets.utils;
 
 import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.ServiceRestlet;
 import org.iucn.sis.server.api.utils.FormattedDate;
 import org.iucn.sis.server.api.utils.TaxomaticException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.io.AssessmentIOMessage;
 import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class TrashRestlet extends ServiceRestlet {
 
 
 	public TrashRestlet(String vfsroot, Context context) {
 		super(vfsroot, context);
 	}
 
 	public void definePaths() {
 		paths.add("/trash/{action}");
 		paths.add("/trash/{action}/{option1}");
 
 	}
 
 	private void handleDelete(Request request, Response response) {
 		try {
 			Document doc = new DomRepresentation(request.getEntity()).getDocument();
 			Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
 			String id = element.getAttribute("id");
 			String type = element.getAttribute("type");
 			
 			boolean success = false;
 			String message = null;
 			if (type.equalsIgnoreCase("taxon")) {
 				success = SIS.get().getTaxonIO().permanentlyDeleteTaxon(Integer.parseInt(id));
 				message = "Unable to delete taxon " + id;
 			} else if (type.equalsIgnoreCase("assessment")) {
 				success = SIS.get().getAssessmentIO().permenantlyDeleteAssessment(Integer.valueOf(id), SIS.get().getUser(request));
 				message = "Unable to delete assessment " + id;
 			} else {
 				message = "Invalid type -- should be assessment or taxon.";
 			}
 			if (success) {
 				response.setStatus(Status.SUCCESS_OK);
 			} else {
 				response.setEntity(message, MediaType.TEXT_PLAIN);
 				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			}
 			response.setEntity(message, MediaType.TEXT_PLAIN);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void handleDeleteAll(Request request, Response response) {
 		boolean success = SIS.get().getAssessmentIO().permenantlyDeleteAllTrashedAssessments() && SIS.get().getTaxonIO().permenantlyDeleteAllTrashedTaxa();
 		if (success) {
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity("All trashed items have been deleted.", MediaType.TEXT_PLAIN);
 		} else {
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 			response.setEntity("Items could not be deleted.", MediaType.TEXT_PLAIN);
 		}
 	}
 
 	/**
 	 * Gets all assessments and taxa that have been deleted
 	 * 
 	 * @param request
 	 * @param response
 	 * @param ec
 	 */
 	private void handleGet(Request request, Response response) {
		Date defaultDate = Calendar.getInstance().getTime();
 		StringBuilder xml = new StringBuilder("<trash>");
 		try {
 			for (Assessment assessment : SIS.get().getAssessmentIO().getTrashedAssessments()) {
 				xml.append("<data id=\"" + assessment.getId() + "\" ");
 				xml.append("type=\"assessment\" ");
 				xml.append("status=\"" + assessment.getAssessmentType().getDisplayName() + "\" ");
				if (!assessment.getEdit().isEmpty()) {
					Edit last = assessment.getLastEdit();
					xml.append("user=\"" + last.getUser().getUsername() + "\" ");
					xml.append("date=\"" + FormattedDate.impl.getDate(last.getCreatedDate())
							+ "\" ");
				}
				else {
					xml.append("user=\"Unknown\" ");
					xml.append("date=\"" + FormattedDate.impl.getDate(defaultDate) + "\" ");
				}
 				xml.append("node=\"" + assessment.getSpeciesName() + "\" ");
 				xml.append("display=\"\" ");
 				xml.append("/>");
 			}
 
 			for (Taxon taxon : SIS.get().getTaxonIO().getTrashedTaxa()) {
 				xml.append("<data id=\"" + taxon.getId() + "\" ");
 				xml.append("type=\"taxon\" ");
 				xml.append("status=\"\" ");
				if (!taxon.getEdits().isEmpty()) {
					Edit last = taxon.getLastEdit();
					xml.append("user=\"" + last.getUser().getUsername() + "\" ");
					xml.append("date=\"" + FormattedDate.impl.getDate(last.getCreatedDate()) + "\" ");
				}
				else {
					xml.append("user=\"Unknown\" ");
					xml.append("date=\"" + FormattedDate.impl.getDate(defaultDate) + "\" ");	
				}
 				xml.append("node=\"" + taxon.getFullName() + "\" ");
 				xml.append("display=\"\" ");
 				xml.append("/>");
 			}
 			xml.append("</trash>");
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity(xml.toString(), MediaType.TEXT_XML);
 		} catch (PersistentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 			response.setEntity(e.getMessage(), MediaType.TEXT_PLAIN);
 		}
 	}
 
 	private void handleRestore(Request request, Response response) {
 		try {
 			String restoreRelatedAssessments = (String) request.getAttributes().get("option1");
 			Document doc = new DomRepresentation(request.getEntity()).getDocument();
 			Element element = (Element) doc.getDocumentElement().getElementsByTagName("data").item(0);
 			String id = element.getAttribute("id");
 			String type = element.getAttribute("type");
 			User user = SIS.get().getUser(request);
 
 			boolean success = false;
 			String message = null;
 			if (type.equalsIgnoreCase("taxon")) {
 				try {
 					SIS.get().getTaxonIO().restoreTrashedTaxon(Integer.valueOf(id), user);
 				} catch (TaxomaticException e) {
 					response.setEntity(new DomRepresentation(MediaType.TEXT_XML, e.getErrorAsDocument()));
 					response.setStatus(e.isClientError() ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SERVER_ERROR_INTERNAL);
 					return;
 				}
 				
 				if (restoreRelatedAssessments != null && restoreRelatedAssessments.equalsIgnoreCase("true")) {
 					AssessmentIOMessage m = SIS.get().getAssessmentIO().restoreDeletedAssessmentsAssociatedWithTaxon(Integer
 							.valueOf(id), SIS.get().getUser(request));
 					message = m.getMessage();
 					if (m.getFailed() == null || m.getFailed().isEmpty()) {
 						success = true;
 					}
 				} else {
 					success = true;
 				}
 			} else if (type.equalsIgnoreCase("assessment")) {
 				success = SIS.get().getAssessmentIO().restoreTrashedAssessments(Integer.valueOf(id), SIS.get().getUser(request)).status.isSuccess();
 				if (success) {
 					Taxon taxon = SIS.get().getAssessmentIO().getNonCachedAssessment(Integer.valueOf(id)).getTaxon();
 					if (taxon.getState() == Taxon.DELETED) {
 						try {
 							SIS.get().getTaxonIO().restoreTrashedTaxon(taxon.getId(), SIS.get().getUser(request));
 						} catch (TaxomaticException e) {
 							Debug.println(e);
 							success = false;
 						}
 					}
 				}
 			} else {
 				message = "Invalid type -- should be assessment or taxon.";
 			}
 			if (success && message == null)
 				message = "Successfully restored " + type;
 			else if (!success && message == null)
 				message = "Unable to find " + type;
 
 			if (success)
 				response.setStatus(Status.SUCCESS_OK);
 			else
 				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			response.setEntity(message, MediaType.TEXT_PLAIN);
 		} catch (IOException e) {
 			e.printStackTrace();
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		} catch (PersistentException e) {
 			e.printStackTrace();
 			response.setStatus(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 	public void performService(Request request, Response response) {
 		try {
 			if (((String) request.getAttributes().get("action")).equals("list"))
 				handleGet(request, response);
 			if (((String) request.getAttributes().get("action")).equals("restore"))
 				handleRestore(request, response);
 			if (((String) request.getAttributes().get("action")).equals("delete"))
 				handleDelete(request, response);
 			if (((String) request.getAttributes().get("action")).equals("deleteall"))
 				handleDeleteAll(request, response);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
