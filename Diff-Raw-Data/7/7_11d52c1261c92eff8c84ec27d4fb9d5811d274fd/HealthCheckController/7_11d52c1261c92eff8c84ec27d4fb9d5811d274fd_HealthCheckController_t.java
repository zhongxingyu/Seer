 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.*;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.domain.UserGroup;
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.service.GroupService;
 import nl.surfnet.bod.util.Environment;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.google.common.collect.Lists;
 
 @Controller
 public class HealthCheckController {
 
   private Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
 
   @Resource
   private IddClient iddClient;
 
   @Resource
   private NbiClient nbiClient;
 
   @Resource
   private GroupService groupService;
 
   @Resource(name = "bodEnvironment")
   private Environment environment;
 
   @RequestMapping(value = "/healthcheck")
   public String index(Model model) {
 
     final ServiceCheck iddServiceCheck = new ServiceCheck() {
       @Override
       public boolean healty() {
         return iddClient.getInstitutes().size() > 0;
       }
 
       @Override
       public String getName() {
         return "IDD";
       }
     };
 
     final ServiceCheck nbiServiceCheck = new ServiceCheck() {
       @Override
       public boolean healty() {
         return nbiClient.getPhysicalPortsCount() > 0;
       }
 
       @Override
       public String getName() {
         return "NBI";
       }
     };
 
     final ServiceCheck oAuthServerServiceCheck = new ServiceCheck() {
       @Override
       public boolean healty() throws IOException {
         DefaultHttpClient client = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(environment.getOauthServerUrl() + "/admin");
         HttpResponse response = client.execute(httpGet);
         httpGet.releaseConnection();
         return response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN;
       }
 
       @Override
       public String getName() {
         return "OAuth server";
       }
     };
 
     final ServiceCheck apiServiceCheck = new ServiceCheck() {
       private static final String PERSON_URI = "urn:collab:person:surfnet.nl:hanst";
 
       @Override
       public boolean healty() throws IOException {
         Collection<UserGroup> groups = groupService.getGroups(PERSON_URI);
         return groups.size() > 0;
       }
 
       @Override
       public String getName() {
         return "API (groupService)";
       }
     };
 
     @SuppressWarnings("unchecked")
     List<Callable<Boolean>> tasks = Lists.newArrayList(
         callable(iddServiceCheck),
         callable(nbiServiceCheck),
         callable(oAuthServerServiceCheck),
         callable(apiServiceCheck)
     );
 
     try {
       List<Future<Boolean>> futures = Executors.newFixedThreadPool(4).invokeAll(tasks, 15, TimeUnit.SECONDS);
 
       model.addAttribute("iddHealth", toBooleanValue(futures.get(0)));
       model.addAttribute("nbiHealth", toBooleanValue(futures.get(1)));
       model.addAttribute("oAuthServer", toBooleanValue(futures.get(2)));
       model.addAttribute("openConextApi", toBooleanValue(futures.get(3)));
     }
     catch (InterruptedException e) {
      logger.error("Error during calling healthchecks", e);
     }
 
     return "healthcheck";
   }
 
   private boolean toBooleanValue(Future<Boolean> healthFuture) {
     if (healthFuture.isCancelled()) {
       return false;
     }
 
     try {
       return healthFuture.isDone() && healthFuture.get();
     }
     catch (InterruptedException | ExecutionException e) {
      logger.error("Error during healthcheck", e);
       return false;
     }
   }
 
   private Callable<Boolean> callable(final ServiceCheck serviceCheck) {
     return new Callable<Boolean>() {
       @Override
       public Boolean call() {
         return isServiceHealthy(serviceCheck);
       }
     };
   }
 
   public boolean isServiceHealthy(ServiceCheck check) {
     boolean result;
     try {
       result = check.healty();
      logger.debug("Performed healthcheck for {}", check.getName());
     }
     catch (Exception e) {
       logger.error("Healthcheck failed: ", e);
       result = false;
     }
 
     if (!result) {
       logger.error("HealthCheck for '{}' failed", check.getName());
     }
 
     return result;
   }
 
   interface ServiceCheck {
     boolean healty() throws Exception;
     String getName();
   }
 
   protected void setLogger(Logger logger) {
     this.logger = logger;
   }
 }
