 package org.atlasapi.feeds.radioplayer.upload;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 
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
 	
 	private final RadioPlayerFTPCredentials credentials;
 	private final String ftpPath;
 	
 	private final AdapterLog log;
 	private final KnownTypeQueryExecutor queryExecutor;
 	
 	private final RadioPlayerXMLValidator validator;
 
 	public RadioPlayerFileUploader(RadioPlayerFTPCredentials credentials, String ftpPath, KnownTypeQueryExecutor queryExecutor, AdapterLog log, RadioPlayerXMLValidator validator) {
 		this.credentials = credentials;
 		this.ftpPath = ftpPath;
 		this.queryExecutor = queryExecutor;
 		this.log = log;
 		this.validator = validator;
 	}
 	
 	@Override
 	public void run() {
 		log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("RadioPlayerFileUploader started"));
 		
 		try {
 			checkNotNull(Strings.emptyToNull(credentials.server()), "No Radioplayer FTP Host, set rp.ftp.host");
 			checkNotNull(Strings.emptyToNull(credentials.username()), "No Radioplayer FTP Username, set rp.ftp.username");
 			checkNotNull(Strings.emptyToNull(credentials.password()), "No Radioplayer FTP Password, set rp.ftp.password");
 			
 			checkNotNull(ftpPath, "No Radioplayer FTP Path, set rp.ftp.path");
 
 			FTPClient client = new FTPClient();
 		
 			client.connect(credentials.server(), credentials.port());
 			
 			client.enterLocalPassiveMode();
 			
 			if (!client.login(credentials.username(), credentials.password())) {
                 throw new RuntimeException("Unable to connect to " + credentials.server() + " with username: " + credentials.username() + " and password...");
             }
 			
             if (!ftpPath.isEmpty() && !client.changeWorkingDirectory(ftpPath)) {
                 throw new RuntimeException("Unable to change working directory to " + ftpPath);
             }
             
             int count = 0;
            DateTime day = new DateTime(DateTimeZones.UTC).minusDays(2);
             for (int i = 0; i < 10; i++, day = day.plusDays(1)) {
             	for (RadioPlayerService service : RadioPlayerServices.services) {
             		for(RadioPlayerFeedType type : ImmutableSet.of(RadioPlayerFeedType.PI)) {
             			try {
 							ByteArrayOutputStream baos = new ByteArrayOutputStream();
 							type.compileFeedFor(day, service, queryExecutor, baos);
 							
 							if(validator == null || validator.validate(new ByteArrayInputStream(baos.toByteArray()))){
 							
 								String filename = filenameFrom(day, service, type);
 								
 								OutputStream toServer = client.storeFileStream(filename);
 								toServer.write(baos.toByteArray());
 								Closeables.closeQuietly(toServer);
 								
 								if(!client.completePendingCommand()) {
 									throw new Exception("Failed to complete pending command");
 								}
 								
 								count++;
 								
 							}
 						} catch (Exception e) {
 							String desc = String.format("Exception creating %s feed for service %s for %s", type, service.getName(), day.toString("dd/MM/yyyy"));
 							log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription(desc ));
 						} 
             		}
             	}
 			}
             log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("RadioPlayerFileUploader finished: "+count+" files uploaded"));
 		} catch (Exception e) {
 			log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withDescription("Exception running RadioPlayerFileUploader"));
 		}
 	}
 
 	private String filenameFrom(DateTime today, RadioPlayerService service, RadioPlayerFeedType type) {
 		String date = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC).print(today);
 		return String.format("%s_%s_%s.xml", date, service.getRadioplayerId(), type.toString());
 	}
 
 }
