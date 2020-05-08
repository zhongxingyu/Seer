 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.designtime.internal.symbols;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.context.symbol.source.AbstractSymbolSourceProviderFactory;
 import org.eclipse.jst.jsf.context.symbol.source.ISymbolConstants;
 import org.eclipse.jst.jsf.context.symbol.source.ISymbolSourceProvider;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.internal.jsp.JSPModelProcessor;
 import org.eclipse.jst.jsf.designtime.symbols.FileContextUtil;
 import org.eclipse.jst.jsf.designtime.symbols.SymbolUtil;
 
 
 /**
  * Self-factory for a symbol provider that derives symbol information from
  * meta-data annotations on tag attributes that declare runtime EL variables
  * 
  * @author cbateman
  *
  */
 public class JSPTagVariableSymbolSourceProvider extends
         AbstractSymbolSourceProviderFactory implements ISymbolSourceProvider 
 {
     protected ISymbolSourceProvider create(IProject project) 
     {
         return this;
     }
 
     public ISymbol[] getSymbols(IAdaptable context, int symbolScopeMask) 
     {
         final IFile   fileContext = FileContextUtil.deriveIFileFromContext(context);
         
        if (isProvider(fileContext) &&  fileContext.isAccessible())
         {
         	JSPModelProcessor modelProcessor = null;
         	
             try
             {
                 modelProcessor = JSPModelProcessor.get(fileContext);
 
                 // ensure internal model is sync'ed with document
                 // but don't force refresh
                 modelProcessor.refresh(!JSPModelProcessor.FORCE_REFRESH, JSPModelProcessor.RUN_ON_CURRENT_THREAD);
                 final List<ISymbol> symbols = new ArrayList();
                 
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_REQUEST) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_REQUEST_STRING).values());
                 }
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_SESSION) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_SESSION_STRING).values());
                 }
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_APPLICATION) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_APPLICATION_STRING).values());
                 }
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_NONE) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_NONE_STRING).values());
                 }
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_VIEW) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_VIEW_STRING).values());
                 }
                 if ((symbolScopeMask & ISymbolConstants.SYMBOL_SCOPE_FLASH) != 0)
                 {
                     symbols.addAll(modelProcessor.getMapForScope(ISymbolConstants.SYMBOL_SCOPE_FLASH_STRING).values());
                 }  
                 return symbols.toArray(ISymbol.EMPTY_SYMBOL_ARRAY);
             }
             catch (Exception e)
             {
                 JSFCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID, 0, "Error acquiring model processor",e)); //$NON-NLS-1$
                 // fall-through to empty symbol array
             }
         }
         
         return ISymbol.EMPTY_SYMBOL_ARRAY;
     }
 
     public ISymbol[] getSymbols(String prefix, IAdaptable context,
             int symbolScopeMask) {
         return SymbolUtil.
             filterSymbolsByPrefix(getSymbols(context, symbolScopeMask), prefix);
     }
 
     public boolean isProvider(IAdaptable context) 
     {
         IFile file = FileContextUtil.deriveIFileFromContext(context);
         
         return (file != null
                 && file.getProject() == this.getProject());
     }
 }
