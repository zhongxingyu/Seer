 /**
  * Copyright (c) 2011. Physion Consulting LLC
  * All rights reserved.
  */
 package com.physion.ebuilder.translator;
 
 import com.physion.ebuilder.datamodel.DataModel;
 import com.physion.ebuilder.datamodel.RowData;
 import com.physion.ebuilder.datatypes.*;
 import com.physion.ebuilder.expression.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * This class is used to translate an ExpressionTree into a RowData.
  *
  * The ExpressionTree object is the class that most closely
  * maps to PQL.  It is the format that the "other" code in
  * the system understands.  The RowData object is the class
  * that represents the GUI's "view" of the data.
  *
  * The only method you will probably need to use is the
  * translate() method.
  *
  * @see RowDataToExpressionTree
  */
 public class ExpressionTreeToRowData
     implements Translator {
 
     /**
      * This method turns the passed in ExpressionTree into a
      * RowData object.
      *
      * To turn a RowData into an ExpressionTree, use
      * the RowDataToExpressionTree.translate() class and method.
      *
      * @return translated RowData
      * @param expressionTree ExpressionTree to translate
      */
     public static RowData translate(ExpressionTree expressionTree) {
 
         /**
          * First create the root RowData object.
          * It simply contains the CUQ (Class Under Qualification)
          * and a CollectionOperator.
          *
          * Note that mapping an Expression collection operator into
          * a RowData CollectionOperator, is not always one-to-one.
          * These are one-to-one:
          *
          *      "or" -> CollectionOperator.ANY
          *      "and" -> CollectionOperator.ALL
          *
          * but the RowData CollectionOperator.NONE is represented by
          * the Expression tree having a "not" OperatorExpression with
          * an "or" OperatorExpression as its only operand:
          *
          *      "not(or)" -> CollectionOperator.NONE
          */
         RowData rootRow = RowData.createRootRow();
         String name = expressionTree.getClassUnderQualification();
         ClassDescription cuq = DataModel.getClassDescription(name);
         rootRow.setClassUnderQualification(cuq);
 
         IOperatorExpression oe = (IOperatorExpression)expressionTree.
             getRootExpression();
 
         rootRow.setCollectionOperator(getCOForOE(oe));
 
         /**
          * Convert the oe into a list of child RowData objects and
          * add them to the rootRow.
          */
 
         if (OE_NOT.equals(oe.getOperatorName())) {
             if ((oe.getOperandList().size() < 1) ||
                 (!(oe.getOperandList().get(0) instanceof
                    IOperatorExpression))) {
 
                 String s = "IOperatorExpression(not) without an operand "+
                     "that is an IOperatorExpression.  A \"not\" operator "+
                     "at the root of an expression tree should have a single "+
                     "operand that is an IOperatorExpression of with an "+
                     "operator name of:  \"or\" or \"and\".";
                 throw(new IllegalArgumentException(s));
             }
 
             oe = (IOperatorExpression)oe.getOperandList().get(0);
         }
 
         List<IExpression> operandList = oe.getOperandList();
         for (IExpression ex : operandList) {
             if (!(ex instanceof IOperatorExpression)) {
                 String s = "Root IOperatorExpression("+oe.getOperatorName()+
                     ") had an operand that was not an IOperatorExpression().";
                 throw(new IllegalArgumentException(s));
             }
 
             createAndAddChildRows(rootRow, (IOperatorExpression)ex, cuq);
         }
 
         return(rootRow);
     }
 
 
     /**
      * Create the list of child RowData objects that describe the passed
      * in expression tree.  This method calls itself recursively to
      * generate the sub-tree of RowData objects.
      *
      * This method should NOT be called with the OperatorExpression
      * that is the root operator (or operators) of the Expression tree.
      * That operator(s) must be handled a bit differently to generate
      * the root RowData object.  See getFirstChildOE() and the method
      * that calls it for more information about generating the root
      * RowData object in the GUI.
      *
      * This method never returns null, but might return an empty list.
      *
      * @param classDescription The ClassDescription that is the "parent"
      * class for all the child rows that will be created.  So, if we
      * are creating child rows for the topmost row in the GUI, this
      * would be the Class Under Qualification.
      *
      * @return child rows
      * @param oe operator expression
      */
     private static List<RowData> createChildRows(IOperatorExpression oe,
         ClassDescription classDescription) {
 
         /*
         System.out.println("\nEnter createChildRows()");
         System.out.println("oe: "+(Expression)oe);
         System.out.println("classDescription: "+classDescription);
         */
 
         List<RowData> childRows = new ArrayList<RowData>();
 
         /**
          * If the oe is null, return an empty list.
          */
         if (oe == null)
             return(childRows);
 
         List<IExpression> ol = oe.getOperandList();
 
         RowData rowData = new RowData();
 
         CollectionOperator collectionOperator = getCOForOE(oe);
         //System.out.println("collectionOperator = "+collectionOperator);
 
         /**
          * Handle the special case of the Count collection operator.
          */
         if (collectionOperator == null) {
             if (oe.getOperandList().size() > 0) {
                 IExpression ex = oe.getOperandList().get(0);
                 if (ex instanceof IOperatorExpression) {
                     IOperatorExpression oe2 = (IOperatorExpression)ex;
                     if (OE_COUNT.equals(oe2.getOperatorName())) {
                         collectionOperator = getCOForOE(oe2);
                     }
                 }
             }
         }
 
         if (collectionOperator == CollectionOperator.NONE) {
             /**
              * We need to "skip ahead" one operator because
              * the NONE collection operator is implemented in
              * the Expression syntax as two OperatorExpressions:
              *
              *      OperatorExpression(not)
              *        OperatorExpression(or)
              */
             ol = ((IOperatorExpression)ol.get(0)).getOperandList();
         }
 
         Operator attributeOperator = getAOForOE(oe);
         //System.out.println("attributeOperator = "+attributeOperator);
 
         IExpression tempEx = ol.get(0);
         if ((collectionOperator != null) &&
             isElementsOfTypeOperator(tempEx)) {
             /**
              * We got a collection operator whose first operand
              * is IOperatorExpression(elementsOfType).  That means
              * it is the start of a PER_USER_PARAMETERS_MAP Attribute
              * such as "My Property" (myproperties) or
              * "Any Property" (properties).
              *
              * For example, the RowData below:
              *
              *      Any Property.someKey(int) != "34"
              *
              * is equivalent to the Expression:
              *
              *      OperatorExpression(and)
              *        OperatorExpression(any)
              *          OperatorExpression(elementsOfType)
              *            OperatorExpression(properties)
              *              AttributeExpression(this)
              *              StringLiteralValueExpression(someKey)
              *            ClassLiteralValueExpression(ovation.IntegerValue)
              *          OperatorExpression(!=)
              *            AttributeExpression(value)
              *            Int32LiteralValueExpression(34)
              *
              * A more complicated RowData example with nesting:
              *
              *  nextEpoch.nextEpoch.prevEpoch.Any Property.someKey(int) != "34"
              *
              * is equivalent to the Expression:
              *
              *  OperatorExpression(and)
              *    OperatorExpression(any)
              *      OperatorExpression(elementsOfType)
              *        OperatorExpression(properties)
              *          OperatorExpression(.)
              *            OperatorExpression(.)
              *              AttributeExpression(nextEpoch)
              *              AttributeExpression(nextEpoch)
              *            AttributeExpression(prevEpoch)
              *          StringLiteralValueExpression(someKey)
              *        ClassLiteralValueExpression(ovation.IntegerValue)
              *      OperatorExpression(!=)
              *        AttributeExpression(value)
              *        Int32LiteralValueExpression(34)
              *
              * Note, as of October 2011, the collectionOperator will
              * always be "any".  It will have two operands.  The first
              * is IOperatorExpression(elementsOfType), which will have
              * the "properties" or "myproperties" IOperatorExpression
              * as its first operand and IClassLiteralValueExpression
              * as its second.
              *
              * The second operand for the "any" collectionOperator
              * will be an IOperatorExpression with AttributeExpression(value)
              * as its first operand and some LiteralValueExpression as its
              * second operand.
              */
             //System.out.println("This is a PER_USER_PARAMETERS_MAP exp.");
 
             /**
              * If we get here, tempEx is IOperatorExpression(elementsOfType).
              * It should have two operands.  The first is the Attribute (and
              * the path to the Attribute).  The second is a
              * IClassLiteralValueExpression(ovation.<someType>.
              * Where <someType> is a value like: ovation.DateValue,
              * ovation.FloatingPointValue, ovation.IntegerValue, etc.
              */
 
             /**
              * First, because it is simple and easy, handle
              * the IOperatorExpression(elementsOfType) node's second
              * operand which gives us the property type.
              */
 
             IOperatorExpression oeElementsOfType = (IOperatorExpression)tempEx;
             IExpression exTemp = oeElementsOfType.getOperandList().get(1);
             if (!(exTemp instanceof IClassLiteralValueExpression)) {
                 String s = "IOperatorExpression(elementsOfType)'s second "+
                     "operand is not of type "+
                     "IClassLiteralValueExpression.";
                 throw(new IllegalArgumentException(s));
             }
 
             IClassLiteralValueExpression clve =
                 (IClassLiteralValueExpression)exTemp;
             Type type = getTypeForCLVE(clve);
             rowData.setPropType(type);
 
             /**
              * Now deal with the IOperatorExpression(elementsOfType) node's
              * first operand.
              */
 
             exTemp = oeElementsOfType.getOperandList().get(0);
             if (!(exTemp instanceof IOperatorExpression)) {
                 String s = "IOperatorExpression(elementsOfType) does not have "+
                     "an IOperatorExpression as its first "+
                     "operand.";
                 throw(new IllegalArgumentException(s));
             }
 
             IOperatorExpression oeAttributePath = (IOperatorExpression)exTemp;
 
             /**
              * At this point, oeAttributePath tells us the
              * name of the Attribute, the "path" to it, and
              * the key that it uses.
              *
              * oeAttributePath contains the name of the Attribute.
              * Its second operand is the "path" to the attribute.
              *
              * Its first operand is the property "key".
              */
 
             if (oeAttributePath.getOperandList().size() != 2) {
                 String s = "PER_USER_PARAMETERS_MAP IOperatorExpression("+
                     oeAttributePath.getOperatorName()+
                     ") does not have two operands.";
                 throw(new IllegalArgumentException(s));
             }
 
 
             /**
              * Turn the first operand into the property name/key
              * for the row.
              */
 
             exTemp = oeAttributePath.getOperandList().get(0);
             if (!(exTemp instanceof IStringLiteralValueExpression)) {
                 String s = "PER_USER_PARAMETERS_MAP "+
                     "IOperatorExpression("+
                     oeAttributePath.getOperatorName()+
                     ") is not of type IStringLiteralValueExpression.";
                 throw(new IllegalArgumentException(s));
             }
 
             IStringLiteralValueExpression slve =
                     (IStringLiteralValueExpression)exTemp;
             rowData.setPropName(slve.getValue().toString());
 
             /**
              * Now the second operand into the target.
              */
             exTemp = oeAttributePath.getOperandList().get(1);
             setAttributePath(rowData, oeAttributePath, classDescription);
 
 
             /**
              * Set the attributeOperator and (possibly) the
              * attributeValue.
              */
 
             tempEx = ol.get(1);
             if (!(tempEx instanceof IOperatorExpression)) {
                 String s = "Operand after the IOperatorExpression("+
                     "elementsOfType) operand is not of type "+
                     "IOperatorExpression. It is: "+tempEx;
                 throw(new IllegalArgumentException(s));
             }
 
             oe = (IOperatorExpression)tempEx;
             attributeOperator = getAOForOE(oe);
             if ((attributeOperator != Operator.IS_NULL) &&
                 (attributeOperator != Operator.IS_NOT_NULL) &&
                 (attributeOperator != Operator.IS_TRUE) &&
                 (attributeOperator != Operator.IS_FALSE)) {
 
                 List<IExpression> operandList = oe.getOperandList();
                 ILiteralValueExpression lve;
                 lve = (ILiteralValueExpression)operandList.get(1);
                 Attribute attribute = rowData.getChildmostAttribute();
                 Object attributeValue = createAttributeValue(
                     lve, attribute.getType());
                 rowData.setAttributeValue(attributeValue);
             }
 
             //System.out.println("Calling rowData.setAttributeOperator");
             rowData.setAttributeOperator(attributeOperator);
         }
         else if (collectionOperator != null) {
 
             rowData.setCollectionOperator(collectionOperator);
 
             if (attributeOperator != null) {
                 setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                  attributeOperator);
             }
 
             if (collectionOperator.isCompoundOperator()) {
 
                 int olIndex = 0;
                 ClassDescription childClass = classDescription;
                 if (ol.size() > 1) {
                     /**
                      * Convert the first operand into a RowData
                      * attributePath.
                      */
                     IExpression firstOperand = ol.get(olIndex++);
                     setAttributePath(rowData, firstOperand, classDescription);
                     //System.out.println("rowData so far: "+
                     //    rowData.getRowString());
                     Attribute childmostAttribute =
                         rowData.getChildmostAttribute();
                     childClass = childmostAttribute.getClassDescription();
                 }
                 //System.out.println("childClass: "+childClass);
 
                 /**
                  * When we get here, we know that the row is one
                  * of three kinds:
                  *
                  * Type 1)
                  *
                  *      The first operand was an Expression that told
                  *      us what attribute is being queried.  The operands
                  *      AFTER the first operand are the Expressions that
                  *      are being tested.  TODO:  Add example.
                  *
                  * Type 2)
                  *
                  *      The first operand was an Expression that told
                  *      us what attribute is being queried.  ("responses"
                  *      in the example below.)  The second
                  *      operand is another collection operator that will
                  *      become the row's second collection operator.
                  *      (The not(or) operators in the example below.)
                  *
                  *      OperatorExpression(not)
                  *        OperatorExpression(any)
                  *          AttributeExpression(responses)
                  *          OperatorExpression(not)
                  *            OperatorExpression(or)
                  *              OperatorExpression(==)
                  *                AttributeExpression(uuid)
                  *                StringLiteralValueExpression(xyz)
                  *
                  *      will become the RowData
                  *
                  *          responses None have None
                  *              uuid == xyz
                  *
                  * Type 3)
                  *
                  *      The row is a compound row that only has a
                  *      collection operator in it.
                  *      For example, the row says:  "Any of the following",
                  *      "All of the following", "None of the following".
                  *      So, there is a list of operands that are the
                  *      children of this Any/All/None collection operator.
                  */
                 IExpression secondOperand = ol.get(olIndex);
                 //System.out.println("Second operand = "+
                 //    ((Expression)secondOperand).toString(""));
 
                 if (!(secondOperand instanceof IOperatorExpression)) {
                     String s = "Second operand is "+secondOperand+
                         ".  It should be an IOperatorExpression.";
                     throw(new IllegalArgumentException(s));
                 }
 
                 IOperatorExpression oe2 = (IOperatorExpression)secondOperand;
                 //System.out.println("oe2 = "+oe.getOperatorName());
                 CollectionOperator collectionOperator2 = getCOForOE(oe2);
                 //System.out.println("collectionOperator2 = "+
                 //                   collectionOperator2);
 
                 Attribute childmost = rowData.getChildmostAttribute();
                 //System.out.println("childmost = "+childmost);
 
                 if (collectionOperator2 == null) {
                     /**
                      * Type 1 described above.
                      */
                     //System.out.println("Type 1");
                 }
                 else if ((childmost != null) &&
                          ((childmost.getType() != Type.PARAMETERS_MAP) &&
                           (childmost.getType() != Type.PER_USER_PARAMETERS_MAP))) {
                     /**
                      * Type 2 described above.
                      */
                     //System.out.println("Type 2");
                     rowData.setCollectionOperator2(collectionOperator2);
 
                     if (OE_NOT.equals(oe2.getOperatorName())) {
                         if ((oe2.getOperandList().size() < 1) ||
                             (!(oe2.getOperandList().get(0) instanceof
                              IOperatorExpression))) {
 
                             String s = "An IOperatorExpression(not) does not "+
                                 "have an IOperatorExpression of some type "+
                                 "as its one and only operand.";
                             throw(new IllegalArgumentException(s));
                         }
                         IExpression ex = oe2.getOperandList().get(0);
                         oe2 = (IOperatorExpression)ex;
                     }
                     ol = oe2.getOperandList();
                     olIndex = 0;
                 }
                 else {
                     /**
                      * Type 3 described above.
                      */
                     //System.out.println("Type 3");
                 }
 
                 /**
                  * For all three types described above,
                  * process the operands.
                  * Note that the olIndex has been set above
                  * somewhere to either 0 or 1.
                  */
                 for (; olIndex < ol.size(); olIndex++) {
 
                     IOperatorExpression operand =
                         (IOperatorExpression)ol.get(olIndex);
                     createAndAddChildRows(rowData, operand, childClass);
                 }
             }
         }
         else {
             if (attributeOperator != null) {
                 /**
                  * TODO: Perhaps put this "if (attributeOperator != null)"
                  * block outside of this else block.  The same code
                  * is executed above also.
                  */
                 setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                  attributeOperator);
                 //rowData.setAttributeOperator(attributeOperator);
             }
         }
 
         childRows.add(rowData);
 
         return(childRows);
     }
 
 
     /**
      * This method will set the attributeOperator, attributePath,
      * and attributeValue of the passed in rowData based on the
      * passed in values.
      *
      * @param rowData RowData
      * @param operandList list of operands
      * @param classDescription DataModel class description
      * @param attributeOperator operator
      */
     private static void setAttributeOperatorPathAndValue(RowData rowData,
         List<IExpression> operandList, ClassDescription classDescription,
         Operator attributeOperator) {
 
         //System.out.println("Enter setAttributeOperatorPathAndValue");
         //System.out.println("attributeOperator: "+attributeOperator);
 
         /**
          * Convert the first (left) operand into a RowData
          * attributePath.
          */
         IExpression ex = operandList.get(0);
         if (ex instanceof IOperatorExpression) {
 
             String operatorName = ((IOperatorExpression)ex).getOperatorName();
             if (OE_IS_NULL.equals(operatorName)) {
 
                 // TODO: Throw exception if no operand.
                 ex = ((IOperatorExpression)ex).getOperandList().get(0);
             }
         }
         setAttributePath(rowData, ex, classDescription);
 
         /**
          * Now handle the second (right) operand.
          */
         if ((attributeOperator != Operator.IS_NULL) &&
             (attributeOperator != Operator.IS_NOT_NULL) &&
             (attributeOperator != Operator.IS_TRUE) &&
             (attributeOperator != Operator.IS_FALSE)) {
 
             ILiteralValueExpression lve;
             lve = (ILiteralValueExpression)operandList.get(1);
             Attribute attribute = rowData.getChildmostAttribute();
             Object attributeValue = createAttributeValue(
                 lve, attribute.getType());
             rowData.setAttributeValue(attributeValue);
         }
 
         //System.out.println("Calling rowData.setAttributeOperator");
         rowData.setAttributeOperator(attributeOperator);
     }
 
 
     /**
      * @param rowData The row whose child rows we will create.
      * We also use this to get the "parent" class used to interpret
      * values in the oe's operand list.
      *
      * @param oe The IOperatorExpression whose list of operands will
      * define the RowData children we create.
      * @param classDescription data model class description
      */
     private static void createAndAddChildRows(RowData rowData,
         IOperatorExpression oe, ClassDescription classDescription) {
 
         List<RowData> childRows = createChildRows(oe, classDescription);
         rowData.addChildRows(childRows);
     }
 
 
     /**
      * This returns true if the passed in IExpression is
      * a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Attribute like "keywords", "mykeywords", etc.
      *
      * @return true if the given expression is a per-user operator
      * @param ex
      * @param cd
      */
     private static boolean isPerUserOperator(IExpression ex,
                                              ClassDescription cd) {
 
         if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             String name = oe.getOperatorName();
 
             Attribute attribute = cd.getAttribute(name);
             if ((attribute != null) &&
                 (attribute.getType() == Type.PER_USER_OR_CUSTOM_REFERENCE_OPERATOR)) {
                 return(true);
             }
         }
 
         return(false);
     }
 
 
     /**
      * This returns true if the passed in IExpression is
      * a PER_USER_PARAMETERS_MAP Attribute like "properties",
      * "myproperties", etc.
      *
      * @return true if the given expression is a per-user parameters map operator
      * @param ex
      * @param cd
      */
     private static boolean isPerUserParametersMapOperator(IExpression ex,
                                                           ClassDescription cd) {
 
         if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             String name = oe.getOperatorName();
 
             Attribute attribute = cd.getAttribute(name);
             if ((attribute != null) &&
                 (attribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {
                 return(true);
             }
         }
 
         return(false);
     }
 
 
     /**
      * This returns true if the passed in IExpression is
      * the parent of a PER_USER_PARAMETERS_MAP Attribute like "properties"
      * or "myproperties".
      *
      * Note, we know that this IExpression tree is a
      * PER_USER_PARAMETERS_MAP tree if it is an
      * IOperatorExpression(elementsOfType).  If that is
      * the case, then the IOperatorExpression that is its
      * first operand, (e.g. IOperatorExpression(myproperties)),
      * is an Attribute of type PER_USER_PARAMETERS_MAP.
      * So, we don't actually check the child at this point.
      *
      * Note, as of October 2011, the only Attributes of
      * Type.PER_USER_PARAMETERS_MAP are "properties" and
      * "myproperties".
      *
      * @return true if the given expression is an elements of type operator
      * @param ex
      */
     private static boolean isElementsOfTypeOperator(IExpression ex) {
 
         if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             String name = oe.getOperatorName();
 
             if (OE_ELEMENTS_OF_TYPE.equals(name))
                 return(true);
         }
 
         return(false);
     }
 
 
     /**
      * Set the value of the rowData's attributePath to be the
      * equivalent of the value in the IExpression.
      * This method converts the passed in IExpression tree
      * into Attributes and adds them to the rowData's attributePath.
      *
      * Please note, this method calls itself recursively.
      * So, for each RowData object, (i.e. each row in the GUI),
      * this method is only called once by another function,
      * but after that initial call with the node that is
      * the "top" of the attribute path part of the expression
      * passed in as the "ex" parameter, this method calls itself
      * recursively to create the entire path.
      *
      * @param rowData The RowData object whose attributePath we will set.
      *
      * @param ex This is the subtree that defines the attribute path.
      * This is probably the "left" (i.e. first) operand of an operator.
      *
      * @param classDescription This is the "parent" class that
      * is the class of the leftmost Attribute of the path.
      *
      * @return The ClassDescription of the leftmost Attribute
      * on which this method is currently working.  If the
      * Attribute is a primitive, (e.g. int, string), this returns null.
      */
     /**
      * @param ex The IExpression that is the left operand of an operator.
      */
     private static ClassDescription setAttributePath(RowData rowData,
         IExpression ex, ClassDescription classDescription) {
 
         /*
         System.out.println("Enter setAttributePath");
         System.out.println("rowData: "+rowData.getRowString());
         System.out.println("ex: "+((Expression)ex));
         System.out.println("classDescription: "+classDescription);
         */
 
         if ((ex instanceof IAttributeExpression) ||
             (isPerUserOperator(ex, classDescription)) ||
             (isPerUserParametersMapOperator(ex, classDescription))) {
 
             String name;
             if (ex instanceof IAttributeExpression) {
                 IAttributeExpression ae = (IAttributeExpression)ex;
                 name = ae.getAttributeName();
             }
             else {
                 /**
                  * ex is a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR "operator" such as "keywords"
                  * or "mykeywords", or it is a PER_USER_PARAMETERS_MAP
                  * "operator" such as "properties" or "myproperties.
                  *
                  * In either case, it will have an attribute
                  * path as a subtree.  If so, we need to parse it
                  * and prepend it to the attribute path.
                  * For example:
                  *
                  *      OperatorExpression(and)
                  *        OperatorExpression(all)
                  *          OperatorExpression(keywords)
                  *            OperatorExpression(.)
                  *              OperatorExpression(.)
                  *                AttributeExpression(nextEpoch)
                  *                AttributeExpression(nextEpoch)
                  *              AttributeExpression(prevEpoch)
                  *          OperatorExpression(or)
                  *            OperatorExpression(==)
                  *              AttributeExpression(uuid)
                  *              StringLiteralValueExpression(xyz)
                  *
                  * needs to become:
                  *
                  *      nextEpoch.nextEpoch.prevEpoch.All Keywords All have Any
                  *
                  * Note that if there is no "path", the subtree
                  * under OperatorExpression(keywords) will
                  * be just the AttributeExpression(this).
                  */
                 IOperatorExpression oe = (IOperatorExpression)ex;
                 name = oe.getOperatorName();
 
                 IExpression ex2;
                 if(isPerUserOperator(ex, classDescription)) {
                     if (oe.getOperandList().size() < 1) {
                         String s = "A PER_USER_OR_CUSTOM_REFERENCE_OPERATOR IOperatorExpression("+name+") "+
                                 "does not have any operands.  It should have at "+
                                 "least one operand such as AttributeExpression(this).";
                         throw(new IllegalArgumentException(s));
                     }
 
                     ex2 = oe.getOperandList().get(0);
                 } else {
                     if (oe.getOperandList().size() < 1) {
                         String s = "A PER_USER_OR_CUSTOM_REFERENCE_OPERATOR IOperatorExpression("+name+") "+
                                 "does not have any operands.  It should have at "+
                                 "least two operands such as (key,AttributeExpression(this)).";
                         throw(new IllegalArgumentException(s));
                     }
 
                     ex2 = oe.getOperandList().get(1);
                 }
 
                 /**
                  * Check whether the operand is the special
                  * AttributeExpression(this) value.
                  */
                 if ((ex2 instanceof IAttributeExpression) &&
                     AE_THIS.equals(((IAttributeExpression)ex2).
                                     getAttributeName())) {
                     /**
                      * The operand is AttributeExpresion(this).
                      * It is NOT added to the attribute path.
                      * It is something that exists in
                      * the Expression tree, but not in the GUI.
                      */
                 }
                 else {
                     /**
                      * Traverse the subtree that define the
                      * attribute path to the special PER_USER_OR_CUSTOM_REFERENCE_OPERATOR
                      * operator.  E.g. traverse the
                      * nextEpoch.nextEpoch.prevEpoch of the
                      * example attribute path described above.
                      */
                     setAttributePath(rowData, ex2, classDescription);
                 }
             }
 
             Attribute attribute = getAttribute(name, classDescription);
 
             if (attribute == null) {
                 String s = "Attribute name \""+name+
                     "\" does not exist in class \""+classDescription.getName()+
                     "\"";
                 throw(new IllegalArgumentException(s));
             }
 
             //System.out.println("Adding attribute \""+attribute+"\" to path.");
             rowData.addAttribute(attribute);
             return(attribute.getClassDescription());
         }
         else if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             if (OE_DOT.equals(oe.getOperatorName())) {
 
                 /**
                  * The operator is a ".", so this could be a
                  * "normal" attribute path or this could
                  * be a PARAMETERS_MAP attribute path.
                  */
 
                 IExpression op = oe.getOperandList().get(0);
 
                 if ((op instanceof IOperatorExpression) &&
                     OE_AS.equals(((IOperatorExpression)op).getOperatorName())) {
 
                     /**
                      * This is a PARAMETERS_MAP attribute path.
                      *
                      * Here is an example that is not "nested" after
                      * other attributes:
                      *
                      * protocolParameters.someTimeKey(time) == "Fri Jan 01 2010"
                      *
                      * OperatorExpression(and)
                      *   OperatorExpression(==)
                      *     OperatorExpression(.)
                      *       OperatorExpression(as)
                      *         OperatorExpression(parameter)
                      *           AttributeExpression(protocolParameters)
                      *           StringLiteralValueExpression(someTimeKey)
                      *         ClassLiteralValueExpression(ovation.DateValue)
                      *       AttributeExpression(value)
                      *     TimeLiteralValueExpression(Fri Jan 01 2010)
                      *
                      * Here is an example that is "nested":
                      *
                      * nextEpoch.nextEpoch.prevEpoch.protocolParameters.key(float) == "12.3"
                      *
                      * OperatorExpression(and)
                      *   OperatorExpression(==)
                      *     OperatorExpression(.)
                      *       OperatorExpression(as)
                      *         OperatorExpression(parameter)
                      *           OperatorExpression(.)
                      *             OperatorExpression(.)
                      *               OperatorExpression(.)
                      *                 AttributeExpression(nextEpoch)
                      *                 AttributeExpression(nextEpoch)
                      *               AttributeExpression(prevEpoch)
                      *             AttributeExpression(protocolParameters)
                      *           StringLiteralValueExpression(key)
                      *         ClassLiteralValueExpression(ovation.FloatingPointValue)
                      *       AttributeExpression(value)
                      *     Float64LiteralValueExpression(12.3)
                      *
                      * The other operand should be AttributeExpression(value).
                      * TODO:  Do we need to check that that is the case?
                      *
                      * The OperatorExpression(as) node should have two
                      * operands:  OperatorExpression(parameter) and
                      * ClassLiteralValueExpression(ovation.<someType>).
                      * Where <someType> is a value like: ovation.DateValue,
                      * ovation.FloatingPointValue, ovation.IntegerValue, etc.
                      */
                     IOperatorExpression oeAs = (IOperatorExpression)op;
                     if (oeAs.getOperandList().size() != 2) {
                         String s = "IOperatorExpression(as) does not have "+
                             "two operands.";
                         throw(new IllegalArgumentException(s));
                     }
 
                     /**
                      * First, because it is simple and easy, handle
                      * the IOperatorExpression(as) node's second
                      * operand which gives us the property type.
                      */
 
                     IExpression exTemp = oeAs.getOperandList().get(1);
                     if (!(exTemp instanceof IClassLiteralValueExpression)) {
                         String s = "IOperatorExpression(as)'s second "+
                             "operand is not of type "+
                             "IClassLiteralValueExpression.";
                         throw(new IllegalArgumentException(s));
                     }
 
                     IClassLiteralValueExpression clve =
                         (IClassLiteralValueExpression)exTemp;
                     Type type = getTypeForCLVE(clve);
                     rowData.setPropType(type);
 
                     /**
                      * Now deal with the IOperatorExpression(as) node's
                      * first operand.
                      */
 
                     exTemp = oeAs.getOperandList().get(0);
                     if ((!(exTemp instanceof IOperatorExpression)) ||
                         !OE_PARAMETER.equals(((IOperatorExpression)exTemp).
                                              getOperatorName())) {
                         String s = "IOperatorExpression(as) does not have "+
                             "IOperatorExpression(parameter) as its first "+
                             "operand.";
                         throw(new IllegalArgumentException(s));
                     }
 
                     IOperatorExpression oeParameter =
                         (IOperatorExpression)exTemp;
 
                     /**
                      * The OperatorExpression(parameter) node should have
                      * two operands.  The first is an IExpression
                      * node/tree that is the path to the attribute
                      * name.  For example, protocolParameters, or
                      * nextEpoch.nextEpoch.prevEpoch.protocolParameter.
                      * The second operand is a StringLiteralValueExpression
                      * that gives key.  The string "someTimeKey" or "key"
                      * in the examples in the comments above.
                      *
                      * Turn the first operand into an attribute path.
                      * Use the second operand to set the property name
                      * field in the RowData.
                      */
 
                     if (oeParameter.getOperandList().size() != 2) {
                         String s = "IOperatorExpression(parameter) does not "+
                             "have two operands.";
                         throw(new IllegalArgumentException(s));
                     }
 
                     exTemp = oeParameter.getOperandList().get(0);
                     setAttributePath(rowData, exTemp, classDescription);
 
                     exTemp = oeParameter.getOperandList().get(1);
                     if (!(exTemp instanceof IStringLiteralValueExpression)) {
                         String s = "IOperatorExpression(parameter)'s second "+
                             "operand is not of type "+
                             "IStringLiteralValueExpression.";
                         throw(new IllegalArgumentException(s));
                     }
 
                     IStringLiteralValueExpression slve =
                         (IStringLiteralValueExpression)exTemp;
                     rowData.setPropName(slve.getValue().toString());
                 }
                 else {
 
                     ClassDescription childClass;
                     childClass = setAttributePath(rowData, op,
                                                   classDescription);
 
                     if (oe.getOperandList().size() > 1) {
                         op = oe.getOperandList().get(1);
                         return(setAttributePath(rowData, op, childClass));
                     }
                     else {
                         /**
                          * This should never happen.  A dot operator must
                          * always have two operands.
                          */
                     }
                 }
             }
             else if (OE_COUNT.equals(oe.getOperatorName())) {
                 IExpression op = oe.getOperandList().get(0);
                 return(setAttributePath(rowData, op, classDescription));
             }
             else if (OE_IS_NULL.equals(oe.getOperatorName())) {
                 /**
                  * Do nothing because the later call to
                  * RowData.setAttributeOperator() calls
                  * addAttribute() with the correct
                  * Attribute.IS_NULL or Attribute.IS_NOT_NULL
                  * value.
                  */
             }
             /* Should not get any of these
             else if (OE_ANY.equals(oe.getOperatorName())) {
             }
             else if (OE_OR.equals(oe.getOperatorName())) {
             }
             else if (OE_ALL.equals(oe.getOperatorName())) {
             }
             */
             else {
                 String s = "Unhandled IOperatorExpression: "+
                     oe.getOperatorName();
                 (new Exception(s)).printStackTrace();
             }
         }
         else {
             (new Exception("Unhandled IExpression")).printStackTrace();
         }
         return(null);
     }
 
 
     private static Attribute getAttribute(String attributeName,
         ClassDescription classDescription) {
 
         Attribute attribute = classDescription.getAttribute(attributeName);
 
         return(attribute);
     }
 
 
     /**
      * Convert the passed in ILiteralValueExpression into an attributeValue
      * that the RowData object expects.
      *
      * In the case of an IInt32LiteralValueExpression we also need
      * the passed in Type to know what type of object to create.
      * This is because the Expression structure does not have a
      * IInt16LiteralValueExpression class.  (All integer values are
      * the same.)
      */
     private static Object createAttributeValue(ILiteralValueExpression lve,
                                                Type type) {
 
         /**
          * Some values in the Expression tree are already the
          * correct object type for the RowData object.
          *
          * But, Expression IInt32LiteralValueExpression values
          * need to compared to the DataModel to figure out
          * whether the attributeValue should be an Integer or a
          * Short object.  (INT_16 or INT_32)
          */
         if ((lve instanceof ITimeLiteralValueExpression) ||
             (lve instanceof IStringLiteralValueExpression) ||
             (lve instanceof IFloat64LiteralValueExpression)) {
             return(lve.getValue());
         }
         else if (lve instanceof IBooleanLiteralValueExpression) {
             /**
              * We should not be passed an IBooleanLiteralValueExpression
              * to turn into an attributeValue, because booleans are
              * handled via the special Operator.IS_TRUE
              * The caller should have figured that out and not called us.
              */
             (new Exception("Unhandled ILiteralValueExpression subclass")).
                 printStackTrace();
             return(null);
         }
         else if (lve instanceof IInt32LiteralValueExpression) {
             /**
              * Look at the DataModel to figure out whether this
              * should be a Short or an Integer.
              */
             if (type == Type.INT_16) {
                 int value = (Integer)lve.getValue();
                 return(new Short((short)value));
             }
             else {
                 /**
                  * The value is already an Integer object.
                  * (Or at least it should be!)
                  */
                 return(lve.getValue());
             }
         }
         else {
             (new Exception("Unhandled ILiteralValueExpression subclass")).
                 printStackTrace();
             return(new String("ERROR"));
         }
     }
 
 
     /**
      * Get the CollectionOperator that is equivalent to the passed
      * in OperatorExpression.
      *
      * If the passed in OperatorExpression cannot be mapped to a
      * CollectionOperator, this method returns null.
      */
     private static CollectionOperator getCOForOE(IOperatorExpression oe) {
 
         if ((OE_OR.equals(oe.getOperatorName())) ||
             (OE_ANY.equals(oe.getOperatorName()))) {
             return(CollectionOperator.ANY);
         }
         else if ((OE_AND.equals(oe.getOperatorName())) ||
                  (OE_ALL.equals(oe.getOperatorName()))) {
             return(CollectionOperator.ALL);
         }
         else if (OE_NOT.equals(oe.getOperatorName())) {
             oe = (IOperatorExpression)(oe.getOperandList().get(0));
             if (OE_OR.equals(oe.getOperatorName()) ||
                 OE_ANY.equals(oe.getOperatorName())) {
                 return(CollectionOperator.NONE);
             }
         }
         else if (OE_COUNT.equals(oe.getOperatorName())) {
             return(CollectionOperator.COUNT);
         }
 
         //System.err.println("ERROR:  ExpressionTranslator.getCOForOE()"+
         //    "\nCode must be updated to handle this type of expression.");
         return(null);
     }
 
 
     /**
      * Get the attribute Operator that is equivalent to the passed
      * in IOperatorExpression.
      *
      * If the passed in IOperatorExpression cannot be mapped to a
      * attribute Operator, this method returns null.
      * Note, that is not necessarily and error.
      */
     private static Operator getAOForOE(IOperatorExpression oe) {
 
         /**
          * Note there is no OE_IS_NOT_NULL, OE_IS_TRUE, OE_IS_FALSE.
          * Those values are handled differently in an Expression and
          * in a RowData.
          */
 
         if (OE_EQUALS.equals(oe.getOperatorName())) {
 
             IExpression ex = null;
             if (oe.getOperandList().size() > 1)
                 ex = oe.getOperandList().get(1);
 
             if (ex instanceof IBooleanLiteralValueExpression) {
 
                 IBooleanLiteralValueExpression blve;
                 blve = (IBooleanLiteralValueExpression)ex;
                 Boolean value = (Boolean)blve.getValue();
                 if (value.booleanValue() == true)
                     return(Operator.IS_TRUE);
                 else
                     return(Operator.IS_FALSE);
             }
             else {
                 return(Operator.EQUALS);
             }
         }
         else if (OE_NOT_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.NOT_EQUALS);
         }
         else if (OE_LESS_THAN.equals(oe.getOperatorName())) {
             return(Operator.LESS_THAN);
         }
         else if (OE_GREATER_THAN.equals(oe.getOperatorName())) {
             return(Operator.GREATER_THAN);
         }
         else if (OE_LESS_THAN_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.LESS_THAN_EQUALS);
         }
         else if (OE_GREATER_THAN_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.GREATER_THAN_EQUALS);
         }
         else if (OE_MATCHES_CASE_SENSITIVE.equals(oe.getOperatorName())) {
             return(Operator.MATCHES_CASE_SENSITIVE);
         }
         else if (OE_MATCHES_CASE_INSENSITIVE.equals(oe.getOperatorName())) {
             return(Operator.MATCHES_CASE_INSENSITIVE);
         }
         else if (OE_DOES_NOT_MATCH_CASE_SENSITIVE.equals(
                  oe.getOperatorName())) {
             return(Operator.DOES_NOT_MATCH_CASE_SENSITIVE);
         }
         else if (OE_DOES_NOT_MATCH_CASE_INSENSITIVE.equals(
                  oe.getOperatorName())) {
             return(Operator.DOES_NOT_MATCH_CASE_INSENSITIVE);
         }
         else if (OE_IS_NULL.equals(oe.getOperatorName())) {
             return(Operator.IS_NULL);
         }
         else if (OE_NOT.equals(oe.getOperatorName())) {
             oe = (IOperatorExpression)(oe.getOperandList().get(0));
             if (OE_IS_NULL.equals(oe.getOperatorName())) {
                 return(Operator.IS_NOT_NULL);
             }
             // What if we get here?
         }
 
         /**
          * The passed in IOperatorExpression cannot be mapped to an
          * attribute Operator.
          */
         return(null);
     }
 
 
     private static Type getTypeForCLVE(IClassLiteralValueExpression clve) {
 
         if ((clve == null) || (clve.getValue() == null))
             return(null);
 
         String name = clve.getValue().toString();
 
         /**
          * At this point, name should be something like:
          *
          *      ovation.DateValue
          */
 
         if (CLVE_BOOLEAN.equals(name))
             return(Type.BOOLEAN);
         else if (CLVE_STRING.equals(name))
             return(Type.UTF_8_STRING);
         else if (CLVE_INTEGER.equals(name))
             return(Type.INT_32);
         else if (CLVE_FLOAT.equals(name))
             return(Type.FLOAT_64);
         else if (CLVE_DATE.equals(name))
             return(Type.DATE_TIME);
         else {
             String s = "Bad IClassLiteralValue parameter clve: \""+name+"\"";
             throw(new IllegalArgumentException(s));
         }
     }
 }
