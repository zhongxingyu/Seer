 package org.jackie.jvm.spi;
 
 import org.jackie.jvm.JNode;
 
 /**
  * @author Patrik Beno
  */
public abstract class AbstractJNode implements JNode {
 
 	protected JNode owner;
 
 	public AbstractJNode(JNode owner) {
 		this.owner = owner;
 	}
 
 	public JNode owner() {
 		return owner;
 	}
}
