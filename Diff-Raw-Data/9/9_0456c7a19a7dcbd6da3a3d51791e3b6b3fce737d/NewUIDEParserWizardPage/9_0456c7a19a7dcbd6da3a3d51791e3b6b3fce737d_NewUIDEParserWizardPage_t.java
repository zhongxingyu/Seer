 package org.jikespg.uide.wizards;
 
 import org.eclipse.pde.core.plugin.IPluginElement;
 import org.eclipse.pde.core.plugin.IPluginExtension;
 import org.eclipse.pde.core.plugin.IPluginModel;
 import org.eclipse.pde.core.plugin.IPluginObject;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.uide.core.ErrorHandler;
 import org.eclipse.uide.runtime.RuntimePlugin;
 import org.eclipse.uide.wizards.ExtensionPointEnabler;
 import org.eclipse.uide.wizards.ExtensionPointWizard;
 import org.eclipse.uide.wizards.ExtensionPointWizardPage;
 import org.eclipse.uide.wizards.WizardPageField;
 import org.jikespg.uide.JikesPGPlugin;
 
 /**
  * The "New" wizard page allows setting the container for the new file as well as the
  * file name. The page will only accept file name without the extension OR with the
  * extension that matches the expected one (g).
  */
 public class NewUIDEParserWizardPage extends ExtensionPointWizardPage {
     GrammarOptions fGrammarOptions= new GrammarOptions();
 
     public NewUIDEParserWizardPage(ExtensionPointWizard wizard) {
 	super(wizard, RuntimePlugin.UIDE_RUNTIME, "parser");
 	setTitle("JikesPG Grammar");
 	setDescription("This wizard creates a new JikesPG grammar file with a '.g' extension.");
     }
 
     protected void createAdditionalControls(Composite parent) {
 	GrammarPageHelper helper= new GrammarPageHelper(parent, null, fGrammarOptions, getShell());
 
 	helper.createFields();
     }
 
     public void createControl(Composite parent) {
 	super.createControl(parent);
 	setLanguageIfEmpty();
 	try {
 //	    getField("class").setEnabled(false);
             String lang= getField("language").getText();
             // RMF 10/10/2006 - fOmitExtensionIDName is true except for builder
             // extensions, so there *is* no "id" field for the parser extension!
 //          getField("id").setText(lang + ".safari.parser");
             getField("language").fText.addModifyListener(new ModifyListener() {
                 public void modifyText(ModifyEvent e) {
                     // RMF 10/10/2006 - fOmitExtensionIDName is true except for builder
                     // extensions, so there *is* no "id" field for the parser extension!
 //                  setIDIfEmpty();
                     setClassIfEmpty();
                 }
             });
 	    fProjectText.addModifyListener(new ModifyListener() {
 		public void modifyText(ModifyEvent e) {
 		    setLanguageIfEmpty();
 		}
 	    });
 	} catch (Exception e) {
 	    ErrorHandler.reportError("Internal error, extension point schema may have changed", e);
 	}
     }
 
     public String determineLanguage() {
 	try {
 	    IPluginModel pluginModel= ExtensionPointEnabler.getPluginModel(this);
 
 	    if (pluginModel != null) {
 		IPluginExtension[] extensions= pluginModel.getExtensions().getExtensions();
 
 		for(int n= 0; n < extensions.length; n++) {
 		    IPluginExtension extension= extensions[n];
 
                     if (!extension.getPoint().equals("org.eclipse.uide.runtime.languageDescription"))
                         continue;
 
                     IPluginObject[] children= extension.getChildren();
 
 		    for(int k= 0; k < children.length; k++) {
 			IPluginObject object= children[k];
 
 			if (object.getName().equals("language")) {
 			    return ((IPluginElement) object).getAttribute("language").getValue();
 			}
 		    }
 		    System.out.println("Unable to determine language for plugin '" + pluginModel.getBundleDescription().getName() + "': no languageDescription extension.");
 		}
 	    } else if (getProject() != null)
 		System.out.println("Not a plugin project: " + getProject().getName());
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
             getField("class").setText(fGrammarOptions.getPackageForLanguage(pluginLang) + ".ParseController");
         } catch (Exception e) {
             ErrorHandler.reportError("Cannot set language", e);
         }
     }
 
     // RMF 10/10/2006 - There *is* no "id" field/attribute; that only exists for builder extensions that actually need one.
 //    protected void setIDIfEmpty() {
 //        if (true) return;
 //	try {
 //	    WizardPageField langField= getField("language");
 //            String language= langField.getText();
 //
 //            if (language.length() == 0)
 //                return;
 //            String langID= lowerCaseFirst(language);
 //	    getField("id").setText(langID + ".safari.parser");
 //	} catch (Exception e) {
 //	    ErrorHandler.reportError("Cannot set ID", e);
 //	}
 //    }
 
     protected void setClassIfEmpty() {
         try {
             WizardPageField langField= getField("language");
             String language= langField.getText();
 
             if (language.length() == 0)
                 return;
             String langPkg= lowerCaseFirst(language);
             String langClass= upperCaseFirst(language);
 
            // TODO Unify the following code with similar stuff in NewUIDEParserWizard.generateCodeStubs().
             getField("class").setText(fGrammarOptions.getPackageForLanguage(language) + ".ParseController");
         } catch (Exception e) {
             ErrorHandler.reportError("Cannot set class", e);
         }
     }
 
     public GrammarOptions getOptions() {
 	return fGrammarOptions;
     }
 }
