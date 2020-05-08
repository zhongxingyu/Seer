 package net.minecraft.launcher.updater.download;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import net.minecraft.launcher.Launcher;
 
 public class DownloadJob {
 	private static final int MAX_ATTEMPTS_PER_FILE = 5;
 	private static final int ASSUMED_AVERAGE_FILE_SIZE = 5242880;
 	private final Queue<Downloadable> remainingFiles = new ConcurrentLinkedQueue<Downloadable>();
 	private final List<Downloadable> allFiles = Collections
 			.synchronizedList(new ArrayList<Downloadable>());
 	private final List<Downloadable> failures = Collections
 			.synchronizedList(new ArrayList<Downloadable>());
 	private final List<Downloadable> successful = Collections
 			.synchronizedList(new ArrayList<Downloadable>());
 	private final List<ProgressContainer> progressContainers = Collections
 			.synchronizedList(new ArrayList<ProgressContainer>());
 	private final DownloadListener listener;
 	private final String name;
 	private final boolean ignoreFailures;
 	private final AtomicInteger remainingThreads = new AtomicInteger();
 	private boolean started;
 	private boolean checked = false;
 
 	public DownloadJob(String name, boolean ignoreFailures,
 			DownloadListener listener) {
 		this(name, ignoreFailures, listener, null);
 	}
 
 	public DownloadJob(String name, boolean ignoreFailures,
 			DownloadListener listener, Collection<Downloadable> files) {
 		this.name = name;
 		this.ignoreFailures = ignoreFailures;
 		this.listener = listener;
 		if (files != null)
 			addDownloadables(files);
 	}
 
 	public void addDownloadables(Collection<Downloadable> downloadables) {
 		if (this.started)
 			throw new IllegalStateException(
 					"Cannot add to download job that has already started");
 
 		this.allFiles.addAll(downloadables);
 		this.remainingFiles.addAll(downloadables);
 
 		for (Downloadable downloadable : downloadables) {
 			this.progressContainers.add(downloadable.getMonitor());
 			if (downloadable.getExpectedSize() == 0L)
 				downloadable.getMonitor().setTotal(ASSUMED_AVERAGE_FILE_SIZE);
 			else {
 				downloadable.getMonitor().setTotal(
 						downloadable.getExpectedSize());
 			}
 			downloadable.getMonitor().setJob(this);
 		}
 	}
 
 	public void addDownloadables(Downloadable[] downloadables) {
 		if (this.started)
 			throw new IllegalStateException(
 					"Cannot add to download job that has already started");
 
 		for (Downloadable downloadable : downloadables) {
 			this.allFiles.add(downloadable);
 			this.remainingFiles.add(downloadable);
 			this.progressContainers.add(downloadable.getMonitor());
 			if (downloadable.getExpectedSize() == 0L)
 				downloadable.getMonitor().setTotal(ASSUMED_AVERAGE_FILE_SIZE);
 			else {
 				downloadable.getMonitor().setTotal(
 						downloadable.getExpectedSize());
 			}
 			downloadable.getMonitor().setJob(this);
 		}
 	}
 
 	public int getFailures() {
 		return this.failures.size();
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public float getProgress() {
 		float current = 0.0F;
 		float total = 0.0F;
 
 		synchronized (this.progressContainers) {
 			for (ProgressContainer progress : this.progressContainers) {
 				total += progress.getTotal();
 				current += progress.getCurrent();
 			}
 		}
 
 		float result = -1.0F;
 		if (total > 0.0F)
 			result = current / total;
 		return result;
 	}
 
 	public int getSuccessful() {
 		return this.successful.size();
 	}
 
 	public boolean isComplete() {
 		return (this.started) && (this.remainingFiles.isEmpty())
				&& (this.remainingThreads.get() == 0);
	}

	public boolean isFinished() {
		return (isComplete()) && (this.checked);
 	}
 
 	public boolean isStarted() {
 		return this.started;
 	}
 
 	private void popAndDownload() {
 		Downloadable downloadable;
 		while ((downloadable = this.remainingFiles.poll()) != null) {
 			if (downloadable.getNumAttempts() > MAX_ATTEMPTS_PER_FILE) {
 				if (!this.ignoreFailures)
 					this.failures.add(downloadable);
 				Launcher.getInstance().println(
 						"Gave up trying to download " + downloadable.getUrl()
 								+ " for job '" + this.name + "'");
 			} else {
 				try {
 					String result = downloadable.download();
 					this.successful.add(downloadable);
 					Launcher.getInstance().println(
 							"Finished downloading " + downloadable.getTarget()
 									+ " for job '" + this.name + "'" + ": "
 									+ result);
 				} catch (Throwable t) {
 					Launcher.getInstance().println(
 							"Couldn't download " + downloadable.getUrl()
 									+ " for job '" + this.name + "'", t);
 					this.remainingFiles.add(downloadable);
 				}
 			}
 		}
 		if (this.remainingThreads.decrementAndGet() <= 0)
 			this.listener.onDownloadJobFinished(this);
 	}
 
 	public void setChecked() {
 		this.checked = true;
 	}
 
 	public boolean shouldIgnoreFailures() {
 		return this.ignoreFailures;
 	}
 
 	public void startDownloading(ThreadPoolExecutor executorService) {
 		if (this.started)
 			throw new IllegalStateException(
 					"Cannot start download job that has already started");
 		this.started = true;
 
 		if (this.allFiles.isEmpty()) {
 			Launcher.getInstance().println(
 					"Download job '" + this.name
 							+ "' skipped as there are no files to download");
 			this.listener.onDownloadJobFinished(this);
 		} else {
 			int threads = executorService.getMaximumPoolSize();
 			this.remainingThreads.set(threads);
 			Launcher.getInstance().println(
 					"Download job '" + this.name + "' started (" + threads
 							+ " threads, " + this.allFiles.size() + " files)");
 			for (int i = 0; i < threads; i++)
 				executorService.submit(new Runnable() {
 					@Override
 					public void run() {
 						DownloadJob.this.popAndDownload();
 					}
 				});
 		}
 	}
 
 	public void updateProgress() {
 		this.listener.onDownloadJobProgressChanged(this);
 	}
 }
 
 /*
  * Location: Z:\home\vadim\.minecraft\launcher.jar Qualified Name:
  * net.minecraft.launcher.updater.download.DownloadJob JD-Core Version: 0.6.2
  */
