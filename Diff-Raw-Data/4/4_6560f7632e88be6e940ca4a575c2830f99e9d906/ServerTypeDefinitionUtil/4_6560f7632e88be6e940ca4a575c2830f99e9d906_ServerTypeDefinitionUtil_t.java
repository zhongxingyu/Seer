 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gorkem Ercan - initial API and implementation
  *     Naci M. Dai
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 
 package org.eclipse.jst.server.generic.internal.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.server.generic.core.CorePlugin;
import org.eclipse.jst.server.generic.internal.xml.Resolver;
 import org.eclipse.jst.server.generic.servertype.definition.ArchiveType;
 import org.eclipse.jst.server.generic.servertype.definition.Classpath;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IRuntime;
 
 
 public class ServerTypeDefinitionUtil 
 {
 	/**
 	 * 
 	 * @param runtime
 	 * @return
 	 */
 	public static ServerRuntime getServerTypeDefinition(IRuntime runtime)
 	{
 		String serverType = runtime.getAttribute(GenericServerRuntime.SERVER_DEFINITION_ID,(String)null);
 		Map properties = runtime.getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,(Map)null);
 		ServerRuntime definition = 
 			CorePlugin.getDefault().getServerTypeDefinitionManager().getServerRuntimeDefinition(serverType,properties);
 		return definition;
 	}
 	
 	public static IClasspathEntry[] getServerClassPathEntry(IRuntime runtime)
 	{
 		ServerRuntime definition = getServerTypeDefinition(runtime);		
 		String ref = definition.getProject().getClasspathReference();
 		Classpath cp = definition.getClasspath(ref);
 		Iterator archives = cp.getArchive().iterator();
 		ArrayList entryList = new ArrayList();
 		while (archives.hasNext()) {
 			ArchiveType archive = (ArchiveType) archives.next();
			String item = definition.getResolver().resolveProperties(archive.getPath());
 			IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(item),null,null );
 			entryList.add(entry);
 		}
 
 		return (IClasspathEntry[])entryList.toArray(new IClasspathEntry[entryList.size()]);
 	}
 	
 }
