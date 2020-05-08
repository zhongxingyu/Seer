 /**
  * @(#)Product.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.product.base.db;
 
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
 import com.tourapp.tour.base.db.*;
 import java.util.*;
 import com.tourapp.tour.product.hotel.db.*;
 import com.tourapp.tour.acctpay.db.*;
 import com.tourapp.tour.product.land.db.*;
 import org.jbundle.thin.base.message.*;
 import com.tourapp.thin.app.booking.entry.search.*;
 import org.jbundle.main.msg.db.*;
 import org.jbundle.base.message.core.trx.*;
 import com.tourapp.tour.message.base.response.*;
 import java.text.*;
 import com.tourapp.tour.product.tour.db.*;
 import com.tourapp.tour.profile.db.*;
 import com.tourapp.tour.product.trans.db.*;
 import com.tourapp.tour.product.car.db.*;
 import com.tourapp.tour.product.cruise.db.*;
 import com.tourapp.tour.product.item.db.*;
 import com.tourapp.tour.message.base.request.*;
 import com.tourapp.tour.message.base.request.data.*;
 import com.tourapp.tour.message.base.response.data.*;
 import com.tourapp.tour.product.air.db.*;
 import org.jbundle.main.db.base.*;
 import com.tourapp.tour.product.base.event.*;
 import com.tourapp.model.tour.booking.db.*;
 import com.tourapp.model.tour.booking.detail.db.*;
 import com.tourapp.model.tour.booking.inventory.db.*;
 import org.jbundle.model.message.*;
 import com.tourapp.model.tour.product.base.db.*;
 
 /**
  *  Product - Product.
  */
 public class Product extends VirtualRecord
      implements ProductModel, MessageDetailTarget
 {
     private static final long serialVersionUID = 1L;
 
    public static final int PRICING_GRID_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 32;
     public static final int INVENTORY_GRID_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 2;
     public static final int INVENTORY_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 4;
     public static final int RANGE_ADJUST_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 16;
     public static final int BOOKING_DETAIL_GRID_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 128;
     public static final int PRODUCT_SEARCH_DETAIL_GRID_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 256;
     public static final int MESSAGE_DETAIL_MODE = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 64;
     protected ProductPricing m_recProductPricing = null;
     protected ProductTerms m_recProductTerms = null;
     protected InventoryModel m_recInventory = null;
     protected MessageProcessInfo m_recMessageProcessInfo = null;
     /**
      * Default constructor.
      */
     public Product()
     {
         super();
     }
     /**
      * Constructor.
      */
     public Product(RecordOwner screen)
     {
         this();
         this.init(screen);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwner screen)
     {
         m_recProductPricing = null;
         m_recProductTerms = null;
         m_recInventory = null;
         m_recMessageProcessInfo = null;
         super.init(screen);
     }
     /**
      * Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? Record.formatTableNames(PRODUCT_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Product";
     }
     /**
      * Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "product";
     }
     /**
      * Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return DBConstants.LOCAL | DBConstants.USER_DATA;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & Product.MESSAGE_DETAIL_MODE) == Product.MESSAGE_DETAIL_MODE)
             screen = Record.makeNewScreen(MessageDetail.MESSAGE_DETAIL_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Product.BOOKING_DETAIL_GRID_SCREEN) == Product.BOOKING_DETAIL_GRID_SCREEN)
             screen = Record.makeNewScreen(BookingModel.PRODUCT_BOOKING_DETAIL_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Product.PRODUCT_SEARCH_DETAIL_GRID_SCREEN) == Product.PRODUCT_SEARCH_DETAIL_GRID_SCREEN)
             screen = Record.makeNewScreen(PRODUCT_SEARCH_DETAIL_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
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
             field = new ProductDesc(this, DESCRIPTION, 50, null, null);
         if (iFieldSeq == 4)
             field = new StringField(this, CODE, 10, null, null);
         if (iFieldSeq == 5)
         {
             field = new VendorField(this, VENDOR_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 6)
             field = new StringField(this, OPERATORS_CODE, 20, null, null);
         if (iFieldSeq == 7)
             field = new ProductChainField(this, PRODUCT_CHAIN_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 8)
             field = new CityField(this, CITY_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 9)
             field = new TimeField(this, ETD, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 10)
             field = new ShortField(this, ACK_DAYS, 2, null, null);
         if (iFieldSeq == 11)
             field = new MemoField(this, COMMENTS, 32767, null, null);
         if (iFieldSeq == 12)
             field = new PropertiesField(this, PROPERTIES, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 13)
             field = new XmlField(this, ITINERARY_DESC, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 14)
             field = new ProductDescSort(this, DESC_SORT, 10, null, null);
         if (iFieldSeq == 15)
         {
             field = new ProductTypeAutoField(this, PRODUCT_TYPE, 15, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 16)
         {
             field = new FullCurrencyField(this, PRODUCT_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 17)
         {
             field = new CurrencyField(this, PRODUCT_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 18)
             field = new MessageTransportSelect(this, PRODUCT_MESSAGE_TRANSPORT_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 19)
         {
             field = new InventoryStatusField(this, DISPLAY_INVENTORY_STATUS_ID, Constants.DEFAULT_FIELD_LENGTH, null, new Integer(BaseStatus.NO_STATUS));
             field.setVirtual(true);
         }
         if (iFieldSeq == 20)
         {
             field = new ShortField(this, INVENTORY_AVAILABILITY, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 21)
         {
             field = new StringField(this, CURRENCY_CODE, 3, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 22)
         {
             field = new StringField(this, CURRENCY_CODE_LOCAL, 3, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 23)
         {
             field = new StringField(this, VENDOR_NAME, 30, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 24)
         {
             field = new CostStatusField(this, DISPLAY_COST_STATUS_ID, Constants.DEFAULT_FIELD_LENGTH, null, new Integer(BaseStatus.NULL_STATUS));
             field.setVirtual(true);
         }
         if (iFieldSeq == 25)
         {
             field = new FullCurrencyField(this, PP_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 26)
         {
             field = new CurrencyField(this, PP_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 27)
         {
             field = new BaseRateField(this, RATE_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 28)
         {
             field = new BaseClassField(this, CLASS_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 29)
         {
             field = new CurrencyField(this, PRODUCT_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 30)
         {
             field = new CurrencyField(this, PP_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
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
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "DescSort");
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 3)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "VendorID");
             keyArea.addKeyField(VENDOR_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 4)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "CityID");
             keyArea.addKeyField(CITY_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 5)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "OperatorsCode");
             keyArea.addKeyField(OPERATORS_CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 6)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "ProductChainID");
             keyArea.addKeyField(PRODUCT_CHAIN_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
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
         if (m_recProductPricing != null)
             m_recProductPricing.free();
         m_recProductPricing = null;
         if (m_recProductTerms != null)
             m_recProductTerms.free();
         m_recProductTerms = null;
         if (m_recInventory != null)
             m_recInventory.free();
         m_recInventory = null;
         if (m_recMessageProcessInfo != null)
             m_recMessageProcessInfo.free();
         m_recMessageProcessInfo = null;
         
         super.free();
     }
     /**
      * AddMasterListeners Method.
      */
     public void addMasterListeners()
     {
         super.addMasterListeners();
         BaseField productDesc = this.getField(Product.DESCRIPTION);
         BaseField productSort = this.getField(Product.DESC_SORT);
         CopyFieldHandler copyField = new CopyFieldHandler(Product.DESC_SORT);
         productDesc.addListener(copyField);
         FieldToUpperHandler toUpper = new FieldToUpperHandler(null);
         productSort.addListener(toUpper);
         CheckForTheHandler checkThe = new CheckForTheHandler(null);
         productSort.addListener(checkThe);
         this.getField(Product.PRODUCT_TYPE).addListener(new GetProductDescHandler(null));
     }
     /**
      * Convert the command to the screen document type.
      * @param strCommand The command text.
      * @param The standard document type (MAINT/DISPLAY/SELECT/MENU/etc).
      */
     public int commandToDocType(String strCommand)
     {
         if (Product.MESSAGE_DETAIL_SCREEN.equalsIgnoreCase(strCommand))
             return Product.MESSAGE_DETAIL_MODE;
         if (Product.PRODUCT_SEARCH_DETAIL.equalsIgnoreCase(strCommand))
             return Product.PRODUCT_SEARCH_DETAIL_GRID_SCREEN;
         return super.commandToDocType(strCommand);
     }
     /**
      * Get the physical message to do this task for this product type.
      * Note: Basically this method is here in case I want to add per-product
      * message definitions.
      * @param strMessageProcessorCode The code in the message info file.
      * @return The base physical message.
      */
     public TrxMessageHeader createProcessMessage(String strMessageProcessorCode, String strMessageTransport)
     {
         TrxMessageHeader trxMessageHeader = null;
         MessageProcessInfo recMessageProcessInfo = new MessageProcessInfo(this.findRecordOwner());
         MessageProcessInfo recMessageProcessInfoCurrent = (MessageProcessInfo)recMessageProcessInfo.getMessageProcessInfo(strMessageProcessorCode);
         if (recMessageProcessInfoCurrent != null)
             trxMessageHeader = recMessageProcessInfoCurrent.createProcessMessageHeader(this, strMessageTransport);
         recMessageProcessInfo.free();
         return trxMessageHeader;
     }
     /**
      * Get the physical message to do this task for this product type.
      * Note: Basically this method is here in case I want to add per-product
      * message definitions.
      * @param strMessageProcessorCode The code in the message info file.
      * @return The base physical message.
      */
     public TrxMessageHeader createProcessMessage(String strMessageInfoType, String strContactType, String strRequestType, String strMessageProcessType, String strProcessType, String strMessageTransport)
     {
         TrxMessageHeader trxMessageHeader = null;
         MessageProcessInfo recMessageProcessInfo = new MessageProcessInfo(this.findRecordOwner());
         MessageProcessInfo recMessageProcessInfoCurrent = (MessageProcessInfo)recMessageProcessInfo.getMessageProcessInfo(strMessageInfoType, strContactType, strRequestType, strMessageProcessType, strProcessType);
         if (recMessageProcessInfoCurrent != null)
             trxMessageHeader = recMessageProcessInfoCurrent.createProcessMessageHeader(this, strMessageTransport);
         recMessageProcessInfo.free();
         return trxMessageHeader;
     }
     /**
      * GetNextMessageDetailTarget Method.
      */
     public MessageDetailTarget getNextMessageDetailTarget()
     {
         if (this.getField(Hotel.VENDOR_ID).isNull())
             return null;
         return (Vendor)((ReferenceField)this.getField(Hotel.VENDOR_ID)).getReference();
     }
     /**
      * Get the message properties for this product.
      * @param strMessageName The message name.
      * @return A map with the message properties.
      */
     public TrxMessageHeader addMessageProperties(TrxMessageHeader trxMessageHeader)
     {
         return trxMessageHeader;
     }
     /**
      * GetMessageTransport Method.
      */
     public MessageTransport getMessageTransport(TrxMessageHeader trxMessageHeader)
     {
         return (MessageTransport)((ReferenceField)this.getField(Product.PRODUCT_MESSAGE_TRANSPORT_ID)).getReference();
     }
     /**
      * AddDestInfo Method.
      */
     public TrxMessageHeader addDestInfo(TrxMessageHeader trxMessageHeader)
     {
         return trxMessageHeader;
     }
     /**
      * Process this product information request (override this).
      */
     public Message processInfoRequestInMessage(Message messageIn, Message messageReply)
     {
         MessageRecordDesc productRequest = (ProductRequest)((BaseMessage)messageIn).getMessageDataDesc(null);
         MessageRecordDesc messageData = (ProductMessageData)productRequest.getMessageDataDesc(ProductRequest.PRODUCT_MESSAGE);
         
         BaseProductResponse responseMessage = null;
         if (messageReply == null)
             messageReply = (BaseMessage)this.getMessageProcessInfo().createReplyMessage((BaseMessage)messageData.getMessage());
         responseMessage = (BaseProductResponse)((BaseMessage)messageReply).getMessageDataDesc(null);
         responseMessage.moveRequestInfoToReply(messageIn);
         
         int iProductStatus = InventoryStatus.VALID;
         //?String strErrorMessage = null;
         
         //?this.getField(Product.DISPLAY_INFO_STATUS_ID).setValue(iProductStatus);
         responseMessage.setMessageDataStatus(iProductStatus);
         //?if (strErrorMessage != null)
         //?    responseMessage.setMessageDataError(strErrorMessage);
         return messageReply;
     }
     /**
      * Read the locally stored product cost (Override).
      */
     public Message processCostRequestInMessage(Message messageIn, Message messageReply)
     {
         return null;    // Unknown (Override this!)
     }
     /**
      * Check the inventory for this detail.
      * @param message Contains all the update data for this check
      * @param fldTrxID If null, just check the inventory, if not null, update the inventory using this BookingDetail trxID.
      */
     public Message processAvailabilityRequestInMessage(Message messageIn, Message messageReply, Field fldTrxID)
     {
         ProductRequest productRequest = (ProductRequest)((BaseMessage)messageIn).getMessageDataDesc(null);
         ProductMessageData messageData = (ProductMessageData)productRequest.getMessageDataDesc(ProductRequest.PRODUCT_MESSAGE);
         PassengerMessageData bookingMessageData = (PassengerMessageData)productRequest.getMessageDataDesc(ProductRequest.PASSENGER_MESSAGE);
         Date dateTarget = messageData.getTargetDate();
         int iTargetAmount = bookingMessageData.getTargetPax();
         if (productRequest instanceof CancelRequest)    // CancelRequest (then pax = 0)
             iTargetAmount = 0;
         if (RequestType.BOOKING_CANCEL.equalsIgnoreCase(productRequest.getRequestType()))
             iTargetAmount = 0;
         int iRateID = messageData.getRateTypeID();
         int iClassID = messageData.getRateClassID();
         Object objOtherID = messageData.get(Product.OTHER_ID_PARAM);
         if (objOtherID == null)
             objOtherID = InventoryModel.NO_OTHER;
         int iOtherID = Integer.parseInt(objOtherID.toString());
         
         BaseProductResponse responseMessage = null;
         if (messageReply == null)
             messageReply = (BaseMessage)this.getMessageProcessInfo().createReplyMessage((BaseMessage)messageData.getMessage());
         responseMessage = (BaseProductResponse)((BaseMessage)messageReply).getMessageDataDesc(null);
         responseMessage.moveRequestInfoToReply(messageIn);
         
         // First, calculate the room cost
         InventoryModel recInventory = this.getInventory().getAvailability(this, dateTarget, iRateID, iClassID, iOtherID);
         int iAvailability = InventoryModel.NO_INVENTORY;
         if (recInventory != null)
             iAvailability = (int)recInventory.getField(InventoryModel.AVAILABLE).getValue();
         if (responseMessage instanceof ProductAvailabilityResponse)
         {
             ProductResponseMessageData responseMessageData = (ProductResponseMessageData)responseMessage.getMessageDataDesc(ProductAvailabilityResponse.PRODUCT_RESPONSE_MESSAGE);
             responseMessageData.setAvailability(iAvailability);
         }
         
         int iInventoryStatus = InventoryStatus.VALID;
         String strErrorMessage = null;
         if (iAvailability < iTargetAmount)
         {
             strErrorMessage = InventoryStatus.NO_INVENTORY_ERROR_MESSAGE;
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getTask() != null)
             {
                 strErrorMessage = this.getRecordOwner().getTask().getApplication().getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(strErrorMessage);
                 strErrorMessage = MessageFormat.format(strErrorMessage, iAvailability, dateTarget);
             }
             iInventoryStatus = InventoryStatus.NOT_VALID;
         }
         else if (iAvailability == InventoryModel.NO_INVENTORY)
         {
             strErrorMessage = InventoryStatus.LOOKUP_ERROR_MESSAGE;
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getTask() != null)
                     strErrorMessage = this.getRecordOwner().getTask().getApplication().getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(strErrorMessage);
             iInventoryStatus = InventoryStatus.ERROR;
         }
         else
         { // Okay
             if (fldTrxID != null) // If in update mode, update the availability
             {
                 boolean bIsDeleted = false;   // todo (don) Fix this.
                 int iErrorCode = recInventory.updateAvailability(iTargetAmount, fldTrxID, 0, bIsDeleted, null);
                 if (iErrorCode != DBConstants.NORMAL_RETURN)
                 {
                     iInventoryStatus = BaseDataStatus.NOT_VALID;
                     if ((this.getRecordOwner() != null)
                         && (this.getRecordOwner().getTask() != null))
                             strErrorMessage = this.getRecordOwner().getTask().getLastError(iErrorCode);
                     else
                         strErrorMessage = "Inventory not available";
                 }
             }
                 
         }
         this.getField(Product.DISPLAY_INVENTORY_STATUS_ID).setValue(iInventoryStatus);
         responseMessage.setMessageDataStatus(iInventoryStatus);
         if (strErrorMessage != null)
             responseMessage.setMessageDataError(strErrorMessage);
         
         ProductResponseMessageData responseMessageData = (ProductResponseMessageData)responseMessage.getMessageDataDesc(ProductRateResponse.PRODUCT_RESPONSE_MESSAGE);
         responseMessageData.put(BookingDetailModel.PRODUCT_ID, messageData.get(BookingDetailModel.PRODUCT_ID));
         responseMessageData.setMessageDataStatus(iInventoryStatus);   // Status for this segment
         if (strErrorMessage != null)
             responseMessageData.setMessageDataError(strErrorMessage);
         
         return messageReply;
     }
     /**
      * This is for products that can be externally booked.
      * @return the booking reply message with the proper params.
      */
     public Message processBookingRequestInMessage(Message messageIn, Message messageReply)
     {
         ProductRequest productRequest = (ProductRequest)((BaseMessage)messageIn).getMessageDataDesc(null);
         RecordOwner recordOwner = this.findRecordOwner();
         ProductMessageData messageData = (ProductMessageData)productRequest.getMessageDataDesc(ProductRequest.PRODUCT_MESSAGE);
         ProductBookingResponse responseMessage = null;
         
         if (messageData.getProduct(this))
         {
             String strBookingNo = null;
             int iMessageStatus = BaseDataStatus.OKAY;
             String strErrorMessage = null;
         
             Date dateTarget = messageData.getTargetDate();
             DateField fldDepDate = new DateField(null, "TargetDate", DBConstants.DEFAULT_FIELD_LENGTH, DBConstants.BLANK, null);
             fldDepDate.setDate(dateTarget, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
             dateTarget = fldDepDate.getDateTime();  // Make sure module date matches departure date.
         
             if (messageReply == null)
             {
                 messageReply = new TreeMessage(null, null);
                 responseMessage = this.getProductBookingResponse(productRequest.getRequestType(), messageReply, null);
             }
             else
                 responseMessage = (ProductBookingResponse)((BaseMessage)messageReply).getMessageDataDesc(null);
             responseMessage.moveRequestInfoToReply(messageIn);
         
             String strMessageTransportID = null;
             if (productRequest.getMessage().getMessageHeader() != null)
                 strMessageTransportID = (String)productRequest.getMessage().getMessageHeader().get(MessageTransport.TRANSPORT_ID_PARAM);
             BookingDetailModel recBookingDetail = this.getBookingDetail(this.findRecordOwner());
             MessageTransport recMessageTransport = new MessageTransport(this.findRecordOwner());
             boolean bIsDirectTransport = true;
             if (strMessageTransportID != null)
                 if (recMessageTransport.getMessageTransport(strMessageTransportID) != null)
                     bIsDirectTransport = recMessageTransport.isDirectTransport();
             recMessageTransport.free();
             if (bIsDirectTransport)
             { // If direct DO NOT create a new booking (this detail is already part of a booking), Just reduce the inventory and return ok
                 iMessageStatus = BaseDataStatus.OKAY;
                 if (RequestType.BOOKING_CANCEL.equalsIgnoreCase(productRequest.getRequestType()))
                     iMessageStatus = BaseDataStatus.CANCELED;
             }
             else
             { 
                 // Now create a new booking from the data passed in
                 BookingModel recBooking = (BookingModel)Record.makeRecordFromClassName(BookingModel.THICK_CLASS, recordOwner);
                 TourModel recTour = (TourModel)Record.makeRecordFromClassName(TourModel.THICK_CLASS, recordOwner);
                 BookingControl recBookingControl = new BookingControl(recordOwner);
                 ProfileControl recProfileControl = new ProfileControl(recordOwner);
                 recBooking.addControlDefaults(recBookingControl, recProfileControl);
             
                 recBookingDetail.addDetailBehaviors(recBooking, recTour);
         
                 try {
                     if (RequestType.BOOKING_ADD.equalsIgnoreCase(productRequest.getRequestType()))
                     {
                         recBooking.getTable().addNew();
                 
                         TourHeader recTourHeader = this.getBookingTourHeader(recBookingControl);
                         recBooking.setupDefaultDesc(recTourHeader, fldDepDate);
                         int iErrorCode = recTour.setupTourFromHeader(recTourHeader, fldDepDate, recBooking.getField(BookingModel.CODE).toString(), recBooking.getField(BookingModel.DESCRIPTION).toString());
                         if (iErrorCode != DBConstants.NORMAL_RETURN)
                             return null;
                         ((BaseField)recBooking.getField(BookingModel.TOUR_ID)).addListener(new CalcBookingDatesHandler((Record)recTour, recTourHeader));
                         ((BaseField)recBooking.getField(BookingModel.TOUR_ID)).moveFieldToThis((BaseField)recTour.getField(TourModel.ID));
                 
                         recBooking.getTable().add(recBooking);
                         strBookingNo = recBooking.getTable().getLastModified(DBConstants.OBJECT_ID_HANDLE).toString();
                         recBooking.getTable().setHandle(strBookingNo, DBConstants.OBJECT_ID_HANDLE);
                         recBooking.getTable().edit();
                         
                         BookingAnswerModel recBookingAnswer = null;  // This causes addTourDetail to resolve the answers automatically
                         BookingPaxModel recBookingPax = null;
                         iErrorCode = ((BookingModel)recBooking).addTourDetail(recTour, recTourHeader, recBookingPax, recBookingAnswer, dateTarget, recBooking.getField(BookingModel.ASK_FOR_ANSWER));
                         if (iErrorCode != DBConstants.NORMAL_RETURN)
                             return null;
         
                         this.addMessageBookingDetail(recBookingDetail, recBooking, recTour, strMessageTransportID, productRequest);
         
                         recBooking.getField(BookingModel.BOOKING_STATUS_ID).setValue(BookingStatus.OKAY); // Make sure all downline components are ordered
                         recTour.refreshToCurrent(DBConstants.AFTER_UPDATE_TYPE, false);  // Make sure I have the latest copy
                         recTour.getField(TourModel.ORDER_COMPONENTS).setState(true);     // Book = Order Components! (just in case the previous line didn't do this)
                     }
                     else
                     {
                         strBookingNo = (String)messageData.get(BookingDetailModel.REMOTE_BOOKING_NO);
                         if ((strBookingNo == null) || (strBookingNo.length() == 0))
                         {   
                             iMessageStatus = BaseDataStatus.NOT_VALID;
                             strErrorMessage = "No booking number supplied";
                         }
                         else
                         {
                             recBooking.getField(BookingModel.ID).setString(strBookingNo);
                             int iOldKeyArea = recBooking.getDefaultOrder();
                             recBooking.setKeyArea(BookingModel.ID_KEY);
                             if (!recBooking.getTable().seek(null))
                             {
                                 iMessageStatus = BaseDataStatus.NOT_VALID;
                                 strErrorMessage = "Invalid booking number supplied";
                             }
                             else
                             {
                                 recTour = (TourModel)((ReferenceField)recBooking.getField(BookingModel.TOUR_ID)).getReference();
                                 if ((recTour == null) ||
                                         ((recTour.getEditMode() != DBConstants.EDIT_CURRENT) && (recTour.getEditMode() != DBConstants.EDIT_IN_PROGRESS)))
                                 {   // Never
                                     iMessageStatus = BaseDataStatus.NOT_VALID;
                                     strErrorMessage = "No tour associated with this booking";
                                 }
                             }
                             ((Record)recBooking).setKeyArea(iOldKeyArea);
                             
                             if (iMessageStatus == BaseDataStatus.OKAY)
                             {
                                 if (RequestType.BOOKING_CHANGE.equalsIgnoreCase(productRequest.getRequestType()))
                                 {
                                     this.changeMessageBookingDetail(recBookingDetail, recBooking, recTour, strMessageTransportID, productRequest);
                                 }
                                 else // if (RequestType.BOOKING_CANCEL.equalsIgnoreCase(message.getRequestType()))
                                 {
                                     int iCancelledStatusID = ((ReferenceField)recBooking.getField(BookingModel.BOOKING_STATUS_ID)).getIDFromCode(BookingStatus.CANCELLED_CODE);
                                     recBooking.getField(BookingModel.BOOKING_STATUS_ID).setValue(iCancelledStatusID);
                                 }
                             }
                         }
                     }
         
                     if (iMessageStatus == BaseDataStatus.OKAY)
                     {
                         // Return the confirmation number
                         ProductResponseMessageData responseMessageData = (ProductResponseMessageData)responseMessage.getMessageDataDesc(ProductRateResponse.PRODUCT_RESPONSE_MESSAGE);
                         recBookingDetail.refreshToCurrent(DBConstants.AFTER_UPDATE_TYPE, false);
                         responseMessageData.handlePutRawRecordData(recBookingDetail);
         
                         responseMessageData.setRemoteBookingNo(strBookingNo);
                         iMessageStatus = (int)recTour.getField(TourModel.TOUR_STATUS_ID).getValue();
                         long iTimeoutMS = (long)recBookingControl.getField(BookingControl.REMOTE_WAIT_TIME).getValue() * 1000;
                         if (BaseStatus.isWaiting(iMessageStatus))
                         {
                             // Setup tour status listener
                             WaitForFieldChangeHandler listener = new WaitForFieldChangeHandler(iTimeoutMS);
                             ((BaseField)recTour.getField(TourModel.TOUR_STATUS_ID)).addListener(listener);
                             recTour.refreshToCurrent(DBConstants.AFTER_UPDATE_TYPE, false); // Start with the most recent version
                             iMessageStatus = (int)recTour.getField(TourModel.TOUR_STATUS_ID).getValue();
                         // Wait for the status to change (or timeout)
                             int iErrorCode = DBConstants.NORMAL_RETURN;
                             while (BaseStatus.isWaiting(iMessageStatus))
                             {
                                 iErrorCode = listener.waitForChange();
                                 if (iErrorCode == WaitForFieldChangeHandler.TIMEOUT_ERROR)
                                     break;
                                 iMessageStatus = (int)recTour.getField(TourModel.TOUR_STATUS_ID).getValue();
                             }
                             if (BaseStatus.isWaiting(iMessageStatus))
                             {
                                 strErrorMessage = null;
                                 if (recBookingDetail != null)
                                     if ((recBookingDetail.getEditMode() == DBConstants.EDIT_CURRENT) || (recBookingDetail.getEditMode() == DBConstants.EDIT_IN_PROGRESS))
                                         strErrorMessage = recBookingDetail.getErrorMessage(BookingDetailModel.PRODUCT_STATUS_ID);
                                 if ((strErrorMessage == null) || (strErrorMessage.length() == 0))
                                     strErrorMessage = ((ReferenceField)recTour.getField(TourModel.TOUR_STATUS_ID)).getReference().getField(BaseDataStatus.DESCRIPTION).toString();
                             }
                         }
                         // Don't need to transfer the cost since you got it on the costing lookup
                         // todo(don) may want to transfer cost and line items for client comparison later
                         //msgBookingRateResponse.setProductCost(recBooking.getField(Booking.GROSS).getValue());
                         //msgBookingRateResponse.putRawLineItemData(recBooking);
                 
                         if (recTour.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
                             recTour.getTable().set(recTour);
                         if (recBooking.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
                             recBooking.getTable().set(recBooking);
                     }
                 } catch (DBException ex) {
                     ex.printStackTrace();
                 } finally {
                     recProfileControl.free();
                     recBookingControl.free();
                     recTour.free();
                     recBooking.free();
                     fldDepDate.free();
                 }
             }
             responseMessage.setMessageDataStatus(iMessageStatus);
             if (strErrorMessage != null)
                 responseMessage.setMessageDataError(strErrorMessage);
         
             ProductResponseMessageData responseMessageData = (ProductResponseMessageData)responseMessage.getMessageDataDesc(ProductRateResponse.PRODUCT_RESPONSE_MESSAGE);
             responseMessageData.put(BookingDetailModel.PRODUCT_ID, messageData.get(BookingDetailModel.PRODUCT_ID));
             responseMessageData.setMessageDataStatus(iMessageStatus);   // Status for this segment
             if (strErrorMessage != null)
                 responseMessageData.setMessageDataError(strErrorMessage);
             
             recBookingDetail.free();
         }
         return messageReply;
     }
     /**
      * Get the est. time of this product.
      * @return The duration of this product (in seconds).
      */
     public long getLengthTime()
     {
         return 0; // Override
     }
     /**
      * Add the booking detail that goes with the product in this message.
      */
     public int addMessageBookingDetail(BookingDetailModel recBookingDetail, BookingModel recBooking, TourModel recTour, String strMessageTransportID, MessageRecordDesc productRequest) throws DBException
     {
         //+ String strDestination = null;   // todo(don) Need to figure out where this message was sent to (what is my xyz [soap?] address?)
         
         // HACK: I only did direct transport while I was testing since this would cause an endless message loop
         //x recBookingDetail.addListener(new SetDirectTransportHandler(null));
         
         return productRequest.handleGetRawRecordData(recBookingDetail);
     }
     /**
      * ChangeMessageBookingDetail Method.
      */
     public int changeMessageBookingDetail(BookingDetailModel recBookingDetail, BookingModel recBooking, TourModel recTour, String strMessageTransportID, MessageRecordDesc productRequest) throws DBException
     {
         String strDestination = null;   // todo(don) Need to figure out where this message was sent to (what is my xyz [soap?] address?)
         
         // HACK: to make sure the local products were booked directly, otherwise I return a booking in progress to the remote client
         //x recBookingDetail.addListener(new SetDirectTransportHandler(null));
         
         return productRequest.handleGetRawRecordData(recBookingDetail);   // Add the booking detail
     }
     /**
      * Get the correct remote tour header for this product.
      */
     public TourHeader getBookingTourHeader(BookingControl recBookingControl)
     {
         return (TourHeader)((ReferenceField)recBookingControl.getField(BookingControl.REMOTE_TOUR_HEADER_ID)).getReference();
     }
     /**
      * GetProductBookingResponse Method.
      */
     public ProductBookingResponse getProductBookingResponse(String strRequestType, Message message, String strKey)
     {
         if (RequestType.BOOKING_CANCEL.equalsIgnoreCase(strRequestType))
             return new CancelResponse((BaseMessage)message, strKey);
         return null;    // Override this!
     }
     /**
      * Create the booking detail for this product type.
      */
     public BookingDetailModel getBookingDetail(RecordOwner recordOwner)
     {
         return null;    // Override this!
     }
     /**
      * Given this date, return the best Starting date and time. If none,
      * just return the date that was passed in.
      */
     public Date getStartDate(Date date)
     {
         Date dateEtd = ((TimeField)this.getField(Product.ETD)).getDateTime();
         if (dateEtd == null)
             return date;
         Calendar calendar = Converter.gCalendar;
         calendar.setTime(dateEtd);
         int iHour = calendar.get(Calendar.HOUR_OF_DAY);
         int iMinute = calendar.get(Calendar.MINUTE);
         int iSecond = calendar.get(Calendar.SECOND);
         int iMillisecond = calendar.get(Calendar.MILLISECOND);
         
         calendar.setTime(date);
         calendar.set(Calendar.HOUR_OF_DAY, iHour);
         calendar.set(Calendar.MINUTE, iMinute);
         calendar.set(Calendar.SECOND, iSecond);
         calendar.set(Calendar.MILLISECOND, iMillisecond);
         date = calendar.getTime();
         return date;
     }
     /**
      * Get the inventory record.
      */
     public InventoryModel getInventory()
     {
         if (m_recInventory == null)
             m_recInventory = (InventoryModel)Record.makeRecordFromClassName(InventoryModel.THICK_CLASS, this.findRecordOwner());
         return m_recInventory;
     }
     /**
      * GetProductPricing Method.
      */
     public ProductPricing getProductPricing()
     {
         if (m_recProductPricing == null)
         {
             m_recProductPricing = this.createProductPricing(this.findRecordOwner());
             if (m_recProductPricing != null)
                 if (m_recProductPricing.getRecordOwner() != null)
             {
                 m_recProductPricing.getRecordOwner().removeRecord(m_recProductPricing);
                 this.addListener(new FreeOnFreeHandler(m_recProductPricing));
             }
         }
         return m_recProductPricing;
     }
     /**
      * GetProductTerms Method.
      */
     public ProductTerms getProductTerms()
     {
         if (m_recProductTerms == null)
         {
             m_recProductTerms = new ProductTerms(this.findRecordOwner());
             if (m_recProductTerms.getRecordOwner() != null)
                 m_recProductTerms.getRecordOwner().removeRecord(m_recProductTerms);
             this.addListener(new FreeOnFreeHandler(m_recProductTerms));
         }
         return m_recProductTerms;
     }
     /**
      * GetMessageProcessInfo Method.
      */
     public MessageProcessInfo getMessageProcessInfo()
     {
         if (m_recMessageProcessInfo == null)
         {
             RecordOwner recordOwner = this.findRecordOwner();
             m_recMessageProcessInfo = new MessageProcessInfo(recordOwner);
             if (recordOwner != null)
                 recordOwner.removeRecord(m_recMessageProcessInfo);
         }
         return m_recMessageProcessInfo;
     }
     /**
      * Get this product's code in the vendor's system and fake it if it doesn't exist.
      */
     public String getOperatorsCode()
     {
         String strProductCode = null;
         if (!this.getField(Product.OPERATORS_CODE).isNull())
             strProductCode = this.getField(Product.OPERATORS_CODE).toString();
         else if (!this.getField(Product.CODE).isNull())
             strProductCode = this.getField(Product.CODE).toString();
         else
             strProductCode = this.getField(Product.DESCRIPTION).toString();    // Hopefully not.
         return strProductCode;
     }
     /**
      * Get the product vendor's chain code.
      */
     public String getChainCode()
     {
         String strChainCode = null;
         Record recProductChain = ((ReferenceField)this.getField(Product.PRODUCT_CHAIN_ID)).getReference();
         if (recProductChain != null)
             if ((recProductChain.getEditMode() == DBConstants.EDIT_CURRENT) || (recProductChain.getEditMode() == DBConstants.EDIT_IN_PROGRESS))
                 if (!recProductChain.getField(ProductChain.CODE).isNull())
                     strChainCode = recProductChain.getField(ProductChain.CODE).toString();
         if ((strChainCode == null) || (strChainCode.length() == 0))
             return this.getOperatorsCode();
         return strChainCode;
     }
     /**
      * CreateProductPricing Method.
      */
     public ProductPricing createProductPricing(RecordOwner recordOwner)
     {
         return null;    // Override this!
     }
     /**
      * Using this booking detail, create or change the pricing
      * using the price amount in the ProductPricing record.
      * @param recBookingLine The line file
      * @param recBookingDetail The detail for this type of product
      * @return NORMAL_RETURN if price exists and was added
      * @return ERROR_RETURN if no pricing (or a zero price) was added.
      */
     public int updateBookingPricing(BookingLineModel recBookingLine, BookingDetailModel recBookingDetail, int iChangeType)
     {
         return DBConstants.ERROR_RETURN;    // No update done.
     }
     /**
      * Create the product record for this product type.
      */
     public static Product getProductRecord(String strProductType, RecordOwner recordOwner)
     {
         Product recProduct = null;
         if (Hotel.HOTEL_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Hotel(recordOwner);
         else if (Land.LAND_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Land(recordOwner);
         else if (Air.AIR_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Air(recordOwner);
         else if (Transportation.TRANSPORTATION_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Transportation(recordOwner);
         else if (Car.CAR_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Car(recordOwner);
         else if (Cruise.CRUISE_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Cruise(recordOwner);
         else if (Item.ITEM_FILE.equalsIgnoreCase(strProductType))
             recProduct = new Item(recordOwner);
         else if (TourHeader.TOUR_HEADER_FILE.equalsIgnoreCase(strProductType))
             recProduct = new TourHeader(recordOwner);
         else if (TourModel.TOUR_FILE.equalsIgnoreCase(strProductType))
             recProduct = new TourHeader(recordOwner);  // Yes - Tour header is a tour component
         return recProduct;
     }
     /**
      * SetProperty Method.
      */
     public boolean setProperty(String strKey, String strProperty)
     {
         ((PropertiesField)this.getField(Product.PROPERTIES)).setProperty(strKey, strProperty);
         return true;
     }
     /**
      * Get this property.
      */
     public String getProperty(String strKey)
     {
         return ((PropertiesField)this.getField(Product.PROPERTIES)).getProperty(strKey);
     }
     /**
      * MarkupPriceFromCost Method.
      */
     public void markupPriceFromCost(double dMarkup, boolean bMarkupOnlyIfNoPrice)
     {
         if ((!bMarkupOnlyIfNoPrice) || (this.getField(Product.PRODUCT_PRICE_LOCAL).getValue() == 0))
         {
             if (dMarkup == 0.00)
                 this.getField(Product.PRODUCT_PRICE_LOCAL).setData(null);
             else
                 this.getField(Product.PRODUCT_PRICE_LOCAL).setValue(Math.floor(this.getField(Product.PRODUCT_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
         }
     }
 
 }
