 package de.codescape.bitvunit.rule.page;
 
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
import org.w3c.dom.DocumentType;
 
 /**
  * DoctypeForPageRule ensures that every page includes a <code>&lt;!DOCTYPE /&gt;</code> element to support rendering
  * and validation of this page by browsers and other viewers or interpreters.
  *
  * @author Stefan Glase
  * @since 0.10
  */
 public class DoctypeForPageRule extends AbstractRule {
 
     private static final String RULE_NAME = "DoctypeForPage";
     private static final String RULE_MESSAGE = "Every page must include a DOCTYPE declaration to support rendering and validation.";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
        DocumentType doctype = page.getDoctype();
        if (doctype == null) {
             violations.add(createViolation(null, page, RULE_MESSAGE));
         }
     }
 
 }
