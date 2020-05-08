 package org.eclipse.graphiti.func;
 
 public class Proposal implements IProposal {
 
 	private String text;
 
 	private Object object;
 
 	public Proposal(String text) {
 		super();
 		this.text = text;
 	}
 
 	public Proposal(String text, Object object) {
 		this(text);
 		setObject(object);
 	}
 
 	@Override
 	public Object getObject() {
 		return object;
 	}
 
 	@Override
 	public String getText() {
 		return text;
 	}
 
 	public void setObject(Object object) {
 		this.object = object;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
	
 	public static Proposal[] textToProposals(String[] possibleValues) {
 		Proposal[] ret = new Proposal[possibleValues.length];
 		for (int i = 0; i < ret.length; i++) {
			ret[i].setText(possibleValues[i]);
 		}
 		return ret;
 	}
 

 }
