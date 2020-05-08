 package gov.usgs.derivative;
 
 import com.google.common.base.Joiner;
 import com.google.common.primitives.Doubles;
 import gov.usgs.derivative.aparapi.AbstractGridKernel;
 import gov.usgs.derivative.aparapi.GridInputTZYXKernel;
 import static gov.usgs.derivative.time.NetCDFDateUtil.toDateTimeUTC;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
 import java.util.Date;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.DataType;
 import ucar.nc2.Attribute;
 import ucar.nc2.Variable;
 import ucar.nc2.constants.CF;
 import ucar.nc2.constants.CDM;
 import ucar.nc2.dataset.CoordinateAxis1D;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
 import ucar.units.ConversionException;
 
 /**
  *
  * @author tkunicki
  */
 public abstract class AbstractTimeStepAveragingVisitor extends DerivativeGridVisitor {
     
     private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTimeStepAveragingVisitor.class);
         
     @Override
     protected String getOutputFilePath() {
         return outputDir;
     }
 
     @Override
     protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
         return Joiner.on(".").join(
                 "derivative",
                 gridDatatypeList.get(0).getVariable().getName());
     }
     
     @Override
     protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
         GridDatatype gridDatatype = gridDatatypeList.get(0);
         
         Variable gridVariable = gridDatatype.getVariable();
         Attribute gridStandardName = gridVariable.findAttribute(CF.STANDARD_NAME);
         Attribute gridUnits = gridVariable.findAttribute(CDM.UNITS);
         
         CoordinateAxis1D thresholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
         
         if (thresholdAxis == null) {
             return new DerivativeValueDescriptor(
                     null, // name
                     null, // standard_name
                     null, // units
                     null, // DataType
                     null, // values
                     gridVariable.getShortName(), // name
                     gridStandardName == null ? null : gridStandardName.getStringValue(), // standard name TODO: ???
                     gridUnits == null ? null : gridUnits.getStringValue(), // units
                     Float.valueOf(-1f),
                     DataType.FLOAT);
         } else {
             Variable thresholdVariable = thresholdAxis.getOriginalVariable() ;
             Attribute thresholdStandardName = thresholdVariable.findAttribute(CF.STANDARD_NAME);
             Attribute thresholdUnits = thresholdVariable.findAttribute(CDM.UNITS);
             return new DerivativeValueDescriptor(
                     thresholdVariable.getShortName(), // name
                     thresholdStandardName == null ? null : thresholdStandardName.getStringValue(), // standard_name
                     thresholdUnits == null ? null : thresholdUnits.getStringValue(),
                     thresholdVariable.getDataType(),
                     Doubles.asList(thresholdAxis.getCoordValues()),
                     gridVariable.getShortName(), // name
                     gridStandardName == null ? null : gridStandardName.getStringValue(), // standard name TODO: ???
                     gridUnits == null ? null : gridUnits.getStringValue(), // units
                     Float.valueOf(-1f),
                     DataType.FLOAT);
         }
     }
     
     @Override
     protected final int getInputGridCount() {
         return 1;
     }
 
     @Override
     protected final boolean isValidInputGridType(GridType gridType) {
         return gridType == GridType.TZYX || gridType == gridType.TYX;
     }
 
     @Override
     protected AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
         GridDatatype gridDatatype = gridDatatypeList.get(0);
         
         GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
                 
         int xCount = GridUtility.getXAxisLength(gridCoordSystem);
         int yCount = GridUtility.getYAxisLength(gridCoordSystem);
         int yxCount = yCount * xCount;
         
         CoordinateAxis1D zAxis = gridCoordSystem.getVerticalAxis();
         int zCount = zAxis == null ? 1 : zAxis.getShape(0);
         
         CoordinateAxis1DTime tAxis = gridCoordSystem.getTimeAxis1D();
         int inputTimeStepCount = tAxis.getShape(0);
         int[] inputTimeStepCountForOutputTimeStep = new int[getTimeStepDescriptor().getOutputTimeStepCount()];
         for (int inputTimeStepIndex = 0; inputTimeStepIndex < inputTimeStepCount; ++inputTimeStepIndex) {
             Date inputTimeStepDate = tAxis.getTimeDate(inputTimeStepIndex);
             int outputTimeStepIndex = getTimeStepDescriptor().getOutputTimeStepIndex(toDateTimeUTC(inputTimeStepDate));
            if (outputTimeStepIndex < 0) {
                continue; // skip missing time steps
            }
             inputTimeStepCountForOutputTimeStep[outputTimeStepIndex]++;
         }
         int tCountMax = 0;
         for(int tCount : inputTimeStepCountForOutputTimeStep) {
             if (tCount > tCountMax) {
                 tCountMax = tCount;
             }
         }
         
         return new DerivativeKernel(
                 tCountMax,
                 zCount,
                 yxCount);
     }
 
     
     private class DerivativeKernel extends GridInputTZYXKernel {
         
         public DerivativeKernel(int tInputCount, int zInputCount, int yxCount) {
             super(getInputGridCount(), tInputCount, zInputCount, yxCount);
         }
         
         @Override
          public void run() {
             int zyxOutputIndex = k_getZYXOutputIndex();
             float value = k_getTZYXInputValue(0);
             if (value == value) {
                 k_zyxOutputValues[zyxOutputIndex] = k_zyxOutputValues[zyxOutputIndex] + (value / (float)k_tInputCountExecuteA[0]);
             }
         }
         
     }
 }
