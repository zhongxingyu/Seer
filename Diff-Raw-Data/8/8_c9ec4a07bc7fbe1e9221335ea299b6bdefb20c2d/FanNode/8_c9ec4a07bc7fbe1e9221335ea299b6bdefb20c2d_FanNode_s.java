 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.project;
 
 import java.awt.Image;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 import javax.swing.Action;
 import net.colar.netbeans.fan.actions.RunFanShellAction;
 import org.netbeans.api.project.Project;
 import org.netbeans.spi.project.ActionProvider;
 import org.netbeans.spi.project.ui.support.CommonProjectActions;
 import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
 import org.openide.filesystems.FileObject;
 import org.openide.nodes.Node;
 import org.openide.util.lookup.Lookups;
 import org.openide.actions.*;
 import org.openide.nodes.FilterNode;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.util.actions.SystemAction;
 import org.openide.util.lookup.ProxyLookup;
 
 /**
  * Fan project Nodes (used within logical view)
  * @author tcolar
  */
 public class FanNode extends FilterNode
 {
 
     public static final String ATTR_NODE_FILEOBJECT = "NODE_FILEOBJECT";
     //TODO: are those vars working ?
     boolean isPod = false;
     boolean isRoot = false;
     //boolean isRunnable = false;
     private final FileObject file;
     private String icon;
 
     public FanNode(Project project, Node originalNode, org.openide.nodes.Children children, FileObject file)
     {
 	super(originalNode, children, new ProxyLookup(new Lookup[]
 		{
 		    Lookups.singleton(project),
 		    originalNode.getLookup()
 		}));
 	this.file = file;
 	// customize the node
 	if (file.isFolder())
 	{
 	    //Allow child folders of the scenes/ dir
 	    if (file.getFileObject("build.fan") != null)
 	    {
 		isPod = true;
 	    }
 	    if (isPod)
 	    {
 		setIcon("net/colar/netbeans/fan/fan.png");
 	    } else if (getName().equalsIgnoreCase("fan"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/folderFan.png");
 	    } else if (file.getName().equalsIgnoreCase("java"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/folderJava.png");
 	    } else if (file.getName().equalsIgnoreCase("test"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/folderTest.png");
	    } else
	    {
		setIcon("net/colar/netbeans/fan/project/resources/folder.png");
 	    }
 	} else
 	{
 	    if (file.getNameExt().equalsIgnoreCase("build.fan"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/fanBuild.png");
 	    } else if (file.getExt().equalsIgnoreCase("fan"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/fanFile.png");
 	    } else if (file.getExt().equalsIgnoreCase("fwt"))
 	    {
 		setIcon("net/colar/netbeans/fan/project/resources/fanFwt.png");
 	    }
 	    //TODO: check if runnable
 	    }
 	System.out.println("Node: " + file.getPath() + " " + (isRoot ? "project" : "") + (isPod ? "pod" : ""));
 
     }
 
     @Override
     public Action[] getActions(boolean popup)
     {
 	Vector<Action> actions = new Vector();
 
 	if (isRoot || isPod)
 	{
 	    // project level actions
 	    actions.add(CommonProjectActions.newFileAction());
 	    actions.add(null);
 	    actions.add(ProjectSensitiveActions.projectCommandAction(RunFanShellAction.COMMAND_RUN_FAN_SHELL, "Start Fan Shell", null));
 	    actions.add(null);
 	    //actions.add(CommonProjectActions.copyProjectAction());
 	    actions.add(CommonProjectActions.deleteProjectAction());
 	    actions.add(CommonProjectActions.closeProjectAction());
 	    actions.add(SystemAction.get(PasteAction.class));
 
 	    actions.add(null);
 	    actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, "Build", null));
 	    // TODO: rename ?
 	    // TODO: run (build.fan)
 	    // TODO: debug
 	} else
 	{
 	    // Folder actions
 	    if (getChildren().getNodesCount() != 0)
 	    {
 		actions.add(CommonProjectActions.newFileAction());
 		actions.add(SystemAction.get(FindAction.class));
 		actions.add(SystemAction.get(PasteAction.class));
 	    } else
 	    // item actions
 	    {
 		actions.add(SystemAction.get(OpenAction.class));
 		actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN_SINGLE, "Run Fan file", null));
 	    }
 
 	    actions.add(null);
 	    actions.add(SystemAction.get(CopyAction.class));
 	    actions.add(SystemAction.get(CutAction.class));
 	    actions.add(SystemAction.get(DeleteAction.class));
 	    actions.add(null);
 	    actions.add(SystemAction.get(RenameAction.class));
 	}
 
 	// standard actions
 	actions.add(null);
 	Action[] stdActions = super.getActions(popup);
 	List list = Arrays.asList(stdActions);
 	actions.addAll(list);
 
 	return actions.toArray(new Action[0]);
     }
 
     @Override
     public Image getIcon(int arg0)
     {
 	return ImageUtilities.loadImage(icon);
     }
 
     @Override
     public Image getOpenedIcon(int arg0)
     {
 	return getIcon(arg0);
     }
 
     protected void setIcon(String ic)
     {
 	icon = ic;
     }
 }
