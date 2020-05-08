 /**
  * 
  */
 package org.opf_labs.arc_cd.cdrdao;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.opf_labs.arc_cd.cdrdao.CdrdaoCommands.Command;
 import org.opf_labs.arc_cd.cdrdao.CdrdaoWrapper.CdrdaoException;
 import org.opf_labs.arc_cd.cdrdao.CdrdaoWrapper.NoCdDeviceException;
 import org.opf_labs.arc_cd.cdrdao.CdrdaoWrapper.NoCdException;
 import org.opf_labs.arc_cd.cdrdao.test.MockCdrProcessFactory;
 import org.opf_labs.arc_cd.cdrdao.test.MockCdrdaoProcessRunner;
 import org.opf_labs.arc_cd.cdrdao.toc.TocItemRecord;
 import org.opf_labs.utils.ProcessRunnerFactory;
 
 /**
  * Test fixture for the CdrdaoCliWrapper class.
  * 
  * @author <a href="mailto:carl@openplanetsfoundation.org">Carl Wilson</a>.</p>
  *         <a href="https://github.com/carlwilson">carlwilson AT github</a>.</p>
  * @version 0.1
  * 
  *          Created 4 Oct 2013:16:43:40
  */
 public class CdrdaoCliWrapperTest {
 	private static final String EMPTY_STRING = "";
 	private static final String NO_SUCH_DEVICE = "/dev/sr1";
 	static final ProcessRunnerFactory MOCK_FACTORY = MockCdrProcessFactory
 			.getInstance();
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#getDevices()}.
 	 */
 	@Test
 	public void testGetCdDeviceNames() {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		List<String> retrievedDevices = wrapper.getDevices();
 		assertTrue("retrievedDevices.size():" + retrievedDevices.size(),
 				retrievedDevices.size() == 1);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#getDefaultDevice()} .
 	 * 
 	 * @throws NoCdDeviceException
 	 */
 	@Test
 	public void testGetDefaultCdDeviceName() throws NoCdDeviceException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		assertTrue(wrapper.getDefaultDevice().equals(
 				wrapper.getDevices().get(0)));
 	}
 
 	/**
 	 * @throws NoCdDeviceException
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	@Test
 	public void testGetDriveInfo() throws NoCdDeviceException,
 			URISyntaxException, IOException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		try (FileInputStream expectedFis = new FileInputStream(
 				AllArcCdrdaoWrapperTests.getCdrdaoDriveInfoOutput())) {
 			assertTrue(IOUtils.contentEquals(wrapper.getDriveInfo(),
 					expectedFis));
 			IOUtils.closeQuietly(expectedFis);
 		}
 	}
 
 	/**
 	 * @throws NoCdDeviceException
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testGetDriveInfoNullString() throws NoCdDeviceException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		wrapper.getDriveInfo(null);
 	}
 
 	/**
 	 * @throws NoCdDeviceException
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetDriveInfoEmptyString() throws NoCdDeviceException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		wrapper.getDriveInfo(EMPTY_STRING);
 	}
 
 	/**
 	 * @throws NoCdDeviceException
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	@Test
 	public void testGetDriveInfoString() throws NoCdDeviceException,
 			IOException, URISyntaxException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		try (FileInputStream expectedFis = new FileInputStream(
 				AllArcCdrdaoWrapperTests.getCdrdaoDriveInfoOutput())) {
 			assertTrue(IOUtils.contentEquals(
 					wrapper.getDriveInfo(wrapper.getDevices().get(0)),
 					expectedFis));
 			IOUtils.closeQuietly(expectedFis);
 		}
 	}
 
 	/**
 	 * @throws NoCdDeviceException
 	 */
 	@Test(expected = NoCdDeviceException.class)
 	public void testGetDriveInfoNoSuchDrive() throws NoCdDeviceException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		wrapper.getDriveInfo(NO_SUCH_DEVICE);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#isDiskLoaded()}.
 	 * 
 	 * @throws URISyntaxException
 	 * @throws FileNotFoundException
 	 * @throws NoCdDeviceException
 	 */
 	@Test
 	public void testIsDiskLoadedTrue() throws FileNotFoundException,
 			URISyntaxException, NoCdDeviceException {
 		MockCdrdaoProcessRunner.COMMAND_OUTPUT_MAP.put(
 				Command.DISC_INFO.value(),
 				AllArcCdrdaoWrapperTests.getCdrdaoDiskLoadedTrueOutput());
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		assertTrue(wrapper.isDiskLoaded());
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#isDiskLoaded(java.lang.String)}
 	 * 
 	 * @throws NoCdDeviceException
 	 * @throws URISyntaxException
 	 * @throws FileNotFoundException
 	 */
 	@Test
 	public void testIsDiskLoadedFalse() throws NoCdDeviceException,
 			FileNotFoundException, URISyntaxException {
 		MockCdrdaoProcessRunner.COMMAND_OUTPUT_MAP.put(
 				Command.DISC_INFO.value(),
 				AllArcCdrdaoWrapperTests.getCdrdaoDiskLoadedFalseOutput());
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		assertFalse(wrapper.isDiskLoaded());
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#isDiskLoaded(java.lang.String)}
 	 * 
 	 * @throws NoCdDeviceException
 	 * @throws URISyntaxException
 	 * @throws FileNotFoundException
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testIsDiskLoadedNullString() throws NoCdDeviceException,
 			FileNotFoundException, URISyntaxException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		wrapper.isDiskLoaded(null);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#isDiskLoaded(java.lang.String)}
 	 * 
 	 * @throws NoCdDeviceException
 	 * @throws URISyntaxException
 	 * @throws FileNotFoundException
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testIsDiskLoadedEmptyString() throws NoCdDeviceException,
 			FileNotFoundException, URISyntaxException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		wrapper.isDiskLoaded(EMPTY_STRING);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#readTocFromDefaultCdDevice()}
 	 * .
 	 * @throws CdrdaoException 
 	 * @throws NoCdException 
 	 * @throws NoCdDeviceException 
 	 * @throws IOException 
 	 */
 	@Test
 	public void testReadTocFromDefaultDevice() throws NoCdDeviceException, NoCdException, CdrdaoException, IOException {
 		CdrdaoCliWrapper wrapper = createTestableInstance(MOCK_FACTORY);
 		try (InputStream stream = wrapper.readTocFromDefaultCdDevice()) {
 			TocItemRecord fromWrapper = TocItemRecord.fromInputStream(stream);
 			System.out.println(fromWrapper.getTracks().size());
 		}
 		/**
 		 * TODO: Implement proper test
 		 */
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#ripCdToBinFromDefaultCdDevice(java.io.File, java.lang.String)}
 	 * .
 	 */
 	@Test
 	public void testRipCdToBinFromDefaultCdDevice() {
 		/**
 		 * TODO: Implement proper test
 		 */
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.opf_labs.arc_cd.cdrdao.CdrdaoCliWrapper#ripCdToBinFromCdDevice(java.lang.String, java.io.File, java.lang.String)}
 	 * .
 	 */
 	@Test
 	public void testRipCdToBinFromCdDevice() {
 		/**
 		 * TODO: Implement proper test
 		 */
 	}
 
 	static CdrdaoCliWrapper createTestableInstance(final ProcessRunnerFactory testMock) {
 		
 		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
 				MockCdrdaoProcessRunner.COMMAND_OUTPUT_MAP.get("scanbus"))))) {
 			reader.mark(10000);
 			String version = Cdrdao.parseCdrdaoVersion(IOUtils.toString(reader));
 			reader.reset();
 			List<String> devices = CdrdaoCliWrapperFactory.parseCdDeviceNames(reader);
 			IOUtils.closeQuietly(reader);
 			CdrdaoExecutable cdrdao = CdrdaoExecutable.getNewInstance(
 					Cdrdao.CDRDAO_COMMAND, version, devices);
 			return new CdrdaoCliWrapper(cdrdao,
 					CdrdaoCommands.getParanoidInstance(cdrdao.getCommand()),
 					testMock);
 		} catch (IOException excep) {
 			throw new IllegalStateException("Couldn't read in scanbus test data.", excep);
 		}
 	}
 
 }
