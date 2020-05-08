 package org.opentox.aa.cli;
 
 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.net.HttpURLConnection;
 import java.util.Hashtable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.opentox.aa.IOpenToxUser;
 import org.opentox.aa.OpenToxUser;
 import org.opentox.aa.opensso.AAServicesConfig;
 import org.opentox.aa.opensso.AAServicesConfig.CONFIG;
 import org.opentox.aa.opensso.OpenSSOPolicy;
 import org.opentox.aa.opensso.OpenSSOToken;
 import org.opentox.aa.policy.IPolicyHandler;
 import org.opentox.aa.policy.PolicyArchiveHandler;
 import org.opentox.aa.policy.PolicyHandler;
 
 public class aacli {
 	protected String authService;
 	protected String policyService;
 	protected OpenSSOToken ssotoken;
 	protected String policyId;
 	protected IOpenToxUser user;
 	protected String uri;
 	protected policy_command command = policy_command.authorize;
 	protected String backupDir = System.getProperty("java.io.tmpdir");
 	private final static Logger LOGGER = Logger.getLogger(aacli.class .getName());
 	protected int max = -1;
 	
 	enum policy_command {
 		authorize,
 		list,
 		delete,
 		create,
 		archive
 	}
 	public aacli() throws Exception {
 		super();
 		user = new OpenToxUser();
 		authService = AAServicesConfig.getSingleton().getOpenSSOService();
 		policyService = AAServicesConfig.getSingleton().getPolicyService();
 		user.setUserName(AAServicesConfig.getSingleton().getTestUser());
 		user.setPassword(AAServicesConfig.getSingleton().getTestUserPass());
 		LOGGER.setLevel(Level.OFF);
 	}
 	
 	public void login() throws Exception {
 		Hashtable<String, String> results = new Hashtable<String, String>();
 		System.out.println(String.format("Using %s: %s",_option.authn.getDescription(),authService));
 		System.out.println(String.format("Using %s: %s",_option.authz.getDescription(),policyService));
 		
 		if (ssotoken==null) ssotoken = new OpenSSOToken(authService);
 		if (ssotoken.getToken()==null) {
 			ssotoken.login(user);
 			if (ssotoken.getToken()==null) {
 				throw new InvalidCredentials(authService);
 			} else
 			System.out.println(String.format("Logged as %s token %s",user.getUsername(),ssotoken.getToken()));
 		} else {
 			System.out.println(String.format("Using provided token %s",ssotoken.getToken()));
 			if (!ssotoken.isTokenValid()) {
 				throw new InvalidCredentials("Invalid token submited to ");
 			}
 		}
 		ssotoken.getAttributes(new String[] {"uid"},results);
 		System.out.println(results);
 		
 	}
 	public void logout() throws Exception {
 
 		if ((ssotoken!=null) && (ssotoken.getToken()!=null)) {
 			System.out.println("Invalidating the token ...");
 			ssotoken.logout();
 			System.out.println("Logout completed. The token is no longer valid.");
 		}
 		
 	}
 	protected void log(policy_command command, String message) {
 		System.out.println(String.format("%s> %s",command, message));
 	}
 	
 	public void listPolicies(OpenSSOPolicy policy,IPolicyHandler handler) throws Exception {
 		if (uri!=null) {
 			log(command,String.format("URI: %s",uri));
 			OpenToxUser owner = new OpenToxUser();
 			int code = policy.getURIOwner(ssotoken, uri, owner, handler);
 			
 			log(command,String.format("HTTP result code: %d",code));
 			if (policyId!=null) {
 				log(command,String.format("Retrieve XML of policyId: %s",policyId));
 				try {
 					long now = System.currentTimeMillis();
 					code = policy.listPolicy(ssotoken, policyId, handler);
 					now = System.currentTimeMillis() - now;
 					log(command,String.format("HTTP result code: %d [elapsed %s ms]",code,now));
 					if (code ==401) log(command,"Error: Only the policy creator can retrieve its content.");
 				} catch (Exception x) {
 					log(command,x.getMessage());
 				}
 			}
 		} else {
 			if (policyId!=null) {
 				log(command,String.format("Searching for PolicyID: %s",policyId));
 				try {
 					int code = policy.listPolicy(ssotoken, policyId, handler);
 					log(command,String.format("HTTP result code: %d",code));
 					if (code ==401) log(command,"Error: Only the policy creator can retrieve its content.");
 				} catch (Exception x) {
 					log(command,x.getMessage());
 				}
 			} else {
 				log(command,"Retrieving all policies for the current user");
 				handler.handleOwner(user.getUsername());
 				policy.listPolicies(ssotoken,handler);
 			}			
 		}
 		log(command,String.format("Listed %d policies.",handler.getProcessed()));
 
 	}
 	public int run() throws Exception {
 		final OpenSSOPolicy policy = new OpenSSOPolicy(policyService);
 		
 		switch (command) {
 		case authorize: {
 			if ((uri==null)||"".equals(uri.trim()))
 					throw new MissingParameterException(String.format("%s>%s",command,"Missing URI. Have you specified an argument to the -r option?"));
 			log(command,String.format("URI: %s",uri));
 			String[] mm = new String[] {"GET","POST","PUT","DELETE"};
 			for (String m : mm)
 				try {
 				log(command,String.format("%s: %s %s",uri,m,ssotoken.authorize(uri, m)?"Allow":"Deny"));
 				} catch (Exception x) {
 					log(command,String.format("%s: %s %s",uri,m,x.getMessage()));
 				}
 			break;	
 		}
 		case list: {
 			
 			IPolicyHandler handler = new PolicyHandler() {
 				@Override
 				public boolean handleOwner(String owner) throws Exception {
 					super.handleOwner(owner);
 					if (owner !=null)
 					log(command,String.format("Owner: %s",owner));
 					return true;
 					
 				}				
 				@Override
 				public boolean handlePolicy(String policyID) throws Exception {
 					super.handlePolicy(policyID);
 					log(command,String.format("PolicyID: %s",policyID));
 					return true;
 				}
 				@Override
 				public boolean handlePolicy(String policyID, String content)
 						throws Exception {
 					super.handlePolicy(policyID,content);
 					log(command,String.format("PolicyID: %s \n %s",policyID,content));
 					return true;
 					
 				}
 			};			
 			listPolicies(policy, handler);
 			break;
 		}
 		case delete: {
 			IPolicyHandler deleteHandler = new PolicyHandler() {
 				@Override
 				public boolean handleOwner(String owner) throws Exception {
 					boolean ok = super.handleOwner(owner);
 					if (owner !=null)
 					log(command,String.format("Owner: %s",owner));
 					return ok;
 				}
 				@Override
 				public boolean handlePolicy(String policyID) throws Exception {
 					boolean ok = super.handlePolicy(policyID);
 					log(command,String.format("Deleting PolicyID: %s",policyID));
 					 try {
 						 long now = System.currentTimeMillis();
 						 int code = policy.deletePolicy(ssotoken, policyID);
 						 now = System.currentTimeMillis()-now;
 							if (code == 200)
 								log(command,String.format("Deleted PolicyID: %s  [%s ms]",policyID,now));
 							else
 								log(command,String.format("HTTP result code: %d [%s ms]",code,now));							 
 					 } catch (Exception x) {
 						log(command,String.format("ERROR: %s",x.getMessage()));
 					 }
 					 return ok;
 				}
 				@Override
 				public boolean handlePolicy(String policyID, String content)
 						throws Exception {
 					return super.handlePolicy(policyID,content);
 				}
 			};
 			
 			int code = 0;
 			if (policyId!=null) {
 				log(command,"Deleting single policy");
 				if (confirm(String.format("Do you really want to delete the policy %s ?",policyId))) {
 					deleteHandler.handlePolicy(policyId);
 				} else  throw new UserCancelledException();
 			} else if (uri!=null) {
 				log(command,String.format("Deleting all policies for the URI %s",uri));
 				if (confirm(String.format("Do you really want to delete all policies for %s ?",uri))) {
 					OpenToxUser owner = new OpenToxUser();
 					policy.getURIOwner(ssotoken, uri, owner, deleteHandler);
 				} else throw new UserCancelledException();
 			} else {	
 				log(command,String.format("Deleting all policies defined by user %s",user.getUsername()));
 				if (confirm(String.format("Do you really want to delete all policies defined by %s?",user.getUsername()))) {
 					policy.listPolicies(ssotoken, deleteHandler);
 				} else throw new UserCancelledException();
 				
 		
 			}
 			log(command,String.format("Deleted %d policies.",deleteHandler.getProcessed()));
 			return 0;
 		} 
 		case archive: {
 
 			File dir = new File(backupDir);
 			if (!dir.exists()) try {
 				dir.mkdir();
 			} catch (Exception x) {
 				throw new Exception(String.format("Can't create backup directory %s", dir.getAbsoluteFile()),x);
 			}
 			log(command,String.format("Using backup directory %s",dir.getAbsoluteFile()));
 			PolicyArchiveHandler handler = new PolicyArchiveHandler(policy,dir);
 			/*{
 				@Override
 				public boolean handlePolicy(String policyID) throws Exception {
 					boolean ok = super.handlePolicy(policyID);
 					long now = System.currentTimeMillis();
 					try {
 						policy.listPolicy(ssotoken, policyID, this);
 						now = System.currentTimeMillis() - now;
 						log(command,String.format("Policy '%s' retrieved and written in [%s ms]",policyID,now));
 					} catch (Exception x) {
 						log(command,String.format("ERROR retrieving policy %s",policyID));
 					}
 					return ok;
 				}
 			};
 			*/
 			listPolicies(policy, handler);
 			handler.close();
 			handler.backupXML(ssotoken, policy);
 			break;
 		}
 		case create: {
 			
 			File dir = new File(backupDir);
 			if (!dir.exists()) throw new MissingParameterException(String.format("Directory %s does not exist!", dir.getAbsoluteFile()));
 			
 			File serversFile = new File(backupDir,"servers.txt");
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new FileReader(serversFile));
 				log(command,String.format("Reading %s",serversFile.getAbsoluteFile() ));
 				String line = null;
 				while ((line = reader.readLine())!=null)
 					log(command,String.format("The policies were retrieved from: %s", line));
 			} catch (Exception x) {
 				if (!confirm(String.format("ERROR reading %s! Is this a backup directory?",serversFile.getAbsoluteFile())))
 					throw new UserCancelledException();
 						
 			} finally {
 				if (reader !=null) reader.close();
 			}
 			
 			FilenameFilter selectxml = new FileListFilter("policy_", "xml");
 			int record = 0;
 			File[] files = dir.listFiles(selectxml);
 			log(command,String.format("There are %d policy_*.xml files in %s",files.length,dir.getAbsoluteFile()));
 			boolean confirmed = false;
 			for (File file:files) {
 				FileInputStream fis= null;
 				try {
 					byte[] b = new byte[(int) file.length()];  
 					fis = new FileInputStream(file);
 					int l = fis.read(b);
 					if (l>0) {
 						//System.out.println(new String(b));
 						long now = System.currentTimeMillis();
 						log(command,String.format("Preparing to send '%s' to the policy service at %s ...",file.getAbsoluteFile(),policy.getPolicyService()));
 						if (confirmed || confirm(String.format("Do you really want to create new policies at %s ?",ssotoken.getAuthService()))) {
 							confirmed = true;
 							int code = policy.sendPolicy(ssotoken,new String(b));
 							if (HttpURLConnection.HTTP_OK==code)
 								log(command,String.format("Policy '%s' sent in [%s ms]",file.getAbsoluteFile(),now));
 							else
 								log(command,String.format("ERROR %d %s",code));
 						}
						now = System.currentTimeMillis() - now;
 						
 						record++;
 					}
 				} catch (InvalidCredentials x) {
 					throw x;					
 				} catch (UserCancelledException x) {
 					throw x;
 				} catch (Exception x) {
 					log(command,String.format("Policy creation '%s' failed. [%s]",file.getAbsoluteFile(),x));
 				} finally {
 					if (fis !=null) fis.close();
 				}
 			}
 			
 			log(command,String.format("Created %d policies",record));
 			break;
 
 		}
 		default : throw new MissingParameterException(String.format("%s not supported",command));
 		}
 		return 0;
 	}
 	protected boolean confirm(String message) throws UserCancelledException {
 		Console c = System.console();
 		if (c!=null) {
 			String confirm = c.readLine(String.format("Hope you know what are you doing.\n%s\nEnter Y, N or Q to quit:",message));
 			if ("Y".equals(confirm.trim().toUpperCase())) return true;
 			if ("Q".equals(confirm.trim().toUpperCase())) throw new UserCancelledException();
 		}
 		return true;
 	}
 	
 	public void setOption(_option option, String argument) throws Exception {
 		if (argument!=null) argument = argument.trim();
 		switch (option) {
 		case authn: {
 			if ((argument==null) || "".equals(argument.trim())) return;
 			if ((argument==null) || !argument.startsWith("http")) 
 				throw new IllegalArgumentException("Not a valid HTTP URI "+argument);
 			this.authService = argument;
 		}
 		case authz: {
 			if ((argument==null) || "".equals(argument.trim())) return;
 			if ((argument==null) || !argument.startsWith("http")) 
 				throw new IllegalArgumentException("Not a valid HTTP URI "+argument);
 			this.policyService = argument;
 		}
 		case user: {
 			if ((argument==null) || "".equals(argument.trim())) throw new IllegalArgumentException("Empty user name!");
 			user.setUserName(argument);
 			break;
 		}
 		case password: {
 			user.setPassword(argument);
 			break;
 		}
 		case subjectid: {
 			if ((argument==null) || "".equals(argument.trim())) throw new IllegalArgumentException("Empty token!");
 			this.ssotoken = new OpenSSOToken(authService);
 			this.ssotoken.setToken(argument);
 			break;
 		}
 		case policyid: {
 			//if ((argument==null) || "".equals(argument.trim())) throw new IllegalArgumentException("Empty policy identifier!");
 			this.policyId = argument;
 			break;
 		}		
 		case uri: {
 			if ((argument==null) || !argument.startsWith("http")) 
 				throw new IllegalArgumentException("Not a valid HTTP URI "+argument);
 			this.uri = argument;
 			break;
 		}			
 		case command: {
 			this.command = policy_command.valueOf(argument);
 			break;
 		}			
 		case backupdir: {
 			this.backupDir = argument==null?option.getDefaultValue():argument;
 			break;
 		}
 		case max: {
 			try { 
 				max = Integer.parseInt(argument);
 			} catch (Exception x) {
 				max = -1;
 			}
 		}
 		default: 
 		}
 	}
 	public static void main(String[] args) {
 
     	Options options = createOptions();
     	
     	aacli cli=null;
     	CommandLineParser parser = new PosixParser();
 		try {
 			cli = new aacli();
 		    CommandLine line = parser.parse( options, args,false );
 		    if (line.hasOption(_option.help.name())) {
 		    	printHelp(options, null);
 		    	return;
 		    }
 		    	
 	    	for (_option o: _option.values()) 
 	    		if (line.hasOption(o.getShortName())) try {
 	    			cli.setOption(o,line.getOptionValue(o.getShortName()));
 	    		} catch (Exception x) {
 	    			printHelp(options,x.getMessage());
 	    			return;
 	    		}
 	    	
 	    	cli.login();	
 	    	cli.run();	
 	    		
 /*
 		    
 		    File file = new File(input);
 		    if (file.exists()) {
 		    	OpenToxAAcli cropper = new OpenToxAAcli();
 		    	if (file.isDirectory()) {
 		    		cropper.crop(file.listFiles());
 		    	} else cropper.crop(file);
 		    } else {
 		    	printHelp(options,String.format("File not found %s", input));
 		    }
 		
 		    if (line.hasOption("h") || (input==null)) {
 		    	printHelp(options,null);
 		    }		
 		       */    
 		} catch (MissingParameterException x) {
 			System.out.println(x.getMessage());
 		} catch (InvalidCredentials x) {
 			System.out.println(x.getMessage());
 		} catch (UserCancelledException x) {
 			System.out.println(x.getMessage());
 		} catch (Exception x ) {
 			x.printStackTrace();
 			printHelp(options,x.getMessage());
 		} finally {
 			try { 
 				if(cli!=null) cli.logout(); 
 			} catch (Exception xx) {
 				printHelp(options,xx.getMessage());
 			}
 		}
 	}
 	
 	enum _option {
 
 		authn {
 			@Override
 			public String getArgName() {
 				return "URI";
 			}
 			@Override
 			public String getDescription() {
 				return "URI of OpenSSO/OpenAM service";
 			}
 			@Override
 			public String getShortName() {
 				return "n";
 			}
 			@Override
 			public String getDefaultValue() {
 				try {
 				return AAServicesConfig.getSingleton().getOpenSSOService();
 				} catch (Exception x) {return null;}
 			}
 		},
 		authz {
 			@Override
 			public String getArgName() {
 				return "URI";
 			}
 			@Override
 			public String getDescription() {
 				return "URI of OpenTox policy service";
 			}
 			@Override
 			public String getShortName() {
 				return "z";
 			}
 			@Override
 			public String getDefaultValue() {
 				try {
 				return AAServicesConfig.getSingleton().getPolicyService();
 				} catch (Exception x) {return null;}
 			}
 		},		
 		user {
 			@Override
 			public String getArgName() {
 				return "username";
 			}
 			@Override
 			public String getDescription() {
 				return "OpenTox user name";
 			}
 			@Override
 			public String getShortName() {
 				return "u";
 			}
 		},
 		password {
 			@Override
 			public String getArgName() {
 				return "password";
 			}
 			@Override
 			public String getDescription() {
 				return "OpenTox user password";
 			}
 			@Override
 			public String getShortName() {
 				return "p";
 			}
 		},		
 		subjectid {
 			@Override
 			public String getArgName() {
 				return "token";
 			}
 			@Override
 			public String getDescription() {
 				return "OpenSSO/OpenAM token. If the token is present, user and password are ignored.";
 			}
 			@Override
 			public String getShortName() {
 				return "s";
 			}
 		},	
 		policyid {
 			@Override
 			public String getArgName() {
 				return "policyid";
 			}
 			@Override
 			public String getDescription() {
 				return "An OpenSSO/OpenAM policy identifier";
 			}
 			@Override
 			public String getShortName() {
 				return "i";
 			}
 			
 		},		
 		uri {
 			@Override
 			public String getArgName() {
 				return "URI";
 			}
 			@Override
 			public String getDescription() {
 				return "URI of an OpenTox resource";
 			}
 			@Override
 			public String getShortName() {
 				return "r";
 			}
 	
 		},			
 		command {
 			@Override
 			public String getArgName() {
 				return "the command";
 			}
 			@Override
 			public String getDescription() {
 				StringBuilder b = new StringBuilder();
 				b.append("The command to be performed. One of ");
 				String d = "";
 				for (policy_command pc : policy_command.values()) {
 					b.append(d);
 					b.append(pc);
 					d = "|";
 				}
 				return b.toString();
 			}
 			@Override
 			public String getShortName() {
 				return "c";
 			}
 			@Override
 			public String getDefaultValue() {
 				return policy_command.authorize.name();
 			}
 		},			
 		backupdir {
 			@Override
 			public String getArgName() {
 				return "Directory";
 			}
 			@Override
 			public String getDescription() {
 				return "Directory to archive policy XML files";
 			}
 			@Override
 			public String getShortName() {
 				return "b";
 			}
 			@Override
 			public String getDefaultValue() {
 				return System.getProperty("java.io.tmpdir");
 			}			
 		},
 		max {
 			@Override
 			public String getArgName() {
 				return "number of records";
 			}
 			@Override
 			public String getDescription() {
 				return "Max number of records";
 			}
 			@Override
 			public String getShortName() {
 				return "m";
 			}
 			@Override
 			public String getDefaultValue() {
 				return "all";
 			}			
 		},		
 		help {
 			@Override
 			public String getArgName() {
 				return null;
 			}
 			@Override
 			public String getDescription() {
 				return "OpenTox Authentication and Authorization client";
 			}
 			@Override
 			public String getShortName() {
 				return "h";
 			}
 			@Override
 			public String getDefaultValue() {
 				return null;
 			}
 			public Option createOption() {
 		    	Option option   = OptionBuilder.withLongOpt(name())
 		        .withDescription(getDescription())
 		        .create(getShortName());
 
 		    	return option;
 			}
 		}				
 		;
 		public abstract String getArgName();
 		public abstract String getDescription();
 		public abstract String getShortName();
 		public String getDefaultValue() { return null; }
 			
 		public Option createOption() {
 			String defaultValue = getDefaultValue();
 	    	Option option   = OptionBuilder.withLongOpt(name())
 	        .hasArg()
 	        .withArgName(getArgName())
 	        .withDescription(String.format("%s %s %s",getDescription(),defaultValue==null?"":"Default value: ",defaultValue==null?"":defaultValue))
 	        .create(getShortName());
 
 	    	return option;
 		}
 	}
 	protected static Options createOptions() {
     	Options options = new Options();
     	for (_option o: _option.values()) {
     		options.addOption(o.createOption());
     	}
 
     	
     	return options;
 	}	
 	
 	protected static String exampleRetrievePoliciesPerURI() {
 		return
 		"Retrieve all policies per URI:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-n http://opensso.in-silico.ch/opensso/identity\n"+
 		"\t-z http://opensso.in-silico.ch/Pol/opensso-pol\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-r http://blabla.uni-plovdiv.bg:8080/ambit2/dataset/999\n"+
 		"\t-c list";
 
 	}	
 	
 	
 	protected static String exampleArchivePolicies() {
 		return
 		"Retriewe and store locally XML files for the user:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-n http://opensso.in-silico.ch/opensso/identity\n"+
 		"\t-z http://opensso.in-silico.ch/Pol/opensso-pol\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-b /tmp\n"+
 		"\t-c archive";
 		
 
 	}	
 	
 	protected static String exampleCreatePolicies() {
 		return
 		"Create new policies from a backup directory of XML files:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-n your-sso-server\n"+
 		"\t-z your-policy-server\n"+
 		"\t-u your-user\n"+
 		"\t-p your-pass\n"+
 		"\t-b /tmp\n"+
 		"\t-c create";
 		
 
 	}	
 	
 	protected static String exampleRetrievePolicyContent() {
 		return
 		"Retrieve policy content by policy id\n"+
 		"\tjava -jar aacli\n"+
 		"\t-n http://opensso.in-silico.ch/opensso/identity\n"+
 		"\t-z http://opensso.in-silico.ch/Pol/opensso-pol\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-i member_rohttpsambit.uni-plovdiv.bg8443ambit2dataset1\n"+
 		"\t-c list";
 
 	}
 	
 	protected static String exampleDeletePolicyURI() {
 		return
 		"Delete all policies per URI:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-r http://blabla.uni-plovdiv.bg:8080/ambit2/dataset/999\n"+
 		"\t-c delete";
 
 	}
 	
 	protected static String exampleDeletePolicyUser() {
 		return
 		"Delete all policies per user:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-c delete";
 
 	}
 	
 	protected static String exampleDeletePolicy() {
 		return
 		"Delete policy by policy id:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-i guest_c35ceda9-e548-47d6-a377-ac2cae708100\n"+
 		"\t-c delete";
 
 	}	
 	
 	protected static String exampleAuth() {
 		return
 		"Verify authorization:\n"+
 		"\tjava -jar aacli\n"+
 		"\t-n http://opensso.in-silico.ch/opensso/identity\n"+
 		"\t-z http://opensso.in-silico.ch/Pol/opensso-pol\n"+			
 		"\t-u guest\n"+
 		"\t-p guest\n"+
 		"\t-r https://ambit.uni-plovdiv.bg:8443/ambit2/dataset/1\n"+
 		"\t-c authorize";
 	}
 	protected static void printHelp(Options options,String message) {
 		if (message!=null) System.out.println(message);
 		System.out.println("An OpenTox Authentication and Authorization client.");
 	    HelpFormatter formatter = new HelpFormatter();
 	    formatter.printHelp( aacli.class.getName(), options );
 	    System.out.println("Examples:");
 	    System.out.println(exampleAuth());
 	    System.out.println(exampleRetrievePoliciesPerURI());
 	    System.out.println(exampleRetrievePolicyContent());
 	    System.out.println(exampleArchivePolicies());
 	    System.out.println(exampleCreatePolicies());
 	    System.out.println(exampleDeletePolicy());
 	    System.out.println(exampleDeletePolicyURI());
 	    System.out.println(exampleDeletePolicyUser());
 
 	    Runtime.getRuntime().runFinalization();						 
 		Runtime.getRuntime().exit(0);	
 	}
 }
 
 class FileListFilter implements FilenameFilter {
 	  private String name; 
 
 	  private String extension; 
 
 	  public FileListFilter(String name, String extension) {
 	    this.name = name;
 	    this.extension = extension;
 	  }
 
 	  public boolean accept(File directory, String filename) {
 	    boolean fileOK = true;
 
 	    if (name != null) {
 	      fileOK &= filename.startsWith(name);
 	    }
 
 	    if (extension != null) {
 	      fileOK &= filename.endsWith('.' + extension);
 	    }
 	    return fileOK;
 	  }
 	}
