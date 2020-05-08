 package net.sf.beezle.jasmin.scss;
 
 public class SimpleSelector {
     private BaseSelector[] bases;
 
    public SimpleSelector(ElementName first, BaseSelector[] bases) {
        this.bases = new BaseSelector[bases.length + 1];
        this.bases[0] = first;
        System.arraycopy(bases, 0, this.bases, 1, bases.length);
     }
 }
