 package jp.skypencil.pmd.slf4j;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.sourceforge.pmd.PMD;
 import net.sourceforge.pmd.PMDException;
 import net.sourceforge.pmd.Rule;
 import net.sourceforge.pmd.RuleContext;
 import net.sourceforge.pmd.RuleSet;
 import net.sourceforge.pmd.RuleSets;
 import net.sourceforge.pmd.SourceType;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class ExampleBasedTest {
 		private static final File TEST_SAMPLE_ROOT_DIR = new File("src/test/java/jp/skypencil/pmd/slf4j/example");
 		private final Logger logger = LoggerFactory.getLogger(getClass());
 
 		protected abstract Rule createRule();
 		protected abstract Collection<String> getExpectedExampleNames();
 
		private Set<String> getExampleNames() {
			return new HashSet<String>(Arrays.asList(new String[]{
 				"UsingFormat",
 				"UsingFormatWithoutArgument",
 				"UsingFormatWithoutArgumentButWithException",
 				"UsingFormatWithException",
 				"LogWithException",
 				"UsingStaticLogger",
 				"UsingGetClass",
 				"GivingOtherClassToFactory",
 				"UsingPublicLogger",
 				"UsingProtectedLogger",
 				"UsingPackagePrivateLogger",
 				"UsingPrivateLogger",
 				"UsingNotFinalLogger",
 				"LoggerForInnerClass",
 				"HasOtherField"
			}));
		}
 
 		@Test
 		public void test() {
 			final Collection<String> expected = getExpectedExampleNames();
 			final Collection<String> exampleNames = getExampleNames();
 			for (String expectedName : expected) {
 				if (!exampleNames.contains(expectedName)) {
 					throw new AssertionError();
 				}
 			}
 			for (final String exampleName : exampleNames) {
 				assertResult(exampleName, !expected.contains(exampleName));
 			}
 		}
 
 		private final void assertResult(String sampleFileName, boolean expected) {
 			File sampleFile = new File(TEST_SAMPLE_ROOT_DIR, sampleFileName.concat(".java"));
 			try {
 				assertEquals(sampleFile.getName(), expected, test(sampleFile));
 			} catch (FileNotFoundException e) {
 				logger.error("sample code not found", e);
 				throw new RuntimeException(e);
 			}
 		}
 
 		private boolean test(File file) throws FileNotFoundException {
 			RuleSet ruleset = new RuleSet();
 			Rule rule = createRule();
 
 			if (rule.getMessage() == null) {
 				// AbstractJavaRule#addViolation()縺ｧNullPointerException縺悟�繧九�繧貞屓驕ｿ縺吶ｋ
 				rule.setMessage("");
 			}
 
 			ruleset.addRule(rule);
 			RuleContext ctx = new RuleContext();
 			ctx.setSourceCodeFilename(file.getName());
 			ctx.setSourceType(SourceType.JAVA_16);
 			RuleSets ruleSets = new RuleSets();
 			ruleSets.addRuleSet(ruleset);
 
 			Reader reader = null;
 			try {
 				reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
 				new PMD().processFile(reader, ruleSets, ctx, SourceType.JAVA_16);
 				return ctx.getReport().getViolationTree().size() == 0;
 			} catch (PMDException e) {
 				logger.error("PMDException caused", e);
 				throw new RuntimeException(e);
 			} catch (UnsupportedEncodingException e) {
 				logger.error("UnsupportedEncodingException caused", e);
 				throw new RuntimeException(e);
 			} finally {
 				try {
 					reader.close();
 				} catch (IOException ignore) {
 				}
 			}
 		}
 	}
