 package org.lantern.state;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.codehaus.jackson.annotate.JsonCreator;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonValue;
 import org.lantern.state.Friend.Status;
 
 /**
  * Lantern friends are users whom you trust to directly proxy for you (when they
  * are in give mode), and to whom you send and receive kscope ads. That is, it
  * is effectively Lantern's overlay on your XMPP roster.
  *
  * Friendship is a directional relationship, like XMPP subscription. As soon as
  * you invite someone to Lantern, you also friend them (and, if they are not on
  * your XMPP roster, attempt to subscribe to them). If they reject your XMPP
  * subscribe request, you keep them listed as a friend. After all, they might be
  * subscribed to you, and thus you will still send them ads (you just won't
  * receive any).
  *
  * This is because directional friendship is relatively easy to implement, and
  * is probably a simpler UI.
  *
  */
 public class Friends implements Serializable {
 
     private static final long serialVersionUID = -6101864776815427653L;
 
     private final Map<String, Friend> friends =
         new ConcurrentHashMap<String, Friend>();
 
     /**
      * Whether we need to sync to the server. This is only used to reduce the
      * number of friends syncs.
      */
     private boolean needsSync = true;
 
     public Friends() {}
 
     @JsonValue
     public Collection<Friend> getFriends() {
         return vals(friends);
     }
 
     public void add(Friend friend) {
         friends.put(friend.getEmail(), friend);
         needsSync = true;
     }
 
     @JsonCreator
     public static Friends create(final List<Friend> list) {
         Friends friends = new Friends();
         for (final Friend profile : list) {
             friends.friends.put(profile.getEmail(), profile);
         }
         return friends;
     }
 
     public void remove(final String email) {
        friends.remove(email);
         needsSync = true;
     }
 
     private Collection<Friend> vals(final Map<String, Friend> map) {
         synchronized (map) {
             return map.values();
         }
     }
 
     public void clear() {
         friends.clear();
     }
 
     @JsonIgnore
     public void setPendingSubscriptionRequest(String email) {
         Friend friend = friends.get(email);
         if (friend == null) {
             friend = new Friend(email);
             friends.put(email, friend);
             needsSync = true;
         }
         if (!friend.isPendingSubscriptionRequest()) {
             friend.setPendingSubscriptionRequest(true);
             needsSync = true;
         }
     }
 
     public Friend get(String email) {
        return friends.get(email);
     }
 
     public boolean needsSync() {
         return needsSync;
     }
 
     @JsonIgnore
     public void setNeedsSync(boolean needsSync) {
         this.needsSync = needsSync;
     }
 
     @JsonIgnore
     public void setStatus(String email, Status status) {
         Friend friend = friends.get(email);
         if (friend.getStatus() != Status.friend) {
             friend.setStatus(status);
             this.needsSync = true;
         }
     }
 
 }
