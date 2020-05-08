 /*-
  * Copyright (c) 2015 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.dawnsci.conversion;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawb.common.services.conversion.IConversionService;
 import org.dawb.common.util.io.FileUtils;
 import org.dawnsci.boofcv.BoofCVImageTransformCreator;
 import org.dawnsci.conversion.converters.AlignImagesConverter.ConversionAlignBean;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.io.Utils;
 
 public class AlignConvertTest {
 
 	private File dir;
 	private File output;
 	private IImageTransform transformer;
 
 	@Before
 	public void before() {
 		dir = new File(System.getProperty("java.io.tmpdir"), "AlignTestFolder");
 		output = new File(dir.getAbsolutePath()+"/Aligned_images");
 		FileUtils.createNewUniqueDir(output);
 	}
 
 	@Test
 	public void testNexusStack() throws Exception {
 		System.out.println("starting align image conversion test from Nxs file");
 		doTestNexus();
 	}
 
 	@Test
 	public void testDir() throws Exception {
 		System.out.println("starting align image conversion test from directory with image files");
 		doTestDir();
 	}
 
 	@After
 	public void after() {
 		FileUtils.recursiveDelete(dir);
 	}
 
 	private void doTestNexus() throws Exception {
 
 		final File image = new File("testfiles/ImageStack.nxs");
 
 		final File nf = new File(dir, "copy_imageStack.nxs");
 		nf.deleteOnExit();
 		FileUtils.copyNio(image, nf);
 
 		IConversionService service = new ConversionServiceImpl();
 
 		final IConversionContext context = service.open(nf.getAbsolutePath());
 
 		String[] filePaths = new String[] { nf.getAbsolutePath() };
 		List<IDataset> data = loadData(filePaths);
 		List<String> names = new ArrayList<String>(data.size());
 		for (int i = 0; i < data.size(); i++) {
 			names.add("image_" + i);
 		}
 		context.setDatasetNames(names);
 		// disable macro
 		context.setEchoMacro(false);
 		context.setOutputPath(output.getAbsolutePath());
 		context.setConversionScheme(ConversionScheme.ALIGNED_FROM_3D);
 
 		ConversionAlignBean bean = new ConversionAlignBean();
 
 		List<IDataset> aligned = getAlignedImages(data);
 		bean.setAligned(aligned);
 		context.setUserObject(bean);
 
 		service.process(context);
 
 		String[] outputFilePaths = new String[aligned.size()];
 		for (int i = 0; i < outputFilePaths.length; i++) {
 			outputFilePaths[i] = output.getAbsolutePath() + "/aligned_" + names.get(i) + ".tiff";
 		}
 
 		// load saved data
 		List<IDataset> alignedSaved = loadData(outputFilePaths);
 
 		int num = 5;
 		int[] alignedShape = aligned.get(num).getShape();
 		int[] alignedSavedShape = alignedSaved.get(5).getShape();
 		if (!Arrays.equals(alignedShape, alignedSavedShape)) {
 			fail("Shape of aligned data in memory and aligned data saved is not " + "the same for dataset with name "
 					+ aligned.get(num).getName());
 		}
 
 		if (aligned.get(num).getDouble(10, 10) != alignedSaved.get(num).getDouble(10, 10)) {
 			fail("Data at slice " + num + " is not the same for aligned dataset in memory and dataset saved.");
 		}
 	}
 
 	private void doTestDir() throws Exception {
 
 		final File sourcedir = new File("testfiles/27099_drifted_png");
 		org.apache.commons.io.FileUtils.copyDirectory(sourcedir, dir);
 
 		IConversionService service = new ConversionServiceImpl();
 
 		final IConversionContext context = service.open(dir.getAbsolutePath());
 		final File output = new File(dir.getAbsolutePath() + "/Aligned_images");
 		FileUtils.createNewUniqueDir(output);
 
 		List<File> files = listFiles(dir, new String[] { "png" }, false);
 		String[] filePaths = new String[files.size()];
 		for (int i = 0; i < filePaths.length; i++) {
 			filePaths[i] = files.get(i).getAbsolutePath();
 		}
 
 		List<IDataset> data = loadData(filePaths);
 		List<String> names = new ArrayList<String>(data.size());
 		for (int i = 0; i < data.size(); i++) {
 			names.add("image_" + i);
 		}
 		context.setDatasetNames(names);
 		context.setFilePaths(filePaths);
 		// disable macro
 		context.setEchoMacro(false);
 		context.setOutputPath(output.getAbsolutePath());
 		context.setConversionScheme(ConversionScheme.ALIGNED_FROM_3D);
 
 		ConversionAlignBean bean = new ConversionAlignBean();
 
 		List<IDataset> aligned = getAlignedImages(data);
 		bean.setAligned(aligned);
 		context.setUserObject(bean);
 
 		service.process(context);
 
 		String[] outputFilePaths = new String[aligned.size()];
 		for (int i = 0; i < outputFilePaths.length; i++) {
 			outputFilePaths[i] = output.getAbsolutePath() + "/aligned_" + names.get(i) + ".tiff";
 		}
 
 		// load saved data
 		List<IDataset> alignedSaved = loadData(outputFilePaths);
 
 		int num = 5;
 		int[] alignedShape = aligned.get(num).getShape();
 		int[] alignedSavedShape = alignedSaved.get(5).getShape();
 		if (!Arrays.equals(alignedShape, alignedSavedShape)) {
 			fail("Shape of aligned data in memory and aligned data saved is not " + "the same for dataset with name "
 					+ aligned.get(num).getName());
 		}
 
 		if (aligned.get(num).getDouble(10, 10) != alignedSaved.get(num).getDouble(10, 10)) {
 			fail("Data at slice " + num + " is not the same for aligned dataset in memory and dataset saved.");
 		}
 	}
 
 	private List<File> listFiles(File dir, String[] extensions, boolean isRecursive) {
 		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(dir, extensions, isRecursive);
 		List<File> listFiles = new ArrayList<File>(files);
 		Collections.sort(listFiles);
 		return listFiles;
 	}
 
 	private List<IDataset> loadData(String[] filePaths) {
 		final List<IDataset> data = new ArrayList<IDataset>();
 		try {
 			Utils.loadData(data, filePaths);
 		} catch (Exception e) {
			fail("Failed to load image stack:" + e);
 		}
 		return data;
 	}
 
 	private List<IDataset> getAlignedImages(List<IDataset> data) {
 		try {
 			if (transformer == null)
 				transformer = BoofCVImageTransformCreator.createTransformService();
 			List<IDataset> shiftedImages = transformer.align(data);
 			return shiftedImages;
 		} catch (Exception e) {
 			fail("An error occured by aligning datasets:" + e);
 		}
 		return null;
 	}
 }
