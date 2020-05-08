 /*
  * DCQLGenerator.java
  *
  * Created on July 27, 2006, 10:32 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.duke.cabig.catrip.gui.query;
 
 import caBIG.caGrid.x10.govNihNciCagridDcql.Association;
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.common.ForeignAssociationBean;
 import edu.duke.cabig.catrip.gui.dnd.ClassNode;
 import java.util.ArrayList;
 
 // DCQL XML imports..
 import caBIG.caGrid.x10.govNihNciCagridDcql.DCQLQueryDocument;
 import caBIG.caGrid.x10.govNihNciCagridDcql.DCQLQueryDocument.DCQLQuery;
 import caBIG.caGrid.x10.govNihNciCagridDcql.ForeignAssociation;
 import caBIG.caGrid.x10.govNihNciCagridDcql.Group;
 import caBIG.caGrid.x10.govNihNciCagridDcql.Join;
 import caBIG.caGrid.x10.govNihNciCagridDcql.JoinCondition;
 import caBIG.caGrid.x10.govNihNciCagridDcql.TargetObject;
 import caBIG.cql.x1.govNihNciCagridCQLQuery.Attribute;
 import caBIG.cql.x1.govNihNciCagridCQLQuery.LogicalOperator;
 import edu.duke.cabig.catrip.gui.common.AttributeBean;
 import org.apache.xmlbeans.XmlOptions;
 // DCQL XML imports..
 
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class DCQLGenerator {
     
     /** Creates a new instance of DCQLGenerator */
     public DCQLGenerator() {
     }
     
     public static DCQLQueryDocument getDCQLDocument(){
         DCQLQueryDocument dc = null;
         try{
             
             ClassNode targetNode = DCQLRegistry.getTargetNode();
             ClassBean targetObject= targetNode.getAssociatedClassObject();
             
             dc = DCQLQueryDocument.Factory.newInstance();
             DCQLQuery dcql = DCQLQuery.Factory.newInstance();
             TargetObject to = TargetObject.Factory.newInstance();
             to.setName(targetObject.getFullyQualifiedName());
             to.setServiceURL(targetObject.getServiceUrl());
             
             buildAssociationGroup(to, targetObject);
             
             dcql.setTargetObject(to);
             dc.setDCQLQuery(dcql);
             
         } catch (Exception e){
             e.printStackTrace();
         }
         return dc;
     }
     
     
     public static String getDCQLText(){
         String txt = "";
         txt = getDCQLDocument().xmlText();
         return txt;
     }
     
     public static String getDCQLText(XmlOptions xmlOptions){
         String txt = "";
         txt = getDCQLDocument().xmlText(xmlOptions);
         return txt;
     }
     
     
     // TODO - add notNull/Null predicates also into this..
     private static void buildAssociationGroup(caBIG.caGrid.x10.govNihNciCagridDcql.Object outerObject, ClassBean outerObjectBean){//Association
         
         boolean targetHasAtts = outerObjectBean.hasNotNullAttributes();
         boolean targetHasAss = outerObjectBean.hasAssociations();
         boolean targetHasFass = outerObjectBean.hasForeignAssociations();
         
         Group gp1 = null;
         if (targetHasAtts && !(targetHasAss || targetHasFass)){
             // <editor-fold>   // only attributes are there
             
             ArrayList targetObjAttributes = outerObjectBean.getNonNullAttributes();
             // if more than one attribute.. create an internal group...
             if (targetObjAttributes.size() > 1){
                 // has multiple attcibutes..
                 gp1 = outerObject.addNewGroup();
                 gp1.setLogicRelation(LogicalOperator.AND);
                 
                 createAttributesGroup(gp1, targetObjAttributes);
                 
             }else {
                 // has only 1 attribute
                 Attribute oneAtt = outerObject.addNewAttribute();
                 AttributeBean aBean = (AttributeBean)targetObjAttributes.get(0);
                 oneAtt.setName(aBean.getAttributeName());
                 oneAtt.setPredicate(caBIG.cql.x1.govNihNciCagridCQLQuery.Predicate.Enum.forString(aBean.getPredicate()));
                 boolean likePredicate = aBean.getPredicate().equalsIgnoreCase("LIKE");
                 String attributeValue = aBean.getAttributeValue();
                 boolean hasChar = attributeValue.endsWith("%");
                 if (likePredicate && !hasChar){
                     oneAtt.setValue(aBean.getAttributeValue()+"%");
                 } else {
                     oneAtt.setValue(aBean.getAttributeValue());
                 }
             }
             // </editor-fold>  // only attributes are there
             
         } else if (targetHasAtts && (targetHasAss || targetHasFass)){
             // <editor-fold>   // attriibutes and associations both are there
             ArrayList targetObjAttributes = outerObjectBean.getNonNullAttributes();
             
             gp1 = outerObject.addNewGroup();
             gp1.setLogicRelation(LogicalOperator.AND);
             if (targetObjAttributes.size() > 1){
                 
                 // has multiple attributes.. create an internal group...
                 Group gp2 = gp1.addNewGroup();
                 gp2.setLogicRelation(LogicalOperator.AND);
                 
                 createAttributesGroup(gp2, targetObjAttributes);
                 
                 createAssociations(gp1, outerObjectBean);
                 
             }else {
                 // has only 1 attribute
                 Attribute oneAtt = gp1.addNewAttribute();
                 AttributeBean aBean = (AttributeBean)targetObjAttributes.get(0);
                 oneAtt.setName(aBean.getAttributeName());
                 oneAtt.setPredicate(caBIG.cql.x1.govNihNciCagridCQLQuery.Predicate.Enum.forString(aBean.getPredicate()));
                 boolean likePredicate = aBean.getPredicate().equalsIgnoreCase("LIKE");
                 String attributeValue = aBean.getAttributeValue();
                 boolean hasChar = attributeValue.endsWith("%");
                 if (likePredicate && !hasChar){
                     oneAtt.setValue(aBean.getAttributeValue()+"%");
                 } else {
                     oneAtt.setValue(aBean.getAttributeValue());
                 }
                 
                 createAssociations(gp1, outerObjectBean);
                 
             }
             // </editor-fold>   // attriibutes and associations both are there
             
             
         }else if (!targetHasAtts && (targetHasAss || targetHasFass)){
             ArrayList targetObjAttributes = outerObjectBean.getNonNullAttributes();
             int numAss = outerObjectBean.getAssociations().size();
             
             if (numAss>1){
                 gp1 = outerObject.addNewGroup();
                 gp1.setLogicRelation(LogicalOperator.AND);
                 createAssociations(gp1, outerObjectBean);
             }else{
                 createAssociations(outerObject,outerObjectBean );
             }
             
             
         }
         
     }
     
     
     private static void createAttributesGroup(Group gp2, ArrayList targetObjAttributes){
         Attribute[] targetAtts = new Attribute[targetObjAttributes.size()];
         for (int i = 0; i < targetObjAttributes.size(); i++) {
             AttributeBean aBean = (AttributeBean)targetObjAttributes.get(i);
             targetAtts[i] = Attribute.Factory.newInstance();
             targetAtts[i].setName(aBean.getAttributeName());
             targetAtts[i].setPredicate(caBIG.cql.x1.govNihNciCagridCQLQuery.Predicate.Enum.forString(aBean.getPredicate()));
             boolean likePredicate = aBean.getPredicate().equalsIgnoreCase("LIKE");
             String attributeValue = aBean.getAttributeValue();
             boolean hasChar = attributeValue.endsWith("%");
             if (likePredicate && !hasChar){
                 targetAtts[i].setValue(aBean.getAttributeValue()+"%");
             } else {
                 targetAtts[i].setValue(aBean.getAttributeValue());
             }
             
         }
         gp2.setAttributeArray(targetAtts);
     }
     
     
     private static void createAssociations(Group outerObject, ClassBean outerObjectBean){
         
         boolean targetHasAss = outerObjectBean.hasAssociations();
         boolean targetHasFass = outerObjectBean.hasForeignAssociations();
         
         Group gp1 = outerObject;
         
         if(targetHasAss){
             //- iterate the local associations... recursively.. and create the DCQL.
             ArrayList associationList = outerObjectBean.getAssociations();
             for (int i = 0;i<associationList.size() ;i++){
                 
                 Association ass = gp1.addNewAssociation(); // adding a local association..
                 ClassBean localAss = (ClassBean)associationList.get(i);
                 ass.setName(localAss.getFullyQualifiedName());
                 ass.setRoleName( outerObjectBean.getAssociationRoleName(localAss.getId()) );
 //                ass.setRoleName("localAssociationRoleName");
                 
                 buildAssociationGroup(ass, localAss);
                 
             }
         }
         
         if(targetHasFass){
             //- iterate the foreign associations... recursively.. and create the DCQL.
             ArrayList foreignAssociationList = outerObjectBean.getForeignAssociations();
             for (int i = 0;i<foreignAssociationList.size() ;i++){
                 
                 ClassBean foreignLeft = ((ForeignAssociationBean)foreignAssociationList.get(i)).getLeftObj();
                 ClassBean foreignRight = ((ForeignAssociationBean)foreignAssociationList.get(i)).getRighObj();
                 String leftProp = ((ForeignAssociationBean)foreignAssociationList.get(i)).getLeftProperty();
                String rightProp = ((ForeignAssociationBean)foreignAssociationList.get(i)).getRightProperty();
                 
                 ForeignAssociation forAss = gp1.addNewForeignAssociation(); // adding a foreign association..
                 
                 TargetObject fo = forAss.addNewForeignObject();//TargetObject.Factory.newInstance(); // foreign object  //foreignRight
                 fo.setName(foreignRight.getFullyQualifiedName());
                 fo.setServiceURL(foreignRight.getServiceUrl());
                 
                 
                 JoinCondition jc = JoinCondition.Factory.newInstance();
                 Join leftJ = Join.Factory.newInstance();
                 leftJ.setObject(foreignLeft.getFullyQualifiedName());
                 leftJ.setProperty(leftProp);
                 Join rightJ = Join.Factory.newInstance();
                 rightJ.setObject(foreignRight.getFullyQualifiedName());
                 rightJ.setProperty(rightProp);
                 jc.setLeftJoin(leftJ);jc.setRightJoin(rightJ);
                 forAss.setJoinCondition(jc);
                 
                 buildAssociationGroup(fo, foreignRight);
                 
             }
             
         }
     }
     
     
     
     private static void createAssociations(caBIG.caGrid.x10.govNihNciCagridDcql.Object outerObject, ClassBean outerObjectBean){
         
         boolean targetHasAss = outerObjectBean.hasAssociations();
         boolean targetHasFass = outerObjectBean.hasForeignAssociations();
         
         caBIG.caGrid.x10.govNihNciCagridDcql.Object gp1 = outerObject;
         
         if(targetHasAss){
             //- iterate the local associations... recursively.. and create the DCQL.
             ArrayList associationList = outerObjectBean.getAssociations();
             for (int i = 0;i<associationList.size() ;i++){
                 
                 Association ass = gp1.addNewAssociation(); // adding a local association..
                 ClassBean localAss = (ClassBean)associationList.get(i);
                 ass.setName(localAss.getFullyQualifiedName());
                 ass.setRoleName( outerObjectBean.getAssociationRoleName(localAss.getId()) );
 //                ass.setRoleName("localAssociationRoleName");
                 
                 buildAssociationGroup(ass, localAss);
                 
             }
         }
         
         if(targetHasFass){
             //- iterate the foreign associations... recursively.. and create the DCQL.
             ArrayList foreignAssociationList = outerObjectBean.getForeignAssociations();
             for (int i = 0;i<foreignAssociationList.size() ;i++){
                 
                 ClassBean foreignLeft = ((ForeignAssociationBean)foreignAssociationList.get(i)).getLeftObj();
                 ClassBean foreignRight = ((ForeignAssociationBean)foreignAssociationList.get(i)).getRighObj();
                 String leftProp = ((ForeignAssociationBean)foreignAssociationList.get(i)).getLeftProperty();
                String rightProp = ((ForeignAssociationBean)foreignAssociationList.get(i)).getRightProperty();
                 
                 ForeignAssociation forAss = gp1.addNewForeignAssociation(); // adding a foreign association..
                 
                 TargetObject fo = forAss.addNewForeignObject();//TargetObject.Factory.newInstance(); // foreign object  //foreignRight
                 fo.setName(foreignRight.getFullyQualifiedName());
                 fo.setServiceURL(foreignRight.getServiceUrl());
                 
                 
                 JoinCondition jc = JoinCondition.Factory.newInstance();
                 Join leftJ = Join.Factory.newInstance();
                 leftJ.setObject(foreignLeft.getFullyQualifiedName());
                 leftJ.setProperty(leftProp);
                 Join rightJ = Join.Factory.newInstance();
                 rightJ.setObject(foreignRight.getFullyQualifiedName());
                 rightJ.setProperty(rightProp);
                 jc.setLeftJoin(leftJ);jc.setRightJoin(rightJ);
                 forAss.setJoinCondition(jc);
                 
                 buildAssociationGroup(fo, foreignRight);
                 
             }
             
         }
     }
     
 }
