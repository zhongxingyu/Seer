 package com.intuit.ginsu.cli;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
import org.testng.AssertJUnit;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.beust.jcommander.IDefaultProvider;
 import com.beust.jcommander.JCommander;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.intuit.ginsu.commands.CommandDispatchServiceImpl;
 import com.intuit.ginsu.commands.CommandGenerateProject;
 import com.intuit.ginsu.commands.CommandHelp;
 import com.intuit.ginsu.commands.ICommandDispatchService;
 import com.intuit.ginsu.commands.SupportedCommandCollection;
 import com.intuit.ginsu.config.IConfigurationService;
 import com.intuit.ginsu.config.PropertyFileConfigurationService;
 
 public class GinsuCLIModuleTest {
 
 	public Injector injector;
 
 	@BeforeClass
 	public void setupInjector() {
 		this.injector = Guice.createInjector(new GinsuCLIModule());
 	}
 
 	@Test
 	public void testAppNameAnnotation() {
 
 		StringBuilder stringBuilder = new StringBuilder();
 		JCommander jCommander = injector.getInstance(JCommander.class);
 		jCommander.parse(new String[] { CommandGenerateProject.NAME });
 		jCommander.usage(stringBuilder);
 		assert stringBuilder.toString().contains("Ginsu");
 	}
 
 	@Test
 	public void testParsingServiceBinding() {
 		IInputParsingService inputParsingService = injector
 				.getInstance(IInputParsingService.class);
 		assert inputParsingService instanceof CommandLineParsingService;
 
 		// the IInputParsingService is not bound as a singleton
 		// so it should not equal another instance
 		IInputParsingService secondIInputParsingService = injector
 				.getInstance(IInputParsingService.class);
 		assert inputParsingService != secondIInputParsingService;
 	}
 
 	@Test
 	public void testConfigurationServiceSingletonBinding() {
 		IConfigurationService configService = injector
 				.getInstance(IConfigurationService.class);
 		assert configService instanceof PropertyFileConfigurationService;
 
 		// the IInputParsingService is not bound as a singleton
 		// so it should not equal another instance
 		IConfigurationService secondConfigService = injector
 				.getInstance(IConfigurationService.class);
 		assert configService == secondConfigService;
 	}
 
 	@Test
 	public void testCommandDispatchServiceBinding() {
 		ICommandDispatchService inputParsingService = injector
 				.getInstance(ICommandDispatchService.class);
 		assert inputParsingService instanceof CommandDispatchServiceImpl;
 
 		// the ICommandDispatchService is not bound as a singleton
 		// so it should not equal another instance
 		ICommandDispatchService secondICommandDispatchService = injector
 				.getInstance(ICommandDispatchService.class);
 		assert inputParsingService != secondICommandDispatchService;
 	}
 
 	@Test
 	public void testIDefaultProviderIsConfigurationService() {
 
 		// Since we mapped the IDefault provider to the
 		// PropertyFileConfigurationService
 		// we should get that instance back.
 		IDefaultProvider defaultProvider = injector
 				.getInstance(IDefaultProvider.class);
 		assert defaultProvider instanceof PropertyFileConfigurationService;
 
 		// Further validation that the Configuration Service Implementation is a
 		// singleton.
 		IConfigurationService secondConfigService = injector
 				.getInstance(IConfigurationService.class);
 		assert defaultProvider == secondConfigService;
 	}
 
 	@Test
 	public void testOutputStreamBinding() {
		AssertJUnit.assertEquals(System.out, injector.getInstance(OutputStream.class));
 	}
 
 	@Test
 	public void testMainArgsBinding() {
 		assert injector.getInstance(MainArgs.class) instanceof MainArgs;
 	}
 
 	@Test
 	public void testPrintWriterProvider() {
 		assert injector.getInstance(PrintWriter.class) instanceof PrintWriter;
 		// TODO: Figure out a way to validate that this contains a reference to
 		// System.out
 	}
 
 	@Test
 	public void testJCommanderProvider() {
 		// if we run the parse command and are able to get the parsed command
 		// then we can be sure that the injected JCommander object was delivered
 		// by the provider method
 		JCommander jCommander = injector.getInstance(JCommander.class);
 		jCommander.parse(new String[] { CommandGenerateProject.NAME });
 		String parsedCommand = jCommander.getParsedCommand();
 		assert parsedCommand.equals(CommandGenerateProject.NAME);
 
 		// Just for safe measure, also make sure that we are always getting a
 		// new instance of JCommander when we ask for it.
 		assert jCommander != injector.getInstance(JCommander.class);
 	}
 
 	@Test
 	public void testSupportedCommandProvider() {
 		SupportedCommandCollection collection = injector.getInstance(SupportedCommandCollection.class);
 		assert collection.size() > 0;
 		assert collection.get(CommandHelp.NAME) instanceof CommandHelp;
 	}
 }
