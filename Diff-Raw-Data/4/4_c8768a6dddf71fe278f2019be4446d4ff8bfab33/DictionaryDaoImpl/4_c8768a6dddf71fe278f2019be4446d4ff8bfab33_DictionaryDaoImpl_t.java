 package com.nkhoang.gae.dao.impl;
 
 import com.nkhoang.gae.dao.DictionaryDao;
 import com.nkhoang.gae.model.Dictionary;
 import org.apache.commons.lang.StringUtils;
 
 import javax.persistence.Query;
 import java.util.List;
 
 
 public class DictionaryDaoImpl extends BaseDaoImpl<Dictionary, Long> implements DictionaryDao {
     public String getClassName() {
         return Dictionary.class.getName();
     }
 
     public Dictionary getDictionaryByName(String dictName) {
         if (StringUtils.isNotEmpty(dictName)) {
             Query query = entityManager
                     .createQuery("select from " + getClassName() + " u where u.name=:dictName");
             query.setParameter("dictName", dictName);
 
             List<Dictionary> result = query.getResultList();
             if (result != null && result.size() > 0) {
                 return result.get(0);
             }
         }
         return null;
     }
 }
