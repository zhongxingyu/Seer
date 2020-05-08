 package titocc.compiler;
 
 /**
  * Class for symbols that are only used internally by the compiler, like code
  * positions inside functions etc.
  */
 public class InternalSymbol implements Symbol
 {
	private String name, globallyUniqueName, suffix;
 
 	/**
 	 * Constructs a new internal symbol. The actual name of the symbol is
 	 * prefixed with double underscore, because C standard reserves those
 	 * identifiers for implementation.
 	 *
 	 * @param name name of the symbol
 	 * @param scope scope this symbol belongs to
 	 * @param referenceSuffix suffix that is added to global name to get the
 	 * reference ("(fp)" can be used for stack frame variables)
 	 */
 	public InternalSymbol(String name, Scope scope, String referenceSuffix)
 	{
 		this.name = "__" + name;
 		this.globallyUniqueName = scope.makeGloballyUniqueName(name);
		this.suffix = suffix;
 	}
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	@Override
 	public String getGlobalName()
 	{
 		return globallyUniqueName;
 	}
 
 	@Override
 	public String getReference()
 	{
		return globallyUniqueName + suffix;
 	}
 }
