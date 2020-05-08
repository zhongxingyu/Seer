 /**
  * Mule Vertex Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 /* * Copyright (c) 2012 Zauber S.A. -- All rights reserved */
 
 package org.mule.modules.vertex;
 
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.modules.vertex.api.VertexClient;
 import org.mule.modules.vertex.exception.VertexRuntimeException;
 import org.mule.modules.vertex.impl.SimpleVertexClient;
 
 import javax.annotation.PostConstruct;
 
 import vertexinc.o_series.tps._6._0.APInvoiceSyncRequestType;
 import vertexinc.o_series.tps._6._0.APInvoiceSyncResponseType;
 import vertexinc.o_series.tps._6._0.ARBillingSyncRequestType;
 import vertexinc.o_series.tps._6._0.ARBillingSyncResponseType;
 import vertexinc.o_series.tps._6._0.AccrualRequestType;
 import vertexinc.o_series.tps._6._0.AccrualResponseType;
 import vertexinc.o_series.tps._6._0.AccrualSyncRequestType;
 import vertexinc.o_series.tps._6._0.AccrualSyncResponseType;
 import vertexinc.o_series.tps._6._0.AssetMovementRequestType;
 import vertexinc.o_series.tps._6._0.AssetMovementResponseType;
 import vertexinc.o_series.tps._6._0.BuyerInputTaxRequestType;
 import vertexinc.o_series.tps._6._0.BuyerInputTaxResponseType;
 import vertexinc.o_series.tps._6._0.DeleteRequestType;
 import vertexinc.o_series.tps._6._0.DeleteResponseType;
 import vertexinc.o_series.tps._6._0.DistributeTaxProcurementRequestType;
 import vertexinc.o_series.tps._6._0.DistributeTaxProcurementResponseType;
 import vertexinc.o_series.tps._6._0.DistributeTaxRequestType;
 import vertexinc.o_series.tps._6._0.DistributeTaxResponseType;
 import vertexinc.o_series.tps._6._0.ERSRequestType;
 import vertexinc.o_series.tps._6._0.ERSResponseType;
 import vertexinc.o_series.tps._6._0.FindChangedTaxAreaIdsRequestType;
 import vertexinc.o_series.tps._6._0.FindChangedTaxAreaIdsResponseType;
 import vertexinc.o_series.tps._6._0.FindTaxAreasRequestType;
 import vertexinc.o_series.tps._6._0.FindTaxAreasResponseType;
 import vertexinc.o_series.tps._6._0.InventoryRemovalRequestType;
 import vertexinc.o_series.tps._6._0.InventoryRemovalResponseType;
 import vertexinc.o_series.tps._6._0.InvoiceRequestType;
 import vertexinc.o_series.tps._6._0.InvoiceResponseType;
 import vertexinc.o_series.tps._6._0.InvoiceVerificationRequestType;
 import vertexinc.o_series.tps._6._0.InvoiceVerificationResponseType;
 import vertexinc.o_series.tps._6._0.IsTaxAreaChangedRequestType;
 import vertexinc.o_series.tps._6._0.IsTaxAreaChangedResponseType;
 import vertexinc.o_series.tps._6._0.PurchaseOrderRequestType;
 import vertexinc.o_series.tps._6._0.PurchaseOrderResponseType;
 import vertexinc.o_series.tps._6._0.QuotationRequestType;
 import vertexinc.o_series.tps._6._0.QuotationResponseType;
 import vertexinc.o_series.tps._6._0.ReversalRequestType;
 import vertexinc.o_series.tps._6._0.ReversalResponseType;
 import vertexinc.o_series.tps._6._0.RollbackRequestType;
 import vertexinc.o_series.tps._6._0.RollbackResponseType;
 import vertexinc.o_series.tps._6._0.TaxAreaRequestType;
 import vertexinc.o_series.tps._6._0.TaxAreaResponseType;
 import vertexinc.o_series.tps._6._0.TransactionExistsRequestType;
 import vertexinc.o_series.tps._6._0.TransactionExistsResponseType;
 
 
 
 /**
  * Provides Vertex connectivity to mule.
  * <p>
  * Vertex provides tax data, technology, services, and expertise to help you manage
  * every stage of the corporate tax lifecycle -- from tax accounting to calculation,
  * compliance, audit support, and planning.
  * </p>
  * 
  * @author MuleSoft, Inc.
  */
 @Module(name = "vertex", schemaVersion = "1.0", friendlyName = "Vertex")
 public class VertexModule
 {
 
     /**
      * The User ID of the individual initiating the login
      */
     @Configurable
     private String username;
 
     /**
      * The password associated with the User ID of the individual initiating the
      * login
      */
     @Configurable
     private String password;
 
     /**
      * An identifier supplied by an ERP
      */
     @Optional
     @Configurable
     private String trustedId;
 
     /**
      * Vertex client
      */
     private VertexClient client;
 
     /**
      * <p>
      * Use the Accrual schema in self-accrual situations. These situations occur when
      * the buyer must remit tax on tangible personal property or services (used or
      * consumed) when tax is due but has not been paid. Accrual transactions are
      * written to the Tax Journal unless you set the postToJournal attribute to
      * false.
      * </p>
      * <p>
      * Some examples of self-accrual situations include:
      * <ul>
      * <li>When a seller does not have nexus in the buyer's state and does not
      * collect tax</li>
      * <li>When a buyer purchases non-exempt tangible personal property without tax
      * and subsequently uses it in a taxable manner (and the buyer has provided the
      * seller a direct pay permit)</li>
      * <li>When a company uses items from inventory in a taxable manner (and the
      * buyer has provided the seller a resale certificate)</li>
      * <li>When a buyer in a VAT situation is required to collect and remit the tax,
      * as in the case of intra-EU transactions as well as imports to EU and non-EU
      * countries</li>
      * <li>When a country's Place of Supply rules require a buyer to self-accrue in a
      * VAT situation</li>
      * </ul>
      * </p>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-accrual-tax}
      * 
      * @param accrualRequest The {@link AccrualRequestType}
      * 
      * @return The {@link AccrualResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public AccrualResponseType calculateAccrualTax(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.AccrualRequestType accrualRequest)
     {
         return client.calculateTax(TaxTransactionType.ACCRUAL_TYPE, accrualRequest);
 
     }
 
     /**
      * Use the Invoice Verification schema to verify sales or seller use tax on
      * incoming invoices to the buyer for tangible personal property, rentals,
      * leases, and services. The invoice is recreated from the seller's perspective
      * to validate that the proper tax has been applied. The interface verifies the
      * total tax amount on the original invoice versus the calculated amount.
      * User-defined thresholds for tolerance are validated at the transaction level
      * or line-item level, and an indicator is returned for undercharges and
      * overcharges outside these settings. No tax may be on the invoice if the buyer
      * has filed a direct pay permit with the vendor. Invoice Verification
      * transactions are written to the Tax Journal unless you set the postToJournal
      * attribute to false. Invoice Verification transactions are not part of most
      * standard reports. If the Invoice Verification transaction falls within the
      * user-defined threshold, the Calculation Engine automatically determines if
      * additional taxes are due in any jurisdictions. If additional taxes are due,
      * the amount is returned with the AdditionalTaxesDue element in the response
      * message. The result of this additional tax calculation is not written to the
      * Tax Journal. If you require this detail to be recorded in the Tax Journal,
      * pass a separate Accrual transaction.
      * 
      * {@sample.xml../../../doc/vertex-connector-samples.xml vertex:calculate-invoice-verification}
      * 
      * @param invoiceVerification The {@link InvoiceVerificationRequestType}.
      * 
      * @return The {@link InvoiceVerificationResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public InvoiceVerificationResponseType calculateInvoiceVerification(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.InvoiceVerificationRequestType invoiceVerification)
     {
         return client.calculateTax(TaxTransactionType.INVOICE_VERIFICATION_TYPE, invoiceVerification);
     }
 
     /**
      * Use the Buyer Input Tax schema to record any input or import VAT paid on
      * purchases, and to calculate the recoverable amounts on these taxes. Buyer
      * Input Tax transactions are written to the Tax Journal unless you set the
      * postToJournal attribute to false. 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-buyer-input-tax}
      * 
      * @param buyerInputTax The {@link BuyerInputTaxRequestType}
      * @return The {@link BuyerInputTaxResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public BuyerInputTaxResponseType calculateBuyerInputTax(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.BuyerInputTaxRequestType buyerInputTax)
     {
         return client.calculateTax(TaxTransactionType.BUYER_INPUT_TAX_TYPE, buyerInputTax);
     }
 
     /**
      * Use the ERS schema to calculate tax when a formalized agreement exists between
      * the buyer and seller that places the tax calculation burden on the buyer. In
      * these situations, no invoice is actually issued by the seller; rather, the
      * buyer relies on pre-established terms (represented in a purchase order or
      * other agreement) to calculate his or her obligation and remit payment directly
      * to the seller. A current knowledge of the seller's tax status (for example,
      * where the seller has nexus) must be maintained to assess tax at the proper
      * rate. ERS transactions are written to the Tax Journal unless you set the
      * postToJournal attribute to false. 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-ers-tax}
      * 
      * @param ers The {@link ERSRequestType}
      * @return The {@link ERSResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public ERSResponseType calculateErsTax(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.ERSRequestType ers)
     {
         return client.calculateTax(TaxTransactionType.ERS_TYPE, ers);
     }
 
     /**
      * Use the Asset Movement schema to calculate consumer use tax on movement of
      * fixed assets from one tax jurisdiction to another. Whenever an asset is moved,
      * the use tax rates and rules for the new location must be compared with the use
      * tax rates and rules for the old location. Various reciprocity rules, which
      * vary by tax jurisdiction, may need to be applied in these situations. Two
      * factors that can be used to determine reciprocity are duration at a location
      * and the number of times that an asset was moved. Unless you set the
      * postToJournal attribute to false, Asset Movement transactions are written to
      * the Tax Journal when additional use tax must be accrued at the new location.
      * You can also use the Asset Movement schema to calculate the VAT on movement of
      * fixed assets from one tax jurisdiction to another. A Call Off or Consignment
      * indicator may be passed in the simplificationCode attribute on Asset Movement
      * transactions to allow for the deferment of VAT when appropriate.
      *  
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-asset-movement-tax}
      * 
      * @param assetMovement The {@link AssetMovementRequestType}
      * 
      * @return The {@link AssetMovementResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public AssetMovementResponseType calculateAssetMovementTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.AssetMovementRequestType assetMovement)
     {
         return client.calculateTax(TaxTransactionType.ASSET_MOVEMENT_TYPE, assetMovement);
     }
 
     /**
      * Use the Inventory Removal schema to calculate consumer use tax when
      * withdrawing inventory for a specific internal project or task. Consumer use
      * tax that results from an Inventory Removal transaction should be accrued at
      * the final destination location of the inventory. Inventory Removal
      * transactions are written to the Tax Journal unless you set the postToJournal
      * attribute to false. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-inventory-removal-tax}
      * 
      * @param inventoryRemoval The {@link InventoryRemovalRequestType} 
      * 
      * @return The {@link InventoryRemovalResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public InventoryRemovalResponseType calculateInventoryRemovalTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.InventoryRemovalRequestType inventoryRemoval)
     {
         return client.calculateTax(TaxTransactionType.INVENTORY_REMOVAL_TYPE, inventoryRemoval);
     }
 
     /**
      * Use the Invoice schema to calculate tax at the time of shipping, billing, or
      * invoicing from the seller's perspective. Because tax liability is typically
      * incurred at the point of invoicing, Invoice transactions are written to the
      * Tax Journal. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-invoice-tax}
      * 
      * @param invoice The {@link InvoiceRequestType}
      * 
      * @return The {@link InvoiceResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public InvoiceResponseType calculateInvoiceTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.InvoiceRequestType invoice)
     {
         return client.calculateTax(TaxTransactionType.INVOICE_TYPE, invoice);
     }
 
     /**
      * Use the Purchase Order schema to estimate the sales tax or value added tax on
      * intended purchases made by a buyer. The intended purchase of goods or
      * services, including the estimated tax, is used as input to generate the
      * purchase order. For sales tax, the ultimate use of the goods or services
      * typically determines the taxability of the purchase. Note that because these
      * events are used to estimate tax, no transactions are stored in the Tax
      * Journal. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-purchase-order-tax}
      * 
      * @param purchaseOrder The {@link PurchaseOrderRequestType}
      * 
      * @return The {@link PurchaseOrderResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public PurchaseOrderResponseType calculatePurchaseOrderTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.PurchaseOrderRequestType purchaseOrder)
     {
         return client.calculateTax(TaxTransactionType.PURCHASE_ORDER_TYPE, purchaseOrder);
     }
 
     /**
      * Use the Quotation schema to estimate taxes on the proposed sale, rental, or
      * lease of goods or services by the seller. Quotations may be called from a CRM,
      * Mobile Sale, Order Entry, or Internet Sale application. Because Quotation
      * transactions are subject to change until an invoice is finalized, they are not
      * written to the Tax Journal. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-quotation-tax}
      * 
      * @param quotation The {@link QuotationRequestType}
      * 
      * @return The {@link QuotationResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public QuotationResponseType calculateQuotationTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.QuotationRequestType quotation)
     {
         return client.calculateTax(TaxTransactionType.QUOTATION_TYPE, quotation);
     }
 
     /**
      * Use the Distribute Sales Tax (Distribute Previously Calculated Tax) schema to
      * accept a combined total tax amount for a purchase and distribute that tax to
      * the appropriate jurisdictions. The combined rates for the taxing jurisdictions
      * are used with the total tax amount to determine the taxable base and then to
      * redistribute the calculated taxes to each level for remittance. Distribute
      * Sales Tax transactions are written to the Tax Journal unless you set the
      * postToJournal attribute to false. You can also use the Distribute Sales Tax
      * schema to process a tax-only credit (to receive credit for an overpayment to
      * taxing jurisdictions) or debit (to report tax that should have been remitted
      * to a taxing jurisdiction) on a sales tax transaction. You do this by setting
      * the isTaxOnlyAdjustmentIndicator attribute on the Request message. The total
      * tax amount that you provide is distributed across the taxing jurisdiction
      * locations you identified, debiting or crediting the tax amount in the Tax
      * Journal. The Distribute Sales Tax transaction uses Vertex Central rules that
      * are in effect as of the date in the documentDate attribute. If records for
      * jurisdiction registration, product exception, or customer exception or
      * exemption are dated on or before that date, they override information in the
      * transaction. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-distribute-tax}
      * 
      * @param distributeTax The {@link DistributeTaxRequestType}
      * 
      * @return The {@link DistributeTaxResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public DistributeTaxResponseType calculateDistributeTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.DistributeTaxRequestType distributeTax)
     {
         return client.calculateTax(TaxTransactionType.DISTRIBUTE_TAX_TYPE, distributeTax);
     }
 
     /**
      * Use the Distribute Tax Procurement (Distribute Previously Calculated Tax)
      * schema to accept a combined total tax amount for a purchase and distribute
      * that tax to the appropriate jurisdictions. The combined rates for the taxing
      * jurisdictions are used with the total tax amount to determine the taxable base
      * and then to redistribute the calculated taxes to each level for remittance.
      * Distribute Tax Procurement transactions are written to the Tax Journal unless
      * you set the postToJournal attribute to false. You can also use the Distribute
      * Tax Procurement schema to process a tax-only credit (to receive credit for an
      * overpayment to taxing jurisdictions) or debit (to report tax that should have
      * been remitted to a taxing jurisdiction) on a consumer use tax transaction. You
      * do this by setting the isTaxOnlyAdjustmentIndicator attribute on the Request
      * message. The total tax amount that you provide is distributed across the
      * taxing jurisdiction locations you identified, debiting or crediting the tax
      * amount in the Tax Journal. The Distribute Tax Procurement transaction uses
      * Vertex Central rules that are in effect as of the date in the documentDate
      * attribute. If records for jurisdiction registration, product exception, or
      * customer exception or exemption are dated on or before that date, they
      * override information in the transaction. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-distribute-tax-procurement}
      * 
      * @param distributeTaxProcurement The {@link DistributeTaxProcurementRequestType}
      * 
      * @return The {@link DistributeTaxProcurementResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public DistributeTaxProcurementResponseType 
             calculateDistributeTaxProcurement(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.DistributeTaxProcurementRequestType distributeTaxProcurement)
     {
         return client.calculateTax(TaxTransactionType.DISTRIBUTE_TAX_PROCUREMENT_TYPE, distributeTaxProcurement);
     }
 
     /**
      * Use the Reversal schema to post a new transaction to the Tax Journal, using
      * values that are numerically opposite the values in the original transaction.
      * Note that no calculations are performed; instead, the values from the original
      * transaction are used.
      * <p>
      * <b>Restrictions</b><br>
      * Note the following restrictions on Reversal transactions:
      * <ul>
      * <li>You cannot reverse a transaction unless it was posted to the Tax Journal
      * and given a unique user-defined identifier.</li>
      * <li>Once a transaction has been reversed, you cannot reverse the original
      * transaction again. However, you can reverse the reversal transaction itself.</li>
      * <li>You can reverse active synchronization transactions but not inactive
      * synchronization transactions. If you reverse a rolled-back active
      * synchronization transaction, no exception is returned.</li>
      * <li>Transaction Tester does not allow you to test transaction reversals.</li>
      * <li>Point of sale transactions cannot be reversed. An exception is returned if
      * you try to reverse a point of sale transaction.</li>
      * </ul>
      * </p>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-reversal-tax}
      * 
      * @param reversalRequest The {@link ReversalRequestType}
      * 
      * @return The {@link ReversalResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public ReversalResponseType calculateReversalTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.ReversalRequestType reversalRequest)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.REVERSAL_TYPE, reversalRequest);
     }
 
     /**
      * Use the Transaction Exists schema to find out whether a transaction with a
      * unique user-defined identifier exists in the Tax Journal. The identifier is
      * transactionId in the XML message and transSyncIdCode in the Tax Journal. Note
      * that transactions that have been deleted from the Tax Journal (that is,
      * flagged as Deleted) return False on the Transaction Exists query.
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:transaction-exists}
      * 
      * @param transactionExists The {@link TransactionExistsRequestType}
      *              
      * @return The {@link TransactionExistsResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public TransactionExistsResponseType transactionExists(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.TransactionExistsRequestType transactionExists)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.TRANSACTION_EXISTS_TYPE, transactionExists);
     }
 
     /**
      * Use the Synchronization Services schema to reconcile transactions between
      * Vertex O Series and your host system. Any changes made to a financial document
      * can impact previously calculated transactions already written to the Tax
      * Journal. The Synchronization Service schema enable you to accept changes made
      * to a financial document and update the transaction in the Tax Journal
      * accordingly. In addition, schema are provided that enable you to delete a
      * transaction or to roll back a transaction to its previous state.
      * <p>
      * Note the following restrictions on synchronization services:<br>
      * <ul>
      * <li>A synchronization transaction fails if attempted on a reversal
      * transaction.</li>
      * <li>You cannot use this transaction on point of sale transactions. If you do,
      * an exception is returned.</li>
      * </ul>
      * </p>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-accrual-sync-tax}
      * 
      * @param accrualSync The {@link AccrualSyncRequestType}
      * 
      * @return The {@link AccrualSyncResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public AccrualSyncResponseType calculateAccrualSyncTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.AccrualSyncRequestType accrualSync)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.ACCRUAL_SYNC_TYPE, accrualSync);
     }
 
     /**
      * Use the Synchronization Services schema to reconcile transactions between
      * Vertex O Series and your host system. Any changes made to a financial document
      * can impact previously calculated transactions already written to the Tax
      * Journal. The Synchronization Service schema enable you to accept changes made
      * to a financial document and update the transaction in the Tax Journal
      * accordingly. In addition, schema are provided that enable you to delete a
      * transaction or to roll back a transaction to its previous state. <br>
      * Note the following restrictions on synchronization services:<br>
      * <ul>
      * <li>A synchronization transaction fails if attempted on a reversal
      * transaction.</li>
      * <li>You cannot use this transaction on point of sale transactions. If you do,
      * an exception is returned.</li>
      * </ul>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-a-p-invoice-sync-tax}
      * 
      * @param apInvoiceSync The {@link APInvoiceSyncRequestType}
      * 
      * @return The {@link APInvoiceSyncResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public APInvoiceSyncResponseType calculateAPInvoiceSyncTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.APInvoiceSyncRequestType apInvoiceSync)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.AP_INVOICE_TYPE, apInvoiceSync);
     }
 
     /**
      * Use the Synchronization Services schema to reconcile transactions between
      * Vertex O Series and your host system. Any changes made to a financial document
      * can impact previously calculated transactions already written to the Tax
      * Journal. The Synchronization Service schema enable you to accept changes made
      * to a financial document and update the transaction in the Tax Journal
      * accordingly. In addition, schema are provided that enable you to delete a
      * transaction or to roll back a transaction to its previous state. <br>
      * Note the following restrictions on synchronization services:<br>
      * <ul>
      * <li>A synchronization transaction fails if attempted on a reversal
      * transaction.</li>
      * <li>You cannot use this transaction on point of sale transactions. If you do,
      * an exception is returned.</li>
      * </ul>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-a-r-billing-sync-tax}
      * 
      * @param arBillingSync The {@link ARBillingSyncRequestType}
      * 
      * @return The {@link ARBillingSyncResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public ARBillingSyncResponseType calculateARBillingSyncTax(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.ARBillingSyncRequestType arBillingSync)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.AR_BILLING_TYPE, arBillingSync);
     }
 
     /**
      * Use the Synchronization Services schema to reconcile transactions between
      * Vertex O Series and your host system. Any changes made to a financial document
      * can impact previously calculated transactions already written to the Tax
      * Journal. The Synchronization Service schema enable you to accept changes made
      * to a financial document and update the transaction in the Tax Journal
      * accordingly. In addition, schema are provided that enable you to delete a
      * transaction or to roll back a transaction to its previous state. <br>
      * Note the following restrictions on synchronization services:<br>
      * <ul>
      * <li>A synchronization transaction fails if attempted on a reversal
      * transaction.</li>
      * <li>You cannot use this transaction on point of sale transactions. If you do,
      * an exception is returned.</li>
      * </ul>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:delete-transaction-sync-tax}
      * 
      * @param deleteTransactionSync The {@link DeleteRequestType}
      * 
      * @return The {@link DeleteResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public DeleteResponseType deleteTransactionSync(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.DeleteRequestType deleteTransactionSync)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.DELETE_TYPE, deleteTransactionSync);
     }
 
     /**
      * Use the Synchronization Services schema to reconcile transactions between
      * Vertex O Series and your host system. Any changes made to a financial document
      * can impact previously calculated transactions already written to the Tax
      * Journal. The Synchronization Service schema enable you to accept changes made
      * to a financial document and update the transaction in the Tax Journal
      * accordingly. In addition, schema are provided that enable you to delete a
      * transaction or to roll back a transaction to its previous state. <br>
      * Note the following restrictions on synchronization services:<br>
      * <ul>
      * <li>A synchronization transaction fails if attempted on a reversal
      * transaction.</li>
      * <li>You cannot use this transaction on point of sale transactions. If you do,
      * an exception is returned.</li>
      * </ul>
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:rollback-transaction-sync-tax}
      * 
      * @param rollbackTransactionSync The {@link RollbackRequestType}
      * 
      * @return The {@link RollbackResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public RollbackResponseType rollbackTransactionSync(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.RollbackRequestType rollbackTransactionSync)
     {
         return client.calculateTaxSync(TaxTransactionSyncType.ROLLBACK_TYPE, rollbackTransactionSync);
     }
 
     /**
      * Use the Find Changed Tax Area schema to identify Tax Area IDs that have
      * changed within a specified date period.
      *  
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:find-changed-tax-area-ids}
      * 
      * @param findChangedAreaIds The {@link FindChangedTaxAreaIdsRequestType}.
      * 
      * @return The {@link FindChangedTaxAreaIdsResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public FindChangedTaxAreaIdsResponseType findChangedTaxAreaIds(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.FindChangedTaxAreaIdsRequestType findChangedAreaIds)
     {
         return client.lookupTaxArea(TaxGisType.FIND_CHANGED_TAX_AREA_IDS_TYPE, findChangedAreaIds);
     }
 
     /**
      * Use the Is Tax Area Changed schema to indicate whether a specified Tax Area ID
      * has changed within a specified date period. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:is-tax-area-changed}
      * 
      * @param isTaxAreaChanged The {@link IsTaxAreaChangedRequestType}.
      * 
      * @return The {@link IsTaxAreaChangedResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public IsTaxAreaChangedResponseType isTaxAreaChanged(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.IsTaxAreaChangedRequestType isTaxAreaChanged)
     {
         return client.lookupTaxArea(TaxGisType.IS_TAX_AREA_CHANGED_TYPE, isTaxAreaChanged);
     }
 
     /**
      * Use the Tax Area schema to send a single query to retrieve jurisdictional
      * information. For example, you could use this to retrieve the Tax Area ID for a
      * single postal address. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:tax-area-lookup}
      * 
      * @param taxAreaLookup The {@link TaxAreaRequestType}
      * 
      * @return The {@link TaxAreaResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public TaxAreaResponseType taxAreaLookup(@Optional @Default("#[payload]") vertexinc.o_series.tps._6._0.TaxAreaRequestType taxAreaLookup)
     {
         return client.lookupTaxArea(TaxGisType.TAX_AREA_TYPE, taxAreaLookup);
     }
 
     /**
      * Use the Find Tax Areas schema to send a batch of queries to retrieve
      * jurisdictional information. For example, you could use this to retrieve the
      * Tax Area IDs for multiple postal addresses. 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:find-tax-areas}
      * 
      * @param findTaxAreas The {@link FindTaxAreasRequestType}
      * 
      * @return The {@link FindTaxAreasResponseType}
      * @throws VertexRuntimeException
      */
     @Processor
     public FindTaxAreasResponseType findTaxAreas(@Optional @Default("#[payload]")  vertexinc.o_series.tps._6._0.FindTaxAreasRequestType findTaxAreas)
     {
         return client.lookupTaxArea(TaxGisType.FIND_TAX_AREAS_TYPE, findTaxAreas);
     }
 
     /**
      * Pings the webservice 
      * 
      * {@sample.xml ../../../doc/vertex-connector-samples.xml vertex:calculate-accrual-tax}
      * 
      * @param echo The <code>String</code> to send
      * @return The <code>String</code> with the echo
      */
     public String ping(String echo)
     {
         return (String) client.pingService(echo);
     }
 
     /**
      * Initialize the {@link VertexClient} with the given login credentials
      */
     @PostConstruct
     public void init()
     {
         if (client == null)
         {
             client = new SimpleVertexClient(username, password, trustedId);
         }
     }
 
     /**
      * Returns the username.
      * 
      * @return {@link String} with the username.
      */
 
     public String getUsername()
     {
         return username;
     }
 
     /**
      * Sets the username.
      * 
      * @param username The vertex username
      */
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     /**
      * Returns the password.
      * 
      * @return {@link String} with the password.
      */
 
     public String getPassword()
     {
         return password;
     }
 
     /**
      * Sets the password.
      * 
      * @param password {@link String} with the vertex account password.
      */
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     /**
      * Returns the trustedId.
      * 
      * @return {@link String} with the trustedId.
      */
 
     public String getTrustedId()
     {
         return trustedId;
     }
 
     /**
      * Sets the trustedId.
      * 
      * @param {@link String} with the trustedId.
      */
 
     public void setTrustedId(String trustedId)
     {
         this.trustedId = trustedId;
     }
 
     /**
      * Sets the VertexClient.
      * 
     * @param The client.
      */
 
     public void setClient(VertexClient client)
     {
         this.client = client;
     }
 
 }
