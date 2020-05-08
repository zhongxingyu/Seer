 package org.eclipse.dltk.ruby.core.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.ruby.core.tests.assist.RubySelectionTests;
 import org.eclipse.dltk.ruby.core.tests.parser.RubyParserRecoveryTests;
 import org.eclipse.dltk.ruby.core.tests.parser.RubyParserTests;
 import org.eclipse.dltk.ruby.core.tests.resources.RubyResourcesTests;
import org.eclipse.dltk.ruby.core.tests.resources.SourceCacheTests;
 import org.eclipse.dltk.ruby.core.tests.resources.SourceModuleInfoCacheTest;
 import org.eclipse.dltk.ruby.core.tests.search.RubyFullNameSearchTests;
 import org.eclipse.dltk.ruby.core.tests.search.RubySearchTests;
 import org.eclipse.dltk.ruby.core.tests.search.mixin.MixinModelManipulationTests;
 import org.eclipse.dltk.ruby.core.tests.search.mixin.MixinProjectIsolationTests;
 import org.eclipse.dltk.ruby.core.tests.search.mixin.MixinTestsSuite;
 import org.eclipse.dltk.ruby.core.tests.search.mixin.RubyMixinClassTests;
 import org.eclipse.dltk.ruby.core.tests.text.completion.RubyCompletionTests;
 import org.eclipse.dltk.ruby.core.tests.typeinference.MethodsTest;
 import org.eclipse.dltk.ruby.core.tests.typeinference.SimpleTest;
 import org.eclipse.dltk.ruby.core.tests.typeinference.StatementsTest;
 import org.eclipse.dltk.ruby.core.tests.typeinference.VariablesTest;
 
 public class AllTests {
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Test for org.eclipse.dltk.ruby.core");
 		// $JUnit-BEGIN$
 		suite.addTest(RubyResourcesTests.suite());
		suite.addTest(SourceCacheTests.suite());
 		suite.addTest(SourceModuleInfoCacheTest.suite());
 		suite.addTest(MixinTestsSuite.suite());
 		suite.addTest(MixinModelManipulationTests.suite());
 		suite.addTest(RubyMixinClassTests.suite());
 
 		suite.addTest(RubySelectionTests.suite());
 		suite.addTest(RubyCompletionTests.suite());
 
 		suite.addTestSuite(RubyParserTests.class);
 		suite.addTestSuite(RubyParserRecoveryTests.class);
 
 		// FIXME: fix running of this tests under mac os x
 		// suite.addTest(StdlibRubyParserTests.suite());
 		// suite.addTest(JRuby1RubyParserTests.suite());
 
 		// XXX: uncomment this tests, when type hierarchies
 		// support will be implemented
 		// suite.addTest(TypeHierarchyTests.suite());
 
 		// Type inference
 		suite.addTest(VariablesTest.suite());
 		suite.addTest(MethodsTest.suite());
 		suite.addTest(StatementsTest.suite());
 		suite.addTest(SimpleTest.suite());
 		suite.addTest(MixinProjectIsolationTests.suite());
 
 		// Launching
 		// suite.addTest(RubyLaunchingTests.suite());
 
 		// Search
 		suite.addTest(RubySearchTests.suite());
 		suite.addTest(RubyFullNameSearchTests.suite());
 
 		// $JUnit-END$
 		return suite;
 	}
 }
