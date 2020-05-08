 package gate.languages.rt;
 
 import gate.AnnotationSet;
 import gate.Corpus;
 import gate.Document;
 import gate.FeatureMap;
 import gate.Gate;
 import gate.creole.ExecutionException;
 import gate.creole.ResourceInstantiationException;
 
 import java.io.File;
 import java.net.URI;
 
 import clojure.lang.Compiler;
 import clojure.lang.RT;
 import clojure.lang.Var;
 
 /**
  * Language runtime implementation for Clojure
  * @author Žygimantas Medelis zygimantas.medelis@tokenmill.lt
  *
  */
 public class Clojure implements LanguageRuntime {
 
 	@Override
 	public Object execute(Document document, Corpus corpus, String content, AnnotationSet inputAS, AnnotationSet outputAS, FeatureMap scriptParams) throws ExecutionException {
 		try {
 			Var foo = RT.var("user", EXECUTE_METHOD_NAME);
 			return foo.invoke(document, corpus, content, inputAS,
 					outputAS, scriptParams);
 		} catch (Exception re) {
 			throw new ExecutionException(re.getMessage(), re);
 		}
 	}
 
 	@Override
 	public void load(URI script) throws ResourceInstantiationException {
 		try {
 			Thread.currentThread().setContextClassLoader(Gate.getClassLoader());
      //http://stackoverflow.com/questions/15207596/npe-in-clojure-lang-compiler-when-trying-to-load-a-resource
      RT.box(1);
 			Compiler.loadFile(new File(script).getPath());
 		} catch (Exception e) {
 			throw new ResourceInstantiationException(e.getMessage(), e);
 		}
 	}
}
