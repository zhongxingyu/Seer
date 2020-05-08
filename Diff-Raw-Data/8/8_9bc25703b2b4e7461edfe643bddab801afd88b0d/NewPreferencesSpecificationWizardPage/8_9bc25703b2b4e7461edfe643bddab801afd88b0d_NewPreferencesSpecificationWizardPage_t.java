 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.preferences.wizards;
 
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.imp.core.ErrorHandler;
 import org.eclipse.imp.preferences.PreferencesPlugin;
 import org.eclipse.imp.runtime.RuntimePlugin;
 import org.eclipse.imp.wizards.ExtensionPointEnabler;
 import org.eclipse.imp.wizards.ExtensionPointWizard;
 import org.eclipse.imp.wizards.ExtensionPointWizardPage;
 import org.eclipse.imp.wizards.WizardPageField;
 import org.eclipse.pde.core.plugin.IPluginElement;
 import org.eclipse.pde.core.plugin.IPluginExtension;
 import org.eclipse.pde.core.plugin.IPluginModel;
 import org.eclipse.pde.core.plugin.IPluginObject;
 import org.eclipse.pde.internal.core.schema.Schema;
 import org.eclipse.swt.widgets.Composite;
 import org.osgi.framework.Bundle;
 
 /**
 
  */
 public class NewPreferencesSpecificationWizardPage extends ExtensionPointWizardPage
 {
 	
 	protected String fLanguageName = null;	
 	
 	
  	 NewPreferencesSpecificationWizardPage(ExtensionPointWizard owner) {
  		 // The "false" value provided at the end of the parameters
  		 // controls whether fields for extension name and id are shown
  		 // in the wizard--the parameter is "omitIDName", so "false"
  		 // means don't omit them (and "true" means do omit them)
  		 //super(owner, RuntimePlugin.UIDE_RUNTIME, "preferencesSpecification", false);
  		 super(owner, RuntimePlugin.IMP_RUNTIME, "preferencesSpecification", true, true);
     }
 
 
  	 /**
  	  * Create the other controls, then use the language name
  	  * to set a default value for the (preferences menu) cagetory
  	  * under which the new preferences dialog should appear.
  	  */
     public void createControl(Composite parent)
     {
 		super.createControl(parent);
 
 		setLanguageIfEmpty();
 		
 		setTemplateIfEmpty();
 		
 		//setFolderIfEmpty();
 		
 		setFileNameIfEmpty();
 		
 		setPagePackageIfEmpty();
 		
 		setPageClassNameBaseIfEmpty();
 		
 		setPageNameIfEmpty();
 		
 		setPageIdIfEmpty();
 		
 		// Don't set category if empty, because the most
 		// likely (or, anyway, safest) default value is
 		// no category (i.e., a top-level item)
     }
 
     
     // Originally copied from package org.eclipse.imp...NewUIDEParserWizardPage
     // Now may duplicate method in NewLanguageSupportWizardPage
     
     public String determineLanguage()
     {
 		if (fLanguageName != null)
 			return fLanguageName;
 		
 		try {
     		// SMS 9 Oct 2007
     		IProject project = null;
         	if (fProject != null)
         		project = fProject;
         	else {
         		project = getProjectBasedOnNameField();
         	}
 
 		    IPluginModel pluginModel= ExtensionPointEnabler.getPluginModel(project);
 	
 		    if (pluginModel != null) {
 				IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
 		
 				for(int n= 0; n < extensions.length; n++) {
 				    IPluginExtension extension= extensions[n];
 		
                     if (!extension.getPoint().equals("org.eclipse.imp.runtime.languageDescription"))
                         continue;
 
                     IPluginObject[] children= extension.getChildren();
 		
 				    for(int k= 0; k < children.length; k++) {
 						IPluginObject object= children[k];
 			
 						if (object.getName().equals("language")) {
 							fLanguageName = ((IPluginElement) object).getAttribute("language").getValue();
 						    return fLanguageName;
 						}
 				    }
 				    System.err.println("NewPreferencesSpecificationWizardPage.determineLanguage():  Unable to determine language for plugin '" + pluginModel.getBundleDescription().getName() + "': no languageDescription extension.");
 				}
 		    } else if (project != null)
 		    	System.out.println("Not a plugin project: " + project.getName());
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}
 		return "";
     }
 
     	
     protected void setLanguageIfEmpty() {
         try {
             String pluginLang= determineLanguage(); // if a languageDesc exists
             if (pluginLang.length() == 0)
                 return;
 
             WizardPageField field= getField("language");
 
             if (field.getText().length() == 0)
                 field.setText(pluginLang);
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setLanguageIfEmpty():  Cannot set 'language' field", e);
         }
     }
     
     
     protected void setTemplateIfEmpty() {
         try {
             WizardPageField field= getField("template");
 
             if (field.getText().length() == 0) {
             	String templatesPath = getTemplatesPath() + "standard.pfsp";
             	templatesPath = templatesPath.replace('\\', '/');
                 field.setText(templatesPath);
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setTemplateIfEmpty():  Cannot set 'template' field", e);
         }
     }
     
     
 //    protected void setFolderIfEmpty() {
 //        try {
 //
 //            WizardPageField field= getField("folder");
 //
 //            if (field.getText().length() == 0) {
 //            	IProject project = getProject();
 //            	String text = null;
 //            	IFolder prefsFolder = project.getFolder("preferences");
 //            	if (prefsFolder.exists())
 //            		text = prefsFolder.getLocation().toString();
 //            	else
 //            		text = project.getLocation().toString();
 //            	
 //                field.setText(text);
 //            }
 //        } catch (Exception e) {
 //            ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setFolderIfEmpty():  Cannot set 'folder' field", e);
 //        }
 //    }
     
     
     protected void setFileNameIfEmpty() {
         try {
 
             WizardPageField field= getField("fileName");
 
             if (field.getText().length() == 0) {
             	String baseName = null;
                 String pluginLang= determineLanguage();
                 if (pluginLang.length() != 0)
                 	baseName = pluginLang;
                 else
                 	baseName = "preferences";
             	
                 field.setText(baseName + ".pfsp");
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setFileNameIfEmpty():  Cannot set 'fileName' field", e);
         }
     }
     
     
     protected void setPagePackageIfEmpty() {
         try {
             WizardPageField field= getField("pagePackage");
 
             if (field.getText().length() == 0) {
             	String baseName = null;
                 String pluginLang= determineLanguage();
                 if (pluginLang.length() != 0)
                     field.setText(pluginLang + ".imp.preferences");	
                 else
                     field.setText("imp.preferences");
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setPagePackageIfEmpty	():  Cannot set 'pageName' field", e);
         }
     }
     
     
     protected void setPageClassNameBaseIfEmpty() {
         try {
             WizardPageField field= getField("pageClassNameBase");
 
             if (field.getText().length() == 0) {
             	String baseName = null;
                 String pluginLang= determineLanguage(); // if a languageDesc exists
                 if (pluginLang.length() != 0)
                     field.setText(pluginLang.substring(0,1).toUpperCase() +
                     			  pluginLang.substring(1) + "Preferences");	
                 else
                     field.setText("Preferences");
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setPageClassNameBaseIfEmpty	():  Cannot set 'pageName' field", e);
         }
     }
     
     
     // SMS 4 Aug 2007:  The prefspecs language currently requires the name
     // of a prefspecs specification to be an identifier instead of an
     // arbitrary string (and for now it seems easiest to use the specification
     // name as the name of the page).  Both of these restrictions could be
     // loosened in the future.
     protected void setPageNameIfEmpty() {
         try {
             WizardPageField field= getField("pageName");
 
             if (field.getText().length() == 0) {
                 String pluginLang= determineLanguage();
                 // make sure the language has an upper case initial for this purpose
                 pluginLang = pluginLang.substring(0, 1).toUpperCase() + pluginLang.substring(1);
                 
                 if (pluginLang.length() != 0)
                    field.setText(pluginLang);	
                 else
                    field.setText(getProjectOfRecord().getName());
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setPageNameIfEmpty():  Cannot set 'pageName' field", e);
         }
     }
     
     
     protected void setPageIdIfEmpty() {
         try {
             WizardPageField field= getField("pageId");
 
             if (field.getText().length() == 0) {
                 String pluginLang= determineLanguage();
                 if (pluginLang.length() != 0)
                    field.setText("org.eclipse.imp." + pluginLang + ".preferences");
                 else
                     field.setText(getProjectOfRecord()	.getName() + ".preferences");
             }
         } catch (Exception e) {
             ErrorHandler.reportError("NewPreferencesSpecificationWizardPage.setPageNameIfEmpty():  Cannot set 'pageName' field", e);
         }
     }
     
     
     
     public static String getTemplatesPath() {
     	Bundle bundle= Platform.getBundle(PreferencesPlugin.PREFERENCES_PLUGIN_ID);
     	try {
     	    // Use getEntry() rather than getResource(), since the "templates" folder is
     	    // no longer inside the plugin jar (which is now expanded upon installation).
     	    String tmplPath= FileLocator.toFileURL(bundle.getEntry("templates")).getFile();
 
     	    if (Platform.getOS().equals("win32"))
     		tmplPath= tmplPath.substring(1);
     	    return tmplPath;
     	} catch(IOException e) {
     	    return null;
     	}
     }
     
     
     /**
      * Overrides the default version of this method so as to do nothing, i.e.,
      * so as to not actually create the template field for this wizard page.
      * NOTE:  This wizard already creates its own template field.
      */
 	public WizardPageField createTemplateBrowseField(Composite parent, String componentID) {
 		return null;
 	}
 	
 	
     protected Schema getSchema() {
     	return fSchema;
     }
     
 }
