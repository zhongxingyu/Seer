 package descent.internal.compiler.lookup;
 
 import descent.core.IField;
 import descent.core.JavaModelException;
 import descent.internal.compiler.parser.ASTNodeEncoder;
 import descent.internal.compiler.parser.Expression;
 import descent.internal.compiler.parser.IDsymbol;
 import descent.internal.compiler.parser.IExpInitializer;
 import descent.internal.compiler.parser.IInitializer;
 import descent.internal.compiler.parser.ISignatureConstants;
 import descent.internal.compiler.parser.IVarDeclaration;
 import descent.internal.compiler.parser.Initializer;
 import descent.internal.compiler.parser.Loc;
 import descent.internal.compiler.parser.Scope;
 import descent.internal.compiler.parser.SemanticContext;
 import descent.internal.compiler.parser.SemanticMixin;
 import descent.internal.compiler.parser.Type;
 import descent.internal.core.SourceField;
 import descent.internal.core.SourceFieldElementInfo;
 import descent.internal.core.util.Util;
 
 public class RVarDeclaration extends RDeclaration implements IVarDeclaration {
 	
 	private Type type;
 	private int nestedref;
 	private Initializer init;
 	private boolean initComputed;
 
 	public RVarDeclaration(IField element, SemanticContext context) {
 		super(element, context);
 	}
 
 	public int canassign() {
 		return 0;
 	}
 	
 	public Expression callAutoDtor() {
 		throw new IllegalStateException("Should not be called");
 	}
 
 	public void checkNestedReference(Scope sc, Loc loc, SemanticContext context) {
 		SemanticMixin.checkNestedReference(this, sc, loc, context);
 	}
 
 	public boolean ctorinit() {
 		return false;
 	}
 
 	public void ctorinit(boolean c) {
 		throw new IllegalStateException("Should not be called");
 	}
 
 	public IExpInitializer getExpInitializer(SemanticContext context) {
 		return SemanticMixin.getExpInitializer(this, context);
 	}
 
 	public IInitializer init() {
 		if (!initComputed) {
 			// TODO: expose this value via the IField interface?
 			SourceField f = (SourceField) element;
 			try {
 				SourceFieldElementInfo info = (SourceFieldElementInfo) f.getElementInfo();
 				char[] encodedValue = info.getInitializationSource();
 				if (encodedValue != null) {
 					init = ASTNodeEncoder.decodeInitializer(encodedValue);
 					// Run semantic in order to compute type
 					if (init != null) {
 						init.semantic(null, type(), context);
 					}
 				}
 			} catch (JavaModelException e) {
 				Util.log(e);
 			}
 			initComputed = true;
 		}
 		return init;
 	}
 	
 	public void init(IInitializer init) {
 		throw new IllegalStateException("Should not be called");
 	}
 
 	public int inuse() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	@Override
 	public boolean isDataseg(SemanticContext context) {
 		return SemanticMixin.isDataseg(this, context);
 	}
 
 	public boolean noauto() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public int offset() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void offset(int offset) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public Type type() {
 		if (type == null) {
 			type = getTypeFromField();
 		}
 		return type;
 	}
 	
 	@Override
 	public IDsymbol toAlias(SemanticContext context) {
 		// TODO Auto-generated method stub
 		return this;
 	}
 
 	public Expression value() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void value(Expression value) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public int nestedref() {
 		return nestedref;
 	}
 	
 	public void nestedref(int nestedref) {
 		this.nestedref = nestedref;
 	}
 	
 	@Override
 	public IVarDeclaration isVarDeclaration() {
 		return this;
 	}
 	
 	public char getSignaturePrefix() {
 		return ISignatureConstants.VARIABLE;
 	}
 
 }
