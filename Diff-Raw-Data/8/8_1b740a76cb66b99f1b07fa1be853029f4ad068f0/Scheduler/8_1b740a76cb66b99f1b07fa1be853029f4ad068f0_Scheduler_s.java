 package org.oxymores.chronix.main;
 
 import java.io.File;
import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Properties;
 
 import org.oxymores.chronix.core.Application;
 import org.oxymores.chronix.core.Chain;
 import org.oxymores.chronix.core.ChronixContext;
 import org.oxymores.chronix.core.PlaceGroup;
 import org.oxymores.chronix.core.State;
 import org.oxymores.chronix.core.active.Clock;
 import org.oxymores.chronix.core.active.ClockRRule;
 import org.oxymores.chronix.core.active.ShellCommand;
import org.oxymores.chronix.demo.DemoApplication;
 import org.oxymores.chronix.demo.PlanBuilder;
 import org.oxymores.chronix.engine.ChronixEngine;
 import org.oxymores.chronix.wapi.JettyServer;
 
 public class Scheduler
 {
 	public static void main(String[] args) throws Exception
 	{
 		// Load properties
 		Properties applicationProps = new Properties();
 		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("scheduler.properties");
 		// FileInputStream in = new FileInputStream("scheduler.properties");
 		applicationProps.load(in);
 		in.close();
 
 		String hostname;
 		try
 		{
 			hostname = InetAddress.getLocalHost().getCanonicalHostName();
 		} catch (UnknownHostException e)
 		{
 			hostname = "localhost";
 		}
 
 		boolean startEngine = Boolean.parseBoolean(applicationProps.getProperty("jChronix.startup.engine", "true"));
 		boolean startWS = Boolean.parseBoolean(applicationProps.getProperty("jChronix.startup.webserver", "true"));
 		// boolean startRunner = Boolean.parseBoolean(applicationProps.getProperty("jChronix.startup.runner", "true"));
 		String repoPath = applicationProps.getProperty("jChronix.repository.path", ".");
 		String mainDataInterface = applicationProps.getProperty("jChronix.engine.mainDataInteface", hostname);
 		int mainDataPort = Integer.parseInt(applicationProps.getProperty("jChronix.engine.mainDataPort", "1789"));
 		String transacUnit = applicationProps.getProperty("jChronix.engine.transacPersistenceUnitName", "TransacUnit");
 		String historyUnit = applicationProps.getProperty("jChronix.engine.historyPersistenceUnitName", "HistoryUnit");
 		int nbRunner = Integer.parseInt(applicationProps.getProperty("jChronix.runner.maxjobs", "10"));
 
 		// /////////////////////
 		// DEBUG
 		// Application a = DemoApplication.getNewDemoApplication(mainDataInterface, mainDataPort);
 		Application a = PlanBuilder.buildApplication("testing console", "no description for tests");
 		PlaceGroup pgLocal = PlanBuilder.buildDefaultLocalNetwork(a, mainDataPort, mainDataInterface);
 		Chain c = PlanBuilder.buildChain(a, "chain1", "chain1", pgLocal);
 
 		ClockRRule rr1 = PlanBuilder.buildRRule10Seconds(a);
 		Clock ck1 = PlanBuilder.buildClock(a, "every 10 second", "every 10 second", rr1);
 		ck1.setDURATION(0);
		ShellCommand sc1 = PlanBuilder.buildShellCommand(a, "echo aa", "aa", "should display 'aa'");
 
 		State s1 = PlanBuilder.buildState(c, pgLocal, ck1);
 		State s2 = PlanBuilder.buildState(c, pgLocal, sc1);
 		s1.connectTo(s2);
 
 		ChronixContext ctx = new ChronixContext();
 		ctx.configurationDirectory = new File(repoPath);
 		ChronixEngine tmp = new ChronixEngine(repoPath, mainDataInterface + ":" + mainDataPort);
 		tmp.emptyDb();
 		ctx.saveApplication(a);
 		ctx.setWorkingAsCurrent(a);
 
 		// END DEBUG
 		// /////////////////////
 
 		// Engine
 		ChronixEngine e = null;
 		if (startEngine)
 		{
 			e = new ChronixEngine(repoPath, mainDataInterface + ":" + mainDataPort, transacUnit, historyUnit, false, nbRunner);
 			e.start();
 			e.waitForInitEnd();
 		}
 
 		// Web server
 		if (startWS)
 		{
 			JettyServer server = new JettyServer(e.ctx);
 			server.start();
 		}
 
 		Thread.sleep(1000000); // 1000s, debug
 	}
 }
