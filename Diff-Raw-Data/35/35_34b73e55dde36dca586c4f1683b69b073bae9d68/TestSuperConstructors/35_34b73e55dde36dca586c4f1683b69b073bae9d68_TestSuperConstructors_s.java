 package ch.codedump.ooplss.test;
 
 import org.junit.Test;
 
 import ch.codedump.ooplss.symbolTable.exceptions.ArgumentDoesntMatchException;
 import ch.codedump.ooplss.symbolTable.exceptions.NoSuperTypeException;
 import ch.codedump.ooplss.symbolTable.exceptions.UnknownSuperClassException;
 import ch.codedump.ooplss.utils.ErrorHandler;
 
 
 public class TestSuperConstructors extends OoplssTest {
 	@Test
 	public void testSuperConstructor() throws Exception {
 		String str = 	"class foo {}" +
 						"class bar subtypeOf foo {" +
 						"	def __construct() : base() {}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=NoSuperTypeException.class)
 	public void testInvalidSuperConstructor() throws Exception {
 		String str = 	"class bar {" +
 						"	def __construct() : base() {}" +
 						"}";
 		try {
 			this.createTyper(str);
 		} catch (NullPointerException e) {}
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testMultipleSuperConstructor() throws Exception {
 		String str = 	"class foo {}" +
 						"class A {}" +
						"class bar subtypeOf foo supclassOf A {" +
 						"	def __construct() : base(), A() {}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=UnknownSuperClassException.class)
 	public void testIllegalMultipleSuperConstructor() throws Exception {
 		String str = 	"class foo {}" +
 						"class bar subtypeOf foo {" +
 						"	def __construct() : base(), A() {}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testSuperTypeConstructorArguments() throws Exception {
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subtypeOf foo {" +
 						"	def __construct(): base(\"abc\") {}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();				
 	}
 	
 	@Test
 	public void testSuperClassConstructorArguments() throws Exception {
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def __construct(): foo(\"abc\") {}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();				
 	}
 	
 	@Test (expected=ArgumentDoesntMatchException.class)
 	public void testInvalidSuperConstructorArguments() throws Exception {
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subtypeOf foo {" +
 						"	def __construct(): base(3) {}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();				
 	}
 	
 	@Test (expected=ArgumentDoesntMatchException.class)
 	public void testEmptySuperConstructorArguments() throws Exception {
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subtypeOf foo {" +
 						"	def __construct(): base() {}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();				
 	}
 	
 	@Test (expected=NoSuperTypeException.class)
 	public void testNoSuperTypeException() throws Exception {
 		
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def __construct(): base() {}" +
 						"}";
 		try {
 			this.createTyper(str);
 		} catch (NullPointerException e) {}
 		
 		ErrorHandler.getInstance().throwException();				
 	}
 	
 	@Test (expected=ArgumentDoesntMatchException.class)
 	public void testEmptySuperClassArguments() throws Exception {
 		
 		String str = 	"class foo {" +
 						"	def __construct(o:String) {}" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def __construct(): foo() {}" +
 						"}";
 		try {
 			this.createTyper(str);
 		} catch (NullPointerException e) {}
 		
 		ErrorHandler.getInstance().throwException();				
 	}
 }
