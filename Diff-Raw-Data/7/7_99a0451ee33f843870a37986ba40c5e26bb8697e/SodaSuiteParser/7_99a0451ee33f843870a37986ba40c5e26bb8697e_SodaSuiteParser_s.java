 /*
 Copyright 2011 Trampus Richmond. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
    1. Redistributions of source code must retain the above copyright notice, this list of
       conditions and the following disclaimer.
 
    2. Redistributions in binary form must reproduce the above copyright notice, this list
       of conditions and the following disclaimer in the documentation and/or other materials
       provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY TRAMPUS RICHMOND ``AS IS'' AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 TRAMPUS RICHMOND OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 The views and conclusions contained in the software and documentation are those of the authors and 
 should not be interpreted as representing official policies, either expressed or implied, of Trampus Richmond.
  
  */
 
 package soda;
 
 import java.io.File;
 import java.util.Arrays;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 
 public class SodaSuiteParser {
 	
 	private SodaTestList tests = null;
 	
 	public SodaSuiteParser(String suitefile) {
 		Document doc = null;
 		File suiteFD = null;
 		DocumentBuilderFactory dbf = null;
 		DocumentBuilder db = null;
 
 		try {
 			this.tests = new SodaTestList();
 			suiteFD = new File(suitefile);
 			dbf = DocumentBuilderFactory.newInstance();
 			db = dbf.newDocumentBuilder();
 			doc = db.parse(suiteFD);
 			this.parse(doc.getDocumentElement().getChildNodes());
 		} catch (Exception exp) {
 			exp.printStackTrace();
 		}
 	}
 	
 	public SodaTestList getTests() {
 		return this.tests;
 	}
 	
 	private void parse(NodeList nodes) {
 		int len = nodes.getLength() -1;
 		
 		for (int i = 0; i <= len; i++) {
 			String name = nodes.item(i).getNodeName();
 			if (name.contains("#text")) {
 				continue;
 			}
 			
 			NamedNodeMap attrs = nodes.item(i).getAttributes();
 			int atts_len = attrs.getLength() -1;
 			for (int x = 0; x <= atts_len; x++) {
 				String attr_name = attrs.item(x).getNodeName();
 				String attr_value = attrs.item(x).getNodeValue();
 				File fd_tmp = null;
 				
 				if (attr_name.contains("fileset")) {
 					fd_tmp = new File(attr_value);
 					String base_path = fd_tmp.getAbsolutePath();
 					String[] files = fd_tmp.list();
 					Arrays.sort(files);
 					for (int findex = 0; findex <= files.length -1; findex++) {
						this.tests.add(base_path+"/"+files[findex]);
 					}
 				} else {
 					this.tests.add(attr_value);
 				}
 			}
 		}
 	}
 	
 }
