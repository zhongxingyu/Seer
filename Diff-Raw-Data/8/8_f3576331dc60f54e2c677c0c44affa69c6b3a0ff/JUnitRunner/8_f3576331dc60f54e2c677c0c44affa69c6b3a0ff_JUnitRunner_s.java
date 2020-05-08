 import org.junit.runner.RunWith;
 import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
 import org.rascalmpl.interpreter.Evaluator;
 import org.rascalmpl.test.infrastructure.IRascalJUnitTestSetup;
 import org.rascalmpl.test.infrastructure.RascalJUnitTestPrefix;
 import org.rascalmpl.test.infrastructure.RascalJUnitTestRunner;
import org.rascalmpl.uri.ClassResourceInputOutput;
 import org.rascalmpl.uri.URIResolverRegistry;
 import org.rascalmpl.uri.URIUtil;
 
 @RunWith(RascalJUnitTestRunner.class)
 @RascalJUnitTestPrefix("tests")
 public class JUnitRunner implements IRascalJUnitTestSetup {
   @Override
   public void setup(Evaluator eval) {
     URIResolverRegistry resolverRegistry = eval.getResolverRegistry();
    resolverRegistry.registerInput(new ClassResourceInputOutput(resolverRegistry, "rascal-tests", getClass(), ""));
     eval.addRascalSearchPath(URIUtil.rootScheme("rascal-tests"));
     
    ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
     resolverRegistry.registerInput(eclipseResolver);
     
     eval.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
     eval.addClassLoader(RascalScriptInterpreter.class.getClassLoader());
   }
 }
