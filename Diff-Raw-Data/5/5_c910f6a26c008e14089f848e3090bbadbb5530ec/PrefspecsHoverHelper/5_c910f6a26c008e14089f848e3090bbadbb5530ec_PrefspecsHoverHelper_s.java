 package prefspecs.safari.hoverHelper;
 
 import java.util.ArrayList;
 
 import lpg.runtime.IPrsStream;
 import lpg.runtime.IToken;
 
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.uide.editor.IHoverHelper;
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.eclipse.uide.parser.IParseController;
 
 import prefspecs.safari.parser.PrefspecsLexer;
 import prefspecs.safari.parser.Ast.ASTNode;
 import prefspecs.safari.parser.Ast.ASTNodeToken;
 import prefspecs.safari.parser.Ast.IASTNodeToken;
 import prefspecs.safari.parser.Ast.IfieldSpec;
 
 public class PrefspecsHoverHelper implements IHoverHelper
 {
     public String getHoverHelpAt(IParseController parseController, ISourceViewer srcViewer, int offset)
     {
         IPrsStream ps= parseController.getParser().getParseStream();
         // SMS 16 Mar 2007:  index out of bounds occurs here sometimes,
         // probably when ast has errors (around point of error)
         IToken token= ps.getTokenAtCharacter(offset);
         if (token == null) return null;
 
         Object ast = parseController.getCurrentAst();
         if (ast == null) return null;
 
         IASTNodeLocator nodeLocator= parseController.getNodeLocator();
         Object node = nodeLocator.findNode(ast, offset);
 
         if (node == null)
             return null;
 	
         if (node instanceof IASTNodeToken	
         		|| node instanceof ASTNodeToken)
         {
         	int tokenKind = ((IASTNodeToken) node).getLeftIToken().getKind();
         	
         	switch (tokenKind) {
         	
         	case PrefspecsLexer.TK_TABS:
         		return("In this section list each of the four tabs:  'default', 'configuration', 'instance', and 'project',	" +
         				"and, for eack, indicate whether it is 'in' or 'out' of the preference page and assign tab-wide attributes, " +
         				"'iseditable' and/or 'isremovable', as appropriate");
         	case PrefspecsLexer.TK_FIELDS:
         		return("In this section list the fields that will appear on the generated preferences page (each field will appear" +
         				"on each included tab).  To list a field, give its type and name, and optionally set any properties specific " +
         				"to that field (applicable properties depend on field type).  The name of each field should be unique.");
         	case PrefspecsLexer.TK_CUSTOM:
         		return("In this section provide property values that apply to specific fields on specific tabs.  " +
         				"Designate fields by 'tab-name' 'field-name'; " +
         				"applicable properties depend on field type.  " +
         				"Field names should have been introduced in the 'fields' section.  " +
         				"Specific fields (specific tab-field combinations) may appear multiple times " +
         				"with any subset of applicable properties (later assignments override earlier ones).");
         	case PrefspecsLexer.TK_CONDITIONALS:
         		return("In this section list fields of any type whose enabled state depends on the state of another field of boolean type." +
         				"E.g., 'nameOfDependentField with nameOfBooleanField' or 'nameOfDependentField against nameOfBooleanField'");
  
         	case PrefspecsLexer.TK_DEFAULT:
         		return("Designates the 'default' level preferences tab");
         	case PrefspecsLexer.TK_CONFIGURATION:
         		return("Designates the 'configuration' level preferences tab");
         	case PrefspecsLexer.TK_INSTANCE:
         		return("Designates the 'instance' level preferences tab");
         	case PrefspecsLexer.TK_PROJECT:
         		return("Designates the 'project' level preferences tab");
 
         		 
         	case PrefspecsLexer.TK_BOOLEAN:
         		return("Designates the 'boolean' (checkbox) field type");
         	case PrefspecsLexer.TK_COMBO:
         		return("Designates the 'combo' (combo box) field type");
         	case PrefspecsLexer.TK_DIRLIST:
         		return("Designates the 'dirlist' (directory list) field type");
         	case PrefspecsLexer.TK_FILE:
         		return("Designates the 'file' (file name) field type");
         	case PrefspecsLexer.TK_INT:
         		return("Designates the 'int' field type");
         	case PrefspecsLexer.TK_RADIO:
         		return("Designates the 'radio' (radio buttons) field type");
         	case PrefspecsLexer.TK_STRING:
         		return("Designates the 'string' field type");
 
         		
         	case PrefspecsLexer.TK_EMPTYALLOWED:
         		return("'emptyallowed' indicates whether the field has, and is allowed to take on, " +
         				"an 'empty' value; this attribute takes a boolean value; if 'true' then a value " +
            				"of the field type must be provided (to serve as the 'empty' value); " +
         				"if 'false', then no other argument is needed");
         	case PrefspecsLexer.TK_HASSPECIAL:
         		return("'hasspecial' requires a value of the type of the field that " +
         				"will serve as a distinguished value; omit if no such value");
         	case PrefspecsLexer.TK_ISEDITABLE:
         		return("'iseditable' takes a boolean value:  'true' indicates that the field " +
         				"can be edited (normal case); false indicates that it cannot (field is 'read only')");
         	case PrefspecsLexer.TK_ISREMOVABLE:
         		return("'isremovable' takes a boolean value:  'true' indicates that the value " +
         				"can be removed from this field an inherited from a higher level; " +
         				"'false' means that the field must always have a local value (not inherited");
         	case PrefspecsLexer.TK_RANGE:
         		return("'range' sets a range for numeric field types in the form 'lowVal .. highVal'");
 
 
         	case PrefspecsLexer.TK_AGAINST:
         		return("'against' means that the preceding field (of any type) is enabled if and only if " +
         				"the following field (of boolean type) is set to 'false'");
         	case PrefspecsLexer.TK_WITH:
         		return("'with' means that the preceding field (of any type) is enabled if and only if " +
         				"the following field (of boolean type) is set to 'true'");
         		
         	case PrefspecsLexer.TK_STRING_LITERAL:
         		return("String literal");
         		
         	case PrefspecsLexer.TK_IDENTIFIER:
         		int tokenNumber = ((IASTNodeToken) node).getLeftIToken().getTokenIndex();
         		if (tokenNumber == 2)
         			return "Preference-page identifier";
         		else
         			return "Preference-field identifier";
 
         	case PrefspecsLexer.TK_SINGLE_LINE_COMMENT:
         		// Comment tokens may not appear as such, and I don't want
         		// to go digging around in the adjuncts of "real" tokens,
         		// so don't expect much here
         		return "Comment (no effect on pagegeneration)";
         	
            	case PrefspecsLexer.TK_IN:
         		return("'in' means that the associated tab will be included in the generated preferences page");
         	case PrefspecsLexer.TK_OUT:
            		return("'out' means that the associated tab will not be included in the generated preferences page");
  
            	case PrefspecsLexer.TK_TRUE:
         		return("The opposite of false.");
         	case PrefspecsLexer.TK_FALSE:
            		return("The opposite of true.");
        		
         	case PrefspecsLexer.TK_PAGE:
         		return "This designates the beginning of a preference-page specification";
            		
         	case PrefspecsLexer.TK_LEFTBRACE:
         	case PrefspecsLexer.TK_RIGHTBRACE:
         		// getParent() returns an IAst, but prefspecs ASTNode implements that
         		ASTNode grandParentNode = (ASTNode) ((ASTNode)node).getParent().getParent();
         		if (grandParentNode == null) {
         			if (tokenKind == PrefspecsLexer.TK_LEFTBRACE)
         				return "This is the beginning of the body of the specification";
         			else
         				return "This is the end of the body of the specification";
         		}
         		
         		String parentTypeName = grandParentNode.getClass().getName();
         		if (parentTypeName.endsWith("tabSpecs")) {
         			return("Specify tab properties within braces; these properties by default apply to " +
         					"all fields in the tab.  There are two optional properties:  " +
         					"'iseditable' (boolean) indicates whether fields in the tab are editable; " +
         					"'isremovable' (boolean) indicates whether values stored in fields on the tab " +
         					"can be removed (triggering inheritance).  When both are used 'iseditable' " +
         					"must appear first.  Each should be followed by a ';'.");
         		} else if (parentTypeName.endsWith("fieldSpecs")) {
         			ASTNode specNode = getFieldSpecNode((ASTNode) node);
         			if (specNode != null) {
         				if (specNode.getClass().toString().endsWith("booleanFieldSpec")) {
             				return("Specify attributes for a boolean field:  three optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (boolean), 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("comboFieldSpec")) {
             				return("Specify attributes for a combo-box field:  four optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (string), " +
             						"'emptyallowed' (boolean--plus a string if 'true'), and 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("dirListFieldSpec")) {
             				return("Specify attributes for a directory-list field:   four optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (string), " +
             						"'emptyallowed' (boolean--plus a string if 'true'), and 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("fileFieldSpec")) {
             				return("Specify attributes for a file-name field:   four optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (string), " +
     								"'emptyallowed' (boolean--plus a string if 'true'), and 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("intFieldSpec")) {
             				return("Specify attributes for an integer field:   four optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'range' (lowval .. highval), " +
             						"'hasspecial' (int), and 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("radioFieldSpec")) {
             				return("Specify attributes for a radio-button field:   three optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (int), " +
 									"and 'isremovable' (boolean).");
         				} else if (specNode.getClass().toString().endsWith("stringFieldSpec")) {
             				return("Specify attributes for a string	field:   four optional, semicolon-separated attributes, " +
             						"in this order, if used:  'iseditable' (boolean), 'hasspecial' (string), " +
     								"'emptyallowed' (boolean--plus a string if 'true'), and 'isremovable' (boolean).");
         				}	
         				return("Unrecognized field-spec type; no hover help available");
         			} else {
         				return("No field spec found; no hover help possible");
         			}
         		} else {
        			return("This is some kind of crazy AST node!");
         		}
         		
         	default:				 // Below depends on JikesPG-specific types 
 		        ASTNode def = null;  //ASTNode) ASTUtils.findDefOf((IASTNodeToken) node, (JikesPG) ast);
 		        if (def != null)
 		            return getSubstring(parseController, def.getLeftIToken().getStartOffset(), def.getRightIToken().getEndOffset());
 		        else
		        	return "Abandon hope";
         	}
         }
         return getSubstring(parseController, token);
     }
 
     public static String getSubstring(IParseController parseController, int start, int end) {
         return new String(parseController.getLexer().getLexStream().getInputChars(), start, end-start+1);
     }
 
     public static String getSubstring(IParseController parseController, IToken token) {
         return getSubstring(parseController, token.getStartOffset(), token.getEndOffset());
     }
 
     public static String stripName(String rawId) {
 	int idx= rawId.indexOf('$');
 
 	return (idx >= 0) ? rawId.substring(0, idx) : rawId;
     }
     
     
 	protected ASTNode getFieldSpecNode(ASTNode node)
 	{
    		ASTNode grandParentNode = (ASTNode) node.getParent().getParent();
    		int nodeOffset = node.getLeftIToken().getStartOffset();
    		int specOffset = 0;
    		ArrayList parents = grandParentNode.getChildren();
    		ASTNode spec = null;
    		
    		for (int i = 0; i < parents.size(); i++) {
    			ASTNode parent = (ASTNode) parents.get(i);
    			if (parent instanceof IfieldSpec) {
    				int parentOffset = parent.getLeftIToken().getStartOffset();
    				if (parentOffset < nodeOffset && parentOffset > specOffset) {
    					specOffset = parentOffset;
    					spec = parent;
    				}
    			}
    		}
    		return spec;
 	}
     
     
 }
