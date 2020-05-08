 package amstin.tools;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import amstin.models.grammar.Alt;
 import amstin.models.grammar.Bool;
 import amstin.models.grammar.Element;
 import amstin.models.grammar.Grammar;
 import amstin.models.grammar.Id;
 import amstin.models.grammar.Int;
 import amstin.models.grammar.Iter;
 import amstin.models.grammar.IterSep;
 import amstin.models.grammar.IterSepStar;
 import amstin.models.grammar.IterStar;
 import amstin.models.grammar.Key;
 import amstin.models.grammar.Lit;
 import amstin.models.grammar.Opt;
 import amstin.models.grammar.Real;
 import amstin.models.grammar.Ref;
 import amstin.models.grammar.Rule;
 import amstin.models.grammar.Str;
 import amstin.models.grammar.Sym;
 import amstin.models.grammar.Symbol;
 import amstin.models.grammar.parsing.cps.Parser;
 import amstin.models.meta.Boot;
 import amstin.models.meta.Class;
 import amstin.models.meta.Field;
 import amstin.models.meta.Klass;
 import amstin.models.meta.MetaModel;
 import amstin.models.meta.Mult;
 import amstin.models.meta.Parent;
 import amstin.models.meta.Single;
 import amstin.models.meta.Star;
 import amstin.models.meta.Type;
 import amstin.models.meta._Main;
 
 public class GrammarToMetaModel {
 
 	public static void main(String[] args) throws IOException {
 		Grammar metaGrammar = Parser.parseGrammar(_Main.METAMODEL_MDG);
 		GrammarToMetaModel inf = new GrammarToMetaModel("MetaMetaModel", metaGrammar);
 		inf.infer();
 		MetaModel metaModel = inf.metaModel;
 		
 		// Returns false because list ordering is different.
 		// TODO: need order attribute on many fields.
 		if (ModelEquality.equals(Boot.instance, metaModel)) {
 			System.out.println("YES!");
 		}
 		else {
 			System.out.println("NO!");
 		}
 		Writer writer = new PrintWriter(System.out);
 		ModelToString.unparse(metaGrammar, metaModel, writer);
 		writer.flush();
 	}
 	
 	public static MetaModel infer(String name, Grammar grammar) {
 		GrammarToMetaModel inf = new GrammarToMetaModel(name, grammar);
 		inf.infer();
 		return inf.metaModel;
 	}
 	
 	private Grammar grammar;
 	private MetaModel metaModel;
 	private Map<String, Class> table;
 	
 	public GrammarToMetaModel(String name, Grammar grammar) {
 		this.grammar = grammar;
 		this.metaModel = new MetaModel();
 		metaModel.name = name;
 		metaModel.classes = new ArrayList<Class>();
 		this.table = new HashMap<String, Class>();
 	}
 	
 	private void infer() {
 		for (Rule rule: grammar.rules) {
 			Class klass = new Class();
 			klass.fields = new ArrayList<Field>();
 			klass.name = rule.name;
 			klass.isAbstract = true;
 			table.put(rule.name, klass);
 			metaModel.classes.add(klass);
 		}
 		
 		for (Rule rule: grammar.rules) {
 			Class sup = table.get(rule.name);
 			
 			if (rule.isInjection()) {
 				// injection means all alts delegate to a non-terminal
 				// this means the types for those symbols are dealt with elsewhere
 				continue;
 			}
 			
 			if (rule.isSingleton()) {
 				sup.isAbstract = false;
 				addFields(sup, rule.alts.get(0));
 			}
 			else {
 				for (Alt alt: rule.alts) {
 					inferAlt(sup, alt);
 				}
 			}
 		}

 	}
 	
 	private void addFields(Class klass, Alt alt) {
 		for (Element elt: alt.elements) {
 			if (elt.label == null) {
 				continue;
 			}
 			
 			Field f = new Field();
 			f.name = elt.label.name;
 			
 			Mult mult[] = {null};
 			Symbol sym = elt.symbol;
 			Type type = getType(sym, mult);
 			if (type instanceof Class) {
 				// this indirection is unfortunate
 				Klass k = new Klass();
 				k.klass = (Class) type;
 				f.type = k;
 			}
 			else {
 				f.type = type;
 			}
 			f.mult = mult[0];
 			
 			klass.fields.add(f);
 		}
 	}
 
 	private Type getType(Symbol sym, Mult[] mult) {
 		mult[0] = new Single();
 		if (sym instanceof Id || sym instanceof Str) {
 			return new amstin.models.meta.Str();
 		}
 
 		if (sym instanceof Int) {
 			return new amstin.models.meta.Int();
 		}
 		if (sym instanceof Real) {
 			return new amstin.models.meta.Real();
 		}
 		if (sym instanceof Bool) {
 			return new amstin.models.meta.Bool();
 		}
 		
 		Mult dummy[] = {null};
 		
 		if (sym instanceof Iter) {
 			mult[0] = new Star();
 			return getType(((Iter)sym).arg, dummy);
 		}
 
 		if (sym instanceof IterSep) {
 			mult[0] = new Star();
 			return getType(((IterSep)sym).arg, dummy);
 		}
 
 		if (sym instanceof IterStar) {
 			mult[0] = new Star();
 			return getType(((IterStar)sym).arg, dummy);
 		}
 
 		if (sym instanceof IterSepStar) {
 			mult[0] = new Star();
 			return getType(((IterSepStar)sym).arg, dummy);
 		}
 
 		if (sym instanceof Opt && ((Opt)sym).arg instanceof Lit) {
 			return new amstin.models.meta.Bool();
 		}
 
 		if (sym instanceof Opt) {
 			mult[0] = new amstin.models.meta.Opt();
 			return getType(((Opt)sym).arg, dummy);
 		}
 		
 		if (sym instanceof Sym) {
 			return table.get(((Sym)sym).rule.name);
 		}
 		
 		if (sym instanceof Ref) {
 			return table.get(((Ref)sym).ref);
 		}
 		
 		if (sym instanceof Key) {
 			return new amstin.models.meta.Str();
 		}
 		
 
 		throw new RuntimeException("Cannot convert symbol: " + sym);
 
 	}
 	
 	private void inferAlt(Class sup, Alt alt) {
 		Class klass = new Class();
 		klass.fields = new ArrayList<Field>();
 		Parent parent = new Parent();
 		parent.type = sup;
 		klass.parent = parent;
 		klass.name = alt.type.name;
 		metaModel.classes.add(klass);
 		addFields(klass, alt);
 	}
 
 }
