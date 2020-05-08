 package cd.semantic.ti.constraintSolving;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import cd.ir.symbols.TypeSymbol;
 import cd.semantic.TypeSymbolTable;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Constructs and caches constant type sets.
  * 
  * It avoids instantiating the same constant type sets for a given type symbol
  * table over and over again, especially the ones that only contain one type.
  * Besides, it also provides commonly-used constant type sets, for instance for
  * all numerical, array and reference types.
  */
 public class ConstantTypeSetFactory {
 
 	private final TypeSymbolTable typeSymbols;
 	private final Map<TypeSymbol, ConstantTypeSet> singletonTypeSets;
 
 	private ConstantTypeSet numericalTypeSet;
 	private ConstantTypeSet referenceTypeSet;
 	private ConstantTypeSet arrayTypeSet;
 
 	/**
 	 * Constructs a new factory for constant type symbols.
 	 * 
 	 * @param typeSymbols
	 *            The table with all type symbols to construct the sets from. it
	 *            It must be final at this point in time.
 	 */
 	public ConstantTypeSetFactory(TypeSymbolTable typeSymbols) {
 		super();
 		this.typeSymbols = checkNotNull(typeSymbols);
 		this.singletonTypeSets = new HashMap<>();
 	}
 
 	/**
 	 * Returns a constant type set for a single type.
 	 */
 	public ConstantTypeSet make(TypeSymbol typeSymbol) {
 		checkNotNull(typeSymbol);
 
 		ConstantTypeSet result = singletonTypeSets.get(typeSymbol);
 		if (result == null) {
 			result = new ConstantTypeSet(typeSymbol);
 			singletonTypeSets.put(typeSymbol, result);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a constant type set containing only the boolean type.
 	 */
 	public ConstantTypeSet makeBoolean() {
 		return make(typeSymbols.getBooleanType());
 	}
 
 	/**
 	 * Returns a constant type set containing only the integer type.
 	 */
 	public ConstantTypeSet makeInt() {
 		return make(typeSymbols.getIntType());
 	}
 
 	/**
 	 * Returns a constant type set containing only the float type.
 	 */
 	public ConstantTypeSet makeFloat() {
 		return make(typeSymbols.getFloatType());
 	}
 
 	/**
 	 * Returns a constant type set containing all numerical types.
 	 */
 	public ConstantTypeSet makeNumericalTypeSet() {
 		if (numericalTypeSet == null) {
 			numericalTypeSet = new ConstantTypeSet(
 					typeSymbols.getNumericalTypeSymbols());
 		}
 		return numericalTypeSet;
 	}
 
 	/**
 	 * Returns a constant type set containing all reference types.
 	 */
 	public ConstantTypeSet makeReferenceTypeSet() {
 		if (referenceTypeSet == null) {
 			referenceTypeSet = new ConstantTypeSet(
 					typeSymbols.getReferenceTypeSymbols());
 		}
 		return referenceTypeSet;
 	}
 
 	/**
 	 * Returns a constant type set containing all array types.
 	 */
 	public ConstantTypeSet makeArrayTypeSet() {
 		if (arrayTypeSet == null) {
 			arrayTypeSet = new ConstantTypeSet(
 					typeSymbols.getArrayTypeSymbols());
 		}
 		return arrayTypeSet;
 	}
 
 }
