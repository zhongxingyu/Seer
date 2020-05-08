 package ideah.editor;
 
 import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.lang.ASTNode;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.RawText;
 import com.intellij.openapi.editor.SelectionModel;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.util.text.LineTokenizer;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.psi.PsiDocumentManager;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.tree.IElementType;
 import ideah.lexer.HaskellTokenTypes;
 import ideah.lexer.Unescaper;
 import ideah.util.LineCol;
 
 // todo: for IDEA 11 override StringLiteralCopyPasteProcessor
 public final class HaskellStringCopyPasteProcessor implements CopyPastePreProcessor {
 
     public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
         boolean isLiteral = true;
         for (int i = 0; i < startOffsets.length; i++) {
             if (findLiteralTokenType(file, startOffsets[i], endOffsets[i]) == null) {
                 isLiteral = false;
                 break;
             }
         }
         return isLiteral ? Unescaper.unescape(text) : null;
     }
 
     public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
         Document document = editor.getDocument();
         PsiDocumentManager.getInstance(project).commitDocument(document);
         SelectionModel selectionModel = editor.getSelectionModel();
 
         // pastes in block selection mode (column mode) are not handled by a CopyPasteProcessor
         int selectionStart = selectionModel.getSelectionStart();
         int selectionEnd = selectionModel.getSelectionEnd();
         IElementType tokenType = findLiteralTokenType(file, selectionStart, selectionEnd);
 
         if (tokenType == HaskellTokenTypes.STRING) {
             if (rawText != null && rawText.rawText != null)
                 return rawText.rawText; // Copied from the string literal. Copy as is.
 
             StringBuilder buffer = new StringBuilder(text.length());
             LineCol lineCol = LineCol.fromOffset(file, selectionStart);
             String indent;
             if (lineCol != null) {
                 int column = lineCol.column - 1;
                 indent = StringUtil.repeat(" ", column > 0 ? column - 1 : 0);
             } else {
                 indent = "";
             }
             String breaker = "\\n\\\n" + indent + "\\";
             String[] lines = LineTokenizer.tokenize(text.toCharArray(), false, true);
             for (int i = 0; i < lines.length; i++) {
                 if (i > 0) {
                     buffer.append(breaker);
                 }
                 String line = lines[i];
                 buffer.append(escape(line));
             }
             text = buffer.toString();
         }
         return text;
     }
 
     private static IElementType findLiteralTokenType(PsiFile file, int selectionStart, int selectionEnd) {
         PsiElement elementAtSelection = file.findElementAt(selectionStart);
         if (elementAtSelection == null)
             return null;
		ASTNode node = elementAtSelection.getNode();
		if(node == null) {
			return null;
		}
		IElementType tokenType = node.getElementType();
         if (tokenType != HaskellTokenTypes.STRING)
             return null;
         TextRange textRange = elementAtSelection.getTextRange();
         if (selectionStart <= textRange.getStartOffset() || selectionEnd >= textRange.getEndOffset()) {
             return null;
         }
         return tokenType;
     }
 
     private static String escape(String str) {
         StringBuilder buf = new StringBuilder(str.length());
         boolean prevHex = false;
         for (int i = 0; i < str.length(); i++) {
             char ch = str.charAt(i);
             boolean nextPrevHex = false;
             if (ch == '\'') {
                 buf.append(ch);
             } else {
                 char escaped = Unescaper.escape(ch);
                 if (escaped != 0) {
                     buf.append('\\').append(escaped);
                 } else {
                     if (Character.isISOControl(ch)) {
                         String hexCode = Integer.toHexString(ch).toUpperCase();
                         buf.append("\\x");
                         int paddingCount = 4 - hexCode.length();
                         while (paddingCount-- > 0) {
                             buf.append('0');
                         }
                         buf.append(hexCode);
                         nextPrevHex = true;
                     } else {
                         if (prevHex && (ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f')) {
                             buf.append('\\').append('&');
                         }
                         buf.append(ch);
                     }
                 }
             }
             prevHex = nextPrevHex;
         }
         return buf.toString();
     }
 }
