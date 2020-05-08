 package com.laboki.eclipse.plugin.fastopen.files;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.fastopen.events.FilterFilesEvent;
 import com.laboki.eclipse.plugin.fastopen.events.FilteredFilesResultEvent;
 import com.laboki.eclipse.plugin.fastopen.events.RankedFilesEvent;
 import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.fastopen.instance.Instance;
 import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
 import com.laboki.eclipse.plugin.fastopen.main.EventBus;
 import com.laboki.eclipse.plugin.fastopen.task.Task;
 import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;
 
 public final class FilesFilter extends EventBusInstance {
 
 	private static final String CAMEL_CASE_PATTERN =
 		"(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
 	private static final String FAMILY = "FilesFilter task family";
 	private static final TaskMutexRule RULE = new TaskMutexRule();
 	protected final List<IFile> files = Lists.newArrayList();
 	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE
 		| Pattern.CANON_EQ
 		| Pattern.UNICODE_CASE;
 
 	@Subscribe
 	public void
 	eventHandler(final RankedFilesEvent event) {
 		new Task() {
 
 			@Override
 			public void
 			execute() {
 				EditorContext.cancelJobsBelongingTo(FilesFilter.FAMILY);
 				FilesFilter.this.updateFiles(event.getFiles());
 			}
 		}.setRule(FilesFilter.RULE).start();
 	}
 
 	protected void
 	updateFiles(final ImmutableList<IFile> files) {
 		new Task() {
 
 			@Override
 			public void
 			execute() {
 				this.updateFiles();
 			}
 
 			private void
 			updateFiles() {
 				FilesFilter.this.files.clear();
 				FilesFilter.this.files.addAll(files);
 			}
 		}.setRule(FilesFilter.RULE).setFamily(FilesFilter.FAMILY).start();
 	}
 
 	@Subscribe
 	public void
 	eventHandler(final FilterFilesEvent event) {
 		new Task() {
 
 			@Override
 			public void
 			execute() {
 				EditorContext.cancelJobsBelongingTo(FilesFilter.FAMILY);
 				FilesFilter.this.filterRecentFiles(event.getString());
 			}
 		}.setRule(FilesFilter.RULE).start();
 	}
 
 	protected void
 	filterRecentFiles(final String string) {
 		new Task() {
 
 			private final List<IFile> recentFiles = FilesFilter.this.getFiles();
 
 			@Override
 			public void
 			execute() {
 				this.filter(string.trim());
 			}
 
 			private void
 			filter(final String string) {
 				if (string.isEmpty()) this.postEvent(this.recentFiles);
 				else this.filterFiles(string);
 			}
 
 			private void
 			filterFiles(final String string) {
				this.postEvent(this.getFilteredList(this.getCamelCaseQuery(string)));
 			}
 
 			private String
 			getCamelCaseQuery(final String string) {
 				final String[] camelCaseString =
 					string.split(FilesFilter.CAMEL_CASE_PATTERN);
 				return ".*" + Joiner.on(".*").join(camelCaseString) + ".*";
 			}
 
 			private void
 			postEvent(final List<IFile> files) {
 				EventBus.post(new FilteredFilesResultEvent(ImmutableList.copyOf(files)));
 			}
 
 			private List<IFile>
 			getFilteredList(final String query) {
 				final List<IFile> filteredList = Lists.newArrayList();
 				for (final IFile file : this.recentFiles)
 					if (this.matchFound(file, query)) filteredList.add(file);
 				return filteredList;
 			}
 
 			private boolean
 			matchFound(final IFile file, final String query) {
 				if (Pattern.compile(query, FilesFilter.PATTERN_FLAGS)
 					.matcher(file.getName())
 					.find()) return true;
 				return false;
 			}
 		}.setFamily(FilesFilter.FAMILY).setRule(FilesFilter.RULE).start();
 	}
 
 	protected List<IFile>
 	getFiles() {
 		return Lists.newArrayList(this.files);
 	}
 
 	@Override
 	public Instance
 	stop() {
 		this.files.clear();
 		return super.stop();
 	}
 }
