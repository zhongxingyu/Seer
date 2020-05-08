 package edu.knowitall.tac2013.preprocess.tac;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Reads the raw XML document format from the KBP 2013 corpus and produces
  * flattened output suitable for input to later preprocessing steps.
  * 
  * @author rbart
  * 
  */
 public class KbpCorpusParser {
 
 	private static String offsetPlaceHolder = "\t0\t0";
 
 	public static void main(String[] args) throws FileNotFoundException {
 
 		String usage = 
 				"Usage: KbpCorpusParser <inputfile> <outputfile> [--news, --web, --forum]\n" +
 			    "Set inputfile=\"stdin\" to read from standard input.\n" +
 				"Set outputfile=\"stdout\" to write to standard output.";
 		
 		if (args.length != 3) {
 			System.out.println(usage);
 			return;
 		}
 		
 		String inputFile = args[0];
 		String outputFile = args[1];
 		String corpusType = args[2];
 
 		InputStream inputStream;
 		if (inputFile.equals("stdin")) inputStream = System.in;
 		else inputStream = new FileInputStream(inputFile);
 		
 		PrintStream outputStream;
 		if (outputFile.equals("stdout")) outputStream = System.out;
 		else outputStream = new PrintStream(outputFile);
 		
 		boolean web = corpusType.equals("--web");
 		boolean news = corpusType.equals("--news");
 		boolean forum = corpusType.equals("--forum");
 		
 		if (!web && !news && !forum) {
 			System.out.println(usage);
 			return;
 		}
 		
 		// Actual processing begins here.
 		
 		KbpCorpusParser parser = new KbpCorpusParser();
 		
 		Iterator<String> docs = parser.breakFileByDocTags(inputStream);
 
 		while (docs.hasNext()) {
 
 			InputStream docStream = new ByteArrayInputStream(docs.next()
 					.getBytes());
 
 			try {
 				Document doc = parser.getXmlDocument(docStream);
 
				List<String> sentences;
				if (forum) sentences = parser.getForumContent(doc);
				else if (news) sentences = parser.getNewsContent(doc);
				else sentences = parser.getWebContent(doc);
 
 				for (String sent : sentences)
 					outputStream.println(sent);
 
 			} catch (SAXException | IOException | ParserConfigurationException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * KBP corpus files are pseudo-XML and are like a concatenation of XML
 	 * documents whose root elements are DOC elements.
 	 * 
 	 * To make it easier to recover from an error in an individual DOC, we do a
 	 * simple string split at DOC tags.
 	 * 
 	 * @param filename
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	private Iterator<String> breakFileByDocTags(InputStream input)
 			throws FileNotFoundException {
 
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(
 				input));
 
 		return new Iterator<String>() {
 			@Override
 			public boolean hasNext() {
 				try {
 					return reader.ready();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 
 			@Override
 			public String next() {
 
 				List<String> docLines = getUntilNextDoc(reader);
 				StringBuilder docBuffer = new StringBuilder(
 						docLines.size() * 10); // rough estimate of initial size
 				for (String line : docLines)
 					docBuffer.append(line + "\n");
 				return docBuffer.toString();
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	/**
 	 * Takes lines from reader until and including the next line that contains
 	 * "</DOC>"
 	 * 
 	 * Includes a hack that tries to fix malformed <QUOTE PREVIOUSPOST=" ... ">
 	 * elements in the KBP source corpus, that should end instead with ... "/>
 	 * to make it a self-closing element.
 	 * 
 	 * @param reader
 	 * @return
 	 */
 	private List<String> getUntilNextDoc(BufferedReader reader) {
 
 		String next;
 
 		List<String> stringList = new LinkedList<String>();
 
 		boolean inQuote = false;
 
 		try {
 			while ((next = reader.readLine()) != null) {
 				if (next.startsWith("<QUOTE"))
 					inQuote = true;
 				if (inQuote && next.contains("\">")) {
 					next = next.replaceFirst(Pattern.quote("\">"), "\"/>");
 					inQuote = false;
 				}
 
 				stringList.add(next);
 				if (next.contains("</DOC>") || next.contains("</doc>")) {
 					break;
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return stringList;
 	}
 
 	public Document getXmlDocument(InputStream docStream) throws SAXException,
 			IOException, ParserConfigurationException {
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setIgnoringElementContentWhitespace(true);
 		return factory.newDocumentBuilder().parse(docStream);
 	}
 
 	public List<String> getForumContent(Document doc) {
 
 		List<String> lines = new LinkedList<String>();
 
 		// get DocID
 		String docId = doc.getElementsByTagName("doc").item(0).getAttributes()
 				.getNamedItem("id").getTextContent();
 		int sentenceCounter = 0;
 
 		NodeList headLine = doc.getElementsByTagName("headline");
 
 		// get the headline, if there is one.
 		if (headLine.getLength() != 0) {
 			String headLineString = headLine.item(0).getTextContent().trim();
 			lines.add(docId + "\t" + sentenceCounter + "\t" + headLineString
 					+ offsetPlaceHolder);
 			sentenceCounter++;
 		}
 
 		// get the main post text.
 		NodeList nodeList = doc.getElementsByTagName("post");
 
 		for (int j = 0; j < nodeList.getLength(); ++j) {
 			NodeList childNodeList = nodeList.item(j).getChildNodes();
 			for (int i = childNodeList.getLength() - 1; i > -1; i--) {
 				if (childNodeList.item(i).getNodeName().equals("#text")) {
 					for (String line : childNodeList.item(i).getTextContent()
 							.split("\n\n")) {
 						String trimmed = line.replace("\n", " ").trim();
 						if (trimmed.isEmpty())
 							continue;
 						lines.add(docId + "\t" + sentenceCounter + "\t"
 								+ trimmed + offsetPlaceHolder);
 						sentenceCounter++;
 					}
 					break;
 				}
 			}
 		}
 
 		return lines;
 	}
 
 	public List<String> getNewsContent(Document doc) {
 
 		List<String> lines = new LinkedList<String>();
 
 		// get DocID
 		String docId = doc.getElementsByTagName("DOC").item(0).getAttributes()
 				.getNamedItem("id").getTextContent();
 		int sentenceCounter = 0;
 
 		// get the headline, if there is one.
 		NodeList headLine = doc.getElementsByTagName("HEADLINE");
 		if (headLine.getLength() != 0) {
 			String headLineString = headLine.item(0).getTextContent().trim();
 			lines.add(docId + "\t" + sentenceCounter + "\t" + headLineString
 					+ offsetPlaceHolder);
 			sentenceCounter++;
 		}
 
 		NodeList nodeList = doc.getElementsByTagName("P");
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			String text = nodeList.item(i).getTextContent().replace("\n", " ")
 					.trim();
 			if (text.isEmpty())
 				continue;
 			lines.add(docId + "\t" + sentenceCounter + "\t" + text
 					+ offsetPlaceHolder);
 			sentenceCounter++;
 		}
 		return lines;
 	}
 
 	public List<String> getWebContent(Document doc) {
 
 		List<String> lines = new LinkedList<String>();
 		// get DocID
 		String docId = doc.getElementsByTagName("DOCID").item(0)
 				.getTextContent().trim();
 		int sentenceCounter = 0;
 
 		NodeList headLine = doc.getElementsByTagName("HEADLINE");
 
 		// get the headline, if there is one.
 		if (headLine.getLength() != 0) {
 			String headLineString = headLine.item(0).getTextContent().trim();
 			lines.add(docId + "\t" + sentenceCounter + "\t" + headLineString
 					+ offsetPlaceHolder);
 			sentenceCounter++;
 		}
 
 		// get the main post text.
 		NodeList nodeList = doc.getElementsByTagName("POST");
 
 		for (int j = 0; j < nodeList.getLength(); ++j) {
 			NodeList childNodeList = nodeList.item(j).getChildNodes();
 			for (int i = childNodeList.getLength() - 1; i > -1; i--) {
 				if (childNodeList.item(i).getNodeName().equals("#text")) {
 					for (String line : childNodeList.item(i).getTextContent()
 							.split("\n\n")) {
 						String trimmed = line.replace("\n", " ").trim();
 						if (trimmed.isEmpty())
 							continue;
 						lines.add(docId + "\t" + sentenceCounter + "\t"
 								+ trimmed + offsetPlaceHolder);
 						sentenceCounter++;
 					}
 					break;
 				}
 			}
 		}
 
 		return lines;
 	}
 
 }
