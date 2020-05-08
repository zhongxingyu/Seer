 /**
  * Copyright (C) 2012 philnate (http://github.com/philnate/textmanager)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.philnate.textmanager.web.config.datastore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.apache.commons.io.FileUtils;
 import org.mongodb.MongoClients;
 import org.mongodb.MongoDatabase;
 import org.mongodb.connection.ServerAddress;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.github.cherimojava.data.mongo.entity.Entity;
 import com.github.cherimojava.data.mongo.entity.EntityFactory;
 import com.github.cherimojava.data.mongo.entity.EntityUtils;
 import com.google.common.base.Charsets;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 
 import de.flapdoodle.embed.mongo.Command;
 import de.flapdoodle.embed.mongo.MongodExecutable;
 import de.flapdoodle.embed.mongo.MongodStarter;
 import de.flapdoodle.embed.mongo.config.ArtifactStoreBuilder;
 import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
 import de.flapdoodle.embed.mongo.config.IMongodConfig;
 import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
 import de.flapdoodle.embed.mongo.config.Net;
 import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
 import de.flapdoodle.embed.mongo.config.Storage;
 import de.flapdoodle.embed.mongo.config.Timeout;
 import de.flapdoodle.embed.mongo.distribution.Version;
 import de.flapdoodle.embed.process.config.IRuntimeConfig;
 import de.flapdoodle.embed.process.config.io.ProcessOutput;
 import de.flapdoodle.embed.process.extract.UserTempNaming;
 import de.flapdoodle.embed.process.io.IStreamProcessor;
 import de.flapdoodle.embed.process.io.Processors;
 import de.flapdoodle.embed.process.io.directories.FixedPath;
 import de.flapdoodle.embed.process.runtime.Network;
 import me.philnate.textmanager.web.entities.Setting;
 
 import static java.lang.String.format;
 
 /**
  * MongoDB spring configuration
  *
  * @author philnate
  *
  */
 @Configuration
 public class cfgMongo {
 
 	@Autowired
 	String mongoDBName;
 
 	@Autowired
 	File storagePath;
 
 	@Autowired
 	File logPath;
 
	@Autowired
	boolean production;

 	private MongodExecutable exe;
 
 	File mongoStoragePath() {
 		return new File(storagePath, "mongo");
 	}
 
 	@Bean
 	public File logPath() {
 		return new File("./log");
 	}
 
 	@Bean
 	Integer mongoPort() {
 		// try {
 		// return Network.getFreeServerPort();
 		// } catch (IOException e) {
 		return 27017;
 		// }
 	}
 
 	@Bean
 	Version version() {
 		return Version.V2_4_6;
 	}
 
 	@Bean
 	EntityFactory factory() {
 		return new EntityFactory(mongoDatabase());
 	}
 
 	@Bean
 	MongoDatabase mongoDatabase() {
 		return MongoClients.create(new ServerAddress("localhost", mongoPort())).getDatabase(mongoDBName);
 	}
 
 	private IMongodConfig mongodConfig() throws IOException {
 		return new MongodConfigBuilder().version(version()).net(new Net(mongoPort(), Network.localhostIsIPv6())).replication(
 				new Storage(mongoStoragePath().toString(), null, 0)).timeout(new Timeout()).build();
 	}
 
 	private IRuntimeConfig runtimeConfig() {
 		FixedPath path = new FixedPath("bin/");
 		IStreamProcessor mongodOutput;
 		IStreamProcessor mongodError;
 		IStreamProcessor commandsOutput;
 		try {
 			mongodOutput = Processors.named("[mongod>]", new FileStreamProcessor(new File(logPath, "mongo.log")));
 
 			mongodError = new FileStreamProcessor(new File(logPath, "mongo-err.log"));
 			commandsOutput = Processors.named("[mongod>]", new FileStreamProcessor(new File(logPath, "mongo.log")));
 		} catch (FileNotFoundException e) {
 			throw Throwables.propagate(e);
 		}
 		return new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(
 				new ProcessOutput(mongodOutput, mongodError, commandsOutput)).artifactStore(
 				new ArtifactStoreBuilder().executableNaming(new UserTempNaming()).tempDir(path).download(
 						new DownloadConfigBuilder().defaultsForCommand(Command.MongoD).artifactStorePath(path))).build();
 	}
 
 	@PostConstruct
 	public void setup() {
 		try {
 			MongodStarter runtime = MongodStarter.getInstance(runtimeConfig());
 			exe = runtime.prepare(mongodConfig());
 			exe.start();
 			// TODO we need some check if init happened already, right now simply create it always
			if (production) {
				// stuff which needs to be done only in production
				imprt();
			}
 		} catch (Exception e) {
 			throw Throwables.propagate(e);
 		}
 	}
 
 	@PreDestroy
 	public void teardown() {
 		exe.stop();
 	}
 
 	/**
 	 * import entities which need to be there on first startup
 	 *
 	 * @throws IOException
 	 */
 	private void imprt() throws IOException {
 		EntityFactory factory = factory();
 		List<Class<? extends Entity>> imports = Lists.newArrayList();
 		imports.add(Setting.class);
 
 		for (Class<? extends Entity> imprt : imports) {
 			URL url = Thread.currentThread().getContextClassLoader().getResource(
 					format("%s.import", EntityUtils.getCollectionName(imprt)));
 			File file = new File(url.getPath());
			List<String> entities = FileUtils.readLines(file, Charsets.UTF_8.name());
 			for (String entity : entities) {
 				factory.fromJson(imprt, entity).save();
 			}
 		}
 	}
 
 	public class FileStreamProcessor implements IStreamProcessor {
 
 		private FileOutputStream outputStream;
 
 		public FileStreamProcessor(File file) throws FileNotFoundException {
 			file.getParentFile().mkdirs();
 			outputStream = new FileOutputStream(file);
 		}
 
 		@Override
 		public void process(String block) {
 			try {
 				outputStream.write(block.getBytes());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onProcessed() {
 			try {
 				outputStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
