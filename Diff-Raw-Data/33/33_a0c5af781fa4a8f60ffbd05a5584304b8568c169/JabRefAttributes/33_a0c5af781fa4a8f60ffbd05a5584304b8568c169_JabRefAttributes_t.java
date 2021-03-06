 package org.docear.plugin.bibtex;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.ws.rs.core.UriBuilder;
 
 import net.sf.jabref.BibtexEntry;
 import net.sf.jabref.Globals;
 import net.sf.jabref.labelPattern.LabelPatternUtil;
 
 import org.docear.plugin.core.CoreConfiguration;
 import org.docear.plugin.pdfutilities.util.NodeUtils;
 import org.freeplane.core.util.LogUtils;
 import org.freeplane.core.util.TextUtils;
 import org.freeplane.features.attribute.Attribute;
 import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
 import org.freeplane.features.attribute.NodeAttributeTableModel;
 import org.freeplane.features.link.LinkController;
 import org.freeplane.features.link.NodeLinks;
 import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
 import org.freeplane.features.map.NodeModel;
 import org.freeplane.features.mode.Controller;
 import org.freeplane.plugin.workspace.WorkspaceUtils;
 
 public class JabRefAttributes {
 
 	private HashMap<String, String> valueAttributes = new HashMap<String, String>();
 	private String keyAttribute;
 
 	public JabRefAttributes() {		
 		registerAttributes();
 	}
 	
 	public void registerAttributes() {
 		this.keyAttribute = TextUtils.getText("bibtex_key");
 		
 		this.valueAttributes.put(TextUtils.getText("jabref_author"), "author");
 		this.valueAttributes.put(TextUtils.getText("jabref_title"), "title");
 		this.valueAttributes.put(TextUtils.getText("jabref_year"), "year");
 		this.valueAttributes.put(TextUtils.getText("jabref_journal"), "journal");
 	}
 	
 	public String getKeyAttribute() {
 		return keyAttribute;
 	}
 	
 	public HashMap<String, String> getValueAttributes() {
 		return valueAttributes;
 	}
 	
 	
 	public boolean isReferencing(BibtexEntry entry, NodeModel node) {
 		boolean found = false;
 		
 		NodeAttributeTableModel attributeTable = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
 		for (Attribute attribute : attributeTable.getAttributes()) {
 			if (attribute.getName().equals(this.keyAttribute) && attribute.getValue().equals(entry.getCiteKey())) {
 				found = true;
 			}
 		}
 		
 		return found;
 	}
 	
 	public void setReferenceToNode(BibtexEntry entry) {
 		NodeModel target = Controller.getCurrentModeController().getMapController().getSelectedNode();
 		setReferenceToNode(entry, target);
 	}
 	
 	public void removeReferenceFromNode(BibtexEntry entry, NodeModel target) {
 		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
 		for (String attributeKey : attributes.getAttributeKeyList()) {
			if (this.valueAttributes.containsKey(attributeKey) || this.keyAttribute.equals(attributeKey)) {				
				AttributeController.getController().performRemoveRow(attributes, attributes.getAttributePosition(attributeKey));
 			}
 		}
 	}
 
 	public void setReferenceToNode(BibtexEntry entry, NodeModel target) {
 		if (entry.getCiteKey()==null) {
 			LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), ReferencesController.getController().getJabrefWrapper().getDatabase(), entry);						
 		}		
 		
 		removeReferenceFromNode(entry, target);
 		
 		for (Entry<String, String> e : this.valueAttributes.entrySet()) {
 			NodeUtils.setAttributeValue(target, e.getKey(), entry.getField(e.getValue()), false);
 		}
 
 		NodeUtils.setAttributeValue(target, keyAttribute, entry.getCiteKey(), false);
 		
 		NodeLinks nodeLinks = NodeLinks.getLinkExtension(target);
 		if (nodeLinks != null) {
 			System.out.println("debug remove hyperlink");
 			nodeLinks.setHyperLink(null);
 		}
 
 		String files = entry.getField("file");
 		System.out.println("debug path: "+files);
 		
 		
 		if (files != null && files.length() > 0) {			
 			String[] paths = files.split("(?<!\\\\);"); // taken from splmm, could not test it
             for(String path : paths){
             	URI uri = parsePath(entry, path);
             	if(uri != null){
             		NodeUtils.setLinkFrom(uri, target);
             		break;
             	}
             }		
 		}
 		else {
 			String url = entry.getField("url");			
 			if (url != null && url.length() > 0) {
 				URI link;			
 				try {
 					link = LinkController.createURI(url.trim());
 					final MLinkController linkController = (MLinkController) MLinkController.getController();
 					linkController.setLink(target, link, LinkController.LINK_ABSOLUTE);
 				}
 				catch (URISyntaxException e) {				
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 
 	private URI parsePath(BibtexEntry entry, String path) {		
 		path = extractPath(path);
 		if(path == null){
 			LogUtils.warn("Could not extract path from: "+ entry.getCiteKey());
 			return null; 
 		}		
 		path = removeEscapingCharacter(path);
 		if(isAbsolutePath(path)){
 			if(new File(path).exists()){
 				return new File(path).toURI();
 			}
 		}
 		else{
 			try {
 				URI uri = new URI("property:/" + CoreConfiguration.BIBTEX_PATH);
 				URI absUri = WorkspaceUtils.absoluteURI(uri);
 				
 				System.out.println(UriBuilder.fromPath(path).build());
 				URI pdfUri = absUri.resolve(UriBuilder.fromPath(path).build());
 				if(new File(pdfUri.normalize()) != null && new File(pdfUri.normalize()).exists()){
 					return pdfUri;
 				}
 			} catch (URISyntaxException e) {
 				LogUtils.warn(e);
 				return null;
 			}
 		}		
 		return null;
 	}
 	
 	private static boolean isAbsolutePath(String path) {
 		return path.matches("^/.*") || path.matches("^[a-zA-Z]:.*");		
 	}
 
 	private static String removeEscapingCharacter(String string) {
 		return string.replaceAll("([^\\\\]{1,1})[\\\\]{1}", "$1");	
 	}
 
 	private static String extractPath(String path) {
 		String[] array = path.split("(^:|(?<=[^\\\\]):)"); // splits the string at non escaped double points
 		if(array.length >= 3){
 			return array[1];
 		}
 		return null;
 	}
 
 }
