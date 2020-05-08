 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.PriorityQueue;
 
 public class StructureBuilder {
 	private String folderPath;
 	private String outputFilePath;
 
 	public StructureBuilder(String folderPath, String outputFilePath) {
 		this.folderPath = folderPath;
 		this.outputFilePath = outputFilePath;
 	}
 
 	public void build() {
 		PriorityQueue<MenuItem> itemQueue = new PriorityQueue<StructureBuilder.MenuItem>();
 
 		System.out.println("Parsing menu files ...");
 		File folder = new File(folderPath);
 		if (!folder.exists()) {
 			System.out.println("Could not find path '"
 					+ folder.getAbsolutePath() + "'. Abort.");
 			return;
 		}
 		for (File f : folder.listFiles()) {
 			if (f.isDirectory()) {
 				buildFolderItemQueue(f, itemQueue);
 			}
 		}
 
 		System.out.println("Writing menu structure file ...");
 		try {
 			OutputStreamWriter outputWriter = new OutputStreamWriter(
 					new FileOutputStream(outputFilePath));
 			try {
 				writeMenuStructure(itemQueue, outputWriter);
 			} finally {
 				outputWriter.close();
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not write output file '" + outputFilePath
 					+ '.');
 			return;
 		} catch (IOException e) {
 			System.out.println("Could not write output file '" + outputFilePath
 					+ '.');
 			return;
 		}
 
 		System.out.println("Done.");
 	}
 
 	private void buildFolderItemQueue(File folder,
 			PriorityQueue<MenuItem> itemQueue) {
 
 		File menuFile = new File(folder.getAbsolutePath() + "/.menu");
 		MenuItem menuItem = null;
 		try {
 			BufferedReader menuFileReader = new BufferedReader(
 					new InputStreamReader(new FileInputStream(menuFile)));
 			try {
 				// Read item name
 				String itemName = menuFileReader.readLine();
 
 				// Read and parse oder
 				String itemOrderStr = menuFileReader.readLine();
 				int itemOrder;
 				try {
 					itemOrder = Integer.parseInt(itemOrderStr);
 				} catch (NumberFormatException e) {
 					System.out.println("Could not parse order of menu file "
 							+ menuFile.getAbsolutePath() + "'. Set to 9999.");
 					itemOrder = 9999;
 				}
 
 				// Create menu item in queue
 				menuItem = new MenuItem(itemName, itemOrder);
 				itemQueue.add(menuItem);
 			} finally {
 				menuFileReader.close();
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not find menu file in '"
 					+ folder.getAbsolutePath() + "'. Skipping path.");
 			return;
 		} catch (IOException e) {
 			System.out.println("Could not parse content of menu file '"
 					+ menuFile.getAbsolutePath() + "'. Skipping path.");
 			return;
 		}
 
 		// Check subfolders
 		for (File f : folder.listFiles()) {
 			if (f.isDirectory()) {
 				buildFolderItemQueue(f, menuItem.getChildItemsQueue());
 			}
 		}
 	}
 
 	/**
 	 * Write ordered menu items in JSON format.
 	 */
 	private void writeMenuStructure(PriorityQueue<MenuItem> itemQueue,
 			OutputStreamWriter outputWriter) throws IOException {
 
 		outputWriter.write("[");
		for (MenuItem curItem : itemQueue) {
 			outputWriter.write("{");
 			outputWriter.write("name:'" + curItem.getName() + "',");
 			outputWriter.write("items:");
 			writeMenuStructure(curItem.getChildItemsQueue(), outputWriter);
 			outputWriter.write("}");
 		}
 		outputWriter.write("]");
 	}
 
 	/**
 	 * Website menu item
 	 * 
 	 */
 	class MenuItem implements Comparable<MenuItem> {
 		private final String name;
 		private final int order;
 		private final PriorityQueue<MenuItem> childs;
 
 		public MenuItem(String name, int order) {
 			this.name = name;
 			this.order = order;
 			this.childs = new PriorityQueue<StructureBuilder.MenuItem>();
 
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public int getOrder() {
 			return order;
 		}
 
 		public PriorityQueue<MenuItem> getChildItemsQueue() {
 			return childs;
 		}
 
 		@Override
 		public int compareTo(MenuItem o) {
 			return this.order - o.order;
 		}
 	}
 }
