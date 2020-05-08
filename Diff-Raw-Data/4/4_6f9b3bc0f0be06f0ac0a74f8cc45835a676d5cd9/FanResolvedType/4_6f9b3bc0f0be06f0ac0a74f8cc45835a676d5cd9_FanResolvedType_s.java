 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.types;
 
 import java.lang.reflect.Member;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.FanIndexerFactory;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.indexer.model.FanTypeInheritance;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase.VarKind;
 import net.colar.netbeans.fan.scope.FanTypeScopeVar;
 import org.parboiled.google.base.Predicate;
 
 /**
  * Resolved type - Immutable (since we cache the base type, and we need static/nullable copy at time)
  * Store infos of a resolved type
  * Also contains the factory methods / logic to resolve a type expr.
  *
  * All ResolvedType are storred unparameterized (generic stays in generic form)
  * Use parameterize() to get a parameterizer (shallow) copy.
  *
  * @author tcolar
  */
 public class FanResolvedType implements Cloneable
 {
 
 	public enum TypeKind
 	{
 
 		SIMPLE, LIST, MAP, FUNC
 	}
 	private final String asTypedType;
 	private final FanType dbType;
 	private final String shortAsTypedType;
 	private final AstNode scopeNode;
 	// whether it's used in a  nullable context : ex: Str? vs Str
 	private boolean nullableContext = false;
 	// whether it's used in a static context: ex Str. vs str.
 	private boolean staticContext = false;
 
 	protected FanResolvedType(AstNode scopeNode, String enteredType, FanType type)
 	{
 		this(scopeNode, enteredType, type, false, false);
 	}
 
 	protected FanResolvedType(AstNode scopeNode, String enteredType, FanType type, boolean asStatic, boolean asNullable)
 	{
 		this.scopeNode = scopeNode;
 		this.asTypedType = enteredType;
 		shortAsTypedType = asTypedType.indexOf("::") != -1 ? asTypedType.substring(asTypedType.indexOf("::") + 2) : asTypedType;
 		dbType = type;
 		// Java types are always nullable
 		if (dbType != null && dbType.isJava())
 		{
 			nullableContext = true;
 		}
 	}
 
 	/**
 	 * Wether it was resolved correctly or not
 	 * @return
 	 */
 	public boolean isResolved()
 	{
 		return dbType != null;
 	}
 
 	/**
 	 * Shortcut for dbType.getQName()
 	 * @return
 	 */
 	public String getQualifiedType()
 	{
 		return dbType == null ? null : dbType.getQualifiedName();
 	}
 
 	/**
 	 * Return the backend Type object, this type was resolved to
 	 * @return
 	 */
 	public FanType getDbType()
 	{
 		return dbType;
 	}
 
 	/**
 	 * The original text we resolved this type from
 	 * @return
 	 */
 	public String getAsTypedType()
 	{
 		return asTypedType;
 	}
 
 	/**
 	 * The short name of the type (ie: no pod/package)
 	 * @return
 	 */
 	public String getShortAsTypedType()
 	{
 		return shortAsTypedType;
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder(dbType == null ? "null" : toTypeSig(true)).append(" r:").append(isResolved()).append(" s:").append(isStaticContext()).append(" n:").append(isNullable());
 		return sb.toString();
 	}
 
 	/**
 	 * Create an unresolved type (no dbType)
 	 * @param node
 	 * @return
 	 */
 	public static FanResolvedType makeUnresolved(AstNode node)
 	{
 		return new FanResolvedType(node, FanIndexer.UNRESOLVED_TYPE, null);
 	}
 
 	/**
 	 * Make a type from a signature (@see fromTypeSig())
 	 * Add an error message to the parsertask if unresolved
 	 * @param node
 	 * @return
 	 */
 	public static FanResolvedType makeFromTypeSigWithWarning(AstNode node)
 	{
 		FanResolvedType result = makeFromTypeSig(node, node.getNodeText(true));
 		if (!result.isResolved())
 		{
 			String type = node.getNodeText(true);
 			//TODO: Propose to auto-add using statements (Hints)
 			node.getRoot().getParserTask().addError("Unresolved type: " + type, node);
 			FanUtilities.GENERIC_LOGGER.info("Could not resolve type: " + (node == null ? "null" : node.toString()));
 		}
 		return result;
 	}
 
 	/**
 	 * Resolve the return type of a slot of the baseType(and it's supertypes), using the DB
 	 * @param baseType
 	 * @param slotName
 	 * @typeSlotsCache : optional 
 	 * @return
 	 */
 	public FanResolvedType resolveSlotType(String slotName, FanParserTask task)
 	{
 		FanResolvedType baseType = this;
 
 		FanResolvedType slotBaseType = resolveSlotBaseType(slotName, task);
 		if (slotBaseType.isResolved())
 		{
 			if (baseType.getDbType().isJava())
 			{
 				List<Member> members = FanIndexerFactory.getJavaIndexer().findTypeSlots(slotBaseType.getQualifiedType());
 				for (Member member : members)
 				{
 					if (member.getName().equalsIgnoreCase(slotName))
 					{
 						return makeFromTypeSig(baseType.scopeNode, FanIndexerFactory.getJavaIndexer().getReturnType(member));
 					}
 				}
 			} else
 			{
 				if (slotBaseType.getDbType().isEnum())
 				{
 					// TODO: index Enum values.
					// Always of type is Obj?
					return makeFromTypeSig(scopeNode, "sys::Obj?");
 				}
 				FanSlot slot = FanSlot.findByTypeAndName(slotBaseType.getQualifiedType(), slotName);
 				FanResolvedType t = makeFromTypeSig(baseType.scopeNode, slot.returnedType);
 				return t;
 			}
 		}
 		return makeUnresolved(baseType.scopeNode);
 	}
 
 	/**
 	 * Find the type a slot is defined in
 	 *	IE: BaseType or any of it's super types
 	 * @param baseType
 	 * @param slotName
 	 * @param task
 	 * @return
 	 */
 	public FanResolvedType resolveSlotBaseType(String slotName, FanParserTask task)
 	{
 		FanResolvedType baseType = this;
 
 		// slot resoltuion need to be made of "base" types (no lists, maps)
 		if (baseType instanceof FanResolvedGenericType)
 		{
 			baseType = ((FanResolvedGenericType) baseType).getPhysicalType();
 		} else if (baseType instanceof FanResolvedListType)
 		{
 			baseType = FanResolvedListType.makeFromDbType(scopeNode, "sys::List");
 		} else if (baseType instanceof FanResolvedMapType)
 		{
 			baseType = FanResolvedListType.makeFromDbType(scopeNode, "sys::Map");
 		} else if (baseType instanceof FanResolvedFuncType)
 		{
 			baseType = FanResolvedListType.makeFromDbType(scopeNode, "sys::Func");
 		}
 
 		if (baseType == null || !baseType.isResolved()
 			|| baseType.dbType.getQualifiedName().equals("sys::Void")) // Void extends from object ... but not callable
 		{
 			return FanResolvedType.makeUnresolved(null);
 		}
 		if (baseType.getDbType().isJava())
 		{
 			// java slots
 			List<Member> members = FanIndexerFactory.getJavaIndexer().findTypeSlots(baseType.getDbType().getQualifiedName());
 			// Returning the first match .. because java has overloading this could be wrong
 			// However I assume overloaded methods return the same type (If it doesn't too bad, it's ugly code anyway :) )
 			for (Member member : members)
 			{
 				if (member.getName().equalsIgnoreCase(slotName))
 				{
 					FanType slotBaseType = FanType.findByQualifiedName(member.getClass().getName());
 					return makeFromTypeSig(scopeNode, slotBaseType.getQualifiedName());
 				}
 			}
 		} else
 		{
 			if (baseType.getDbType().isEnum())
 			{
 				// TODO: Index enums values
 				return baseType;
 			}
 			// Fan slots
 			for (FanSlot slot : FanSlot.getAllSlotsForType(baseType.getDbType().getQualifiedName(), true, task))
 			{
 				if (slot.getName().equals(slotName))
 				{
 					FanType slotBaseType = FanType.findByID(slot.getTypeId());
 					return makeFromTypeSig(scopeNode, slotBaseType.getQualifiedName());
 				}
 			}
 		}
 		return makeUnresolved(baseType.scopeNode);
 	}
 
 	private static boolean isGenericType(String enteredType)
 	{
 		String qt = enteredType;
 		if (qt.startsWith("sys::"))
 		{
 			qt = enteredType.substring(5);
 		}
 		return qt.length() == 1
 			&& Character.toUpperCase(qt.charAt(0)) == qt.charAt(0);
 	}
 
 	/**
 	 * Wether this is a staitc variable or an instance
 	 * Example : "Str" -> 'Str' is of type Str and is a static type
 	 * 'Str s' -> s is an Str too, but is not static (an instance)
 	 * @return
 	 */
 	public boolean isStaticContext()
 	{
 		return staticContext;
 	}
 
 	/**
 	 * Return a copy with nullable context set as requested
 	 * @param nullable
 	 * @return
 	 */
 	public FanResolvedType asNullableContext(boolean nullable)
 	{
 		if (nullableContext == nullable)
 		{
 			return this;
 		}
 		try
 		{
 			FanResolvedType copy = (FanResolvedType) clone();
 			copy.nullableContext = nullable;
 			return copy;
 		} catch (CloneNotSupportedException e)
 		{
 			FanUtilities.GENERIC_LOGGER.exception("Clone error", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Wether it's a nullable type, such as Obj?
 	 * @return
 	 */
 	public boolean isNullable()
 	{
 		return nullableContext;
 	}
 
 	public FanResolvedType asStaticContext(boolean b)
 	{
 		if (staticContext == b)
 		{
 			return this;
 		}
 		try
 		{
 			FanResolvedType copy = (FanResolvedType) clone();
 			copy.staticContext = b;
 			return copy;
 		} catch (CloneNotSupportedException e)
 		{
 			FanUtilities.GENERIC_LOGGER.exception("Clone error", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Resolve the "local" type, either "it" if an itBlock
 	 * If not in an in block just return "this" type
 	 * @param node
 	 * @return
 	 */
 	public static FanResolvedType resolveItType(AstNode node)
 	{
 		FanAstScopeVarBase var = node.getAllScopeVars().get("it");
 		if (var != null)
 		{
 			return var.getType();
 		} else
 		{
 			return resolveThisType(node);
 		}
 	}
 
 	/**
 	 * Resolve the type of "this" for a specific node (within the type definition).
 	 * @param node
 	 * @return
 	 */
 	public static FanResolvedType resolveThisType(AstNode node)
 	{
 		AstNode typeNode = FanLexAstUtils.findParentNode(node, AstKind.AST_TYPE_DEF);
 		if (typeNode != null)
 		{
 			return typeNode.getType();
 		}
 		return makeUnresolved(node);
 	}
 
 	/**
 	 * Resolve the type of "super" for a given node (within type def.)
 	 * ie: the superclass of the type whe are in
 	 * @param node
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static FanResolvedType resolveSuper(AstNode node)
 	{
 		AstNode typeNode = FanLexAstUtils.findParentNode(node, AstKind.AST_TYPE_DEF);
 		if (typeNode != null)
 		{
 			String name = FanLexAstUtils.getFirstChildText(typeNode, new NodeKindPredicate(AstKind.AST_ID));
 			if (name != null)
 			{
 				FanAstScopeVarBase var = node.getRoot().getLocalScopeVars().get(name);
 				if (var != null && var instanceof FanTypeScopeVar)
 				{
 					if (var.getKind() == VarKind.TYPE_MIXIN || var.getKind() == VarKind.TYPE_ENUM)
 					{
 						node.getRoot().getParserTask().addError("Cannot use 'super' within a mixin or enum.", node);
 					} else
 					{
 						return var.getType().getParentType();
 					}
 				}
 			}
 		}
 
 		return makeFromDbType(node, "sys::Obj");
 	}
 
 	/**
 	 * "Serialize" the type into a db signature
 	 * Overloaded as needed in subclasses
 	 * @param fullyQualified
 	 * @return
 	 */
 	public String toTypeSig(boolean fullyQualified)
 	{
 		String fq;
 		if (isResolved())
 		{
 			if (fullyQualified)
 			{
 				fq = dbType.getQualifiedName();
 			} else
 			{
 				fq = dbType.getSimpleName();
 			}
 		} else if (fullyQualified)
 		{
 			fq = getAsTypedType();
 		} else
 		{
 			fq = getShortAsTypedType();
 		}
 		if (isNullable())
 		{
 			fq += "?";
 		}
 		//if(fq.indexOf("n/a")>=0)
 		//	System.out.println("breakpoint");
 		return fq;
 	}
 
 	/**
 	 * Create type from the a type signature ("Serialized" dbType)
 	 * ie: sys::Str   or sys::Str? or sys::Str[]?
 	 * @param sig
 	 * @return
 	 */
 	public static FanResolvedType makeFromTypeSig(AstNode scopeNode, String sig)
 	{
 		// this results in infinite recursion, so check for it.
 		if (sig == null || sig.length() == 0)
 		{
 			throw new RuntimeException("Calling fromTypeSig with invalid sig");
 		}
 
 		FanResolvedType type = makeUnresolved(scopeNode);
 		boolean nullable = false;
 		boolean list = false;
 		boolean nullableList = false;
 		if (sig.endsWith("?"))
 		{
 			sig = sig.substring(0, sig.length() - 1);
 			nullable = true;
 		}
 		if (sig.endsWith("[]"))
 		{
 			sig = sig.substring(0, sig.length() - 2);
 			list = true;
 			if (sig.endsWith("?"))
 			{
 				sig = sig.substring(0, sig.length() - 1);
 				nullableList = true;
 			}
 		}
 		if (sig.startsWith("[") && sig.endsWith("]"))
 		{// Full map
 			sig = sig.substring(1, sig.length() - 1);
 			int index = findMapSeparatorIndex(sig);
 			if (index != -1 && index < sig.length() - 1)
 			{
 				FanResolvedType keyType = makeFromTypeSig(scopeNode, sig.substring(0, index).trim());
 				FanResolvedType valType = makeFromTypeSig(scopeNode, sig.substring(index + 1).trim());
 				type = new FanResolvedMapType(scopeNode, keyType, valType);
 			}
 		} else if (sig.startsWith("|") && sig.endsWith("|"))
 		{
 			Vector<FanResolvedType> types = new Vector<FanResolvedType>();
 			sig = sig.substring(1, sig.length() - 1);
 			// Without -1 it will drop empty strings, causing |->| to have a zero length result array.
 			String[] parts = sig.split("->", -1);
 			String[] typeParts = parts[0].split(",");
 			for (int i = 0; i != typeParts.length; i++)
 			{
 				String formal = typeParts[i].trim();
 				String formalType = formal;
 				//String formalName = "";
 				int idx = formal.indexOf(" ");
 				if (idx != -1)
 				{
 					formalType = formal.substring(0, idx).trim();
 					//formalName = formal.substring(idx).trim();
 				}
 				if (formalType.length() == 0)
 				{
 					formalType = "sys::Void";
 				}
 				types.add(makeFromTypeSig(scopeNode, formalType));
 			}
 			String returnType = "sys::Void"; // Default if not specified
 			if (parts.length == 2)
 			{
 				// for a sig without a return type like |->| we keep returnType as Void
 				if (parts[1].trim().length() > 0)
 				{
 					returnType = parts[1].trim();
 				}
 			}
 			type = new FanResolvedFuncType(scopeNode, types, makeFromTypeSig(scopeNode, returnType));
 		} else
 		{	// check for simple map type like Sys:Int
 			int index = findMapSeparatorIndex(sig);
 			if (index != -1 && index < sig.length() - 1)
 			{
 				FanResolvedType keyType = makeFromTypeSig(scopeNode, sig.substring(0, index).trim());
 				FanResolvedType valType = makeFromTypeSig(scopeNode, sig.substring(index + 1).trim());
 				type = new FanResolvedMapType(scopeNode, keyType, valType);
 			} else
 			{
 				// true simple type like Int or Sys::Int
 				type = resolveInScope(scopeNode, sig);
 			}
 		}
 
 		if (nullable)
 		{
 			type = type.asNullableContext(true);
 		}
 		if (list)
 		{
 			type = new FanResolvedListType(scopeNode, type);
 			if (nullableList)
 			{
 				type = type.asNullableContext(true);
 			}
 		}
 		return type;
 	}
 
 	/**
 	 * Resolve a basic type(no list, map, function) in scope
 	 * - Try a fully resolved type (from db)
 	 * - Try to find in the current scope
 	 * - Try to find from implicit imported types other types in same pod, sys pod
 	 * - Try to resolve Genric types (L, V etc...)
 	 * - Fallbnack to unresolved if failed resolving
 	 * @param scopeNode
 	 * @param enteredType
 	 * @return
 	 */
 	private static FanResolvedType resolveInScope(AstNode scopeNode, String enteredType)
 	{
 		//System.out.println("Make from local type: "+enteredType);
 		boolean toStatic = false;
 		FanType type = null;
 		if (enteredType.indexOf("::") != -1 && !isGenericType(enteredType))
 		{	// Qualified type
 			type = scopeNode.getRoot().getParserTask().findCachedQualifiedType(enteredType);
 			toStatic = true;
 		} else
 		{
 			Hashtable<String, FanAstScopeVarBase> types = scopeNode.getAllScopeVars();
 			if (types.containsKey(enteredType))
 			{
 				return types.get(enteredType).getType();
 			}
 			// If not found in scope, try "implicit" imports
 			if (type == null)
 			{
 				// first, other types in this pod
 				type = scopeNode.getRoot().getParserTask().findCachedQualifiedType(scopeNode.getRoot().getPod() + "::" + enteredType);
 				toStatic = true;
 			}
 			// if still not found try in "sys" pod
 			if (type == null)
 			{
 				type = scopeNode.getRoot().getParserTask().findCachedQualifiedType("sys::" + enteredType);
 				toStatic = true;
 			}
 			// Deal with Generic types
 			if (type == null && isGenericType(enteredType))
 			{
 				if (!enteredType.startsWith("sys::"))
 				{
 					enteredType = "sys::" + enteredType;
 				}
 				return new FanResolvedGenericType(scopeNode, enteredType);
 			}
 			if (type == null)
 			{
 				if (type == null)
 				{
 					return makeUnresolved(scopeNode);
 				}
 			}
 		}
 		FanResolvedType result = new FanResolvedType(scopeNode, enteredType, type);
 		if (toStatic)
 		{
 			result = result.asStaticContext(true);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Make a type from the type database (doesn't look at the local scope)
 	 * (external type, like a 'using')
 	 * Does NOT support generics (as those are not real types stored in the DB)
 	 * @param node
 	 * @param qualifiedType
 	 * @return
 	 */
 	public static FanResolvedType makeFromDbType(AstNode node, String qualifiedType)
 	{
 		FanType type = null;
 		if (node == null)
 		{	// typically, shouldn't be null, but protect in case it is
 			type = FanType.findByQualifiedName(qualifiedType);
 		} else
 		{
 			type = node.getRoot().getParserTask().findCachedQualifiedType(qualifiedType);
 		}
 		return new FanResolvedType(node, qualifiedType, type);
 	}
 
 	/**
 	 * Lookup where the separator of a map pair is (":")
 	 * @param sig
 	 * @return
 	 */
 	private static int findMapSeparatorIndex(String sig)
 	{
 		int index = 0;
 		while (index != -1 && index < sig.length())
 		{
 			index = sig.indexOf(":", index);
 			if (index == -1)
 			{
 				break; // not found
 			}				// looking for ":" but NOT "::"
 			if (index > 1 && index < sig.length() - 1 && sig.charAt(index - 1) != ':' && sig.charAt(index + 1) != ':')
 			{
 				break; // found
 			}
 			// try next one
 			index++;
 		}
 		return index;
 	}
 
 	/**
 	 * Check whether current type is compatible with baseType
 	 * In other words, whether type is of the same type as baseType or inherits from it
 	 * @param baseType
 	 */
 	public boolean isTypeCompatible(FanResolvedType baseType)
 	{
 		FanResolvedType t = this;
 		while (t != null)
 		{
 			if (this instanceof FanResolvedNullType)
 			{
 				return baseType.isNullable();
 			}
 			if (this instanceof FanUnknownType || baseType instanceof FanUnknownType)
 			{
 				return true;
 			}
 			// TODO: might not work for generics
 			if (t.getDbType().getQualifiedName().equals(baseType.getDbType().getQualifiedName())
 				&& t.getClass().getName().equals(baseType.getClass().getName()))
 			{
 				return true;
 			}
 			t = t.getParentType();
 		}
 		return false;
 	}
 
 	/**
 	 * Get the parent type of type (class it inherits from)
 	 * @param type
 	 * @return
 	 */
 	public FanResolvedType getParentType()
 	{
 		if (this instanceof FanUnknownType)
 		{
 			return this;
 		}
 		if (!this.isResolved())
 		{
 			return makeUnresolved(scopeNode);
 		}
 		if (this instanceof FanResolvedNullType)
 		{
 			return makeFromTypeSig(scopeNode, "sys::Obj?");
 		}
 		if (getDbType().isEnum())
 		{
 			return makeFromTypeSig(scopeNode, "sys::Enum");
 		}
 		if (getDbType().isMixin())
 		{
 			return null;
 		}
 
 		Vector<FanTypeInheritance> inhs = FanTypeInheritance.findAllForMainType(null, getDbType().getQualifiedName());
 		for (FanTypeInheritance inh : inhs)
 		{
 			FanResolvedType t = FanResolvedType.makeFromDbType(getScopeNode(), inh.getInheritedType());
 			if (t.isResolved() && t.getDbType().isClass())
 			{
 				return t;
 			}
 		}
 		if (!getDbType().getQualifiedName().equals("sys::Obj")
 			&& !getDbType().isMixin())
 		{
 			return FanResolvedType.makeFromDbType(getScopeNode(), "sys::Obj");
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a type with the most "generic" type common to all the items
 	 * For example {Int, Float} would give Num
 	 * @param itemsNode
 	 * @param items
 	 * @return
 	 */
 	public static FanResolvedType makeFromItemList(AstNode itemsNode, List<FanResolvedType> items)
 	{
 		if (items.size() == 0)
 		{
 			return makeFromTypeSig(itemsNode, "sys::Obj?");
 		}
 		boolean nullable = false;
 		FanResolvedType best = null;
 		for (FanResolvedType item : items)
 		{
 			if (item == null)
 			{
 				nullable = true;
 				continue;
 			}
 			FanResolvedType t = item;
 			if (best == null)
 			{
 				best = t;
 				continue;
 			}
 			while (!t.isTypeCompatible(best)) // exetnds
 			{
 				best = best.getParentType(); // get parent
 				if (best == null)
 				{
 					return nullable ? makeFromTypeSig(itemsNode, "sys::Obj?") : makeFromTypeSig(itemsNode, "sys::Obj");
 				}
 			}
 		}
 		if (best == null)
 		{
 			best = makeFromTypeSig(itemsNode, "sys::Obj");
 		}
 		if (nullable)
 		{
 			best = best.asNullableContext(true);
 		}
 		return best;
 	}
 
 	public AstNode getScopeNode()
 	{
 		return scopeNode;
 	}
 
 	/**
 	 * Take this type and return a parameterized version
 	 * (parameterize it against baseType)
 	 * @param baseType
 	 * @param genericType
 	 * @return
 	 */
 	public FanResolvedType parameterize(FanResolvedType baseType, AstNode errNode)
 	{
 		// Deal with generics
 		if (this instanceof FanResolvedGenericType)
 		{
 			int col = getAsTypedType().indexOf("::");
 			String n = getAsTypedType().substring(col + 2);
 			FanResolvedType t = makeUnresolved(null);
 			if (n.equals("L") && baseType instanceof FanResolvedListType)
 			{	// list type
 				t = baseType;
 			} else if (n.equals("V") && baseType instanceof FanResolvedListType)
 			{	// list value
 				t = ((FanResolvedListType) baseType).getItemType();
 			} else if (n.equals("M") && baseType instanceof FanResolvedMapType)
 			{	// map type
 				t = baseType;
 			} else if (n.equals("K") && baseType instanceof FanResolvedMapType)
 			{	// map key
 				t = ((FanResolvedMapType) baseType).getKeyType();
 			} else if (n.equals("V") && baseType instanceof FanResolvedMapType)
 			{	// map value
 				t = ((FanResolvedMapType) baseType).getValType();
 			} else if (n.equals("R") && baseType instanceof FanResolvedFuncType)
 			{	// function return value
 				t = ((FanResolvedFuncType) baseType).getRetType();
 			} else if (n.equals("R") && baseType.getQualifiedType().equals("sys::Func"))
 			{
 				t = baseType;
 			} else if (baseType instanceof FanResolvedFuncType)
 			{	// function value (should be A-H)
 				t = parameterizeFuncParam(((FanResolvedFuncType) baseType), n);
 			} else
 			{	// Not good
 				errNode.getRoot().getParserTask().addError("Invalid Generic type for " + baseType.getQualifiedType(), errNode);
 			}
 			return t.asStaticContext(false);
 		}
 		// Special case for "This"
 		if (isResolved() && getDbType().getQualifiedName().equals("sys::This"))
 		{
 			return baseType.asStaticContext(false);
 		}
 		// Otherwise, leave it alone.
 		return this;
 	}
 
 	private FanResolvedType parameterizeFuncParam(FanResolvedFuncType baseType, String letter)
 	{
 		List<FanResolvedType> types = baseType.getTypes();
 		FanResolvedType t = makeUnresolved(null);
 		int index = -1;
 		if (letter.equals("A"))
 		{
 			index = 0;
 		} else if (letter.equals("B"))
 		{
 			index = 1;
 		} else if (letter.equals("C"))
 		{
 			index = 2;
 		} else if (letter.equals("D"))
 		{
 			index = 3;
 		} else if (letter.equals("E"))
 		{
 			index = 4;
 		} else if (letter.equals("F"))
 		{
 			index = 5;
 		} else if (letter.equals("G"))
 		{
 			index = 6;
 		} else if (letter.equals("H"))
 		{
 			index = 7;
 		}
 
 		if (index == -1)
 		{
 			getScopeNode().getRoot().getParserTask().addError("Invalid Generic type for " + baseType.getQualifiedType(), getScopeNode());
 		} else if (types.size() < index)
 		{
 			getScopeNode().getRoot().getParserTask().addError("Generic '" + letter + "', but only " + types.size() + " func params in " + baseType.getQualifiedType(), getScopeNode());
 		} else
 		{
 			t = types.get(index);
 		}
 		return t;
 	}
 }
