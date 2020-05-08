 package models;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import util.Check;
 import util.Utils;
 import util.XStreamHelper;
 import exception.QuickException;
 
 public class Repo implements Serializable {
 	
 	private static final String MARKER_FILE_NAME = ".tserver";
 
 	private static final String LOCK_FILE_NAME = ".tlock";
 	
 	private static final String RESULT_FILE_NAME = "_result";
 
 	private static final String INPUT_FILE_NAME = "_input";
 	
 	private static final String CREATION_FILE_NAME = ".creation-time";
 	
 	String rid;
 	
 	File fRoot;
 	
 	File fLock;
 	
 	File fResult;
 
 	File fMarker;
 	
 	File fInput;
 	
 	File fCreationTime;
 	
 	/** reports that this repository context contains a cached alignment result */
 	public boolean cached = false;
 	
 	/** 
 	 * Create the context folder on the file system for the specified <i>requets identifier</i> 
 	 * */
 	public Repo( final String rid ) {
 		this(rid,false);
 	}
 
 	public Repo( final String rid, final boolean create ) {
 		this(new File(AppProps.instance().getDataPath(),rid),create);
 	}
 	
 	protected Repo( final File folder, final boolean create ) {
 		Check.notNull(folder,"Repo folder cannot be null");
 		
 		this.fRoot = folder;
 		this.fLock = new File(fRoot, LOCK_FILE_NAME);
 		this.fResult = new File(fRoot, RESULT_FILE_NAME);
 		this.fMarker = new File(fRoot, MARKER_FILE_NAME);
 		this.fInput = new File(fRoot, INPUT_FILE_NAME);
 		this.fCreationTime = new File(fRoot, CREATION_FILE_NAME);
 		
 		this.rid = folder.getName();
 		
 		if(create) {
 			if( fMarker.exists() && fResult.exists() ) {
 				cached = true;
 			}
 			else {
 				create(fRoot);
 			}
 		}
 	}
 	
 	
 	/** The copy constructor */
 	public Repo( Repo that ) {
 		this.rid = that.rid;
 		this.fRoot = that.fRoot;
 		this.fLock = that.fLock;
 		this.fMarker = new File(fRoot,MARKER_FILE_NAME);
 		this.fResult = that.fResult;
 	}
 	
 	@Override
 	public boolean equals( Object that ) {
 		return Utils.isEquals(this, that, "rid", "fRoot", "fLock", "fMarker", "fResult");
 	}
 	
 	@Override
 	public int hashCode() {
 		return Utils.hash(this, "rid", "fRoot", "fLock", "fMarker", "fResult");
 	}
 
 	/** Locks the current context folder */
 	public boolean lock() {
 		
 		//TODO check if FileLock api does a better work 
 		
 		try {
 			return fLock.createNewFile();
 		}
 		catch( IOException e ) {
 			/* otherwise is an unexecptected condition */
 			throw new QuickException("Unable to create .lock file for context folder '%s'", rid); 
 		}
 		
 	}
 	
 	/** Release the lock on the context folder */
 	public void unlock() {
 
 		if( !fLock.exists() ) {
 			Logger.warn(".lock file does not exist for context folder '%s'", rid);
 			return;
 		}
 		
 		if( !fLock.delete() ) {
 			throw new QuickException("Unable to delete .lock file for context folder '%s'", rid);
 		}
 	}
 	
 	/**
 	 * @return the repository root folder 
 	 */
 	public File getFile() {
 		return fRoot;
 	}
 	
 	public String getPath() { 
 		return fRoot != null ? fRoot.getAbsolutePath() : null;
 	}
 	
 	public File getFile( String path ) { 
 		return new File(fRoot, path);
 	}
 	
 	public Status getStatus() {
 		if( !fRoot.exists() || !fMarker.exists()) {
 			return Status.UNKNOWN;
 		}
 		
 		if( fLock.exists() ) {
 			return Status.RUNNING;
 		}
 		
 		if( fResult.exists() ) {
 			try {
 				OutResult out = XStreamHelper.fromXML(fResult);
 				return out.status!=null ? out.status : Status.UNKNOWN;
 			} 
 			catch( Exception e ) {
				Logger.warn("Error on parsing result file: '%s'. Caused by: %s", fResult, e);
 				return Status.UNKNOWN;
 			}
 		}
 
 		return Status.READY;
 	}
 	
 	public boolean isTerminated() {
 		Status status = getStatus();
 		return Status.DONE.equals(status) || Status.FAILED.equals(status); 
 	}
 	
 	public boolean hasResult() {
 		return fResult != null && fResult.exists() && isTerminated();
 	}
 	
 	public boolean isExpired() {
 		Status status = getStatus();
 		if( status == null ) { return false; }
 		
 		long now = System.currentTimeMillis();
 		long exp = getExpirationTime();
 		boolean result = (status.isDone() || status.isFailed()) && (now > exp);
 		if( Logger.log4j.isDebugEnabled()) { 
 			DateFormat fmt = new SimpleDateFormat("dd/MMM HH:mm:ss");
 			String sExp = fmt.format(exp);
 			String sDelta = exp>now ? Utils.asDuration(exp-now) : "0";
 			Logger.debug("Repo '%s' - Expired: %s (status: '%s' - exp time: '%s' - delta: %s)", 
 					rid,
 					result, 
 					status, 
 					sExp,
 					sDelta);
 		}
 		return result;
 	}
 	
 	/**
 	 * @return the {@link OutResult} instance contained in this Repo
 	 * 
 	 */
 
 	public OutResult getResult() {
 		return (OutResult) (fResult.exists() ? XStreamHelper.fromXML(fResult) : null);
 	}
 	
 	/**
 	 * Serialize the specified instance of {@link OutResult}
 	 * @param out {@link OutResult} instance to save in this report 
 	 */
 	public void saveResult( OutResult out ) {
 		XStreamHelper.toXML(out, fResult);
 	} 
 	
 	File create( File folder ) {
 		if( folder.exists() ) {
 			Logger.warn("Well, cannot create an already existing folder: '%s'", folder.toString());
 			return folder;
 		}
 		
 		boolean result = folder.mkdirs();
 		if( !result ) {
 			throw new QuickException("Fail on creating repo folder: '%s'", folder);
 		}
 		
 		/* 
 		 * the creation time file 
 		 */
 		if( fCreationTime.exists() ) { 
 			fCreationTime.setLastModified( System.currentTimeMillis() );
 		}
 		else { 
 			try {
 				fCreationTime.createNewFile();
 			} catch (IOException e) {
 				throw new QuickException(e, "Unable to create file: '%s'", fCreationTime);
 			}
 		}
  		
 		/* 
 		 * create the marker file 
 		 */
 		if( fMarker.exists() ) {
 			return folder;
 		} 
 
 		RuntimeException fail=null;  
 		try {
 			if( !fMarker.createNewFile() ) {
 				fail = new QuickException("Unable to create repo marker file: '%s'", fMarker);
 			}
 		} catch (IOException e) {
 			fail = new QuickException(e, "Fail on creating repo marker file: '%s'", fMarker);
 		}
 		
 		if( fail != null ) throw fail;
 		
 		return folder;		
 	}
 	
 	public void touch() {
 		touch(new Date());
 	}
 	
 	void touch( Date date ) {
 		touch(date.getTime());
 	}
 	
 	void touch( long millis ) {
 		fMarker.setLastModified(millis);
 	}
 	
 	public File getInputFile() {
 		return fInput;
 	}
 	
 	/**
 	 * Remove all the content of the current repo path with the exception of the marked file. See {@link #fMarker}.
 	 */
 	public void clean() {
 		
 		File[] all = fRoot.listFiles();
 		if( all!=null ) for( File file : all ) {
 			
 			if( !fMarker.equals(file) ) {
 				if( !FileUtils.deleteQuietly(file) ) {
 					Logger.warn("Unable to clean file: '%s'", file);
 				}
 			}
 		}
 		
 		// reset the current folder timestamp
 		touch();
 	}
 	
 
 	/**
 	 * Drop this folder and all its content
 	 */
 	public void drop() {
 		drop(false);
 	}
 	
 
 	public void drop(boolean forceKill) {
 		if( forceKill ) {
 			Logger.info("Force KILL on repo: '%s'", fRoot);
 			/* 
 			 * kill all pending process that are locking the folder 
 			 * An alternative command to kill all process is: "fuser -k <file>" 
 			 * but the -k switch (kill) is not supported on OSX 
 			 * 
 			 * kill -9 `lsof -t +D %s`
 			 */
 
 			try {
 				//TODO win32 porting: rewrite this command to support Windows platform 
 				String kill = String.format("kill -9 `lsof -t +D %s`", fRoot);
 //				DefaultExecutor exec = new DefaultExecutor();
 //				exec.setWatchdog( new ExecuteWatchdog(5000) ); // <-- 5secs timeout to run kill all pending  
 //				exec.execute(new CommandLine(kill));
 				Process proc = Runtime.getRuntime().exec(kill);
 				proc.waitFor();
 				
 				
 			} catch( Exception e ) {
 				Logger.warn(e, "Error kill pending process for folder: '%s' ", fRoot);
 			}
 			
 		}
 
 		/*
 		 * remove all files 
 		 */
 		String sRoot = fRoot.getAbsolutePath();
 		Logger.debug("Deleting Repo folder: '%s'", sRoot);
 		
 		try {
 			/* sanity check
 			 * the path to delete HAVE TO BE a subfolder of the workspace path 
 			 */
 			String sWork = AppProps.WORKSPACE_FOLDER.getAbsolutePath();
 			if( !sRoot.startsWith(sWork)) { 
 				Logger.warn("Cannot delete a Repo folder outside of the workspace: %s", sRoot );
 				return;
 			}
 			
 			String check = sRoot.substring(sWork.length());
 			if( check.startsWith(File.separator) ) {
 				check = check.substring(1);
 			}
 			
 			if( check.endsWith(File.separator)) { 
 				check = check.substring(0,check.length()-1);
 			}
 			
 			if( !check.equals(rid) ) { 
 				Logger.error("Cannot delete Repo: '%s'. Sanity check failed: ", rid, check);
 				return;
 			}
 			
 			
 			/* 
 			 * OK proceed 
 			 */
 			String rm = String.format("rm -rf %s", sRoot);
 			Logger.debug("Executing remove command: %s", rm);
 			Process proc = Runtime.getRuntime().exec(rm);
 			int exitcode = proc.waitFor();
 			if( exitcode != 0 ) { 
 				Logger.warn("Cannot execute: '%s'; exitcode: %s", rm, exitcode);
 			}
 			else if( new File(sRoot).exists() ) { 
 				Logger.warn("Unable to remove Repo: '%s'", rid);
 			}
 			else { 
 				Logger.info("Deleted Repo: '%s'", rid);
 			}
 			
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Error deleting Repo folder: '%s'", sRoot);
 		}
 	}
 	
 	/**
 	 * @return The repository folder creation time. Since Java does not let top access to file creation time, 
 	 * we use the 'marker' file last update time,  
 	 *  
 	 */
 	public long getCreationTime() {
 		if( fCreationTime != null && fCreationTime.exists() ) { 
 			return fCreationTime.lastModified();
 		}
 		if( fInput != null && fInput.exists() ) { 
 			return fInput.lastModified();
 		}
 		
 		Logger.warn("Missing creation time for repo: '%s'", fRoot.getName());
 		return fRoot.lastModified();
 	} 
 
 	public String getCreationTimeFmt() {
 		Date date = new Date(getCreationTime());
 		return new SimpleDateFormat("dd MMM yyyy, HH:MM (z)").format(date);
 	}
 	
 	/**
 	 * @return The repository last accessed time.  
 	 */
 	public long getLastAccessedTime() {
 		return fMarker.lastModified();
 	}
 	
 	public String getLastAccessTimeFmt() {
 		return Utils.asSmartString( new Date(getLastAccessedTime()) );
 	}
 	
 	/**
 	 * @return The repository expiration timestamp millis or {@link Long#MAX_VALUE} if repository state is invalid.  
 	 */
 	public long getExpirationTime() {
 
 		Status status = getStatus();
 		if( status != null && (status.isDone() || status.isFailed()) ) {
 			return getLastAccessedTime() + (AppProps.instance().getDataCacheDuration() *1000);
 		}
 		else { 
 			return Long.MAX_VALUE;
 		}
 	
 	}
 	
 	public String getExpirationTimeFmt() {
 		return Utils.asSmartString( new Date(getExpirationTime()) );
 	}
 	
 	/**
 	 * Find all repository instance being in one of the specified status 
 	 * 
 	 * @param status open array of the requested status of <code>null</code> for any status 
 	 * @return the list of {@link Repo} in the requested status or a empty list if nothing is found 
 	 */
 	public static List<Repo> findByStatus(Status ... status) {
 		List<Repo> result = new ArrayList<Repo>();
 		
 		File ROOT_FOLDER = new File( AppProps.instance().getDataPath() );
 		for( File file : ROOT_FOLDER.listFiles() ) {
 
 			if( isRepoFolder(file) ) {
 				Repo repo=new Repo(file,false);
 				if( status == null || Utils.contains(status, repo.getStatus()) ) {
 					result.add(repo);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Find all the repo objects in any status 
 	 */
 	public static List<Repo> findAll() {
 		return findByStatus((Status[])null);
 	}
 
 	/**
 	 * Find all the repo eclusing the ones being the specified status 
 	 * 
 	 * @param exception an open array of the status to be excluded in the result list 
 	 * @return a list of {@link Repo} instances or an empty list if nothing is found 
 	 */
 	public static List<Repo> findAllExcept(Status... exception) {
 		List<Status> status = Arrays.asList( Status.values() );
 
 		if( exception != null )
 			status.removeAll( Arrays.asList(exception) );
 		
 		return findByStatus((Status[])status.toArray());
 	}
 	
 	/**
 	 * @param folder a not null file referencing a folder on file system
 	 * @return <code>true</code> if it is a valid tcoffee job context repository or <code>false</code> otherwise 
 	 */
 	public static boolean isRepoFolder( File folder ) {
 		return folder.exists() && folder.isDirectory() && new File(folder,MARKER_FILE_NAME).exists();
 	}
 
 	
 	/**
 	 * Remove all status regardless the status in which the repo is  
 	 */
 	public static void deleteAll() {
 		List<Repo> repos = Repo.findAll();
 		
 		for( Repo repo : repos ) {
 			Logger.info("Deleting Repo folder: '%s'", repo.getFile());
 			/* drop this folder */
 			repo.drop();
 		}
 	}
 
 	/**
 	 * Delete all the expired jobs
 	 */
 	public static void deleteExpired() {
 		List<Repo> all = findByStatus(Status.DONE, Status.FAILED);
 		for( Repo repo : all ) {
 			if(repo.isExpired()) {
 				repo.drop();
 			}
 		}
 	}
 
 	/**
 	 * This method to save disk space, remove as soon as possible the following paths created by T-Coffee 
 	 * <p>
 	 * <li>_cache/</li>
 	 * <li>_tmp/</li>
 	 * <li>_lck/</li>
 	 * </li>
 	 * 
 	 * <p>
 	 * These path  are defined in the 'bundle.environment' T-Coffee bundle configuration,
 	 * changing that configuration this code have to be update accordingly
 	 */
 	public static void cleanTcoffeeCache() { 
 
		List<Repo> all = findByStatus(Status.DONE, Status.FAILED, Status.UNKNOWN);
 		for( Repo repo : all ) {
 
 			if( repo.fLock.exists() ) { 
 				/* skip still running jobs .. */
 				continue; 
 			} 
 			
 			File path;
 			
 			/* 
 			 * remove '_cache'
 			 */
 			if( (path=new File(repo.fRoot, "_cache")).exists() ) { 
 				if( !FileUtils.deleteQuietly(path) ) { 
 					Logger.warn("Unable to delete cache path: '%s'", path);
 				}
 			}
 			
 			/* 
 			 * remove '_tmp'
 			 */
 			if( (path=new File(repo.fRoot, "_tmp")).exists() ) { 
 				if( !FileUtils.deleteQuietly(path) ) { 
 					Logger.warn("Unable to delete tmp path: '%s'", path);
 				}
 			}
 			
 			/* 
 			 * remove '_lck'
 			 */
 			if( (path=new File(repo.fRoot, "_lck")).exists() ) { 
 				if( !FileUtils.deleteQuietly(path) ) { 
 					Logger.warn("Unable to delete lock path: '%s'", path);
 				}
 			}
 		
 		
 		}
 		
 	}
 	
 	
 
 }
