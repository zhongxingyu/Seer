 /**
   * @(#)StatusDescConverter.
   * Copyright © 2012 tourapp.com. All rights reserved.
   * GPL3 Open Source Software License.
   */
 package com.tourapp.tour.product.base.db;
 
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
 import org.jbundle.base.screen.model.util.*;
 import com.tourapp.thin.app.booking.entry.*;
 import org.jbundle.base.screen.model.*;
 import com.tourapp.model.tour.booking.detail.db.*;
 
 /**
  *  StatusDescConverter - .
  */
 public class StatusDescConverter extends DescConverter
 {
     /**
      * Default constructor.
      */
     public StatusDescConverter()
     {
         super();
     }
     /**
      * Constructor.
      */
     public StatusDescConverter(Converter converter)
     {
         this();
         this.init(converter);
     }
     /**
      * Initialize class fields.
      */
     public void init(BaseField field)
     {
         super.init(field);
     }
     /**
      * GetMaxLength Method.
      */
     public int getMaxLength()
     {
         return 30;
     }
     /**
      * GetProductType Method.
      */
     public String getProductType()
     {
         BookingDetailModel recCustSaleDetail = (BookingDetailModel)((BaseField)this.getField()).getRecord();
         String strProductType = recCustSaleDetail.getField(BookingDetailModel.PRODUCT_TYPE).toString();
         if ((strProductType == null) || (strProductType.length() == 0))
             strProductType = ProductType.ITEM;
         return strProductType;
     }
     /**
      * GetString Method.
      */
     public String getString()
     {
         ResourceBundle resources = null;
         if ((((BaseField)this.getField()).getRecord().getRecordOwner()) != null)
        	if ((((BaseField)this.getField()).getRecord().getRecordOwner().getTask()) != null)
        		if ((((BaseField)this.getField()).getRecord().getRecordOwner().getTask().getApplication()) != null)
        	resources = ((BaseApplication)((BaseField)this.getField()).getRecord().getRecordOwner().getTask().getApplication()).getResources(ResourceConstants.BOOKING_RESOURCE, true);
         String strProductType = this.getProductType();
         if (resources != null)
        	strProductType = resources.getString(this.getProductType());
         int iStatus = (int)this.getValue();
         boolean bNormalStatus = true;
         if ((iStatus & (1 << BookingConstants.INFO_LOOKUP)) != 0)
         {
             strProductType += ' ' + resources.getString(BookingConstants.INFO);
             bNormalStatus = false;
         }
         if ((iStatus & (1 << BookingConstants.COST_LOOKUP)) != 0)
         {
             strProductType += ' ' + resources.getString(BookingConstants.COST);
             bNormalStatus = false;
         }
         if ((iStatus & (1 << BookingConstants.INVENTORY_LOOKUP)) != 0)
         {
             strProductType += ' ' + resources.getString(BookingConstants.INVENTORY);
             bNormalStatus = false;
         }
         if ((iStatus & (1 << BookingConstants.PRODUCT_LOOKUP)) != 0)
         {
             strProductType += ' ' + resources.getString(BookingConstants.PRODUCT);
             bNormalStatus = false;
         }
         if (!bNormalStatus)
             strProductType += ' ' + resources.getString("Pending");
         return strProductType;
     }
 
 }
