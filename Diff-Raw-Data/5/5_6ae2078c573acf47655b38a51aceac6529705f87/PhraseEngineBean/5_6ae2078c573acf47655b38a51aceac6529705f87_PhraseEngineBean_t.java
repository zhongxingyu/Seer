 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2012
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
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.PhraseEngine;
 import com.flexive.shared.interfaces.PhraseEngineLocal;
 import com.flexive.shared.interfaces.SequencerEngineLocal;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.value.FxString;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.flexive.core.DatabaseConst.*;
 
 /**
  * Phrase engine implementation
  *
  * @author Markus Plesser (markus.plesser@ucs.at), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @since 3.2.0
  */
 @SuppressWarnings("UnusedDeclaration")
 @Stateless(name = "PhraseEngine", mappedName = "PhraseEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class PhraseEngineBean implements PhraseEngine, PhraseEngineLocal {
     // Our logger
     private static final Log LOG = LogFactory.getLog(PhraseEngineBean.class);
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
     /**
      * Fetch the next phrase id for the requested mandator
      *
      * @param mandatorId mandator id
      * @return next phrase id
      * @throws FxApplicationException on errors
      */
     private long fetchNextPhraseId(long mandatorId) throws FxApplicationException {
         final String SEQ_NAME = "PhraseSeq_" + mandatorId;
         if (!seq.sequencerExists(SEQ_NAME))
             seq.createSequencer(SEQ_NAME, false, 1);
         return seq.getId(SEQ_NAME);
     }
 
     /**
      * Remove the phrase sequencer for the requested mandator
      *
      * @param mandatorId mandator
      */
     private void removePhraseSequencer(long mandatorId) {
         final String SEQ_NAME = "PhraseSeq_" + mandatorId;
         try {
             if (seq.sequencerExists(SEQ_NAME))
                 seq.removeSequencer(SEQ_NAME);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * Fetch the next phrase tree node id for the requested mandator
      *
      * @param mandatorId mandator id
      * @param catalog    catalog id
      * @return next phrase tree node id
      */
     private long fetchNextNodeId(long mandatorId, int catalog) {
         final String SEQ_NAME = "PhraseNodeSeq_" + mandatorId + "_" + catalog;
         try {
             if (!seq.sequencerExists(SEQ_NAME))
                 seq.createSequencer(SEQ_NAME, false, 1);
             return seq.getId(SEQ_NAME);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * Remove the node sequencer for the requested mandator
      *
      * @param mandatorId mandator
      * @param catalog    catalog id
      */
     private void removeNodeSequencer(long mandatorId, int catalog) {
         final String SEQ_NAME = "PhraseNodeSeq_" + mandatorId + "_" + catalog;
         try {
             if (seq.sequencerExists(SEQ_NAME))
                 seq.removeSequencer(SEQ_NAME);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * @param categories FxPhraseCategorySelection
      * @return categories as comma separated String list
      */
     private String categoriesList(FxPhraseCategorySelection categories) {
         return FxArrayUtils.toStringArray(categories.getCategories(), ',');
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void syncPhraseSequencer(long mandatorId) {
         FxContext.startRunningAsSystem();
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT MAX(ID) FROM " + TBL_PHRASE + " WHERE MANDATOR=?");
             ps.setLong(1, mandatorId);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 long max = rs.getLong(1) + 1;
                 final String SEQ_NAME = "PhraseSeq_" + mandatorId;
                 if (seq.sequencerExists(SEQ_NAME))
                     seq.removeSequencer(SEQ_NAME);
                 seq.createSequencer(SEQ_NAME, false, max);
             }
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e.asRuntimeException();
         } finally {
             FxContext.stopRunningAsSystem();
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void syncPhraseNodeSequencer(long mandatorId) {
         FxContext.startRunningAsSystem();
         final String seqPrefix = "PhraseNodeSeq_" + mandatorId + "_";
         try {
             for (String sname : seq.getCustomSequencerNames()) {
                 if (!sname.startsWith(seqPrefix))
                     continue;
                 int category = Integer.parseInt(sname.substring(seqPrefix.length()));
                 syncPhraseNodeSequencer(category, mandatorId);
             }
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e.asRuntimeException();
         } finally {
             FxContext.stopRunningAsSystem();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void syncPhraseNodeSequencer(int category, long mandatorId) {
         FxContext.startRunningAsSystem();
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT MAX(ID) FROM " + TBL_PHRASE_TREE + " WHERE MANDATOR=? AND CAT=?");
             ps.setLong(1, mandatorId);
             ps.setInt(2, category);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 long max = rs.getLong(1) + 1;
                 final String SEQ_NAME = "PhraseNodeSeq_" + mandatorId + "_" + category;
                 if (seq.sequencerExists(SEQ_NAME))
                     seq.removeSequencer(SEQ_NAME);
                 seq.createSequencer(SEQ_NAME, false, max);
             }
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw e.asRuntimeException();
         } finally {
             FxContext.stopRunningAsSystem();
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * Check if the userTicket belongs to the mandator or is global supervisor
      *
      * @param mandator   mandator id to check
      * @param userTicket calling users ticket
      * @throws FxNoAccessException no access if not global supervisor
      */
     private void checkMandatorAccess(long mandator, UserTicket userTicket) throws FxNoAccessException {
         Mandator man = CacheAdmin.getEnvironment().getMandator(mandator); //will throw an exception if the mandator does not exist
         if (mandator != userTicket.getMandatorId() && !userTicket.isGlobalSupervisor())
             throw new FxNoAccessException("ex.phrases.noAccess.mandator", man.getName());
     }
 
     /**
      * Check if a phrase key is valid, will throw an exception if invalid
      *
      * @param key phrase key to check
      */
     private void checkPhraseKey(String key) {
         if (StringUtils.isBlank(key))
             throw new FxApplicationException("ex.phrase.key.empty").asRuntimeException();
         if (!key.equals(key.trim()))
             throw new FxApplicationException("ex.phrase.key.trim", key).asRuntimeException();
         if (key.length() > 250)
             throw new FxApplicationException("ex.phrase.key.tooLong", key).asRuntimeException();
         if (!StringUtils.isAsciiPrintable(key) || key.contains("%"))
             throw new FxApplicationException("ex.phrase.key.nonAscii", key).asRuntimeException();
     }
 
     /**
      * Check mandator filtering
      *
      * @param mandators mandators to check
      * @return mandators or the current users mandator if none was provided
      */
     private long[] checkMandatorFiltering(long[] mandators) {
         if (mandators == null || mandators.length == 0)
             return new long[]{FxContext.getUserTicket().getMandatorId()};
         if (mandators.length > 2)
             throw new FxApplicationException("ex.phrase.mandatorFilter.limited", 2).asRuntimeException();
         if (mandators.length == 2 && mandators[0] == mandators[1])
             return new long[]{mandators[0]};
         return mandators;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(String phraseKey, FxString value, long mandator) throws FxNoAccessException {
         return savePhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, value, null, null, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(int category, String phraseKey, FxString value, long mandator) throws FxNoAccessException {
         return savePhrase(category, phraseKey, value, null, null, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(String phraseKey, FxString value, FxPhraseSearchValueConverter converter, long mandator) throws FxNoAccessException {
         return savePhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, value, converter, null, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(String phraseKey, FxString value, Object tag, long mandator) throws FxNoAccessException {
         return savePhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, value, null, tag, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(int category, String phraseKey, FxString value, Object tag, long mandator) throws FxNoAccessException {
         return savePhrase(category, phraseKey, value, null, tag, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(String phraseKey, FxString value, FxPhraseSearchValueConverter converter, Object tag, long mandator) throws FxNoAccessException {
         return savePhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, value, converter, tag, mandator);
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long savePhrase(int category, String phraseKey, FxString value, FxPhraseSearchValueConverter converter, Object tag, long mandator) throws FxNoAccessException {
         Connection con = null;
         PreparedStatement ps = null;
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(mandator, userTicket);
         checkPhraseKey(phraseKey);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             long phraseId;
             ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE + " WHERE PKEY=? AND MANDATOR=? AND CAT=?");
             ps.setString(1, phraseKey);
             ps.setLong(2, mandator);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 phraseId = rs.getLong(1);
                 rs.close();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE ID=? AND MANDATOR=?");
                 ps.setLong(1, phraseId);
                 ps.setLong(2, mandator);
                 ps.executeUpdate();
             } else {
                 try {
                     phraseId = fetchNextPhraseId(mandator);
                 } catch (FxApplicationException e) {
                     EJBUtils.rollback(ctx);
                     throw e.asRuntimeException();
                 }
                 ps.close();
                 ps = con.prepareStatement("INSERT INTO " + TBL_PHRASE + "(ID,PKEY,MANDATOR,HID,CAT)VALUES(?,?,?,?,?)");
                 ps.setLong(1, phraseId);
                 ps.setString(2, phraseKey);
                 ps.setLong(3, mandator);
                 ps.setBoolean(4, false);
                 ps.setInt(5, category);
                 ps.executeUpdate();
             }
             if (!value.isEmpty()) {
                 ps.close();
                 ps = con.prepareStatement("INSERT INTO " + TBL_PHRASE_VALUES + "(ID,MANDATOR,LANG,PVAL,SVAL,TAG)VALUES(?,?,?,?,?,?)");
                 ps.setLong(1, phraseId);
                 ps.setLong(2, mandator);
                 FxString fxTag = tag instanceof FxString ? (FxString) tag : null;
                 for (long lang : value.getTranslatedLanguages()) {
                     ps.setLong(3, lang);
                     final String translation = value.getTranslation(lang);
                     if (StringUtils.isBlank(translation))
                         continue;
                     ps.setString(4, translation);
                     if (converter != null)
                         ps.setString(5, converter.convert(translation, lang));
                     else
                         ps.setString(5, translation.trim().toUpperCase());
                     if (fxTag != null) {
                         if (!fxTag.isMultiLanguage() || fxTag.translationExists(lang))
                             ps.setString(6, fxTag.getTranslation(lang));
                         else
                             ps.setNull(6, Types.VARCHAR);
                     } else {
                         if (tag != null && !StringUtils.isBlank(String.valueOf(tag)))
                             ps.setString(6, String.valueOf(tag));
                         else
                             ps.setNull(6, Types.VARCHAR);
                     }
                     ps.addBatch();
                 }
                 ps.executeBatch();
             }
             return phraseId;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setPhraseHidden(String phraseKey, long mandator, boolean hidden) throws FxNoAccessException {
         setPhraseHidden(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, mandator, hidden);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setPhraseHidden(int category, String phraseKey, long mandator, boolean hidden) throws FxNoAccessException {
         Connection con = null;
         PreparedStatement ps = null;
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(mandator, userTicket);
         checkPhraseKey(phraseKey);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("UPDATE " + TBL_PHRASE + " SET HID=? WHERE PKEY=? AND MANDATOR=? AND CAT=?");
             ps.setBoolean(1, hidden);
             ps.setString(2, phraseKey);
             ps.setLong(3, mandator);
             ps.setInt(4, category);
             ps.executeUpdate();
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean removePhrase(String phraseKey, long mandator) throws FxNoAccessException, FxEntryInUseException {
         return removePhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean removePhrase(int category, String phraseKey, long mandator) throws FxNoAccessException, FxEntryInUseException {
         Connection con = null;
         PreparedStatement ps = null;
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(mandator, userTicket);
         checkPhraseKey(phraseKey);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE + " WHERE PKEY=? AND MANDATOR=? AND CAT=?");
             ps.setString(1, phraseKey);
             ps.setLong(2, mandator);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 long id = rs.getLong(1);
                 rs.close();
                 ps.close();
                 ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE PHRASEID=? AND PMANDATOR=? AND CAT=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.setInt(3, category);
                 ResultSet rsCheck = ps.executeQuery();
                 if (rsCheck != null && rsCheck.next())
                     throw new FxEntryInUseException("ex.phrase.inUse.tree", phraseKey);
                 if (rsCheck != null)
                     rsCheck.close();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE ID=? AND MANDATOR=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE PHRASEID=? AND PMANDATOR=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.setInt(3, category);
                 ps.execute();
                 return true;
             }
             return false;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int removePhrases(String phraseKeyPrefix, long mandator) throws FxNoAccessException, FxEntryInUseException {
         return removePhrases(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKeyPrefix, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int removePhrases(int category, String phraseKeyPrefix, long mandator) throws FxNoAccessException, FxEntryInUseException {
         Connection con = null;
         PreparedStatement psResolve = null, ps = null;
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(mandator, userTicket);
         checkPhraseKey(phraseKeyPrefix);
         int count = 0;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             psResolve = con.prepareStatement("SELECT ID,PKEY FROM " + TBL_PHRASE + " WHERE PKEY LIKE ? AND MANDATOR=? AND CAT=?");
             psResolve.setString(1, phraseKeyPrefix + "%");
             psResolve.setLong(2, mandator);
             psResolve.setInt(3, category);
             ResultSet rsResolve = psResolve.executeQuery();
             while (rsResolve != null && rsResolve.next()) {
                 long id = rsResolve.getLong(1);
                 String key = rsResolve.getString(2);
                 if (ps != null)
                     ps.close();
                 ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE PHRASEID=? AND PMANDATOR=? AND CAT=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.setInt(3, category);
                 ResultSet rsCheck = ps.executeQuery();
                 if (rsCheck != null && rsCheck.next())
                     throw new FxEntryInUseException("ex.phrase.inUse.tree", key);
                 if (rsCheck != null)
                     rsCheck.close();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE ID=? AND MANDATOR=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE PHRASEID=? AND PMANDATOR=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
                 ps.setLong(1, id);
                 ps.setLong(2, mandator);
                 ps.setInt(3, category);
                 count += ps.executeUpdate();
             }
             if (rsResolve != null)
                 rsResolve.close();
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, ps);
             Database.closeObjects(PhraseEngineBean.class, con, psResolve);
         }
         return count;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int removeMandatorPhrases(long mandator) throws FxNoAccessException, FxEntryInUseException {
         return removeMandatorPhrases(FxPhraseCategorySelection.SELECTION_ANY, mandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int removeMandatorPhrases(FxPhraseCategorySelection categories, long mandator) throws FxNoAccessException, FxEntryInUseException {
         Connection con = null;
         PreparedStatement ps = null;
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(mandator, userTicket);
         int count = 0;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             if (categories.isAny()) {
                 ps = con.prepareStatement("SELECT COUNT(ID) FROM " + TBL_PHRASE_TREE + " WHERE PMANDATOR=?");
                 ps.setLong(1, mandator);
             } else {
                 ps = con.prepareStatement("SELECT COUNT(ID) FROM " + TBL_PHRASE_TREE + " WHERE PMANDATOR=? AND CAT IN(" + categoriesList(categories) + ")");
                 ps.setLong(1, mandator);
             }
             ResultSet rsCheck = ps.executeQuery();
             if (rsCheck != null && rsCheck.next()) {
                 long used = rsCheck.getLong(1);
                 if (used > 0)
                     throw new FxEntryInUseException("ex.phrase.inUse.tree.count", used);
             }
             if (rsCheck != null)
                 rsCheck.close();
             ps.close();
             if (categories.isAny()) {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE MANDATOR=?");
                 ps.setLong(1, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE PMANDATOR=?");
                 ps.setLong(1, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE MANDATOR=?");
                 ps.setLong(1, mandator);
                 count = ps.executeUpdate();
             } else {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE MANDATOR=? AND ID IN(SELECT DISTINCT ID FROM " + TBL_PHRASE + " WHERE MANDATOR=? AND CAT IN(" + categoriesList(categories) + "))");
                 ps.setLong(1, mandator);
                 ps.setLong(2, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE PMANDATOR=? AND PHRASEID IN(SELECT DISTINCT ID FROM " + TBL_PHRASE + " WHERE MANDATOR=? AND CAT IN(" + categoriesList(categories) + "))");
                 ps.setLong(1, mandator);
                 ps.execute();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE MANDATOR=? AND CAT IN(" + categoriesList(categories) + ")");
                 ps.setLong(1, mandator);
                 count = ps.executeUpdate();
             }
             removePhraseSequencer(mandator); //force a re-initialization on next use/call
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
         return count;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String loadPhraseValue(long language, String phraseKey, long... mandators) throws FxNotFoundException {
         Connection con = null;
         PreparedStatement ps = null;
         checkPhraseKey(phraseKey);
         if (mandators == null || mandators.length == 0)
             mandators = new long[]{FxContext.getUserTicket().getMandatorId()};
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT v.PVAL FROM " + TBL_PHRASE_VALUES + " v, " + TBL_PHRASE +
                     " p WHERE p.PKEY=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR AND v.LANG=?");
             ps.setString(1, phraseKey);
             ps.setLong(3, language);
             for (long mandator : mandators) {
                 ps.setLong(2, mandator);
                 ResultSet rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     return rs.getString(1);
                 }
                 if (rs != null)
                     rs.close();
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
         throw new FxNotFoundException("ex.phrase.key.notFound", phraseKey);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxString loadPhraseValue(String phraseKey, long... mandators) throws FxNotFoundException {
         Connection con = null;
         PreparedStatement ps = null;
         checkPhraseKey(phraseKey);
         if (mandators == null || mandators.length == 0)
             mandators = new long[]{FxContext.getUserTicket().getMandatorId()};
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT v.LANG, v.PVAL FROM " + TBL_PHRASE_VALUES + " v, " + TBL_PHRASE +
                     " p WHERE p.PKEY=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             ps.setString(1, phraseKey);
             for (long mandator : mandators) {
                 ps.setLong(2, mandator);
                 ResultSet rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     boolean ml = rs.getLong(1) != FxLanguage.SYSTEM_ID;
                     FxString val = new FxString(ml, rs.getLong(1), rs.getString(2));
                     while (rs.next())
                         val.setTranslation(rs.getLong(1), rs.getString(2));
                     return val;
                 }
                 if (rs != null)
                     rs.close();
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
         throw new FxNotFoundException("ex.phrase.key.notFound", phraseKey);
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhrase loadPhrase(String phraseKey, long... mandators) throws FxNotFoundException {
         return loadPhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, mandators);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhrase loadPhrase(int category, String phraseKey, long... mandators) throws FxNotFoundException {
         Connection con = null;
         PreparedStatement ps = null;
         checkPhraseKey(phraseKey);
         if (mandators == null || mandators.length == 0)
             mandators = new long[]{FxContext.getUserTicket().getMandatorId()};
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT v.LANG,v.PVAL,v.TAG,p.ID,p.HID FROM " + TBL_PHRASE_VALUES + " v, " + TBL_PHRASE +
                     " p WHERE p.PKEY=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR AND p.CAT=?");
             ps.setString(1, phraseKey);
             ps.setInt(3, category);
             for (long mandator : mandators) {
                 ps.setLong(2, mandator);
                 ResultSet rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     final long id = rs.getLong(4);
                     final boolean hidden = rs.getBoolean(5);
                     boolean ml = rs.getLong(1) != FxLanguage.SYSTEM_ID;
                     FxString val = new FxString(ml, rs.getLong(1), rs.getString(2));
                     FxString fxTag;
                     String tag = rs.getString(3);
                     boolean hasTag = false;
                     if (rs.wasNull())
                         fxTag = new FxString(FxString.EMPTY).setEmpty();
                     else {
                         fxTag = new FxString(ml, rs.getLong(1), tag);
                         hasTag = true;
                     }
                     while (rs.next()) {
                         val.setTranslation(rs.getLong(1), rs.getString(2));
                         if (hasTag)
                             fxTag.setTranslation(rs.getLong(1), rs.getString(3));
                     }
                     return new FxPhrase(mandator, phraseKey, val, fxTag).setId(id).flagHidden(hidden).flagCategory(category);
                 }
                 if (rs != null)
                     rs.close();
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
         throw new FxNotFoundException("ex.phrase.key.notFound", phraseKey);
     }
 
     /**
      * Directly fetch a phrase from a result set (first next() hast to be issued prior to the call)
      * Positions:
      * 1 .. id
      * 2 .. lang
      * 3 .. val
      * 4 .. tag
      * 5 .. mandator
      * 6 .. key
      * 7 .. hidden
      * 8 .. category
      *
      * @param rs ResultSet
      * @return FxPhrase
      * @throws SQLException on errors
      */
     private FxPhrase loadPhrase(ResultSet rs) throws SQLException {
         final long id = rs.getLong(1);
         boolean ml = rs.getLong(2) != FxLanguage.SYSTEM_ID;
         FxString val = new FxString(ml, rs.getLong(2), rs.getString(3));
         FxString fxTag;
         String tag = rs.getString(4);
         boolean hasTag = false;
         if (rs.wasNull())
             fxTag = new FxString(FxString.EMPTY).setEmpty();
         else {
             fxTag = new FxString(ml, rs.getLong(2), tag);
             hasTag = true;
         }
         final long mandator = rs.getLong(5);
         final String key = rs.getString(6);
         final boolean hidden = rs.getBoolean(7);
         final int category = rs.getInt(8);
         while (rs.next()) {
             val.setTranslation(rs.getLong(2), rs.getString(3));
             if (hasTag)
                 fxTag.setTranslation(rs.getLong(2), rs.getString(4));
         }
         return new FxPhrase(mandator, key, val, fxTag).setId(id).flagHidden(hidden).flagCategory(category);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxPhrase> loadPhrases(String phraseKeyPrefix, long... _mandators) {
         return loadPhrases(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKeyPrefix, _mandators);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxPhrase> loadPhrases(int category, String phraseKeyPrefix, long... _mandators) {
         Connection con = null;
         PreparedStatement ps = null;
         checkPhraseKey(phraseKeyPrefix);
         long[] mandators = checkMandatorFiltering(_mandators);
 
         List<FxPhrase> result = Lists.newArrayListWithExpectedSize(50);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             if (mandators.length == 1) {
                 //                                  1     2       3           4       5       6      7
                 ps = con.prepareStatement("SELECT r.ID, r.PKEY, r.MANDATOR, v.LANG, v.PVAL, v.TAG, r.HID FROM " + TBL_PHRASE_VALUES + " v, " + TBL_PHRASE +
                         " r WHERE r.PKEY LIKE ? AND r.MANDATOR=? AND r.CAT=? AND v.ID=r.ID AND v.MANDATOR=r.MANDATOR ORDER BY r.PKEY");
                 ps.setString(1, phraseKeyPrefix + "%");
                 ps.setLong(2, mandators[0]);
                 ps.setInt(3, category);
             } else {
                 //                                  1     2       3           4       5       6      7
                 ps = con.prepareStatement("SELECT r.ID, r.PKEY, r.MANDATOR, v.LANG, v.PVAL, v.TAG, r.HID FROM " + TBL_PHRASE_VALUES + " v, " + TBL_PHRASE +
                         //                    1                 2               3                                                                                    4            5
                         " r WHERE r.PKEY LIKE ? AND (r.MANDATOR=? OR(r.MANDATOR=? AND NOT EXISTS(SELECT r2.ID FROM FX_PHRASE r2 WHERE r2.PKEY=r.PKEY AND r2.MANDATOR=? AND r2.CAT=?)))" +
                         //         6
                         "AND r.CAT=? AND v.ID=r.ID AND v.MANDATOR=r.MANDATOR ORDER BY r.PKEY");
                 ps.setString(1, phraseKeyPrefix + "%");
                 ps.setLong(2, mandators[0]);
                 ps.setLong(3, mandators[1]);
                 ps.setLong(4, mandators[0]);
                 ps.setInt(5, category);
                 ps.setInt(6, category);
             }
 
             ResultSet rs = ps.executeQuery();
 
             long currId = -1L;
             long currMandator;
             String currKey;
             FxPhrase currPhrase = null;
 
             boolean ml;
             FxString val = null;
             Boolean hasTag = null;
             FxString fxTag = null;
 
             while (rs != null && rs.next()) {
                 final long lang = rs.getLong(4);
                 if (currId != rs.getLong(1)) {
                     if (currPhrase != null)
                         result.add(currPhrase);
                     currId = rs.getLong(1);
                     currKey = rs.getString(2);
                     currMandator = rs.getLong(3);
                     ml = lang != FxLanguage.SYSTEM_ID;
                     final String tag = rs.getString(6);
                     hasTag = rs.wasNull();
                     fxTag = hasTag ? new FxString(ml, lang, tag) : new FxString(FxString.EMPTY).setEmpty();
                     val = new FxString(ml, lang, rs.getString(5));
                     currPhrase = new FxPhrase(currMandator, currKey, val, fxTag).flagCategory(category).flagHidden(rs.getBoolean(7));
                 } else {
                     val.setTranslation(lang, rs.getString(5));
                     if (hasTag)
                         fxTag.setTranslation(lang, rs.getString(6));
                 }
             }
             if (currPhrase != null)
                 result.add(currPhrase);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clearPhrases(long mandatorId) throws FxNoAccessException, FxEntryInUseException {
         checkMandatorAccess(mandatorId, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE MANDATOR=?");
             ps.setLong(1, mandatorId);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE MANDATOR=?");
             ps.setLong(1, mandatorId);
             ps.executeUpdate();
             removePhraseSequencer(mandatorId);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clearPhrases(FxPhraseCategorySelection categories, long mandatorId) throws FxNoAccessException, FxEntryInUseException {
         if (categories.isAny()) {
             clearPhrases(mandatorId);
             return;
         }
         checkMandatorAccess(mandatorId, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_VALUES + " WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_PHRASE + " WHERE MANDATOR=? AND CAT IN (" + categoriesList(categories) + "))");
             ps.setLong(1, mandatorId);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE + " WHERE MANDATOR=? AND CAT IN (" + categoriesList(categories) + ")");
             ps.setLong(1, mandatorId);
             ps.executeUpdate();
             //sequencer can not be removed if categories is not any
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxPhraseTreeNode saveTreeNode(FxPhraseTreeNode node) throws FxNoAccessException, FxNotFoundException {
         Connection con = null;
         PreparedStatement ps = null;
 
         checkMandatorAccess(node.getMandatorId(), FxContext.getUserTicket());
         //load the node's phrase to check if it exists
         FxPhrase checkPhrase = loadPhrase(node.getPhrase().getCategory(), node.getPhrase().getKey(), node.getPhrase().getMandator());
         if (!checkPhrase.hasId())
             throw new FxNotFoundException("ex.phrases.noId");
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT ID, MANDATOR FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setInt(3, node.getCategory());
             boolean newNode = node.isNew();
             if (!node.isNew()) {
                 //check if the node has an id set but does not exist yet
                 ps.setLong(1, node.getId());
                 ps.setLong(2, node.getMandatorId());
                 ResultSet rs = ps.executeQuery();
                 newNode = !(rs != null && rs.next());
             }
             if (node.hasParent()) {
                 //check if the parent node exists
                 ps.setLong(1, node.getParentNodeId());
                 ps.setLong(2, node.getParentNodeMandatorId());
                 ResultSet rs = ps.executeQuery();
                 if (!(rs != null && rs.next()))
                     throw new FxNotFoundException("ex.phrase.tree.parent.notFound", node.getParentNodeId(), node.getParentNodeMandatorId());
 
             }
             ps.close();
             if (!newNode) {
                 ps = con.prepareStatement("UPDATE " + TBL_PHRASE_TREE + " SET PARENTID=?,PARENTMANDATOR=?,PHRASEID=?,PMANDATOR=?,POS=? WHERE ID=? AND MANDATOR=? AND CAT=?");
                 if (node.hasParent()) {
                     ps.setLong(1, node.getParentNodeId());
                     ps.setLong(2, node.getParentNodeMandatorId());
                 } else {
                     ps.setNull(1, Types.NUMERIC);
                     ps.setNull(2, Types.NUMERIC);
                 }
                 ps.setLong(3, checkPhrase.getId());
                 ps.setLong(4, checkPhrase.getMandator());
                 if (!node.hasPos())
                     node.setPos(getNextNodePos(con, node.getCategory(), node.getParentNodeId(), node.getParentNodeMandatorId(), node.getMandatorId()));
                 ps.setLong(5, node.getPos());
                 ps.setLong(6, node.getId());
                 ps.setLong(7, node.getMandatorId());
                 ps.setInt(8, node.getCategory());
                 ps.executeUpdate();
                 node.getPhrase().setId(checkPhrase.getId());
                 return node;
             } else {
                 ps = con.prepareStatement("INSERT INTO " + TBL_PHRASE_TREE + " (ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR,POS,CAT)VALUES(?,?,?,?,?,?,?,?)");
                 final long nodeId = node.isNew() ? fetchNextNodeId(node.getMandatorId(), node.getCategory()) : node.getId();
                 ps.setLong(1, nodeId);
                 ps.setLong(2, node.getMandatorId());
                 if (node.hasParent()) {
                     ps.setLong(3, node.getParentNodeId());
                     ps.setLong(4, node.getParentNodeMandatorId());
                 } else {
                     ps.setNull(3, Types.NUMERIC);
                     ps.setNull(4, Types.NUMERIC);
                 }
                 ps.setLong(5, checkPhrase.getId());
                 ps.setLong(6, checkPhrase.getMandator());
                 if (!node.hasPos())
                     node.setPos(getNextNodePos(con, node.getCategory(), node.getParentNodeId(), node.getParentNodeMandatorId(), node.getMandatorId()));
                 ps.setLong(7, node.getPos());
                 ps.setInt(8, node.getCategory());
                 ps.executeUpdate();
                 node.setId(nodeId);
                 node.getPhrase().setId(checkPhrase.getId());
                 return node;
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * Get the next available position for the requested node
      *
      * @param con                  an open and valid Connection
      * @param parentNodeId         id of the parent node
      * @param parentNodeMandatorId mandator id of the parent node
      * @param mandatorId           mandator id of the requested node
      * @return next available position for the requested node
      * @throws SQLException on errors
      */
     private long getNextNodePos(Connection con, int category, long parentNodeId, long parentNodeMandatorId, long mandatorId) throws SQLException {
         PreparedStatement ps = null;
         try {
             if (parentNodeId >= 0) {
                 ps = con.prepareStatement("SELECT MAX(POS) FROM " + TBL_PHRASE_TREE + " WHERE PARENTID=? AND PARENTMANDATOR=? AND MANDATOR=?");
                 ps.setLong(1, parentNodeId);
                 ps.setLong(2, parentNodeMandatorId);
                 ps.setLong(3, mandatorId);
             } else {
                 ps = con.prepareStatement("SELECT MAX(POS) FROM " + TBL_PHRASE_TREE + " WHERE PARENTID IS NULL AND MANDATOR=?");
                 ps.setLong(1, parentNodeMandatorId);
             }
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return rs.getLong(1) + 1;
             }
             return 0;
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setPhraseTreeNodeParent(long nodeId, long nodeMandator, long parentId, long parentMandator) throws FxNoAccessException, FxNotFoundException {
         Connection con = null;
         PreparedStatement ps = null;
 
         checkMandatorAccess(nodeMandator, FxContext.getUserTicket());
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //check if the node and the parent node exists
             ps = con.prepareStatement("SELECT ID, MANDATOR, CAT FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, nodeMandator);
             ResultSet rs = ps.executeQuery();
             if (!(rs != null && rs.next()))
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, nodeMandator);
             final int nodeCategory = rs.getInt(3);
             final int parentCategory;
             if (parentId != FxPhraseTreeNode.NOT_SET) {
                 ps.setLong(1, parentId);
                 ps.setLong(2, parentMandator);
                 rs = ps.executeQuery();
                 if (!(rs != null && rs.next()))
                     throw new FxNotFoundException("ex.phrase.tree.parent.notFound", parentId, parentMandator);
                 parentCategory = rs.getInt(3);
             } else
                 parentCategory = nodeCategory;
             if (nodeCategory != parentCategory)
                 throw new FxInvalidParameterException("parent", "ex.phrase.tree.category.mismatch").asRuntimeException();
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_PHRASE_TREE + " SET PARENTID=?,PARENTMANDATOR=? WHERE ID=? AND MANDATOR=?");
             if (parentId != FxPhraseTreeNode.NOT_SET) {
                 ps.setLong(1, parentId);
                 ps.setLong(2, parentMandator);
             } else {
                 ps.setNull(1, Types.NUMERIC);
                 ps.setNull(1, Types.NUMERIC);
             }
             ps.setLong(3, nodeId);
             ps.setLong(4, nodeMandator);
             ps.executeUpdate();
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxPhraseTreeNode> loadPhraseTree(boolean mandator2top, long... _mandators) {
         return loadPhraseTree(FxPhraseCategorySelection.CATEGORY_DEFAULT, mandator2top, _mandators);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxPhraseTreeNode> loadPhraseTree(int category, boolean mandator2top, long... _mandators) {
         Connection con = null;
         PreparedStatement ps = null;
         PreparedStatement psPhrase = null;
         long[] mandators = checkMandatorFiltering(_mandators);
         if (mandators.length == 2 && mandator2top) {
             long tmp = mandators[0];
             mandators[0] = mandators[1];
             mandators[1] = tmp;
         }
         //load the node's phrase to check if it exists
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             List<FxPhraseTreeNode> nodes = Lists.newArrayListWithExpectedSize(5);
             //                                1  2        3        4              5        6         7   8
             ps = con.prepareStatement("SELECT ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR,POS,CAT FROM " + TBL_PHRASE_TREE +
                     " WHERE MANDATOR=? AND CAT=? AND PARENTID IS NULL AND PARENTMANDATOR IS NULL ORDER BY POS");
             /* 1 .. id 2 .. lang 3 .. val 4 .. tag 5 .. mandator 6 .. key 7 .. hidden 8 .. category*/
             psPhrase = con.prepareStatement("SELECT p.ID,v.LANG,v.PVAL,v.TAG,p.MANDATOR,p.PKEY,p.HID,p.CAT FROM " + TBL_PHRASE + " p, " +
                     TBL_PHRASE_VALUES + " v WHERE p.ID=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             ps.setInt(2, category);
             for (long mandator : mandators) {
                 ps.setLong(1, mandator);
                 ResultSet rs = ps.executeQuery();
                 while (rs != null && rs.next()) {
                     psPhrase.setLong(1, rs.getLong(5));
                     psPhrase.setLong(2, rs.getLong(6));
                     ResultSet rsPhrase = psPhrase.executeQuery();
                     if (rsPhrase == null || !rsPhrase.next())
                         throw new FxNotFoundException("ex.phrases.notFound.id", rs.getLong(5), rs.getLong(6)).asRuntimeException();
                     nodes.add(new FxPhraseTreeNode(rs.getLong(1), rs.getLong(2), rs.getInt(8), FxPhraseTreeNode.NOT_SET, FxPhraseTreeNode.NOT_SET, loadPhrase(rsPhrase), null).setPos(rs.getLong(7)));
                     rsPhrase.close();
                 }
                 if (rs != null)
                     rs.close();
             }
             if (nodes.size() > 0) {
                 ps.close();
                 ps = con.prepareStatement("SELECT ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR,POS,CAT FROM " + TBL_PHRASE_TREE +
                         " WHERE MANDATOR=? AND PARENTID=? AND PARENTMANDATOR=? AND CAT=? ORDER BY POS");
                 ps.setInt(4, category);
                 for (FxPhraseTreeNode node : nodes)
                     loadChildren(node, mandators, ps, psPhrase);
             }
             return nodes;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, psPhrase);
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhraseTreeNode loadPhraseTreeNode(int category, long nodeId, long mandatorId, boolean mandator2top, long... _mandators) throws FxNotFoundException {
         Connection con = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             return loadPhraseTreeNode(con, true, category, nodeId, mandatorId, mandator2top, _mandators);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhraseTreeNode loadPhraseTreeNode(long nodeId, long mandatorId, boolean mandator2top, long... _mandators) throws FxNotFoundException {
         return loadPhraseTreeNode(FxPhraseCategorySelection.CATEGORY_DEFAULT, nodeId, mandatorId, mandator2top, _mandators);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxPhraseTreeNode loadPhraseTreeNodeOnly(int category, long nodeId, long mandatorId) throws FxNotFoundException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return loadPhraseTreeNode(con, false, category, nodeId, mandatorId, false);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, null);
         }
     }
 
     private FxPhraseTreeNode loadPhraseTreeNode(Connection _con, boolean loadChildren, int category, long nodeId, long mandatorId, boolean mandator2top, long... _mandators) throws FxNotFoundException {
         PreparedStatement ps = null;
         PreparedStatement psPhrase = null;
         long[] mandators = checkMandatorFiltering(_mandators);
         if (mandators.length == 2 && mandator2top) {
             long tmp = mandators[0];
             mandators[0] = mandators[1];
             mandators[1] = tmp;
         }
         FxPhraseTreeNode node = null;
         Connection con = null;
         //load the node's phrase to check if it exists
         try {
             // Obtain a database connection
             con = _con == null ? Database.getDbConnection() : _con;
             //                                1  2        3        4              5        6         7   8
             ps = con.prepareStatement("SELECT ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR,POS,CAT FROM " + TBL_PHRASE_TREE +
                     " WHERE ID=? AND MANDATOR=? AND CAT=?");
             /* 1 .. id 2 .. lang 3 .. val 4 .. tag 5 .. mandator 6 .. key 7 .. hidden 8 .. category*/
             psPhrase = con.prepareStatement("SELECT p.ID,v.LANG,v.PVAL,v.TAG,p.MANDATOR,p.PKEY,p.HID,p.CAT FROM " + TBL_PHRASE + " p, " +
                     TBL_PHRASE_VALUES + " v WHERE p.ID=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 psPhrase.setLong(1, rs.getLong(5));
                 psPhrase.setLong(2, rs.getLong(6));
                 ResultSet rsPhrase = psPhrase.executeQuery();
                 if (rsPhrase == null || !rsPhrase.next())
                     throw new FxNotFoundException("ex.phrases.notFound.id", rs.getLong(5), rs.getLong(6)).asRuntimeException();
                 long parentId = rs.getLong(3);
                 if (rs.wasNull())
                     parentId = FxPhraseTreeNode.NOT_SET;
                 long parentMandatorId = rs.getLong(4);
                 if (rs.wasNull())
                     parentMandatorId = FxPhraseTreeNode.NOT_SET;
                 node = new FxPhraseTreeNode(rs.getLong(1), rs.getLong(2), rs.getInt(8), parentId, parentMandatorId, loadPhrase(rsPhrase), null).setPos(rs.getLong(7));
                 rsPhrase.close();
             }
             if (rs != null)
                 rs.close();
             if (node == null)
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, mandatorId);
             if (loadChildren) {
                 ps.close();
                 ps = con.prepareStatement("SELECT ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR,POS,CAT FROM " + TBL_PHRASE_TREE +
                         " WHERE MANDATOR=? AND PARENTID=? AND PARENTMANDATOR=? AND CAT=? ORDER BY POS");
                 ps.setInt(4, category);
                 loadChildren(node, mandators, ps, psPhrase);
             }
             return node;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             if (_con == null)
                 Database.closeObjects(PhraseEngineBean.class, con, null);
             Database.closeObjects(PhraseEngineBean.class, ps, psPhrase);
         }
     }
 
     private void loadChildren(FxPhraseTreeNode node, long[] mandators, PreparedStatement psNodes, PreparedStatement psPhrase) throws SQLException {
         for (long mandator : mandators) {
             psNodes.setLong(1, mandator);
             psNodes.setLong(2, node.getId());
             psNodes.setLong(3, node.getMandatorId());
             ResultSet rsNodes = psNodes.executeQuery();
             List<FxPhraseTreeNode> children = Lists.newArrayListWithCapacity(10);
             while (rsNodes != null && rsNodes.next()) {
                 psPhrase.setLong(1, rsNodes.getLong(5));
                 psPhrase.setLong(2, rsNodes.getLong(6));
                 ResultSet rsPhrase = psPhrase.executeQuery();
                 if (rsPhrase == null || !rsPhrase.next())
                     throw new FxNotFoundException("ex.phrases.notFound.id", rsNodes.getLong(5), rsNodes.getLong(6)).asRuntimeException();
                 children.add(new FxPhraseTreeNode(rsNodes.getLong(1), rsNodes.getLong(2), rsNodes.getInt(8), node.getId(), node.getMandatorId(), loadPhrase(rsPhrase), null).setPos(rsNodes.getLong(7)));
                 rsPhrase.close();
             }
             if (rsNodes != null)
                 rsNodes.close();
             node.setChildren(children);
             for (FxPhraseTreeNode child : children)
                 loadChildren(child, mandators, psNodes, psPhrase);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void moveTreeNode(int category, long nodeId, long mandatorId, int delta) throws FxNoAccessException, FxNotFoundException {
         if (delta == 0)
             return;
         checkMandatorAccess(mandatorId, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT PARENTID, PARENTMANDATOR FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, mandatorId);
             long parentId = rs.getLong(1);
             if (rs.wasNull())
                 parentId = -1L;
             long parentMandatorId = rs.getLong(2);
             if (rs.wasNull())
                 parentMandatorId = -1L;
             rs.close();
             ps.close();
             //0..node id, 1..pos
             List<Long> positions = Lists.newArrayListWithCapacity(10);
 
             if (parentId == -1L) {
                 ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE PARENTID IS NULL AND MANDATOR=? AND CAT=? ORDER BY POS");
                 ps.setLong(1, mandatorId);
                 ps.setInt(2, category);
             } else {
                 ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE PARENTID=? AND PARENTMANDATOR=? AND MANDATOR=? AND CAT=? ORDER BY POS");
                 ps.setLong(1, parentId);
                 ps.setLong(2, parentMandatorId);
                 ps.setLong(3, mandatorId);
                 ps.setInt(4, category);
             }
             rs = ps.executeQuery();
             long currPos = 1;
             int index = -1;
             while (rs != null && rs.next()) {
                 if (index == -1 && nodeId == rs.getLong(1))
                     index = (int) currPos - 1;
                 positions.add(rs.getLong(1));
                 currPos++;
             }
             if (positions.size() < 2) //only one node, can not change position
                 return;
             int newIndex = index + delta;
             if (newIndex < 0)
                 newIndex = 0;
             if (delta > 0)
                 newIndex++;
             if (newIndex > (positions.size() - 1))
                 newIndex = positions.size();
             positions.add(newIndex, nodeId);
             if (newIndex > index)
                 positions.remove(index);
             else
                 positions.remove(index + 1);
             //write back new positions
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_PHRASE_TREE + " SET POS=? WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(3, mandatorId);
             ps.setInt(4, category);
             for (int i = 1; i <= positions.size(); i++) {
                 ps.setLong(1, i);
                 ps.setLong(2, positions.get(i - 1));
                 ps.addBatch();
             }
             ps.executeBatch();
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void moveTreeNode(long nodeId, long mandatorId, int delta) throws FxNoAccessException, FxNotFoundException {
         moveTreeNode(FxPhraseCategorySelection.CATEGORY_DEFAULT, nodeId, mandatorId, delta);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeTreeNode(long nodeId, long mandatorId) throws FxNoAccessException, FxNotFoundException {
         removeTreeNode(FxPhraseCategorySelection.CATEGORY_DEFAULT, nodeId, mandatorId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeTreeNode(int category, long nodeId, long mandatorId) throws FxNoAccessException, FxNotFoundException {
         checkMandatorAccess(mandatorId, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null, psUpdate = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT PARENTID, PARENTMANDATOR FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, mandatorId);
             long parentId = rs.getLong(1);
             if (rs.wasNull())
                 parentId = -1L;
             long parentMandatorId = rs.getLong(2);
             if (rs.wasNull())
                 parentMandatorId = -1L;
             rs.close();
             ps.close();
             psUpdate = con.prepareStatement("UPDATE " + TBL_PHRASE_TREE + " SET PARENTID=?, PARENTMANDATOR=?, POS=? WHERE ID=? AND MANDATOR=? AND CAT=?");
             psUpdate.setInt(6, category);
             long currPos = getNextNodePos(con, category, parentId, parentMandatorId, mandatorId);
             ps = con.prepareStatement("SELECT ID, MANDATOR FROM " + TBL_PHRASE_TREE + " WHERE PARENTID=? AND PARENTMANDATOR=? AND CAT=? ORDER BY POS");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (parentId != -1L) {
                     psUpdate.setLong(1, parentId);
                     psUpdate.setLong(2, parentMandatorId);
                 } else {
                     psUpdate.setNull(1, Types.NUMERIC);
                     psUpdate.setNull(2, Types.NUMERIC);
                 }
                 psUpdate.setLong(3, currPos++);
                 psUpdate.setLong(4, rs.getLong(1));
                 psUpdate.setLong(5, rs.getLong(2));
                 psUpdate.executeUpdate();
             }
             if (rs != null)
                 rs.close();
             ps.close();
 
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE NODEID=? AND NODEMANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, mandatorId);
             ps.setInt(3, category);
             ps.executeUpdate();
             rebuildPhraseChildMapping(con, mandatorId, category, -1, -1);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, psUpdate);
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clearTree(long mandatorId) throws FxNoAccessException {
         clearTree(FxPhraseCategorySelection.SELECTION_ANY, mandatorId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clearTree(FxPhraseCategorySelection categories, long mandatorId) throws FxNoAccessException {
         checkMandatorAccess(mandatorId, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             if (categories.isAny()) {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=?");
                 ps.setLong(1, mandatorId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE NODEMANDATOR=?");
                 ps.setLong(1, mandatorId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE PMANDATOR=?");
                 ps.setLong(1, mandatorId);
                 ps.executeUpdate();
                 ps.close();
                 //fetch all categories for which node sequencers have to be removed
                 ps = con.prepareStatement("SELECT DISTINCT CAT FROM " + TBL_PHRASE_TREE + " WHERE MANDATOR=?");
                 ps.setLong(1, mandatorId);
                 ResultSet rs = ps.executeQuery();
                 List<Integer> cats = new ArrayList<Integer>(10);
                 while (rs != null && rs.next()) {
                     cats.add(rs.getInt(1));
                 }
                 if (rs != null)
                     rs.close();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_TREE + " WHERE MANDATOR=?");
                 ps.setLong(1, mandatorId);
                 ps.executeUpdate();
                 for (int cat : cats)
                     removeNodeSequencer(mandatorId, cat);
             } else {
                 final String cat = categoriesList(categories);
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE (MANDATOR=? OR NODEMANDATOR=? OR PMANDATOR=?) AND NODEID IN(SELECT DISTINCT ID FROM " + TBL_PHRASE_TREE + " WHERE MANDATOR=? AND CAT IN(" + cat + ")) AND CAT IN(" + cat + ")");
                 ps.setLong(1, mandatorId);
                 ps.setLong(2, mandatorId);
                 ps.setLong(3, mandatorId);
                 ps.setLong(4, mandatorId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_TREE + " WHERE MANDATOR=? AND CAT IN(" + cat + ")");
                 ps.setLong(1, mandatorId);
                 ps.executeUpdate();
                 for (int category : categories.getCategories())
                     removeNodeSequencer(mandatorId, category);
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrase(long assignmentOwner, long nodeId, long nodeMandator, long phraseId, long phraseMandator, long pos, boolean checkPositioning) throws FxNotFoundException, FxNoAccessException {
         assignPhrase(FxPhraseCategorySelection.CATEGORY_DEFAULT, assignmentOwner, nodeId, nodeMandator, phraseId, phraseMandator, pos, checkPositioning);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrase(int category, long assignmentOwner, long nodeId, long nodeMandator, long phraseId, long phraseMandator, long pos, boolean checkPositioning) throws FxNotFoundException, FxNoAccessException {
         checkMandatorAccess(assignmentOwner, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, nodeMandator);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !(rs.next()))
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, nodeMandator);
             ps.close();
             ps = con.prepareStatement("SELECT POS FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=? AND DIRECT=TRUE");
             ps.setLong(1, assignmentOwner);
             ps.setLong(2, nodeId);
             ps.setLong(3, nodeMandator);
             ps.setLong(4, phraseId);
             ps.setLong(5, phraseMandator);
             ps.setInt(6, category);
             rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 long orgPos = rs.getLong(1);
                 if (!rs.wasNull()) {
                     if (orgPos == pos)
                         return;
                     if (pos <= 1 && orgPos == 1)
                         return; //already at the top
                     updatePhrasePosition(con, category, assignmentOwner, nodeId, nodeMandator, phraseId, phraseMandator, pos, checkPositioning);
                     return;
                 }
             }
             //insert
             ps.close();
             //remove from phrase map in case it is already there as indirect
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND CAT=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=?");
             ps.setLong(1, assignmentOwner);
             ps.setInt(2, category);
             ps.setLong(3, nodeId);
             ps.setLong(4, nodeMandator);
             ps.setLong(5, phraseId);
             ps.setLong(6, phraseMandator);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement("INSERT INTO " + TBL_PHRASE_MAP + "(MANDATOR,CAT,NODEID,NODEMANDATOR,PHRASEID,PMANDATOR,POS,DIRECT)VALUES(?,?,?,?,?,?,?,?)");
             ps.setLong(1, assignmentOwner);
             ps.setInt(2, category);
             ps.setLong(3, nodeId);
             ps.setLong(4, nodeMandator);
             ps.setLong(5, phraseId);
             ps.setLong(6, phraseMandator);
             ps.setLong(7, pos);
             ps.setBoolean(8, true);
             ps.executeUpdate();
             if (checkPositioning)
                 updatePhrasePosition(con, category, assignmentOwner, nodeId, nodeMandator, phraseId, phraseMandator, pos, checkPositioning);
             rebuildPhraseChildMapping(con, assignmentOwner, category, phraseId, phraseMandator);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     private void updatePhrasePosition(Connection con, int category, long assignmentOwner, long nodeId, long nodeMandator, long phraseId, long phraseMandator, long pos, boolean checkPositioning) throws SQLException {
         PreparedStatement psUpdate = null, psFetch = null;
         try {
             //                                                                     1                2            3                  4              5               6         7
             psUpdate = con.prepareStatement("UPDATE " + TBL_PHRASE_MAP + " SET POS=? WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=?");
             psUpdate.setLong(2, assignmentOwner);
             psUpdate.setLong(3, nodeId);
             psUpdate.setLong(4, nodeMandator);
             psUpdate.setInt(7, category);
             if (checkPositioning) {
                 //                                                                                                         1            2                  3         4
                 psFetch = con.prepareStatement("SELECT POS, PHRASEID, PMANDATOR FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND CAT=? AND DIRECT=TRUE ORDER BY POS");
                 psFetch.setLong(1, assignmentOwner);
                 psFetch.setLong(2, nodeId);
                 psFetch.setLong(3, nodeMandator);
                 psFetch.setInt(4, category);
                 ResultSet rs = psFetch.executeQuery();
                 long currPos = 0;
                 while (rs != null && rs.next()) {
                     long _pos = rs.getLong(1);
                     long _phraseId = rs.getLong(2);
                     long _phraseMandatorId = rs.getLong(3);
 //                    if (phraseId == _phraseId && phraseMandator == _phraseMandatorId)
 //                        continue;
                     currPos++;
                     if (currPos == pos)
                         continue;
                     if (_pos == currPos)
                         continue;
                     psUpdate.setLong(1, currPos);
                     psUpdate.setLong(5, _phraseId);
                     psUpdate.setLong(6, _phraseMandatorId);
                     psUpdate.addBatch();
                 }
             }
             psUpdate.setLong(1, pos);
             psUpdate.setLong(5, phraseId);
             psUpdate.setLong(6, phraseMandator);
             psUpdate.addBatch();
             psUpdate.executeBatch();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, psFetch, psUpdate);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrase(int category, long assignmentOwner, long nodeId, long nodeMandator, String phraseKey, long phraseMandator, long pos, boolean checkPositioning) throws FxNotFoundException, FxNoAccessException {
         assignPhrase(category, assignmentOwner, nodeId, nodeMandator, loadPhrase(phraseKey, phraseMandator).getId(), phraseMandator, pos, checkPositioning);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrase(long assignmentOwner, long nodeId, long nodeMandator, String phraseKey, long phraseMandator, long pos, boolean checkPositioning) throws FxNotFoundException, FxNoAccessException {
         assignPhrase(assignmentOwner, nodeId, nodeMandator, loadPhrase(phraseKey, phraseMandator).getId(), phraseMandator, pos, checkPositioning);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrases(long position, long assignmentOwner, long nodeId, long nodeMandator, FxPhrase[] phrases) throws FxApplicationException {
         assignPhrases(FxPhraseCategorySelection.CATEGORY_DEFAULT, position, assignmentOwner, nodeId, nodeMandator, phrases);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void assignPhrases(int category, long position, long assignmentOwner, long nodeId, long nodeMandator, FxPhrase[] phrases) throws FxApplicationException {
         if (phrases == null || phrases.length == 0)
             return;
         checkMandatorAccess(assignmentOwner, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //check categories
             ps = con.prepareStatement("SELECT ID FROM " + TBL_PHRASE_TREE + " WHERE ID=? AND MANDATOR=? AND CAT=?");
             ps.setLong(1, nodeId);
             ps.setLong(2, nodeMandator);
             ps.setInt(3, category);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !(rs.next()))
                 throw new FxNotFoundException("ex.phrases.node.notFound.id", nodeId, nodeMandator);
             ps.close();
             long startPhraseId = -1, startPhraseMandator = -1;
             ps = con.prepareStatement("SELECT PHRASEID,PMANDATOR FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND POS>=? AND CAT=? AND DIRECT=TRUE ORDER BY POS ASC");
             ps.setLong(1, assignmentOwner);
             ps.setLong(2, nodeId);
             ps.setLong(3, nodeMandator);
             ps.setLong(4, position);
             ps.setInt(5, category);
             rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 long pid = rs.getLong(1);
                 long mid = rs.getLong(2);
                 if (!phrasesContains(phrases, pid, mid)) {
                     startPhraseId = pid;
                     startPhraseMandator = mid;
                     break;
                 }
             }
             ps.close();
             boolean useMaxPos = startPhraseId == -1 || startPhraseMandator == -1;
             //remove all contained phrases
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=? AND DIRECT=TRUE");
             ps.setLong(1, assignmentOwner);
             ps.setLong(2, nodeId);
             ps.setLong(3, nodeMandator);
             ps.setInt(6, category);
             for (FxPhrase rm : phrases) {
                 ps.setLong(4, rm.getId());
                 ps.setLong(5, rm.getMandator());
                 ps.addBatch();
             }
             ps.executeBatch();
             ps.close();
             //close gaps and reposition
             updatePhrasePosition(con, category, assignmentOwner, nodeId, nodeMandator, -1, -1, 0, true);
             int insertPos = -1;
             if (!useMaxPos) {
                 ps = con.prepareStatement("SELECT POS FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=? AND DIRECT=?");
                 ps.setLong(1, assignmentOwner);
                 ps.setLong(2, nodeId);
                 ps.setLong(3, nodeMandator);
                 ps.setLong(4, startPhraseId);
                 ps.setLong(5, startPhraseMandator);
                 ps.setInt(6, category);
                 ps.setBoolean(7, true);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next())
                     insertPos = rs.getInt(1);
                 ps.close();
             }
             if (insertPos == -1)
                 useMaxPos = true;
             if (!useMaxPos) {
                 //make room for the phrases to insert
                 ps = con.prepareStatement("UPDATE " + TBL_PHRASE_MAP + " SET POS=POS+" + (phrases.length) + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND POS>? AND CAT=? AND DIRECT=?");
                 ps.setLong(1, assignmentOwner);
                 ps.setLong(2, nodeId);
                 ps.setLong(3, nodeMandator);
                 ps.setLong(4, insertPos);
                 ps.setInt(5, category);
                 ps.setBoolean(6, true);
                 ps.executeUpdate();
                 ps.close();
             } else {
                 ps = con.prepareStatement("SELECT MAX(POS) FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND CAT=? AND DIRECT=?");
                 ps.setLong(1, assignmentOwner);
                 ps.setLong(2, nodeId);
                 ps.setLong(3, nodeMandator);
                 ps.setInt(4, category);
                 ps.setBoolean(5, true);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next())
                     insertPos = rs.getInt(1);
                 else
                     insertPos = 1; //fallback: first entry
                 ps.close();
             }
             ps = con.prepareStatement("INSERT INTO " + TBL_PHRASE_MAP + "(MANDATOR,CAT,NODEID,NODEMANDATOR,PHRASEID,PMANDATOR,POS,DIRECT)VALUES(?,?,?,?,?,?,?,?)");
             ps.setLong(1, assignmentOwner);
             ps.setInt(2, category);
             ps.setLong(3, nodeId);
             ps.setLong(4, nodeMandator);
             ps.setBoolean(8, true);
             for (FxPhrase phrase : phrases) {
                 ps.setLong(5, phrase.getId());
                 ps.setLong(6, phrase.getMandator());
                 ps.setLong(7, ++insertPos);
                 ps.addBatch();
             }
             ps.executeBatch();
             if(phrases.length > 10)
                 rebuildPhraseChildMapping(con, assignmentOwner, category, -1, -1);
             else {
                 for (FxPhrase phrase : phrases) {
                     rebuildPhraseChildMapping(con, assignmentOwner, category, phrase.getId(), phrase.getMandator());
                 }
             }
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     private boolean phrasesContains(FxPhrase[] phrases, long id, long mandator) {
         for (FxPhrase phrase : phrases) {
             if (phrase.getId() == id && phrase.getMandator() == mandator)
                 return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void buildPhraseChildMapping(long mandator, int category) {
         rebuildPhraseChildMapping(null, mandator, category, -1, -1);
     }
 
     private void rebuildPhraseChildMapping(Connection _con, long mandator, int category, long phraseId, long phraseMandator) {
         if(FxContext.preventPhraseTreeRebuild())
             return;
         /*
         - delete all non-direct mappings for mandator and category
         - for all direct nodes of mandator fetch:
           - nodeid, nodemandator, phraseid, phrasemandator
          */
         Connection con = _con;
         PreparedStatement ps = null, psNodeFetch = null, psNodeAdd = null, psNodeCheck = null;
         //Set<String> duplicateCheck = new HashSet<String>();
         try {
             // Obtain a database connection
             if(con == null)
                 con = Database.getDbConnection();
             boolean singlePhraseOnly = phraseId != -1 && phraseMandator != -1;
             if(singlePhraseOnly) {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND CAT=? AND DIRECT=? AND PHRASEID=? AND PMANDATOR=?");
                 ps.setLong(4, phraseId);
                 ps.setLong(5, phraseMandator);
             } else
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND CAT=? AND DIRECT=?");
             ps.setLong(1, mandator);
             ps.setInt(2, category);
             ps.setBoolean(3, false);
             ps.executeUpdate();
             ps.close();
             if(singlePhraseOnly) {
                 //                                1      2            3        4
                 ps = con.prepareStatement("SELECT NODEID,NODEMANDATOR,PHRASEID,PMANDATOR FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND CAT=? AND DIRECT=? AND PHRASEID=? AND PMANDATOR=?");
                 ps.setLong(4, phraseId);
                 ps.setLong(5, phraseMandator);
             } else {
                 //                                1      2            3        4
                 ps = con.prepareStatement("SELECT NODEID,NODEMANDATOR,PHRASEID,PMANDATOR FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND CAT=? AND DIRECT=?");
             }
             ps.setLong(1, mandator);
             ps.setInt(2, category);
             ps.setBoolean(3, true);
             ResultSet rs = ps.executeQuery();
             //                                                                    1      2            3        4              5        6   7
             psNodeAdd = con.prepareStatement("INSERT INTO " + TBL_PHRASE_MAP + " (NODEID,NODEMANDATOR,PHRASEID,PMANDATOR,MANDATOR,CAT,DIRECT)VALUES(?,?,?,?,?,?,?)");
             psNodeAdd.setLong(5, mandator);
             psNodeAdd.setInt(6, category);
             psNodeAdd.setBoolean(7, false);
             //                                         1  2        3        4              5        6
             psNodeFetch = con.prepareStatement("SELECT ID,MANDATOR,PARENTID,PARENTMANDATOR,PHRASEID,PMANDATOR FROM " + TBL_PHRASE_TREE +
                     " WHERE ID=? AND MANDATOR=? AND CAT=?");
             psNodeFetch.setInt(3, category);
             //                                                                                           1                  2              3               4              5         6
             psNodeCheck = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_PHRASE_MAP + " WHERE NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND MANDATOR=? AND CAT=?");
             psNodeCheck.setLong(5, mandator);
             psNodeCheck.setInt(6, category);
             while(rs != null && rs.next()) {
                 psNodeFetch.setLong(1, rs.getLong(1));
                 psNodeFetch.setLong(2, rs.getLong(2));
                 final long currPhraseId = rs.getLong(3);
                 final long currPhraseMandator = rs.getLong(4);
                 psNodeCheck.setLong(3, currPhraseId);
                 psNodeCheck.setLong(4, currPhraseMandator);
                 psNodeAdd.setLong(3, currPhraseId);
                 psNodeAdd.setLong(4, currPhraseMandator);
                 ResultSet rsNodeFetch = psNodeFetch.executeQuery();
                 if(rsNodeFetch != null && rsNodeFetch.next()) {
                     long nodeId = rsNodeFetch.getLong(3);
                     boolean hasParent = !rsNodeFetch.wasNull();
                     long nodeMandator = rsNodeFetch.getLong(4);
                     while(hasParent) {
                         //add indirect nodemapping
                         psNodeAdd.setLong(1, nodeId);
                         psNodeAdd.setLong(2, nodeMandator);
                         psNodeCheck.setLong(1, nodeId);
                         psNodeCheck.setLong(2, nodeMandator);
                         //final String key = nodeId + "-" + nodeMandator + "-" + rs.getLong(3) + "-" + rs.getLong(4);
                         ResultSet rsCheck = psNodeCheck.executeQuery();
                         if (rsCheck != null && rsCheck.next() && rsCheck.getLong(1) == 0) {
                             //if(!duplicateCheck.contains(key))
                             psNodeAdd.executeUpdate();
                            /* else {
                                 System.err.println("Duplicate: "+key);
                             }*/
 
                             //duplicateCheck.add(key);
                         }
                         psNodeFetch.setLong(1, rsNodeFetch.getLong(3));
                         psNodeFetch.setLong(2, rsNodeFetch.getLong(4));
                         rsNodeFetch = psNodeFetch.executeQuery();
                         if (rsNodeFetch != null && rsNodeFetch.next()) {
                             nodeId = rsNodeFetch.getLong(3);
                             hasParent = !rsNodeFetch.wasNull();
                             nodeMandator = rsNodeFetch.getLong(4);
                         } else
                             hasParent = false;
                     }
                 }
 
             }
 //            System.out.println("dup size:"+duplicateCheck.size());
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, psNodeAdd, psNodeFetch, psNodeCheck);
             if(_con == null)
                 Database.closeObjects(PhraseEngineBean.class, _con, ps);
             else
                 Database.closeObjects(PhraseEngineBean.class, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void moveTreeNodeAssignment(long assignmentOwner, long nodeId, long nodeMandatorId, long phraseId, long phraseMandator, int delta) throws FxNotFoundException, FxNoAccessException {
         moveTreeNodeAssignment(FxPhraseCategorySelection.CATEGORY_DEFAULT, assignmentOwner, nodeId, nodeMandatorId, phraseId, phraseMandator, delta);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void moveTreeNodeAssignment(int category, long assignmentOwner, long nodeId, long nodeMandatorId, long phraseId, long phraseMandator, int delta) throws FxNotFoundException, FxNoAccessException {
         if (delta == 0)
             return;
         checkMandatorAccess(assignmentOwner, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             List<Long> positionsId = Lists.newArrayListWithCapacity(50);
             List<Long> positionsMandator = Lists.newArrayListWithCapacity(50);
 
             //0..phrase id, 1..phrase mandator,2..pos
             ps = con.prepareStatement("SELECT PHRASEID,PMANDATOR,POS FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND CAT=? ORDER BY POS");
             ps.setLong(1, assignmentOwner);
             ps.setLong(2, nodeId);
             ps.setLong(3, nodeMandatorId);
             ps.setInt(4, category);
             ResultSet rs = ps.executeQuery();
             long currPos = 1;
             int index = -1;
             while (rs != null && rs.next()) {
                 if (index == -1 && phraseId == rs.getLong(1))
                     index = (int) currPos - 1;
                 positionsId.add(rs.getLong(1));
                 positionsMandator.add(rs.getLong(2));
                 currPos++;
             }
             if (positionsId.size() < 2 || index == -1) //only one node or node not found, can not change position
                 return;
             int newIndex = index + delta;
             if (newIndex < 0)
                 newIndex = 0;
             if (delta > 0)
                 newIndex++;
             if (newIndex > (positionsId.size() - 1))
                 newIndex = positionsId.size();
             positionsId.add(newIndex, phraseId);
             positionsMandator.add(newIndex, phraseMandator);
             if (newIndex > index) {
                 positionsId.remove(index);
                 positionsMandator.remove(index);
             } else {
                 positionsId.remove(index + 1);
                 positionsMandator.remove(index + 1);
             }
             //write back new positionsId
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_PHRASE_MAP + " SET POS=? WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=?");
             ps.setLong(2, assignmentOwner);
             ps.setLong(3, nodeId);
             ps.setLong(4, nodeMandatorId);
             ps.setInt(7, category);
             for (int i = 1; i <= positionsId.size(); i++) {
                 ps.setLong(1, i);
                 ps.setLong(5, positionsId.get(i - 1));
                 ps.setLong(6, positionsMandator.get(i - 1));
                 ps.addBatch();
             }
             ps.executeBatch();
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean removePhraseAssignment(long assignmentOwner, long nodeId, long nodeMandator, long phraseId, long phraseMandator) throws FxNoAccessException {
         return removePhraseAssignment(FxPhraseCategorySelection.CATEGORY_DEFAULT, assignmentOwner, nodeId, nodeMandator, phraseId, phraseMandator);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean removePhraseAssignment(int category, long assignmentOwner, long nodeId, long nodeMandator, long phraseId, long phraseMandator) throws FxNoAccessException {
         checkMandatorAccess(assignmentOwner, FxContext.getUserTicket());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND PHRASEID=? AND PMANDATOR=? AND CAT=?");
             ps.setLong(1, assignmentOwner);
             ps.setLong(2, nodeId);
             ps.setLong(3, nodeMandator);
             ps.setLong(4, phraseId);
             ps.setLong(5, phraseMandator);
             ps.setInt(6, category);
             boolean found = ps.executeUpdate() > 0;
             if (found) {
                 updatePhrasePosition(con, category, assignmentOwner, nodeId, nodeMandator, phraseId, phraseMandator, 0, true);
             }
             rebuildPhraseChildMapping(con, nodeMandator, category, phraseId, phraseMandator);
            if (nodeMandator != phraseMandator)
                 rebuildPhraseChildMapping(con, phraseMandator, category, phraseId, phraseMandator);
            else if (assignmentOwner != nodeMandator)
                rebuildPhraseChildMapping(con, assignmentOwner, category, phraseId, phraseMandator);
             return found;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignmentsFromNode(long nodeId, long nodeMandator) {
         removeAssignmentsFromNode(FxPhraseCategorySelection.CATEGORY_DEFAULT, nodeId, nodeMandator);
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignmentsFromNode(int category, long nodeId, long nodeMandator) {
         final long ownMandator = FxContext.getUserTicket().getMandatorId();
         final boolean removeAll = ownMandator == nodeMandator;
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             if (removeAll) {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE NODEID=? AND NODEMANDATOR=? AND CAT=?");
                 ps.setLong(1, nodeId);
                 ps.setLong(2, nodeMandator);
                 ps.setInt(3, category);
             } else {
                 ps = con.prepareStatement("DELETE FROM " + TBL_PHRASE_MAP + " WHERE MANDATOR=? AND NODEID=? AND NODEMANDATOR=? AND CAT=?");
                 ps.setLong(1, ownMandator);
                 ps.setLong(2, nodeId);
                 ps.setLong(3, nodeMandator);
                 ps.setInt(4, category);
             }
             ps.executeUpdate();
             rebuildPhraseChildMapping(con, ownMandator, category, -1, -1);
             if(!removeAll)
                 rebuildPhraseChildMapping(con, nodeMandator, category, -1, -1);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<FxPhraseTreeNodePosition> getAssignedNodes(String phraseKey, long... _mandators) {
         return getAssignedNodes(FxPhraseCategorySelection.CATEGORY_DEFAULT, phraseKey, _mandators);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<FxPhraseTreeNodePosition> getAssignedNodes(int category, String phraseKey, long... _mandators) {
         final long ownMandator = FxContext.getUserTicket().getMandatorId();
         Connection con = null;
         PreparedStatement ps = null;
         long[] mandators = checkMandatorFiltering(_mandators);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             List<FxPhraseTreeNodePosition> result = Lists.newArrayListWithCapacity(10);
             final String mandatorQuery = mandators.length == 1 ? "=" + mandators[0] : " IN(" + FxArrayUtils.toStringArray(mandators, ',') + ")";
             ps = con.prepareStatement("SELECT m.NODEID,m.NODEMANDATOR,m.POS FROM " + TBL_PHRASE_MAP +
                     " m," + TBL_PHRASE + " p WHERE p.PKEY=? AND p.MANDATOR" + mandatorQuery +
                     " AND p.CAT=? AND m.PHRASEID=p.ID AND m.CAT=p.CAT AND m.DIRECT=? AND m.MANDATOR" + mandatorQuery + " ORDER BY m.NODEMANDATOR,m.NODEID,m.POS");
             ps.setString(1, phraseKey);
             ps.setInt(2, category);
             ps.setBoolean(3, true);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 try {
                     result.add(new FxPhraseTreeNodePosition(loadPhraseTreeNode(con, false, category, rs.getLong(1), rs.getLong(2), false, mandators),
                             rs.getLong(3)));
                 } catch (FxNotFoundException e) {
                     LOG.error("Failed to load node " + rs.getLong(1) + " (mandator " + rs.getLong(2) + ") found! This should not be possible!");
                 }
             }
             return result;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<FxPhraseTreeNodePosition> getAssignedNodes(long phraseId, long phraseMandator, long... _mandators) {
         final long ownMandator = FxContext.getUserTicket().getMandatorId();
         Connection con = null;
         PreparedStatement ps = null;
         long[] mandators = checkMandatorFiltering(_mandators);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             List<FxPhraseTreeNodePosition> result = Lists.newArrayListWithCapacity(10);
             final String mandatorQuery = mandators.length == 1 ? "=" + mandators[0] : " IN(" + FxArrayUtils.toStringArray(mandators, ',') + ")";
             ps = con.prepareStatement("SELECT m.NODEID,m.NODEMANDATOR,m.POS,m.CAT FROM " + TBL_PHRASE_MAP +
                     " m WHERE m.PHRASEID=? AND m.PMANDATOR=? AND m.DIRECT=? AND m.MANDATOR" + mandatorQuery + " ORDER BY m.NODEMANDATOR,m.NODEID,m.POS");
             ps.setLong(1, phraseId);
             ps.setLong(2, phraseMandator);
             ps.setBoolean(3, true);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 try {
                     result.add(new FxPhraseTreeNodePosition(loadPhraseTreeNode(con, false, rs.getInt(4), rs.getLong(1), rs.getLong(2), false, mandators),
                             rs.getLong(3)));
                 } catch (FxNotFoundException e) {
                     LOG.error("Failed to load node " + rs.getLong(1) + " (mandator " + rs.getLong(2) + ") found! This should not be possible!");
                 }
             }
             return result;
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     ///////////////////////////////////
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhraseQueryResult search(FxPhraseQuery query) {
         return search(query, 1, Integer.MAX_VALUE);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxPhraseQueryResult search(FxPhraseQuery query, int page, int pageSize) {
         return query.isLanguageFallback() ? searchWithFallback(query, page, pageSize) : searchNoFallback(query, page, pageSize);
     }
 
     private FxPhraseQueryResult searchWithFallback(FxPhraseQuery query, int page, int pageSize) {
         Connection con = null;
         PreparedStatement ps = null, psPhrase = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             List<FxPhrase> phrases = Lists.newArrayListWithCapacity(pageSize);
             boolean isSortPos = false;
             final boolean treeNodeRestricted = query.isTreeNodeRestricted();
             if (query.getSortMode() == FxPhraseQuery.SortMode.POS_ASC || query.getSortMode() == FxPhraseQuery.SortMode.POS_DESC) {
                 if (treeNodeRestricted && !query.isIncludeChildNodes())
                     isSortPos = true;
                 else {
                     //sorting by position is not possible if child tree nodes are included, fall back to value sorting
                     if (query.getSortMode() == FxPhraseQuery.SortMode.POS_ASC)
                         query.setSortMode(FxPhraseQuery.SortMode.VALUE_ASC);
                     else if (query.getSortMode() == FxPhraseQuery.SortMode.POS_DESC)
                         query.setSortMode(FxPhraseQuery.SortMode.VALUE_DESC);
                 }
             }
 
             StringBuilder sql = new StringBuilder(2500);
             final String NVL = "COALESCE";
             String sub_restriction = "";
             if (query.isValueMatchRestricted()) {
                 sub_restriction = " AND s.SVAL";
                 String saveVal = query.getValueQueryNormalized().replaceAll("['\"\\\\]", "\\\\$0").toUpperCase();
                 switch (query.getValueMatchMode()) {
                     case CONTAINS:
                         sub_restriction = sub_restriction + " LIKE '%" + saveVal + "%'";
                         break;
                     case EXACT:
                         sub_restriction = sub_restriction + "='" + saveVal + "'";
                         break;
                     case STARTS_WITH:
                         sub_restriction = sub_restriction + " LIKE '" + saveVal + "%'";
                         break;
                 }
             }
             if (query.isTagMatchRestricted()) {
                 sub_restriction = sub_restriction + " AND UPPER(s.TAG)";
                 String saveVal = query.getTagQuery().replaceAll("['\"\\\\]", "\\\\$0").toUpperCase();
                 switch (query.getTagMatchMode()) {
                     case CONTAINS:
                         sub_restriction = sub_restriction + " LIKE '%" + saveVal + "%'";
                         break;
                     case EXACT:
                         sub_restriction = sub_restriction + "='" + saveVal + "'";
                         break;
                     case STARTS_WITH:
                         sub_restriction = sub_restriction + " LIKE '" + saveVal + "%'";
                         break;
                 }
             }
             final String searchValueSubQuery;
             if (query.isSearchLanguageRestricted())
                 searchValueSubQuery = "(" + NVL + "(\n" +
                         "(SELECT s.SVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR AND s.LANG=" + query.getSearchLanguage() + sub_restriction + "),\n" +
                         "(SELECT s.SVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1)\n" +
                         ")) AS SVAL";
             else
                 searchValueSubQuery = "(SELECT s.PVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1) AS SVAL ";
             final String resultValueSubQuery;
             if (query.isResultLanguageRestricted())
                 resultValueSubQuery = "(" + NVL + "(\n" +
                         "(SELECT s.PVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR AND s.LANG=" + query.getResultLanguage() + "),\n" +
                         "(SELECT s.PVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR LIMIT 1)\n" +
                         ")) AS PVAL";
             else
                 resultValueSubQuery = "(SELECT s.PVAL FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1) AS PVAL ";
             final String langSubQuery;
             if (query.isResultLanguageRestricted())
                 langSubQuery = "(" + NVL + "(\n" +
                         "(SELECT s.LANG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR AND s.LANG=" + query.getResultLanguage() + sub_restriction + "),\n" +
                         "(SELECT s.LANG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1)\n" +
                         ")) AS LANG";
             else
                 langSubQuery = "(SELECT s.LANG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1) AS LANG ";
 
             final String resultTagSubQuery;
             if (query.isResultLanguageRestricted())
                 resultTagSubQuery = "(" + NVL + "(\n" +
                         "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR AND s.LANG=" + query.getResultLanguage() + sub_restriction + "),\n" +
                         "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1)\n" +
                         ")) AS RTAG";
             else
                 resultTagSubQuery = "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1) AS RTAG ";
             final String searchTagSubQuery;
             if (query.isSearchLanguageRestricted())
                 searchTagSubQuery = "(" + NVL + "(\n" +
                         "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR AND s.LANG=" + query.getSearchLanguage() + "),\n" +
                         "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR LIMIT 1)\n" +
                         ")) AS STAG";
             else
                 searchTagSubQuery = "(SELECT s.TAG FROM " + TBL_PHRASE_VALUES + " s WHERE s.ID=p.ID AND s.MANDATOR=p.MANDATOR " + sub_restriction + " LIMIT 1) AS STAG ";
             //                 1  2        3    4    5    6    7    8    9
             sql.append("SELECT ID,MANDATOR,PKEY,PVAL,LANG,RTAG,SVAL,STAG,CAT");
             if (treeNodeRestricted) {
                 //9
                 sql.append(",MMANDATOR");
                 if (isSortPos) {
                     //10
                     sql.append(",POS");
                 }
             }
 
             sql.append(" FROM(");
             sql.append("SELECT DISTINCT p.ID AS ID,p.MANDATOR AS MANDATOR,p.PKEY AS PKEY,").append(resultValueSubQuery).
                     append(",").append(langSubQuery).append(",").append(resultTagSubQuery).
                     append(",").append(searchValueSubQuery).append(",").append(searchTagSubQuery).append(",p.CAT AS CAT");
             if (treeNodeRestricted) {
                 sql.append(",m.MANDATOR AS MMANDATOR");
                 if (isSortPos)
                     sql.append(",m.POS AS POS");
             }
             sql.append(" FROM ").
                     append(TBL_PHRASE).append(" p");
             if (treeNodeRestricted)
                 sql.append(", " + TBL_PHRASE_MAP + " m");
             sql.append(" WHERE ");
             final String categoryRestriction;
             if (treeNodeRestricted) {
                 if (query.getCategories().isSingleCategory())
                     categoryRestriction = "m.CAT=" + query.getCategories().getSingleCategory();
                 else
                     categoryRestriction = "m.CAT IN(" + categoriesList(query.getCategories()) + ")";
                 sql.append(categoryRestriction);
             } else {
                 if (query.getCategories().isSingleCategory())
                     categoryRestriction = "p.CAT=" + query.getCategories().getSingleCategory();
                 else
                     categoryRestriction = "p.CAT IN(" + categoriesList(query.getCategories()) + ")";
                 sql.append(categoryRestriction);
             }
             if (!query.isIncludeHidden())
                 sql.append(" AND p.HID=FALSE");
 
             if (query.isKeyMatchRestricted()) {
                 sql.append(" AND UPPER(p.PKEY)");
                 String saveVal = query.getKeyQuery().replaceAll("['\"\\\\]", "\\\\$0").toUpperCase();
                 switch (query.getKeyMatchMode()) {
                     case CONTAINS:
                         sql.append(" LIKE '%").append(saveVal).append("%'");
                         break;
                     case EXACT:
                         sql.append("='").append(saveVal).append("'");
                         break;
                     case STARTS_WITH:
                         sql.append(" LIKE '").append(saveVal).append("%'");
                         break;
                 }
             }
             if (query.isPhraseMandatorRestricted()) {
                 sql.append(" AND p.MANDATOR");
                 if (query.getPhraseMandators().length == 1)
                     sql.append("=").append(query.getPhraseMandators()[0]);
                 else
                     sql.append(" IN(").append(FxArrayUtils.toStringArray(query.getPhraseMandators(), ',')).append(")");
             }
             if (treeNodeRestricted) {
                 sql.append(" AND m.NODEID=").append(query.getTreeNode()).append(" AND m.NODEMANDATOR=").
                         append(query.getTreeNodeMandator()).append(" AND m.MANDATOR IN(").
                         append(FxArrayUtils.toStringArray(query.getTreeNodeMappingOwner(), ',')).
                         append(")AND m.PHRASEID=p.ID AND m.PMANDATOR=p.MANDATOR");
                 if (!query.isIncludeChildNodes())
                     sql.append(" AND m.DIRECT=TRUE");
             } else if (query.isOnlyUnassignedPhrases()) {
                 Long[] man;
                 if (query.isTreeNodeMappingOwnerRestricted())
                     man = query.getTreeNodeMappingOwner();
                 else
                     man = new Long[]{FxContext.getUserTicket().getMandatorId()};
                 sql.append(" AND (SELECT COUNT(m2.PHRASEID)FROM " + TBL_PHRASE_MAP + " m2 WHERE m2.MANDATOR IN (").
                         append(FxArrayUtils.toStringArray(man, ',')).append(") AND m2.NODEMANDATOR IN (").
                         append(FxArrayUtils.toStringArray(man, ',')).append(") AND m2.PHRASEID=p.ID AND m2.PMANDATOR=p.MANDATOR AND m2.CAT=p.CAT AND m2.DIRECT=TRUE)=0");
             }
             sql.append(" ORDER BY ");
             //order by tree node mapping mandator if result is sorted by position
             if (treeNodeRestricted && !query.isMixMandators() && query.isTreeNodeMappingOwnerRestricted() && query.getTreeNodeMappingOwner().length == 2) {
                 long ownMandator = FxContext.getUserTicket().getMandatorId();
                 long otherMandator = -1;
                 Long[] mand = query.getTreeNodeMappingOwner();
                 if (ownMandator == mand[0])
                     otherMandator = mand[1];
                 else if (ownMandator == mand[1])
                     otherMandator = mand[0];
                 if (otherMandator != -1) {
                     //own mandator is included
                     sql.append("m.MANDATOR ");
                     if (query.isOwnMandatorTop()) {
                         if (ownMandator < otherMandator)
                             sql.append("ASC,");
                         else
                             sql.append("DESC,");
                     } else {
                         if (ownMandator < otherMandator)
                             sql.append("DESC,");
                         else
                             sql.append("ASC,");
                     }
                 }
             }
             switch (query.getSortMode()) {
                 case POS_ASC:
                     sql.append("POS ASC");
                     break;
                 case POS_DESC:
                     sql.append("POS DESC");
                     break;
                 case VALUE_ASC:
                     if (query.isLanguageFallback())
                         sql.append("PVAL ASC");
                     else
                         sql.append("SVAL ASC");
                     break;
                 case VALUE_DESC:
                     if (query.isLanguageFallback())
                         sql.append("PVAL DESC");
                     else
                         sql.append("SVAL DESC");
                     break;
                 case KEY_ASC:
                     sql.append("PKEY ASC");
                     break;
                 case KEY_DESC:
                     sql.append("PKEY DESC");
                     break;
                 case TAG_ASC:
                     sql.append("STAG ASC");
                     break;
                 case TAG_DESC:
                     sql.append("STAG DESC");
                     break;
             }
             //order by phrase mandator
             if (query.isPhraseMandatorRestricted() && query.getPhraseMandators().length == 2) {
                 long ownMandator = FxContext.getUserTicket().getMandatorId();
                 long otherMandator = -1;
                 Long[] mand = query.getPhraseMandators();
                 if (ownMandator == mand[0])
                     otherMandator = mand[1];
                 else if (ownMandator == mand[1])
                     otherMandator = mand[0];
                 if (otherMandator != -1) {
                     //own mandator is included
                     sql.append(",p.MANDATOR ");
                     if (query.isOwnMandatorTop()) {
                         if (ownMandator < otherMandator)
                             sql.append("ASC");
                         else
                             sql.append("DESC");
                     } else {
                         if (ownMandator < otherMandator)
                             sql.append("DESC");
                         else
                             sql.append("ASC");
                     }
                 }
             }
             //PVAL,LANG,RTAG,SVAL,STAG
             sql.append(") R");
             if (query.isValueMatchRestricted() || query.isKeyMatchRestricted() || query.isTagMatchRestricted()) {
                 sql.append(" WHERE ");
                 if (query.isValueMatchRestricted())
                     sql.append(" R.SVAL IS NOT NULL");
                 if (query.isTagMatchRestricted()) {
                     if (query.isValueMatchRestricted())
                         sql.append(" AND");
                     sql.append(" R.RTAG IS NOT NULL");
                 }
                 if (query.isKeyMatchRestricted()) {
                     if (query.isValueMatchRestricted() || query.isTagMatchRestricted())
                         sql.append(" AND");
                     sql.append(" R.PKEY IS NOT NULL");
                 }
             }
 //            System.out.println("Query: [\n" + sql.toString() + "\n]");
             ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery();
             int startRow = pageSize * (page - 1);
             int endRow = pageSize * page;
             int currRow = 0;
             if (query.isFetchFullPhraseInfo()) {
                 /* 1 .. id 2 .. lang 3 .. val 4 .. tag 5 .. mandator 6 .. key 7 .. hidden 8 .. category*/
                 psPhrase = con.prepareStatement("SELECT p.ID,v.LANG,v.PVAL,v.TAG,p.MANDATOR,p.PKEY,p.HID,p.CAT FROM " + TBL_PHRASE + " p, " +
                         TBL_PHRASE_VALUES + " v WHERE p.ID=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             }
             //                    1  2        3    4    5    6    7    8   9
 //            sql.append("SELECT ID,MANDATOR,PKEY,PVAL,LANG,RTAG,SVAL,STAG,CAT FROM(");
             while (rs != null && rs.next()) {
                 if (currRow >= startRow && currRow < endRow) {
                     if (!query.isFetchFullPhraseInfo()) {
                         FxString val = new FxString(rs.getLong(5), rs.getString(4));
                         String tag = rs.getString(6);
                         final FxPhrase phrase = new FxPhrase(rs.getLong(2), rs.getString(3), val, tag).setId(rs.getLong(1));
                         phrase.setCategory(rs.getInt(9));
                         if (treeNodeRestricted)
                             phrase.setAssignmentMandator(rs.getLong(10));
                         if (isSortPos)
                             phrase.setPosition(rs.getInt(11));
                         phrases.add(phrase);
                     } else {
                         psPhrase.setLong(1, rs.getLong(1));
                         psPhrase.setLong(2, rs.getLong(2));
                         ResultSet rsPhrase = psPhrase.executeQuery();
                         if (rsPhrase != null && rsPhrase.next()) {
                             final FxPhrase phrase = loadPhrase(rsPhrase);
                             if (treeNodeRestricted)
                                 phrase.setAssignmentMandator(rs.getLong(10));
                             if (isSortPos)
                                 phrase.setPosition(rs.getInt(11));
                             phrases.add(phrase);
                         }
                         if (rsPhrase != null)
                             rsPhrase.close();
                     }
                 }
                 currRow++;
             }
             return new FxPhraseQueryResult(query, currRow, (startRow + 1), pageSize, phrases.size(), phrases);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             if (psPhrase != null)
                 Database.closeObjects(PhraseEngineBean.class, psPhrase);
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
 
     private FxPhraseQueryResult searchNoFallback(FxPhraseQuery query, int page, int pageSize) {
         Connection con = null;
         PreparedStatement ps = null, psPhrase = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
 
             List<FxPhrase> phrases = Lists.newArrayListWithCapacity(pageSize);
             boolean isSortPos = false;
             final boolean treeNodeRestricted = query.isTreeNodeRestricted();
             if (query.getSortMode() == FxPhraseQuery.SortMode.POS_ASC || query.getSortMode() == FxPhraseQuery.SortMode.POS_DESC) {
                 if (treeNodeRestricted && !query.isIncludeChildNodes())
                     isSortPos = true;
                 else {
                     //sorting by position is not possible if child tree nodes are included, fall back to value sorting
                     if (query.getSortMode() == FxPhraseQuery.SortMode.POS_ASC)
                         query.setSortMode(FxPhraseQuery.SortMode.VALUE_ASC);
                     else if (query.getSortMode() == FxPhraseQuery.SortMode.POS_DESC)
                         query.setSortMode(FxPhraseQuery.SortMode.VALUE_DESC);
                 }
             }
 
             StringBuilder sql = new StringBuilder(500);
             sql.append("SELECT DISTINCT p.ID,p.MANDATOR,p.PKEY");
             //we need special result values if result language != search language and not full FxPhrases are returned
             final boolean needResultVal = !query.isFetchFullPhraseInfo() && query.isResultLanguageRestricted() && query.isSearchLanguageRestricted() &&
                     !query.getSearchLanguage().equals(query.getResultLanguage());
             final String searchAlias = needResultVal ? "sv" : "v";
             final boolean needValueTable = needResultVal || query.isSortByTag() || query.isSortByValue() || query.isSearchLanguageRestricted();
             if (treeNodeRestricted)
                 sql.append(",m.MANDATOR");
             if (needValueTable)
                 sql.append(",v.PVAL,v.TAG");
             if (isSortPos)
                 sql.append(",m.POS");
             //append SVAL to allow an order by
             switch (query.getSortMode()) {
                 case VALUE_ASC:
                 case VALUE_DESC:
                     sql.append(",v.SVAL");
                     break;
             }
 
             sql.append(" FROM " + TBL_PHRASE + " p");
             if (needValueTable)
                 sql.append("," + TBL_PHRASE_VALUES + " v");
             if (needResultVal)
                 sql.append(", " + TBL_PHRASE_VALUES + " sv");
             if (treeNodeRestricted)
                 sql.append(", " + TBL_PHRASE_MAP + " m");
             sql.append(" WHERE ");
 
             final String categoryRestriction;
             if (treeNodeRestricted) {
                 if (query.getCategories().isSingleCategory())
                     categoryRestriction = "m.CAT=" + query.getCategories().getSingleCategory();
                 else
                     categoryRestriction = "m.CAT IN(" + categoriesList(query.getCategories()) + ")";
                 sql.append(categoryRestriction);
             } else {
                 if (query.getCategories().isSingleCategory())
                     categoryRestriction = "p.CAT=" + query.getCategories().getSingleCategory();
                 else
                     categoryRestriction = "p.CAT IN(" + categoriesList(query.getCategories()) + ")";
                 sql.append(categoryRestriction);
             }
             if (!query.isIncludeHidden())
                 sql.append(" AND p.HID=FALSE");
 
             if (needValueTable)
                 sql.append(" AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             if (needResultVal)
                 sql.append(" AND sv.ID=v.ID AND sv.MANDATOR=v.MANDATOR AND v.LANG=").append(query.getResultLanguage());
             if (query.isSearchLanguageRestricted())
                 sql.append(" AND ").append(searchAlias).append(".LANG=").append(query.getSearchLanguage());
             if (query.isPhraseMandatorRestricted()) {
                 sql.append(" AND p.MANDATOR");
                 if (query.getPhraseMandators().length == 1)
                     sql.append("=").append(query.getPhraseMandators()[0]);
                 else
                     sql.append(" IN(").append(FxArrayUtils.toStringArray(query.getPhraseMandators(), ',')).append(")");
             }
             if (treeNodeRestricted) {
                 sql.append(" AND m.NODEID=").append(query.getTreeNode()).append(" AND m.NODEMANDATOR=").
                         append(query.getTreeNodeMandator()).append(" AND m.MANDATOR IN(").
                         append(FxArrayUtils.toStringArray(query.getTreeNodeMappingOwner(), ',')).
                         append(")AND m.PHRASEID=p.ID AND m.PMANDATOR=p.MANDATOR");
                 if (!query.isIncludeChildNodes())
                     sql.append(" AND m.DIRECT=TRUE");
             } else if (query.isOnlyUnassignedPhrases()) {
                 Long[] man;
                 if (query.isTreeNodeMappingOwnerRestricted())
                     man = query.getTreeNodeMappingOwner();
                 else
                     man = new Long[]{FxContext.getUserTicket().getMandatorId()};
                 sql.append(" AND (SELECT COUNT(m2.PHRASEID)FROM " + TBL_PHRASE_MAP + " m2 WHERE m2.MANDATOR IN (").
                         append(FxArrayUtils.toStringArray(man, ',')).append(") AND m2.NODEMANDATOR IN (").
                         append(FxArrayUtils.toStringArray(man, ',')).append(") AND m2.PHRASEID=p.ID AND m2.PMANDATOR=p.MANDATOR AND m2.CAT=p.CAT AND m2.DIRECT=TRUE)=0");
             }
             if (query.isKeyMatchRestricted()) {
                 sql.append(" AND UPPER(p.PKEY)");
                 switch (query.getKeyMatchMode()) {
                     case CONTAINS:
                     case STARTS_WITH:
                         sql.append(" LIKE ?");
                         break;
                     default:
                         sql.append("=?");
                 }
             }
             if (query.isTagMatchRestricted()) {
                 sql.append(" AND UPPER(").append(searchAlias).append(".TAG)");
                 switch (query.getTagMatchMode()) {
                     case CONTAINS:
                     case STARTS_WITH:
                         sql.append(" LIKE ?");
                         break;
                     default:
                         sql.append("=?");
                 }
             }
             if (query.isValueMatchRestricted()) {
                 sql.append(" AND ").append(searchAlias).append(".SVAL");
                 switch (query.getValueMatchMode()) {
                     case CONTAINS:
                     case STARTS_WITH:
                         sql.append(" LIKE ?");
                         break;
                     default:
                         sql.append("=?");
                 }
             }
             if (ps != null)
                 ps.close();
             sql.append(" ORDER BY ");
             //order by tree node mapping mandator if result is sorted by position
             if (treeNodeRestricted && !query.isMixMandators() && query.isTreeNodeMappingOwnerRestricted() && query.getTreeNodeMappingOwner().length == 2) {
                 long ownMandator = FxContext.getUserTicket().getMandatorId();
                 long otherMandator = -1;
                 Long[] mand = query.getTreeNodeMappingOwner();
                 if (ownMandator == mand[0])
                     otherMandator = mand[1];
                 else if (ownMandator == mand[1])
                     otherMandator = mand[0];
                 if (otherMandator != -1) {
                     //own mandator is included
                     sql.append("m.MANDATOR ");
                     if (query.isOwnMandatorTop()) {
                         if (ownMandator < otherMandator)
                             sql.append("ASC,");
                         else
                             sql.append("DESC,");
                     } else {
                         if (ownMandator < otherMandator)
                             sql.append("DESC,");
                         else
                             sql.append("ASC,");
                     }
                 }
             }
             switch (query.getSortMode()) {
                 case POS_ASC:
                     sql.append("m.POS ASC");
                     break;
                 case POS_DESC:
                     sql.append("m.POS DESC");
                     break;
                 case VALUE_ASC:
                     sql.append("v.SVAL ASC");
                     break;
                 case VALUE_DESC:
                     sql.append("v.SVAL DESC");
                     break;
                 case KEY_ASC:
                     sql.append("p.PKEY ASC");
                     break;
                 case KEY_DESC:
                     sql.append("p.PKEY DESC");
                     break;
                 case TAG_ASC:
                     sql.append("v.TAG ASC");
                     break;
                 case TAG_DESC:
                     sql.append("v.TAG DESC");
                     break;
             }
             //order by phrase mandator
             if (query.isPhraseMandatorRestricted() && query.getPhraseMandators().length == 2) {
                 long ownMandator = FxContext.getUserTicket().getMandatorId();
                 long otherMandator = -1;
                 Long[] mand = query.getPhraseMandators();
                 if (ownMandator == mand[0])
                     otherMandator = mand[1];
                 else if (ownMandator == mand[1])
                     otherMandator = mand[0];
                 if (otherMandator != -1) {
                     //own mandator is included
                     sql.append(",p.MANDATOR ");
                     if (query.isOwnMandatorTop()) {
                         if (ownMandator < otherMandator)
                             sql.append("ASC");
                         else
                             sql.append("DESC");
                     } else {
                         if (ownMandator < otherMandator)
                             sql.append("DESC");
                         else
                             sql.append("ASC");
                     }
                 }
             }
 //            System.out.println("Query: [\n" + sql.toString() + "\n]");
             ps = con.prepareStatement(sql.toString());
             int queryParam = 1;
             if (query.isKeyMatchRestricted())
                 setQueryParam(ps, queryParam++, query.getKeyMatchMode(), query.getKeyQuery());
             if (query.isTagMatchRestricted())
                 setQueryParam(ps, queryParam++, query.getTagMatchMode(), query.getTagQuery());
             if (query.isValueMatchRestricted())
                 setQueryParam(ps, queryParam, query.getValueMatchMode(), query.getValueQueryNormalized());
             ResultSet rs = ps.executeQuery();
             int startRow = pageSize * (page - 1);
             int endRow = pageSize * page;
             int currRow = 0;
             if (query.isFetchFullPhraseInfo()) {
                 /* 1 .. id 2 .. lang 3 .. val 4 .. tag 5 .. mandator 6 .. key 7 .. hidden 8 .. category*/
                 psPhrase = con.prepareStatement("SELECT p.ID,v.LANG,v.PVAL,v.TAG,p.MANDATOR,p.PKEY,p.HID,p.CAT FROM " + TBL_PHRASE + " p, " +
                         TBL_PHRASE_VALUES + " v WHERE p.ID=? AND p.MANDATOR=? AND v.ID=p.ID AND v.MANDATOR=p.MANDATOR");
             }
             while (rs != null && rs.next()) {
                 if (currRow >= startRow && currRow < endRow) {
                     if (!query.isFetchFullPhraseInfo()) {
                         final FxPhrase phrase = new FxPhrase(rs.getLong(2), rs.getString(3), needValueTable ? rs.getString(treeNodeRestricted ? 5 : 4) : null, needValueTable ? rs.getString(treeNodeRestricted ? 6 : 5) : null).setId(rs.getLong(1));
                         if (treeNodeRestricted)
                             phrase.setAssignmentMandator(rs.getLong(4));
                         if (isSortPos)
                             phrase.setPosition(rs.getInt(needValueTable ? 7 : 5));
                         phrases.add(phrase);
                     } else {
                         psPhrase.setLong(1, rs.getLong(1));
                         psPhrase.setLong(2, rs.getLong(2));
                         ResultSet rsPhrase = psPhrase.executeQuery();
                         if (rsPhrase != null && rsPhrase.next()) {
                             final FxPhrase phrase = loadPhrase(rsPhrase);
                             if (treeNodeRestricted)
                                 phrase.setAssignmentMandator(rs.getLong(4));
                             if (isSortPos)
                                 phrase.setPosition(rs.getInt(needValueTable ? 7 : 5));
                             phrases.add(phrase);
                         }
                         if (rsPhrase != null)
                             rsPhrase.close();
                     }
                 }
                 currRow++;
             }
             return new FxPhraseQueryResult(query, currRow, (startRow + 1), pageSize, phrases.size(), phrases);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             if (psPhrase != null)
                 Database.closeObjects(PhraseEngineBean.class, psPhrase);
             Database.closeObjects(PhraseEngineBean.class, con, ps);
         }
     }
 
     private void setQueryParam(PreparedStatement ps, int queryParam, FxPhraseQuery.MatchMode matchMode, String query) throws SQLException {
         switch (matchMode) {
             case CONTAINS:
                 ps.setString(queryParam, "%" + query.toUpperCase() + "%");
                 break;
             case STARTS_WITH:
                 ps.setString(queryParam, query.toUpperCase() + "%");
                 break;
             case EXACT:
                 ps.setString(queryParam, query.toUpperCase());
                 break;
         }
     }
 
     private void buildNodeTreeSelect(StringBuilder sql, PreparedStatement ps, Long node, Long nodeMandator) throws SQLException {
         sql.append("OR(m.NODEID=").append(node).append(" AND m.NODEMANDATOR=").append(nodeMandator).append(")");
         ps.setLong(1, node);
         ps.setLong(2, nodeMandator);
         ResultSet rs = ps.executeQuery();
         List<Long> nodes = Lists.newArrayListWithExpectedSize(10);
         List<Long> nodeMandators = Lists.newArrayListWithExpectedSize(10);
         while (rs != null && rs.next()) {
             nodes.add(rs.getLong(1));
             nodeMandators.add(rs.getLong(2));
         }
         if (rs != null)
             rs.close();
         for (int i = 0; i < nodes.size(); i++)
             buildNodeTreeSelect(sql, ps, nodes.get(i), nodeMandators.get(i));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void syncDivisionResources(long targetMandator, FxPhraseSearchValueConverter converter) throws FxApplicationException {
         final UserTicket userTicket = FxContext.getUserTicket();
         checkMandatorAccess(targetMandator, userTicket);
         clearTree(targetMandator);
         clearPhrases(targetMandator);
 
         Connection con = null;
         PreparedStatement psFetch = null, psPhrase = null, psPhraseVal = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             psFetch = con.prepareStatement("SELECT RKEY,LANG,RVAL FROM " + TBL_RESOURCES + " ORDER BY RKEY ASC, LANG ASC");
             psPhrase = con.prepareStatement("INSERT INTO " + TBL_PHRASE + "  (ID,MANDATOR,PKEY)VALUES(?,?,?)");
             psPhrase.setLong(2, targetMandator);
             psPhraseVal = con.prepareStatement("INSERT INTO " + TBL_PHRASE_VALUES + "  (ID,MANDATOR,LANG,PVAL,SVAL,TAG)VALUES(?,?,?,?,?,NULL)");
             psPhraseVal.setLong(2, targetMandator);
 
             long currentId = 0;
             String currKey = null;
             ResultSet rs = psFetch.executeQuery();
             while (rs != null && rs.next()) {
                 if (currKey == null || !currKey.equals(rs.getString(1))) {
                     currentId++;
                     currKey = rs.getString(1);
                     psPhrase.setLong(1, currentId);
                     psPhraseVal.setLong(1, currentId);
                     psPhrase.setString(3, currKey);
                     psPhrase.executeUpdate();
                 }
                 psPhraseVal.setLong(3, rs.getLong(2));
                 psPhraseVal.setString(4, rs.getString(3));
                 if (converter != null)
                     psPhraseVal.setString(5, converter.convert(rs.getString(3), rs.getLong(2)));
                 else
                     psPhraseVal.setString(5, rs.getString(3).trim().toUpperCase());
                 psPhraseVal.executeUpdate();
             }
             final String SEQ_NAME = "PhraseSeq_" + targetMandator;
             if (seq.sequencerExists(SEQ_NAME))
                 seq.removeSequencer(SEQ_NAME);
             seq.createSequencer(SEQ_NAME, false, currentId + 1);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(PhraseEngineBean.class, psPhrase, psPhraseVal);
             Database.closeObjects(PhraseEngineBean.class, con, psFetch);
         }
     }
 }
