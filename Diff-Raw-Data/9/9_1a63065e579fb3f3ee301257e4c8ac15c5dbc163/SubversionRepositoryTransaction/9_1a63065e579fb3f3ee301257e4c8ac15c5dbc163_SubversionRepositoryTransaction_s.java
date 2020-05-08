 /*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Copyright (C) 2005 Marco Aurélio Graciotto Silva <magsilva@gmail.com>
 */
 
 
 package net.sf.ideais.repository;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.concurrent.atomic.AtomicLong;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tigris.subversion.javahl.*;
 
 
 /**
  * Subversion (http://subversion.tigris.org) is a software configuration
  * manager inspired by CVS. It copies much of CVS's functionalities, but
  * without the glitches (directories are versioned too, for example).
  */
 public class SubversionRepositoryTransaction extends RepositoryTransaction
 {
 	/**
 	* Commons Logging instance.
 	*/
 	private static Log log = LogFactory.getLog(SubversionRepositoryTransaction.class);
 	
 	/**
 	 * Subversion error handler.
 	 */
 	class SubversionNotifier implements Notify2
     {
         /**
          * Handler for Subversion notifications.
          *
          * @param info everything to know about this event
          */
         public void onNotify( NotifyInformation info )
         {
         	log.info( info );
         	log.debug( String.format( "[%1$d] %2$d (%1$s)",
         			info.getRevision(), info.getAction(), info.getPath() ) );
         }
     }
 
 	/**
 	 * Default Subversion error handler.
 	 */
 	private final Notify2 notifier = new SubversionNotifier();
 	
 	/**
 	 * The username used to login in the Subversion repository.
 	 */
 	private String username;
 	
 	/**
 	 * The password for the username.
 	 */
 	private String password;
 	
 	/**
 	 * The repository URL.
 	 */
 	private String url;
 	
 	private String rootDir;
 
 	
 	/**
 	 * The real Subversion client. It comes from the Javahl bindindgs, that use
 	 * JNI to access the native Subversion libraries. The documentation says
 	 * this class is not thread-safe. Maybe should be a good idea to use the
 	 * thread-safe SVNClientSynchronized or, at least, investigate how the
 	 * threading issues can affect this class.
 	 */
 	private SVNClientInterface client;
 	
 	/**
 	 * Counter used by createTempFile.
 	 */
 	private static AtomicLong counter = new AtomicLong(Double.doubleToLongBits(Math.random()));
 
 	/**
 	* Create the repository transaction to access the Subversion.
 	* 
 	* @param project ProjectIF to use to retrieve the configuration items.
 	* @param user UserIF to be used to access the repository.
 	* 
 	* @throws RepositoryTransactionError If the URL is invalid or the user's
 	* configuration directory could not be created.
 	*/
 	public SubversionRepositoryTransaction(SourceCodeRepository repository)
 	{
 		super(repository);
 		client = new SVNClientSynchronized();
 		rootDir = "/";
 		try {
 			client.setConfigDirectory(rootDir);
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError(
 				"exception.repositoryTransaction.transactionInitialization", e );
 		}
 		setUrl( repository.getLocation() );
 		setUsername( repository.getUsername() );
 		setPassword( repository.getPassword() );
 		
 		client.notification2( notifier );
 	}
 
 	/**
 	 * Set the user's password (as required by Subversion).
 	 * 
 	 * @param password The password used to login in the Subversion repository.
 	 */
 	private void setPassword( String password )
 	{
 		if ( password != null ) {
 			this.password = password;
 			client.password( this.password );
 		}
 	}
 	
 	/**
 	 * Set the username (as required by Subversion).
 	 * 
 	 * @param username The username used to login in the Subversion repository.
 	 */
 	private void setUsername( String username )
 	{
 		if ( username != null ) {
 			this.username = username;
 			client.username( this.username );
 		}
 	}
 
 	/**
 	 * Set the URL used to access the repository.
 	 * 
 	 * @param url The address that will be used to access the repository.
 	 * 
 	 * @throws RepositoryTransactionError The URL is invalid.
 	 */
 	private void setUrl( String url )
 	{
 		try {
 			new URL( url );
 		} catch ( MalformedURLException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError(
 					"exception.repositoryTransaction.invalidRepositoryURL", e );
 		}
 		this.url = url;
 	}
 
 
 	/**
 	 * Checkout a recent (HEAD) copy of the project's repository.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when checking out
 	 * the files.
 	 */
 	public void checkout()
 	{
 		super.checkout();
 		_checkout( Revision.HEAD );
 	}
 
 	/**
 	 * Checkout a copy of the project's repository at the given version.
 	 * 
 	 * @param version The version (the Subversion revision) to be retrieved.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when checking out
 	 * the files.
 	 */
 	public void checkout( String version )
 	{
 		super.checkout( version );
 		Revision revision = Revision.getInstance( Long.parseLong( version ) );
 		_checkout( revision );
 	}
 
 	/**
 	 * Checkout a copy of the project's repository at the given revision.
 	 * 
 	 * @param revision The revision to be retrieved.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when checking out
 	 * the files.
 	 */
 	private void _checkout( Revision revision )
 	{
 		try {
             client.checkout( url, workdir, revision, true );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.checkout", e );
 		}
 	}
 
 	/**
 	 * Add a file or directory to the project's repository. If a directory is
 	 * used as parameter, all the files and subdirs are added. 
 	 * 
 	 * @param path The file or directory to be added.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when adding the
 	 * file(s).
 	 */
 	public void add( String path )
 	{
 		super.add( path );
 		_add( path, true );
 	}
 	
 	/**
 	 * Add a file or directory to the project's repository. If a directory is
 	 * used as parameter and "recurse" is true, all the files and subdirs will
 	 * be added. 
 	 * 
 	 * @param path The file or directory to be added.
 	 * @param recurse If the files and subdirectories within the directory
 	 * must be added.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when adding the
 	 * file(s).
 	 */
 	public void add( String path, boolean recurse )
 	{
 		super.add( path );
 		_add( path, recurse );
 	}
 
 	/**
 	 * Add a file or directory to the project's repository. If a directory is
 	 * used as parameter and "recurse" is true, all the files and subdirs will
 	 * be added. 
 	 * 
 	 * @param path The file or directory to be added.
 	 * @param recurse If the files and subdirectories within the directory
 	 * must be added.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when adding the
 	 * file(s).
 	 */
 	private void _add( String path, boolean recurse )
 	{
 		try {
 			client.add( path, recurse );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.add", e );
 		}
 	}
 
 	
 	/**
 	 * Remove a file or directory from the project's repository. If a directory
 	 * is used as parameter, all the files and subdirs are removed. 
 	 * 
 	 * @param path The file or directory to be removed.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when removing
 	 * the file(s).
 	 */
 	public void remove( String path )
 	{
 		super.remove( path );
 		try {
 			String[] paths = { path };
 			client.remove( paths, "", true );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.remove", e );
 		}
 	}
 	
 	/**
 	 * Revert a file or directory to its previous stable state in the project's
 	 * repository. If a directory is used as parameter, all the files and
 	 * subdirs are reverted. 
 	 * 
 	 * @param path The file or directory to be reverted.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when reverting
 	 * the file(s).
 	 */
 	public void revert( String path )
 	{
 		super.revert( path );
 		try {
 			client.revert( path, true );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.revert", e );
 		}
 	}
 
 	/**
 	 * Get metadata for every file in the given path. If a directory is used as
 	 * parameter, metadata for every files and subdirs is retrieved. 
 	 * 
 	 * @param path The file or directory to have its metadata retrieved.
 	 * 
 	 * @return The metadata for every file in the path.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when retrieving
 	 * the metadata.
 	 */
 	public ConfigurationItem[] info( String path )
 	{
 		super.info( path );
 		try {
 			ArrayList<ConfigurationItem> items = new ArrayList<ConfigurationItem>();
 			Status[] overallStatus = client.status( path, true, false, true );
 
 			for ( Status status : overallStatus ) {
 				Info info = client.info( status.getPath() );
 				ConfigurationItem ci = new ConfigurationItem( info.getName() );
 				ci.setVersion( Long.toString( info.getCopyRev() ) );
 				ci.setAuthor( info.getAuthor() );
 				int svnStatus = status.getTextStatus();
 				switch ( svnStatus ) {
 					case StatusKind.added:
 						ci.setStatus( ConfigurationItem.Status.ADDED );
 						break;
 					case StatusKind.conflicted:
 						ci.setStatus( ConfigurationItem.Status.CONFLICTED );
 						break;
 					case StatusKind.deleted:
 						ci.setStatus( ConfigurationItem.Status.REMOVED );
 						break;
 					case StatusKind.external:
 						ci.setStatus( ConfigurationItem.Status.EXTERNAL );
 						break;
 					case StatusKind.ignored:
 						ci.setStatus( ConfigurationItem.Status.IGNORED );
 						break;
 					case StatusKind.incomplete:
 						ci.setStatus( ConfigurationItem.Status.INCOMPLETE );
 						break;
 					case StatusKind.merged:
 						ci.setStatus( ConfigurationItem.Status.MERGED );
 						break;
 					case StatusKind.missing:
 						ci.setStatus( ConfigurationItem.Status.MISSING );
 						break;
 					case StatusKind.modified:
 						ci.setStatus( ConfigurationItem.Status.MODIFIED );
 						break;
 					case StatusKind.none:
 						ci.setStatus( ConfigurationItem.Status.NORMAL );
 						break;
 					case StatusKind.normal:
 						ci.setStatus( ConfigurationItem.Status.NORMAL );
 						break;
 					case StatusKind.obstructed:
 						ci.setStatus( ConfigurationItem.Status.MODIFIED );
 						break;
 					case StatusKind.replaced:
 						ci.setStatus( ConfigurationItem.Status.MODIFIED );
 						break;
 					case StatusKind.unversioned:
 						ci.setStatus( ConfigurationItem.Status.UNVERSIONED );
 						break;
 					default:
 						ci.setStatus( ConfigurationItem.Status.NORMAL );
 				}
 				items.add( ci );
 			}
 			return items.toArray( new ConfigurationItem[0] );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.info", e );
 		}
 	}
 
 	/**
 	 * Update a file or directory to its latest version (the HEAD from the
 	 * project's repository). If a directory is used as parameter, all the
 	 * files and subdirs are updated. 
 	 * 
 	 * @param path The file or directory to be updated.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when updating
 	 * the file(s).
 	 */
 	public void update( String path )
 	{
 		super.update( path );
 		_update( path, Revision.HEAD );
 	}
 
 	/**
 	 * Update a file or directory to the request version. If a directory is
 	 * used as parameter, all the files and subdirs are updated. 
 	 * 
 	 * @param path The file or directory to be updated.
 	 * @param version The version to update to.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when updating
 	 * the file(s).
 	 */
 	public void update( String path, String version )
 	{
 		super.update( path, version );
 		Revision revision = Revision.getInstance( Long.parseLong( version ) );
 		_update( path, revision );
 	}
 	
 	/**
 	 * Update a file or directory to the requested revision. If a directory is
 	 * used as parameter, all the files and subdirs are updated. 
 	 * 
 	 * @param path The file or directory to be updated.
 	 * @param revision The revision to update to.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when updating
 	 * the file(s).
 	 */
 	private void _update( String path, Revision revision )
 	{
 		try {
 			client.update( path, revision, true );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.update", e );
 		}
 	}
 
 	/**
 	 * Create a branch. 
 	 * 
 	 * @param srcPath The source directory (the directory that will be
 	 * branched).
 	 * @param destPath The branch directory that will be created.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when creating the
 	 * branch.
 	 */
 	public void branch( String srcPath, String destPath )
 	{
 		super.branch( srcPath, destPath );
 		try {
 			client.copy( srcPath, destPath, "", Revision.HEAD );
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.branch", e );
 		}
 	}
 
 	/**
 	 * Diff two files or directories. 
 	 * 
 	 * @param srcPath The source directory.
 	 * @param destPath The destination directory.
 	 * 
 	 * @return The name of the file holding the diff.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when comparing
 	 * the files.
 	 */
 	public String diff( String srcPath, String destPath )
 	{
 		super.diff( srcPath, destPath );
 		try {
 			String destFile = workdir + File.separator + "diff" + getId() +
 				"-" + counter.getAndIncrement() + ".patch";
 			client.diff( srcPath, Revision.HEAD, destPath, Revision.HEAD, destFile, true );
 			return destFile;
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.diff", e );
 		}
 	}
 	
 	/**
 	 * Diff different versions of a files or directory.  
 	 * 
 	 * @param path The file or directory whose versions will be compared.
 	 * @param version1 The source version.
 	 * @param version2 The destination version.
 	 * 
 	 * @return The name of the file holding the diff.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when comparing
 	 * the files.
 	 */	
 	public String diff( String path, String version1, String version2 )
 	{
 		super.diff( path, version1, version2 );
 		return diff( path, version1, path, version2 );
 	}
 
 	/**
 	 * Diff different versions of different files or directory.  
 	 * 
 	 * @param srcPath The source file or directory whose versions will be
 	 * compared.
 	 * @param destPath The destination file or directory whose versions will be
 	 * compared.
 	 * @param version1 The source version.
 	 * @param version2 The destination version.
 	 * 
 	 * @return The name of the file holding the diff.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when comparing
 	 * the files.
 	 */	
 	public String diff( String srcPath, String version1, String destPath, String version2 )
 	{
 		super.diff( srcPath, version1, destPath, version2 );
 		try {
 			Revision revision1 = Revision.getInstance( Long.parseLong( version1 ) );
 			Revision revision2 = Revision.getInstance( Long.parseLong( version2 ) );
 			String destFile = workdir + File.separator + "diff" + getId() +
 				"-" + counter.getAndIncrement() + ".patch";
 			client.diff( srcPath, revision1, destPath, revision2, destFile, true );
 			return destFile;
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.diff", e );
 		}
 	}
 
 	/**
 	 * Commit the modifications made to the workcopy.
 	 * 
 	 * @param changelog Description of the changes made to the repository.
 	 * 
 	 * @throws RepositoryTransactionError If an error occurred when commiting
 	 * the files.
 	 */	
 	public void commit( String changelog )
 	{
 		try {
 			String[] paths = { workdir };
 			client.resolved( workdir, true );
 			client.commit( paths, changelog, true );
 			client.dispose();
 			IoUtil.removeDir(workdir);
 		} catch ( ClientException e ) {
 			log.debug( e.getMessage() );
 			throw new RepositoryTransactionError( "exception.repositoryTransaction.commit", e );
 		}
 		super.commit( changelog );
 	}
 	
 	/**
 	 * Abort (rollback) the modifications made to the workcopy.
 	 */	
 	public void abort()
 	{
 		client.dispose();
 		super.abort();
 	}
 
 	public TransactionStatus getStatus()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
