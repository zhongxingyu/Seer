 // Copyright (c) 2009 The Chromium Authors. All rights reserved.
 // Use of this source code is governed by a BSD-style license that can be
 // found in the LICENSE file.
 
 package org.chromium.debug.ui.editors;
 
 import org.eclipse.ui.editors.text.TextEditor;
 
 /**
  * A simplistic Javascript editor which supports its own key binding scope.
  */
 public class JsEditor extends TextEditor {
 
   /** The ID of this editor as defined in plugin.xml */
   public static final String EDITOR_ID =
       "org.chromium.debug.ui.editors.JsEditor"; //$NON-NLS-1$
 
   /** The ID of the editor context menu */
   public static final String EDITOR_CONTEXT = EDITOR_ID + ".context"; //$NON-NLS-1$
 
   /** The ID of the editor ruler context menu */
  public static final String RULER_CONTEXT = EDITOR_CONTEXT + ".ruler"; //$NON-NLS-1$
 
   protected void initializeEditor() {
     super.initializeEditor();
     setEditorContextMenuId(EDITOR_CONTEXT);
     setRulerContextMenuId(RULER_CONTEXT);
   }
 
   public JsEditor() {
     super();
     setSourceViewerConfiguration(new JsSourceViewerConfiguration());
     setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", //$NON-NLS-1$
         "org.chromium.debug.ui.editors.JsEditor.context" }); //$NON-NLS-1$
   }
 
 }
