 /*
  * Copyright (C) 2013 Brockmann Consult GmbH (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see http://www.gnu.org/licenses/
  */
 
 package org.esa.beam.occci.merging;
 
 
 import org.esa.beam.binning.Vector;
 import org.esa.beam.binning.WritableVector;
 import org.esa.beam.binning.support.VectorImpl;
 import org.junit.Test;
 
 import java.util.Arrays;
 
 import static java.lang.Float.NaN;
 import static org.junit.Assert.*;
 
 public class SensorMergingTest {
 
     @Test
     public void testCreation() throws Exception {
 
         String[] spatial = {"rrs_2", "rrs_4", "sensor"};
         String[] temporal = {
                 "rrs_2_meris", "rrs_2_modis", "rrs_2_seawifs",
                 "rrs_2_meris_biasmap", "rrs_2_modis_biasmap", "rrs_2_seawifs_biasmap",
 
                 "rrs_4_meris", "rrs_4_modis", "rrs_4_seawifs",
                 "rrs_4_meris_biasmap", "rrs_4_modis_biasmap", "rrs_4_seawifs_biasmap"
         };
         String[] outputMerging = {"rrs_2", "rrs_4", "sensor_0", "sensor_1", "sensor_2"};
         String[] outputBiasCorr = {
                 "rrs_2_meris",
                 "rrs_2_modis",
                 "rrs_2_seawifs",
                 "rrs_4_meris",
                 "rrs_4_modis",
                 "rrs_4_seawifs"
         };
 
         SensorMerging sensorMerging = new SensorMerging(null, SensorMerging.Mode.MERGING, "rrs_2", "rrs_4");
         assertArrayEquals(spatial, sensorMerging.getSpatialFeatureNames());
         assertArrayEquals(temporal, sensorMerging.getTemporalFeatureNames());
         assertArrayEquals(outputMerging, sensorMerging.getOutputFeatureNames());
 
         sensorMerging = new SensorMerging(null, SensorMerging.Mode.AGGREGATION, "rrs_2", "rrs_4");
         assertArrayEquals(spatial, sensorMerging.getSpatialFeatureNames());
         assertArrayEquals(temporal, sensorMerging.getTemporalFeatureNames());
         assertArrayEquals(temporal, sensorMerging.getOutputFeatureNames());
 
         sensorMerging = new SensorMerging(null, SensorMerging.Mode.BIAS_CORRECTION, "rrs_2", "rrs_4");
         assertArrayEquals(spatial, sensorMerging.getSpatialFeatureNames());
         assertArrayEquals(temporal, sensorMerging.getTemporalFeatureNames());
         assertArrayEquals(outputBiasCorr, sensorMerging.getOutputFeatureNames());
     }
 
     @Test
     public void testTemporalAggregation() throws Exception {
         SensorMerging sensorMerging = new SensorMerging(null, SensorMerging.Mode.AGGREGATION, "rrs_2", "rrs_4");
         float[] temporalElems = new float[sensorMerging.getTemporalFeatureNames().length];
         final WritableVector temporalVector = new VectorImpl(temporalElems);
         sensorMerging.initTemporal(null, temporalVector);
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{1, 2, 0}), 1, temporalVector);
         assertArrayEquals(new float[]{1, NaN, NaN, NaN, NaN, NaN, 2, NaN, NaN, NaN, NaN, NaN}, temporalElems, 1e-6f);
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{3, 4, 1}), 1, temporalVector);
         assertArrayEquals(new float[]{1, 3, NaN, NaN, NaN, NaN, 2, 4, NaN, NaN, NaN, NaN}, temporalElems, 1e-6f);
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{5, 6, 2}), 1, temporalVector);
         assertArrayEquals(new float[]{1, 3, 5, NaN, NaN, NaN, 2, 4, 6, NaN, NaN, NaN}, temporalElems, 1e-6f);
 
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{11, 12, 10}), 1, temporalVector);
         assertArrayEquals(new float[]{1, 3, 5, 11, NaN, NaN, 2, 4, 6, 12, NaN, NaN}, temporalElems, 1e-6f);
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{13, 14, 11}), 1, temporalVector);
         assertArrayEquals(new float[]{1, 3, 5, 11, 13, NaN, 2, 4, 6, 12, 14, NaN}, temporalElems, 1e-6f);
 
         sensorMerging.aggregateTemporal(null, new VectorImpl(new float[]{15, 16, 12}), 1, temporalVector);
         assertArrayEquals(new float[]{1, 3, 5, 11, 13, 15, 2, 4, 6, 12, 14, 16}, temporalElems, 1e-6f);
 
         float[] outputElems = new float[sensorMerging.getOutputFeatureNames().length];
         final WritableVector outputVector = new VectorImpl(outputElems);
         sensorMerging.computeOutput(temporalVector, outputVector);
         assertArrayEquals(new float[]{1, 3, 5, 11, 13, 15, 2, 4, 6, 12, 14, 16}, outputElems, 1e-6f);
     }

    @Test
    public void testCorrectBias() throws Exception {
        float[] correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, 2, 3, 4, 5, 6}), 0);
        assertArrayEquals(new float[]{1f * 6 / 4, 2f * 6 / 5, 3f}, correctedRrs, 1e-6f);

        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{NaN, 2, 3, 4, 5, 6}), 0);
        assertArrayEquals(new float[]{NaN, 2f * 6 / 5, 3f}, correctedRrs, 1e-6f);
        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, NaN, 3, 4, 5, 6}), 0);
        assertArrayEquals(new float[]{1f * 6 / 4, NaN, 3f}, correctedRrs, 1e-6f);
        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, 2, NaN, 4, 5, 6}), 0);
        assertArrayEquals(new float[]{1f * 6 / 4, 2f * 6 / 5, NaN}, correctedRrs, 1e-6f);


        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, 2, 3, NaN, 5, 6}), 0);
        assertArrayEquals(new float[]{NaN, 2f * 6 / 5, 3}, correctedRrs, 1e-6f);
        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, 2, 3, 4, NaN, 6}), 0);
        assertArrayEquals(new float[]{1f * 6 / 4, NaN, 3f}, correctedRrs, 1e-6f);
        correctedRrs = SensorMerging.correctBias(new VectorImpl(new float[]{1, 2, 3, 4, 5, NaN}), 0);
        assertArrayEquals(new float[]{NaN, NaN, 3}, correctedRrs, 1e-6f);
    }
 }
