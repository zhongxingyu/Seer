 package sortvisualization;
 
 import java.util.ArrayList;
 import processing.core.*;
 
 @SuppressWarnings("serial")
 public class SortVisualization extends PApplet {
 
 	// Anybody should be able to adjust the array length and height
 	private static final int ARRAY_LENGTH = 100;
 	private static final int HEIGHT = 750; // TODO: If less than 600, stuff will overlap
 
 	// Set up the rest of the values based on the array length and height
 	@SuppressWarnings("unused")
 	private static final int WIDTH = ARRAY_LENGTH > 250 ? ARRAY_LENGTH * 4 : 1000;
 	private static final int MIN = (int)(HEIGHT * 0.1);
 	private static final int MAX = (int)(HEIGHT * 0.9);
 	private static final int BAR_WIDTH = WIDTH / ARRAY_LENGTH - 1;
	private static final String version = "0.1.0";
 
 	// Make a spot for an array
 	private Number[] array = new Number[ARRAY_LENGTH];
 	// Lets keep track of when we are sorting
 	private boolean running = false;
 
 	// Lists to keep track of what needs to be highlighted or dehighlighted
 	private ArrayList<Integer> highlighted = new ArrayList<Integer>();
 	private ArrayList<Integer> toHighlight = new ArrayList<Integer>();
 
 	// Counter for frames per second and the last time we showed it
 	private double fps = 0;
 	private long lastFpsOut = 0;
 
 	@Override
 	public void setup() {
 		// Set the size, background, stroke, fill, text settings, and sound length
 		size(WIDTH, HEIGHT);
 		background(255);
 		stroke(255);
 		fill(0);
 		textSize(16);
 		textAlign(TOP, LEFT);
 		AudioEngine.setLength(10);
 		// We need an array to sort obviously
 		this.newArray();
 	}
 
 	@Override
 	public void draw() {
 		// We are drawing, so that is another frame for the current second
 		++fps;
 		// What time is it right now
 		long time = System.currentTimeMillis();
 		// Has it been a second since we showed the last FPS count
 		if (time - 1000 > lastFpsOut) {
 			// We should repaint the FPS count and other miscellaneous text
 			fill(255);
 			rect(5, 0, WIDTH, 59);
 			fill(0);
			text("v" + this.version + ", FPS: " + (int)fps, 5, 5, WIDTH, 23);
 			text("Sorting algorithms: (s)election  (i)nsertion  (m)erge  recursive m(e)rge  (b)ogo", 5, 23, WIDTH, 41);
 			text("Miscellaneous: (n)ew  (f)lip  (c)heck  (1-9) pause length", 5, 41, WIDTH, 59);
 			// Reset the FPS count and set the last FPS out time to now
 			fps = 0;
 			lastFpsOut = time;
 		}
 
 		// If we are not running and there are highlighted bars, we need to dehighlight the bars
 		if (!running && !highlighted.isEmpty()) {
 			fill(0);
 			for (int i : highlighted)
 				rect(i * BAR_WIDTH + i, HEIGHT - array[i].getValue(), BAR_WIDTH, array[i].getValue());
 			// Clear the highlighted list
 			highlighted.clear();
 		}
 
 		// Clear the highlight queue
 		toHighlight.clear();
 		// We should repaint the bars that have changed
 		for (int i = 0; i < array.length; ++i) {
 			// Find out if this bar should be highlighted
 			if (array[i].isHighlighted()) {
 				toHighlight.add(i);
 			}
 
 			// Find out if this bar has to be repainted entirely
 			if (array[i].isDirty()) {
 				// Get rid of the old bar
 				fill(255);
 				rect(i * BAR_WIDTH + i, HEIGHT - MAX, BAR_WIDTH, MAX);
 				// Paint the new bar
 				fill(0);
 				rect(i * BAR_WIDTH + i, HEIGHT - array[i].getValue(), BAR_WIDTH, array[i].getValue());
 			}
 		}
 
 		// Is there anything in the highlight queue
 		if (!toHighlight.isEmpty()) {
 			// Are there already bars highlighted
 			if (!highlighted.isEmpty()) {
 				// Get rid of any bars that were highlighted
 				for (int i = highlighted.size() - 1; i >= 0; --i) {
 					// Is the old bar in the new queue
 					if (!toHighlight.contains(highlighted.get(i))) {
 						// No, we should repaint it dehighlighted
 						fill(0);
 						rect(highlighted.get(i) * BAR_WIDTH + highlighted.get(i), HEIGHT - array[highlighted.get(i)].getValue(), BAR_WIDTH, array[highlighted.get(i)].getValue());
 						// Remove it from the highlighted list
 						highlighted.remove(i);
 					}
 				}
 			}
 
 			// Are there any new bars to highlight that were not already highlighted
 			if (!toHighlight.equals(highlighted)) {
 				// Loop over the queue
 				for (int i : toHighlight) {
 					// Is it already highlighted
 					if (!highlighted.contains(i)) {
 						// No, we should repaint it highlighted
 						fill(0, 0, 255);
 						rect(i * BAR_WIDTH + i, HEIGHT - array[i].getValue(), BAR_WIDTH, array[i].getValue());
 						// Add it to the highlighted list
 						highlighted.add(i);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased() {
 		// If it was a number key, switch the running speed
 		switch (key) {
 			case '1':
 			case '2':
 			case '3':
 			case '4':
 			case '5':
 			case '6':
 			case '7':
 			case '8':
 			case '9':
 				AudioEngine.setLength(Integer.parseInt(key + "") * 10);
 		}
 
 		// Everything else only works if you are not running
 		if (running)
 			return;
 		
 		// We are running now
 		running = true;
 		switch (key) {
 			case 's':
 				thread("selectionSort");
 				break;
 			case 'i':
 				thread("insertionSort");
 				break;
 			case 'e':
 				thread("mergeSortRecursive");
 				break;
 			case 'b':
 				thread("bogoSort");
 				break;
 			case 'm':
 				thread("mergeSort");
 				break;
 			case 'c':
 				thread("isSorted");
 				break;
 			case 'n':
 				newArray();
 				break;
 			case 'f':
 				flipArray();
 				break;
 			default:
 				running = false;
 		}
 	}
 
 	private void newArray() {
 		for (int i = 0; i < array.length; ++i)
 			array[i] = new Number((int)random(MIN, MAX));
 		running = false;
 	}
 
 	private void flipArray() {
 		for (int i = 0; i < array.length / 2; ++i)
 			swap(i, array.length - i - 1);
 		running = false;
 	}
 
 	private void swap(int a, int b) {
 		array[a].dirty();
 		array[b].dirty();
 		Number temp = array[a];
 		array[a] = array[b];
 		array[b] = temp;
 	}
 
 	private void merge(int start, int middle, int end) {
 		Number[] merge = new Number[end - start];
 		int l = 0, r = 0, pos = 0;
 		while (l < middle - start && r < end - middle) {
 			if (array[start + l].lt(array[middle + r]))
 				merge[pos++] = array[start + l++];
 			else
 				merge[pos++] = array[middle + r++];
 		}
 		while (r < end - middle)
 			merge[pos++] = array[middle + r++];
 		while (l < middle - start)
 			merge[pos++] = array[start + l++];
 		for (int i = 0; i < merge.length; ++i) {
 			array[start++] = merge[i];
 			array[start - 1].dirty();
 		}
 	}
 
 	private void mergeArrays(Number[] array, int startL, int stopL, int startR, int stopR) {
 		Number[] right = new Number[stopR - startR + 1];
 		Number[] left = new Number[stopL - startL + 1];
 
 		for (int i = 0, k = startR; i < (right.length - 1); ++i, ++k)
 			right[i] = array[k];
 		for (int i = 0, k = startL; i < (left.length - 1); ++i, ++k)
 			left[i] = array[k];
 
 		right[right.length - 1] = new Number(Integer.MAX_VALUE);
 		left[left.length - 1] = new Number(Integer.MAX_VALUE);
 
 		for (int k = startL, m = 0, n = 0; k < stopR; ++k) {
 			if (left[m].lte(right[n])) {
 				array[k] = left[m];
 				m++;
 			} else {
 				array[k] = right[n];
 				n++;
 			}
 			array[k].dirty();
 		}
 	}
 
 	public boolean isSorted() {
 		if (array.length <= 1)
 			return !(running = false);
 		for (int i = 1; i < array.length; ++i)
 			if (array[i].lt(array[i - 1]))
 				return (running = false);
 		return !(running = false);
 	}
 
 	private boolean isSorted(boolean keepRunning) {
 		if (!keepRunning)
 			return isSorted();
 		if (array.length <= 1)
 			return true;
 		for (int i = 1; i < array.length; ++i)
 			if (array[i].lt(array[i - 1]))
 				return false;
 		return true;
 	}
 
 	public void selectionSort() {
 		if (array.length <= 1)
 			return;
 		for (int i = 0; i < array.length; ++i) {
 			int min = i;
 			for (int j = i + 1; j < array.length; ++j) {
 				if (array[j].lt(array[min]))
 					min = j;
 			}
 			if (min != i)
 				swap(i, min);
 		}
 		this.isSorted();
 	}
 
 	public void insertionSort() {
 		if (array.length <= 1)
 			return;
 		for (int i = 0; i < array.length; ++i) {
 			Number valueToInsert = array[i];
 			int holePos = i;
 			while (holePos > 0 && array[holePos].lt(array[holePos - 1])) {
 				swap(holePos, holePos - 1);
 				--holePos;
 			}
 			array[holePos] = valueToInsert;
 			array[holePos].dirty();
 		}
 		this.isSorted();
 	}
 
 	public void mergeSort() {
 		Number[] array = this.array;
 		if (array.length < 2)
 			return;
 		int step = 1;
 		int startL, startR;
 
 		while (step < array.length) {
 			startL = 0;
 			startR = step;
 			while (startR + step <= array.length) {
 				mergeArrays(array, startL, startL + step, startR, startR + step);
 				startL = startR + step;
 				startR = startL + step;
 			}
 			if (startR < array.length)
 				mergeArrays(array, startL, startL + step, startR, array.length);
 			step *= 2;
 		}
 		this.isSorted();
 	}
 
 	public void mergeSortRecursive() {
 		mergeSortRecursive(array, 0, array.length);
 		this.isSorted();
 	}
 
 	private void mergeSortRecursive(Number[] array, int start, int end) {
 		if (end - start <= 1)
 			return;
 		int middle = start + (end - start) / 2;
 		mergeSortRecursive(array, start, middle);
 		mergeSortRecursive(array, middle, end);
 		merge(start, middle, end);
 	}
 
 	public void bogoSort() {
 		while (!isSorted(true))
 			for (int i = 0; i < array.length; ++i)
 				swap(i, (int)random(0, array.length));
 		this.isSorted();
 	}
 
 }
