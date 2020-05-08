 /*
  * Copyright © 2011 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.field;
 
 /**
  * @(#)BaseField.java 0.00 12-Feb-97 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Enumeration;
 import java.util.Map;
 
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.SQLParams;
 import org.jbundle.base.db.event.FileListener;
 import org.jbundle.base.db.event.FileRemoveBOnCloseHandler;
 import org.jbundle.base.field.convert.FieldConverter;
 import org.jbundle.base.field.convert.FieldDescConverter;
 import org.jbundle.base.field.convert.QueryConverter;
 import org.jbundle.base.field.event.FieldListener;
 import org.jbundle.base.field.event.MainReadOnlyHandler;
 import org.jbundle.base.field.event.ReadSecondaryHandler;
 import org.jbundle.base.screen.model.BaseGridScreen;
 import org.jbundle.base.screen.model.BasePanel;
 import org.jbundle.base.screen.model.GridScreen;
 import org.jbundle.base.screen.model.SCannedBox;
 import org.jbundle.base.screen.model.SEditText;
 import org.jbundle.base.screen.model.STEView;
 import org.jbundle.base.screen.model.ScreenField;
 import org.jbundle.base.screen.model.TopScreen;
 import org.jbundle.base.screen.model.util.SSelectBox;
 import org.jbundle.base.screen.model.util.ScreenLocation;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.base.util.Debug;
 import org.jbundle.base.util.ScreenConstants;
 import org.jbundle.base.util.Utility;
 import org.jbundle.model.Freeable;
 import org.jbundle.model.db.Convert;
 import org.jbundle.model.db.Rec;
 import org.jbundle.model.screen.ComponentParent;
 import org.jbundle.model.screen.ScreenComponent;
 import org.jbundle.model.screen.ScreenLoc;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.db.FieldInfo;
 import org.jbundle.thin.base.util.ThinMenuConstants;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * BaseField - This is the base class for all fields.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class BaseField extends FieldInfo
     implements ListenerOwner, Freeable
 {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Was this field modified on the last change?
      */
     protected boolean m_bJustChanged = false;
     /**
      * Optionally used by DB Engine to cache values.
      */
     protected Object m_DBObject = null;
     /**
      * List of listeners.
      */
     protected FieldListener m_listener = null;
     /**
      * Is this a virtual (Not physically present in DB record) field?
      */
     protected boolean m_bVirtual = false;
     /**
      * Selected for retrieval (by SQL).
      */
     protected boolean m_bSelected = true;
     /**
      * Can this field be null?
      */
     protected boolean m_bNullable = true;
     /**
      * So I don't have to put a bunch of these on the stack.
      */
     public static final boolean[] EMPTY_RG_BOOLEAN = new boolean[0];
     /**
      * The default field length for a big field.
      */
     public static int BIG_DEFAULT_LENGTH = 32000;
 
     /**
      * Constructor.
      */
     public BaseField()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public BaseField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         m_bJustChanged = false;
         m_DBObject = null;
         m_listener = null;
         m_bVirtual = false;
         m_bSelected = true;
         m_bNullable = true;
 
         super.init(record, strName, iDataLength, strDesc, strDefault);
 
         this.setModified(false);    // No modifications to start with
     }
     /**
      * Free this field.
      */
     public void free()
     {
         m_DBObject = null;
         while (m_listener != null)
         {
             this.removeListener(m_listener, true);    // free all the behaviors
         }
         if (m_vScreenField != null)
         {   // Remove all the screen fields
             while (!m_vScreenField.isEmpty())
             {
                 ScreenComponent sField = this.getComponent(0);
                 sField.free();      // Delete this screen field (will free this link).
             }
             m_vScreenField.removeAllElements();
             m_vScreenField = null;
         }
         super.free();
     }
     /**
      * Creates a new object of the same class as this object.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         return null;
     }
     /**
      * Clone the record behaviors of this table and add them to newTable.
      * @param record The record to clone listeners from.
      * @param bCloneListeners Clone listeners that do not exist?
      * @param bMatchEnabledState If source listener is disabled, disable the new (or existing) listener.
      * @param bSyncReferenceFields Make sure any reference fields are referencing the same record.
      */
     public void matchListeners(BaseField field, boolean bCloneListeners, boolean bMatchEnabledState, boolean bSyncReferenceFields, boolean bMatchSelectedState)
     {
         FieldListener listener = this.getListener();
         // Don't add the listeners that already exist in the target record (from the record's addListeners() method).
         FieldListener fieldListener = field.getListener();
         while ((listener != null) && (fieldListener != null))
         {   // Get through the first listeners (The ones added in init in the addListeners() method)
             if (!fieldListener.getClass().getName().equals(listener.getClass().getName()))
                 break;  // Not the same... STOP. and start looking for matching listeners.
             if (bMatchEnabledState)
                 fieldListener.setEnabledListener(listener.isEnabledListener());
             listener = (FieldListener)listener.getNextListener();    // Skip to the next one in the chain.
             fieldListener = (FieldListener)fieldListener.getNextListener();
         }
         while (listener != null)
         {   // Clone all the file behaviors (that want to be cloned)
             int iBehaviorCount = 0;
             FieldListener thisListener = this.getListener(listener.getClass());
             while ((thisListener != null) && (thisListener != listener))
             {   // There are more than one, count which one it is.
                 iBehaviorCount++;
                 thisListener = (FieldListener)thisListener.getListener(listener.getClass());
             }
             FieldListener newBehavior = null;
             newBehavior = field.getListener(listener.getClass());
             while (iBehaviorCount > 0)
             {   // Get the correct one
                 iBehaviorCount--;
                 if (newBehavior != null)
                     newBehavior = (FieldListener)newBehavior.getListener(listener.getClass());
             }
             if ((bCloneListeners) && (newBehavior == null))
             {
                 try   {
                     newBehavior = (FieldListener)listener.clone(field); // Clone the field behavior
                 } catch (CloneNotSupportedException ex)   {
                     newBehavior = null;
                 }
                 if (newBehavior != null)
                     if (newBehavior.getOwner() == null)     // This should have been set, just being careful (ie., next line never called)
                         field.addListener(newBehavior);        // Add them to the new query
             }
             if ((bMatchEnabledState) && (newBehavior != null))
                 newBehavior.setEnabledListener(listener.isEnabledListener());
             listener = (FieldListener)listener.getNextListener();            // Do the next one in the chain
         }
         if ((bSyncReferenceFields) && (field instanceof ReferenceField))
         {
             Record recReference = ((ReferenceField)field).getReferenceRecord(null, false);
             if (recReference != null)
                 ((ReferenceField)field).setReferenceRecord(recReference);
         }
         if (bMatchSelectedState)
             field.setSelected(this.isSelected());
     }
     /**
      * Creates a new object of the exact same class as this field.
      * The clone method will clone a field that can contain the same kind of data but may not be the exact same field class.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public static BaseField cloneField(BaseField fieldToClone) throws CloneNotSupportedException
     {
         BaseField field = null;
         String strClassName = fieldToClone.getClass().getName();
         field = (BaseField)ClassServiceUtility.getClassService().makeObjectFromClassName(strClassName);
         if (field != null)
         {
             field.init(null, fieldToClone.getFieldName(), fieldToClone.getMaxLength(), fieldToClone.getFieldDesc(), fieldToClone.getDefault());
             field.setRecord(fieldToClone.getRecord());     // Set table without adding to table field list
         }
         return field;
     }
     /**
      * Set the next listener in the listener chain.
      * Note: You can pass the full class name, or the short class name or (preferably) the class.
      * @param strListenerClass The name of the class I'm looking for.
      * @return The first listener of this class or null if no match.
      */
     public void setListener(FieldListener listener)
     {
         m_listener = listener;
     }
     /**
      * Add a listener to the chain.
      * @param listener The listener to add to the end of the chain.
      */
     public void addListener(BaseListener listener)
     {
         listener.setNextListener(null);  // Just being safe
         if ((this.getRecord() != null) && (this.getRecord().getTable() != null))
             this.getRecord().getTable().addListener((FieldListener)listener, this);   // Give the table a chance to clone this or whatever.
         else
             this.doAddListener((FieldListener)listener);
     }
     /**
      * Add a listener to the chain (don't use this method).
      * @param listener The listener to add to the end of the chain.
      */
     public void doAddListener(BaseListener listener)
     {
         if (m_listener != null)
             m_listener.doAddListener((FieldListener)listener);
         else
             m_listener = (FieldListener)listener;
         boolean bOldState = listener.setEnabledListener(false);   // To disable recursive forever loop!
         listener.setOwner(this);
         listener.setEnabledListener(bOldState);        // Renable the listener to eliminate echos
     }
     /**
      * Remove this listener from the chain.
      * @param listener The listener to remove.
      * @param bFreeListener If true, the listener is also freed.
      */
     public void removeListener(BaseListener listener, boolean bFreeListener)
     {
         if (m_listener != null)
         {
             if (m_listener == listener)
             {
                 m_listener = (FieldListener)listener.getNextListener();
                 listener.unlink(bFreeListener);     // remove theBehavior from the linked list
             }
             else
                 m_listener.removeListener(listener, bFreeListener);
         } // Remember to free the listener after removing it!
     }
     /**
      * Get the component at this position.
      * @return The component at this position or null.
      */
     public ScreenComponent getComponent(int iPosition)
     {
         return (ScreenComponent)super.getComponent(iPosition);
     }
     /**
      * This screen component is displaying this field.
      * @param Object sField The screen component.. either a awt.Component or a ScreenField.
      */
     public void addComponent(Object sField)
     {
         super.addComponent(sField);     // Notify this screen field if I change
         this.displayField();    // Tell field to redisplay itself using this field's value
     }
     /**
      * Add these quotes to this string.
      * @param szTableNames The source string.
      * @param charStart The starting quote.
      * @param charEnd The ending quote.
      * @return The new (quoted) string.
      */
     public static final String addQuotes(String szTableNames, char charStart, char charEnd)
     {
         String strFileName = szTableNames;
         if (charStart == -1)
             charStart = DBConstants.SQL_START_QUOTE;
         if (charEnd == -1)
             charEnd = DBConstants.SQL_END_QUOTE;
 
         for (int iIndex = 0; iIndex < strFileName.length(); iIndex++)
         {
             if ((strFileName.charAt(iIndex) == charStart)
                 || (strFileName.charAt(iIndex) == charEnd))
             {   // If a quote is in this string, replace with a double-quote Fred's -> Fred''s
                 strFileName = strFileName.substring(0, iIndex) + 
                     strFileName.substring(iIndex, iIndex + 1) + 
                     strFileName.substring(iIndex, iIndex + 1) + 
                     strFileName.substring(iIndex + 1, strFileName.length());
                 iIndex++; // Skip the second quote
             }
         }
         if ((charStart != ' ') && (charEnd != ' '))
             strFileName = charStart + strFileName + charEnd;    // Spaces in name, quotes required
         return strFileName;
     }
     /**
      * Convert the field's code to the display's index (for popup).
      * @return The value in this field to a 0 based index in a table.
      */
     public int convertFieldToIndex()
     {
         String tempString = this.getString();
         return this.convertStringToIndex(tempString);
     }
     /**
      * Convert this index to a display field.
      * @param index The index to convert.
      * @return The display string.
      */
     public String convertIndexToDisStr(int index)
     {
         return this.convertIndexToString(index);    // By default, use the same string
     }
     /**
      * Convert the display's index to the field value and move to field.
      * @param index The index to convert an set this field to.
      * @param bDisplayOption If true, display the change in the converters.
      * @param iMoveMove The type of move.
      */
     public int convertIndexToField(int index, boolean bDisplayOption, int iMoveMode)
     {
         String tempString = this.convertIndexToString(index);
         return this.setString(tempString, bDisplayOption, iMoveMode); // Move value to this field
     }
     /**
      * Convert this index to a string.
      * This method is usually overidden by popup fields.
      * @param index The index to convert.
      * @return The display string.
      */
     public String convertIndexToString(int index)
     {
         String tempString = null;
         if ((index >= 0) && (index <= 9))
         {
             char value[] = new char[1];
             value[0] = (char)('0' + index);
             tempString = new String(value);         // 1 = '1'; 2 = '2'; etc...
         }
         else
             tempString = Constants.BLANK;
         return tempString;
     }
     /**
      * Convert the field's value to a index (for popup) (usually overidden).
      * @param string The string to convert to an index.
      * @return The resulting index.
      */
     public int convertStringToIndex(String tempString)
     {
         int index = 1;
         if (tempString.length() == 0)
             return (short)index;
         if ((tempString.charAt(0) >= '0') & (tempString.charAt(0) <= '9'))
             index = tempString.charAt(0) - '0';             // Convert to number; 1 = 1, 2 = 2, etc...
         if ((tempString.charAt(0) >= 'A') & (tempString.charAt(0) <= 'Z'))
             index = tempString.charAt(0) - 'A' + 1;         // Convert to number; A = 1, B = 2, etc...
         if ((tempString.charAt(0) >= 'a') & (tempString.charAt(0) <= 'z'))
             index = tempString.charAt(0) - 'a' + 1;         // Convert to number; a = 1, b = 2, etc...
         return index;   // Return the position
     }
     /**
      * Display this field using all this field's screen fields.
      */
     public void displayField()                  // init this field override for other value
     {
         if (m_vScreenField == null)
             return;
         for (Enumeration<Object> e = m_vScreenField.elements() ; e.hasMoreElements() ;) { 
             ScreenComponent sField = (ScreenComponent)e.nextElement();
             sField.fieldToControl();    // Display using the new value(s)
         }
     }
     /**
      * Move this physical binary data to this field.
      * @param data The physical data to move to this field (must be the correct raw data class).
      * @param bDisplayOption If true, display after setting the data.
      * @param iMoveMode The type of move.
      * @return an error code (0 if success).
      */
     public int doSetData(Object data, boolean bDisplayOption, int iMoveMode)
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         m_bJustChanged = false;
         if (data == null)
         {
             if (!this.isNullable())
                 return DBConstants.NULL_FIELD;  // Null fields are not allowed
         }
         if (data == m_data)
             m_bJustChanged = false;  // No Change (Usually still null)
         else if ((data == null) || (m_data == null))
             m_bJustChanged = true;
         else if (!data.equals(m_data))
             m_bJustChanged = true;
         if (m_bJustChanged)
         {
             m_bModified = true;
             m_data = data;
         }
         if (iMoveMode == DBConstants.INIT_MOVE)
             m_bJustChanged = true; // Force the behaviors to run
         return iErrorCode;
     }
     /**
      * Merge my changed data back into field that I just restored from disk.
      * Note: I don't care about enabled listeners since all the listeners should be disabled on merge.
      * @param objData The value this field held before I refreshed from disk.
      * @return The setData error code.
      */
     public int mergeData(Object objData)
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         FieldListener nextListener = (FieldListener)this.getListener();
         if (nextListener != null)
             iErrorCode = nextListener.doMergeData(objData);
         else
             iErrorCode = this.doMergeData(objData);
         return iErrorCode;
     }
     /**
      * Merge my changed data back into field that I just restored from disk.
      * @param objData The value this field held before I refreshed from disk.
      * @return The setData error code.
      */
     public int doMergeData(Object objData)
     {
         int iErrorCode = this.setData(objData);
         return iErrorCode;
     }
     /**
      * Get the first field listener on the chain.
      * @return The field listener.
      */
     public FieldListener getListener()
     {
         return m_listener;
     }
     /**
      * Get the listener with this identifier.
      * @param strListenerClass The name of the class I'm looking for.
      * @return The first listener of this class or null if no match.
      */
     public FieldListener getListener(Object strBehaviorClass)
     {
         return this.getListener(strBehaviorClass, true);   // By default need exact match
     }
     /**
      * Get the listener with this class identifier.
      * Note: You can pass the full class name, or the short class name or (preferably) the class.
      * @param bExactMatch Only returns classes that are an exact match... if false, return classes that are an instanceof the class
      * @param strListenerClass The name of the class I'm looking for.
      * @return The first listener of this class or null if no match.
      */
     public FieldListener getListener(Object strBehaviorClass, boolean bExactMatch)
     {
         FieldListener listener = m_listener;
         if (listener == null)
             return null;
         if (!bExactMatch)
         {
             try {
                 if (strBehaviorClass instanceof String)
                     strBehaviorClass = Class.forName((String)strBehaviorClass);
                 if (((Class<?>)strBehaviorClass).isAssignableFrom(listener.getClass()))
                     return listener;
                 else
                     return (FieldListener)listener.getListener(strBehaviorClass, bExactMatch);
             } catch (ClassNotFoundException ex) {
                 ex.printStackTrace();
                 return null;
             }
         }
         if (strBehaviorClass instanceof String)
         {
             String strClass = listener.getClass().getName();
             if (((String)strBehaviorClass).indexOf('.') == -1)
             {   // If identifier is not fully qualified, strip the package from the class
                 if (strClass.lastIndexOf('.') != -1)
                     strClass = strClass.substring(strClass.lastIndexOf('.') + 1);
             }
             if (strClass.equalsIgnoreCase((String)strBehaviorClass))
                 return listener;
         }
         else if (listener.getClass().equals(strBehaviorClass))
             return listener;
         return (FieldListener)listener.getListener(strBehaviorClass, bExactMatch);
     }
     /**
      * Get the next enabled listener on the chain.
      * @param iMoveMode The move mode to respond to.
      * @return The listener (or null if none).
      */
     public BaseListener getNextValidListener(int iMoveMode)
     {
         if (m_listener != null)
         {
             if ((m_listener.isEnabled()) & (m_listener.respondsToMode(iMoveMode)))
                 return m_listener;
             else
                 return m_listener.getNextValidListener(iMoveMode);
         }
         else
             return null;
     }
     /**
      * Enable/Disable all listeners and save the state of the listeners.
      * @param bEnable If true, enable all listeners, else disable all.
      * Note: Besides returning the current status of the listeners, this also saves
      * the status internally, so you can just call restoreEnableListeners(null) to
      * restore them.
      */
     public boolean[] setEnableListeners(boolean bEnable)
     {
         int iCount = 0;
         FieldListener fieldBehavior = this.getListener();
         while (fieldBehavior != null)
         {
             fieldBehavior = (FieldListener)fieldBehavior.getNextListener();
             iCount++;
         }
         boolean[] rgbEnabled = null;
         if (iCount == 0)
             rgbEnabled = EMPTY_RG_BOOLEAN;
         else
             rgbEnabled = new boolean[iCount];
         int iIndex = 0;
         fieldBehavior = this.getListener();
         while (fieldBehavior != null)
         {
             rgbEnabled[iIndex] = fieldBehavior.isEnabledListener();
             fieldBehavior.setEnabledListener(bEnable);
             fieldBehavior = (FieldListener)fieldBehavior.getNextListener();
             iIndex++;
         }
         return rgbEnabled;
     }
     /**
      * Get the status of the the FieldChanged behaviors?
      * @return The handle field change flag (if false field change handling is disabled).
      */
     public void setEnableListeners(boolean[] rgbEnabled)
     {
         int iIndex = 0;
         FieldListener fieldBehavior = this.getListener();
         while (fieldBehavior != null)
         {
             boolean bEnable = true;
             if ((rgbEnabled != null) && (iIndex < rgbEnabled.length))
                 bEnable = rgbEnabled[iIndex];
             fieldBehavior.setEnabledListener(bEnable);
             
             fieldBehavior = (FieldListener)fieldBehavior.getNextListener();
             iIndex++;
         }
     }
     /**
      * Get the physical binary data from this field.
      * Behaviors are often used to initiate a complicated action only when the system asks for this data.
      * @return The raw data for this field.
      */
     public Object getData()
     {
         Object objData = null;
         FieldListener nextListener = (FieldListener)this.getNextValidListener(DBConstants.SCREEN_MOVE);   // Fix this
         if (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);      // Disable the listener to eliminate echos
             objData = nextListener.doGetData();
             nextListener.setEnabledListener(bOldState);       // Reenable
         }
         else
             objData = this.doGetData();
         return objData;
     }
     /**
      * Set a database object.
      * This object usually points to the database's Field object or FieldProperties Object.
      * @param dBObject The database object.
      */
     public void setDBObject(Object dBObject)
     {
         m_DBObject = dBObject;
     }
     /**
      * Get a database defined object.
      * This object usually points to the database's Field object or FieldProperties Object.
      * @return The database object.
      */
     public Object getDBObject()
     {
         return m_DBObject;
     }
     /**
      * Get this field's name.
      * @param bAddQuotes Add quotes if this field contains a space.
      * @param bIncludeFileName include the file name as file.field.
      * @return The field name.
      */
     public String getFieldName(boolean bAddQuotes, boolean bIncludeFileName)
     {
         if (!bAddQuotes) if (!bIncludeFileName)
             return super.getFieldName(bAddQuotes, bIncludeFileName);    // Return m_sFieldName
         String strFieldName = Constants.BLANK;
         if (bIncludeFileName)
             if (this.getRecord() != null)
         {
             strFieldName = this.getRecord().getTableNames(bAddQuotes);
             if (strFieldName.length() != 0)
                 strFieldName += ".";
         }
         strFieldName += Record.formatTableNames(m_strFieldName, bAddQuotes);
         return strFieldName;
     }
     /**
      * Get the field description (usually from the resource file).
      * @return The field desc.
      */
     public String getFieldDesc()
     {
         return super.getFieldDesc();
     }
     /**
      * Get the long field description for display in the status box.
      * @return The detailed field desc.
      */
     public String getFieldTip()
     {
         return super.getFieldTip();
     }
     /**
      * Get the HTML Input Type.
      * This is used by the html views.
      * @param strViewType The view type
      * @return the html type.
      */
     public String getInputType(String strViewType)
     {
         if (TopScreen.HTML_TYPE.equalsIgnoreCase(strViewType))
             return "text";
         else //if (TopScreen.XML_TYPE.equalsIgnoreCase(strViewType))
             return "textbox";
     }
     /**
      * Can this field be null?
      * @return true if this field can be null.
      */
     public boolean isNullable()
     {
         return m_bNullable;
     }
     /**
      * Can this field be null?
      * @param bNullable true if this field can be null.
      */
     public void setNullable(boolean bNullable)
     {
         m_bNullable = bNullable;
     }
     /**
      * Get this field's parent record.
      * @return The parent record.
      */
     public Record getRecord()
     {
         return (Record)super.getRecord();
     }
     /**
      * Get the SQL type of this field.
      * Typically used to get the SQL type for a create.
      * @param bIncludeLength Include the field length in this description.
      * @param properties Database properties to determine the SQL type.
      */
     public String getSQLType(boolean bIncludeLength, Map<String, Object> properties) 
     {
         return "CHAR";      // The default SQL Type
     }
     /**
      * Retrieve (in string format) from this field.
      * This method is used for SQL calls (ie., WHERE Id=5 AND Name="Don")
      * @return String the ' = "Don"' portion.
      * @param   strSeekSign The sign in the comparison.
      * @param   strCompare  Use the current field values or plug in a '?'
      * @param   bComparison true if this is a comparison, otherwise this is an assignment statement.
      */
     public String getSQLFilter(String strSeekSign, String strCompare, boolean bComparison) 
     {
         String strValue = null;
         String strSign = strSeekSign;
         if ((strSign == null) || (strSign.equals("==")))
             strSign = "=";
         FieldInfo field = this.getField();
         boolean bIsNull = true;
         if (field != null) if (!field.isNull())
             bIsNull = false;
         if (strCompare != null)
         {
             strValue = strCompare;
             bIsNull = false;
         }
         if (bIsNull)
             if (bComparison)
                 if ((!DBConstants.EQUALS.equals(strSign)) && (!FileListener.NOT_EQUAL.equals(strSign)))
                     bIsNull = false;        // If comparison is not not =, and value is null, must do <= '' or <= 0
         if (bIsNull == false)
         {
             if (strValue == null)
                 strValue = this.getSQLString();
             if (!strValue.equals("?"))
                 strValue = BaseField.addQuotes(strValue, this.getSQLQuote(true), this.getSQLQuote(false));  // Don't do this! Override and add quotes!
         }
         else
         { // Special logic to handle nulls
             if (bComparison)
             {
                 if (FileListener.NOT_EQUAL.equals(strSign))
                     strSign = " IS NOT";
                 else
                     strSign = " IS";
             }
             strValue = " NULL";
         }
         String strFilter = strSign + strValue;
         return strFilter;
     }
     /**
      * Return the type of quote that goes around this SQL value.
      * @return char The quote character.
      * @param bStartQuote boolean True for starting quote/false for ending quote.
      */
     public char getSQLQuote(boolean bStartQuote )
     {
         return ' ';   // By default, don't use a quote
     }
     /**
      * Move the physical binary data to this SQL parameter row.
      * This is overidden to move the physical data type.
      * @param statement The SQL prepare statement.
      * @param iType the type of SQL statement.
      * @param iParamColumn The column in the prepared statement to set the data.
      * @exception SQLException From SQL calls.
      */
     public void getSQLFromField(PreparedStatement statement, int iType, int iParamColumn) throws SQLException
     {
         if (this.isNull())
         {
             if ((this.isNullable()) && (iType != DBConstants.SQL_SELECT_TYPE) && (iType != DBConstants.SQL_SEEK_TYPE))
                 statement.setNull(iParamColumn, Types.VARCHAR);
             else
                 statement.setString(iParamColumn, Constants.BLANK);     // Can't be null in the db          
         }
         else
         {
             String string = this.getString();
             boolean bUseBlob = false;
             if (string.length() >= 256)   // Access must use blob if >= 256 
                 if (DBConstants.TRUE.equals(this.getRecord().getTable().getDatabase().getProperty(SQLParams.USE_BLOB_ON_LARGE_STRINGS)))
                     bUseBlob = true;
             if (!bUseBlob)
             {
                 statement.setString(iParamColumn, string);
             }
             else
             {
                 byte rgBytes[] = string.getBytes();
                 ByteArrayInputStream ba = new ByteArrayInputStream(rgBytes);
                 statement.setAsciiStream(iParamColumn, ba, rgBytes.length);
             }
         }
     }
     /**
      * Move the physical binary data to this SQL parameter row.
      * This is overidden to move the physical data type.
      * @param resultset The resultset to get the SQL data from.
      * @param iColumn the column in the resultset that has my data.
      * @exception SQLException From SQL calls.
      */
     public void moveSQLToField(ResultSet resultset, int iColumn) throws SQLException
     {
         String strResult = resultset.getString(iColumn);
         if (resultset.wasNull())
             this.setString(Constants.BLANK, false, DBConstants.READ_MOVE);  // Null value
         else
             this.setString(strResult, false, DBConstants.READ_MOVE);
     }
     /**
      * Do I skip getting/putting this field into the SQL param list?
      * @param iType The type of SQL statement (UPDATE/INSERT/etc).
      * @return true To skip this param (ie., skip insert field if not modified).
      */
     public boolean getSkipSQLParam(int iType)
     {
         if (this.isSelected() == false)
             return true;
         if (iType == DBConstants.SQL_UPDATE_TYPE) if (!this.isModified())
             return true;    // Only update changed fields
         if ((iType == DBConstants.SQL_INSERT_TABLE_TYPE) || (iType == DBConstants.SQL_INSERT_VALUE_TYPE))
             if (!this.isModified()) if ((this.isNull()) && (this.isNullable()))
                 return true;    // Only add new or non-null fields
         if (this.isVirtual())
             return true;
         return false;   // Don't skip this
     }
     /**
      * Get this field in SQL format.
      * ie., number 1,234.56 -> 1234.56.
      * @return This field as a SQL string.
      */
     public String getSQLString()
     {
         return this.toString();     // By Default
     }
     /**
      * For binary fields, set the current state.
      * @param state The state to set this field.
      * @param bDisplayOption Display changed fields if true.
      * @param iMoveMode The move mode.
      * @return The error code (or NORMAL_RETURN).
      */
     public int setState(boolean state, boolean bDisplayOption, int iMoveMode)
     {
         String tempString = "N";
         if (state)
             tempString = "Y";
         return this.setString(tempString, bDisplayOption, iMoveMode); // Move value to this field
     }
     /**
      * For binary fields, return the current state.
      * @param True is this field is true.
      */
     public boolean getState()
     {
         String tempChar = this.getString();
         if (tempChar.length() == 0)
             tempChar = " ";
         if ((tempChar.charAt(0) == 'Y') || (tempChar.charAt(0) == 'y'))
             return true;
         else
             return false;
     }
     /**
      * A field changed, call the listener(s).
      * Also, for screen moves, notify the record that this field changed it (FIELD_CHANGED_TYPE).
      * @param bDisplayOption Display changed fields if true.
      * @param iMoveMode The move mode.
      * @return An error code (NORMAL if successful).
      */
     public int handleFieldChanged(boolean bDisplayOption, int iMoveMode)
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         FieldListener nextListener = (FieldListener)this.getNextValidListener(iMoveMode);
         while (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);      // Disable the listener to eliminate echos
             iErrorCode = nextListener.fieldChanged(bDisplayOption, iMoveMode);
             nextListener.setEnabledListener(bOldState);       // Reenable
             if (iErrorCode != DBConstants.NORMAL_RETURN)
                 return iErrorCode;
             nextListener = (FieldListener)nextListener.getNextValidListener(iMoveMode);
         }
         if (iMoveMode == DBConstants.SCREEN_MOVE)
             if (iErrorCode == DBConstants.NORMAL_RETURN)
                 if (this.getRecord() != null)
             iErrorCode = this.getRecord().handleRecordChange(this, DBConstants.FIELD_CHANGED_TYPE, bDisplayOption);   // Tell table that I'm getting changed (if not locked)
         return iErrorCode;
     }
     /**
      * Was this field just changed?
      * @return true if this changed.
      */
     public boolean isJustModified()
     {
         return m_bJustChanged;
     }
     /**
      * Are the data in these fields the same type?
      * @param field The field to check.
      * @return True if the raw data type is the same for both fields.
      */
     public boolean isSameType(FieldInfo field)
     { // Copy this data to a field  //Change this to lock the objects down first!
         boolean bSameType = false;
         if (this.getClass().getName().equals(field.getClass().getName()))
             bSameType = true;
         else
         {
             Object data = this.getData();
             Class<?> classData = this.getDataClass();
             if (data != null)
                 classData = data.getClass();
             Object fieldData = field.getData();
             Class<?> classField = field.getDataClass();
             if (fieldData != null)
                 classField = fieldData.getClass();
             if (classData.equals(classField))
                 bSameType = true;
         }
         return bSameType;
     }
     /**
      * Select/Deselect this field (usually for SQL queries).
      * @param bSelected Select this field if true.
      */
     public void setSelected(boolean bSelected)
     {
         m_bSelected = bSelected;
     }
     /**
      * Is this field selected (usually for SQL queries)?
      * @return true If this field is selected.
      */
     public boolean isSelected()
     {
         return m_bSelected;
     }
     /**
      * Set this field to virtual.
      * A virtual field will not be read or written to a file, listeners set this type of field.
      */
     public void setVirtual(boolean bVirtual)
     {
         m_bVirtual = bVirtual;
     }           // This field should not be written to disk
     /**
      * Is this a virtual field?
      * A virtual field will not be read or written to a file, listeners set this type of field.
      * @return true if this is a virtual field.
      */
     public boolean isVirtual()
     {
         return m_bVirtual;
     }
     /**
      * Move data to this field from another field.
      * @param field The source field.
      * @return The error code (or NORMAL_RETURN).
      */
     public int moveFieldToThis(FieldInfo field)
     { // Copy a field to this
         return this.moveFieldToThis(field, DBConstants.DISPLAY, DBConstants.SCREEN_MOVE);
     }
     /**
      * Move data to this field from another field.
      * If the data types are the same data is moved, otherwise a string conversion is done.
      * @param field The source field.
      * @param bDisplayOption If true, display changes.
      * @param iMoveMode The move mode.
      * @return The error code (or NORMAL_RETURN).
      */
     public int moveFieldToThis(FieldInfo field, boolean bDisplayOption, int iMoveMode)
     { // Copy a field to this
         if (this.isSameType(field))
         { // Same type, just move the info
             Object data = field.getData();
             return this.setData(data, bDisplayOption, iMoveMode);
         }
         else
         { // Different type... convert to text first
             String tempString = field.getString();
             return this.setString(tempString, bDisplayOption, iMoveMode);
         }
     }
     /**
      * Move the physical binary data to this field.
      * (Must be the same physical type... setText makes sure of that)
      * This is a little tricky. First, I call the behaviors (doSetData)
      * which actually moves the data. Then, I call the HandleFieldChange
      * listener for each field, except on a read move, where the HandleFieldChange
      * listener is called in the doValidRecord method because each field comes
      * in one at a time, and if a listener modifies or accesses
      * another field, the field may not have been moved from the db yet.
      * @param data The raw data to move to this field.
      * @param iDisplayOption If true, display the new field.
      * @param iMoveMove The move mode.
      * @return An error code (NORMAL_RETURN for success).
      */
     public int setData(Object data, boolean bDisplayOption, int iMoveMode)
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         FieldListener nextListener = (FieldListener)this.getNextValidListener(iMoveMode);
         Object dataOld = m_data;
         boolean m_bModifiedOld = m_bModified;
         if (nextListener != null)
         {
             boolean bOldState = nextListener.setEnabledListener(false);      // Disable the listener to eliminate echos
             iErrorCode = nextListener.doSetData(data, bDisplayOption, iMoveMode);
             nextListener.setEnabledListener(bOldState);       // Reenable
         }
         else
             iErrorCode = this.doSetData(data, bDisplayOption, iMoveMode);
         if (iErrorCode == DBConstants.NORMAL_RETURN) if (m_bJustChanged)
         {
             iErrorCode = this.handleFieldChanged(bDisplayOption, iMoveMode);
             if (iErrorCode != DBConstants.NORMAL_RETURN)
             {   // Revert the data to old
                 if (dataOld != m_data)
                 {   // Always
                     if ((dataOld == data) ||
                         ((dataOld != null) && (dataOld.equals(data))))   // Make sure behaviors aren't causing change
                             iErrorCode = DBConstants.NORMAL_RETURN;     // If changes soely due to a behavior would cause an error, don't return an error
                     m_data = dataOld;
                     m_bModified = m_bModifiedOld;
                     m_bJustChanged = false;
                 }
             }
         }
         if (bDisplayOption)         // Can't do only if (m_bJustChanged) (last value may not be on screen)
             this.displayField();
         return iErrorCode;
     }
     /**
      * This is a utility method to simplify setting a single field to the field's property.
      * @return The error code on set.
      */
     public int setSFieldToProperty()
     {
         int iErrorCode = DBConstants.NORMAL_RETURN;
         m_bJustChanged = false;
         for (int iComponent = 0; ; iComponent++)
         {
             ScreenField sField = (ScreenField)this.getComponent(iComponent);
             if (sField == null)
                 break;
             iErrorCode = sField.setSFieldToProperty(null, DBConstants.DISPLAY, DBConstants.READ_MOVE);
         }
         if (iErrorCode == DBConstants.NORMAL_RETURN)
             if (!this.isJustModified())
                 if (this.getComponent(0) != null)
         {
             ScreenComponent sField = this.getComponent(0);
             ComponentParent parentScreen = sField.getParentScreen();
             String strParam = this.getFieldName(false, true);
             String strParamValue = parentScreen.getProperty(strParam);
             if (strParamValue != null)
                 this.setString(strParamValue, DBConstants.DISPLAY, DBConstants.READ_MOVE);
         }
         return iErrorCode;
     }
     /**
      * Enable/Disable the associated control(s).
      * @param state If false, disable all this field's screen fields.
      */
     public void setEnabled(boolean bEnable)
     {
         if (m_vScreenField != null)
         {
             for (Enumeration<Object> e = m_vScreenField.elements() ; e.hasMoreElements() ;)
             {
                 ((ScreenComponent)e.nextElement()).setEnabled(bEnable);
             }
         }
     }
     /**
      * Set the minimum field length.
      * @param iMinumum string field length.
      */
     public void setMinimumLength(int iMinumumLength)
     {
         m_iMinLength = iMinumumLength;
     }
     /**
      * Set the dirty flag.
      * @param flag If true, this field is interpreted as modified.
      */
     public void setModified(boolean flag)
     {
         super.setModified(flag);
         m_bJustChanged = flag;
     }
     /**
      * Get this field to the maximum or minimum value.<p>
      * Override this to supply the correct max/min for this field type.
      * @param iAreaDesc END_SELECT_KEY means set to largest value, others mean smallest.
      */
     public void setToLimit(int iAreaDesc)   // Set this field to the largest or smallest value
     {
         // Override this!!
         String filler = Constants.BLANK;
         if (iAreaDesc == DBConstants.END_SELECT_KEY)
         {   // \376 is consistently the largest in the character set
             filler = StringField.HIGH_STRING; // Change the filler pointer
         }
         this.doSetData(filler, DBConstants.DONT_DISPLAY, DBConstants.SCREEN_MOVE);  // will fill with zeros
     }
     /**
      * Set up the default control for this field.
      *  @param  itsLocation     Location of this component on screen (ie., GridBagConstraint).
      *  @param  targetScreen    Where to place this component (ie., Parent screen or GridBagLayout).
      *  @param  iDisplayFieldDesc Display the label? (optional).
      *  @return   Return the component or ScreenField that is created for this field.
      */
     public ScreenField setupDefaultView(ScreenLocation itsLocation, BasePanel targetScreen, int iDisplayFieldDesc)  // Add this view to the list
     {
         return this.setupDefaultView(itsLocation, targetScreen, this, iDisplayFieldDesc);
     }
     /**
      * Set up the default control for this field.
      *  @param  itsLocation     Location of this component on screen (ie., GridBagConstraint).
      * @param  targetScreen    Where to place this component (ie., Parent screen or GridBagLayout).
      * @param  iDisplayFieldDesc Display the label? (optional).
      *  @return   Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupDefaultView(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String, Object> properties)   // Add this view to the list
     {
         return this.setupDefaultView((ScreenLocation)itsLocation, (BasePanel)targetScreen, (Converter)converter, iDisplayFieldDesc);
     }
     /**
      * Set up the default screen control for this field.
      * @param itsLocation Location of this component on screen (ie., GridBagConstraint).
      * @param targetScreen Where to place this component (ie., Parent screen or GridBagLayout).
      * @param converter The converter to set the screenfield to.
      * @param iDisplayFieldDesc Display the label? (optional).
      * @return Return the component or ScreenField that is created for this field.
      */
     public ScreenField setupDefaultView(ScreenLocation itsLocation, BasePanel targetScreen, Converter converter, int iDisplayFieldDesc)   // Add this view to the list
     {
         ScreenField screenField = null;
         if (converter.getMaxLength() <= ScreenConstants.kMaxEditLineChars)
             screenField = new SEditText(itsLocation, targetScreen, converter, iDisplayFieldDesc);
         else
         {
             if (targetScreen instanceof GridScreen)
                 screenField = new SEditText(itsLocation, targetScreen, converter, iDisplayFieldDesc);
             else
                 screenField = new STEView(itsLocation, targetScreen, this, iDisplayFieldDesc);
         }
         return screenField;
     }
     /**
      * Set up the default control for this field (using default params).
      * @param targetScreen  Where to place this component (ie., Parent screen or GridBagLayout).
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupFieldView(ComponentParent targetScreen)
     {
         return this.setupDefaultView(targetScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), targetScreen, ScreenConstants.DISPLAY_DESC);
     }
     /**
      * Add a popup for the table tied to this field.
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTablePopup(ScreenLoc itsLocation, ComponentParent targetScreen, int iDisplayFieldDesc, Rec record, boolean bIncludeBlankOption)
     {
         return this.setupTablePopup(itsLocation, targetScreen, this, iDisplayFieldDesc, record, DBConstants.MAIN_KEY_AREA, -1, bIncludeBlankOption, false);
     }
     /**
      * Add a popup for the table tied to this field.
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTablePopup(ScreenLoc itsLocation, ComponentParent targetScreen, int iDisplayFieldDesc, Rec record, int iDisplayFieldSeq, boolean bIncludeBlankOption)
     {
         return this.setupTablePopup(itsLocation, targetScreen, this, iDisplayFieldDesc, record, DBConstants.MAIN_KEY_AREA, iDisplayFieldSeq, bIncludeBlankOption, false);
     }
     /**
      * Add a popup for the table tied to this field.
      * Key must be the first and primary and only key.
      * @param record Record to display in a popup
      * @param iQueryKeySeq Order to display the record (-1 = Primary field)
      * @param iDisplayFieldSeq Description field for the popup (-1 = second field)
      * @param bIncludeBlankOption Include a blank option in the popup?
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTablePopup(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Rec record, int iQueryKeySeq, int iDisplayFieldSeq, boolean bIncludeBlankOption, boolean bIncludeFormButton)
     {
         if ((!(this instanceof ReferenceField)) && (!(this instanceof CounterField)))
             Debug.doAssert(false);    // error, wrong field type
         ((Record)record).setKeyArea(iQueryKeySeq);
         ((Record)record).close();
         if (converter == null)
             converter = this;
         if (iDisplayFieldSeq == -1)
             iDisplayFieldSeq = ((Record)record).getDefaultDisplayFieldSeq();
         FieldConverter convert = new QueryConverter((Converter)converter, (Record)record, iDisplayFieldSeq, bIncludeBlankOption);
         ScreenComponent screenField = createScreenComponent(ScreenModel.POPUP_BOX, itsLocation, targetScreen, convert, iDisplayFieldDesc, null);
         if (bIncludeFormButton)
             if (!(targetScreen instanceof BaseGridScreen))
                 new SCannedBox((ScreenLocation)targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), (BasePanel)targetScreen, (Converter)converter, ThinMenuConstants.FORM, ScreenConstants.DONT_DISPLAY_FIELD_DESC, (Record)record);
         ((Record)record).selectScreenFields();    // Only select fields that you will display
         return screenField;
     }
     /**
      * Same as setupTablePopup for larger files (that don't fit in a popup).
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTableLookup(ScreenLoc itsLocation, ComponentParent targetScreen, int iDisplayFieldDesc, Rec record, int iQueryKeySeq, int iDisplayFieldSeq, boolean bIncludeFormButton)
     {
         return this.setupTableLookup(itsLocation, targetScreen, this, iDisplayFieldDesc, record, iQueryKeySeq, iDisplayFieldSeq, true, bIncludeFormButton);
     }
     /**
      * Same as setupTablePopup for larger files (that don't fit in a popup).
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTableLookup(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Rec record, int iQueryKeySeq, int iDisplayFieldSeq, boolean bIncludeBlankOption, boolean bIncludeFormButton)
     {
         Converter fldDisplayFieldDesc = null;
         if (iDisplayFieldSeq == -1)
             iDisplayFieldSeq = ((Record)record).getDefaultDisplayFieldSeq();
         if (iDisplayFieldSeq >= DBConstants.MAIN_FIELD)
             fldDisplayFieldDesc = ((Record)record).getField(iDisplayFieldSeq);
         return this.setupTableLookup(itsLocation, targetScreen, converter, iDisplayFieldDesc, record, iQueryKeySeq, fldDisplayFieldDesc, bIncludeBlankOption, bIncludeFormButton);
     }
     /**
      * Same as setupTablePopup for larger files (that don't fit in a popup).
      * Displays a [Key to record (opt)] [Record Description] [Lookup button] [Form button (opt)]
      * @param record Record to display in a popup
      * @param iQueryKeySeq Key to use for code-lookup operation (-1 = None)
      * @param iDisplayFieldSeq Description field for the display field (-1 = third field)
      * @param bIncludeFormButton Include a form button (in addition to the lookup button)?
      * @return  Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupTableLookup(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Rec record, int iQueryKeySeq, Converter fldDisplayFieldDesc, boolean bIncludeBlankOption, boolean bIncludeFormButton)
     {
         ScreenComponent screenField = null;
         if ((!(this instanceof ReferenceField)) && (!(this instanceof CounterField)))
             Debug.doAssert(false);    // error, wrong field type
         Converter conv = null;
         if (iQueryKeySeq != -1)
         { // Set up the listener to read the record using the code key (optional)
             BaseField fldKey = ((Record)record).getKeyArea(iQueryKeySeq).getField(DBConstants.MAIN_KEY_FIELD);    // Code
             if (this.getRecord().getRecordOwner() instanceof BasePanel)
                 if (!((BasePanel)this.getRecord().getRecordOwner()).isPrintReport())
             {   // Only need the read behavior if this is an input field
                 ((Record)record).setKeyArea(iQueryKeySeq);
                 MainReadOnlyHandler behavior = new MainReadOnlyHandler(iQueryKeySeq);
                 fldKey.addListener(behavior);
             }
             if (iDisplayFieldDesc != ScreenConstants.DONT_DISPLAY_DESC)
                 conv = new FieldDescConverter(fldKey, (Converter)converter); // Use the description for this field
             else
                 conv = fldKey;
             screenField = (ScreenField)conv.setupDefaultView(itsLocation, targetScreen, iDisplayFieldDesc);
 //            screenField = new SEditText(itsLocation, targetScreen, conv, iDisplayFieldDesc);
             itsLocation = null;
             iDisplayFieldDesc = ScreenConstants.DONT_DISPLAY_DESC;      // Display it only once
         }
         // Set up to display the record description
         if (fldDisplayFieldDesc != null)
         {
             if ((conv == null) && (iDisplayFieldDesc != ScreenConstants.DONT_DISPLAY_DESC))
                 conv = new FieldDescConverter(fldDisplayFieldDesc, (Converter)converter);  // Use the description for this field
             else
                 conv = fldDisplayFieldDesc;
         }
         if (conv != null)
         {
             if (itsLocation == null)
                 itsLocation = targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR);
             ScreenField sfDesc = (ScreenField)conv.setupDefaultView(itsLocation, targetScreen, iDisplayFieldDesc);
 //            ScreenField sfDesc = new SEditText(itsLocation, targetScreen, conv, iDisplayFieldDesc);
             sfDesc.setEnabled(false);
         }
         // Add the lookup button and form (opt) button (Even though SSelectBoxes don't use converter, pass it, so field.enable(true), etc will work)
         new SSelectBox((ScreenLocation)targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), (BasePanel)targetScreen, (Converter)converter, ScreenConstants.DONT_DISPLAY_DESC, (Record)record);
         if (bIncludeFormButton)
             if (!(targetScreen instanceof BaseGridScreen))
                 new SCannedBox((ScreenLocation)targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), (BasePanel)targetScreen, (Converter)converter, ThinMenuConstants.FORM, ScreenConstants.DONT_DISPLAY_FIELD_DESC, (Record)record);
         if ((bIncludeBlankOption) || (iQueryKeySeq == -1))     // If there is no code field, the only way to blank a field is to click this button
             if (!(targetScreen instanceof BaseGridScreen))
                 new SCannedBox((ScreenLocation)targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), (BasePanel)targetScreen, (Converter)converter, ScreenModel.CLEAR, ScreenConstants.DONT_DISPLAY_FIELD_DESC, this);
 
 //x can't yet - what if someone wants access to a field.        record.selectScreenFields();    // Only select fields that you will display
 
         boolean bUpdateRecord = false;
         if ((record.getDatabaseType() & (DBConstants.REMOTE | DBConstants.USER_DATA)) == (DBConstants.REMOTE | DBConstants.USER_DATA))
             bUpdateRecord = true;
         //  Set up the listener to read the current record on a valid main record
         ReadSecondaryHandler behavior = new ReadSecondaryHandler((Record)record, DBConstants.MAIN_FIELD, DBConstants.CLOSE_ON_FREE, bUpdateRecord, bIncludeBlankOption);
         this.addListener(behavior);
         if (((Record)record).getRecordOwner() != targetScreen)
             if (this.getRecord().getRecordOwner() != targetScreen)
                 if (this.getRecord().getRecordOwner() != targetScreen.getParentScreen())
                     if (targetScreen.getMainRecord() != null)
                         ((Record)targetScreen.getMainRecord()).addListener(new FileRemoveBOnCloseHandler(behavior)); // Being very careful (remove this behavior when screen closes)
 
         return screenField;
     }
     /**
      * Create a screen component of this type.
      * @param componentType
      * @param itsLocation
      * @param targetScreen
      * @param convert
      * @param iDisplayFieldDesc
      * @param properties
      * @return
      */
     public static ScreenComponent createScreenComponent(String componentType, ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
        String screenFieldClass = ScreenModel.BASE_PACKAGE + componentType;
         ScreenComponent screenField = (ScreenComponent)ClassServiceUtility.getClassService().makeObjectFromClassName(screenFieldClass);
         if (screenField == null)
         {
             Utility.getLogger().warning("Screen component not found " + componentType);
             screenField = (ScreenComponent)ClassServiceUtility.getClassService().makeObjectFromClassName(ScreenModel.BASE_PACKAGE + ScreenModel.EDIT_TEXT);
         }
         screenField.init(itsLocation, targetScreen, converter, iDisplayFieldDesc, properties);
         return screenField;
     }
     /**
      * Read the physical data from a stream file and set this field.
      * @param daIn Input stream to read this field's data from.
      * @param bFixedLength If false (default) be sure to save the length in the stream.
      * @return boolean Success?
      */
     public boolean read(DataInputStream daIn, boolean bFixedLength) // Fixed length = false
     {
         try   {
             String string = null;
             if (bFixedLength)
             {   // HACK Change this to use the system's default byte to char encoding
                 byte[] byData = new byte[this.getMaxLength()];
                 daIn.readFully(byData, 0, this.getMaxLength());   // Get the bytes and convert to Char
                 int iCount;
                 for (iCount = 0; iCount < this.getMaxLength(); iCount++)
                 {
                     if (byData[iCount] == 0)
                         break;
                 }
                 string = new String(byData, 0, iCount, "8859_1");
             }
             else
                 string = daIn.readUTF();
             if (string == null)
                 return false;
             int errorCode = this.setString(string, DBConstants.DONT_DISPLAY, DBConstants.READ_MOVE);
             return (errorCode == DBConstants.NORMAL_RETURN);    // Success
         } catch (IOException ex)    {
             ex.printStackTrace();
             return false;
         }
     }
     /**
      * Write the physical data in this field to a stream file.
      * @param daOut Output stream to add this field to.
      * @param bFixedLength If false (default) be sure to get the length from the stream.
      * @return boolean Success?
      */
     public boolean write(DataOutputStream daOut, boolean bFixedLength)
     {
         try   {
             String string = this.toString();
             if (bFixedLength)
             { // Write out exactly this.getMaxLength() bytes
                 daOut.writeBytes(string);
                 for (int i = string.length(); i < this.getMaxLength(); i++)
                 {   // Zero delimited
                     daOut.writeByte((byte)0);
                 }
             }
             else
                 daOut.writeUTF(string);
             return true;
         } catch (IOException ex)    {
             ex.printStackTrace();
             return false;
         }
     }
 }
