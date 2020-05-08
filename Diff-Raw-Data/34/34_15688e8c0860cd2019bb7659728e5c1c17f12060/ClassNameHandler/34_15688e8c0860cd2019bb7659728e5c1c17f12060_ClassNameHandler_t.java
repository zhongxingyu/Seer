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
  * Filename: ClassNameHandler.java
  *
  * Author: Jose San Leandro Armendariz (chous)
  *
  * Description: Resolves "class_name" placeholders.
  *
  * Date: 5/24/12
  * Time: 5:06 AM
  *
  */
 package org.acmsl.queryj.placeholders;
 
 /*
  * Importing some project classes.
  */
 import org.acmsl.queryj.api.TemplateContext;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /**
  * Resolves "class_name" placeholders.
  * @author <a href="mailto:chous@acm-sl.org">chous</a>
  * @since 3.0
  * Created: 2012/05/24
  */
 @ThreadSafe
 public class ClassNameHandler<C extends TemplateContext>
     extends AbstractDecoratedStringHandler<C>
 {
     /**
      * Creates a {@link ClassNameHandler} using given {@link org.acmsl.queryj.api.QueryJTemplateContext context}.
      * @param context the {@link org.acmsl.queryj.api.QueryJTemplateContext context}.
      */
     public ClassNameHandler(@NotNull final C context)
     {
         super(context);
     }
 
     /**
      * Returns "class_name".
      * @return such placeholder.
      */
     @NotNull
     @Override
     public String getPlaceHolder()
     {
         return "class_name";
     }
 
     //            defaultThemeUtils.buildDAOImplementationClassName(
     // t_strCapitalizedEngine, t_strSingularName),
     //             stringUtils.capitalize(
     // englishGrammarUtils.getSingular(
     //    tableName.toLowerCase()),
     //    '_');
 
 
     /**
     * Resolves the actual value using given {@link TemplateContext context}.
      *
     * @param context the {@link TemplateContext context}.
      * @return such value.
      */
     @NotNull
     @Override
     protected String resolveContextValue(@NotNull final C context)
     {
         return context.getTemplateName();
     }
 }
