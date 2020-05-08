 /*
 IEC - Copyright (c) 2012 Hendrik Iben - hendrik [dot] iben <at> googlemail [dot] com
 Inkscape Export Cleaner - get's rid of export definitions in Inkscape documents
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
  */
 package iec;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.regex.Pattern;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.Namespace;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class IEC {
 	public static final String idAttrName = "id";
 
 	public static final String inkscapeNS = "inkscape";
 	public static final String inkscapeExportFilenameAttrName = "export-filename";
 	public static final String inkscapeExportXDPIAttrName = "export-xdpi";
 	public static final String inkscapeExportYDPIAttrName = "export-ydpi";
 
 	public static final String defaultExportItemFormat = "id: '%i', export: \"%f\" @ %x DPI (x) %y DPI (y) (element: '%t')";
 	
 	public static final String matchFormatId = " (matches-id: %s)";
 	public static final String matchFormatFile = " (matches-file: %s)";
 	public static final String matchFormatYes = "yes";
 	public static final String matchFormatNo = "no";
 
 	public static final String noIDString = "(no id)";
 	public static final String noFilenameString = "(no filename set)";
 	public static final String noTagString = "(no tag)";
 	public static final String noXDPIString = "(undefined)";
 	public static final String noYDPIString = noXDPIString;
 	
 	public static final String xmlMinimumAttrIndention = "   ";
 	public static final String xmlIndentionStep = "  ";
 
 	public static class ExportItem {
 		public String tag;
 		public String id;
 		public String filename;
 		public String xdpi;
 		public String ydpi;
 
 		public ExportItem(String tag, String id, String filename, String xdpi,
 				String ydpi) {
 			this.tag = tag;
 			this.id = id;
 			this.filename = filename;
 			this.xdpi = xdpi;
 			this.ydpi = ydpi;
 		}
 	}
 
 	public static String formatExportItem(String fmt, ExportItem ei) {
 		if (fmt == null)
 			fmt = defaultExportItemFormat;
 
 		fmt = fmt.replace("%i", ei.id == null ? noIDString : ei.id);
 		fmt = fmt.replace("%f", ei.filename == null ? noFilenameString
 				: ei.filename);
 		fmt = fmt.replace("%x", ei.xdpi == null ? noXDPIString : ei.xdpi);
 		fmt = fmt.replace("%y", ei.ydpi == null ? noYDPIString : ei.ydpi);
 		fmt = fmt.replace("%t", ei.tag == null ? noTagString : ei.tag);
 
 		return fmt;
 	}
 
 	public static String avalue(Attr a) {
 		return a == null ? null : a.getValue();
 	}
 	
 	public static interface ItemKeepMatch {
 		final int KEEP_MATCH_NONE = 0;
 		final int KEEP_MATCH_ID = 1;
 		final int KEEP_MATCH_FILENAME = 2;
 		
 		public int keepResult(ExportItem ei);
 		
 		public boolean checksId();
 		public boolean checksFilename();
 	}
 	
 	public static class KeepByPattern implements ItemKeepMatch {
 
 		private List<Pattern> keepIds = null;
 		private List<Pattern> keepFiles = null;
 		
 		public KeepByPattern(List<Pattern> keepIds, List<Pattern> keepFiles) {
 			this.keepIds = keepIds;
 			this.keepFiles = keepFiles;
 		}
 		
 		public boolean checksId() {
			return keepIds!=null || keepIds.size()>0;
 		}
 		
 		public boolean checksFilename() {
			return keepFiles!=null || keepFiles.size()>0;
 		}
 		
 		public int keepResult(ExportItem ei) {
 			int keep_match = KEEP_MATCH_NONE;
 			
 			if(ei.id!=null && keepIds!=null && keepIds.size()>0) {
 				if(matchesPatternList(keepIds, ei.id)) {
 					keep_match |= KEEP_MATCH_ID;
 				}
 			}
 			
 			if(ei.filename!=null && keepFiles!=null && keepFiles.size()>0) {
 				if(matchesPatternList(keepFiles, ei.filename)) {
 					keep_match |= KEEP_MATCH_FILENAME;
 				}
 			}
 			
 			return keep_match;
 		}
 	}
 
 	public static List<ExportItem> listExports(OutputStream os, Element svg, String format, ItemKeepMatch ikm, List<ExportItem> listOutput) {
 		String inkscapeNSURI = svg.getAttributeNS(
 				XMLConstants.XMLNS_ATTRIBUTE_NS_URI, inkscapeNS);
 
 		Queue<Element> elements = new LinkedList<Element>();
 		elements.add(svg);
 		
 		PrintStream ps = null;
 		if(os!=null) {
 			if(os instanceof PrintStream) {
 				ps = (PrintStream)os;
 			} else {
 				ps = new PrintStream(os);
 			}
 		}
 		
 		boolean checksId = ikm != null ? ikm.checksId() : false;
 		boolean checksFilename = ikm != null ? ikm.checksFilename() : false;
 
 		while (!elements.isEmpty()) {
 			Element e = elements.remove();
 			NodeList children = e.getChildNodes();
 			for (int i = 0; i < children.getLength(); i++) {
 				Node n = children.item(i);
 				if (n instanceof Element) {
 					elements.add((Element) n);
 				}
 			}
 			Attr exportFileAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportFilenameAttrName);
 			Attr exportXDPIAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportXDPIAttrName);
 			Attr exportYDPIAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportYDPIAttrName);
 			if (exportFileAttr != null || exportXDPIAttr != null
 					|| exportYDPIAttr != null) {
 				Attr idAttr = e.getAttributeNodeNS(null, idAttrName);
 				ExportItem ei = new ExportItem(e.getTagName(), avalue(idAttr),
 						avalue(exportFileAttr), avalue(exportXDPIAttr),
 						avalue(exportYDPIAttr));
 				
 				if(listOutput!=null)
 					listOutput.add(ei);
 				
 				if(ps!=null) {
 					ps.print(formatExportItem(format, ei));
 					
 					if(ikm!=null) {
 						int res = ikm.keepResult(ei);
 					
 						if(checksId && ei.id != null) {
 							ps.format(matchFormatId, ((res & ItemKeepMatch.KEEP_MATCH_ID)!=0) ? matchFormatYes : matchFormatNo);
 						}
 						if(checksFilename && ei.filename != null) {
 							ps.format(matchFormatFile, ((res & ItemKeepMatch.KEEP_MATCH_FILENAME)!=0) ? matchFormatYes : matchFormatNo);
 						}
 					}
 
 					ps.println();
 				}
 			}
 		}
 		
 		return listOutput;
 	}
 	
 	public static String attrVal(Attr a, String def) {
 		if(a==null)
 			return def;
 		return a.getValue();
 	}
 
 	public static void pruneExports(Element svg, ItemKeepMatch ikm) {
 		String inkscapeNSURI = svg.getAttributeNS(
 				XMLConstants.XMLNS_ATTRIBUTE_NS_URI, inkscapeNS);
 
 		Queue<Element> elements = new LinkedList<Element>();
 		elements.add(svg);
 
 		while (!elements.isEmpty()) {
 			Element e = elements.remove();
 			NodeList children = e.getChildNodes();
 			for (int i = 0; i < children.getLength(); i++) {
 				Node n = children.item(i);
 				if (n instanceof Element) {
 					elements.add((Element) n);
 				}
 			}
 			Attr exportFileAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportFilenameAttrName);
 			Attr exportXDPIAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportXDPIAttrName);
 			Attr exportYDPIAttr = e.getAttributeNodeNS(inkscapeNSURI,
 					inkscapeExportYDPIAttrName);
 			
 			boolean keep = false;
 			
 			if(ikm!=null) {
 				Attr idAttr = e.getAttributeNodeNS(null, idAttrName);
 				
 				ExportItem ei = new ExportItem(e.getTagName(), attrVal(idAttr, null), attrVal(exportFileAttr, null), attrVal(exportXDPIAttr, null), attrVal(exportYDPIAttr, null));
 				
 				keep = ikm.keepResult(ei) != ItemKeepMatch.KEEP_MATCH_NONE;
 			}
 
 			if(!keep) {
 				if (exportFileAttr != null)
 					e.removeAttributeNode(exportFileAttr);
 				if (exportXDPIAttr != null)
 					e.removeAttributeNode(exportXDPIAttr);
 				if (exportYDPIAttr != null)
 					e.removeAttributeNode(exportYDPIAttr);
 			}
 		}
 	}
 	
 	public static void writeNiceXML(InputStream is, OutputStream os) throws XMLStreamException, FactoryConfigurationError {
 		XMLEventReader xmler = XMLInputFactory.newInstance()
 				.createXMLEventReader(is);
 
 		XMLEventWriter xmlev = XMLOutputFactory.newInstance()
 				.createXMLEventWriter(os);
 
 		XMLEventFactory xf = XMLEventFactory.newFactory();
 		LinkedList<Attribute> newal = new LinkedList<Attribute>();
 		LinkedList<Namespace> newnl = new LinkedList<Namespace>();
 
 		int indent = 0;
 		boolean hadElement = false;
 		
 		while(xmler.hasNext()) {
 			XMLEvent e = xmler.nextEvent();
 			if(e.isStartElement()) {
 				hadElement = true;
 				String indention = xmlMinimumAttrIndention;
 				for(int i=0; i<indent; i++) {
 					indention += xmlIndentionStep;
 				}
 				indent++;
 				
 				newnl.clear();
 				newal.clear();
 
 				StartElement se = (StartElement)e;
 				Iterator<?> ni = se.getNamespaces();
 				
 				// Ugly hack...
 				// the API does not allow to change the prefix
 				// of a Namespace element
 				// so they are rewritten as attributes...
 				while(ni.hasNext()) {
 					Object nio = ni.next();
 					if(nio instanceof Namespace) {
 						Namespace n = (Namespace)nio;
 						QName nname = n.getName();
 						boolean noPrefix = nname.getPrefix().length()==0;
 						String lname = noPrefix ? "\n" + indention +nname.getLocalPart() : nname.getLocalPart();
 						String lp = noPrefix ? nname.getPrefix() : "\n" + indention + nname.getPrefix();
 						QName newname = new QName(XMLConstants.DEFAULT_NS_PREFIX, lname.length()==0 ? lp : lname, lname.length()==0 ? "" : lp);
 
 						Attribute newnsa = xf.createAttribute(newname, n.getNamespaceURI());
 						newal.add(newnsa);
 					}
 				}
 				
 				Iterator<?> ai = se.getAttributes();
 				while(ai.hasNext()) {
 					Object aio = ai.next();
 					if(aio instanceof Attribute) {
 						Attribute a = (Attribute)aio;
 						QName aname = a.getName();
 						boolean noPrefix = aname.getPrefix().length()==0;
 						String lname = noPrefix ? "\n" + indention +aname.getLocalPart() : aname.getLocalPart();
 						String lp = noPrefix ? aname.getPrefix() : "\n" + indention + aname.getPrefix();
 						QName newname = new QName(aname.getNamespaceURI(), lname, lp);
 						Attribute newa = xf.createAttribute(newname, a.getValue());
 						newal.add(newa);
 					}
 				}
 				QName sename = se.getName();
 				StartElement newse = xf.createStartElement(sename.getPrefix(), sename.getNamespaceURI(), sename.getLocalPart(), newal.iterator(), null, se.getNamespaceContext());
 				xmlev.add(newse);
 			} else {
 				if(!hadElement && e.getEventType() == XMLEvent.COMMENT) {
 					xmlev.add(xf.createCharacters("\n"));
 					xmlev.add(e);
 					xmlev.add(xf.createCharacters("\n"));
 				} else {
 					if(e.isEndElement()) {
 						indent--;
 						if(indent < 0)
 							indent = 0;
 					}
 					xmlev.add(e);
 				}
 			}
 		}
 
 		xmlev.flush();
 		xmlev.close();
 	}
 
 	public static final String flagList = "list";
 	public static final String flagListS = "l";
 	
 	public static final String flagPrune = "prune";
 	public static final String flagPruneS = "p";
 
 	public static final String flagHelp = "help";
 	public static final String flagHelpS= "h";
 
 	public static final String flagGUI = "gui";
 	public static final String flagGUIS= "g";
 	
 	public static final String optFormat = "format";
 	public static final String optFormatS = "m";
 	
 	public static final String optKeepId = "keep";
 	public static final String optKeepIdS = "k";
 
 	public static final String optKeepFile = "keep-file";
 	public static final String optKeepFileS = "f";
 
 
 	public static final String optKeepIdR = "keep-regex";
 	public static final String optKeepIdRS = "K";
 
 	public static final String optKeepFileR = "keep-file-regex";
 	public static final String optKeepFileRS = "F";
 
 	public static final String optInput = "input";
 	public static final String optInputS = "i";
 	
 	public static final String optOutput = "output";
 	public static final String optOutputS = "o";
 	
 	public static boolean isOpt(String s, boolean wasShort, String lo, String so) {
 		if(wasShort) {
 			if(so == null)
 				return false;
 			return so.equals(s);
 		}
 		if(lo == null)
 			return false;
 		return lo.equals(s);
 	}
 	
 	public static void help() {
 		System.out.println("InkscapeExportCleaner");
 		System.out.println(
 				"Options:\n" +
 				"  --help|-h                        this text\n" +
 				"  --list|-l                        list defined exports (default for single file)\n" +
 				"  --prune|-p                       prune exports (default for two files)\n" +
 				"  --input|-i <file>                input file ('-' for standard in)\n" +
 				"  --output|-o <file>               output file ('-' for standard out)\n" +
 				"  --keep|-k <idglob>               add element id to preserve export def\n" +
 				"  --keep-regex|-K <idreg>          add regular-expression for id\n" +
 				"  --keep-file|-f <fileglob>        add filename to preserve export def\n" +
 				"  --keep-file-regex|-F <filereg>   add regular-expression for file\n" +
 				"  --format <fmt>                   set list format\n" +
 				"  --gui|-g [<filename>]            start graphical interface with optional file\n" +
 				"\n" +
 				"Input and output can also be set by just giving the files in the right order.\n" +
 				"\n" +
 				"Examples:\n" +
 				"  IEC full.svg clean.svg                        save full.svg to clean.svg and remove all exports\n" +
 				"  IEC --keep \"icon-*\" full.svg clean.svg        remove exports where the element id does not start with 'icon-'\n" +
 				"  IEC --keep-file full.png full.svg clean.svg   keep only when file is set to 'full.png'\n" +
 				"  IEC -K ^final -K ^test full.svg clean.svg     keep exports for ids starting with 'final' and 'test'\n" +
 				"  IEC -K ^final -f \"*.png\" clean.svg            list exports and show matches for ids and files\n" +
 				"\n" +
 				"Format-String:\n" +
 				"  %i: <id> %f: <file> %x: DPI (x) %y: DPI (y) %t: element type (g, path, ...)\n" +
 				"  default: " + defaultExportItemFormat
 				);
 		
 		System.exit(0);
 	}
 	
 	public static String getArg(Iterator<String> i, String s, String optArg) {
 		if(optArg!=null)
 			return optArg;
 		
 		if(!i.hasNext()) {
 			System.err.println("Error! Argument needed for '" + s + "'");
 			System.exit(1);
 		}
 		return i.next();
 	}
 	
 	public static String globToRegex(String glob) {
 		return glob.replace(".", "\\.").replace("*", ".*").replace("?", ".");
 	}
 	
 	public static Pattern globToPattern(String glob) {
 		Pattern p = Pattern.compile(globToRegex(glob));
 		System.out.println(p);
 		return Pattern.compile(globToRegex(glob));
 	}
 	
 	public static boolean matchesPatternList(List<Pattern> l, String s) {
 		for(Pattern p : l) {
 			if(p.matcher(s).matches())
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public static void main(String... args) {
 		
 		String infile = null;
 		String outfile = null;
 		
 		boolean doList = false;
 		boolean doPrune = false;
 		
 		List<Pattern> keepIds = new LinkedList<Pattern>();
 		List<Pattern> keepFiles = new LinkedList<Pattern>();
 		
 		String listFormat = defaultExportItemFormat;
 		
 		Iterator<String> argi = Arrays.asList(args).iterator();
 		
 		while(argi.hasNext()) {
 			String s = argi.next();
 			String opt = null;
 			boolean shortopt = false;
 			if(s.length()>2 && s.startsWith("--")) {
 				opt = s.substring(2);
 			} else {
 				if(s.length()>1 && s.startsWith("-")) {
 					opt = s.substring(1);
 					shortopt = true;
 				}
 			}
 			if(opt!=null) {
 				String arg = null;
 				
 				int idx;
 				if( (idx = opt.indexOf('=')) != -1 ) {
 					arg = opt.substring(idx+1);
 					if(arg.length()==0)
 						arg = null;
 					opt = opt.substring(0, idx);
 				}
 				
 				if(isOpt(opt, shortopt, flagHelp, flagHelpS)) {
 					help();
 					continue;
 				}
 				if(isOpt(opt, shortopt, flagGUI, flagGUIS)) {
 					ArrayList<String> remArgs = new ArrayList<String>();
 					while(argi.hasNext()) {
 						remArgs.add(argi.next());
 					}
 					IECUI.main(remArgs.toArray(new String [remArgs.size()]));
 					return;
 				}
 				if(isOpt(opt, shortopt, flagList, flagListS)) {
 					doList = true;
 					continue;
 				}
 				if(isOpt(opt, shortopt, flagPrune, flagPruneS)) {
 					doPrune = true;
 					continue;
 				}
 				if(isOpt(opt, shortopt, optInput, optInputS)) {
 					infile = getArg(argi, s, arg);
 					continue;
 				}
 				if(isOpt(opt, shortopt, optOutput, optOutputS)) {
 					outfile = getArg(argi, s, arg);
 					continue;
 				}
 				if(isOpt(opt, shortopt, optFormat, optFormatS)) {
 					listFormat = getArg(argi, s, arg);
 					continue;
 				}
 				if(isOpt(opt, shortopt, optKeepId, optKeepIdS)) {
 					keepIds.add(globToPattern(getArg(argi, s, arg)));
 					continue;
 				}
 				if(isOpt(opt, shortopt, optKeepFile, optKeepFileS)) {
 					keepFiles.add(globToPattern(getArg(argi, s, arg)));
 					continue;
 				}
 				if(isOpt(opt, shortopt, optKeepIdR, optKeepIdRS)) {
 					keepIds.add(Pattern.compile(getArg(argi, s, arg)));
 					continue;
 				}
 				if(isOpt(opt, shortopt, optKeepFileR, optKeepFileRS)) {
 					keepFiles.add(Pattern.compile(getArg(argi, s, arg)));
 					continue;
 				}
 
 				System.err.println("Unknown parameter: " + s);
 				System.exit(1);
 			} else {
 				if(infile == null) {
 					infile = s;
 					continue;
 				}
 				if(outfile == null) {
 					outfile = s;
 					continue;
 				} 
 				System.err.println("Unexpected parameter: " + s);
 				System.exit(1);
 			}
 		}
 
 		if(infile == null && outfile == null) {
 			help();
 		}
 		
 		if(!(doList || doPrune)) {
 			if(outfile != null) {
 				doPrune = true;
 			} else {
 				doList = true;
 			}
 		}
 		
 		if(infile == null) {
 			infile = "-";
 		}
 
 		if(doPrune && outfile == null) {
 			outfile = "-";
 		}
 		
 		if(doList && outfile != null) {
 			System.err.println("Error! Output file can not be used with list!");
 			System.exit(1);
 		}
 		
 		try {
 			InputStream is = "-".equals(infile) ? System.in : new FileInputStream(infile);
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			dbf.setValidating(false);
 			dbf.setNamespaceAware(true);
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(is);
 			Element base = doc.getDocumentElement();
 			
 			if(doList) {
 				System.out.println("List of exports:");
 				listExports(System.out, base, listFormat, new KeepByPattern(keepIds, keepFiles), null);
 			}
 
 			if(doPrune) {
 				pruneExports(base, new KeepByPattern(keepIds, keepFiles));
 
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
 				Transformer t = TransformerFactory.newInstance().newTransformer();
 				StreamResult sr = new StreamResult(baos);
 				t.transform(new DOMSource(doc), sr);
 
 				ByteArrayInputStream bis = new ByteArrayInputStream(
 						baos.toByteArray());
 				
 				baos.reset();
 
 				// write xml to memory first to not overwrite output on xml error
 				writeNiceXML(bis, baos);
 				
 				// real write
 				("-".equals(outfile) ? System.out : new FileOutputStream(outfile)).write(baos.toByteArray());
 			}
 
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 			System.exit(1);
 		}
 	}
 }
