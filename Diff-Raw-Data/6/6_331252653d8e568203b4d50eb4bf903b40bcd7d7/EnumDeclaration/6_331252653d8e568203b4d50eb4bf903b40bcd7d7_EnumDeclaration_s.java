 package descent.internal.compiler.parser;
 
 import java.math.BigInteger;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.IType;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.lookup.SemanticRest;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 // DMD 1.020
 public class EnumDeclaration extends ScopeDsymbol {
 
 	private final static int N_2 = 2;
 	private final static int N_128 = 128;
 	private final static int N_256 = 256;
 	private final static int N_0x8000 = 0x8000;
 	private final static int N_0x10000 = 0x10000;
 	private final static long N_0x80000000 = 0x80000000L;
 	private final static long N_0x100000000 = 0x100000000L;
 	private final static long N_0x8000000000000000 = 0x8000000000000000L;
 
 	public Type type; // the TypeEnum
 	public Type memtype, sourceMemtype; // type of the members
 	public integer_t maxval;
 	public integer_t minval;
 	public integer_t defaultval; // default initializer
 	
 	private IType javaElement;
 	
 	public SemanticRest rest;
 
 	public EnumDeclaration(Loc loc, IdentifierExp id, Type memtype) {
 		super(id);
 		this.loc = loc;
 		this.type = new TypeEnum(this);
 		this.memtype = this.sourceMemtype = memtype;
 		this.maxval = integer_t.ZERO;
 		this.minval = integer_t.ZERO;
 		this.defaultval = integer_t.ZERO;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, modifiers);
 			TreeVisitor.acceptChildren(visitor, ident);
 			TreeVisitor.acceptChildren(visitor, memtype);
 			TreeVisitor.acceptChildren(visitor, members);
 			
 			acceptSynthetic(visitor);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public int getNodeType() {
 		return ENUM_DECLARATION;
 	}
 
 	@Override
 	public Type getType() {
 		return type;
 	}
 
 	@Override
 	public EnumDeclaration isEnumDeclaration() {
 		return this;
 	}
 
 	@Override
 	public String kind() {
 		return "enum";
 	}
 
 	@Override
 	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
 		if (isAnonymous()) {
 			return super.oneMembers(members, ps, context);
 		}
 		return super.oneMember(ps, context);
 	}
 	
 	@Override
 	public Dsymbol search(Loc loc, char[] ident, int flags, SemanticContext context) {
 		consumeRest();
 		
 		return super.search(loc, ident, flags, context);
 	}
 
 	@Override
 	public void semantic(Scope sc, SemanticContext context) {
 		if (rest != null && !rest.isConsumed()) {
 			if (rest.getScope() == null) {
 				rest.setSemanticContext(sc, context);
 			}
 			return;
 		}
 		
 		integer_t number;
 		Type t;
 		Scope sce;
 
 		if (symtab != null) { // if already done
 			return;
 		}
 
 		if (memtype == null) {
 			memtype = Type.tint32;
 		}
 
 		parent = sc.scopesym;
 		memtype = memtype.semantic(loc, sc, context);
 
 		/*
 		 * Check to see if memtype is forward referenced
 		 */
 		if (memtype.ty == TY.Tenum) {
 			EnumDeclaration sym = (EnumDeclaration) memtype.toDsymbol(sc,
 					context);
 			if (sym.memtype == null) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.BaseEnumIsForwardReference, sourceMemtype));
 				memtype = Type.tint32;
 			}
 		}
 
 		if (!memtype.isintegral()) {
 			context.acceptProblem(Problem.newSemanticTypeError(
 					IProblem.EnumBaseTypeMustBeOfIntegralType, sourceMemtype));
 			memtype = Type.tint32;
 		}
 
 		t = isAnonymous() ? memtype : type;
 		symtab = new DsymbolTable();
 		sce = sc.push(this);
 		sce.parent = this;
 		number = integer_t.ZERO;
 		if (members == null) { // enum ident;
 			return;
 		}
 
 		if (members.size() == 0) {
 			context.acceptProblem(Problem.newSemanticTypeErrorLoc(
 					IProblem.EnumMustHaveAtLeastOneMember, this));
 		}
 
 		boolean first = true;
		for (Dsymbol sym : members) {
 			EnumMember em = sym.isEnumMember();
 			Expression e;
 
 			if (em == null) {
 				/*
 				 * The e.semantic(sce) can insert other symbols, such as
 				 * template instances and function literals.
 				 */
 				continue;
 			}
 
 			e = em.value();
 			if (e != null) {
 				e = e.semantic(sce, context);
 				e = e.optimize(ASTDmdNode.WANTvalue, context);
 				// Need to copy it because we're going to change the type
 				e = e.copy();
 				e = e.implicitCastTo(sc, memtype, context);
 				e = e.optimize(ASTDmdNode.WANTvalue, context);
 				number = e.toInteger(context);
 				e.type = t;
 			} else { // Default is the previous number plus 1
 
 				// Check for overflow
 				if (!first) {
 					switch (t.toBasetype(context).ty) {
 					case Tbool:
 						if (number.equals(N_2)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tint8:
 						if (number.equals(N_128)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tchar:
 					case Tuns8:
 						if (number.equals(N_256)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tint16:
 						if (number.equals(N_0x8000)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Twchar:
 					case Tuns16:
 						if (number.equals(N_0x10000)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tint32:
 						if (number.equals(N_0x80000000)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tdchar:
 					case Tuns32:
 						if (number.equals(N_0x100000000)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tint64:
 						if (number.equals(N_0x8000000000000000)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					case Tuns64:
 						// TODO semantic incorrect comparison in Java
 						if (number.equals(BigInteger.ZERO)) {
 							enumValueOverflow(em, context);
 						}
 						break;
 
 					default:
 						throw new IllegalStateException();
 					}
 				}
 				e = new IntegerExp(em.loc, number, t);
 			}
 			em.value(e);
 
 			// Add to symbol table only after evaluating 'value'
 			if (isAnonymous()) {
 				for (Scope scx = sce.enclosing; scx != null; scx = scx.enclosing) {
 					if (scx.scopesym != null) {
 						if (scx.scopesym.symtab == null) {
 							scx.scopesym.symtab = new DsymbolTable();
 						}
 						em.addMember(sce, scx.scopesym, 1, context);
 						break;
 					}
 				}
 			} else {
 				em.addMember(sc, this, 1, context);
 			}
 
 			if (first) {
 				first = false;
 				defaultval = number;
 				minval = number;
 				maxval = number;
 			} else if (memtype.isunsigned()) {
 				if (number.compareTo(minval) < 0) {
 					minval = number;
 				}
 				if (number.compareTo(maxval) > 0) {
 					maxval = number;
 				}
 			} else {
 				// TODO a cast to sinteger_t is missing (I think long in Java)
 				if (number.compareTo(minval) < 0) {
 					minval = number;
 				}
 				if (number.compareTo(maxval) > 0) {
 					maxval = number;
 				}
 			}
 
 			number = number.add(1);
 		}
 		
 		sce.pop();
 		
 		// This is for Descent to compute the signature
 		type.merge(context);
 	}
 
 	@Override
 	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
 		// Descent: lazy initialization
 		consumeRestStructure();
 		
 		Type t = null;
 		if (memtype != null) {
 			t = memtype.syntaxCopy(context);
 		}
 
 		EnumDeclaration ed;
 		if (s != null) {
 			ed = (EnumDeclaration) s;
 			ed.memtype = t;
 		} else {
 			ed = new EnumDeclaration(loc, ident, t);
 		}
 		super.syntaxCopy(ed, context);
 		
 		ed.copySourceRange(this);
 		ed.javaElement = javaElement;
 		
 		return ed;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		int i;
 
 		buf.writestring("enum ");
 		if (ident != null) {
 			buf.writestring(ident.toChars());
 			buf.writeByte(' ');
 		}
 		if (memtype != null) {
 			buf.writestring(": ");
 			memtype.toCBuffer(buf, null, hgs, context);
 		}
 		if (members == null) {
 			buf.writeByte(';');
 			buf.writenl();
 			return;
 		}
 		buf.writenl();
 		buf.writeByte('{');
 		buf.writenl();
 		for (i = 0; i < members.size(); i++) {
 			EnumMember em = (members.get(i)).isEnumMember();
 			if (em == null) {
 				continue;
 			}
 			em.toCBuffer(buf, hgs, context);
 			buf.writeByte(',');
 			buf.writenl();
 		}
 		buf.writeByte('}');
 		buf.writenl();
 	}
 	
 	private final void enumValueOverflow(EnumMember em, SemanticContext context) {
 		context.acceptProblem(Problem.newSemanticTypeErrorLoc(
 				IProblem.EnumValueOverflow, em));
 	}
 	
 	@Override
 	public int getErrorStart() {
 		if (ident != null) {
 			return ident.getErrorStart();
 		} else {
 			return start;
 		}
 	}
 	
 	@Override
 	public int getErrorLength() {
 		if (ident != null) {
 			return ident.getLength();
 		} else {
 			return 4; // "enum".length()
 		}
 	}
 	
 	@Override
 	public char getSignaturePrefix() {
 		return ISignatureConstants.ENUM;
 	}
 	
 	public void setJavaElement(IType javaElement) {
 		this.javaElement = javaElement;
 	}
 	
 	@Override
 	public IType getJavaElement() {
 		return javaElement;
 	}
 	
 	public void consumeRestStructure() {
 		if (rest != null && !rest.isStructureKnown()) {
 			rest.buildStructure();
 		}
 	}
 	
 	public void consumeRest() {
 		if (rest != null && !rest.isConsumed()) {
 			rest.consume(this);
 		}
 	}
 
 }
