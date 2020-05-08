 package net.sourceforge.eclipseccase.ui;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import net.sourceforge.eclipseccase.StateCache;
 import net.sourceforge.eclipseccase.StateCacheFactory;
 import net.sourceforge.eclipseccase.StateChangeListener;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jface.viewers.ILightweightLabelDecorator;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.team.ui.ISharedImages;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.internal.decorators.DecoratorManager;
 
 public class ClearcaseDecorator
 	extends LabelProvider
 	implements ILightweightLabelDecorator, StateChangeListener
 {
 	private static final String ID =
 		"net.sourceforge.eclipseccase.ui.decorator";
 
 	// Used to exit the isDirty resource visitor
 	private static final CoreException CORE_DIRTY_EXCEPTION =
 		new CoreException(new Status(IStatus.OK, "dirty", 1, "", null));
 	private static final CoreException CORE_UNKNOWN_EXCEPTION =
 		new CoreException(new Status(IStatus.OK, "unknown", 1, "", null));
 	private final int CLEAN_STATE = 0;
 	private final int DIRTY_STATE = 1;
 	private final int UNKNOWN_STATE = 2;
 
 	private boolean decorationInProcess = false;
 	private List parentQueue = new LinkedList();
 
 	public ClearcaseDecorator()
 	{
 		super();
 		DecoratorManager manager =
 			(DecoratorManager) ClearcasePlugin
 				.getDefault()
 				.getWorkbench()
 				.getDecoratorManager();
 		addListener(manager);
 		StateCacheFactory.getInstance().addStateChangeListerer(this);
 		
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask()
 		{
 			public void run()
 			{
 				synchronized(ClearcaseDecorator.this)
 				{
 					if (decorationInProcess)
 					{
 						decorationInProcess = false;
 						return;
 					}
 				}
 				synchronized(parentQueue)
 				{
 					resourceStateChanged(
 						(IResource[]) parentQueue.toArray(
 							new IResource[parentQueue.size()]));
 					parentQueue.clear();
 				}
 			}
 		}, 0, 250);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
 	 */
 	public void decorate(Object element, IDecoration decoration)
 	{
 		IResource resource = getResource(element);
 		if (resource == null || resource.getType() == IResource.ROOT)
 			return;
 		ClearcaseProvider p = ClearcaseProvider.getProvider(resource);
 		if (p == null)
 			return;
 
 		if (p.isUnknownState(resource))
 		{
 			decoration.addOverlay(
 				ClearcaseImages.getImageDescriptor(
 					ClearcaseImages.IMG_UNKNOWN_OVR));
 			p.hasRemote(resource);
 		}
 		else
 		{
 			if (resource.getType() != IResource.PROJECT && !p.hasRemote(resource))
 				return;
 
 			int dirty = isDirty(resource);
 
 			if (p.isCheckedOut(resource))
 			{
 				decoration.addOverlay(
 					ClearcaseImages.getImageDescriptor(
 						ISharedImages.IMG_CHECKEDOUT_OVR));
 			}
 			else if (dirty == DIRTY_STATE)
 			{
 				decoration.addOverlay(
 					ClearcaseImages.getImageDescriptor(
 						ClearcaseImages.IMG_DIRTY_OVR));
 			}
 			else if (dirty == UNKNOWN_STATE)
 			{
 				decoration.addOverlay(
 					ClearcaseImages.getImageDescriptor(
 						ClearcaseImages.IMG_DIRTY_UNKNOWN_OVR));
 			}
 			else if (p.isHijacked(resource))
 			{
 				decoration.addOverlay(
 					ClearcaseImages.getImageDescriptor(
 						ClearcaseImages.IMG_HIJACKED_OVR));
 			}
 			else if (p.hasRemote(resource))
 			{
 				decoration.addOverlay(
 					ClearcaseImages.getImageDescriptor(
 						ISharedImages.IMG_CHECKEDIN_OVR));
 			}
 
 			StringBuffer prefix = new StringBuffer();
 			StringBuffer suffix = new StringBuffer();
 
 			if (ClearcasePlugin.isTextViewDecoration()
 				&& resource.getType() == IResource.PROJECT)
 			{
 				suffix.append(" [view: ");
 				suffix.append(p.getViewName(resource));
 				suffix.append("]");
 			}
 
 			if (ClearcasePlugin.isTextDirtyDecoration())
 			{
 				if (dirty == DIRTY_STATE)
 				{
 					prefix.append(">");
 				}
 				else if (dirty == UNKNOWN_STATE)
 				{
 					prefix.append("?>");
 				}
 			}
 
 			if (ClearcasePlugin.isTextVersionDecoration())
 			{
 				suffix.append(" : ");
 				suffix.append(p.getVersion(resource));
 			}
 
 			decoration.addPrefix(prefix.toString());
 			decoration.addSuffix(suffix.toString());
 		}
 
 	}
 
 	public static void refresh()
 	{
 		IDecoratorManager manager =
 			ClearcasePlugin.getDefault().getWorkbench().getDecoratorManager();
 		if (manager.getEnabled(ID))
 		{
 			ClearcaseDecorator activeDecorator =
				(ClearcaseDecorator) manager.getLightweightLabelDecorator(ID);
 			if (activeDecorator != null)
 			{
 				activeDecorator.postLabelEvent(
 					new LabelProviderChangedEvent(activeDecorator));
 			}
 		}
 	}
 
 	public static void refresh(IResource resource)
 	{
 		final List resources = new LinkedList();
 		try
 		{
 			resource.accept(new IResourceVisitor()
 			{
 				/**
 				 * @see org.eclipse.core.resources.IResourceVisitor#visit(IResource)
 				 */
 				public boolean visit(IResource resource) throws CoreException
 				{
 					resources.add(resource);
 					return true;
 				}
 			});
 		}
 		catch (CoreException ex)
 		{
 		}
 		labelResources(
 			(IResource[]) resources.toArray(new IResource[resources.size()]));
 	}
 
 	public static void labelResource(IResource resource)
 	{
 		IDecoratorManager manager =
 			ClearcasePlugin.getDefault().getWorkbench().getDecoratorManager();
 		if (manager.getEnabled(ID))
 		{
 			ClearcaseDecorator activeDecorator =
				(ClearcaseDecorator) manager.getLightweightLabelDecorator(ID);
 			if (activeDecorator != null)
 			{
 				activeDecorator.resourceStateChanged(new IResource[] { resource });
 			}
 		}
 	}
 
 	public static void labelResources(IResource[] resources)
 	{
 		IDecoratorManager manager =
 			ClearcasePlugin.getDefault().getWorkbench().getDecoratorManager();
 		if (manager.getEnabled(ID))
 		{
 			ClearcaseDecorator activeDecorator =
				(ClearcaseDecorator) manager.getLightweightLabelDecorator(ID);
 			if (activeDecorator != null)
 			{
 				activeDecorator.resourceStateChanged(resources);
 			}
 		}
 	}
 
 	public void resourceStateChanged(IResource[] changedResources)
 	{
 		boolean deepDecoration = ClearcasePlugin.isDeepDecoration();
 		for (int i = 0; i < changedResources.length; i++)
 		{
 			if (deepDecoration)
 				queueParents(changedResources[i]);
 		}
 
 		postLabelEvent(
 			new LabelProviderChangedEvent(this, changedResources));
 	}
 
 	private void queueParents(IResource resource)
 	{
 		IResource current = resource.getParent();
 
 		while (current != null && current.getType() != IResource.ROOT)
 		{
 			synchronized (parentQueue)
 			{
 				if (! parentQueue.contains(current))
 					parentQueue.add(current);
 			}
 			current = current.getParent();
 		}
 	}
 
 	private void postLabelEvent(final LabelProviderChangedEvent event)
 	{
 		synchronized (this)
 		{
 			decorationInProcess = true;
 		}
 		Display.getDefault().asyncExec(new Runnable()
 		{
 			public void run()
 			{
 				fireLabelProviderChanged(event);
 			}
 		});
 	}
 
 	private int isDirty(IResource resource)
 	{
 		// Since dirty == checkout/hijacked for files, redundant to show files as dirty
 		if (resource.getType() == IResource.FILE)
 			return CLEAN_STATE;
 
 		if (!ClearcasePlugin.isDeepDecoration())
 			return CLEAN_STATE;
 
 		try
 		{
 			resource.accept(new IResourceVisitor()
 			{
 				public boolean visit(IResource resource) throws CoreException
 				{
 					ClearcaseProvider p =
 						ClearcaseProvider.getProvider(resource);
 					if (p == null)
 						return false;
 
 					if (p.isUnknownState(resource))
 						throw CORE_UNKNOWN_EXCEPTION;
 
 					if (!p.hasRemote(resource))
 						return false;
 
 					if (p.isCheckedOut(resource) || p.isHijacked(resource))
 						throw CORE_DIRTY_EXCEPTION;
 
 					return true;
 				}
 			}, IResource.DEPTH_INFINITE, true);
 		}
 		catch (CoreException e)
 		{
 			//if our exception was caught, we know there's a dirty child
 			if (e == CORE_DIRTY_EXCEPTION)
 			{
 				return DIRTY_STATE;
 			}
 			else if (e == CORE_UNKNOWN_EXCEPTION)
 			{
 				return UNKNOWN_STATE;
 			}
 		}
 		return CLEAN_STATE;
 	}
 
 	private IResource getResource(Object object)
 	{
 		if (object instanceof IResource)
 		{
 			return (IResource) object;
 		}
 		if (object instanceof IAdaptable)
 		{
 			return (IResource) ((IAdaptable) object).getAdapter(
 				IResource.class);
 		}
 		return null;
 	}
 	
 	public void stateChanged(StateCache stateCache)
 	{
 		resourceStateChanged(new IResource[] { stateCache.getResource() });
 	}
 
 }
