 package $PACKAGE_NAME$;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
import $PACKAGE_NAME$.FooParseController.ASTNode;
import $PACKAGE_NAME$.FooParseController.AbstractVisitor;
 
 import org.eclipse.uide.parser.IASTNodeLocator;
 
 /**
  * NOTE:  This version of the Node Locator is for use when the Node
  * Locator and corresponding Parse Controller are generated separately from
  * a corresponding set of LPG grammar templates and possibly in the absence
  * of the lexer, parser, and AST-related types that would be generated from
  * those templates.  To enable compilation of the Locator and Controller,
  * dummy types have been defined as member types of the Controller in place
  * of possibly missing lexer, parser, and AST-related types.  This version
  * of the Node Locator refers to some of those types.  When those types
  * are replaced by real implementation types, the Locator must be modified
  * to refer to those.  Apart from statements to import needed types from
  * the Parse Controller, this Node Locator is the same as that used with
  * LPG.
  * @see the corresponding ParseController type
  * 
  * @author Stan Sutton (suttons@us.ibm.com)
  * @since May 15, 2007
  * 
  */  
 public class $CLASS_NAME_PREFIX$ASTNodeLocator implements IASTNodeLocator
 {
     private final $AST_NODE$[] fNode= new $AST_NODE$[1];
 
     private int fStartOffset;
     private int fEndOffset;
     
     public $CLASS_NAME_PREFIX$ASTNodeLocator( ) {
     }
 
     private final class NodeVisitor extends AbstractVisitor {
 
         public void unimplementedVisitor(String s) {
             // System.out.println("NodeVisitor.unimplementedVisitor:  Unimplemented");
         }
 
         public boolean preVisit($AST_NODE$ element)
         {
             int nodeStartOffset = element.getLeftIToken().getStartOffset();
             int nodeEndOffset = element.getRightIToken().getEndOffset();
             //System.out.println("$CLASS_NAME_PREFIX$NodeLocator.NodeVisitor.preVisit($AST_NODE$):  Examining " + element.getClass().getName() +
             //    " @ [" + nodeStartOffset + "->" + nodeEndOffset + ']');
 
             // If this node contains the span of interest then record it
             if (nodeStartOffset <= fStartOffset && nodeEndOffset >= fEndOffset) {
                 //System.out.println("$CLASS_NAME_PREFIX$NodeLocator.NodeVisitor.preVisit($AST_NODE$) SELECTED for offsets [" + fStartOffset + ".." + fEndOffset + "]");
                 fNode[0]= element;
                 return true; // to continue visiting here?
             }
             return false; // to stop visiting here?
         }
     }
 
     private NodeVisitor fVisitor= new NodeVisitor();
 
     public Object findNode(Object ast, int offset) {
         return findNode(ast, offset, offset);
     }
 
     public Object findNode(Object ast, int startOffset, int endOffset) {
         // System.out.println("Looking for node spanning offsets " + startOffset + " => " + endOffset);
         fStartOffset = startOffset;
         fEndOffset = endOffset;
         // The following could be treated as an IASTNodeToken, but $AST_NODE$
         // is required for the visit/preVisit method, and there's no reason
         // to use both of those types
         (($AST_NODE$) ast).accept(fVisitor);
         if (fNode[0] == null) {
             //System.out.println("Selected node:  null");
         } else {
             //System.out.println("Selected node: " + fNode[0] + " [" +
             //   fNode[0].getLeftIToken().getStartOffset() + ".." + fNode[0].getLeftIToken().getEndOffset() + "]");
         }
         return fNode[0];
     }
 
     public int getStartOffset(Object node) {
         $AST_NODE$ n = ($AST_NODE$) node;
         return n.getLeftIToken().getStartOffset();
     }
 
     public int getEndOffset(Object node) {
         $AST_NODE$ n = ($AST_NODE$) node;
         return n.getRightIToken().getEndOffset();
     }
 
     public int getLength(Object  node) {
         $AST_NODE$ n = ($AST_NODE$) node;
         return getEndOffset(n) - getStartOffset(n);
     }
 
 
     public IPath getPath(Object node) {
         // TODO Determine path of compilation unit containing this node
         return new Path("");
     }
 }
