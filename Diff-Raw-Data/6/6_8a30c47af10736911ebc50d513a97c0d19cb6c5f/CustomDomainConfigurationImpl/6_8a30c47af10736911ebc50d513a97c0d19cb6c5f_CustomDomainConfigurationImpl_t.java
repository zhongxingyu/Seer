 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.beans.configuration;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.configuration.Parameter;
 import com.flexive.shared.configuration.ParameterData;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoadException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.interfaces.CustomDomainConfigurationEngine;
 import com.flexive.shared.FxContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.Serializable;
 import java.util.Map;
import org.apache.commons.lang.StringUtils;
 
 /**
  * Extension of {@link GenericConfigurationImpl} configurations with an arbitrary domain field
  * (e.g. user ID).
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  * @since 3.1
  */
 public abstract class CustomDomainConfigurationImpl<T extends Serializable> extends GenericConfigurationImpl implements CustomDomainConfigurationEngine<T> {
     private static final Log LOG = LogFactory.getLog(CustomDomainConfigurationImpl.class);
     private static final String CTX_DOMAIN_OVERRIDE = "CUSTOMDOMAINCONFIG_OVERRIDE";
 
     private final String configurationName;
     private final String idColumn;
     private final String tableName;
     private final boolean enableCaching;
 
     protected CustomDomainConfigurationImpl(String configurationName, String tableName, String idColumn, boolean enableCaching) {
         this.configurationName = configurationName;
         this.idColumn = idColumn;
         this.tableName = tableName;
         this.enableCaching = enableCaching;
     }
 
     protected abstract T getCurrentDomain();
 
     protected abstract void setDomain(PreparedStatement stmt, int column, T domain) throws SQLException;
     
     protected abstract T getDomain(ResultSet rs, int column) throws SQLException;
 
     protected abstract boolean mayUpdate();
 
     protected abstract boolean mayUpdateForeignDomains();
 
     protected abstract boolean mayListDomains();
 
     protected final void setDomain(PreparedStatement stmt, int column) throws SQLException {
         setDomain(stmt, column, getInvokeDomain());
     }
 
     /**
      * Override the current domain for a method call.
      *
      * @param <ResultType>  the method result type
      */
     private abstract class DomainOverride<ResultType> {
         private final T domain;
 
         protected DomainOverride(T domain) {
             this.domain = domain;
         }
 
         public ResultType invoke() throws FxApplicationException {
             try {
                 setDomainOverride(domain);
                 return performAction();
             } finally {
                 removeDomainOverride();
             }
         }
 
         protected abstract ResultType performAction() throws FxApplicationException;
     }
     
     @Override
     protected final Connection getConnection() throws SQLException {
         return Database.getDbConnection();
     }
 
     @Override
     protected final PreparedStatement getSelectStatement(Connection conn, String path, String key) throws SQLException {
         final String sql = "SELECT cvalue FROM " + tableName + " WHERE " + idColumn + "=? AND cpath=? AND ckey=?";
         final PreparedStatement stmt = conn.prepareStatement(sql);
         setDomain(stmt, 1);
         stmt.setString(2, path);
         stmt.setString(3, key);
         return stmt;
     }
 
     @Override
     protected final PreparedStatement getSelectStatement(Connection conn, String path) throws SQLException {
         final String sql = "SELECT ckey, cvalue FROM " + tableName + " WHERE " + idColumn + "=? AND cpath=?";
         final PreparedStatement stmt = conn.prepareStatement(sql);
         setDomain(stmt, 1);
         stmt.setString(2, path);
         return stmt;
     }
 
     @Override
     protected final PreparedStatement getSelectStatement(Connection conn) throws SQLException {
         final String sql = "SELECT cpath, ckey, cvalue, classname FROM " + tableName + " WHERE " + idColumn + "=?";
         final PreparedStatement stmt = conn.prepareStatement(sql);
         setDomain(stmt, 1);
         return stmt;
     }
 
     @Override
     protected final PreparedStatement getUpdateStatement(Connection conn, String path, String key, String value, String className) throws SQLException, FxNoAccessException {
         if (!mayUpdate()) {
             throw new FxNoAccessException("ex.configuration.update.perm", configurationName);
         }
         final String sql = "UPDATE " + tableName + " SET cvalue=?, className=? WHERE " + idColumn + "=? AND cpath=? AND ckey=?";
         final PreparedStatement stmt = conn.prepareStatement(sql);
         StorageManager.setBigString(stmt, 1, value);
         stmt.setString(2, className);
         setDomain(stmt, 3);
         stmt.setString(4, path);
         stmt.setString(5, key);
         return stmt;
     }
 
     @Override
     protected final PreparedStatement getInsertStatement(Connection conn, String path, String key, String value, String className) throws SQLException, FxNoAccessException {
         if (!mayUpdate()) {
             throw new FxNoAccessException("ex.configuration.update.perm", configurationName);
         }
         final String sql = "INSERT INTO " + tableName + "(" + idColumn + ", cpath, ckey, cvalue, className) VALUES (?, ?, ?, ?, ?)";
         final PreparedStatement stmt = conn.prepareStatement(sql);
         setDomain(stmt, 1);
         stmt.setString(2, path);
         stmt.setString(3, key);
         StorageManager.setBigString(stmt, 4, value);
         stmt.setString(5, className);
         return stmt;
     }
 
     @Override
     protected final PreparedStatement getDeleteStatement(Connection conn, String path, String key) throws SQLException, FxNoAccessException {
         if (!mayUpdate()) {
             throw new FxNoAccessException("ex.configuration.delete.perm", configurationName);
         }
         final String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + "=? AND cpath=? "
                 + (key != null ? " AND ckey=?" : "");
         final PreparedStatement stmt = conn.prepareStatement(sql);
         setDomain(stmt, 1);
         stmt.setString(2, path);
         if (key != null) {
             stmt.setString(3, key);
         }
         return stmt;
     }
 
     @Override
     protected String getCachePath(String path) {
        if (enableCaching && StringUtils.isNotBlank(path)) {
            return "/" + configurationName + "Config/" + getInvokeDomain() + (path.startsWith("/") ? path : "/" + path);
         } else {
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public <PT extends Serializable> void put(final T domain, final Parameter<PT> parameter, final String key, final PT value) throws FxApplicationException {
         new DomainOverride<Object>(domain) {
             @Override
             protected Object performAction() throws FxApplicationException {
                 put(parameter, key, value);
                 return null;
             }
         }.invoke();
     }
 
     /**
      * {@inheritDoc}
      */
     public <PT extends Serializable> void remove(final T domain, final Parameter<PT> parameter, final String key) throws FxApplicationException {
         new DomainOverride<Object>(domain) {
             @Override
             public Object performAction() throws FxApplicationException {
                 remove(parameter, key);
                 return null;
             }
         }.invoke();
     }
 
     /**
      * {@inheritDoc}
      */
     public <PT extends Serializable> void removeAll(final T domain, final Parameter<PT> parameter) throws FxApplicationException {
         new DomainOverride<Object>(domain) {
             @Override
             public Object performAction() throws FxApplicationException {
                 removeAll(parameter);
                 return null;
             }
         }.invoke();
     }
 
     /**
      * {@inheritDoc}
      */
     public <PT extends Serializable> PT get(final T domain, final Parameter<PT> parameter, final String key, final boolean ignoreDefault) throws FxApplicationException {
         return new DomainOverride<PT>(domain) {
             @Override
             protected PT performAction() throws FxApplicationException {
                 return get(parameter, key, ignoreDefault);
             }
         }.invoke();
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<ParameterData, Serializable> getAll(T domain) throws FxApplicationException {
         return new DomainOverride<Map<ParameterData, Serializable>>(domain) {
             @Override
             protected Map<ParameterData, Serializable> performAction() throws FxApplicationException {
                 return getAll();
             }
         }.invoke();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<T> getDomains(Parameter parameter) throws FxApplicationException {
         return getMatchingDomains(parameter.getPath().getValue(), parameter.getKey());
     }
 
     /**
      * {@inheritDoc}
      */
     public List<T> getDomains(Parameter parameter, String key) throws FxApplicationException {
         return getMatchingDomains(parameter.getPath().getValue(), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<T> getDomains() throws FxApplicationException {
         return getMatchingDomains(null, null);
     }
 
     private List<T> getMatchingDomains(String path, String key) throws FxApplicationException {
         if (!mayListDomains()) {
             throw new FxNoAccessException("ex.configuration.listDomains.perm", configurationName);
         }
         final String sql = "SELECT DISTINCT " + idColumn
                 + " FROM " + tableName
                 + (path != null ? " WHERE cpath=?" + (key != null ? " AND ckey=?" : "")
                   : "");
         Connection conn = null;
         PreparedStatement stmt = null;
         try {
             conn = getConnection();
             stmt = conn.prepareStatement(sql);
             if (path != null) {
                 stmt.setString(1, path);
                 if (key != null) {
                     stmt.setString(2, key);
                 }
             }
             final ResultSet rs = stmt.executeQuery();
             final List<T> result = new ArrayList<T>();
             while (rs.next()) {
                 @SuppressWarnings({"unchecked"}) final T id = getDomain(rs, 1);
                 result.add(id);
             }
             return result;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(getClass(), conn, stmt);
         }
     }
 
     /**
      * Store the given domain in the current thread's FxContext attribute map. This will override
      * the value returned by {@link #getCurrentDomain()} for all invocations on this implementation.
      *
      * @param domain the configuration domain
      * @throws FxNoAccessException if the caller may not influence the configuration domain
      */
     private void setDomainOverride(T domain) throws FxNoAccessException {
         if (!domain.equals(getCurrentDomain()) && !mayUpdateForeignDomains()) {
             throw new FxNoAccessException("ex.configuration.modify.foreign", domain, getCurrentDomain());
         }
         FxContext.get().setAttribute(CTX_DOMAIN_OVERRIDE, domain);
     }
 
     private void removeDomainOverride() {
         FxContext.get().setAttribute(CTX_DOMAIN_OVERRIDE, null);
     }
 
     private T getInvokeDomain() {
         @SuppressWarnings({"unchecked"})
         final T override = (T) FxContext.get().getAttribute(CTX_DOMAIN_OVERRIDE);
         return override != null ? override : getCurrentDomain();
     }
 }
