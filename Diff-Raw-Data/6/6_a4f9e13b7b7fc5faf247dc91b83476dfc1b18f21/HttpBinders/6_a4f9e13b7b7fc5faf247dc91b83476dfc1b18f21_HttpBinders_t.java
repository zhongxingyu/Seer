 package playground.caliper;
 
 import com.google.caliper.Benchmark;
 import com.google.common.base.Charsets;
 import feign.Feign;
 import feign.gson.GsonCodec;
 import retrofit.RestAdapter;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
 
 /**
  * tests the relative performance of wiring vs usage w/o taking into account network.
  */
 public class HttpBinders {
   private static final String API_URL = "https://api.github.com";
 
   static class Contributor {
     String login;
     int contributions;
   }
 
   /**
    * annotations in order of retrofit, feign
    */
   interface GitHub {
 
     @retrofit.http.GET("/repos/{owner}/{repo}/contributors")
     @feign.RequestLine("GET /repos/{owner}/{repo}/contributors") List<Contributor> contributors(
         @retrofit.http.Path("owner")
        @javax.inject.Named("owner") String owner,
         @retrofit.http.Path("repo")
        @javax.inject.Named("repo") String repo
     );
   }
 
   @Benchmark int initializeRetrofit(int reps) {
     int hashCode = 0;
     for (int i = 0; i < reps; ++i) {
       hashCode |= retrofitGitHub().hashCode();
     }
     return hashCode;
   }
 
   @Benchmark int initializeFeign(int reps) {
     int hashCode = 0;
     for (int i = 0; i < reps; ++i) {
       hashCode |= feignGitHub().hashCode();
     }
     return hashCode;
   }
 
   @Benchmark int useRetrofit(int reps) {
     int size = 0;
     for (int i = 0; i < reps; ++i) {
       size |= retrofitGitHub.contributors("foo", "bar").size();
     }
     return size;
   }
 
   @Benchmark int useFeign(int reps) {
     int size = 0;
     for (int i = 0; i < reps; ++i) {
       size |= feignGitHub.contributors("foo", "bar").size();
     }
     return size;
   }
 
   private final GitHub retrofitGitHub = retrofitGitHub();
   private final GitHub feignGitHub = feignGitHub();
 
   private static GitHub retrofitGitHub() {
     return new RestAdapter.Builder() //
         .setServer(API_URL) //
         .setClient(RetrofitClient.INSTANCE) //
         .setExecutors(sameThreadExecutor(), sameThreadExecutor()) //
         .build().create(GitHub.class);
   }
 
   private static enum RetrofitClient implements retrofit.client.Client {
     INSTANCE;
 
     @Override public retrofit.client.Response execute(retrofit.client.Request request) throws IOException {
       return retrofitResponse;
     }
   }
 
   private static final retrofit.client.Response retrofitResponse = new retrofit.client.Response(200, "OK", Collections.<retrofit.client.Header>emptyList(), new retrofit.mime.TypedByteArray("application/json", "[]".getBytes(Charsets.UTF_8)));
 
   private static GitHub feignGitHub() {
     return Feign.builder()//
         .decoder(new GsonCodec())//
         .client(FeignClient.INSTANCE)//
         .target(GitHub.class, API_URL);
   }
 
   enum FeignClient implements feign.Client {
     INSTANCE;
 
     @Override public feign.Response execute(feign.Request request, feign.Request.Options options) throws IOException {
       return feignResponse;
     }
   }
 
   private static final feign.Response feignResponse = feign.Response.create(200, "OK", Collections.<String, Collection<String>>emptyMap(), "[]");
 }
