 /*
  * Copyright 2013 jk-5 and Lordmau5
  *
  * jk-5 and Lordmau5 License this file to you under the LGPL v3 License (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  * http://www.gnu.org/licenses/lgpl.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License
  */
 
 package jkmau5.superpowers;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import jkmau5.superpowers.config.ConfigFile;
 import jkmau5.superpowers.proxy.IProxy;
 
 /**
  * Author: Lordmau5
  * Date: 03.09.13
  * Time: 22:44
  */
 @Mod(modid = Constants.MOD_ID, name = "Super Powers", version = SPVersion.VERSION)
 public class SuperPowers {
 
     @SidedProxy(modId = Constants.MOD_ID, clientSide = "jkmau5.superpowers.proxy.ProxyClient", serverSide = "jkmau5.superpowers.proxy.ProxyServer")
    private static IProxy sidedProxy;
 
     private ConfigFile config;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
         this.config = new ConfigFile(event.getSuggestedConfigurationFile()).setComment("Main SuperPowers config file");
     }
 
     @EventHandler
     public void init(FMLInitializationEvent event) {
         sidedProxy.registerEvents();
     }
 
    public ConfigFile getConfig(){
         return this.config;
     }
 }
