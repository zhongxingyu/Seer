 /*
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 /**
  * 
  */
 /*
  * Copyright (C) 2003-2009 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.organization.hibernate;
 
 import org.exoplatform.commons.utils.ListAccess;
 import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.database.HibernateService;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
  * @version $Id: HibernateUserListAccess.java 111 2008-11-11 11:11:11Z $
  */
 public class HibernateListAccess<E> implements ListAccess<E>
 {
 
    /**
     * The Hibernate Service.
     */
    protected HibernateService service;
 
    /**
     * Find query string.
     */
    protected String findQuery;
 
    /**
     * Count query string.
     */
    protected String countQuery;
 
    /**
     * Binded fields.
     */
    protected Map<String, Object> binding;
 
    /**
     * HibernateUserListAccess constructor.
     * 
     * @param service
     *          The Hibernate Service.
     * @param findQuery
     *          Find query string
     * @param countQuery
     *          Count query string
     */
    public HibernateListAccess(HibernateService service, String findQuery, String countQuery)
    {
       this.service = service;
       this.findQuery = findQuery;
       this.countQuery = countQuery;
       this.binding = new HashMap<String, Object>();
    }
 
    /**
     * HibernateUserListAccess constructor.
     * 
     * @param service
     *          The Hibernate Service.
     * @param findQuery
     *          Find query string
     * @param countQuery
     *          Count query string
     * @param binding
     *          Binded fields
     */
    public HibernateListAccess(HibernateService service, String findQuery, String countQuery,
       Map<String, Object> binding)
    {
       this.service = service;
       this.findQuery = findQuery;
       this.countQuery = countQuery;
       this.binding = binding;
    }
 
    /**
     * {@inheritDoc}
     */
    public int getSize() throws Exception
    {
       final Session session = service.openSession();
 
       Query query = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Query>()
       {
          public Query run()
          {
             return session.createQuery(countQuery);
          }
       });
 
       bindFields(query);
 
       List l = query.list();
       if (!l.isEmpty())
       {
          return ((Number)l.get(0)).intValue();
       }

       throw new HibernateException("The query execution " + countQuery + " failed");
    }
 
    /**
     * {@inheritDoc}
     */
    public E[] load(int index, int length) throws Exception, IllegalArgumentException
    {
       final Session session = service.openSession();
 
       if (index < 0)
       {
          throw new IllegalArgumentException("Illegal index: index must be a positive number");
       }
 
       if (length < 0)
       {
          throw new IllegalArgumentException("Illegal length: length must be a positive number");
       }
 
       List<E> entities = new ArrayList<E>(length);
 
       Query query = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Query>()
       {
          public Query run()
          {
             return session.createQuery(findQuery);
          }
       });
       bindFields(query);
 
       Iterator<Object> results = query.iterate();
       for (int p = 0, counter = 0; counter < length; p++)
       {
          if (!results.hasNext())
          {
             throw new IllegalArgumentException(
                "Illegal index or length: sum of the index and the length cannot be greater than the list size");
          }
 
          Object result = results.next();
 
          if (p >= index)
          {
             entities.add((E)result);
             counter++;
          }
       }
 
       return (E[])entities.toArray();
    }
 
    /**
     * BindFields.
     * 
     * @param query
     *          Query
     */
    private void bindFields(Query query)
    {
       for (Entry<String, Object> entry : binding.entrySet())
       {
          query.setParameter(entry.getKey(), entry.getValue());
       }
    }
 }
