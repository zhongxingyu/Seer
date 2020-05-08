 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: CaesarEditor.java,v 1.27 2005-11-17 16:58:23 gasiunas Exp $
  */
 
 package org.caesarj.ui.editor;
 
 import org.apache.log4j.Logger;
 import org.caesarj.ui.CaesarPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 /**
  * CaesarEditor
  * 
  * TODO [feature] solve the problem with annotations, at the moment user has to
  * disable "Analyse Annotations while typing" TODO [feature] completer for
  * caesar keywords TODO [feature] add new file type (e.g.: .cj) in order to
  * avoid conflicts with coexisting pure java projects TODO [feature] add new
  * Wizardtype for Caesar Files
  * 
  * @author Ivica Aracic <ivica.aracic@bytelords.de>
  */
 
 public class CaesarEditor extends CompilationUnitEditor {
 
 	/**
 	 * @author Jochen
 	 * 
 	 * To change the template for this generated type comment go to
 	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 	 */
 
 	private static Logger log = Logger.getLogger(CaesarEditor.class);
 
 	private CaesarJContentOutlinePage outlineView;
 
 	//private CompositeRuler caesarVerticalRuler;
 
 	public CaesarEditor() {
 		super();
 		// set ruler context menu id so that the caesar-ruler actions are used.
 		setRulerContextMenuId("#CJCompilationUnitRulerContext");
 		/* initialize auto annotation on/off switching */
 		CaesarPlugin.getDefault().initPluginUI();
 	}
 	
    public void init(IEditorSite site, IEditorInput input)  throws PartInitException {
    	super.init(site, input);
    	log.debug("Initializing CaesarJ Editor."); //$NON-NLS-1$
 		try {
 			IPreferenceStore store = this.getPreferenceStore();
 			CaesarTextTools textTools = new CaesarTextTools(store);
 			JavaSourceViewerConfiguration svConfig = new CaesarSourceViewerConfiguration(
 					textTools, this, store);
 			setSourceViewerConfiguration(svConfig);
 			log.debug("CaesarJ Editor Initialized."); //$NON-NLS-1$
 		} catch (Exception e) {
 			log.error("Initalizing CaesarJ Editor.", e); //$NON-NLS-1$
 		}
    }
	
	public IJavaElement getInputJavaElement() {
		return super.getInputJavaElement();
 	}
 
 	public Object getAdapter(Class key) {
 		if (key.equals(IContentOutlinePage.class)) {
 			if (this.outlineView == null) {
 				this.outlineView = new CaesarJContentOutlinePage(this, 
 						getInputJavaElement().getResource().getProject());
 				//this.outlineView.setEnabled(true);
 			}
 			return this.outlineView;
 		}
 		return super.getAdapter(key);
 	}
 
 	public void doSetInput(IEditorInput input) throws CoreException {
 		super.doSetInput(input);
 	}
 
 	public void dispose() {
 		/* Fix TR35: must call super to free reference to the file */
 		super.dispose(); 
 		
 		log.debug("dispose"); //$NON-NLS-1$
 		if(this.outlineView != null){
 			// Enable doesn't make sense anymore. For this reason the setEnabled above
 			// was removed. Now we call the static method to remove this instance from 
 			// the list
 			//this.outlineView.setEnabled(false);
 			CaesarJContentOutlinePage.removeInstance(this.outlineView);
 		}
 	}
 }
