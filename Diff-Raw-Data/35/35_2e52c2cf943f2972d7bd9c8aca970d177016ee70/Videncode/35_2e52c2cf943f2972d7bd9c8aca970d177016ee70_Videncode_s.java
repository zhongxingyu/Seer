 package com.github.dnsev.videncode;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.nio.channels.FileChannel;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import java.awt.Dimension;
 import java.awt.*;
 import java.awt.MediaTracker;
 import java.awt.image.*;
 import javax.swing.SwingUtilities;
 
 import java.text.DecimalFormat;
 
 
 
 public final class Videncode extends ThreadManager {
 	// Classes
 	public static final class FrameRate {
 		private static final Pattern pattern = Pattern.compile("\\s*([\\.eE0-9]+)\\s*(fps)?(\\s+.*)?");
 
 
 		private int numerator;
 		private int denominator;
 		private double exact;
 
 		public FrameRate(final FrameRate other) {
 			this.numerator = other.numerator;
 			this.denominator = other.denominator;
 			this.exact = other.exact;
 		}
 		public FrameRate(String str) {
 			this.numerator = -1;
 			this.denominator = -1;
 
 			this.exact = FrameRate.parseFrameRate(str);
 		}
 		public FrameRate(double rate) {
 			this.numerator = -1;
 			this.denominator = -1;
 
 			this.exact = rate;
 		}
 		public FrameRate(int numerator) {
 			this.numerator = numerator;
 			this.denominator = 1;
 		}
 		public FrameRate(int numerator, int denominator) {
 			this.numerator = numerator;
 			this.denominator = denominator;
 		}
 
 		public void setFrameRate(final FrameRate other) {
 			this.numerator = other.numerator;
 			this.denominator = other.denominator;
 			this.exact = other.exact;
 		}
 		public void setFrameRate(String str) {
 			this.numerator = -1;
 			this.denominator = -1;
 
 			this.exact = FrameRate.parseFrameRate(str);
 		}
 		public void setFrameRate(double rate) {
 			this.numerator = -1;
 			this.denominator = -1;
 
 			this.exact = rate;
 		}
 		public void setFrameRate(int numerator) {
 			this.numerator = numerator;
 			this.denominator = 1;
 		}
 		public void setFrameRate(int numerator, int denominator) {
 			this.numerator = numerator;
 			this.denominator = denominator;
 		}
 
 		public static final double parseFrameRate(String str) {
 			Matcher m = FrameRate.pattern.matcher(str);
 			if (m.matches()) {
 				try {
 					return Double.parseDouble(m.group(1));
 				}
 				catch (NumberFormatException e) {}
 			}
 			return 1.0;
 		}
 		public double getFrameRate() {
 			return (this.numerator < 0 ? this.exact : this.numerator / (double) this.denominator);
 		}
 
 		public boolean equals(final FrameRate other) {
 			return Math.abs(other.getFrameRate() - this.getFrameRate()) < Videncode.DECIMAL_THRESHOLD;
 		}
 
 		@Override
 		public String toString() {
 			return this.toShortString() + " fps";
 		}
 
 		public String toShortString() {
 			return new DecimalFormat("#.###").format(this.getFrameRate());
 		}
 
 		public static final FrameRate create(final FrameRate other) {
 			return new FrameRate(other);
 		}
 		public static final FrameRate create(String name) {
 			return new FrameRate(name);
 		}
 		public static final FrameRate create(double rate) {
 			return new FrameRate(rate);
 		}
 		public static final FrameRate create(int numerator) {
 			return new FrameRate(numerator);
 		}
 		public static final FrameRate create(int numerator, int denominator) {
 			return new FrameRate(numerator, denominator);
 		}
 	};
 	public final class AutoQualityProfile {
 		private String name = "";
 		private double minBitsPerPixel = 0.0;
 
 		public AutoQualityProfile(double minBitsPerPixel, String name) {
 			this.minBitsPerPixel = minBitsPerPixel;
 			this.name = name;
 		}
 
 		public final double getMinBitsPerPixel() {
 			return this.minBitsPerPixel;
 		}
 
 		@Override
 		public String toString() {
 			return this.name;
 		}
 	}
 	public final class AutoQuality {
 		public double bitrate = 0;
 		public Dimension resolution = new Dimension();
 	};
 	public static final class Bitrate {
 		private static final Pattern pattern = Pattern.compile("\\s*([\\.eE0-9]+)\\s*(([kmKM])([bB])?([pP\\/][sS])?)?(\\s+.*)?");
 
 		private int bitrate = 0;
 		private String label = "";
 
 		public Bitrate(final String label) {
 			this.label = label;
 			this.bitrate = Bitrate.parseBitrate(this.label);
 		}
 		public Bitrate(int bitrate, final String label) {
 			this.bitrate = bitrate;
 			this.label = label;
 		}
 		public Bitrate(int kbitrate) {
 			this.bitrate = kbitrate * 1000;
 			this.label = kbitrate + " kbps";
 		}
 
 		public final int getBitrate() {
 			return this.bitrate;
 		}
 
 		public static final int parseBitrate(final String str) {
 			Matcher m = FrameRate.pattern.matcher(str);
 			if (m.matches()) {
 				double d = 0.0;
 				try {
 					d = Double.parseDouble(m.group(1));
 				}
 				catch (NumberFormatException e) {
 					return 1;
 				}
 
 				int factor = 1;
 				int bitFactor = 1;
 				if (m.group(3) != null) {
 					if (m.group(3).equals("k")) factor = 1000;
 					else if (m.group(3).equals("m")) factor = 1000 * 1000;
 					else if (m.group(3).equals("K")) {
 						factor = 1000;
 						bitFactor = 8;
 					}
 					else if (m.group(3).equals("M")) {
 						factor = 1000 * 1000;
 						bitFactor = 8;
 					}
 				}
 				if (m.group(4) != null) {
 					if (m.group(4).equals("b")) bitFactor = 1;
 					else if (m.group(4).equals("B")) bitFactor = 8;
 				}
 
 				return (int) Math.ceil(d * factor * bitFactor);
 			}
 			return 1;
 		}
 
 		public boolean equals(Bitrate other) {
 			return (this.bitrate == other.bitrate);
 		}
 
 		@Override
 		public final String toString() {
 			return this.label;
 		}
 	}
 	public static final class AudioChannelCount {
 		private int channels;
 
 		public AudioChannelCount(int channels) {
 			this.channels = channels;
 		}
 
 		public boolean equals(AudioChannelCount other) {
 			return (this.channels == other.channels);
 		}
 
 		public int getChannelCount() {
 			return this.channels;
 		}
 
 		@Override
 		public String toString() {
 			if (this.channels < 0) return "Original";
 			return Integer.valueOf(channels).toString();
 		}
 	}
 	public static final class AudioSampleRate {
 		private int sampleRate;
 
 		public AudioSampleRate(int sampleRate) {
 			this.sampleRate = sampleRate;
 		}
 
 		public boolean equals(AudioSampleRate other) {
 			return (this.sampleRate == other.sampleRate);
 		}
 
 		public int getSampleRate() {
 			return this.sampleRate;
 		}
 
 		@Override
 		public String toString() {
 			if (this.sampleRate < 0) return "Original";
 			return Videncode.intAddComas(this.sampleRate) + " Hz";
 		}
 	}
 	private final class InfoStats {
 		boolean hasAudio = false;
 		boolean hasVideo = false;
 	}
 	private static abstract class ProgressCallbackThread extends ManagedThread {
 		private static final Pattern progressPattern = Pattern.compile("^((\\w+)=(.+?)\r?\n)*((progress)=(.+?)\r?\n)");
 		private static final Pattern progressLine = Pattern.compile("(\\w+)=(.+?)\r?\n");
 		private static final Pattern cmdEscaped = Pattern.compile("[\\<\\>\\|\\*\\&\"'`~]");
 
 		private StringBuilder progressScanner = new StringBuilder();
 		private GeneratorProgressCallback progressCallback = null;
 		private double targetLength = 1.0;
 		private int maxFrames = 1;
 
 		protected ProgressCallbackThread(final ThreadManager threadManager, final GeneratorProgressCallback progressCallback, final double targetLength, final double frameRate) {
 			super(threadManager);
 
 			this.progressCallback = progressCallback;
 			this.targetLength = targetLength;
 			this.maxFrames = (int) Math.ceil(frameRate * this.targetLength);
 		}
 
 		public final boolean hasProgressCallback() {
 			return (this.progressCallback != null);
 		}
 		protected final void updateProgress(final String str) {
 			this.updateProgress(str, true, 1.0, 0.0);
 		}
 		protected final void updateProgress(final String str, boolean useTime, double progressFactor, double progressOffset) {
 			this.progressScanner.append(str);
 			this.onProgressStringUpdate(str);
 			this.progressCheck(useTime, progressFactor, progressOffset);
 		}
 		private final void progressCheck(boolean useTime, double progressFactor, double progressOffset) {
 			Matcher m = ProgressCallbackThread.progressPattern.matcher(this.progressScanner);
 			Matcher m2;
 			int start = 0, pos;
 			long time;
 			int flags;
 			int frame;
 			while (m.find(start)) {
 				// Parse
 				time = 0L;
 				frame = 0;
 				flags = 0;
 				m2 = ProgressCallbackThread.progressLine.matcher(m.group(0));
 				pos = start;
 				while (m2.find(pos)) {
 					if (m2.group(1).equals("out_time_ms")) {
 						time = Long.parseLong(m2.group(2));
 					}
 					else if (m2.group(1).equals("frame")) {
 						frame = Integer.parseInt(m2.group(2));
 					}
 					else if (m2.group(1).equals("progress")) {
 						if (m2.group(2).equals("end")) flags |= GeneratorProgressCallback.COMPLETE;
 					}
 					pos = m2.end();
 				}
 				// Event
 				this.triggerCallback(
 					useTime ? ((time / (this.targetLength * 1000000.0)) * progressFactor + progressOffset) : ((frame / ((double) maxFrames)) * progressFactor + progressOffset),
 					flags
 				);
 
 				// Next
 				start = m.end();
 			}
 			// Remove
 			this.progressScanner.delete(0, start);
 		}
 		protected final void triggerCallback(double progress, int flags) {
 			this.progressCallback.onProgress(
 				Math.min(Math.max(progress, 0.0), 1.0),
 				flags
 			);
 		}
 
 		protected void onProgressStringUpdate(final String str) {
 		}
 
 		protected final String paramArrayToString(final ArrayList<String> array) {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < array.size(); ++i) {
 				if (sb.length() > 0) sb.append(" ");
 				if (cmdEscaped.matcher(array.get(i)).find()) {
 					sb.append('"');
 					sb.append(array.get(i).replaceAll("\"", "\"'\"'\""));
 					sb.append('"');
 				}
 				else {
 					sb.append(array.get(i));
 				}
 			}
 			return sb.toString();
 		}
 	}
 	private static abstract class ScreenshotGenerator extends ProgressCallbackThread {
 		private File inputFile = null;
 		private File outputFile = null;
 		private File outputFileDir = null;
 		private double time = 0.0;
 		private int quality = 0;
 		private Boolean canceled = false;
 		private Dimension imageSize = new Dimension(0, 0);
 		private Component guiComponent = null;
 		private boolean copySource = false;
 		private Process infoProcess = null;
 
 		public ScreenshotGenerator(
 			final ThreadManager threadManager,
 			final Component guiComponent,
 			final File inputFile,
 			final String outputFilename,
 			final File outputDirectory,
 			boolean copyableSource,
 			final Dimension imageSize,
 			int quality,
 			double time
 		) {
 			super(threadManager, null, 1.0, 0);
 
 			String ext;
 			if (copyableSource && quality == -1) {
 				ext = Videncode.getFileExt(inputFile);
 				if (ext.equals("")) ext = ".png";
 				this.copySource = true;
 			}
 			else {
 				ext = (quality <= 0 ? ".png" : ".jpg");
 				if (quality < ((Videncode) threadManager).getImageQualityRange()[0]) {
 					quality = ((Videncode) threadManager).getImageQualityRange()[0];
 				}
 				else if (quality > ((Videncode) threadManager).getImageQualityRange()[1]) {
 					quality = ((Videncode) threadManager).getImageQualityRange()[1];
 				}
 				this.copySource = false;
 			}
 
 			this.outputFileDir = outputDirectory;
 			this.outputFile = new File(outputDirectory, (outputFilename + ext));
 			for (int i = 1; this.outputFile.exists(); ++i) {
 				this.outputFile = new File(outputDirectory, (outputFilename + "[" + i + "]" + ext));
 			}
 
 			this.inputFile = inputFile;
 			this.time = time;
 			this.quality = quality;
 			this.guiComponent = guiComponent;
 
 			this.imageSize.width = imageSize.width;
 			this.imageSize.height = imageSize.height;
 		}
 
 		protected abstract void onScreenshotGet(File f, Image img);
 		protected void onInfoUpdate(final String str) {
 		}
 		protected void onCommandGenerate(final String str) {
 		}
 
 		public final void cancel() {
 			synchronized (this.canceled) {
 				if (!this.canceled) {
 					this.canceled = true;
 					if (this.infoProcess != null) {
 						this.infoProcess.destroy();
 						try {
 							this.infoProcess.waitFor();
 						}
 						catch (InterruptedException e) {}
 						this.infoProcess = null;
 					}
 					this.outputFile.delete();
 				}
 			}
 		}
 		public final boolean isCanceled() {
 			boolean b;
 			synchronized (this.canceled) {
 				b = this.canceled;
 			}
 			return b;
 		}
 		@Override
 		public void stopRunning() {
 			this.cancel();
 		}
 
 		public final void execute() {
 			final ScreenshotGenerator self = this;
 
 			// Copy source
 			if (this.copySource) {
 				this.onCommandGenerate(this.paramArrayToString(new ArrayList<String>(Arrays.asList(new String[]{
 					"copy", this.inputFile.getAbsolutePath(), this.outputFile.getAbsolutePath()
 				}))));
 				try {
 					Videncode.copyFile(this.inputFile, this.outputFile);
 				}
 				catch (IOException e) {
 					// Bad file
 					this.completed(null);
 					return;
 				}
 
 				// Use file
 				this.completed(this.outputFile);
 				return;
 			}
 
 			// No image
 			try {
 				// Process params
 				ArrayList<String> params = new ArrayList<String>(Arrays.asList(new String[]{
 					"ffmpeg",
 					"-y",
 					"-v", "info",
 					"-ss", Videncode.timeToString(this.time),
 					"-i", this.inputFile.getAbsolutePath()
 				}));
 
 				if (this.quality <= 0) {
 					params.addAll(Arrays.asList(new String[]{ "-vcodec", "png" }));
 				}
 				else {
 					params.addAll(Arrays.asList(new String[]{ "-vcodec", "mjpeg", "-q:v" }));
 					params.add(Integer.valueOf(this.quality).toString());
 				}
 
 				params.addAll(Arrays.asList(new String[]{
 					"-vframes", "1",
 					"-map_metadata", "-1",
 					"-an",
 					"-f", "rawvideo"
 				}));
 				if (this.imageSize.width > 0 && this.imageSize.height > 0) {
 					params.addAll(Arrays.asList(new String[]{
 						"-s", this.imageSize.width + "x" + this.imageSize.height
 					}));
 				}
 				params.add(this.outputFile.getAbsolutePath());
 
 				this.onCommandGenerate(this.paramArrayToString(params));
 				this.infoProcess = Runtime.getRuntime().exec(params.toArray(new String[params.size()]), null, this.outputFileDir);
 			}
 			catch (IOException e) {
 				this.infoProcess = null;
 			}
 
 			// Done
 			if (this.infoProcess == null) {
 				this.completed(null);
 			}
 			else {
 				// Handle stderr
 				final Process p = this.infoProcess;
 				Thread t = new Thread() {
 					public void run() {
 						byte[] buffer = new byte[128];
 						int length;
 						try {
 							while ((length = p.getErrorStream().read(buffer)) >= 0) {
 								self.onInfoUpdate(new String(buffer, 0, length));
 							}
 						}
 						catch (IOException e) {
 						}
 					}
 				};
 				t.start();
 
 				// Handle stdout
 				byte[] buffer = new byte[128];
 				try {
 					while (this.infoProcess.getInputStream().read(buffer) >= 0);
 				}
 				catch (IOException e) {
 				}
 
 				// Wait for completion
 				try {
 					t.join();
 					p.waitFor();
 				}
 				catch (InterruptedException e) {}
 
 				// Done
 				this.completed(this.outputFile);
 			}
 		}
 
 		private final void completed(File f) {
 			if (!this.isCanceled()) {
 				Image img = (f == null ? null : Videncode.createImageFromFile(f, this.guiComponent));
 
 				if (!this.isCanceled()) {
 					this.onScreenshotGet(f, img);
 
 					// Done; can "cancel"
 					synchronized (this.canceled) {
 						this.canceled = true;
 					}
 				}
 			}
 		}
 	}
 	private static abstract class AudioClipGenerator extends ProgressCallbackThread {
 
 		private File inputFile = null;
 		private File outputFile = null;
 		private File outputFileDir = null;
 		private File wavFile = null;
 		private double start = 0.0;
 		private double length = 0.0;
 		private Bitrate bitrate = null;
 		private Boolean canceled = false;
 		private AudioChannelCount channelCount = null;
 		private AudioSampleRate sampleRate = null;
 		private Process infoProcess = null;
 
 		public AudioClipGenerator(
 			final ThreadManager threadManager,
 			final File inputFile,
 			final String outputFilename,
 			final File outputDirectory,
 			double start,
 			double length,
 			final Bitrate bitrate,
 			final AudioChannelCount channelCount,
 			final AudioSampleRate sampleRate,
 			GeneratorProgressCallback callback,
 			boolean genSound
 		) {
 			super(threadManager, callback, length, 0);
 
 			String ext = ".ogg";
 
 			this.outputFileDir = outputDirectory;
 			this.outputFile = new File(outputDirectory, (outputFilename + ext));
 			for (int i = 1; this.outputFile.exists(); ++i) {
 				this.outputFile = new File(outputDirectory, (outputFilename + "[" + i + "]" + ext));
 			}
 
 			if (genSound) {
 				ext = ".wav";
 
 				this.wavFile = new File(outputDirectory, (outputFilename + ext));
 				for (int i = 1; this.wavFile.exists(); ++i) {
 					this.wavFile = new File(outputDirectory, (outputFilename + "[" + i + "]" + ext));
 				}
 			}
 
 			this.inputFile = inputFile;
 			this.start = start;
 			this.length = length;
 			this.bitrate = bitrate;
 			this.channelCount = channelCount;
 			this.sampleRate = sampleRate;
 		}
 
 		protected abstract void onAudioGet(File f, Sound snd);
 		protected void onInfoUpdate(final String str) {
 		}
 		protected void onCommandGenerate(final String str) {
 		}
 
 		public final void cancel() {
 			synchronized (this.canceled) {
 				this.canceled = true;
 				if (this.infoProcess != null) {
 					this.infoProcess.destroy();
 					try {
 						this.infoProcess.waitFor();
 					}
 					catch (InterruptedException e) {}
 					this.infoProcess = null;
 				}
 				this.outputFile.delete();
 				if (this.wavFile != null) this.wavFile.delete();
 			}
 		}
 		public final boolean isCanceled() {
 			boolean b;
 			synchronized (this.canceled) {
 				b = this.canceled;
 			}
 			return b;
 		}
 		@Override
 		public void stopRunning() {
 			this.cancel();
 		}
 
 		public final void execute() {
 			final AudioClipGenerator self = this;
 			int passMax = (this.wavFile == null ? 1 : 2);
 
 			for (int pass = 0; pass < passMax; ++pass) {
 				// Start process
 				try {
 					// Process params
 					ArrayList<String> params = new ArrayList<String>(Arrays.asList(new String[]{
 						"ffmpeg"
 					}));
 					if (this.hasProgressCallback()) {
 						params.addAll(Arrays.asList(new String[]{ "-progress", "pipe:1" }));
 					}
 					params.addAll(Arrays.asList(new String[]{
 						"-y",
						"-v", "info",
 						"-i", (pass == 0 ? this.inputFile.getAbsolutePath() : this.outputFile.getAbsolutePath()),
 						"-map_metadata", "-1",
 						"-vn",
 					}));
 
 					if (pass == 0) {
 						params.addAll(Arrays.asList(new String[]{
 							"-codec:a", "libvorbis",
 							"-ss", Videncode.timeToString(this.start),
 							"-t", Videncode.timeToString(this.length),
 							"-b:a", Integer.valueOf(this.bitrate.getBitrate()).toString()
 						}));
 
 						if (this.sampleRate.getSampleRate() > 0) {
 							params.addAll(Arrays.asList(new String[]{ "-ar" , Integer.valueOf(this.sampleRate.getSampleRate()).toString() }));
 						}
 						if (this.channelCount.getChannelCount() > 0) {
							params.addAll(Arrays.asList(new String[]{ "-ac" , Integer.valueOf(this.channelCount.getChannelCount()).toString() }));
 						}
 
 						params.addAll(Arrays.asList(new String[]{
 							"-f", "ogg", this.outputFile.getAbsolutePath(),
 						}));
 					}
 					else {
 						params.addAll(Arrays.asList(new String[]{
 							"-codec:a", "pcm_s16le",
 							"-f", "wav", this.wavFile.getAbsolutePath(),
 						}));
 					}
 
 					this.onCommandGenerate(this.paramArrayToString(params));
 					this.infoProcess = Runtime.getRuntime().exec(params.toArray(new String[params.size()]), null, this.outputFileDir);
 				}
 				catch (IOException e) {
 					this.infoProcess = null;
 				}
 
 				// Done
 				if (this.infoProcess == null) {
 					this.completed(null);
 					return;
 				}
 				else {
 					// Initial progress
 					if (this.hasProgressCallback()) {
 						this.triggerCallback(0.0, 0);
 					}
 
 					// Handle stderr
 					final Process p = this.infoProcess;
 					Thread t = new Thread() {
 						public void run() {
 							byte[] buffer = new byte[128];
 							int length;
 							try {
 								while ((length = p.getErrorStream().read(buffer)) >= 0) {
 									self.onInfoUpdate(new String(buffer, 0, length));
 								}
 							}
 							catch (IOException e) {
 							}
 						}
 					};
 					t.start();
 
 					// Handle stdout
 					byte[] buffer = new byte[128];
 					if (this.hasProgressCallback()) {
 						int length;
 						try {
 							while ((length = this.infoProcess.getInputStream().read(buffer)) >= 0) {
 								this.updateProgress(new String(buffer, 0, length));
 							}
 						}
 						catch (IOException e) {
 						}
 					}
 					else {
 						try {
 							while (this.infoProcess.getInputStream().read(buffer) >= 0);
 						}
 						catch (IOException e) {
 						}
 					}
 
 					// Wait for completion
 					try {
 						t.join();
 						p.waitFor();
 					}
 					catch (InterruptedException e) {}
 
 				}
 			}
 			// Done
 			if (passMax == 2) {
 				this.outputFile.delete();
 			}
 			this.completed(passMax == 2 ? this.wavFile : this.outputFile);
 		}
 
 		private final void completed(File f) {
 			if (!this.isCanceled()) {
 				Sound snd = (this.wavFile == null ? null : (f == null ? null : this.createSound(f)));
 
 				if (!this.isCanceled()) {
 					this.onAudioGet(f, snd);
 
 					// Done; can "cancel"
 					synchronized (this.canceled) {
 						this.canceled = true;
 					}
 				}
 			}
 		}
 
 		private final Sound createSound(final File f) {
 			return new Sound(f);
 		}
 	}
 	private static abstract class VideoGenerator extends ProgressCallbackThread {
 		private File inputFile = null;
 		private File outputFile = null;
 		private File outputFileDir = null;
 		private double start = 0.0;
 		private double length = 0.0;
 		private Bitrate bitrate = null;
 		private Boolean canceled = false;
 		private Dimension videoSize = new Dimension(0, 0);
 		private Process infoProcess = null;
 		private FrameRate framerate = null;
 		private int threads = 1;
 		private int quality = 0;
 		private int qualityDecrease = 0;
 		private int qualityIncrease = 0;
 		private long maxFileSize = 0;
 		private boolean reEncodePossible = false;
 		private boolean stopped = false;
 
 		public VideoGenerator(
 			final ThreadManager threadManager,
 			final File inputFile,
 			final String outputFilename,
 			final File outputDirectory,
 			double start,
 			double length,
 			final Bitrate bitrate,
 			final Dimension videoSize,
 			final FrameRate framerate,
 			final GeneratorProgressCallback callback,
 			int threads,
 			int quality,
 			long maxFileSize,
 			boolean reEncodePossible
 		) {
 			super(threadManager, callback, length, framerate.getFrameRate());
 
 			String ext = ".webm";
 
 			this.outputFileDir = outputDirectory;
 			this.outputFile = new File(outputDirectory, (outputFilename + ext));
 			for (int i = 1; this.outputFile.exists(); ++i) {
 				this.outputFile = new File(outputDirectory, (outputFilename + "[" + i + "]" + ext));
 			}
 
 			this.inputFile = inputFile;
 			this.start = start;
 			this.length = length;
 			this.bitrate = bitrate;
 			this.videoSize.width = videoSize.width;
 			this.videoSize.height = videoSize.height;
 			this.framerate = framerate;
 			this.threads = threads;
 			this.quality = quality;
 			this.maxFileSize = maxFileSize;
 			this.reEncodePossible = reEncodePossible;
 		}
 
 		protected abstract void onVideoGet(File f);
 		protected void onInfoUpdate(final String str) {
 		}
 		protected void onCommandGenerate(final String str) {
 		}
 
 		public final void cancel() {
 			synchronized (this.canceled) {
 				this.canceled = true;
 				if (this.infoProcess != null) {
 					this.infoProcess.destroy();
 					try {
 						this.infoProcess.waitFor();
 					}
 					catch (InterruptedException e) {}
 					this.infoProcess = null;
 				}
 				this.outputFile.delete();
 			}
 		}
 		public final boolean isCanceled() {
 			boolean b;
 			synchronized (this.canceled) {
 				b = this.canceled;
 			}
 			return b;
 		}
 		@Override
 		public void stopRunning() {
 			this.cancel();
 		}
 
 		public final void execute() {
 			final VideoGenerator self = this;
 
 			boolean qualityLoop = true;
 			int quality = this.quality;
 			while (qualityLoop) {
 				qualityLoop = false;
 				int passMax = (quality <= 1 ? 2 : 1);
 
 				for (int pass = 0; pass < passMax; ++pass) {
 					if (this.isCanceled()) {
 						this.completed(null);
 						return;
 					}
 
 					// Start process
 					try {
 						// Process params
 						ArrayList<String> params = new ArrayList<String>(Arrays.asList(new String[]{
 							"ffmpeg"
 						}));
 						if (this.hasProgressCallback()) {
 							params.addAll(Arrays.asList(new String[]{ "-progress", "pipe:1" }));
 						}
 						params.addAll(Arrays.asList(new String[]{
 							"-y",
 							"-v", "info",
 							"-i", this.inputFile.getAbsolutePath(),
 							"-an",
 							"-map_metadata", "-1",
 							"-codec:v", "libvpx",
							"-ss", Videncode.timeToString(this.start),
 							"-t", Videncode.timeToString(this.length),
 							"-r", Double.valueOf(this.framerate.getFrameRate()).toString(),
 							"-s", Integer.valueOf(this.videoSize.width).toString() + "x" + Integer.valueOf(this.videoSize.height).toString(),
 							"-cpu-used", "0",
 							"-threads", Integer.valueOf(this.threads).toString()
 						}));
 						if (quality <= 0) { // best
 							params.addAll(Arrays.asList(new String[]{
 								"-maxrate", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-b:v", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-bufsize", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								//"-crf", "10"
 							}));
 						}
 						if (quality == 1) { // okay
 							params.addAll(Arrays.asList(new String[]{
 								"-maxrate", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-b:v", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-bufsize", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-crf", "10"
 							}));
 						}
 						if (quality >= 2) { // fast
 							params.addAll(Arrays.asList(new String[]{
 								"-maxrate", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-minrate", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-b:v", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 								"-bufsize", Integer.valueOf(this.bitrate.getBitrate()).toString(),
 							}));
 						}
 
 						if (passMax > 1) {
 							params.addAll(Arrays.asList(new String[]{
 								"-pass", Integer.valueOf(pass + 1).toString()
 							}));
 						}
 						params.addAll(Arrays.asList(new String[]{
 							"-f", "webm", (pass + 1 == passMax ? this.outputFile.getAbsolutePath() : Videncode.getNullStreamName())
 						}));
 
 						this.onCommandGenerate(this.paramArrayToString(params));
 						this.infoProcess = Runtime.getRuntime().exec(params.toArray(new String[params.size()]), null, this.outputFileDir);
 					}
 					catch (IOException e) {
 						this.infoProcess = null;
 					}
 
 					// Done
 					if (this.infoProcess == null) {
 						this.completed(null);
 						return;
 					}
 					else {
 						// Initial progress
 						if (this.hasProgressCallback()) {
 							this.triggerCallback(pass / (double) passMax, 0);
 						}
 
 						// Handle stderr
 						final Process p = this.infoProcess;
 						Thread t = new Thread() {
 							public void run() {
 								byte[] buffer = new byte[128];
 								int length;
 								try {
 									while ((length = p.getErrorStream().read(buffer)) >= 0) {
 										self.onInfoUpdate(new String(buffer, 0, length));
 									}
 								}
 								catch (IOException e) {
 								}
 							}
 						};
 						t.start();
 
 						// Handle stdout
 						byte[] buffer = new byte[128];
 						if (this.hasProgressCallback()) {
 							int length;
 							try {
 								while ((length = this.infoProcess.getInputStream().read(buffer)) >= 0) {
 									this.updateProgress(new String(buffer, 0, length), (pass + 1 == passMax), 1.0 / passMax, pass / (double) passMax);
 								}
 							}
 							catch (IOException e) {
 							}
 						}
 						else {
 							try {
 								while (this.infoProcess.getInputStream().read(buffer) >= 0);
 							}
 							catch (IOException e) {
 							}
 						}
 
 						// Wait for completion
 						try {
 							t.join();
 							p.waitFor();
 						}
 						catch (InterruptedException e) {}
 
 					}
 				}
 
 				if (this.outputFile.length() > this.maxFileSize) {
 					if (this.reEncodePossible) {
 						this.reEncodePossible = false;
 						qualityLoop = true;
 					}
 					else if (quality == 0) {
 						++quality;
 						qualityLoop = true;
 					}
 				}
 			}
 			// Done
 			this.completed(this.outputFile);
 		}
 
 		private final void completed(File f) {
 			if (!this.isCanceled()) {
 				this.onVideoGet(f);
 
 				// Done; can "cancel"
 				synchronized (this.canceled) {
 					this.canceled = true;
 				}
 			}
 		}
 	}
 	private static abstract class MuxGenerator extends ProgressCallbackThread {
 		private Boolean canceled = false;
 		private Process infoProcess = null;
 
 		private File tempOutputFile = null;
 		private File tempOutputFileDir = null;
 		private File finalOutputFile = null;
 
 		private File imageFile = null;
 		private File audioFile = null;
 		private File videoFile = null;
 		private byte[] tag = null;
 		private boolean muxAudioAndVideo = true;
 		private boolean videoIsLonger = false;
 		private double syncOffset = 0.0;
 		private int flags1 = 0;
 		private int flags2 = 0;
 
 		private boolean maskFile = true;
 		private int mask = 0x12;
 		private int maskValue = 0xABCDEF;
 
 		public MuxGenerator(
 			final ThreadManager threadManager,
 			final File tempOutputDirectory,
 			final File finalOutputDirectory,
 			final File imageFile,
 			final File audioFile,
 			final File videoFile,
 			final String tempOutputName,
 			final String finalOutputName,
 			final String finalOutputNameExt,
 			final byte[] tag,
 			final int tagLength,
 			final boolean muxAudioAndVideo,
 			final boolean videoIsLonger,
 			final double syncOffset,
 			final int flags1,
 			final int flags2
 		) {
 			super(threadManager, null, 1, 1);
 
 			// Mux'd audio+video file
 			String ext = ".webm";
 			this.tempOutputFileDir = tempOutputDirectory;
 			this.tempOutputFile = new File(tempOutputDirectory, (tempOutputName + ext));
 			for (int i = 1; this.tempOutputFile.exists(); ++i) {
 				this.tempOutputFile = new File(tempOutputDirectory, (tempOutputName + "[" + i + "]" + ext));
 			}
 
 			// Output file
 			ext = finalOutputNameExt;
 			this.finalOutputFile = new File(finalOutputDirectory, (finalOutputName + ext));
 			for (int i = 1; this.finalOutputFile.exists(); ++i) {
 				this.finalOutputFile = new File(finalOutputDirectory, (finalOutputName + "[" + i + "]" + ext));
 			}
 
 			// Values
 			this.imageFile = imageFile;
 			this.audioFile = audioFile;
 			this.videoFile = videoFile;
 			this.tag = new byte[tagLength];
 			for (int i = 0; i < tagLength; ++i) this.tag[i] = tag[i];
 			this.muxAudioAndVideo = muxAudioAndVideo;
 			this.videoIsLonger = videoIsLonger;
 			this.syncOffset = syncOffset;
 			this.flags1 = flags1;
 			this.flags2 = flags2;
 		}
 
 		protected abstract void onMuxGet(File f);
 		protected void onInfoUpdate(final String str) {
 		}
 		protected void onCommandGenerate(final String str) {
 		}
 
 		public final void cancel() {
 			synchronized (this.canceled) {
 				this.canceled = true;
 				if (this.infoProcess != null) {
 					this.infoProcess.destroy();
 					try {
 						this.infoProcess.waitFor();
 					}
 					catch (InterruptedException e) {}
 					this.infoProcess = null;
 				}
 				this.tempOutputFile.delete();
 			}
 		}
 		public final boolean isCanceled() {
 			boolean b;
 			synchronized (this.canceled) {
 				b = this.canceled;
 			}
 			return b;
 		}
 		@Override
 		public void stopRunning() {
 			this.cancel();
 		}
 
 		private final void completed(File f) {
 			if (!this.isCanceled()) {
 				this.onMuxGet(f);
 
 				// Done; can "cancel"
 				synchronized (this.canceled) {
 					this.canceled = true;
 				}
 			}
 		}
 
 		public final void execute() {
 			final MuxGenerator self = this;
 			boolean error = false;
 
 			if (this.muxAudioAndVideo) {
 				try {
 					// Process params
 					ArrayList<String> params = new ArrayList<String>(Arrays.asList(new String[]{
 						"ffmpeg",
 						"-y",
 						"-v", "info",
 						"-i", this.audioFile.getAbsolutePath(),
 						"-i", this.videoFile.getAbsolutePath(),
 						"-map_metadata", "-1",
 						"-codec:a", "copy",
 						"-codec:v", "copy",
 						"-f", "webm", this.tempOutputFile.getAbsolutePath()
 					}));
 
 					this.onCommandGenerate(this.paramArrayToString(params));
 					this.infoProcess = Runtime.getRuntime().exec(params.toArray(new String[params.size()]), null, this.tempOutputFileDir);
 				}
 				catch (IOException e) {
 					this.infoProcess = null;
 				}
 
 				// Done
 				if (this.infoProcess == null) {
 					this.completed(null);
 					error = true;
 				}
 				else {
 					// Handle stderr
 					final Process p = this.infoProcess;
 					Thread t = new Thread() {
 						public void run() {
 							byte[] buffer = new byte[128];
 							int length;
 							try {
 								while ((length = p.getErrorStream().read(buffer)) >= 0) {
 									self.onInfoUpdate(new String(buffer, 0, length));
 								}
 							}
 							catch (IOException e) {
 							}
 						}
 					};
 					t.start();
 
 					// Handle stdout
 					byte[] buffer = new byte[128];
 					try {
 						while (this.infoProcess.getInputStream().read(buffer) >= 0);
 					}
 					catch (IOException e) {
 					}
 
 					// Wait for completion
 					try {
 						t.join();
 						p.waitFor();
 					}
 					catch (InterruptedException e) {}
 				}
 
 			}
 
 			if (!error) {
 				this.encode();
 			}
 		}
 
 
 		private final void encode() {
 			try {
 				int length;
 				byte[] buffer = new byte[256];
 				byte[] temp;
 
 				// Open output file
 				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.finalOutputFile));
 
 				// Open image and copy
 				BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.imageFile));
 				while ((length = in.read(buffer)) >= 0) {
 					this.maskModifyFromBytes(buffer, length);
 					out.write(buffer, 0, length);
 				}
 				in.close();
 
 				// Signature
 				temp = new byte[]{ '.' , 'v' , 'e' , '.' , 's' , 'n' , 'd' , '\0' };
 				this.maskBytes(temp, temp.length);
 				out.write(temp);
 
 				// Version
 				temp = new byte[]{ 1 };
 				this.maskBytes(temp, temp.length);
 				out.write(temp);
 
 				// Flags1
 				temp[0] = (byte) this.flags1;
 				this.maskBytes(temp, temp.length);
 				out.write(temp);
 
 				// Flags2
 				if ((this.flags1 & 0x03) == 0x03) {
 					temp[0] = (byte) this.flags2;
 					this.maskBytes(temp, temp.length);
 					out.write(temp);
 				}
 
 				// Tag length
 				temp = new byte[5];
 				length = this.intToVarLengthBytes(this.tag.length, temp);
 				this.maskBytes(temp, length);
 				out.write(temp, 0, length);
 
 				// Tag
 				temp = new byte[this.tag.length];
 				for (int i = 0; i < this.tag.length; ++i) temp[i] = this.tag[i];
 				this.maskBytes(temp, temp.length);
 				out.write(temp);
 
 				// Sync offset
 				if ((this.flags1 & 0x03) == 0x03) {
 					double v = this.syncOffset;
 					int intPart = (int) v;
 					v -= intPart;
 
 					// Length
 					temp = new byte[5];
 					length = this.intToVarLengthBytes(intPart, temp);
 					this.maskBytes(temp, length);
 					out.write(temp, 0, length);
 
 					// Decimal part
 					temp = new byte[2];
 					for (int i = 0; i < temp.length * 8; ++i) {
 						if ((v *= 2) >= 1.0) {
 							v -= 1.0;
 							temp[i / 8] |= (1 << (i % 8));
 						}
 					}
 					this.maskBytes(temp, temp.length);
 					out.write(temp, 0, temp.length);
 				}
 
 				// Video file
 				if (this.videoFile != null) {
 					File f = (this.muxAudioAndVideo ? this.tempOutputFile : this.videoFile);
 
 					// Video size
 					temp = new byte[5];
 					length = this.intToVarLengthBytes((int) f.length(), temp);
 					this.maskBytes(temp, length);
 					out.write(temp, 0, length);
 
 					// Open video and copy
 					in = new BufferedInputStream(new FileInputStream(f));
 					while ((length = in.read(buffer)) >= 0) {
 						this.maskBytes(buffer, length);
 						out.write(buffer, 0, length);
 					}
 					in.close();
 				}
 
 				// Audio file
 				if (this.audioFile != null && !this.muxAudioAndVideo) {
 					File f = this.audioFile;
 
 					// Video size
 					temp = new byte[5];
 					length = this.intToVarLengthBytes((int) f.length(), temp);
 					this.maskBytes(temp, length);
 					out.write(temp, 0, length);
 
 					// Open video and copy
 					in = new BufferedInputStream(new FileInputStream(f));
 					while ((length = in.read(buffer)) >= 0) {
 						this.maskBytes(buffer, length);
 						out.write(buffer, 0, length);
 					}
 					in.close();
 				}
 
 				// Done
 				out.close();
 			}
 			catch (IOException e) {
 				this.completed(null);
 				return;
 			}
 
 			// Done
 			this.completed(this.finalOutputFile);
 		}
 		private final void maskBytes(byte[] b, int length) {
 			if (!this.maskFile) return;
 
 			int j;
 			for (int i = 0; i < length; ++i) {
 				this.maskValue = (int) (this.maskValue * 102293L + 390843L);
 				this.mask = this.maskValue >>> 24;
 				this.maskValue += (j = (((int) b[i]) & 0xFF));
 				b[i] = (byte) (j ^ this.mask);
 			}
 		}
 		private final void maskModifyFromBytes(final byte[] b, int length) {
 			if (!this.maskFile) return;
 
 			for (int i = 0; i < length; ++i) {
 				this.maskValue = (int) (this.maskValue * 102293L + 390843L);
 				this.mask = this.maskValue >>> 24;
 				this.maskValue += ((((int) b[i]) & 0xFF) ^ this.mask);
 			}
 		}
 		private final int intToVarLengthBytes(int value, byte[] bytes) {
 			int i = 0;
 			for (; i < bytes.length; ++i) {
 				if (i > 0) bytes[i - 1] |= 0x80;
 
 				bytes[i] = (byte) (value & 0x7F);
 				value = value >>> 7;
 				if (value == 0) return i + 1;
 			}
 			return i;
 		}
 	}
 	private static abstract class GeneratorProgressCallback {
 		public static final int COMPLETE = 1;
 
 		public GeneratorProgressCallback() {
 		}
 
 		public abstract void onProgress(double progress, int flags);
 	}
 
 
 	// Static data
 	private static final Pattern timecodePattern = Pattern.compile("\\s*(([0-9]+)\\s*[Hh:])?(([0-9]+)\\s*[Mm:])?(([\\.eE0-9]+)\\s*[sS]?)?\\s*");
 
 	private static final String[] defaultTimecodeDelimiters = new String[]{":",":","","."};
 	private static final int[] defaultTimecodeLengths = new int[]{ 2 , 2 , 2 , 2 };
 
 	private static final double DECIMAL_THRESHOLD = 0.001;
 
 	private static final int PREVIEW_FIRST = 0;
 	private static final int PREVIEW_LAST = 1;
 
 	public static final int SYNC_NOTHING = 0;
 	public static final int SYNC_LOOP = 1;
 	public static final int SYNC_PREVIEW = 2;
 	public static final int SYNC_EXTERNAL = 3;
 
 
 	// Settings data
 	private final Bitrate[] audioBitrates = new Bitrate[]{
 		new Bitrate(45),
 		new Bitrate(64),
 		new Bitrate(80),
 		new Bitrate(96),
 		new Bitrate(112),
 		new Bitrate(128),
 		new Bitrate(160),
 		new Bitrate(192),
 		new Bitrate(224),
 		new Bitrate(256),
 		new Bitrate(320),
 		new Bitrate(500)
 	};
 	private final AudioChannelCount[] audioChannels = new AudioChannelCount[]{
 		new AudioChannelCount(-1),
 		new AudioChannelCount(2),
 		new AudioChannelCount(1)
 	};
 	private final AudioSampleRate[] audioSampleRates = new AudioSampleRate[]{
 		//new AudioSampleRate(8000),
 		new AudioSampleRate(11025),
 		new AudioSampleRate(16000),
 		new AudioSampleRate(22050),
 		new AudioSampleRate(32000),
 		new AudioSampleRate(44100),
 		new AudioSampleRate(48000),
 		new AudioSampleRate(-1),
 	};
 	private Bitrate audioBitrateDefault = this.audioBitrates[2];
 	private AudioChannelCount audioChannelsDefault = this.audioChannels[0];
 	private AudioSampleRate audioSampleRateDefault = this.audioSampleRates[this.audioSampleRates.length - 1];
 
 	private final int[] resolutions = new int[]{ 60 , 120 , 240 , 270 , 360 , 480 , 576 , 720 , 1080 };
 	private final int[] imageQualityRange = new int[]{ 0 , 31 };
 	private int imageQualityDefault = 1;
 
 	private final AutoQualityProfile[] videoAutoQualityProfiles = new AutoQualityProfile[]{
 		new AutoQualityProfile(0.005, "Best"),
 		new AutoQualityProfile(0.00325, "Better"),
 		new AutoQualityProfile(0.002, "Good"),
 		new AutoQualityProfile(0.0015, "Average"),
 		new AutoQualityProfile(0.001, "Okay"),
 		new AutoQualityProfile(0.0005, "Bad"),
 		new AutoQualityProfile(0.0002, "Very Bad"),
 //		new AutoQualityProfile(0.0, "Disabled"),
 	};
 	private final FrameRate[] videoFrameRates = new FrameRate[]{
 		new FrameRate(1),
 		new FrameRate(4),
 		new FrameRate(5),
 		new FrameRate(6),
 		new FrameRate(8),
 		new FrameRate(10),
 		new FrameRate(12),
 		new FrameRate(15 * 1000, 1001),
 		new FrameRate(15),
 		new FrameRate(20),
 		new FrameRate(24 * 1000, 1001),
 		new FrameRate(24),
 		new FrameRate(25 * 1000, 1001),
 		new FrameRate(25),
 		new FrameRate(30 * 1000, 1001),
 		new FrameRate(30),
 		new FrameRate(30 * 1000, 1001),
 		new FrameRate(30),
 		new FrameRate(48 * 1000, 1001),
 		new FrameRate(48),
 		new FrameRate(60 * 1000, 1001),
 		new FrameRate(60),
 	};
 	private AutoQualityProfile videoAutoQualityProfileDefault = this.videoAutoQualityProfiles[3];
 
 	private final String[] videoEncodingProfiles = new String[]{ "Best" , "Quick & Good" , "Fastest" };
 	private final String[] videoEncodingProfileDescriptions = new String[]{
 		"Attempt to get the best possible encoding (may take more time)",
 		"Encode without trying to get the best video, but should still be pretty good",
 		"Encode the fastest (may sacrifice some quality)"
 	};
 	private int videoEncodingProfileDefault = 1;
 
 
 	// Data
 	private String tempDirName = "temp";
 	private File tempDir = new File("");
 	private Component guiComponent = null;
 
 
 	// Output settings
 	private Integer outputLock = new Integer(-1);
 
 	private long outputMaxFileSize = 1024 * 1024 * 3;
 	private int outputTagMaxLength = 100;
 	private int outputMaxThreads = Runtime.getRuntime().availableProcessors();
 	private String outputTag = "";
 	private byte[] outputTagBytes = new byte[0];
 	private String outputFilename = "";
 	private String outputExtensionDefault = ".ve*";
 	private String outputExtension = this.outputExtensionDefault;
 	private boolean outputEncoding = false;
 	private File outputFileLast = null;
 	private boolean outputIsMuxing = false;
 	private MuxGenerator outputGenerator = null;
 	private int outputAdditionalMuxSpace = 30000;
 
 	// Source data
 	private Integer videoFileSourceLock = new Integer(0);
 	private Integer imageFileSourceLock = new Integer(1);
 	private Integer audioFileSourceLock = new Integer(2);
 
 	private File videoFileSource = null;
 	private File imageFileSource = null;
 	private File audioFileSource = null;
 
 	private JSON.Node imageFileSourceInfoNode = null;
 	private Dimension imageFileSourceDimensions = new Dimension(0, 0);
 	private double imageFileSourceVideoTime = 0.0;
 
 	private JSON.Node videoFileSourceInfoNode = null;
 	private Dimension videoFileSourceDimensions = new Dimension(0, 0);
 	private FrameRate videoFileSourceFrameRate = new FrameRate(0.0);
 	private double videoFileSourceDuration = 0.0;
 	private double[] videoFileSourceEncodeDuration = new double[]{ 0.0 , 0.0 };
 	private double videoFileSourceSyncOffset = 0.0;
 
 	private JSON.Node audioFileSourceInfoNode = null;
 	private double audioFileSourceDuration = 0.0;
 	private double[] audioFileSourceEncodeDuration = new double[]{ 0.0 , 0.0 };
 	private double audioFileSourceSyncOffset = 0.0;
 
 
 	// Temp data
 	private Integer videoFileTempLock = new Integer(3);
 	private Integer videoPreviewFilesTempLock = new Integer(4);
 	private Integer imageFileTempLock = new Integer(5);
 	private Integer audioFileTempLock = new Integer(6);
 	private Integer audioPreviewFilesTempLock = new Integer(6);
 
 	private File videoFileTemp = null;
 	private File[] videoPreviewFilesTemp = new File[]{ null , null };
 	private File imageFileTemp = null;
 	private File audioFileTemp = null;
 	private File[] audioPreviewFilesTemp = new File[]{ null , null };
 
 	private ScreenshotGenerator[] videoPreviewTempScreenshotGenerators = new ScreenshotGenerator[]{ null , null };
 	private Image[] videoPreviewTempImages = new Image[]{ null , null };
 
 	private Dimension videoFileTempDimensions = new Dimension(0, 0);
 	private FrameRate videoFileTempFrameRate = new FrameRate(0.0);
 	private boolean videoFileTempIsGenerating = false;
 	private double videoFileTempGeneratingPercent = 0.0;
 	private VideoGenerator videoFileTempGenerator = null;
 	private int videoFileTempEncodingThreads = this.outputMaxThreads;
 	private boolean videoFileTempMightNeedReEncoding = false;
 	private int videoFileTempQuality = this.videoEncodingProfileDefault;
 
 	private Dimension imageFileTempDimensions = new Dimension(0, 0);
 	private ScreenshotGenerator imageFileTempScreenshotGenerator = null;
 	private Image imageFileImageTemp = null;
 	private int imageFileTempQuality = this.imageQualityDefault;
 
 	private Bitrate audioFileTempBitrate = this.audioBitrateDefault;
 	private AudioChannelCount audioFileTempChannels = this.audioChannelsDefault;
 	private AudioSampleRate audioFileTempSampleRate = this.audioSampleRateDefault;
 	private boolean audioFileTempIsGenerating = false;
 	private double audioFileTempGeneratingPercent = 0.0;
 	private AudioClipGenerator audioFileTempGenerator = null;
 
 	private Sound[] audioPreviewTempClips = new Sound[]{ null , null };
 	private AudioClipGenerator[] audioPreviewTempClipGenerators = new AudioClipGenerator[]{ null , null };
 	private double audioPreviewTempLength = 5.0;
 
 
 	// Generation triggers
 	private boolean triggerGenerateImageOnQualityChange = true;
 	private boolean triggerGenerateImageOnScaleChange = true;
 	private boolean triggerGenerateImageOnTimeChange = true;
 	private boolean triggerGenerateVideoPreviewImagesOnEncodingDurationChangeChange = true;
 	private boolean triggerGenerateAudioPreviewClipsOnEncodingDurationChangeChange = true;
 	private boolean triggerEncodeOnClear = true;
 
 
 	// Sync
 	private boolean[] syncAudioFade = new boolean[]{ false , false };
 	private int[] syncAudioState = new int[]{ SYNC_NOTHING , SYNC_NOTHING };
 	private boolean[] syncVideoFade = new boolean[]{ true , true };
 	private int[] syncVideoState = new int[]{ SYNC_EXTERNAL , SYNC_EXTERNAL };
 
 
 	// Other
 	private ArrayList<Thread> cleanupThreads = new ArrayList<Thread>();
 	private ArrayList<VidencodeEventListener> videoChangeListeners = new ArrayList<VidencodeEventListener>();
 
 
 	// Constructor
 	public Videncode(JSON.Node node) {
 		this.loadSettings(node);
 
 		this.tempDir = new File(Main.getAppDir(), this.tempDirName);
 		if (!this.createTempDir()) {
 			this.tempDir = new File("");
 		}
 	}
 	private final void loadSettings(JSON.Node node) {
 		if (node == null) return;
 
 		// Temp dir
 		try {
 			String str = node.getObject().get("app").getObject().get("temp_dir").getString();
 			if (str != null) this.tempDirName = Videncode.fixFilename(str);
 		}
 		catch (Exception e) {}
 
 		// Default extension
 		try {
 			String str = node.getObject().get("app").getObject().get("extensions").getObject().get("output_default").getString();
 			if (str != null) {
 				this.outputExtensionDefault = str;
 				this.outputExtension = str;
 			}
 		}
 		catch (Exception e) {}
 
 		// Max file size
 		try {
 			long i = node.getObject().get("settings").getObject().get("output_max_file_size").getInteger();
 			if (i > 0) this.outputMaxFileSize = i;
 		}
 		catch (Exception e) {}
 
 		// Thread count
 		try {
 			int threads = (int) node.getObject().get("ffmpeg").getObject().get("threads").getInteger();
 			if (threads > this.outputMaxThreads) {
 				this.outputMaxThreads = threads;
 			}
 			if (threads > 0) {
 				this.videoFileTempEncodingThreads = threads;
 			}
 		}
 		catch (Exception e) {}
 
 		// Image quality
 		try {
 			int quality = (int) node.getObject().get("settings").getObject().get("image_quality").getInteger();
 			if (quality < this.imageQualityRange[0]) quality = this.imageQualityRange[0];
 			else if (quality > this.imageQualityRange[this.imageQualityRange.length - 1]) quality = this.imageQualityRange[this.imageQualityRange.length - 1];
 			this.imageQualityDefault = quality;
 			this.imageFileTempQuality = quality;
 		}
 		catch (Exception e) {}
 
 		// Video quality mode
 		try {
 			int quality = (int) node.getObject().get("settings").getObject().get("video_encoding_mode").getInteger();
 			if (quality > 2) quality = 2;
 			else if (quality < 0) quality = 0;
 			this.videoEncodingProfileDefault = quality;
 			this.videoFileTempQuality = quality;
 		}
 		catch (Exception e) {}
 
 		// Auto quality level default
 		try {
 			String str = node.getObject().get("settings").getObject().get("video_auto_quality").getString();
 			if (str != null) {
 				for (int i = 0; i < this.videoAutoQualityProfiles.length; ++i) {
 					if (str.equals(this.videoAutoQualityProfiles[i].toString())) {
 						this.videoAutoQualityProfileDefault = this.videoAutoQualityProfiles[i];
 						break;
 					}
 				}
 			}
 		}
 		catch (Exception e) {}
 
 		// Audio kbps
 		try {
 			int val = ((int) node.getObject().get("settings").getObject().get("audio_kbps").getInteger()) * 1000;
 			for (int i = 0; i < this.audioBitrates.length; ++i) {
 				if (val == this.audioBitrates[i].getBitrate()) {
 					this.audioBitrateDefault = this.audioBitrates[i];
 					this.audioFileTempBitrate = this.audioBitrates[i];
 					break;
 				}
 			}
 		}
 		catch (Exception e) {}
 
 		// Audio channels
 		try {
 			int val = ((int) node.getObject().get("settings").getObject().get("audio_channels").getInteger());
 			for (int i = 0; i < this.audioChannels.length; ++i) {
 				if (val == this.audioChannels[i].getChannelCount()) {
 					this.audioChannelsDefault = this.audioChannels[i];
 					this.audioFileTempChannels = this.audioChannels[i];
 					break;
 				}
 			}
 		}
 		catch (Exception e) {}
 
 		// Audio sample rate
 		try {
 			int val = ((int) node.getObject().get("settings").getObject().get("audio_sample_rate").getInteger());
 			for (int i = 0; i < this.audioSampleRates.length; ++i) {
 				if (val == this.audioSampleRates[i].getSampleRate()) {
 					this.audioSampleRateDefault = this.audioSampleRates[i];
 					this.audioFileTempSampleRate = this.audioSampleRates[i];
 					break;
 				}
 			}
 		}
 		catch (Exception e) {}
 	}
 	public final void saveSettings(JSON.Node node) {
 		if (node == null) return;
 
 		node.get("ffmpeg")
 		.set("threads", JSON.node(this.videoFileTempEncodingThreads));
 
 		node.get("app")
 		.set("temp_dir", JSON.node(this.tempDirName));
 
 		node.get("app").get("extensions")
 		.set("output_default", JSON.node(this.outputExtensionDefault));
 
 		node.get("settings")
 		.set("output_max_file_size", JSON.node(this.outputMaxFileSize))
 		.set("image_quality", JSON.node(this.imageQualityDefault))
 		.set("video_encoding_mode", JSON.node(this.videoEncodingProfileDefault))
 		.set("video_auto_quality", JSON.node(this.videoAutoQualityProfileDefault.toString()))
 		.set("audio_kbps", JSON.node(this.audioBitrateDefault.getBitrate() / 1000))
 		.set("audio_channels", JSON.node(this.audioChannelsDefault.getChannelCount()))
 		.set("audio_sample_rate", JSON.node(this.audioSampleRateDefault.getSampleRate()));
 	}
 
 	// Sync check this
 	public final void clean() {
 		// Clean any threads
 		this.cleanThreads();
 
 		// Remove directory
 		this.removeDir(this.tempDir);
 	}
 	public final File getTempDir() {
 		return this.tempDir;
 	}
 	public final void setGUIComponent(Component c) {
 		this.guiComponent = c;
 	}
 	private final boolean createTempDir() {
 		boolean okay = this.tempDir.mkdirs();
 		return (okay || this.tempDir.exists());
 	}
 	private final boolean removeDir(File dir) {
 		boolean r = true;
 		// Empty the directory
 		if (dir.isDirectory()) {
 			String[] nodes = dir.list();
 			for (int i = 0; i < nodes.length; i++) {
 				if (!this.removeDir(new File(dir, nodes[i]))) r = false;
 			}
 		}
 		// Delete the file / directory
 		return r && dir.delete();
 	}
 	private final JSON.Node getFileInfo(final File f) {
 		JSON.Node videoInfo = null;
 
 		Process infoProcess = null;
 		try {
 			// invoke the process, keeping a handle to it for later...
 			infoProcess = Runtime.getRuntime().exec(new String[]{
 				"ffprobe",
 				"-v", "quiet",
 				f.getAbsolutePath(),
 				"-print_format", "json",
 				"-show_format",
 				"-show_streams"
 			}, null, this.getTempDir());
 		}
 		catch (IOException e) {
 			infoProcess = null;
 		}
 
 		if (infoProcess != null) {
 			// Handle stderr
 			final Process p = infoProcess;
 			Thread t = new Thread() {
 				public void run() {
 					byte[] buffer = new byte[128];
 					try {
 						while (p.getErrorStream().read(buffer) >= 0);
 					}
 					catch (IOException e) {}
 				}
 			};
 			t.start();
 
 			// Handle stdout
 			StringBuilder videoInfoJson = new StringBuilder();
 			byte[] buffer = new byte[128];
 			int len;
 			try {
 				while ((len = infoProcess.getInputStream().read(buffer)) >= 0) {
 					videoInfoJson.append(new String(buffer, 0, len));
 				}
 			}
 			catch (IOException e) {}
 
 			// Wait for completion
 			try {
 				t.join();
 				p.waitFor();
 			}
 			catch (InterruptedException e) {}
 
 			// Parse
 			try {
 				videoInfo = JSON.parse(videoInfoJson.toString());
 			}
 			catch (JSON.Exception e) {
 				videoInfo = null;
 			}
 
 			// Invalid file
 			if (videoInfo != null) {
 				if (videoInfo.getObject().size() == 0) {
 					videoInfo = null;
 				}
 			}
 		}
 
 		return videoInfo;
 	}
 
 	private final InfoStats parseInfoForStatistics(JSON.Node videoInfo) {
 		InfoStats info = new InfoStats();
 
 		// Video
 		try {
 			JSON.Node node;
 			ArrayList<JSON.Node> a = videoInfo.getObject().get("streams").getArray();
 			for (int i = 0; i < a.size(); ++i) {
 				if ((node = a.get(i).getObject().get("codec_type")) != null) {
 					if (node.getString().toLowerCase().equals("audio")) {
 						info.hasAudio = true;
 					}
 					if (node.getString().toLowerCase().equals("video")) {
 						info.hasVideo = true;
 					}
 				}
 			}
 		}
 		catch (Exception e) {}
 
 		return info;
 	}
 	private final void parseInfoForVideoData(JSON.Node videoInfo) {
 		// Duration
 		double duration = 0.0;
 		try {
 			duration = Double.parseDouble(videoInfo.getObject().get("format").getObject().get("duration").getString());
 		}
 		catch (Exception e) {
 			duration = 0.0;
 		}
 
 
 		// Dimension
 		int width = 0;
 		int height = 0;
 		try {
 			JSON.Node wn, hn;
 			ArrayList<JSON.Node> a = videoInfo.getObject().get("streams").getArray();
 			for (int i = 0; i < a.size(); ++i) {
 				if (
 					(wn = a.get(i).getObject().get("width")) != null &&
 					(hn = a.get(i).getObject().get("height")) != null
 				) {
 					width = (int) wn.getInteger();
 					height = (int) hn.getInteger();
 					break;
 				}
 			}
 		}
 		catch (Exception e) {
 			width = 0;
 			height = 0;
 		}
 
 
 		// Framerate
 		double framerateFallback = 30.0;
 		double framerate = framerateFallback;
 		Pattern p = Pattern.compile("([0-9]+)(/([0-9]+))?");
 		String fps = "0";
 		try {
 			JSON.Node n;
 			ArrayList<JSON.Node> a = videoInfo.getObject().get("streams").getArray();
 			for (int i = 0; i < a.size(); ++i) {
 				if (
 					a.get(i).getObject().get("width") != null &&
 					a.get(i).getObject().get("height") != null &&
 					(n = a.get(i).getObject().get("r_frame_rate")) != null
 				) {
 					fps = n.getString();
 					break;
 				}
 			}
 		}
 		catch (Exception e) {}
 		try {
 			Matcher m = p.matcher(fps);
 			if (m.matches()) {
 				if (Integer.parseInt(m.group(3)) == 0) {
 					framerate = framerateFallback;
 				}
 				else {
 					framerate = Integer.parseInt(m.group(1)) / ((double) Integer.parseInt(m.group(3)));
 				}
 			}
 			else {
 				framerate = Integer.parseInt(fps);
 			}
 		}
 		catch (NumberFormatException e) {
 			framerate = framerateFallback;
 		}
 
 		if (framerate >= 1000.0) {
 			framerate = framerateFallback;
 		}
 
 
 		// Set
 		synchronized (this.videoFileSourceLock) {
 			this.videoFileSourceDimensions.width = width;
 			this.videoFileSourceDimensions.height = height;
 			this.videoFileSourceFrameRate.setFrameRate(framerate);
 
 			this.videoFileSourceDuration = duration;
 			this.videoFileSourceEncodeDuration[0] = 0.0;
 			this.videoFileSourceEncodeDuration[1] = duration;
 		}
 	}
 	private final void parseInfoForImageData(JSON.Node videoInfo) {
 		// Duration
 		double duration = 0.0;
 		try {
 			duration = Double.parseDouble(videoInfo.getObject().get("format").getObject().get("duration").getString());
 		}
 		catch (Exception e) {
 			duration = 0.0;
 		}
 
 
 		// Dimension
 		int width = 0;
 		int height = 0;
 		try {
 			JSON.Node wn, hn;
 			ArrayList<JSON.Node> a = videoInfo.getObject().get("streams").getArray();
 			for (int i = 0; i < a.size(); ++i) {
 				if (
 					(wn = a.get(i).getObject().get("width")) != null &&
 					(hn = a.get(i).getObject().get("height")) != null
 				) {
 					width = (int) wn.getInteger();
 					height = (int) hn.getInteger();
 					break;
 				}
 			}
 		}
 		catch (Exception e) {
 			width = 0;
 			height = 0;
 		}
 
 
 		// Set
 		synchronized (this.imageFileSourceLock) {
 			this.imageFileSourceDimensions.width = width;
 			this.imageFileSourceDimensions.height = height;
 
 			this.imageFileSourceVideoTime = (int) (duration * (1.0 / 2.0));
 		}
 	}
 	private final void parseInfoForAudioData(JSON.Node videoInfo) {
 		// Duration
 		double duration = 0.0;
 		try {
 			duration = Double.parseDouble(videoInfo.getObject().get("format").getObject().get("duration").getString());
 		}
 		catch (Exception e) {
 			duration = 0.0;
 		}
 
 
 		// Set
 		synchronized (this.videoFileSourceLock) {
 			this.audioFileSourceDuration = duration;
 			this.audioFileSourceEncodeDuration[0] = 0.0;
 			this.audioFileSourceEncodeDuration[1] = duration;
 		}
 	}
 
 
 	// Nullify things
 	private final void nullifyVideoFileSourceData() {
 		synchronized (this.videoFileSourceLock) {
 			this.videoFileSource = null;
 			this.videoFileSourceDimensions.width = 0;
 			this.videoFileSourceDimensions.height = 0;
 			this.videoFileSourceFrameRate.setFrameRate(0.0);
 			this.videoFileSourceDuration = 0.0;
 			this.videoFileSourceEncodeDuration[0] = 0.0;
 			this.videoFileSourceEncodeDuration[1] = 0.0;
 			this.videoFileSourceInfoNode = null;
 		}
 	}
 	private final void nullifyImageFileSourceData() {
 		synchronized (this.imageFileSourceLock) {
 			this.imageFileSource = null;
 			this.imageFileSourceDimensions.width = 0;
 			this.imageFileSourceDimensions.height = 0;
 			this.imageFileSourceVideoTime = 0.0;
 			this.imageFileSourceInfoNode = null;
 		}
 	}
 	private final void nullifyAudioFileSourceData() {
 		synchronized (this.audioFileSourceLock) {
 			this.audioFileSource = null;
 			this.audioFileSourceDuration = 0.0;
 			this.audioFileSourceEncodeDuration[0] = 0.0;
 			this.audioFileSourceEncodeDuration[1] = 0.0;
 			this.audioFileSourceInfoNode = null;
 		}
 	}
 	private final void nullifyVideoFileTempData() {
 		synchronized (this.videoFileTempLock) {
 			if (this.videoFileTempGenerator != null) {
 				this.videoFileTempGenerator.cancel();
 				this.videoFileTempIsGenerating = false;
 			}
 			if (this.videoFileTemp != null) {
 				this.videoFileTemp.delete();
 				this.videoFileTemp = null;
 			}
 			for (int i = 0; i < this.videoPreviewTempScreenshotGenerators.length; ++i) {
 				if (this.videoPreviewTempScreenshotGenerators[i] != null) {
 					this.videoPreviewTempScreenshotGenerators[i].cancel();
 				}
 			}
 			for (int i = 0; i < this.videoPreviewFilesTemp.length; ++i) {
 				if (this.videoPreviewFilesTemp[i] != null) {
 					this.videoPreviewFilesTemp[i].delete();
 					this.videoPreviewFilesTemp[i] = null;
 				}
 			}
 			for (int i = 0; i < this.videoPreviewTempImages.length; ++i) {
 				this.videoPreviewTempImages[i] = null;
 			}
 			this.videoFileTempMightNeedReEncoding = false;
 		}
 		this.stopEncoding();
 		this.clearMuxFile();
 	}
 	private final void nullifyImageFileTempData() {
 		synchronized (this.imageFileTempLock) {
 			if (this.imageFileTemp != null) {
 				this.imageFileTemp.delete();
 				this.imageFileTemp = null;
 			}
 			this.imageFileTempDimensions.width = 0;
 			this.imageFileTempDimensions.height = 0;
 			if (this.imageFileTempScreenshotGenerator != null) {
 				this.imageFileTempScreenshotGenerator.cancel();
 			}
 			this.imageFileImageTemp = null;
 		}
 		this.stopEncoding();
 		this.clearMuxFile();
 	}
 	private final void nullifyAudioFileTempData() {
 		synchronized (this.audioFileTempLock) {
 			if (this.audioFileTempGenerator != null) {
 				this.audioFileTempGenerator.cancel();
 				this.audioFileTempIsGenerating = false;
 			}
 			if (this.audioFileTemp != null) {
 				this.audioFileTemp.delete();
 				this.audioFileTemp = null;
 			}
 			for (int i = 0; i < this.audioPreviewTempClipGenerators.length; ++i) {
 				if (this.audioPreviewTempClipGenerators[i] != null) {
 					this.audioPreviewTempClipGenerators[i].cancel();
 				}
 			}
 			for (int i = 0; i < this.audioPreviewFilesTemp.length; ++i) {
 				if (this.audioPreviewFilesTemp[i] != null) {
 					this.audioPreviewFilesTemp[i].delete();
 					this.audioPreviewFilesTemp[i] = null;
 				}
 			}
 			for (int i = 0; i < this.audioPreviewTempClips.length; ++i) {
 				this.audioPreviewTempClips[i] = null;
 			}
 		}
 		this.stopEncoding();
 		this.clearMuxFile();
 	}
 
 	private final boolean deleteVideoFileTempFile() {
 		boolean r = false;
 		synchronized (this.videoFileTempLock) {
 			if (this.videoFileTemp != null) {
 				this.videoFileTemp.delete();
 				this.videoFileTemp = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 	private final boolean deleteVideoFileTempFilePreview(int id) {
 		boolean r = false;
 		synchronized (this.videoFileTempLock) {
 			if (this.videoPreviewFilesTemp[id] != null) {
 				this.videoPreviewFilesTemp[id].delete();
 				this.videoPreviewFilesTemp[id] = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 	private final boolean deleteImageFileTempFile() {
 		boolean r = false;
 		synchronized (this.imageFileTempLock) {
 			if (this.imageFileTemp != null) {
 				this.imageFileTemp.delete();
 				this.imageFileTemp = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 	private final boolean deleteAudioFileTempFile() {
 		boolean r = false;
 		synchronized (this.audioFileTempLock) {
 			if (this.audioFileTemp != null) {
 				this.audioFileTemp.delete();
 				this.audioFileTemp = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 	private final boolean deleteAudioFileTempFilePreview(int id) {
 		boolean r = false;
 		synchronized (this.audioFileTempLock) {
 			if (this.audioPreviewFilesTemp[id] != null) {
 				this.audioPreviewFilesTemp[id].delete();
 				this.audioPreviewFilesTemp[id] = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 	private final boolean deleteMuxFile() {
 		boolean r = false;
 		synchronized (this.audioFileTempLock) {
 			if (this.outputFileLast != null) {
 				this.outputFileLast = null;
 				r = true;
 			}
 		}
 		return r;
 	}
 
 	public final void clearVideoFileTemp() {
 		boolean b = this.cancelVideoTempFileEncoding();
 		b = this.deleteVideoFileTempFile() || b;
 		if (b) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 				VidencodeChangeEvent.TEMPORARY
 			));
 		}
 	}
 	public final void clearAudioFileTemp() {
 		boolean b = this.cancelAudioTempFileEncoding();
 		b = this.deleteAudioFileTempFile() || b;
 		if (b) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 				VidencodeChangeEvent.TEMPORARY
 			));
 		}
 		this.clearVideoFileTemp();
 	}
 	public final void clearMuxFile() {
 		boolean b = this.cancelMuxing();
 		synchronized (this.outputLock) {
 			if (this.outputFileLast != null) {
 				this.outputFileLast = null;
 				b = true;
 			}
 		}
 	}
 
 	public final boolean cancelAudioTempFileEncoding() {
 		boolean r = false;
 		if (this.audioFileTempGenerator != null) {
 			if (!this.audioFileTempGenerator.isCanceled()) {
 				this.audioFileTempGenerator.cancel();
 				r = true;
 			}
 			synchronized (this.audioFileTempLock) {
 				this.audioFileTempIsGenerating = false;
 				this.audioFileTempGeneratingPercent = 0.0;
 			}
 		}
 		if (this.triggerEncodeOnClear && this.isOutputEncoding()) this.encodeAll();
 		return r;
 	}
 	public final boolean cancelVideoTempFileEncoding() {
 		boolean r = false;
 		if (this.videoFileTempGenerator != null) {
 			if (!this.videoFileTempGenerator.isCanceled()) {
 				this.videoFileTempGenerator.cancel();
 				r = true;
 			}
 			synchronized (this.videoFileTempLock) {
 				this.videoFileTempIsGenerating = false;
 				this.videoFileTempGeneratingPercent = 0.0;
 			}
 		}
 		if (this.triggerEncodeOnClear && this.isOutputEncoding()) this.encodeAll();
 		return r;
 	}
 	public final boolean cancelMuxing() {
 		boolean r = false;
 		if (this.outputGenerator != null) {
 			if (!this.outputGenerator.isCanceled()) {
 				this.outputGenerator.cancel();
 				r = true;
 			}
 			synchronized (this.outputLock) {
 				this.outputIsMuxing = false;
 			}
 		}
 		if (this.triggerEncodeOnClear && this.isOutputEncoding()) this.encodeAll();
 		return r;
 	}
 
 	public final void encodeAudioTempFile() {
 		this.genAudio();
 	}
 	public final void encodeVideoTempFile() {
 		this.genVideo();
 	}
 
 	public final void encodeAll() {
 		this.encodeAll(-1, null);
 	}
 	private final void encodeAll(int state, String error) {
 		boolean b = this.triggerEncodeOnClear;
 		this.triggerEncodeOnClear = false;
 
 		synchronized (this.outputLock) {
 			this.outputEncoding = true;
 		}
 
 		boolean encode = true;
 
 		// Start
 		if (state < 0) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.ENCODE_STATUS,
 				VidencodeChangeEvent.CLEAR
 			)
 			.setValue(0.0)
 			.setText(0, "Starting..."));
 		}
 
 		// No image?
 		if (encode && this.getImageFileTemp() == null) {
 			if (state >= 0) {
 				this.stopEncoding(true);
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					VidencodeChangeEvent.ERROR
 				)
 				.setValue(0.0)
 				.setText(0, error));
 			}
 			else {
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					VidencodeChangeEvent.ERROR
 				)
 				.setValue(0.0)
 				.setText(0, "Image not encoded"));
 			}
 			encode = false;
 		}
 
 		// No audio?
 		if (encode && this.getAudioFileTemp() == null && this.getAudioFileSource() != null) {
 			if (state >= 1) {
 				this.stopEncoding(true);
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					VidencodeChangeEvent.ERROR
 				)
 				.setValue(0.0)
 				.setText(0, error));
 			}
 			else if (this.audioFileTempGenerator == null || this.audioFileTempGenerator.isCanceled()) {
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					0
 				)
 				.setValue(0.3)
 				.setText(0, "Encoding audio"));
 				this.genAudio();
 			}
 			encode = false;
 		}
 
 		// No video?
 		if (encode && this.getVideoFileTemp() == null && this.getVideoFileSource() != null) {
 			if (state >= 2) {
 				this.stopEncoding(true);
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					VidencodeChangeEvent.ERROR
 				)
 				.setValue(0.0)
 				.setText(0, error));
 			}
 			else if (this.videoFileTempGenerator == null || this.videoFileTempGenerator.isCanceled()) {
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					0
 				)
 				.setValue(0.6)
 				.setText(0, "Encoding video"));
 				this.genVideo();
 			}
 			encode = false;
 		}
 
 		// Mux
 		if (encode) {
 			this.mux();
 		}
 
 		// Multiplex
 		if (encode) {
 			if (state >= 3) {
 				this.stopEncoding(true);
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					VidencodeChangeEvent.ERROR
 				)
 				.setValue(0.0)
 				.setText(0, error));
 			}
 			else if (this.outputGenerator == null || this.outputGenerator.isCanceled()) {
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.ENCODE_STATUS,
 					0
 				)
 				.setValue(0.6)
 				.setText(0, "Multiplexing"));
 				this.mux();
 			}
 			encode = false;
 		}
 
 
 		this.triggerEncodeOnClear = b;
 	}
 	public final void stopEncoding() {
 		this.stopEncoding(false);
 	}
 	public final void stopEncoding(boolean error) {
 		if (!this.isOutputEncoding()) return;
 
 		synchronized (this.outputLock) {
 			this.outputEncoding = false;
 		}
 
 		this.cancelAudioTempFileEncoding();
 		this.cancelVideoTempFileEncoding();
 		this.cancelMuxing();
 
 		if (!error) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.ENCODE_STATUS,
 				VidencodeChangeEvent.RESET
 			)
 			.setValue(0.0)
 			.setText(0, ""));
 		}
 
 	}
 	public final boolean isOutputEncoding() {
 		boolean b;
 
 		synchronized (this.outputLock) {
 			b = this.outputEncoding;
 		}
 
 		return b;
 	}
 
 
 	// Resource setting
 	public final boolean setVideoFileSource(final File f, boolean async) {
 		return this.setVideoFileSource(f, async, null);
 	}
 	public final boolean setVideoFileSource(final File f, boolean async, final Runnable callback) {
 		if (this.getVideoFileSource() != f) {
 			// Clear
 			this.nullifyVideoFileSourceData();
 			this.nullifyVideoFileTempData();
 			this.resetSyncState();
 
 			// Change
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_PREVIEW_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.FIRST | VidencodeChangeEvent.LAST
 			));
 
 			if (f != null) {
 				if (async) {
 					final Videncode self = this;
 					new Thread() {
 						@Override
 						public final void run() {
 							self.setVideoFileSource2(f, callback);
 						}
 					}.start();
 				}
 				else {
 					this.setVideoFileSource2(f, callback);
 				}
 			}
 			else {
 				if (this.getAudioFileSource() == null) {
 					this.setOutputTag("");
 				}
 			}
 		}
 
 		return true;
 	}
 	private final void setVideoFileSource2(File f, Runnable callback) {
 		// Load info from somewhere
 		JSON.Node n = this.getFileInfo(f);
 
 		if (n != null) {
 			InfoStats info = this.parseInfoForStatistics(n);
 			if (info.hasVideo) {
 				// Get info
 				synchronized (this.videoFileSourceLock) {
 					this.videoFileSource = f;
 					this.videoFileSourceInfoNode = n;
 				}
 				this.setOutputTag(Videncode.getFileNameNoExt(f));
 				synchronized (this.videoFileTempLock) {
 					this.videoFileTempMightNeedReEncoding = false;
 				}
 				this.parseInfoForVideoData(n);
 				this.autoGetVideoFileTempSettings();
 
 				// Change
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 					VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 				));
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.VIDEO_PREVIEW_CHANGED,
 					VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING | VidencodeChangeEvent.FIRST | VidencodeChangeEvent.LAST
 				));
 
 				// Regen temp files
 				this.genVideoPreviewImages(PREVIEW_FIRST);
 				this.genVideoPreviewImages(PREVIEW_LAST);
 			}
 		}
 
 		if (callback != null) {
 			callback.run();
 		}
 	}
 	public final boolean setImageFileSource(final File f, boolean async) {
 		if (this.getImageFileSource() != f) {
 			// Clear
 			this.nullifyImageFileSourceData();
 			this.nullifyImageFileTempData();
 
 			// Change
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 
 			if (f != null) {
 				if (async) {
 					final Videncode self = this;
 					new Thread() {
 						@Override
 						public final void run() {
 							self.setImageFileSource2(f);
 						}
 					}.start();
 				}
 				else {
 					this.setImageFileSource2(f);
 				}
 			}
 			else {
 				this.setOutputFilename("");
 			}
 		}
 
 		return true;
 	}
 	public final void setImageFileSource2(File f) {
 		// Load info from somewhere
 		JSON.Node n = this.getFileInfo(f);
 
 		if (n != null) {
 			InfoStats info = this.parseInfoForStatistics(n);
 			if (info.hasVideo) {
 				// Get info
 				synchronized (this.imageFileSourceLock) {
 					this.imageFileSource = f;
 					this.imageFileSourceInfoNode = n;
 				}
 				this.setOutputFilename(Videncode.getFileNameNoExt(f));
 				this.parseInfoForImageData(n);
 				this.autoGetImageFileTempSettings();
 
 				// Change
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED,
 					VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING
 				));
 
 				// Regen temp files
 				this.genImage();
 			}
 		}
 	}
 	public final boolean setAudioFileSource(final File f, boolean async) {
 		if (this.getAudioFileSource() != f) {
 			// Clear
 			this.nullifyAudioFileSourceData();
 			this.nullifyAudioFileTempData();
 			this.resetSyncState();
 
 			// Change
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_PREVIEW_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.FIRST | VidencodeChangeEvent.LAST
 			));
 
 			if (f != null) {
 				if (async) {
 					final Videncode self = this;
 					new Thread() {
 						@Override
 						public final void run() {
 							self.setAudioFileSource2(f);
 						}
 					}.start();
 				}
 				else {
 					this.setAudioFileSource2(f);
 				}
 			}
 			else {
 				if (this.getVideoFileSource() == null) {
 					this.setOutputTag("");
 				}
 			}
 		}
 
 		return true;
 	}
 	public final void setAudioFileSource2(File f) {
 		// Load info from somewhere
 		JSON.Node n = this.getFileInfo(f);
 
 		if (n != null) {
 			InfoStats info = this.parseInfoForStatistics(n);
 			if (info.hasAudio) {
 				// Get info
 				synchronized (this.audioFileSourceLock) {
 					this.audioFileSource = f;
 					this.audioFileSourceInfoNode = n;
 				}
 				this.setOutputTag(Videncode.getFileNameNoExt(f));
 				synchronized (this.videoFileTempLock) {
 					this.videoFileTempMightNeedReEncoding = false;
 				}
 				this.parseInfoForAudioData(n);
 				this.autoGetAudioileTempSettings();
 
 				// Change
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 					VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 				));
 				this.signalChange(new VidencodeChangeEvent(
 					this,
 					VidencodeChangeEvent.EventType.AUDIO_PREVIEW_CHANGED,
 					VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING | VidencodeChangeEvent.FIRST | VidencodeChangeEvent.LAST
 				));
 
 				// Regen temp files
 				this.genAudioPreviewClips(PREVIEW_FIRST);
 				this.genAudioPreviewClips(PREVIEW_LAST);
 			}
 		}
 	}
 
 
 	// Auto
 	private final void autoGetVideoFileTempSettings() {
 		Dimension d = this.getVideoFileSourceDimensions();
 		FrameRate r = this.getVideoFileSourceFrameRate();
 
 		synchronized (this.videoFileTempLock) {
 			this.videoFileTempDimensions.width = d.width;
 			this.videoFileTempDimensions.height = d.height;
 
 			this.videoFileTempFrameRate.setFrameRate(r);
 		}
 	}
 	private final void autoGetImageFileTempSettings() {
 		Dimension d = this.getVideoFileSourceDimensions();
 
 		synchronized (this.videoFileTempLock) {
 			this.imageFileTempDimensions.width = d.width;
 			this.imageFileTempDimensions.height = d.height;
 		}
 
 		this.setImageFileTempQuality(this.isImageFromVideo() ? this.imageQualityDefault : -1);
 	}
 	private final void autoGetAudioileTempSettings() {
 		synchronized (this.audioFileTempLock) {
 			this.audioFileTempChannels = this.audioChannels[0];
 			this.audioFileTempSampleRate = this.audioSampleRates[this.audioSampleRates.length - 1];
 		}
 	}
 
 	// Getting
 	public final File getVideoFileSource() {
 		File f;
 		synchronized (videoFileSourceLock) {
 			f = this.videoFileSource;
 		}
 		return f;
 	}
 	public final File getImageFileSource() {
 		File f;
 		synchronized (imageFileSourceLock) {
 			f = this.imageFileSource;
 		}
 		return f;
 	}
 	public final File getAudioFileSource() {
 		File f;
 		synchronized (audioFileSourceLock) {
 			f = this.audioFileSource;
 		}
 		return f;
 	}
 	public final File getVideoFileTemp() {
 		File f;
 		synchronized (videoFileTempLock) {
 			f = this.videoFileTemp;
 		}
 		return f;
 	}
 	public final File getImageFileTemp() {
 		File f;
 		synchronized (imageFileTempLock) {
 			f = this.imageFileTemp;
 		}
 		return f;
 	}
 	public final File getAudioFileTemp() {
 		File f;
 		synchronized (this.audioFileTempLock) {
 			f = this.audioFileTemp;
 		}
 		return f;
 	}
 	public final File getAudioPreviewTemp(int id) {
 		File f;
 		synchronized (this.audioPreviewFilesTempLock) {
 			f = this.audioPreviewFilesTemp[id];
 		}
 		return f;
 	}
 	public final Sound getAudioPreviewTempSound(int id) {
 		Sound s;
 		synchronized (this.audioPreviewFilesTempLock) {
 			s = this.audioPreviewTempClips[id];
 		}
 		return s;
 	}
 	public final Sound[] getAudioPreviewTempSounds() {
 		Sound[] s;
 		synchronized (this.audioPreviewFilesTempLock) {
 			s = new Sound[this.audioPreviewTempClips.length];
 			for (int i = 0; i < this.audioPreviewTempClips.length; ++i) {
 				s[i] = this.audioPreviewTempClips[i];
 			}
 		}
 		return s;
 	}
 
 	// Testing
 	public final boolean isImageFromVideo() {
 		File f1, f2;
 		synchronized (imageFileSourceLock) {
 			f1 = this.imageFileSource;
 		}
 		synchronized (videoFileSourceLock) {
 			f2 = this.videoFileSource;
 		}
 		return (f1 == f2);
 	}
 	public final boolean isAudioFromVideo() {
 		File f1, f2;
 		synchronized (audioFileSourceLock) {
 			f1 = this.audioFileSource;
 		}
 		synchronized (videoFileSourceLock) {
 			f2 = this.videoFileSource;
 		}
 		return (f1 == f2);
 	}
 
 
 	// Gen methods
 	private final void genImage() {
 		File inputFile = this.getImageFileSource();
 		if (inputFile == null) {
 			if (this.isOutputEncoding()) this.encodeAll(0, "No image input");
 			return;
 		}
 
 		final Videncode self = this;
 		boolean copyableSource = !this.isImageFromVideo();
 		String filename = "preview.custom";
 		int quality = this.getImageFileTempQuality();
 		double time = (copyableSource ? 0.0 : this.getImageFileSourceVideoTime());
 		Dimension imageSize = this.getImageFileTempDimensions();
 
 		if (this.imageFileTempScreenshotGenerator != null) {
 			this.imageFileTempScreenshotGenerator.cancel();
 		}
 		if (this.deleteImageFileTempFile()) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED,
 				VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING
 			));
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.IMAGE_FILE_ENCODING_LOG,
 			VidencodeChangeEvent.CLEAR
 		));
 		this.imageFileTempScreenshotGenerator = new ScreenshotGenerator(
 			this,
 			this.guiComponent,
 			inputFile,
 			filename,
 			this.getTempDir(),
 			copyableSource,
 			imageSize,
 			quality,
 			time
 		){
 			@Override
 			public void onScreenshotGet(File f, Image img) {
 				self.onImageCompleted(f, img);
 			}
 			@Override
 			protected void onCommandGenerate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.IMAGE_FILE_ENCODING_LOG,
 					0
 				).setText(0, str));
 			}
 			@Override
 			protected void onInfoUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.IMAGE_FILE_ENCODING_LOG,
 					0
 				).setText(1, str));
 			}
 			@Override
 			protected void onProgressStringUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.IMAGE_FILE_ENCODING_LOG,
 					0
 				).setText(2, str));
 			}
 		};
 		this.imageFileTempScreenshotGenerator.start();
 	}
 	private final void genAudio() {
 		File inputFile = this.getAudioFileSource();
 		if (inputFile == null) {
 			if (this.isOutputEncoding()) this.encodeAll(1, "No audio input");
 			return;
 		}
 
 		final Videncode self = this;
 		String filename = "output";
 		double[] time = this.getAudioFileSourceEncodeDuration();
 		Bitrate bitrate = this.getAudioFileTempBitrate();
 		AudioChannelCount channelCount = this.getAudioFileTempChannels();
 		AudioSampleRate sampleRate = this.getAudioFileTempSampleRate();
 		double length = time[1] - time[0];
 		double start = time[0];
 
 		this.cancelAudioTempFileEncoding();
 		if (this.deleteAudioFileTempFile()) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 				VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING
 			));
 		}
 
 		GeneratorProgressCallback callback = new GeneratorProgressCallback() {
 			@Override
 			public final void onProgress(double progress, int flags) {
 				self.onAudioProgress(progress);
 			}
 		};
 
 		synchronized (this.audioFileTempLock) {
 			this.audioFileTempIsGenerating = true;
 			this.audioFileTempGeneratingPercent = 0.0;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.AUDIO_FILE_ENCODING_LOG,
 			VidencodeChangeEvent.CLEAR
 		));
 		this.audioFileTempGenerator = new AudioClipGenerator(
 			this,
 			inputFile,
 			filename,
 			this.getTempDir(),
 			start,
 			length,
 			bitrate,
 			channelCount,
 			sampleRate,
 			callback,
 			false
 		){
 			@Override
 			public void onAudioGet(File f, Sound snd) {
 				self.onAudioCompleted(f);
 			}
 			@Override
 			protected void onCommandGenerate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.AUDIO_FILE_ENCODING_LOG,
 					0
 				).setText(0, str));
 			}
 			@Override
 			protected void onInfoUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.AUDIO_FILE_ENCODING_LOG,
 					0
 				).setText(1, str));
 			}
 			@Override
 			protected void onProgressStringUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.AUDIO_FILE_ENCODING_LOG,
 					0
 				).setText(2, str));
 			}
 		};
 		this.audioFileTempGenerator.start();
 	}
 	private final void genVideo() {
 		File inputFile = this.getVideoFileSource();
 		if (inputFile == null) {
 			if (this.isOutputEncoding()) this.encodeAll(2, "No video input");
 			return;
 		}
 
 		final Videncode self = this;
 		String filename = "output";
 		double[] time = this.getVideoFileSourceEncodeDuration();
 		double length = time[1] - time[0];
 		if (length <= 0.0) {
 			if (this.isOutputEncoding()) this.encodeAll(2, "No duration");
 			return;
 		}
 		double start = time[0];
 		Dimension size = this.getVideoFileTempDimensions();
 		FrameRate framerate = this.getVideoFileTempFrameRate();
 		int threads = this.getVideoFileTempEncodingThreads();
 		int quality = this.getVideoFileTempQuality();
 		boolean reEncodePossible;
 
 		long max = this.getOutputMaxFileSize();
 		long imageSize = this.getImageFileTempFileSize();
 		long audioSize = this.getAudioFileTempFileSize();
 		long metadataSize = this.getOutputMetadataLength();
 		max -= imageSize + audioSize + metadataSize;
 		if (max <= 0) {
 			if (this.isOutputEncoding()) this.encodeAll(2, "No space");
 			return;
 		}
 
 		Bitrate bitrate = new Bitrate((int) (max * 8 / length), "");
 
 		this.cancelVideoTempFileEncoding();
 		if (this.deleteVideoFileTempFile()) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 				VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING
 			));
 		}
 
 		GeneratorProgressCallback callback = new GeneratorProgressCallback() {
 			@Override
 			public final void onProgress(double progress, int flags) {
 				self.onVideoProgress(progress);
 			}
 		};
 
 		synchronized (this.videoFileTempLock) {
 			this.videoFileTempIsGenerating = true;
 			this.videoFileTempGeneratingPercent = 0.0;
 			reEncodePossible = this.videoFileTempMightNeedReEncoding;
 			this.videoFileTempMightNeedReEncoding = false;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.VIDEO_FILE_ENCODING_LOG,
 			VidencodeChangeEvent.CLEAR
 		));
 		this.videoFileTempGenerator = new VideoGenerator(
 			this,
 			inputFile,
 			filename,
 			this.getTempDir(),
 			start,
 			length,
 			bitrate,
 			size,
 			framerate,
 			callback,
 			threads,
 			quality,
 			max,
 			reEncodePossible
 		){
 			@Override
 			public void onVideoGet(File f) {
 				self.onVideoCompleted(f);
 			}
 			@Override
 			protected void onCommandGenerate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.VIDEO_FILE_ENCODING_LOG,
 					0
 				).setText(0, str));
 			}
 			@Override
 			protected void onInfoUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.VIDEO_FILE_ENCODING_LOG,
 					0
 				).setText(1, str));
 			}
 			@Override
 			protected void onProgressStringUpdate(final String str) {
 				self.signalChange(new VidencodeChangeEvent(
 					self,
 					VidencodeChangeEvent.EventType.VIDEO_FILE_ENCODING_LOG,
 					0
 				).setText(2, str));
 			}
 		};
 		this.videoFileTempGenerator.start();
 	}
 	private final void genVideoPreviewImages(final int id) {
 		File inputFile = this.getVideoFileSource();
 		if (inputFile == null) return;
 
 		final Videncode self = this;
 		String filename = (id == PREVIEW_FIRST ? "preview.start" : "preview.end");
 		int quality = 0;
 		double time = this.getVideoFileSourceEncodeDuration()[id];
 		Dimension imageSize = this.getVideoFileSourceDimensions();
 
 		if (this.videoPreviewTempScreenshotGenerators[id] != null) {
 			this.videoPreviewTempScreenshotGenerators[id].cancel();
 		}
 		if (this.deleteVideoFileTempFilePreview(id)) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_PREVIEW_CHANGED,
 				VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING | (id == PREVIEW_FIRST ? VidencodeChangeEvent.FIRST : VidencodeChangeEvent.LAST)
 			));
 		}
 
 		this.videoPreviewTempScreenshotGenerators[id] = new ScreenshotGenerator(
 			this,
 			this.guiComponent,
 			inputFile,
 			filename,
 			this.getTempDir(),
 			false,
 			imageSize,
 			quality,
 			time
 		){
 			@Override
 			public void onScreenshotGet(File f, Image img) {
 				self.onVideoPreviewCompleted(f, img, id);
 			}
 		};
 		this.videoPreviewTempScreenshotGenerators[id].start();
 	}
 	private final void genAudioPreviewClips(final int id) {
 		File inputFile = this.getAudioFileSource();
 		if (inputFile == null) return;
 
 		final Videncode self = this;
 		String filename = (id == PREVIEW_FIRST ? "preview.start" : "preview.end");
 		double[] time = this.getAudioFileSourceEncodeDuration();
 		Bitrate bitrate = this.getAudioFileTempBitrate();
 		AudioChannelCount channelCount = this.getAudioFileTempChannels();
 		AudioSampleRate sampleRate = this.getAudioFileTempSampleRate();
 		double maxlen = this.getAudioPreviewTempLength();
 		double length = time[1] - time[0];
 		if (length > maxlen) length = maxlen;
 		double start = (id == PREVIEW_FIRST ? time[0] : time[1] - length);
 
 		if (this.audioPreviewTempClipGenerators[id] != null) {
 			this.audioPreviewTempClipGenerators[id].cancel();
 		}
 		if (this.deleteAudioFileTempFilePreview(id)) {
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_PREVIEW_CHANGED,
 				VidencodeChangeEvent.TEMPORARY | VidencodeChangeEvent.UPDATING | (id == PREVIEW_FIRST ? VidencodeChangeEvent.FIRST : VidencodeChangeEvent.LAST)
 			));
 		}
 
 		this.audioPreviewTempClipGenerators[id] = new AudioClipGenerator(
 			this,
 			inputFile,
 			filename,
 			this.getTempDir(),
 			start,
 			length,
 			bitrate,
 			channelCount,
 			sampleRate,
 			null,
 			true
 		) {
 			@Override
 			public void onAudioGet(File f, Sound snd) {
 				self.onAudioPreviewCompleted(f, snd, id);
 			}
 		};
 		this.audioPreviewTempClipGenerators[id].start();
 	}
 	private final void mux() {
 		// Files
 		File imageFile = this.getImageFileTemp();
 		if (imageFile == null) {
 			if (this.isOutputEncoding()) this.encodeAll(3, "No image");
 			return;
 		}
 		File audioFile = this.getAudioFileTemp();
 		File videoFile = this.getVideoFileTemp();
 		if (videoFile == null && audioFile == null) {
 			if (this.isOutputEncoding()) this.encodeAll(3, "No audio/video");
 			return;
 		}
 
 
 		final Videncode self = this;
 
 		String tempFilename = "multiplex";
 		String finalOutputName = Videncode.fixFilename(this.getOutputFilename());
 		String finalOutputNameExt = this.getOutputExtension().replaceAll("\\*", Videncode.getFileExt(imageFile));
 		byte[] tag = this.getOutputTagBytes();
 		int tagLength = this.getOutputTagLength();
 
 		double[] videoTime = this.getVideoFileSourceEncodeDuration();
 		double[] audioTime = this.getAudioFileSourceEncodeDuration();
 
 		boolean muxAudioAndVideo = videoFile != null && audioFile != null && Videncode.withinThreshold(videoTime[1] - videoTime[0], audioTime[1] - audioTime[0]);
 		boolean videoIsLonger = (videoTime[1] - videoTime[0] > audioTime[1] - audioTime[0]);
 		double syncOffset = (videoIsLonger ? this.getAudioFileSourceSyncOffset() : this.getVideoFileSourceSyncOffset());
 		int flags1 = (
 			(videoFile != null ? 0x01 : 0x00) |
 			(audioFile != null && !muxAudioAndVideo ? 0x02 : 0x00) |
 			// 0x04 : reserved
 			// 0x08 : reserved
 			(this.getSyncVideoUseFade(true) && !muxAudioAndVideo ? 0x10 : 0x00) |
 			(this.getSyncVideoUseFade(false) && !muxAudioAndVideo ? 0x20 : 0x00) |
 			(this.getSyncAudioUseFade(true) && !muxAudioAndVideo ? 0x40 : 0x00) |
 			(this.getSyncAudioUseFade(false) && !muxAudioAndVideo ? 0x80 : 0x00)
 		);
 		int flags2 = (
 			(this.getSyncVideoState(true)) | // 0x01 , 0x02
 			(this.getSyncVideoState(false) << 2) | // 0x04 , 0x08
 			(this.getSyncAudioState(true) == SYNC_LOOP ? 0x10 : 0x00) |
 			(this.getSyncAudioState(false) == SYNC_LOOP ? 0x20 : 0x00)
 			// 0x40 : reserved
 			// 0x80 : reserved
 		);
 
 		this.cancelMuxing();
 
 		synchronized (this.outputLock) {
 			this.outputIsMuxing = true;
 		}
 
 		this.outputGenerator = new MuxGenerator(
 			this,
 			this.tempDir,
 			Main.getAppDir(),
 			imageFile,
 			audioFile,
 			videoFile,
 			tempFilename,
 			finalOutputName,
 			finalOutputNameExt,
 			tag,
 			tagLength,
 			muxAudioAndVideo,
 			videoIsLonger,
 			syncOffset,
 			flags1,
 			flags2
 		){
 			@Override
 			public final void onMuxGet(File f) {
 				self.onMuxCompleted(f);
 			}
 		};
 		this.outputGenerator.start();
 
 	}
 
 
 	// Gen method completion events
 	private final void onImageCompleted(File f, Image img) {
 		boolean signalSourceChange = false;
 
 		if (img != null) {
 			synchronized (this.imageFileSourceLock) {
 				if (this.imageFileSourceDimensions.width == 0 && this.imageFileSourceDimensions.height == 0) {
 					this.imageFileSourceDimensions.width = img.getWidth(null);
 					this.imageFileSourceDimensions.height = img.getHeight(null);
 					signalSourceChange = true;
 				}
 			}
 		}
 		synchronized (this.imageFileTempLock) {
 			this.imageFileTemp = f;
 			this.imageFileImageTemp = img;
 
 			if (img == null) {
 				this.imageFileTempDimensions.width = 0;
 				this.imageFileTempDimensions.height = 0;
 			}
 			else {
 				this.imageFileTempDimensions.width = img.getWidth(null);
 				this.imageFileTempDimensions.height = img.getHeight(null);
 			}
 		}
 
 		if (signalSourceChange) {
 			this.signalChange(new VidencodeChangeEvent(this, VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED, VidencodeChangeEvent.SOURCE));
 		}
 		this.signalChange(new VidencodeChangeEvent(this, VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED, VidencodeChangeEvent.TEMPORARY));
 
 		this.clearVideoFileTemp();
 
 
 		if (this.isOutputEncoding()) this.encodeAll(0, "Image error");
 	}
 	private final void onVideoPreviewCompleted(File f, Image img, int id) {
 		synchronized (this.videoPreviewFilesTempLock) {
 			this.videoPreviewFilesTemp[id] = f;
 			this.videoPreviewTempImages[id] = img;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(this, VidencodeChangeEvent.EventType.VIDEO_PREVIEW_CHANGED, VidencodeChangeEvent.TEMPORARY | (id == PREVIEW_FIRST ? VidencodeChangeEvent.FIRST : VidencodeChangeEvent.LAST)));
 	}
 	private final void onAudioPreviewCompleted(File f, Sound snd, int id) {
 		synchronized (this.videoPreviewFilesTempLock) {
 			this.audioPreviewFilesTemp[id] = f;
 			this.audioPreviewTempClips[id] = snd;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.AUDIO_PREVIEW_CHANGED,
 			VidencodeChangeEvent.TEMPORARY | (id == PREVIEW_FIRST ? VidencodeChangeEvent.FIRST : VidencodeChangeEvent.LAST)
 		));
 	}
 	private final void onAudioCompleted(File f) {
 		synchronized (this.audioFileTempLock) {
 			this.audioFileTemp = f;
 			this.audioFileTempIsGenerating = false;
 			this.audioFileTempGeneratingPercent = 1.0;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 			VidencodeChangeEvent.TEMPORARY
 		));
 
 		this.clearVideoFileTemp();
 
 		if (this.isOutputEncoding()) this.encodeAll(1, "Audio error");
 	}
 	private final void onAudioProgress(double progress) {
 		synchronized (this.audioFileTempLock) {
 			this.audioFileTempGeneratingPercent = progress;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.AUDIO_FILE_ENCODING_PROGRESS,
 			VidencodeChangeEvent.TEMPORARY
 		).setValue(progress));
 	}
 	private final void onVideoCompleted(File f) {
 		synchronized (this.videoFileTempLock) {
 			this.videoFileTemp = f;
 			this.videoFileTempIsGenerating = false;
 			this.videoFileTempGeneratingPercent = 1.0;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 			VidencodeChangeEvent.TEMPORARY
 		));
 
 		if (this.isOutputEncoding()) this.encodeAll(2, "Video error");
 	}
 	private final void onVideoProgress(double progress) {
 		synchronized (this.videoFileTempLock) {
 			this.videoFileTempGeneratingPercent = progress;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.VIDEO_FILE_ENCODING_PROGRESS,
 			VidencodeChangeEvent.TEMPORARY
 		).setValue(progress));
 	}
 	private final void onMuxCompleted(File f) {
 		synchronized (this.outputLock) {
 			this.outputFileLast = f;
 			this.outputEncoding = false;
 			this.outputIsMuxing = false;
 		}
 
 		this.signalChange(new VidencodeChangeEvent(
 			this,
 			VidencodeChangeEvent.EventType.ENCODE_STATUS,
 			VidencodeChangeEvent.COMPLETE
 		).setValue(1.0).setText(0, "Complete"));
 	}
 
 	// Get/set methods
 	public final Dimension getImageFileSourceDimensions() {
 		Dimension d;
 		synchronized (this.imageFileSourceLock) {
 			d = new Dimension(this.imageFileSourceDimensions);
 		}
 		return d;
 	}
 	public final double getImageFileSourceVideoTime() {
 		double t;
 		synchronized (this.imageFileSourceLock) {
 			t = this.imageFileSourceVideoTime;
 		}
 		return t;
 	}
 	public final void setImageFileSourceVideoTime(double time) {
 		double max = this.getVideoFileSourceDuration();
 		boolean trigger = false;
 
 		if (time < 0.0) time = 0.0;
 		if (time > max) time = max;
 
 		synchronized (this.imageFileSourceLock) {
 			if (this.imageFileSourceVideoTime != time) {
 				this.imageFileSourceVideoTime = time;
 				trigger = true;
 			}
 		}
 
 		if (trigger && this.triggerGenerateImageOnTimeChange) {
 			this.genImage();
 		}
 	}
 
 	public final Dimension getVideoFileSourceDimensions() {
 		Dimension d;
 		synchronized (this.videoFileSourceLock) {
 			d = new Dimension(this.videoFileSourceDimensions);
 		}
 		return d;
 	}
 	public final FrameRate getVideoFileSourceFrameRate() {
 		FrameRate r;
 		synchronized (this.videoFileSourceLock) {
 			r = new FrameRate(this.videoFileSourceFrameRate);
 		}
 		return r;
 	}
 	public final double getVideoFileSourceDuration() {
 		double d;
 		synchronized (this.videoFileSourceLock) {
 			d = this.videoFileSourceDuration;
 		}
 		return d;
 	}
 	public final double[] getVideoFileSourceEncodeDuration() {
 		double[] d = new double[2];
 		synchronized (this.videoFileSourceLock) {
 			d[0] = this.videoFileSourceEncodeDuration[0];
 			d[1] = this.videoFileSourceEncodeDuration[1];
 		}
 		return d;
 	}
 	public final void setVideoFileSourceEncodeDuration(double start, double end) {
 		int changeFlags = 0;
 
 		if (start < 0.0) start = 0.0;
 		boolean sync = false;
 		double ve, ae;
 		synchronized (this.audioFileSourceLock) {
 			ae = this.audioFileSourceEncodeDuration[1] - this.audioFileSourceEncodeDuration[0];
 		}
 		synchronized (this.videoFileSourceLock) {
 			if (end > this.videoFileSourceDuration) end = this.videoFileSourceDuration;
 			if (start > end) start = 0.0;
 
 			if (start != this.videoFileSourceEncodeDuration[0]) {
 				changeFlags |= 1;
 				this.videoFileSourceEncodeDuration[0] = start;
 			}
 			if (end != this.videoFileSourceEncodeDuration[1]) {
 				changeFlags |= 2;
 				this.videoFileSourceEncodeDuration[1] = end;
 			}
 
 			ve = this.videoFileSourceEncodeDuration[1] - this.videoFileSourceEncodeDuration[0];
 			if (ve > ae - Videncode.DECIMAL_THRESHOLD) {
 				this.videoFileSourceSyncOffset = 0.0;
 				sync = true;
 			}
 			else if (ve + this.videoFileSourceSyncOffset > ae) {
 				this.videoFileSourceSyncOffset = Math.max(0.0, ae - ve);
 			}
 		}
 		if (sync) {
 			synchronized (this.audioFileSourceLock) {
 				if (ae + this.audioFileSourceSyncOffset > ve) {
 					this.audioFileSourceSyncOffset = Math.max(0.0, ve - ae);
 				}
 			}
 		}
 		if (this.triggerGenerateVideoPreviewImagesOnEncodingDurationChangeChange && changeFlags != 0) {
 			this.clearVideoFileTemp();
 
 			if ((changeFlags & 1) != 0) {
 				this.genVideoPreviewImages(PREVIEW_FIRST);
 			}
 			if ((changeFlags & 2) != 0) {
 				this.genVideoPreviewImages(PREVIEW_LAST);
 			}
 		}
 	}
 	public final void setVideoFileSourceSyncOffset(double offset) {
 		double ve, ae;
 		synchronized (this.audioFileSourceLock) {
 			ae = this.audioFileSourceEncodeDuration[1] - this.audioFileSourceEncodeDuration[0];
 		}
 		synchronized (this.videoFileSourceLock) {
 			ve = this.videoFileSourceEncodeDuration[1] - this.videoFileSourceEncodeDuration[0];
 			if (ae > ve) {
 				if (ve + offset > ae) offset = ae - ve;
 				this.videoFileSourceSyncOffset = offset;
 			}
 		}
 	}
 	public final double getVideoFileSourceSyncOffset() {
 		double d;
 		synchronized (this.videoFileSourceLock) {
 			d = this.videoFileSourceSyncOffset;
 		}
 		return d;
 	}
 
 	public final double getAudioFileSourceDuration() {
 		double d;
 		synchronized (this.audioFileSourceLock) {
 			d = this.audioFileSourceDuration;
 		}
 		return d;
 	}
 	public final double[] getAudioFileSourceEncodeDuration() {
 		double[] d = new double[2];
 		synchronized (this.audioFileSourceLock) {
 			d[0] = this.audioFileSourceEncodeDuration[0];
 			d[1] = this.audioFileSourceEncodeDuration[1];
 		}
 		return d;
 	}
 	public final void setAudioFileSourceEncodeDuration(double start, double end) {
 		int changeFlags = 0;
 
 		double oldLength;
 		double newLength;
 
 		if (start < 0.0) start = 0.0;
 		boolean sync = false;
 		double ve, ae;
 		synchronized (this.videoFileSourceLock) {
 			ve = this.videoFileSourceEncodeDuration[1] - this.videoFileSourceEncodeDuration[0];
 		}
 		synchronized (this.audioFileSourceLock) {
 			if (end > this.audioFileSourceDuration) end = this.audioFileSourceDuration;
 			if (start > end) start = 0.0;
 
 			oldLength = this.audioFileSourceEncodeDuration[1] - this.audioFileSourceEncodeDuration[0];
 			newLength = end - start;
 
 			if (start != this.audioFileSourceEncodeDuration[0]) {
 				changeFlags |= 1;
 				this.audioFileSourceEncodeDuration[0] = start;
 			}
 			if (end != this.audioFileSourceEncodeDuration[1]) {
 				changeFlags |= 2;
 				this.audioFileSourceEncodeDuration[1] = end;
 			}
 
 			ae = this.audioFileSourceEncodeDuration[1] - this.audioFileSourceEncodeDuration[0];
 			if (ae > ve - Videncode.DECIMAL_THRESHOLD) {
 				this.audioFileSourceSyncOffset = 0.0;
 				sync = true;
 			}
 			else if (ae + this.audioFileSourceSyncOffset > ve) {
 				this.audioFileSourceSyncOffset = Math.max(0.0, ve - ae);
 			}
 		}
 		if (sync) {
 			synchronized (this.videoFileSourceLock) {
 				if (ve + this.videoFileSourceSyncOffset > ae) {
 					this.videoFileSourceSyncOffset = Math.max(0.0, ae - ve);
 				}
 			}
 		}
 
 		if (this.triggerGenerateAudioPreviewClipsOnEncodingDurationChangeChange && changeFlags != 0) {
 			double maxlen = this.getAudioPreviewTempLength();
 
 			// Also update if any length was less than the max clip length
 			this.clearAudioFileTemp();
 			if ((changeFlags & 1) != 0 || (oldLength < maxlen || newLength < maxlen)) {
 				this.genAudioPreviewClips(PREVIEW_FIRST);
 			}
 			if ((changeFlags & 2) != 0 || (oldLength < maxlen || newLength < maxlen)) {
 				this.genAudioPreviewClips(PREVIEW_LAST);
 			}
 		}
 	}
 	public final void setAudioFileSourceSyncOffset(double offset) {
 		double ve, ae;
 		synchronized (this.videoFileSourceLock) {
 			ve = this.videoFileSourceEncodeDuration[1] - this.videoFileSourceEncodeDuration[0];
 		}
 		synchronized (this.audioFileSourceLock) {
 			ae = this.audioFileSourceEncodeDuration[1] - this.audioFileSourceEncodeDuration[0];
 			if (ve > ae) {
 				if (ae + offset > ve) offset = ve - ae;
 				this.audioFileSourceSyncOffset = offset;
 			}
 		}
 	}
 	public final double getAudioFileSourceSyncOffset() {
 		double d;
 		synchronized (this.audioFileSourceLock) {
 			d = this.audioFileSourceSyncOffset;
 		}
 		return d;
 	}
 
 
 	public final long getImageFileTempFileSize() {
 		File f;
 		synchronized (this.imageFileTempLock) {
 			f = this.imageFileTemp;
 		}
 		return (f == null ? 0 : f.length());
 	}
 	public final int getImageFileTempQuality() {
 		int q;
 		synchronized (this.imageFileTempLock) {
 			q = this.imageFileTempQuality;
 		}
 		return q;
 	}
 	public final void setImageFileTempQuality(int quality) {
 		if (quality != -1) {
 			if (quality < this.imageQualityRange[0]) quality = this.imageQualityRange[0];
 			else if (quality > this.imageQualityRange[1]) quality = this.imageQualityRange[1];
 		}
 
 		boolean trigger = false;
 
 		synchronized (this.imageFileTempLock) {
 			if (this.imageFileTempQuality != quality) {
 				this.imageFileTempQuality = quality;
 				trigger = true;
 			}
 		}
 
 		if (trigger && this.triggerGenerateImageOnQualityChange) {
 			this.genImage();
 		}
 	}
 	public final Image getImageFileImageTemp() {
 		Image i;
 		synchronized (this.imageFileTempLock) {
 			i = this.imageFileImageTemp;
 		}
 		return i;
 	}
 	public final Dimension getImageFileTempDimensions() {
 		Dimension d;
 		synchronized (this.imageFileTempLock) {
 			d = new Dimension(this.imageFileTempDimensions);
 		}
 		return d;
 	}
 	public final void setImageFileTempDimensions(Dimension size) {
 		if (size.width <= 0 && size.height <= 0) return;
 
 		Dimension fullSize = this.getImageFileSourceDimensions();
 		if (fullSize.width <= 0 && fullSize.height <= 0) return;
 
 		Dimension newSize = new Dimension(size);
 		if (newSize.width <= 0) {
 			newSize.width = newSize.height * fullSize.width / fullSize.height;
 		}
 		else if (newSize.height <= 0) {
 			newSize.height = newSize.width * fullSize.height / fullSize.width;
 		}
 
 		boolean trigger = false;
 
 		synchronized (this.imageFileTempLock) {
 			if (this.imageFileTempDimensions.width != newSize.width || this.imageFileTempDimensions.height != newSize.height) {
 				this.imageFileTempDimensions.width = newSize.width;
 				this.imageFileTempDimensions.height = newSize.height;
 				trigger = true;
 			}
 		}
 
 		if (trigger && this.triggerGenerateImageOnScaleChange) {
 			this.genImage();
 		}
 	}
 
 	public final Image getVideoPreviewTempImage(int id) {
 		Image i;
 		synchronized (this.videoPreviewFilesTempLock) {
 			i = this.videoPreviewTempImages[id];
 		}
 		return i;
 	}
 
 	public final long getVideoFileTempFileSize() {
 		File f;
 		synchronized (this.videoFileTempLock) {
 			f = this.videoFileTemp;
 		}
 		return (f == null ? 0 : f.length());
 	}
 	public final Dimension getVideoFileTempDimensions() {
 		Dimension d;
 		synchronized (this.videoFileTempLock) {
 			d = new Dimension(this.videoFileTempDimensions);
 		}
 		return d;
 	}
 	public final void setVideoFileTempDimensions(Dimension size) {
 		if (size.width <= 0 && size.height <= 0) return;
 
 		Dimension fullSize = this.getVideoFileSourceDimensions();
 		if (fullSize.width <= 0 && fullSize.height <= 0) return;
 
 		Dimension newSize = new Dimension(size);
 		if (newSize.width <= 0) {
 			newSize.width = newSize.height * fullSize.width / fullSize.height;
 		}
 		else if (newSize.height <= 0) {
 			newSize.height = newSize.width * fullSize.height / fullSize.width;
 		}
 
 		boolean trigger = false;
 
 		synchronized (this.videoFileTempLock) {
 			if (this.videoFileTempDimensions.width != newSize.width || this.videoFileTempDimensions.height != newSize.height) {
 				this.videoFileTempDimensions.width = newSize.width;
 				this.videoFileTempDimensions.height = newSize.height;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			this.clearVideoFileTemp();
 		}
 	}
 	public final FrameRate getVideoFileTempFrameRate() {
 		FrameRate r;
 		synchronized (this.videoFileTempLock) {
 			r = new FrameRate(this.videoFileTempFrameRate);
 		}
 		return r;
 	}
 	public final void setVideoFileTempFrameRate(FrameRate rate) {
 		boolean trigger = false;
 
 		synchronized (this.videoFileTempLock) {
 			if (!rate.equals(this.videoFileTempFrameRate)) {
 				this.videoFileTempFrameRate.setFrameRate(rate);
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			// Change
 			this.clearVideoFileTemp();
 		}
 	}
 	public final boolean isVideoFileTempEncoding() {
 		boolean b;
 		synchronized (this.videoFileTempLock) {
 			b = this.videoFileTempIsGenerating;
 		}
 		return b;
 	}
 	public final double getVideoFileTempEncodingPercent() {
 		double d;
 		synchronized (this.videoFileTempLock) {
 			d = this.videoFileTempGeneratingPercent;
 		}
 		return d;
 	}
 	public final int getVideoFileTempEncodingThreads() {
 		int t;
 		synchronized (this.videoFileTempLock) {
 			t = this.videoFileTempEncodingThreads;
 		}
 		return t;
 	}
 	public final void setVideoFileTempEncodingThreads(int t) {
 		int m = this.getOutputMaxThreads();
 		synchronized (this.videoFileTempLock) {
 			this.videoFileTempEncodingThreads = Math.max(1, Math.min(m, t));
 		}
 	}
 	public final int getVideoFileTempQuality() {
 		int t;
 		synchronized (this.videoFileTempLock) {
 			t = this.videoFileTempQuality;
 		}
 		return t;
 	}
 	public final void setVideoFileTempQuality(int value) {
 		boolean trigger = false;
 
 		synchronized (this.videoFileTempLock) {
 			if (this.videoFileTempQuality != value) {
 				this.videoFileTempQuality = value;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			this.clearVideoFileTemp();
 		}
 	}
 
 
 	public final Bitrate getAudioFileTempBitrate() {
 		Bitrate br;
 		synchronized (this.audioFileTempLock) {
 			br = this.audioFileTempBitrate;
 		}
 		return br;
 	}
 	public final void setAudioFileTempBitrate(Bitrate rate) {
 		boolean trigger = false;
 
 		synchronized (this.audioFileTempLock) {
 			if (!rate.equals(this.audioFileTempBitrate)) {
 				this.audioFileTempBitrate = rate;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			// Change
 			this.clearAudioFileTemp();
 			this.genAudioPreviewClips(PREVIEW_FIRST);
 			this.genAudioPreviewClips(PREVIEW_LAST);
 		}
 	}
 	public final AudioChannelCount getAudioFileTempChannels() {
 		AudioChannelCount acc;
 		synchronized (this.audioFileTempLock) {
 			acc = this.audioFileTempChannels;
 		}
 		return acc;
 	}
 	public final void setAudioFileTempChannels(AudioChannelCount acc) {
 		boolean trigger = false;
 
 		synchronized (this.audioFileTempLock) {
 			if (!acc.equals(this.audioFileTempChannels)) {
 				this.audioFileTempChannels = acc;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			// Change
 			this.clearAudioFileTemp();
 			this.genAudioPreviewClips(PREVIEW_FIRST);
 			this.genAudioPreviewClips(PREVIEW_LAST);
 		}
 	}
 	public final long getAudioFileTempApproximateSize() {
 		Bitrate rate = this.getAudioFileTempBitrate();
 		double[] encodeDuration = this.getAudioFileSourceEncodeDuration();
 
 		return (long) Math.ceil((encodeDuration[1] - encodeDuration[0]) * rate.getBitrate() / 8);
 	}
 	public final AudioSampleRate getAudioFileTempSampleRate() {
 		AudioSampleRate rate;
 		synchronized (this.audioFileTempLock) {
 			rate = this.audioFileTempSampleRate;
 		}
 		return rate;
 	}
 	public final void setAudioFileTempSampleRate(AudioSampleRate rate) {
 		boolean trigger = false;
 
 		synchronized (this.audioFileTempLock) {
 			if (!rate.equals(this.audioFileTempSampleRate)) {
 				this.audioFileTempSampleRate = rate;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			// Change
 			this.clearAudioFileTemp();
 			this.genAudioPreviewClips(PREVIEW_FIRST);
 			this.genAudioPreviewClips(PREVIEW_LAST);
 		}
 	}
 	public final boolean isAudioFileTempEncoding() {
 		boolean b;
 		synchronized (this.audioFileTempLock) {
 			b = this.audioFileTempIsGenerating;
 		}
 		return b;
 	}
 	public final double getAudioFileTempEncodingPercent() {
 		double d;
 		synchronized (this.audioFileTempLock) {
 			d = this.audioFileTempGeneratingPercent;
 		}
 		return d;
 	}
 	public final long getAudioFileTempFileSize() {
 		File f;
 		synchronized (this.audioFileTempLock) {
 			f = this.audioFileTemp;
 		}
 		return (f == null ? 0 : f.length());
 	}
 
 	public final double getAudioPreviewTempLength() {
 		double d;
 		synchronized (this.audioPreviewFilesTempLock) {
 			d = this.audioPreviewTempLength;
 		}
 		return d;
 	}
 
 
 	public final void resetSyncState() {
 		for (int i = 0; i < this.syncAudioFade.length; ++i) {
 			this.syncAudioFade[i] = false;
 		}
 		for (int i = 0; i < this.syncAudioState.length; ++i) {
 			this.syncAudioState[i] = SYNC_NOTHING;
 		}
 		for (int i = 0; i < this.syncVideoFade.length; ++i) {
 			this.syncVideoFade[i] = true;
 		}
 		for (int i = 0; i < this.syncVideoState.length; ++i) {
 			this.syncVideoState[i] = SYNC_EXTERNAL;
 		}
 	}
 	public final boolean getSyncAudioUseFade(boolean start) {
 		return this.syncAudioFade[start ? 0 : 1];
 	}
 	public final void setSyncAudioUseFade(boolean start, boolean enabled) {
 		this.syncAudioFade[start ? 0 : 1] = enabled;
 	}
 	public final int getSyncAudioState(boolean start) {
 		return this.syncAudioState[start ? 0 : 1];
 	}
 	public final void setSyncAudioState(boolean start, int state) {
 		this.syncAudioState[start ? 0 : 1] = state;
 	}
 	public final boolean getSyncVideoUseFade(boolean start) {
 		return this.syncVideoFade[start ? 0 : 1];
 	}
 	public final void setSyncVideoUseFade(boolean start, boolean enabled) {
 		this.syncVideoFade[start ? 0 : 1] = enabled;
 	}
 	public final int getSyncVideoState(boolean start) {
 		return this.syncVideoState[start ? 0 : 1];
 	}
 	public final void setSyncVideoState(boolean start, int state) {
 		this.syncVideoState[start ? 0 : 1] = state;
 	}
 
 
 	public final int getOutputMaxThreads() {
 		return this.outputMaxThreads;
 	}
 	public final File getOutputFileLast() {
 		File f;
 		synchronized (this.outputLock) {
 			f = this.outputFileLast;
 		}
 		return f;
 	}
 	public final long getOutputMetadataLength() {
 		int tagLen = this.getOutputTagLength();
 
 		File audioFile = this.getAudioFileTemp();
 		File videoFile = this.getVideoFileTemp();
 		double[] videoTime = this.getVideoFileSourceEncodeDuration();
 		double[] audioTime = this.getAudioFileSourceEncodeDuration();
 		boolean mux = videoFile != null && audioFile != null && Videncode.withinThreshold(videoTime[1] - videoTime[0], audioTime[1] - audioTime[0]);
 
 		return (
 			7 + 1 + // signature + version
 			1 + (mux ? 1 : 0) + // flags1 + flags2
 			Videncode.getVarLenIntLength(tagLen, 5) + tagLen + // tag
 			5 + 5 // max for video/audio
 		) + (mux ? this.outputAdditionalMuxSpace : 0); // additional space for muxing
 	}
 	public final long getOutputMaxFileSize() {
 		long s;
 		synchronized (this.outputLock) {
 			s = this.outputMaxFileSize;
 		}
 		return s;
 	}
 	public final void setOutputMaxFileSize(long size) {
 		boolean trigger = false;
 
 		synchronized (this.outputLock) {
 			if (this.outputMaxFileSize != size) {
 				this.outputMaxFileSize = size;
 				trigger = true;
 			}
 		}
 
 		if (trigger) {
 			// Events
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.VIDEO_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.AUDIO_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 			this.signalChange(new VidencodeChangeEvent(
 				this,
 				VidencodeChangeEvent.EventType.IMAGE_FILE_CHANGED,
 				VidencodeChangeEvent.SOURCE | VidencodeChangeEvent.TEMPORARY
 			));
 		}
 	}
 	public final int getOutputTagMaxLength() {
 		int len;
 		synchronized (this.outputLock) {
 			len = this.outputTagMaxLength;
 		}
 		return len;
 	}
 	public final String getOutputTag() {
 		String s;
 		synchronized (this.outputLock) {
 			s = this.outputTag;
 		}
 		return s;
 	}
 	public final byte[] getOutputTagBytes() {
 		byte[] b;
 		synchronized (this.outputLock) {
 			b = this.outputTagBytes;
 		}
 		return b;
 	}
 	public final int getOutputTagLength() {
 		int i;
 		synchronized (this.outputLock) {
 			i = this.outputTagBytes.length;
 		}
 		return Math.min(i, this.outputTagMaxLength);
 	}
 	public final void setOutputTag(String tag) {
 		boolean trigger = false;
 
 		synchronized (this.outputLock) {
 			try {
 				byte[] b = tag.getBytes("UTF-8");
 				if (Math.min(b.length, this.outputTagMaxLength) > Math.min(this.outputTagBytes.length, this.outputTagMaxLength)) {
 					trigger = true;
 				}
 				this.outputTagBytes = b;
 				this.outputTag = new String(this.outputTagBytes, 0, Math.min(this.outputTagBytes.length, this.outputTagMaxLength), "UTF-8");
 			}
 			catch (Exception e) {
 			}
 		}
 
 		if (trigger) {
 			synchronized (this.videoFileTempLock) {
 				this.videoFileTempMightNeedReEncoding = true;
 			}
 		}
 	}
 	public final String getOutputFilename() {
 		String s;
 		synchronized (this.outputLock) {
 			s = this.outputFilename;
 		}
 		return s;
 	}
 	public final void setOutputFilename(String fn) {
 		synchronized (this.outputLock) {
 			this.outputFilename = fn;
 		}
 	}
 	public final String getOutputExtension() {
 		String s;
 		synchronized (this.outputLock) {
 			s = this.outputExtension;
 		}
 		return s;
 	}
 	public final void setOutputExtension(String fn) {
 		synchronized (this.outputLock) {
 			this.outputExtension = fn;
 		}
 	}
 	public final String getOutputExtensionDefault() {
 		String s;
 		synchronized (this.outputLock) {
 			s = this.outputExtensionDefault;
 		}
 		return s;
 	}
 	public final void setOutputExtensionDefault(String fn) {
 		synchronized (this.outputLock) {
 			this.outputExtensionDefault = fn;
 		}
 	}
 	public final boolean isOutputMuxing() {
 		boolean b;
 		synchronized (this.outputLock) {
 			b = this.outputIsMuxing;
 		}
 		return b;
 	}
 
 
 
 	// Global settings
 	public final int[] getImageQualityRange() {
 		return this.imageQualityRange;
 	}
 	public final int getImageQualityDefault() {
 		return this.imageQualityDefault;
 	}
 	public final void setImageQualityDefault(int quality) {
 		if (quality < this.imageQualityRange[0]) quality = this.imageQualityRange[0];
 		else if (quality > this.imageQualityRange[1]) quality = this.imageQualityRange[1];
 
 		this.imageQualityDefault = quality;
 	}
 
 	public final int[] getResolutions() {
 		return this.resolutions;
 	}
 	public final FrameRate[] getFramerates() {
 		return this.videoFrameRates;
 	}
 	public final AutoQualityProfile[] getVideoAutoQualityProfiles() {
 		return this.videoAutoQualityProfiles;
 	}
 	public final AutoQualityProfile getVideoAutoQualityProfileDefault() {
 		return this.videoAutoQualityProfileDefault;
 	}
 	public final void setVideoAutoQualityProfileDefault(AutoQualityProfile profile) {
 		this.videoAutoQualityProfileDefault = profile;
 	}
 
 	public final Bitrate[] getAudioBitrates() {
 		return this.audioBitrates;
 	}
 	public final AudioChannelCount[] getAudioChannels() {
 		return this.audioChannels;
 	}
 	public final AudioSampleRate[] getAudioSampleRates() {
 		return this.audioSampleRates;
 	}
 	public final Bitrate getAudioBitrateDefault() {
 		return this.audioBitrateDefault;
 	}
 	public final void setAudioBitrateDefault(Bitrate rate) {
 		this.audioBitrateDefault = rate;
 	}
 	public final AudioChannelCount getAudioChannelCountDefault() {
 		return this.audioChannelsDefault;
 	}
 	public final void setAudioChannelCountDefault(AudioChannelCount acc) {
 		this.audioChannelsDefault = acc;
 	}
 	public final AudioSampleRate getAudioSampleRateDefault() {
 		return this.audioSampleRateDefault;
 	}
 	public final void setAudioSampleRateDefault(AudioSampleRate rate) {
 		this.audioSampleRateDefault = rate;
 	}
 
 	public final String[] getVideoEncodingProfiles() {
 		return this.videoEncodingProfiles;
 	}
 	public final String[] getVideoEncodingProfileDescriptions() {
 		return this.videoEncodingProfileDescriptions;
 	}
 	public final int getVideoEncodingProfileDefault() {
 		return this.videoEncodingProfileDefault;
 	}
 	public final void setVideoEncodingProfileDefault(int mode) {
 		if (mode < 0) mode = 0;
 		else if (mode >= this.videoEncodingProfiles.length) mode = this.videoEncodingProfiles.length - 1;
 
 		this.videoEncodingProfileDefault = mode;
 	}
 
 
 	public final AutoQuality getAutoQuality(AutoQualityProfile qualityProfile, Dimension videoSize, double framerate, double encodeDuration, long maxSpace) {
 		AutoQuality q = new AutoQuality();
 
 		double minBits = qualityProfile.getMinBitsPerPixel();
 
 		int resHeight = videoSize.height;
 		int resWidth = videoSize.width;
 		int i = this.resolutions.length;
 		double bitrate = maxSpace * 8 / Math.max(1.0, encodeDuration);
 
 		while (i >= 0) {
 			if ((bitrate / (8 * 3 * resHeight * resWidth * framerate)) >= minBits) break;
 
 			// Next
 			for (--i; i >= 0; --i) {
 				if (this.resolutions[i] < resHeight) {
 					resHeight = resolutions[i];
 					resWidth = videoSize.width * resolutions[i] / videoSize.height;
 					break;
 				}
 			}
 		}
 
 		q.resolution.width = resWidth;
 		q.resolution.height = resHeight;
 		q.bitrate = bitrate;
 
 		return q;
 	}
 
 
 	// Event listeners
 	private void signalChange(final VidencodeChangeEvent event) {
 		for (int i = 0; i < this.videoChangeListeners.size(); ++i) {
 			final VidencodeEventListener listener = this.videoChangeListeners.get(i);
 
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					listener.onVidencodeChange(event);
 				}
 			});
 			//this.videoChangeListeners.get(i).onVidencodeChange(event);
 		}
 	}
 
 	public final void addChangeListener(VidencodeEventListener el) {
 		this.videoChangeListeners.add(el);
 	}
 	public final void removeChangeListener(VidencodeEventListener el) {
 		this.videoChangeListeners.remove(el);
 	}
 
 
 	// Other
 	public final void testFFmpegInstall(final Runnable r) {
 		final Videncode self = this;
 		Thread t = new Thread() {
 			@Override
 			public final void run() {
 				// Test ffmpeg
 				Process p = null;
 				try {
 					p = Runtime.getRuntime().exec(new String[]{ "ffmpeg" , "-version" }, null, self.getTempDir());
 				}
 				catch (IOException e) {
 					p = null;
 				}
 
 				if (p == null) {
 					// FFmpeg not installed
 					if (r != null) {
 						SwingUtilities.invokeLater(r);
 						return;
 					}
 				}
 
 				// Test ffprobe
 				p = null;
 				try {
 					p = Runtime.getRuntime().exec(new String[]{ "ffprobe" , "-version" }, null, self.getTempDir());
 				}
 				catch (IOException e) {
 					p = null;
 				}
 
 				if (p == null) {
 					// FFmpeg not installed
 					if (r != null) {
 						SwingUtilities.invokeLater(r);
 						return;
 					}
 				}
 			}
 		};
 		t.start();
 
 	}
 
 	// Common
 	public static final String getFileNameNoExt(File f) {
 		if (f == null) return "";
 		int i = f.getName().lastIndexOf('.');
 		return (i >= 0 ? f.getName().substring(0, i) : f.getName());
 	}
 	public static final String getFileExt(File f) {
 		if (f == null) return "";
 		int i = f.getName().lastIndexOf('.');
 		return (i >= 0 ? f.getName().substring(i) : "");
 	}
 
 	public static final double parseTimecodeToSeconds(String timecode, double dfault) {
 		Matcher m = Videncode.timecodePattern.matcher(timecode);
 		if (m.matches()) {
 			String hours = m.group(2);
 			String minutes = m.group(4);
 			String seconds = m.group(6);
 			if (minutes == null && m.group(1) != null && m.group(1).indexOf('h') <= 0 && m.group(1).indexOf('H') <= 0) {
 				minutes = hours;
 				hours = null;
 			}
 
 			int hrs = 0;
 			int min = 0;
 			double sec = 0.0;
 
 			if (hours != null) {
 				try {
 					hrs = Integer.parseInt(hours);
 				}
 				catch (NumberFormatException e) {
 					return dfault;
 				}
 			}
 			if (minutes != null) {
 				try {
 					min = Integer.parseInt(minutes);
 				}
 				catch (NumberFormatException e) {
 					return dfault;
 				}
 			}
 			if (seconds != null) {
 				try {
 					sec = Double.parseDouble(seconds);
 				}
 				catch (NumberFormatException e) {
 					return dfault;
 				}
 			}
 
 			return sec + 60 * (min + 60 * hrs);
 		}
 
 		return dfault;
 	}
 
 	public static final String getNullStreamName() {
 		return (Videncode.isWindows() ? "NUL" : "/dev/null");
 	}
 
 	public static final Image createImageFromFile(File f, Component guiComponent) {
 		if (f == null) return null;
 		try {
 			return Videncode.createImageFromFile(f.toURI().toURL(), guiComponent);
 		}
 		catch (MalformedURLException e) {
 			return null;
 		}
 	}
 	public static final Image createImageFromFile(URL url, Component guiComponent) {
 		if (url == null) return null;
 
 		try {
 			//Image img = ImageIO.read(f);
 			Image img = Toolkit.getDefaultToolkit().createImage(url);
 
 			// Wait for load
 			MediaTracker tracker = new MediaTracker(guiComponent);
 			tracker.addImage(img, 1);
 			try {
 				tracker.waitForAll();
 			}
 			catch (InterruptedException e) {
 				img = null;
 			}
 			return img;
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 
 	private static void copyFile(File sourceFile, File destFile) throws IOException {
 		if (!destFile.exists()) {
 			destFile.createNewFile();
 		}
 
 		FileChannel source = null;
 		FileChannel destination = null;
 
 		try {
 			source = new FileInputStream(sourceFile).getChannel();
 			destination = new FileOutputStream(destFile).getChannel();
 			destination.transferFrom(source, 0, source.size());
 		}
 		finally {
 			if (source != null) {
 				source.close();
 			}
 			if (destination != null) {
 				destination.close();
 			}
 		}
 	}
 
 	public static final String intAddComas(int number) {
 		StringBuilder sb = new StringBuilder();
 		if (number < 0) {
 			sb.append("-");
 			number = -number;
 		}
 
 		sb.append(Integer.valueOf(number).toString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1" + Main.getComma()));
 
 		return sb.toString();
 	}
 	public static final String strAddComas(String str) {
 		return str.replaceAll("(\\d)(?=(\\d{3})+$)", "$1" + Main.getComma());
 	}
 	public static final String numberToLabeledSize(double number, int minLenForCommas, int decimalLength, int cutoffRange, int divisionValue, final String[] suffixes) {
 		int i = 2;
 		String temp;
 		StringBuilder sb = new StringBuilder();
 
 		// Negative
 		if (number < 0) {
 			sb.append("-");
 			number = -number;
 		}
 
 		// 1
 		if (number == 1) {
 			sb.append("1");
 			i = 1;
 		}
 		else {
 			// Get base value
 			if (suffixes != null) {
 				for (; i < suffixes.length && number >= cutoffRange; ++i) {
 					number /= divisionValue;
 				}
 			}
 
 			// Value
 			StringBuilder format = new StringBuilder();
 			format.append("0");
 			if (decimalLength > 0) {
 				format.append('.');
 				for (int j = 0; j < decimalLength; ++j) format.append('#');
 			}
 			temp = (new DecimalFormat(format.toString()).format(number));
 			if (minLenForCommas >= 0) {
 				// Add commas
 				Matcher m = Pattern.compile("[0-9]{" + minLenForCommas + ",}").matcher(temp);
 				if (m.matches()) {
 					temp = strAddComas(m.group()) + temp.substring(m.group().length());
 				}
 			}
 			sb.append(temp);
 		}
 
 		// Done
 		if (suffixes != null) sb.append(suffixes[i - 1]);
 		return sb.toString();
 	}
 
 	public static final String timeToString(double time) {
 		return Videncode.timeToString(time, Videncode.defaultTimecodeLengths, Videncode.defaultTimecodeDelimiters);
 	}
 	public static final String timeToString(double time, final int[] timecodeLengths) {
 		return Videncode.timeToString(time, timecodeLengths, Videncode.defaultTimecodeDelimiters);
 	}
 	/**
 		@param time
 			the time in seconds to encode
 		@param decimals
 			the max number of decimals to include
 		@param timecodeLengths
 			An array of [hoursLength, minutesLength, secondsLength, decimals]
 
 			For the first 3:
 			if the number is no-negative, a value of 2 will be propagated to the next fields (if the value is non-zero)
 			examples:
 				2 = always 2
 				1 = at least 1
 				0 = optional
 				-1 = optional without 2 propagation
 
 			For "decimals", it's the max number of decimals to include
 		@param delimiters
 	*/
 	public static final String timeToString(double time, final int[] timecodeLengths, final String[] delimiters) {
 		int[] tcLen = Arrays.copyOf(Videncode.defaultTimecodeLengths, Videncode.defaultTimecodeLengths.length);
 		if (timecodeLengths != null) {
 			for (int i = 0; i < timecodeLengths.length; ++i) {
 				tcLen[i] = timecodeLengths[i];
 			}
 		}
 
 		int t = (int) time;
 		int i, x, len, div = 3600;
 		time -= t;
 		StringBuilder sb = new StringBuilder();
 
 		// H:M:S
 		for (i = 0; i < 3; ++i) {
 			len = tcLen[i];
 			x = t / div;
 
 			if (x > 0 || len > 0 || (i == 2 && len >= 0 && sb.length() == 0)) {
 				if (x < 10 && len >= 2) sb.append('0'); // 0-padding
 				if (i == 2) {
 					// Seconds + decimal
 					StringBuilder format = new StringBuilder();
 					format.append("0");
 					if (tcLen[3] > 0) {
 						format.append('.');
 						for (int j = 0; j < tcLen[3]; ++j) format.append('#');
 					}
 					sb.append(new DecimalFormat(format.toString()).format(x + time));
 				}
 				else {
 					sb.append(x); // number
 				}
 				sb.append(delimiters == null || delimiters.length <= i ? Videncode.defaultTimecodeDelimiters[i] : delimiters[i]); // delimiter
 
 				// propagate
 				if (len >= 0) {
 					for (int j = i + 1; j < tcLen.length - 1; ++j) {
 						tcLen[j] = 2;
 					}
 				}
 
 				t -= x * div;
 			}
 
 			div /= 60;
 		}
 
 		return sb.toString();
 	}
 
 	public static final boolean withinThreshold(double x, double y) {
 		return Math.abs(x - y) < Videncode.DECIMAL_THRESHOLD;
 	}
 
 	public static final String fixFilename(String filename) {
 		if (Videncode.isWindows()) {
 			return filename.replaceAll("[\\\\\\/\\:\\*\\?\"<>\\|]", "");
 		}
 		else {
 			return filename.replaceAll("[\\/\\:]", "");
 		}
 	}
 
 	public static final int getVarLenIntLength(int value, int maxlen) {
 		int i = 0;
 		for (; i < maxlen; ++i) {
 			value = value >>> 7;
 			if (value == 0) return i + 1;
 		}
 		return i;
 	}
 
 	public static final boolean isWindows() {
 		return (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0);
 	}
 
 	public static final String intToLabeledString(long number, int divisionValue, final String[] suffixes) {
 		int i = 2;
 		String temp;
 		StringBuilder sb = new StringBuilder();
 
 		// Negative
 		if (number < 0) {
 			sb.append("-");
 			number = -number;
 		}
 
 		// 1
 		if (number == 1) {
 			sb.append("1");
 			i = 1;
 		}
 		else {
 			// Get base value
 			if (suffixes != null) {
 				for (; i < suffixes.length && number >= divisionValue && (number / divisionValue) * divisionValue == number; ++i) {
 					number /= divisionValue;
 				}
 			}
 
 			// Get integer part
 			sb.append(number);
 		}
 
 		// Done
 		if (suffixes != null) sb.append(suffixes[i - 1]);
 		return sb.toString();
 	}
 	public static final long labeledStringToInt(String value, int divisionValue, final String[] suffixes) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("(?i)\\s*([\\.eE0-9]+)\\s*(");
 		for (int i = 0; i < suffixes.length; ++i) {
 			sb.append('|');
 			sb.append(suffixes[i]);
 		}
 		sb.append(")?(\\s+.*)?");
 		Pattern pattern = Pattern.compile(sb.toString());
 
 		Matcher m = pattern.matcher(value);
 		if (m.matches()) {
 			try {
 				double d = Double.parseDouble(m.group(1));
 				int sId = 0;
 				for (int i = 0; i < suffixes.length; ++i) {
 					if (Pattern.compile("(?i)" + suffixes[i]).matcher(m.group(2)).matches()) {
 						sId = i;
 						break;
 					}
 				}
 				sId = Math.max(0, sId - 1);
 				for (int i = 0; i < sId; ++i) d *= divisionValue;
 				return (long) d;
 			}
 			catch (NumberFormatException e) {}
 		}
 		return 0L;
 	}
 }
 
 
