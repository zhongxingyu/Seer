 package cx.ath.mancel01.utils;
 
 import cx.ath.mancel01.utils.F.Action;
 import cx.ath.mancel01.utils.Actors.Actor;
 import org.junit.Test;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.concurrent.CountDownLatch;
 import static cx.ath.mancel01.utils.M.*;
 
 public class ActorTest {
     
     private static CountDownLatch down = new CountDownLatch(20);
     
     private static CountDownLatch userlatch = new CountDownLatch(12);
     
    //@Test
     public void testChatRoom() throws Exception {
         User user1 = new User("maurice");
         User user2 = new User("john");
         User user3 = new User("pete");
         ChatRoom room = new ChatRoom();
         room.startActor();
         room.send(new Suscribe(user1));
         room.send(new Suscribe(user2));
         room.send(new Suscribe(user3));
         Thread.sleep(200);
         user1.send("Hello guys!", room);
         Thread.sleep(200);
         user2.send("Hello too!", room);
         Thread.sleep(200);
         user3.send("Hello user1!", room);
         Thread.sleep(200);
         room.send(new Unsuscribe(user1));
         room.send(new Unsuscribe(user2));
         room.send(new Unsuscribe(user3)); 
         userlatch.await();
         room.stopSession();
         room.stopActor();
         Actors.shutdownAll();
     }
     
    //@Test
     public void testPingPong() throws Exception {
         Pong pong = new Pong();
         Ping ping = new Ping(pong);
         pong.startActor();
         ping.startActor();
         down.await();
         pong.stopActor();
         ping.stopActor();
         Actors.shutdownAll();
     }
 
     @Test
     public void testDummy() throws Exception {}
     
     public static class Ping extends Actor {
         
         private final Actor pongActor;
 
         public Ping(Actor pongActor) {
             this.pongActor = pongActor;
         }
 
         @Override
         public void act() {
             pongActor.send("ping", this);
             System.out.println("PING");
             loop(new Action<String>() {
                 @Override
                 public void apply(String msg) {
                     for (String value : with(caseStartsWith("pong")).match(msg)) {
                         System.out.println("PING");
                         if (down.getCount() == 0) {
                             pongActor.send("stop");
                             stopActor();
                             System.out.println("STOP PING");
                         } else {
                             pongActor.send("ping", me());
                         }
                     }
                 }
             });
         }
     }
     
     public static class Pong extends Actor {
 
         @Override
         public void act() {
             loop(new Action<String>() {
                 @Override
                 public void apply(String msg) {
                     for (String value : with(caseStartsWith("ping")).match(msg)) {
                         System.out.println("PONG");
                         down.countDown();
                         if (sender.isDefined()) {
                             sender.get().send("pong");
                         }
                     }
                     for (String value : with(caseStartsWith("stop")).match(msg)) {
                         System.out.println("STOP PONG");    
                         stopActor();
                     }
                 }
             });
         }
     }
     
     public static class ChatRoom extends Actor {
         
         private Map<User, Actor> session = new HashMap<User, Actor>();
 
         @Override
         public void act() {
             loop( new Action<Object>() {
                 @Override
                 public void apply(Object msg) {
                     for (Suscribe value : with(caseClassOf(Suscribe.class)).match(msg)) {
                         System.out.println(value.user.name + " suscribed ....");
                         userlatch.countDown();
                         session.put(value.getUser(), new UserActor(value.getUser()).startActor());
                     }
                     for (Unsuscribe value : with(caseClassOf(Unsuscribe.class)).match(msg)) {
                         System.out.println(value.user.name + " unsuscribed ....");
                         userlatch.countDown();
                         session.get(value.getUser()).stopActor();
                         session.remove(value.getUser());
                     }
                     for (Post value : with(caseClassOf(Post.class)).match(msg)) {
                         for (User user : session.keySet()) {
                             if (!user.equals(value.user)) {
                                 session.get(user).send(value);
                             }
                         }
                     }
                 }
             });
         }
         
         public void stopSession() {
             for (Actor actor : session.values()) {
                 actor.stopActor();
             }
         }
     }
     
     public static class UserActor extends Actor {
         
         private final User user;
 
         public UserActor(User user) {
             this.user = user;
         }
 
         @Override
         public void act() {
             loop(new Action<Post>() {
                 @Override
                 public void apply(Post post) {
                     System.out.println(user.name + " receive " + post.msg);
                     userlatch.countDown();
                 }
             });
         }
     }
     
     public static class Suscribe {
         
         private final User user;
 
         public Suscribe(User user) {
             this.user = user;
         }
 
         public User getUser() {
             return user;
         }
     }
     
     public static class Unsuscribe {
         
         private final User user;
 
         public Unsuscribe(User user) {
             this.user = user;
         }
         
         public User getUser() {
             return user;
         }
     }
     
     public static class Post {
         
         private final String msg;
         
         private final User user;
 
         public Post(String msg, User user) {
             this.msg = msg;
             this.user = user;
         }
 
         public String getMsg() {
             return msg;
         }
 
         public User getUser() {
             return user;
         }
     }
     
     public static class User {
         private final String name;
 
         public User(String name) {
             this.name = name;
         }
         
         public void send(String msg, ChatRoom room) {
             System.out.println(name + " sent " + msg);
             room.send(new Post(msg, this));
         }
     }
 }
