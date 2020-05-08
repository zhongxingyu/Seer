 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.nexusprocessor;
 
 import static org.mockito.Mockito.when;
 import gda.data.nexus.extractor.NexusGroupData;
 import gda.device.DeviceException;
 import gda.device.detector.GDANexusDetectorData;
 import gda.device.detector.NXDetectorData;
 import gda.device.detector.NexusDetector;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mockito;
 import org.nexusformat.NexusFile;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 
 public class NexusDetectorProcessorTest {
 
 	private static final String PROCESSOR_DATA = "processor_data";
 	private static final String PROCESSOR_DATA2 = "processor_data2";
 	private static final String PROCESSOR_DATA3 = "processor_data3";
 	private static final String PROCESSOR_DATA4 = "processor_data4";
 	private static final Double DOUBLE_FROM_DETECTOR = 1.0;
 	private static final Double DOUBLE_FROM_PROCESSOR = DOUBLE_FROM_DETECTOR + 1.0;
 	private static final Double DOUBLE_FROM_PROCESSOR2 = DOUBLE_FROM_DETECTOR + 2.0;
 	private static final Double DOUBLE_FROM_PROCESSOR3 = DOUBLE_FROM_DETECTOR + 3.0;
 	private static final Double DOUBLE_FROM_PROCESSOR4 = DOUBLE_FROM_DETECTOR + 4.0;
 	private static final String DETNAME = "det";
 
 	NexusDetector det;
 	private NexusTreeProviderProcessors processor;
 	private NXDetectorData result;
 	private NXDetectorData detector_data;
 	private DataSetProcessor dsp;
 
 	@Before
 	public void oneTimeSetUp() throws Exception {
 		detector_data = new NXDetectorData();
 		detector_data.getDetTree(DETNAME);
 		int width = 10;
 		int height = 10;
 		byte[] sdsData = new byte[width * height];
 		Arrays.fill(sdsData, (byte) 1);
 		detector_data.addData(DETNAME, new int[] { width, height }, NexusFile.NX_UINT8, sdsData, null, null);
 		detector_data.setDoubleVals(new Double[] { DOUBLE_FROM_DETECTOR });
 
 		// create detector
 		det = Mockito.mock(NexusDetector.class);
 		when(det.readout()).thenReturn(detector_data);
 		when(det.getExtraNames()).thenReturn(new String[] { "max" });
 		when(det.getOutputFormat()).thenReturn(new String[] { "%5.5g" });
 
 		result = new NXDetectorData();
 		result.getDetTree(DETNAME);
 		result.addData(DETNAME, PROCESSOR_DATA, new int[] { 1 }, NexusFile.NX_FLOAT64,
 				new double[] { DOUBLE_FROM_PROCESSOR }, null, null);
 		result.setDoubleVals(new Double[] { DOUBLE_FROM_PROCESSOR });
 
 		dsp = Mockito.mock(DataSetProcessor.class);
 		when(dsp.process(Matchers.any(String.class), Matchers.any(String.class), Matchers.any(AbstractDataset.class)))
 				.thenReturn(result);
 		when(dsp.getExtraNames()).thenReturn(Arrays.asList(new String[] { "max_p" }));
 		when(dsp.getOutputFormat()).thenReturn(Arrays.asList(new String[] { "%5.5g" }));
 		when(dsp.isEnabled()).thenReturn(true);
 
 		// make list of dataset processors
 		List<DataSetProcessor> processors = new Vector<DataSetProcessor>();
 		processors.add(dsp);
 
 		// make list of processors
 		List<NexusTreeProviderProcessor> processorList = new Vector<NexusTreeProviderProcessor>();
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors, null));
 
 		processor = new NexusTreeProviderProcessors();
 		processor.setProcessors(processorList);
 
 	}
 
 	@Test
 	public void testNoMerge() throws DeviceException {
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		NexusDetector det = Mockito.mock(NexusDetector.class);
 		NXDetectorData data = new NXDetectorData();
 		data.getDetTree(DETNAME);
 		when(det.readout()).thenReturn(data);
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(false);
 		Assert.assertEquals(data, ndp.getPosition());
 	}
 
 	/**
 	 * position should be the position returned from the processor only
 	 */
 
 	@Test
 	public void testMergeNoProcessors() throws DeviceException {
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		NexusDetector det = Mockito.mock(NexusDetector.class);
 		NXDetectorData data = new NXDetectorData();
 		data.getDetTree(DETNAME);
 		when(det.readout()).thenReturn(data);
 		ndp.setDetector(det);
 		Assert.assertEquals(data, ndp.getPosition());
 	}
 
 	/**
 	 * position should be the position returned from the processor only
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testNoMergeSimpleProcessor() throws Exception {
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(false);
 		ndp.setProcessor(processor);
 
 		Assert.assertEquals(1, ndp.getExtraNames().length);
 		Assert.assertEquals(1, ndp.getOutputFormat().length);
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
 		Assert.assertEquals(result.getData(DETNAME, PROCESSOR_DATA, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA, "SDS"));
 		Assert.assertEquals(1, position.getDoubleVals().length);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR, position.getDoubleVals()[0]);
 	}
 
 	/**
 	 * position should be the position returned from the processor only
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testMergeSimpleProcessor() throws Exception {
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(true);
 		ndp.setProcessor(processor);
 
 		Assert.assertEquals(2, ndp.getExtraNames().length);
 		Vector<String> extraNames = new Vector<String>();
 		extraNames.addAll(Arrays.asList(det.getExtraNames()));
 		extraNames.addAll(processor.getExtraNames());
 		Assert.assertArrayEquals(extraNames.toArray(new String[] {}), ndp.getExtraNames());
 		Assert.assertEquals(2, ndp.getOutputFormat().length);
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
 		Assert.assertEquals(result.getData(DETNAME, PROCESSOR_DATA, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA, "SDS"));
 		Assert.assertEquals(detector_data.getData(DETNAME, "data", "SDS"), position.getData(DETNAME, "data", "SDS"));
 		Assert.assertEquals(2, position.getDoubleVals().length);
 		Assert.assertEquals(DOUBLE_FROM_DETECTOR, position.getDoubleVals()[0]);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR, position.getDoubleVals()[1]);
 	}
 
 	/**
 	 * position should be the position returned from the processor only
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testMergeTwoProcessors() throws Exception {
 
 		NXDetectorData result2 = new NXDetectorData();
 		result2.getDetTree(DETNAME);
 		result2.addData(DETNAME, PROCESSOR_DATA2, new int[] { 1 }, NexusFile.NX_FLOAT64,
 				new double[] { DOUBLE_FROM_PROCESSOR2 }, null, null);
 		result2.setDoubleVals(new Double[] { DOUBLE_FROM_PROCESSOR2 });
 
 		DataSetProcessor dsp2 = Mockito.mock(DataSetProcessor.class);
 		when(dsp2.process(Matchers.any(String.class), Matchers.any(String.class), Matchers.any(AbstractDataset.class)))
 				.thenReturn(result2);
 		when(dsp2.getExtraNames()).thenReturn(Arrays.asList(new String[] { "max_p2" }));
 		when(dsp2.getOutputFormat()).thenReturn(Arrays.asList(new String[] { "%5.5g" }));
 		when(dsp2.isEnabled()).thenReturn(true);
 
 		NXDetectorData result3 = new NXDetectorData();
 		result3.getDetTree(DETNAME);
 		result3.addData(DETNAME, PROCESSOR_DATA3, new int[] { 1 }, NexusFile.NX_FLOAT64,
 				new double[] { DOUBLE_FROM_PROCESSOR3 }, null, null);
 		result3.setDoubleVals(new Double[] { DOUBLE_FROM_PROCESSOR3 });
 
 		DataSetProcessor dsp3 = Mockito.mock(DataSetProcessor.class);
 		when(dsp3.process(Matchers.any(String.class), Matchers.any(String.class), Matchers.any(AbstractDataset.class)))
 				.thenReturn(result3);
 		when(dsp3.getExtraNames()).thenReturn(Arrays.asList(new String[] { "max_p3" }));
 		when(dsp3.getOutputFormat()).thenReturn(Arrays.asList(new String[] { "%5.5g" }));
 		when(dsp3.isEnabled()).thenReturn(true);
 
 		NXDetectorData result4 = new NXDetectorData();
 		result4.getDetTree(DETNAME);
 		result4.addData(DETNAME, PROCESSOR_DATA4, new int[] { 1 }, NexusFile.NX_FLOAT64,
 				new double[] { DOUBLE_FROM_PROCESSOR4 }, null, null);
 		result4.setDoubleVals(new Double[] { DOUBLE_FROM_PROCESSOR4 });
 
 		DataSetProcessor dsp4 = Mockito.mock(DataSetProcessor.class);
 		when(dsp4.process(Matchers.any(String.class), Matchers.any(String.class), Matchers.any(AbstractDataset.class)))
 				.thenReturn(result4);
 		when(dsp4.getExtraNames()).thenReturn(Arrays.asList(new String[] { "max_p4" }));
 		when(dsp4.getOutputFormat()).thenReturn(Arrays.asList(new String[] { "%5.5g" }));
 		when(dsp4.isEnabled()).thenReturn(true);
 
 		// make list of dataset processors
 		List<DataSetProcessor> processors = new Vector<DataSetProcessor>();
 		processors.add(dsp);
 		processors.add(dsp2);
 
 		List<DataSetProcessor> processors2 = new Vector<DataSetProcessor>();
 		processors2.add(dsp3);
 		processors2.add(dsp4);
 
 		// make list of processors
 		List<NexusTreeProviderProcessor> processorList = new Vector<NexusTreeProviderProcessor>();
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors, null));
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors2, null));
 
 		NexusTreeProviderProcessors processor = new NexusTreeProviderProcessors();
 		processor.setProcessors(processorList);
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(true);
 		ndp.setProcessor(processor);
 
 		Assert.assertEquals(5, ndp.getExtraNames().length);
 		Vector<String> extraNames = new Vector<String>();
 		extraNames.addAll(Arrays.asList(det.getExtraNames()));
 		extraNames.addAll(processor.getExtraNames());
 		Assert.assertArrayEquals(extraNames.toArray(new String[] {}), ndp.getExtraNames());
 		Assert.assertEquals(5, ndp.getOutputFormat().length);
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
 		Assert.assertEquals(result.getData(DETNAME, PROCESSOR_DATA, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA, "SDS"));
 		Assert.assertEquals(result2.getData(DETNAME, PROCESSOR_DATA2, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA2, "SDS"));
 		Assert.assertEquals(result3.getData(DETNAME, PROCESSOR_DATA3, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA3, "SDS"));
 		Assert.assertEquals(result4.getData(DETNAME, PROCESSOR_DATA4, "SDS"),
 				position.getData(DETNAME, PROCESSOR_DATA4, "SDS"));
 		Assert.assertEquals(detector_data.getData(DETNAME, "data", "SDS"), position.getData(DETNAME, "data", "SDS"));
 		Assert.assertEquals(5, position.getDoubleVals().length);
 		Assert.assertEquals(DOUBLE_FROM_DETECTOR, position.getDoubleVals()[0]);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR, position.getDoubleVals()[1]);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR2, position.getDoubleVals()[2]);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR3, position.getDoubleVals()[3]);
 		Assert.assertEquals(DOUBLE_FROM_PROCESSOR4, position.getDoubleVals()[4]);
 	}
 
 	@Test
 	public void testNoMergeFitter() throws Exception {
 
 		NXDetectorData detector_data = new NXDetectorData();
 		detector_data.getDetTree(DETNAME);
 
 		int width = 10;
 		int height = 10;
 
 		double centreX = 0.8 * width;
 		double centreY = 0.2 * width;
 		GaussianCalc g = new GaussianCalc(127 * .9, centreX, centreY, centreX, centreY);
 
 		byte[] sdsData = new byte[width * height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				sdsData[j * width + i] = (byte) Math.min(127, g.getVal(i, j));
 			}
 		}
 		detector_data.addData(DETNAME, new int[] { width, height }, NexusFile.NX_UINT8, sdsData, null, null);
 
 		NexusDetector det = Mockito.mock(NexusDetector.class);
 		when(det.readout()).thenReturn(detector_data);
 
 		DataSetFitter dsp2 = new DataSetFitter();
 		dsp2.afterPropertiesSet();
 
 		// make list of dataset processors
 		List<DataSetProcessor> processors = new Vector<DataSetProcessor>();
 		processors.add(dsp2);
 
 		// make list of processors
 		List<NexusTreeProviderProcessor> processorList = new Vector<NexusTreeProviderProcessor>();
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors, null));
 
 		NexusTreeProviderProcessors processor = new NexusTreeProviderProcessors();
 		processor.setProcessors(processorList);
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(false);
 		ndp.setProcessor(processor);
 
 		Assert.assertEquals(dsp2.getExtraNames().size(), ndp.getExtraNames().length);
 		Assert.assertEquals(dsp2.getOutputFormat().size(), ndp.getOutputFormat().length);
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
 		Assert.assertEquals(8, position.getDoubleVals().length);
 		Double calcCentreX = position.getDoubleVals()[0];
 		Assert.assertEquals(centreX, calcCentreX, centreX/10.);
 		
 		NexusGroupData dataX = position.getData(DETNAME, "data.1_centre", "SDS");
 		double calcCentreX_SDS = ((double[])dataX.getBuffer())[0];
 		Assert.assertEquals(centreX, calcCentreX_SDS, centreX/10.);
 
 		NexusGroupData dataY = position.getData(DETNAME, "data.2_centre", "SDS");
 		double calcCentreY_SDS = ((double[])dataY.getBuffer())[0];
 		Assert.assertEquals(centreY, calcCentreY_SDS, centreY/10.);
 	}
 
 	@Test
 	public void testNoMergeFitterDisabled() throws Exception {
 
 		NXDetectorData detector_data = new NXDetectorData();
 		detector_data.getDetTree(DETNAME);
 
 		int width = 10;
 		int height = 10;
 
 		double centreX = 0.5 * width;
 		GaussianCalc g = new GaussianCalc(127 * .9, centreX, 0.5 * height, centreX, 0.5 * height);
 
 		byte[] sdsData = new byte[width * height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				sdsData[j * width + i] = (byte) Math.min(127, g.getVal(i, j));
 			}
 		}
 		detector_data.addData(DETNAME, new int[] { width, height }, NexusFile.NX_UINT8, sdsData, null, null);
 
 		NexusDetector det = Mockito.mock(NexusDetector.class);
 		when(det.readout()).thenReturn(detector_data);
 
 		DataSetFitter dsp2 = new DataSetFitter();
 		dsp2.afterPropertiesSet();
 		dsp2.setEnable(false);
 
 		// make list of dataset processors
 		List<DataSetProcessor> processors = new Vector<DataSetProcessor>();
 		processors.add(dsp2);
 
 		// make list of processors
 		List<NexusTreeProviderProcessor> processorList = new Vector<NexusTreeProviderProcessor>();
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors, null));
 
 		NexusTreeProviderProcessors processor = new NexusTreeProviderProcessors();
 		processor.setProcessors(processorList);
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(false);
 		ndp.setProcessor(processor);
 
 		Assert.assertEquals(0, ndp.getExtraNames().length);
 		Assert.assertEquals(0, ndp.getOutputFormat().length);
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
 		Assert.assertEquals(0, position.getDoubleVals().length);
 	}
 	
 	
 	@Test
 	public void testDoubleCall() throws Exception {
 
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(false);
 		ndp.setProcessor(processor);
 
 		ndp.getPosition();
 		ndp.getPosition();
 		Mockito.verify(det, Mockito.times(1)).readout();
 	}
 
 	@Test
 	public void testROISUM() throws DeviceException {
 		NexusDetectorProcessor ndp = new NexusDetectorProcessor();
 		ndp.setDetector(det);
 		ndp.setMergeWithDetectorData(true);
 
 		DatasetCreatorFromROI dsroi = new DatasetCreatorFromROI();
 		
 		dsroi.setEnable(true);
 		Slice xSlice = new Slice(2, 6);
 		Slice[] sliceArray = new Slice[]{xSlice, xSlice};
 		dsroi.setSliceArray(sliceArray);
 		
 		DatasetStats stats = new DatasetStats();
 		List<DataSetProcessor> processors = new Vector<DataSetProcessor>();
 		processors.add(stats);
 
 		// make list of processors
 		List<NexusTreeProviderProcessor> processorList = new Vector<NexusTreeProviderProcessor>();
 		processorList.add(new NexusProviderDatasetProcessor(DETNAME, "data", "SDS", processors, dsroi));
 
 		NexusTreeProviderProcessors processor = new NexusTreeProviderProcessors();
 		processor.setProcessors(processorList);
 		ndp.setProcessor(processor);
 		
		
 		GDANexusDetectorData position = (GDANexusDetectorData) ndp.getPosition();
		Assert.assertEquals(2, position.getDoubleVals().length);
 		Double sum = position.getDoubleVals()[1];
 		Double expected=(double) (xSlice.getNumSteps()*xSlice.getNumSteps());
 		Assert.assertEquals(expected, sum, 0.);
 		
 		NexusGroupData dataX = position.getData(DETNAME, "data.total", "SDS");
 		double sum_SDS = ((double[])dataX.getBuffer())[0];
 		Assert.assertEquals(expected, sum_SDS, 0.);
 
 		
 	}
 }
 
 class GaussianCalc {
 	private double height;
 	private double centreX;
 	private double centreY;
 	private double widthX;
 	private double widthY;
 
 	double getVal(double x, double y) {
 		double arg = -1. * (Math.pow((x - centreX) / widthX, 2) + Math.pow((y - centreY) / widthY, 2));
 		return height * Math.exp(arg);
 	}
 
 	public GaussianCalc(double height, double centreX, double centreY, double widthX, double widthY) {
 		super();
 		this.height = height;
 		this.centreX = centreX;
 		this.centreY = centreY;
 		this.widthX = widthX;
 		this.widthY = widthY;
 	}
 
 }
