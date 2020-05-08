 package net.sf.colorer.eclipse;
 
 import java.net.URL;
 
 import org.eclipse.ui.plugin.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.core.resources.*;
 
 import net.sf.colorer.ParserFactory;
 import net.sf.colorer.swt.ColorManager;
 
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class EclipsecolorerPlugin extends AbstractUIPlugin {
 	//The shared instance.
 	private static EclipsecolorerPlugin plugin;
   private String catalogPath;
   private ParserFactory parserFactory;
   private ColorManager colorManager = new ColorManager();
 	
 	/**
 	 * The constructor.
 	 */
 	public EclipsecolorerPlugin(IPluginDescriptor descriptor) {
 		super(descriptor);
 		plugin = this;
     
     reloadParserFactory();
     
     IPreferenceStore store = getPreferenceStore();
     store.setDefault(PreferencePage.TEXT_FONT, "");
     store.setDefault(PreferencePage.SPACES_FOR_TABS, false);
     store.setDefault(PreferencePage.WORD_WRAP, false);
     store.setDefault(PreferencePage.TAB_WIDTH, 4);
     
     store.setDefault(PreferencePage.FULL_BACK, false);
     store.setDefault(PreferencePage.USE_BACK, false);
     store.setDefault(PreferencePage.VERT_CROSS, false);
     store.setDefault(PreferencePage.HORZ_CROSS, true);
     store.setDefault(PreferencePage.PAIRS_MATCH, "PAIRS_OUTLINE");
     
     store.setDefault(PreferencePage.HRD_SET, "default");   
     store.setDefault(PreferencePage.RELOAD_HRC, "xx");   
 
     store.setDefault("Outline.Hierarchy", true);
     store.setDefault("Outline.Sort", false);
     
     store.setDefault("g.Prefix", "");
     store.setDefault("g.Suffix", ".html");
     store.setDefault("g.HRDSchema", store.getString(PreferencePage.HRD_SET));
     store.setDefault("g.HtmlHeaderFooter", true);
     store.setDefault("g.InfoHeader", true);
     store.setDefault("g.UseLineNumbers", true);
     store.setDefault("g.OutputEncoding", "default");
     store.setDefault("g.TargetDirectory", "/");
     store.setDefault("g.LinkSource", "");
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static EclipsecolorerPlugin getDefault() {
 		return plugin;
 	}
 
   public ParserFactory getParserFactory(){
     return parserFactory;
   };
   public ColorManager getColorManager(){
     return colorManager;
   }
   
   public void reloadParserFactory(){
     try{
       catalogPath = Platform.resolve(new URL(getDescriptor().getInstallURL(), "colorer/catalog.xml")).toExternalForm();
      /*
      if (catalogPath.startsWith("file:/")){
        catalogPath = catalogPath.substring(6);
      };
      */
       parserFactory = new ParserFactory(catalogPath);
     }catch(Throwable e){
       boolean error = true;
       Throwable exc = e;
       try{
         parserFactory = new ParserFactory();
         error = false;
       }catch(Throwable e1){
         error = true;
         exc = e1;
       }
       if (error) MessageDialog.openError(null, Messages.getString("init.error.title"),
                                          Messages.getString("init.error.pf")+
                                          "\n" + exc.getMessage());
     };
     // informs all the editors about ParserFactory reloading
     getPreferenceStore().firePropertyChangeEvent(PreferencePage.RELOAD_HRC, "", "");
   };
   
 	/**
 	 * Returns the workspace instance.
 	 */
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 }
 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is the Colorer Library.
  *
  * The Initial Developer of the Original Code is
  * Cail Lomecb <cail@nm.ru>.
  * Portions created by the Initial Developer are Copyright (C) 1999-2003
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
