 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 
 /*
  * Created on Mar 10, 2004
  *
  * Copyright Sapienter Enterprise Software
  */
 package com.sapienter.jbilling.server.item;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.naming.NamingException;
 
import com.sapienter.jbilling.common.Constants;
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.CommonConstants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Util;
 import com.sapienter.jbilling.server.util.db.CurrencyDAS;
 import com.sapienter.jbilling.server.util.db.CurrencyDTO;
 import com.sapienter.jbilling.server.util.db.CurrencyExchangeDAS;
 import com.sapienter.jbilling.server.util.db.CurrencyExchangeDTO;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * @author Emil
  */
 public class CurrencyBL {
     private static final Logger LOG = Logger.getLogger(CurrencyBL.class);
     private CurrencyDAS das = null;
     private CurrencyDTO currency = null;
     //private Logger log = null;
     
     public CurrencyBL(Integer currencyId) {
         init();
         set(currencyId);
     }
     
     public void set(Integer id)  {
         currency = das.find(id);
     }
     
     public CurrencyBL() throws NamingException {
         init();
     }
     
     private void init() {
         das = new CurrencyDAS();
     }
     
     public CurrencyDTO getEntity() {
         return currency;
     }
 
     public BigDecimal convert(Integer fromCurrencyId, Integer toCurrencyId,
             BigDecimal amount, Integer entityId)
             throws SessionInternalError {
         BigDecimal retValue = null;
         
         LOG.debug("Converting " + fromCurrencyId + " to " + toCurrencyId +
                 " am " + amount + " en " + entityId);
         if (fromCurrencyId.equals(toCurrencyId)) {
             // mmm.. no conversion needed
             return amount;
         }
         
         // make the conversions
         retValue = convertPivotToCurrency(toCurrencyId, 
                 convertToPivot(fromCurrencyId, amount, entityId), entityId);
         
         return retValue;         
     }
     
     public BigDecimal convertToPivot(Integer currencyId, BigDecimal amount,
             Integer entityId) 
             throws SessionInternalError {
         CurrencyExchangeDTO exchange = null;
         
         if (currencyId.intValue() == 1) {
             // this is already in the pivot
             return amount;
         }
         
         exchange = findExchange(entityId, currencyId);
         // make the conversion itself
         BigDecimal tmp = new BigDecimal(amount.toString());
        tmp = tmp.divide(exchange.getRate(), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
         
         return tmp;
     }
     
     public BigDecimal convertPivotToCurrency(Integer currencyId, BigDecimal amount,
             Integer entityId) 
             throws SessionInternalError {
         CurrencyExchangeDTO exchange = null;
         
         if (currencyId.intValue() == 1) {
             // this is already in the pivot
             return amount;
         }
         
         exchange = findExchange(entityId, currencyId);
         // make the conversion itself
         BigDecimal tmp = new BigDecimal(amount.toString());
         tmp = tmp.multiply(exchange.getRate());
         
         return tmp;
     }
     
     public CurrencyExchangeDTO findExchange(Integer entityId, Integer currencyId)
             throws SessionInternalError {
         CurrencyExchangeDTO exchange = null;
 
         exchange = new CurrencyExchangeDAS().findExchange(entityId, currencyId);
         if (exchange == null) {
             // this entity doesn't have this exchange defined
             // 0 is the default, don't try to use null, it won't work
             exchange = new CurrencyExchangeDAS().findExchange(new Integer(0), currencyId);
             if (exchange == null) {
                 throw new SessionInternalError("Currency " + currencyId
                         + " doesn't have a defualt exchange");
             }
         }
 
         return exchange;
     }
     
     /**
      * Returns all the currency symbols in the proper order, so
      * retValue[currencyId] would be the symbol of currencyId
      * 
      * @return
      * @throws NamingException
      * @throws SQLException
      */
     public CurrencyDTO[] getSymbols() 
             throws NamingException, SQLException {
         JNDILookup jndi = JNDILookup.getFactory();
         Connection conn = jndi.lookUpDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 "select id, symbol, code " +
                 "  from currency");
         ResultSet result = stmt.executeQuery();
         Vector results = new Vector();
         while (result.next()) {
             int currencyId = result.getInt(1);
             // ensure that the vector will have space for this currency
             if (results.size() < currencyId) {
                 results.setSize(currencyId);
             }
             String symbol = result.getString(2);
             String code = result.getString(3);
             CurrencyDTO bean = new CurrencyDTO(0, symbol, code, null);
             results.add(result.getInt(1), bean);
         }
         result.close();
         stmt.close();
         conn.close();
         CurrencyDTO[] retValue = new CurrencyDTO[results.size()];
         
         return (CurrencyDTO []) results.toArray(retValue);
     }
     
     public CurrencyDTO[] getCurrencies(Integer languageId, 
             Integer entityId) 
             throws NamingException, SQLException {
         List result = new ArrayList();
         
         CurrencyDTO[] all = getSymbols();
         for (int f = 1; f < all.length; f++) {
             Integer currencyId = new Integer(f);
             set(currencyId);
             CurrencyDTO newCurrency = new CurrencyDTO();
             newCurrency.setId(currencyId);
             newCurrency.setName(currency.getDescription(languageId));
             // find the system rate
             if (f == 1) {
                 newCurrency.setSysRate(new BigDecimal("1.0"));
             } else {
                 newCurrency.setSysRate(new CurrencyExchangeDAS().findExchange(new Integer(0), currencyId).getRate());
             }
             // may be there's an entity rate
             EntityBL en = new EntityBL(entityId);
             CurrencyExchangeDTO exchange = new CurrencyExchangeDAS().findExchange(entityId,
                     currencyId);
             if (exchange != null) {
                 newCurrency.setRate(exchange.getRate().toString());
             }
             // let's see if this currency is in use by this entity
             newCurrency.setInUse(new Boolean(entityHasCurrency(entityId, 
                     currencyId)));
             result.add(newCurrency);
         }
         CurrencyDTO[] retValue = new CurrencyDTO[result.size()];
         return (CurrencyDTO[]) result.toArray(retValue);
     }
     
     public void setCurrencies(Integer entityId, CurrencyDTO[] currencies) 
             throws NamingException, ParseException {
         EntityBL entity = new EntityBL(entityId);
 
         // start by wiping out the existing data for this entity
         entity.getEntity().getCurrencies().clear();
         for (Iterator it = new CurrencyExchangeDAS().findByEntity(entityId).
                 iterator(); it.hasNext(); ) {
             CurrencyExchangeDTO exchange = 
                     (CurrencyExchangeDTO) it.next();
             new CurrencyExchangeDAS().delete(exchange);
         }
         
         for (int f = 0; f < currencies.length; f++) {
             if (currencies[f].getInUse().booleanValue()) {
                 set(currencies[f].getId());
                 
                 entity.getEntity().getCurrencies().add(new CurrencyDAS().find(currency.getId()));
 
                 if (currencies[f].getRate() != null) {
                     CurrencyExchangeDTO exchange = new CurrencyExchangeDTO();
                     exchange.setCreateDatetime(Calendar.getInstance().getTime());
                     exchange.setCurrency(new CurrencyDAS().find(currencies[f].getId()));
                     exchange.setEntityId(entityId);
                     exchange.setRate(currencies[f].getRateAsDecimal());
                     new CurrencyExchangeDAS().save(exchange);
                 }
             }
         }
     }
     
     public Integer getEntityCurrency(Integer entityId) {
         CompanyDTO entity = new CompanyDAS().find(entityId);
         return entity.getCurrencyId();
     }
     
     public void setEntityCurrency(Integer entityId, Integer currencyId) {
         CompanyDTO entity = new CompanyDAS().find(entityId);
         entity.setCurrency(new CurrencyDAS().find(currencyId));
     }
     
     /**
      * Ok, this is cheating, but heck is easy and fast.
      * @param entityId
      * @param currencyId
      * @return
      * @throws SQLException
      * @throws NamingException
      */
     private boolean entityHasCurrency(Integer entityId, Integer currencyId) 
             throws SQLException, NamingException {
         boolean retValue = false;
         JNDILookup jndi = JNDILookup.getFactory();
         Connection conn = jndi.lookUpDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 "select 1 " +
                 "  from currency_entity_map " +
                 " where currency_id = ? " +
                 "   and entity_id = ?");
         stmt.setInt(1, currencyId.intValue());
         stmt.setInt(2, entityId.intValue());
         ResultSet result = stmt.executeQuery();
         if (result.next()) {
             retValue = true;
         }
         result.close();
         stmt.close();
         conn.close();
         
         return retValue;
     }
 }
