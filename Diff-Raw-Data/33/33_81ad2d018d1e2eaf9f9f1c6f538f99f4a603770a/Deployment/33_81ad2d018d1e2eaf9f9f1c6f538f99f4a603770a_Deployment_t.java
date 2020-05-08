 /*
  * Copyright (C) 2003-2013 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
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
 package org.exoplatform.acceptance.model;
 
 import lombok.Getter;
 import lombok.Setter;
 import org.springframework.data.annotation.TypeAlias;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 /**
  * Represent an acceptance deployment
  */
 @Document(collection = "deployment")
 @TypeAlias("Deployment")
 public class Deployment {
  @Getter
  @Setter
   private Software software;
   //private JVM jvm;
   //private OS os;
 }
