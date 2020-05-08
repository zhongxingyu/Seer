 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core;
 
 import java.net.URI;
 import java.util.Collection;
 
 import scala.Option;
 
 import me.footlights.api.KernelInterface;
 import me.footlights.core.apps.*;
 import me.footlights.core.data.File;
 import me.footlights.core.data.Link;
 import me.footlights.core.data.store.Stat;
 
 
 /** Interface to the software core */
 public interface Footlights extends KernelInterface
 {
 	/** Open a particular {@link Link}. */
 	public Option<File> open(Link link);
 
 	/** Save data to a local {@link java.io.File}. */
 	public void saveLocal(File file, java.io.File filename);
 
 	/**
 	 * Convert a placeholder name (e.g. "user.name") into a meaningful value.
 	 *
 	 * This is part of {@link Footlights} rather than the {@link KernelInterface} because apps
 	 * cannot request placeholder evaluation directly; it has to be done by a trusted bit of UI
 	 * code, which inserts the proxied content in such a way that the app UI can't read it.
 	 */
 	public String evaluate(String placeholder);
 
 	public void registerUI(UI ui);
 	public void deregisterUI(UI ui);
 
 	public Option<java.util.jar.JarFile> localizeJar(URI uri);
 
 	/**
 	 * List some of the {@link File} names which are known to exist in the {@link Store}.
 	 *
 	 * This list describes files stored in local cache, not remotely on the global CAS.
 	 */
 	public Collection<Stat> listFiles();
 
 	public Collection<AppWrapper> runningApplications();
	public AppWrapper loadApplication(URI uri) throws AppStartupException;
 	public void unloadApplication(AppWrapper plugin);
 }
