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
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.transform.Source;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.testng.Assert;
 import org.testng.AssertJUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.trancecode.AbstractTest;
 import org.trancecode.TcAssert;
 import org.trancecode.io.Uris;
 
 /**
  * @author Herve Quiroz
  */
 public abstract class AbstractXProcTest extends AbstractTest
 {
     public static final String PROPERTY_TARGET_DIRECTORY = "project.build.directory";
     public static final String DEFAULT_TARGET_DIRECTORY = "target";
 
     private static final org.trancecode.logging.Logger LOG = org.trancecode.logging.Logger
             .getLogger(AbstractXProcTest.class);
     private XProcTestSuiteReportBuilder reportBuilder;
 
     @BeforeClass
     public void setupReportBuilder()
     {
         reportBuilder = new XProcTestSuiteReportBuilder();
     }
 
     @AfterClass
     public void writeReportToFile() throws Exception
     {
         final String reportDirectoryPath = System.getProperty(PROPERTY_TARGET_DIRECTORY, DEFAULT_TARGET_DIRECTORY);
         final File reportDirectory = new File(reportDirectoryPath, "xproc-test-reports");
         FileUtils.forceMkdir(reportDirectory);
         LOG.info("report directory: {}", reportDirectory.getAbsolutePath());
         final File reportFile = new File(reportDirectory, getClass().getSimpleName() + ".xml");
         LOG.info("report file: {}", reportFile.getAbsolutePath());
         reportBuilder.write(reportFile);
     }
 
     @BeforeClass
     public static void setupLoggingLevel()
     {
         Logger.getLogger("org.trancecode").setLevel(Level.TRACE);
         Logger.getLogger("org.apache").setLevel(Level.TRACE);
     }
 
     @BeforeClass
     public static void parseStandardLibrary()
     {
         Logger.getLogger("org.trancecode").setLevel(Level.INFO);
         new PipelineConfiguration();
         Logger.getLogger("org.trancecode").setLevel(Level.TRACE);
     }
 
     protected void test(final URL testUrl) throws Exception
     {
         test(testUrl, null);
     }
 
     protected void test(final URL testUrl, final String testSuite) throws Exception
     {
         LOG.info("Starting test: {}", testUrl);
         final PipelineConfiguration configuration = new PipelineConfiguration();
         final PipelineProcessor pipelineProcessor = new PipelineProcessor(configuration);
         final XProcTestCase test = getTest(testUrl, configuration.getProcessor(), testSuite);
 
         try
         {
             test(test, pipelineProcessor);
         }
         catch (final XProcException e)
         {
             if (!reportBuilder.result(test, e).failed())
             {
                 return;
             }
 
             if (test.getError() != null)
             {
                 Assert.fail(String.format("expected error: %s ; actual: %s", test.getError(), e.getName()), e);
             }
 
             throw e;
         }
         catch (final Throwable e)
         {
             reportBuilder.result(test, e);
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }

             Assert.fail(e.getMessage(), e);
         }
 
         if (reportBuilder.result(test, null).failed())
         {
             Assert.fail(String.format("expected error: %s", test.getError()));
         }
         LOG.info("Ending test: {}", testUrl);
     }
 
     private void test(final XProcTestCase test, final PipelineProcessor pipelineProcessor)
     {
         log.info("== parse pipeline {} ==", test.url());
         final Pipeline pipeline = pipelineProcessor.buildPipeline(test.getPipeline().asSource());
         log.info("== pipeline parsed ==");
         final RunnablePipeline runnablePipeline = pipeline.load();
 
         // Set inputs
         for (final String port : test.getInputs().keySet())
         {
             final List<Source> sources = Lists.newArrayList();
             for (final XdmNode inputDoc : test.getInputs().get(port))
             {
                 sources.add(inputDoc.asSource());
             }
             runnablePipeline.bindSourcePort(port, sources);
         }
 
         // Set options
         for (final QName name : test.getOptions().keySet())
         {
             runnablePipeline.withOption(name, test.getOptions().get(name));
         }
 
         // Set parameters
         // TODO how to deal with multiple parameter ports ?
         for (final String port : test.getParameters().keySet())
         {
             for (final QName name : test.getParameters().get(port).keySet())
             {
                 runnablePipeline.withParam(name, test.getParameters().get(port).get(name));
             }
         }
 
         log.info("== run pipeline ==");
         final PipelineResult result = runnablePipeline.run();
         log.info("== pipeline run ==");
 
         PipelineResult compareResult = null;
         if (test.getComparePipeline() != null)
         {
             log.info("== parse compare pipeline ==");
             final Pipeline comparePipeline = pipelineProcessor.buildPipeline(test.getComparePipeline().asSource());
             log.info("== compare pipeline parsed ==");
             final RunnablePipeline compareRunnablePipeline = comparePipeline.load();
             for (final String port : test.getOutputs().keySet())
             {
                 final List<Source> sources = Lists.newArrayList();
                 for (final XdmNode inputDoc : test.getOutputs().get(port))
                 {
                     sources.add(inputDoc.asSource());
                 }
                 compareRunnablePipeline.bindSourcePort(port, sources);
             }
             for (final QName name : test.getOptions().keySet())
             {
                 compareRunnablePipeline.withOption(name, test.getOptions().get(name));
             }
             for (final String port : test.getParameters().keySet())
             {
                 for (final QName name : test.getParameters().get(port).keySet())
                 {
                     compareRunnablePipeline.withParam(name, test.getParameters().get(port).get(name));
                 }
             }
             compareResult = compareRunnablePipeline.run();
         }
         final PipelineResult resultPipeline = (test.getComparePipeline() != null) ? compareResult : result;
         assert resultPipeline != null;
 
         for (final String port : test.getOutputs().keySet())
         {
             final Iterable<XdmNode> actualNodes = resultPipeline.readNodes(port);
             final Iterable<XdmNode> expectedNodes = test.getOutputs().get(port);
             AssertJUnit.assertEquals(port + " = " + actualNodes.toString(), Iterables.size(expectedNodes),
                     Iterables.size(actualNodes));
 
             final Iterator<XdmNode> expectedNodesIterator = expectedNodes.iterator();
             final Iterator<XdmNode> actualNodesIterator = actualNodes.iterator();
             while (expectedNodesIterator.hasNext())
             {
                 assert actualNodesIterator.hasNext();
                 final XdmNode expectedNode = expectedNodesIterator.next();
                 final XdmNode actualNode = actualNodesIterator.next();
                 TcAssert.compare(expectedNode, actualNode);
             }
             assert !actualNodesIterator.hasNext();
         }
     }
 
     private XProcTestCase getTest(final URL testUrl, final Processor processor, final String testSuite)
     {
         final XProcTestParser parser = new XProcTestParser(processor, testUrl, testSuite);
         parser.parse();
         return parser.getTest();
     }
 
     protected String getTestUrlPrefix()
     {
         final String localPath = System.getProperty(LocalXProcTestsProvider.TEST_DIR_PROPERTY);
         if (localPath != null && !localPath.isEmpty())
         {
             return Uris.asDirectory(new File(localPath).toURI()).toString();
         }
 
         return "http://svn.xproc.org/tests/";
     }
 
     protected void test(final String testName) throws Exception
     {
         final String testUrlString = getTestUrlPrefix() + testName;
         final URL testUrl;
         try
         {
             testUrl = new URL(testUrlString);
         }
         catch (final MalformedURLException e)
         {
             throw new IllegalArgumentException(testUrlString, e);
         }
 
         test(testUrl);
     }
 }
