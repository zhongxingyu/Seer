 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.palette.impl;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.metadata.Model;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.IImageDescriptorProvider;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataSourceModelProvider;
 import org.eclipse.jst.jsf.common.metadata.internal.TraitValueHelper;
 import org.eclipse.jst.jsf.common.metadata.query.ITaglibDomainMetaDataModelContext;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.jsf.common.ui.JSFUICommonPlugin;
 import org.eclipse.jst.jsf.common.ui.internal.utils.JSFSharedImages;
 import org.eclipse.jst.jsf.tagdisplay.internal.paletteinfos.PaletteInfo;
 import org.eclipse.jst.jsf.tagdisplay.internal.paletteinfos.PaletteInfos;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDDocument;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDElementDeclaration;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemManager;
 import org.eclipse.jst.pagedesigner.editors.palette.TagToolPaletteEntry;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDocument;
 import org.eclipse.wst.html.core.internal.contentmodel.JSPCMDocument;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
 import org.eclipse.wst.xml.core.internal.provisional.contentmodel.CMDocType;
 
 /**
  * Helper class.
  * 
  * @author mengbo
  */
 public class PaletteHelper {
 	
     // pattern to strip all <x> and </x> HTML tags
     final private static Pattern removeHTMLTags = Pattern.compile("<[/?\\w\\s=\"\\.\\#]+>");
     
     // pattern to find all runs of spaces longer than one
     final private static Pattern trimInteriorWhitespace = Pattern.compile("[ ]+");
     
     // pattern to find all new lines for removal
     final private static Pattern removeNewLines = Pattern.compile("[\n]");
     
 	private final static ImageDescriptor DEFAULT_SMALL_ICON = JSFUICommonPlugin
 		.getDefault().getImageDescriptor(
 			JSFSharedImages.DEFAULT_PALETTE_TAG_IMG);
 
 	private final static ImageDescriptor DEFAULT_LARGE_ICON = PDPlugin
 		.getDefault().getImageDescriptor(
 				"palette/GENERIC/large/PD_Palette_Default.gif");
 
 
 
 // how many characters to truncate a palette item's description to.
 // TODO: add preference?
 // the soft length is the ideal length we try to truncate to. We first
 // try to find a period (end of sentence; TODO: should have a character class)
 // inside the first SOFT_LENGTH chars at which to truncate a description string.
 // if we can't find one then we search for the first one between SOFT_LENGTH
 // and min(HARD_LENGTH, str.length()).  If found, we truncate there.  If not,
 // we truncate to HARD_LENGTH-" ...".length() and append the ellipsis.
 // In all cases the truncated description string returned should <= HARD_LENGTH.
 //	private final static int  DESCRIPTION_TRUNCATE_SOFT_LENGTH = 150;
 	private final static int  DESCRIPTION_TRUNCATE_HARD_LENGTH = 250;
 	
 	
 	/**
 	 * Creates a TaglibPaletteDrawer with TagTool palette entries for each tag from the CMDocument
 	 * @param manager
 	 * @param project
 	 * @param doc
 	 * @return TaglibPaletteDrawer
 	 */
 	public static TaglibPaletteDrawer configPaletteItemsByTLD(IPaletteItemManager manager, IProject project,
 			CMDocument doc) {
 		//bit of a hack... could be greatly improved		
 		String tldURI = null;
 		if (doc instanceof TLDDocument){
 			tldURI = ((TLDDocument)doc).getUri();
 		}
 		else if (doc instanceof HTMLCMDocument){
 			tldURI = CMDocType.HTML_DOC_TYPE;
 		}
 		else if (doc instanceof JSPCMDocument){
 			tldURI = CMDocType.JSP11_DOC_TYPE;
 		}
 		
 		if (tldURI == null) 
 			return null;
 			
 		TaglibPaletteDrawer category = findCategory(manager, tldURI);
 		if (category != null) 
 			return category;
 		
 		ITaglibDomainMetaDataModelContext modelContext = TaglibDomainMetaDataQueryHelper.createMetaDataModelContext(project, tldURI);
 		Model model = TaglibDomainMetaDataQueryHelper.getModel(modelContext);
 		category = createTaglibPaletteDrawer(manager, doc, model);
 		
 		if (category != null){		
 			loadTags(category, doc, model);		
 			sortTags(category.getChildren());
 		}
 		return category;
 	}
 
 	private static void sortTags(List tags) {
 		//note that once we store ordering customizations, we will need to do something different
 		//it will also be complicated if we decide to do 181958 and 181866
 		Collections.sort(tags, new Comparator(){
 
 			public int compare(Object o1, Object o2) {
 				String label1 = ((PaletteEntry)o1).getLabel();
 				String label2 = ((PaletteEntry)o2).getLabel();
 				
 				if (label1 == null)
 				{
 				    // if both null, then equal
 				    if (label2 == null)
 				    {
 				        return 0;
 				    }
 				    // otherwise, sort label 2 before
 			        return 1;
 				}
 				
 				
 				if (label2 == null)
 				{
 				    // if both null then equal
 				    if (label1 == null)
 				    {
 				        return 0;
 				    }
 				    // if label1 not null, then sort it first
 				    return -1;
 				}
 				return label1.compareTo(label2);
 			}
 			
 		});
 		
 	}
 
 	private static void loadTags(TaglibPaletteDrawer category,
 			CMDocument doc,Model model) {
 		
 		if (model != null) {//load from metadata - should always drop in here
 			Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(model, "paletteInfos");
 			if (trait != null){
 				PaletteInfos tags = (PaletteInfos)trait.getValue();
 				for (Iterator it=tags.getInfos().iterator();it.hasNext();){
 					PaletteInfo tag = (PaletteInfo)it.next();
 					createTagEntry(category, tag);
 				}
 			} else {
 				for (Iterator it=model.getChildEntities().iterator();it.hasNext();){
 					Entity tagAsEntity = (Entity)it.next();
 					createTagEntry(category, tagAsEntity);
 				}
 			}
 		}
 		else {//fail safe loading from cmDoc... should no longer go in here 
 			loadFromCMDocument(category, doc);
 		}
 		
 	}
 
 	private static TaglibPaletteDrawer createTaglibPaletteDrawer(IPaletteItemManager manager,
 			CMDocument doc, Model model) {
 		
 		TaglibPaletteDrawer	 category = null;
 		if (model != null){
 			//do we create it?
 			boolean isHidden = getBooleanTagTraitValue(model, "hidden", false);			
 			if (isHidden){
 				return null;
 			}
 						
 			String label = getStringTagTraitValue(model, "display-label", model.getId());
 			label = label.equals("") ? model.getId() : label;
 			category = manager.createTaglibPaletteDrawer(model.getId(), label);
 			
 			String desc = getStringTagTraitValue(model, "description", model.getId());
 			category.setDescription(formatDescription(desc));
 			
 			ImageDescriptor largeIconImage = getImageDescriptorFromTagTraitValueAsString(model, "small-icon", null);
 			if (largeIconImage != null)
 				category.setLargeIcon(largeIconImage);			
 			
 			String prefix = getStringTagTraitValue(model, "default-prefix", null);
 			category.setDefaultPrefix(prefix);
 			
 			boolean isVisible = !(getBooleanTagTraitValue(model, "expert", false));
 			category.setVisible(isVisible);
 			
 			category.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
 		
 		}
 		return category;
 	}
 
 	private static TaglibPaletteDrawer findCategory(IPaletteItemManager manager,
 			String tldURI) {
 		TaglibPaletteDrawer lib = null;
 		for (Iterator it = manager.getAllCategories().iterator();it.hasNext();){
 			lib = (TaglibPaletteDrawer)it.next();
 			if (tldURI.equals(lib.getURI()))
 				return lib;					
 		}
 		return null;
 	}
 
 	/* (non-JavaDoc)
 	 * This method will read information from the CMDocument to create the tag entries. It will
 	 * check the existing items in the registry. If the corresponding tag is not
 	 * in palette manager, then it will create one, and mark the newly created
 	 * item as "expert". Otherwise, it will check whether the tld contains more
 	 * information than the palette manager, and adding those information to it
 	 * (such as description, icons for tags)
 	 * 
 	 * @param category 
 	 * @param cmdoc
 	 */
 	private static void loadFromCMDocument(TaglibPaletteDrawer category,
 			CMDocument cmdoc) {
 		
 		CMNamedNodeMap nodeMap = cmdoc.getElements();
 		for (int i = 0, size = nodeMap.getLength(); i < size; i++) {
 			CMElementDeclaration eledecl = (CMElementDeclaration) nodeMap
 					.item(i);
 			String tagName = eledecl.getElementName();
 			TagToolPaletteEntry item;
 			if (tagName.equalsIgnoreCase(IHTMLConstants.TAG_INPUT)) {//TODO:  fix this nonsense!
 				StringBuffer name = new StringBuffer(category.getURI());
 				name.append(":").append(tagName).append(":").append(tagName);
 				item = category.getTagPaletteEntryById(name.toString());
 			} else {
 				item = category.getTagPaletteEntryByTagName(tagName);
 			}
 			if (item == null) {
 				createTagEntry(category, eledecl);
 
 			}
 		}
 	}
 	
 	private static void createTagEntry(TaglibPaletteDrawer category,
 			PaletteInfo info) {
 		
 		Boolean hidden = info.getHidden();
 		if ((hidden != null) && (hidden.booleanValue()))//do not create a palette entry
 			return; 
 		
 		IMetaDataSourceModelProvider sourceProvider = ((Trait)info.eContainer().eContainer()).getSourceModelProvider();
 		String tagName = info.getTag();
 		String id = info.getId();		
 		String label = info.getDisplayLabel();
 		String desc = formatDescription(info.getDescription());		
 		ImageDescriptor smallIcon = getImageDescriptorFromString(sourceProvider, info.getSmallIcon(), DEFAULT_SMALL_ICON);
 		ImageDescriptor largeIcon = getImageDescriptorFromString(sourceProvider, info.getLargeIcon(), DEFAULT_LARGE_ICON);
 		Boolean expert = info.getExpert();
 				
 		internalCreateTagEntry(category, id, tagName, label, desc, smallIcon, largeIcon, (expert !=null && expert.booleanValue()));		
 		
 	}
 
 	private static void createTagEntry(TaglibPaletteDrawer category,
 			Entity entity) {
 		
 		boolean hidden = getBooleanTagTraitValue(entity, "hidden", false);
 		if (hidden)//do not create a palette entry
 			return; 
 		
 		String tagName = entity.getId();
 		String label = getStringTagTraitValue(entity, "display-label", tagName);
 		String desc = formatDescription(getStringTagTraitValue(entity, "description", tagName));		
 		ImageDescriptor smallIcon = getImageDescriptorFromTagTraitValueAsString(entity, "small-icon", DEFAULT_SMALL_ICON);
 		ImageDescriptor largeIcon = getImageDescriptorFromTagTraitValueAsString(entity, "large-icon", DEFAULT_LARGE_ICON);
 		boolean expert = getBooleanTagTraitValue(entity, "expert", false);
 				
 		internalCreateTagEntry(category, tagName, tagName, label, desc, smallIcon, largeIcon, expert);
 		
 	}
 
 	private static TagToolPaletteEntry internalCreateTagEntry(TaglibPaletteDrawer category, String id, String tagName, String label, String desc, ImageDescriptor smallIcon, ImageDescriptor largeIcon, boolean expert){
 		TagToolPaletteEntry item = new TagToolPaletteEntry(tagName, label, desc, smallIcon, largeIcon);
 		item.setId(id);
 		
 		item.setVisible(!expert);
 		category.getChildren().add(item);
 		item.setParent(category);
 		
 		return item;
 	}
 
 	private static boolean getBooleanTagTraitValue(Entity entity,
 			String key, boolean defaultValue) {
 		Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(entity, key);
 		if (trait != null){
 			return TraitValueHelper.getValueAsBoolean(trait);
 		}
 		return defaultValue;	
 	}
 
 	private static String getStringTagTraitValue(Entity entity, String key, String defaultValue){
 		Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(entity, key);
 		if (trait != null){
			return TraitValueHelper.getValueAsString(trait);
 		}
 		return defaultValue;		
 	}
 
 	private static ImageDescriptor getImageDescriptorFromTagTraitValueAsString(Entity entity, String key, ImageDescriptor defaultValue){
 		Trait t = TaglibDomainMetaDataQueryHelper.getTrait(entity, key);
 		if (t != null){
 			String imgDesc = TraitValueHelper.getValueAsString(t);
 			return getImageDescriptorFromString(t.getSourceModelProvider(), imgDesc, defaultValue);
 		}
 		return defaultValue;
 	}
 	
 	private static ImageDescriptor getImageDescriptorFromString(IMetaDataSourceModelProvider sourceModelProvider,  String imgDesc, ImageDescriptor defaultValue){
 		ImageDescriptor image = defaultValue;
 		IImageDescriptorProvider imageProvider = (IImageDescriptorProvider)sourceModelProvider.getAdapter(IImageDescriptorProvider.class);			
 		if (imageProvider != null){
 			image = imageProvider.getImageDescriptor(imgDesc);
 		}
 		return image;
 	}
 	
 	private static void createTagEntry(TaglibPaletteDrawer category,
 			CMElementDeclaration eledecl) {
 		
 		String tagName = eledecl.getElementName();
 		String label = null;
 		String desc = null;
 
 		if (eledecl instanceof TLDElementDeclaration){
 			TLDElementDeclaration tag = (TLDElementDeclaration)eledecl;			
 			label = tag.getDisplayName();			
 			desc = tag.getDescription();						
 		}
 		
 		if (label == null || label.equals(""))
 			label = tagName;
 		
 		if (desc == null )
 			desc = "";
 		else
 			desc = formatDescription(desc);
 		
 		TagToolPaletteEntry item = internalCreateTagEntry(category, tagName, tagName, label, desc, getDefaultSmallIcon(), getDefaultLargeIcon(), false);
 		item.setToolProperty("CMElementDeclaration", eledecl);
 		
 	}
 	
 	/**
 	 * @return DEFAULT_LARGE_ICON
 	 */
 	private static ImageDescriptor getDefaultLargeIcon() {
 		return DEFAULT_LARGE_ICON;
 	}
 
 	/**
 	 * @return DEFAULT_SMALL_ICON
 	 */
 	private static ImageDescriptor getDefaultSmallIcon() {
 		return DEFAULT_SMALL_ICON;
 	}
 	
 	private static String formatDescription(final String desc) {
 		//TODO: modify and use a formatter in the future?
 		String aDesc = filterConvertString(desc);
 		if (aDesc != null){
 			if (aDesc.length() > DESCRIPTION_TRUNCATE_HARD_LENGTH) {
 				StringBuffer result = new StringBuffer(aDesc.substring(0, DESCRIPTION_TRUNCATE_HARD_LENGTH));
 				result.append("...");
 				return result.toString();
 			}
 			return aDesc;
 
 		}
 		return "";
 	}
 	
 	private static String filterConvertString(String text) {
 		if (text == null) {
 			return "";
 		}
          
 		String result = removeHTMLTags.matcher(text).replaceAll("");
 		result = removeNewLines.matcher(result).replaceAll(" ");
         result = trimInteriorWhitespace.matcher(result).replaceAll(" ");        
 
 		return result;
 	}
 }
