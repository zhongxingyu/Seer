 package com.tp.service;
 
 import com.tp.dao.log.LogCCInstallWithContentDao;
 import com.tp.entity.log.LogCountClientInstallWithContent;
 import com.tp.orm.Page;
 import com.tp.orm.PropertyFilter;
 import com.tp.utils.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * User: ken.cui
  * Date: 13-4-15
  * Time: 上午11:44
  */
 
 @Component
 @Transactional
 public class LogCCInstallWithContentService {
     private LogCCInstallWithContentDao logCCInstallWithContentDao;
 
     public Page<LogCountClientInstallWithContent> searchCountClientInstallWithContent(
             final Page<LogCountClientInstallWithContent> page, final List<PropertyFilter> filters) {
         return logCCInstallWithContentDao.findPage(page, filters);
     }
 
     public void save(LogCountClientInstallWithContent entity){
         logCCInstallWithContentDao.save(entity);
     }
 
     public List<String> getMarketFromLog(String sdate){
        String edate= DateUtil.getNextDate(sdate);
        return logCCInstallWithContentDao.getMarketByDate(sdate,edate);
     }
 
     public List<LogCountClientInstallWithContent> getLogs(String sdate){
        String edate= DateUtil.getNextDate(sdate);
        return logCCInstallWithContentDao.getByDate(sdate,edate);
     }
 
     @Autowired
     public void setLogCCInstallWithContentDao(LogCCInstallWithContentDao logCCInstallWithContentDao) {
         this.logCCInstallWithContentDao = logCCInstallWithContentDao;
     }
 }
