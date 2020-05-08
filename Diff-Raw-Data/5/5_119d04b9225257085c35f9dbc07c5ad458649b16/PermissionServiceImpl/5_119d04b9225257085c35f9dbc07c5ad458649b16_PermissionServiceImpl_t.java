 /*
  * Copyright 2013 Blazebit.
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
 package com.blazebit.security.impl;
 
 import com.blazebit.security.Action;
 import com.blazebit.security.Permission;
 import com.blazebit.security.PermissionService;
 import com.blazebit.security.Resource;
 import com.blazebit.security.Role;
 import com.blazebit.security.Subject;
 import java.util.Collection;
 
 /**
  *
  * @author Christian Beikov
  */
 public class PermissionServiceImpl implements PermissionService {
 
     @Override
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> boolean isGranted(Subject<R, P, Q> subject, Action action) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> void grant(Subject<R, P, Q> authorizer, Subject<R, P, Q> subject, Action action) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> void revoke(Subject<R, P, Q> authorizer, Subject<R, P, Q> subject, Action action) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> Collection<Action> getAllowedActions(Subject<R, P, Q> subject) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Action getGrantAction() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Action getRevokeAction() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Action getAllAction() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Action getNoneAction() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Resource getAllResource() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
