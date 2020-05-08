 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.preferences;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.ruby.internal.ui.RubyPreferenceConstants;
 import org.eclipse.dltk.ruby.internal.ui.editor.RubyDocumentSetupParticipant;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitions;
 import org.eclipse.dltk.ruby.ui.preferences.RubyPreferencesMessages;
 import org.eclipse.dltk.ui.preferences.AbstractScriptEditorColoringConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.IPreferenceConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
 import org.eclipse.dltk.ui.preferences.PreferencesMessages;
 import org.eclipse.dltk.ui.text.IColorManager;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class RubyEditorColoringConfigurationBlock extends
 		AbstractScriptEditorColoringConfigurationBlock implements
 		IPreferenceConfigurationBlock {
 	
 	private static final String PREVIEW_FILE_NAME = "PreviewFile.txt";
 
 	private static final String[][] fSyntaxColorListModel = new String[][] {
 			{ RubyPreferencesMessages.RubyEditorPreferencePage_rubyDoc,
 				RubyPreferenceConstants.EDITOR_DOC_COLOR, sDocumentationCategory },
 				
 			{ RubyPreferencesMessages.RubyEditorPreferencePage_rubyDocTopic,
 					RubyPreferenceConstants.EDITOR_DOC_TOPIC_COLOR, sDocumentationCategory },
 			{ PreferencesMessages.DLTKEditorPreferencePage_singleLineComment,
 				RubyPreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
 					sCommentsCategory },
 					
 			{ PreferencesMessages.DLTKEditorPreferencePage_keywords,
 				RubyPreferenceConstants.EDITOR_KEYWORD_COLOR, sCoreCategory },
                                
	                { PreferencesMessages.DLTKEditorPreferencePage_returnKeyword,
                                RubyPreferenceConstants.EDITOR_KEYWORD_RETURN_COLOR, sCoreCategory },
 
 			{ PreferencesMessages.DLTKEditorPreferencePage_strings,
 				RubyPreferenceConstants.EDITOR_STRING_COLOR, sCoreCategory },
				
 			{ PreferencesMessages.DLTKEditorPreferencePage_numbers,
 				RubyPreferenceConstants.EDITOR_NUMBER_COLOR, sCoreCategory },							
 				
 			{ RubyPreferencesMessages.RubyClassVariable,
 					RubyPreferenceConstants.EDITOR_CLASS_VARIABLE_COLOR, sCoreCategory },					
 			{ RubyPreferencesMessages.RubyInstanceVariable,
 					RubyPreferenceConstants.EDITOR_INSTANCE_VARIABLE_COLOR, sCoreCategory },					
 			{ RubyPreferencesMessages.RubyGlobalVariable,
 					RubyPreferenceConstants.EDITOR_GLOBAL_VARIABLE_COLOR, sCoreCategory },					
 			{ RubyPreferencesMessages.RubyPseudoVariable,
 					RubyPreferenceConstants.EDITOR_PSEUDO_VARIABLE_COLOR, sCoreCategory },					
 			{ RubyPreferencesMessages.RubySymbols,
 					RubyPreferenceConstants.EDITOR_SYMBOLS_COLOR, sCoreCategory },					
 					
 /*			{ PreferencesMessages.DLTKEditorPreferencePage_variables,
 								RubyPreferenceConstants.EDITOR_VARIABLE_COLOR, sCoreCategory }*/ };
 	
 
 	public RubyEditorColoringConfigurationBlock(OverlayPreferenceStore store) {
 		super(store);
 	}
 
 	protected String[][] getSyntaxColorListModel() {
 		return fSyntaxColorListModel;
 	}
 
 	protected ProjectionViewer createPreviewViewer(Composite parent,
 			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
 			boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
 		return new ScriptSourceViewer(parent, verticalRuler, overviewRuler,
 				showAnnotationsOverview, styles, store);
 	}
 
 	protected ScriptSourceViewerConfiguration createSimpleSourceViewerConfiguration(
 			IColorManager colorManager, IPreferenceStore preferenceStore,
 			ITextEditor editor, boolean configureFormatter) {
 		return new SimpleRubySourceViewerConfiguration(colorManager,
 				preferenceStore, editor, RubyPartitions.RUBY_PARTITIONING,
 				configureFormatter);
 	}
 
 	protected void setDocumentPartitioning(IDocument document) {
 		RubyDocumentSetupParticipant participant = new RubyDocumentSetupParticipant();
 		participant.setup(document);
 	}
 
 	protected String getPreviewContent() {
 		String line;
 		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
 		StringBuffer buffer = new StringBuffer(512);
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(getClass()
 					.getResourceAsStream(PREVIEW_FILE_NAME)));
 			while ((line = reader.readLine()) != null) {
 				buffer.append(line);
 				buffer.append(separator);
 			}
 		} catch (IOException io) {
 			// DLTKUIPlugin.log(io);
 			io.printStackTrace();
 			// System.err.println("io");
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return buffer.toString();
 
 	}
 }
