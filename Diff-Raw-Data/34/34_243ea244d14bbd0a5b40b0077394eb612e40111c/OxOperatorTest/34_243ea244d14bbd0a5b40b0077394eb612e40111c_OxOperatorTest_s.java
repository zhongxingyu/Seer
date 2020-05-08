 package com.bazaarvoice.oxen;
 
 import com.bazaarvoice.oxen.expressions.OxFunctions;
 import com.bazaarvoice.oxen.expressions.OxOperator;
 import com.bazaarvoice.oxen.expressions.OxOperators;
 import com.bazaarvoice.oxen.symbols.OxLongSymbol;
import junit.framework.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.Locale;
 
/**
 * Created by steve.ohara
 * Date: 9/21/12 11:48 AM
 */

 public class OxOperatorTest {
     private final Locale _locale = Locale.getDefault();
     private final OxConstants _constants = new OxConstants();
     private final OxFunctions _functions = new OxFunctions(_constants, _locale);
     private final OxOperators _operators = new OxOperators(_constants);
     private final OxSymbolTable _symbolTable = new OxSymbolTable();
 
     @BeforeClass
     private void init () {
         // Create some variables to work with
         OxLongSymbol five = new OxLongSymbol("five");
         five.setLong(5);
         _symbolTable.addSymbol(five);
     }
 
     @DataProvider (name = "testOperatorData")
     public Object[][] operatorData() {
         OxOperator[] ops = _operators.allOperators();
         Object[][] provider = new Object[ops.length][];
         for (int i = 0; i < ops.length; i++) {
             provider[i] = new Object[] { ops[i] };
         }
         return provider;
     }
 
     @Test (dataProvider = "testOperatorData")
     private void testOperator(final OxOperator op) {
         boolean ok = op.testOperator(_symbolTable, _constants, _functions, _operators);
        Assert.assertTrue("Operator " + op.toString() + " test failed.", ok);
     }
 }
