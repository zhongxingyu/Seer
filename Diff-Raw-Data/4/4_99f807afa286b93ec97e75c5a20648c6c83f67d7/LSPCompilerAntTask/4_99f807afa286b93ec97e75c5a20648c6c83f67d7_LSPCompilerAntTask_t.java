 /*
  * Copyright (c) 2003-2005, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp.compiler;
 
 import java.io.*;
 
 import org.apache.tools.ant.*;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 
 import nu.staldal.lsp.*;
  
 
 /**
  * Apache Ant interface to the LSP compiler.
  *
  * @see LSPCompilerHelper
  */
 public class LSPCompilerAntTask extends Task
 {
 	private LSPCompilerHelper compiler;
 	
 	private Path sourcepath;
 	private File destdir;	
 	private FileSet fileset;
 	private boolean force;
 	private boolean xhtml;
     private boolean acceptNull;
 
 	
 	public LSPCompilerAntTask()
 	{
 		compiler = new LSPCompilerHelper();
 	}
 
 
 	public void init()
     	throws BuildException
 	{
 		fileset = null;
 		sourcepath = null;		
 		destdir = null;
 		force = false;
         xhtml = false;
         acceptNull= false;
     }
 
 	
 	// Attribute setter methods
 	
 	public void setForce(boolean force)
 	{
 		this.force = force;
 	}
 	
 	public void setXhtml(boolean xhtml)
 	{
 		this.xhtml = xhtml;
 	}
 
 	public void setAcceptNull(boolean acceptNull)
 	{
 		this.acceptNull = acceptNull;
 	}
 
 	public void setSourcepath(Path sourcepath)
 	{
 		this.sourcepath = sourcepath;
 	}
 		
 	public void setDestdir(File destdir)
 	{
 		this.destdir = destdir;
 	}
 
 	
 	// Handle nested elements
 	
 	public void addConfiguredFileset(FileSet fileset)
 	{
 		this.fileset = fileset;	
 	}
 	
 		
 	public void execute() throws BuildException
 	{
 		if (fileset == null)
 			throw new BuildException("Must have a nested <fileset> element");
 
 		if (destdir == null)
 			throw new BuildException("Must have a destdir attribute");
 
 		compiler.targetDir = destdir;
         
         compiler.setXhtml(xhtml);
         compiler.setAcceptNull(acceptNull);
 		
 		if (sourcepath != null)
         {
             String[] _sp = sourcepath.list();
             File[] sp = new File[_sp.length];
             
             for (int i = 0; i<_sp.length; i++)
             {
                 sp[i] = new File(_sp[i]);   
             }
             
             compiler.sourcePath = sp;            
         }
 
		DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
		File fromDir = fileset.getDir(getProject());
 		compiler.startDir = fromDir;
 
 		String[] srcFiles = ds.getIncludedFiles();
 		
 		for (int i = 0; i<srcFiles.length; i++)
 		{
 			try {
 				if (compiler.doCompile(srcFiles[i], force))
 					log("Compiling " + srcFiles[i]);
 			}
 			catch (LSPException e)
 			{
 				log(e.getMessage(), Project.MSG_ERR);
 				throw new BuildException("LSP compilation failed");
 			}			
 		}
 	}
 
 }
 
