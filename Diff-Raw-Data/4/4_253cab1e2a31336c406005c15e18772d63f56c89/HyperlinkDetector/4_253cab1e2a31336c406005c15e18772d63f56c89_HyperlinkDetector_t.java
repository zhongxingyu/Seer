 package org.jikespg.uide.editor;
 
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.PrsStream;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.uide.core.ILanguageService;
 import org.eclipse.uide.editor.ISourceHyperlinkDetector;
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.eclipse.uide.parser.IParseController;
 import org.jikespg.uide.parser.ASTUtils;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 import org.jikespg.uide.parser.JikesPGParser.IASTNodeToken;
 import org.jikespg.uide.parser.JikesPGParser.Imacro_name_symbol;
 import org.jikespg.uide.parser.JikesPGParser.JikesPG;
 import org.jikespg.uide.parser.JikesPGParser.nonTerm;
 import org.jikespg.uide.parser.JikesPGParser.terminal;
 
 public class HyperlinkDetector implements ISourceHyperlinkDetector, ILanguageService {
     public HyperlinkDetector() { }
 
     public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, IParseController parseController) {
         int offset= region.getOffset();
         PrsStream ps= parseController.getParser().getParseStream();
         IToken token= ps.getTokenAtCharacter(offset);
 
         if (token == null) return null;
 
         ASTNode ast= (ASTNode) parseController.getCurrentAst();
 
         if (ast == null) return null;
 
         IASTNodeLocator nodeLocator= parseController.getNodeLocator();
 
         ASTNode node= (ASTNode) nodeLocator.findNode(ast, offset);
 
         if (node == null)
             return null;
 
         if (node instanceof IASTNodeToken) {
             final ASTNode def= ASTUtils.findDefOf((IASTNodeToken) node, (JikesPG) ast);
 
             if (def != null) {
                 final int srcStart= node.getLeftIToken().getStartOffset();
                 final int srcLength= node.getRightIToken().getEndOffset() - srcStart + 1;
                 final int targetStart= def.getLeftIToken().getStartOffset();
                // SMS 2 Jun 2006:  replaced original calculation of target length
                // (which was erroneously based on node rather than def)
                final int targetLength= def.getRightIToken().getEndOffset() - targetStart + 1;
 
                 return new IHyperlink[] {
                         new IHyperlink() {
                             public IRegion getHyperlinkRegion() {
                                 return new Region(srcStart, srcLength);
                             }
                             public String getTypeLabel() {
                         	if (def instanceof nonTerm)
                         	    return "non-terminal";
                         	if (def instanceof terminal)
                         	    return "terminal";
                         	if (def instanceof Imacro_name_symbol)
                         	    return "macro";
                         	return null;
                             }
                             public String getHyperlinkText() {
                                 return def.getLeftIToken().toString();
                             }
                             public void open() {
                                 textViewer.setSelectedRange(targetStart, targetLength);
                                 textViewer.revealRange(targetStart, targetLength);
                             }
                         }
                 };
             }
         }
         return null;
     }
 }
