 package org.twuni.evy;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EvyTest {
 
 	protected boolean success;
 
 	@Before
 	public void setUp() {
 		success = false;
 	}
 
 	@Test
 	public void testMultiLineProgramWithAncestralLookup() {
 
 		StatementTreeExecutor root = new Evy( "on say message\n  print message\nsay message=\"Hello\"" );
 
 		success = false;
 		root.subscribe( "print", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				Assert.assertEquals( "\"Hello\"", statement.lookup( "message" ) );
 				Assert.assertEquals( "\"Hello\"", statement.lookup( 0 ) );
 				Assert.assertNull( statement.lookup( 1 ) );
 				Assert.assertNull( statement.lookup( "print" ) );
 				success = true;
 			}
 
 		} );
 
 		root.execute();
 
 		Assert.assertTrue( success );
 
 	}
 
 	@Test
 	public void testMultiLineProgramWithSeveralSubscriptionsRunsAsExpected() {
 		StatementTreeExecutor root = new Evy( "on error\n  print message\nerror message=\"haha u suck\"" );
 		success = false;
 		root.subscribe( "print", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				success = true;
 				Assert.assertEquals( "\"haha u suck\"", statement.lookup( 0 ) );
 				Assert.assertEquals( "\"haha u suck\"", statement.lookup( "message" ) );
 			}
 
 		} );
 		root.execute();
 		Assert.assertTrue( success );
 	}
 
 	@Test
 	public void testMultipleExecutions() {
 
 		Evy evy = new Evy( "@ happens\n  test\n@@ happening\n  else" );
 
 		evy.subscribe( "test", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				success = false;
 			}
 
 		} );
 
 		evy.subscribe( "else", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				success = false;
 			}
 
 		} );
 
 		success = true;
 		evy.execute();
 		Assert.assertTrue( success );
 
 		success = true;
 		evy.execute( "happens once" );
 		Assert.assertFalse( success );
 
 		success = true;
 		evy.execute( "happens once" );
 		Assert.assertTrue( success );
 
 		success = true;
 		evy.execute( "happening" );
 		Assert.assertFalse( success );
 
 		success = true;
 		evy.execute( "happening" );
 		Assert.assertFalse( success );
 
 	}
 
 	@Test
 	public void testSubscriptionWithComplicatedStuff() {
 		Evy root = new Evy( "@ Greet\n  NPC says=\"Hi\"\n  Consider saying=\"Sup\"\n  Consider saying=\"Yo\"\n  @ Player says=\"Yo\"\n    Smack\n  @ Player says=\"Sup\"\n    Nod\n" );
 		success = false;
 		root.subscribe( "Nod", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				success = true;
 			}
 
 		} );
 		root.subscribe( "Smack", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				Assert.fail( "Should NOT get smacked in this scenario." );
 			}
 
 		} );
 		root.execute();
 		root.execute( "Greet" );
 		root.execute( "Player says=\"Sup\"" );
 		Assert.assertTrue( success );
 		success = false;
 		root.execute( "Player says=\"Sup\"" );
 		Assert.assertFalse( success );
 	}
 
 	@Test
 	public void testSubscriptionWithParameterQualifierDoesNotTriggerWhenEventIsPublishedWithoutThoseParameters() {
 		Evy root = new Evy( "@ taste chicken\n  say \"Yum!\"\n@ taste cauliflower\n  say \"Eww, nasty!\"" );
 		root.subscribe( "say", new StatementExecutor() {
 
 			@Override
 			public void execute( Statement statement ) {
 				if( !"Yum!".equals( statement.lookup( 0 ).split( "\"" )[1] ) ) {
 					Assert.fail( "Should never say anything except Yum! when tasting chicken." );
 				}
 			}
 
 		} );
 		root.execute();
 		root.execute( "taste chicken" );
 	}
 
 }
