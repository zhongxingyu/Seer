 /*
  * Copyright (C) 2010 Herve Quiroz
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
 package org.trancecode.xproc.step;
 
 import com.google.common.base.Supplier;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.Closeables;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.trancecode.io.TcByteStreams;
 import org.trancecode.lang.StringPredicates;
 import org.trancecode.lang.TcStrings;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.XProcXmlModel;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.XProcOptions;
 
 /**
  * Step processor for the p:exec optional XProc step.
  * 
  * @author Herve Quiroz
  * @see <a href="http://www.w3.org/TR/xproc/#c.exec">p:exec</a>
  */
 public final class ExecStepProcessor extends AbstractStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(AddAttributeStepProcessor.class);
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.EXEC;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output) throws Exception
     {
         final String command = input.getOptionValue(XProcOptions.COMMAND);
         final String argSeparator = input.getOptionValue(XProcOptions.ARG_SEPARATOR, " ");
         final Iterable<String> args = TcStrings.split(input.getOptionValue(XProcOptions.ARGS), argSeparator);
         final String cwd = input.getOptionValue(XProcOptions.CWD);
 
         final List<XdmNode> inputDocuments = ImmutableList.copyOf(input.readNodes(XProcPorts.SOURCE));
         if (inputDocuments.size() > 1)
         {
             throw XProcExceptions.xd0006(input.getStep().getLocation(),
                     input.getStep().getPortReference(XProcPorts.SOURCE));
         }
         final boolean sourceIsXml = Boolean.parseBoolean(input.getOptionValue(XProcOptions.SOURCE_IS_XML));
         final boolean resultIsXml = Boolean.parseBoolean(input.getOptionValue(XProcOptions.RESULT_IS_XML));
         final boolean wrapResultLines = Boolean.parseBoolean(input.getOptionValue(XProcOptions.WRAP_RESULT_LINES));
         final boolean errorsIsXml = Boolean.parseBoolean(input.getOptionValue(XProcOptions.ERRORS_IS_XML));
         final boolean wrapErrorLines = Boolean.parseBoolean(input.getOptionValue(XProcOptions.WRAP_ERROR_LINES));
         if ((resultIsXml && wrapResultLines) || (errorsIsXml && wrapErrorLines))
         {
             throw XProcExceptions.xc0035(input.getStep().getLocation());
         }
 
         final List<String> commandLine = Lists.newArrayList();
         commandLine.add(command);
         Iterables.addAll(commandLine, Iterables.filter(args, StringPredicates.isNotEmpty()));
         LOG.trace("commandLine = {}", commandLine);
         final ProcessBuilder processBuilder = new ProcessBuilder(commandLine.toArray(new String[0]));
         processBuilder.redirectErrorStream(true);
         if (cwd != null)
         {
             processBuilder.directory(new File(cwd));
         }
         final Process process = processBuilder.start();
 
         if (!inputDocuments.isEmpty())
         {
             final String inputContent;
             if (sourceIsXml)
             {
                 inputContent = inputDocuments.get(0).toString();
             }
             else
             {
                 inputContent = SaxonAxis.childElement(inputDocuments.get(0)).getStringValue();
             }
 
             new Thread(new Runnable()
             {
                 @Override
                 public void run()
                 {
                     try
                     {
                         IOUtils.write(inputContent, process.getOutputStream());
                     }
                     catch (final IOException e)
                     {
                         throw new IllegalStateException(e);
                     }
                     finally
                     {
                         Closeables.closeQuietly(process.getOutputStream());
                     }
                 }
             }).start();
         }
 
         final Supplier<File> stdout = TcByteStreams.copyToTempFile(process.getInputStream());
         final Supplier<File> stderr = TcByteStreams.copyToTempFile(process.getErrorStream());
 
         final int exitCode = process.waitFor();
         final File stdoutFile = stdout.get();
         final File stderrFile = stderr.get();
         process.destroy();
 
        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
         builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.RESULT, input.getStep().getNode());
        if (resultIsXml)
         {
            final XdmNode resultNode = input.getPipelineContext().getProcessor().newDocumentBuilder().build(stdoutFile);
             builder.nodes(SaxonAxis.childElement(resultNode));
         }
         else
         {
            if (wrapResultLines)
             {
                 @SuppressWarnings("unchecked")
                final List<String> lines = FileUtils.readLines(stdoutFile);
                 for (final String line : lines)
                 {
                     builder.startElement(XProcXmlModel.Elements.LINE);
                     builder.text(line);
                     builder.endElement();
                 }
             }
             else
             {
                builder.text(FileUtils.readFileToString(stdoutFile));
             }
         }
 
         builder.endElement();
         builder.endDocument();
 
        output.writeNodes(XProcPorts.RESULT, builder.getNode());
     }
 }
