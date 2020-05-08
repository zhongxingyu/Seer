 package org.ow2.mindEd.adl.custom.adapters;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.util.EContentAdapter;
import org.ow2.mindEd.adl.Body;
 
 
 public class BodyAdapter extends EContentAdapter {
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		super.notifyChanged(notification);
		if (notification.getNotifier() instanceof Body) {
			((Body)notification.getNotifier()).isAnonymous();
		}
 	}
 
 	/**
 	 * <b>Class</b> <i>SingletonHolder</i>
 	 * <p>
 	 * Provides static access to class
 	 * 
 	 * @author proustr
 	 * @model kind="custom implementation"
 	 */
 	private static class SingletonHolder {
 		private static BodyAdapter instance = new BodyAdapter();
 	}
 
 	public static BodyAdapter getInstance() {
 		return SingletonHolder.instance;
 	}
 }
