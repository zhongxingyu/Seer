 package de.codescape.bitvunit.hamcrest;
 
 import de.codescape.bitvunit.rule.Rule;
 import de.codescape.bitvunit.rule.Violations;
 import de.codescape.bitvunit.ruleset.BasicRuleSet;
 import de.codescape.bitvunit.ruleset.RuleSet;
import de.codescape.bitvunit.util.Assert;
 import org.hamcrest.Description;
 import org.hamcrest.Factory;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 
 import static de.codescape.bitvunit.util.html.HtmlPageUtil.toHtmlPage;
 
 /**
  * Hamcrest matcher to be used to run accessibility checks against a single {@link Rule} or a {@link RuleSet}.
  * <p/>
  * <b>Usage examples:</b>
  * <pre><code>
  * assertThat(supportedType, is(compliantTo(ruleSet)));
  * assertThat(supportedType, is(compliantTo(rule)));
  * </code></pre>
  * <p/>
  * Have a look at {@link de.codescape.bitvunit.util.html.HtmlPageUtil} methods for supported types.
  *
  * @param <T> one of the supported types
  * @author Stefan Glase
  * @see de.codescape.bitvunit.util.html.HtmlPageUtil
  * @since 0.4
  */
 public class ComplianceMatcher<T> extends TypeSafeMatcher<T> {
 
     private final RuleSet ruleSet;
     private Violations violations;
 
     /**
      * Creates a new {@link ComplianceMatcher} against the provided {@link RuleSet}.
      *
      * @param ruleSet the {@link RuleSet} to be used
      */
     public ComplianceMatcher(RuleSet ruleSet) {
         this.ruleSet = ruleSet;
     }
 
     @Override
     protected boolean matchesSafely(T item) {
         violations = ruleSet.applyTo(toHtmlPage(item));
         return !violations.hasViolations();
     }
 
     @Override
     public void describeTo(Description description) {
         description.appendText("compliant to ").appendText(ruleSet.toString());
     }
 
     @Override
     protected void describeMismatchSafely(T item, Description mismatchDescription) {
        Assert.notNull("Call matches method before.",violations);
         mismatchDescription.appendText(violations.toString());
     }
 
     /**
      * Returns a {@link ComplianceMatcher} that checks one of the supported types (see JavaDoc at class level) against
      * the given {@link RuleSet}.
      *
      * @param ruleSet {@link RuleSet} that should be used
      * @param <T>     supported types are contained in JavaDoc at class level
      * @return {@link ComplianceMatcher} to check against the {@link RuleSet}
      */
     @Factory
     public static <T> Matcher<T> compliantTo(RuleSet ruleSet) {
         return new ComplianceMatcher<T>(ruleSet);
     }
 
     /**
      * Returns a {@link ComplianceMatcher} that checks one of the supported types (see JavaDoc at class level) against
      * the given {@link Rule}.
      *
      * @param rule {@link Rule} that should be used
      * @param <T>  supported types are contained in JavaDoc at class level
      * @return {@link ComplianceMatcher} to check against the {@link Rule}
      */
     @Factory
     public static <T> Matcher<T> compliantTo(Rule rule) {
         return compliantTo(new BasicRuleSet(rule));
     }
 
 }
