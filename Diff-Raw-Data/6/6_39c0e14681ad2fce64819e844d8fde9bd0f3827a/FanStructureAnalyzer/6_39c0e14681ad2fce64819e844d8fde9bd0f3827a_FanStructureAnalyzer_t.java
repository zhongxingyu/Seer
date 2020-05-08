 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.structure;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import net.colar.netbeans.fan.FanParserResult;
 import net.colar.netbeans.fan.antlr.FanParser;
 import net.colar.netbeans.fan.ast.FanAstField;
 import net.colar.netbeans.fan.ast.FanAstMethod;
 import net.colar.netbeans.fan.ast.FanAstScope;
 import net.colar.netbeans.fan.ast.FanAstScopeVarBase;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import net.colar.netbeans.fan.ast.FanRootScope;
 import net.colar.netbeans.fan.ast.FanTypeScope;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import org.antlr.runtime.CommonToken;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.netbeans.modules.csl.api.ElementKind;
 import org.netbeans.modules.csl.api.Modifier;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.StructureItem;
 import org.netbeans.modules.csl.api.StructureScanner;
 import org.netbeans.modules.csl.spi.ParserResult;
 
 /**
  * StructureScanner impl.
  * Generates a structure tree from the source (~AST), used for Navigator window (class,enum,mixin,field...)
  * Also provides code folding support.
  * 
  * See: http://hg.netbeans.org/main/file/24d72d2643e1/csl.api/src/org/netbeans/modules/csl/editor/fold/GsfFoldManager.java
  * @author thibautc
  */
 public class FanStructureAnalyzer implements StructureScanner
 {
 
 	public static final String CODE_FOLDS = "codeblocks";
 	public static final String DOC_FOLDS = "comments";
 	public static final String COMMENT_FOLDS = "comments";
 	public static final String IMPORT_FOLDS = "imports";
 
 	public FanStructureAnalyzer()
 	{
 		super();
 	}
 
 	@Override
 	public List<StructureItem> scan(ParserResult result)
 	{
 		List<StructureItem> items = new ArrayList<StructureItem>();
 		try
 		{
 			FanParserResult fanResult = (FanParserResult) result;
 
 			FanRootScope root = fanResult.getRootScope();
 			List<FanAstScope> types = root.getChildren();
 			for (FanAstScope scope : types)
 			{
 				FanStructureItem item = null;
 				FanTypeScope t = (FanTypeScope) scope;
 				item = new FanStructureItem(t.getAstNode(), ElementKind.CLASS, result);
 				List<FanResolvedType> inhs = t.getInheritedItems();
 				String inheritance = "";
 				for (FanResolvedType inh : inhs)
 				{
 					if (inheritance.length() > 0)
 					{
 						inheritance += ", ";
 					}
 					inheritance += inh.toDbSig(false);
 				}
 				item.setName(t.getName());
 				String html = t.getName();
 				if (inheritance.length() > 0)
 				{
 					html += "<font color='#aaaaaa'>" + inheritance + "</font>";
 				}
 				item.setHtml(html);
 				// Do slots
 				Collection<FanAstScopeVarBase> vars = t.getScopeVars();
 				List<StructureItem> slots = new ArrayList<StructureItem>();
 				for (FanAstScopeVarBase var : vars)
 				{
 					if (var instanceof FanAstMethod)
 					{
 						FanAstMethod m = (FanAstMethod) var;
 
 						ElementKind kind = m.isCtor() ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
 						FanStructureItem mItem = new FanStructureItem(var.getNode(), kind, result);
 						String returnType = FanType.getShortName(m.getTypeString());
						if (returnType==null || returnType.equals(FanIndexer.UNRESOLVED_TYPE) || returnType.equalsIgnoreCase("void"))
 						{
 							returnType = "";
 						}
 						// method params
 						Hashtable<String, FanResolvedType> parameters = m.getParameters();
 						String params = "";
 						for (String pname : parameters.keySet())
 						{
 							if (params.length() > 0)
 							{
 								params += ", ";
 							}
 							String pType = parameters.get(pname).toDbSig(false);
 							if (pType.equals(FanIndexer.UNRESOLVED_TYPE))
 							{
 								pType = "";
 							}
 
 							if (pType.equals(FanIndexer.UNRESOLVED_TYPE))
 							{
 								pType = "";
 							}
 							params += "<font color='#aaaaaa'>" + pType + "</font>";
 							params += " " + pname;
 						}
 						/*String constChain = FanLexAstUtils.getSubChildTextByType(result, node, FanParser.AST_CONSTRUCTOR_CHAIN);*/
 						handleModifiers(mItem, m.getModifiers());
 						mItem.setName(var.getName());
 						String mHtml = var.getName() + "(" + params + ")";
 						if (returnType.length() > 0)
 						{
 							mHtml += " : <font color='#aaaaaa'>" + returnType + "</font>";
 						} /*else if (constChain.length() > 0)
 						{
 						mHtml += " : <font color='#aaaaaa'>" + returnType + "</font>";
 						}*/
 						mItem.setHtml(mHtml);
 						slots.add(mItem);
 						break;
 					} else if (var instanceof FanAstField)
 					{
 						FanStructureItem slotItem = new FanStructureItem(var.getNode(), ElementKind.FIELD, result);
 						String type = var.getType().toDbSig(false);
 						if (type.equals(FanIndexer.UNRESOLVED_TYPE))
 						{
 							type = "";
 						}
 						handleModifiers(slotItem, var.getModifiers());
 						slotItem.setName(var.getName());
 						String slotHtml = var.getName();
 						if (type.length() > 0)
 						{
 							slotHtml += " : <font color='#aaaaaa'>" + type + "</font>";
 						}
 						slotItem.setHtml(slotHtml);
 						slots.add(slotItem);
 					}
 				}
 				if (slots.size() > 0)
 				{
 					item.setNestedItems(slots);
 				}
 				items.add(item);
 			}
 
 		} catch (Exception e)
 		{
 			// if there is an error here, we don't want to propagate to the user, should log though
 			e.printStackTrace();
 		}
 		return items;
 	}
 
 	@Override
 	public Map<String, List<OffsetRange>> folds(ParserResult result)
 	{
 		CommonTokenStream tokenStream = ((FanParserResult) result).getTokenStream();
 
 		FanParserResult fanResult = (FanParserResult) result;
 		Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
 		CommonTree ast = fanResult.getTree();
 		addFolds(
 			tokenStream, folds, ast);
 		return folds;
 	}
 
 	public Configuration getConfiguration()
 	{
 		return null;
 	}
 
 	private void handleModifiers(FanStructureItem item, List<FanAstScopeVarBase.ModifEnum> modifs)
 	{
 		for (FanAstScopeVarBase.ModifEnum modif : modifs)
 		{
 			if (modif == modif.STATIC)
 			{
 				item.addModifier(Modifier.STATIC);
 			} else if (modif == modif.PRIVATE)
 			{
 				item.addModifier(Modifier.PRIVATE);
 			} else if (modif == modif.PUBLIC)
 			{
 				item.addModifier(Modifier.PUBLIC);
 			} else if (modif == modif.PROTECTED)
 			{
 				item.addModifier(Modifier.PROTECTED);
 			}
 		}
 	}
 
 	private void addFolds(CommonTokenStream tokenStream, Map<String, List<OffsetRange>> folds, CommonTree node)
 	{
 		if (node != null)
 		{
 			boolean foldable = false;
 			String type = CODE_FOLDS;
 			//System.err.println("** " + node.getText());
 			switch (node.getType())
 			{
 				case FanParser.AST_CODE_BLOCK:
 					type = CODE_FOLDS;
 					foldable = true;
 
 
 					break;
 				case FanParser.AST_DOCS:
 					//TODO: comments folding (in hidden channel)
 					//TODO: also imports folding (using)
 					//case FanParser.MULTI_COMMENT:
 					type = DOC_FOLDS;
 					foldable = true;
 					break;
 			}
 
 			if (foldable && node.getTokenStartIndex() != -1 && node.getTokenStopIndex() != -1)
 			{
 				CommonToken startToken = (CommonToken) tokenStream.get(node.getTokenStartIndex());
 				CommonToken endToken = (CommonToken) tokenStream.get(node.getTokenStopIndex());
 				int start = startToken.getStartIndex();
 				int end = endToken.getStopIndex() + 1;
 				// don't add unless at least 1 long (ex: class with def alone)
 				if (start >= 0 && end >= 0 && end > start)
 				{
 					OffsetRange range = new OffsetRange(start, end);
 					List<OffsetRange> fold = folds.get(type);
 					if (fold == null)
 					{
 						fold = new ArrayList<OffsetRange>();
 						folds.put(type, fold);
 					}
 					fold.add(range);
 				}
 			}
 			// recurse into children
 			for (int i = 0; i
 				< node.getChildCount(); i++)
 			{
 				CommonTree subNode = (CommonTree) node.getChild(i);
 				addFolds(
 					tokenStream, folds, subNode);
 
 			}
 		}
 	}
 }
