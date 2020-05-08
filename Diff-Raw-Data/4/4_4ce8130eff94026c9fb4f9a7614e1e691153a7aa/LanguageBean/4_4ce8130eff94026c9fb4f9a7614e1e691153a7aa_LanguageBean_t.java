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
 package com.flexive.ejb.beans;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.LanguageEngine;
 import com.flexive.shared.interfaces.LanguageEngineLocal;
 import com.flexive.shared.mbeans.FxCacheMBean;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.ejb.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.flexive.core.DatabaseConst.*;
 
 /**
  * [fleXive] language engine interface.
  * This engine should not be used to load languages as they are available from the environment!
  * Its purpose is to enable/disable, initially load and manage (position, etc.) languages.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 
 @Stateless(name = "LanguageEngine", mappedName = "LanguageEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class LanguageBean implements LanguageEngine, LanguageEngineLocal {
 
     private static final Log LOG = LogFactory.getLog(LanguageBean.class);
 
     /**
      * All tables that have references to languages and the referencing column
      */
     private final static String[][] LANG_USAGE = {
             {TBL_ACCOUNTS, "LANG"},
             {TBL_ACLS + ML, "LANG"},
             {TBL_WORKFLOW_STEPDEFINITION + ML, "LANG"},
             {TBL_STRUCT_TYPES + ML, "LANG"},
             {TBL_STRUCT_GROUPS + ML, "LANG"},
             {TBL_STRUCT_DATATYPES + ML, "LANG"},
             {TBL_STRUCT_SELECTLIST + ML, "LANG"},
             {TBL_STRUCT_SELECTLIST_ITEM + ML, "LANG"},
             {TBL_STRUCT_PROPERTIES + ML, "LANG"},
             {TBL_STRUCT_ASSIGNMENTS, "DEFLANG"},
             {TBL_STRUCT_ASSIGNMENTS + ML, "LANG"},
             {TBL_CONTENT, "MAINLANG"},
             {TBL_CONTENT_DATA, "LANG"},
             {TBL_CONTENT_DATA_FT, "LANG"}
     };
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxLanguage load(long languageId) throws FxApplicationException {
         try {
             FxLanguage lang = (FxLanguage) CacheAdmin.getInstance().get(CacheAdmin.LANGUAGES_ID, languageId);
             if (lang == null) {
                loadAll(true, true);
                lang = (FxLanguage) CacheAdmin.getInstance().get(CacheAdmin.LANGUAGES_ID, languageId);
            }
            if (lang == null) {
                 //check unavailable
                 for (FxLanguage l : loadAll(false, false)) {
                     if (l.getId() == languageId)
                         return l;
                 }
                 throw new FxInvalidLanguageException("ex.language.invalid", languageId);
             }
             return lang;
         } catch (FxCacheException e) {
             throw new FxLoadException(LOG, e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxLanguage load(String languageIsoCode) throws FxApplicationException {
         try {
             if (StringUtils.isBlank(languageIsoCode) || languageIsoCode.length() != 2)
                 throw new FxInvalidLanguageException("ex.language.invalid", languageIsoCode);
             FxLanguage lang = (FxLanguage) CacheAdmin.getInstance().get(CacheAdmin.LANGUAGES_ISO, languageIsoCode);
             if (lang == null) {
                 //check unavailable
                 String check = languageIsoCode.toLowerCase();
                 for (FxLanguage l : loadAll(false, false)) {
                     if (l.getIso2digit().equals(check))
                         return l;
                 }
                 throw new FxInvalidLanguageException("ex.language.invalid", languageIsoCode);
             }
             return lang;
         } catch (FxCacheException e) {
             throw new FxLoadException(LOG, e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxLanguage> loadAvailable() throws FxApplicationException {
         try {
             List<FxLanguage> available = (List<FxLanguage>) CacheAdmin.getInstance().get(CacheAdmin.LANGUAGES_ALL, "id");
             if (available == null) {
                 loadAll(true, true);
                 available = (List<FxLanguage>) CacheAdmin.getInstance().get(CacheAdmin.LANGUAGES_ALL, "id");
                 if (available == null)
                     throw new FxInvalidLanguageException("ex.language.loadFailed");
             }
             return available;
         } catch (FxCacheException e) {
             throw new FxLoadException(LOG, e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxLanguage> loadDisabled() throws FxApplicationException {
         return loadAll(false, false);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxLanguage> loadAvailable(boolean excludeSystemLanguage) throws FxApplicationException {
         List<FxLanguage> tmp = loadAvailable();
         ArrayList<FxLanguage> result = new ArrayList<FxLanguage>();
         for (FxLanguage lang : tmp) {
             if (excludeSystemLanguage && lang.getId() == 0) continue;
             result.add(lang);
         }
         return result;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public boolean isValid(long languageId) {
         // Does the language exist at all? Check via constructor
         try {
             load(languageId);
         } catch (FxApplicationException exc) {
             return false;
         }
         return true;
     }
 
     /**
      * Initial load function.
      *
      * @param used      load used or unused languages?
      * @param add2cache put loaded languages into cache?
      * @return list containing requested languages
      */
     private synchronized List<FxLanguage> loadAll(boolean used, boolean add2cache) {
         String sql = "SELECT l.LANG_CODE, l.ISO_CODE, t.LANG, t.DESCRIPTION FROM " + TBL_LANG + " l, " +
                 TBL_LANG + ML + " t " +
                 "WHERE t.LANG_CODE=l.LANG_CODE AND l.INUSE=" + StorageManager.getBooleanExpression(used) +
                 " ORDER BY l.DISPPOS ASC, l.LANG_CODE ASC";
         Connection con = null;
         Statement stmt = null;
         List<FxLanguage> alLang = new ArrayList<FxLanguage>(140);
         try {
             con = Database.getDbConnection();
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             Map<Long, String> hmMl = new HashMap<Long, String>(5);
             int lang_code = -1;
             String iso_code = null;
             FxCacheMBean cache = CacheAdmin.getInstance();
             while (rs != null && rs.next()) {
                 if (lang_code != rs.getInt(1)) {
                     if (lang_code != -1 && lang_code != FxLanguage.SYSTEM_ID) {
                         //add
                         FxLanguage lang = new FxLanguage(lang_code, iso_code, new FxString(FxLanguage.DEFAULT_ID, hmMl), true);
                         if (add2cache) {
                             cache.put(CacheAdmin.LANGUAGES_ID, lang.getId(), lang);
                             cache.put(CacheAdmin.LANGUAGES_ISO, lang.getIso2digit(), lang);
                         }
                         alLang.add(lang);
                     }
                     lang_code = rs.getInt(1);
                     iso_code = rs.getString(2);
                     hmMl.clear();
                 }
                 hmMl.put(rs.getLong(3), rs.getString(4));
             }
             if (lang_code != -1 && lang_code != FxLanguage.SYSTEM_ID) {
                 //add
                 FxLanguage lang = new FxLanguage(lang_code, iso_code, new FxString(FxLanguage.DEFAULT_ID, hmMl), true);
                 if (add2cache && used) {
                     cache.put(CacheAdmin.LANGUAGES_ID, lang.getId(), lang);
                     cache.put(CacheAdmin.LANGUAGES_ISO, lang.getIso2digit(), lang);
                 }
                 alLang.add(lang);
             }
             if (add2cache && used) {
                 cache.put(CacheAdmin.LANGUAGES_ALL, "id", alLang);
             }
         } catch (SQLException e) {
             LOG.error(e, e);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         } finally {
             Database.closeObjects(LanguageBean.class, con, stmt);
         }
         return alLang;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void activateLanguage(FxLanguage language) throws FxApplicationException {
         List<FxLanguage> available = loadAvailable();
         if (available.contains(language)) {
             LOG.info("Language " + language + " is already active.");
             return;
         }
         available.add(language);
         setAvailable(available, false);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setAvailable(List<FxLanguage> available, boolean ignoreUsage) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.GlobalSupervisor);
         Connection con = null;
         PreparedStatement ps = null;
         if (available == null || available.size() == 0)
             throw new FxInvalidParameterException("available", "ex.language.noAvailable");
         try {
             con = Database.getDbConnection();
             if (!ignoreUsage) {
                 List<FxLanguage> orgLang = loadAvailable(true);
                 boolean found;
                 for (FxLanguage org : orgLang) {
                     found = false;
                     for (FxLanguage tmp : available) {
                         if (tmp.getId() == org.getId()) {
                             found = true;
                             break;
                         }
                     }
                     if (!found && hasUsages(con, org))
                         throw new FxInvalidParameterException("available", "ex.language.removeUsed", org.getLabel());
                 }
             }
             ps = con.prepareStatement("UPDATE " + TBL_LANG + " SET INUSE=?, DISPPOS=?");
             ps.setBoolean(1, false);
             ps.setNull(2, java.sql.Types.INTEGER);
             ps.executeUpdate();
             ps.close();
             int pos = 0;
             ps = con.prepareStatement("UPDATE " + TBL_LANG + " SET INUSE=?, DISPPOS=? WHERE LANG_CODE=?");
             ps.setBoolean(1, true);
             for (FxLanguage lang : available) {
                 ps.setInt(2, pos++);
                 ps.setLong(3, lang.getId());
                 ps.addBatch();
             }
             ps.executeBatch();
             StructureLoader.updateLanguages(FxContext.get().getDivisionId(), loadAll(true, true));
         } catch (FxCacheException e) {
             LOG.error(e, e);
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(LanguageBean.class, con, ps);
         }
     }
 
     /**
      * Check if the given language is referenced from a table
      *
      * @param con      an open and valid connection
      * @param language the language to check
      * @return if the language is in use
      * @throws FxApplicationException on errors
      * @throws SQLException           on errors
      */
     private boolean hasUsages(Connection con, FxLanguage language) throws FxApplicationException, SQLException {
         System.out.println("checking removed language " + language.getLabel().getBestTranslation());
         PreparedStatement ps = null;
         try {
             ResultSet rs;
             for (String[] check : LANG_USAGE) {
                 if (ps != null)
                     ps.close();
                 ps = con.prepareStatement("SELECT COUNT(*) FROM " + check[0] + " WHERE " + check[1] + "=?");
                 ps.setLong(1, language.getId());
                 rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     if (rs.getLong(1) > 0) {
                         LOG.info("Language [" + language.getIso2digit() + "] has [" + rs.getLong(1) + "] usages in table " + check[0] + ", column " + check[1]);
                         return true;
                     }
                 }
             }
         } finally {
             if (ps != null)
                 ps.close();
         }
         return false;
     }
 }
 
