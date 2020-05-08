 package com.bradmcevoy.http.http11.auth;
 
 import com.bradmcevoy.http.Request;
 import com.bradmcevoy.http.Resource;
 import java.util.Date;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author brad
  */
 public class SimpleMemoryNonceProvider implements NonceProvider {
 
     private static final Logger log = LoggerFactory.getLogger( SimpleMemoryNonceProvider.class );
     private final int nonceValiditySeconds;
     private Map<UUID, Nonce> nonces = new ConcurrentHashMap<UUID, Nonce>();
     private final ExpiredNonceRemover remover;
    private boolean enableNonceCountChecking;
 
     public SimpleMemoryNonceProvider( int nonceValiditySeconds ) {
         this.nonceValiditySeconds = nonceValiditySeconds;
         this.remover = new ExpiredNonceRemover( nonces, nonceValiditySeconds );
         log.debug( "created" );
     }
 
     public SimpleMemoryNonceProvider( int nonceValiditySeconds, ExpiredNonceRemover remover ) {
         this.nonceValiditySeconds = nonceValiditySeconds;
         this.remover = remover;
     }
 
     public String createNonce( Resource resource, Request request ) {
         UUID id = UUID.randomUUID();
         Date now = new Date();
         Nonce n = new Nonce( id, now );
         nonces.put( n.getValue(), n );
         log.debug( "created nonce: " + n.getValue() );
         log.debug( "map size: " + nonces.size() );
         return n.getValue().toString();
     }
 
     public NonceValidity getNonceValidity( String nonce, Long nc ) {
         log.debug( "getNonceValidity: " + nonce );
         UUID value = null;
         try {
             value = UUID.fromString( nonce );
         } catch( Exception e ) {
             log.debug( "couldnt parse nonce" );
             return NonceValidity.INVALID;
         }
         Nonce n = nonces.get( value );
         if( n == null ) {
             log.debug( "not found in map of size: " + nonces.size() );
             return NonceValidity.INVALID;
         } else {
             if( isExpired( n.getIssued() ) ) {
                 log.debug( "nonce has expired" );
                 return NonceValidity.EXPIRED;
             } else {
                 if( nc == null ) {
                     log.debug( "nonce ok" );
                     return NonceValidity.OK;
                 } else {
                    if( enableNonceCountChecking && nc <= n.getNonceCount() ) {
                         log.warn( "nonce-count was not greater then previous, possible replay attack. new: " + nc + " old:" + n.getNonceCount() );
                         return NonceValidity.INVALID;
                     } else {
                         log.debug( "nonce and nonce-count ok" );
                         Nonce newNonce = n.increaseNonceCount( nc );
                         nonces.put( newNonce.getValue(), newNonce );
                         return NonceValidity.OK;
                     }
                 }
             }
         }
     }
 
     private boolean isExpired( Date issued ) {
         long dif = ( System.currentTimeMillis() - issued.getTime() ) / 1000;
         return dif > nonceValiditySeconds;
     }

    /**
     * IE seems to send nc (nonce count) parameters out of order. To correctly
     * implement checking we need to record which nonces have been sent, and not
     * assume they will be sent in a monotonically increasing sequence.
     *
     * The quick fix here is to disable checking of the nc param, since other
     * common servers seem to do so to.
     *
     * Note that this will allow replay attacks.
     *
     * @return
     */
    public boolean isEnableNonceCountChecking() {
        return enableNonceCountChecking;
    }

    public void setEnableNonceCountChecking( boolean enableNonceCountChecking ) {
        this.enableNonceCountChecking = enableNonceCountChecking;
    }

 }
