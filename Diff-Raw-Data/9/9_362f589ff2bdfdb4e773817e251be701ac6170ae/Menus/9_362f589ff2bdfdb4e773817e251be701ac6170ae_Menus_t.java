 /**
  * @(#)Menus.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.main.db;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.jbundle.base.db.xmlutil.*;
 import org.jbundle.model.main.db.*;
 
 /**
  *  Menus - Menu maintenance.
  */
 public class Menus extends Folder
      implements MenusModel
 {
     private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor.
      */
     public Menus()
     {
         super();
     }
     /**
      * Constructor.
      */
     public Menus(RecordOwner screen)
     {
         this();
         this.init(screen);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwner screen)
     {
         super.init(screen);
     }
     /**
      * Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? Record.formatTableNames(MENUS_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Menu";
     }
     /**
      * Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "main";
     }
     /**
      * Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return DBConstants.LOCAL | DBConstants.SHARED_DATA | DBConstants.LOCALIZABLE;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & ScreenConstants.DOC_MODE_MASK) == ScreenConstants.DETAIL_MODE)
             screen = Record.makeNewScreen(MENUS_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(MENUS_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.DISPLAY_MODE) != 0)
             screen = Record.makeNewScreen(MENUS_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MENU_MODE) != 0)
             screen = Record.makeNewScreen(MENU_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = super.makeScreen(itsLocation, parentScreen, iDocMode, properties);
         return screen;
     }
     /**
      * Add this field in the Record's field sequence.
      */
     public BaseField setupField(int iFieldSeq)
     {
         BaseField field = null;
         //if (iFieldSeq == 0)
         //{
         //  field = new CounterField(this, ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 1)
         //{
         //  field = new RecordChangedField(this, LAST_CHANGED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 2)
         //{
         //  field = new BooleanField(this, DELETED, Constants.DEFAULT_FIELD_LENGTH, null, new Boolean(false));
         //  field.setHidden(true);
         //}
         if (iFieldSeq == 3)
             field = new StringField(this, NAME, 50, null, null);
         if (iFieldSeq == 4)
             field = new MenusField(this, PARENT_FOLDER_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 5)
             field = new ShortField(this, SEQUENCE, Constants.DEFAULT_FIELD_LENGTH, null, new Short((short)0));
         if (iFieldSeq == 6)
             field = new XmlField(this, COMMENT, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 7)
         //  field = new StringField(this, CODE, 30, null, null);
         if (iFieldSeq == 8)
             field = new StringField(this, TYPE, 10, null, null);
         if (iFieldSeq == 9)
             field = new BooleanField(this, AUTO_DESC, Constants.DEFAULT_FIELD_LENGTH, null, new Boolean(true));
         if (iFieldSeq == 10)
             field = new StringField(this, PROGRAM, 255, null, null);
         if (iFieldSeq == 11)
             field = new XMLPropertiesField(this, PARAMS, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 12)
             field = new StringField(this, ICON_RESOURCE, 255, null, null);
         if (iFieldSeq == 13)
             field = new StringField(this, KEYWORDS, 50, null, null);
         if (iFieldSeq == 14)
             field = new XmlField(this, XML_DATA, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 15)
             field = new XmlField(this, MENUS_HELP, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (field == null)
             field = super.setupField(iFieldSeq);
         return field;
     }
     /**
      * Add this key area description to the Record.
      */
     public KeyArea setupKey(int iKeyArea)
     {
         KeyArea keyArea = null;
         if (iKeyArea == 0)
         {
             keyArea = this.makeIndex(DBConstants.UNIQUE, ID_KEY);
             keyArea.addKeyField(ID, DBConstants.ASCENDING);
         }
         if (iKeyArea == 1)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, PARENT_FOLDER_ID_KEY);
             keyArea.addKeyField(PARENT_FOLDER_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(SEQUENCE, DBConstants.ASCENDING);
             keyArea.addKeyField(TYPE, DBConstants.ASCENDING);
             keyArea.addKeyField(NAME, DBConstants.ASCENDING);
         }
         if (iKeyArea == 2)
         {
             keyArea = this.makeIndex(DBConstants.SECONDARY_KEY, CODE_KEY);
             keyArea.addKeyField(CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 3)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, TYPE_KEY);
             keyArea.addKeyField(TYPE, DBConstants.ASCENDING);
             keyArea.addKeyField(PROGRAM, DBConstants.ASCENDING);
         }
         if (keyArea == null)
             keyArea = super.setupKey(iKeyArea);     
         return keyArea;
     }
     /**
      * Get the html code to access this link.
      */
     public String getLink()
     {
         String strType = this.getField(Menus.TYPE).getString();
         String strLink = this.getField(Menus.PROGRAM).getString();
         if (strLink != null)
             if (strLink.length() > 1)
                 if (strLink.charAt(0) == '.')
                     strLink = DBConstants.ROOT_PACKAGE + strLink.substring(1);
         String strTitle = this.getField(Menus.NAME).getString();
         String strParams = Utility.addURLParam(null, strType, strLink); // Default command
         if ((strType.equalsIgnoreCase(DBParams.SCREEN))
                 || (strType.equalsIgnoreCase(DBParams.RECORD)))
         {
             strParams = Utility.addURLParam(null, strType, strLink);
         }
         else if ((strType.equalsIgnoreCase(MenuConstants.MENUREC)) 
                 || (strType.equalsIgnoreCase(MenuConstants.FORM))
                 || (strType.equalsIgnoreCase(MenuConstants.GRID)))
         {
             strParams = Utility.addURLParam(null, DBParams.RECORD, strLink);
             strParams = Utility.addURLParam(strParams, DBParams.COMMAND, strType);
         }
         else if (strType.equalsIgnoreCase(DBParams.MENU))
         { // Default is correct
         }
         else if (strType.equalsIgnoreCase(DBParams.XML))
         {
             strParams = Utility.addURLParam(strParams, "title", strTitle);
         }
         else if (strType.equalsIgnoreCase(DBParams.LINK))
         {
             strParams = strLink;
             if ((strLink.indexOf('.') < strLink.indexOf('/'))
                 && (strLink.indexOf('.') != -1))
                     strParams = "http://" + strLink;
         }
         else if (strType.equalsIgnoreCase(DBParams.MAIL))
         {
             strParams = strLink;
             if (strLink.indexOf("mailto:") != 0)
                 strParams = "mailto:" + strLink;
         }
         else if (strType.equalsIgnoreCase(DBParams.APPLET))
         { // Default is usually okay
             if ((strLink == null) || (strLink.length() == 0))
                 strParams = Utility.addURLParam(null, strType, DBParams.BASE_APPLET); // Default command
             if (strParams.indexOf(DBConstants.DEFAULT_SERVLET) == 0)
                strParams = strParams.substring(DBConstants.DEFAULT_SERVLET.length());
         }
         else
         { // Default is okay
         }
         Map<String,Object> properties = ((PropertiesField)this.getField(Menus.PARAMS)).loadProperties();
         if (properties != null)
             if (DBConstants.BLANK.equalsIgnoreCase((String)properties.get(DBParams.USER_NAME)))
         {       // They want me to fill in the user name
             if (this.getTask() != null)
             {
                 if (this.getTask().getProperty(DBParams.USER_ID) != null)
                     properties.put(DBParams.USER_ID, this.getTask().getProperty(DBParams.USER_ID));
         // todo (don) For now, don't pass authentication to remote menu items
         //        if (this.getTask().getProperty(DBParams.AUTH_TOKEN) != null)    // This is used instead of a password to authenticate
         //            properties.put(DBParams.AUTH_TOKEN, this.getTask().getProperty(DBParams.AUTH_TOKEN));
                 properties.remove(DBParams.USER_NAME);
             }
         }
         if (properties != null)
             if (!strType.equalsIgnoreCase(DBParams.MENU))
                 strParams = Utility.propertiesToURL(strParams, properties);
         return strParams;
     }
     /**
      * Add a tag to this XML for the menu link.
      */
     public StringBuffer addLinkTag(StringBuffer sb)
     {
         int iIndex = sb.lastIndexOf(Utility.endTag(this.getTableNames(false)));
         if (iIndex != -1)
         {
             String strLink = Utility.encodeXML(this.getLink());
             String strHelpLink = strLink;
             if (strLink.indexOf('?') != -1)   // Always
                 strHelpLink = "?" + DBParams.HELP + "=" + "&amp;" + strLink.substring(strLink.indexOf('?') + 1);
             sb.insert(iIndex,
                     Utility.startTag(XMLTags.LINK) +
                         strLink +
                     Utility.endTag(XMLTags.LINK) + XmlUtilities.NEWLINE +
                     Utility.startTag(XMLTags.HELPLINK) +
                         strHelpLink +
                     Utility.endTag(XMLTags.HELPLINK) + XmlUtilities.NEWLINE);
         }
         return sb;
     }
     /**
      * Get the XML for this menu item and it's sub-menus.
      */
     public String getSubMenuXML()
     {
         StringBuffer sbMenuArea = new StringBuffer();
         
         if (this.getEditMode() == Constants.EDIT_CURRENT)
         {
             if (this.getField(Menus.ICON_RESOURCE).isNull())
             {
                 String strIcon = this.getField(Menus.TYPE).toString();
                 if (strIcon.length() > 0)
                     strIcon = strIcon.substring(0, 1).toUpperCase() + strIcon.substring(1);
                 this.getField(Menus.ICON_RESOURCE).setString(strIcon);
             }
             sbMenuArea.append(XmlUtilities.createXMLStringRecord(this));
             sbMenuArea = this.addLinkTag(sbMenuArea);
         }
         
         if (sbMenuArea.length() == 0)
             sbMenuArea.append(
                     Utility.startTag(this.getTableNames(false)) +
                     " <Name>Name</Name>" +
                     " <Description>Description</Description>" +
                     " <Program>Program</Program>" +
                     " <Params>Params</Params>" +
                     " <IconResource>IconResource</IconResource>" +
                     " <Keywords>Keywords</Keywords>" +
                     " <Html>Html</Html>" +
                     Utility.endTag(this.getTableNames(false)));
         
         StringBuffer sbContentArea = new StringBuffer();
         sbContentArea.append(Utility.startTag(XMLTags.MENU_LIST));
         try   {
             String strMenu = this.getField(Menus.ID).toString();
             this.setKeyArea(Menus.PARENT_FOLDER_ID_KEY);
             FileListener behavior = new StringSubFileFilter(strMenu, this.getField(Menus.PARENT_FOLDER_ID), null, null, null, null);
             this.addListener(behavior);
             this.close();
             while (this.hasNext())
             {
                 this.next();
                 if (this.getField(Menus.ICON_RESOURCE).isNull())
                 {
                     String strIcon = this.getField(Menus.TYPE).toString();
                     if (strIcon.length() > 0)
                         strIcon = strIcon.substring(0, 1).toUpperCase() + strIcon.substring(1);
                     this.getField(Menus.ICON_RESOURCE).setString(strIcon);
                 }
                 StringBuffer sbMenuItem = new StringBuffer();
                 sbMenuItem.append(XmlUtilities.createXMLStringRecord(this));
                 sbMenuItem = this.addLinkTag(sbMenuItem);
                 sbContentArea.append(sbMenuItem);
             }
             this.removeListener(behavior, true);
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         sbContentArea.append(Utility.endTag(XMLTags.MENU_LIST));
         sbMenuArea.insert(sbMenuArea.lastIndexOf(Utility.endTag(this.getTableNames(false))), sbContentArea);
         return sbMenuArea.toString();
     }
 
 }
