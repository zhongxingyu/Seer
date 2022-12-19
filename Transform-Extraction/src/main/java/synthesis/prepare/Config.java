package synthesis.prepare;

public class Config {

	public static String projectsRoot;
	public static String projectsRootfix;
	public static String defects4jRoot;
	public static String defects4jdiffraw;
	public static String defects4jresult;
	public static String defects4jpatch;
	public static String defects4jrepairtemp;
	public static String defects4jtempstore;
	public static String resultsRoot; 
	public static String javaHome_8;
	public static String javaHome8;
	
	public static void setPath(){
		 
		 String userHome = System.getProperty("user.home");
		 projectsRoot=userHome+"/projects"; 
		 projectsRootfix=userHome+"/projects_fix"; 
		 // the root path that contains the defects4j framework
		 defects4jRoot=userHome+"/defects4j";
		 defects4jdiffraw=userHome+"/defects4j-diff-raw";
		 defects4jresult=userHome+"/defects4j-result";
		 defects4jpatch=userHome+"/defects4j-patch";
		 defects4jrepairtemp=userHome+"/defects4j-tempoary";
		 defects4jtempstore=userHome+"/defects4j-tempoary-store";
		 // the root path that stores the results
		 resultsRoot=userHome+"/transform-synthesis-results";  
		 
		 javaHome_8 = userHome+"/jdk1.8";	 
		 javaHome8 = userHome+"/jdk1.8/bin";
	}
	
	public static String processPath(String orginalPath, int jdkNumber) {
		
		String transferredPath="";
		if(orginalPath.indexOf("java-8-openjdk")!=-1)
		{
			String[] splittedBin=orginalPath.split(":");
			for (int index=0; index< splittedBin.length; index++)
			{
				if(splittedBin[index].indexOf("java-8-openjdk")==-1)
				{
					transferredPath+=splittedBin[index];
					transferredPath+=":";
				}
			}
			transferredPath = transferredPath.substring(0, transferredPath.length()-1);
		}
		else transferredPath=orginalPath;
		
		String internString="";
		if(jdkNumber==8)
			internString= Config.javaHome8;
		
		internString += ":";
		transferredPath = internString + transferredPath;
		
		return transferredPath;
	}	
}
