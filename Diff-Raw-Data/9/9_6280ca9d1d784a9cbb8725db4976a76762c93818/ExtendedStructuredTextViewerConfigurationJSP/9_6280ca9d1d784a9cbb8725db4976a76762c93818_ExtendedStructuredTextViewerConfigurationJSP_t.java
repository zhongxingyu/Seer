 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jst.jsp.core.text.IJSPPartitions;
 import org.eclipse.jst.jsp.ui.StructuredTextViewerConfigurationJSP;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.wst.html.core.text.IHTMLPartitions;
 import org.eclipse.wst.xml.core.text.IXMLPartitions;
 import org.osgi.framework.Bundle;
 
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.text.xml.contentassist.ContentAssistProcessorBuilder;
 import org.jboss.tools.common.text.xml.contentassist.ContentAssistProcessorDefinition;
 import org.jboss.tools.jst.jsp.contentassist.RedHatHtmlContentAssistProcessor;
 import org.jboss.tools.jst.jsp.contentassist.RedHatJSPContentAssistProcessor;
 
 /**
  * @author Igels
  */
 public class ExtendedStructuredTextViewerConfigurationJSP extends StructuredTextViewerConfigurationJSP {
 
 	public ExtendedStructuredTextViewerConfigurationJSP() {
 		super();
 	}
 
 	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
 		// if we have our own processors we need 
 		// to define them in plugin.xml file of their
 		// plugins using extention point 
 		// "org.jboss.tools.common.text.xml.contentAssistProcessor"
 		
 		ContentAssistProcessorDefinition[] defs = ContentAssistProcessorBuilder.getInstance().getContentAssistProcessorDefinitions(partitionType);
 
 		if(defs==null) return null;
 
 		List processors = new ArrayList();
 		for(int i=0; i<defs.length; i++) {
 		    IContentAssistProcessor processor = defs[i].createContentAssistProcessor();
 		    if(!processors.contains(processor)) {
 			    processors.add(processor);			        
 		    }
 		}
 
 		if ((partitionType == IXMLPartitions.XML_DEFAULT) || 
 				(partitionType == IHTMLPartitions.HTML_DEFAULT) || 
 				(partitionType == IJSPPartitions.JSP_DEFAULT) || 
 				(partitionType == IJSPPartitions.JSP_DIRECTIVE) || 
 				(partitionType == IJSPPartitions.JSP_CONTENT_DELIMITER) ||
 				(partitionType == IJSPPartitions.JSP_DEFAULT_EL) ||
 				(partitionType == IJSPPartitions.JSP_DEFAULT_EL2)) {
 			processors.add(new RedHatJSPContentAssistProcessor());
 			return (IContentAssistProcessor[])processors.toArray(new IContentAssistProcessor[0]);
 		}
		IContentAssistProcessor[] superProcessors = super.getContentAssistProcessors(sourceViewer, partitionType);
 
		if (superProcessors != null && superProcessors.length > 0) {
			for (int i = 0; i < superProcessors.length; i++)
				processors.add(superProcessors[i]);
		}
		
		return (IContentAssistProcessor[])processors.toArray(new IContentAssistProcessor[0]);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
 	 * @since 3.1
 	 */
 	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
 		if (fPreferenceStore == null)
 			return null;
 		if (sourceViewer == null || !fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
 			return null;
 
 		List allDetectors = new ArrayList(0);
 
 		IHyperlinkDetector extHyperlinkDetector = getTextEditorsExtensionsHyperlinkDetector(); 
 
 		if (extHyperlinkDetector != null) allDetectors.add(extHyperlinkDetector);
 
 /*		IHyperlinkDetector[] superDetectors = super.getHyperlinkDetectors(sourceViewer);
 		for (int m = 0; m < superDetectors.length; m++) {
 			IHyperlinkDetector detector = superDetectors[m];
 			if (!allDetectors.contains(detector)) {
 				allDetectors.add(detector);
 			}
 		}
 */
 		return (IHyperlinkDetector[]) allDetectors.toArray(new IHyperlinkDetector[0]);
 	}
 
 	private IHyperlinkDetector getTextEditorsExtensionsHyperlinkDetector() {
 		Plugin plugin = Platform.getPlugin("org.jboss.tools.common.text.ext");
 		return (plugin != null && plugin instanceof IAdaptable ? (IHyperlinkDetector)((IAdaptable)plugin).getAdapter(IHyperlinkDetector.class):null);
 	}
 
 	private IHyperlinkDetector getTextEditorsExtensionsHyperlinkDetector1() {
 		IHyperlinkDetector result = null;
 		final Object[] bundleActivationResult = new Object[] { Boolean.FALSE };
 		final Bundle bundle = Platform.getBundle("org.jboss.tools.common.text.ext");
 		if (bundle != null && bundle.getState() == org.osgi.framework.Bundle.ACTIVE) {
 			bundleActivationResult[0] = Boolean.TRUE;
 		} else {
 			BusyIndicator.showWhile(null, new Runnable() {
 				public void run() {
 					bundleActivationResult[0] = Boolean.TRUE;
 				}
 			});
 		}
 
 		if (Boolean.TRUE.equals(bundleActivationResult[0])) {
 			try {
 				Dictionary headers = bundle.getHeaders();
 				String pluginClass = (String)headers.get("Plugin-Class");
 				Class plugin = bundle.loadClass(pluginClass);
 				
 				Object obj = plugin.newInstance();
 				if (obj instanceof IAdaptable) {
 					result = (IHyperlinkDetector)((IAdaptable)obj).getAdapter(IHyperlinkDetector.class);
 				}
 			} catch (Exception x) {
 				JspEditorPlugin.getPluginLog().logError("Error in loading hyperlink detector", x);
 			}
 		}
 		return result;
 	}
 }
