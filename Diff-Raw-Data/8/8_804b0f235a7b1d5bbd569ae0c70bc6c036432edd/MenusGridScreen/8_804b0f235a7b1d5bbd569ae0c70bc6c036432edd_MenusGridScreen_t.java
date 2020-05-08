 /**
  * @(#)MenusGridScreen.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.main.screen;
 
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
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import org.jbundle.main.db.*;
 
 /**
  *  MenusGridScreen - .
  */
 public class MenusGridScreen extends FolderGridScreen
 {
     protected App oldApp = null;
     /**
      * Default constructor.
      */
     public MenusGridScreen()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?.
      */
     public MenusGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         this();
         this.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         oldApp = null;
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * MenusGridScreen Method.
      */
     public MenusGridScreen(Record recHeader, Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object>
      properties)
     {
         this();
         this.init(recHeader, record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * GetDatabaseOwner Method.
      */
     public DatabaseOwner getDatabaseOwner()
     {
        Task task = this.getTask();
        App app = task.getApplication();
        String databaseName = app.getProperty("main" + BaseDatabase.DBSHARED_PARAM_SUFFIX);
         if ((databaseName == null) || (!databaseName.equalsIgnoreCase(Utility.addToPath("main", BaseDatabase.getSystemSuffix(this.getProperty(DBConstants.SYSTEM_NAME)), BaseDatabase.DB_NAME_SEPARATOR))))
         {
            oldApp = app;
             Map<String,Object> properties = Utility.copyAppProperties(null, oldApp.getProperties());
             
             BaseDatabase.addDBProperties(properties, this, null);
             properties.put("main" + BaseDatabase.DBSHARED_PARAM_SUFFIX, Utility.addToPath("main", BaseDatabase.getSystemSuffix(this.getProperty(DBConstants.SYSTEM_NAME)), BaseDatabase.DB_NAME_SEPARATOR));
             properties.put(DBConstants.MODE, BaseDatabase.RUN_MODE);
         
             Environment env = ((BaseApplication)oldApp).getEnvironment();
             BaseApplication newApp = new MainApplication(env, properties, null);
             oldApp.removeTask(task);
             newApp.addTask(task, null);
             task.setApplication(newApp);
         
             return newApp;
         }
         return super.getDatabaseOwner();
     }
     /**
      * Free Method.
      */
     public void free()
     {
         if (oldApp != null)
         {
             Task task = this.getTask();
             task.getApplication().removeTask(task);
             oldApp.addTask(task, null);
             task.setApplication(oldApp);            
         }
         super.free();
     }
     /**
      * Override this to open the main file.
      * <p />You should pass this record owner to the new main file (ie., new MyNewTable(thisRecordOwner)).
      * @return The new record.
      */
     public Record openMainRecord()
     {
         return new Menus(this);
     }
     /**
      * Process the command.
      * <br />Step 1 - Process the command if possible and return true if processed.
      * <br />Step 2 - If I can't process, pass to all children (with me as the source).
      * <br />Step 3 - If children didn't process, pass to parent (with me as the source).
      * <br />Note: Never pass to a parent or child that matches the source (to avoid an endless loop).
      * @param strCommand The command to process.
      * @param sourceSField The source screen field (to avoid echos).
      * @param iCommandOptions If this command creates a new screen, create in a new window?
      * @return true if success.
      */
     public boolean doCommand(String strCommand, ScreenField sourceSField, int iCommandOptions)
     {
         if (strCommand.equalsIgnoreCase(MenuConstants.FORMDETAIL))
             return (this.onForm(null, ScreenConstants.DETAIL_MODE, true, iCommandOptions, null) != null);
         if (strCommand.equalsIgnoreCase(MenuConstants.FORMLINK))
         {
             int iDocMode = ScreenConstants.MAINT_MODE;
             BasePanel parentScreen = this.getParentScreen();
             ScreenLocation itsLocation = null;
             if ((iCommandOptions & ScreenConstants.USE_NEW_WINDOW) == ScreenConstants.USE_SAME_WINDOW)  // Use same window
                 itsLocation = this.getScreenLocation();
             else
                 parentScreen = Screen.makeWindow(this.getTask().getApplication());
         
             Record record = this.getMainRecord();
             Record recordNew = null;
             try   {
                 recordNew = (Record)record.clone();
                 boolean bRefreshIfChanged = false;
                 if ((iCommandOptions & ScreenConstants.USE_NEW_WINDOW) == ScreenConstants.USE_NEW_WINDOW)
                     bRefreshIfChanged = true;    // If I use the same window, I will free this record, so no need to refresh
                 if ((record.getEditMode() == Constants.EDIT_CURRENT) || (record.getEditMode() == Constants.EDIT_IN_PROGRESS))
                     recordNew.readSameRecord(record, true, bRefreshIfChanged);
                 else
                     recordNew.addNew();
             } catch (CloneNotSupportedException ex)   {
                 return false;
             } catch (DBException ex)    {
                 ex.printStackTrace();
             }
         
             Record recHeader = m_recHeader;
             m_recHeader = null;   // Pass header, don't close it on free
         
             if ((iCommandOptions & ScreenConstants.USE_NEW_WINDOW) == ScreenConstants.USE_SAME_WINDOW)  // Use same window
                 this.free();
             new MenusScreen(recHeader, recordNew, itsLocation, parentScreen, null, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
             return true;
         }
         else
             return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
     /**
      * SetupSFields Method.
      */
     public void setupSFields()
     {
         this.getRecord(Menus.MENUS_FILE).getField(Menus.SEQUENCE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Menus.MENUS_FILE).getField(Menus.TYPE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Menus.MENUS_FILE).getField(Menus.NAME).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
     }
 
 }
