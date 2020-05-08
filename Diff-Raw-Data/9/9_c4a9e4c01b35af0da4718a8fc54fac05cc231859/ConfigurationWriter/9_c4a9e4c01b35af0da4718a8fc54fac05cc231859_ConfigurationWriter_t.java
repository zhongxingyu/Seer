 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import net.lmxm.ute.beans.Configuration;
 import net.lmxm.ute.beans.FileReference;
 import net.lmxm.ute.beans.FindReplacePattern;
 import net.lmxm.ute.beans.Preference;
 import net.lmxm.ute.beans.Property;
 import net.lmxm.ute.beans.jobs.Job;
 import net.lmxm.ute.beans.locations.FileSystemLocation;
 import net.lmxm.ute.beans.locations.HttpLocation;
 import net.lmxm.ute.beans.locations.SubversionRepositoryLocation;
 import net.lmxm.ute.beans.sources.HttpSource;
 import net.lmxm.ute.beans.sources.SubversionRepositorySource;
 import net.lmxm.ute.beans.targets.FileSystemTarget;
 import net.lmxm.ute.beans.tasks.FileSystemDeleteTask;
 import net.lmxm.ute.beans.tasks.FindReplaceTask;
 import net.lmxm.ute.beans.tasks.GroovyTask;
 import net.lmxm.ute.beans.tasks.HttpDownloadTask;
 import net.lmxm.ute.beans.tasks.SubversionExportTask;
 import net.lmxm.ute.beans.tasks.SubversionUpdateTask;
 import net.lmxm.ute.beans.tasks.Task;
 import net.lmxm.ute.enums.Scope;
 import net.lmxm.ute.exceptions.ConfigurationException;
 import noNamespace.FileSystemDeleteTaskType;
 import noNamespace.FileSystemLocationType;
 import noNamespace.FileSystemTargetType;
 import noNamespace.FileType;
 import noNamespace.FilesType;
 import noNamespace.FindReplaceTaskType;
 import noNamespace.GroovyTaskType;
 import noNamespace.HttpDownloadTaskType;
 import noNamespace.HttpLocationType;
 import noNamespace.HttpSourceType;
 import noNamespace.JobType;
 import noNamespace.JobsType;
 import noNamespace.LocationsType;
 import noNamespace.PatternType;
 import noNamespace.PatternsType;
 import noNamespace.PreferenceType;
 import noNamespace.PreferencesType;
 import noNamespace.PropertiesType;
 import noNamespace.PropertyType;
 import noNamespace.ScopeType;
 import noNamespace.SubversionExportTaskType;
 import noNamespace.SubversionRepositorySourceType;
 import noNamespace.SubversionRespositoryLocationType;
 import noNamespace.SubversionUpdateTaskType;
 import noNamespace.TaskType;
 import noNamespace.TasksType;
 import noNamespace.UteConfigurationDocument;
 import noNamespace.UteConfigurationType;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class ConfigurationWriter.
  */
 public class ConfigurationWriter {
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationWriter.class);
 
 	/** The configuration. */
 	private final Configuration configuration;
 
 	/**
 	 * Instantiates a new configuration writer.
 	 */
 	public ConfigurationWriter(final Configuration configuration) {
 		super();
 
 		this.configuration = configuration;
 	}
 
 	/**
 	 * Convert scope to scope type.
 	 * 
 	 * @param scope the scope
 	 * @return the scope type. enum
 	 */
 	private ScopeType.Enum convertScopeToScopeType(final Scope scope) {
 		final String prefix = "writeFiles() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final ScopeType.Enum scopeType;
 
 		if (scope == Scope.FILE) {
 			scopeType = ScopeType.FILE;
 		}
 		else if (scope == Scope.LINE) {
 			scopeType = ScopeType.LINE;
 		}
 		else {
 			scopeType = ScopeType.LINE;
 		}
 
 		LOGGER.debug("{} returning {}", prefix, scopeType);
 
 		return scopeType;
 	}
 
 	/**
 	 * Write.
 	 * 
 	 * @param configuration the configuration
 	 */
 	public void write() {
 		final String prefix = "write() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		configuration.getAbsolutePath();
 
 		try {
 			ConfigurationUtils.validateConfiguration(configuration);
 
 			final UteConfigurationDocument document = UteConfigurationDocument.Factory.newInstance();
 			final UteConfigurationType configurationType = document.addNewUteConfiguration();
 
 			writePreferences(configurationType, configuration);
 			writeProperties(configurationType, configuration);
 			writeLocations(configurationType, configuration);
 			writeJobs(configurationType, configuration);
 
 			final File file = new File(configuration.getAbsolutePath());
 
 			// Create a backup of the existing file
 			if (file.exists()) {
				final File backupFile = new File(file.getAbsolutePath() + ".bak");

				// Delete existing backup
				if (backupFile.exists()) {
					FileUtils.deleteQuietly(backupFile);
				}

				FileUtils.moveFile(file, backupFile);
 			}
 
 			document.save(file);
 		}
 		catch (final IOException e) {
 			LOGGER.error("IOException caught while writing configuration file", e);
 
 			throw new ConfigurationException("Error occurred writing configuration file", e);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write files.
 	 * 
 	 * @param filesType the files type
 	 * @param files the files
 	 */
 	private void writeFiles(final FilesType filesType, final List<FileReference> files) {
 		final String prefix = "writeFiles() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		for (final FileReference file : files) {
 			final FileType fileType = filesType.addNewFile();
 
 			fileType.setName(file.getName());
 			fileType.setTargetName(file.getTargetName());
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write file system delete task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeFileSystemDeleteTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeFileSystemDeleteTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FileSystemDeleteTask fileSystemDeleteTask = (FileSystemDeleteTask) task;
 
 		final FileSystemDeleteTaskType fileSystemDeleteTaskType = taskType.addNewFileSystemDeleteTask();
 
 		fileSystemDeleteTaskType.setStopOnError(fileSystemDeleteTask.getStopOnError());
 
 		final FileSystemTargetType fileSystemTargetType = fileSystemDeleteTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, fileSystemDeleteTask.getTarget());
 
 		final List<FileReference> files = fileSystemDeleteTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = fileSystemDeleteTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write file system location.
 	 * 
 	 * @param locationsType the locations type
 	 * @param fileSystemLocation the file system location
 	 */
 	private void writeFileSystemLocation(final LocationsType locationsType, final FileSystemLocation fileSystemLocation) {
 		final String prefix = "writeFileSystemLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FileSystemLocationType fileSystemLocationType = locationsType.addNewFileSystemLocation();
 
 		fileSystemLocationType.setId(fileSystemLocation.getId());
 		fileSystemLocationType.setPath(fileSystemLocation.getPath());
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write file system locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void writeFileSystemLocations(final LocationsType locationsType, final Configuration configuration) {
 		final String prefix = "writeFileSystemLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<FileSystemLocation> fileSystemLocations = configuration.getFileSystemLocations();
 
 		if (fileSystemLocations.isEmpty()) {
 			return;
 		}
 
 		for (final FileSystemLocation fileSystemLocation : fileSystemLocations) {
 			writeFileSystemLocation(locationsType, fileSystemLocation);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write file system target.
 	 * 
 	 * @param fileSystemTargetType the file system target type
 	 * @param fileSystemTarget the file system target
 	 */
 	private void writeFileSystemTarget(final FileSystemTargetType fileSystemTargetType,
 			final FileSystemTarget fileSystemTarget) {
 		final String prefix = "writeFileSystemTarget() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		fileSystemTargetType.setLocationId(fileSystemTarget.getLocation().getId());
 
 		if (StringUtils.isNotBlank(fileSystemTarget.getRelativePath())) {
 			fileSystemTargetType.setRelativePath(fileSystemTarget.getRelativePath());
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write find replace task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeFindReplaceTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeFindReplaceTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FindReplaceTask findReplaceTask = (FindReplaceTask) task;
 
 		final FindReplaceTaskType findReplaceTaskType = taskType.addNewFindReplaceTask();
 
 		findReplaceTaskType.setScope(convertScopeToScopeType(findReplaceTask.getScope()));
 
 		writePatterns(findReplaceTaskType, findReplaceTask.getPatterns());
 
 		final FileSystemTargetType fileSystemTargetType = findReplaceTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, findReplaceTask.getTarget());
 
 		final List<FileReference> files = findReplaceTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = findReplaceTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write groovy task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeGroovyTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeGroovyTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final GroovyTask groovyTask = (GroovyTask) task;
 
 		final GroovyTaskType groovyTaskType = taskType.addNewGroovyTask();
 
 		groovyTaskType.setScript(groovyTask.getScript());
 
 		final FileSystemTargetType fileSystemTargetType = groovyTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, groovyTask.getTarget());
 
 		final List<FileReference> files = groovyTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = groovyTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write http download task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeHttpDownloadTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeHttpDownloadTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final HttpDownloadTask httpDownloadTask = (HttpDownloadTask) task;
 
 		final HttpDownloadTaskType httpDownloadTaskType = taskType.addNewHttpDownloadTask();
 
 		final HttpSourceType httpSourceType = httpDownloadTaskType.addNewHttpSource();
 		writeHttpSource(httpSourceType, httpDownloadTask.getSource());
 
 		final FileSystemTargetType fileSystemTargetType = httpDownloadTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, httpDownloadTask.getTarget());
 
 		final List<FileReference> files = httpDownloadTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = httpDownloadTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write http location.
 	 * 
 	 * @param locationsType the locations type
 	 * @param httpLocation the http location
 	 */
 	private void writeHttpLocation(final LocationsType locationsType, final HttpLocation httpLocation) {
 		final String prefix = "writeHttpLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final HttpLocationType httpLocationType = locationsType.addNewHttpLocation();
 
 		httpLocationType.setId(httpLocation.getId());
 		httpLocationType.setUrl(httpLocation.getUrl());
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write http locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void writeHttpLocations(final LocationsType locationsType, final Configuration configuration) {
 		final String prefix = "writeHttpLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<HttpLocation> httpLocations = configuration.getHttpLocations();
 
 		if (httpLocations.isEmpty()) {
 			return;
 		}
 
 		for (final HttpLocation httpLocation : httpLocations) {
 			writeHttpLocation(locationsType, httpLocation);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write http source.
 	 * 
 	 * @param httpSourceType the http source type
 	 * @param httpSource the http source
 	 */
 	private void writeHttpSource(final HttpSourceType httpSourceType, final HttpSource httpSource) {
 		final String prefix = "writeHttpSource() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		httpSourceType.setLocationId(httpSource.getLocation().getId());
 
 		if (StringUtils.isNotBlank(httpSource.getRelativePath())) {
 			httpSourceType.setRelativePath(httpSource.getRelativePath());
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write job.
 	 * 
 	 * @param jobsType the jobs type
 	 * @param job the job
 	 */
 	private void writeJob(final JobsType jobsType, final Job job) {
 		final String prefix = "writeJob() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final JobType jobType = jobsType.addNewJob();
 
 		if (StringUtils.isNotBlank(job.getDescription())) {
 			jobType.setDescription(job.getDescription());
 		}
 
 		jobType.setId(job.getId());
 
 		writeTasks(jobType, job);
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write jobs.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void writeJobs(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "writeJobs() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Job> jobs = configuration.getJobs();
 
 		if (jobs.isEmpty()) {
 			return;
 		}
 
 		final JobsType jobsType = configurationType.addNewJobs();
 
 		for (final Job job : jobs) {
 			writeJob(jobsType, job);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write locations.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void writeLocations(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "writeLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final LocationsType locationsType = configurationType.addNewLocations();
 
 		writeFileSystemLocations(locationsType, configuration);
 		writeHttpLocations(locationsType, configuration);
 		writeSubversionRepositoryLocations(locationsType, configuration);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Write pattern.
 	 * 
 	 * @param patternsType the patterns type
 	 * @param pattern the pattern
 	 */
 	private void writePattern(final PatternsType patternsType, final FindReplacePattern pattern) {
 		final String prefix = "writePattern() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final PatternType patternType = patternsType.addNewPattern();
 
 		patternType.setFind(pattern.getFind());
 		patternType.setReplace(pattern.getReplace());
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write patterns.
 	 * 
 	 * @param findReplaceTaskType the find replace task type
 	 * @param patterns the patterns
 	 */
 	private void writePatterns(final FindReplaceTaskType findReplaceTaskType, final List<FindReplacePattern> patterns) {
 		final String prefix = "writePatterns() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		if (patterns.isEmpty()) {
 			return;
 		}
 
 		final PatternsType patternsType = findReplaceTaskType.addNewPatterns();
 
 		for (final FindReplacePattern pattern : patterns) {
 			writePattern(patternsType, pattern);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write preference.
 	 * 
 	 * @param preferencesType the preferences type
 	 * @param preference the preference
 	 */
 	private void writePreference(final PreferencesType preferencesType, final Preference preference) {
 		final String prefix = "writePreference() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final PreferenceType preferenceType = preferencesType.addNewPreference();
 
 		preferenceType.setId(preference.getId());
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write preferences.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void writePreferences(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "writePreferences() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Preference> preferences = configuration.getPreferences();
 
 		if (preferences.isEmpty()) {
 			return;
 		}
 
 		final PreferencesType preferencesType = configurationType.addNewPreferences();
 
 		for (final Preference preference : preferences) {
 			writePreference(preferencesType, preference);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write properties.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void writeProperties(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "writeProperties() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Property> properties = configuration.getProperties();
 
 		if (properties.isEmpty()) {
 			return;
 		}
 
 		final PropertiesType propertiesType = configurationType.addNewProperties();
 
 		for (final Property property : properties) {
 			writeProperty(propertiesType, property);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write property.
 	 * 
 	 * @param propertiesType the properties type
 	 * @param property the property
 	 */
 	private void writeProperty(final PropertiesType propertiesType, final Property property) {
 		final String prefix = "writeProperty() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final PropertyType propertyType = propertiesType.addNewProperty();
 
 		propertyType.setId(property.getId());
 		propertyType.setValue(property.getValue());
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write subversion export task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeSubversionExportTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeSubversionExportTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionExportTask subversionExportTask = (SubversionExportTask) task;
 
 		final SubversionExportTaskType subversionExportTaskType = taskType.addNewSubversionExportTask();
 
 		final SubversionRepositorySourceType subversionRepositorySourceType = subversionExportTaskType
 				.addNewSubversionRepositorySource();
 		writeSubversionRepositorySource(subversionRepositorySourceType, subversionExportTask.getSource());
 
 		final FileSystemTargetType fileSystemTargetType = subversionExportTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, subversionExportTask.getTarget());
 
 		final List<FileReference> files = subversionExportTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = subversionExportTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write subversion repository location.
 	 * 
 	 * @param locationsType the locations type
 	 * @param subversionRepositoryLocation the subversion repository location
 	 */
 	private void writeSubversionRepositoryLocation(final LocationsType locationsType,
 			final SubversionRepositoryLocation subversionRepositoryLocation) {
 		final String prefix = "writeSubversionRepositoryLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionRespositoryLocationType subversionRepositoryLocationType = locationsType
 				.addNewSubversionRespositoryLocation();
 
 		subversionRepositoryLocationType.setId(subversionRepositoryLocation.getId());
 		subversionRepositoryLocationType.setUrl(subversionRepositoryLocation.getUrl());
 
 		final String username = subversionRepositoryLocation.getUsername();
 		if (StringUtils.isNotBlank(username)) {
 			subversionRepositoryLocationType.setUsername(username);
 		}
 
 		final String password = subversionRepositoryLocation.getPassword();
 		if (StringUtils.isNotBlank(password)) {
 			subversionRepositoryLocationType.setPassword(password);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write subversion repository locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void writeSubversionRepositoryLocations(final LocationsType locationsType, final Configuration configuration) {
 		final List<SubversionRepositoryLocation> subversionRepositoryLocations = configuration
 				.getSubversionRepositoryLocations();
 		final String prefix = "writeSubversionRepositoryLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		if (subversionRepositoryLocations.isEmpty()) {
 			return;
 		}
 
 		for (final SubversionRepositoryLocation subversionRepositoryLocation : subversionRepositoryLocations) {
 			writeSubversionRepositoryLocation(locationsType, subversionRepositoryLocation);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write subversion repository source.
 	 * 
 	 * @param subversionRepositorySourceType the subversion repository source type
 	 * @param subversionRepositorySource the subversion repository source
 	 */
 	private void writeSubversionRepositorySource(final SubversionRepositorySourceType subversionRepositorySourceType,
 			final SubversionRepositorySource subversionRepositorySource) {
 		final String prefix = "writeSubversionRepositorySource() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		subversionRepositorySourceType.setLocationId(subversionRepositorySource.getLocation().getId());
 
 		if (StringUtils.isNotBlank(subversionRepositorySource.getRelativePath())) {
 			subversionRepositorySourceType.setRelativePath(subversionRepositorySource.getRelativePath());
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write subversion update task.
 	 * 
 	 * @param taskType the task type
 	 * @param task the task
 	 */
 	private void writeSubversionUpdateTask(final TaskType taskType, final Task task) {
 		final String prefix = "writeSubversionUpdateTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionUpdateTask subversionUpdateTask = (SubversionUpdateTask) task;
 
 		final SubversionUpdateTaskType subversionUpdateTaskType = taskType.addNewSubversionUpdateTask();
 
 		final FileSystemTargetType fileSystemTargetType = subversionUpdateTaskType.addNewFileSystemTarget();
 		writeFileSystemTarget(fileSystemTargetType, subversionUpdateTask.getTarget());
 
 		final List<FileReference> files = subversionUpdateTask.getFiles();
 		if (!files.isEmpty()) {
 			final FilesType filesType = subversionUpdateTaskType.addNewFiles();
 			writeFiles(filesType, files);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Write tasks.
 	 * 
 	 * @param jobType the job type
 	 * @param job the job
 	 */
 	private void writeTasks(final JobType jobType, final Job job) {
 		final String prefix = "writeTasks() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Task> tasks = job.getTasks();
 
 		if (tasks.isEmpty()) {
 			return;
 		}
 
 		final TasksType tasksType = jobType.addNewTasks();
 
 		for (final Task task : tasks) {
 			final TaskType taskType = tasksType.addNewTask();
 
 			if (StringUtils.isNotBlank(task.getDescription())) {
 				taskType.setDescription(task.getDescription());
 			}
 
 			if (!task.getEnabled()) {
 				taskType.setEnabled(task.getEnabled());
 			}
 
 			taskType.setId(task.getId());
 
 			if (task instanceof FileSystemDeleteTask) {
 				writeFileSystemDeleteTask(taskType, task);
 			}
 			else if (task instanceof FindReplaceTask) {
 				writeFindReplaceTask(taskType, task);
 			}
 			else if (task instanceof GroovyTask) {
 				writeGroovyTask(taskType, task);
 			}
 			else if (task instanceof HttpDownloadTask) {
 				writeHttpDownloadTask(taskType, task);
 			}
 			else if (task instanceof SubversionExportTask) {
 				writeSubversionExportTask(taskType, task);
 			}
 			else if (task instanceof SubversionUpdateTask) {
 				writeSubversionUpdateTask(taskType, task);
 			}
 			else {
 				throw new ConfigurationException("Unsupported task type"); // TODO
 			}
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 }
