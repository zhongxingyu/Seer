 package helperclasses;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import annotationclasses.DocInfoAnnotation;
 import annotationclasses.EventAnnotation;
 import annotationclasses.LinkInfoAnnotation;
 
 import dataclasses.DocInfo;
 import dataclasses.EventInfo;
 import dataclasses.LinkInfo;
 
 import edu.stanford.nlp.ling.CoreAnnotations;
 import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
 import edu.stanford.nlp.time.Timex;
 import edu.stanford.nlp.time.XMLUtils;
 import edu.stanford.nlp.util.CoreMap;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class AnnotationWriter {
 
 	public static final String HEADER = "<?xml version=\"1.0\" ?>\n"
 			+ "<TimeML xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 			+ "xsi:noNamespaceSchemaLocation=\"http://timeml.org/timeMLdocs/TimeML_1.2.1.xsd\">\n\n";
 	public static final String FOOTER = "</TimeML>\n\n";
 	
 	/*
 	 * Taken from http://www.java2s.com/Code/Java/Data-Type/Returnstrueifspecifiedcharacterisapunctuationcharacter.htm
 	 */
 	private static boolean isPunctuation(char c) {
 		return c == ','
 	            || c == '.'
 	            || c == '!'
 	            || c == '?'
 	            || c == ':'
 	            || c == ';'
 	            || c == '"'
 	            || c == '\''
 	            || c == '`';
 	}
 	
 	private static void writeText(Annotation annotation, BufferedWriter out,
 			ArrayList<EventInfo> events, ArrayList<LinkInfo> links) throws IOException {
 		Element textElem = createTextElement(annotation, events, links);
 		String s = XMLUtils.nodeToString(textElem, false);
 		out.write(s + "\n");
 	}
 
 	private static Element createTextElement(Annotation annotation,
 			ArrayList<EventInfo> events, ArrayList<LinkInfo> links) throws IOException {
 
 		// Link ID assignment happens here
 		int nextLinkID = 0;
 
 		// Keep track of whether we're inside a tag
 		String curType = "";
 		Timex curTimex = null;
 
 		Element element = XMLUtils.createElement("TEXT");
 
		int lastCharOffset = 0;
		String docText = annotation.get(CoreAnnotations.TextAnnotation.class);
 		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
 		int tid = 1;
 		for(CoreMap sentence: sentences) {
 			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 			for (CoreLabel token: tokens) {
 
 				// Get information from token
 				Timex timex = token.get(TimexAnnotation.class);
 				EventInfo event = token.get(EventAnnotation.class);
 				LinkInfo link = token.get(LinkInfoAnnotation.class);
				String tokenText = token.get(CoreAnnotations.OriginalTextAnnotation.class);
 
 				Node tokenNode = null;
				int annotatedBeginCharOffset = -1;
				int annotatedEndCharOffset = -1;
 
 				// Handle if this is a timex
 				if (timex != null) {
 					if (!timex.equals(curTimex)) {
 						// TODO: use timex tid as is once fixed in SUTime
 						Element timexElem = timex.toXmlElement();
 						timexElem.setAttribute("tid", "t" + tid);
 						tid++;
 						tokenNode = timexElem;
 						curType = timex.timexType();
 						curTimex = timex;
						annotatedBeginCharOffset = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
						annotatedEndCharOffset = annotatedBeginCharOffset + timex.text().length();
 					}
 					// Handle if this is an event
 				} else if (event != null /*&& !curType.equals(event.currEventType)*/) {
 					// TODO: Handle events with multiple tokens?
 					Element eventElem = XMLUtils.createElement("EVENT");
 					eventElem.setAttribute("eid", event.currEventId);
 					eventElem.setAttribute("class", event.currEventType);
					eventElem.setTextContent(tokenText);
 					tokenNode = eventElem;
 					curType = event.currEventType;
 					curTimex = null;
 					events.add(event);
					annotatedBeginCharOffset = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
					annotatedEndCharOffset = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
 					// Handle normal tokens
 				} else {
 					curType = "";
 					curTimex = null;
 				}
 				if (tokenNode != null) {
					String spanText = docText.substring(lastCharOffset,annotatedBeginCharOffset);
				    element.appendChild(XMLUtils.createTextNode(spanText));						
 					element.appendChild(tokenNode);
					lastCharOffset = annotatedEndCharOffset;
 				}
 
 				// Handle links
 				if (link != null) {
 					link.id = "l" + (nextLinkID++);
 					links.add(link);
 				}
 			}
 		}
		if (lastCharOffset < docText.length()) {
			String spanText = docText.substring(lastCharOffset);
			element.appendChild(XMLUtils.createTextNode(spanText));		
		}
 		return element;
 	}
 
 	private static void writeEventInstances(ArrayList<EventInfo> events, BufferedWriter out)
 			throws IOException {
 		for (EventInfo event: events) {
 			out.write("<MAKEINSTANCE eventID=\"" + event.currEventId 
 					+ "\" eiid=\"" + event.currEiid + "\" tense=\"" + event.tense
 					+ "\" aspect=\"" + event.aspect + "\" polarity=\"" + event.polarity
 					+ "\" pos=\"" + event.pos + "\"/>\n");
 		}
 	}
 
 	private static void writeTimexEventLinks(ArrayList<LinkInfo> links, BufferedWriter out)
 			throws IOException {
 		for (LinkInfo link: links) {
 			String instanceString = "";
 			if (link.time != null)
 				instanceString = "timeID=\"" + link.time.currTimeId + "\"";
 			else
 				instanceString = "eventInstanceID=\"" + link.eventInstance.currEiid + "\"";
 
 			out.write("<TLINK lid=\"" + link.id + "\" relType=\"" + link.type
 					+ "\" " + instanceString + " relatedToEventInstance=\"" + link.relatedTo.currEiid
 					+ "\" origin=\"USER\"/>\n"); //TODO fix origin
 		}
 	}
 
 	public static Element createElement(String tag, String text) {
 		Element elem = XMLUtils.createElement(tag);
 		elem.setTextContent(text);
 		return elem;
 	}
 
 	public static void writeElement(BufferedWriter out, String tag, String text) throws IOException {
 		Element elem = createElement(tag, text);
 		String s = XMLUtils.nodeToString(elem, false);
 		out.write(s + "\n");
 	}
 
 	public static void writeAnnotation(Annotation annotation, BufferedWriter out)
 			throws IOException {
 
 		ArrayList<EventInfo> events = new ArrayList<EventInfo>();
 		ArrayList<LinkInfo> links = new ArrayList<LinkInfo>();
 
 		// Get the auxiliary information associated with this document
 		DocInfo info = annotation.get(DocInfoAnnotation.class);
 
 		out.write(HEADER);
 		out.write("<DOCID>" + info.id + "</DOCID>\n\n");
 		out.write("<DCT>" + info.dct + "</DCT>\n\n");
 		writeElement(out, "TITLE", info.title);
 		writeElement(out, "EXTRAINFO", info.extra);
 
 		writeText(annotation, out, events, links);
 		writeEventInstances(events, out);
 		writeTimexEventLinks(links, out);
 
 		out.write(FOOTER);
 	}
 }
