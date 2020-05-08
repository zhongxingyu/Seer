 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.cli;
 
 import java.util.List;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 /**
  * This command will list all deployed cadmium wars.
  * 
  * @author John McEntire
  *
  */
 @Parameters(commandDescription="Lists all cadmium wars deployed", separators="=")
 public class ListCommand extends AbstractAuthorizedOnly implements CliCommand {
 
   @Parameter(description="<site>", required=true)
   private List<String> args;
   
   @Override
   public String getCommandName() {
     return "list";
   }
 
   @Override
   public void execute() throws Exception {
     if(args == null || args.size() != 1) {
       System.err.println("Please specify the url to a deployer.");
       System.exit(1);
     }
     try{
       String site = getSecureBaseUrl(args.get(0));
       List<String> deployed = UndeployCommand.getDeployed(site, token);
       if(deployed == null || deployed.isEmpty()) {
         System.out.println("There are no cadmium wars currently deployed.");
         return;
       }
      System.out.format("%s\n", "Cadmium App");
       for(String app : deployed) {
        System.out.format("\"%s\"\n", app);
       }
     } catch(Throwable t){t.printStackTrace();}
   }
 
 }
