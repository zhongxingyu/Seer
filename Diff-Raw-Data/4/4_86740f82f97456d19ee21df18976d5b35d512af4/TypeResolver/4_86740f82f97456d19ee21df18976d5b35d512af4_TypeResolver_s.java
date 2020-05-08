 package uk.ac.ic.doc.gander.flowinference;
 
 import org.python.pydev.parser.jython.ast.exprType;
 
 import uk.ac.ic.doc.gander.flowinference.result.Result;
 import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
 import uk.ac.ic.doc.gander.flowinference.types.Type;
 import uk.ac.ic.doc.gander.model.ModelSite;
 
 public class TypeResolver {
 
 	private final TimingTypeEngine engine;
 
 	public TypeResolver(TimingTypeEngine engine) {
 		this.engine = engine;
 	}
 
 	public Type typeOf(ModelSite<exprType> expression) {
 
 		Result<Type> types = engine.typeOf(expression);
 
 		return types.transformResult(new Singletoniser());
 	}
 
 	private final class Singletoniser implements Transformer<Type, Type> {
 
 		@Override
 		public Type transformFiniteResult(java.util.Set<Type> result) {
 			if (result.size() == 1) {
 				return result.iterator().next();
 			} else {
 				System.err.println("Oh dear, not a singleton: " + result);
 				return null;
 			}
 		}
 
 		@Override
 		public Type transformInfiniteResult() {
 			System.err.println("Oh dear, Top");
 			return null;
 		}
 	}
 
 	public long flowCost() {
 		return engine.milliseconds();
 	}
 }
