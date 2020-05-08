 package com.ikkerens.apifetcher;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.methods.HttpUriRequest;
 
 import com.ikkerens.apifetcher.methods.Delete;
 import com.ikkerens.apifetcher.methods.Get;
 import com.ikkerens.apifetcher.methods.JsonPost;
 import com.ikkerens.apifetcher.methods.JsonPut;
 import com.ikkerens.apifetcher.methods.Post;
 
 public enum Method {
     GET ( Get.class ),
     POST ( Post.class ),
     JSON_POST ( JsonPost.class ),
     JSON_PUT ( JsonPut.class ),
     DELETE ( Delete.class );
 
     private Class< ? extends ApiMethod > clazz;
 
     private Method( Class< ? extends ApiMethod > clazz ) {
         this.clazz = clazz;
     }
 
     HttpUriRequest prepare( URI uri ) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (HttpUriRequest) clazz.getConstructor( URI.class ).newInstance( uri );
     }
 
     String getHeader() throws InstantiationException, IllegalAccessException {
         return clazz.newInstance().getHeader();
     }
 
     HttpEntity getBody( Map< String, Object > arguments ) throws Exception {
         return clazz.newInstance().getBody( arguments );
     }
 }
