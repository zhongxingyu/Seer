 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * SchedVis is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.util;
 
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.Formatter;
 import java.util.Properties;
 
 import org.slf4j.LoggerFactory;
 
 import cz.muni.fi.spc.SchedVis.util.l10n.Messages;
 
 /**
  * The class that is used to access every bit of SchedVis configuration. It is a
  * singleton.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public final class Configuration {
 
 	private static Properties p;
 
 	public static boolean createGroupPerMachine() {
 		final String val = Configuration.getProperties().getProperty(
 		    "import.group_per_machine", "0");
 		if (val.equals("1")) {
 			return true;
 		}
 		return false;
 	}
 
 	public static Integer getBenchmarkFrequency() {
 		return Integer.valueOf(Configuration.getProperties().getProperty(
 		    "benchmark.every_nth_event", "100"));
 	}
 
 	public static Integer getBenchmarkIterations() {
 		return Integer.valueOf(Configuration.getProperties().getProperty(
 		    "benchmark.number_of_runs", "5"));
 	}
 
 	/**
 	 * Retrieve the file that holds the SQLite database.
 	 * 
 	 * @return A database file.
 	 */
 	public static File getDatabaseFile() {
 		return new File(Configuration.getProperties().getProperty("files.database",
 		    "Production.sqlite")).getAbsoluteFile();
 	}
 
 	/**
 	 * Retrieve the file that holds the available events.
 	 * 
 	 * @return Events file.
 	 */
 	public static File getEventsFile() {
 		return new File(Configuration.getProperties().getProperty("files.events",
 		    "Data-set.txt")).getAbsoluteFile();
 	}
 
 	/**
 	 * Retrieve the file that holds the available machines.
 	 * 
 	 * @return Machines file.
 	 */
 	public static File getMachinesFile() {
 		return new File(Configuration.getProperties().getProperty("files.machines",
 		    "machines.txt")).getAbsoluteFile();
 	}
 
 	/**
 	 * The maximum allowed width of the schedule image.
 	 * 
 	 * @return Image width in pixels.
 	 */
 	public static Integer getMaxImageWidth() {
 		return Integer.valueOf(Configuration.getProperties().getProperty(
 		    "graphics.max_image_width", "800"));
 	}
 
 	/**
 	 * Number of pixels each CPU should take up on the y axis.
 	 * 
 	 * @return Will always be even and >= 5.
 	 */
 	public static Integer getNumberOfPixelsPerCPU() {
 		final Integer minValue = 5;
 		Integer actualValue = Integer.valueOf(Configuration.getProperties()
 		    .getProperty("graphics.pixels_per_cpu", minValue.toString()));
 		if ((actualValue % 2) == 0) {
 			actualValue++;
 		}
 		return Math.max(minValue, actualValue);
 	}
 
 	/**
 	 * The length between guiding lines on the x axis of a schedule.
 	 * 
 	 * @return Number of ticks between every two guiding lines.
 	 */
 	public static Integer getNumberOfTicksPerGuide() {
 		return Integer.valueOf(Configuration.getProperties().getProperty(
 		    "graphics.ticks_per_guide", "5"));
 	}
 
 	/**
 	 * Get the delay that the "play" function uses between frames.
 	 * 
 	 * @return A number of milliseconds to wait before showing another frame.
 	 */
 	public static Integer getPlayDelay() {
 		return Integer.valueOf(Configuration.getProperties().getProperty(
 		    "play.delay", "1000"));
 	}
 
 	/**
 	 * Retrieve the parsed configuration file.
 	 * 
 	 * @return The object holding all the configuration values on success, empty
 	 *         object on failure.
 	 */
 	protected synchronized static Properties getProperties() {
 		if (Configuration.p == null) {
 			try {
				final InputStreamReader in = new InputStreamReader(Configuration.class
				    .getResourceAsStream("/config.properties"));
 				Configuration.p = new Properties();
 				Configuration.p.load(in);
 			} catch (final Exception e) {
 				Configuration.p = new Properties();
 				LoggerFactory.getLogger(Configuration.class).error(
 				    new Formatter().format(Messages.getString("Configuration.17"),
 				        e.getLocalizedMessage()).toString());
 			}
 		}
 		return Configuration.p;
 	}
 
 }
