 package applets.Termumformungen$in$der$Technik_08_Ethanolloesungen;
 
 import java.math.BigInteger;
 import java.util.*;
 
 
 class OperatorTree implements Comparable<OperatorTree> {
 	String op = "";
 	List<OTEntity> entities = new LinkedList<OTEntity>();
 	final static String knownOps = "= +- ∙ / ^"; // they also must be in order
 	
 	OperatorTree() {}
 	OperatorTree(String op) { this.op = op; } 
 	OperatorTree(String op, OTEntity e) { this.op = op; entities.add(e); } 
 	OperatorTree(String op, List<OTEntity> entities) { this.op = op; this.entities = entities; }
 	OperatorTree copy() { return new OperatorTree(op, new LinkedList<OTEntity>(entities)); }
 	static OperatorTree MergedEquation(OperatorTree left, OperatorTree right) {
 		return new OperatorTree("+", Utils.listFromArgs(left.asEntity(), right.minusOne().asEntity()));
 	}    	
 	OperatorTree subtree(int start, int end) {
 		return new OperatorTree(op, entities.subList(start, end));
 	}
 
 	static OperatorTree Sum(List<OTEntity> entities) { return new OperatorTree("+", entities); }
 	static OperatorTree Product(List<OTEntity> entities) { return new OperatorTree("∙", entities); }
 	static OperatorTree Power(OperatorTree base, OperatorTree exp) { return new OperatorTree("^", Utils.listFromArgs(base.asEntity(), exp.asEntity())); }
 	static OperatorTree Power(Utils.Pair<OperatorTree,OTNumber> pot) {
 		if(pot.second.compareTo(OTNumber.ONE) == 0) return pot.first;
 		return Power(pot.first, Number(pot.second));
 	}
 
     static OperatorTree Zero() { return Number(0); }        
     boolean isZero() { return isNumber(0); }
 
     static OperatorTree One() { return Number(1); }        
     boolean isOne() { return isNumber(1); }
     
     static OperatorTree Variable(String var) {
     	try {
     		new OTNumber(var);
     		throw new AssertionError("Variable '" + var + "' must not be interpretable as a number.");
     	}
     	catch(NumberFormatException e) {
         	return new OTRawString(var).asTree();        		
     	}
     }
 	static OperatorTree Number(long num) { return Number(OTNumber.valueOf(num)); }
 	static OperatorTree Number(OTNumber num) {
 		if(num.compareTo(OTNumber.ZERO) >= 0) return new OTRawString("" + num.toString()).asTree();
 		else return Number(num.negate()).minusOne();
 	}
 	OTNumber asNumber() {
 		if(isFunctionCall()) return null;
 		if(entities.isEmpty() && op.isEmpty()) return OTNumber.ZERO;
 		if(entities.size() == 1) {
 			OTEntity e = entities.get(0);
 			if(e instanceof OTSubtree)
 				return ((OTSubtree)e).content.asNumber();
 			String s = ((OTRawString)e).content;
 			try { return new OTNumber(s); }
 			catch(NumberFormatException exc) { return null; }
 		}
 
 		OTNumber x;
 		if(op.equals("+") || op.equals("-")) x = OTNumber.ZERO;
 		else if(op.equals("∙") || op.equals("/")) x = OTNumber.ONE;
 		else if(op.equals("^")) x = null;
 		else return null;
 		boolean isFirst = true;
 		for(OTEntity e : entities) {
 			OTNumber other = e.asTree().asNumber();
 			if(other == null) return null;
 			if(isFirst && (op.equals("-") || op.equals("/"))) {
 				x = other;
 				isFirst = false;
 				continue;
 			}
 			try {
     			if(op.equals("+")) x = x.add(other);
     			else if(op.equals("-")) x = x.subtract(other);
     			else if(op.equals("∙")) x = x.multiply(other);
     			else if(op.equals("/")) x = x.divide(other);
     			else if(op.equals("^")) x = x.pow(other.intValueExact());
 			} catch(ArithmeticException exc) { // e.g. div by zero
 				return null;
 			}
 			isFirst = false;
 		}
 		return x;
 	}
     boolean isNumber(long num) {
     	OTNumber x = asNumber();
     	if(x == null) return false;
     	return x.compareTo(OTNumber.valueOf(num)) == 0;
     }
 	boolean isNumber() {
 		return asNumber() != null;
 	}
 
     boolean isFunctionCall() {
     	// right now, everything which is not empty and not a known operation is a function
     	if(op.length() == 0) return false;
     	if(op.length() == 1) return knownOps.indexOf(op) < 0;
     	return true;
     }
     
 	boolean canBeInterpretedAsUnaryPrefixed() {
 		return !isFunctionCall() && entities.size() == 2 && entities.get(0) instanceof OTSubtree && ((OTSubtree)entities.get(0)).content.entities.isEmpty();	
 	}
 	
 	OTEntity unaryPrefixedContent() {
 		if(!canBeInterpretedAsUnaryPrefixed()) throw new AssertionError("we expect the OT to be unary prefixed");
 		return entities.get(1);
 	}
 	
 	OperatorTree prefixed(String prefixOp) {
 		return new OperatorTree(prefixOp, Utils.listFromArgs(new OTSubtree(new OperatorTree()), asEntity()));
 	}
 
 	Integer signum() {
 		if(isNumber()) {
 			OTNumber n = asNumber();
 			if(n.compareTo(OTNumber.ZERO) < 0) return -1;
 			if(n.compareTo(OTNumber.ZERO) == 0) return 0;
 			if(n.compareTo(OTNumber.ZERO) > 0) return 1;
 		}
 
 		// NOTE: We treat function calls, similar as variables, as positive.
 		// If this should be changed at some point, note that isNegative() is
 		// used in normedProductFactorList() which should really work this way.
 		if(isFunctionCall()) return 1;
 
 		if(entities.isEmpty()) return null; // not a number. i.e. unknown what we are
 
 		if(entities.size() == 1) {
 			OTEntity e = entities.get(0);
 			if(e instanceof OTRawString) return 1; // var. it's not a number because we checked that above
 			return ((OTSubtree) e).content.signum();
 		}
 
 		if(canBeInterpretedAsUnaryPrefixed() && op.equals("-")) {
 			Integer s = unaryPrefixedContent().asTree().signum();
 			if(s == null) return null;
 			return -s;
 		}
 
 		if(op.equals("+") || op.equals("-")) {
 			Integer s = null;
 			for(OTEntity e : entities) {
 				Integer eSignum = e.asTree().signum();
 				if(eSignum == null) return null;
 				if(op.equals("-")) eSignum *= -1;
 				if(s == null) {
 					if(eSignum != 0)
 						s = eSignum;
 				}
 				else {
 					if(s.intValue() != eSignum.intValue())
 						return null;
 				}
 			}
 			if(s == null) return 0;
 			return s;
 		}
 
 		if(op.equals("∙") || op.equals("/")) {
 			Integer s = 1;
 			for(OTEntity e : entities) {
 				Integer eSignum = e.asTree().signum();
 				if(eSignum == null) return null;
 				s *= eSignum;
 				if(s == 0) return s;
 			}
 			return s;
 		}
 
 		if(op.equals("^")) {
 			Utils.Pair<OperatorTree,OTNumber> pot = normedPot();
 			if(pot.second.compareTo(OTNumber.ZERO) == 0) return 1;
 			if(pot.second.compareTo(OTNumber.ZERO) > 0) return pot.first.signum();
 			return null;
 		}
 
 		return null;
 	}
 
     boolean isNegative() {
 	    Integer s = signum();
 	    return s != null && s < 0;
 	}
 	
 	@Override public String toString() {
 		if(debugOperatorTreeDump) {
 			if(canBeInterpretedAsUnaryPrefixed())
 				return "<" + op + ">" + unaryPrefixedContent();
     		return "[" + op + "] " + Utils.concat(entities, ", ");
 		}
 		if(canBeInterpretedAsUnaryPrefixed())
 			// this is a special case used for unary ops (or ops which look like those)
 			return op + unaryPrefixedContent().toString(); // always put brackets if it is a subtree
 		Iterable<String> entitiesStr = Utils.map(entities, new Utils.Function<OTEntity,String>() {
 			public String eval(OTEntity obj) {
 				return obj.toString(op);
 			}
 		});
         if(isFunctionCall()) {
             if(entities.size() == 1 && entities.get(0) instanceof OTSubtree)
                 return op + entities.get(0); // some shorter version
             return op + " " + Utils.concat(entitiesStr, " ");
         }
 		return Utils.concat(entitiesStr, " " + op + " ");
 	}
     static boolean debugOperatorTreeDump = false;
 
     String toString(boolean debug) {
     	boolean oldDebugState = debugOperatorTreeDump;
     	debugOperatorTreeDump = debug;
     	String s = toString();
     	debugOperatorTreeDump = oldDebugState;
     	return s;
     }
     
     String debugStringDouble() { return toString(false) + " // " + toString(true); }
 
 	boolean canIgnoreOp() { return entities.size() == 1 && !isFunctionCall(); }
 	public int compareTo(OperatorTree o) {
 		if(!canIgnoreOp() || !o.canIgnoreOp()) {
 			int c = op.compareTo(o.op);
 			if(c != 0) return c;
 		}
 		return Utils.<OTEntity,Collection<OTEntity>>collectionComparator().compare(entities, o.entities);
 	}
 	@Override public int hashCode() {
 		int result = 1;
 		result = 31 * result + op.hashCode();
 		result = 31 * result + entities.hashCode();
 		return result;
 	}
 	@Override public boolean equals(Object obj) {
 		if(!(obj instanceof OperatorTree)) return false;
 		return compareTo((OperatorTree) obj) == 0;
 	}
 
 	Iterable<OTRawString> leafs() { return OTRawStringIterator.iterable(this); }
 	Iterable<String> leafsAsString() { return Utils.map(leafs(), OTRawString.toStringConverter()); }
 
     Iterable<String> vars() {
     	Iterable<String> leafs = leafsAsString();
     	return Utils.filter(leafs, new Utils.Predicate<String>() {
 			public boolean apply(String s) {
 				try {
 					new OTNumber(s);
 					return false; // it's an OTNumber -> it's not a var
 				}
 				catch(NumberFormatException ex) {
 					return true; // it's not an OTNumber -> it's a var
 				}
 			}
     	});
 	}
 
 	Collection<OperatorTree> allFactors(boolean includingSelf) {
 		Iterable<Iterable<OperatorTree>> subs = Utils.map(entities, new Utils.Function<OTEntity,Iterable<OperatorTree>>() {
 			public Iterable<OperatorTree> eval(OTEntity obj) {
 				if(obj instanceof OTRawString) {
 					String s = ((OTRawString) obj).content;
 					try {
 						new OTNumber(s);
 						return Utils.listFromArgs(); // it's an OTNumber -> it's not a var
 					}
 					catch(NumberFormatException ignored) {} // it's not an OTNumber -> it's a var
 					return Utils.listFromArgs(Variable(s));
 				}
 				else { // obj instanceof OTSubtree
 					return ((OTSubtree) obj).content.allFactors(true);
 				}
 			}
 		});
 
 		if(includingSelf && isFunctionCall())
 			return Utils.concatCollectionView(Utils.listFromArgs(this), Utils.concatCollectionView(subs));
 		return Utils.concatCollectionView(subs);
 	}
 
 	Collection<OperatorTree> baseFactors() {
 		Set<OperatorTree> facs = new TreeSet<OperatorTree>(allFactors(true));
 		facs.removeAll(baseFactorBlackList());
 		return facs;
 	}
 
 	Set<OperatorTree> baseFactorBlackList() {
 		if(isFunctionCall()) return new HashSet<OperatorTree>(allFactors(false));
 
 		if(entities.size() > 1 && (op.length() != 1 || "+-∙/".indexOf(op) < 0))
 			return new HashSet<OperatorTree>(allFactors(false));
 
 		Iterable<Iterable<OperatorTree>> subs = Utils.map(entities, new Utils.Function<OTEntity,Iterable<OperatorTree>>() {
 			public Iterable<OperatorTree> eval(OTEntity obj) {
 				if(obj instanceof OTRawString) {
 					return Utils.listFromArgs();
 				}
 				else { // obj instanceof OTSubtree
 					return ((OTSubtree) obj).content.baseFactorBlackList();
 				}
 			}
 		});
 
 		return new HashSet<OperatorTree>(Utils.concatCollectionView(subs));
 	}
 
 	Iterable<String> ops() {
 		return new Iterable<String>() {
 			public Iterator<String> iterator() {
 				return new Iterator<String>() {
 					Iterator<String> childs = null;
 					public boolean hasNext() { return childs == null || childs.hasNext(); }
 					public String next() {
 						if(childs == null) {
 							Iterable<OTSubtree> subtrees = Utils.filterType(OperatorTree.this.entities, OTSubtree.class);
 							Iterable<Iterable<String>> iterables = Utils.map(subtrees, new Utils.Function<OTSubtree,Iterable<String>>() {
 								public Iterable<String> eval(OTSubtree obj) {
 									return obj.content.ops();
 								}
 							});
 							childs = Utils.concatCollectionView(iterables).iterator();
 							return OperatorTree.this.op;
 						}
 						return childs.next();
 					}
 					public void remove() { throw new UnsupportedOperationException(); }
 				};
 			}
 		};
 	}
 	
 	OperatorTree removeObsolete() {
 		if(isFunctionCall()) {
 			OperatorTree ot = new OperatorTree(op);
 			for(OTEntity e : entities) {
 				if(e instanceof OTSubtree)
 					// Don't use asTree as below because we want to keep one OTSubtree around.
 					// Semantically, this wouldn't be needed but we keep it as a convention
 					// and for prettier output.
 					e = new OTSubtree(((OTSubtree) e).content.removeObsolete(), ((OTSubtree) e).explicitEnclosing);
 				ot.entities.add(e);
 			}
 			return ot;
 		}
 
 		if(entities.size() == 1) {
 			if(entities.get(0) instanceof OTSubtree)
 				return ((OTSubtree) entities.get(0)).content.removeObsolete();
 			return this;
 		}
 			
 		OperatorTree ot = new OperatorTree(op);
 
 		if(op.equals("+") || op.equals("-")) {
 			boolean first = true;
 			for(OTEntity e : entities) {
 				if(e instanceof OTSubtree)
 					e = ((OTSubtree) e).content.removeObsolete().asEntity();
 				if(!e.asTree().isZero() || (op.equals("-") && first))
 					ot.entities.add(e);
 				first = false;
 			}
 		}
 		else if(op.equals("∙") || op.equals("/")) {
 			boolean first = true;
 			for(OTEntity e : entities) {
 				if(e instanceof OTSubtree)
 					e = ((OTSubtree) e).content.removeObsolete().asEntity();
 				if(!e.asTree().isOne() || (op.equals("/") && first))
 					ot.entities.add(e);
 				first = false;
 			}
 		}
 		else {
 			for(OTEntity e : entities) {
 				if(e instanceof OTSubtree)
 					e = ((OTSubtree) e).content.removeObsolete().asEntity();
 				ot.entities.add(e);
 			}
 		}
 		
 		return ot;
 	}
 	
     OperatorTree mergeOps(Set<String> ops) {
     	if(!isFunctionCall() && entities.size() == 1 && entities.get(0) instanceof OTSubtree)
     		return ((OTSubtree) entities.get(0)).content.mergeOps(ops);
     	
     	OperatorTree ot = new OperatorTree(op);
     	for(OTEntity e : entities) {
     		if(e instanceof OTRawString)
     			ot.entities.add(e);
     		else {
     			OperatorTree subtree = ((OTSubtree) e).content.mergeOps(ops);
     			if(subtree.op.equals(ot.op) && ops.contains(op) && !subtree.canBeInterpretedAsUnaryPrefixed())
     				ot.entities.addAll(subtree.entities);
     			else
     				ot.entities.add(new OTSubtree(subtree, ((OTSubtree) e).explicitEnclosing));
     		}
     	}
     	return ot;
     }
     
     OperatorTree mergeOpsFromRight(Set<String> ops) {
     	if(!isFunctionCall() && entities.size() == 1 && entities.get(0) instanceof OTSubtree)
     		return ((OTSubtree) entities.get(0)).content.mergeOpsFromRight(ops);
     	
     	OperatorTree ot = new OperatorTree(op);
     	boolean first = true;
     	for(OTEntity e : entities) {
     		if(e instanceof OTRawString)
     			ot.entities.add(e);
     		else {
     			OperatorTree subtree = ((OTSubtree) e).content.mergeOpsFromRight(ops);
     			if(first && subtree.op.equals(ot.op) && ops.contains(op) && !subtree.canBeInterpretedAsUnaryPrefixed())
     				ot = subtree;
     			else
     				ot.entities.add(new OTSubtree(subtree, ((OTSubtree) e).explicitEnclosing));
     		}
     		first = false;
     	}
     	return ot;
     }        
     
     OperatorTree mergeOps(String ops) { return mergeOps(OTParser.simpleParseOps(ops)); }        
     OperatorTree mergeOpsFromRight(String ops) { return mergeOpsFromRight(OTParser.simpleParseOps(ops)); }
     OperatorTree simplify() { return mergeOps("=+∙").mergeOpsFromRight("-/^").removeObsolete(); }
     
     OperatorTree transformOp(String oldOp, String newOp, Utils.Function<OTEntity,OTEntity> leftTransform, Utils.Function<OTEntity,OTEntity> rightTransform) {
     	OperatorTree ot = new OperatorTree();
     	if(!op.equals(oldOp) || canBeInterpretedAsUnaryPrefixed()) {
     		ot.op = op;
         	for(OTEntity e : entities) {
         		if(e instanceof OTSubtree)
         			ot.entities.add( new OTSubtree( ((OTSubtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform), ((OTSubtree) e).explicitEnclosing ) );
         		else // e instanceof RawString
         			ot.entities.add(e);
         	}
         	return ot;
     	}
     	
     	// op == oldOp here 
     	ot.op = newOp;
     	boolean first = true;
     	for(OTEntity e : entities) {
     		if(e instanceof OTSubtree)
     			e = new OTSubtree( ((OTSubtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform), ((OTSubtree) e).explicitEnclosing );
     		if(first) {
     			if(leftTransform != null) e = leftTransform.eval(e);
     		} else {
     			if(rightTransform != null) e = rightTransform.eval(e);
     		}
     		ot.entities.add(e);
     		first = false;
     	}
     	return ot;
     }
     
     OperatorTree transformMinusToPlus() {
 		return transformOp("-", "+", null,
 				new Utils.Function<OTEntity,OTEntity>() {
 					public OTEntity eval(OTEntity obj) {
 						return new OTSubtree(obj.prefixed("-"));
 					}
 				});
     }
     
     OperatorTree transformMinusPushedDown() {
     	return transformMinusPushedDown(false).asTree();
     }
     OTEntity transformMinusPushedDown(boolean negate) {
     	if(canBeInterpretedAsUnaryPrefixed() && op.equals("-")) {        		
     		OTEntity e = unaryPrefixedContent();
     		negate = !negate;
     		if(e instanceof OTSubtree)
     			return ((OTSubtree) e).content.transformMinusPushedDown(negate);
     		if(negate)
     			return new OTSubtree(e.prefixed("-"));
     		return e;
     	}
     	else if(op.isEmpty() || (op.length() == 1 && "+-∙/".indexOf(op) >= 0)) {
     		OperatorTree ot = new OperatorTree();
     		ot.op = op;
     		for(OTEntity e : entities) {
     			if(e instanceof OTSubtree) {
     				OTSubtree origSubtree = (OTSubtree) e;
     				e = origSubtree.content.transformMinusPushedDown(negate);
     				if(e instanceof OTSubtree)
     					((OTSubtree) e).explicitEnclosing = origSubtree.explicitEnclosing;
     			}
     			else {
     				if(negate)
     					e = new OTSubtree(e.prefixed("-"));
     			}
     			ot.entities.add(e);
     			// don't negate further entries if this is a multiplication/division
     			if(op.equals("∙") || op.equals("/")) negate = false;
     		}
     		return new OTSubtree(ot);
     	}
 	    else {
 		    if(negate) return prefixed("-").asEntity(); // NOTE: we cannot use minusOne() because that would be circular
 		    return asEntity();
 	    }
     }
     
 	OperatorTree sum(OperatorTree other) {
 		if(this.isZero()) return other;
 		if(other.isZero()) return this;
 
 		if(     (this.op.equals("+") || (!this.isFunctionCall() && this.entities.size() == 1)) &&
 				(other.op.equals("+") || (!other.isFunctionCall() && other.entities.size() == 1)))
 			return Sum(new LinkedList<OTEntity>(Utils.concatCollectionView(entities, other.entities)));
 					
 		if(this.op.equals("+"))
 			return Sum(new LinkedList<OTEntity>(Utils.concatCollectionView(this.entities, Utils.listFromArgs(other.asEntity()))));
 
 		if(other.op.equals("+"))
 			return Sum(new LinkedList<OTEntity>(Utils.concatCollectionView(Utils.listFromArgs(this.asEntity()), other.entities)));
 
 		return Sum(Utils.listFromArgs(this.asEntity(), other.asEntity()));
 	}
 
     OperatorTree minusOne() {
     	if(canBeInterpretedAsUnaryPrefixed() && op.equals("-"))
     		return unaryPrefixedContent().asTree();
     	return transformMinusPushedDown(true).asTree();
 	}
 
 	OTEntity asEntity() {
 		if(entities.size() == 1 && !isFunctionCall())
 			return entities.get(0);
 		return new OTSubtree(this);
 	}
     
 	OperatorTree divide(OperatorTree other) {
 		if(isZero()) return this;
 		if(other.isOne()) return this;
 		if(other.isNumber(-1)) return this.minusOne();
 		{
 			OTNumber thisNum = this.asNumber(), otherNum = other.asNumber();
 			if(thisNum != null && otherNum != null) {
 				try { return Number(thisNum.divide(otherNum)); }
 				catch(ArithmeticException ignored) {}
 			}
 		}
 		if(op.equals("/")) {
 			if(entities.size() == 2)
 				return new OperatorTree("/", Utils.listFromArgs(entities.get(0), entities.get(1).asTree().multiply(other).asEntity()));
 			OperatorTree ot = new OperatorTree(op);
 			for(OTEntity e : entities) ot.entities.add(e);
 			ot.entities.add(other.asEntity());
 			return ot;
 		}
 		return new OperatorTree("/", Utils.listFromArgs(asEntity(), other.asEntity()));
 	}
 	
 	OperatorTree mergeDivisions() {
 		if(!isFunctionCall() && entities.size() == 1) {
 			OTEntity e = entities.get(0);
 			if(e instanceof OTSubtree)
 				return ((OTSubtree)e).content.mergeDivisions();
 			return this;
 		}
 		
 		if(op.equals("∙")) {
 			OperatorTree nom = One(), denom = One();
 			for(OTEntity e : entities) {
 				OperatorTree ot = e.asTree().mergeDivisions();
 				if(ot.op.equals("/")) {
 					int i = 0;
 					for(OTEntity e2 : ot.entities) {
 						if(i == 0) nom = nom.multiply(e2.asTree());
 						else denom = denom.multiply(e2.asTree());
 						i++;
 					}
 				}
 				else nom = nom.multiply(ot);
 			}
 			return nom.divide(denom);
 		}
 		else if(op.equals("/")) {
 			OperatorTree nom = One(), denom = One();
 			int i = 0;
 			for(OTEntity e : entities) {
 				OperatorTree ot = e.asTree().mergeDivisions();
 				if(ot.op.equals("/")) {
 					int j = 0;
 					for(OTEntity e2 : ot.entities) {
 						if(j == 0 && i == 0 || j > 0 && i > 0) nom = nom.multiply(e2.asTree());
 						else denom = denom.multiply(e2.asTree());
 						j++;
 					}
 				}
 				else {
 					if(i == 0) nom = nom.multiply(e.asTree());
 					else denom = denom.multiply(e.asTree());
 				}
 				i++;
 			}
 			return nom.divide(denom);
 		}
 		else {
 			OperatorTree ot = new OperatorTree(op);
 			for(OTEntity e : entities)
 				ot.entities.add(e.asTree().mergeDivisions().asEntity());
 			return ot;
 		}
 	}
 
 	OperatorTree asSum() {
 		if(op.equals("+")) return this;
 		return Sum(Utils.listFromArgs(asEntity()));
 	}
 	
 	OperatorTree asProduct() {
 		if(op.equals("∙")) return this;
 		return Product(Utils.listFromArgs(asEntity()));
 	}
 
 	Utils.Pair<OperatorTree,OTNumber> normedPot() {
 		if(!op.equals("^")) return new Utils.Pair<OperatorTree,OTNumber>(this,OTNumber.ONE);
 		if(entities.size() == 0) return new Utils.Pair<OperatorTree,OTNumber>(Number(1),OTNumber.ONE);
 		if(entities.size() == 1) return new Utils.Pair<OperatorTree,OTNumber>(entities.get(0).asTree(),OTNumber.ONE);
 
 		OTNumber power = entities.get(entities.size()-1).asTree().asNumber();
 		if(power == null) return new Utils.Pair<OperatorTree,OTNumber>(this,OTNumber.ONE);
 		if(power.compareTo(OTNumber.ZERO) == 0) return new Utils.Pair<OperatorTree,OTNumber>(One(),OTNumber.ONE);
 		if(entities.size() == 2) return new Utils.Pair<OperatorTree,OTNumber>(entities.get(0).asTree(),power);
 
 		Utils.Pair<OperatorTree,OTNumber> rest = subtree(0, entities.size()-1).normedPot();
 		rest.second = rest.second.multiply(power);
 		if(rest.first.isOne() || rest.first.isZero()) rest.second = OTNumber.ONE;
 		return rest;
 	}
 
 	static private void __mergeProductFactor(SortedMap<OperatorTree,OTNumber> facs, OperatorTree ot) {
 		Utils.Pair<OperatorTree,OTNumber> pot = ot.normedPot();
 		OTNumber power = facs.get(pot.first);
 		if(power == null) power = OTNumber.ZERO;
 		power = power.add(pot.second);
 		facs.put(pot.first, power);
 	}
 
 	Utils.Pair<OTNumber,List<OperatorTree>> normedProductFactorList() {
 		if(isNumber()) return new Utils.Pair<OTNumber,List<OperatorTree>>(asNumber(), Utils.<OperatorTree>listFromArgs());
 		if(op.equals("^")) {
 			Utils.Pair<OperatorTree,OTNumber> pot = normedPot();
 			return new Utils.Pair<OTNumber,List<OperatorTree>>(OTNumber.ONE, Utils.<OperatorTree>listFromArgs(Power(pot)));
 		}
 		if(op.equals("-") && canBeInterpretedAsUnaryPrefixed()) {
 			Utils.Pair<OTNumber,List<OperatorTree>> ret = unaryPrefixedContent().asTree().normedProductFactorList();
 			ret.first = ret.first.negate();
 			return ret;
 		}
 		if(!op.equals("∙")) return new Utils.Pair<OTNumber,List<OperatorTree>>(OTNumber.ONE, Utils.listFromArgs(this));
 
 		OTNumber fac = OTNumber.ONE;
 		SortedMap<OperatorTree,OTNumber> facs = new java.util.TreeMap<OperatorTree,OTNumber>();
 
 		for(OTEntity e : entities) {
 			OperatorTree eOt = e.asTree();
 			if(eOt.isNumber()) {
 				fac = fac.multiply(eOt.asNumber());
 				if(fac.compareTo(OTNumber.ZERO) == 0)
 					return new Utils.Pair<OTNumber,List<OperatorTree>>(OTNumber.ZERO, Utils.<OperatorTree>listFromArgs());
 				continue;
 			}
 
 			if(eOt.isNegative()) {
 				eOt = eOt.minusOne();
 				fac = fac.negate();
 			}
 
 			if(eOt.op.equals("∙")) {
 				Utils.Pair<OTNumber,List<OperatorTree>> prod = eOt.normedProductFactorList();
 				fac = fac.multiply(prod.first);
 				if(fac.compareTo(OTNumber.ZERO) == 0)
 					return new Utils.Pair<OTNumber,List<OperatorTree>>(OTNumber.ZERO, Utils.<OperatorTree>listFromArgs());
 				for(OperatorTree ot : prod.second)
 					__mergeProductFactor(facs, ot);
 				continue;
 			}
 
 			__mergeProductFactor(facs, eOt);
 		}
 
 		Utils.Pair<OTNumber,List<OperatorTree>> ret = new Utils.Pair<OTNumber,List<OperatorTree>>(fac, new LinkedList<OperatorTree>());
 		for(OperatorTree ot : facs.keySet()) {
 			OTNumber power = facs.get(ot);
 			if(power.compareTo(OTNumber.ONE) == 0)
 				ret.second.add(ot);
 			else if(power.compareTo(OTNumber.ZERO) != 0)
 				ret.second.add(Power(ot, Number(power)));
 		}
 		return ret;
 	}
 
 	static OperatorTree productFactorListAsTree(Utils.Pair<OTNumber,List<OperatorTree>> prod) {
 		if(prod.first.compareTo(OTNumber.ZERO) == 0) return Zero();
 		if(prod.second.isEmpty()) return Number(prod.first);
 		if(prod.first.compareTo(OTNumber.ONE) == 0 && prod.second.size() == 1) return prod.second.get(0);
 
 		OperatorTree ot = new OperatorTree("∙");
 		if(prod.first.compareTo(OTNumber.ONE) != 0 && prod.first.compareTo(OTNumber.ONE.negate()) != 0)
 			ot.entities.add(Number(prod.first).asEntity());
 		boolean first = true;
 		for(OperatorTree e : prod.second) {
 			if(first && prod.first.compareTo(OTNumber.ONE.negate()) == 0)
 				ot.entities.add(e.minusOne().asEntity());
 			else
 				ot.entities.add(e.asEntity());
 			first = false;
 		}
 		return ot;
 	}
 
 	OperatorTree normedSum() { return normedSum(true); }
 	OperatorTree normedSum(boolean alsoNormFactor) {
		if(isNumber()) return Number(asNumber());
 		if(op.equals("∙") || op.equals("^")) return productFactorListAsTree(normedProductFactorList());
 		if(!op.equals("+")) return this;
 
 		List<Utils.Pair<OTNumber,List<OperatorTree>>> newEntries = new LinkedList<Utils.Pair<OTNumber,List<OperatorTree>>>();
 		for(OTEntity prod : entities)
 			newEntries.add(prod.asTree().asProduct().normedProductFactorList());		
 		Collections.sort(newEntries, Utils.<OTNumber,List<OperatorTree>>pairComparatorSecond(Utils.<OperatorTree,List<OperatorTree>>collectionComparator()));
 		Utils.Pair<OTNumber,List<OperatorTree>> lastProd = null;
 
 		OperatorTree sum = new OperatorTree("+");
 		for(Utils.Pair<OTNumber,List<OperatorTree>> prod : newEntries) {
 			OTNumber fac = prod.first;
 			List<OperatorTree> facs = prod.second;
 			if(lastProd != null && lastProd.second.equals(facs)) {
 				lastProd.first = lastProd.first.add(fac);
 				sum.entities.set(sum.entities.size()-1, productFactorListAsTree(lastProd).asEntity());
 			} else {
 				lastProd = prod;
 				sum.entities.add(productFactorListAsTree(prod).asEntity());
 			}
 		}
 		for(Iterator<OTEntity> pit = sum.entities.iterator(); pit.hasNext();) {
 			if(pit.next().asTree().isZero())
 				pit.remove();
 		}
 
 		if(alsoNormFactor) {
 			OTNumber gcd = OTNumber.gcd(Utils.map(sum.entities, new Utils.Function<OTEntity,OTNumber>() {
 				public OTNumber eval(OTEntity prod) {
 					return prod.asTree().asProduct().normedProductFactorList().first;
 				}
 			}));
 			sum = sum.multiply(Number(gcd.multiplicativeInverse()));	
 		}
 
 		if(alsoNormFactor && !sum.entities.isEmpty() && sum.entities.get(0).asTree().isNegative())
 			return sum.minusOne();
 		return sum;
 	}
 
 	Collection<OperatorTree> sumEntries() {
 		if(op.equals("+") && !entities.isEmpty()) return Utils.map(entities, OTUtils.entityToTreeFunc);
 		return Utils.listFromArgs(this);
 	}
 
 	Collection<OperatorTree> prodEntries() {
 		if(op.equals("∙") && !entities.isEmpty()) return Utils.map(entities, OTUtils.entityToTreeFunc);
 		return Utils.listFromArgs(this);
 	}
 
 	@SuppressWarnings({"UnusedDeclaration"})
 	OperatorTree mergedEquation() {
 		if(!op.equals("=")) return this;
 		if(entities.size() != 2) return this;
 		return MergedEquation(entities.get(0).asTree(), entities.get(1).asTree());
 	}
 
 	OperatorTree normalized() {
 		return
 		 transformMinusToPlus()
 		.simplify()
 		.transformMinusPushedDown()
 		.multiplyAllDivisions()
 		.pushdownAllMultiplications()
 		.transformMinusPushedDown()
 		.normedSum();
 	}
 
 	OperatorTree divideIfReducing(OperatorTree ot, boolean requireRemove) {
 		if(equals(ot)) return One();
 		if(minusOne().equals(ot)) return Number(-1);
 		if(ot.isOne()) return null;
 		
 		if(op.equals("+")) {
 			OperatorTree sum = new OperatorTree("+");
 			for(OTEntity p : entities) {
 				OperatorTree t = p.asTree().divideIfReducing(ot, requireRemove);
 				if(t == null) return null;
 				sum.entities.add(t.asEntity());
 			}
 			return sum;
 		}
 
 		else { // take as product
 			Utils.Pair<OTNumber,List<OperatorTree>> prod = normedProductFactorList();
 			
 			if(ot.isNumber()) {
 				prod.first = prod.first.divide(ot.asNumber());
 				if(prod.first.isInteger())
 					return productFactorListAsTree(prod);
 				return null;
 			}
 			
 			for(int i = 0; i < prod.second.size(); ++i) {
 				Utils.Pair<OperatorTree,OTNumber> p = prod.second.get(i).normedPot();
 				if(p.first.equals(ot)) {
 					if(p.second.compareTo(OTNumber.ONE) > 0 && !requireRemove) {
 						p.second = p.second.subtract(OTNumber.ONE);
 						prod.second.set(i, Power(p));
 						return productFactorListAsTree(prod);
 					}
 					if(p.second.isOne()) {
 						prod.second.remove(i);
 						return productFactorListAsTree(prod);
 					}
 					break;
 				}
 			}
 			return null; // var not included or not pot=1
 		}
 	}
 
 	OperatorTree reducedSum() {
 		// IMPORTANT: assumes that we can simply divide by all variables
 		// See comment in EquationSystem about the assumption that all vars!=0.
 		if(entities.size() <= 1) return this;
 
 		List<OperatorTree> firstProdEntries = new LinkedList<OperatorTree>(sumEntries().iterator().next().prodEntries());
 		OperatorTree sum = this;
 		for(OperatorTree p : firstProdEntries) {
 			if(p.isNumber()) continue; // we only want to handle variables here
 			Utils.Pair<OperatorTree,OTNumber> pot = p.normedPot();
 			int potSecond = pot.second.intValueExact();
 			for(int i = 0; i < potSecond; ++i) {
 				OperatorTree newSum = sum.divideIfReducing(pot.first, false);
 				if(newSum == null) break;
 				sum = newSum;
 			}
 		}
 		return sum;
 	}
 
 	static OTNumber commonPowerInProductFactorList(Utils.Pair<OTNumber,List<OperatorTree>> prod) {
 		OTNumber c = null;
 		for(OperatorTree ot : prod.second) {
 			Utils.Pair<OperatorTree,OTNumber> pot = ot.normedPot();
 			if(c == null) c = pot.second;
 			else c = c.gcd(pot.second);
 		}
 		if(c == null) return OTNumber.ONE;
 		return c;
 	}
 
 	@SuppressWarnings({"UnusedDeclaration"})
 	OperatorTree reducedPotInSum() {
 		// IMPORTANT: Assumes that all variables are >=0 and thus that we can reduce any powers. E.g. A^2=B^2 -> A=B
 
 		Collection<OperatorTree> entries = sumEntries();
 		if(entries.size() != 2) return this;
 		Iterator<OperatorTree> entriesIter = entries.iterator();
 
 		Utils.Pair<OTNumber,List<OperatorTree>> prod1 = entriesIter.next().normedProductFactorList();
 		Utils.Pair<OTNumber,List<OperatorTree>> prod2 = entriesIter.next().normedProductFactorList();
 		if(prod1.first.multiply(prod2.first).compareTo(OTNumber.valueOf(-1)) != 0) return this; // we require that we can write this as 'A = B'
 
 		OTNumber c1 = commonPowerInProductFactorList(prod1), c2 = commonPowerInProductFactorList(prod2);
 		OTNumber c = c1.gcd(c2);
 		if(c.compareTo(OTNumber.ONE) <= 0) return this;
 
 		// we can divide each power by c
 		for(List<OperatorTree> prodList : Utils.listFromArgs(prod1.second,prod2.second)) {
 			for(int i = 0; i < prodList.size(); ++i) {
 				Utils.Pair<OperatorTree,OTNumber> pot = prodList.get(i).normedPot();
 				pot.second = pot.second.divide(c);
 				prodList.set(i, Power(pot));
 			}
 		}
 
 		return Sum(Utils.listFromArgs(productFactorListAsTree(prod1).asEntity(), productFactorListAsTree(prod2).asEntity()));
 	}
 
 	static class ExtractedFactor {
 		OperatorTree fac;
 		OperatorTree varMult = new OperatorTree("+");
 		OperatorTree independentPart = new OperatorTree("+");
 		@Override public String toString() {
 			return "{var=" + fac + ", varMult=" + varMult + ", independentPart=" + independentPart + "}";
 		}
 	}
 
 	ExtractedFactor extractFactor(OperatorTree fac) {
 		ExtractedFactor extracted = new ExtractedFactor();
 		extracted.fac = fac;
 		for(OperatorTree p : sumEntries()) {
 			if(p.isZero()) continue;
 			OperatorTree newP = p.divideIfReducing(fac, true);
 			if(newP != null)
 				extracted.varMult.entities.add(newP.asEntity());
 			else
 				extracted.independentPart.entities.add(p.asEntity());
 		}
 		if(!extracted.varMult.entities.isEmpty())
 			return extracted;
 		return null;
 	}
 
 	OperatorTree firstProductInSum() { // expecting that we are a sum
 		if(entities.isEmpty()) return null;
 		if(op.equals("+") || op.equals("-")) return entities.get(0).asTree().asProduct();
 		return null;
 	}
 	
 	OperatorTree simplifyDivision() {
 		if(op.equals("/") && entities.size() == 2) {
 			OperatorTree nom = entities.get(0).asTree().normedSum(false).asSum().copy(), denom = entities.get(1).asTree().normedSum(false).asSum().copy();
 			if(nom.entities.isEmpty() || denom.entities.isEmpty()) return this;
 			Utils.Pair<OTNumber,List<OperatorTree>> nomProd = nom.firstProductInSum().normedProductFactorList();
 			Utils.Pair<OTNumber,List<OperatorTree>> denomProd = denom.firstProductInSum().normedProductFactorList();
 			if(denomProd.first.compareTo(OTNumber.ZERO) < 0) {
 				nomProd.first = nomProd.first.negate();
 				denomProd.first = denomProd.first.negate();
 				for(int i = 1; i < nom.entities.size(); ++i)
 					nom.entities.set(i, nom.entities.get(i).asTree().minusOne().asEntity());
 				for(int i = 1; i < denom.entities.size(); ++i)
 					denom.entities.set(i, denom.entities.get(i).asTree().minusOne().asEntity());
 			}
 			nom.entities.set(0, productFactorListAsTree(nomProd).asEntity());
 			denom.entities.set(0, productFactorListAsTree(denomProd).asEntity());
 
 			//System.out.println("div: " + nomProd + " / " + denomProd);
 			for(OperatorTree ot : new ArrayList<OperatorTree>(Utils.concatCollectionView(denomProd.second, Utils.listFromArgs(denom, Number(denomProd.first))))) {
 				if(ot.isNumber(1) || ot.isNumber(-1)) continue;
 
 				Utils.Pair<OperatorTree,OTNumber> pot = ot.normedPot();
 				while(true) {
 					OperatorTree newNom = nom.divideIfReducing(pot.first, false);
 					OperatorTree newDenom = denom.divideIfReducing(pot.first, false);
 					//System.out.println("removing " + pot.first.debugStringDouble() + " from " + nom.debugStringDouble() + ": " + newNom);
 					//System.out.println("removing " + pot.first.debugStringDouble() + " from " + denom.debugStringDouble() + ": " + newDenom);
 
 					if(newNom != null && newDenom != null) {
 						nom = newNom.asSum(); denom = newDenom.asSum();
 						nomProd = nom.firstProductInSum().normedProductFactorList();
 						denomProd = denom.firstProductInSum().normedProductFactorList();
 					}
 					else break;
 				}
 				if(nomProd == null || denomProd == null) break;
 			}
 			
 			return nom.divide(denom);
 		}
 		return this;
 	}
 	
 	OperatorTree nextDivision() {
 	    // NOTE: must match haveDenominatorInSubtree traversal
 		if(entities.isEmpty()) return null;
 		if(op.equals("/") && entities.size() >= 2) return entities.get(entities.size()-1).asTree();
 		if(entities.size() == 1 && !isFunctionCall()) {
 			if(entities.get(0) instanceof OTRawString) return null;
 			return entities.get(0).asTree().nextDivision();
 		}
 		if(op.length() != 1 || "+-∙/".indexOf(op) < 0) return null;
 		for(OTEntity e : entities) {
 			if(e instanceof OTSubtree) {
 				OperatorTree next = ((OTSubtree) e).content.nextDivision();
 				if(next != null) return next;
 			}
 		}
 		return null;
 	}
 	
     OperatorTree multiplyAllDivisions() {
     	OperatorTree ot = this;
         OperatorTree nextDiv;
 	    int counter = 0;
 	    final int COUNTER_LIMIT = 1000;
     	while((nextDiv = ot.nextDivision()) != null) {
     		ot = ot.multiply(nextDiv);
 		    counter++;
 		    if(counter > COUNTER_LIMIT)
 			    throw new AssertionError("counter limit reached\n ot: " + ot.debugStringDouble() + "\n nextDiv: " + nextDiv.debugStringDouble());
 	    }
     	return ot;
     }
     
     boolean matchDenominatorInDiv(OperatorTree denom) {
     	return op.equals("/") && entities.size() > 1 && entities.get(entities.size()-1).asTree().equals(denom);
     }
     
     boolean haveDenominatorInSubtree(OperatorTree denom) {
 	    // NOTE: must match nextDivision traversal
     	if(matchDenominatorInDiv(denom)) return true;
 	    if(entities.size() == 1 && !isFunctionCall()) {
 		    if(entities.get(0) instanceof OTRawString) return false;
 		    return entities.get(0).asTree().haveDenominatorInSubtree(denom);
 	    }
 	    if(op.length() != 1 || "+-∙/".indexOf(op) < 0) return false;
 	    for(OTEntity e : entities) {
 		    if(e instanceof OTSubtree) {
 			    if(((OTSubtree) e).content.haveDenominatorInSubtree(denom))
 				    return true;
 		    }
 	    }
     	return false;
     }
     
     OperatorTree multiply(OperatorTree other) {
     	if(isZero()) return this;
     	if(isOne()) return other;
     	if(isNumber(-1)) return other.minusOne();
     	if(other.isOne()) return this;
     	if(other.isNumber(-1)) return this.minusOne();
     	if(isNumber() && other.isNumber())
     		return Number(asNumber().multiply(other.asNumber()));
     	
     	OperatorTree ot = new OperatorTree(op);
 
     	if(op.equals("∙")) {
     		for(int i = 0; i < entities.size(); ++i) {
     			OperatorTree subtree = entities.get(i).asTree(); 
     			if(subtree.haveDenominatorInSubtree(other) || (subtree.isNumber() && other.isNumber())) {
     				// multiply exactly with this index
     				for(int j = 0; j < entities.size(); ++j) {
     					if(i != j)
     						ot.entities.add(entities.get(j));
     					else
     						ot.entities.add(entities.get(j).asTree().multiply(other).asEntity());
     				}
     				return ot;
     			}
     		}
     		ot.entities.addAll(entities);
     		if(other.op.equals(op))
     			ot.entities.addAll(other.entities);
     		else
     			ot.entities.add(other.asEntity());
     		return ot;
     	}
     	
     	if(canBeInterpretedAsUnaryPrefixed() && !op.equals("/")) {
     		OTEntity e = unaryPrefixedContent();
     		if(e instanceof OTSubtree)
     			return ((OTSubtree) e).content.multiply(other).prefixed(op);
     		ot.op = "∙";
     		ot.entities.add(asEntity());
     		ot.entities.add(other.asEntity());
     		return ot;
     	}
     	
     	if(!isFunctionCall() && entities.size() == 1) {
     		OTEntity e = entities.get(0);
     		if(e instanceof OTSubtree)
     			return ((OTSubtree) e).content.multiply(other);
     		ot.op = "∙";
     		ot.entities.add(e);
     		ot.entities.add(other.asEntity());
     		return ot;
     	}
 
     	if(op.equals("/")) {
     		if(entities.isEmpty())
     			ot.entities.add(other.asEntity());
     		else if(matchDenominatorInDiv(other)) {
     			ot.entities.addAll(entities);
     			ot.entities.remove(entities.size()-1);
     			if(ot.entities.size() == 1)
     				ot = ot.entities.get(0).asTree();
     		}
     		else {
     			boolean first = true;
             	for(OTEntity e : entities) {
             		if(first)
             			ot.entities.add(e.asTree().multiply(other).asEntity());
             		else
             			ot.entities.add(e);
             		first = false;
             	}
     		}
     		return ot;
     	}
     	
 	    if(op.isEmpty() || op.equals("+") || op.equals("-")) {
     	    for(OTEntity e : entities)
 	    		ot.entities.add(e.asTree().multiply(other).asEntity());
 	    	return ot;
 	    }
 
 	    ot.op = "∙";
 	    ot.entities.add(this.asEntity());
 	    ot.entities.add(other.asEntity());
 	    return ot;
     }
     
     OperatorTree pushdownMultiplication(OperatorTree other) {
     	if(this.isOne()) return other;
 		if((op.equals("+") || op.equals("-")) && !canBeInterpretedAsUnaryPrefixed()) {
 			OperatorTree newOt = new OperatorTree(op);
 			for(OTEntity e : entities) {
 				OperatorTree ot = e.asTree().pushdownMultiplication(other);
 				if(ot.op.equals("+"))
 					newOt.entities.addAll(ot.entities);
 				else
 					newOt.entities.add(ot.asEntity());
 			}
 			return newOt;
 		}
 		if(other.op.equals("+") || other.op.equals("-") && !other.canBeInterpretedAsUnaryPrefixed()) {
 			OperatorTree newOt = new OperatorTree(other.op);
 			for(OTEntity e : other.entities) {
 				OperatorTree ot = pushdownMultiplication(e.asTree());
 				if(ot.op.equals("+"))
 					newOt.entities.addAll(ot.entities);
 				else
 					newOt.entities.add(ot.asEntity());
 			}
 			return newOt;
 		}
 		if(canBeInterpretedAsUnaryPrefixed() && op.equals("-") && other.canBeInterpretedAsUnaryPrefixed() && other.op.equals("-")) {
 			return unaryPrefixedContent().asTree().pushdownMultiplication(other.unaryPrefixedContent().asTree());
 		}
 		if(op.equals("∙")) {
 			OperatorTree newOt = new OperatorTree(op);
 			newOt.entities.addAll(entities);
 			if(other.op.equals("∙"))
 				newOt.entities.addAll(other.entities);
 			else
 				newOt.entities.add(other.asEntity());
 			return newOt;
 		}
     	return multiply(other);
     }
     
     OperatorTree pushdownAllMultiplications() {
     	if(op.equals("∙")) {
     		OperatorTree ot = One();
     		for(OTEntity e : entities)
     			ot = ot.pushdownMultiplication(e.asTree().pushdownAllMultiplications());
     		return ot;
     	}
     	else {
         	OperatorTree ot = new OperatorTree(op);
         	for(OTEntity e : entities) {
         		if(e instanceof OTSubtree) {
         			OperatorTree subtree = ((OTSubtree) e).content.pushdownAllMultiplications();
         			if(op.equals("+") && subtree.op.equals("+"))
         				ot.entities.addAll(subtree.entities);
         			else
         				ot.entities.add(subtree.asEntity());
         		}
         		else
         			ot.entities.add(e);
         	}
         	return ot;
     	}
     }
 
 }
