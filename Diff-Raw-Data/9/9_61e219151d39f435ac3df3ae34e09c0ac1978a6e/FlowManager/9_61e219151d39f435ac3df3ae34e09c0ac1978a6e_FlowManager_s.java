 /**
  * 
  */
 package org.cotrix.web.importwizard.client.flow;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.cotrix.web.importwizard.client.flow.FlowUpdatedEvent.FlowUpdatedHandler;
 import org.cotrix.web.importwizard.client.flow.FlowUpdatedEvent.HasFlowUpdatedHandlers;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class FlowManager<T> implements FlowUpdatedHandler, HasFlowUpdatedHandlers {
 	
 	protected FlowNode<T> flowRoot;
 	protected FlowNode<T> currentNode;
 	protected Stack<FlowNode<T>> stack;
 	protected HandlerManager handlerManager;
 	
 	public FlowManager()
 	{
 		this.stack = new Stack<FlowNode<T>>();
 		handlerManager = new HandlerManager(this);
 	}
 
 	/**
 	 * @param flowRoot the flowRoot to set
 	 */
 	public void setFlowRoot(FlowNode<T> flowRoot) {
 		this.flowRoot = flowRoot;
 		this.currentNode = flowRoot;
 	}
 
 	public T getCurrentItem() {
 		return currentNode.getItem();
 	}
 	
 	public boolean isFirst() {
 		return stack.size()==0;
 	}
 	
 	public boolean isLast() {
 		return currentNode.getNext()==null;
 	}
 	
 	public void goNext() {
 		if (currentNode.getNext() == null) throw new IllegalStateException("Flow end reached");
 		stack.push(currentNode);
 		currentNode = currentNode.getNext();
 	}
 	
 	public void goBack() {
 		if (stack.isEmpty()) throw new IllegalStateException("Flow start reached");
 		currentNode = stack.pop();
 	}
 	
 	public List<T> getCurrentFlow()
 	{
 		List<T> flow = new ArrayList<T>();
 		FlowNode<T> node = flowRoot;
		while(node.getNext()!=null) {
 			flow.add(node.getItem());
 			node = node.getNext();
 		}
 		return flow;
 	}
 
 	@Override
 	public void fireEvent(GwtEvent<?> event) {
 		handlerManager.fireEvent(event);
 	}
 
 	@Override
 	public HandlerRegistration addFlowUpdatedHandler(FlowUpdatedHandler handler) {
 		return handlerManager.addHandler(FlowUpdatedEvent.TYPE, handler);
 	}
 
 	@Override
 	public void onFlowUpdated(FlowUpdatedEvent event) {
 		Log.trace("FlowManage flow updated");
 		fireEvent(event);		
 	}
 }
