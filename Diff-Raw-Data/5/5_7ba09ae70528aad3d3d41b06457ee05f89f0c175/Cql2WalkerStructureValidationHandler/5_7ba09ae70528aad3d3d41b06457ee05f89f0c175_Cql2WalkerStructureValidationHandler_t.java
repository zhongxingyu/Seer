 package gov.nih.nci.cagrid.data.cql2.validation.walker;
 
 import gov.nih.nci.cagrid.data.cql.validation.MalformedStructureException;
 
 import java.math.BigInteger;
 import java.util.Stack;
 
 import org.cagrid.cql2.AssociationPopulationSpecification;
 import org.cagrid.cql2.AttributeValue;
 import org.cagrid.cql2.CQLAssociatedObject;
 import org.cagrid.cql2.CQLAttribute;
 import org.cagrid.cql2.CQLExtension;
 import org.cagrid.cql2.CQLGroup;
 import org.cagrid.cql2.CQLQuery;
 import org.cagrid.cql2.CQLQueryModifier;
 import org.cagrid.cql2.CQLTargetObject;
 import org.cagrid.cql2.DistinctAttribute;
 import org.cagrid.cql2.NamedAssociation;
 import org.cagrid.cql2.NamedAssociationList;
 import org.cagrid.cql2.NamedAttribute;
 import org.cagrid.cql2.PopulationDepth;
 
 
 public class Cql2WalkerStructureValidationHandler extends Cql2WalkerHandlerAdapter {
 
     private Stack<BigInteger> childCount = null;
     private boolean processingModifier = false;
     private boolean expectAttributeValue = false;
     private boolean foundAttributeValue = false;
     
     public Cql2WalkerStructureValidationHandler() {
         this.childCount = new Stack<BigInteger>();
     }
     
     
     private void incrementCurrentChildCount() {
         BigInteger current = childCount.peek();
         childCount.set(childCount.size() - 1, current.add(BigInteger.ONE));
     }
 
 
     public void endAssociation(CQLAssociatedObject assoc) throws MalformedStructureException {
         BigInteger count = childCount.pop();
         if (count.intValue() > 1) {
             throw new MalformedStructureException("CQL Objects may have at most one child (found " 
                 + count.intValue() + ")");
         }
     }
 
 
     public void endAssociationPopulation(AssociationPopulationSpecification pop) throws MalformedStructureException {
         // TODO Auto-generated method stub
 
     }
 
 
     public void endAttribute(CQLAttribute attrib) throws MalformedStructureException {
         if (expectAttributeValue && !foundAttributeValue) {
             throw new MalformedStructureException("Expected to find attribute value, did not");
         }
     }
 
 
     public void endAttributeValue(AttributeValue val) throws MalformedStructureException {
         
     }
 
 
     public void endDistinctAttribute(DistinctAttribute distinct) throws MalformedStructureException {
         
     }
 
 
     public void endExtension(CQLExtension ext) throws MalformedStructureException {
         
     }
 
 
     public void endGroup(CQLGroup group) throws MalformedStructureException {
         BigInteger count = childCount.pop();
         if (count.intValue() < 2) {
             throw new MalformedStructureException("CQL Groups must have two or more children (found "
                 + count.intValue() + ")");
         }
     }
 
 
     public void endNamedAssociation(NamedAssociation assoc) throws MalformedStructureException {
         
     }
 
 
     public void endNamedAssociationList(NamedAssociationList list) throws MalformedStructureException {
         
     }
 
 
     public void endNamedAttribute(NamedAttribute named) throws MalformedStructureException {
         
     }
 
 
     public void endPopulationDepth(PopulationDepth depth) throws MalformedStructureException {
         
     }
 
 
     public void endQuery(CQLQuery query) throws MalformedStructureException {
         
     }
 
 
     public void endQueryModifier(CQLQueryModifier mods) throws MalformedStructureException {
         processingModifier = false;
     }
 
 
     public void endTargetObject(CQLTargetObject obj) throws MalformedStructureException {
         BigInteger count = childCount.pop();
         if (count.intValue() > 1) {
             throw new MalformedStructureException("CQL Objects may have at most one child (found "
                 + count.intValue() + ")");
         }
     }
 
 
     public void startAssociation(CQLAssociatedObject assoc) throws MalformedStructureException {
         incrementCurrentChildCount();
         childCount.push(BigInteger.valueOf(0));
     }
 
 
     public void startAssociationPopulation(AssociationPopulationSpecification pop) throws MalformedStructureException {
        if (pop.getNamedAssociationList() == null && pop.getPopulationDepth() == null) {
             throw new MalformedStructureException(
                 "Association Population spec must have either a named association list or a population depth.  Found none");
         }
        if (pop.getNamedAssociationList() != null && pop.getPopulationDepth() != null) {
             throw new MalformedStructureException(
                 "Association Population spec must have either a named association list or a population depth.  Found both");
         }
     }
 
 
     public void startAttribute(CQLAttribute attrib) throws MalformedStructureException {
         incrementCurrentChildCount();
         if (attrib.getBinaryPredicate() == null && attrib.getUnaryPredicate() == null) {
             throw new MalformedStructureException(
                 "Attributes must have either a binary or unary predicate, found none");
         }
         if (attrib.getBinaryPredicate() != null && attrib.getUnaryPredicate() != null) {
             throw new MalformedStructureException(
                 "Attributes must have either a binary or unary predicate, found both");
         }
         expectAttributeValue = attrib.getBinaryPredicate() != null;
     }
 
 
     public void startAttributeValue(AttributeValue val) throws MalformedStructureException {
         foundAttributeValue = true;
         int populatedValues = 0;
         populatedValues += val.getIntegerValue() != null ? 1 : 0;
         populatedValues += val.getBooleanValue() != null ? 1 : 0;
         populatedValues += val.getDateValue() != null ? 1 : 0;
         populatedValues += val.getDoubleValue() != null ? 1 : 0;
         populatedValues += val.getLongValue() != null ? 1 : 0;
         populatedValues += val.getStringValue() != null ? 1 : 0;
         populatedValues += val.getTimeValue() != null ? 1 : 0;
         populatedValues += val.getFloatValue() != null ? 1 : 0;
         if (populatedValues != 1) {
             throw new MalformedStructureException("Expected to find one populated attribute value, found " + populatedValues);
         }
     }
 
 
     public void startDistinctAttribute(DistinctAttribute distinct) throws MalformedStructureException {
         if (distinct.getAttributeName() == null) {
             throw new MalformedStructureException("Distinct Attributes must have an attribute name");
         }
     }
 
 
     public void startExtension(CQLExtension ext) throws MalformedStructureException {
         if (!processingModifier) {
             childCount.peek().add(BigInteger.ONE);
         }
     }
 
 
     public void startGroup(CQLGroup group) throws MalformedStructureException {
         incrementCurrentChildCount();
         childCount.push(BigInteger.valueOf(0));
     }
 
 
     public void startNamedAssociation(NamedAssociation assoc) throws MalformedStructureException {
         if (assoc.getNamedAssociationList() != null && assoc.getPopulationDepth() != null) {
             throw new MalformedStructureException(
                 "Named associations may have either a named association list or a population depth.  Found both");
         }
     }
 
 
     public void startNamedAssociationList(NamedAssociationList list) throws MalformedStructureException {
         if (list.getNamedAssociation() == null || list.getNamedAssociation().length == 0) {
             throw new MalformedStructureException("Named association list must have 1 or more named associations");
         }
     }
 
 
     public void startNamedAttribute(NamedAttribute named) throws MalformedStructureException {
         if (named.getAttributeName() == null) {
             throw new MalformedStructureException("Named Attribute must have an attribute name");
         }
     }
 
 
     public void startPopulationDepth(PopulationDepth depth) throws MalformedStructureException {
         // TODO Auto-generated method stub
     }
 
 
     public void startQuery(CQLQuery query) throws MalformedStructureException {
         // reset state variables
         childCount.clear();
         processingModifier = false;
     }
 
 
     public void startQueryModifier(CQLQueryModifier mods) throws MalformedStructureException {
         processingModifier = true;
     }
 
 
     public void startTargetObject(CQLTargetObject obj) throws MalformedStructureException {
         childCount.push(BigInteger.valueOf(0));
     }
 }
