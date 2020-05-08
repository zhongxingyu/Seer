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
 package org.trancecode.xproc;
 
 import com.google.common.collect.ImmutableSet;
 
 import java.net.URI;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.XProcException.Type;
 import org.trancecode.xproc.port.PortReference;
 import org.trancecode.xproc.step.Step;
 import org.trancecode.xproc.variable.Variable;
 
 /**
  * @author Herve Quiroz
  */
 public final class XProcExceptions
 {
     private XProcExceptions()
     {
         // No instantiation
     }
 
     public static XProcException xd0001(final Location location)
     {
         return newXProcException(Type.DYNAMIC, 1, location,
                 "It is a dynamic error if a non-XML resource is produced on a step output or arrives on a step input.");
     }
 
     public static XProcException xd0003(final Location location, final int numberOfNodes)
     {
         return newXProcException(Type.DYNAMIC, 3, location,
                 "the viewport source does not provide exactly one document but %s", numberOfNodes);
     }
 
     public static XProcException xd0004(final Location location)
     {
         return newXProcException(Type.DYNAMIC, 4, location,
                 "no subpipeline is selected by the p:choose and no default is provided");
     }
 
     public static XProcException xd0006(final Location location, final PortReference port)
     {
         return newXProcException(Type.DYNAMIC, 6, location, "wrong number of input documents in port %s", port);
     }
 
     public static XProcException xd0007(final Location location)
     {
         return newXProcException(Type.DYNAMIC, 7, location, "wrong number of input documents in ");
     }
 
     public static XProcException xd0010(final XdmNode node)
     {
         return newXProcException(Type.DYNAMIC, 10, SaxonLocation.of(node),
                 "the match expression on p:viewport does not match an element or document: %s", node.getNodeKind());
     }
 
     public static XProcException xd0011(final Location location, final String resource, final Exception error)
     {
         final XProcException exception = newXProcException(Type.DYNAMIC, 11, location, "cannot load document: %s",
                 resource);
         exception.initCause(error);
         return exception;
     }
 
     public static XProcException xd0014(final XdmNode node)
     {
         return newXProcException(
                 Type.DYNAMIC,
                 14,
                 SaxonLocation.of(node),
                 "It is a dynamic error for any unqualified attribute names other than 'name', 'namespace', or 'value' to appear on a c:param element.");
     }
 
     public static XProcException xd0023(final Location location, final String select, final String errorMessage)
     {
         return newXProcException(Type.DYNAMIC, 23, location, "XPath expression cannot be evaluated: %s\n%s", select,
                 errorMessage);
     }
 
     public static XProcException xd0025(final Location location)
     {
         return newXProcException(
                 Type.DYNAMIC,
                 25,
                 location,
                 "It is a dynamic error if the namespace attribute is specified, the name contains a colon, and the specified namespace is not the same as the in-scope namespace binding for the specified prefix.");
     }
 
     public static XProcException xd0034(final Location location)
     {
         return newXProcException(
                 Type.DYNAMIC,
                 34,
                 location,
                 "It is a dynamic error to specify a new namespace or prefix if the lexical value of the specified name contains a colon (or if no wrapper is explicitly specified).");
     }
 
     public static XProcException xs0004(final Variable option)
     {
         return newXProcException(
                 Type.STATIC,
                 4,
                 option.getLocation(),
                 "It is a static error if an option or variable declaration duplicates the name of any other option or variable in the same environment.");
     }
 
     public static XProcException xs0018(final Variable option)
     {
         return newXProcException(Type.STATIC, 18, option.getLocation(), "Option %s is required and is missing a value",
                 option.getName());
     }
 
     public static XProcException xs0026(final Location location, final Step step, final String port)
     {
         return newXProcException(
                 Type.STATIC,
                 26,
                 location,
                 "It is a static error if the port specified on the p:log is not the name of an output port on the step in which it appears or if more than one p:log element is applied to the same port ; step = %s ; port = %s",
                 step.getName(), port);
     }
 
     public static XProcException xs0031(final Location location, final QName optionName, final QName stepType)
     {
         return newXProcException(Type.STATIC, 31, location, "Option %s is not declared on this step type (%s)",
                 optionName, stepType);
     }
 
     public static XProcException xs0044(final XdmNode element)
     {
         return newXProcException(Type.STATIC, 44, SaxonLocation.of(element), "Unsupported element: %s",
                 element.getNodeName());
     }
 
     public static XProcException xs0052(final Location location, final URI uri)
     {
         return newXProcException(
                 Type.STATIC,
                 52,
                 location,
                 "%s cannot be retrieved or if, once retrieved, it does not point to a p:library, p:declare-step, or p:pipeline",
                 uri);
     }
 
     public static XProcException xc0002(final XdmNode node)
     {
         return newXProcException(Type.STEP, 2, SaxonLocation.of(node),
                 "It is a dynamic error if the value starts with the string \"--\".");
     }
 
     public static XProcException xc0003(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 3,
                 SaxonLocation.of(node),
                 "It is a dynamic error if a username or password is specified without specifying an auth-method, if the requested auth-method isn't supported, or the authentication challenge contains an authentication method that isn't supported.");
     }
 
     public static XProcException xc0004(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 4,
                 SaxonLocation.of(node),
                 "It is a dynamic error if the status-only attribute has the value true and the detailed attribute does not have the value true.");
     }
 
     public static XProcException xc0005(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 5,
                 SaxonLocation.of(node),
                 "It is a dynamic error if the request contains a c:body or c:multipart but the method does not allow for an entity body being sent with the request.");
     }
 
     public static XProcException xc0006(final XdmNode node)
     {
         return newXProcException(Type.STEP, 6, SaxonLocation.of(node),
                 "It is a dynamic error if the method is not specified on a c:request.");
     }
 
     public static XProcException xc0010(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 10,
                 SaxonLocation.of(node),
                 "It is a dynamic error if an encoding of base64 is specified and the character set is not specified or if the specified character set is not supported by the implementation.");
     }
 
     public static XProcException xc0012(final Step step)
     {
         return newXProcException(
                 Type.STEP,
                 12,
                 step.getLocation(),
                 "It is a dynamic error if the contents of the directory path are not available to the step due to access restrictions in the environment in which the pipeline is run.");
     }
 
     public static XProcException xc0013(final XdmNode node)
     {
         return newXProcException(Type.STEP, 13, SaxonLocation.of(node),
                 "It is a dynamic error if the pattern matches a processing instruction and the new name has a non-null namespace.");
     }
 
     public static XProcException xc0014(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 14,
                 SaxonLocation.of(node),
                 "It is a dynamic error if the XML namespace (http://www.w3.org/XML/1998/namespace) or the XMLNS namespace (http://www.w3.org/2000/xmlns/) is the value of either the from option or the to option.");
     }
 
     public static XProcException xc0017(final Step step)
     {
         return newXProcException(
                 Type.STEP,
                 17,
                 step.getLocation(),
                 "It is a dynamic error if the absolute path does not identify a directory.");
     }
 
     public static XProcException xc0019(final Step step)
     {
         return newXProcException(Type.STEP, 19, step.getLocation(), "Documents are not equal in step %s",
                 step.getName());
     }
 
     public static XProcException xc0020(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 20,
                 SaxonLocation.of(node),
                 "It is a dynamic error if the the user specifies a value or values that are inconsistent with each other or with the requirements of the step or protocol.");
     }
 
     public static XProcException xc0022(final XdmNode node)
     {
         return newXProcException(
                 Type.STEP,
                 22,
                 SaxonLocation.of(node),
                 "it is a dynamic error if the content of the c:body element does not consist of exactly one element, optionally preceded and/or followed by any number of processing instructions, comments or whitespace characters");
     }
 
     public static XProcException xc0023(final XdmNode node, final XdmNodeKind... allowedNodeTypes)
     {
         return xc0023(node, ImmutableSet.copyOf(allowedNodeTypes));
     }
 
     public static XProcException xc0023(final XdmNode node, final Iterable<XdmNodeKind> allowedNodeTypes)
     {
         return newXProcException(Type.STEP, 23, SaxonLocation.of(node),
                 "Selected node type %s is not allowed by the step ; allowed types: %s ", node.getNodeKind(),
                 allowedNodeTypes);
     }
 
     public static XProcException xc0025(final Location location, final XdmNode node, final String position)
     {
         return newXProcException(Type.STEP, 25, location,
                 "the match pattern matches a %s which is not allowed when the position is %s", node.getNodeKind(),
                 position);
     }
 
     public static XProcException xc0027(final Location location)
     {
         return newXProcException(Type.STEP, 27, location,
                 "It is a dynamic error if the document is not valid or the step doesn't support DTD validation.");
     }
 
     public static XProcException xc0028(final Location location)
     {
         return newXProcException(Type.STEP, 28, location,
                 "it is a dynamic error if the content of the c:body element does not consist entirely of characters");
     }
 
     public static XProcException xc0029(final Location location)
     {
         return newXProcException(Type.STEP, 29, location,
                 "It is a dynamic error if an XInclude error occurs during processing.");
     }
 
     public static XProcException xc0030()
     {
         return newXProcException(Type.STEP, 30, null,
                 "It is a dynamic error if the override-content-type value cannot be used (e.g. text/plain to override image/png).");
     }
 
     public static XProcException xc0035(final Location location)
     {
         return newXProcException(Type.STEP, 35, location,
                 "It is a dynamic error to specify both result-is-xml and wrap-result-lines.");
     }
 
     public static XProcException xc0036(final Location location)
     {
         return newXProcException(
                 Type.STEP,
                 36,
                 location,
                 "It is a dynamic error if the requested hash algorithm is not one that the processor understands or if the value or parameters are not appropriate for that algorithm.");
     }
 
     public static XProcException xc0038(final Location location, final String version)
     {
         return newXProcException(Type.STEP, 38, location, "XSLT version %s not supported", version);
     }
 
     public static XProcException xc0039(final Location location, final int numberOfInputDocuments)
     {
         return newXProcException(
                 Type.STEP,
                 39,
                 location,
                 "It is a dynamic error if a sequence of documents (including an empty sequence) is provided to an XSLT 1.0 step. (%s input documents)",
                 numberOfInputDocuments);
     }
 
     public static XProcException xc0040(final Location location)
     {
         return newXProcException(Type.STEP, 40, location,
                 "It is a dynamic error if the document element of the document that arrives on the source port is not c:request.");
     }
 
     public static XProcException xc0050(final Location location)
     {
         return newXProcException(Type.STEP, 50, location,
                 "It is a dynamic error if the URI scheme is not supported or the step cannot store to the specified location.");
     }
 
     public static XProcException xc0051(final Location location)
     {
         return newXProcException(Type.STEP, 51, location,
                 "It is a dynamic error if the content-type specified is not supported by the implementation.");
     }
 
     public static XProcException xc0052(final Location location)
     {
         return newXProcException(Type.STEP, 52, location,
                 "It is a dynamic error if the encoding specified is not supported by the implementation.");
     }
 
     public static XProcException xc0053(final Location location)
     {
         return newXProcException(Type.STEP, 53, location,
                 "It is a dynamic error if the assert-valid option is true and the input document is not valid.");
     }
 
     public static XProcException xc0057(final Location location)
     {
         return newXProcException(
                 Type.STEP,
                 57,
                 location,
                 "It is a dynamic error if the sequence that results from evaluating the XQuery contains items other than documents and elements.");
     }
 
     public static XProcException xc0058(final Location location)
     {
         return newXProcException(Type.STEP, 58, location,
                 "It is a dynamic error if the all and relative options are both true.");
     }
 
     public static XProcException xc0059(final Location location)
     {
         return newXProcException(
                 Type.STEP,
                 59,
                 location,
                 "It is a dynamic error if the QName value in the attribute-name option uses the prefix \"xmlns\" or any other prefix that resolves to the namespace name \"http://www.w3.org/2000/xmlns/\".");
     }
 
     public static XProcException xc0060(final Location location)
     {
         return newXProcException(Type.STEP, 60, location,
                 "It is a dynamic error if the processor does not support the specified version of the UUID algorithm.");
     }
 
     public static XProcException xc0062(final Location location, final XdmNode node)
     {
         return newXProcException(Type.STEP, 62, location, "the match option matches a namespace node: %s",
                 node.getNodeName());
     }
 
     public static XProcException xc0064(final Location location, final int exitCode, final int failureThreshold)
     {
         return newXProcException(Type.STEP, 64, location,
                 "the exit code (%d) is greater than the failure threshold (%s)", exitCode, failureThreshold);
     }
 
     private static XProcException newXProcException(final Type type, final int code, final Location location,
             final String message, final Object... parameters)
     {
         final XProcException exception = new XProcException(type, code, location, message, parameters);
         final StackTraceElement[] stackTrace = new StackTraceElement[exception.getStackTrace().length - 1];
         System.arraycopy(exception.getStackTrace(), 1, stackTrace, 0, stackTrace.length);
         exception.setStackTrace(stackTrace);
         return exception;
     }
 }
