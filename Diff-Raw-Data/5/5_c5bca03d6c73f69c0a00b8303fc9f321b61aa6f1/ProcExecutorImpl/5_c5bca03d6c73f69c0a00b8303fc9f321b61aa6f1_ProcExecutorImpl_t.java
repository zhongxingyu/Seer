 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import models.ServerNode;
 
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.PumpStreamHandler;
 
 import play.cache.Cache;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.ProcOutputStream;
 import server.WriteEventListener;
 
 
 /**
  * This class extends a {@link DefaultExecutor} and contains the server information where the recipe was deployed.
  * It also contains an output stream for the forked process. 
  * 
  * @author Igor Goldenberg
  * @see DeployManager
  */
 public class ProcExecutorImpl extends DefaultExecutor implements ProcExecutor 
 {
     private String id;
     private File recipe;
     private String[] args;
 
     final static class ProcessStreamHandler extends PumpStreamHandler
 	 {
 		private WriteEventListener wel;
 		
 		public ProcessStreamHandler(WriteEventListener wel) { 
 			this.setWriteEventListener(wel);
 			
 		}
 		
 		@Override
 		protected void createProcessOutputPump(InputStream is, OutputStream os)
 		{
 			ProcOutputStream procOutputStream = createOutputStream();
 			super.createProcessOutputPump(is, procOutputStream);
 		}
 		
 		
 		@Override
 		protected void createProcessErrorPump(InputStream is, OutputStream os)
 		{
 			ProcOutputStream procOutputStream = createOutputStream();
 			super.createProcessErrorPump(is, procOutputStream);
 		}
 		
 		public void setWriteEventListener(final WriteEventListener wel) {
 			this.wel = wel;
 		}
 		
 		private ProcOutputStream createOutputStream() {
 			ProcOutputStream procOutputStream = new ProcOutputStream();
 			procOutputStream.setProcEventListener(this.wel);
 			return procOutputStream;
 		}
 	 }
    
    public ProcExecutorImpl() { }
 
     public ProcExecutorImpl( ServerNode server, File recipe, String... args )
     {
         this.setId(server.getId());
 
         this.recipe = recipe;
         this.args = args;
         this.id = server.getId();
         
         Cache.set( "output-" + server.getId(),  new StringBuilder());
     }
 
     public File getRecipe()
     {
         return recipe;
     }
 
     public String[] getArgs()
     {
         return args;
     }
 
 	@Override
 	public void setRecipe(File recipe) {
 		this.recipe = recipe;
 	}
 
 	@Override
 	public void setArgs(String... args) {
 		this.args = args;
 	}
 	
 	@Override
     public String getId()
     {
         return id;
     }
 	
 	@Override
 	public void setId(String id) {
 		this.id = id;
 	}
 
 }
