 package br.ufmg.dcc.vod.ncrawler.ui;
 
import java.io.IOException;
 import java.lang.reflect.Constructor;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 import br.ufmg.dcc.vod.ncrawler.common.LoggerInitiator;
 import br.ufmg.dcc.vod.ncrawler.distributed.nio.service.NIOServer;
 import br.ufmg.dcc.vod.ncrawler.distributed.worker.JobExecutorListener;
 import br.ufmg.dcc.vod.ncrawler.jobs.JobExecutor;
 import br.ufmg.dcc.vod.ncrawler.protocol_buffers.Worker.CrawlRequest;
 
 public class WorkerUP extends Command {
 
 	private static final String PORT = "p";
 	private static final String LOG_FILE = "l";
 	private static final String EXECUTOR_CLASS = "e";
 	private static final String SLEEP_TIME = "t";
 
 	@Override
 	@SuppressWarnings("static-access")
 	public Options getOptions() {
 		Options opts = new Options();
 		Option portOpt = OptionBuilder
 				.withArgName("port")
 				.hasArg()
 				.isRequired()
 				.withDescription("Port to Bind")
 				.create(PORT);
 		
 		Option logFileOpt = OptionBuilder
 				.withArgName("file")
 				.hasArg()
 				.isRequired()
 				.withDescription("Log File")
 				.create(LOG_FILE);
 		
 		Option executorOpt = OptionBuilder
 				.withArgName("executor")
 				.hasArg()
 				.isRequired()
 				.withDescription("Executor class to use")
 				.create(EXECUTOR_CLASS);
 		
 		Option sleepTimeOpt = OptionBuilder
 				.withArgName("sleep-time")
 				.hasArg()
 				.isRequired()
 				.withDescription("Sleep time (seconds)")
 				.create(SLEEP_TIME);
 		
 		opts.addOption(portOpt);
 		opts.addOption(logFileOpt);
 		opts.addOption(executorOpt);
 		opts.addOption(sleepTimeOpt);
 		
 		return opts;
 	}
 
 	@Override
	public int exec(CommandLine cli) throws IOException, InstantiationException, 
			IllegalAccessException, ClassNotFoundException {
 		
 		int port = Integer.parseInt(cli.getOptionValue(PORT));
 		LoggerInitiator.initiateLog(cli.getOptionValue(LOG_FILE));
 		String cls = cli.getOptionValue(EXECUTOR_CLASS);
 		
 		Constructor<?> constructor = Class.forName(cls)
 				.getConstructor(Long.class);
 		JobExecutor executor = (JobExecutor) constructor.newInstance(sleepTime);
 		
 		JobExecutorListener jExec = new JobExecutorListener(executor);
 		NIOServer<CrawlRequest> server = new NIOServer<>(1, null, port, jExec);
 		server.start(true);
 		
 		return EXIT_CODES.OK;
 	}
 }
