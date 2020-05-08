 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SchedVis is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.rendering;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 import javax.imageio.ImageIO;
 import javax.swing.SwingWorker;
 
 import org.apache.log4j.Logger;
 
 import cz.muni.fi.spc.SchedVis.Configuration;
 import cz.muni.fi.spc.SchedVis.model.Database;
 import cz.muni.fi.spc.SchedVis.model.entities.Event;
 import cz.muni.fi.spc.SchedVis.model.entities.Machine;
 
 /**
  * This class knows how to render schedule for a machine into an image and how
  * to save it to a file, if necessary.
  * 
  * This class uses some terms that need explanation:
  * <dl>
  * <dt>Caching</dt>
  * <dd>Name for the state of this class when it is run from a command line,
  * doing nothing but pre-generating schedule images. In this case, some
  * optimizations are performed so that no unnecessary operations are performed.</dd>
  * <dt>Delayed file saving</dt>
  * <dd>Happens when the schedule image has been rendered. In order not to block
  * other possible threads in rendering, the slow operation of saving a file is
  * "out-sourced" to another thread and the rendered image is returned
  * immediately.</dd>
  * </dl>
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public final class ScheduleRenderer extends SwingWorker<Image, Void> {
 
 	/**
 	 * Holds the machine whose schedule is currently being rendered.
 	 */
 	private final Machine m;
 	/**
 	 * Holds a name of the database so that the cached schedule images from
 	 * different databases don't interfere.
 	 */
 	private static final String instanceId = new File(Database.getName())
 	    .getName();
 	/**
 	 * Holds the position on the timeline that is currently being rendered.
 	 */
 	private final Integer clock;
 	/**
 	 * How many pixels shall one CPU of a machine occupy on the y axis of the
 	 * schedule.
 	 */
 	private static final Integer NUM_PIXELS_PER_CPU = 5;
 	/**
 	 * How many pixels shall be used per a single tick on the x axis of the
 	 * schedule.
 	 */
 	private static final Float NUM_PIXELS_PER_TICK = new Float(0.1);
 	/**
 	 * How many pixels should be left in the left of the schedule for jobs that
 	 * were supposed to be executed before the current clock.
 	 */
 	private static final Integer OVERFLOW_WIDTH = Math.round(Event
 	    .getMaxJobSpan()
 	    * ScheduleRenderer.NUM_PIXELS_PER_TICK) / 8;
 	/**
 	 * Total length of the x axis of the schedule. If you need to change it,
 	 * please change the input values, not the equation.
 	 */
 	private static final Integer LINE_WIDTH = Math.round(Event.getMaxJobSpan()
 	    * ScheduleRenderer.NUM_PIXELS_PER_TICK)
 	    + ScheduleRenderer.OVERFLOW_WIDTH;
 	/**
 	 * Colors that are available for the jobs. This array can be extended at will
 	 * and the color-picking code will adjust to it.
 	 * 
 	 * Please remember not to use following colors: white (background for
 	 * machines), dark gray (background for disabled machines) and red (overdue
 	 * jobs).
 	 */
 	private static final Color[] colors = { Color.BLUE, Color.CYAN, Color.GREEN,
 	    Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY, Color.PINK,
 	    Color.YELLOW };
 
 	/**
 	 * Holds a font used throughout the schedules. Memory use improvement.
 	 */
 	private static Font font = new Font("Monospaced", Font.PLAIN, 9);
 
 	/**
 	 * Whether or not this renderer is caching.
 	 */
 	private final boolean isCaching;
 
 	/**
 	 * Holds events in a currently rendered schedule. Stored for performance
 	 * reasons.
 	 */
 	private List<Event> events;
 
 	private static Logger logger = Logger.getLogger(ScheduleRenderer.class);
 
 	/**
 	 * Micro-optimization. Holds the parsed values of assigned CPUs.
 	 */
 	private final Map<String, Integer[]> sets = new HashMap<String, Integer[]>();
 
 	/**
 	 * The executor service used for delayed file saving.
 	 */
 	private final ExecutorService fileSaver;
 
 	/**
 	 * Holds colors for different jobs, so that they persist and are the same over
 	 * the whole application runtime.
 	 */
 	private static final Map<Integer, Color> jobsToColors = new HashMap<Integer, Color>();
 	/**
 	 * Random for assigning job colors.
 	 */
 	private static final Random rand = new Random();
 
 	/**
 	 * Class constructor that disables caching and does not report progress.
 	 * 
 	 * @param m
 	 *          Machine to render.
 	 * @param clock
 	 *          A point in time in which we want the schedule rendered.
 	 * @param fileSaver
 	 *          Executor service used for delayed file saving.
 	 */
 	public ScheduleRenderer(final Machine m, final Integer clock,
 	    final ExecutorService fileSaver) {
 		this(m, clock, fileSaver, false, null);
 	}
 
 	/**
 	 * Class constructor that does not report progress.
 	 * 
 	 * @param m
 	 *          Machine to render.
 	 * @param clock
 	 *          A point in time in which we want the schedule rendered.
 	 * @param fileSaver
 	 *          Executor service used for delayed file saving.
 	 * @param isCaching
 	 *          Whether or not this instance should be caching.
 	 */
 	public ScheduleRenderer(final Machine m, final Integer clock,
 	    final ExecutorService fileSaver, final boolean isCaching) {
 		this(m, clock, fileSaver, isCaching, null);
 	}
 
 	/**
 	 * Class constructor.
 	 * 
 	 * @param m
 	 *          Machine to render.
 	 * @param clock
 	 *          A point in time in which we want the schedule rendered.
 	 * @param fileSaver
 	 *          Executor service used for delayed file saving.
 	 * @param isCaching
 	 *          Whether or not this instance should be caching.
 	 * @param l
 	 *          A listener to report progress to.
 	 */
 	public ScheduleRenderer(final Machine m, final Integer clock,
 	    final ExecutorService fileSaver, final boolean isCaching,
 	    final PropertyChangeListener l) {
 		this.m = m;
 		this.clock = clock;
 		this.isCaching = isCaching;
 		this.fileSaver = fileSaver;
 		if (l != null) {
 			this.addPropertyChangeListener(l);
 		}
 	}
 
 	/**
 	 * Performs the actual drawing of the machine schedule. Draws a frame and
 	 * calls another method to perform drawing of jobs.
 	 * 
 	 * @return
 	 */
 	private BufferedImage actuallyDraw() {
 		Double time = Double.valueOf(System.nanoTime());
 		this.events = Machine.getLatestSchedule(this.m, this.clock);
 		final BufferedImage img = new BufferedImage(ScheduleRenderer.LINE_WIDTH,
 		    this.m.getCPUs() * ScheduleRenderer.NUM_PIXELS_PER_CPU,
 		    BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g = (Graphics2D) img.getGraphics();
 		this.fineTuneGraphics(g);
 		g.setFont(ScheduleRenderer.font);
 		boolean isActive = Machine.isActive(this.m, this.clock);
 		if (isActive) {
 			g.setColor(Color.WHITE);
 		} else {
 			g.setColor(Color.DARK_GRAY);
 		}
 		g.fillRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
 		this.drawJobs(g);
 		if (isActive) {
 			g.setColor(Color.BLACK);
 			g.drawString(this.m.getName() + "@" + this.clock, 1, 9);
 		} else {
 			g.setColor(Color.WHITE);
 			g.drawString(this.m.getName() + "@" + this.clock + " (off-line)", 1, 9);
 		}
 		// draw a line in a place where "zero" (current clock) is.
 		g.drawLine(ScheduleRenderer.OVERFLOW_WIDTH, 0,
 		    ScheduleRenderer.OVERFLOW_WIDTH, this.m.getCPUs()
 		        * ScheduleRenderer.NUM_PIXELS_PER_CPU);
 		time = (System.nanoTime() - time) / 1000 / 1000 / 1000;
 		ScheduleRenderer.logger.debug(this.m.getName() + "@" + this.clock
 		    + " finished rendering. Took " + time + " seconds.");
 		return img;
 	}
 
 	/**
 	 * Background task to render the images used for machine schedules.
 	 * 
 	 * The logic in this method tries to cache rendered images whenever possible.
 	 * If a cache file is not found, image is rendered and, if possible, cached so
 	 * that it needs not be rendered next time.
 	 */
 	@Override
 	public Image doInBackground() {
 		File f = new File(this.getFilename()).getAbsoluteFile();
 		BufferedImage img = null;
 		if (!f.exists()) {
 			img = this.actuallyDraw();
 			this.fileSaver.submit(new MachineFileWriter(img, f));
 		} else if (!this.isCaching) {
 			try {
				if (f.length() == 0) {
					f.delete();
					img = (BufferedImage) this.doInBackground();
				} else {
					img = ImageIO.read(f);
				}
 			} catch (IOException e) {
 				ScheduleRenderer.logger.warn("Cannot read cache for machine "
 				    + this.m.getId() + "@" + this.clock
 				    + ". Failed to read from a file " + f.getAbsolutePath() + ".");
 				img = this.actuallyDraw();
 			}
 		}
 		return img;
 	}
 
 	/**
 	 * Takes machine schedule data and renders them.
 	 * 
 	 * @param g
 	 *          The graphics in question.
 	 * @todo Produces unclear job boundaries, probably because of rounding.
 	 */
 	private void drawJobs(final Graphics2D g) {
 		// render jobs in a schedule, one by one
 		for (final Event evt : this.events) {
 			// get assigned CPUs, set will ensure they are unique and sorted
 			synchronized (this.sets) {
 				if (!this.sets.containsKey(evt.getAssignedCPUs())) {
 					final Set<Integer> assignedCPUs = new HashSet<Integer>();
 					for (final String num : evt.getAssignedCPUs().split(",")) {
 						assignedCPUs.add(Integer.valueOf(num));
 					}
 					this.sets.put(evt.getAssignedCPUs(), assignedCPUs
 					    .toArray(new Integer[0]));
 				}
 			}
 			/*
 			 * now isolate all the contiguous blocks of CPUs in the job and paint
 			 * them.
 			 */
 			final Integer[] cpus = this.sets.get(evt.getAssignedCPUs());
 			for (int i = 0; i < cpus.length; i++) {
 				final int crntCPU = cpus[i];
 				try {
 					while (cpus[i + 1] == cpus[i] + 1) {
 						// loop until a gap is found in the list of used CPUs
 						i++;
 					}
 				} catch (final ArrayIndexOutOfBoundsException e) {
 					// but finish when all the CPUs have been seeked through
 				}
 				final int lastCPU = cpus[i];
 				final int numCPUs = lastCPU - crntCPU + 1;
 				// now draw
 				final int jobStartX = this.getStartingPosition(evt);
 				if (jobStartX < 0) {
 					// might be ok, but might also be bad. so inform.
 					ScheduleRenderer.logger.info("Machine " + this.m.getName() + " at "
 					    + this.clock + " is drawing " + jobStartX
 					    + " before its boundary.");
 				}
 				final int jobLength = this.getJobLength(evt);
 				final int ltY = crntCPU * ScheduleRenderer.NUM_PIXELS_PER_CPU;
 				final int jobHgt = numCPUs * ScheduleRenderer.NUM_PIXELS_PER_CPU;
 				if ((evt.getDeadline() > -1) && (evt.getDeadline() < this.clock)) {
 					// the job has a deadline and has missed it
 					g.setColor(Color.RED);
 				} else {
 					// job with no deadlines
 					g.setColor(this.getJobColor(evt.getJob()));
 				}
 				g.fill3DRect(jobStartX, ltY, jobLength, jobHgt, true);
 				g.setColor(Color.BLACK);
 				g.drawString(evt.getJob().toString(), Math.max(jobStartX + 2, 2), ltY
 				    + jobHgt - 2);
 				int rightBoundary = jobStartX + jobLength - ScheduleRenderer.LINE_WIDTH;
 				if (rightBoundary > 0) {
 					// always bad. warn.
 					ScheduleRenderer.logger.warn("Machine " + this.m.getName() + " at "
 					    + this.clock + " is drawing " + rightBoundary
 					    + " over its boundary.");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tweaks the graphics object so that it performs better. Probably just a
 	 * micro-optimization.
 	 * 
 	 * @param g
 	 *          The graphics in question.
 	 */
 	private void fineTuneGraphics(final Graphics2D g) {
 		g.setRenderingHint(RenderingHints.KEY_RENDERING,
 		    RenderingHints.VALUE_RENDER_SPEED);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 		    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
 		    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
 		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
 		    RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 		    RenderingHints.VALUE_ANTIALIAS_OFF);
 	}
 
 	/**
 	 * Get filename for the cache file.
 	 * 
 	 * @return The file name.
 	 * @todo Make the max length of the id unlimited.
 	 */
 	private String getFilename() {
 		String id = "0000000000" + this.clock;
 		return Configuration.getTempFolder() + System.getProperty("file.separator")
 		    + ScheduleRenderer.instanceId + "-" + this.m.getName() + "-"
 		    + id.substring(id.length() - 10, id.length()) + ".gif";
 	}
 
 	/**
 	 * Make sure a job has always the same color, no matter when and where it is
 	 * painted.
 	 * 
 	 * @param jobId
 	 * @return A color that shall be used for that job. May be ignored when we
 	 *         need to use another color for a job, indicating some special state
 	 *         the job is in.
 	 */
 	private synchronized Color getJobColor(final Integer jobId) {
 		if (!ScheduleRenderer.jobsToColors.containsKey(jobId)) {
 			Integer random = ScheduleRenderer.rand
 			    .nextInt(ScheduleRenderer.colors.length);
 			ScheduleRenderer.jobsToColors.put(jobId, ScheduleRenderer.colors[random]);
 		}
 		return ScheduleRenderer.jobsToColors.get(jobId);
 	}
 
 	/**
 	 * Calculate the length of a job on the screen.
 	 * 
 	 * @param evt
 	 *          The job in question.
 	 * @return Length in pixels.
 	 */
 	private int getJobLength(final Event evt) {
 		try {
 			return Math.round((evt.getExpectedEnd() - evt.getExpectedStart())
 			    * ScheduleRenderer.NUM_PIXELS_PER_TICK);
 		} catch (final NullPointerException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Get the starting position for the event, when being rendered on the screen.
 	 * 
 	 * @param evt
 	 *          The event in question.
 	 * @return The X starting coordinate.
 	 */
 	private int getStartingPosition(final Event evt) {
 		try {
 			return Math.round((evt.getExpectedStart() - this.clock)
 			    * ScheduleRenderer.NUM_PIXELS_PER_TICK)
 			    + ScheduleRenderer.OVERFLOW_WIDTH;
 		} catch (final NullPointerException e) {
 			return ScheduleRenderer.OVERFLOW_WIDTH;
 		}
 	}
 
 }
