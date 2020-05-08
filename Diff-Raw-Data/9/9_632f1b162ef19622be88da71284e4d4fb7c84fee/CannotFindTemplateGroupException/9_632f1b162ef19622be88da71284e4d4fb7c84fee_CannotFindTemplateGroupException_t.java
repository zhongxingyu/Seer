 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: CannotFindTemplateGroupException.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Represents the exceptional situation when the template group
  * cannot be found.
  *
  * Date: 7/9/13
  * Time: 5:42 AM
  *
  */
 package org.acmsl.queryj.api.exceptions;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 
 /**
  * Represents the exceptional situation when the template group
  * cannot be found.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro</a>
  * @since 2013/07/13
  */
 @ThreadSafe
 public class CannotFindTemplateGroupException
    extends InvalidTemplateException
 {
     /**
      * Creates an instance for given class.
      * @param contextClassName the class name of the context.
      */
     public CannotFindTemplateGroupException(@NotNull final String contextClassName)
     {
         super("cannot.find.template.group", new Object[] { contextClassName });
     }
 }
 
