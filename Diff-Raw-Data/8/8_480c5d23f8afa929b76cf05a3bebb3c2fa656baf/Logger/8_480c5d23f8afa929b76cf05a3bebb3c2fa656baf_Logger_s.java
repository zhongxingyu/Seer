 package nz.ac.vuw.ecs.rprofs.server;
 
 import com.google.common.annotations.VisibleForTesting;
 import nz.ac.vuw.ecs.rprofs.server.context.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
 import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
 import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
 import nz.ac.vuw.ecs.rprofs.server.db.Database;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.Event;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.annotation.Nullable;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 
 @SuppressWarnings("serial")
 @Configurable(autowire = Autowire.BY_TYPE)
 public class Logger extends HttpServlet {
 
 	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);
 
 	@VisibleForTesting
 	@Autowired
 	DatasetManager datasets;
 
 	@VisibleForTesting
 	@Autowired
 	EventManager events;
 
 	@VisibleForTesting
 	@Autowired
 	Context context;
 
 	@VisibleForTesting
 	@Autowired
 	Database database;
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		Dataset current = datasets.findDataset(req.getHeader("Dataset"));
 		context.setDataset(current);
 
 		parseEvents(current, req.getContentLength(), req.getInputStream());
 
 		resp.setStatus(201);
 		context.clear();
 	}
 
 	protected void parseEvents(Dataset ds, int length, InputStream in) throws IOException {
 		DataInputStream dis = new DataInputStream(in);
 
 		//		#define MAX_PARAMETERS 16
 		//		struct EventRecord {
 		//			long int id;
 		//			long int thread;
 		//			char message[255];
 		//			int cnum;
 		//			int mnum;
 		//			int len;
 		//			long int params[MAX_PARAMETERS];
 		//		}
 		final int MAX_PARAMETERS = 16;
 		final int RECORD_LENGTH = 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8;
 
 		EventCreator<?> b = events.createEvent();
 
 		log.debug("storing {} events", length / RECORD_LENGTH);
 		long started = Calendar.getInstance().getTime().getTime();
 
 		for (int i = 0; i < length / RECORD_LENGTH; i++) {
 			long id = dis.readLong();
 			b.setId(EventId.create(ds, id));
 			b.setThread(parseObjectId(ds, dis.readLong()));
 
 			int type = dis.readInt();
 
 			b.setEvent(type);
 
 			int cnum = dis.readInt();
 			int mnum = dis.readInt();
 
 			if ((type & Event.HAS_CLASS) == type) {
 				ClazzId clazzId = ClazzId.create(ds, cnum);
 				b.setClazz(clazzId);
 
 				if ((type & Event.METHODS) == type) {
 					b.setMethod(MethodId.create(ds, clazzId, (short) mnum));
 				} else if ((type & Event.FIELDS) == type) {
 					b.setField(FieldId.create(ds, clazzId, (short) mnum));
 				}
 			}
 
 			int len = dis.readInt();
 
 			if (len > MAX_PARAMETERS) {
 				log.warn("warning: {} is greater than MAX_PARAMETERS\n", len);
 				len = MAX_PARAMETERS;
 			}
 
 			for (int j = 0; j < MAX_PARAMETERS; j++) {
 				long arg = dis.readLong();
 				if (j < len) {
 					b.addArg(parseObjectId(ds, arg));
 				}
 			}
 
 			b.store();
 		}
 
 		long flushing = Calendar.getInstance().getTime().getTime();
 		log.debug("finshed storing in {}ms, flushing to disk", flushing - started);
 		database.flush();
 
		log.debug("waiting 30 seconds to simulate a slow store...");

		try {
			Thread.sleep(30000l);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

 		long finished = Calendar.getInstance().getTime().getTime();
 		log.debug("{} events stored successfully in {}ms",
 				length / RECORD_LENGTH, finished - started);
 	}
 
 	@Nullable
 	InstanceId parseObjectId(Dataset ds, long id) {
 		if (id == 0) return null;
 		return InstanceId.create(ds, id);
 	}
 }
