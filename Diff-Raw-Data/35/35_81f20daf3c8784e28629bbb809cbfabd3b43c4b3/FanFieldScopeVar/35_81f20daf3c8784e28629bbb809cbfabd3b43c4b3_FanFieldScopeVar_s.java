 /*
  * Thibaut Colar Mar 5, 2010
  */
 package net.colar.netbeans.fan.scope;
 
 import java.util.List;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.types.FanResolvedType;
 
 /**
  *
  * @author thibautc
  */
 public class FanFieldScopeVar extends FanAstScopeVarBase
 {
 	// the string the type was resolved from
 
 	protected String typeString;
 
        @SuppressWarnings("unchecked")
 	public FanFieldScopeVar(AstNode fieldNode, String name)
 	{
 		super(fieldNode, name);
 		this.kind = VarKind.FIELD;
 		if (node.getKind() == AstKind.AST_CTOR_DEF)
 		{
 			// ctor is always returning type sys::This
 			type = FanResolvedType.makeFromDbType(node, "sys::This");
 			if (type == null)
 			{
 				FanUtilities.GENERIC_LOGGER.error(getClass().getName() + " Null type for: " + node.getNodeText(true));
 			}
 		} else
 		{
 			AstNode typeNode = FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_TYPE));
 			typeString = typeNode.getNodeText(true);
 			type = FanResolvedType.makeFromTypeSigWithWarning(typeNode);
 			if (type == null)
 			{
 				FanUtilities.GENERIC_LOGGER.error(getClass().getName() + " Null type for: " + typeString);
 			}
 		}
 		if (type == null)
 		{
 			type = FanResolvedType.makeUnresolved(node);
 		}
 
 		type = type.asStaticContext(false);
 
 		node.setType(type);
 		//FanLexAstUtils.dumpTree(node, 0);
 		List<AstNode> modifs = FanLexAstUtils.getChildren(node, new NodeKindPredicate(AstKind.AST_MODIFIER));
 		for (AstNode m : modifs)
 		{
 			addModifiers(m.getNodeText(true));
 		}
 	}
 
 	public String getTypeString()
 	{
 		return typeString;
 	}
 }
