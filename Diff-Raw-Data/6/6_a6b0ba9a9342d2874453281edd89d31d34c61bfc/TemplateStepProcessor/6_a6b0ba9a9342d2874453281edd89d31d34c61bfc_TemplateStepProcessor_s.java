 /*
  * Copyright (C) 2008 Herve Quiroz
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
 
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 
 import java.util.Map;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import net.sf.saxon.s9api.XdmValue;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xml.saxon.SaxonPredicates;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.port.XProcPorts;
 
 /**
  * {@code p:template}.
  * 
  * @author Lucas Soltic
  * @see <a href="http://www.w3.org/TR/xproc-template/#c.template">p:template</a>
  */
 @ExternalResources(read = false, write = false)
 public final class TemplateStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(TemplateStepProcessor.class);
 
     enum Mode {
         REGULAR, XPATH, SINGLE_QUOTE, DOUBLE_QUOTE
     }
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.TEMPLATE;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
         LOG.trace("p:template execution");
 
         final XdmNode templateNode = input.readNode(XProcPorts.TEMPLATE);
         final Iterable<XdmNode> sourceNodeList = input.readNodes(XProcPorts.SOURCE);
         XdmNode sourceNode = null;
         assert Iterables.size(sourceNodeList) <= 1;
 
         if (Iterables.size(sourceNodeList) == 1)
         {
             sourceNode = Iterables.getOnlyElement(sourceNodeList);
         }
 
         // unused
         /*
          * LOG.trace("source count:{}", Iterables.size(sourceNodeList)); if
          * (Iterables.size(sourceNodeList) > 1) { throw
          * XProcExceptions.xc0067(input.getLocation()); }
          */
 
         final Map<QName, String> parameters = input.getParameters(XProcPorts.PARAMETERS);
         final Processor processor = input.getPipelineContext().getProcessor();
         final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
 
         builder.startDocument();
         processNode(templateNode, sourceNode, input, builder, processor, parameters);
         builder.endDocument();
 
         output.writeNodes(XProcPorts.RESULT, builder.getNode());
         LOG.trace("built result:\n{}", builder.getNode());
         LOG.trace("end of p:template");
     }
 
     private void insertXmlFragment(final String raw, final SaxonBuilder builder, final Processor processor)
     {
         final String toBeParsed = "<wrapperElement>" + raw + "</wrapperElement>";
         final XdmNode parsedNode = Saxon.parse(toBeParsed, processor);
         final XdmNode rootNode = Iterables.getOnlyElement(SaxonAxis.childNodes(parsedNode));
 
         // Re-ask for subnodes as the first childNodes() call will give the
         // wrapperElement element
         final Iterable<XdmNode> subNodesList = SaxonAxis.childNodes(rootNode);
         final Iterable<XdmNode> filteredSubNodesList = Iterables.filter(subNodesList,
                 Predicates.not(SaxonPredicates.isIgnorableWhitespace()));
 
         builder.nodes(filteredSubNodesList);
     }
 
     private void processNode(final XdmNode templateNode, final XdmNode sourceNode, final StepInput input,
             final SaxonBuilder builder, final Processor processor, final Map<QName, String> parameters)
     {
         final Iterable<XdmNode> fullNodesList = Iterables.filter(SaxonAxis.childNodes(templateNode), XdmNode.class);
         final Iterable<XdmNode> filteredNodesList = Iterables.filter(fullNodesList,
                 Predicates.not(SaxonPredicates.isIgnorableWhitespace()));
 
         for (final XdmItem currentItem : filteredNodesList)
         {
            assert currentItem.isAtomicValue() == false;
 
             final XdmNode itemAsNode = (XdmNode) currentItem;
             final XdmNodeKind nodeKind = itemAsNode.getNodeKind();
 
             if (nodeKind == XdmNodeKind.ATTRIBUTE || nodeKind == XdmNodeKind.COMMENT
                     || nodeKind == XdmNodeKind.PROCESSING_INSTRUCTION || nodeKind == XdmNodeKind.TEXT)
             {
                 final String nodeValue = itemAsNode.getStringValue();
                 final QName nodeName = itemAsNode.getNodeName();
                 final String evaluatedString = evaluateString(nodeValue, sourceNode, input, nodeKind, parameters);
 
                 switch (nodeKind)
                 {
                     case ATTRIBUTE:
                         builder.attribute(nodeName, evaluatedString);
                         break;
                     case COMMENT:
                         builder.comment(evaluatedString);
                         break;
                     case PROCESSING_INSTRUCTION:
                         builder.processingInstruction(nodeName.toString(), evaluatedString);
                         break;
                     case TEXT:
                         insertXmlFragment(evaluatedString, builder, processor);
                         break;
                     default:
                        LOG.error("unhandled node kind in switch");
                 }
             }
             else
             {
                 if (nodeKind == XdmNodeKind.ELEMENT)
                 {
                     builder.startElement(itemAsNode.getNodeName());
                     processNode(itemAsNode, sourceNode, input, builder, processor, parameters);
                     builder.endElement();
                 }
                 else
                 {
                     LOG.error("unhandled node kind");
                 }
             }
         }
     }
 
     private String evaluateString(final String stringToEvaluate, final XdmNode source, final StepInput input,
             final XdmNodeKind nodeKind, final Map<QName, String> parameters)
     {
         Mode mode = Mode.REGULAR;
         String remainingCharacters = stringToEvaluate;
         String expression = "";
         String result = "";
 
         while (remainingCharacters.length() > 0)
         {
             final char firstCharacter = remainingCharacters.charAt(0);
             final String firstCharacterString = remainingCharacters.substring(0, 1);
 
             if (mode == Mode.REGULAR)
             {
                 switch (firstCharacter)
                 {
                     case '{':
                         if (remainingCharacters.length() > 1 && remainingCharacters.charAt(1) == '{')
                         {
                             result = result.concat("{");
                             remainingCharacters = remainingCharacters.substring(1);
                         }
                         else
                         {
                             expression = "";
                             mode = Mode.XPATH;
                         }
                         break;
 
                     case '}':
                         if (remainingCharacters.length() > 1 && remainingCharacters.charAt(1) == '}')
                         {
                             result = result.concat("}");
                             remainingCharacters = remainingCharacters.substring(1);
                         }
                         else
                         {
                             throw XProcExceptions.xc0067(input.getLocation());
                         }
                         break;
 
                     default:
                         result = result.concat(firstCharacterString);
                         break;
                 }
             }
             else if (mode == Mode.XPATH)
             {
                 switch (firstCharacter)
                 {
                     case '{':
                         throw XProcExceptions.xc0067(input.getLocation());
 
                     case '}':
                         final XdmValue value = input.evaluateXPath(expression, source, parameters);
 
                         // In an attribute value, processing instruction, or
                         // comment, the string value of the XPath expression is
                         // used. In text content, an expression that selects
                         // nodes will cause those nodes to be copied into the
                         // template document.
                         if (nodeKind == XdmNodeKind.TEXT)
                         {
                             result = result.concat(value.toString());
                         }
                         else
                         {
                             result = result.concat(value.itemAt(0).getStringValue());
                         }
 
                         mode = Mode.REGULAR;
                         break;
 
                     case '\'':
                         expression = expression.concat("'");
                         mode = Mode.SINGLE_QUOTE;
                         break;
 
                     case '"':
                         expression = expression.concat("\"");
                         mode = Mode.DOUBLE_QUOTE;
                         break;
 
                     default:
                         expression = expression.concat(firstCharacterString);
                         break;
                 }
             }
             else if (mode == Mode.SINGLE_QUOTE)
             {
                 switch (firstCharacter)
                 {
                     case '\'':
                         expression = expression.concat("'");
                         mode = Mode.XPATH;
                         break;
 
                     default:
                         expression = expression.concat(firstCharacterString);
                         break;
                 }
             }
             else if (mode == Mode.DOUBLE_QUOTE)
             {
                 switch (firstCharacter)
                 {
                     case '\"':
                         expression = expression.concat("\"");
                         mode = Mode.XPATH;
                         break;
 
                     default:
                         expression = expression.concat(firstCharacterString);
                         break;
                 }
             }
 
             remainingCharacters = remainingCharacters.substring(1);
         }
 
         if (mode != Mode.REGULAR)
         {
             throw XProcExceptions.xc0067(input.getLocation());
         }
 
         return result;
     }
 }
