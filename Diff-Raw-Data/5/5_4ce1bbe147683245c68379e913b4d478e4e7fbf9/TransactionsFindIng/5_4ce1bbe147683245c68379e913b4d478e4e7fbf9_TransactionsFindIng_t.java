 /*
  * Copyright (c) Open Source Strategies, Inc.
  *
  * Opentaps is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Opentaps is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.opensourcestrategies.financials.transactions;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javolution.util.FastList;
 import javolution.util.FastMap;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.GeneralException;
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 import org.ofbiz.party.party.PartyHelper;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.AcctgTransOrgPresupIng;
 import org.opentaps.base.entities.AcctgTransType;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.GlFiscalType;
 import org.opentaps.base.entities.InvoiceAndInvoiceItem;
 import org.opentaps.base.entities.NivelPresupuestal;
 import org.opentaps.common.builder.EntityListBuilder;
 import org.opentaps.common.builder.PageBuilder;
 import org.opentaps.common.util.UtilCommon;
 import org.opentaps.domain.DomainsDirectory;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.domain.organization.Organization;
 import org.opentaps.domain.organization.OrganizationRepositoryInterface;
 import org.opentaps.foundation.action.ActionContext;
 
 
 /**
  * TransactionActions - Java Actions for Transactions.
  */
 public class TransactionsFindIng {
 
     private static final String MODULE = TransactionActions.class.getName();
 
     /**
      * Action for the find / list transactions screen.
      * @param context the screen context
      * @throws GeneralException if an error occurs
      * @throws ParseException if an error occurs
      */
     public static void findTransactionsIngr(Map<String, Object> context) throws GeneralException, ParseException {
 
         final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         final TimeZone timeZone = ac.getTimeZone();
 
         String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
 
         // possible fields we're searching by
         String partyId = ac.getParameter("partyId");
         String acctgTransId = ac.getParameter("findAcctgTransId");
         String glFiscalTypeId = ac.getParameter("glFiscalTypeId");
         if (UtilValidate.isEmpty(glFiscalTypeId)) {
         	glFiscalTypeId = "ACTUAL";
         }
  
         ac.put("glFiscalTypeId", glFiscalTypeId);
 
         String desde = ac.getParameter("desde");
         String hasta = ac.getParameter("hasta");
 		
 
 
         DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
         final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 
         // TODO: Put a currencyUomId on AcctgTrans and modify postAcctgTrans to set that in addition to postedAmount,
         // instead of using the organization's base currency
         OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();
         Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
         if (organization != null) {
             ac.put("orgCurrencyUomId", organization.getPartyAcctgPreference().getBaseCurrencyUomId());
         }
 
         
         
         // get the list of transactionTypes for the parametrized form ftl
         List<AcctgTransType> transactionTypes = ledgerRepository.findAll(AcctgTransType.class);
         List<Map<String, Object>> transactionTypesList = new FastList<Map<String, Object>>();
         for (AcctgTransType s : transactionTypes) {
             Map<String, Object> map = s.toMap();
             transactionTypesList.add(map);
         }
         ac.put("transactionTypes", transactionTypesList);
 
 
         // get the list of glFiscalTypes for the parametrized form ftl
         List<GlFiscalType> glFiscalTypes = ledgerRepository.findAll(GlFiscalType.class);
         List<Map<String, Object>> glFiscalTypesList = new FastList<Map<String, Object>>();
         for (GlFiscalType s : glFiscalTypes) {
             Map<String, Object> map = s.toMap();
             glFiscalTypesList.add(map);
         }
         ac.put("glFiscalTypes", glFiscalTypesList);
         
         
         
         // construct search conditions
        //if ("Y".equals(ac.getParameter("performFind"))) {
             // build search conditions
             List<EntityCondition> searchConditions = new FastList<EntityCondition>();
             // this needs to allow null organizationPartyId for new AcctgTrans which have no AcctgTransEntries yet
             if (UtilValidate.isNotEmpty(acctgTransId)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
             }
             
            searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.tipoPoliza.name(), EntityOperator.EQUALS, "INGRESO"));
             if (UtilValidate.isNotEmpty(partyId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.partyId.name(), EntityOperator.EQUALS, partyId));
             }
             if (UtilValidate.isNotEmpty(acctgTransId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
             }
             if (UtilValidate.isNotEmpty(glFiscalTypeId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.glFiscalTypeId.name(), EntityOperator.EQUALS, glFiscalTypeId));
             }
 
            /*  if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransTypeId.name(), EntityOperator.EQUALS, acctgTransTypeId));
             }*/
 
             String dateFormat = UtilDateTime.getDateFormat(locale);
             if (desde != null) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(desde, dateFormat, timeZone, locale), timeZone, locale)));
             }
             if (hasta != null) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(hasta, dateFormat, timeZone, locale), timeZone, locale)));
             }
            
            
            /*
             
             if (UtilValidate.isNotEmpty(hasta)) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(hasta)));
             }*/
             // fields to select
             
         
             
             List<String> fieldsToSelect = UtilMisc.toList("acctgTransId", "acctgTransTypeId", "isPosted", "partyId", "transactionDate", "scheduledPostingDate");
             fieldsToSelect.add("postedDate");
             fieldsToSelect.add("postedAmount");
             fieldsToSelect.add("description");
             Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
             EntityListBuilder acctgTransListBuilder = new EntityListBuilder(ledgerRepository,  AcctgTransOrgPresupIng.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, UtilMisc.toList( AcctgTransOrgPresupIng.Fields.transactionDate.desc()));
             PageBuilder< AcctgTransOrgPresupIng> pageBuilder = new PageBuilder< AcctgTransOrgPresupIng>() {
                 public List<Map<String, Object>> build(List< AcctgTransOrgPresupIng> page) throws Exception {
                     Delegator delegator = ac.getDelegator();
                     List<Map<String, Object>> newPage = FastList.newInstance();
                     for ( AcctgTransOrgPresupIng acctgTrans : page) {
                         Map<String, Object> newRow = FastMap.newInstance();
                         newRow.putAll(acctgTrans.toMap());
                         if (UtilValidate.isNotEmpty(acctgTrans.getPartyId())) {
                             newRow.put("partyNameAndId", PartyHelper.getPartyName(delegator, acctgTrans.getPartyId(), false) + " (" + acctgTrans.getPartyId() + ")");
                         }
                         AcctgTransType acctgTransType = ledgerRepository.findOneCache(AcctgTransType.class, ledgerRepository.map(AcctgTransType.Fields.acctgTransTypeId, acctgTrans.getAcctgTransTypeId()));
                         newRow.put("acctgTransTypeDescription", acctgTransType.get(AcctgTransType.Fields.description.name(), locale));
                         newPage.add(newRow);
                     }
                     return newPage;
                 }
             };
             acctgTransListBuilder.setPageBuilder(pageBuilder);
             ac.put("acctgTransListBuilder", acctgTransListBuilder);
         //}
     }
     public static void findTransactionsEgre(Map<String, Object> context) throws GeneralException, ParseException {
 
         final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         final TimeZone timeZone = ac.getTimeZone();
         String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
 
         // possible fields we're searching by
         String partyId = ac.getParameter("partyId");
         String acctgTransId = ac.getParameter("findAcctgTransId");
         String glFiscalTypeId = ac.getParameter("glFiscalTypeId");
         if (UtilValidate.isEmpty(glFiscalTypeId)) {
         	glFiscalTypeId = "ACTUAL";
         }
  
         ac.put("glFiscalTypeId", glFiscalTypeId);
 
         String desde = ac.getParameter("desde");
         String hasta = ac.getParameter("hasta");
 
         DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
         final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 
         // TODO: Put a currencyUomId on AcctgTrans and modify postAcctgTrans to set that in addition to postedAmount,
         // instead of using the organization's base currency
         OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();
         Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
         if (organization != null) {
             ac.put("orgCurrencyUomId", organization.getPartyAcctgPreference().getBaseCurrencyUomId());
         }
 
         
         
         
         // get the list of transactionTypes for the parametrized form ftl
         List<AcctgTransType> transactionTypes = ledgerRepository.findAll(AcctgTransType.class);
         List<Map<String, Object>> transactionTypesList = new FastList<Map<String, Object>>();
         for (AcctgTransType s : transactionTypes) {
             Map<String, Object> map = s.toMap();
             transactionTypesList.add(map);
         }
         ac.put("transactionTypes", transactionTypesList);
         
         List<AcctgTrans> transactionsTypes = ledgerRepository.findAll(AcctgTrans.class);
         List<Map<String, Object>> transactionTypeList = new FastList<Map<String, Object>>();
         for (AcctgTrans s : transactionsTypes) {
             Map<String, Object> map = s.toMap();
             transactionTypeList.add(map);
         }
         ac.put("AcgTransTypes", transactionTypeList);
         
 
         // get the list of glFiscalTypes for the parametrized form ftl
         List<GlFiscalType> glFiscalTypes = ledgerRepository.findAll(GlFiscalType.class);
         List<Map<String, Object>> glFiscalTypesList = new FastList<Map<String, Object>>();
         for (GlFiscalType s : glFiscalTypes) {
             Map<String, Object> map = s.toMap();
             glFiscalTypesList.add(map);
         }
         ac.put("glFiscalTypes", glFiscalTypesList);
         
         
         
         // construct search conditions
        //if ("Y".equals(ac.getParameter("performFind"))) {
             // build search conditions
             List<EntityCondition> searchConditions = new FastList<EntityCondition>();
             // this needs to allow null organizationPartyId for new AcctgTrans which have no AcctgTransEntries yet
             if (UtilValidate.isNotEmpty(acctgTransId)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
             }
             
            searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.tipoPoliza.name(), EntityOperator.EQUALS, "EGRESO"));
             if (UtilValidate.isNotEmpty(partyId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.partyId.name(), EntityOperator.EQUALS, partyId));
             }
             if (UtilValidate.isNotEmpty(acctgTransId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
             }
             if (UtilValidate.isNotEmpty(glFiscalTypeId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.glFiscalTypeId.name(), EntityOperator.EQUALS, glFiscalTypeId));
             }
             String dateFormat = UtilDateTime.getDateFormat(locale);
             if (desde != null) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(desde, dateFormat, timeZone, locale), timeZone, locale)));
             }
             if (hasta != null) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(hasta, dateFormat, timeZone, locale), timeZone, locale)));
             }
            
 
            /*  if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransTypeId.name(), EntityOperator.EQUALS, acctgTransTypeId));
             }*/
 /*
             if (UtilValidate.isNotEmpty(desde)) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(desde)));
             }
             if (UtilValidate.isNotEmpty(hasta)) {
             	searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.postedDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(hasta)));
             }
             */
             // fields to select
             
         
             
             List<String> fieldsToSelect = UtilMisc.toList("acctgTransId", "acctgTransTypeId", "isPosted", "partyId", "transactionDate", "scheduledPostingDate");
             fieldsToSelect.add("postedDate");
             fieldsToSelect.add("postedAmount");
             fieldsToSelect.add("description");
             Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
             EntityListBuilder acctgTransListBuilder = new EntityListBuilder(ledgerRepository,  AcctgTransOrgPresupIng.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, UtilMisc.toList( AcctgTransOrgPresupIng.Fields.transactionDate.desc()));
             PageBuilder< AcctgTransOrgPresupIng> pageBuilder = new PageBuilder< AcctgTransOrgPresupIng>() {
                 public List<Map<String, Object>> build(List< AcctgTransOrgPresupIng> page) throws Exception {
                     Delegator delegator = ac.getDelegator();
                     List<Map<String, Object>> newPage = FastList.newInstance();
                     for ( AcctgTransOrgPresupIng acctgTrans : page) {
                         Map<String, Object> newRow = FastMap.newInstance();
                         newRow.putAll(acctgTrans.toMap());
                         if (UtilValidate.isNotEmpty(acctgTrans.getPartyId())) {
                             newRow.put("partyNameAndId", PartyHelper.getPartyName(delegator, acctgTrans.getPartyId(), false) + " (" + acctgTrans.getPartyId() + ")");
                         }
                         AcctgTransType acctgTransType = ledgerRepository.findOneCache(AcctgTransType.class, ledgerRepository.map(AcctgTransType.Fields.acctgTransTypeId, acctgTrans.getAcctgTransTypeId()));
                         newRow.put("acctgTransTypeDescription", acctgTransType.get(AcctgTransType.Fields.description.name(), locale));
                         newPage.add(newRow);
                     }
                     return newPage;
                 }
             };
             acctgTransListBuilder.setPageBuilder(pageBuilder);
             ac.put("acctgTransListBuilder", acctgTransListBuilder);
         //}
     }
 
     public static void findTrans(Map<String, Object> context) throws GeneralException, ParseException {
 
         final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
 
         // possible fields we're searching by
        
         String acctgTransId = ac.getParameter("findAcctgTransId");
        
 
         String desde = ac.getParameter("desde");
         String hasta = ac.getParameter("hasta");
 
         DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
         final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 
         // TODO: Put a currencyUomId on AcctgTrans and modify postAcctgTrans to set that in addition to postedAmount,
         // instead of using the organization's base currency
         OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();
         Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
         if (organization != null) {
             ac.put("orgCurrencyUomId", organization.getPartyAcctgPreference().getBaseCurrencyUomId());
         }
 
         
         
         
         // get the list of transactionTypes for the parametrized form ftl
         List<AcctgTransType> transactionTypes = ledgerRepository.findAll(AcctgTransType.class);
         List<Map<String, Object>> transactionTypesList = new FastList<Map<String, Object>>();
         for (AcctgTransType s : transactionTypes) {
             Map<String, Object> map = s.toMap();
             transactionTypesList.add(map);
         }
         ac.put("transactionTypes", transactionTypesList);
         
         List<AcctgTrans> transactionsTypes = ledgerRepository.findAll(AcctgTrans.class);
         List<Map<String, Object>> transactionTypeList = new FastList<Map<String, Object>>();
         for (AcctgTrans s : transactionsTypes) {
             Map<String, Object> map = s.toMap();
             transactionTypeList.add(map);
         }
         ac.put("AcgTransTypes", transactionTypeList);
         
 
         // get the list of glFiscalTypes for the parametrized form ftl
         List<GlFiscalType> glFiscalTypes = ledgerRepository.findAll(GlFiscalType.class);
         List<Map<String, Object>> glFiscalTypesList = new FastList<Map<String, Object>>();
         for (GlFiscalType s : glFiscalTypes) {
             Map<String, Object> map = s.toMap();
             glFiscalTypesList.add(map);
         }
         ac.put("glFiscalTypes", glFiscalTypesList);
         
         
         
         // construct search conditions
        //if ("Y".equals(ac.getParameter("performFind"))) {
             // build search conditions
             List<EntityCondition> searchConditions = new FastList<EntityCondition>();
             // this needs to allow null organizationPartyId for new AcctgTrans which have no AcctgTransEntries yet
            // if (UtilValidate.isNotEmpty(acctgTransId)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgTransOrgPresupIng.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
           //  }
 
             List<String> fieldsToSelect = UtilMisc.toList("acctgTransId", "acctgTransTypeId", "isPosted", "partyId", "transactionDate", "scheduledPostingDate");
             fieldsToSelect.add("postedDate");
             fieldsToSelect.add("postedAmount");
             fieldsToSelect.add("description");
             Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
             EntityListBuilder acctgTransListBuilder = new EntityListBuilder(ledgerRepository,  AcctgTransOrgPresupIng.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, UtilMisc.toList( AcctgTransOrgPresupIng.Fields.transactionDate.desc()));
             PageBuilder< AcctgTransOrgPresupIng> pageBuilder = new PageBuilder< AcctgTransOrgPresupIng>() {
                 public List<Map<String, Object>> build(List< AcctgTransOrgPresupIng> page) throws Exception {
                     Delegator delegator = ac.getDelegator();
                     List<Map<String, Object>> newPage = FastList.newInstance();
                     for ( AcctgTransOrgPresupIng acctgTrans : page) {
                         Map<String, Object> newRow = FastMap.newInstance();
                         newRow.putAll(acctgTrans.toMap());
                         if (UtilValidate.isNotEmpty(acctgTrans.getPartyId())) {
                             newRow.put("partyNameAndId", PartyHelper.getPartyName(delegator, acctgTrans.getPartyId(), false) + " (" + acctgTrans.getPartyId() + ")");
                         }
                         AcctgTransType acctgTransType = ledgerRepository.findOneCache(AcctgTransType.class, ledgerRepository.map(AcctgTransType.Fields.acctgTransTypeId, acctgTrans.getAcctgTransTypeId()));
                         newRow.put("acctgTransTypeDescription", acctgTransType.get(AcctgTransType.Fields.description.name(), locale));
                         newPage.add(newRow);
                     }
                     return newPage;
                 }
             };
             acctgTransListBuilder.setPageBuilder(pageBuilder);
             ac.put("acctgTransListBuilder", acctgTransListBuilder);
         //}
     }
 //Metodo que devuelve la lista que  se  genera el mapa para la exportacin a Excel    
     public static void findExcelTag(Map<String, Object> context) throws GeneralException, ParseException {
         final ActionContext ac = new ActionContext(context);
         DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
         final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 //////////////////////////////////////////////////////////        ///
         List<NivelPresupuestal> nivelList = ledgerRepository.findAll(NivelPresupuestal.class);
         List<Map<String, Object>> nivelLists = new FastList<Map<String, Object>>();
         for (NivelPresupuestal s : nivelList) {
             Map<String, Object> map = s.toMap();
             nivelLists.add(map);
         }
         ac.put("nivelLists", nivelLists);
         
         List<Enumeration> enumList = ledgerRepository.findAll(Enumeration.class);
         List<Map<String, Object>> Enumlists = new FastList<Map<String, Object>>();
         for (Enumeration s : enumList) {
             Map<String, Object> map = s.toMap();
             Enumlists.add(map);
         }
         ac.put("Enumlists", Enumlists);
         
     }
     
 }
