 package sorcer.util;
 /**
  *
  * Copyright 2013 Rafał Krupiński.
  * Copyright 2013 Sorcersoft.com S.A.
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
 
 
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 
 /**
  * @author Rafał Krupiński
  */
 public class SorcerProviderNameUtil extends ProviderNameUtil {
 
     {
        overrideFromProps("sorcer.core.Cataloger", SorcerConstants.P_CATALOGER_NAME);
         overrideFromProps("sorcer.core.provider.Jobber", SorcerConstants.S_JOBBER_NAME);
         overrideFromProps("sorcer.core.provider.Spacer", SorcerConstants.S_SPACER_NAME);
         overrideFromProps("net.jini.space.JavaSpace05", SorcerConstants.P_SPACE_NAME);
     }
 
     protected void overrideFromProps(String type, String key) {
         String override = SorcerEnv.getProperty(key);
         if (override == null) return;
         names.put(type, override);
     }
 
     @Override
     public String getName(Class<?> providerType) {
         String name = super.getName(providerType);
         if (SorcerEnv.nameSuffixed()) {
             name = SorcerEnv.getSuffixedName(name);
         }
         return name;
     }
 }
