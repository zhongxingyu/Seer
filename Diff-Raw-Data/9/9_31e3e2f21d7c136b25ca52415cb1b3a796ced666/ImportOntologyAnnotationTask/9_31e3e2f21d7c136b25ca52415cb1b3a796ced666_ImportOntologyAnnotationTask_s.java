 package cytoscape.data.servers.ui;
 
 import java.io.IOException;
 
 import cytoscape.data.readers.TextTableReader;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 
 public class ImportOntologyAnnotationTask implements Task {
 	private TextTableReader reader;
 
 	private String ontology;
 	private String source;
 
 	private TaskMonitor taskMonitor;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param file
 	 *            File.
 	 * @param fileType
 	 *            FileType, e.g. Cytoscape.FILE_SIF or Cytoscape.FILE_GML.
 	 */
 
 	public ImportOntologyAnnotationTask(TextTableReader reader, String ontology,
 			String source) {
 		this.reader = reader;
 		this.ontology = ontology;
 		this.source = source;
 	}
 
 	/**
 	 * Executes Task.
 	 */
 	public void run() {

		System.out.println("### Running table reader task...");

		taskMonitor.setStatus("Importing annotation data...");
 		taskMonitor.setPercentCompleted(-1);
 
 		try {
 			reader.readTable();
 			taskMonitor.setPercentCompleted(100);
 		} catch (IOException e) {
 			e.printStackTrace();
 			taskMonitor.setException(e, "Unable to import annotation data.");
 		}
 
 		informUserOfAnnotationStats();
 	}
 
 	/**
 	 * Inform User of Network Stats.
 	 */
 	private void informUserOfAnnotationStats() {
 		StringBuffer sb = new StringBuffer();
 
 		// Give the user some confirmation
 		sb.append("Succesfully loaded annotation data for " + ontology);
 		sb.append(" from: \n" + source + "\n");
 		sb.append("\n\nAnnotation data source contains ");
 
 		taskMonitor.setStatus(sb.toString());
 	}
 
 	/**
 	 * Halts the Task: Not Currently Implemented.
 	 */
 	public void halt() {
 		// Task can not currently be halted.
 	}
 
 	/**
 	 * Sets the Task Monitor.
 	 * 
 	 * @param taskMonitor
 	 *            TaskMonitor Object.
 	 */
 	public void setTaskMonitor(TaskMonitor taskMonitor)
 			throws IllegalThreadStateException {
 		this.taskMonitor = taskMonitor;
 	}
 
 	/**
 	 * Gets the Task Title.
 	 * 
 	 * @return Task Title.
 	 */
 	public String getTitle() {
		return new String("Loading Network");
 	}
 
 }
