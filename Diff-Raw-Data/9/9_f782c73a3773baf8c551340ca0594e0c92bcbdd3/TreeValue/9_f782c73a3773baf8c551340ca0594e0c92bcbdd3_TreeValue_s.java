 package com.lolcode.tree;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/12/13
  * Time: 2:57 PM
  */
 
 /**
  * Defines lolcode value handling objects.
  */
public abstract class TreeValue extends TreeExpression implements TreeTypedValue {
 
     private TreeValue value; //
     private TYPE type;
 
     TreeValue() {
         type = TYPE.UNKNOWN;
         value = null;
     }
 
     public TreeValue getValue() {
         return value;
     }
 
     public void setValue(TreeValue value) {
         this.value = value;
     }
 
     @Override
     public void setType(TYPE type) {
         this.type = type;
     }
 
     @Override
     public TYPE getType() {
         return type;
     }
 
 
     //    @Override
 //    public void accept(BaseASTVisitor v) {
 //        v.visit(this);
 //    }
 }
