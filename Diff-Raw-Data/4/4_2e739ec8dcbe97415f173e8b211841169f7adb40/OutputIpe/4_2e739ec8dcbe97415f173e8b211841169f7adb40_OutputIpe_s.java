 package output;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Locale;
 import java.util.Set;
 
 import model.Schedule;
 import model.Task;
 import model.TaskInstance;
 
 /**
  * This class can be used to output a schedule to an IPE file.
  * 
  * @author Thom Castermans
  */
 public class OutputIpe {
 
 	/** List of colors supported by Ipe. */
 	public static final String[] IPE_COLORS = { "red", "green", "blue",
 			"yellow", "orange", "gold", "purple", "gray", "brown", "navy",
 			"pink", "seagreen", "turquoise", "violet", "darkblue", "darkcyan",
 			"darkgray", "darkgreen", "darkmagenta", "darkorange", "darkred",
 			"lightblue", "lightcyan", "lightgray", "lightgreen", "lightyellow" };
 	/** Size of grid, used in outputting graph. */
 	public static final int GRID_SIZE = 16;
 	/**
 	 * Offset for graph in Ipe file, over X-axis. This is the X-coordinate of
 	 * the upper-left corner of the drawing.
 	 */
 	public static final int OFFSET_X = 16 + 7 * GRID_SIZE;
 	/**
 	 * Offset for graph in Ipe file, over Y-axis. This is the Y-coordinate of
 	 * the upper-left corner of the drawing.
 	 */
 	public static final int OFFSET_Y = 832 - 4 * GRID_SIZE;
 	/** Padding, used in drawing squares. */
 	public static final double PADDING = 0.5;
 	/** Space around text. */
 	public static final int TEXT_MARGIN = GRID_SIZE / 5;
 
 	private PrintStream output;
 	private File outFile = null;
 
 	/**
 	 * Create a new object capable of outputting to the default output.
 	 */
 	public OutputIpe() {
 		output = System.out;
 	}
 
 	/**
 	 * Create a new object capable of outputting to the given file.
 	 * 
 	 * @param file
 	 *            The file to write to.
 	 * @throws FileNotFoundException
 	 *             If given file cannot be found.
 	 */
 	public OutputIpe(File file) throws FileNotFoundException {
 		output = new PrintStream(new FileOutputStream(file));
 		outFile = file;
 	}
 
 	/**
 	 * Output the given schedule to the file given at construction or standard
 	 * output, depending on how this object was constructed.
 	 * 
 	 * @param schedule
 	 *            The schedule to be outputted.
 	 * @param options
 	 *            Options for output.
 	 */
 	public void outputIpeFile(Schedule schedule, OutputIpeOptions options) {
 		if (outFile != null) {
 			try {
 				output = new PrintStream(new FileOutputStream(outFile));
 			} catch (FileNotFoundException e) {
 				// Does not occur by construction, see constructor: we check it
 				// there already
 				e.printStackTrace();
 			}
 		}
 
 		// Compress schedule, it is easier to have nice output like this
 		schedule.compress();
 
 		// Get tasks in the schedule, convert this to a list and
 		// sort the list so that tasks are sorted by name
 		Set<Task> tasksSet = schedule.getTasks();
 		ArrayList<Task> tasks = new ArrayList<Task>(tasksSet);
 		Collections.sort(tasks, new Comparator<Task>() {
 			@Override
 			public int compare(Task o1, Task o2) {
 				return o2.getName().compareTo(o1.getName());
 			}
 		});
 
 		// Some variable declarations
 		TaskInstance curTaskInstance, prevTaskInstance = null;
 		int i, j;
 
 		// Ipe header
 		outputHeader();
 
 		// Draw tasks
 		double time = 0;
 		while (time < schedule.getLcm()) {
 			curTaskInstance = schedule.getTaskInstanceAt(time);
 			if (curTaskInstance == null) {
 				if (schedule.getNextTaskAt(time) == null)
 					break;
 				time = schedule.getNextTaskAt(time).getStart();
 				curTaskInstance = schedule.getTaskInstanceAt(time);
 			}
 
 			j = 0;
 			for (Task tt : tasks) {
 				if (tt.equals(curTaskInstance.getTask()))
 					break;
 				j++;
 			}
 
 			// deciding the colors based on the options
 			String lineColor;
 			String fillColor;
 			if (options.getBooleanOption("useColors")) {
 				lineColor = IPE_COLORS[j % IPE_COLORS.length];
 			} else {
 				lineColor = "black";
 			}
 			if (options.getBooleanOption("fill")) {
 				fillColor = lineColor;
 			} else {
 				fillColor = "white";
 			}
 
 			writeSquareFilled(
 					OFFSET_X
 							+ GRID_SIZE
 							* curTaskInstance.getStart()
 							+ (prevTaskInstance != null
 									&& curTaskInstance.getTask().equals(
 											prevTaskInstance.getTask()) ? -PADDING
 									: PADDING),
 					OFFSET_Y + GRID_SIZE * (j - tasks.size()) + PADDING,
 					GRID_SIZE
 							* (curTaskInstance.getEnd() - curTaskInstance
 									.getStart())
 							- (prevTaskInstance != null
 									&& curTaskInstance.getTask().equals(
 											prevTaskInstance.getTask()) ? 0
 									: 2 * PADDING), GRID_SIZE - 2 * PADDING,
 					lineColor, fillColor);
 			prevTaskInstance = curTaskInstance;
 			time = curTaskInstance.getEnd();
 		}
 
 		// Draw axis
 		writeLine(OFFSET_X, OFFSET_Y - GRID_SIZE * tasks.size(), OFFSET_X
 				+ GRID_SIZE * schedule.getLcm(),
 				OFFSET_Y - GRID_SIZE * tasks.size(), "black", null);
 		writeLine(OFFSET_X, OFFSET_Y, OFFSET_X,
 				OFFSET_Y - GRID_SIZE * tasks.size(), "black", null);
 
 		// write X-axis scale
 		int xAxisNumbering = options.getIntegerOption("xAxisNumbering");
 		if (xAxisNumbering != 0) { // we want to use a numbering
 			// getting pre-/postfix
 			String prefix = options.getStringOption("xAxisPreLabelText");
 			String postfix = options.getStringOption("xAxisPostLabelText");
 			
 			// writing the numbers
 			int stepSize = xAxisNumbering < 0 ? 1 : (schedule.getLcm() / (xAxisNumbering - 1));
 			int maxOption = options.getIntegerOption("scheduleMaxLength");
 			int until = maxOption == 0 ? schedule.getLcm() : maxOption;
 			for (i = 0; i <= until; i += stepSize) {
 				writeString(prefix + i + postfix, OFFSET_X
 						+ GRID_SIZE * i, OFFSET_Y - GRID_SIZE * tasks.size()
 						- TEXT_MARGIN, "center", "top");
 			}
 			// always draw the last
 			writeString(prefix + until + postfix, OFFSET_X
 					+ GRID_SIZE * until, OFFSET_Y - GRID_SIZE * tasks.size()
 					- TEXT_MARGIN, "center", "top");
 		}
 
 		// write Y-axis task names
		String taskPrefix = options.getStringOption("xAxisPreLabelText");
		String taskPostfix = options.getStringOption("xAxisPostLabelText");
 		
 		// looping over the tasks
 		j = 0;
 		for (Task tt : tasks) {
 			String string = taskPrefix + tt.getName() + taskPostfix;
 			writeString(string, OFFSET_X - TEXT_MARGIN, OFFSET_Y
 					+ GRID_SIZE * (j - tasks.size()) + GRID_SIZE / 2, "right",
 					"center");
 			j++;
 		}
 
 		// Draw deadline miss, if any
 		if (!schedule.isFeasible()) {
 			// First, draw dashed border around last instance of task that
 			// missed its deadline and draw a dashed line where the deadline is.
 			TaskInstance lastTaskInstance = schedule
 					.getMissedTaskLastInstance();
 			j = 0;
 			for (Task tt : tasks) {
 				if (tt.equals(lastTaskInstance.getTask()))
 					break;
 				j++;
 			}
 			writeSquare(
 					OFFSET_X + GRID_SIZE * lastTaskInstance.getStart(),
 					OFFSET_Y + GRID_SIZE * (j - tasks.size()),
 					GRID_SIZE
 							* (lastTaskInstance.getEnd() - lastTaskInstance
 									.getStart()), GRID_SIZE, "black", "dashed");
 			writeLine(
 					OFFSET_X
 							+ GRID_SIZE
 							* lastTaskInstance.getTask().getAbsoluteDeadline(
 									lastTaskInstance.getStart()),
 					OFFSET_Y - GRID_SIZE * tasks.size(),
 					OFFSET_X
 							+ GRID_SIZE
 							* lastTaskInstance.getTask().getAbsoluteDeadline(
 									lastTaskInstance.getStart()), OFFSET_Y
 							+ GRID_SIZE, "black", "dashed");
 		}
 		
 		
 		// show the schedule name
 		
 
 		// Ipe footer
 		outputFooter();
 
 		// Close stream
 		if (output != System.out)
 			output.close();
 	}
 
 	private void outputFromFile(String path) {
 		InputStream is = getClass().getResourceAsStream(path);
 		// Read header from file and output it to the stream
 		byte[] buffer = new byte[4096]; // tweaking this number may increase
 										// performance
 		int len;
 		try {
 			while ((len = is.read(buffer)) != -1) {
 				output.write(buffer, 0, len);
 			}
 			output.flush();
 			output.println();
 			is.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void outputFooter() {
 		outputFromFile("/res/ipe_footer.txt");
 	}
 
 	private void outputHeader() {
 		outputFromFile("/res/ipe_header.txt");
 	}
 
 	/* methods to write "shapes" to the Ipe file */
 	private final String SQUARE = "<path layer=\"alpha\" stroke=\"%s\" dash=\"%s\"> \n"
 			+ "%f %f m \n"
 			+ "%f %f l \n"
 			+ "%f %f l \n"
 			+ "%f %f l \n"
 			+ "h \n" + "</path> \n";
 	private final String SQUARE_FILLED = "<path layer=\"alpha\" stroke=\"%s\" fill=\"%s\"> \n"
 			+ "%f %f m \n"
 			+ "%f %f l \n"
 			+ "%f %f l \n"
 			+ "%f %f l \n"
 			+ "h \n" + "</path> \n";
 
 	@SuppressWarnings("boxing")
 	private void writeSquare(double x, double y, double width, double height,
 			String color, String dashed) {
 		String square = String.format(Locale.US, SQUARE, color, dashed, x, y, x
 				+ width, y, x + width, y + height, x, y + height);
 		try {
 			output.write(square.getBytes());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("boxing")
 	private void writeSquareFilled(double x, double y, double width,
 			double height, String lineColor, String color) {
 		String square = String
 				.format(Locale.US, SQUARE_FILLED, lineColor, color, x, y, x
 						+ width, y, x + width, y + height, x, y + height);
 		try {
 			output.write(square.getBytes());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private final String LINE = "<path stroke=\"%s\"> \n" + "%f %f m \n"
 			+ "%f %f l \n" + "</path> \n";
 	private final String LINE_DASHED = "<path stroke=\"%s\" dash=\"%s\"> \n"
 			+ "%f %f m \n" + "%f %f l \n" + "</path> \n";
 
 	@SuppressWarnings("boxing")
 	private void writeLine(double x1, double y1, double x2, double y2,
 			String color, String dashed) {
 		String line = "";
 		if (dashed != null) {
 			line = String.format(Locale.US, LINE_DASHED, color, dashed, x1, y1,
 					x2, y2);
 		} else {
 			line = String.format(Locale.US, LINE, color, x1, y1, x2, y2);
 		}
 		try {
 			output.write(line.getBytes());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private final String STRING = "<text transformations=\"translations\" pos=\"%f %f\" "
 			+ "stroke=\"black\" type=\"label\" depth=\"0\" "
 			+ "halign=\"%s\" valign=\"%s\">%s</text> \n";
 
 	@SuppressWarnings("boxing")
 	private void writeString(String text, double x, double y, String halign,
 			String valign) {
 		String string = String.format(Locale.US, STRING, x, y, halign, valign,
 				text);
 		try {
 			output.write(string.getBytes());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
