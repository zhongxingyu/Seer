package synthesis.prepare;

import java.io.IOException;

public class prepareRepository {
	
	/**
     * this method downloads the Defects4J repository form GitHub site, initializes Defects4J, 
     * checkouts buggy source code versions
     */		
	public static void downloadRepository() {
		
		String commandSequence="";
		String homePath = System.getProperty("user.home");
		commandSequence = "cd "+homePath+" && ";
		
		/**
	     * clones and initializes the Defects4J repository
	     */
		String defects4jString="";
		defects4jString="git clone https://github.com/rjust/defects4j"+" && "+"cd "+"defects4j"+" && "
		  +"cpanm --installdeps ."+" && "+"./init.sh"+" && ";
		commandSequence+=defects4jString;
		
		 /**
	     * adds jdk8 path and Defects4J's executables to the PATH
	     */
		String pathString="";
		pathString="export PATH="+Config.processPath(System.getenv("PATH"),8)+" && "+"export PATH="+Config.defects4jRoot
				+"/framework/bin:$PATH"+" && "+"mkdir "+Config.projectsRoot+" && "+"mkdir "+Config.projectsRootfix+" && ";
		commandSequence+=pathString;
	    
		String tempString ="";
		String tempStringfix ="";
		/**
	     * checkouts the Char, Lang, Math, Closure, Mockito, and Time repository respectively
	     */
		for (int chartID=1; chartID <=26; chartID++)
   	    { 	    	
   	    	String chartString="";
   	    	String chartStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(chartID==1)
   	    	{
   	    		chartString="mkdir "+Config.projectsRoot+"/chart"+" && ";
   	    		chartStringfix ="mkdir "+Config.projectsRootfix+"/chart"+" && ";
   	    		commandSequence+=chartString;
   	    		commandSequence+=chartStringfix;
   	    	}	
   	    	tempString="defects4j checkout -p Chart -v "+Integer.toString(chartID)+"b"+" -w "+Config.projectsRoot
   	    			+"/chart/chart_"+Integer.toString(chartID);
   	    	tempStringfix="defects4j checkout -p Chart -v "+Integer.toString(chartID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/chart/chart_"+Integer.toString(chartID);
   	    	commandSequence +=tempString;
   	    	commandSequence +=" && ";
   	    	commandSequence +=tempStringfix;
   	    	commandSequence += " && ";
   	    }
   	    
		for(int LangID=1; LangID <=65; LangID++)
	    { 
			if(LangID==2)
   	    	{
   	    		continue;
   	    	}
			
   	    	String langString="";
   	    	String langStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(LangID==1)
   	    	{
   	    		langString="mkdir "+Config.projectsRoot+"/lang"+" && ";
   	    		langStringfix ="mkdir "+Config.projectsRootfix+"/lang"+" && ";
   	    		commandSequence+=langString;
   	    		commandSequence+=langStringfix;
   	    	}	
   	    	
   	    	tempString="defects4j checkout -p Lang -v "+Integer.toString(LangID)+"b"+" -w "+Config.projectsRoot
   	    			+"/lang/lang_"+Integer.toString(LangID);
   	    	tempStringfix="defects4j checkout -p Lang -v "+Integer.toString(LangID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/lang/lang_"+Integer.toString(LangID);
   	    	commandSequence +=tempString;
  	    	commandSequence += " && ";
  	    	commandSequence +=tempStringfix;
   	    	commandSequence += " && ";
	    }

		for (int MathID=1; MathID <=106; MathID++)
	    {    	
   	    	String mathString="";
   	    	String mathStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(MathID==1)
   	    	{
   	    		mathString="mkdir "+Config.projectsRoot+"/math"+" && ";
   	    		mathStringfix ="mkdir "+Config.projectsRootfix+"/math"+" && ";
   	    		commandSequence+=mathString;
   	    		commandSequence+=mathStringfix;
   	    	}	
   	    	tempString="defects4j checkout -p Math -v "+Integer.toString(MathID)+"b"+" -w "+Config.projectsRoot
   	    			+"/math/math_"+Integer.toString(MathID);
   	    	tempStringfix="defects4j checkout -p Math -v "+Integer.toString(MathID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/math/math_"+Integer.toString(MathID);
   	    	commandSequence +=tempString;
   	    	commandSequence += " && ";
   	    	commandSequence +=tempStringfix;
   	    	commandSequence += " && ";
	    }
		
		for (int TimeID=1; TimeID <=27; TimeID++)
	    {
			if(TimeID==21)
   	    	{
   	    		continue;
   	    	}	
			
			String timeString="";
   	    	String timeStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(TimeID==1)
   	    	{
   	    		timeString="mkdir "+Config.projectsRoot+"/time"+" && ";
   	    		timeStringfix ="mkdir "+Config.projectsRootfix+"/time"+" && ";
   	    		commandSequence+=timeString;
   	    		commandSequence+=timeStringfix;
   	    	}	
   	    	tempString="defects4j checkout -p Time -v "+Integer.toString(TimeID)+"b"+" -w "+Config.projectsRoot
   	    			+"/time/time_"+Integer.toString(TimeID);
   	    	tempStringfix="defects4j checkout -p Time -v "+Integer.toString(TimeID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/time/time_"+Integer.toString(TimeID);
   	    	commandSequence +=tempString;
	    	commandSequence += " && ";
	    	commandSequence +=tempStringfix;
   	    	commandSequence += " && ";
	    }
		
		for (int ClosureID=1; ClosureID <=133; ClosureID++)
	    {
			if(ClosureID==63||ClosureID==93)
   	    	{
   	    		continue;
   	    	}
			
			String closureString="";
   	    	String closureStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(ClosureID==1)
   	    	{
   	    		closureString="mkdir "+Config.projectsRoot+"/closure"+" && ";
   	    		closureStringfix ="mkdir "+Config.projectsRootfix+"/closure"+" && ";
   	    		commandSequence+=closureString;
   	    		commandSequence+=closureStringfix;
   	    	}	
   	    	tempString="defects4j checkout -p Closure -v "+Integer.toString(ClosureID)+"b"+" -w "+Config.projectsRoot
   	    			+"/closure/closure_"+Integer.toString(ClosureID);
   	    	tempStringfix="defects4j checkout -p Closure -v "+Integer.toString(ClosureID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/closure/closure_"+Integer.toString(ClosureID);
   	    	commandSequence +=tempString;
	    	commandSequence += " && ";
	    	commandSequence +=tempStringfix;
   	    	commandSequence += " && ";
	    }
		
		for (int MockitoID=1; MockitoID <=38; MockitoID++)
	    {
   	    	String mockitoString="";
   	    	String mockitoStringfix="";
   	    	tempString ="";
   	    	tempStringfix ="";
   	    	if(MockitoID==1)
   	    	{
   	    		mockitoString="mkdir "+Config.projectsRoot+"/mockito"+" && ";
   	    		mockitoStringfix ="mkdir "+Config.projectsRootfix+"/mockito"+" && ";
   	    		commandSequence+=mockitoString;
   	    		commandSequence+=mockitoStringfix;
   	    	}	
   	    	tempString="defects4j checkout -p Mockito -v "+Integer.toString(MockitoID)+"b"+" -w "+Config.projectsRoot
   	    			+"/mockito/mockito_"+Integer.toString(MockitoID);
   	    	tempStringfix="defects4j checkout -p Mockito -v "+Integer.toString(MockitoID)+"f"+" -w "+Config.projectsRootfix
   	    			+"/mockito/mockito_"+Integer.toString(MockitoID);
   	    	commandSequence +=tempString;
	    	commandSequence += " && ";
	    	commandSequence +=tempStringfix;
	    	if (MockitoID!=38)
   	    	   commandSequence += " && ";
	    }
   	    
   	    /**
	     * starts a process to run the shell command
	     */
		String[] cmd = { "/bin/bash", "-c", commandSequence};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = null;
        try {
            pb.inheritIO();
            p = pb.start();
            p.waitFor();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
	}
}
