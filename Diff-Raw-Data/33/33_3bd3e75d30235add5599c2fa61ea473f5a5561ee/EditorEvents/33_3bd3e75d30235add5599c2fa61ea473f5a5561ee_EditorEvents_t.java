/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 * Thanks to:
 *    emil.crumhorn@gmail.com - Some of the code was coped from the 
 *    "eclipsemissingfeatrues" plugin. 
 *******************************************************************************/


 package com.chookapp.org.overviewrulerbackground.core;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.OverviewRuler;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 
 import com.chookapp.org.overviewrulerbackground.Activator;
 import com.chookapp.org.overviewrulerbackground.preferences.PreferenceConstants;
 
 public class EditorEvents implements IStartup {
 
 	@Override	
 	public void earlyStartup()
 	{
 		// hook us on an async as we need the active page
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				// hook the startup editor if any, it doesn't get notified via a normal event
 				IEditorPart startupEditorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 				if (startupEditorPart != null) {
 					activated(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
 				}
 
 				Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener() {
 
 					public void partActivated(IWorkbenchPart part) {
 						activated(part);
 					}
 
 					public void partBroughtToTop(IWorkbenchPart part) {
 						activated(part);
 					}				
 
 					public void partClosed(IWorkbenchPart part) {
 					//	deactivated(part);
 					}
 
 					public void partDeactivated(IWorkbenchPart part) {
 					//	deactivated(part);
 					}
 
 					public void partOpened(IWorkbenchPart part) {
 						activated(part);
 					}
 
 				});
 			}
 		});
 	}	
 	
 	private void activated(IWorkbenchPart part) {
 		try {
 			if( Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SYS_DEF))
 				return;
 			
 			RGB rgb = PreferenceConverter.getColor(
 							Activator.getDefault().getPreferenceStore(),
 							PreferenceConstants.P_COLOR);
 
 			IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 			SourceViewer sv = (SourceViewer) callGetSourceViewer((AbstractTextEditor) editorPart);
 			OverviewRuler ruler = (OverviewRuler) getOverviewRuler(sv);
 			ruler.getControl().setBackground(new Color(Display.getDefault(),rgb));
 		} catch (Exception e) {
 			System.err.println(e);
 		}		
 	}	
 
 	/**
 	 * Calls AbstractTextEditor.getSourceViewer() through reflection, as that method is normally protected (for some
 	 * ungodly reason).
 	 * 
 	 * @param AbstractTextEditor to run reflection on
 	 */
 	private ITextViewer callGetSourceViewer(AbstractTextEditor editor) throws Exception {
 		try {
 			Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
 			method.setAccessible(true);
 
 			return (ITextViewer) method.invoke(editor);
 		} catch (NullPointerException npe) {
 			return null;
 		}
 	}
 	
     /**
      * Gets the overview ruler off the SourceViewer by using Java Reflection. Only way as far as I can tell.
      * 
      * @param viewer SourceViewer
      * @return overview ruler or null if errors were encountered
      */
     private IOverviewRuler getOverviewRuler(SourceViewer viewer) {
         try {
             Field f = SourceViewer.class.getDeclaredField("fOverviewRuler");
             f.setAccessible(true);
             return (IOverviewRuler) f.get(viewer);
         } catch (Exception err) {
             return null;
         }
     }	
 	  
 }
