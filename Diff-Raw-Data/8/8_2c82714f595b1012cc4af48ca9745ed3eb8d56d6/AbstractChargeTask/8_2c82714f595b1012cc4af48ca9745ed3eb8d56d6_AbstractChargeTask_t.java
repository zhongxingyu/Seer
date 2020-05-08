 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.process.task;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
 import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.item.db.ItemDAS;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.pluggableTask.InvoiceCompositionTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
 import com.sapienter.jbilling.server.process.PeriodOfTime;
 import com.sapienter.jbilling.server.util.Constants;
 
 /**
  * This plug-in calculates taxes for invoice.
  * 
  * Plug-in parameters:
  * 
  * charge_carrying_item_id (required) The item that will be added to an invoice with the
  * taxes
  * 
  * 
  * @author Vikas Bodani
  * @since 28-Jul-2011
  * 
  */
 public abstract class AbstractChargeTask extends PluggableTask implements InvoiceCompositionTask {
 
     private static final Logger LOG = Logger.getLogger(AbstractChargeTask.class);
     
     //mandatory plugin parameters
     protected static final ParameterDescription PARAM_TAX_ITEM_ID = 
         new ParameterDescription("charge_carrying_item_id", true, ParameterDescription.Type.STR);
     
     /**
      * The tax item initialized via a plugin-parameter holds the charge that'll be applied 
      * to the invoice provided other conditions are met. The price may be percetage or fixed rate.
      */
     protected ItemDTO taxItem= null;
     
     // initializer for pluggable params
     {
         descriptions.add(PARAM_TAX_ITEM_ID);
     }
 
     /**
      * 
      */
     public AbstractChargeTask() {
         super();
     }
     
     public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException {
         LOG.debug("apply()");
         this.setPluginParameters();
     
         if (!isTaxCalculationNeeded(invoice, userId)) {
             return;
         }
     
         calculateAndApplyTax(invoice, userId);
     }
 
     /**
      * Apply the percetage or flat rate from the taxItem and apply it to the invoice.
      * @param invoice
      * @param userId
      */
     protected void applyCharge(NewInvoiceDTO invoice, Integer userId, BigDecimal taxOrPenaltyBaseAmt, Integer INVOICE_LINE_TYPE) {
         LOG.debug("applyCharge()");
         BigDecimal taxOrPenaltyValue= null;
        String itemDescription= taxItem.getDescription();
         
         if (taxItem.getPercentage() != null) {
             LOG.debug("Percentage: " + taxItem.getPercentage());
             LOG.debug("Calculating tax on = " + taxOrPenaltyBaseAmt);
             taxOrPenaltyValue = taxOrPenaltyBaseAmt.multiply(taxItem.getPercentage()).divide(
                     BigDecimal.valueOf(100L), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
            //itemDescription= taxItem.getDescription() + " @ %" + new DecimalFormat("#0.##").format(taxItem.getPercentage().doubleValue());
         } else {
             LOG.debug("Flat Price."); 
             ItemBL itemBL = new ItemBL(taxItem);
             taxOrPenaltyValue= itemBL.getPriceByCurrency(taxItem, userId, Integer.valueOf(invoice.getCurrency().getId()));
            //itemDescription= taxItem.getDescription();
         }
         LOG.debug("Adding Tax Or Penalty as additional Invoice Line");
         //if (taxOrPenaltyValue != null && taxOrPenaltyValue.compareTo(BigDecimal.ZERO) != 0) {
         InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null, itemDescription,
                 taxOrPenaltyValue, taxOrPenaltyValue, BigDecimal.ONE, INVOICE_LINE_TYPE, 0,
                 Integer.valueOf(taxItem.getId()), userId, null);
         invoice.addResultLine(invoiceLine);
         //}
     }
     
     /**
      * Set all the current plugin params
      */
     protected void setPluginParameters() throws TaskException {
         LOG.debug("setPluginParameters()");
         try {
             String paramValue = getParameter(PARAM_TAX_ITEM_ID.getName(), "");// mandatory
             if (paramValue == null || "".equals(paramValue.trim())) {
                 throw new TaskException("Tax item id is not defined!");
             }
             this.taxItem = new ItemDAS().find(Integer.valueOf(paramValue));
             if (taxItem == null) {
                 throw new TaskException("Tax item not found!");
             }
             LOG.debug("The Tax Item is set.");
         } catch (NumberFormatException e) {
             LOG.error("Incorrect plugin configuration", e);
             throw new TaskException(e);
         }
     }
     
     /**
      * 
      * @param invoice
      * @param userId
      * @return
      */    
     protected abstract boolean isTaxCalculationNeeded(NewInvoiceDTO invoice, Integer userId);
     
     /**
      * 
      * @param invoice
      * @param userId
      */
     protected abstract void calculateAndApplyTax(NewInvoiceDTO invoice, Integer userId);
     
     public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
         LOG.debug("calculatePeriodAmount(). Throwing exception.");
         throw new UnsupportedOperationException("Can't call this method");
     }
 
 }
