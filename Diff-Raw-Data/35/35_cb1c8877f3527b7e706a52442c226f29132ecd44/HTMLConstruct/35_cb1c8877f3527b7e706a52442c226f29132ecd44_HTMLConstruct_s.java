 
 package Entities;
 
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.html.HTML.Tag;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 
 public abstract class HTMLConstruct extends DefaultMutableTreeNode implements Element  {
     protected Document doc;
     protected HTMLConstruct parent;
    private Tag tag;
     
     public HTMLConstruct(javax.swing.text.Document doc, Tag tag,HTMLConstruct parent){
         super(tag);
         this.doc = doc;
         this.parent = parent;
         this.tag = tag;
     }
     
     public HTMLConstruct(javax.swing.text.Document doc, String text,HTMLConstruct parent){
         super(text,false);
         this.doc = doc;
         this.parent = parent;
         this.tag = Tag.CONTENT;
     }
     
     
     @Override
     public String getName() {
         // TODO Auto-generated method stub
         return tag.toString();
     }
     
     @Override
     public Document getDocument() {
         // TODO Auto-generated method stub
         return this.doc;
     }
 }
 
