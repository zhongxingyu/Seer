 /*
  * $Id $
  * 
  * $Date$ $Revision$ $Author$
  * 
  * Copyright CERN ${year}, All Rights Reserved.
  */
 package cern.accsoft.steering.jmad.modeldefs.defs.lhc;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized.Parameters;
 
 import test.LabelledParameterized;
 import cern.accsoft.steering.jmad.JMadException;
 import cern.accsoft.steering.jmad.domain.ex.JMadModelException;
 import cern.accsoft.steering.jmad.domain.machine.RangeDefinition;
 import cern.accsoft.steering.jmad.domain.optics.Optic;
 import cern.accsoft.steering.jmad.domain.result.tfs.TfsSummary;
 import cern.accsoft.steering.jmad.domain.var.enums.MadxGlobalVariable;
 import cern.accsoft.steering.jmad.model.JMadModel;
 import cern.accsoft.steering.jmad.modeldefs.JMadModelDefinitionManager;
 import cern.accsoft.steering.jmad.modeldefs.domain.JMadModelDefinition;
 import cern.accsoft.steering.jmad.modeldefs.domain.OpticsDefinition;
 import cern.accsoft.steering.jmad.modeldefs.lhc.LhcUtil;
 import cern.accsoft.steering.jmad.service.JMadService;
 import cern.accsoft.steering.jmad.service.JMadServiceFactory;
 
 /**
  * Test if all lsa model optics are matched correctly in tune and chroma. For now we only check the 2011 optics for the
  * FULL lhc ranges (lhcb1, lhcb2 ALL)
  * 
  * @author muellerg
  */
 @RunWith(value = LabelledParameterized.class)
 public class OpticsTest {
    private static final Logger LOGGER = Logger.getLogger(OpticsTest.class);
 
     private static JMadModel MODEL;
    private static int MODEL_LOOP_CNT = 0;
    private static final int MAX_MODEL_LOOP_CNT = 30;
 
     /** XXX optics names which need higher tolerances on tune and chroma values: */
     private static List<String> OPTICS_NAMES_HIGHER_TOLERANCE = Arrays.asList(new String[] {
             "A5400C5400A1000L1000_2011", "A9000C9000A1000L1000_2011", "A7500C7500A1000L1000_2011",
             "A5100C5100A1000L1000_2011", "A5100C5100A1000L1000_2011", "A5400C5400A1000L1000_2011",
             "A6700C6700A1000L1000_2011", "A3000C3000A1000L1000_2011", "A5100C5100A1000L1000_2011",
             "A7500C7500A1000L1000_2011", "A3000C3000A1000L1000_2011", "A3300C3300A1000L1000_2011",
             "A4600C4600A1000L1000_2011", "A4000C4000A1000L1000_2011", "A4300C4300A1000L1000_2011",
             "A4600C4600A1000L1000_2011", "A4300C4300A1000L1000_2011", "A3600C3600A1000L1000_2011",
             "A6700C6700A1000L1000_2011", "A6000C6000A1000L1000_2011", "A9000C9000A1000L1000_2011",
             "A4000C4000A1000L1000_2011", "A6000C6000A1000L1000_2011", "A3600C3600A1000L1000_2011" });
 
     static {
         BasicConfigurator.configure();
         JMadService service = JMadServiceFactory.createJMadService();
         JMadModelDefinitionManager modelDefinitionManager = service.getModelDefinitionManager();
        JMadModelDefinition modelDefinition = modelDefinitionManager
                .getModelDefinition(LhcUtil.NOMINAL_MODEL_DEFINITION_NAME);
         MODEL = service.createModel(modelDefinition);
        LOGGER.info("Set-Up");
     }
 
     @Parameters
     public static final Collection<Object> getParamters() {
         List<Object> parameters = new ArrayList<Object>();
         for (OpticsDefinition opticDefinition : MODEL.getModelDefinition().getOpticsDefinitions()) {
             /* check only 2011 optics */
             if (!opticDefinition.getName().contains("2011")) {
                 continue;
             }
 
             double tuneChromeDelta = 1e-3;
             if (OPTICS_NAMES_HIGHER_TOLERANCE.contains(opticDefinition.getName())) {
                 tuneChromeDelta = 3e-3;
             }
 
             for (RangeDefinition rangeDefinition : MODEL.getModelDefinition().getRangeDefinitions()) {
                 /* only lhcb1 and lhcb2 full range without twiss initial conditions */
                 if (!rangeDefinition.getName().equals(LhcUtil.DEFAULT_RANGE_NAME)) {
                     continue;
                 }
 
                 parameters.add(new Object[] {
                         rangeDefinition.getSequenceDefinition().getName() + "(" + rangeDefinition.getName() + ") - "
                                 + opticDefinition.getName(), opticDefinition, rangeDefinition,
                         new Double(tuneChromeDelta) });
             }
         }
 
         return parameters;
     }
 
     /** the range definition to test */
     private final RangeDefinition rangeDefinition;
     /** the optic definition to test */
     private final OpticsDefinition opticDefinition;
     /** the delta values to use for tune and chroma testing */
     private Double tuneChromaDelta;
 
    /**
     * the constructor of the test
     * 
     * @throws JMadModelException
     */
     public OpticsTest(String testName, OpticsDefinition opticDefinition, RangeDefinition rangeDefinition,
            Double tuneChromaDelta) throws JMadModelException {

        LOGGER.info("***********************************  START EXECUTION OF: " + testName);

         this.opticDefinition = opticDefinition;
         this.rangeDefinition = rangeDefinition;
         this.tuneChromaDelta = tuneChromaDelta;
     }
 
     /**
      * set the optic definition and the range to test in the model and calculate the optics
      * 
      * @throws JMadException
      */
     @Test
     public void getOpticsForOpticAndRange() throws JMadException {
        if (MODEL_LOOP_CNT++ > MAX_MODEL_LOOP_CNT) {
            MODEL.reset();
            LOGGER.info("*********** MODEL RESETTED");
        }

         assertNotNull("Optic definition should not be null", opticDefinition);
         assertNotNull("Range definition should not be null", rangeDefinition);
 
         MODEL.setActiveOpticsDefinition(opticDefinition);
         MODEL.setActiveRangeDefinition(rangeDefinition);
 
         Optic optic = MODEL.getOptics();
         assertNotNull("Calculated optic MUST not be null", optic);
     }
 
     /**
      * retrieve the twiss summary and check that the chroma and tune values are correct as well as that the right range
      * is loaded, according to the sequence flag in the summary
      * 
      * @throws JMadModelException
      */
     @Test
     public void checkTuneAndChroma() throws JMadModelException {
         TfsSummary tfsSummary = MODEL.calcTwissSummary();
         this.ensureRightSequence(tfsSummary, rangeDefinition);
         this.ensureRightTuneChromaValues(opticDefinition, tfsSummary);
     }
 
     private void ensureRightTuneChromaValues(OpticsDefinition opticsDefinition, TfsSummary tfsSummary) {
         Map<MadxGlobalVariable, Double> tuneChromaMap = new HashMap<MadxGlobalVariable, Double>();
         if (opticsDefinition.getName().contains("INJ")) {
             tuneChromaMap.put(MadxGlobalVariable.DQ1, 2.0);
             tuneChromaMap.put(MadxGlobalVariable.DQ2, 2.0);
             tuneChromaMap.put(MadxGlobalVariable.Q1, 64.28);
             tuneChromaMap.put(MadxGlobalVariable.Q2, 59.31);
         } else {
             tuneChromaMap.put(MadxGlobalVariable.DQ1, 2.0);
             tuneChromaMap.put(MadxGlobalVariable.DQ2, 2.0);
             tuneChromaMap.put(MadxGlobalVariable.Q1, 64.31);
             tuneChromaMap.put(MadxGlobalVariable.Q2, 59.32);
         }
 
         for (Entry<MadxGlobalVariable, Double> entry : tuneChromaMap.entrySet()) {
             double currentValue = tfsSummary.getDoubleValue(entry.getKey()).doubleValue();
             assertEquals(entry.getKey() + " delta value should be below " + this.tuneChromaDelta + " for optic "
                     + opticsDefinition.getName(), entry.getValue().doubleValue(), currentValue, this.tuneChromaDelta);
         }
     }
 
     private void ensureRightSequence(TfsSummary tfsSummary, RangeDefinition rangeDefinition) {
         String tfsSequenceName = tfsSummary.getStringValue(MadxGlobalVariable.SEQUENCE).toUpperCase();
         String activeSequenceName = rangeDefinition.getSequenceDefinition().getName().toUpperCase();
         assertEquals("Loaded sequence and name of sequence in tfs should be equal.", activeSequenceName,
                 tfsSequenceName);
     }
 
     @AfterClass
     public static void tearDown() {
         try {
             MODEL.cleanup();
            LOGGER.info("Teared-Down");
         } catch (JMadModelException e) {
             e.printStackTrace();
         }
     }
 }
