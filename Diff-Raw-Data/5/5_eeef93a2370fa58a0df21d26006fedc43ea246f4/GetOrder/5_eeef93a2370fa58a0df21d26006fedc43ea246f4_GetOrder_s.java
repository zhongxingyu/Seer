 package org.glite.authz.pap.ui.cli.papmanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class GetOrder extends PAPManagementCLI {
     
     private static final String USAGE = "";
     private static final String[] commandNameValues = { "get-paps-order", "gpo" };
     private static final String DESCRIPTION = "Get the defined order of the remote PAPs";
     
     public GetOrder() {
         super(commandNameValues, USAGE, DESCRIPTION, null);
     }
     
 	@Override
     protected Options defineCommandOptions() {
 		return null;
 	}
 
     @Override
     protected int executeCommand(CommandLine commandLine) throws ParseException, RemoteException {
     	
     	String[] aliasArray = papMgmtClient.getOrder();
     	
     	if (aliasArray == null) {
     		System.out.println("No ordering has been defined");
     		return ExitStatus.SUCCESS.ordinal();
     	}
     	
     	if (aliasArray.length == 0) {
     		System.out.println("No ordering has been defined");
     		return ExitStatus.SUCCESS.ordinal();
     	}
     	
     	System.out.print(aliasArray[0]);
     	
     	for (int i=1; i<aliasArray.length; i++) {
    		System.out.println(", " + aliasArray[i]);
     	}
     	
         return ExitStatus.SUCCESS.ordinal();
         
     }
 }
