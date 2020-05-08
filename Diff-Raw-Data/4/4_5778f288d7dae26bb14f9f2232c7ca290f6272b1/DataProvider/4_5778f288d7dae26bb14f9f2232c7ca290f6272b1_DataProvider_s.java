 package com.scurab.gwt.rlw.server.data;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.transform.Transformers;
 
 import com.scurab.gwt.rlw.server.Queries;
 import com.scurab.gwt.rlw.server.Queries.AppQuery;
 import com.scurab.gwt.rlw.shared.QueryNames;
 import com.scurab.gwt.rlw.shared.SharedParams;
 import com.scurab.gwt.rlw.shared.model.Device;
 import com.scurab.gwt.rlw.shared.model.LogItem;
 
 public class DataProvider {
 
     private static final String AND = " AND ";// put rather space on both sides
     private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
     /**
      * Get devices
      * 
      * @param app
      *            - optional application
      * @param page
      *            - page to download
      * @return
      */
     public List<Device> getDevices(HashMap<String, Object> params) {
         List<Device> result = new ArrayList<Device>();
 
         Session s = Database.openSession();
         AppQuery query = null;
         if (params.containsKey(SharedParams.APP_NAME)) {
             query = Queries.getQuery(QueryNames.SELECT_DEVS_BY_APP);
         } else {
             query = Queries.getQuery(QueryNames.SELECT_DEVS);
         }
         Query q = getQueryWithDynamicParams(s, query, params).setResultTransformer(
                 Transformers.aliasToBean(Device.class));
         result.addAll(q.list());
         s.close();
         return result;
     }
 
     public List<String> getApplications() throws Exception {
         Session s = Database.openSession();
         AppQuery sql = Queries.getQuery(QueryNames.SELECT_APPS);
         List data = Database.getDataByQuery(s, sql.Query, AppQuery.TYPE_SQL.equals(sql.Type));
         List<String> apps = new ArrayList<String>();
         for (int i = 0, n = data.size(); i < n; i++) {
             apps.add(data.get(i).toString());
         }
         s.close();
         return apps;
     }
 
     public List<String> getDistinctValues(String query, String appName) {
         Session s = Database.openSession();
         AppQuery sql = Queries.getQuery(query);
         Query q = s.createSQLQuery(sql.Query);
         if (appName != null) {
             q.setParameter(SharedParams.APP_NAME, appName);
         }
 
         List data = q.list();
         List<String> apps = new ArrayList<String>();
         for (int i = 0, n = data.size(); i < n; i++) {
             apps.add(String.valueOf(data.get(i)));
         }
         s.close();
         return apps;
     }
 
     public List<LogItem> getLogs(HashMap<String, Object> params) {
         try {
             List<LogItem> result = new ArrayList<LogItem>();
 
             Session s = Database.openSession();
             AppQuery query = null;
             if (params.containsKey(SharedParams.APP_NAME)) {
                 query = Queries.getQuery(QueryNames.SELECT_LOGS_BY_APP);
             } else {
                 query = Queries.getQuery(QueryNames.SELECT_LOGS);
             }
             Query q = getQueryWithDynamicParams(s, query, params).setResultTransformer(
                     Transformers.aliasToBean(LogItem.class));
             result.addAll(q.list());
             s.close();
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             throw new IllegalStateException(e);
         }
     }
 
     private void initQuery(Query q) {
         initQueryWithDefinedParams(q, null);
     }
 
     /**
      * 
      * @param q
      * @param params
      */
     private void initQueryWithDefinedParams(Query q, HashMap<String, Object> params) {
         int page = 0;
         if (params.containsKey(SharedParams.PAGE)) {
             page = ((Number) params.get(SharedParams.PAGE)).intValue();
         }
         q.setMaxResults(SharedParams.PAGE_SIZE);
         if (page != 0) {
             q.setFirstResult(page * SharedParams.PAGE_SIZE);
         }
         // init params
         if (params != null) {
             for (String param : params.keySet()) {
                 if (SharedParams.PAGE.equals(param)) {
                     continue;
                 }
                 q.setParameter(param, params.get(param));
             }
         }
     }
 
     /**
      * 
      * @param s
      *            cant be null
      * @param sqlQuery
      *            cant be null
      * @param srcParams
      *            optional
      * @return
      */
     protected Query getQueryWithDynamicParams(Session s, AppQuery appQuery, HashMap<String, Object> srcParams) {
         if (s == null) {
             throw new IllegalArgumentException("Session is null");
         }
         if (appQuery == null) {
             throw new IllegalArgumentException("Invalid query");
         }
         // make a copy of params to avoid changins set
         HashMap<String, Object> params = new HashMap<String, Object>();
         if (srcParams != null) {
             params.putAll(srcParams);
         }
 
         // get page value and remove it from params
         int page = 0;
         if (params.containsKey(SharedParams.PAGE)) {
             page = ((Number) params.get(SharedParams.PAGE)).intValue();
             params.remove(SharedParams.PAGE);
         }
 
         // remove params defined in query statically
         if (appQuery.Parameters != null) {
             for (String key : appQuery.Parameters) {
                 params.remove(key);
             }
         }
 
         boolean addedParams = false;
 
         StringBuilder sb = new StringBuilder();
 
         // generate dynamic filter part
         if (params.size() > 0) {
             StringBuilder filter = new StringBuilder();
             filter.append("WHERE ");
            Set<String> keySet = params.keySet();
             for (String key : keySet) {
                 String columnName = key;
 
                 Object o = params.get(columnName);
                 String op = "=";
 
                 if (o != null) {
                     if (o instanceof Date) {
                         filter.append(String.format("date(%1$s) = :%1$s" + AND, key)); // http://www.stpe.se/2008/07/hibernate-hql-like-query-named-parameters/
                         // filter.append(String.format("(%1$s BETWEEN :%1$s_FROM AND :%1$s_TO)" + AND, key)); //
                         // http://www.stpe.se/2008/07/hibernate-hql-like-query-named-parameters/
                     } else {
                         /**
                          * if(contains *) key LIKE :key AND else key = :key AND
                          **/
 
                         String v = params.get(columnName).toString();
                         op = (v.charAt(0) == '*' || v.charAt(v.length() - 1) == '*') ? "LIKE" : "=";
                         filter.append(String.format("%1$s %2$s :%1$s" + AND, key, op)); // http://www.stpe.se/2008/07/hibernate-hql-like-query-named-parameters/
                     }
                 } else {
                     params.remove(key);
                     // key IS NULL AND
                     filter.append(String.format("%1$s IS NULL" + AND, key));
                 }
 
                 addedParams = true;
             }
             // remove last AND if necessary
             if (addedParams) {
                 filter.setLength(filter.length() - AND.length());
                 sb.append(filter.toString());
             }
         }
 
         String qry = appQuery.Query;
 
         // check if the last char is not ';'
         if (qry.charAt(qry.length() - 1) == ';') {
             qry = qry.substring(0, qry.length() - 1);
         }
         // if added, wrap requested query into new one
         if (addedParams) {
             qry = String.format("SELECT * FROM (%s) as drvTbl %s", qry, sb.toString());
         }
 
         Query q = s.createSQLQuery(qry);
 
         // set static params
         if (appQuery.Parameters != null) {
             for (String key : appQuery.Parameters) {
                 q.setParameter(key, srcParams.get(key));
             }
         }
 
         // set dynamic params
         if (addedParams) {
             for (String key : params.keySet()) {
                 Object o = params.get(key);
                 if (o instanceof Integer) {
                     q.setInteger(key, (Integer) o);
                 } else if (o instanceof Double) {
                     q.setDouble(key, (Double) o);
                 } else if (o instanceof String) {
                     // replace * chars by % to proper SQL
                     String v = (String) o;
                     if (v.charAt(v.length() - 1) == '*') {
                         v = v.substring(0, v.length() - 1) + "%";
                     }
                     if (v.charAt(0) == '*') {
                         v = "%" + v.substring(1, v.length());
                     }
                     q.setString(key, v);
                 } else if (o instanceof Date) {
                     Date d = (Date) o;
                     Date from = removeTime(d);
                     q.setParameter(key, from);
                     /*
                      * For between Date to = new Date(from.getTime() + 86399999); java.sql.Date dFrom = new
                      * java.sql.Date(from.getTime()); java.sql.Date dTo = new java.sql.Date(to.getTime());
                      * q.setParameter(key+"_FROM", dFrom); q.setParameter(key+"_TO", dTo);
                      */
                 } else {
                     throw new IllegalStateException("Not implemented!");
                 }
             }
         }
 
         q.setMaxResults(SharedParams.PAGE_SIZE);
         if (page != 0) {
             q.setFirstResult(page * SharedParams.PAGE_SIZE);
         }
         return q;
     }
 
     private static final Date removeTime(Date d) {
         Calendar c = Calendar.getInstance();
         c.setTime(d);
         c.set(Calendar.HOUR_OF_DAY, 0);
         c.set(Calendar.MINUTE, 0);
         c.set(Calendar.SECOND, 0);
         c.set(Calendar.MILLISECOND, 0);
 
         return c.getTime();
     }
 
 }
