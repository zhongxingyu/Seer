 /**
  * @(#)Vendor.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.acctpay.db;
 
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
 import org.jbundle.main.db.*;
 import org.jbundle.base.message.core.trx.*;
 import org.jbundle.thin.base.message.*;
 import com.tourapp.tour.base.db.*;
 import org.jbundle.main.msg.db.*;
 import com.tourapp.tour.genled.db.*;
 import com.tourapp.model.tour.acctpay.db.*;
 
 /**
  *  Vendor - Vendors.
  */
 public class Vendor extends Company
      implements VendorModel, MessageDetailTarget
 {
     private static final long serialVersionUID = 1L;
 
     protected ApControl m_recApControl = null;
    public static final int VENDOR_DETAIL_MODE = ScreenConstants.DETAIL_MODE | 64;
    public static final int BROKER_DETAIL_MODE = ScreenConstants.DETAIL_MODE | 128;
    public static final int MESSAGE_DETAIL_MODE = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 2;
    public static final int MESSAGE_LOG_MODE = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 4;
     /**
      * Default constructor.
      */
     public Vendor()
     {
         super();
     }
     /**
      * Constructor.
      */
     public Vendor(RecordOwner screen)
     {
         this();
         this.init(screen);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwner screen)
     {
         m_recApControl = null;
         super.init(screen);
     }
     /**
      * Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? Record.formatTableNames(VENDOR_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Vendor";
     }
     /**
      * Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "acctpay";
     }
     /**
      * Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return DBConstants.REMOTE | DBConstants.USER_DATA;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & Vendor.BROKER_DETAIL_MODE) == Vendor.BROKER_DETAIL_MODE)
             screen = Record.makeNewScreen(BROKER_DIST_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Vendor.MESSAGE_DETAIL_MODE) == Vendor.MESSAGE_DETAIL_MODE)
             screen = Record.makeNewScreen(MessageDetail.MESSAGE_DETAIL_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Vendor.MESSAGE_LOG_MODE) == Vendor.MESSAGE_LOG_MODE)
             screen = Record.makeNewScreen(MessageLog.MESSAGE_LOG_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.DOC_MODE_MASK) == Vendor.VENDOR_DETAIL_MODE)
             screen = Record.makeNewScreen(ApTrx.VENDOR_AP_TRX_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(VENDOR_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = Record.makeNewScreen(VENDOR_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         return screen;
     }
     /**
      * Add this field in the Record's field sequence.
      */
     public BaseField setupField(int iFieldSeq)
     {
         BaseField field = null;
         if (iFieldSeq == 0)
         {
             field = new CounterField(this, ID, 8, null, null);
             field.setHidden(true);
         }
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
             field = new StringField(this, CODE, 16, null, null);
         if (iFieldSeq == 4)
             field = new StringField(this, VENDOR_NAME, 30, null, null);
         //if (iFieldSeq == 5)
         //  field = new StringField(this, ADDRESS_LINE_1, 40, null, null);
         //if (iFieldSeq == 6)
         //  field = new StringField(this, ADDRESS_LINE_2, 40, null, null);
         //if (iFieldSeq == 7)
         //  field = new StringField(this, CITY_OR_TOWN, 15, null, null);
         //if (iFieldSeq == 8)
         //  field = new StringField(this, STATE_OR_REGION, 15, null, null);
         //if (iFieldSeq == 9)
         //  field = new StringField(this, POSTAL_CODE, 10, null, null);
         //if (iFieldSeq == 10)
         //  field = new StringField(this, COUNTRY, 15, null, null);
         //if (iFieldSeq == 11)
         //  field = new PhoneField(this, TEL, 24, null, null);
         //if (iFieldSeq == 12)
         //  field = new FaxField(this, FAX, 24, null, null);
         //if (iFieldSeq == 13)
         //  field = new EMailField(this, EMAIL, 40, null, null);
         //if (iFieldSeq == 14)
         //  field = new URLField(this, WEB, 60, null, null);
         //if (iFieldSeq == 15)
         //  field = new Vendor_DateEntered(this, DATE_ENTERED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 16)
         //  field = new DateField(this, DATE_CHANGED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 17)
         //  field = new ReferenceField(this, CHANGED_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 18)
         //  field = new MemoField(this, COMMENTS, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 19)
         //  field = new UserField(this, USER_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 20)
         //  field = new StringField(this, PASSWORD, 16, null, null);
         //if (iFieldSeq == 21)
         //  field = new StringField(this, NAME_SORT, 6, null, null);
         //if (iFieldSeq == 22)
         //  field = new StringField(this, POSTAL_CODE_SORT, 5, null, null);
         //if (iFieldSeq == 23)
         //  field = new StringField(this, CONTACT, 30, null, null);
         if (iFieldSeq == 24)
             field = new StringField(this, CONTACT_TITLE, 30, null, null);
         if (iFieldSeq == 25)
         {
             field = new CountryField(this, COUNTRY_ID, 2, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 26)
         {
             field = new CurrencysField(this, CURRENCYS_ID, 3, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 27)
             field = new MessageTransportSelect(this, MESSAGE_TRANSPORT_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 28)
         {
             field = new StringField(this, MESSAGE_SERVER, 255, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 29)
         {
             field = new StringField(this, WSDL_PATH, 255, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 30)
             field = new PropertiesField(this, PROPERTIES, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 31)
             field = new VendorStatusField(this, VENDOR_STATUS_ID, 1, null, null);
         if (iFieldSeq == 32)
             field = new PaymentCycleField(this, PAYMENT_CYCLE_ID, 1, null, null);
         if (iFieldSeq == 33)
         {
             field = new PaymentCodeField(this, PAYMENT_CODE_ID, 1, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 34)
         {
             field = new PrepayTypeField(this, PREPAY_TYPE_ID, 1, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 35)
             field = new DepositField(this, DEPOSIT_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 36)
             field = new OperationTypeField(this, OPERATION_TYPE_CODE, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 37)
         {
             field = new AccountField(this, DEFAULT_ACCOUNT_ID, 7, null, null);
             field.addListener(new InitOnceFieldHandler(null));
             field.setMinimumLength(3);
         }
         if (iFieldSeq == 38)
         {
             field = new BooleanField(this, SEND_1099, 1, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 39)
         {
             field = new StringField(this, TAX_ID, 11, null, "");
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 40)
         {
             field = new StringField(this, VENDORS_CUST_NO, 20, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 41)
         {
             field = new FullCurrencyField(this, AMOUNT_SELECTED, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 42)
         {
             field = new FullCurrencyField(this, VENDOR_BALANCE, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
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
             keyArea = this.makeIndex(DBConstants.UNIQUE, "ID");
             keyArea.addKeyField(ID, DBConstants.ASCENDING);
         }
         if (iKeyArea == 1)
         {
             keyArea = this.makeIndex(DBConstants.SECONDARY_KEY, "Code");
             keyArea.addKeyField(CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 2)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "NameSort");
             keyArea.addKeyField(NAME_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 3)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "CurrencysID");
             keyArea.addKeyField(CURRENCYS_ID, DBConstants.ASCENDING);
         }
         if (keyArea == null)
             keyArea = super.setupKey(iKeyArea);     
         return keyArea;
     }
     /**
      * Free Method.
      */
     public void free()
     {
         m_recApControl = null;
         super.free();
     }
     /**
      * AddPropertyListeners Method.
      */
     public void addPropertyListeners()
     {
         BaseField fldProperties = this.getField(Vendor.PROPERTIES);
         BaseField fldDisplay = this.getField(Vendor.MESSAGE_SERVER);
         FieldListener listener = new CopyConvertersHandler(new PropertiesConverter(fldProperties, TrxMessageHeader.DESTINATION_MESSAGE_PARAM));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, false);
         listener.setRespondsToMode(DBConstants.READ_MOVE, false);
         fldDisplay.addListener(listener);
         listener = new CopyConvertersHandler(fldDisplay, new PropertiesConverter(fldProperties, TrxMessageHeader.DESTINATION_MESSAGE_PARAM));
         fldProperties.addListener(listener);
         
         fldDisplay = this.getField(Vendor.WSDL_PATH);
         listener = new CopyConvertersHandler(new PropertiesConverter(fldProperties, TrxMessageHeader.WSDL_PATH));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, false);
         listener.setRespondsToMode(DBConstants.READ_MOVE, false);
         fldDisplay.addListener(listener);
         listener = new CopyConvertersHandler(fldDisplay, new PropertiesConverter(fldProperties, TrxMessageHeader.WSDL_PATH));
         fldProperties.addListener(listener);
     }
     /**
      * Add the behaviors to calculate the "Amount Selected" and Balance field.
      */
     public ApTrx addSelectBehaviors()
     {
         RecordOwner recordOwner = this.findRecordOwner();
         ApTrx recApTrx2 = new ApTrx(recordOwner);      // Don't add second copy to screen
         if (recordOwner != null)
             recordOwner.removeRecord(recApTrx2);
         this.addListener(new FreeOnFreeHandler(recApTrx2));  // ...but be sure to free it
         recApTrx2.addListener(new SubFileFilter(this));
         this.addListener(new RecountOnValidHandler(recApTrx2));
         
         BooleanField fldTrue = new BooleanField(null, "True", 1, null, new Boolean(true));
         recApTrx2.addListener(new FreeOnFreeHandler(fldTrue));
         recApTrx2.addListener(new CompareFileFilter(ApTrx.ACTIVE_TRX, fldTrue, "=", fldTrue, true));
         
         recApTrx2.addListener(new SubCountHandler(this.getField(Vendor.AMOUNT_SELECTED), ApTrx.AMOUNT_SELECTED, true, true));
         recApTrx2.addListener(new SubCountHandler(this.getField(Vendor.VENDOR_BALANCE), ApTrx.INVOICE_BALANCE, true, true));
         
         return recApTrx2;
     }
     /**
      * Convert the command to the screen document type.
      * @param strCommand The command text.
      * @param The standard document type (MAINT/DISPLAY/SELECT/MENU/etc).
      */
     public int commandToDocType(String strCommand)
     {
         if (Vendor.MESSAGE_DETAIL_SCREEN.equalsIgnoreCase(strCommand))
             return Vendor.MESSAGE_DETAIL_MODE;
         if (Vendor.MESSAGE_LOG_SCREEN.equalsIgnoreCase(strCommand))
             return Vendor.MESSAGE_LOG_MODE;
         return super.commandToDocType(strCommand);
     }
     /**
      * GetNextMessageDetailTarget Method.
      */
     public MessageDetailTarget getNextMessageDetailTarget()
     {
         if (m_recApControl == null)
         {
             RecordOwner recordOwner = this.findRecordOwner();
             m_recApControl = new ApControl(recordOwner);
             if (recordOwner != null)
                 recordOwner.removeRecord(m_recApControl);
             this.addListener(new FreeOnFreeHandler(m_recApControl));
         }
         return m_recApControl;
     }
     /**
      * Get the message properties for this vendor.
      * @param strMessageName The message name.
      * @return A map with the message properties.
      */
     public TrxMessageHeader addMessageProperties(TrxMessageHeader trxMessageHeader)
     {
         return trxMessageHeader;
     }
     /**
      * Add the information from this record to this message header.
      */
     public MessageTransport getMessageTransport(TrxMessageHeader trxMessageHeader)
     {
         return (MessageTransport)((ReferenceField)this.getField(Vendor.MESSAGE_TRANSPORT_ID)).getReference();
     }
     /**
      * Add the destination information of this person to the message.
      */
     public TrxMessageHeader addDestInfo(TrxMessageHeader trxMessageHeader)
     {
         String strMessageTransport = (String)trxMessageHeader.get(MessageTransport.SEND_MESSAGE_BY_PARAM);
         
         if (((MessageTransport.SOAP.equalsIgnoreCase(strMessageTransport)) || (MessageTransport.XML.equalsIgnoreCase(strMessageTransport)))
             && (trxMessageHeader.get(TrxMessageHeader.DESTINATION_PARAM) == null))
         {
             String strDestination = ((PropertiesField)this.getField(Vendor.PROPERTIES)).getProperty(TrxMessageHeader.DESTINATION_MESSAGE_PARAM);
             if (strDestination != null)
             {
                 if (strDestination.startsWith("/"))
                     if (!this.getField(Person.WEB).isNull())
                         strDestination = this.getField(Person.WEB).toString() + strDestination;
                 trxMessageHeader.put(TrxMessageHeader.DESTINATION_PARAM, strDestination);
             }
             else
                 trxMessageHeader.put(TrxMessageHeader.DESTINATION_PARAM, this.getField(Person.WEB).toString());
         }
         
         trxMessageHeader = super.addDestInfo(trxMessageHeader);
         
         return trxMessageHeader;
     }
     /**
      * SetProperty Method.
      */
     public boolean setProperty(String strKey, String strProperty)
     {
         if (TrxMessageHeader.DESTINATION_PARAM.equalsIgnoreCase(strKey))
             this.getField(Vendor.WEB).setString(strProperty);
         else
             ((PropertiesField)this.getField(Vendor.PROPERTIES)).setProperty(strKey, strProperty);
         return true;
     }
     /**
      * Get this property.
      */
     public String getProperty(String strKey)
     {
         if (TrxMessageHeader.DESTINATION_PARAM.equalsIgnoreCase(strKey))
             return this.getField(Vendor.WEB).toString();
         else
             return ((PropertiesField)this.getField(Vendor.PROPERTIES)).getProperty(strKey);
     }
 
 }
