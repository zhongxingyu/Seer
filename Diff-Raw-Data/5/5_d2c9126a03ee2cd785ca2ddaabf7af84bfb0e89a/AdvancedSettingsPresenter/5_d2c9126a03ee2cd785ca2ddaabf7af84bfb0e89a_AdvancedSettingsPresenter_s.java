 package org.esa.beam.chris.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.esa.beam.chris.operators.DestripingFactorsOp;
 import org.esa.beam.chris.operators.DropoutCorrectionOp;
 import org.esa.beam.dataio.chris.internal.DropoutCorrection;
import org.esa.beam.framework.gpf.annotations.ParameterDefinitionFactory;
 
 import com.bc.ceres.binding.ConverterRegistry;
 import com.bc.ceres.binding.ValueContainer;
 import com.bc.ceres.binding.ValueContainerFactory;
 import com.bc.ceres.binding.converters.EnumConverter;
 
 /**
  * Created by Marco Peters.
  *
  * @author Marco Peters
  * @author Ralf Quast
  * @version $Revision:$ $Date:$
  */
 class AdvancedSettingsPresenter {
 
     static {
         final ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
         final Class<DropoutCorrection.Type> type = DropoutCorrection.Type.class;
         if (converterRegistry.getConverter(type) == null) {
             converterRegistry.setConverter(type, new EnumConverter<DropoutCorrection.Type>(type));
         }
     }
 
     private Map<String, Object> destripingParameterMap;
     private Map<String, Object> dropoutCorrectionParameterMap;
     private ValueContainer destripingValueContainer;
     private ValueContainer dropoutCorrectionValueContainer;
 
     public AdvancedSettingsPresenter() {
         destripingParameterMap = new HashMap<String, Object>();
         dropoutCorrectionParameterMap = new HashMap<String, Object>();
 
         initValueContainers();
     }
 
     private AdvancedSettingsPresenter(Map<String, Object> destripingParameterMap,
                                       Map<String, Object> dropoutCorrectionParameterMap) {
         this.destripingParameterMap = new HashMap<String, Object>(destripingParameterMap);
         this.dropoutCorrectionParameterMap = new HashMap<String, Object>(dropoutCorrectionParameterMap);
 
         initValueContainers();
     }
 
     private void initValueContainers() {
        final ValueContainerFactory factory = new ValueContainerFactory(new ParameterDefinitionFactory());
         destripingValueContainer =
                 factory.createMapBackedValueContainer(DestripingFactorsOp.class, destripingParameterMap);
         dropoutCorrectionValueContainer =
                 factory.createMapBackedValueContainer(DropoutCorrectionOp.class, dropoutCorrectionParameterMap);
     }
 
     public ValueContainer getDestripingValueContainer() {
         return destripingValueContainer;
     }
 
     public Map<String, Object> getDestripingParameterMap() {
         return destripingParameterMap;
     }
 
     public Map<String, Object> getDropoutCorrectionParameterMap() {
         return dropoutCorrectionParameterMap;
     }
 
     public ValueContainer getDropoutCorrectionValueContainer() {
         return dropoutCorrectionValueContainer;
     }
 
     // todo - replace with clone()
     public AdvancedSettingsPresenter createCopy() {
         return new AdvancedSettingsPresenter(destripingParameterMap, dropoutCorrectionParameterMap);
     }
 
 }
