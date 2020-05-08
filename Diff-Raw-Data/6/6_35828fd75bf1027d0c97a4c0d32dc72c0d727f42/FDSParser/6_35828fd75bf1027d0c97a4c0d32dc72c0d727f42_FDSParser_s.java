 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import board.Board;
 import board.Cell;
 
 public class FDSParser {
 
 	private Board board;
 	private float dx, dy, offsetX, offsetY; // [m]
 
 	public FDSParser(Board board) {
 		this.board = board;
 	}
 
 	/**
 	 * Wczytywanie danych z pliku wejciowego FDS-a.
 	 * 
 	 * Pamitaj: x to szeroko tunelu, y to dugo. Podajemy czsto
 	 * wsprzdne x,y,z albo x,x1,y,y1,z,z1.
 	 * 
 	 * @param board
 	 * @param path
 	 * @return Simulation time in [ms].
 	 */
 	public int inputFile(String path) throws FileNotFoundException {
 		boolean gotDimensions = false;
 		Pattern patternDimensions = Pattern
 				.compile("^&MESH\\s+IJK=(\\d+),(\\d+),\\d+,\\s*XB=(\\d+),(\\d+),(\\d+),(\\d+),\\d+,\\d+");
 
 		Pattern patternObstacle = Pattern
 				.compile("^&OBST\\s+XB=(\\d+),(\\d+),(\\d+),(\\d+),\\d+,\\d+");
 
 		boolean gotDuration = false;
 		int duration = 0;
 		Pattern patternDuration = Pattern.compile("^&TIME\\s+T_END=(\\d+)");
 		Matcher matcher;
 
 		String line;
 		BufferedReader br = new BufferedReader(new FileReader(path));
 		try {
 			while ((line = br.readLine()) != null) {
 				// dimensions
 				if (!gotDimensions) {
 					matcher = patternDimensions.matcher(line);
 					if (matcher.find()) {
 						int width = Integer.parseInt(matcher.group(1));
 						int length = Integer.parseInt(matcher.group(2));
 
 						offsetX = Integer.parseInt(matcher.group(3));
 						dx = (Integer.parseInt(matcher.group(4)) - offsetX)
 								/ width;
 						offsetY = Integer.parseInt(matcher.group(5));
 						dy = (Integer.parseInt(matcher.group(6)) - offsetY)
 								/ length;
 
 						board.initCells(width, length);
 						gotDimensions = true;
 						continue;
 					}
 				}
 
 				// duration
 				if (!gotDuration) {
 					matcher = patternDuration.matcher(line);
 					if (matcher.find()) {
 						duration = 1000 * Integer.parseInt(matcher.group(1));
 						gotDuration = true;
 						continue;
 					}
 				}
 
 				// obstacles
 				matcher = patternObstacle.matcher(line);
 				if (matcher.find()) {
 					if (!gotDimensions)
 						throw new RuntimeException("&OBST before &MESH!");
 					int x1 = Integer.parseInt(matcher.group(1));
 					int x2 = Integer.parseInt(matcher.group(2));
 					int y1 = Integer.parseInt(matcher.group(3));
 					int y2 = Integer.parseInt(matcher.group(4));
 					board.addObstacle(x1, x2, y1, y2);
 					continue;
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if (br != null)
 				try {
 					br.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			if (!gotDimensions)
 				throw new RuntimeException(path
 						+ ": no mesh dimensions declared");
 			if (!gotDuration)
 				throw new RuntimeException(path
 						+ ": no simulation duration declared");
 		}
 
 		return duration;
 	}
 
 	private class DataFile implements Comparable<DataFile> {
 		public File file;
 		public int start, end;
 		public boolean alreadyRead = false;
 
 		public DataFile(File file, int start, int end) {
 			this.file = file;
 			this.start = start;
 			this.end = end;
 		}
 
 		@Override
 		public int compareTo(DataFile o) {
 			if (start == o.start)
 				return end - o.end;
 			return start - o.start;
 		}
 	}
 
 	SortedSet<DataFile> dataFiles;
 
 	public void setDataDirectory(String directory) {
 		Pattern patternName = Pattern.compile("^temp_(\\d+)-(\\d+)s\\.csv$");
 		Matcher matcher;
 
 		dataFiles = new TreeSet<DataFile>();
 
 		for (File file : (new File(directory)).listFiles())
 			if (file.isFile()) {
 				matcher = patternName.matcher(file.getName());
 				if (matcher.find()) {
 					dataFiles.add(new DataFile(file, 1000 * Integer
 							.parseInt(matcher.group(1)), 1000 * Integer
 							.parseInt(matcher.group(2))));
 				}
 			}
 
 		if (dataFiles.isEmpty())
 			throw new RuntimeException(directory + ": no data files inside!");
 
 		currentDataFile = dataFiles.first();
 	}
 
 	private DataFile currentDataFile;
 
 	public void readData(int currentTime) throws FileNotFoundException {
		if (currentTime < currentDataFile.start
				|| currentTime > currentDataFile.end)
 			for (DataFile f : dataFiles)
				if (currentTime >= f.start && currentTime <= f.end) {
 					currentDataFile = f;
 					break;
 				}
 		if (currentDataFile.alreadyRead)
 			return;
 
 		String line;
 		BufferedReader br = new BufferedReader(new FileReader(
 				currentDataFile.file));
 		try {
 			br.readLine();
 			br.readLine();
 			while ((line = br.readLine()) != null) {
 				String[] v = line.trim().split("\\s*,\\s*");
 				if (v.length != 3)
 					throw new RuntimeException(currentDataFile.file.getPath()
 							+ ": invalid format");
 				int x = (int) Math.round(Double.valueOf(v[0]) / dx);
 				int y = (int) Math.round(Double.valueOf(v[1]) / dy);
 				double temperature = Double.valueOf(v[2]);
 
 				Cell cell = board.getCellAt(x, y);
 				if (cell == null)
 					continue;
 
 				cell.setTemperature((float) temperature);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null)
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 
 		currentDataFile.alreadyRead = true;
 	}
 }
