 package org.iucn.sis.server.schemas.usetrade.docs;
 
 import java.io.IOException;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.w3c.dom.Document;
 
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.vfs.VFSPath;
 import com.solertium.vfs.VFSPathToken;
 
 public class DocumentLoader {
 	
 	private static final VFSPath FIELDS_DIR = 
 		new VFSPath("/browse/docs/fields/org.iucn.sis.server.schemas.usetrade");
 	
 	public static Document getView() {
 		final VFSPathToken token = new VFSPathToken("views.xml");
 		if (SIS.get().getVFS().exists(FIELDS_DIR.child(token))) {
 			try {
				return SIS.get().getVFS().getMutableDocument(FIELDS_DIR.child(token));
 			} catch (IOException e) {
 				Debug.println("View reported existence, but could not be loaded:\n{0}", e);
 			}
 		}
 		
 		return BaseDocumentUtils.impl.getInputStreamFile(
 			DocumentLoader.class.getResourceAsStream("views.xml")
 		);
 	}
 	
 	public static Document getField(String fieldName) {
 		final VFSPathToken token = new VFSPathToken(fieldName + ".xml");
 		if (SIS.get().getVFS().exists(FIELDS_DIR.child(token))) {
 			try {
				return SIS.get().getVFS().getMutableDocument(FIELDS_DIR.child(token));
 			} catch (IOException e) {
 				Debug.println("Field {0} reported existence, but could not be loaded:\n{1}", fieldName, e);
 			}
 		}
 		
 		return BaseDocumentUtils.impl.getInputStreamFile(
 			DocumentLoader.class.getResourceAsStream(fieldName + ".xml")
 		);
 	}
 
 }
