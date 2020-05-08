 package org.magnolialang.terms;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IValue;
 
 import static org.magnolialang.terms.TermFactory.*;
 public class MagnoliaSkin implements ILanguageSkin {
 	SkinTable table = new SkinTable("MagnoliaSkinTable.pbf");
 	static Set<String> verticals = new HashSet<String>();
 	static {
 		verticals.add("Stat");
 		verticals.add("Decl");
		verticals.add("DeclNS");
 		verticals.add("TopDecl");
 		verticals.add("ModuleHead");
		verticals.add("StatDefBodyS");
		verticals.add("StatDefBodyNS");
 	}
 	public boolean isVertical(String cons, String sort, int arity, IValue context) {
 		if(sort == null)
 			sort = table.getSort(cons + "/" + arity);
 		return verticals.contains(sort);
 	}
 
 	public IList getConcrete(String cons, String sort, int arity, IValue context) {
 		return table.getConcrete(cons + "/" + arity);
 	}
 
 	public IConstructor getListSep(String sort, IValue context) {
 		if(sort != null && (sort.equals("Expr") || sort.equals("Type")))
 			return token(", ");
 		return null;
 	}
 
 }
