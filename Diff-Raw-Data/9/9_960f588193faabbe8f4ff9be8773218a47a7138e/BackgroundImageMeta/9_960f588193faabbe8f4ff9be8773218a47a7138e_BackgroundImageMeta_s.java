 /*******************************************************************************
  * Copyright (c) 2008 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.pagedesigner.css2.property;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.utils.DOMUtil;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.w3c.dom.Element;
 import org.w3c.dom.css.CSSValue;
 
 /**
  * Provides metadata for the "background-image" CSS property.
  * 
  * @author Ian Trimble - Oracle
  */
 public class BackgroundImageMeta extends CSSPropertyMeta {
 
 	private static final String[] _keywords = {ICSSPropertyID.VAL_NONE};
 
 	/**
 	 * Construct an instance.
 	 */
 	public BackgroundImageMeta() {
 		super(false, ICSSPropertyID.VAL_NONE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.css2.property.CSSPropertyMeta#calculateCSSValueResult(org.w3c.dom.css.CSSValue, java.lang.String, org.eclipse.jst.pagedesigner.css2.ICSSStyle)
 	 */
 	@Override
 	public Object calculateCSSValueResult(CSSValue value, String propertyName,
 			ICSSStyle style) {
 		Object ret = null;
 		String valueText = value.getCssText();
 		if (valueText != null && valueText.length() > 0) {
 			valueText = stripURLSyntax(valueText);
 			ret = getImage(valueText, null);
 		}
 		if (ret == null) {
 			ret = getInitialValue(propertyName, style);
 		}
 		return ret;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.css2.property.CSSPropertyMeta#calculateHTMLAttributeOverride(org.w3c.dom.Element, java.lang.String, java.lang.String, org.eclipse.jst.pagedesigner.css2.ICSSStyle)
 	 */
 	@Override
 	public Object calculateHTMLAttributeOverride(Element element,
 			String htmltag, String propertyName, ICSSStyle style) {
 		Image image = null;
 		if (
 				element != null &&
 				element.getNodeName() != null &&
 				element.getNodeName().equalsIgnoreCase(IHTMLConstants.TAG_BODY)) {
 			if (ICSSPropertyID.ATTR_BACKGROUND_IMAGE.equalsIgnoreCase(propertyName)) {
 				String attrValue = DOMUtil.getAttributeIgnoreCase(element, IHTMLConstants.ATTR_BACKGROUND);
 				if (attrValue != null && attrValue.trim().length() > 0) {
 					image = getImage(attrValue.trim(), element);
 				}
 			}
 		}
 		return image;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.css2.property.CSSPropertyMeta#getKeywordValues()
 	 */
 	@Override
 	protected String[] getKeywordValues() {
 		return _keywords;
 	}
 
 	private String stripURLSyntax(String input) {
 		String output = null;
 		if (input != null) {
 			//strip "url(...)"
 			int startPos = input.indexOf("url(") + 4; //$NON-NLS-1$
 			if (startPos > -1 && startPos < input.length() - 1) {
 				int endPos = input.indexOf(')', startPos);
 				if (endPos > startPos) {
 					String insideURL = input.substring(startPos, endPos).trim();
 					//strip double-quotes
 					if (insideURL.startsWith("\"") && insideURL.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
 						output = insideURL.substring(1, insideURL.length() - 1);
 					//strip single-quotes
 					} else if (insideURL.startsWith("'") && insideURL.endsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
 						output = insideURL.substring(1, insideURL.length() - 1);
 					} else {
 						output = insideURL;
 					}
 				}
 			}
 		}
 		return output != null ? output : input;
 	}
 
 	/* Image instances returned from this method should not be disposed because
 	 * they are cached in the plug-in's ImageRegistry and will be disposed of
 	 * by the registry.
 	 */
 	private Image getImage(String imagePath, Element element) {
 		Image image = null;
 		if (imagePath != null && imagePath.length() > 0) {
 			ImageRegistry registry = PDPlugin.getDefault().getImageRegistry();
 			image = registry.get(imagePath);
 			if (image == null) {
 				try {
 					URL imageURL = new URL(imagePath);
 					ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
 					image = imageDescriptor.createImage();
 					if (image != null) {
 						registry.put(imagePath, image);
 					}
 				} catch(MalformedURLException mue) {
 					//attempt to resolve as relative to document
 					if (element instanceof IDOMNode) {
 						IDOMModel model = ((IDOMNode)element).getModel();
 						if (model != null) {
 							String baseLocation = model.getBaseLocation();
 							if (baseLocation != null && baseLocation.length() > 0) {
 								IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 								if (wsRoot != null) {
 									IResource jspRes = wsRoot.findMember(baseLocation);
 									if (jspRes != null) {
 										IContainer jspFolder = jspRes.getParent();
 										if (jspFolder != null) {
 											IResource imageRes = jspFolder.findMember(imagePath);
 											if (imageRes != null) {
 												URI imageURI = imageRes.getLocationURI();
 												if (imageURI != null) {
 													try {
 														URL imageURL = imageURI.toURL();
 														ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
 														image = imageDescriptor.createImage();
 														if (image != null) {
 															registry.put(imagePath, image);
 														}
 													} catch(MalformedURLException mue2) {
 														//ignore - what else can be done?
 													}
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			} else if (image.isDisposed()) {
 				//shouldn't be able to get here from there, but...just in case
 				registry.remove(imagePath);
 				image = getImage(imagePath, element);
 			}
 		}
 		return image;
 	}
 
 }
