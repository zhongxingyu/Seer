 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.classloading.plugins.visitor;
 
 import java.util.Arrays;
 
 import org.jboss.classloading.spi.visitor.ResourceContext;
 import org.jboss.classloading.spi.visitor.ResourceFilter;
 import org.jboss.classloading.spi.visitor.ResourceVisitor;
 
 /**
  * Federated resource visitor.
  * 
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class FederatedResourceVisitor implements ResourceVisitor
 {
    private ResourceVisitor[] visitors;
    private ResourceFilter[] filters;
    private ResourceFilter[] recurseFilters;
 
    private ResourceFilter filter;
    private ResourceFilter recurseFilter;
    private boolean[] recurseFlags;
    private boolean[] filterFlags;
 
    public FederatedResourceVisitor(ResourceVisitor[] visitors)
    {
       if (visitors == null || visitors.length == 0)
          throw new IllegalArgumentException("Null or empty visitors: " + Arrays.toString(visitors));
       this.visitors = visitors;
    }
 
    public FederatedResourceVisitor(ResourceVisitor[] visitors, ResourceFilter[] filters, ResourceFilter[] recurseFilters)
    {
       this(visitors);
       this.filters = filters;
       this.recurseFilters = recurseFilters;
    }
 
    public ResourceFilter getRecurseFilter()
    {
       if (recurseFilters == null || recurseFilters.length == 0)
          return null;
 
       if (recurseFilter == null)
       {
          recurseFlags = new boolean[recurseFilters.length];
          recurseFilter = new FederatedRecurseFilter();
       }
       return recurseFilter;
    }
 
    public ResourceFilter getFilter()
    {
       if (filters != null && filters.length == 0)
          return null;
 
       if (filter == null)
       {
          if (filters == null)
          {
             filters = new ResourceFilter[visitors.length];
             for (int i =0; i < visitors.length; i++)
                filters[i] = visitors[i].getFilter();
          }
 
         filterFlags = new boolean[filters == null ? 0 : filters.length];
          filter = new FederatedResourceFilter();
       }
       return filter;
    }
 
    public void visit(ResourceContext resource)
    {
       for (int i = 0; i < visitors.length; i++)
       {
          if (filterFlags == null || filterFlags.length <= i || filterFlags[i])
          {
             visitors[i].visit(resource);               
          }
       }
    }
 
    private class FederatedRecurseFilter implements ResourceFilter
    {
       public boolean accepts(ResourceContext resource)
       {
          boolean accept = false;
          for (int i = 0; i < recurseFilters.length; i++)
          {
             recurseFlags[i] = recurseFilters[i] == null || recurseFilters[i].accepts(resource);
             if (recurseFlags[i])
                accept = true;
          }
          return accept;
       }
    }
 
    private class FederatedResourceFilter implements ResourceFilter
    {
       public boolean accepts(ResourceContext resource)
       {
          boolean accept = false;
          for (int i = 0; i < filters.length; i++)
          {
             if (recurseFlags == null || recurseFlags.length <= i || recurseFlags[i])
             {
                filterFlags[i] = filters[i] == null || filters[i].accepts(resource);
                if (filterFlags[i])
                   accept = true;
             }
             else
                filterFlags[i] = false;
          }
          return accept;
       }
    }
 }
