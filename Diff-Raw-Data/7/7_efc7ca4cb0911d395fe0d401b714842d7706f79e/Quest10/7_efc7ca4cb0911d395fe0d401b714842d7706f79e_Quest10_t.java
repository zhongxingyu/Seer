 package org.akquinet.audit.bsi.httpd.usersNrights;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.akquinet.audit.Automated;
 import org.akquinet.audit.ModuleHelper;
 import org.akquinet.audit.ShellAnsweredQuestion;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.bsi.httpd.PrologueData;
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Directive;
 
 @Automated
 public class Quest10 extends ModuleHelper implements YesNoQuestion
 {
 	private static final long serialVersionUID = 7570966044840051414L;
 	
 	private static final String _id = "Quest10";
 	private transient UserCommunicator _uc = UserCommunicator.getDefault();
 	private static final String PERMISSION_PATTERN = ".....-..-.";
 	private String _commandPath;
 	private String _command;
 	private transient ResourceBundle _labels;
 
 	public Quest10(PrologueData pd)
 	{
 		this(pd._conf);
 	}
 	
 	public Quest10(PrologueData pd, UserCommunicator uc)
 	{
 		this(pd._conf, uc);
 	}
 	
 	public Quest10(ConfigFile conf)
 	{
 		this(conf, "./", "QfileSafe.sh");
 	}
 	
 	public Quest10(ConfigFile conf, UserCommunicator uc)
 	{
 		this(conf, "./", "QfileSafe.sh", uc);
 	}
 	
 	public Quest10(ConfigFile conf, String commandPath, String command)
 	{
 		this(conf, commandPath, command, UserCommunicator.getDefault());
 	}
 	
 	public Quest10(ConfigFile conf, String commandPath, String command, UserCommunicator uc)
 	{
 		super(conf);
 		_uc = uc;
 		_commandPath = commandPath;
 		_command = command;
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 	}
 
 	@Override
 	public boolean answer()
 	{
 		_uc.printHeading3( _labels.getString("name") );
 		_uc.printParagraph( _labels.getString("Q0") );
 		
 		
 		_uc.println( _labels.getString("L1") );
 		
 		Set<String> probs = lookForProblems(PERMISSION_PATTERN);
 		boolean ret = probs.isEmpty();;
 
 		if(!ret)
 		{
 			_uc.println( _labels.getString("L2") );
			
			StringBuilder problemList = new StringBuilder();
 			for (String str : probs)
 			{
				problemList = problemList.append(str).append('\n');
 			}
			_uc.printExample(problemList.toString());
 		}
 		
 		_uc.printAnswer(ret, ret ?  _labels.getString("S1_good") 
 							:  _labels.getString("S1_bad") );
 		
 		return ret;
 	}
 
 	private List<File> getModuleFiles()
 	{
 		List<File> ret = new LinkedList<File>();
 		List<Directive> dirList = getLoadModuleList();
 		for (Directive dir : dirList)
 		{
 			try
 			{
 				ret.add(new File(dir.getValue().trim().split("[ \t]+")[1]));
 			}
 			catch(IndexOutOfBoundsException e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 		
 		dirList = getLoadFileList();
 		for (Directive dir : dirList)
 		{
 			ret.add(new File(dir.getValue().trim()));
 		}
 		return ret;
 	}
 	
 	private Set<String> lookForProblems(String permissionPattern)
 	{
 		List<File> moduleFiles = getModuleFiles();
 		Set<String> probs = new HashSet<String>();
 		for (File file : moduleFiles)
 		{
 			ShellAnsweredQuestion quest = new ShellAnsweredQuestion(_commandPath + _command, file.getAbsolutePath(), permissionPattern);
 			if(!quest.answer())
 			{
 				probs.add(file.getAbsolutePath());
 			}
 			//also the enclosing directory has to be safe (for the case that file is a symbolic link, which we assume)
 			quest = new ShellAnsweredQuestion(_commandPath + _command, file.getParent(), permissionPattern);
 			if(!quest.answer())
 			{
 				probs.add(file.getParent());
 			}
 		}
 		
 		return probs;
 	}
 
 	@Override
 	public boolean isCritical()
 	{
 		return false;
 	}
 
 	@Override
 	public String getID()
 	{
 		return _id;
 	}
 
 	@Override
 	public int getBlockNumber()
 	{
 		return 3;
 	}
 
 	@Override
 	public int getNumber()
 	{
 		return 10;
 	}
 
 	@Override
 	public String[] getRequirements()
 	{
 		return new String[0];
 	}
 
 	@Override
 	public void initialize() throws Exception
 	{
 		reparse();
 	}
 	
 	private synchronized void readObject( java.io.ObjectInputStream s ) throws IOException, ClassNotFoundException
 	{
 		s.defaultReadObject();
 		_uc = UserCommunicator.getDefault();
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 	}
 	
 	@Override
 	public String getName()
 	{
 		return _labels.getString("name");
 	}
 
 	@Override
 	public void setUserCommunicator(UserCommunicator uc)
 	{
 		_uc = uc;
 	}
 }
