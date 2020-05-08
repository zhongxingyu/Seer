 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: DatabasePolicies.java,v 1.3 2004-04-28 16:55:04 shahid.shah Exp $
  */
 
 package com.netspective.axiom;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import com.netspective.axiom.policy.AnsiDatabasePolicy;
 import com.netspective.axiom.policy.HSqlDbDatabasePolicy;
 import com.netspective.axiom.policy.MySqlDatabasePolicy;
 import com.netspective.axiom.policy.OracleDatabasePolicy;
 import com.netspective.axiom.policy.PostgreSqlDatabasePolicy;
 import com.netspective.axiom.policy.SqlServerDatabasePolicy;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.commons.validate.ValidationUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.discovery.tools.DiscoverSingleton;
 
 public class DatabasePolicies
 {
     protected static final Log log = LogFactory.getLog(DatabasePolicies.class);
     public static final DatabasePolicy DBPOLICY_ANSI = new AnsiDatabasePolicy();
     public static final String DBMSID_DEFAULT = "ansi";
 
     public static final String DBPOLICYIDMATCH_ALL = "*";
 
     private static DatabasePolicies instance = (DatabasePolicies) DiscoverSingleton.find(DatabasePolicies.class, DatabasePolicies.class.getName());
     private static DatabasePolicyEnumeratedAttribute enumAttrInstance = new DatabasePolicyEnumeratedAttribute();
     private Map policiesById = new HashMap();
     private Set policiesSet = new HashSet();
 
     public static class DatabasePolicyEnumeratedAttribute extends XdmEnumeratedAttribute
     {
         public String[] getValues()
         {
             return (String[]) instance.policiesById.keySet().toArray(new String[instance.policiesById.size()]);
         }
     }
 
     public static final DatabasePolicies getInstance()
     {
         return instance;
     }
 
     public DatabasePolicies()
     {
         registerDatabasePolicy(DBPOLICY_ANSI);
         registerDatabasePolicy((DatabasePolicy) DiscoverSingleton.find(HSqlDbDatabasePolicy.class, HSqlDbDatabasePolicy.class.getName()));
         registerDatabasePolicy((DatabasePolicy) DiscoverSingleton.find(MySqlDatabasePolicy.class, MySqlDatabasePolicy.class.getName()));
         registerDatabasePolicy((DatabasePolicy) DiscoverSingleton.find(OracleDatabasePolicy.class, OracleDatabasePolicy.class.getName()));
         registerDatabasePolicy((DatabasePolicy) DiscoverSingleton.find(PostgreSqlDatabasePolicy.class, PostgreSqlDatabasePolicy.class.getName()));
         registerDatabasePolicy((DatabasePolicy) DiscoverSingleton.find(SqlServerDatabasePolicy.class, SqlServerDatabasePolicy.class.getName()));
     }
 
     public void registerDatabasePolicy(DatabasePolicy policy)
     {
         String[] dbmsIdentifiers = policy.getDbmsIdentifiers();
 
         for(int i = 0; i < dbmsIdentifiers.length; i++)
         {
             String dbmsIdentifier = dbmsIdentifiers[i];
             if(log.isDebugEnabled() && policiesById.containsKey(dbmsIdentifier))
             {
                 DatabasePolicy existingPolicy = (DatabasePolicy) policiesById.get(dbmsIdentifier);
                 log.debug("Replacing existing policy id '' class "+ existingPolicy.getClass().getName() +" with " + policy.getClass().getName());
             }
 
             policiesById.put(dbmsIdentifier, policy);
             policiesSet.add(policy);
             log.trace("Registered database policy "+ policy.getClass().getName() +" as '"+ dbmsIdentifier +"'.");
         }
     }
 
     public DatabasePolicyEnumeratedAttribute getEnumeratedAttribute()
     {
         return enumAttrInstance;
     }
 
     public DatabasePolicy getDatabasePolicy(String dbmsIdentifier)
     {
         return (DatabasePolicy) policiesById.get(dbmsIdentifier);
     }
 
     public DatabasePolicy getDatabasePolicy(Connection conn) throws SQLException
     {
         String databaseProductName = conn.getMetaData().getDatabaseProductName();
         DatabasePolicy policy = getDatabasePolicy(databaseProductName);
         if(policy == null)
        {
            log.error("Database policy not found for database '" + databaseProductName + "', using 'ansi'. Available: " + policiesById.keySet());
            return getDatabasePolicy(DBMSID_DEFAULT);
        }
         else
             return policy;
     }
 
     /**
      * Return the database policies with identifiers (keys) that match the given regular expression. A special case,
      * called '*' will simply return all the database policies.
      * @param regularExpression
      * @return
      */
 
     public DatabasePolicy[] getMatchingPolices(String regularExpression)
     {
         Set policies = new HashSet();
 
         for(Iterator i = policiesById.entrySet().iterator(); i.hasNext(); )
         {
             Map.Entry entry = (Map.Entry) i.next();
             String id = (String) entry.getKey();
 
             if(regularExpression.equals(DBPOLICYIDMATCH_ALL) || ValidationUtils.matchRegexp(id, regularExpression))
                 policies.add(entry.getValue());
         }
 
         return (DatabasePolicy[]) policies.toArray(new DatabasePolicy[policies.size()]);
     }
 
     public Set getPolicies()
     {
         return policiesSet;
     }
 
     public int size()
     {
         return policiesSet.size();
     }
 }
