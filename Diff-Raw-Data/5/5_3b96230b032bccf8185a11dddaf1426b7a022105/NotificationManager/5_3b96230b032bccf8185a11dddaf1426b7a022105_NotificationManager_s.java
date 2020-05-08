 /*
  * Created by IntelliJ IDEA.
  * User: gpothier
  * Date: Feb 18, 2002
  * Time: 2:15:59 PM
  * To change template for new class use
  * Code Style | Class Templates options (Tools | IDE Options).
  */
 package zz.utils.notification;
 
 import java.lang.ref.WeakReference;
 import java.util.Iterator;
 import java.util.List;
 
 import zz.utils.FailsafeLinkedList;
 import zz.utils.Utils;
 
 /**
  * Registers a set of {@link Notifiable notifiables} whose {@link Notifiable#processMessage(Message) } method
  * will be called when this {@link #notify(Message) } is invoked.
  */
 public class NotificationManager
 {
 	private List itsNotifiableReferences = new FailsafeLinkedList();
 
 	private boolean itsActive = true;
 
 	public NotificationManager()
 	{
 	}
 
 
 	/**
 	 * Activates or deactivates the notification manager.
 	 * When deactivated, it doesn't dispatch any message.
 	 */
 	public void setActive(boolean aActive)
 	{
 		itsActive = aActive;
 	}
 
 	public boolean isActive()
 	{
 		return itsActive;
 	}
 
 	/**
 	 * Removes all Notifiables from the notification manager
 	 */
 	public void clear()
 	{
 		itsNotifiableReferences.clear();
 		check();
 	}
 
 	/**
 	 * Adds a notifiable to the list.
 	 * <p>
 	 * IMPORTANT: the notifiables are held in weak references, so constructs such as
 	 * <code>
 	 * theNotificationManager.addNotifiable (new Notifiable ()
 	 * {
 	 * 		...
 	 * });
 	 * </code>
 	 * are incorrect, as the notifiable will be immediately garbage collected.
 	 */
 	public void addNotifiable(Notifiable aNotifiable)
 	{
 		itsNotifiableReferences.add(new NotifiableReference(aNotifiable));
 		check();
 	}
 
 	public void removeNotifiable(Notifiable aNotifiable)
 	{
 		Utils.remove(aNotifiable, itsNotifiableReferences);
 		check();
 	}
 	
 	private void check()
 	{
 		for (Iterator theIterator = itsNotifiableReferences.iterator(); theIterator.hasNext();)
 		{
 			NotifiableReference theNotifiableReference = (NotifiableReference) theIterator.next();
 			assert theNotifiableReference != null;
 		}		
 	}
 
 	public void notify(Message aMessage)
 	{
 		check();
 		if (!isActive()) return;
 
 		for (Iterator theIterator = itsNotifiableReferences.iterator(); theIterator.hasNext();)
 		{
 			NotifiableReference theNotifiableReference = (NotifiableReference) theIterator.next();
 			Notifiable theNotifiable = theNotifiableReference.getNotifiable();
 
 			if (theNotifiable == null)
 				theIterator.remove();
 			else
 				theNotifiable.processMessage(aMessage);
 		}
 		check();
 	}
 
 	private static class NotifiableReference extends WeakReference
 	{
 		public NotifiableReference(Notifiable aNotifiable)
 		{
 			super(aNotifiable);
 		}
 
 		public Notifiable getNotifiable()
 		{
 			return (Notifiable) get();
 		}
 
 		public boolean equals(Object obj)
 		{
 			if (obj == this) return true;
 
 			if (obj instanceof Notifiable)
 			{
 				Notifiable theNotifiable = (Notifiable) obj;
 				return theNotifiable == getNotifiable();
 			}
 
 			return false;
 		}
 	}
 
 }
