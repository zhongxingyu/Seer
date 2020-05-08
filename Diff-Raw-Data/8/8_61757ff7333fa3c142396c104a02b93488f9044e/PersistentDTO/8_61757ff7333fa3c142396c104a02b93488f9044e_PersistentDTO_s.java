 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.domain;
 
 /**
  *
  * @author Imran M Yousuf
  */
 public abstract class PersistentDTO <Template extends PersistentDTO> implements Domain<Template>
 {
     
     protected Integer id;
     
     protected Integer version;
     
     /** Creates a new instance of PersistentDTO */
     public PersistentDTO()
     {
     }
     
     public int compareTo(Template o)
     {
         if(o == null)
         {
             throw new IllegalArgumentException();
         }
         return o.getId().compareTo(id);
     }
     
     public int compare(Template o1, Template o2)
     {
         if(o1 == null && o2 == null)
         {
             return 0;
         }
         if(o1 == null && o2 != null)
         {
             return -1;
         }
         if(o1 != null && o2 == null)
         {
             return 1;
         }
         return o1.id.compareTo(o2.id);
     }
     
     public Integer getId()
     {
         return id;
     }
     
     public void setId(Integer id)
     {
         this.id = id;
     }
     
     public Integer getVersion()
     {
         return version;
     }
     
     public void setVersion(Integer version)
     {
         this.version = version;
     }
     
 }
