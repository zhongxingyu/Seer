 package org.openmrs.module;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.flowsheet.FlowsheetEntry;
 import org.openmrs.module.flowsheet.impl.FlowsheetService;
 import org.openmrs.test.BaseContextSensitiveTest;
 
 public class FlowsheetServiceTest extends BaseContextSensitiveTest {
 	private List<FlowsheetEntry> getFlowSheetEntry(int personId) {
 		return service.getFlowsheet(personId).getEntries();
 	}
 
 	private FlowsheetService service;
 	private FlowsheetEntry entry;
 
 	@Before
 	public void setUp() throws Exception {
 		service = Context.getService(FlowsheetService.class);
 		entry = getFlowSheetEntry(7).get(0);
 	}
 
 	@Test
 	public void shouldReturnObservationsForPerson() {
 		Assert.assertEquals(0, getFlowSheetEntry(1).size());
		Assert.assertEquals(9, getFlowSheetEntry(7).size());
 	}
 
 	@Test
 	public void shouldReturnConceptNameForEachObservation() {
 		Assert.assertEquals("WT", entry.getName());
 	}
 
 	@Test
 	public void shouldReturnValueForEachObservation() {
 		Assert.assertEquals("50.0", entry.getValue());
 	}
 
 	@Test
 	public void shouldReturnDataTypeForEachObservation() {
 		Assert.assertEquals("Numeric", entry.getDataType());
 	}
 
 	@Test
 	public void shouldReturnClassTypeForEachObservation() {
 		Assert.assertEquals("Test", entry.getClassType());
 	}
 
 	@Test
 	public void shouldReturnDateForEachObservation() {
 		Assert.assertEquals("2008-07-01", entry.getDate());
 	}
 
 	@Test
 	public void shouldReturnUnitForEachObservation() throws Exception {
 		Assert.assertEquals("kg", entry.getNumeric().getUnit());
 	}
 
 	@Test
 	@Ignore("Add additional data set to test this")
 	public void shouldReturnCommentObservation() throws Exception {
 		FlowsheetEntry entry = getFlowSheetEntry(7).get(10);
 		Assert.assertEquals("", entry.getComment());
 	}
 
 	@Test
 	@Ignore("Add additional data set to test this")
 	public void shouldReturnHiValueEachObservation() throws Exception {
 		Assert.assertEquals("250.0", entry.getNumeric().getHi().toString());
 	}
	
 	@Test
	@Ignore("Add additional data set to test this")
 	public void shouldReturnHiLowAsEmpty() throws Exception {
 		Assert.assertEquals("", entry.getNumeric().getHi());
 		Assert.assertEquals("", entry.getNumeric().getLow());
 	}
 
 }
