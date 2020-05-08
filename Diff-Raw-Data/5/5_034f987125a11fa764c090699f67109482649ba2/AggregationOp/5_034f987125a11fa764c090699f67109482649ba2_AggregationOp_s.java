 package org.esa.cci.lc.aggregation;
 
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
 import org.esa.beam.framework.gpf.annotations.Parameter;
 import org.esa.beam.framework.gpf.annotations.SourceProduct;
 
 /**
  * The LC map and conditions products are delivered in a full spatial resolution version, both as global
  * files and as regional subsets, in a Plate Carree projection. However, climate models may need products
  * associated with a coarser spatial resolution, over specific areas (e.g. for regional climate models)
  * and/or in another projection. This Operator implementation provides this functionality.
  *
  * @author Marco Peters
  */
 @OperatorMetadata(
         alias = "Aggregate",
         version = "0.1",
         authors = "Marco Peters",
         copyright = "(c) 2012 by Brockmann Consult",
         description = "Allows to re-project, aggregate and subset LC map and conditions products.")
 public class AggregationOp extends Operator {
 
     @SourceProduct(description = "LC CCI map or conditions product")
     Product source;
 
     @Parameter(description = "Defines the projection method for the target product.",
                valueSet = {"GAUSSIAN_GRID", "GEOGRAPHIC_LAT_LON", "ROTATED_LAT_LON"}, defaultValue = "GAUSSIAN_GRID")
     ProjectionMethod projectionMethod;
 
     @Parameter(description = "Size of a pixel in X-direction in degree.", defaultValue = "0.05", unit = "°")
     double pixelSizeX;
     @Parameter(description = "Size of a pixel in Y-direction in degree.", defaultValue = "0.05", unit = "°")
     double pixelSizeY;
 
     @Parameter(description = "The western longitude.", interval = "[-180,180]", defaultValue = "-15.0", unit = "°")
     double westBound;
     @Parameter(description = "The northern latitude.", interval = "[-90,90]", defaultValue = "75.0", unit = "°")
     double northBound;
     @Parameter(description = "The eastern longitude.", interval = "[-180,180]", defaultValue = "30.0", unit = "°")
     double eastBound;
     @Parameter(description = "The southern latitude.", interval = "[-90,90]", defaultValue = "35.0", unit = "°")
     double southBound;
 
     @Parameter(description = "Whether or not to add majority classes and the fractional area to the output.")
     boolean outputMajorityClasses;
     // todo (mp, 26.07.12) - set value range if max. classes is fixed or add validator which uses input to define maximum.
    @Parameter(description = "Whether or not to add majority classes to the output.", defaultValue = "5")
     int numberOfMajorityClasses;
 
     @Parameter(description = "Whether or not to add PFT classes to the output.")
     boolean outputPFTClasses;
     // todo (mp, 26.07.12) - set value range if max. classes is fixed or add validator which uses input to define maximum.
     // todo (mp, 26.07.12) - meaningful default value?
    @Parameter(description = "Whether or not to add majority classes to the output.", defaultValue = "3")
     int numberOfPFTClasses;
 
     @Override
     public void initialize() throws OperatorException {
     }
 
     public enum ProjectionMethod {
         GAUSSIAN_GRID,
         GEOGRAPHIC_LAT_LON,
         ROTATED_LAT_LON
     }
 
     /**
      * The Service Provider Interface (SPI) for the operator.
      * It provides operator meta-data and is a factory for new operator instances.
      */
     public static class Spi extends OperatorSpi {
 
         public Spi() {
             super(AggregationOp.class);
         }
     }
 
 }
