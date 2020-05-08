 /**
  * Copyright (C) 2012 MK124
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
 
 package net.gtaun.shoebill.dependency;
 
 import java.io.PrintStream;
 
 import org.sonatype.aether.AbstractRepositoryListener;
 import org.sonatype.aether.RepositoryEvent;
 
 /**
  * 
  * 
  * @author MK124
  */
 public class ShoebillRepositoryListener extends AbstractRepositoryListener
 {
 	private PrintStream out;
 	
 	
 	public ShoebillRepositoryListener()
 	{
 		this(null);
 	}
 	
 	public ShoebillRepositoryListener(PrintStream out)
 	{
 		this.out = (out != null) ? out : System.out;
 	}
 	
 	public void artifactDescriptorInvalid(RepositoryEvent event)
 	{
 		out.println("Invalid artifact descriptor for " + event.getArtifact() + ": " + event.getException().getMessage());
 	}
 	
 	public void artifactDescriptorMissing(RepositoryEvent event)
 	{
 		out.println("Missing artifact descriptor for " + event.getArtifact());
 	}
 	
 	public void artifactResolved(RepositoryEvent event)
 	{
		if (event.getArtifact().getExtension().equals("pom")) return;
 		out.println("Resolved artifact " + event.getArtifact() + " from " + event.getRepository().getId());
 	}
 	
 	public void artifactDownloading(RepositoryEvent event)
 	{
		if (event.getArtifact().getExtension().equals("pom")) return;
 		out.println("Downloading artifact " + event.getArtifact() + " from " + event.getRepository().getId());
 	}
 	
 	public void artifactDownloaded(RepositoryEvent event)
 	{
		if (event.getArtifact().getExtension().equals("pom")) return;
 		if (event.getException() != null) return;
 		out.println("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository().getId());
 	}
 	
 	public void metadataInvalid(RepositoryEvent event)
 	{
 		out.println("Invalid metadata " + event.getMetadata());
 	}
 }
