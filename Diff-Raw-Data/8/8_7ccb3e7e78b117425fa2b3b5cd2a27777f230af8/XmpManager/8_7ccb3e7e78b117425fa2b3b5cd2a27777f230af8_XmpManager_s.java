 /*
  * This file is part of PdfLicenseManager.
  *
  * Copyright (C) 2006  Enrico Masala
  *
  * PdfLicenseManager is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * PdfLicenseManager is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  *
  *
  * Author: Enrico Masala   < masala _at-symbol_ polito dot it >
  * Date: Jun 30, 2006
  *
  * Please cite the original author if you extend or modify this program
  */
 
 
 package pdflicense;
 
 import javax.xml.parsers.*;
 import org.xml.sax.*;
 import org.w3c.dom.*;
 
 import java.io.*;
 
 public class XmpManager
 {
 	StringBuffer sb;
 	Document doc;
 
 	static final String[] typeName = {
 		"none",
 		"Element",
 		"Attr",
 		"Text",
 		"CDATA",
 		"EntityRef",
 		"Entity",
 		"ProcInstr",
 		"Comment",
 		"Document",
 		"DocType",
 		"DocFragment",
 		"Notation",
 	};
 
 	public XmpManager() {
 		this.doc = null;
 	}
 
 	public void parseXmp(String xmp) throws IOException, LicenseException {
 		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder bu = f.newDocumentBuilder();
 			StringReader sr = new StringReader(xmp);
 			InputSource is = new InputSource(sr);
 			doc = bu.parse( is );
 		} catch (SAXException saxe) {
 			Exception  x = saxe;
 			if (saxe.getException() != null)
 				x = saxe.getException();
 			//x.printStackTrace();
 			System.out.println(x.getMessage());
 			throw new LicenseException("ERROR: The XMP info cannot be parsed!\n(Use putforce option to override corrupt XMP)\n");
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		}
 	}
 
 	public License getLicenseInfo() throws LicenseException {
 		NodeList nl;
 		nl = doc.getElementsByTagName("rdf:RDF");
 		if (nl == null) {
 			throw new LicenseException("Malformed XMP: Cannot find rdf:RDF string\n");
 		}
 		License lic = new License();	
 
 		nl = doc.getElementsByTagName("cc:license");
 		if (nl!=null) {
 			Node cclicense = nl.item(0);
 			if (cclicense != null) {
 				Node text = cclicense.getFirstChild();
 				if (text != null) {
 					lic.setCCLicense( text.getNodeValue() );
 				}
 			}
 		}
 
 		nl = doc.getElementsByTagName("xapRights:Marked");
 		if (nl!=null) {
 			Node rightsMark = nl.item(0);
 			if (rightsMark != null) {
 				Node text = rightsMark.getFirstChild();
 				if (text != null) {
 					lic.setLicenseMark( text.getNodeValue() );
 				}
 			}
 		}
 		nl = doc.getElementsByTagName("xapRights:WebStatement");
 		if (nl!=null) {
 			Node rightsWebStat = nl.item(0);
 			if (rightsWebStat != null) {
 				Node text = rightsWebStat.getFirstChild();
 				if (text != null) {
 					lic.setLicenseWebStatement( text.getNodeValue() );
 				}
 			}
 		}
 		nl = doc.getElementsByTagName("dc:rights");
 		if (nl!=null) {
 			Node dcRights = nl.item(0);
 			if (dcRights != null) {
 				Element el = (Element)dcRights;
 				NodeList nli = el.getElementsByTagName("rdf:Alt");
 				if (nli != null) {
 					Node alt = nli.item(0);
 					if (alt != null) {
 						Element el2 = (Element)alt;
 						NodeList nli2 = el2.getElementsByTagName("rdf:li");
 						if (nli2 != null) {
 							Node li = nli2.item(0);
 							if (li != null) {
 								Node text = li.getFirstChild();
 								if (text != null) {
 									lic.setRightsText( text.getNodeValue() );
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return lic;
 	}
 
 	/**
  	 * Eliminate other licensing info and insert license info taken from lic;
 	 * A valid XMP must have already been parsed.
 	 *
 	 * @param lic The object containing the licensing information
 	 */
 	public void updateLicense(CCLicense lic) throws LicenseException {
 		final boolean resetAbout=true;
 		NodeList nl;
 		nl = doc.getElementsByTagName("rdf:RDF");
 		if (nl == null) {
 			throw new LicenseException("Malformed XMP: Cannot find rdf:RDF string\n");
 		}
 
 		if (resetAbout) {
 			nl = doc.getElementsByTagName("rdf:Description");
 			int len = nl.getLength();
 			for (int i=0; i<len; i++) {
 				Element el = (Element)nl.item(i);
 				// Reset the "rdf:about" attribute
 				el.setAttribute("rdf:about","");
 			}
 		}
 
 		Node rdf = nl.item(0);
 		nl = doc.getElementsByTagName("cc:license");
 		if (nl==null || nl.getLength()==0) {
 			Element descr = doc.createElement("rdf:Description");
 			Element cclicense = doc.createElement("cc:license");
 			descr.setAttribute("rdf:about", "");
 			descr.setAttribute("xmlns:cc", "http://web.resource.org/cc/");
 			cclicense.appendChild(doc.createTextNode(lic.getUrl()) );
 			//cclicense.setAttribute("rdf:", lic.getUrl() );
 			descr.appendChild(cclicense);
 			rdf.appendChild(descr);
 			//Node descr = addNewDescriptionNode();
 		} else {
 			if (nl.getLength() > 1) {
 				throw new LicenseException("More than one cc:licence tag is present\n");
 			} else {
 				Node ccnode = nl.item(0);
 				Node text = ccnode.getFirstChild();
 				if (text == null) {
 					ccnode.appendChild(doc.createTextNode(lic.getUrl()));
 				} else {
 					if (! typeName[text.getNodeType()].equals("Text") )
 						throw new LicenseException("INTERNAL ERROR: Cannot find cc:license string\n");
 					text.setNodeValue(lic.getUrl());
 				}
 			}
 		}
 
 		nl = doc.getElementsByTagName("dc:rights");
 		if (nl==null || nl.getLength()==0) {
 			Node descr = findOrCreateDescriptionElemWithAttr("xmlns:dc", "http://purl.org/dc/elements/1.1/", rdf);
 			Element rights = doc.createElement("dc:rights");
 			descr.appendChild(rights);
 			Element alt = doc.createElement("rdf:Alt");
 			rights.appendChild(alt);
 			Element li = doc.createElement("rdf:li");
 			alt.appendChild(li);
 			li.setAttribute("xml:lang", "x-default");
 			li.appendChild(doc.createTextNode(lic.getText()) );
 		} else {
 			if (nl.getLength() > 1) {
 				throw new LicenseException("More than one dc:rights tag is present\n");
 			} else {
 				Element el = (Element)nl.item(0);
 				NodeList nli = el.getElementsByTagName("rdf:Alt");
 				if (nli != null) {
 					Node alt = nli.item(0);
 					if (alt != null) {
 						Element el2 = (Element)alt;
 						NodeList nli2 = el2.getElementsByTagName("rdf:li");
 						if (nli2 != null) {
 							Node li = nli2.item(0);
 							if (li != null) {
 								Node text = li.getFirstChild();
 								if (text != null) {
 									text.setNodeValue(lic.getText());
 								} else {
 									li.appendChild(doc.createTextNode(lic.getText()));
 								}
 							}
 						} else {
 							throw new LicenseException("INTERNAL ERROR: Cannot find rdf:li tag under rdf:Alt under dc:rights tag\n");
 						}
 					}
 				} else {
 					throw new LicenseException("INTERNAL ERROR: Cannot find rdf:Alt tag under dc:rights tag\n");
 				}
 			}
 		}
 
 		nl = doc.getElementsByTagName("xapRights:Marked");
 		if (nl==null || nl.getLength()==0) {
 			Node descr = findOrCreateDescriptionElemWithAttr("xmlns:xapRights", "http://ns.adobe.com/xap/1.0/rights/", rdf);
 			Element marked = doc.createElement("xapRights:Marked");
 			descr.appendChild(marked);
 			marked.appendChild(doc.createTextNode("True") );
 		} else {
 			if (nl.getLength() > 1) {
 				throw new LicenseException("More than one xapRights:Marked tag is present\n");
 			} else {
 				Node marked = nl.item(0);
 				Node text = marked.getFirstChild();
 				if (text == null) {
 					marked.appendChild(doc.createTextNode("True"));
 				} else {
 					if (! typeName[text.getNodeType()].equals("Text") )
 						throw new LicenseException("INTERNAL ERROR: Cannot find xapRights:Marked string\n");
 					text.setNodeValue("True");
 				}
 			}
 		}
 
 		nl = doc.getElementsByTagName("xapRights:WebStatement");
 		if (nl==null || nl.getLength()==0) {
 			Node descr = findOrCreateDescriptionElemWithAttr("xmlns:xapRights", "http://ns.adobe.com/xap/1.0/rights/", rdf);
 			Element marked = doc.createElement("xapRights:WebStatement");
 			descr.appendChild(marked);
 			marked.appendChild(doc.createTextNode(lic.getUrl()) );
 		} else {
 			if (nl.getLength() > 1) {
 				throw new LicenseException("More than one xapRights:WebStatement tag is present\n");
 			} else {
 				Node webstatement = nl.item(0);
 				Node text = webstatement.getFirstChild();
 				if (text == null) {
 					webstatement.appendChild(doc.createTextNode(lic.getUrl()));
 				} else {
 					if (! typeName[text.getNodeType()].equals("Text") )
 						throw new LicenseException("INTERNAL ERROR: Cannot find xapRights:WebStatement string\n");
 					text.setNodeValue(lic.getUrl());
 				}
 			}
 		}
 
 	}
 
 
 	/**
  	 * Create an XMP for the given license. Then the object is automatically parsed and it can be used as usual.
 	 *
 	 * @param lic The object containing the licensing information
 	 */
 	public void createLicense(CCLicense lic) throws LicenseException, IOException {
 		StringBuffer s;
 		
 		s = new StringBuffer("");
 
 		s.append("<?xpacket begin='\uFEFF' id='W5M0MpCehiHzreSzNTczkc9d'?>\n");
 		s.append("<x:xmpmeta xmlns:x='adobe:ns:meta/'>\n");
 		s.append("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n");
 
 		s.append(" <rdf:Description rdf:about='' xmlns:xapRights='http://ns.adobe.com/xap/1.0/rights/'>\n");
 		s.append("  <xapRights:Marked>True</xapRights:Marked>\n");
 		s.append("  <xapRights:WebStatement>"+lic.getUrl()+"</xapRights:WebStatement>\n");
 		s.append(" </rdf:Description>\n");
 
 		s.append(" <rdf:Description rdf:about='' xmlns:dc='http://purl.org/dc/elements/1.1/'>\n");
 		s.append("  <dc:rights>\n");
 		s.append("   <rdf:Alt>\n");
 		s.append("    <rdf:li xml:lang='x-default'>"+lic.getText()+"</rdf:li>\n");
 		s.append("   </rdf:Alt>\n");
 		s.append("  </dc:rights>\n");
 		s.append(" </rdf:Description>\n");
 
 		s.append(" <rdf:Description rdf:about='' xmlns:cc='http://web.resource.org/cc/'>\n");
 		s.append("  <cc:license>"+lic.getUrl()+"</cc:license>\n");
 		s.append(" </rdf:Description>\n");
 
 		s.append("</rdf:RDF>\n");
 		s.append("</x:xmpmeta>\n");
 		s.append("<?xpacket end='r'?>\n");
 
 		this.parseXmp(s.toString());
 	}
 
 
 	private Element findOrCreateDescriptionElemWithAttr(String attr, String value, Node rdf) {
 		boolean found=false;
 		NodeList nl = doc.getElementsByTagName("rdf:Description");
 		int len = nl.getLength();
 		int i;
 		for (i=0; i<len; i++) {
 			Node n = nl.item(i);
 			if (n.hasAttributes()) {
 				NamedNodeMap nnm = n.getAttributes();
 				int nAttr = nnm.getLength();
 				for (int j=0; j<nAttr; j++) {
 					Node a = nnm.item(j);
 					if (a.getNodeName().equals(attr)) {
 						found=true;
 						break;
 					}
 				}
 			}
 			if (found) {
 				break;
 			}	
 		}
 		if (found) {
 			return (Element)nl.item(i);
 		} else {
 			// Create the rdf:Description element
 			Element el = doc.createElement("rdf:Description");
 			rdf.appendChild(el);	
 			el.setAttribute("rdf:about", "");
 			el.setAttribute(attr, value);
 			return el;
 		}
 	}
 
 /*
 	private Node addNewDescriptionNode() throws LicenseException {
 		Node rdf = doc.getElementsByTagName("rdf:RDF").item(0);
 		Node nn = rdf.cloneNode(false);
 		if (! (nn instanceof Element) ) {
 			throw new LicenseException("INTERNAL ERROR: cannot find rdf:RDF element");
 		}
 		NamedNodeMap nnm = nn.getAttributes();
 		while (nnm!=null && nnm.getLength() > 0) {
 			Attr tn = (Attr)nnm.item(0);
 			System.out.println("   NAME: "+tn.getName()+" VALUE: "+tn.getValue());
 			Element e = (Element)nn;
 			e.removeAttributeNode(tn);
 			nnm = nn.getAttributes();
 		}
 		nn.setName
 		rdf.appendChild(nn);	
 		return nn;
 	}
 
 	private void removeNodeList (NodeList nl) {
 		if (nl != null) {
 			int len=nl.getLength();
 			for (int i=0; i<len; i++) {
 				Node ch = nl.item(i);
 				Node pn = ch.getParentNode();
 				if (pn != null) {
 					pn.removeChild(ch);	
 				} else {
 					System.out.println("INTERNAL ERROR getting parent node\n"); System.exit(1);
 				}
 			}	
 		}
 	}
 */
 
 	// Serialize XMP (with human-readable formatting)
 
 	public String getXmpString() {
 		sb = new StringBuffer();
 		sb.append("<?xpacket begin='\uFEFF' id='W5M0MpCehiHzreSzNTczkc9d'?>\n");
 		printXmlNode(doc, 0);
 		for (int j=0; j<5; j++) {
 			for (int i=0; i<100; i++) {
 				sb.append("");
 			}
 			sb.append("\n");
 		}
 		sb.append("<?xpacket end='w'?>");
 		return sb.toString();
 	}
 
 	boolean isLastText = false;
 
 	private void printXmlNode(Node n, int level) {
 
 		NodeList nl = n.getChildNodes();
 		int nChild = nl.getLength();
 		//System.out.println("nChild: "+nChild);
 
 		//System.out.println("type: "+typeName[n.getNodeType()]+" name: "+ n.getNodeName()+" value: "+n.getNodeValue());
 		if ( typeName[n.getNodeType()].equals("Text") ) {
 			if ( ! (n.getNodeValue().trim().equals("")) ) {
 				sb.append(n.getNodeValue());
 				isLastText = true;
 			}
 		} else if ( typeName[n.getNodeType()].equals("Document") ) {
 		} else if ( typeName[n.getNodeType()].equals("ProcInstr") ) {
 		} else {
 			if (! "x:xmpmeta".equals(n.getNodeName())) {
 				sb.append("\n");
 			}
 			for (int i=0; i<level; i++) sb.append(" ");
 			sb.append("<"+n.getNodeName());
 			NamedNodeMap nnm = n.getAttributes();
 			if (nnm!=null) {
 				int len = nnm.getLength();
 				for (int j=0; j<len; j++) {
 					Node tn = nnm.item(j);
 					sb.append(" "+tn.getNodeName()+"='"+tn.getNodeValue()+"'");
 				}
 			}
 			sb.append(">");
 		}
 
 		if (nChild > 0) {
 			for (int i=0; i<nChild; i++) {
 				Node cn = nl.item(i);
 				printXmlNode(cn,level+1);
 			}
 		}
 		if ( !typeName[n.getNodeType()].equals("Text") &&
 			!typeName[n.getNodeType()].equals("Document") && 
 			!typeName[n.getNodeType()].equals("ProcInstr")  ) {
 			if (!isLastText) {
 				sb.append("\n");
 				for (int i=0; i<level; i++) sb.append(" ");
 			}
 			isLastText = false;
 			sb.append("</"+n.getNodeName()+">");
 		}
 	}
 }
 
