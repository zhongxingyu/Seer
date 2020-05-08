 package org.fedoraproject.eclipse.packager.api;
 
 import java.io.File;
 
 import org.apache.http.HttpResponse;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.LookasideCache;
 import org.fedoraproject.eclipse.packager.SourcesFile;
 
 /**
  * A class used to execute a {@code download sources} command. It has setters
  * for all supported options and arguments of this command and a {@link #call()}
  * method to finally execute the command. Each instance of this class should
  * only be used for one invocation of the command (means: one call to
  * {@link #call()})
  * 
  */
 public class DownloadSourceCommand extends
 		FedoraPackagerCommand<HttpResponse> {
 
 	private File fileToDownload; // should probably be a list
 	private HttpResponse response;
 	private SourcesFile sources;
 	private LookasideCache lookasideCache;
 	
 	/**
 	 * @param projectRoot The project root abstraction.
 	 * @param sources The sources file abstraction.
 	 * @param lookasideCache The lookaside cache abstraction.
 	 */
 	public DownloadSourceCommand(FedoraProjectRoot projectRoot, SourcesFile sources,
 			LookasideCache lookasideCache) {
 		super(projectRoot);
 		this.sources = sources;
 		this.lookasideCache = lookasideCache;
 	}
 	
 	/**
	 * @param uploadURL the uploadURL to set.
 	 * @return this instance.
 	 */
	public DownloadSourceCommand setUploadURL(String uploadURL) {
		this.lookasideCache.setUploadUrl(uploadURL);
 		return this;
 	}
 
 	/**
 	 * @param fileToUpload the fileToUpload to set. Required.
 	 * @return this instance.
 	 */
 	public DownloadSourceCommand setFileToUpload(File fileToUpload) {
 		this.fileToDownload = fileToUpload;
 		return this;
 	}
 
 	/**
 	 * Executes the {@code UploadSources} command. Each instance of this class
 	 * should only be used for one invocation of the command. Don't call this
 	 * method twice on an instance.
 	 */
 	@Override
 	public HttpResponse call() throws Exception {
 		// Don't allow this very same instance to be called twice.
 		checkCallable();
 		// TODO Implement
 		return null;
 	}
 
 }
