 package com.horsefire.filecabinet;
 
 import java.io.File;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.horsefire.filecabinet.web.WebServer;
 
 public class FileCabinet {
 
 	private final WebServer m_server;
 	private final Importer m_importer;
 	private final AtomicBoolean m_shutdown;
 	private final File m_deskDir;
 	private final File m_cabinetDir;
 
 	@Inject
 	public FileCabinet(WebServer server, Importer importer,
 			@Named("shutdown-monitor") AtomicBoolean shutdown,
 			@Named("desk") File deskDir, @Named("cabinet") File cabinetDir) {
 		m_server = server;
 		m_importer = importer;
 		m_shutdown = shutdown;
 		m_deskDir = deskDir;
 		m_cabinetDir = cabinetDir;
 	}
 
 	public void run() throws Exception {
 		if (!m_deskDir.isDirectory()) {
 			if (!m_deskDir.mkdir()) {
 				System.err.println("Cannot create folder: " + m_deskDir);
 				return;
 			}
 		}
 		if (!m_cabinetDir.isDirectory()) {
 			if (!m_cabinetDir.mkdir()) {
 				System.err.println("Cannot create folder: " + m_cabinetDir);
 				return;
 			}
 		}
 
 		m_server.start();
 		m_importer.start();
 
 		while (!m_shutdown.get()) {
 			Thread.sleep(1000);
 		}
 		m_server.shutdown();
 		m_importer.shutdown();
 	}
 
 	public static void main(String[] args) throws Exception {
 		Options options = new Options();
 		try {
 			JCommander jc = new JCommander(options, args);
 			jc.setProgramName("java -jar filecabinet.jar");
 
 			if (options.help) {
 				jc.usage();
 				return;
 			}
 
 			if (options.version) {
				String version = FileCabinet.class.getPackage()
						.getImplementationVersion();
				System.out.println("File Cabinet " + version);
 				return;
 			}
 		} catch (ParameterException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Use --help to display usage");
 			return;
 		}
 
 		Guice.createInjector(new FcModule(options))
 				.getInstance(FileCabinet.class).run();
 	}
 }
