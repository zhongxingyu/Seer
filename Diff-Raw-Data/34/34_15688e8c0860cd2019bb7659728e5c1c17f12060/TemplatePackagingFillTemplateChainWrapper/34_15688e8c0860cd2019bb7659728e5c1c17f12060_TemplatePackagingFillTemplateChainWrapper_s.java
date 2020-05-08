 /*
                     QueryJ's Template Packaging
 
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
  * Filename: TemplatePackagingFillTemplateChainWrapper.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Wraps a chain to provide Template Packaging's placeholders.
  *
  * Date: 2013/08/25
  * Time: 16:34
  *
  */
 package org.acmsl.queryj.templates.packaging;
 
 /*
  * Importing QueryJ Template Packaging classes.
  */
 import org.acmsl.queryj.templates.packaging.placeholders.TemplateDefHandler;
 
 /*
  * Importing QueryJ-Core classes.
  */
 import org.acmsl.queryj.api.FillTemplateChain;
 import org.acmsl.queryj.api.handlers.fillhandlers.FillHandler;
 import org.acmsl.queryj.api.placeholders.AbstractFillTemplateChainWrapper;
 
 /*
  * Importing QueryJ-Placeholders classes.
  */
import org.acmsl.queryj.placeholders.ClassNameHandler;
 import org.acmsl.queryj.placeholders.CopyrightYearsHandler;
 import org.acmsl.queryj.placeholders.CurrentYearHandler;
 import org.acmsl.queryj.placeholders.FileNameHandler;
 import org.acmsl.queryj.placeholders.PackageNameHandler;
 import org.acmsl.queryj.placeholders.SerialVersionUIDHandler;
 import org.acmsl.queryj.placeholders.TemplateNameHandler;
 import org.acmsl.queryj.placeholders.TimestampHandler;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /*
  * Importing JDK classes.
  */
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Wraps a chain to provide Template Packaging's placeholders.
  * @author <a href="mailto:queryj@acm-sl.org">Jose San Leandro</a>
  * @since 3.0
  * Created: 2013/08/25 16:34
  */
 @ThreadSafe
 public class TemplatePackagingFillTemplateChainWrapper
     extends AbstractFillTemplateChainWrapper<DefaultTemplatePackagingContext>
 {
     /**
      * Creates a new chain wrapper.
      * @param chain the chain to wrap.
      */
     public TemplatePackagingFillTemplateChainWrapper(@NotNull final FillTemplateChain<DefaultTemplatePackagingContext> chain)
     {
         super(chain);
     }
 
     /**
      * Retrieves the list of generic placeholder handlers.
      * @param context the context.
      * @return such list.
      */
     @NotNull
     @Override
     protected List<FillHandler> getHandlers(@NotNull final DefaultTemplatePackagingContext context)
     {
         @NotNull final List<FillHandler> result = new ArrayList<FillHandler>();
 
        result.add(new ClassNameHandler<DefaultTemplatePackagingContext>(context));
         result.add(new CopyrightYearsHandler());
         result.add(new CurrentYearHandler());
         result.add(new FileNameHandler<DefaultTemplatePackagingContext>(context));
         result.add(new PackageNameHandler<DefaultTemplatePackagingContext>(context));
         result.add(new SerialVersionUIDHandler<DefaultTemplatePackagingContext>(context));
         result.add(new TimestampHandler());
         result.add(new TemplateNameHandler<DefaultTemplatePackagingContext>(context));
         result.add(new TemplateDefHandler(context));
 
         return result;
     }
 }
