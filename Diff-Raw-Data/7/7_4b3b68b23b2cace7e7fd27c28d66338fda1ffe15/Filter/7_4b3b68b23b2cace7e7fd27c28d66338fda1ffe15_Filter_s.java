 // Copyright (c) 2002 Mark Miller for the XMLResume Project 
 // All rights reserved.
 //
 // <http://xmlresume.sourceforge.net>
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are
 // met:
 // 
 // 1. Redistributions of source code must retain the above copyright
 //    notice, this list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the
 //    distribution.
 // 
 // THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 // IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 // BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 // CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 // SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 // BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 // IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 package net.sourceforge.xmlresume.filter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Vector;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class Filter { 
 
     /**
      * Run the filter and output the data to an intermediate file.
      * @param argv Command-line arguments.
      * @throws Throwable if an error occurs.
      */
 
     public static void main(String[] argv) throws Throwable 
     {
 	int debugLevel=9;
 	int i=0;
 	int nCats;
 	Vector categoryList = new Vector();
 	File in = null;
 	File outfile = null;
 	PrintStream out = System.out;
 	CategoryFilter filter;
 	FileWriterHandler writer;
 	SAXParser parser;
 
 	//process the commandline
 	while (i < argv.length) {
 	    if ("-v".equals(argv[i]) || "--verbose".equals(argv[i])) {
 		debugLevel = 0;
 		i++;
 	    } else if ("-in".equals(argv[i])) {
 		in = new File(argv[i+1]);
 		i += 2;
 	    } else if ("-out".equals(argv[i])) {
 		outfile = new File(argv[i+1]);
 		outfile.createNewFile();
 		if (!outfile.canWrite()) {
 		    System.err.println("Error: can't open file " + outfile + " for writing.");
 		    System.exit(1);
 		}
 		i += 2;
 		out = new PrintStream(
                    new FileOutputStream(outfile,
                        false, // auto-flush data?
                        "UTF-8" // character set
                    )
                 );
 	    } else {
 		categoryList.addElement(argv[i]);
 		i++;
 	    }
 	}
 
 	if (in == null || argv.length < 2 || "-h".equals(argv[0]) || "--help".equals(argv[0])) {
 	    System.err.println("Filter -- preprocess an XMLResume to select for elements in a given category\n" + 
 			       "Usage: java Filter [-v|--verbose] -in <in_file> [-out <out_file>] [category1 [category2 [...]]\n" + 
 			       "If -out <out_file> is not specified, output will be printed on STDOUT.");
 	    System.exit(1);
 	} else if (!in.canRead()) {
 	    System.err.println("Error: can't open file " + in + " for reading.");
 	    System.exit(1);
 	} else {
 	    parser = SAXParserFactory.newInstance().newSAXParser();
 	    filter = new CategoryFilter(parser.getXMLReader(), categoryList.iterator(), debugLevel);
 	    writer = new FileWriterHandler(out, debugLevel);
 	    filter.parse(in, writer);
 	    System.exit(0);
 	}
     }
 }
 
