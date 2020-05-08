 package com.ettrema.http.fs;
 
 import com.bradmcevoy.http.LockInfo;
 import com.bradmcevoy.http.LockResult;
 import com.bradmcevoy.http.LockTimeout;
 import com.bradmcevoy.http.LockToken;
 import com.sun.servicetag.UnauthorizedAccessException;
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class MemoryLockManager implements FsLockManager {
 
     private static final Logger log = LoggerFactory.getLogger( MemoryLockManager.class );
     /**
      * maps current locks by the file associated with the resource
      */
     Map<File, CurrentLock> locksByFile;
     Map<String, CurrentLock> locksByToken;
 
     public MemoryLockManager() {
         locksByFile = new HashMap<File, CurrentLock>();
         locksByToken = new HashMap<String, CurrentLock>();
     }
 
     public synchronized LockResult lock( LockTimeout timeout, LockInfo lockInfo, FsResource resource ) {
         LockToken currentLock = currentLock( resource );
         if( currentLock != null ) {
             return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
         }
         LockToken newToken = new LockToken( UUID.randomUUID().toString(), lockInfo, timeout );
        CurrentLock newLock = new CurrentLock( resource.getFile(), newToken, lockInfo.owner );
         locksByFile.put( resource.getFile(), newLock );
         locksByToken.put( newToken.tokenId, newLock );
         return LockResult.success( newToken );
     }
 
     public synchronized LockResult refresh( String tokenId, FsResource resource ) {
         CurrentLock curLock = locksByToken.get( tokenId );
         curLock.token.setFrom( new Date() );
         return LockResult.success( curLock.token );
     }
 
     public synchronized void unlock( String tokenId, FsResource resource ) {
         LockToken lockToken = currentLock( resource );
         if( lockToken == null ) {
             log.debug( "not locked" );
             return;
         }
         if( lockToken.tokenId.equals( tokenId ) ) {
             removeLock( lockToken );
         } else {
             throw new UnauthorizedAccessException( "Request lock id is not valid" );
         }
     }
 
     private LockToken currentLock( FsResource resource ) {
         CurrentLock curLock = locksByFile.get( resource.getFile() );
         if( curLock == null ) return null;
         LockToken token = curLock.token;
         if( token.isExpired() ) {
             removeLock( token );
             return null;
         } else {
             return token;
         }
     }
 
     private void removeLock( LockToken token ) {
         log.debug( "removeLock: " + token.tokenId );
         CurrentLock currentLock = locksByToken.get( token.tokenId );
         if( currentLock != null ) {
             locksByFile.remove( currentLock.file );
             locksByToken.remove( currentLock.token.tokenId );
         } else {
             log.warn( "couldnt find lock: " + token.tokenId );
         }
     }
 
     public LockToken getCurrentToken( FsResource resource ) {
         CurrentLock lock = locksByFile.get( resource.getFile() );
         if( lock == null ) return null;
         LockToken token = new LockToken();
         token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.owner, LockInfo.LockDepth.ZERO );
         token.timeout = lock.token.timeout;
         token.tokenId = lock.token.tokenId;
         return token;
     }
 
     class CurrentLock {
 
         final File file;
         final LockToken token;
         final String owner;
 
        public CurrentLock( File file, LockToken token, String owner ) {
             this.file = file;
             this.token = token;
             this.owner = owner;
         }
     }
 }
