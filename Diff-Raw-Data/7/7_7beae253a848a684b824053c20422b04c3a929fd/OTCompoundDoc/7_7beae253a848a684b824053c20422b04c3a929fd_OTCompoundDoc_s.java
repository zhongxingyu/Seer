 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Aug 19, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk.view.document;
 
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.otrunk.OTXMLString;
 import org.concord.otrunk.view.OTFolderObject;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class OTCompoundDoc extends OTFolderObject
 	implements OTDocument
 {
 	public static interface ResourceSchema extends OTFolderObject.ResourceSchema {
 		public OTXMLString getBodyText();
 		public void setBodyText(OTXMLString text);
 		
 		public static boolean DEFAULT_input = true;
 		public boolean getInput();
 		public void setInput(boolean flag);
 
 		public OTObjectList getDocumentRefs();
 		public void setDocumentRefs(OTObjectList list);
 		
 		public String getMarkupLanguage();
 		public void setMarkupLanguage(String lang);
 		
 		//This property is to specify the list of objects that can be inserted
 		//into the document using its edit view
 		//XXX: This is TEMPORARY just to demonstrate object insertion
 		//It will be replaced with a vew config parameter or view services 
 		public OTObjectList getObjectsToInsert();
 		public void setObjectsToInsert(OTObjectList list);
 	}
 
 	private ResourceSchema resources;
 
 	public OTCompoundDoc(ResourceSchema resources) 
 	{
 		super(resources);
 		this.resources = resources;		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.PortfolioObject#getBodyText()
 	 */
 	public String getBodyText() 
 	{	  
 	    OTXMLString xmlString = resources.getBodyText();
 	    if(xmlString ==  null) {
 	        return null;
 	    }
 	    
 	    return xmlString.getContent();
 	}
 
 	public boolean getInput()
 	{
 		// How do we set default values for primitives...
 		return resources.getInput();
 	}
 	
 	public void setInput(boolean flag)
 	{
 		resources.setInput(flag);
 	}
 	
 	public void addDocumentReference(OTObject pfObject)
 	{
 		OTObjectList embedded = resources.getDocumentRefs();
 		embedded.add(pfObject);
 	}
 
 	public void addDocumentReference(OTID embeddedId)
 	{
 		OTObjectList embedded = resources.getDocumentRefs();
 		embedded.add(embeddedId);
 	}
 	
 	/**
 	 * Returns the list of objects that are actually referenced in the body text.
 	 * Not to be confused with getDocumentRefs() in the resource schema.
 	 * 
 	 * If it finds a referenced object that was not already specified in the DocumentRefs, 
 	 * it adds it to the list.
 	 * 
 	 * TODO: This method should be renamed to getReferencedObjects() or something like that
 	 * 
 	 * @return
 	 */
 	public Vector getDocumentRefs() {
 		String bodyText = getBodyText();
 		
 		Pattern p = Pattern.compile("<object refid=\"([^\"]*)\"[^>]*>");
 		Matcher m = p.matcher(bodyText);
 		while (m.find()) {
 			String idStr = m.group(1);
 			OTID id = getReferencedId(idStr);
 			OTObject obj = getReferencedObject(id);
 			if(obj != null) addDocumentReference(obj);
 		}
 
 		p = Pattern.compile("<a href=\"([^\"]*)\"[^>]*>");
 		m = p.matcher(bodyText);
 		while (m.find()) {
 			String idStr = m.group(1);
 			if(!(idStr.startsWith("http:") || idStr.startsWith("file:")
 					|| idStr.startsWith("https:"))) {
 				OTID id = getReferencedId(idStr);
 				OTObject obj = getReferencedObject(id);
 				if(obj != null) addDocumentReference(obj);
 			}
 		}
 		
 		return resources.getDocumentRefs().getVector();
 	}
 	
 	public void removeAllDocumentReference()
 	{
 		OTObjectList embedded = resources.getDocumentRefs();
 		embedded.removeAll();
 	}
 		
 	public String getDocumentText()
 	{
 		return getBodyText();
 	}
 	
 	public void setDocumentText(String text)
 	{
 		setBodyText(text);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.objects.PfTextObject#setBodyText(java.lang.String)
 	 */
 	public void setBodyText(String bodyText)
 	{
 	    OTXMLString xmlString = new OTXMLString(bodyText);
 		resources.setBodyText(xmlString);
 		notifyOTChange();
 
 		removeAllDocumentReference();
 		
 		// pattern to match the whole body of a resource file.
 		// this should match the object tag not the format of the 
		// reference.
		Pattern p = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})");
 		Matcher m = p.matcher(bodyText);
 		while (m.find()) {
 			String idStr = m.group(1);
 			OTID id = getReferencedId(idStr);
 			addDocumentReference(id);
 		}				
 	}
 	
 	public void setMarkupLanguage(String lang)
 	{
 		resources.setMarkupLanguage(lang);
 	}
 	
 	public String getMarkupLanguage()
 	{
 		return resources.getMarkupLanguage();
 	}
 	
 	public OTObjectList getObjectsToInsert()
 	{
 		return resources.getObjectsToInsert();
 	}
 }
