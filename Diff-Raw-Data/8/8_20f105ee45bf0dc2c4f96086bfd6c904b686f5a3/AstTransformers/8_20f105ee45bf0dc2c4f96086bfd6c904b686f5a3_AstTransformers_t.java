 package sk.stuba.fiit.perconik.core.dom;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
 
 public final class AstTransformers
 {
 	private static final AstPathExtractor<?> namePathExtractor = AstPathExtractor.using(PathExtractorStrategy.NAME);
 	
 	private static final AstPathExtractor<?> typePathExtractor = AstPathExtractor.using(PathExtractorStrategy.TYPE);
 
 	private AstTransformers()
 	{
 		throw new AssertionError();
 	}
 
 	static final <N extends ASTNode> AstPathExtractor<N> internalNamePathExtractor()
 	{
 		// internal singleton has no state and only unbounded type parameters
 		// therefore it is safe to share the same instance across all types
 		@SuppressWarnings("unchecked")
 		AstPathExtractor<N> extractor = (AstPathExtractor<N>) namePathExtractor;
 		
 		return extractor;
 	}
 
 	static final <N extends ASTNode> AstPathExtractor<N> internalTypePathExtractor()
 	{
 		// internal singleton has no state and only unbounded type parameters
 		// therefore it is safe to share the same instance across all types
 		@SuppressWarnings("unchecked")
 		AstPathExtractor<N> extractor = (AstPathExtractor<N>) typePathExtractor;
 		
 		return extractor;
 	}
 
 	public static final <N extends ASTNode> AstCutter<N> cutterUsingFilter(final AstFilter<ASTNode> filter)
 	{
 		return AstCutter.using(filter);
 	}
 	
 	private static enum PathExtractorStrategy implements AstTransformer<ASTNode, String>
 	{
 		NAME
 		{
 			public final String transform(final ASTNode node)
 			{
 				if (node == null)
 				{
					return AstPathExtractor.unknownPathName();
 				}
 				
 				for (StructuralPropertyDescriptor descriptor: AstNodes.structuralProperties(node))
 				{
 					if (descriptor.getId().equals("name"))
 					{
 						return node.getStructuralProperty(descriptor).toString();
 					}
 				}
 				
				return AstPathExtractor.unknownPathName();
 			}
 		},
 		
 		TYPE
 		{
 			public final String transform(final ASTNode node)
 			{
				return node != null ? node.getClass().getSimpleName() : AstPathExtractor.unknownPathName();
 			}
 		};
 	}
 
 	public static final <N extends ASTNode> AstPathExtractor<N> namePathExtractor()
 	{
 		return internalNamePathExtractor();
 	}
 
 	public static final <N extends ASTNode> AstPathExtractor<N> typePathExtractor()
 	{
 		return internalTypePathExtractor();
 	}
 }
