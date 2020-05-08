 package com.sun.identity.admin.model;
 
 import com.icesoft.faces.context.effects.Appear;
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.entitlement.EntitlementSubject;
 
public abstract class ViewSubject implements MultiPanelBean, TreeNode {
     private boolean panelExpanded = true;
     private Effect panelExpandEffect;
     private Effect panelEffect;
     private boolean panelVisible = false;
     private SubjectType subjectType;
     private String name;
 
     public ViewSubject() {
         panelEffect = new Appear();
         panelEffect.setSubmit(true);
         panelEffect.setTransitory(false);
     }
 
     public abstract EntitlementSubject getEntitlementSubject();
 
     public boolean isPanelExpanded() {
         return panelExpanded;
     }
 
     public void setPanelExpanded(boolean panelExpanded) {
         this.panelExpanded = panelExpanded;
     }
 
     public Effect getPanelExpandEffect() {
         return panelExpandEffect;
     }
 
     public void setPanelExpandEffect(Effect panelExpandEffect) {
         this.panelExpandEffect = panelExpandEffect;
     }
 
     public Effect getPanelEffect() {
         return panelEffect;
     }
 
     public void setPanelEffect(Effect panelEffect) {
         this.panelEffect = panelEffect;
     }
 
     public boolean isPanelVisible() {
         return panelVisible;
     }
 
     public void setPanelVisible(boolean panelVisible) {
         if (!this.panelVisible) {
             this.panelVisible = panelVisible;
         }
     }
 
     public SubjectType getSubjectType() {
         return subjectType;
     }
 
     public void setSubjectType(SubjectType subjectType) {
         this.subjectType = subjectType;
     }
 
     public String getName() {
         return name != null ? name : subjectType.getName();
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTitle() {
         return getName();
     }
 
     public String getToString() {
         return toString();
     }
 
     public String getToFormattedString() {
         return toString();
     }
 
     String getToFormattedString(int i) {
         return getIndentString(i) + toString();
     }
 
     public int getSize() {
         Tree t = new Tree(this);
         return t.size();
     }
 
     public int getSizeLeafs() {
         Tree t = new Tree(this);
         return t.sizeLeafs();
     }
 
 
     String getIndentString(int i) {
         String indent = "";
         for (int j = 0; j < i; j++) {
             indent += " ";
         }
 
         return indent;
     }
 
     @Override
     public String toString() {
         return subjectType.getTitle() + ":" + getTitle();
     }
 }
