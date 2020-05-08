 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.image;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Image;
 import uk.ac.diamond.scisoft.analysis.dataset.function.MapToShiftedCartesian;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class AlignImages {
 	private static final Logger logger = LoggerFactory.getLogger(AlignImages.class);
 
 	/**
 	 * Align images
 	 * @param images datasets
 	 * @param shifted images
 	 * @param roi
 	 * @param fromStart direction of image alignment: should currently be set to true 
 	 * (the method needs to be re-modified to take into account the flip mode)
 	 * @param preShift
 	 * @return shifts
 	 */
 	public static List<double[]> align(final AbstractDataset[] images, final List<AbstractDataset> shifted, 
 			final RectangularROI roi, final boolean fromStart, double[] preShift) {
 		List<AbstractDataset> list = new ArrayList<AbstractDataset>();
 		Collections.addAll(list, images);
 		if (!fromStart) {
 			Collections.reverse(list);
 		}
 		final AbstractDataset anchor = list.get(0);
 		final int length = list.size();
 		final List<double[]> shift = new ArrayList<double[]>();
		if(preShift!=null){
 			shift.add(preShift);
 		}else{
 			shift.add(new double[] {0., 0.});
 		}
 		shifted.add(anchor);
 		for (int i = 1; i < length; i++) {
 			AbstractDataset image = list.get(i);
 			
 			double[] s = Image.findTranslation2D(anchor, image, roi);
			// We add the preShift to the shift data
			if (preShift != null) {
				s[0] += preShift[0];
				s[1] += preShift[1];
			}
 			shift.add(s);
 			MapToShiftedCartesian map = new MapToShiftedCartesian(s[0], s[1]);
 			shifted.add(map.value(image).get(0));
 		}
 		return shift;
 	}
 
 	/**
 	 * Align images 
 	 * @param files
 	 * @param shifted images
 	 * @param roi
 	 * @param fromStart
 	 * @param preShift
 	 * @return shifts
 	 */
 	public static List<double[]> align(final String[] files, final List<AbstractDataset> shifted, final RectangularROI roi, final boolean fromStart, final double[] preShift) {
 		AbstractDataset[] images = new AbstractDataset[files.length];
 
 		for (int i = 0; i < files.length; i++) {
 			try {
 				images[i] = LoaderFactory.getData(files[i], false, null).getDataset(0);
 			} catch (Exception e) {
 				logger.error("Cannot load file {}", files[i]);
 				throw new IllegalArgumentException("Cannot load file " + files[i]);
 			}
 		}
 
 		return align(images, shifted, roi, fromStart, preShift);
 	}
 }
