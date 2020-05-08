 package org.nohope.typetools.node;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 11/15/12 5:37 PM
  */
 public class NotNode extends Node {
     private static final long serialVersionUID = 1L;
     private final Node child;
 
     public NotNode(final Node child) {
         this.child = child;
     }
 
    public<R> R getValue(final Interceptor<? extends Node, R> interceptor) {
         return child.evaluate(interceptor);
     }
 }
