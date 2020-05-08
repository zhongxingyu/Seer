 /*
  * Copyright (C) 2011 Emmanuel Tourdot
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc.xpath;
 
 import java.net.URISyntaxException;
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.functions.ResolveURI;
 import net.sf.saxon.lib.ExtensionFunctionCall;
 import net.sf.saxon.lib.ExtensionFunctionDefinition;
 import net.sf.saxon.om.SequenceIterator;
 import net.sf.saxon.om.StructuredQName;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.SingletonIterator;
 import net.sf.saxon.value.AnyURIValue;
 import net.sf.saxon.value.SequenceType;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcXmlModel;
 
 public final class ResolveUriXPathExtensionFunction extends AbstractXPathExtensionFunction
 {
     private static final Logger LOG = Logger.getLogger(ResolveUriXPathExtensionFunction.class);
 
     @Override
     public ExtensionFunctionDefinition getExtensionFunctionDefinition()
     {
         return new ExtensionFunctionDefinition()
         {private static final long serialVersionUID = 6144192979555615389L;
 
             @Override
             public StructuredQName getFunctionQName()
             {
                 return XProcXmlModel.Functions.RESOLVE_URI;
             }
 
             @Override
             public int getMinimumNumberOfArguments()
             {
                 return 1;
             }
 
             @Override
             public int getMaximumNumberOfArguments()
             {
                 return 2;
             }
 
             @Override
             public SequenceType[] getArgumentTypes()
             {
                 return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.OPTIONAL_STRING };
             }
 
             @Override
             public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
             {
                 return SequenceType.SINGLE_ATOMIC;
             }
 
             @Override
             public ExtensionFunctionCall makeCallExpression()
             {
                 return new ExtensionFunctionCall()
                 {private static final long serialVersionUID = 4367717306903282740L;
 
                     @Override
                     public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                             throws XPathException
                     {
                         final String relative = arguments[0].next().getStringValue();
                         final String base;
                        if (arguments.length == 1)
                         {
                             base = Environment.getCurrentXPathContext().getBaseURI().toASCIIString();
                         }
                         else
                         {
                            base = arguments[1].next().getStringValue();
                         }
                         final String resolvedUri;
                         try
                         {
                             resolvedUri = ResolveURI.makeAbsolute(relative, base).toASCIIString();
                             LOG.trace("resolvedUri = {}", resolvedUri);
                             return SingletonIterator.makeIterator(new AnyURIValue(resolvedUri));
                         }
                         catch (URISyntaxException e)
                         {
                         }
                         return null;
                     }
                 };
             }
         };
     }
 }
