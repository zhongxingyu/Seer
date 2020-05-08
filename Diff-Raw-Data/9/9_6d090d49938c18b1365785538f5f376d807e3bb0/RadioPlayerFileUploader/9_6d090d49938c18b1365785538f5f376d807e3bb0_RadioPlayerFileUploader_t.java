 package org.atlasapi.feeds.radioplayer.upload;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.atlasapi.feeds.radioplayer.RadioPlayerFeedType;
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.io.Closeables;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class RadioPlayerFileUploader implements Runnable {
 	
 	private final String ftpHost;
 	private final Integer ftpPort;
 	private final String ftpUsername;
 	private final String ftpPassword;
 	private final String ftpPath;
 	
 	private final AdapterLog log;
 	private final KnownTypeQueryExecutor queryExecutor;
 
 	public RadioPlayerFileUploader(String ftpHost, Integer ftpPort, String ftpUsername, String ftpPassword, String ftpPath, KnownTypeQueryExecutor queryExecutor, AdapterLog log) {
 		this.ftpHost = ftpHost;
 		this.ftpPort = ftpPort;
 		this.ftpUsername = ftpUsername;
 		this.ftpPassword = ftpPassword;
 		this.ftpPath = ftpPath;
 		this.queryExecutor = queryExecutor;
 		this.log = log;
 	}
 
 	@Override
 	public void run() {
 		log.record(new AdapterLogEntry(Severity.INFO).withSource(RadioPlayerFileUploader.class).withDescription("RadioPlayerFileUploader started"));
 		
 		try {
 			checkNotNull(Strings.emptyToNull(ftpHost), "No Radioplayer FTP Host, set rp.ftp.host");
 			checkNotNull(ftpPort, "No Radioplayer FTP Port, set rp.ftp.port");
 			checkNotNull(Strings.emptyToNull(ftpUsername), "No Radioplayer FTP Username, set rp.ftp.username");
 			checkNotNull(Strings.emptyToNull(ftpPassword), "No Radioplayer FTP Password, set rp.ftp.password");
 			checkNotNull(ftpPath, "No Radioplayer FTP Path, set rp.ftp.path");
 
 			FTPClient client = new FTPClient();
 		
 			client.connect(ftpHost, ftpPort);
 			
 			client.enterLocalPassiveMode();
 			
 			if (!client.login(ftpUsername, ftpPassword)) {
                 throw new RuntimeException("Unable to connect to " + ftpHost + " with username: " + ftpUsername + " and password...");
             }
 			
             if (!ftpPath.isEmpty() && !client.changeWorkingDirectory(ftpPath)) {
                 throw new RuntimeException("Unable to change working directory to " + ftpPath);
             }
             
             int count = 0;
             DateTime day = new DateTime(DateTimeZones.UTC).minusDays(2);
            for (int i = 0; i < 10; i++, day = day.plusDays(1)) {
             	for (RadioPlayerService service : RadioPlayerServices.services) {
             		for(RadioPlayerFeedType type : ImmutableSet.of(RadioPlayerFeedType.PI)) {
             			ByteArrayOutputStream baos = new ByteArrayOutputStream();
             			type.compileFeedFor(day, service, queryExecutor, baos);
             			
             			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             			client.storeFile(filenameFrom(day, service, type), bais);
             			Closeables.closeQuietly(bais);
             			count++;
             		}
             	}
 			}
             log.record(new AdapterLogEntry(Severity.INFO).withSource(RadioPlayerFileUploader.class).withDescription("RadioPlayerFileUploader finished: "+count+" files uploaded"));
 		} catch (Exception e) {
 			log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withDescription("Exception running RadioPlayerFileUploader"));
 		}
 	}
 
 	private String filenameFrom(DateTime today, RadioPlayerService service, RadioPlayerFeedType type) {
 		String date = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC).print(today);
 		return String.format("%s_%s_%s.xml", date, service.getRadioplayerId(), type.toString());
 	}
 
 }
