 package com.iorga.webappwatcher.web;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipFile;
 import org.apache.commons.compress.utils.IOUtils;
 import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.UploadedFile;
 import org.tukaani.xz.CorruptedInputException;
 
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.iorga.webappwatcher.EventLogManager;
 import com.iorga.webappwatcher.eventlog.EventLog;
 import com.iorga.webappwatcher.eventlog.RequestEventLog;
 import com.iorga.webappwatcher.eventlog.RequestEventLog.Parameter;
 import com.iorga.webappwatcher.eventlog.SystemEventLog;
 import com.iorga.webappwatcher.web.StatisticsAction.DurationPerPrincipalStats.PrincipalContext;
 import com.iorga.webappwatcher.web.StatisticsAction.DurationPerPrincipalStats.TimeSlice;
 import com.iorga.webappwatcher.web.StatisticsAction.DurationPerPrincipalStats.TimeSlice.StatsPerPrincipal;
 
 @ManagedBean
 @ViewScoped
 public class StatisticsAction implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	private static final long AGGLOMERATION_DURATION_MILLIS = 10 * 60 * 1000;
 	private static final long NULL_AFTER_PROCESS_DATE_DURATION_MILLIS = 45 * 1000;
 	private static final long TIME_SLICE_DURATION_MILLIS = 30 * 60 * 1000; // 30mn
 	private static final int MAX_ITEMS_FOR_DISPERSION_TABLES = 12;
 
 	private List<UploadedFile> uploadedFiles = new ArrayList<UploadedFile>();
 
 	private int nbItemsForDispersionTables = 6;
 	private int timeSliceDurationMinutes = 30;
 
 	private DurationPerPrincipalStats durationPerPrincipalStats;
 	private String cpuUsageJsonValues;
 	private String memoryUsageJsonValues;
 	private String nbUsersJsonValues;
 	private String durationsFor1clickMedianJsonValues;
 	private String durationsFor1clickSeriesJson;
 	private int lastNbItemsForDispersionTables;
 	private int lastTimeSliceDurationMinutes;
 
 	private int minMillisForSlowRequests = 100000;
 
 	private static class CSVDurationStatsLine {
 		private Date startDate = null;
 		private final Set<String> principals = new HashSet<String>();
 		private final DescriptiveStatistics durations = new DescriptiveStatistics();
 	}
 
 	public static class DurationPerPrincipalStats {
 		public static class TimeSlice {
 			private final Date startDate;
 			private final Date endDate;
 			private final DescriptiveStatistics durationsBetween2clicks = new DescriptiveStatistics();
 			private final DescriptiveStatistics durationsFor1click = new DescriptiveStatistics();
 			private final DescriptiveStatistics cpuUsage = new DescriptiveStatistics();
 			private final DescriptiveStatistics memoryUsage = new DescriptiveStatistics();
 
 			public static class StatsPerPrincipal {
 				private final DescriptiveStatistics durationsBetween2clicks = new DescriptiveStatistics();
 				private final DescriptiveStatistics durationsFor1click = new DescriptiveStatistics();
 			}
 			private final Map<String, StatsPerPrincipal> statsPerPrincipal = Maps.newHashMap();
 
 			public TimeSlice(final Date startDate, final long timeSliceDurationMillis) {
 				this.startDate = startDate;
 				this.endDate = new Date(startDate.getTime() + timeSliceDurationMillis);
 			}
 		}
 		public static class PrincipalContext {
 			private Date lastClick;
 		}
 		private final List<TimeSlice> timeSlices = Lists.newArrayList();	// to search by dichotomy
 		private int lastAccessedTimeSliceIndex = -1;
 		private final Map<String, PrincipalContext> principalContexts = Maps.newHashMap();
 		private final DescriptiveStatistics totalDurationsBetween2clicks = new DescriptiveStatistics();
 		private final DescriptiveStatistics totalDurationsFor1click = new DescriptiveStatistics();
 	}
 
 	public static interface RequestActionFilter {
 		public boolean isAnActionRequest(final RequestEventLog requestEventLog);
 	}
 
 	public static class JSF21AndRichFaces4RequestActionFilter implements RequestActionFilter {
 		@Override
 		public boolean isAnActionRequest(final RequestEventLog requestEventLog) {
 			final String requestURI = requestEventLog.getRequestURI();
 			return !requestURI.contains("/javax.faces.resource/") && !requestURI.contains("/rfRes/")
 				&& (requestEventLog.getMethod().equals("GET") || isNotAnAjaxPostOrIsNotAPollerRequest(requestEventLog));
 		}
 
 		private boolean isNotAnAjaxPostOrIsNotAPollerRequest(final RequestEventLog requestEventLog) {
 //			String javaxEvent = null;
 			String javaxSource = null;
 			boolean ajaxEvent = false;
 			for (final Parameter parameter : requestEventLog.getParameters()) {
 				final String parameterName = parameter.getName();
 				if (parameterName.equals("AJAX:EVENTS_COUNT")) {
 					ajaxEvent = true;
 //				} else if (parameterName.equals("javax.faces.partial.event")) {
 //					javaxEvent = parameter.getValues()[0];
 				} else if (parameterName.equals("javax.faces.source")) {
 					javaxSource = parameter.getValues()[0];
 				}
 			}
 //			return !ajaxEvent || ("click".equals(javaxEvent) || "change".equals(javaxEvent) || "keydown".equals(ajaxEvent) || "rich:datascroller:onscroll".equals(ajaxEvent));
 			return !ajaxEvent || !javaxSource.contains("poller");
 		}
 	}
 
 	private abstract class UploadedFileReader {
 
 		protected void init() {}
 
 		public void readUploadedFiles() throws IOException, ClassNotFoundException {
 			init();
 			for (final UploadedFile uploadedFile : uploadedFiles) {
 				handleUploadedFileInputStream(uploadedFile.getInputstream(), uploadedFile.getFileName());
 			}
 		}
 
 		protected void handleUploadedFileInputStream(final InputStream uploadedFileinputstream, final String uploadedFileName) throws IOException, ClassNotFoundException {
 			if (uploadedFileName.endsWith(".zip")) {
 				// it's a zip, we must iterate on each files inside
 				// we will first copy the zip file in order to access it via ZipFile
 				final File tempZipFile = File.createTempFile("waw", ".zip");
 				IOUtils.copy(uploadedFileinputstream, new FileOutputStream(tempZipFile));
 				try {
 					final ZipFile zipFile = new ZipFile(tempZipFile);
 					// sort the files in order to read them ascending
 					final List<ZipArchiveEntry> sortedZipArchiveEntries = Ordering.from(new Comparator<ZipArchiveEntry>() {
 						@Override
 						public int compare(final ZipArchiveEntry o1, final ZipArchiveEntry o2) {
 							return o1.getName().compareTo(o2.getName());
 						}
 					}).sortedCopy(new Iterable<ZipArchiveEntry>() {
 						@Override
 						public Iterator<ZipArchiveEntry> iterator() {
 							return Iterators.forEnumeration(zipFile.getEntries());
 						}
 					});
 					for (final ZipArchiveEntry zipArchiveEntry : sortedZipArchiveEntries) {
 						handleInputStreamAndFileName(zipFile.getInputStream(zipArchiveEntry), zipArchiveEntry.getName());
 					}
 				} finally {
 					tempZipFile.delete();
 				}
 			} else {
 				handleInputStreamAndFileName(uploadedFileinputstream, uploadedFileName);
 			}
 		}
 
 		private void handleInputStreamAndFileName(final InputStream uploadedFileinputstream, final String uploadedFileName) throws IOException, ClassNotFoundException, FileNotFoundException {
 			handleObjectInputStream(EventLogManager.readLog(uploadedFileinputstream, uploadedFileName));
 		}
 
 		private void handleObjectInputStream(final ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
 			try {
 				EventLog eventLog;
 				try {
 					while ((eventLog = readEventLog(objectInputStream)) != null) {
 						handleEventLog(eventLog);
 					}
 				} catch (final EOFException e) {
 					// Normal end of the read file
 				}
 			} finally {
 				objectInputStream.close();
 			}
 		}
 
 		protected abstract void handleEventLog(EventLog eventLog) throws IOException;
 	}
 
 	/// Actions ///
 	//////////////
 	public void extractDurationStats() throws IOException, ClassNotFoundException {
 		// based on http://stackoverflow.com/a/9394237/535203
 		final FacesContext facesContext = FacesContext.getCurrentInstance();
 
 		final HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
 		response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
 		response.setContentType("text/csv"); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
 //		response.setContentLength(contentLength); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
 		response.setHeader("Content-Disposition", "attachment; filename=\"extract_duration.csv\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
 
 		final ServletOutputStream outputStream = response.getOutputStream();
 
 		outputStream.println("Start date;End date;Distinct users;Number of requests;Duration;Average;Median;90c;Min;Max");
 
 		new UploadedFileReader() {
 			CSVDurationStatsLine csvLine = new CSVDurationStatsLine();
 
 			RequestEventLog requestEventLog = null;
 
 			RequestActionFilter requestActionFilter = new JSF21AndRichFaces4RequestActionFilter();
 
 			@Override
 			protected void handleEventLog(final EventLog eventLog) throws IOException {
 				if (eventLog instanceof RequestEventLog) {
 					requestEventLog = (RequestEventLog) eventLog;
 					if (requestActionFilter.isAnActionRequest(requestEventLog)) {
 						csvLine = readRequestEventLogForDurationStats(requestEventLog, csvLine, outputStream);
 					}
 				}
 			}
 		}.readUploadedFiles();
 
 		facesContext.responseComplete();
 	}
 
 	public void extractDurationPerPrincipalStats() throws IOException, ClassNotFoundException {
 		final FacesContext facesContext = FacesContext.getCurrentInstance();
 
 		final HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
 		response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
 		response.setContentType("text/csv"); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
 		response.setHeader("Content-Disposition", "attachment; filename=\"extract_duration_per_principal.csv\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
 
 
 		final DurationPerPrincipalStats durationPerPrincipalStats = new DurationPerPrincipalStats();
 
 		new UploadedFileReader() {
 
 			RequestActionFilter requestActionFilter = new JSF21AndRichFaces4RequestActionFilter();
 
 			@Override
 			protected void handleEventLog(final EventLog eventLog) throws IOException {
 				readRequestEventLogForDurationPerPrincipalStats(eventLog, durationPerPrincipalStats, requestActionFilter, TIME_SLICE_DURATION_MILLIS);
 			}
 		}.readUploadedFiles();
 
 		final ServletOutputStream outputStream = response.getOutputStream();
 		writeDurationPerPrincpalStats(durationPerPrincipalStats, outputStream);
 
 		facesContext.responseComplete();
 	}
 
 	public void extractSlowRequests() throws IOException, ClassNotFoundException {
 		final FacesContext facesContext = FacesContext.getCurrentInstance();
 
 		final HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
 		response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
 		response.setContentType("text/plain"); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
 		response.setHeader("Content-Disposition", "attachment; filename=\"slow_requests.txt\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
 
 		final ServletOutputStream outputStream = response.getOutputStream();
 
 		new UploadedFileReader() {
 			@Override
 			protected void handleEventLog(final EventLog eventLog) throws IOException {
				if (eventLog instanceof RequestEventLog) {
					final Long durationMillis = ((RequestEventLog) eventLog).getDurationMillis();
					if (durationMillis != null && durationMillis >= minMillisForSlowRequests) {
						outputStream.println(eventLog.toString());
					}
 				}
 			}
 		}.readUploadedFiles();
 
 		facesContext.responseComplete();
 	}
 
 	public void computeGraph() throws FileNotFoundException, IOException, ClassNotFoundException {
 		durationPerPrincipalStats = new DurationPerPrincipalStats();
 
 		if (lastTimeSliceDurationMinutes != timeSliceDurationMinutes) {
 			new UploadedFileReader() {
 				RequestActionFilter requestActionFilter = new JSF21AndRichFaces4RequestActionFilter();
 				long timeSliceDurationMillis = timeSliceDurationMinutes * 1000 * 60;
 
 				@Override
 				protected void handleEventLog(final EventLog eventLog) throws IOException {
 					readRequestEventLogForDurationPerPrincipalStats(eventLog, durationPerPrincipalStats, requestActionFilter, timeSliceDurationMillis);
 				}
 			}.readUploadedFiles();
 		}
 
 		if (lastTimeSliceDurationMinutes != timeSliceDurationMinutes || lastNbItemsForDispersionTables != nbItemsForDispersionTables) {
 			final StringBuilder cpuUsageJsonBuilder = new StringBuilder();
 			final StringBuilder memoryUsageJsonBuilder = new StringBuilder();
 			final StringBuilder nbUsersJsonBuilder = new StringBuilder();
 			final StringBuilder durationsFor1clickMedianJsonBuilder = new StringBuilder();
 
 			// now let's build the json series
 			final double[] yValues = new double[nbItemsForDispersionTables];
 			final StringBuilder[] seriesBuilders = new StringBuilder[nbItemsForDispersionTables];
 			// Create the list of different Y values
 			for (int i = 0; i < yValues.length; i++) {
 				final double yValue = durationPerPrincipalStats.totalDurationsFor1click.getPercentile((i+1d)/nbItemsForDispersionTables*100d);
 				final StringBuilder seriesBuilder = new StringBuilder();
 				seriesBuilder.append("{stack:true,lines:{show:true,fill:true},label:'");
 				if (i == 0) {
 					seriesBuilder.append('0');
 				} else {
 					seriesBuilder.append((int)yValues[i - 1]);
 				}
 				seriesBuilder.append(" - ").append((int)yValue).append("',data:[");
 				yValues[i] = yValue;
 				seriesBuilders[i] = seriesBuilder;
 			}
 			boolean firstSlice = true;
 			TimeSlice previousTimeSlice = null;
 			for(final TimeSlice timeSlice : durationPerPrincipalStats.timeSlices) {
 				//TODO : améliorer cet algorithme en itérant sur chaque value de totalDurationsFor1click et pour chacune d'elle aller chercher par dichotomie l'entier à incrémenter correspondant à la bonne tranche des yValues
 				final long middleTimeSliceTime = (timeSlice.endDate.getTime()+timeSlice.startDate.getTime()) / 2;	// the data should be displayed in the middle of the slice
 				final boolean mustAppendNullForPrevious = previousTimeSlice != null && !previousTimeSlice.endDate.equals(timeSlice.startDate);
 				for (int i = 0; i < yValues.length; i++) {
 					final StringBuilder seriesBuilder = seriesBuilders[i];
 					final double maxInclude = yValues[i];
 					final double minExclude = i == 0 ? 0d : yValues[i - 1];
 					final double[] values = timeSlice.durationsFor1click.getValues();
 					int n = 0;
 					for (final double value : values) {
 						if (minExclude < value && value <= maxInclude) {
 							n++;
 						}
 					}
 					writeToJsonBuilderAndAppendNullBeforeIfNecessary(middleTimeSliceTime, n, seriesBuilder, firstSlice, mustAppendNullForPrevious, previousTimeSlice);
 				}
 				// adding cpu & memory info
 				writeToJsonBuilderAndAppendNullBeforeIfNecessary(middleTimeSliceTime, timeSlice.cpuUsage.getMean(), cpuUsageJsonBuilder, firstSlice, mustAppendNullForPrevious, previousTimeSlice);
 				writeToJsonBuilderAndAppendNullBeforeIfNecessary(middleTimeSliceTime, timeSlice.memoryUsage.getMean(), memoryUsageJsonBuilder, firstSlice, mustAppendNullForPrevious, previousTimeSlice);
 				writeToJsonBuilderAndAppendNullBeforeIfNecessary(middleTimeSliceTime, timeSlice.statsPerPrincipal.size(), nbUsersJsonBuilder, firstSlice, mustAppendNullForPrevious, previousTimeSlice);
 				writeToJsonBuilderAndAppendNullBeforeIfNecessary(middleTimeSliceTime, timeSlice.durationsFor1click.getPercentile(50), durationsFor1clickMedianJsonBuilder, firstSlice, mustAppendNullForPrevious, previousTimeSlice);
 
 				firstSlice = false;
 				previousTimeSlice = timeSlice;
 			}
 			final StringBuilder durationsFor1clickSeriesBuilder = new StringBuilder();
 			for (final StringBuilder seriesBuilder : seriesBuilders) {
 				seriesBuilder.append("]}");
 				if (durationsFor1clickSeriesBuilder.length() > 0) {
 					durationsFor1clickSeriesBuilder.append(',');
 				}
 				durationsFor1clickSeriesBuilder.append(seriesBuilder);
 			}
 			durationsFor1clickSeriesJson = durationsFor1clickSeriesBuilder.toString();
 			cpuUsageJsonValues = cpuUsageJsonBuilder.toString();
 			memoryUsageJsonValues = memoryUsageJsonBuilder.toString();
 			nbUsersJsonValues = nbUsersJsonBuilder.toString();
 			durationsFor1clickMedianJsonValues = durationsFor1clickMedianJsonBuilder.toString();
 
 			lastNbItemsForDispersionTables = nbItemsForDispersionTables;
 			lastTimeSliceDurationMinutes = timeSliceDurationMinutes;
 		}
 	}
 
 	/// Events ///
 	/////////////
 	public void handleFileUpload(final FileUploadEvent event) throws IOException, ClassNotFoundException {
 		System.out.println("Handling "+event.getFile().getFileName());
 		uploadedFiles.add(event.getFile());
 		resetComputedGraph();
 	}
 
 	/// Utils ///
 	////////////
 	private CSVDurationStatsLine readRequestEventLogForDurationStats(final RequestEventLog requestEventLog, CSVDurationStatsLine csvLine, final ServletOutputStream outputStream) throws IOException {
 		final Date date = requestEventLog.getDate();
 		if (csvLine.startDate == null) {
 			csvLine.startDate = date;
 		} else if(date.getTime() - csvLine.startDate.getTime() > AGGLOMERATION_DURATION_MILLIS) {
 			writeCsvDurationStatsLine(csvLine, requestEventLog, outputStream);
 			csvLine = new CSVDurationStatsLine();
 			csvLine.startDate = date;
 		}
 		// adding a point in the csv line
 		Long durationMillis = requestEventLog.getDurationMillis();
 		if (durationMillis == null) {
 			System.err.println("Null duration, will treat it as "+NULL_AFTER_PROCESS_DATE_DURATION_MILLIS+"ms");
 			durationMillis = NULL_AFTER_PROCESS_DATE_DURATION_MILLIS;
 		}
 		csvLine.durations.addValue(durationMillis);
 		csvLine.principals.add(requestEventLog.getPrincipal());
 
 		return csvLine;
 	}
 
 	private void writeCsvDurationStatsLine(final CSVDurationStatsLine csvLine, final RequestEventLog currentRequestEventLog, final ServletOutputStream outputStream) throws IOException {
 		if (csvLine.startDate != null) { // else the csvLine has never been filled
 			final DescriptiveStatistics durations = csvLine.durations;
 			final StringBuilder line = new StringBuilder();
 			// Processing "Start date;End date;Distinct users;Number of requests;Duration;Average;Median;90c;Min;Max"
 			final Date currentRequestDate = currentRequestEventLog.getDate();
 			line.append(dateFormatter.format(csvLine.startDate)).append(";")
 				.append(dateFormatter.format(currentRequestDate != null ? currentRequestDate : csvLine.startDate)).append(";")
 				.append(csvLine.principals.size()).append(";")
 				.append(durations.getN()).append(";")
 				.append((int)durations.getSum()).append(";")
 				.append((int)durations.getMean()).append(";")
 				.append((int)durations.getPercentile(50)).append(";")
 				.append((int)durations.getPercentile(90)).append(";")
 				.append((int)durations.getMin()).append(";")
 				.append((int)durations.getMax()).append(";");
 
 			outputStream.println(line.toString());
 		}
 	}
 
 	private void writeToJsonBuilder(final Date date, final Object value, final StringBuilder jsonBuilder, final boolean firstValue) {
 		writeToJsonBuilder(date.getTime(), value, jsonBuilder, firstValue);
 	}
 
 	private void writeToJsonBuilder(final long time, final Object value, final StringBuilder jsonBuilder, final boolean firstValue) {
 		if (!firstValue) {
 			jsonBuilder.append(',');
 		}
 		jsonBuilder.append('[').append(time).append(',').append(value).append(']');
 	}
 
 	private void writeToJsonBuilderAndAppendNullBeforeIfNecessary(final long time, final Object value, final StringBuilder jsonBuilder, final boolean firstValue, final boolean mustAppendNullForPrevious, final TimeSlice previousTimeSlice) {
 		if (mustAppendNullForPrevious) {
 			// First check if we must add an "end point" for previous datas if there is a gap between time slices
 			writeToJsonBuilder(previousTimeSlice.endDate, "null", jsonBuilder, firstValue);
 		}
 		writeToJsonBuilder(time, value, jsonBuilder, firstValue && !mustAppendNullForPrevious);
 	}
 
 	private void readRequestEventLogForDurationPerPrincipalStats(final EventLog eventLog, final DurationPerPrincipalStats stats, final RequestActionFilter requestActionFilter, final long timeSliceDurationMillis) {
 		RequestEventLog requestEventLog = null;
 		final Date eventDate = eventLog.getDate();
 
 		if (eventLog instanceof SystemEventLog) {
 			final TimeSlice timeSlice = getTimeSlice(eventDate, stats, timeSliceDurationMillis);
 			final SystemEventLog systemEventLog = (SystemEventLog) eventLog;
 			timeSlice.cpuUsage.addValue(systemEventLog.getCpuUsage());
 			timeSlice.memoryUsage.addValue(systemEventLog.getNonHeapMemoryUsed() + systemEventLog.getHeapMemoryUsed());
 		} else if (eventLog instanceof RequestEventLog && requestActionFilter.isAnActionRequest(requestEventLog = (RequestEventLog) eventLog)) {
 			final String principal = requestEventLog.getPrincipal();
 			// Compute duration for 1 click
 			final TimeSlice timeSlice = getTimeSlice(eventDate, stats, timeSliceDurationMillis);
 			final StatsPerPrincipal statsPerPrincipal = getStatsPerPrincipal(timeSlice, principal);
 			Long durationMillis = requestEventLog.getDurationMillis();
 			if (durationMillis == null) {
 				System.err.println("Null duration, will treat it as "+NULL_AFTER_PROCESS_DATE_DURATION_MILLIS+"ms");
 				durationMillis = NULL_AFTER_PROCESS_DATE_DURATION_MILLIS;
 			}
 			statsPerPrincipal.durationsFor1click.addValue(durationMillis);
 			timeSlice.durationsFor1click.addValue(durationMillis);
 			stats.totalDurationsFor1click.addValue(durationMillis);
 			// Compute duration between 2 clicks
 			final PrincipalContext principalContext = getPrincipalContext(stats, principal);
 			final Date lastClick = principalContext.lastClick;
 			if (lastClick != null) {
 				// Last click, we can compute the duration between the two clicks
 				final TimeSlice lastClickTimeSlice = getTimeSlice(lastClick, stats, timeSliceDurationMillis);
 				final StatsPerPrincipal lastClickStatsPerPrincipal = getStatsPerPrincipal(lastClickTimeSlice, principal);
 				final long duration = eventDate.getTime() - lastClick.getTime();
 				lastClickStatsPerPrincipal.durationsBetween2clicks.addValue(duration);
 				timeSlice.durationsBetween2clicks.addValue(duration);
 				stats.totalDurationsBetween2clicks.addValue(duration);
 			}
 			principalContext.lastClick = eventDate;
 		}
 	}
 
 	private TimeSlice getTimeSlice(final Date date, final DurationPerPrincipalStats stats, final long timeSliceDurationMillis) {
 		final int index = stats.lastAccessedTimeSliceIndex;
 		if (index == -1) {
 			// no last accessed time slice, we must found the slice by dichotomy
 			return findOrCreateTimeSlice(date, stats, timeSliceDurationMillis);
 		} else {
 			// let's see if the date fits in current time slice
 			final TimeSlice currentTimeSlice = stats.timeSlices.get(index);
 			if (isDateInTimeSlice(date, currentTimeSlice)) {
 				return returnTimeSlice(currentTimeSlice, stats, index);
 			} else if (date.before(currentTimeSlice.startDate)) {
 				// the date is before, let's check the previous index
 				assert index > 0;	// should never append because the time slice should already have been created
 				final int previousIndex = index - 1;
 				final TimeSlice previousTimeSlice = stats.timeSlices.get(previousIndex);
 				if (isDateInTimeSlice(date, previousTimeSlice)) {
 					return returnTimeSlice(previousTimeSlice, stats, previousIndex);
 				} else {
 					// does not fit in the previous time slice, must find it or create it by dichotomy
 					return findOrCreateTimeSlice(date, stats, timeSliceDurationMillis);
 				}
 			} else {
 				// we are after, let's check if the next slice exists and create it other wise
 				final int nextIndex = index + 1;
 				if (stats.timeSlices.size() <= nextIndex) {
 					// let's create the new time slice
 					final TimeSlice timeSlice = createNewTimeSlice(date, stats, timeSliceDurationMillis);
 					return returnTimeSlice(timeSlice, stats, nextIndex);
 				} else {
 					final TimeSlice nextTimeSlice = stats.timeSlices.get(nextIndex);
 					if (isDateInTimeSlice(date, nextTimeSlice)) {
 						return returnTimeSlice(nextTimeSlice, stats, nextIndex);
 					} else {
 						// does not fit in the next time slice, must find it or create it by dichotomy
 						return findOrCreateTimeSlice(date, stats, timeSliceDurationMillis);
 					}
 				}
 			}
 		}
 	}
 
 	private TimeSlice findOrCreateTimeSlice(final Date date, final DurationPerPrincipalStats stats,final long timeSliceDurationMillis) {
 		final List<TimeSlice> timeSlices = stats.timeSlices;
 		// will binary search the time slice
 		int low = 0;
 		int high = timeSlices.size() - 1;
 		int mid;
 		while (low <= high) {
 			mid = (low + high) / 2;
 			final TimeSlice midTimeSlice = timeSlices.get(mid);
 			if (date.after(midTimeSlice.endDate)) {
 				low = mid + 1;
 			} else if (date.before(midTimeSlice.startDate)) {
 				high = mid - 1;
 			} else {
 				return midTimeSlice;
 			}
 		}
 		// not found in the existing time slices, must create a new one
 		return createNewTimeSlice(date, stats, timeSliceDurationMillis);
 	}
 
 	private boolean isDateInTimeSlice(final Date date, final TimeSlice currentTimeSlice) {
 		return date.after(currentTimeSlice.startDate) && date.before(currentTimeSlice.endDate);
 	}
 
 	private TimeSlice returnTimeSlice(final TimeSlice timeSlice, final DurationPerPrincipalStats stats, final int index) {
 		stats.lastAccessedTimeSliceIndex = index;
 		return timeSlice;
 	}
 
 	private TimeSlice createNewTimeSlice(final Date date, final DurationPerPrincipalStats stats, final long timeSliceDurationMillis) {
 		// As the time slices are created in a sorted way, we can create it just by looking the last slice, check if the date fits inside a new slice
 		// which would be just after, and if not, add it a new one which begins with that date
 		final List<TimeSlice> timeSlices = stats.timeSlices;
 		final int lastIndex = timeSlices.size() - 1;
 		final TimeSlice newTimeSlice;
 		if (lastIndex >= 0) {
 			final TimeSlice lastTimeSlice = timeSlices.get(lastIndex);
 			final Date endDate = lastTimeSlice.endDate;
 			final TimeSlice newTimeSliceJustAfterLast = new TimeSlice(endDate, timeSliceDurationMillis);
 			if (isDateInTimeSlice(date, newTimeSliceJustAfterLast)) {
 				newTimeSlice = newTimeSliceJustAfterLast;
 			} else {
 				// the given date doesn't fit in the next time slice, let's create a new one for it
 				newTimeSlice = new TimeSlice(date, timeSliceDurationMillis);
 			}
 		} else {
 			// Create a new time slice because there is no last time slice
 			newTimeSlice = new TimeSlice(date,timeSliceDurationMillis);
 		}
 		timeSlices.add(newTimeSlice);
 		return newTimeSlice;
 	}
 
 	private StatsPerPrincipal getStatsPerPrincipal(final TimeSlice timeSlice, final String principal) {
 		StatsPerPrincipal statsPerPrincipal = timeSlice.statsPerPrincipal.get(principal);
 		if (statsPerPrincipal == null) {
 			statsPerPrincipal = new StatsPerPrincipal();
 			timeSlice.statsPerPrincipal.put(principal, statsPerPrincipal);
 		}
 		return statsPerPrincipal;
 	}
 
 	private PrincipalContext getPrincipalContext(final DurationPerPrincipalStats stats, final String principal) {
 		PrincipalContext principalContext = stats.principalContexts.get(principal);
 		if (principalContext == null) {
 			principalContext = new PrincipalContext();
 			stats.principalContexts.put(principal, principalContext);
 		}
 		return principalContext;
 	}
 
 	private EventLog readEventLog(final ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
 		try {
 			return (EventLog) objectInputStream.readObject();
 		} catch (final CorruptedInputException e) {
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 
 	public static abstract class DurationPerPrincpalStatsWriter {
 		public abstract String getTitle();
 		public abstract DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(StatsPerPrincipal statsPerPrincipal);
 		public abstract DescriptiveStatistics getTotalDescriptiveStatistics(DurationPerPrincipalStats stats);
 		public abstract double getStatValue(DescriptiveStatistics descriptiveStatistics);
 
 		public void write(final DurationPerPrincipalStats stats, final ServletOutputStream out) throws IOException {
 			/// Waiting time between 2 clicks - Mean
 			out.println(getTitle());
 			// List principals
 			out.print(";Principals;");
 			final List<String> principals = Lists.newLinkedList(stats.principalContexts.keySet());
 			for (final String principal : principals) {
 				out.print(principal + ";");
 			}
 			out.println();
 			// for each time slice
 			for(final TimeSlice timeSlice : stats.timeSlices) {
 				final StringBuilder line = new StringBuilder();
 				line.append(dateFormatter.format(timeSlice.startDate)).append(';')
 					.append(dateFormatter.format(timeSlice.endDate)).append(';');
 				for (final String principal : principals) {
 					final StatsPerPrincipal statsPerPrincipal = timeSlice.statsPerPrincipal.get(principal);
 					if (statsPerPrincipal != null) {
 						line.append((int)getStatValue(getDescriptiveStatisticsForStatsPerPrincipal(statsPerPrincipal)));
 					}
 					line.append(';');
 				}
 				out.println(line.toString());
 			}
 			out.println("Total:;"+(int)getStatValue(getTotalDescriptiveStatistics(stats)));
 			out.println();
 		}
 	}
 
 	public static abstract class DurationPerDispersionStatsWriter {
 		public abstract String getTitle();
 		public abstract String getYTitle();
 		public abstract DescriptiveStatistics getTotalDescriptiveStatistics(DurationPerPrincipalStats stats);
 		public abstract DescriptiveStatistics getDescriptiveStatisticsPerTimeSlice(TimeSlice timeSlice);
 		public abstract Object getValueForN(int n, DescriptiveStatistics descriptiveStatisticsPerTimeSlice);
 
 		public void write(final DurationPerPrincipalStats stats, final ServletOutputStream out) throws IOException {
 			/// Waiting time between 2 clicks - Mean
 			out.println(getTitle());
 			// List principals
 			out.print(";"+getYTitle()+";");
 
 			final DescriptiveStatistics totalDescriptiveStatistics = getTotalDescriptiveStatistics(stats);
 
 			final double[] yValues = new double[MAX_ITEMS_FOR_DISPERSION_TABLES];
 			// Create the list of different Y values
 			for (int i = 0; i < yValues.length; i++) {
 				yValues[i] = totalDescriptiveStatistics.getPercentile((i+1d)/MAX_ITEMS_FOR_DISPERSION_TABLES*100d);
 			}
 			// Print it as header
 			for (int i = 0; i < yValues.length; i++) {
 				final double yValue = yValues[i];
 				if (i == 0) {
 					out.print(0);
 				} else {
 					out.print((int)yValues[i - 1]);
 				}
 				out.print(" - "+(int)yValue + ";");
 			}
 			out.println("Total;");
 			// for each time slice
 			for(final TimeSlice timeSlice : stats.timeSlices) {
 				final StringBuilder line = new StringBuilder();
 				line.append(dateFormatter.format(timeSlice.startDate)).append(';')
 					.append(dateFormatter.format(timeSlice.endDate)).append(';');
 
 				final DescriptiveStatistics descriptiveStatisticsPerTimeSlice = getDescriptiveStatisticsPerTimeSlice(timeSlice);
 				for (int i = 0; i < yValues.length; i++) {
 					final double maxInclude = yValues[i];
 					final double minExclude = i == 0 ? 0d : yValues[i - 1];
 					final double[] values = descriptiveStatisticsPerTimeSlice.getValues();
 					int n = 0;
 					for (final double value : values) {
 						if (minExclude < value && value <= maxInclude) {
 							n++;
 						}
 					}
 					line.append(getValueForN(n, descriptiveStatisticsPerTimeSlice)).append(';');
 				}
 				line.append(getValueForN((int) descriptiveStatisticsPerTimeSlice.getN(), descriptiveStatisticsPerTimeSlice)).append(';');
 				out.println(line.toString());
 			}
 			out.println();
 		}
 	}
 
 	private void writeDurationPerPrincpalStats(final DurationPerPrincipalStats stats, final ServletOutputStream out) throws IOException {
 		//// Waiting time between 2 clicks ////
 		/// Waiting time between 2 clicks - Mean
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Thinking time (Mean)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsBetween2clicks;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsBetween2clicks;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getMean();
 			}
 		}.write(stats, out);
 		/// Waiting time between 2 clicks - Number of elements
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Thinking time (Number of elements)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsBetween2clicks;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsBetween2clicks;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getN();
 			}
 		}.write(stats, out);
 		/// Waiting time between 2 clicks - Median
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Thinking time (Median)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsBetween2clicks;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsBetween2clicks;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getPercentile(50);
 			}
 		}.write(stats, out);
 		/// Waiting time between 2 clicks - Total Dispersion number of element
 		new DurationPerDispersionStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Thinking time (Dispersion - Number of elements)";
 			}
 			@Override
 			public String getYTitle() {
 				return "Dispersion slice";
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsBetween2clicks;
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsPerTimeSlice(final TimeSlice timeSlice) {
 				return timeSlice.durationsBetween2clicks;
 			}
 			@Override
 			public Object getValueForN(final int n, final DescriptiveStatistics descriptiveStatisticsPerTimeSlice) {
 				return n;
 			}
 		}.write(stats, out);
 		/// Waiting time between 2 clicks - Total Dispersion %
 		new DurationPerDispersionStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Thinking time (Dispersion %)";
 			}
 			@Override
 			public String getYTitle() {
 				return "Dispersion slice";
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsBetween2clicks;
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsPerTimeSlice(final TimeSlice timeSlice) {
 				return timeSlice.durationsBetween2clicks;
 			}
 			@Override
 			public Object getValueForN(final int n, final DescriptiveStatistics descriptiveStatisticsPerTimeSlice) {
 				return ((double)n/(double)descriptiveStatisticsPerTimeSlice.getN())*100d;
 			}
 		}.write(stats, out);
 
 		//// Waiting time for one click ////
 		/// Waiting time for one click - Mean
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Request duration (Mean)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsFor1click;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsFor1click;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getMean();
 			}
 		}.write(stats, out);
 		/// Waiting time for one click - Number of elements
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Request duration (Number of elements)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsFor1click;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsFor1click;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getN();
 			}
 		}.write(stats, out);
 		/// Waiting time for one click - Median
 		new DurationPerPrincpalStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Request duration (Median)";
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsForStatsPerPrincipal(final StatsPerPrincipal statsPerPrincipal) {
 				return statsPerPrincipal.durationsFor1click;
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsFor1click;
 			}
 			@Override
 			public double getStatValue(final DescriptiveStatistics descriptiveStatistics) {
 				return descriptiveStatistics.getPercentile(50);
 			}
 		}.write(stats, out);
 		/// Waiting time for one click - Total Dispersion number of element
 		new DurationPerDispersionStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Request duration (Dispersion - Number of elements)";
 			}
 			@Override
 			public String getYTitle() {
 				return "Dispersion slice";
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsFor1click;
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsPerTimeSlice(final TimeSlice timeSlice) {
 				return timeSlice.durationsFor1click;
 			}
 			@Override
 			public Object getValueForN(final int n, final DescriptiveStatistics descriptiveStatisticsPerTimeSlice) {
 				return n;
 			}
 		}.write(stats, out);
 		/// Waiting time for one click - Total Dispersion %
 		new DurationPerDispersionStatsWriter() {
 			@Override
 			public String getTitle() {
 				return "Request duration (Dispersion %)";
 			}
 			@Override
 			public String getYTitle() {
 				return "Dispersion slice";
 			}
 			@Override
 			public DescriptiveStatistics getTotalDescriptiveStatistics(final DurationPerPrincipalStats stats) {
 				return stats.totalDurationsFor1click;
 			}
 			@Override
 			public DescriptiveStatistics getDescriptiveStatisticsPerTimeSlice(final TimeSlice timeSlice) {
 				return timeSlice.durationsFor1click;
 			}
 			@Override
 			public Object getValueForN(final int n, final DescriptiveStatistics descriptiveStatisticsPerTimeSlice) {
 				return ((double)n/(double)descriptiveStatisticsPerTimeSlice.getN())*100d;
 			}
 		}.write(stats, out);
 	}
 
 	private void resetComputedGraph() {
 		this.durationPerPrincipalStats = null;
 		this.cpuUsageJsonValues = null;
 		this.memoryUsageJsonValues = null;
 		this.durationsFor1clickSeriesJson = null;
 		this.nbUsersJsonValues = null;
 		this.durationsFor1clickMedianJsonValues = null;
 		this.lastNbItemsForDispersionTables = 0;
 	}
 
 	/// Getters & Setters ///
 	public List<UploadedFile> getUploadedFiles() {
 		return uploadedFiles;
 	}
 
 	public void setUploadedFiles(final List<UploadedFile> uploadedFiles) {
 		this.uploadedFiles = uploadedFiles;
 	}
 
 	public String getDurationsFor1clickSeriesJson() {
 		return durationsFor1clickSeriesJson;
 	}
 
 	public String getCpuUsageJsonValues() {
 		return cpuUsageJsonValues;
 	}
 
 	public String getMemoryUsageJsonValues() {
 		return memoryUsageJsonValues;
 	}
 
 	public int getNbItemsForDispersionTables() {
 		return nbItemsForDispersionTables;
 	}
 
 	public void setNbItemsForDispersionTables(final int nbItemsForDispersionTables) {
 		this.nbItemsForDispersionTables = nbItemsForDispersionTables;
 	}
 
 	public int getTimeSliceDurationMinutes() {
 		return timeSliceDurationMinutes;
 	}
 
 	public void setTimeSliceDurationMinutes(final int timeSliceDurationMinutes) {
 		this.timeSliceDurationMinutes = timeSliceDurationMinutes;
 	}
 
 	public String getNbUsersJsonValues() {
 		return nbUsersJsonValues;
 	}
 
 	public String getDurationsFor1clickMedianJsonValues() {
 		return durationsFor1clickMedianJsonValues;
 	}
 
 	public int getMinMillisForSlowRequests() {
 		return minMillisForSlowRequests;
 	}
 
 	public void setMinMillisForSlowRequests(final int minMillisForSlowRequests) {
 		this.minMillisForSlowRequests = minMillisForSlowRequests;
 	}
 }
