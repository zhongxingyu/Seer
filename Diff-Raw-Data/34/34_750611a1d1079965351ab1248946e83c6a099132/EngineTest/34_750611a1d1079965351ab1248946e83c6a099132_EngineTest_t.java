 package org.acme;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.action.MainAction.*;
 import static org.junit.Assert.*;
 
 import java.util.concurrent.CountDownLatch;
 
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.cotrix.action.Action;
 import org.cotrix.action.MainAction;
import org.cotrix.common.cdi.BeanSession;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.engine.Engine;
 import org.cotrix.engine.TaskOutcome;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.user.User;
 import org.cotrix.user.Users;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.googlecode.jeeunit.JeeunitRunner;
 
 @RunWith(JeeunitRunner.class)
 public class EngineTest {
 
 	
 	@Inject
 	Engine engine;
 	
 	@Inject 
 	LifecycleService service;
 	
 	@Produces @Current
	public BeanSession session() {
		
		User user = Users.user("joe").can(IMPORT,EDIT,LOCK.on("2"),UNLOCK).build(); //but can't seal
		
		BeanSession session = new BeanSession();
		
		session.add(User.class,user);
		
		return session;
	}
	
	@Produces @Current
 	public User testuser() {
 		
 		return Users.user("joe").can(IMPORT,EDIT,LOCK.on("2"),UNLOCK).build(); //but can't seal
 	}
 	
 	@Before
 	public void setup() {
 		
 		service.start("1");
 		service.start("2");
 	}
 	
 	@Test
 	public void executesCodelistAction() throws Exception {
 
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {
 				latch.countDown();
 			}
 		};
 		
 		Action action = LOCK.on("2");
 		
 		TaskOutcome<Void> outcome = engine.perform(action).with(task);
 		
 		if (latch.getCount()!=0)
 			fail();
 		
 		//actions are type-specific and resource-specific
 		for (Action a : outcome.nextActions()) {
 			assertEquals(a.resource(),action.resource());
 			assertSame(a.type(),action.type());
 		}
 		
 		
 	}
 	
 	@Test
 	public void executesCodelistActionAllowedByTemplate() throws Exception {
 
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {
 				latch.countDown();
 			}
 		};
 		
 		Action action = EDIT.on("1");
 		
 		TaskOutcome<Void> outcome = engine.perform(action).with(task);
 		
 		if (latch.getCount()!=0)
 			fail();
 
 		//actions are type-specific and resource-specific
 		for (Action a : outcome.nextActions()) {
 			assertEquals(a.resource(),action.resource());
 			assertSame(a.type(),action.type());
 		}
 		
 	}
 	
 
 	
 	@Test
 	public void executesMainAction() throws Exception {
 
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {
 				latch.countDown();
 			}
 		};
 		
 		Action action = IMPORT;
 		TaskOutcome<Void> outcome = engine.perform(action).with(task);
 		
 		if (latch.getCount()!=0)
 			fail();
 		
 		//actions are type-specific and resource-specific
 		for (Action a : outcome.nextActions()) {
 			assertEquals(a.resource(),action.resource());
 			assertSame(a.type(),action.type());
 		}
 	}
 	
 	@Test
 	public void failsMainAction() throws Exception {
 
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {}
 		};
 		
 		try {
 			
 			engine.perform(MainAction.PUBLISH).with(task);
 			
 			fail();
 		}
 		catch(IllegalAccessError e) {
 			System.out.println(e.getMessage());
 		}
 		
 	}
 
 	@Test
 	public void failsIllegalInstanceAction() throws Exception {
 
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {}
 		};
 		
 		try {
 			
 			engine.perform(UNLOCK.on("1")).with(task);
 			
 			fail();
 		}
 		catch(IllegalStateException e) {
 			System.out.println(e.getMessage());
 		}
 		
 	}
 	
 	
 	@Test
 	public void failsDisallowedInstanceAction() throws Exception {
 
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {}
 		};
 		
 		try {
 			engine.perform(LOCK.on("1")).with(task);
 			
 			fail();
 		}
 		catch(IllegalAccessError e) {
 			System.out.println(e.getMessage());
 		}
 		
 	}
 	
 }
