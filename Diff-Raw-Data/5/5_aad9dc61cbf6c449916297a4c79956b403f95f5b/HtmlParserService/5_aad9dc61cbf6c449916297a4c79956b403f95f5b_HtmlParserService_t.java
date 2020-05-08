 package org.test.tdc.service;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.http.client.ClientProtocolException;
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.NodeClassFilter;
 import org.htmlparser.filters.OrFilter;
 import org.htmlparser.tags.FormTag;
 import org.htmlparser.tags.InputTag;
 import org.htmlparser.tags.OptionTag;
 import org.htmlparser.tags.SelectTag;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 import org.springframework.stereotype.Service;
 import org.test.tdc.common.HttpClientUtils;
 
 @Service
 public class HtmlParserService {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 * @throws URISyntaxException 
 	 */
 	public Map<String, Object> parse(String url) throws ClientProtocolException, IOException, URISyntaxException{
 		
 		String response = HttpClientUtils.httpGet(url);
 
 		Map<String, Object> result = htmlFormParser(response);
 		return result;
 	}
	
	public String getHtml(String url) throws ClientProtocolException, IOException, URISyntaxException{
		String response = HttpClientUtils.httpGet(url);
		return response;
	}
 
 	public Map<String, Object> htmlFormParser(String content) {
 		Map<String, Object> result = new HashMap<String, Object>();
 
 		List<FormInfo> formList = new ArrayList<FormInfo>();
 
 		NodeFilter formFilter = new NodeClassFilter(FormTag.class);
 		Node[] nodes = htmlToNode(content, new NodeFilter[] { formFilter });
 		for (int i = 0; nodes != null && i < nodes.length; i++) {
 			Node formNode = (Node) nodes[i];
 			if (formNode instanceof FormTag) {
 				FormInfo formInfo = new FormInfo();
 
 				FormTag formTag = (FormTag) formNode;
 				formInfo.setFormName(formTag.getFormName());
 				formInfo.setMethod(formTag.getFormMethod());
 				formInfo.setFormAction(formTag.getFormLocation());
 
 				String childrenContent = formTag.getChildrenHTML();
 				NodeFilter inputFilter = new NodeClassFilter(InputTag.class);
 				NodeFilter selectFilter = new NodeClassFilter(SelectTag.class);
 				Node[] childrenNodes = htmlToNode(childrenContent, new NodeFilter[] { inputFilter, selectFilter });
 
 				for (int j = 0; childrenNodes != null
 						&& j < childrenNodes.length; j++) {
 					Node anode = (Node) childrenNodes[j];
 					FormElements fe = new FormElements();
 					if (anode instanceof SelectTag) {
 						SelectTag selectnode = (SelectTag) anode;
 						String snChildren = selectnode.getChildrenHTML();
 						if (snChildren == null || snChildren == "" || selectnode.getAttribute("name") == null) {
 							continue;
 						}
 						
 						NodeFilter optionFilter = new NodeClassFilter(OptionTag.class);
 						Node[] nl_nodes = htmlToNode(childrenContent,new NodeFilter[] { optionFilter });
 						String select_value = "";
 						for (int k = 0; k < nl_nodes.length; k++) {
 							Node optnode = (Node) nl_nodes[k];
 							OptionTag opttag = (OptionTag) optnode;
 							Vector vv = opttag.getAttributesEx();
 							if (vv.toString().indexOf("selected") != -1)
 								select_value = opttag.getOptionText();
 						}
 
 						fe.setName(selectnode.getAttribute("name"));
 						fe.setValue(select_value);
 						fe.setType("select");
 						formInfo.addElements(fe);
 					} else if (anode instanceof InputTag) {
 						InputTag inputnode = (InputTag) anode;
 						if (inputnode.getAttribute("name") == null
 								|| inputnode.getAttribute("name").isEmpty()) {
 							continue;
 						}
 						
 						String type = inputnode.getAttribute("type");
 						String name = inputnode.getAttribute("name");
 						String value = inputnode.getAttribute("value");
 
 						fe.setType(type);
 						fe.setName(name);
 						fe.setValue(value);
 						formInfo.addElements(fe);
 					}
 				}
 				formList.add(formInfo);
 			}
 		}
 
 		result.put("formList", formList);
 		return result;
 	}
 
 	private Node[] htmlToNode(String content, NodeFilter[] filters) {
 		Parser parser = Parser.createParser(content, "GBK");
 		NodeList nodeList = null;
 		OrFilter orFilter = new OrFilter();
 		orFilter.setPredicates(filters);
 		try {
 			nodeList = parser.parse(orFilter);
 		} catch (ParserException e) {
 			e.printStackTrace();
 		}
 		if (nodeList != null) {
 			return nodeList.toNodeArray();
 		} else {
 			return null;
 		}
 	}
 	
 	
 
 }
 
 class FormElements {
 	private String name;
 	private String value;
 	private String type;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 }
 
 class FormInfo {
 	private String formName;
 	private String formAction;
 	private String method;
 	private List<FormElements> inputs = new ArrayList<FormElements>();
 
 	public String getFormName() {
 		return formName;
 	}
 
 	public void setFormName(String formName) {
 		this.formName = formName;
 	}
 
 	public String getFormAction() {
 		return formAction;
 	}
 
 	public void setFormAction(String formAction) {
 		this.formAction = formAction;
 	}
 
 	public List<FormElements> getInputs() {
 		return inputs;
 	}
 
 	public void setInputs(List<FormElements> inputs) {
 		this.inputs = inputs;
 	}
 
 	public String getMethod() {
 		return method;
 	}
 
 	public void setMethod(String method) {
 		this.method = method;
 	}
 
 	public void addElements(FormElements formElements) {
 		this.inputs.add(formElements);
 	}
 
 }
