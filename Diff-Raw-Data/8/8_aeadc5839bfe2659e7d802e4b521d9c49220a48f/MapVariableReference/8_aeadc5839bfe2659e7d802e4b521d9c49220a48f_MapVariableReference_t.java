 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 
 public class MapVariableReference extends VariableReference {
 	private ArrayList/*<Expression>*/ mapKeys;
 	
 	public MapVariableReference(String name, Expression[] mapKeys, ASTNode javaNode, Scope scope) {
 		super(name, javaNode, scope);
 		this.mapKeys = new ArrayList();
 		if (mapKeys != null) {
 			for (int i = 0; i < mapKeys.length; i++) {
 				this.mapKeys.add(mapKeys[i]);
 			}
 		}
 	}
 	
 	public ArrayList getMapKeys() { 
 		return mapKeys;
 	}
 
 	public void toBuffer(BoogieSource out) {
		super.toBuffer(out);
 		for (int i = 0; i < getMapKeys().size(); i++) {
 			out.append(TOKEN_LBRACK);
			((Expression)getMapKeys().get(i)).toBuffer(out);
			out.append(TOKEN_RBRACK);
 		}
 	}
 }
