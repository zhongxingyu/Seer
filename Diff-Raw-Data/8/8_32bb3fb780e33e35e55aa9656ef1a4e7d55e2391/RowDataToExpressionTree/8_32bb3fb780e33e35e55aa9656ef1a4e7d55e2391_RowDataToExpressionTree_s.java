 package com.physion.ovation.gui.ebuilder.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 
 import com.physion.ovation.gui.ebuilder.ExpressionBuilder;
 import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
 import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
 import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
 import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
 import com.physion.ovation.gui.ebuilder.datatypes.Type;
 import com.physion.ovation.gui.ebuilder.datatypes.Operator;
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 import com.physion.ovation.gui.ebuilder.datamodel.DataModel;
 
 
 /**
  * This class is used to translate a RowData expression tree
  * into an ExpressionTree object.
  *
  * The ExpressionTree object is the class that most closely
  * maps to PQL.  It is the format that the "other" code in
  * the system understands.  The RowData object is the class
  * that represents the GUI's "view" of the data.
  *
  * The only method you will probably need to use is the
  * translate() method.
  *
  * @see ExpressionTreeToRowData
  */
 public class RowDataToExpressionTree
     extends Translator {
 
     /**
      * Create an Expression from the passed in root RowData.
      * This method should only be called if the passed in
      * RowData object is the rootRow of an expression tree.
      *
      * To turn an ExpresionTree into a RowData object, use
      * the ExpressionTreeToRowData.translate() class and method.
      *
      * The method will throw an exception if the passed in
      * RowData is not in a format that can be turned
      * into an ExpressionTree object.
      */
     public static ExpressionTree translate(RowData rootRow) {
 
         if (rootRow == null) {
             return(null);
         }
 
         if (rootRow.isRootRow() == false) {
             System.err.println("ERROR:  "+
                 "ExpressionTranslator.createExpressionTree() "+
                 "was passed a rootRow parameter that is not really the "+
                 "root of an expression tree.");
             return(null);
         }
 
         /**
          * This is the root Expression object.  I.e. the "top" node of the
          * expression tree.
          */
         OperatorExpression rootExpression;
 
         /**
          * In most cases, this will be set to point to the
          * same OpertorExpression object as rootExpression.
          * But, in the case of the None collection operator,
          * this will point to a different OpertorExpression
          * object.
          */
         OperatorExpression lastExpression;
 
         /**
          * Create the root expression.  If the root
          * row uses the None CollectionOperator, that
          * might actually create two operators.  I.e. not(or)
          */
         rootExpression = getOEForCO(rootRow.getCollectionOperator(), true);
         lastExpression = getLastOperator(rootExpression);
 
         /**
          * At this point, lastExpression is a reference to
          * the OperatorExpression that will have the
          * list of operands added to.
          *
          * Now add one IExpression operand to lastExpression for each RowData
          * that is a child of the rootRow.
          */
         for (RowData childRow : rootRow.getChildRows()) {
             lastExpression.addOperand(createExpression(childRow));
         }
 
         ExpressionTree expressionTree = new ExpressionTree(
             rootRow.getClassUnderQualification().getName(), rootExpression);
         return(expressionTree);
     }
 
 
     /**
      * Create the operator, or operators, for the passed in
      * RowData object.
      *
      * Most of the time, only one operator needs to be created.
      * For example, the RowData attributeOperators "==", "any",
      * "is null", can all be represented by a single
      * OpertorExpression object.
      *
      * But, some operators need two OpertorExpression
      * objects to represent them.  For example, the "None"
      * CollectionOperator becomes the "not" operator with
      * the "or" operator as its only operand if the rowData
      * is the root row.  I.e. not(or())  It becomes not(any())
      * for rows other than the root row.
      * The "is not null" attribute operator becomes the
      * "not" operator with the "is null" operator as its
      * only operand.
      *
      * So, this method returns an OperatorExpression which
      * might have an operand that is another OperatorExpression.
      * The caller needs to check whether the returned
      * OperatorExpression has an operand.  If it does, then the
      * caller will want to hang the rest of the expression tree
      * of the returned OperatorExpression's first (and only)
      * operand returned from OperatorExpression.getOperandList().
      */
     private static OperatorExpression createOperators(RowData rowData) {
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
         CollectionOperator co = rowData.getCollectionOperator();
         Operator ao = rowData.getAttributeOperator();
 
         if (childmostAttribute == null) {
             return(getOEForCO(co, true));
         }
         else {
             if (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP) {
                 /**
                  * Handle the special case of the PER_USER_PARAMETERS_MAP
                  * type that always has the "any" operator.
                  * Note, in a future release of the GUI, this might change.
                  */
                 return(new OperatorExpression(OE_ANY));
             }
             else if (childmostAttribute.getType() == Type.PARAMETERS_MAP) {
                 //return(new OperatorExpression(ao.toString()));
                 return(getOEForAO(co, ao));
             }
         }
 
         /**
          * If we get here, rowData is not a Compound Row, but
          * it still could have a CollectionOperator.
          */
         return(getOEForAO(co, ao));
     }
 
 
     /**
      * Get the IOperatorExpression for the passed in
      * RowData's collectionOperator and attributeOperator.
      *
      * Most of the time, the translation is straight forward,
      * e.g. (==, !=, <, >, <=, etc.), but for a few Operators
      * that are presented differently in the GUI's RowData object
      * than they are in the ExpressionTree, it gets a little
      * more complicated.  For example:
      * 
      *      Operator.IS_NOT_NULL
      *
      * becomes:
      *
      *      OperatorExpression(not)
      *        OperatorExpression(is null)
      *          <thing being tested to see if it is null>
      *
      * For example:
      *
      *      Operator.IS_TRUE 
      *
      * becomes:
      *
      *      OperatorExpression(==)
      *        <thing being tested to see if it is true>
      *        BooleanLiteralValueExpression(true)
      *
      */
     private static OperatorExpression getOEForAO(CollectionOperator co,
                                                  Operator ao) {
 
         OperatorExpression op1;
         OperatorExpression op2;
 
         if (ao == Operator.IS_TRUE) {
             /**
              * Note that the Operator.IS_TRUE and Operator.IS_FALSE
              * are not operators in the Expression tree.  In the
              * Expression tree we use the "==" operator and
              * a BooleanLiteralValueExpression.
              */
             op1 = new OperatorExpression(OE_EQUALS);
         }
         else if (ao == Operator.IS_FALSE) {
             op1 = new OperatorExpression(OE_EQUALS);
         }
         else if (ao == Operator.IS_NULL) {
             op1 = new OperatorExpression(OE_IS_NULL);
         }
         else if (ao == Operator.IS_NOT_NULL) {
             op1 = new OperatorExpression(OE_NOT);
             op2 = new OperatorExpression(OE_IS_NULL);
             op1.addOperand(op2);
         }
         else if (co != null) {
 
             if (co == CollectionOperator.COUNT) {
                 op1 = new OperatorExpression(ao.toString());
                 op2 = getOEForCO(co, false);
                 op1.addOperand(op2);
             }
             else {
                 op1 = getOEForCO(co, false);
             }
         }
         else {
             op1 = new OperatorExpression(ao.toString());
         }
 
         return(op1);
     }
 
 
     /**
      * This method is meant to be used on an OperatorExpression
      * object that has just been created by the createOperators() method.
      * It either returns the passed in OpertorExpression object or
      * it returns the one and only operand in its operandList.
      *
      * For example, if you pass it a tree like this:
      *
      *      OperatorExprssion(not)
      *        OperatorExpression(or)
      *
      * this method returns a reference to the OperatorExpression(or) node.
      *
      * If you pass it a "tree" like this:
      *
      *      OperatorExpression(any)
      *
      * this method returns a reference to the same OperatorExpression(any)
      * node you passed as the op parameter.
      */
     private static OperatorExpression getLastOperator(OperatorExpression op) {
 
         if (op.getOperandList().size() < 1) {
             return(op);
         }
         else {
             return((OperatorExpression)op.getOperandList().get(0));
         }
     }
 
 
     /**
      * Create an Expression from the passed in RowData object.
      * This method is NOT meant to handle the root RowData object.
      * The method createExpressionTree() handles the root row and
      * starts the whole process of translating everything below
      * the root row, which calls this method for every child RowData
      * object.
      *
      * Please note, this method calls itself recursively.
      */
     private static OperatorExpression createExpression(RowData rowData) {
 
         OperatorExpression expression;
         OperatorExpression lastOperator;
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
 
         /**
          * Create the operator that is the "top" node that this
          * method will return.  The returned OperatorExpression
          * will be placed in the operandList of whatever is
          * at the very top of the expression tree.  For example,
          * the very top of the expression tree might be a single
          * collection operator or the "not" operator with a child
          * operand that is the collection operator.
          */
         expression = createOperators(rowData);
         lastOperator = getLastOperator(expression);
 
         /**
          * At this point, lastOperator is either equal to the
          * same value that is in the expression variable,
          * or it is set to the one and only operand in expression's
          * operandList.
          */
 
         if ((childmostAttribute != null) &&
             (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {
 
             /**
              * As of October 2011, there is only the "properties"
              * and "myproperties" attributes that are of this
              * PER_USER_PARAMETERS_MAP type.
              */
             createAndAddPerUserParametersMap(lastOperator, rowData);
         }
         else if ((childmostAttribute != null) &&
                  (childmostAttribute.getType() == Type.PARAMETERS_MAP)) {
 
             createAndAddParametersMap(lastOperator, rowData);
         }
         else if ((rowData.getCollectionOperator() != null) &&
                  (rowData.getCollectionOperator2() != null)) {
 
                 IExpression ex = createExpressionPath(
                     rowData.getAttributePath(), rowData);
                 lastOperator.addOperand(ex);
 
                 OperatorExpression op = getOEForCO(
                     rowData.getCollectionOperator2(), true);
                 lastOperator.addOperand(op);
 
                 lastOperator = getLastOperator(op);
 
                 for (RowData childRow : rowData.getChildRows()) {
                     //System.out.println("Add an operand");
                     lastOperator.addOperand(createExpression(childRow));
                 } 
         }
         else if (rowData.getCollectionOperator() != null) {
 
             if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
                 /**
                  * This is a row with a CollectionOperator.COUNT like
                  * this:
                  *
                  *      responses Count == 27
                  */
                 lastOperator.addOperand(
                     createExpressionPath(rowData.getAttributePath(), rowData));
                 expression.addOperand(createLiteralValueExpression(
                     Type.INT_32, rowData));
             }
             else {
                 /**
                  * This is an Any/All/None row.
                  */
                 if (rowData.getAttributeCount() > 0) {
                     lastOperator.addOperand(
                         createExpressionPath(rowData.getAttributePath(),
                             rowData));
                 }
                 for (RowData childRow : rowData.getChildRows()) {
                     //System.out.println("Add an operand");
                     lastOperator.addOperand(createExpression(childRow));
                 } 
             }
         }
         else if (rowData.getAttributeOperator() != null) {
 
             lastOperator.addOperand(createExpressionPath(
                 rowData.getAttributePath(), rowData));
             if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                 (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                 lastOperator.addOperand(createLiteralValueExpression(rowData));
             }
         }
 
         if (expression == null) {
             String s = "expression == null, which means code is not finished.";
             (new Exception(s)).printStackTrace();
         }
 
         return(expression);
     }
 
 
     /**
      * Create the operands for a PER_USER expression.
      *
      * The comments use these example rowData values:
      *
      *      nextEpoch.nextEpoch.prevEpoch.My Keywords None
      *          uuid == "xyz"
      */
     private static OperatorExpression createPerUserExpression(RowData rowData) {
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
         OperatorExpression oe;
         oe = new OperatorExpression(childmostAttribute.getQueryName());
 
         if (rowData.getAttributeCount() < 2) {
             oe.addOperand(new AttributeExpression(AE_THIS));
         }
         else {
             /**
              * Add the expression that represents the
              * "nextEpoch.nextEpoch.prevEpoch" path.
              */
             createAndAddDotPath(oe, rowData);
         }
         return(oe);
     }
 
 
     private static void createAndAddParametersMap(
         OperatorExpression lastOperator, RowData rowData) {
 
         OperatorExpression dotOperand;
         IExpression valueOperand;
 
         dotOperand = new OperatorExpression(".");
         valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                     rowData);
         lastOperator.addOperand(dotOperand);
         lastOperator.addOperand(valueOperand);
 
         dotOperand.addOperand(createExpressionPath(rowData.getAttributePath(),
                               rowData));
         dotOperand.addOperand(new AttributeExpression(AE_VALUE));
     }
 
 
     /**
      * Create and add the Expressions for the PER_USER_PARAMETERS_MAP
      * type.  As of October 2011, only the "properties" and "myproperties"
      * Attributes are of this type.
      *
      * By the time this method is called, lastOperator has been set
      * to the OperatorExpression("any").
      * (This is always the case for an Attribute of type
      * PER_USER_PARAMETERS_MAP as far as I understand.)
      *
      * An example tree might look like this:
      *
      */
     private static void createAndAddPerUserParametersMap(
         OperatorExpression lastOperator, RowData rowData) {
 
         OperatorExpression op2;
         OperatorExpression elementsOfTypeOperator;
         IExpression valueOperand;
         OperatorExpression pupmOperator;  // Per User Properties Map Operator
 
         elementsOfTypeOperator = new OperatorExpression(OE_ELEMENTS_OF_TYPE);
         lastOperator.addOperand(elementsOfTypeOperator);
 
         op2 = new OperatorExpression(
             rowData.getAttributeOperator().toString());
         lastOperator.addOperand(op2);
 
         op2.addOperand(new AttributeExpression(AE_VALUE));
         valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                     rowData);
         op2.addOperand(valueOperand);
 
         Attribute attribute = rowData.getChildmostAttribute();
         pupmOperator = new OperatorExpression(attribute.getQueryName());
         elementsOfTypeOperator.addOperand(pupmOperator);
         elementsOfTypeOperator.addOperand(createCLVEForType(rowData.getPropType()));
 
         /**
          * Create the two operands for the pupmOperator.
          * (Per User Properties Map Operator)
          * As of October 2011, only the "properties" and "myproperties"
          * attributes were of this type.
          *
          * The first operand is the qualifying path to the attribute,
          * if any.  We use "this" if no path exists.
          * The second operand is the property key the user entered.
          */
 
         if (rowData.getAttributeCount() < 2) {
             pupmOperator.addOperand(new AttributeExpression(AE_THIS));
         }
         else {
             createAndAddDotPath(pupmOperator, rowData);
         }
 
         pupmOperator.addOperand(new StringLiteralValueExpression(
             rowData.getPropName()));
     }
 
 
     /**
      * Look at the passed in rowData object and create a
      * "dot path" of all the Attributes in the rowData's
      * attributePath except for the last Attribute.
      * I.e. the attributePath qualifies what the last Attribute is.
      * Then add that Expression to the passed in parent Expression.
      *
      * For example, if the rowData's attributePath is:
      *
      *      nextEpoch.nextEpoch.prevEpoch.My Keywords
      *
      * and the parent is:
      *
      *    OperatorExpression(mykeywords)
      *
      * this method will add the Expression tree below to parent:
      *
      *      OperatorExpression(.)
      *        OperatorExpression(.)
      *          AttributeExpression(nextEpoch)
      *          AttributeExpression(nextEpoch)
      *        AttributeExpression(prevEpoch)
      *
      * to create:
      *
      *    OperatorExpression(mykeywords)
      *      OperatorExpression(.)
      *        OperatorExpression(.)
      *          AttributeExpression(nextEpoch)
      *          AttributeExpression(nextEpoch)
      *        AttributeExpression(prevEpoch)
      */
     private static final void createAndAddDotPath(OperatorExpression parent,
         RowData rowData) {
 
         List<Attribute> attributePath = rowData.getAttributePath();
 
         /**
          * We ignore the last Attribute in the path.
          */
         attributePath = attributePath.subList(0, attributePath.size()-1);
         if (attributePath.size() < 1)
             return;
 
         parent.addOperand(createExpressionPath(attributePath, rowData));
     }
 
 
     /**
      * Create a LiteralValueExpression of the appropriate subclass
      * based on the passed in rowData.
      */
     private static ILiteralValueExpression createLiteralValueExpression(
         RowData rowData) {
 
         return(createLiteralValueExpression(rowData.getChildmostAttribute().
                                             getType(), rowData));
     }
     
 
     /**
      * Create a LiteralValueExpression of the appropriate subclass
      * based on the passed in type.  The value of the LiteralValueExpression
      * comes from the passed in rowData.
      */
     private static ILiteralValueExpression createLiteralValueExpression(
         Type type, RowData rowData) {
 
         Attribute attribute = rowData.getChildmostAttribute();
         switch (type) {
 
             case BOOLEAN:
                 boolean value;
                 value = (rowData.getAttributeOperator() == Operator.IS_TRUE);
                 return(new BooleanLiteralValueExpression(value));
 
             case UTF_8_STRING:
                 return(new StringLiteralValueExpression(
                        rowData.getAttributeValue().toString()));
 
             case INT_16:
                 /**
                  * Note that we change the INT_16 to an INT_32.
                  * As of October 15, 2011, there is no
                  * Int16LiteralValueExpression.
                  */
                 return(new Int32LiteralValueExpression(
                       ((Integer)rowData.getAttributeValue()).intValue()));
 
             case INT_32:
                 return(new Int32LiteralValueExpression(
                       ((Integer)rowData.getAttributeValue()).intValue()));
 
             case FLOAT_64:
                 return(new Float64LiteralValueExpression(
                       ((Double)rowData.getAttributeValue()).doubleValue()));
 
             case DATE_TIME:
                 return(new TimeLiteralValueExpression(
                        ((Date)rowData.getAttributeValue())));
 
             case REFERENCE:
                 return(new ClassLiteralValueExpression(
                        attribute.getQueryName()));
 
             default:
                 System.err.println("ERROR:  ExpressionTranslator."+
                     "createLiteralValueExpression().  Unhandled type.\n"+
                     "Type = "+type+"\n"+
                     "rowData:\n"+rowData.getRowString());
                 (new Exception("Unhandled type")).printStackTrace();
                 return(null);
         }
     }
 
 
     /**
      * Create and return a ClassLiteralValueExpression for the passed
      * in Type.
      *
      * TODO:  Perhaps create a set of static final ClassLiteralValueExpression
      * objects instead of the set of strings I currently have?  Then we
      * would not be creating new objects all the time.
      */
     private static ClassLiteralValueExpression createCLVEForType(Type type) {
 
         switch (type) {
 
             case BOOLEAN:
                 return(new ClassLiteralValueExpression(CLVE_BOOLEAN));
 
             case UTF_8_STRING:
                 return(new ClassLiteralValueExpression(CLVE_STRING));
 
             case INT_16:
                 return(new ClassLiteralValueExpression(CLVE_INTEGER));
 
             case INT_32:
                 return(new ClassLiteralValueExpression(CLVE_INTEGER));
 
             case FLOAT_64:
                 return(new ClassLiteralValueExpression(CLVE_FLOAT));
 
             case DATE_TIME:
                 return(new ClassLiteralValueExpression(CLVE_DATE));
 
             default:
                 String s = "Unhandled type parameter: "+type;
                 throw(new IllegalArgumentException(s));
         }
     }
 
 
     /**
      * Create an Expression for the passed in attribute, (which is
      * of type PARAMETERS_MAP).
      *
      * For example:
      *
      *      protocolParamters.key(int) == 1
      *
      * becomes:
      *
      *      OperatorExpression(.)
      *        OperatorExpression(as)
      *          OperatorExpression(parameter)
      *            AttributeExpression(protocolParameters)
      *            StringLiteralValueExpression(key)
      *          OperatorExpression(ovation.IntegerValue)
      *        AttributeExpression(value)
      *
      * This method only returns the "as" sub-tree above.
      *
      * Please note, if the passed in attributePath is more than
      * just the one "protocolParameters" entry in the example above,
      * this method handles the nesting of the parent Attributes
      * differently than for other Attribute types.  For example:
      *
      * nextEpoch.nextEpoch.prevEpoch.protocolParameters.key(float) == "12.3"
      *
      * becomes:
      * 
      * Expression:
      * CUQ: Epoch
      * rootExpression:
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
      * @return The "as" sub-tree described in the example above.
      * I.e. it returns an OperatorExpression(as) that has operands
      * similar to those described above.
      */
     private static IExpression createExpressionParametersMap(
         List<Attribute> attributePath, RowData rowData) {
 
         /**
          * Create the "as" operator and the AttributeExpression(value),
          * and add them as the left and right operands of the "." operator.
          */
         OperatorExpression asOperator = new OperatorExpression(OE_AS);
 
         /**
          * Create the "parameter" operator and the
          * ClassLiteralValueExpression(IntValue), and add them as the left
          * and right operands of the "as" operator.
          */
         OperatorExpression parameterOperator = new OperatorExpression(
             OE_PARAMETER);
         asOperator.addOperand(parameterOperator);
         asOperator.addOperand(createCLVEForType(rowData.getPropType()));
 
         /**
          * Create the left (i.e. first) operand for the 
          * OperatorExpression(parameter) operator.
          */
 
         Attribute lastAttribute = attributePath.get(attributePath.size()-1);
         IExpression aeQueryName = new AttributeExpression(
             lastAttribute.getQueryName());
         IExpression parameterLeftOperand;
         if (attributePath.size() > 1) {
             List<Attribute> allButLastAttribute = attributePath.subList(0,
                 attributePath.size()-1);
 
             IExpression ex = createExpressionPath(allButLastAttribute, rowData);
             OperatorExpression oeDot = new OperatorExpression(OE_DOT);
             oeDot.addOperand(ex);
             oeDot.addOperand(aeQueryName);
             parameterLeftOperand = oeDot;
         }
         else {
             parameterLeftOperand = aeQueryName;
         }
 
         /**
          * Now add the left operand we created above.
          */
         parameterOperator.addOperand(parameterLeftOperand);
 
         /**
          * Create and add the right (i.e. second) operand for the
          * OperatorExpression(parameter) operator.  This is the
          * "key" name that the user entered.
          */
         parameterOperator.addOperand(new StringLiteralValueExpression(
                                      rowData.getPropName()));
 
         return(asOperator);
     }
 
 
     /**
      * Create an expression based on the passed in attributePath and
      * RowData object.  This method calls itself recursively to build
      * up the Expression that represents the passed in attributePath.
      * We sometimes use the passed in rowData parameter to know the
      * property type and property name of the last attribute, if the
      * last attribute is of a type where those values are relevant.
      * For example, a PARAMETERS_MAP attribute type.
      *
      * Please note, although the passed in attributePath is "from"
      * the passed in RowData object, it is possibly a sub-list of
      * the RowData's full attributePath.  (This method calls itself
      * recursively, chopping off the last attribute from the
      * attributePath before calling itself.)  So, don't start
      * calling RowData.getAttributePath() unless you really are
      * sure that is what you want.
      *
      * Note, the syntax of the Expression tree is NOT consistent
      * for all data types.  For "generic" attributes, we create
      * a tree of nested OpertorExpressions.  For example:
      *
      * rootRow:
      * Epoch | Any
      *   Epoch | nextEpoch.nextEpoch.prevEpoch.protocolID == "Test 27"
      * 
      * Expression:
      * CUQ: Epoch
      * rootExpression:
      * OperatorExpression(or)
      *   OperatorExpression(==)
      *     OperatorExpression(.)
      *       OperatorExpression(.)
      *         OperatorExpression(.)
      *           AttributeExpression(nextEpoch)
      *           AttributeExpression(nextEpoch)
      *         AttributeExpression(prevEpoch)
      *       AttributeExpression(protocolID)
      *     StringLiteralValueExpression(Test 27)
      *
      * But for a PARAMETERS_MAP type, things are different.
      * Please see the createExpressionParametersMap() method for more info.
      *
      * @return The IExpression returned is, in the example above,
      * the first operand of the "==" operator subtree shown above.
      * I.e. the subtree that starts with the first OperatorExpression(.)
      */
     private static IExpression createExpressionPath(
         List<Attribute> attributePath, RowData rowData) {
 
         //System.out.println("Enter createExpressionPath(List<Attribute>)");
         //System.out.println("attributePath.size() = "+attributePath.size());
 
         if (attributePath.size() < 1) {
 
             /**
              * This should never happen.
              */
             String s = "attributePath.size() < 1";
             (new Exception(s)).printStackTrace();
             return(null);
         }
 
         /**
          * Throw away special Attributes suchs a "is null" and "is not null".
          */
         Attribute lastAttribute = attributePath.get(attributePath.size()-1);
         if ((attributePath.size() > 1) &&
             ((lastAttribute == Attribute.IS_NULL) ||
              (lastAttribute == Attribute.IS_NOT_NULL))) {
             attributePath = attributePath.subList(0, attributePath.size()-1);
             lastAttribute = attributePath.get(attributePath.size()-1);
         }
 
         /**
          * Handle the special cases:  PARAMETERS_MAP, PER_USER
          *
          * They don't handle "nesting" in the normal way because
          * they cannot be an operand of the dot operator.
          */
         if (lastAttribute.getType() == Type.PARAMETERS_MAP) {
             return(createExpressionParametersMap(attributePath, rowData));
         }
         else if (lastAttribute.getType() == Type.PER_USER) {
             return(createPerUserExpression(rowData));
         }
 
         /**
          * If the attributePath is, (or has been whittled down to),
          * one attribute long, create the Expression for that one
          * attribute.
          */
         if (attributePath.size() == 1) {
 
             Attribute attribute = attributePath.get(0);
 
             /**
              * Quick sanity check during development.
              */
             if (attribute.getType() == Type.PARAMETERS_MAP) {
                 /**
                  * We should never get here.
                  * This type should already have been handled
                  * above.
                  */
                 String s = "Got an Attribute of Type.PARAMETERS_MAP "+
                     "unexpectedly.  There is a problem in the code.";
                 (new Exception(s)).printStackTrace();
                 return(null);
             }
 
             if (attribute.getType() == Type.PER_USER) {
                 /**
                  * TODO:  I don't think we get here any more.
                  */
                 return(new OperatorExpression(
                        attribute.getQueryName()));
             }
             else {
                 return(new AttributeExpression(
                        attribute.getQueryName()));
             }
         }
 
         /**
          * If we get here, the attributePath is longer than one
          * attribute.  So, we will use the "." operator to concatenate
          * attributes on the attributePath.
          */
         OperatorExpression expression = new OperatorExpression(".");
 
         /**
          * Create and add the left operand.  Note, the left operand
          * is the sub-list of all attributes to the left of the the
          * rightmost attribute.  For example, if the attribute
          * path is:
          *
          *      epochGroup.source.label
          *
          * the left operand is made of "epochGroup.source" and the
          * right operand is "label".
          */
 
         IExpression operand;
         List<Attribute> subList;
         subList = attributePath.subList(0, attributePath.size()-1);
         operand = createExpressionPath(subList, rowData);
 
         //System.out.println("Add left operand.");
         expression.addOperand(operand);
 
         /**
          * Create and add the right operand.  The right operand
          * is the rightmost attribute in the passed in attributePath.
          */
 
         subList = attributePath.subList(attributePath.size()-1,
                                         attributePath.size());
         operand = createExpressionPath(subList, rowData);
 
         //System.out.println("Add right operand: ");
         expression.addOperand(operand);
 
         return(expression);
     }
 
 
     /**
      * Get the OperatorExpression that corresponds to the passed
      * in CollectionOperator.
      *
      * Please note, for CollectionOperator.NONE, this method returns
      * a reference to OperatorExpression(not).  The OperatorExpression(not)
      * has as its only child operand another OperatorExpression() that
      * is either "or" or "any".  For example, this method returns a
      * tree like this:
      *
      *      OperatorExprssion(not)
      *        OperatorExpression(or)
      *
      * or a tree like this:
      *
      *      OperatorExprssion(not)
      *        OperatorExpression(any)
      *
      * Use the getLastOperator() method to get the child operator
      * if it exists.
      */
     private static OperatorExpression getOEForCO(CollectionOperator co,
                                                  boolean isCompoundOperator) {
 
         if (isCompoundOperator) {
             switch (co) {
                 case ANY:
                     return(new OperatorExpression(OE_OR));
 
                 case ALL:
                     return(new OperatorExpression(OE_AND));
 
                 case NONE:
                     OperatorExpression notOE = new OperatorExpression(OE_NOT);
                     notOE.addOperand(new OperatorExpression(OE_OR));
                     return(notOE);
 
                 default:
                     String s = "Illegal collectionOperator in rowData?";
                     (new Exception(s)).printStackTrace();
                     return(null);
             }
         }
         else {
             switch (co) {
                 case ANY:
                     return(new OperatorExpression(OE_ANY));
 
                 case ALL:
                     return(new OperatorExpression(OE_ALL));
 
                 case NONE:
                     OperatorExpression notOE = new OperatorExpression(OE_NOT);
                     notOE.addOperand(new OperatorExpression(OE_ANY));
                     return(notOE);
 
                 case COUNT:
                     return(new OperatorExpression(OE_COUNT));
 
                 default:
                     String s = "Illegal collectionOperator in rowData?";
                     (new Exception(s)).printStackTrace();
                     return(null);
             }
         }
     }
 }
