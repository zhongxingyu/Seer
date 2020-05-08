 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2010 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.GlobalSettings;
 import screen.tools.sbs.targets.Parameters;
 import screen.tools.sbs.targets.TargetCall;
 import screen.tools.sbs.utils.targethelper.Mandatory;
 import screen.tools.sbs.utils.targethelper.Option;
 
 public class TargetHelper {
 	private List<Mandatory> mandatories;
 	private List<Option> options;
 	private TargetCall target;
 	
 	public TargetHelper(TargetCall targetCall){
 		mandatories = new ArrayList<Mandatory>();
 		options = new ArrayList<Option>();
 		target = targetCall;
 	}
 	
 	public void addMandatory(Mandatory mandatory){
 		mandatories.add(mandatory);
 	}
 	
 	public void addOption(Option option){
 		options.add(option);
 	}
 	
 	public void perform(Parameters pars){
 		ErrorList err = GlobalSettings.getGlobalSettings().getErrorList();
 		int parIt = 0;
 		
 		for(int i = 0; i<mandatories.size(); i++){
 			int tmp = mandatories.get(i).perform(pars, parIt);
 			if(tmp!=-1){
 				parIt = tmp;
 				parIt++;
 			}
 			else{
				if(pars.size() > parIt)
					err.addError("Unknown parameter / \""+pars.getParameterAt(parIt)+"\" isn't a valid mandatory parameter");
				else
					err.addError("Missing mandatory parameter");
 				GlobalSettings.getGlobalSettings().needUsage();
 				return;
 			}
 		}
 		
 		for(int i = parIt; i<pars.size(); i++){
 			String par = pars.getParameterAt(i);
 			boolean matchOption = false;
 			for(int j = 0; j<options.size(); j++){
 				int tmp = options.get(j).perform(pars, i);
 				matchOption = (tmp!=-1);
 				if(matchOption){
 					i = tmp;
 					break;
 				}
 			}
 			if(!matchOption){
 				err.addError("Unknown parameter / \""+par+"\" isn't a valid option");
 				GlobalSettings.getGlobalSettings().needUsage();
 			}
 		}
 	}
 	
 	public void usage(){
 		String targetUsage = "sbs "+target.getTarget();
 		for(int i=0; i<mandatories.size(); i++){
 			targetUsage+=" <"+mandatories.get(i).getDescription()+">";
 		}
 		targetUsage+=" [options]";
 		Logger.info("target usage :");
 		Logger.info("    "+targetUsage);
 		Logger.info("parameters :");
 		Logger.info("    mandatory :");
 		for(int i=0; i<mandatories.size(); i++){
 			List<String> manUsage = new ArrayList<String>();
 			mandatories.get(i).usage(manUsage);
 			for(int j=0; j<manUsage.size(); j++){
 				Logger.info("        "+manUsage.get(j));
 			}
 		}
 		Logger.info("    optional :");
 		for(int i=0; i<options.size(); i++){
 			List<String> manUsage = new ArrayList<String>();
 			options.get(i).usage(manUsage);
 			for(int j=0; j<manUsage.size(); j++){
 				Logger.info("        "+manUsage.get(j));
 			}
 		}
 	}
 }
