 package org.dawb.hdf5.model.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 
 import org.dawb.hdf5.Hdf5TestUtils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.HierarchicalDataUtils;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.model.IHierarchicalDataFileModel;
 import org.junit.Test;
 
 public class HierarchicalDataFileModelTest {
 
 	private static final IHierarchicalDataFileGetReader get_i05_4859_Reader = new IHierarchicalDataFileGetReader() {
 
 		@Override
 		public IHierarchicalDataFile getReader() throws Exception {
 			String absolutePath = Hdf5TestUtils
 					.getAbsolutePath("test/org/dawb/hdf5/model/internal/i05-4859.nxs");
 			return HierarchicalDataFactory.getReader(absolutePath);
 		}
 	};
 
 	/**
 	 * Used to debug the file structure to help write the other tests
 	 */
 	@Test
 	public void printDataContents() throws Exception {
 		try (IHierarchicalDataFile reader = get_i05_4859_Reader.getReader()) {
 			Map<String, Object> attributeValues = reader.getAttributeValues();
 			for (Entry<String, Object> entry : attributeValues.entrySet()) {
 				String ln = entry.getKey() + "=" + entry.getValue() + "=="
 						+ HierarchicalDataUtils.extractScalar(entry.getValue());
 				System.out.println(ln);
 
 			}
 
 			Group g = reader.getRoot();
 			printGroup("", g);
 		}
 
 	}
 
 	private void printGroup(String path, Group g) throws Exception {
 		List<?> members = g.getMemberList();
 
 		int n = members.size();
 		HObject obj = null;
 		for (int i = 0; i < n; i++) {
 			obj = (HObject) members.get(i);
 			String childPath = path + "/" + obj.toString();
 			System.out.print(childPath);
 			if (obj instanceof Dataset) {
 				Dataset dataset = (Dataset) obj;
 				Object value = dataset.read();
 				System.out.print("=" + value);
 				System.out.print("=="
 						+ HierarchicalDataUtils.extractScalar(value));
 			}
 			System.out.println();
 
 			if (obj instanceof Group) {
 				printGroup(childPath, (Group) obj);
 			}
 		}
 	}
 
 	@Test
 	public void testInvalidFile() {
 		IHierarchicalDataFileGetReader getInvalidFileReader = new IHierarchicalDataFileGetReader() {
 
 			@Override
 			public IHierarchicalDataFile getReader() throws Exception {
 				String absolutePath = Hdf5TestUtils
 						.getAbsolutePath("test/org/dawb/hdf5/model/internal/non-existent-file.nxs");
 				return HierarchicalDataFactory.getReader(absolutePath);
 			}
 		};
 		IHierarchicalDataFileModel invalidFileModel = new HierarchicalDataFileModel(
 				getInvalidFileReader);
 		assertEquals(null, invalidFileModel.getPath("/@HDF5_Version"));
 	}
 
 	@Test
 	public void testGetPath() {
 		IHierarchicalDataFileModel model = new HierarchicalDataFileModel(
 				get_i05_4859_Reader);
 		checkGetPathOnAllCases(model);
 	}
 
 	@Test
 	public void testHasPath() {
 		IHierarchicalDataFileModel model = new HierarchicalDataFileModel(
 				get_i05_4859_Reader);
 		checkHasPathOnAllCases(model);
 	}
 
 	private void checkGetPathOnAllCases(IHierarchicalDataFileModel model) {
		// root attrib
 		assertEquals("1.8.7", model.getPath("/@HDF5_Version"));
 		// path with scalar dataset
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
		// attrib scalar dataset
 		assertEquals("Hz",
 				model.getPath("/entry1/instrument/analyser/cps@units"));
 		// group
 		assertEquals(null, model.getPath("/entry1"));
 		// attrib on group
 		assertEquals("NXentry", model.getPath("/entry1@NX_class"));
 		// path with non 1-size dataset
 		assertEquals(null,
 				model.getPath("/entry1/instrument/analyser/energies"));
 		// attrib on non 1-size dataset, string
 		assertEquals("3",
 				model.getPath("/entry1/instrument/analyser/energies@axis"));
 		// attrib on non 1-size dataset, integer
 		assertEquals(1,
 				model.getPath("/entry1/instrument/analyser/energies@primary"));
 		// non-existent root attrib
 		assertEquals(null, model.getPath("/@NonExistent"));
 		// non-existent attrib on valid path
 		assertEquals(null,
 				model.getPath("/entry1/instrument/analyser/cps@NonExistent"));
 		// non-existent path
 		assertEquals(null,
 				model.getPath("/entry1/instrument/analyser/NonExistent"));
 		// non-existent attrib on group
 		assertEquals(null, model.getPath("/entry1@NonExistent"));
 	}
 
 	private void checkHasPathOnAllCases(IHierarchicalDataFileModel model) {
 		// root attrib
 		assertTrue(model.hasPath("/@HDF5_Version"));
 		// path with scalar dataset
 		assertTrue(model.hasPath("/entry1/instrument/analyser/cps"));
 		// attrib scalar dataset
 		assertTrue(model.hasPath("/entry1/instrument/analyser/cps@units"));
 		// group
 		assertTrue(model.hasPath("/entry1"));
 		// attrib on group
 		assertTrue(model.hasPath("/entry1@NX_class"));
 		// path with non 1-size dataset
 		assertTrue(model.hasPath("/entry1/instrument/analyser/energies"));
 		// attrib on non 1-size dataset, string
 		assertTrue(model.hasPath("/entry1/instrument/analyser/energies@axis"));
 		// attrib on non 1-size dataset, integer
 		assertTrue(model
 				.hasPath("/entry1/instrument/analyser/energies@primary"));
 		// non-existent root attrib
 		assertTrue(!model.hasPath("/@NonExistent"));
 		// non-existent attrib on valid path
 		assertTrue(!model
 				.hasPath("/entry1/instrument/analyser/cps@NonExistent"));
 		// non-existent path
 		assertTrue(!model.hasPath("/entry1/instrument/analyser/NonExistent"));
 		// non-existent attrib on group
 		assertTrue(!model.hasPath("/entry1@NonExistent"));
 	}
 
 	/**
 	 * This test makes sure that re-reading the same attribute does not cause a
 	 * new file access.
 	 * <p>
 	 * This depends on the overall contract of the Model not being violated in
 	 * the code, that is that the reader is not left open between calls to the
 	 * model.
 	 * <p>
 	 * Additionally, this test may fail if more data is cached on the initial
 	 * read of the Nexus file. For example, at one point each attribute was
 	 * fetched individually from the underlying reader. However on discovering
 	 * that reading any one attribute loaded all the attributes on that node a
 	 * new method was added
 	 * {@link IHierarchicalDataFile#getAttributeValues(String)} to get all the
 	 * attributes in one go.
 	 */
 	@Test
 	public void hitsCache() {
 		final int[] count = new int[] { 0 };
 		IHierarchicalDataFileGetReader getCountingReader = new IHierarchicalDataFileGetReader() {
 			@Override
 			public IHierarchicalDataFile getReader() throws Exception {
 				count[0] += 1;
 				return get_i05_4859_Reader.getReader();
 			}
 		};
 		IHierarchicalDataFileModel model = new HierarchicalDataFileModel(
 				getCountingReader);
 
 		// Make sure re-reading attribute (either has or value) does not
 		// increase file access count
 		assertTrue(model.hasPath("/@HDF5_Version"));
 		assertEquals(1, count[0]);
 		assertTrue(model.hasPath("/@HDF5_Version"));
 		assertEquals(1, count[0]);
 		assertEquals("1.8.7", model.getPath("/@HDF5_Version"));
 		assertEquals(1, count[0]);
 
 		// Make sure reading additional attributes on same node does not
 		// increase file access count
 		assertTrue(model.hasPath("/@NeXus_version"));
 		assertEquals(1, count[0]);
 		assertEquals("4.3.1", model.getPath("/@NeXus_version"));
 		assertEquals(1, count[0]);
 
 		// Make sure reading additional non-existent attributes on same
 		// node does not increase file access count
 		assertTrue(!model.hasPath("/@NonExistentAttribute"));
 		assertEquals(1, count[0]);
 		assertEquals(null, model.getPath("/@NonExistentAttribute"));
 		assertEquals(1, count[0]);
 
 		// Make sure re-reading dataset (either has or value) does not
 		// increase file access count
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 		assertEquals(2, count[0]);
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 		assertEquals(2, count[0]);
 		assertTrue(model.hasPath("/entry1/instrument/analyser/cps"));
 		assertEquals(2, count[0]);
 
 		// Make sure oscillating between attribs and dataset on same
 		// path does not increase counts
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 		assertEquals(2, count[0]);
 		assertEquals("Hz",
 				model.getPath("/entry1/instrument/analyser/cps@units"));
 		assertEquals(3, count[0]);
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 		assertEquals(3, count[0]);
 		assertEquals("Hz",
 				model.getPath("/entry1/instrument/analyser/cps@units"));
 		assertEquals(3, count[0]);
 
 		// Make sure reading additional non-existent dataset on same
 		// node does not increase file access count
 		assertTrue(!model.hasPath("/Non/Existent/Path"));
 		assertEquals(4, count[0]);
 		assertEquals(null, model.getPath("/Non/Existent/Path"));
 		assertEquals(4, count[0]);
 
 		// Make sure reading additional non-existent path's attributes
 		// on same node does not increase file access count
 		assertTrue(!model.hasPath("/Another/Non/Existent/Path@Attrib1"));
 		assertEquals(5, count[0]);
 		assertEquals(null, model.getPath("/Another/Non/Existent/Path@Attrib1"));
 		assertEquals(5, count[0]);
 		assertTrue(!model.hasPath("/Another/Non/Existent/Path@Attrib2"));
 		assertEquals(5, count[0]);
 		assertEquals(null, model.getPath("/Another/Non/Existent/Path@Attrib2"));
 		assertEquals(5, count[0]);
 
 		// Re-use check of all cases to make sure re-reading each/all of them
 		// does not increase access count
 		checkGetPathOnAllCases(model);
 		checkHasPathOnAllCases(model);
 		final int expectedCount = count[0];
 		checkGetPathOnAllCases(model);
 		checkHasPathOnAllCases(model);
 		assertEquals(expectedCount, count[0]);
 	}
 
 	@Test
 	public void testReadAttribFirst() {
 		IHierarchicalDataFileModel model = new HierarchicalDataFileModel(
 				get_i05_4859_Reader);
 		assertEquals("Hz",
 				model.getPath("/entry1/instrument/analyser/cps@units"));
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 	}
 
 	@Test
 	public void testReadDatasetFirst() {
 		IHierarchicalDataFileModel model = new HierarchicalDataFileModel(
 				get_i05_4859_Reader);
 		assertEquals(1754416.0,
 				model.getPath("/entry1/instrument/analyser/cps"));
 		assertEquals("Hz",
 				model.getPath("/entry1/instrument/analyser/cps@units"));
 	}
 }
