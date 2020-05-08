 /*
  * Copyright (C) 2010 Romain Deltour
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
 package org.trancecode.xml.saxon;
 
 import com.google.common.base.Preconditions;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathExecutable;
 import net.sf.saxon.s9api.XPathSelector;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 
 /**
  * A utility class to evaluate an XSLT Match pattern against nodes.
  *<p>
  * The implementation is based on the s9api front-end to the Saxon pattern
  * engine (see this <a
  * href="http://markmail.org/message/cy6n4zffsh3zw5mz"/>saxon-help email
  * thread</a> for more information).
  * </p>
  * 
 * @see XPathCompiler#compilePattern(String)
  * @author Romain Deltour
  */
 public class SaxonPatternMatcher
 {
 
     private static final Logger LOG = Logger.getLogger(SaxonPatternMatcher.class);
     private XPathExecutable xpathExec;
 
     /**
      * Creates a new matcher for the given XSLT match pattern.
      * 
      * @param processor
      *            The Saxon processor used to create the internal
      *            {@link XPathCompiler}
     * @param pattern
      *            The XSLT pattern evaluated by this matcher
      * @throws NullPointerException
      *             is <code>processor</code> or <code>pattern</code> is
      *             <code>null</code>
      */
     public SaxonPatternMatcher(final Processor processor, final String pattern)
     {
         Preconditions.checkNotNull(processor);
         Preconditions.checkNotNull(pattern);
 
         final XPathCompiler xpathCompiler = processor.newXPathCompiler();
         // TODO declare namespaces ?
         try
         {
             xpathExec = xpathCompiler.compilePattern(pattern);
         }
         catch (final SaxonApiException e)
         {
             throw new IllegalArgumentException(e.getMessage(), e);
         }
     }
 
     /**
      * Evaluates the given node against the XSLT pattern held by this matcher.
      * 
      * @return <code>true</code> iff <code>node</code> matches the pattern.
      * @throws NullPointerException
      *             is <code>node</code> is <code>null</code>
      */
     public boolean match(final XdmNode node)
     {
         Preconditions.checkNotNull(node);
 
         try
         {
             final XPathSelector s = xpathExec.load();
             s.setContextItem(node);
             return s.effectiveBooleanValue();
         }
         catch (final SaxonApiException e)
         {
             LOG.warn("Unexpected {}: {}", e.getClass().getName(), e.getMessage());
             return false;
         }
 
     }
 }
