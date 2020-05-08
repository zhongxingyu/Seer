 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package test.org.modelcc.csm;
 
 
 import org.modelcc.csm.CSM;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.metamodel.*;
 
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author elezeta
  */
 public class CSMTest {
 
     private Model modelGen(Class cl) {
         JavaModelReader jmr = new JavaModelReader(cl);
         Model m = null;
 
         try {
             m = jmr.read();
         } catch (Exception ex) {
             Logger.getLogger(CSMTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return null;
         }
         return m;
     }
 
 	private Model CSMapply(Model m, String string) {
     	try {
     		return CSM.apply(m,string);
     	} catch (Exception e) {
     		e.printStackTrace();
     		assertTrue(false);
     	}
     	return null; 
 	}
     @Test
     public void CSMTest1() {
     		Model m = modelGen(test.languages.arithmeticcalculator.Expression.class);
     		Model m2 = CSMapply(m,"BinaryExpression freeorder=true;");
        	assertTrue(((ComplexModelElement)m2.getClassToElement().get(test.languages.arithmeticcalculator.Expression.class)).isFreeOrder());
     }
 
 
 }
