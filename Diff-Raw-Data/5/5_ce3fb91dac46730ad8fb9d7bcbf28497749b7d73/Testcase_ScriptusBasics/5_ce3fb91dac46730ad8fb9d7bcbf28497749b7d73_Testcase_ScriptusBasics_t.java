 package net.ex337.scriptus.tests;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import net.ex337.scriptus.ProcessScheduler;
 import net.ex337.scriptus.datastore.ScriptusDatastore;
 import net.ex337.scriptus.exceptions.ProcessNotFoundException;
 import net.ex337.scriptus.model.ScriptAction;
 import net.ex337.scriptus.model.ScriptProcess;
 import net.ex337.scriptus.model.api.Termination;
 import net.ex337.scriptus.model.api.functions.Ask;
 import net.ex337.scriptus.model.api.functions.Exec;
 import net.ex337.scriptus.model.api.functions.Fork;
 import net.ex337.scriptus.model.api.functions.Get;
 import net.ex337.scriptus.model.api.functions.Kill;
 import net.ex337.scriptus.model.api.functions.Listen;
 import net.ex337.scriptus.model.api.functions.Say;
 import net.ex337.scriptus.model.api.functions.Sleep;
 import net.ex337.scriptus.model.api.functions.Wait;
 import net.ex337.scriptus.model.api.output.ErrorTermination;
 import net.ex337.scriptus.model.api.output.NormalTermination;
 import net.ex337.scriptus.tests.support.ProcessSchedulerDelegate;
 import net.ex337.scriptus.transport.Transport;
 import net.ex337.scriptus.transport.impl.DummyTransport;
 
 /**
  * Tests the Scriptus API calls.
  * 
  * @author ian
  *
  */
 public class Testcase_ScriptusBasics extends BaseTestCase {
 
 	private static final String TEST_USER = "test";
 	private ProcessScheduler c;
 	private ScriptusDatastore datastore;
 	private Transport m;
 	
 	private static final Map<String,String> testSources = new HashMap<String,String>() {{
 		put("return.js", "return \"result\";");
 		put("log.js", "log(\"this is a log statement\"); return \"result\";");
 		put("prototypes.js", "Number.prototype.faargh = function(){return 'foo'}; say('foo');return new Number().faargh();");
 		put("syntaxError.js", "return nonexitent()");
 		put("throw.js", "try {throw \"this is an error\"} catch(e) {throw (typeof e);}");
 		put("fiddle.js", "scriptus.fork = function() {return \"not forking\";};return scriptus.fork()");
 		put("fiddle2.js", "scriptus = {}; return scriptus.fork()");
 		put("fork.js", "var pid = scriptus.fork(); return pid;");
 		put("forkNoPrefix.js", "var pid = fork(); return pid;");
 		put("exec.js", "exec('returnArg.js', 'arg1 arg2'); throw 'bad result';");
 		put("returnArg.js", "return \"result\"+args;");
 		put("exit.js", "function foo() {scriptus.exit(\"result\");} foo(); return \"bad result\"");
 		put("getHttp.js", "var s = get(\"http://www.google.com/robots.txt\");");
 		put("evalget.js", "var ss = get(\"https://raw.github.com/ianso/scriptus/master/scripts/lib/date-en-US.js\"); eval(ss); say(Date.today)");
 		put("evalgetBUG.js", "eval(get(\"https://raw.github.com/ianso/scriptus/master/scripts/lib/date-en-US.js\")); say(Date.today)");
 		put("getHttps.js", "var s = get(\"http://encrypted.google.com/robots.txt\");");
 		put("sleepHour.js", "scriptus.sleep(3);");
 		put("sleepDateObject.js", "scriptus.sleep(new Date());");
 		put("sleepDate.js", "scriptus.sleep(\"2012-9-11 10:00\");");
 		put("sleepDuration.js", "sleep(\"1y 2M 3d 4h\");");
 		put("sleepBadDuration.js", "sleep(\"1x\");");
 		put("sleepBadDate.js", "scriptus.sleep(\"2012 10:00\");");
 		put("wait.js", 
 				"var pid = scriptus.fork(); " +
 				"if(pid == 0) {" +
 				"	return \"waited\";" +
 				"} " +
 				"var result; var waited = scriptus.wait(function(arg){result = arg+\"foo\";});" +
 				"return result+waited;"
 			);
 		put("wait2.js", 
 				"var pid = scriptus.fork(); " +
 				"if(pid == 0) {" +
 				"	sleep('1d');" +
 				"	return \"waited\";" +
 				"} " +
 				"var result; var waited = scriptus.wait(function(arg){result = arg+\"fooslept\";});" +
 				"return result+waited;"
 			);
 		put("kill.js", 
 				"var pid = scriptus.fork(); " +
 				"if(pid == 0) {" +
 				"	sleep('1d');" +
 				"} " +
 				"kill(pid);");
 		put("ask.js", "var f = scriptus.ask(\"give me your number\", {to:\"foo\"}); if(f != \"response\") throw 1;");
 		put("askTimeout.js", "var f = scriptus.ask(\"give me your number\", {to:\"foo\", timeout:3}); if(f != \"response\") throw 1;");
 		put("defaultAsk.js", "var f = scriptus.ask(\"give me your number\"); if(f != \"response\") throw 1;");
 		put("say.js", "var foo = scriptus.say(\"message\", {to:\"foo\"}); if(foo == null) throw 1;");
 		put("defaultSay.js", "scriptus.say(\"message\"); if(foo == null) throw 1;");
 		put("listen.js", "var foo = scriptus.listen({to:\"foo\"}); if(foo == null) throw 1;");
 		put("defaultListen.js", "var foo = scriptus.listen(); if(foo == null) throw 1;");
 		put("evalBroken.js", "var foo = eval(\"scriptus.listen();\");");
 		put("eval.js", "var foo = eval(\"function() {scriptus.listen({to:\\\"foo\\\"});}\")(); if(foo == null) throw 1;");
 		put("breakSec.js", "java.lang.System.out.println(\"foo\");");
 		put("breakSec2.js", "var s = \"foo\"; s.getClass().forName(\"java.lang.System\")");
 		put("breakSec3.js", "java.lang.System.out.println(\"foo\");");
 		put("breakSec4.js", "var s = new Date(); var e = typeof s; var t = new RegExp(\"\\\\w+\"); var e = typeof t");
 		put("breakSec5.js", "try {throw \"exStr\";} catch(e) {var s = typeof e}");
 	}};
 
 	@Override
 	protected void setUp() throws Exception {
 
 		System.setProperty("scriptus.config", "test-scriptus.properties");
 		
 		super.setUp();
 		
 		m = (Transport) appContext.getBean("transport");
 		
 		c = (ProcessScheduler) appContext.getBean("scheduler");
 		
 		datastore = (ScriptusDatastore) appContext.getBean("datastore");
 		
 		for(Map.Entry<String,String> e : testSources.entrySet()) {
 			datastore.saveScriptSource(TEST_USER, e.getKey(), e.getValue());
 		}
 		
 		//((DummyTransport)m).response = "response";
 		
 	}
 	
 
 	@Override
 	protected void tearDown() throws Exception {
 	}
 
 	
 	
 	public void test_return() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "return.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Correct result", r instanceof NormalTermination);
 		
 		NormalTermination n = (NormalTermination) r;
 
 		r.visit(c, m, datastore, p); //sould say
 
 		assertEquals("Correct result", "result", n.getResult());
 		
 	}
 
 	public void test_log() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "log.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Correct result", r instanceof NormalTermination);
 		
 		NormalTermination n = (NormalTermination) r;
 
 		r.visit(c, m, datastore, p); //sould say
 
 		assertEquals("Correct result", "result", n.getResult());
 		
 	}
 
 	/**
 	 * Illustrates the problems of scripts breaking
 	 * because of prototypes.
 	 * 
 	 * 
 	 * @throws IOException
 	 */
 	public void test_prototypes() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "prototypes.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Correct result", r instanceof Say);
 		
 		p.save();
 
 		r.visit(new ProcessSchedulerDelegate(c) {
 
 			@Override
 			public void execute(UUID pid) {
 				ScriptProcess pp = datastore.getProcess(pid);
 				ScriptAction rr = pp.call();
                 assertEquals("final result", NormalTermination.class, rr.getClass());
                 assertEquals("final result value OK", "foo", ((NormalTermination)rr).getResult());
 			}
 			
 		}, m, datastore, p); //sould say
 		
 
 	}
 
 	public void test_syntaxError() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "syntaxError.js", "", "owner");
 		
 		ScriptAction r = p.call();
 
 		assertTrue("Error termination", r instanceof ErrorTermination);
 		
 	}
 
 	public void test_throwException() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "throw.js", "", "owner");
 		
 		ScriptAction r = p.call();
 
 		assertTrue("Error termination", r instanceof ErrorTermination);
 		
 	}
 	
 	
 	public void test_fiddleWithAPI() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "fiddle.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Didn't fork correctly", ! (r instanceof Fork));
 	}
 
 	public void test_fiddleWithAPI2() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "fiddle2.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("error condition", r instanceof ErrorTermination);
 		
 	}
 
 	
 	public void test_fork() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "fork.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Forked correctly", r instanceof Fork);
 		
 		r.visit(c, m, datastore, p);
 
 	}
 	
 	public void test_forkNoPrefix() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "forkNoPrefix.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Forked correctly", r instanceof Fork);
 		
 		r.visit(c, m, datastore, p);
 
 	}
 	
 	public void test_exit() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "exit.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Exited correctly", r instanceof NormalTermination);
 		assertTrue("correct exit", ((NormalTermination)r).getResult().equals("result"));
 
 	}
 
 	public void test_exec() throws IOException {
 		
 		final ScriptProcess p = datastore.newProcess(TEST_USER, "exec.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		p.save();
 		
 		assertTrue("Exec correctly", r instanceof Exec);
 		assertEquals("Exec good program", "returnArg.js", ((Exec)r).getScript());
 		assertEquals("Exec good args", "arg1 arg2", ((Exec)r).getArgs());
 		
 		r.visit(new ProcessSchedulerDelegate(c) {
 
 			@Override
 			public void execute(UUID pid) {
 
 				ScriptProcess pp = datastore.getProcess(pid);
 				
 				assertEquals("good pid", p.getPid(), pid);
 				assertEquals("good source", "returnArg.js", pp.getSourceName());
 				assertEquals("good args", "arg1 arg2", pp.getArgs());
 				
 				ScriptAction aa = pp.call();
 				
 				assertEquals("good class result", NormalTermination.class, aa.getClass());
 				assertEquals("goood result", "resultarg1 arg2", ((NormalTermination)aa).getResult());
 			}
 			
 		}, m, datastore, p);
 	}
 
 	public void test_sleepHour() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepHour.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Sleep);
 
 	}
 
 	public void test_getHttp() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "getHttp.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Get);
 
 		p.save();
 
 		Get g = (Get) r;
 		
 		g.visit(c, m, datastore, p);
 		
 		p = datastore.getProcess(p.getPid());
 		
 		assertTrue("got content", p.getState() instanceof String);
 		
 		String content = (String) p.getState();
 		
 		assertTrue("content ok", content.contains("User-agent: *"));
 		
 	}
 
 
 	public void test_evalGet() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "evalget.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Get);
 
 		p.save();
 
 		Get g = (Get) r;
 		
 		g.visit(c, m, datastore, p);
 		
 		p = datastore.getProcess(p.getPid());
 		
 		assertTrue("got content", p.getState() instanceof String);
 		
 		r = p.call();
 		
 		assertTrue("said correctly", r instanceof Say);
 		
 		assertTrue("contains fn def", ((Say)r).getMsg().contains("return new Date().clearTime();"));
 	}
 
 
 	public void test_evalGetBug() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "evalgetBUG.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Get);
 
 		p.save();
 
 		Get g = (Get) r;
 		
 		g.visit(c, m, datastore, p);
 		
 		p = datastore.getProcess(p.getPid());
 		
 		assertTrue("got content", p.getState() instanceof String);
 		
 		r = p.call();
 		
 		assertTrue("said correctly", r instanceof Say);
 		
 		assertTrue("contains fn def", ((Say)r).getMsg().contains("return new Date().clearTime();"));
 	}
 
 	
 	public void test_getHttps() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "getHttps.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Get);
 
 		p.save();
 
 		Get g = (Get) r;
 		
 		g.visit(c, m, datastore, p);
 		
 		p = datastore.getProcess(p.getPid());
 		
 		assertTrue("got content", p.getState() instanceof String);
 		
 		String content = (String) p.getState();
 		
 		assertTrue("content ok", content.contains("User-agent: *"));
 
 	}
 
 	public void test_sleepDate() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepDate.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Sleep);
 
 	}
 
 	public void test_sleepDateObject() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepDateObject.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Sleep);
 
 	}
 
 	public void test_sleepDuration() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepDuration.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("slept correctly", r instanceof Sleep);
 
 		Sleep s = (Sleep)r;
 		
 		Calendar target = Calendar.getInstance();
 		target.add(Calendar.YEAR, 1);
 		target.add(Calendar.MONTH, 2);
 		target.add(Calendar.DATE, 3);
 		target.add(Calendar.HOUR, 4);
 		
 		Calendar c = s.getUntil();
 		
 		assertEquals("good year", target.get(Calendar.YEAR), c.get(Calendar.YEAR));
 		assertEquals("good month", target.get(Calendar.MONTH), c.get(Calendar.MONTH));
 		assertEquals("good day", target.get(Calendar.DATE), c.get(Calendar.DATE));
 		assertEquals("good hour", target.get(Calendar.HOUR), c.get(Calendar.HOUR));
 		
 	}
 
 	public void test_sleepBadDuration() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepBadDuration.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("error", r instanceof ErrorTermination);
 
 		
 	}
 
 	public void test_sleepBadDate() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "sleepBadDate.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Forked correctly", r instanceof ErrorTermination);
 
 	}
 
 	/**
 	 * tests for when child process finished before wait() is called
 	 */
 	public void test_wait() throws IOException {
 
 		final ScriptProcess p = datastore.newProcess(TEST_USER, "wait.js", "", "owner");
 		
 		p.save();
 		
 		ScriptAction r = p.call();
 
 		assertTrue("Forked correctly", r instanceof Fork);
 		
 		final ThreadLocal<Boolean> executedParentPostFork = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedParentPostWait = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedChild = new ThreadLocal<Boolean>();
 		
 		ProcessScheduler testScheduler = new ProcessSchedulerDelegate(c) {
 			
 			private UUID childPid;
 
 			@Override
 			public void execute(UUID pid) {
 				
 				if( ! pid.equals(p.getPid())) {
 					
 					executedChild.set(Boolean.TRUE);
 					
 					childPid = pid;
 					
 					super.execute(pid);
 
 					return;
 				}
 				
 				if(pid.equals(p.getPid())) {
 
 					if(Boolean.TRUE.equals(executedParentPostFork.get())) {
 						
 						executedParentPostWait.set(Boolean.TRUE);
 						
 						ScriptAction enfin = datastore.getProcess(pid).call();
 						
 						assertTrue("script finished", enfin instanceof Termination);
 						assertEquals("script result OK", "waitedfoo"+childPid, ((Termination)enfin).getResult());
 						
 					} else {
 
 						executedParentPostFork.set(Boolean.TRUE);
 						
 						ScriptProcess p2 = datastore.getProcess(pid);
 						
 						ScriptAction r2 = p2.call();
 						
 						p2.save();
 
 						assertTrue("Waited correctly", r2 instanceof Wait);
 
 						//pause thread until child has termination
 						
 						r2.visit(this, m, datastore, p2);
 
 					}
 
 				}
 				
 			}
 			
 		};
 		
 		r.visit(testScheduler, m, datastore, p);
 		
 		assertEquals("Executed child", Boolean.TRUE, executedChild.get());
 		assertEquals("Executed parent (post-fork)", Boolean.TRUE, executedParentPostFork.get());
 		assertEquals("Executed parent (post-wait)", Boolean.TRUE, executedParentPostWait.get());
 		
 	}
 
 	/**
 	 * tests for when child process finished after wait() is called
 	 */
 	public void test_wait2() throws IOException {
 
 		final ScriptProcess p = datastore.newProcess(TEST_USER, "wait2.js", "", "owner");
 		
 		p.save();
 		
 		ScriptAction r = p.call();
 
 		assertTrue("Forked correctly", r instanceof Fork);
 		
 		final ThreadLocal<Boolean> executedParentPostFork = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedParentPostWait = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedChild = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedChildPostSleep = new ThreadLocal<Boolean>();
 
 		ProcessScheduler testScheduler = new ProcessSchedulerDelegate(c) {
 			
 			private UUID childPid;
 
 			@Override
 			public void execute(UUID pid) {
 				
 				if( ! pid.equals(p.getPid())) {
 					//executing child
 
 					if(Boolean.TRUE.equals(executedChild.get())) {
 						
 						executedChildPostSleep.set(Boolean.TRUE);
 						
 						ScriptProcess p2 = datastore.getProcess(pid);
 						
 						ScriptAction r2 = p2.call();
 
 						assertTrue("in child termination", r2 instanceof Termination);
 						
 						p2.save();
 						
 						r2.visit(this, m, datastore, p2);
 
 					} else {
 						
 						executedChild.set(Boolean.TRUE);
 						
 						childPid = pid;
 						
 						ScriptProcess p2 = datastore.getProcess(pid);
 						
 						ScriptAction r2 = p2.call();
 
 						p2.save();
 						
 						assertTrue("in child sleep", r2 instanceof Sleep);
 						
 
 					}
 					
 					return;
 				}
 				
 				if(pid.equals(p.getPid())) {
 
 					if(Boolean.TRUE.equals(executedParentPostFork.get())) {
 						
 						executedParentPostWait.set(Boolean.TRUE);
 						
 						ScriptAction enfin = datastore.getProcess(pid).call();
 						
 						assertTrue("script finished", enfin instanceof Termination);
 						assertEquals("script result OK", "waitedfooslept"+childPid, ((Termination)enfin).getResult());
 						
 					} else {
 						
 						executedParentPostFork.set(Boolean.TRUE);
 						
 						ScriptProcess p2 = datastore.getProcess(pid);
 						
 						ScriptAction r2 = p2.call();
 						
 						p2.save();
 
 						assertTrue("Waited correctly", r2 instanceof Wait);
 
 						//pause thread until child has termination
 						
 						r2.visit(this, m, datastore, p2);
 						
 						//assert parent is still waiting
 						//wake child and execute
 						execute(childPid);
 
 					}
 
 				}
 				
 			}
 			
 		};
 		
 		r.visit(testScheduler, m, datastore, p);
 		
 		assertEquals("Executed child", Boolean.TRUE, executedChild.get());
 		assertEquals("Executed child post-sleep", Boolean.TRUE, executedChildPostSleep.get());
 		
 		assertEquals("Executed parent (post-fork)", Boolean.TRUE, executedParentPostFork.get());
 		assertEquals("Executed parent (post-wait)", Boolean.TRUE, executedParentPostWait.get());
 		
 	}
 
 	public void test_kill() throws IOException {
 
 		final ScriptProcess p = datastore.newProcess(TEST_USER, "kill.js", "", "owner");
 		
 		p.save();
 		
 		ScriptAction r = p.call();
 
 		assertTrue("Forked correctly", r instanceof Fork);
 		
 		final ThreadLocal<Boolean> executedParentPostFork = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedParentPostKill = new ThreadLocal<Boolean>();
 		final ThreadLocal<Boolean> executedChild = new ThreadLocal<Boolean>();
 		
 		System.out.println("pid="+p.getPid());
 		
 		ProcessScheduler testScheduler = new ProcessSchedulerDelegate(c) {
 			
 			private UUID childPid;
 			
 			@Override
 			public void execute(UUID pid) {
 				
 				if( ! pid.equals(p.getPid())) {
 					//executing child
 					
 					executedChild.set(Boolean.TRUE);
 					
 					childPid = pid;
 					
 					super.execute(pid);
 
 					return;
 				}
 				
 				if(pid.equals(p.getPid())) {
 
 					//executing parent
 					
 					if(Boolean.TRUE.equals(executedParentPostFork.get())) {
 						
 						//post-kill
 						
 						executedParentPostKill.set(Boolean.TRUE);
 
 					} else {
 						
 						//post-fork, pre-kill, pid
 
 						executedParentPostFork.set(Boolean.TRUE);
 						
 						ScriptProcess p2 = datastore.getProcess(pid);
 						
 						ScriptAction r2 = p2.call();
 						
 						p2.save();
 
 						assertTrue("Killed correctly", r2 instanceof Kill);
 						
 						r2.visit(this, m, datastore, p2);
 
 						boolean caughtNotFoundExcepton = false;
 						
 						try {
 							datastore.getProcess(childPid);
 						} catch(ProcessNotFoundException sre) {
 							caughtNotFoundExcepton = true;
 						}
 						
 						assertTrue("process doesn't exist", caughtNotFoundExcepton);
 						
 						
 						
 					}
 
 				}
 				
 			}
 			
 		};
 		
 		assertTrue("forking correctly", r instanceof Fork);
 		
 		r.visit(testScheduler, m, datastore, p);
 		
 		assertEquals("Executed child", Boolean.TRUE, executedChild.get());
 		assertEquals("Executed parent (post-fork)", Boolean.TRUE, executedParentPostFork.get());
 		assertEquals("Executed parent (post-kill)", Boolean.TRUE, executedParentPostKill.get());
 		
 	}
 
 
 	public void test_ask() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "ask.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Asked correctly", r instanceof Ask);
 		assertTrue("Asked correctly foo", ((Ask)r).getWho().equals("foo"));
 		
 		p.save();
 
 		r.visit(c, m, datastore, p);
 		
 	}
 
 	public void test_defaultAsk() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "defaultAsk.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Asked correctly", r instanceof Ask);
 		assertTrue("Asked correctly owner", ((Ask)r).getWho().equals("owner"));
 		
 		p.save();
 
 		r.visit(c, m, datastore, p);
 		
 	}
 
 
 	public void test_askTimeout() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "askTimeout.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Asked correctly", r instanceof Ask);
 		assertTrue("Asked correctly owner", ((Ask)r).getWho().equals("foo"));
 		
 		p.save();
 
 		r.visit(c, m, datastore, p);
 		
 	}
 
 	public void test_say() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "say.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Said correctly", r instanceof Say);
 		assertTrue("Said correctly to user", ((Say)r).getWho().equals("foo"));
 		assertTrue("Said correctly message", ((Say)r).getMsg().equals("message"));
 		
 		p.save();
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_defaultSay() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "defaultSay.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Said correctly", r instanceof Say);
 		assertTrue("Said to owner correctly", ((Say)r).getWho().equals("owner"));
 
 	}
 
 	public void test_listen() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "listen.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Listened correctly", r instanceof Listen);
 		assertTrue("Listened correctly to", ((Listen)r).getWho().equals("foo"));
 		
 		p.save();
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_defaultListen() throws IOException {
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "defaultListen.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Listened correctly", r instanceof Listen);
 		assertTrue("Listened correctly to", ((Listen)r).getWho().equals("owner"));
 		
 		p.save();
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_evalBroken() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "evalBroken.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Broken", r instanceof ErrorTermination);
 	}
 
 	public void test_eval() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "eval.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Listened correctly", r instanceof Listen);
 
 		p.save();
 
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_addTwoNumbers() throws IOException {
 
		((DummyTransport)m).defaultResponse = "4";
 
 		ScriptProcess p = datastore.newProcess(TEST_USER, "addTwoNumbers.js", "", "owner");
 		
 		ScriptAction r = p.call();
 
 		p.save();
 
 		assertTrue("First correctly", r instanceof Fork);
 		
 		//everything else should happen immediately with mocks
 		r.visit(c, m, datastore, p);
 		
		((DummyTransport)m).defaultResponse = "response";
 	}
 
 	public void test_breakSecurity() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "breakSec.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Failed correctly", r instanceof ErrorTermination);
 		
 		System.out.println(((ErrorTermination)r).getError());
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_breakSecurity2() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "breakSec2.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Failed correctly", r instanceof ErrorTermination);
 		
 		System.out.println(((ErrorTermination)r).getError());
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_breakSecurity3() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "breakSec3.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Failed correctly", r instanceof ErrorTermination);
 		
 		System.out.println(((ErrorTermination)r).getError());
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_breakSecurity4() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "breakSec4.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Failed correctly", r instanceof ErrorTermination);
 		
 		System.out.println(((ErrorTermination)r).getError());
 		
 		r.visit(c, m, datastore, p);
 	}
 
 	public void test_breakSecurity5() throws IOException {
 		
 		ScriptProcess p = datastore.newProcess(TEST_USER, "breakSec5.js", "", "owner");
 		
 		ScriptAction r = p.call();
 		
 		assertTrue("Failed correctly", r instanceof ErrorTermination);
 		
 		System.out.println(((ErrorTermination)r).getError());
 		
 		r.visit(c, m, datastore, p);
 	}
 	
 	
 }
