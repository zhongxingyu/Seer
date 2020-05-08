 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.downloads;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.CLInput;
 import com.mimpidev.podsalinan.cli.CLInterface;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 
 /**
  * @author sbell
  *
  */
 public class DeleteDownload extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public DeleteDownload(DataStorage newData) {
 		super(newData);
 		debug=true;
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		if (debug) Podsalinan.debugLog.logInfo(this," command: "+command);
 		String downloadUid="";
 		String[] commandOptions = command.split(" ");
 
 		downloadUid = commandOptions[0];
 		if (data.getUrlDownloads().findDownloadByUid(downloadUid)==null){
 			downloadUid="";
 		}
 		
 		if (commandOptions.length==1){
 			ShowDownloadDetails printDetails = new ShowDownloadDetails(data);
 			printDetails.execute(downloadUid);
 		}
 		
 		if (downloadUid.length()>0){
 			CLInput input = new CLInput();
 			if(input.confirmRemoval()){
 				data.getUrlDownloads().deleteActiveDownload(downloadUid);
 				System.out.println("Download Removed.");
 				if ((CLInterface.cliGlobals.getGlobalSelection().containsKey("downloads"))&&
 					(CLInterface.cliGlobals.getGlobalSelection().get("downloads").equalsIgnoreCase(downloadUid))){
 					CLInterface.cliGlobals.getGlobalSelection().clear();
				} else {
					String[] selection = CLInterface.cliGlobals.globalSelectionToString().split(" ", 2);
					returnObject.methodCall = selection[0];
					returnObject.methodParameters= (selection.length>1?selection[1]:"");
 				}
 			}
 			if (commandOptions.length>1){
 				returnObject.methodCall="downloads";
		        returnObject.methodParameters="";
 			}
 	        returnObject.execute=true;
 		} else {
 			System.out.println("Download does not exist");
 		}
 		
 		return returnObject;
 	}
 
 }
