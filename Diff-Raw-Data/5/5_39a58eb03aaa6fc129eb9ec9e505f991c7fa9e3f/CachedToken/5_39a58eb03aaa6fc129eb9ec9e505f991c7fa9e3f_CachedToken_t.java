 package com.cloudbees.api;
 
 import com.cloudbees.api.oauth.OauthToken;
 
 import javax.annotation.CheckForNull;
 import javax.annotation.Nullable;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 final class CachedToken {
     /**
      * Null if the token was invalid to begin with.
      */
     private final @Nullable OauthToken token;
     private final long expiration;
 
     CachedToken(OauthToken token) {
         this.token = token;
        if (token!=null)
            expiration = System.currentTimeMillis()+token.getExpiresIn();
        else
            expiration = -1;
     }
 
     public @CheckForNull OauthToken get() {
         if (token==null)    return null;
         OauthToken t = token.clone();
         t.setExpiresIn(round(expiration - System.currentTimeMillis()));
         return t;
     }
 
     /**
      * Converts long to int by rounding values outside the range of int to the max/min values.
      */
     private int round(long l) {
         if (l>Integer.MAX_VALUE)    return Integer.MAX_VALUE;
         if (l<Integer.MIN_VALUE)    return Integer.MIN_VALUE;
         return (int)l;
     }
 }
