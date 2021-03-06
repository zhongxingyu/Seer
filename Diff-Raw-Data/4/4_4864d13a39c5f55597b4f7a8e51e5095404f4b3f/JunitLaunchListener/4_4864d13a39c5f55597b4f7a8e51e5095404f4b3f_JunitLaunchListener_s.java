 /*
  * Copyright (c) 2012 Andrejs Jermakovics.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Andrejs Jermakovics - initial implementation
  */
 package jmockit.assist;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import jmockit.assist.prefs.Prefs;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.variables.IStringVariableManager;
 import org.eclipse.core.variables.IValueVariable;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchesListener2;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
 
 /**
  * Listens for JUnit launches and adds JMockit javaagent JVM argument
  */
 @SuppressWarnings("restriction")
 final class JunitLaunchListener implements ILaunchesListener2
 {
 	public static final String VM_ARGS = "org.eclipse.jdt.launching.VM_ARGUMENTS";
 	public static final String PROJ_ATTR = "org.eclipse.jdt.launching.PROJECT_ATTR";
 
 	public static final String JMOCKIT_VAR_NAME = "jmockit_javaagent";
 	public static final String JMOCKIT_VAR = "${" + JMOCKIT_VAR_NAME + "}";
 
 	private final List<ILaunch> modifiedLaunches = new ArrayList<ILaunch>();
 
 	@Override
 	public void launchesTerminated(final ILaunch[] launches)
 	{
 		for (ILaunch iLaunch : launches)
 		{
 			if (modifiedLaunches.contains(iLaunch))
 			{
 				modifiedLaunches.remove(iLaunch);
 				try
 				{
 					removeJavaAgentVmArg(iLaunch.getLaunchConfiguration());
 				}
 				catch (CoreException e)
 				{
 					Activator.log(e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void launchesRemoved(final ILaunch[] launches)
 	{
 	}
 
 	@Override
 	public void launchesChanged(final ILaunch[] launches)
 	{
 	}
 
 	@Override
 	public void launchesAdded(final ILaunch[] launches)
 	{
 		if (Activator.getPrefStore().contains(Prefs.PROP_ADD_JAVAAGENT)
 				&& !Activator.getPrefStore().getBoolean(Prefs.PROP_ADD_JAVAAGENT))
 		{
 			return;
 		}
 
 		for (ILaunch launch : launches)
 		{
 			ILaunchConfiguration conf = launch.getLaunchConfiguration();
 			try
 			{
 				String pluginId = conf.getType().getPluginIdentifier();
 
 				if (JUnitCorePlugin.getPluginId().equals(pluginId))
 				{
 					if (addJavaAgentVmArg(conf))
 					{
 						modifiedLaunches.add(launch);
 					}
 				}
 			}
 			catch (CoreException e)
 			{
 				Activator.log(e);
 			}
 		}
 	}
 
 	public void setRunConfVmArgs(final ILaunchConfiguration conf, final String vmargs) throws CoreException
 	{
 		ILaunchConfigurationWorkingCopy confWc = conf.getWorkingCopy();
 		confWc.setAttribute(JunitLaunchListener.VM_ARGS, vmargs);
 		confWc.doSave();
 	}
 
 	public void setOrCreateVariable(final String value) throws CoreException
 	{
 		IStringVariableManager varMan = VariablesPlugin.getDefault().getStringVariableManager();
 		IValueVariable var = varMan.getValueVariable(JunitLaunchListener.JMOCKIT_VAR_NAME);
 
 		if (var == null)
 		{
 			var = varMan.newValueVariable(JunitLaunchListener.JMOCKIT_VAR_NAME, value, false, value);
 			varMan.addVariables(new IValueVariable[]{var});
 		}
 		else
 		{
 			var.setValue(value);
 		}
 	}
 
 	public void removeJavaAgentVmArg(final ILaunchConfiguration conf) throws CoreException
 	{
 		String vmargs = conf.getAttribute(JunitLaunchListener.VM_ARGS, "");
 		if (vmargs.endsWith(JunitLaunchListener.JMOCKIT_VAR))
 		{
 			vmargs = vmargs.substring(0, vmargs.length() - JunitLaunchListener.JMOCKIT_VAR.length() - 1);
 			setRunConfVmArgs(conf, vmargs);
 		}
 	}
 
 	public boolean addJavaAgentVmArg(final ILaunchConfiguration conf) throws CoreException
 	{
 		boolean added = false;
 		String vmargs = conf.getAttribute(JunitLaunchListener.VM_ARGS, "");
 		String project = conf.getAttribute(JunitLaunchListener.PROJ_ATTR, "");
 
 		IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
 		IJavaProject jproj = javaModel.getJavaProject(project);
 
 		IType mockitType = jproj.findType("mockit.Mockit");
 
 		if (mockitType != null)
 		{
 			IPackageFragmentRoot root = (IPackageFragmentRoot) mockitType
 					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
 
 			if (root != null && root.isArchive()) // its a jar
 			{
 				String jarPath = root.getPath().toOSString();
 				if (root.getResource() != null)
 				{
 					jarPath = root.getResource().getRawLocation().toString();
 				}
 
 				if (new File(jarPath).exists())
 				{
					String javaagentArg = "-javaagent:" + jarPath;
 					setOrCreateVariable(javaagentArg);
 
 					if (!vmargs.contains(JunitLaunchListener.JMOCKIT_VAR) && !vmargs.contains("-javaagent"))
 					{
 						setRunConfVmArgs(conf, vmargs + " " + JunitLaunchListener.JMOCKIT_VAR);
 						added = true;
 					}
 				}
 				else
 				{
 					Activator.log(new FileNotFoundException(jarPath));
 				}
 			}
 		}
 
 		return added;
 	}
 }
