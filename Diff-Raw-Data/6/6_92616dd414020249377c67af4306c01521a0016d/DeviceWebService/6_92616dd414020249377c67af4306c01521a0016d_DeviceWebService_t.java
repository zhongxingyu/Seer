 /*
  * Copyright (C) 2011 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.pixmob.droidlink.gae.web.service;
 
 import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
 import static com.pixmob.droidlink.gae.Constants.JSON_MIME_TYPE;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.sitebricks.At;
 import com.google.sitebricks.client.transport.Json;
 import com.google.sitebricks.headless.Reply;
 import com.google.sitebricks.headless.Request;
 import com.google.sitebricks.headless.Service;
 import com.google.sitebricks.http.Delete;
 import com.google.sitebricks.http.Get;
 import com.google.sitebricks.http.Put;
 import com.pixmob.droidlink.gae.queue.SyncQueue;
 import com.pixmob.droidlink.gae.service.AccessDeniedException;
 import com.pixmob.droidlink.gae.service.Device;
 import com.pixmob.droidlink.gae.service.DeviceNotFoundException;
 import com.pixmob.droidlink.gae.service.DeviceService;
 
 /**
  * Remote API for managing devices.
  * @author Pixmob
  */
 @At(DeviceWebService.URI)
 @Service
 public class DeviceWebService {
     public static final String URI = "/api/1/devices";
     private final Logger logger = Logger.getLogger(getClass().getName());
     private final UserService userService;
     private final DeviceService deviceService;
     private final Queue syncQueue;
     
     /**
      * Package protected constructor: use Guice to get an instance of this
      * class.
      */
     @Inject
     DeviceWebService(final DeviceService deviceService, final UserService userService,
             @Named("sync") final Queue syncQueue) {
         this.deviceService = deviceService;
         this.userService = userService;
         this.syncQueue = syncQueue;
     }
     
     @At("/:deviceId/sync")
     @Get
    public Reply<?> syncDevices(Request request, @Named("deviceId") String deviceId) {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
        final String token = request.param("token");
        logger.info("Sync user devices for " + user.getEmail() + "; token=" + token);
         triggerUserSync(user, deviceId, token);
         
         return Reply.saying().ok();
     }
     
     @At("/:deviceId")
     @Get
     public Reply<?> getDevice(@Named("deviceId") String deviceId) {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
         final Device device;
         try {
             device = deviceService.getDevice(user.getEmail(), deviceId);
         } catch (DeviceNotFoundException e) {
             return Reply.saying().notFound();
         } catch (AccessDeniedException e) {
             return Reply.saying().forbidden();
         }
         
         return Reply.with(new DeviceRemote(device)).as(Json.class).type(JSON_MIME_TYPE);
     }
     
     @Get
     public Reply<?> getDevices() {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
         final Collection<DeviceRemote> results = new HashSet<DeviceRemote>(4);
         final Iterable<Device> devices = deviceService.getDevices(user.getEmail());
         for (final Device d : devices) {
             results.add(new DeviceRemote(d));
         }
         if (results.isEmpty()) {
             return Reply.saying().noContent();
         }
         
         return Reply.with(results).as(Json.class).type(JSON_MIME_TYPE);
     }
     
     @At("/:deviceId")
     @Put
     public Reply<?> registerDevice(Request request, @Named("deviceId") String deviceId) {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
         final DeviceRemote device = request.read(DeviceRemote.class).as(Json.class);
         logger.info("Register device " + deviceId);
         try {
             deviceService.registerDevice(user.getEmail(), deviceId, device.name, device.c2dm);
         } catch (AccessDeniedException e) {
             return Reply.saying().forbidden();
         }
         
         return Reply.saying().ok();
     }
     
     @Delete
     public Reply<?> unregisterDevices() {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
         logger.info("Unregister all devices");
         try {
             deviceService.unregisterDevice(user.getEmail(), null);
         } catch (AccessDeniedException e) {
             return Reply.saying().forbidden();
         }
         
         triggerUserSync(user, null, null);
         
         return Reply.saying().ok();
     }
     
     @At("/:deviceId")
     @Delete
     public Reply<?> unregisterDevice(@Named("deviceId") String deviceId) {
         final User user = userService.getCurrentUser();
         if (user == null) {
             return Reply.saying().unauthorized();
         }
         
         logger.info("Unregister device " + deviceId);
         try {
             deviceService.unregisterDevice(user.getEmail(), deviceId);
         } catch (AccessDeniedException e) {
             return Reply.saying().forbidden();
         }
         
         triggerUserSync(user, deviceId, null);
         
         return Reply.saying().ok();
     }
     
     private void triggerUserSync(User user, String deviceIdSource, String token) {
         // Use a queue to close the Http request as soon as possible.
         logger.info("Queue event sync for user " + user.getEmail());
         final TaskOptions taskOptions = withUrl(SyncQueue.URI).param(SyncQueue.USER_PARAM,
             user.getEmail());
         if (deviceIdSource != null) {
             taskOptions.param(SyncQueue.DEVICE_ID_SOURCE_PARAM, deviceIdSource);
         }
         if (token != null) {
             taskOptions.param(SyncQueue.SYNC_TOKEN_PARAM, token);
         }
         syncQueue.add(taskOptions);
     }
 }
