 package test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import javax.script.ScriptException;
 import org.clafer.ast.AstModel;
import org.clafer.collection.Pair;
 import org.clafer.compiler.ClaferCompiler;
 import org.clafer.compiler.ClaferSolver;
 import org.clafer.javascript.Javascript;
 import org.clafer.scope.Scope;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author jimmy
  */
 public class PositiveTest {
 
     @Test
     public void testPositives() throws IOException, ScriptException, URISyntaxException {
         File dir = new File(PositiveTest.class.getResource("/positive").toURI());
         assertTrue(dir.isDirectory());
 
         for (File test : dir.listFiles()) {
            Pair<AstModel, Scope> p = Javascript.readModel(test);
             ClaferSolver s = ClaferCompiler.compile(p.getFst(), p.getSnd());
 
             assertTrue(test + " failed", s.find());
         }
     }
 }
