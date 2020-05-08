 /*
  * Thibaut Colar Feb 23, 2010
  */
 package net.colar.netbeans.fan.parboiled;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import net.colar.netbeans.fan.scope.FanAstScopeVar;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase.VarKind;
 import net.colar.netbeans.fan.scope.FanLocalScopeVar;
 import net.colar.netbeans.fan.scope.FanTypeScopeVar;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.parboiled.Node;
 import org.parboiled.support.InputBuffer;
 import org.parboiled.support.InputLocation;
 
 /**
  * This is the AST node, it's linked to it's parent and children AST Node
  * It also has a reference to the ParseNode it was created from
  * The parseNode has a cross reference to it's AstNode through (getValue)
  * So we can go back and force between parseNode and AstNode easily.
  * @author thibautc
  */
 public class AstNode
 {
 
 	/** Reference to the parse Node, this AST node was created from*/
 	private final Node<AstNode> parseNode;
 	/** ParseNode path*/
 	private final String parsePath;
 	/** kind of this AST Node*/
 	private final AstKind kind;
 	/** Node text */
 	private final String text;
 	/**lazy-init Text minus the 'non-relavant" parts (spacing/comments)*/
 	private String relevantText;
 	/**lazy-init Range of the relevant text*/
 	private OffsetRange relevantTextRange;
 	/** Children AST nodes */
 	private List<AstNode> children = new ArrayList<AstNode>();
 	/** Parent AST Node*/
 	private AstNode parent;
 	/**scope variables table (hash) - Null if not a scoping Node*/
 	private Hashtable<String, FanAstScopeVarBase> scopeVars = null;
 	/**Type of this node (say 'Str' or 'Bool') - used for completion, etc...*/
 	private FanResolvedType type;
 
 	public AstNode(AstKind kind, String path, Node<AstNode> parseNode, String nodeText)
 	{
 		this.parseNode = parseNode;
 		this.kind = kind;
 		this.parsePath = path;
 		this.text = nodeText;
 		// start unresolved, but ParserTask will fill it in.
 		this.type = FanResolvedType.makeUnresolved(this);
 	}
 
 	@Override
 	public String toString()
 	{
 		//ParseTreeUtils.getNodeText(parseNode, null)
 		StringBuffer txt = new StringBuffer(kind.toString());
 		txt.append("(");
 		txt.append(type == null ? "" : type);
 		txt.append(") ");
 		if (text.indexOf("\n") >= 0)
 		{
 			txt.append(text.substring(0, text.indexOf("\n")));
 		}
 		else
 		{
 			txt.append("'").append(text).append("'");
 		}
 		if (isScopeNode())
 		{
 			txt.append(" SCOPE{");
 			for (FanAstScopeVarBase var : getLocalScopeVars().values())
 			{
 				txt.append(var.toString()).append("; ");
 			}
 			txt.append("}");
 		}
 		//txt.append(" [").append(parsePath).append("]");
 		return txt.toString();
 	}
 
 	public String getParsePath()
 	{
 		return parsePath;
 	}
 
 	public String getLabel()
 	{
 		return parseNode.getLabel();
 	}
 
 	public InputLocation getStartLocation()
 	{
 		return parseNode.getStartLocation();
 	}
 
 	public InputLocation getEndLocation()
 	{
 		return parseNode.getEndLocation();
 	}
 
 	public AstNode getParent()
 	{
 		return parent;
 	}
 
 	public List<AstNode> getChildren()
 	{
 		return children;
 	}
 
 	void addChild(AstNode nd)
 	{
 		children.add(nd);
 	}
 
 	void removeChild(AstNode nd)
 	{
 		children.remove(nd);
 	}
 
 	public static void printNodeTree(AstNode nd, String inc, InputBuffer buffer)
 	{
 		if (nd != null)
 		{
 			System.out.println(inc + nd.toString() /*+ " "+ParseTreeUtils.getNodeText(nd.getParseNode(), buffer)*/);
 			for (AstNode subNode : nd.getChildren())
 			{
 				printNodeTree(subNode, inc + "  ", buffer);
 			}
 		}
 	}
 
 	void setParent(AstNode node)
 	{
 		this.parent = node;
 	}
 
 	public AstKind getKind()
 	{
 		return kind;
 	}
 
 	public Node<AstNode> getParseNode()
 	{
 		return parseNode;
 	}
 
 	public void setIsScopeNode()
 	{
 		scopeVars = new Hashtable<String, FanAstScopeVarBase>();
 	}
 
 	public boolean isScopeNode()
 	{
 		return scopeVars != null;
 	}
 
 	/**
 	 * Return the scope vars of THIS node only.
 	 */
 	public Hashtable<String, FanAstScopeVarBase> getLocalScopeVars()
 	{
 		return scopeVars;
 	}
 
 	public Hashtable<String, FanAstScopeVarBase> getAllScopeVars()
 	{
 		Hashtable<String, FanAstScopeVarBase> vars = new Hashtable<String, FanAstScopeVarBase>();
 		AstNode scope = FanLexAstUtils.getScopeNode(this);
 		while (scope != null)
 		{
 			if (scope.isScopeNode())
 			{
 				for (FanAstScopeVarBase var : scope.getLocalScopeVars().values())
 				{
 					if (!vars.containsKey(var.getName()))
 					{
 						vars.put(var.getName(), var);
 					}
 					if (var instanceof FanTypeScopeVar)
 					{
 						// Add inherited slots
 						FanTypeScopeVar typeVar = (FanTypeScopeVar) var;
 						Hashtable<String, FanAstScopeVarBase> inhVars = typeVar.getInheritedSlots();
 						for (FanAstScopeVarBase inhVar : inhVars.values())
 						{
 							if (!vars.containsKey(inhVar.getName()))
 							{
 								vars.put(inhVar.getName(), inhVar);
 							}
 						}
 					}
 				}
 			}
 			scope = scope.getParent();
 		}
 		return vars;
 	}
 
 	/**
 	 * Add a scope var to the closest scope.
 	 * @param name
 	 * @param varKind
 	 * @param type
 	 */
 	public void addScopeVar(String name, VarKind varKind, FanResolvedType type, boolean allowDuplicates)
 	{
 		//System.out.println("New scope var: "+name+":"+varKind+" -> "+type);
 		FanAstScopeVar var = new FanLocalScopeVar(this, varKind, name, type);
 		addScopeVar(var, allowDuplicates);
 	}
 
 	/**
 	 * Add a scope var to the closest scope.
 	 * @param name
 	 * @param varKind
 	 * @param type
 	 */
 	public void addScopeVar(FanAstScopeVarBase var, boolean allowDuplicates)
 	{
 		AstNode scopeNode = FanLexAstUtils.getScopeNode(this);
		if (scopeNode == null || var.getName() == null)
 		{
 			return;
 		}
 		if (!allowDuplicates && scopeNode.getLocalScopeVars().containsKey(var.getName()))
 		{
 			getRoot().getParserTask().addError("Duplicated variable in scope: " + (var==null?"null":var.getName()), this);
 		}
 		scopeNode.getLocalScopeVars().put(var.getName(), var);
 	}
 
 	// TODO: should juts lazy init 
 	public RootNode getRoot()
 	{
 		AstNode nd = this;
 		while (nd != null)
 		{
 			if (nd instanceof RootNode)
 			{
 				return (RootNode) nd;
 			}
 			nd = nd.getParent();
 		}
 		return null;
 	}
 
 	/**
 	 * Get the text content of the node
 	 * @param node
 	 * @return
 	 */
 	public String getNodeText(boolean relevantOnly)
 	{
 		if(relevantOnly && relevantText==null)
 			computeRelevant();
 		return relevantOnly ? relevantText : text;
 	}
 
 	public void setType(FanResolvedType type)
 	{
 		this.type = type;
 	}
 
 	public FanResolvedType getType()
 	{
 		return type;
 	}
 
 	public OffsetRange getRelevantTextRange()
 	{
 		if(relevantText==null)
 			computeRelevant();
 		return relevantTextRange;
 	}
 
 	private void computeRelevant()
 	{
 		// remove trailing spacing
 		relevantText=text;
 		boolean isNumber = false;
 		if(kind == AstKind.AST_EXPR_LIT_BASE &&
 				parseNode.getLabel().equalsIgnoreCase(FantomLexerTokens.TokenName.STRS.name()))
 			isNumber = true;
 		if(isNumber || kind==AstKind.AST_ID || kind==AstKind.AST_TYPE || kind==AstKind.AST_ENUM_NAME)
 		{
 			if(relevantText.indexOf("/*")>-1)
 				relevantText=relevantText.substring(0,relevantText.indexOf("/*"));
 			if(relevantText.indexOf("//")>-1)
 				relevantText=relevantText.substring(0,relevantText.indexOf("//"));
 		}
 		relevantText = relevantText.trim();
 		int startOffset = relevantText.length()>0?text.indexOf(relevantText.charAt(0)):0;
 		int endOffset = startOffset + relevantText.length();
 		relevantTextRange = new OffsetRange(
 				getStartLocation().getIndex()+startOffset,
 				getStartLocation().getIndex()+endOffset);
 	}
 
 
 }
