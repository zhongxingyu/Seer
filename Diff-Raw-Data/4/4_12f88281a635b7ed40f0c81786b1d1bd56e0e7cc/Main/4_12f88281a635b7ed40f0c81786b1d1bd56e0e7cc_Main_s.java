 // Copyright (c) 2002 Mark Miller for the XMLResume Project
 // All rights reserved.
 //
 // Licensing information is available at
 // http://xmlresume.sourceforge.net/license/index.html
 
 package net.sourceforge.xmlresume.filter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.XMLFilterImpl; 
 
 /**
  * <p>Sets up a chain of filters to filter a resume.xml file.
  * Currently, the chain goes like this:</p>
  * 
  * <pre>
  * (source)		(allow)		(deny)		(deny)	   (write to file/STDOUT)
  * XMLFilterImpl , TargetFilter , ElementFilter , AttributeFilter,    FileWriterHandler
  * </pre>
  * 
  */
 
 public class Main { 
 
     /**
      * Run the filter and output the data to an intermediate file.
      * @param argv Command-line arguments.
      * @throws Throwable if an error occurs.
      */
 
     public static void main(String[] argv) throws Throwable 
     {
 	int debugLevel=9;
 	Vector targetIncludes = null, 
 	       elementExcludes = null, 
 	       attributeExcludes = null;
 	File infile = null,
 	     outfile = null;
 	StringTokenizer st;
 	PrintStream out = System.out;
 	FileWriterHandler writer;
 	XMLResumeFilter targetFilter, elementFilter, attributeFilter;
 
 	//process the commandline
 	int i = 0;
 	while (i < argv.length) {
 	    if ("-v".equals(argv[i]) || "--verbose".equals(argv[i])) {
 		debugLevel = 0;
 		i++;
 	    } else if ("-in".equals(argv[i])) {
 		infile = new File(argv[i+1]);
 	        if (!infile.canRead())
 		    throw new Error("Error: can't open file " + infile + " for reading.");
 		i += 2;
 	    } else if ("-out".equals(argv[i])) {
 		outfile = new File(argv[i+1]);
 		outfile.createNewFile();
 		if (!outfile.canWrite()) {
 		    throw new Error("Error: can't open file " + outfile + " for writing.");
 		}
 		i += 2;
 		out = new PrintStream(
                     new FileOutputStream(outfile),
                     false // auto-flush data?
                 );
 	    } else if ("-includeTargets".equals(argv[i])) {
 		targetIncludes = new Vector();
 		st = new StringTokenizer(argv[i+1], " \t\n\r\f,");
 		while (st.hasMoreTokens()) {
 		    targetIncludes.addElement(st.nextToken());
 		}
 		i += 2;
 	    } else if ("-excludeElements".equals(argv[i])) {
 		elementExcludes = new Vector();
 		st = new StringTokenizer(argv[i+1], " \t\n\r\f,");
 		while (st.hasMoreTokens()) {
 		    elementExcludes.addElement(st.nextToken());
 		}
 		i += 2;
 	    } else if ("-excludeAttributes".equals(argv[i])) {
 		attributeExcludes = new Vector();
 		st = new StringTokenizer(argv[i+1], " \t\n\r\f,");
 		while (st.hasMoreTokens()) {
 		    attributeExcludes.addElement(st.nextToken());
 		}
 		i += 2;
 	    } else {
 		usage("Error: invalid option: " + argv[i]);	
 	    }
 	}
 
 	if (infile == null || argv.length < 2 || "-h".equals(argv[0]) || "--help".equals(argv[0])) {
 	     usage();
 	} else {
 
 	    // Define the filtering chain, using XMLResumeFilters as placeholders when actual filters are unnecessary
 	    if (targetIncludes == null ) {
 		targetFilter = new XMLResumeFilter(SAXParserFactory.newInstance().newSAXParser().getXMLReader(), debugLevel);
 	    } else {
 		targetFilter = new TargetFilter(SAXParserFactory.newInstance().newSAXParser().getXMLReader(), targetIncludes.iterator(), debugLevel);
 	    }
 	    if (elementExcludes == null ) {
 		elementFilter = new XMLResumeFilter(targetFilter, debugLevel);
 	    } else {
 		elementFilter = new ElementFilter(targetFilter, elementExcludes.iterator(), debugLevel);
 	    }
 	    if (attributeExcludes == null ) {
 		attributeFilter = new XMLResumeFilter(elementFilter, debugLevel);
 	    } else {
 		attributeFilter = new AttributeFilter(elementFilter, attributeExcludes.iterator(), debugLevel);
 	    } try {
 		writer = new FileWriterHandler(attributeFilter, out, debugLevel, "UTF-8");
 	    } catch (UnsupportedEncodingException e) { throw new Error("Your platform does not support UTF-8 encoding, which is required for using the Filter\n"); }
 	
 	    // Turn the input file into an InputSource
             String uri = "file:" + infile.getAbsolutePath();
             if (File.separatorChar == '\\') {
                 uri = uri.replace('\\', '/');
             }
             InputSource input = new InputSource(uri);
 
 	    targetFilter.getParent().parse(input);
 	}
     }
 
     /**
      * Print out usage information and exit with an error.
      * @param err An error string.
      */
     private static void usage(String err) { 
	throw new Error(err + "\n\n" +
 			"Filter -- preprocess an XMLResume to include/exclude elements and attributes\n" + 
 			"Usage: java Main [-v|--verbose] -in <in_file> [-out <out_file>]\n" + 
 			"\t [-includeTargets \"[target1 [target2 [...]]]\"\n" + 
 			"\t [-excludeElements \"[elementName1 [elementName2 [...]]]\"\n" + 
 			"\t [-excludeAttributes \"[attributeName1 [attributeName2 [...]]]\"\n" + 
 			"If -out <out_file> is not specified, output will be printed on STDOUT.");
     }
 
     /**
      * Print out usage information and exit with an error.
      */
     private static void usage() {
 	usage(""); 
     }
 
 }
 
