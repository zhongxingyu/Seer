 package de.fiz.escidoc.factory.cli;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.IOUtils;
 
 import de.escidoc.core.client.exceptions.InternalClientException;
 import de.escidoc.core.common.jibx.Marshaller;
 import de.escidoc.core.resources.om.item.Item;
 import de.fiz.escidoc.factory.EscidocObjects;
 
 public final class ItemGenerator extends Questionary implements Generator {
 	static final String PROPERTY_RANDOM_NUM_FILES = "generator.item.random.num";
 	static final String PROPERTY_RANDOM_DATA = "generator.item.random.data";
 	static final String PROPERTY_RANDOM_SIZE_FILES = "generator.item.random.size";
 	static final String PROPERTY_INPUT_DIRECTORY = "generator.item.input.directory";
 	static final String PROPERTY_CONTEXT_ID = "generator.item.context.id";
 	static final String PROPERTY_CONTENTMODEL_ID = "generator.item.contentmodel.id";
 	static final String PROPERTY_RESULT_PATH = "generator.item.result.path";
 
 	private final Properties properties;
 	private final Marshaller<Item> itemMarshaller = Marshaller.getMarshaller(Item.class);
 
 	ItemGenerator(final Properties properties) {
 		super(new BufferedReader(new InputStreamReader(System.in)), System.out);
 		this.properties = properties;
 	}
 
 	public void interactive() {
 		try {
 			this.questionResultFile();
 			this.questionRandomData();
 			this.questionContextId();
 			this.questionContentModelId();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void questionContentModelId() throws Exception {
 		String contentModelId;
 		do {
 			contentModelId = poseQuestion(String.class, "", "What's the Content Model ID to use for the items?");
 		} while (contentModelId.length() == 0);
 		properties.setProperty(PROPERTY_CONTENTMODEL_ID, contentModelId);
 	}
 
 	private void questionResultFile() throws Exception {
 		String resultFile;
 		do {
 			resultFile = poseQuestion(String.class, properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY)
 					+ "/testdaten-i.csv", "What's the path to the result file [default="
 					+ properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY) + "/testdaten-i.csv] ?");
 		} while (resultFile.length() == 0);
 		properties.setProperty(PROPERTY_RESULT_PATH, resultFile);
 	}
 
 	private void questionContextId() throws Exception {
 		String contextId;
 		do {
 			contextId = poseQuestion(String.class, "", "What's the Context ID to use for the items?");
 		} while (contextId.length() == 0);
 		properties.setProperty(PROPERTY_CONTEXT_ID, contextId);
 	}
 
 	private void questionRandomData() throws Exception {
 		final boolean randomData = this.poseQuestion(Boolean.class, true, "Do you want to use random data [default=yes] ?");
 		this.properties.setProperty(PROPERTY_RANDOM_DATA, String.valueOf(randomData));
 		if (randomData) {
 			final int numObjects = this.poseQuestion(Integer.class, 10, "How many objects should be created [default=10] ?");
 			this.properties.setProperty(PROPERTY_RANDOM_NUM_FILES, String.valueOf(numObjects));
 			final long size = this.poseQuestion(Long.class, 1000L, "What size in kilobytes should the random data have [default=10] ?");
 			this.properties.setProperty(PROPERTY_RANDOM_SIZE_FILES, String.valueOf(size * 1024));
 		} else {
 			File dir;
 			do {
 				dir = this.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir")), "What's the location of the test files [default="
 						+ System.getProperty("java.io.tmpdir") + "] ?");
 			} while (!dir.exists() && !dir.canRead());
 			this.properties.setProperty(PROPERTY_INPUT_DIRECTORY, dir.getAbsolutePath());
 
 		}
 	}
 
 	public List<File> generateFiles() throws IOException, ParserConfigurationException, InternalClientException {
 		final List<File> files = new ArrayList<File>();
 		final boolean randomData = Boolean.parseBoolean(properties.getProperty(PROPERTY_RANDOM_DATA));
 		final File targetDirectory = new File(properties.getProperty(CommandlineInterface.PROPERTY_TARGET_DIRECTORY));
		final long size = Long.parseLong(properties.getProperty(PROPERTY_RANDOM_SIZE_FILES));
 		final String contextId = properties.getProperty(PROPERTY_CONTEXT_ID);
 		final String contentModelId = properties.getProperty(PROPERTY_CONTENTMODEL_ID);
 		if (randomData) {
 			final int numFiles = Integer.parseInt(properties.getProperty(PROPERTY_RANDOM_NUM_FILES));
 			int currentPercent = 0;
 			int oldPercent = 0;
 			for (int i = 0; i < numFiles; i++) {
 				Item item = EscidocObjects.createItem(contextId, contentModelId, Arrays.asList(EscidocObjects.createComponentFromRandomData(targetDirectory, size)));
 				String xml = itemMarshaller.marshalDocument(item);
 				FileOutputStream out = null;
 				try {
 					File outFile = File.createTempFile("item-", ".xml", targetDirectory);
 					out = new FileOutputStream(outFile);
 					files.add(outFile);
 					IOUtils.write(xml, out);
 					oldPercent = currentPercent;
 					currentPercent = (int) ((double) i / (double) numFiles * 100d);
 					if (currentPercent > oldPercent) {
 						ProgressBar.printProgressBar(currentPercent);
 					}
 				} finally {
 					out.close();
 				}
 			}
 			final File result = new File(properties.getProperty(PROPERTY_RESULT_PATH));
 			FileOutputStream out = null;
 			try {
 				out = new FileOutputStream(result, false);
 				for (File f : files) {
 					out.write(new String("testdaten/daten/" + f.getName()  + "," + f.getName() + ",text/xml\n").getBytes("UTF-8"));
 					out.flush();
 				}
 			} finally {
 				IOUtils.closeQuietly(out);
 			}
 		}
 		ProgressBar.printProgressBar(100, true);
 		return files;
 	}
 }
