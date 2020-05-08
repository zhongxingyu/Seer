 /* ListboxSelectAgentBuilder.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.logging.Logger;
 
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.ConversationException;
 import org.zkoss.zats.mimic.operation.SelectAgent;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listitem;
 
 public class ListboxSelectAgentBuilder implements OperationAgentBuilder<SelectAgent> {
 	private static Logger logger = Logger.getLogger(ListboxSelectAgentBuilder.class.getName());
 	
 	public SelectAgent getOperation(final ComponentAgent target) {
 		return new SelectAgentImpl(target);
 	}
 	class SelectAgentImpl extends AgentDelegator implements SelectAgent{
 		public SelectAgentImpl(ComponentAgent target) {
 			super(target);
 		}
 
 		public void select(int index) {
 			if (target.is(Listbox.class)){
 				Listitem selected = target.as(Listbox.class).getItemAtIndex(index);
 				if (selected==null){
					AuUtility.postOnSelect(target, selected.getUuid());
				}else{
 					logger.warning("select index:"+index+" is null.");
 				}
 			}else{
 				throw new ConversationException(target+" can't select");
 			}
 		}
 	}
 }
