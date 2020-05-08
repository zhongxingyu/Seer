 package org.cimmyt.cril.ibwb.provider.dao;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.util.*;
 import java.util.regex.Pattern;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.transform.Transformers;
 import org.openide.modules.InstalledFileLocator;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 /*
  * used in calling Views, Stored procedures, etc.
  */
 public class UtilityDAO extends HibernateDaoSupport {
 
     public static final String DATABASE_PARAMETER_NAME = "dName";
     public static final String IS_LOCAL = "islocal";
 
 
     private String centralDatabaseName;
 
     private static Logger log = Logger.getLogger(UtilityDAO.class);
     private String accessType;
 
     public UtilityDAO() {
 
     }
 
     public UtilityDAO(String dbConfigRelativePath) {
 
 
            InstalledFileLocator locator = InstalledFileLocator.getDefault();
            File databaseConfigFile = locator.locate(dbConfigRelativePath, "org.cimmyt.cril.ibwb.provider", false);
            Properties propertiesFile = new Properties();
            try {
                InputStream streamProperties = new FileInputStream(databaseConfigFile);
                propertiesFile.load(streamProperties);
 
                String centralDb = propertiesFile.getProperty("dmscentral2.defaultSchema");
                this.setCentralDatabaseName(centralDb);
            } catch (Exception e) {
                e.printStackTrace();
            }
     }
 
 
 
     /**
      * @return the accessType
      */
     public String getAccessType() {
         return accessType;
     }
 
     /**
      * @param accessType the accessType to set
      */
     public void setAccessType(String accessType) {
         this.accessType = accessType;
     }
     
     protected int isLocal() {
         return accessType!=null&&accessType.equals("local")?1:0;
     }
     
     public String getCentralDatabaseName() {
         return centralDatabaseName;
     }
 
     public void setCentralDatabaseName(String centralDatabaseName) {
         this.centralDatabaseName = centralDatabaseName;
     }
 
     @SuppressWarnings("rawtypes")
     public <T> T callStoredProcedureForObject(
             final T bean,
             final String procedureName,
             final String[] inParams,
             final String[] outParams) {
 
         
         final String sql = buildSQLQuery(procedureName, inParams);
         System.out.println("sql = " + sql);
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 SQLQuery query = session.
                         createSQLQuery(sql);
                 query.setResultTransformer(Transformers.aliasToBean(bean.getClass()));
                 try {
                     if (inParams != null && inParams.length > 0) {
                         for (String paramName : inParams) {
                             Object obj = PropertyUtils.getProperty(bean, paramName);
                             System.out.println(paramName + " = " + obj);
                             query.setParameter(paramName, obj);
                         }
 
                         // GCP-4347 Adding central database name as a parameter for all SQL queries
                         // TODO : remove temporary comment
                         //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                         //query.setParameter(IS_LOCAL, isLocal());
                     }
 
                     if (outParams != null && outParams.length > 0) {
                         for (String paramName : outParams) {
                             Class type = PropertyUtils.getPropertyType(bean, paramName);
                             if (type.getSimpleName().equals("Integer")) {
                                 query.addScalar(paramName, Hibernate.INTEGER);
                             } else if (type.getSimpleName().equals("Double")) {
                                 query.addScalar(paramName, Hibernate.DOUBLE);
                             } else {
                                 query.addScalar(paramName);
                             }
                         }
                     }
                 } catch (IllegalAccessException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (InvocationTargetException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (NoSuchMethodException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 }
                 return query.uniqueResult();
             }
         });
 
 
         return (T) result;
     }
 
     /**
      *
      * @param bean Bean
      * @param procedureName Name of the procedure in the database to invoke
      * @param params HashMap containing key value pairs specifying the parameters to provide to the stored proc
      * @param outParams String array containing the column names in the return of the stored proc that need to be mapped back to the object
      * @param <T>
      * @return
      */
     public <T> T callStoredProcedureForObject(
             final T bean,
             final String procedureName,
             final HashMap params,
             final String[] outParams) {
 
         final String sql = buildSQLQuery(procedureName, params);
         System.out.println("sql = " + sql);
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 SQLQuery query = session.
                         createSQLQuery(sql);
                 query.setResultTransformer(Transformers.aliasToBean(bean.getClass()));
                 try {
 
                     if (params != null && params.size() > 0) {
 
                         for (Object o : params.entrySet()) {
                             Map.Entry entry = (Map.Entry) o;
                             System.out.println(entry.getKey() + " = " + entry.getValue());
                             query.setParameter(entry.getKey().toString(), entry.getValue());
                         }
                     }
 
                     // GCP - 4347 Adding central database name as a parameter to all SQL queries
                     // TODO : remove temporary comment
                     //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                     //query.setParameter(IS_LOCAL, isLocal());
 
                     if (outParams != null && outParams.length > 0) {
                         for (String paramName : outParams) {
                             Class type = PropertyUtils.getPropertyType(bean, paramName);
                             if (type.getSimpleName().equals("Integer")) {
                                 query.addScalar(paramName, Hibernate.INTEGER);
                             } else if (type.getSimpleName().equals("Double")) {
                                 query.addScalar(paramName, Hibernate.DOUBLE);
                             } else {
                                 query.addScalar(paramName);
                             }
                         }
                     }
                 } catch (IllegalAccessException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (InvocationTargetException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (NoSuchMethodException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 }
                 return query.uniqueResult();
             }
         });
 
 
         return (T) result;
     }
 
     @SuppressWarnings("rawtypes")
     public Integer callStoredProcedureForCount(
             final String procedureName,
             final HashMap params) {
 
         final String sql = buildSQLQuery(procedureName, params);
         System.out.println("sql = " + sql);
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 SQLQuery query = session.
                         createSQLQuery(sql);
                 query.setResultTransformer(Transformers.aliasToBean(Integer.class));
 
 
                 if (params != null && params.size() > 0) {
 
                     for (Object o : params.entrySet()) {
                         Map.Entry entry = (Map.Entry) o;
                         System.out.println(entry.getKey() + " = " + entry.getValue());
                         query.setParameter(entry.getKey().toString(), entry.getValue());
                     }
                 }
 
                 // GCP - 4347 Adding central database name as a parameter to all SQL queries
                 // TODO : remove temporary comment
                 //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                 //query.setParameter(IS_LOCAL, isLocal());
 
                 return query.uniqueResult();
             }
         });
 
 
         return (Integer) result;
     }
 
     @SuppressWarnings("rawtypes")
     public List callStoredProcedureForList(
             final Class beanClass,
             final String procedureName,
             final HashMap parameters,
             final String[] inParams ,
             final String[] outParams) {
 
 
         final String sql = buildSQLQuery(procedureName, inParams);
         System.out.println("sql = " + sql);
         List result = getHibernateTemplate().executeFind(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 SQLQuery query = session.
                         createSQLQuery(sql);
                 query.setResultTransformer(Transformers.aliasToBean(beanClass));
                 if (parameters != null) {
                     Iterator iterParam = parameters.keySet().iterator();
                     while (iterParam.hasNext()) {
                         String paramName = (String) iterParam.next();
                         Object obj = parameters.get(paramName);
                         System.out.println(paramName + " = " + obj);
                         query.setParameter(paramName, obj);
                     }
                 }
 
 
                 // GCP - 4347 Adding central database name as a parameter to all SQL queries
                 // TODO : remove temporary comment
                 //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                 //query.setParameter(IS_LOCAL, isLocal());
 
                 try {
                     if (outParams != null && outParams.length > 0) {
                         for (String paramName : outParams) {
                             Object object = null;
                             try{
                             object = beanClass.newInstance();
                             }catch (Exception e){
                                 e.printStackTrace();
                             }
                             Class type = PropertyUtils.getPropertyType(object, paramName);
                             if (type.getSimpleName().equals("Integer")) {
                                 query.addScalar(paramName, Hibernate.INTEGER);
                             } else if (type.getSimpleName().equals("Double")) {
                                 query.addScalar(paramName, Hibernate.DOUBLE);
                             } else {
                                 query.addScalar(paramName);
                             }
                         }
                     }
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 } catch (NoSuchMethodException e) {
                     e.printStackTrace();
                 }
                 return query.list();
             }
         });
         return result;
     }
 
     public <T> List<T> callStoredProcedureForList(
             final T bean,
             final String procedureName,
             final String[] inParams,
             final String[] outParams) {
         return callStoredProcedureForListPaged(bean, false, 0, 0, procedureName, inParams, outParams);
     }
 
     @SuppressWarnings({"rawtypes", "unchecked"})
     public <T> List<T> callStoredProcedureForListPaged(
             final T bean,
             final boolean paged,
             final int start,
             final int pageSize,
             final String procedureName,
             final String[] inParams,
             final String[] outParams) {
 
         final String sql = buildSQLQuery(procedureName, inParams);
         System.out.println("sql = " + sql);
         List result = getHibernateTemplate().executeFind(new HibernateCallback() {
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 SQLQuery query = session.
                         createSQLQuery(sql);
                 query.setResultTransformer(Transformers.aliasToBean(bean.getClass()));
                 try {
                     if (inParams != null && inParams.length > 0) {
                         for (String paramName : inParams) {
                             Object obj = PropertyUtils.getProperty(bean, paramName);
                             System.out.println(paramName + " = " + obj);
                             query.setParameter(paramName, obj);
                         }
                     }
 
                     // GCP - 4347 Adding central database name as a parameter to all SQL queries
                     // TODO : remove temporary comment
                     //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                     //query.setParameter(IS_LOCAL, isLocal());
 
                     if (outParams != null && outParams.length > 0) {
                         for (String paramName : outParams) {
                             Class type = PropertyUtils.getPropertyType(bean, paramName);
                             if (type.getSimpleName().equals("Integer")) {
                                 query.addScalar(paramName, Hibernate.INTEGER);
                             } else if (type.getSimpleName().equals("Double")) {
                                 query.addScalar(paramName, Hibernate.DOUBLE);
                             } else {
                                 query.addScalar(paramName);
                             }
                         }
                     }
                 } catch (IllegalAccessException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (InvocationTargetException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 } catch (NoSuchMethodException e) {
                     log.error("error in setting parameters " + e.getMessage());
                 }
                 if (paged) {
                     query.setFirstResult(start);
                     query.setMaxResults(pageSize);
                 }
                 return query.list();
             }
         });
         return result;
     }
 
     public int callStoredProcedureForUpdate(
             final Object bean,
             final String procedureName,
             final String... params) {
         final String sql = buildSQLQuery(procedureName, params);
         System.out.println("sql = " + sql);
         int result = (Integer) getHibernateTemplate().execute(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 Query query = session.
                         createSQLQuery(sql);
                 if (params != null && params.length > 0) {
                     for (String paramName : params) {
                         try {
                             Object obj = PropertyUtils.getProperty(bean, paramName);
                             query.setParameter(paramName, obj);
                             System.out.println(paramName + " = " + obj);
                         } catch (IllegalAccessException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         } catch (InvocationTargetException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         } catch (NoSuchMethodException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         }
                     }
                 }
 
                 // GCP - 4347 Adding central database name as a parameter to all SQL queries
                 // TODO : remove temporary comment
                 //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                 //query.setParameter(IS_LOCAL, isLocal());
                 return query.executeUpdate();
             }
         });
         return result;
     }
 
     public Integer getNextMin(final String tableName){
 
         final String sql = buildSQLQuery("getNextMin", "tableName");
                System.out.println("sql = " + sql);
                Object result = getHibernateTemplate().execute(new HibernateCallback() {
 
                    @Override
                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {
                        SQLQuery query = session.
                                createSQLQuery(sql);
                        query.setParameter("tableName", tableName);
 
                        return query.uniqueResult();
                    }
                });
 
 
                BigInteger id = (BigInteger) result;
             return new Integer(id.intValue());
     }
 
     private String buildSQLQuery(String procedureName, String... params) {
         StringBuilder sql = new StringBuilder();
         sql.append("CALL ");
         sql.append(procedureName);
         sql.append("(");
 
         if (params != null && params.length > 0) {
             boolean start = true;
             for (String paramName : params) {
                 if (!start) {
                     sql.append(", ");
                 } else {
                     start = false;
                 }
                 sql.append(":");
                 sql.append(paramName);
             }
         }
 
         // GCP - 4347 Adding database name as a parameter to all SQL queries
         // TODO : remove temporary comment
         //sql.append(", :" + DATABASE_PARAMETER_NAME);
         //sql.append(", :" + IS_LOCAL);
 
 
         sql.append(")");
         return sql.toString();
     }
 	@SuppressWarnings("rawtypes")
     	public int callStoredProcedureForUpdate(
                 final String procedureName,
                 final LinkedHashMap parameters
                 ) {
 
             String params[] = new String[parameters.keySet().size()];
             Iterator iter = parameters.keySet().iterator();
             int i = 0 ;
             while(iter.hasNext()){
                 params[i++] = (String)iter.next();
             }
             final String sql = buildSQLQuery(procedureName, params);
             System.out.println("sql = "+sql);
             int result = (Integer) getHibernateTemplate().execute(new HibernateCallback() {
 
                 @Override
                 public Object doInHibernate(Session session)
                         throws HibernateException, SQLException {
                     SQLQuery query = session.
                             createSQLQuery(sql);
                     if(parameters != null){
                         Iterator iterParam = parameters.keySet().iterator();
                         while(iterParam.hasNext()){
                             String paramName = (String)iterParam.next();
                             Object obj = parameters.get(paramName);
     						System.out.println(paramName + " = "+obj);
                             query.setParameter(paramName, obj);
                         }
                     }
 
                     // GCP - 4347 Adding central database name as a parameter to all SQL queries
                     // TODO : remove temporary comment
                     //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                     //query.setParameter(IS_LOCAL, isLocal());
 
                     return query.executeUpdate();
                 }
             });
             return result;
         }
 	
 	@SuppressWarnings("rawtypes")
 	public int callStoredProcedureForUpdateAndReturnPK(
 			final Object bean,
             final String procedureName,
             final String... params) {
         final String sql = buildSQLQuery(procedureName, params);
         System.out.println("sql = " + sql);
        // getHibernateTemplate().ex
         Object result = getHibernateTemplate().execute(new HibernateCallback() {
 
             @Override
             public Object doInHibernate(Session session)
                     throws HibernateException, SQLException {
                 
                 Query query = session.
                         createSQLQuery(sql);
                 
                 if (params != null && params.length > 0) {
                     for (String paramName : params) {
                         try {
                             Object obj = PropertyUtils.getProperty(bean, paramName);
                             query.setParameter(paramName, obj);
                             System.out.println(paramName + " = " + obj);
                         } catch (IllegalAccessException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         } catch (InvocationTargetException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         } catch (NoSuchMethodException e) {
                             log.error("error in setting update parameters " + e.getMessage());
                         }
                     }
                 }
 
                 // GCP - 4347 Adding central database name as a parameter to all SQL queries
                 // TODO : remove temporary comment
                 //query.setParameter(DATABASE_PARAMETER_NAME, centralDatabaseName);
                 //query.setParameter(IS_LOCAL, isLocal());
 
                
                 return query.uniqueResult();
             }
         });
       
         return (Integer)result;
     }
 
     private String buildSQLQuery(String procedureName, HashMap params) {
         StringBuilder sql = new StringBuilder();
         sql.append("CALL ");
         sql.append(procedureName);
         sql.append("(");
         if (params != null && params.size() > 0) {
             boolean start = true;
             for (Object o : params.keySet()) {
                 String key = o.toString();
 
                 if (!start) {
                     sql.append(", ");
                 } else {
                     start = false;
                 }
 
                 sql.append(":");
                 sql.append(key);
             }
         }
 
         // GCP - 4347 Adding database name as a parameter to all SQL queries
         // TODO : remove temporary comment
         //sql.append(", :" + DATABASE_PARAMETER_NAME);
         //sql.append(", :" + IS_LOCAL);
         sql.append(")");
         return sql.toString();
     }
 
     private boolean isIntegerType(String value) {
         Pattern pattern = Pattern.compile("[1-9]\\d*");
         return (pattern.matcher(value).matches());
     }
 
     private boolean isDoubleType(String value) {
         Pattern pattern = Pattern.compile("\\d*\\.\\d*");
         return pattern.matcher(value).matches();
     }
 
 }
