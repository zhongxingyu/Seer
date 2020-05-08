 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.parboiled;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.text.Document;
 import net.colar.netbeans.fan.FanTokenID;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.parboiled.FantomLexerTokens.TokenName;
 import net.colar.netbeans.fan.parboiled.pred.NodeLabelPredicate;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenId;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.parboiled.Node;
 import org.parboiled.google.base.Predicate;
 import org.parboiled.support.InputBuffer;
 
 /**
  * Utilities for lexer / parser / ast trees
  * @author tcolar
  */
 public class FanLexAstUtils
 {
 
     /**
      * Return the Actual token at the given caret position in the document.
      * @param doc
      * @param caretIndex
      * @return
      */
     public static Token<? extends FanTokenID> getFanTokenAt(Document doc, int caretIndex)
     {
         TokenSequence ts = getFanTokenSequence(doc);
         ts.move(caretIndex);
         ts.moveNext();
         @SuppressWarnings("unchecked")
         Token<? extends FanTokenID> token = ts.token();
         return token;
     }
 
     /**
      * Returns the tokensequence from adoc
      * @param doc
      * @return
      */
     public static TokenSequence<? extends FanTokenID> getFanTokenSequence(Document doc, int offset)
     {
         //System.err.println("doc:" + doc);
         TokenHierarchy<Document> th = TokenHierarchy.get(doc);
         //System.err.println("hierarchy: " + th);
         return getFanTokenSequence(th, offset);
     }
 
     public static TokenSequence<? extends FanTokenID> getFanTokenSequence(Document doc)
     {
         return getFanTokenSequence(doc, 0);
     }
 
     /**
      * Returns the tokensequence from th tokenhierarchy
      * @param th
      * @return
      */
     public static TokenSequence<? extends FanTokenID> getFanTokenSequence(TokenHierarchy<?> th, int offset)
     {
         TokenSequence<? extends FanTokenID> ts = th.tokenSequence(FanTokenID.language());
         if (offset != 0)
         {
             ts = ts.subSequence(offset);
         }
         return ts;
     }
 
     /**
      * Find the closing token matching the opening one (balanced) and return the range between the two
      * @param doc
      * @param ts
      * @param opening
      * @param closing
      * @return
      */
     public static OffsetRange findRangeFromOpening(Document doc, TokenSequence<? extends FanTokenID> ts, TokenName openingToken, TokenName closingToken)
     {
         return findRange(doc, ts, openingToken, closingToken, true);
     }
 
     public static boolean matchType(TokenId token, TokenName[] array)
     {
         for (TokenName t : array)
         {
             //System.out.println("Matching type: "+t+" VS "+type);
             if (token.name().equals(t.name()))
             {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Find (backward) the opening token matching the closing one (balanced) and return the range between the two
      * @param doc
      * @param ts
      * @param opening
      * @param closing
      * @return
      */
     public static OffsetRange findRangeFromClosing(Document doc, TokenSequence<? extends FanTokenID> ts, TokenName openingToken, TokenName closingToken)
     {
         return findRange(doc, ts, openingToken, closingToken, false);
     }
 
     /**
      * Looking for matching(balanced) opening/closing token
      * @param doc
      * @param ts
      * @param openingID
      * @param closingID
      * @param lookForClosing (false=lookForOpening  - backward)
      * @return
      */
     private static OffsetRange findRange(Document doc, TokenSequence<? extends FanTokenID> ts, TokenName opening, TokenName closing, boolean lookForClosing)
     {
         int balance = 0;
 
         while (move(ts, lookForClosing))
         {
             Token<? extends FanTokenID> token = ts.token();
             FanTokenID id = token.id();
 
             if (id.matches(opening))
             {
                 if ((!lookForClosing) && balance == 0)
                 {
                     return new OffsetRange(ts.offset(), ts.offset() + token.length());
                 }
                 balance++;
             } else if (id.matches(closing))
             {
                 if (lookForClosing && balance == 0)
                 {
                     return new OffsetRange(ts.offset(), ts.offset() + token.length());
                 }
                 balance--;
             }
         }
 
         return OffsetRange.NONE;
     }
 
     private static boolean move(TokenSequence ts, boolean forward)
     {
         if (forward)
         {
             return ts.moveNext();
         } else
         {
             return ts.movePrevious();
         }
     }
 
     public static OffsetRange getNodeRange(AstNode node)
     {
         return new OffsetRange(node.getStartLocation().getIndex(), node.getEndLocation().getIndex());
     }
 
     public static AstNode getScopeNode(AstNode node)
     {
         AstNode nd = node;
         while (nd != null)
         {
             if (nd.isScopeNode())
             {
                 return nd;
             }
             nd = nd.getParent();
         }
         return null;
     }
 
     /**
      * Check whether the parentNode is a direct wrapper of the given nodeKind
      * @param parentNode
      * @param pred
      * @return
      */
     @SuppressWarnings("unchecked")
     public static boolean isWrappingNode(AstNode parentNode, Predicate pred)
     {
         if (pred.apply(parentNode))
         {
             return true;
         }
         if (parentNode.getChildren().size() == 1)
         {
             return isWrappingNode(parentNode.getChildren().get(0), pred);
         }
         return false;
     }
 
     public static AstNode getFirstChildRecursive(AstNode parentNode, Predicate pred)
     {
         AstNode nd = getFirstChild(parentNode, pred);
         if (nd != null)
         {
             return nd;
         }
         for (AstNode subNode : parentNode.getChildren())
         {
            nd = getFirstChildRecursive(subNode, pred);
            if (nd != null)
            {
                return nd;
            }
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public static AstNode getFirstChild(AstNode parentNode, Predicate pred)
     {
         if (parentNode != null)
         {
             for (AstNode child : parentNode.getChildren())
             {
                 if (pred.apply(child))
                 {
                     return child;
                 }
             }
         }
         return null;
     }
 
     public static List<AstNode> getChildren(AstNode parentNode, Predicate<AstNode> pred)
     {
         List<AstNode> nodes = new ArrayList<AstNode>();
         if (parentNode != null)
         {
             for (AstNode child : parentNode.getChildren())
             {
                 if (pred.apply(child))
                 {
                     nodes.add(child);
                 }
             }
         }
         return nodes;
     }
 
     public static String getFirstChildText(AstNode parentNode, Predicate<AstNode> pred)
     {
         AstNode node = getFirstChild(parentNode, pred);
         return node == null ? null : node.getNodeText(true);
     }
 
     public static String getNodeText(Node<AstNode> node, InputBuffer inputBuffer)
     {
         int start = node.getStartLocation().getIndex();
         int end = node.getEndLocation().getIndex();
         return inputBuffer.extract(start, end);
     }
 
     public static OffsetRange getNodeRange(Node<AstNode> node)
     {
         return new OffsetRange(node.getStartLocation().getIndex(), node.getEndLocation().getIndex());
     }
 
     @SuppressWarnings("unchecked")
     public static Node<AstNode> getFirstChild(Node<AstNode> parentNode, NodeLabelPredicate pred)
     {
         if (parentNode != null)
         {
             for (Node<AstNode> child : parentNode.getChildren())
             {
                 if (pred.apply(child))
                 {
                     return child;
                 }
             }
         }
         return null;
     }
 
     public static AstNode findASTNodeAt(AstNode node, int offset)
     {
         if (node != null
                 && offset >= node.getStartLocation().getIndex()
                 && offset <= node.getEndLocation().getIndex())
         {
             for (AstNode child : node.getChildren())
             {
                 AstNode found = findASTNodeAt(child, offset);
                 if (found != null)
                 {
                     return found;
                 }
             }
             return node;
         }
         return null;
     }
 
     public static AstNode findParentNode(final AstNode theNode, AstKind parentType)
     {
         return findParentNodeWithin(theNode, parentType, theNode.getRoot());
     }
 
     public static AstNode findParentNodeWithin(final AstNode theNode, AstKind parentType, AstNode within)
     {
         // don't want to mess with the original node.
         AstNode node = theNode;
         if (node.getKind() == parentType)
         {
             return node;
         }
         while (node != null && node != within)
         {
             //System.out.println(""+node.getType()+" VS "+parentType+" "+node.toStringTree());
             if (node.getKind() == parentType)
             {
                 return node;
             }
             node = node.getParent();
         }
         // not found
         return null;
     }
 
     public static void dumpTree(AstNode node, int indent)
     {
         StringBuffer sb = new StringBuffer();
         getDumpTree(sb, node, indent);
         FanUtilities.GENERIC_LOGGER.info(sb.toString());
     }
 
     private static void getDumpTree(StringBuffer sb, AstNode node, int indent)
     {
         for (int i = 0; i < indent; i++)
         {
             sb = sb.append("  ");
         }
         sb.append(node.toString()).append("\n");
         indent = indent + 1;
         for (AstNode child : node.getChildren())
         {
             getDumpTree(sb, child, indent + 1);
         }
     }
 
     public static AstNode getCallNodeExpr(AstNode node)
     {
         AstNode expr = FanLexAstUtils.findParentNode(node, AstKind.AST_EXPR);
         AstNode callNode = FanLexAstUtils.findParentNodeWithin(node, AstKind.AST_CALL_EXPR, expr);
         if (callNode == null)
         {
             callNode = FanLexAstUtils.findParentNodeWithin(node, AstKind.AST_CALL, expr);
             if (callNode == null)
             {
                 callNode = FanLexAstUtils.findParentNodeWithin(node, AstKind.AST_INC_CALL, expr);
             }
         }
         return callNode;
     }
 }
