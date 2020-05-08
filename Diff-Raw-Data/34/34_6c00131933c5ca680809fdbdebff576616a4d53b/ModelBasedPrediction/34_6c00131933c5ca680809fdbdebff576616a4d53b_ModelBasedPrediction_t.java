 package de.hpi_web.cloudSim.arx;
 
 import Jama.Matrix;
 import de.hpi_web.cloudSim.model.ResourceModel;
 import de.hpi_web.cloudSim.model.ResourceModelCollection;
 import de.hpi_web.cloudSim.model.StringConstants;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 public class ModelBasedPrediction {
 
     private Map<String, ResourceModelCollection> models;
     private CSVFileReader running;
     private static int RUNNING_VALUES = 370;
     // mapping for the keys from the model dictionary to the modeledMetrixIndex
     //1 = cpu
     //2 = memory
     //3 = disk read
     //4 = disk write
     //5 = network in
     //6 = network out
     private static final String[] RESOURCE_ORDER = {
         StringConstants.Resource.CPU,
         StringConstants.Resource.MEMORY,
         StringConstants.Resource.HD_READ,
         StringConstants.Resource.HD_WRITE,
         StringConstants.Resource.BANDWIDTH_IN,
         StringConstants.Resource.BANDWIDTH_OUT};
 
     public ModelBasedPrediction(Map<String, ResourceModelCollection> modelMap, String runFile) {
         models = modelMap;
         running = new CSVFileReader(runFile);
         running.ReadFile();
         RUNNING_VALUES = running.getRowsNum();
     }
 
     public List<List<Double>> predictWebTierUtil() {
         int modeledMetricIndices[] = {28, 29, 30, 31, 32, 33};
         return getResultList(modeledMetricIndices, StringConstants.Tier.WEB);
     }
 
     public List<List<Double>> predictAppTierUtil() {
         int modeledMetricIndices[] = {35, 36, 37, 38, 39, 40};
         return getResultList(modeledMetricIndices, StringConstants.Tier.APP);
     }
 
     public List<List<Double>> predictDbTierUtil() {
         int modeledMetricIndices[] = {42, 43, 44, 45, 46, 47};
         return getResultList(modeledMetricIndices, StringConstants.Tier.DB);
     }
 
     private List<List<Double>> getResultList(int modeledMetricIndices[], String tier) {
         List<List<Double>> resultList = new ArrayList<>();
 
         int resourceIdx = 0;
         for (int value : modeledMetricIndices) {
             System.out.println("starting");
             resultList.add(getUsage(value, models.get(tier).get(RESOURCE_ORDER[resourceIdx])));
             resourceIdx++;
         }
         System.out.println(resultList);
         return resultList;
     }
 
     private List<Double> getUsage(int modeledMetricIndex, ResourceModel model) {
         List<Double> result = new ArrayList<>();
 
         Matrix Mv = new Matrix(running.getRowsNum(), running.getColsNum() - 1);
         for (int row = 0; row < running.getRowsNum(); row++) {
             for (int col = 0; col < running.getColsNum() - 1; col++) {
                 Mv.set(row, col, running.getValue(row, col + 1));
             }
         }
 
 
         Matrix x = convertModelToMatrix(model);
         if (x == null) {
             return zeroedList(RUNNING_VALUES);
         }
 
 //	System.out.println("System parameters, (theta) at equation 5: ");
         x.print(2, 4);
 
         int validationInput[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
         Matrix uv = Mv.getMatrix(0, RUNNING_VALUES, validationInput);
 
         //Assume the last measured value of the modeled metric to be zero
         double yk_minus_1 = 0.0;
         Matrix input_row = new Matrix(1, validationInput.length + 1);
 
         for (int j = 0; j < RUNNING_VALUES; j++) {
             for (int i = 1; i < validationInput.length + 1; i++) {
                 input_row.set(0, i, uv.get(j, i - 1));
             }
 
             input_row.set(0, 0, yk_minus_1 * (-1.0));
 
 //	    System.out.print("\u03D5"+"k(at equation 6):");
             input_row.print(2, 4);
 
             //It is equation 7 at paper!
             Matrix yk = input_row.times(x);
 //	    System.out.print("yk (Predicted y at equation 7):");
             yk.print(2, 4);
 
             yk_minus_1 = yk.get(0, 0);
 
             if (0.0 > yk_minus_1) {
                 yk_minus_1 = 0.0;
             }
             result.add(yk_minus_1);
 
         }
         System.out.println(result);
         return result;
     }
 
     private Matrix convertModelToMatrix(ResourceModel model) {
         if (!model.isActive()) {
             return null;
         }
//        Double[] values = (Double[]) model.toList().toArray();
        // this looks horrible... is there another way?
        Object[] modelArray = model.toList().toArray();
        Double[] values = Arrays.copyOf(modelArray, modelArray.length, Double[].class);
         double[] primitiveValues = new double[RUNNING_VALUES];
         for (int i = 0; i < RUNNING_VALUES; i++) {
             if (i >= values.length) {
                 primitiveValues[i] = 0.0;
             } else {
                 primitiveValues[i] = values[i].doubleValue();
             }
         }
         return new Matrix(primitiveValues, primitiveValues.length);
     }
 
     private List<Double> zeroedList(int size) {
         List<Double> result = new ArrayList<>();
         for (int i = 0; i < size; i++) {
             result.add(0.0);
         }
         return result;
     }
 }
