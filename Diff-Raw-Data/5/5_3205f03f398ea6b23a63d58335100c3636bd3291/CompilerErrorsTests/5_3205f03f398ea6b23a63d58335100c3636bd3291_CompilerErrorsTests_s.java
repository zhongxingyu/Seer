 package org.caesarj.test;
 
 public class CompilerErrorsTests extends FjTestCase {
 
 	public CompilerErrorsTests(String name) {
 		super(name);
 	}
 		
 	/* cyclic dependencies */
 	public void testCaesarTestCase_200() throws Throwable {
         compileAndCheckErrors("test200", new String[]{""});
     }
 	
 	/* class in cclass */
 	public void testCaesarTestCase_201() throws Throwable {
 	    compileAndCheckErrors("test201", new String[]{""});
     }
 	
 	/* cclass in class */
 	public void testCaesarTestCase_202() throws Throwable {
 	    compileAndCheckErrors("test202", new String[]{""});
     }
 	
 	/* cclass inherit class */
 	public void testCaesarTestCase_203() throws Throwable {
 	    compileAndCheckErrors("test203", new String[]{""});
     }
 	
 	/* class extends cclass */
 	public void testCaesarTestCase_204() throws Throwable {
         compileAndCheckErrors("test204", new String[]{""});
     }
 	
 	/* class implements cclass */
 	public void testCaesarTestCase_205() throws Throwable {
         compileAndCheckErrors("test205", new String[]{""});
     }
 	
 	/* inner extends outer */
 	public void testCaesarTestCase_206() throws Throwable {
         compileAndCheckErrors("test206", new String[]{""});
     }
 	
 	/* outer extends inner */
 	public void testCaesarTestCase_207() throws Throwable {
         compileAndCheckErrors("test207", new String[]{""});
     }
 	
 	/* inner extends external class */
 	public void testCaesarTestCase_208() throws Throwable {
         compileAndCheckErrors("test208", new String[]{""});
     }
 	
 	/* outer cclass extends external inner cclass */
 	public void testCaesarTestCase_209() throws Throwable {
         compileAndCheckErrors("test209", new String[]{""});
     }
 	
 	/* inner inheritance cycle through different branches of mixin combination */
 	public void testCaesarTestCase_210() throws Throwable {
         compileAndCheckErrors("test210", new String[]{""});
     }
 	
 	/* interface in cclass */
 	public void testCaesarTestCase_211() throws Throwable {
         compileAndCheckErrors("test211", new String[]{""});
     }
 	
 	/* mixing plain classes */
 	public void testCaesarTestCase_212() throws Throwable {
         compileAndCheckErrors("test212", new String[]{""});
     }
 	
 	/* repeated mixing */
 	public void testCaesarTestCase_213() throws Throwable {
         compileAndCheckErrors("test213", new String[]{""});
     }
 	
 	/* mixing outer with inner */
 	public void testCaesarTestCase_214() throws Throwable {
         compileAndCheckErrors("test214", new String[]{""});
     }
 	
 	/* mixing interfaces */
 	public void testCaesarTestCase_215() throws Throwable {
         compileAndCheckErrors("test215", new String[]{""});
     }
 	
 	/* class extends interface */
 	public void testCaesarTestCase_216() throws Throwable {
         compileAndCheckErrors("test216", new String[]{""});
     }
 	
 	/* cclass in implements */
 	public void testCaesarTestCase_217() throws Throwable {
         compileAndCheckErrors("test217", new String[]{""});
     }
 	
 	/* abstract cclass */
 	public void testCaesarTestCase_218() throws Throwable {
         compileAndCheckErrors("test218", new String[]{""});
     }
 	
 	/* extending overrriden classes */
 	public void testCaesarTestCase_219() throws Throwable {
         compileAndCheckErrors("test219", new String[]{""});
     }
 	
 	/* changing mixing order */
 	public void testCaesarTestCase_220() throws Throwable {
         compileAndCheckErrors("test220", new String[]{""});
     }
 	
 	/* new operator with parameter */
 	public void testCaesarTestCase_221() throws Throwable {
         compileAndCheckErrors("test221", new String[]{""});
     }
 	
 	/* new array on cclass */
 	public void testCaesarTestCase_222() throws Throwable {
         compileAndCheckErrors("test222", new String[]{""});
     }
 	
 	/* direct construction of inner class */
 	public void testCaesarTestCase_223() throws Throwable {
         compileAndCheckErrors("test223", new String[]{""});
     }
 	
 	/* constructing external inner class */
 	public void testCaesarTestCase_224() throws Throwable {
         compileAndCheckErrors("test224", new String[]{""});
     }
 	
 	/* statically qualified new operator */
 	public void testCaesarTestCase_225() throws Throwable {
         compileAndCheckErrors("test225", new String[]{""});
     }
 	
 	/* qualified new operator inside cclass */
 	public void testCaesarTestCase_226() throws Throwable {
         compileAndCheckErrors("test226", new String[]{""});
     }
 	
 	/* constructing non-existing inner class */
 	public void testCaesarTestCase_227() throws Throwable {
         compileAndCheckErrors("test227", new String[]{""});
     }
 	
 	/* construction of virtual class in static context */
 	public void testCaesarTestCase_228() throws Throwable {
         compileAndCheckErrors("test228", new String[]{""});
     }
 	
 	/* constructor with parameter */
 	public void testCaesarTestCase_229() throws Throwable {
         compileAndCheckErrors("test229", new String[]{""});
     }
 	
 	/* constructor with wrong name */
 	public void testCaesarTestCase_230() throws Throwable {
         compileAndCheckErrors("test230", new String[]{""});
     }
 	
 	/* non-public cclass */
 	public void testCaesarTestCase_231() throws Throwable {
         compileAndCheckErrors("test231", new String[]{""});
     }
 	
 	/* restricting access in overriden method public -> protected */
 	public void testCaesarTestCase_232() throws Throwable {
         compileAndCheckErrors("test232", new String[]{""});
     }
 	
 	/* inner inheritance leads to visibility restriction public -> protected */
 	public void testCaesarTestCase_233() throws Throwable {
         compileAndCheckErrors("test233", new String[]{""});
     }
 	
	/* restricting access in overriden method protected -> private */
	public void testCaesarTestCase_234() throws Throwable {
        compileAndCheckErrors("test234", new String[]{""});
    }
	
 	/* public fields */
 	public void testCaesarTestCase_234b() throws Throwable {
         compileAndCheckErrors("test234b", new String[]{""});
     }
 	
 	/* package visible fields */
 	public void testCaesarTestCase_234c() throws Throwable {
         compileAndCheckErrors("test234c", new String[]{""});
     }
 	
 	/* package visible methods */
 	public void testCaesarTestCase_234d() throws Throwable {
         compileAndCheckErrors("test234d", new String[]{""});
     }
 	
 	/* accessing private method from subclass */
 	public void testCaesarTestCase_235() throws Throwable {
         compileAndCheckErrors("test235", new String[]{""});
     }
 	
 	/* accessing private method from inner class */
 	public void testCaesarTestCase_236() throws Throwable {
         compileAndCheckErrors("test236", new String[]{""});
     }
 	
 	/* accessing protected method from inner class */
 	public void testCaesarTestCase_237() throws Throwable {
         compileAndCheckErrors("test237", new String[]{""});
     }
 	
 	/* accessing protected of the another same class object */
 	public void testCaesarTestCase_238() throws Throwable {
         compileAndCheckErrors("test238", new String[]{""});
     }
 	
 	/* accessing protected of newly created same class object */
 	public void testCaesarTestCase_239() throws Throwable {
         compileAndCheckErrors("test239", new String[]{""});
     }
 	
 	/* protected cclass constructor */
 	public void testCaesarTestCase_240() throws Throwable {
         compileAndCheckErrors("test240", new String[]{""});
     }
 	
 	/* access outer field */
 	public void testCaesarTestCase_241() throws Throwable {
         compileAndCheckErrors("test241", new String[]{""});
     }
 	
 	/* method call from static context */
 	public void testCaesarTestCase_245() throws Throwable {
         compileAndCheckErrors("test245", new String[]{""});
     }
 	
 	/* static method call from within the class */
 	public void testCaesarTestCase_246() throws Throwable {
         compileAndCheckErrors("test246", new String[]{""});
     }
 	
 	/* accessing cclass data members */
 	public void testCaesarTestCase_247() throws Throwable {
         compileAndCheckErrors("test247", new String[]{""});
     }
 	
 	/* accessing outer from outer class */
 	public void testCaesarTestCase_248() throws Throwable {
         compileAndCheckErrors("test248", new String[]{""});
     }
 	
 	/* changing outer */
 	public void testCaesarTestCase_249() throws Throwable {
         compileAndCheckErrors("test249", new String[]{""});
     }
 	
 	/* changing wrappee */
 	public void testCaesarTestCase_249b() throws Throwable {
         compileAndCheckErrors("test249b", new String[]{""});
     }
 	
 	/* private access in overriden class */
 	public void testCaesarTestCase_250() throws Throwable {
         compileAndCheckErrors("test250", new String[]{""});
     }
 	
 	/* duplicate inner class */
 	public void testCaesarTestCase_251() throws Throwable {
         compileAndCheckErrors("test251", new String[]{""});
     }
 	
 	/* duplicate constructor */
 	public void testCaesarTestCase_252() throws Throwable {
         compileAndCheckErrors("test252", new String[]{""});
     }
 	
 	/* duplicate method */
 	public void testCaesarTestCase_253() throws Throwable {
         compileAndCheckErrors("test253", new String[]{""});
     }
 	
 	/* overriding inner with incompatible signature */
 	public void testCaesarTestCase_254() throws Throwable {
         compileAndCheckErrors("test254", new String[]{""});
     }
 	
 	/* overriding method in subclass with incompatible signature */
 	public void testCaesarTestCase_255() throws Throwable {
         compileAndCheckErrors("test255", new String[]{""});
     }
 	
 	/* overriding method with exception specification */
 	public void testCaesarTestCase_256() throws Throwable {
         compileAndCheckErrors("test256", new String[]{""});
     }
 	
 	/* mixing incompatible methods */
 	public void testCaesarTestCase_257() throws Throwable {
         compileAndCheckErrors("test257", new String[]{""});
     }
 	
 	/* mixing inner classes with incompatible signatures */
 	public void testCaesarTestCase_258() throws Throwable {
         compileAndCheckErrors("test258", new String[]{""});
     }
 	
 	/* non-existing super call */
 	public void testCaesarTestCase_259() throws Throwable {
         compileAndCheckErrors("test259", new String[]{""});
     }
 	
 	/* cclass and class with the same name */
 	public void testCaesarTestCase_260() throws Throwable {
         compileAndCheckErrors("test260", new String[]{""});
     }
 	
 	/* assigning to more specific virtual class */
 	public void testCaesarTestCase_261() throws Throwable {
         compileAndCheckErrors("test261", new String[]{""});
     }
 	
 	/* assigning to more specific virtual class inside context of more specific class */
 	public void testCaesarTestCase_262() throws Throwable {
         compileAndCheckErrors("test262", new String[]{""});
     }
 	
 	/* assigning to more specific virtual class inside context of more general class */
 	public void testCaesarTestCase_263() throws Throwable {
         compileAndCheckErrors("test263", new String[]{""});
     }
 	
 	/* outer class objects not covariant */
 	public void testCaesarTestCase_264() throws Throwable {
         compileAndCheckErrors("test264", new String[]{""});
     }
 	
 	/* wraps in outer class */
 	public void testCaesarTestCase_266() throws Throwable {
         compileAndCheckErrors("test266", new String[]{""});
     }
 	
 	/* wraps in simple class */
 	public void testCaesarTestCase_267() throws Throwable {
         compileAndCheckErrors("test267", new String[]{""});
     }
 	
 	/* overriding wraps in overriden class */
 	public void testCaesarTestCase_268() throws Throwable {
         compileAndCheckErrors("test268", new String[]{""});
     }
 	
 	/* overriding wraps in subclass */
 	public void testCaesarTestCase_269() throws Throwable {
         compileAndCheckErrors("test269", new String[]{""});
     }
 	
 	/* applying wrapper function on wrong type */
 	public void testCaesarTestCase_270() throws Throwable {
         compileAndCheckErrors("test270", new String[]{""});
     }
 }
