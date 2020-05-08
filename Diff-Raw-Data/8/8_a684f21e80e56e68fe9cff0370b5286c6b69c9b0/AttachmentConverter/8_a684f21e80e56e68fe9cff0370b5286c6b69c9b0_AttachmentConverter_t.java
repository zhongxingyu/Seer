 package org.iucn.sis.shared.conversions;
 
 import java.io.IOException;
 
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.FieldAttachment;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.solertium.util.ElementCollection;
 import com.solertium.util.NodeCollection;
 import com.solertium.util.TrivialExceptionHandler;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPath;
 import com.solertium.vfs.VFSPathToken;
 
 public class AttachmentConverter extends GenericConverter<VFSInfo> {
 	
 	@Override
 	protected void run() throws Exception {
 		final UserIO io = new UserIO(session);
 		
 		final VFS vfs = data.getOldVFS();
 		final VFSPath start = new VFSPath("/attachments");
 		
 		if (vfs.exists(start))
 			scanFolder(io.getUserFromUsername("admin"), vfs, start);
 		else
 			printf("No attachments to import found at %s, skipping...", start);
 	}
 	
 	private void scanFolder(User user, VFS vfs, VFSPath folder) throws IOException {
 		for (VFSPathToken token : vfs.list(folder)) {
 			final VFSPath uri = folder.child(token);
 			if (vfs.isCollection(uri))
 				scanFolder(user, vfs, uri);
			else if ("_attachments.xml".equals(token.toString()))
 				scanFile(user, vfs, uri);
 		}
 	}
 	
 	private void scanFile(User user, VFS vfs, VFSPath uri) throws IOException {
 		final Document document = vfs.getDocument(uri);
 		final ElementCollection nodes = new ElementCollection(document.getDocumentElement().getElementsByTagName("attachment"));
 		for (Element node : nodes) {
 			String assessmentID = node.getAttribute("assessmentID");
 			String filename = null;
 			boolean published = true;
 			
 			for (Node child : new NodeCollection(node.getChildNodes())) {
 				if ("filename".equals(child.getNodeName())) {
 					filename = child.getTextContent();
 				}
 				else if ("published".equals(child.getNodeName())) {
 					published = !"false".equals(child.getTextContent());
 				}
 			}
 			
 			Assessment assessment = null;
 			try {
 				assessment =(Assessment)session.createCriteria(Assessment.class).
 					add(Restrictions.eq("internalId", assessmentID)).uniqueResult();
 			} catch (Exception e) {
 				TrivialExceptionHandler.ignore(this, e);
 			}
 			
 			if (assessment == null) {
 				printf("Could not find assessment for %s", assessmentID);
 				continue;
 			}
 			
 			Edit edit = new Edit();
 			edit.setUser(user);
 			
 			FieldAttachment attachment = new FieldAttachment();
 			attachment.getEdits().add(edit);
 			attachment.setName(filename);
 			attachment.setPublish(published);
			attachment.setKey(uri.getCollection().child(new VFSPathToken(filename)).toString());
 			
 			Field field = assessment.getField(CanonicalNames.TaxonomicNotes);
 			if (field == null) {
 				Edit noteEdit = new Edit();
 				noteEdit.setUser(user);
 				
 				Notes note = new Notes();
 				note.setEdit(noteEdit);
 				note.setValue("This field was auto-generated to house an " +
 					"attachment; removing this note and removing all data " +
 					"from this field will result in the attachment being lost.");
 				
 				field = new Field(CanonicalNames.TaxonomicNotes, assessment);
 				field.getNotes().add(note);
 				
 				note.setField(field);
 			}
 				
 			attachment.getFields().add(field);
 			
 			field.getFieldAttachment().add(attachment);
 			
 			edit.getAttachments().add(attachment);
 			
 			session.save(attachment);
 			session.save(field);
			
			printf("Wrote attachment %s for assessment %s (%s)", attachment.getName(), assessment.getId(), assessment.getInternalId());
 		}
 		
 		if (!nodes.isEmpty())
 			commitAndStartTransaction();
 	}
 
 }
