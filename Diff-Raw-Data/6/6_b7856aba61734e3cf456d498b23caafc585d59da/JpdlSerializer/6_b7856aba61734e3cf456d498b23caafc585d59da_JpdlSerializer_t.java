 package org.jboss.tools.flow.jpdl4.editor;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.jboss.tools.flow.common.model.Element;
 import org.jboss.tools.flow.common.properties.IPropertyId;
 import org.jboss.tools.flow.common.wrapper.ConnectionWrapper;
 import org.jboss.tools.flow.common.wrapper.ContainerWrapper;
 import org.jboss.tools.flow.common.wrapper.FlowWrapper;
 import org.jboss.tools.flow.common.wrapper.LabelWrapper;
 import org.jboss.tools.flow.common.wrapper.NodeWrapper;
 import org.jboss.tools.flow.common.wrapper.Wrapper;
 import org.jboss.tools.flow.jpdl4.Logger;
 import org.jboss.tools.flow.jpdl4.model.Argument;
 import org.jboss.tools.flow.jpdl4.model.Assignment;
 import org.jboss.tools.flow.jpdl4.model.CancelEndEvent;
 import org.jboss.tools.flow.jpdl4.model.CustomTask;
 import org.jboss.tools.flow.jpdl4.model.ErrorEndEvent;
 import org.jboss.tools.flow.jpdl4.model.EventListener;
 import org.jboss.tools.flow.jpdl4.model.EventListenerContainer;
 import org.jboss.tools.flow.jpdl4.model.ExclusiveGateway;
 import org.jboss.tools.flow.jpdl4.model.Field;
 import org.jboss.tools.flow.jpdl4.model.ForkParallelGateway;
 import org.jboss.tools.flow.jpdl4.model.HqlTask;
 import org.jboss.tools.flow.jpdl4.model.HumanTask;
 import org.jboss.tools.flow.jpdl4.model.InputParameter;
 import org.jboss.tools.flow.jpdl4.model.JavaTask;
 import org.jboss.tools.flow.jpdl4.model.JoinParallelGateway;
 import org.jboss.tools.flow.jpdl4.model.MailTask;
 import org.jboss.tools.flow.jpdl4.model.OutputParameter;
 import org.jboss.tools.flow.jpdl4.model.Parameter;
 import org.jboss.tools.flow.jpdl4.model.Process;
 import org.jboss.tools.flow.jpdl4.model.ProcessNode;
 import org.jboss.tools.flow.jpdl4.model.ScriptTask;
 import org.jboss.tools.flow.jpdl4.model.SequenceFlow;
 import org.jboss.tools.flow.jpdl4.model.ServiceTask;
 import org.jboss.tools.flow.jpdl4.model.SqlTask;
 import org.jboss.tools.flow.jpdl4.model.StartEvent;
 import org.jboss.tools.flow.jpdl4.model.SubprocessTask;
 import org.jboss.tools.flow.jpdl4.model.SuperState;
 import org.jboss.tools.flow.jpdl4.model.Swimlane;
 import org.jboss.tools.flow.jpdl4.model.TerminateEndEvent;
 import org.jboss.tools.flow.jpdl4.model.Timer;
 import org.jboss.tools.flow.jpdl4.model.WaitTask;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 public class JpdlSerializer {
 	
 	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
 	private static Transformer transformer = null;
 	
 	static {
 		try {
 			transformer = transformerFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 		} catch (TransformerConfigurationException e) {				
 			Logger.logError("Error while creating XML tranformer.", e);	
 		}
 	}
 	
     public void serialize(Wrapper wrapper, OutputStream os) throws IOException {
     	StringBuffer buffer = new StringBuffer();
     	appendToBuffer(buffer, wrapper, 0);
     	Writer writer = new OutputStreamWriter(os);
     	writer.write(buffer.toString());
     	writer.close();
     }
     
     private void appendToBuffer(StringBuffer buffer, Wrapper wrapper, int level) {
     	Object object = wrapper.getElement();
         if (!(object instanceof Element)) return;
        	appendOpening(buffer, wrapper, level);
        	StringBuffer body = new StringBuffer();
        	appendBody(body, wrapper, level);
        	if (body.length() > 0) {
        		buffer.append(">");
        		buffer.append(body);
        		appendClosing(buffer, wrapper, level);
        	} else {
        		buffer.append("/>"); 
        	}
     }
     
     
     
     private void appendNodeList(StringBuffer buffer, ArrayList<Node> nodeList) {
 		if (transformer == null) {
 			Logger.logInfo("Skipping append nodes as transformer is not initialized.");
 			return;
 		}
 		DOMSource domSource = new DOMSource();
 		for (Node node : nodeList) {
 	    	StringWriter writer = new StringWriter();
 	    	domSource.setNode(node);
 	    	Result result = new StreamResult(writer);
 	    	try {
 				transformer.transform(domSource, result);
 			} catch (TransformerException e) {
 				Logger.logError("Exception while transforming xml.", e);
 			}
 			buffer.append(writer.getBuffer());
 		}
 	}
 
 
 
 	interface WrapperSerializer {
     	void appendOpening(StringBuffer buffer, Wrapper wrapper, int level);
     }
     
     abstract class AbstractWrapperSerializer implements WrapperSerializer {
     	protected abstract List<String> getAttributesToSave();
     	protected String getPropertyName(String attributeName) {
     		return attributeName;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		String value = (String)wrapper.getPropertyValue(getPropertyName(attributeName));
     		if (value == null || "".equals(value)) return;
     		buffer.append(" " + attributeName + "=\"" + value + "\"");
     	}
     	@SuppressWarnings("unchecked")
 		protected void appendLeadingNodes(StringBuffer buffer, Wrapper wrapper, int level) {
         	ArrayList<Node> leadingNodeList = (ArrayList<Node>)wrapper.getElement().getMetaData("leadingNodes");
         	boolean appendLeadingNodes = leadingNodeList != null && !leadingNodeList.isEmpty();
         	if (appendLeadingNodes) {
         		appendNodeList(buffer, leadingNodeList);
         	} else {
         		buffer.append("\n");
         		appendPadding(buffer, level);
         	}
     	}
     	protected void appendDefaultAttribute(StringBuffer buffer, Node node) {
     		buffer.append(" " + node.getNodeName() + "=\"" + node.getNodeValue() + "\"");
     	}
     	protected void appendAttributes(StringBuffer buffer, Wrapper wrapper, int level) {
     		Element element = wrapper.getElement();
     		if (element == null) return;
     		NamedNodeMap attributes = (NamedNodeMap)element.getMetaData("attributes");
     		List<String> attributeNames = getAttributesToSave();
     		if (attributes != null) {
 	     		for (int i = 0; i < attributes.getLength(); i++) {
 	    			String nodeName = attributes.item(i).getNodeName();
 	    			if (attributeNames.contains(nodeName)) {
 	    				appendAttributeToSave(nodeName, buffer, wrapper);
 	    				attributeNames.remove(nodeName);
 	    			} else {
 	    				appendDefaultAttribute(buffer, attributes.item(i));
 	    			}
 	    		}
     		}
      		for (int i = 0; i < attributeNames.size(); i++) {
      			appendAttributeToSave(attributeNames.get(i), buffer, wrapper);
      		}
     	}
     	public void appendOpening(StringBuffer buffer, Wrapper wrapper, int level) {
     		appendLeadingNodes(buffer, wrapper, level);
     		buffer.append("<" + getNodeName(wrapper.getElement()));
     		appendAttributes(buffer, wrapper, level);
     	}
     	public void appendClosing(StringBuffer buffer, Wrapper wrapper) {
     		buffer.append("</" + getNodeName(wrapper.getElement()) + ">");
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     	}
     }
     
     private String getNodeName(Element element) {
     	IConfigurationElement configuration = (IConfigurationElement)element.getMetaData("configurationElement");
     	String elementId = configuration.getAttribute("id");
 		if ("org.jboss.tools.flow.jpdl4.process".equals(elementId)) return "process";
 		else if ("org.jboss.tools.flow.jpdl4.startEvent".equals(elementId)) return "start";
 		else if ("org.jboss.tools.flow.jpdl4.terminateEndEvent".equals(elementId)) return "end";
 		else if ("org.jboss.tools.flow.jpdl4.errorEndEvent".equals(elementId)) return "end-error";
 		else if ("org.jboss.tools.flow.jpdl4.cancelEndEvent".equals(elementId)) return "end-cancel";
 		else if ("org.jboss.tools.flow.jpdl4.waitTask".equals(elementId)) return "state";
 		else if ("org.jboss.tools.flow.jpdl4.hqlTask".equals(elementId)) return "hql";
 		else if ("org.jboss.tools.flow.jpdl4.sqlTask".equals(elementId)) return "sql";
 		else if ("org.jboss.tools.flow.jpdl4.javaTask".equals(elementId)) return "java";
 		else if ("org.jboss.tools.flow.jpdl4.scriptTask".equals(elementId)) return "script";
 		else if ("org.jboss.tools.flow.jpdl4.mailTask".equals(elementId)) return "mail";
 		else if ("org.jboss.tools.flow.jpdl4.serviceTask".equals(elementId)) return "esb";
 		else if ("org.jboss.tools.flow.jpdl4.humanTask".equals(elementId)) return "task";
 		else if ("org.jboss.tools.flow.jpdl4.subprocessTask".equals(elementId)) return "sub-process";
 		else if ("org.jboss.tools.flow.jpdl4.customTask".equals(elementId)) return "custom";
 		else if ("org.jboss.tools.flow.jpdl4.exclusiveGateway".equals(elementId)) return "decision";
 		else if ("org.jboss.tools.flow.jpdl4.parallelJoinGateway".equals(elementId)) return "join";
 		else if ("org.jboss.tools.flow.jpdl4.parallelForkGateway".equals(elementId)) return "fork";
 		else if ("org.jboss.tools.flow.jpdl4.sequenceFlow".equals(elementId)) return "transition";
 		else if ("org.jboss.tools.flow.jpdl4.swimlane".equals(elementId)) return "swimlane";
 		else if ("org.jboss.tools.flow.jpdl4.timer".equals(elementId)) return "timer";
 		else if ("org.jboss.tools.flow.jpdl4.eventListenerContainer".equals(elementId)) return "on";
 		else if ("org.jboss.tools.flow.jpdl4.eventListener".equals(elementId)) return "event-listener";
 		else if ("org.jboss.tools.flow.jpdl4.inputParameter".equals(elementId)) return "parameter-in";
 		else if ("org.jboss.tools.flow.jpdl4.outputParameter".equals(elementId)) return "parameter-out";
 		else if ("org.jboss.tools.flow.jpdl4.argument".equals(elementId)) return "arg";
 		else if ("org.jboss.tools.flow.jpdl4.field".equals(elementId)) return "field";
 		else return null;
     }
     
     class SequenceFlowWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("name");
     		result.add("to");
     		result.add("g");
     		return result;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		if (!(wrapper instanceof ConnectionWrapper)) return;
     		Element element = wrapper.getElement();
     		if (!(element instanceof SequenceFlow)) return;
     		if ("name".equals(attributeName)) { 
     			appendName(buffer, (SequenceFlow)element);
     		} else if ("to".equals(attributeName)) {
 				appendTo(buffer, (SequenceFlow)element);
 			} else if ("g".equals(attributeName)) {
 				appendGraphics(buffer, (ConnectionWrapper)wrapper);
     		}
     	}
     	protected void appendName(StringBuffer buffer, SequenceFlow sequenceFlow) {
     		if (sequenceFlow.getName() == null) return;
     		String value = sequenceFlow.getName();
     		if (value == null || "".equals(sequenceFlow.getName())) return;
     		buffer.append(" name=\"" + value + "\"");
     	}
 		protected void appendTo(StringBuffer buffer, SequenceFlow sequenceFlow) {
 			if (sequenceFlow.getTo() == null) return;
 			String value = sequenceFlow.getTo().getName();
 			if (value == null || "".equals(value)) return;
     		buffer.append(" to=\"" + value + "\"");
 		}
 		protected void appendGraphics(StringBuffer buffer, ConnectionWrapper wrapper) {
 	    	StringBuffer bendPointBuffer = new StringBuffer();
 	    	List<Point> bendPoints = wrapper.getBendpoints();
 	    	if (bendPoints != null && bendPoints.size() > 0) {
 	    		for (int i = 0; i < bendPoints.size(); i++) {
 	    			bendPointBuffer.append(bendPoints.get(i).x);
 	    			bendPointBuffer.append(",");
 	    			bendPointBuffer.append(bendPoints.get(i).y);
 	    			if (i < bendPoints.size() - 1) bendPointBuffer.append(";");
 	    		}	    		
 	    	}
 	    	StringBuffer labelBuffer = new StringBuffer();
 	    	LabelWrapper labelWrapper = wrapper.getLabel();
 	    	if (labelWrapper != null && !isEmpty(labelWrapper.getText())) {
 	    		Point location = labelWrapper.getLocation();
 	    		if (location != null) {
 	    			labelBuffer.append(location.x);
 	    			labelBuffer.append(',');
 	    			labelBuffer.append(location.y);
 	    		}
 	    	}	    	
 	    	if (bendPointBuffer.length() + labelBuffer.length() == 0) return;
 	    	buffer.append(" g=\"");
 	    	buffer.append(bendPointBuffer);
 	    	if (bendPointBuffer.length() > 0) buffer.append(':');
 	    	buffer.append(labelBuffer);
 	    	buffer.append("\"");
 		}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		String timer = (String)wrapper.getPropertyValue(SequenceFlow.TIMER);
     		if (timer != null && !("".equals(timer))) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<timer duedate=\"" + timer + "\"/>");
     		}
     		String outcome = (String)wrapper.getPropertyValue(SequenceFlow.OUTCOME_VALUE);
     		if (outcome != null && !("".equals(outcome))) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<outcome-value><string value=\"" + outcome + "\"/></outcome-value>");
     		}
     		List<Element> eventListeners = wrapper.getChildren("listener");
     		if (eventListeners != null) {
     			for (Element eventListener : eventListeners) {
     				if (!(eventListener instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)eventListener, level+1);
     			}
     		}
     	}
     }
     
     class ProcessNodeWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("name");
     		result.add("g");
     		return result;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		if (!(wrapper instanceof NodeWrapper)) return;
     		Element element = wrapper.getElement();
     		if (!(element instanceof ProcessNode)) return;
     		if ("name".equals(attributeName)) {
 				appendName(buffer, (ProcessNode)element);
 			} else if ("g".equals(attributeName)) {
 				appendGraphics(buffer, (NodeWrapper)wrapper);
     		} else {
     			super.appendAttributeToSave(attributeName, buffer, wrapper);
     		}
     	}
     	protected void appendName(StringBuffer buffer, ProcessNode processNode) {
 			String value = processNode.getName();
 			if (value == null || "".equals(value)) return;
     		buffer.append(" name=\"" + value + "\"");
      	}
     	protected void appendGraphics(StringBuffer buffer, NodeWrapper wrapper) {
         	Rectangle constraint = wrapper.getConstraint();
         	buffer.append(" g=\"");
         	buffer.append(constraint.x);
         	buffer.append(",");
         	buffer.append(constraint.y);
         	buffer.append(",");
         	buffer.append(constraint.width);
         	buffer.append(",");
         	buffer.append(constraint.height);
         	buffer.append("\"");
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
 	    	NodeWrapper nodeWrapper = (NodeWrapper)wrapper;
     		List<Element> eventListenerContainers = nodeWrapper.getChildren("eventListener");
     		if (eventListenerContainers != null) {
     			for (Element eventListenerContainer : eventListenerContainers) {
     				if (!(eventListenerContainer instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)eventListenerContainer, level+1);
     			}
     		}
 	    	List<ConnectionWrapper> children = nodeWrapper.getOutgoingConnections();
 	    	for (ConnectionWrapper connectionWrapper : children) {
 	    		appendToBuffer(buffer, connectionWrapper, level+1);
 	    	}
     	}
     }
     
     class ExclusiveGatewayWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add("expr");
     		result.add("lang");
     		return result;
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		ExclusiveGateway exclusiveGateway = (ExclusiveGateway)wrapper.getElement();
     		String handler = exclusiveGateway.getHandler();
     		if (handler != null && !"".equals(handler)) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<handler class=\"" + handler + "\" />");
    		} 
     		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class TerminateEndEventWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add("ends");
     		result.add("state");
     		return result;
     	}
     }
     
     class JavaTaskWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add("class");
     		result.add("method");
     		result.add("var");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("class".equals(attributeName)) {
     			return JavaTask.CLASS;
     		} else if ("method".equals(attributeName)) {
     			return JavaTask.METHOD;
     		} else if ("var".equals(attributeName)) {
     			return JavaTask.VAR;
     		}
     		return super.getPropertyName(attributeName);
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		List<Element> fields = wrapper.getChildren(JavaTask.FIELDS);
     		if (fields != null) {
 	    		for (Element field : fields) {
 	    			if (!(field instanceof Wrapper)) continue;
 	    			appendToBuffer(buffer, (Wrapper)field, level+1);
 	    		}
     		}
     		List<Element> arguments = wrapper.getChildren(JavaTask.ARGS);
     		if (arguments != null) {
     			for (Element argument : arguments) {
     				if (!(argument instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)argument, level+1);
     			}
     		}
     		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class ScriptTaskWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add("expr");
     		result.add("lang");
     		result.add("var");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("expr".equals(attributeName)) {
     			return ScriptTask.EXPR;
     		} else if ("lang".equals(attributeName)) {
     			return ScriptTask.LANG;
     		} else if ("var".equals(attributeName)) {
     			return ScriptTask.VAR;
     		}
     		return super.getPropertyName(attributeName);
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		String text = (String)wrapper.getPropertyValue(ScriptTask.TEXT);
     		if (text != null && !("".equals(text))) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<text>" + text + "</text>");
    		} 
    		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class HumanTaskWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add(Assignment.ASSIGNEE);
     		result.add(Assignment.CANDIDATE_GROUPS);
     		result.add(Assignment.SWIMLANE);
     		return result;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		if (!(wrapper instanceof NodeWrapper)) return;
     		Element element = wrapper.getElement();
     		if (!(element instanceof HumanTask)) return;
     		if (HumanTask.ASSIGNEE.equals(attributeName)) {
 				appendExpression(HumanTask.ASSIGNEE, buffer, wrapper);
 			} else if (HumanTask.CANDIDATE_GROUPS.equals(attributeName)) {
 				appendExpression(HumanTask.CANDIDATE_GROUPS, buffer, wrapper);
     		} else if (HumanTask.SWIMLANE.equals(attributeName)) {
     			appendExpression(HumanTask.SWIMLANE, buffer, wrapper);
     		} else {
     			super.appendAttributeToSave(attributeName, buffer, wrapper);
     		}
     	}
     	protected void appendExpression(String type, StringBuffer buffer, Wrapper wrapper) {
     		Object assignmentType = wrapper.getPropertyValue(HumanTask.ASSIGNMENT_TYPE);
     		if (!(assignmentType instanceof Integer)) return;
     		if (type.equals(HumanTask.ASSIGNMENT_TYPES[(Integer)assignmentType])) {
     			Object value = wrapper.getPropertyValue(HumanTask.ASSIGNMENT_EXPRESSION);
     			if (value == null || "".equals(value)) return;
     			buffer.append(" " + type + "=\"" + value + "\"");
     		}
      	}
     }
     
     class SwimlaneWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("name");
     		result.add(Assignment.ASSIGNEE);
     		result.add(Assignment.CANDIDATE_GROUPS);
     		result.add(Assignment.SWIMLANE);
     		return result;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		Element element = wrapper.getElement();
     		if (!(element instanceof Swimlane)) return;
     		if (Assignment.ASSIGNEE.equals(attributeName)) {
 				appendExpression(Assignment.ASSIGNEE, buffer, wrapper);
 			} else if (Assignment.CANDIDATE_GROUPS.equals(attributeName)) {
 				appendExpression(Assignment.CANDIDATE_GROUPS, buffer, wrapper);
     		} else if (Assignment.SWIMLANE.equals(attributeName)) {
     			appendExpression(Assignment.SWIMLANE, buffer, wrapper);
     		} else if ("name".equals(attributeName)){
     			appendName(buffer, wrapper);
     		}
     	}
     	protected void appendName(StringBuffer buffer, Wrapper wrapper) {
 			String value = (String)wrapper.getPropertyValue(IPropertyId.NAME);
 			if (value == null || "".equals(value)) return;
     		buffer.append(" name=\"" + value + "\"");
      	}
     	protected void appendExpression(String type, StringBuffer buffer, Wrapper wrapper) {
     		Object assignmentType = wrapper.getPropertyValue(HumanTask.ASSIGNMENT_TYPE);
     		if (!(assignmentType instanceof Integer)) return;
     		if (type.equals(HumanTask.ASSIGNMENT_TYPES[(Integer)assignmentType])) {
     			Object value = wrapper.getPropertyValue(HumanTask.ASSIGNMENT_EXPRESSION);
     			if (value == null || "".equals(value)) return;
     			buffer.append(" " + type + "=\"" + value + "\"");
     		}
      	}
     	public void appendOpening(StringBuffer buffer, Wrapper wrapper, int level) {
     		appendLeadingNodes(buffer, wrapper, level);
     		buffer.append("<swimlane");
     		appendAttributes(buffer, wrapper, level);
     	}
     }
     
     class TimerWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("duedate");
     		result.add("repeat");
     		result.add("duedatetime");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("duedate".equals(attributeName)) {
     			return Timer.DUE_DATE;
     		} else if ("repeat".equals(attributeName)) {
     			return Timer.REPEAT;
     		} else if ("duedatetime".equals(attributeName)) {
     			return Timer.DUE_DATETIME;
     		}
     		return super.getPropertyName(attributeName);
     	}
     }
     
     class ParameterWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("var");
     		result.add("subvar");
     		result.add("expr");
     		result.add("lang");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("var".equals(attributeName)) {
     			return Parameter.VAR;
     		} else if ("subvar".equals(attributeName)) {
     			return Parameter.SUBVAR;
     		} else if ("expr".equals(attributeName)) {
     			return Parameter.EXPR;
     		} else if ("lang".equals(attributeName)) {
     			return Parameter.LANG;
     		}
     		return super.getPropertyName(attributeName);
     	}
     }
     
     class ArgumentWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		return result;
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		String value = (String)wrapper.getPropertyValue(Argument.VALUE);
     		if (value != null && !("".equals(value))) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<string value=\"" + value + "\"/>");
     		}
     		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class FieldWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("name");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("name".equals(attributeName)) {
     			return Field.NAME;
     		}
     		return super.getPropertyName(attributeName);
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		String value = (String)wrapper.getPropertyValue(Field.VALUE);
     		if (value != null && !("".equals(value))) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<string value=\"" + value + "\"/>");
     		}
     		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class SubprocessTaskWrapperSerializer extends ProcessNodeWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		List<String> result = super.getAttributesToSave();
     		result.add("sub-process-id");
     		result.add("sub-process-key");
     		result.add("outcome");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("sub-process-id".equals(attributeName)) {
     			return SubprocessTask.ID;
     		} else if ("sub-process-key".equals(attributeName)) {
     			return SubprocessTask.KEY;
     		} else if ("outcome".equals(attributeName)) {
     			return SubprocessTask.OUTCOME;
     		}
     		return super.getPropertyName(attributeName);
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		List<Element> inputParameters = wrapper.getChildren(SubprocessTask.INPUT_PARAMETERS);
     		if (inputParameters != null) {
 	    		for (Element inputParameter : inputParameters) {
 	    			if (!(inputParameter instanceof Wrapper)) continue;
 	    			appendToBuffer(buffer, (Wrapper)inputParameter, level+1);
 	    		}
     		}
     		List<Element> outputParameters = wrapper.getChildren(SubprocessTask.OUTPUT_PARAMETERS);
     		if (outputParameters != null) {
     			for (Element outputParameter : outputParameters) {
     				if (!(outputParameter instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)outputParameter, level+1);
     			}
     		}
     		super.appendBody(buffer, wrapper, level);
     	}
     }
     
     class EventListenerContainerWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("event");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("event".equals(attributeName)) {
     			return EventListenerContainer.EVENT_TYPE;
     		}
     		return super.getPropertyName(attributeName);
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		EventListenerContainer eventListenerContainer = (EventListenerContainer)wrapper.getElement();
     		String timer = eventListenerContainer.getTimer();
     		if (timer != null && !"".equals(timer)) {
     			buffer.append("\n");
     			appendPadding(buffer, level + 1);
     			buffer.append("<timer duedate=\"" + timer + "\"/>");
     		}
 			List<Element> eventListeners = wrapper.getChildren(EventListenerContainer.LISTENERS);
 			if (eventListeners != null) {
 				for (Element eventListener : eventListeners) {
 					if (!(eventListener instanceof Wrapper)) continue;
 					appendToBuffer(buffer, (Wrapper)eventListener, level+1);
 				}
 			}
     	}
     }
     
     class EventListenerWrapperSerializer extends AbstractWrapperSerializer {
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("class");
     		return result;
     	}
     	protected String getPropertyName(String attributeName) {
     		if ("class".equals(attributeName)) {
     			return EventListener.CLASS_NAME;
     		}
     		return super.getPropertyName(attributeName);
     	}
     }
     
     class ProcessWrapperSerializer extends AbstractWrapperSerializer {
     	public void appendOpening(StringBuffer buffer, Wrapper wrapper, int level) {
     		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
     		buffer.append("<" + getNodeName(wrapper.getElement()));
     		appendAttributes(buffer, wrapper, level);
     	}
     	protected List<String> getAttributesToSave() {
     		ArrayList<String> result = new ArrayList<String>();
     		result.add("name");
     		result.add("initial");
     		result.add("key");
     		result.add("version");
     		result.add("description");
     		return result;
     	}
     	protected void appendAttributeToSave(String attributeName, StringBuffer buffer, Wrapper wrapper) {
     		if (!(wrapper instanceof FlowWrapper)) return;
     		Element element = wrapper.getElement();
     		if (element == null || !(element instanceof Process)) return;
     		if ("name".equals(attributeName)) {
 				appendName(buffer, (Process)element);
     		} else if ("key".equals(attributeName)) {
     			appendKey(buffer, (Process)element);
     		} else if ("version".equals(attributeName)) {
     			appendVersion(buffer, (Process)element);
     		} else if ("description".equals(attributeName)) {
     			appendDescription(buffer, (Process)element);
 			} else if ("initial".equals(attributeName)) {
 				appendInitial(buffer, (Process)element);
     		} else {
     			super.appendAttributeToSave(attributeName, buffer, wrapper);    	    		
 			} 
     	}
     	protected void appendName(StringBuffer buffer, Process process) {
 			String value = process.getName();
 			if (value == null || "".equals(value)) return;
 			buffer.append(" name=\"" + value + "\"");
      	}
     	protected void appendInitial(StringBuffer buffer, Process process) {
     		if (process.getInitial() == null) return;
 			String value = process.getInitial().getName();
 			if (value == null || "".equals(value)) return;
 			buffer.append(" initial=\"" + value + "\"");
     	}
     	protected void appendKey(StringBuffer buffer, Process process) {
     		if (process.getKey() == null) return;
     		String value = process.getKey();
     		if (value == null || "".equals(value)) return;
     		buffer.append(" key=\"" + value + "\"");
     	}
     	protected void appendVersion(StringBuffer buffer, Process process) {
     		if (process.getVersion() == null) return;
     		String value = process.getVersion();
     		if (value == null || "".equals(value)) return;
     		buffer.append(" version=\"" + value + "\"");
     	}
     	protected void appendDescription(StringBuffer buffer, Process process) {
     		if (process.getDescription() == null) return;
     		String value = process.getDescription();
     		if (value == null || "".equals(value)) return;
     		buffer.append(" description=\"" + value + "\"");
     	}
     	public void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     		FlowWrapper flowWrapper = (FlowWrapper)wrapper;
     		List<Element> swimlanes = flowWrapper.getChildren("swimlane");
     		if (swimlanes != null) {
 	    		for (Element swimlane : swimlanes) {
 	    			if (!(swimlane instanceof Wrapper)) continue;
 	    			appendToBuffer(buffer, (Wrapper)swimlane, level+1);
 	    		}
     		}
     		List<Element> timers = flowWrapper.getChildren("timer");
     		if (timers != null) {
     			for (Element timer : timers) {
     				if (!(timer instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)timer, level+1);
     			}
     		}
     		List<Element> eventListenerContainers = flowWrapper.getChildren("eventListener");
     		if (eventListenerContainers != null) {
     			for (Element eventListenerContainer : eventListenerContainers) {
     				if (!(eventListenerContainer instanceof Wrapper)) continue;
     				appendToBuffer(buffer, (Wrapper)eventListenerContainer, level+1);
     			}
     		}
 	    	ContainerWrapper containerWrapper = (ContainerWrapper)wrapper;
 	    	List<NodeWrapper> children = containerWrapper.getNodeWrappers();
 	    	for (NodeWrapper nodeWrapper : children) {
 	    		appendToBuffer(buffer, nodeWrapper, level+1);
 	    	}
     	}
     }
     
 	private void appendOpening(StringBuffer buffer, Wrapper wrapper, int level) {
     	Element element = (Element)wrapper.getElement();
     	if (element instanceof SequenceFlow) {
     		new SequenceFlowWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof TerminateEndEvent) {
     		new TerminateEndEventWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof ErrorEndEvent) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof CancelEndEvent) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof StartEvent) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof SuperState) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof WaitTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof HqlTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof SqlTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof JavaTask) {
     		new JavaTaskWrapperSerializer().appendOpening(buffer, wrapper, level);
        	} else if (element instanceof ScriptTask) {
     		new ScriptTaskWrapperSerializer().appendOpening(buffer, wrapper, level);
        	} else if (element instanceof MailTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof ServiceTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof HumanTask) {
     		new HumanTaskWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof SubprocessTask) {
     		new SubprocessTaskWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof CustomTask) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof ExclusiveGateway) {
     		new ExclusiveGatewayWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof ForkParallelGateway) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof JoinParallelGateway) {
     		new ProcessNodeWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Process) {
     		new ProcessWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Swimlane) {
     		new SwimlaneWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Timer) {
     		new TimerWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof EventListenerContainer) {
     		new EventListenerContainerWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof EventListener) {
     		new EventListenerWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Parameter) {
     		new ParameterWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Argument) {
     		new ArgumentWrapperSerializer().appendOpening(buffer, wrapper, level);
     	} else if (element instanceof Field) {
     		new FieldWrapperSerializer().appendOpening(buffer, wrapper, level);
     	}
     	
     }
     
     @SuppressWarnings("unchecked")
 	private void appendBody(StringBuffer buffer, Wrapper wrapper, int level) {
     	Element element = wrapper.getElement();
     	if (element instanceof SequenceFlow) {
     		new SequenceFlowWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof TerminateEndEvent) {
     		new TerminateEndEventWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof ErrorEndEvent) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof CancelEndEvent) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof StartEvent) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof SuperState) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof WaitTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof HqlTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof SqlTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof JavaTask) {
     		new JavaTaskWrapperSerializer().appendBody(buffer, wrapper, level);
        	} else if (element instanceof ScriptTask) {
     		new ScriptTaskWrapperSerializer().appendBody(buffer, wrapper, level);
        	} else if (element instanceof MailTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof ServiceTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof HumanTask) {
     		new HumanTaskWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof SubprocessTask) {
     		new SubprocessTaskWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof CustomTask) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof ExclusiveGateway) {
     		new ExclusiveGatewayWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof ForkParallelGateway) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof JoinParallelGateway) {
     		new ProcessNodeWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof Process) {
     		new ProcessWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof Swimlane) {
     		new SwimlaneWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof Timer) {
     		new TimerWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof EventListenerContainer) {
     		new EventListenerContainerWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof EventListener) {
     		new EventListenerWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof Argument) {
     		new ArgumentWrapperSerializer().appendBody(buffer, wrapper, level);
     	} else if (element instanceof Field) {
     		new FieldWrapperSerializer().appendBody(buffer, wrapper, level);
     	}
 		ArrayList<Node> trailingNodeList = (ArrayList<Node>)element.getMetaData("trailingNodes");
 		boolean appendTrailingNodes = trailingNodeList != null && !trailingNodeList.isEmpty();
 		if (appendTrailingNodes) {
 			appendNodeList(buffer, trailingNodeList);
 		} else if (buffer.length() > 0){
 			buffer.append("\n");
 			appendPadding(buffer, level);
 		}
 	}
 
 	private void appendPadding(StringBuffer buffer, int level) {
 		for (int i = 0; i < level; i++) {
 			buffer.append("   ");
 		}
 	}
 
 	private void appendClosing(StringBuffer buffer, Wrapper wrapper, int level) {
     	Element element = (Element)wrapper.getElement();
     	if (element instanceof SequenceFlow) {
     		buffer.append("</transition>");
     	} else if (element instanceof TerminateEndEvent) {
     		buffer.append("</end>");
     	} else if (element instanceof ErrorEndEvent) {
     		buffer.append("</end-error>");
     	} else if (element instanceof CancelEndEvent) {
     		buffer.append("</end-cancel>");
     	} else if (element instanceof StartEvent) {
     		buffer.append("</start>");
     	} else if (element instanceof SuperState) {
     		buffer.append("</super-state>");
     	} else if (element instanceof WaitTask) {
     		buffer.append("</state>");
     	} else if (element instanceof HqlTask) {
     		buffer.append("</hql>");
     	} else if (element instanceof SqlTask) {
     		buffer.append("</sql>");
     	} else if (element instanceof JavaTask) {
     		buffer.append("</java>");
     	} else if (element instanceof ScriptTask) {
     		buffer.append("</script>");
     	} else if (element instanceof MailTask) {
     		buffer.append("</mail>");
     	} else if (element instanceof ServiceTask) {
     		buffer.append("</esb>");
     	} else if (element instanceof HumanTask) {
     		buffer.append("</task>");
     	} else if (element instanceof SubprocessTask) {
     		buffer.append("</sub-process>");
     	} else if (element instanceof CustomTask) {
     		buffer.append("</custom>");
     	} else if (element instanceof ExclusiveGateway) {
     		buffer.append("</decision>");
     	} else if (element instanceof ForkParallelGateway) {
     		buffer.append("</fork>");
     	} else if (element instanceof JoinParallelGateway) {
     		buffer.append("</join>");
     	} else if (element instanceof Process) {
     		buffer.append("</process>");
     	} else if (element instanceof Swimlane) {
     		buffer.append("</swimlane>");
     	} else if (element instanceof Timer) {
     		buffer.append("</timer>");
     	} else if (element instanceof EventListenerContainer) {
     		buffer.append("</on>");
     	} else if (element instanceof EventListener) {
     		buffer.append("</event-listener>");
     	} else if (element instanceof InputParameter) {
     		buffer.append("</parameter-in>");
     	} else if (element instanceof OutputParameter) {
     		buffer.append("</parameter-out>");
     	} else if (element instanceof Argument) {
     		buffer.append("</arg>");
     	} else if (element instanceof Field) {
     		buffer.append("</field>");
     	}
     }
     
     private boolean isEmpty(String str) {
     	return str == null || "".equals(str);
     }
     
 }
