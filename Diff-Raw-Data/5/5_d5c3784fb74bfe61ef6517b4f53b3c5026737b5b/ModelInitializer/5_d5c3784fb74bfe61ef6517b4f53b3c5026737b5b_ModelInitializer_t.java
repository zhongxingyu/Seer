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
  */
 package com.netspective.medigy.util;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Expression;
 
 import com.netspective.medigy.model.data.EntitySeedDataPopulator;
 import com.netspective.medigy.model.party.Party;
 import com.netspective.medigy.reference.CachedReferenceEntity;
 import com.netspective.medigy.reference.ReferenceEntity;
 import com.netspective.medigy.reference.custom.CachedCustomReferenceEntity;
 import com.netspective.medigy.reference.custom.CustomReferenceEntity;
 
 public class ModelInitializer
 {
     public enum SeedDataPopulationType
     {
         AUTO, YES, NO
     }
 
     private final Log log = LogFactory.getLog(EntitySeedDataPopulator.class);
     private final SeedDataPopulationType seedDataPopulationType;
     private final Session session;
     private final HibernateConfiguration hibernateConfiguration;
 
     public ModelInitializer(final Session session, final SeedDataPopulationType seedDataPopulationType, final HibernateConfiguration hibernateConfiguration)
     {
         this.session = session;
         this.seedDataPopulationType = seedDataPopulationType;
         this.hibernateConfiguration = hibernateConfiguration;
     }
 
     public void initialize()
     {
         initReferenceEntityCaches();
 
         switch(seedDataPopulationType)
         {
             case AUTO:
                 if(readSystemGlobalPropertyEntity() == null)
                     populateSeedData();
                 break;
 
             case YES:
                 populateSeedData();
                 break;
         }
 
         initSystemGlobalPartyEntity();
         initCustomReferenceEntityCaches();
     }
 
     public void populateSeedData()
     {
         EntitySeedDataPopulator populator = new EntitySeedDataPopulator(session);
         populator.populateSeedData();
     }
 
     public Party readSystemGlobalPropertyEntity()
     {
         final Criteria criteria = session.createCriteria(Party.class);
         criteria.add(Expression.eq("partyName", Party.SYS_GLOBAL_PARTY_NAME));
         return (Party) criteria.uniqueResult();
     }
 
     public void initSystemGlobalPartyEntity()
     {
         final Party entity = readSystemGlobalPropertyEntity();
         if (entity == null)
             throw new RuntimeException("The " + Party.SYS_GLOBAL_PARTY_NAME + " entity MUST exist before trying to " +
                     "access related built-in custom reference entities.");
 
         Party.Cache.SYS_GLOBAL_PARTY.setEntity(entity);
     }
 
     public void initReferenceEntityCaches()
     {
         for(final Map.Entry<Class, Class> entry : hibernateConfiguration.getReferenceEntitiesAndCachesMap().entrySet())
             initReferenceEntityCache(entry.getKey(), (CachedReferenceEntity[]) entry.getValue().getEnumConstants());
     }
 
     public void initCustomReferenceEntityCaches()
     {
         for(final Map.Entry<Class, Class> entry : hibernateConfiguration.getCustomReferenceEntitiesAndCachesMap().entrySet())
             initCustomReferenceEntityCache(entry.getKey(), (CachedCustomReferenceEntity[]) entry.getValue().getEnumConstants());
     }
 
     protected void initReferenceEntityCache(final Class aClass, final CachedReferenceEntity[] cache)
     {
         final List list = session.createCriteria(aClass).list();
 
         for(final Object i : list)
         {
             final ReferenceEntity entity = (ReferenceEntity) i;
             final Object id = entity.getTypeId();
             if(id == null)
             {
                 log.error(entity + " id is NULL: unable to map to one of " + cache);
                 continue;
             }
 
             for(final CachedReferenceEntity c : cache)
             {
                 if(id.equals(c.getId()))
                 {
                     final ReferenceEntity record = c.getEntity();
                     if(record != null)
                         log.error(c.getClass().getName() + " enum '" + c + "' is bound to multiple rows.");
                     else
                         c.setEntity(entity);
                     break;
                 }
             }
         }
 
         for(final CachedReferenceEntity c : cache)
         {
             if(c.getEntity() == null)
                 log.error(c.getClass().getName() + " enum '" + c + "' was not bound to a database row.");
         }
     }
 
     protected void initCustomReferenceEntityCache(final Class aClass, final CachedCustomReferenceEntity[] cache)
     {
        final Criteria criteria = session.createCriteria(aClass);
        criteria.add(Expression.eq("party", Party.Cache.SYS_GLOBAL_PARTY.getEntity()));

        final List list = criteria.list();
         for(final Object i : list)
         {
             final CustomReferenceEntity entity = (CustomReferenceEntity) i;
             final Object code = entity.getCode();
             if(code == null)
             {
                 log.error(entity + " code is NULL: unable to map to one of " + cache);
                 continue;
             }
 
             for(final CachedCustomReferenceEntity c : cache)
             {
                 if(code.equals(c.getCode()))
                 {
                     final CustomReferenceEntity record = c.getEntity();
                     if(record != null)
                         log.error(c.getClass().getName() + " enum '" + c + "' is bound to multiple rows.");
                     else
                         c.setEntity(entity);
                     break;
                 }
             }
         }
 
         for(final CachedCustomReferenceEntity c : cache)
         {
             if(c.getEntity() == null)
                 log.error(c.getClass().getName() + " enum '" + c + "' was not bound to a database row.");
         }
 
     }
 }
