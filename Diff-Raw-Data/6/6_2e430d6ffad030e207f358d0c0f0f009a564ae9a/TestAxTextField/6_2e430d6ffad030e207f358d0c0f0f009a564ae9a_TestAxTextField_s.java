 
 package test.axirassa.webapp.components;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import org.apache.tapestry5.dom.Document;
 import org.apache.tapestry5.dom.Element;
 import org.jaxen.JaxenException;
 import org.junit.Test;
 
 import axirassa.util.test.TapestryPageTest;
 import axirassa.webapp.components.AxTextField;
 
 public class TestAxTextField extends TapestryPageTest {
 	@Test
 	public void structureTest() throws JaxenException {
 		Document page = renderComponentTestPage(AxTextField.class);
 
 		Element txtfield = getElementById(page, "textfield");
 		assertNotNull(txtfield);
 		assertNull(txtfield.getAttribute("disabled"));
 
 		Element txtfieldLabel = getLabelFor(page, "textfield");
 		assertNotNull(txtfieldLabel);
 		assertEquals("Textfield", getElementText(txtfieldLabel));
 
 		Element txtfieldDisabled = getElementById(page, "textfieldDisabled");
 		assertNotNull(txtfieldDisabled);
 		assertNotNull(txtfieldDisabled.getAttribute("disabled"));
 
 		Element txtfieldDisabledLabel = getLabelFor(page, "textfieldDisabled");
 		assertNotNull(txtfieldDisabledLabel);
 		assertEquals("Disabled", getElementText(txtfieldDisabledLabel));
 	}
 
 
 	@Test
 	public void validationTest() throws JaxenException {
 		Document page = renderComponentTestPage(AxTextField.class);
 		Document result = clickSubmitByValue(page, "submit");
 
 		ensureErrorOnField(result, "textfield");
		Element error = getErrorOnField(result, "textfield");
		assertEquals("You must provide a value for Textfield.", getElementText(error));
 
 		ensureErrorOnField(result, "custom");
		assertEquals("You accidently a field", getErrorTextOnField(page, "custom"));
 
 		// disabled field should be fine
 		ensureNoErrorOnField(result, "textfieldDisabled");
 	}
 }
