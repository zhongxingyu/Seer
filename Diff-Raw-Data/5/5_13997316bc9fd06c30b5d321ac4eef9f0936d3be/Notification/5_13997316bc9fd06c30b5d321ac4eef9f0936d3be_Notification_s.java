 package qs.swornshop.notification;
 
 import org.bukkit.entity.Player;
 
 /**
  * The notification interface is used to represent shop-related
  * notifications to players, viewed with /shop p[ending].
  * If this notification can be accepted/rejected, it should 
  * implement the {@link Request} interface.
  * If this notification is a message which must complete an action
  * when certain conditions are true, it should implement the 
  * {@link Claimable} interface.
  * A Notification is regarded as a message and will be deleted
  * once viewed, unless the notification is a Request or Claimable
  */
public interface Notification {
 	/**
 	 * Gets a short message suitable for this notification.
 	 * @param player the player to which the message will be sent (may be null)
 	 * @return the notification message
 	 */
 	public String getMessage(Player player);
 }
