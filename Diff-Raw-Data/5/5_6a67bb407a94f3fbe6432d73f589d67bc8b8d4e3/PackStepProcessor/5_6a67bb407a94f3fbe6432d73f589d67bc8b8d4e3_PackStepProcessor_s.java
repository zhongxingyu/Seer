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
  *
  * $Id$
  */
 package org.trancecode.xproc.step;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * {@code p:pack}.
  * 
  * @author Emmanuel Tourdot
  * @see <a href="http://www.w3.org/TR/xproc/#c.pack">p:pack</a>
  */
 public final class PackStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(PackStepProcessor.class);
     private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.ATTRIBUTE,
             XdmNodeKind.PROCESSING_INSTRUCTION);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.PACK;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         final Iterable<XdmNode> sourceDoc = readeSequencePort(input, XProcPorts.SOURCE);
         final Iterable<XdmNode> alternateDoc = readeSequencePort(input, XProcPorts.ALTERNATE);
 
         final String wrapperLocalName = input.getOptionValue(XProcOptions.WRAPPER);
         assert wrapperLocalName != null;        
 
         final String wrapperPrefix = input.getOptionValue(XProcOptions.WRAPPER_PREFIX, null);
         final String wrapperNamespaceUri = input.getOptionValue(XProcOptions.WRAPPER_NAMESPACE, null);
         final QName wrapperQName = StepUtils.getNewNamespace(wrapperPrefix, wrapperNamespaceUri, wrapperLocalName, input.getStep());
 
        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
            .getUnderlyingConfiguration());
         final Iterator<XdmNode> srcIterator = sourceDoc.iterator();
         final Iterator<XdmNode> altIterator = alternateDoc.iterator();
         while (srcIterator.hasNext() || altIterator.hasNext())
         {
             builder.startDocument();
             builder.startElement(wrapperQName);
             builder.startContent();
             if (srcIterator.hasNext())
             {
                 builder.nodes(srcIterator.next());
             }
             if (altIterator.hasNext())
             {
                 builder.nodes(altIterator.next());
             }
             builder.endDocument();
             output.writeNodes(XProcPorts.RESULT, builder.getNode());
         }
     }
 
     private Iterable<XdmNode> readeSequencePort(final StepInput input, final String portName)
     {
         final Iterable<XdmNode> source = input.readNodes(portName);
         final Iterator<XdmNode> iterator = source.iterator();
         final ImmutableList.Builder builder = new ImmutableList.Builder();
         while (iterator.hasNext())
         {
             final XdmNode node = iterator.next();
             if (XdmNodeKind.DOCUMENT.equals(node.getNodeKind()))
             {
                 builder.addAll(SaxonAxis.childNodes(node));
             }
             else
             {
                 builder.add(node);
             }
         }
         return builder.build();
     }
 
 }
