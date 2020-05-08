 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.theme.basic;
 
 import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.commons.io.UriAddressableFile;
 import com.netspective.commons.io.UriAddressableFileLocator;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.form.DialogSkin;
 import com.netspective.sparx.navigate.NavigationSkin;
 import com.netspective.sparx.panel.HtmlPanelSkin;
 import com.netspective.sparx.report.tabular.HtmlTabularReportSkin;
 import com.netspective.sparx.security.LoginDialogSkin;
 import com.netspective.sparx.theme.Theme;
 
 /**
  * A Theme is defined as a logical collection of the look and feel of various framework components such as pages,
  * reports, and dialogs. This abstract class defines some of the default skins for these components.
  */
 public class AbstractTheme implements Theme, XmlDataModelSchema.InputSourceLocatorListener
 {
     private static final Log log = LogFactory.getLog(AbstractTheme.class);
 
     private InputSourceLocator inputSourceLocator;
     private String name;
     private UriAddressableFileLocator resourceLocator;
     private boolean defaultTheme;
     private Map navigationSkins = new TreeMap();
     private Map tabularReportSkins = new TreeMap();
     private Map panelSkins = new TreeMap();
     private Map dialogSkins = new TreeMap();
     private NavigationSkin defaultNavigationSkin = createNavigationSkin();
     private HtmlPanelSkin tabbedPanelSkin = constructTabbedPanelSkin();;
     private HtmlPanelSkin templatePanelSkin = constructTabbedPanelSkin();
     private HtmlTabularReportSkin defaultReportSkin = new BasicHtmlTabularReportPanelSkin(this, "default", "panel-output", "panel/output", false);
     private DialogSkin defaultDialogSkin = new StandardDialogSkin(this, "default", "panel-input", "panel/input", false);
     private LoginDialogSkin defaulLoginDialogSkin = constructLoginDialogSkin();
     private String[] inheritResourcesFromThemes = new String[0];
     private HtmlListPanelSkin listPanelSkin = constructListPanelSkin();
 
     /**
      * Sole constructor
      */
     public AbstractTheme()
     {
     }
 
     /**
      * Gets the input source locator
      *
      * @return the input source locator
      */
     public InputSourceLocator getInputSourceLocator()
     {
         return inputSourceLocator;
     }
 
     public void setInputSourceLocator(InputSourceLocator inputSourceLocator)
     {
         this.inputSourceLocator = inputSourceLocator;
     }
 
     /**
      * Gets the resource locator
      *
      * @return the resource locator
      */
     public UriAddressableFileLocator getResourceLocator()
     {
         return resourceLocator;
     }
 
     public void setWebResourceLocator(UriAddressableFileLocator locator)
     {
         this.resourceLocator = locator;
     }
 
     /**
      * Constructs a  URL for the resource by prepending the theme's name to the relative URL.
      *
      * @param themeName   the name of the theme
      * @param relativeUrl the relative URL of the resource with respect to the theme's location
      */
     protected String getResourceUrlWithThemePrefix(final String themeName, final String relativeUrl)
     {
         StringBuffer themeRelativeUrlBuf = new StringBuffer("theme/");
         themeRelativeUrlBuf.append(themeName);
         if(!relativeUrl.startsWith("/"))
             themeRelativeUrlBuf.append('/');
         themeRelativeUrlBuf.append(relativeUrl);
         return themeRelativeUrlBuf.toString();
     }
 
     /**
      * Gets the URL of the resource  related to the theme
      */
     public String getResourceUrl(final String relativeUrl)
     {
         String themeRelativeUrl = getResourceUrlWithThemePrefix(name, relativeUrl);
 
         if(resourceLocator == null)
         {
             System.err.println("No resource locator set for theme " + this);
             return themeRelativeUrl;
         }
 
         try
         {
             UriAddressableFile resource = resourceLocator.findUriAddressableFile(themeRelativeUrl);
             if(resource != null)
                 return resource.getUrl();
 
             for(int i = 0; i < inheritResourcesFromThemes.length; i++)
             {
                 resource = resourceLocator.findUriAddressableFile(getResourceUrlWithThemePrefix(inheritResourcesFromThemes[i], relativeUrl));
                 if(resource != null)
                     return resource.getUrl();
             }
         }
         catch(IOException e)
         {
             log.error("Unable to retrieve resource information for URL " + themeRelativeUrl, e);
             return themeRelativeUrl;
         }
 
         if(log.isWarnEnabled()) log.warn("Resource '" + themeRelativeUrl + "' not located in resource locator " + resourceLocator);
         return themeRelativeUrl;
     }
 
     /**
      * Gets the URL of the resource related to the theme
      *
      * @param relativeUrl the relative URL of the resource
      * @param defaultUrl  the URL to use if the resource is not located using the relative URL
      *
      * @return the resource's URL
      */
     public String getResourceUrl(final String relativeUrl, final String defaultUrl)
     {
         if(resourceLocator == null)
         {
             System.err.println("No resource locator set for theme " + this);
             return defaultUrl;
         }
 
         try
         {
             String themeRelativeUrl = getResourceUrlWithThemePrefix(name, relativeUrl);
 
             UriAddressableFile resource = resourceLocator.findUriAddressableFile(themeRelativeUrl);
             if(resource != null)
                 return resource.getUrl();
 
             for(int i = 0; i < inheritResourcesFromThemes.length; i++)
             {
                 resource = resourceLocator.findUriAddressableFile(getResourceUrlWithThemePrefix(inheritResourcesFromThemes[i], relativeUrl));
                 if(resource != null)
                     return resource.getUrl();
             }
         }
         catch(IOException e)
         {
             return defaultUrl;
         }
 
         return defaultUrl;
     }
 
     /**
      * Gets an array of resources inherited from other themese
      *
      * @return array of resources
      */
     public String[] getInheritResourcesFromThemes()
     {
         return inheritResourcesFromThemes;
     }
 
     /**
      * Sets the resources inherited from other themes
      *
      * @param delimitedThemeNames A comma delimited string of resources
      */
     public void setInheritResourcesFromThemes(String delimitedThemeNames)
     {
         List themeNamesList = new ArrayList();
         String[] themeNames = TextUtils.getInstance().split(delimitedThemeNames, ",", true);
         for(int i = 0; i < themeNames.length; i++)
             themeNamesList.add(themeNames[i]);
         for(int i = 0; i < inheritResourcesFromThemes.length; i++)
             themeNamesList.add(inheritResourcesFromThemes[i]);
         inheritResourcesFromThemes = (String[]) themeNamesList.toArray(new String[themeNamesList.size()]);
     }
 
     protected HtmlPanelSkin constructTabbedPanelSkin()
     {
         return new BasicHtmlPanelSkin(this, name, "panel-output", "panel/output", false);
     }
 
     protected LoginDialogSkin constructLoginDialogSkin()
     {
         return new StandardLoginDialogSkin(this, "login", "panel-input", "panel/input", false);
     }
 
     protected HtmlListPanelSkin constructListPanelSkin()
     {
         return new HtmlListPanelSkin(this, "list-panel", "panel-output", "panel/output", false);
     }
 
     /**
      * Gets the name of the theme
      *
      * @return theme name
      */
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     /**
      * Gets the default navigation skin
      *
      * @return default navigation skin
      */
     public NavigationSkin getDefaultNavigationSkin()
     {
         return defaultNavigationSkin;
     }
 
     /**
      * Gets a theme navigation skin by its name
      *
      * @param name navigation skin name
      *
      * @return navigation skin
      */
     public NavigationSkin getNavigationSkin(String name)
     {
         return (NavigationSkin) navigationSkins.get(name);
     }
 
     /**
      * Returns the default report skin
      *
      * @return html tabular report skin
      */
     public HtmlTabularReportSkin createReportSkin()
     {
         return defaultReportSkin;
     }
 
     /**
      * Creates a new dialog skin
      *
      * @return new dialog skin
      */
     public DialogSkin createDialogSkin()
     {
         return new StandardDialogSkin(this, "default", "panel-output", "panel/output", false);
     }
 
     /**
      * Gets a tabbed html panel skin
      */
     public HtmlPanelSkin getTabbedPanelSkin()
     {
         return constructTabbedPanelSkin();
     }
 
     /**
      * Gets a template panel skin
      */
     public HtmlPanelSkin getTemplatePanelSkin()
     {
         return templatePanelSkin;
     }
 
     /**
      * Gets a template skin by its name
      *
      * @param name skin name
      *
      * @return template skin
      */
     public HtmlPanelSkin getTemplateSkin(String name)
     {
         return (HtmlPanelSkin) panelSkins.get(name);
     }
 
     /**
      * Gets the default report skin
      *
      * @return html report skin
      */
     public HtmlTabularReportSkin getDefaultReportSkin()
     {
         return defaultReportSkin;
     }
 
     /**
      * Gets a report skin by its name
      *
      * @param name report skin name
      *
      * @return report skin
      */
     public HtmlTabularReportSkin getReportSkin(String name)
     {
         return (HtmlTabularReportSkin) tabularReportSkins.get(name);
     }
 
     public LoginDialogSkin createLoginDialogSkin()
     {
         return constructLoginDialogSkin();
     }
 
     public void addLoginDialogSkin(LoginDialogSkin loginDialogSkin)
     {
         loginDialogSkin.setTheme(this);
         this.defaulLoginDialogSkin = loginDialogSkin;
     }
 
     /**
      * Gets the login dialog skin
      */
     public LoginDialogSkin getLoginDialogSkin()
     {
         return defaulLoginDialogSkin;
     }
 
     /**
      * Gets the default dialog skin
      *
      * @return default dialog skin
      */
     public DialogSkin getDefaultDialogSkin()
     {
         return defaultDialogSkin;
     }
 
     /**
      * Gets a dialog skin by its name
      *
      * @param name dialog skin name
      *
      * @return dialog skin
      */
     public DialogSkin getDialogSkin(String name)
     {
         return (DialogSkin) dialogSkins.get(name);
     }
 
     /**
      * Adds a dialog skin
      *
      * @param skin dialog skin
      */
     public void addDialogSkin(DialogSkin skin)
     {
         skin.setTheme(this);
         dialogSkins.put(skin.getName(), skin);
         if(skin.isDefault())
             defaultDialogSkin = skin;
     }
 
     /**
      * Creates a new navigation skin. This class implementation returns a null.
      */
     public NavigationSkin createNavigationSkin()
     {
         return null;
     }
 
    public NavigationSkin createNavigationSkin(Class cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if(NavigationSkin.class.isAssignableFrom(cls))
        {
            Constructor c = cls.getConstructor(new Class[]{Theme.class});
            return (NavigationSkin) c.newInstance(new Object[]{this});
        }
        else
            throw new RuntimeException("Don't know what to do with with class: " + cls);
    }

     /**
      * Adds a new navigation skin and also sets the default navigation skin if the passed in skin
      * is configured to be the default one.
      *
      * @param skin navigation skin
      */
     public void addNavigationSkin(NavigationSkin skin)
     {
         skin.setTheme(this);
         navigationSkins.put(skin.getName(), skin);
         if(skin.isDefault())
             defaultNavigationSkin = skin;
     }
 
     /**
      * Adds a html panel skin
      *
      * @param skin panel skin
      */
     public void addPanelSkin(HtmlPanelSkin skin)
     {
         skin.setTheme(this);
         panelSkins.put(skin.getName(), skin);
     }
 
     /**
      * Adds a report skin and sets the default report skin if the passed in skin is configured to be the default one.
      *
      * @param skin report skin
      */
     public void addReportSkin(HtmlTabularReportSkin skin)
     {
         skin.setTheme(this);
         tabularReportSkins.put(skin.getName(), skin);
         if(skin.isDefault())
             defaultReportSkin = skin;
     }
 
     /**
      * Gets a map of dialog skins of the theme
      *
      * @return dialog skins
      */
     public Map getDialogSkins()
     {
         return dialogSkins;
     }
 
     public Map getNavigationSkins()
     {
         return navigationSkins;
     }
 
     public Map getPanelSkins()
     {
         return panelSkins;
     }
 
     public Map getReportSkins()
     {
         return tabularReportSkins;
     }
 
     /**
      * Checks to see if the theme is the default one
      *
      * @return True if the theme is the default one
      */
     public boolean isDefault()
     {
         return defaultTheme;
     }
 
     /**
      * Sets the default flag of the theme
      */
     public void setDefault(boolean defaultTheme)
     {
         this.defaultTheme = defaultTheme;
     }
 
     public HtmlListPanelSkin getListPanelSkin()
     {
         return listPanelSkin;
     }
 }
