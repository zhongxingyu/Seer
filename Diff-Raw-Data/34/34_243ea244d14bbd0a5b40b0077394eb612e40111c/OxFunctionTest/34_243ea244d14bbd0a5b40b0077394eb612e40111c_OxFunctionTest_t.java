 package com.bazaarvoice.oxen;
 
 /**
  * User: steve.ohara
  * Date: 9/19/12 12:10 PM
  */
 
 import com.bazaarvoice.oxen.expressions.OxFunction;
 import com.bazaarvoice.oxen.expressions.OxFunctions;
 import com.bazaarvoice.oxen.expressions.OxOperators;
 import com.bazaarvoice.oxen.symbols.OxLongSymbol;
import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.Locale;
 
 public class OxFunctionTest {
     private final Locale _locale = Locale.getDefault();
     private final OxConstants _constants = new OxConstants();
     private final OxFunctions _functions = new OxFunctions(_constants, _locale);
     private final OxOperators _operators = new OxOperators(_constants);
     private final OxSymbolTable _symbolTable = new OxSymbolTable();
 
     @BeforeClass
     private void init () {
         // Create some variables to work with
         OxLongSymbol six = new OxLongSymbol("six");
         six.setLong(6);
         _symbolTable.addSymbol(six);
     }
 
     @DataProvider (name = "testFunctionData")
     public Object[][] functionData() {
         OxFunction[] fns = _functions.allFunctions();
         Object[][] provider = new Object[fns.length][];
         for (int i = 0; i < fns.length; i++) {
             provider[i] = new Object[] { fns[i] };
         }
         return provider;
     }
 
     @Test (dataProvider = "testFunctionData")
     private void testFunction(final OxFunction fn) {
         boolean ok = fn.testFunction(_symbolTable, _constants, _functions, _operators);
        Assert.assertTrue(ok, "Function " + fn.toString() + " test failed.");
     }
 }
