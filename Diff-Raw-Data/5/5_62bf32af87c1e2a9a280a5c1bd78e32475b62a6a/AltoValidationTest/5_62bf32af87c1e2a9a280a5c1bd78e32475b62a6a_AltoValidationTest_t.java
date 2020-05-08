 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import static org.testng.AssertJUnit.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 
 public class AltoValidationTest {
 
 	private ResultCollector resultCollector = null;
 	
 	@BeforeTest
 	public void setUp() {
 		resultCollector = new ResultCollector("test", "test");
 	}
 
     /**
      * Tests success for 2J16 - nested textblocks with the same language.
      */
     @Test
     public void shouldSucceed2J16() {
         setUp();
                SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
                AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml") {
                    @Override
                    public InputStream getData() throws IOException {
                       return Thread.currentThread().getContextClassLoader().getResourceAsStream("goodData/good.alto.xml");
                    }
 
                    @Override
                    public String getChecksum() throws IOException {
                        return null;
                    }
                };
                handler.handleAttribute(event);
                assertTrue(resultCollector.isSuccess());
     }
 
     /**
      * Tests failure for 2J16 - nested textblocks with different languages
      */
     @Test
     public void shouldFail2J16() {
         setUp();
                SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
                AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml") {
                    @Override
                    public InputStream getData() throws IOException {
                       return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.alto.xml");
                    }
 
                    @Override
                    public String getChecksum() throws IOException {
                        return null;
                    }
                };
                handler.handleAttribute(event);
                assertFalse(resultCollector.isSuccess());
                assertTrue(resultCollector.toReport().contains("2J-16"));
                assertTrue(resultCollector.toReport().contains("klingon"));
     }
 
     @Test
     public void shouldSucceed() {
         setUp();
         SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
         AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml") {
             @Override
             public InputStream getData() throws IOException {
                 return Thread.currentThread().getContextClassLoader().getResourceAsStream("scratch/B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml");
             }
 
             @Override
             public String getChecksum() throws IOException {
                 return null;
             }
         };
         handler.handleAttribute(event);
         System.out.println(resultCollector.toReport());
         assertTrue(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingMeasurementUnit() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit></MeasurementUnit>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToWrongMeasurementUnit() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit>Wrong measurement unit</MeasurementUnit>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingOCRProcessing() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit>inch1200</MeasurementUnit>"
     			+ "      <ocrProcessingStep>"
     			+ "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
     			+ "        <processingSoftware>"
     			+ "          <softwareName>ABBYY Recognition Server</softwareName>"
     			+ "          <softwareVersion>3.0</softwareVersion>"
     			+ "        </processingSoftware>"
     			+ "      </ocrProcessingStep>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingProcessingStepSettings() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit>inch1200</MeasurementUnit>"
     			+ "    <OCRProcessing ID='OCR1'>"
     			+ "      <ocrProcessingStep>"
     			+ "        <processingStepSettings></processingStepSettings>"
     			+ "        <processingSoftware>"
     			+ "          <softwareName>ABBYY Recognition Server</softwareName>"
     			+ "          <softwareVersion>3.0</softwareVersion>"
     			+ "        </processingSoftware>"
     			+ "      </ocrProcessingStep>"
     			+ "    </OCRProcessing>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingSoftwareName() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit>inch1200</MeasurementUnit>"
     			+ "    <OCRProcessing ID='OCR1'>"
     			+ "      <ocrProcessingStep>"
     			+ "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
     			+ "        <processingSoftware>"
     			+ "          <softwareName></softwareName>"
     			+ "          <softwareVersion>3.0</softwareVersion>"
     			+ "        </processingSoftware>"
     			+ "      </ocrProcessingStep>"
     			+ "    </OCRProcessing>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingSoftwareVersion() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Description>"
     			+ "    <MeasurementUnit>inch1200</MeasurementUnit>"
     			+ "    <OCRProcessing ID='OCR1'>"
     			+ "      <ocrProcessingStep>"
     			+ "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
     			+ "        <processingSoftware>"
     			+ "          <softwareName>ABBYY Recognition Server</softwareName>"
     			+ "          <softwareVersion></softwareVersion>"
     			+ "        </processingSoftware>"
     			+ "      </ocrProcessingStep>"
     			+ "    </OCRProcessing>"
     			+ "  </Description>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingPageHeightAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page WIDTH='9304'>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyPageHeightAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='' WIDTH='9304'>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingPageWidthAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408'>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyPageWidthAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH=''>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingStringHeightAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyStringHeightAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
     
     @Test
     public void shouldFailDueToMissingStringWidthAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
     
     @Test
     public void shouldFailDueToEmptyStringWidthAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingStringHPosAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyStringHPosAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingStringVPosAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='584' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyStringVPosAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToMissingStringContentAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToEmptyStringContentAttribute() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToStringContentAttributeContainingTwoWords() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT='Ao. Ao.' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldFailDueToStringContentAttributeContainingTwoWordsStillAllowingWhitespaceAsPrefixOrPostfix() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT=' Ao. Ao. ' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertFalse(resultCollector.isSuccess());
     }
 
     @Test
     public void shouldPassWhenStringContentAttributeContainsWhitespaceAsPrefixOrPostfix() {
     	final String input = ""
     			+ "<?xml version='1.0' encoding='UTF-8'?>"
     			+ "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
     			+ "  <Layout>"
     			+ "    <Page HEIGHT='11408' WIDTH='9304'>"
     			+ "      <PrintSpace>"
     			+ "        <TextBlock language='dan'>"
     			+ "          <TextLine>"
     			+ "            <String CONTENT=' Ao. ' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
     			+ "          </TextLine>"
     			+ "        </TextBlock>"
     			+ "      </PrintSpace>"
     			+ "    </Page>"
     			+ "  </Layout>"
     			+ "</alto>";
     	
         setUp();
         handleTestEvent(input, resultCollector);
         assertTrue(resultCollector.isSuccess());
     }
 
     private void handleTestEvent(final String input, ResultCollector resultCollector) {
 		SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
         AttributeParsingEvent event = new AttributeParsingEvent("test.alto.xml") {
             @Override
             public InputStream getData() throws IOException {
                 return new ByteArrayInputStream(input.getBytes("UTF-8"));
             }
 
             @Override
             public String getChecksum() throws IOException {
                 return null;
             }
         };
         handler.handleAttribute(event);
 	}
 }
