 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ParseContext.java,v 1.5 2003-06-10 02:51:05 shahid.shah Exp $
  */
 
 package com.netspective.commons.xml;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.Writer;
 import java.io.StringWriter;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.Result;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 import com.netspective.commons.io.Resource;
 import com.netspective.commons.io.FileTracker;
 
 public class ParseContext
 {
     private static final String TRANSFORM_INSTRUCTION = "transform";
     private static SAXParserFactory parserFactory;
     private static CommandLineParser CLPARSER = new PosixParser();
     private static Options TRANSFORM_OPTIONS = new Options();
 
     static
     {
         Option styleSheetOption = OptionBuilder.withLongOpt("xslt")
                 .withArgName("style-sheet")
                 .hasArg()
                 .withValueSeparator(',')
                 .withDescription("XSLT style-sheet file")
                 .create("s");
         Option resourceOption = OptionBuilder.withLongOpt("resource").create("r");
         TRANSFORM_OPTIONS.addOption(styleSheetOption);
         TRANSFORM_OPTIONS.addOption(resourceOption);
     }
 
     private String sourceText;
     private File sourceFile;
     private Resource sourceResource;
     private ZipFile sourceJarFile;
     private ZipEntry sourceJarEntry;
 
     private FileTracker parentFileTracker;
     private FileTracker inputFileTracker;
     private InputSource inputSource;
     private XMLReader parser;
     private Locator locator;
     private boolean throwErrorException;
     private List errors;
     private List warnings;
     private Source[] transformSources;
 
     public ParseContext(ParseContext parentPC, String text) throws ParserConfigurationException, SAXException
     {
         if(parentPC != null)
             setParentFileTracker(parentPC.getInputFileTracker());
         init(createInputSource(text));
     }
 
     public ParseContext(ParseContext parentPC, File srcFile) throws ParserConfigurationException, SAXException, FileNotFoundException
     {
         if(parentPC != null)
             setParentFileTracker(parentPC.getInputFileTracker());
         init(createInputSource(srcFile));
     }
 
     public ParseContext(ParseContext parentPC, Resource resource) throws ParserConfigurationException, SAXException, IOException
     {
         if(parentPC != null)
             setParentFileTracker(parentPC.getInputFileTracker());
         init(createInputSource(resource));
     }
 
     public ParseContext(ParseContext parentPC, File jarFile, ZipEntry jarFileEntry) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException
     {
         if(parentPC != null)
             setParentFileTracker(parentPC.getInputFileTracker());
         init(createInputSource(jarFile, jarFileEntry));
     }
 
     public FileTracker getParentFileTracker()
     {
         return parentFileTracker;
     }
 
     public void setParentFileTracker(FileTracker parentFileTracker)
     {
         this.parentFileTracker = parentFileTracker;
     }
 
     public String getTransformInstruction()
     {
         return TRANSFORM_INSTRUCTION;
     }
 
     public InputSource createInputSource(String text)
     {
         this.sourceText = text;
         InputSource is = new InputSource(new StringReader(text));
         is.setSystemId("text://");
         return is;
     }
 
     public InputSource createInputSource(File srcFile) throws FileNotFoundException
     {
         String uri = "file:" + srcFile.getAbsolutePath().replace('\\', '/');
         for (int index = uri.indexOf('#'); index != -1; index = uri.indexOf('#'))
         {
             uri = uri.substring(0, index) + "%23" + uri.substring(index + 1);
         }
 
         BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));
         InputSource inputSource = new InputSource(inputStream);
         inputSource.setSystemId(uri);
 
         this.inputFileTracker = new FileTracker();
         this.inputFileTracker.setFile(srcFile);
         if(parentFileTracker != null)
             this.inputFileTracker.setParent(parentFileTracker);
         this.sourceFile = srcFile;
 
         return inputSource;
     }
 
     public InputSource createInputSource(Resource resource) throws IOException
     {
         this.sourceResource = resource;
 
         InputStream stream = resource.getResourceAsStream();
         if (stream != null)
         {
             InputSource inputSource = new InputSource(stream);
             inputSource.setSystemId(resource.getSystemId());
             return inputSource;
         }
         else
             throw new IOException("Resource '" + resource.getSystemId() + "' not found. ClassPath is "+ System.getProperty("java.class.path") +".");
     }
 
     public InputSource createInputSource(File jarFile, ZipEntry jarFileEntry) throws FileNotFoundException, IOException
     {
         this.sourceFile = jarFile;
         this.sourceJarEntry = jarFileEntry;
         this.sourceJarFile = new ZipFile(jarFile);
 
         InputStream stream = sourceJarFile.getInputStream(jarFileEntry);
         if (stream != null)
         {
             InputSource inputSource = new InputSource(stream);
             inputSource.setSystemId(sourceJarFile.getName() + "!" + jarFileEntry.getName());
 
             this.inputFileTracker = new FileTracker();
             this.inputFileTracker.setFile(jarFile);
             if(parentFileTracker != null)
                 this.inputFileTracker.setParent(parentFileTracker);
 
             return inputSource;
         }
         else
             throw new FileNotFoundException("Zip entry '" + jarFileEntry.getName() + "' not found in zip file '"+ jarFile.getAbsolutePath() +"'.");
     }
 
     public void init(InputSource inputSource) throws ParserConfigurationException, SAXException
     {
         this.inputSource = inputSource;
         this.errors = new ArrayList();
         this.warnings = new ArrayList();
 
         if(inputSource.getSystemId() == null)
             throw new ParserConfigurationException("Please set the system id.");
 
         SAXParser saxParser = getParserFactory().newSAXParser();
         parser = saxParser.getXMLReader();
     }
 
     public boolean prepareTransformInstruction(String instructionParams)
     {
         CommandLine cmd = null;
         try
         {
             cmd = CLPARSER.parse(TRANSFORM_OPTIONS, StringUtils.split(instructionParams));
         }
         catch (ParseException e)
         {
             errors.add("Unable to process transformation command <?" + TRANSFORM_INSTRUCTION + " " + instructionParams + "?>: " + e.toString());
             return false;
         }
 
         if (!cmd.hasOption('s'))
         {
             errors.add("No style-sheet options specified for <?" + TRANSFORM_INSTRUCTION + " " + instructionParams + "?> PI.");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(TRANSFORM_INSTRUCTION, TRANSFORM_OPTIONS);
             return false;
         }
 
         boolean isResource = cmd.hasOption("r");
         List sources = new ArrayList();
         String[] styleSheets = cmd.getOptionValues('s');
         for (int i = 0; i < styleSheets.length; i++)
         {
             if(isResource)
             {
                 InputStream stream = getClass().getClassLoader().getResourceAsStream(styleSheets[i].trim());
                 if(stream == null)
                     errors.add("Error in <?" + TRANSFORM_INSTRUCTION + " " + instructionParams + "?>, stylesheet '" + styleSheets[i].trim() + "' not found as a resource in ClassLoader " + getClass().getClassLoader());
                 else
                 {
                     sources.add(new StreamSource(stream));
                 }
             }
             else
             {
                 File sourceFile = resolveFile(styleSheets[i].trim());
                 try
                 {
                     if(inputFileTracker != null)
                     {
                         FileTracker preProcessor = new FileTracker();
                         preProcessor.setFile(sourceFile);
                         inputFileTracker.addPreProcessor(preProcessor);
                     }
                     sources.add(new StreamSource(new FileInputStream(sourceFile)));
                 }
                 catch (FileNotFoundException e)
                 {
                     errors.add("Error in <?" + TRANSFORM_INSTRUCTION + " " + instructionParams + "?>, stylesheet '" + sourceFile.getAbsolutePath() + "': " + e.toString());
                     continue;
                 }
             }
         }
 
         if (sources.size() == 0)
             return false;
 
         transformSources = (Source[]) sources.toArray(new Source[sources.size()]);
         return true;
     }
 
     public InputSource recreateInputSource() throws FileNotFoundException, IOException
     {
         if(sourceFile != null && sourceJarEntry == null)
             return createInputSource(sourceFile);
 
         if(sourceText != null)
             return createInputSource(sourceText);
 
         if(sourceResource != null)
             return createInputSource(sourceResource);
 
         if(sourceFile != null && sourceJarEntry != null)
             return createInputSource(sourceFile, sourceJarEntry);
 
         return null;
     }
 
     public void closeInputSource() throws IOException
     {
         if (inputSource.getCharacterStream() != null)
         {
             try
             {
                 inputSource.getCharacterStream().close();
             }
             catch (IOException ioe)
             {
                 // ignore this
             }
         }
         if (inputSource.getByteStream() != null)
         {
             try
             {
                 inputSource.getByteStream().close();
             }
             catch (IOException ioe)
             {
                 // ignore this
             }
         }
 
         if(sourceJarFile != null)
             sourceJarFile.close();
     }
 
     public void doExternalTransformations() throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException
     {
         // re-create the input source because the original stream is already closed
         InputSource inputSource = recreateInputSource();
 
         Source activeSource = inputSource.getByteStream() != null ?
                     new StreamSource(inputSource.getByteStream()) :
                     new StreamSource(inputSource.getCharacterStream());
         activeSource.setSystemId(inputSource.getSystemId());
 
         Writer activeResultBuffer = new StringWriter();
         Result activeResult = new StreamResult(activeResultBuffer);
         activeResult.setSystemId(activeResultBuffer.getClass().getName());
 
         TransformerFactory factory = TransformerFactory.newInstance();
         int lastTransformer = transformSources.length - 1;
         for (int i = 0; i <= lastTransformer; i++)
         {
             Transformer transformer = factory.newTransformer(transformSources[i]);
             transformer.transform(activeSource, activeResult);
 
             if (i < lastTransformer)
             {
                 activeSource = new StreamSource(new StringReader(activeResultBuffer.toString()));
                 activeResultBuffer = new StringWriter();
                 activeResult = new StreamResult(activeResultBuffer);
             }
         }
 
         // now that all the transformations have been performed, we want to reset our input source to the final
         // transformation
         init(createInputSource(activeResultBuffer.toString()));
     }
 
     private static SAXParserFactory getParserFactory()
     {
         if (parserFactory == null)
             parserFactory = SAXParserFactory.newInstance();
 
         return parserFactory;
     }
 
     public File resolveFile(String src)
     {
        File file = new File(src);
        if(file.isAbsolute())
            return file;
        else
            return inputFileTracker != null ? new File(inputFileTracker.getFile().getParent(), src) : file;
     }
 
     public void setThrowErrorException(boolean throwErrorException)
     {
         this.throwErrorException = throwErrorException;
     }
 
     public FileTracker getInputFileTracker()
     {
         return inputFileTracker;
     }
 
     public InputSource getInputSource()
     {
         return inputSource;
     }
 
     public XMLReader getParser()
     {
         return parser;
     }
 
     public Locator getLocator()
     {
         return locator;
     }
 
     public void setLocator(Locator locator)
     {
         this.locator = locator;
     }
 
     public boolean isThrowErrorException()
     {
         return throwErrorException;
     }
 
     public void addError(Throwable t)
     {
         errors.add(t);
     }
 
     public void addError(String message)
     {
         this.errors.add(message);
     }
 
     public void addWarning(String message)
     {
         this.warnings.add(message);
     }
 
     public void addErrors(List errors)
     {
         this.errors.addAll(errors);
     }
 
     public List getErrors()
     {
         return errors;
     }
 
     public List getWarnings()
     {
         return warnings;
     }
 }
