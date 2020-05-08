 package org.vsegda.servlet;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import org.vsegda.dao.DataStreamDAO;
 import org.vsegda.data.DataArchive;
 import org.vsegda.data.DataItem;
 import org.vsegda.data.DataStream;
 import org.vsegda.factory.PM;
 import org.vsegda.shared.DataStreamMode;
 import org.vsegda.util.TimeUtil;
 
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Roman Elizarov
  */
 public class DataArchiveTaskServlet extends HttpServlet {
     private static final Logger log = Logger.getLogger(DataArchiveTaskServlet.class.getName());
 
     public static void enqueueTask(long streamId, int seq) {
         seq %= 2; // only 0 and 1
         log.info("Enqueueing data archive task for id=" + streamId + ", seq=" + seq);
         Queue queue = QueueFactory.getQueue("archiveTaskQueue");
         try {
             queue.add(TaskOptions.Builder
                     .withUrl("/task/dataArchive")
                    .taskName("id=" + streamId + ",seq=" + seq)
                     .param("id", String.valueOf(streamId))
                     .param("seq", String.valueOf(seq)));
         } catch (TaskAlreadyExistsException e) {
             log.info("Task already exists");
         }
     }
 
     @SuppressWarnings({"unchecked"})
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         long streamId = Long.parseLong(req.getParameter("id"));
         int seq = Integer.parseInt(req.getParameter("seq"));
 
         log.info("Archiving data for streamId=" + streamId);
         DataStream stream = PM.instance().getObjectById(DataStream.class, streamId);
         if (stream.getMode() == DataStreamMode.LAST) {
             log.warning("Stream mode is " + stream.getMode() + ", ignoring task");
             return;
         }
         DataItem firstItem = DataStreamDAO.getOrFindFirstItem(stream);
         if (firstItem == null || firstItem.isRecent()) {
             log.info("First stream item is recent or missing: " + firstItem);
             return;
         }
         // Archive up to next midnight
         long limit = TimeUtil.getArchiveLimit(firstItem.getTimeMillis());
         Query query = PM.instance().newQuery(DataItem.class);
         query.setOrdering("timeMillis asc");
         query.declareParameters("long id, long limit");
         query.setFilter("streamId == id && timeMillis <= limit");
         List<DataItem> items = new ArrayList<DataItem>((Collection<DataItem>) query.execute(streamId, limit));
 
         // never remove or archive the last item !!!
         if (!items.isEmpty() && items.get(items.size() - 1).getKey().equals(stream.getLastItemKey()))
             items.remove(items.size() - 1);
         if (items.isEmpty()) {
             log.warning("No items to archive");
             return;
         }
 
         if (stream.getMode() == DataStreamMode.RECENT) {
             log.info("Only recent items are kept, removing old ones");
             PM.instance().deletePersistentAll(items);
             return;
         }
         // Actually archive
         DataArchive archive = new DataArchive(streamId);
         archive.setItems(items);
         log.info("Creating archive " + archive);
         PM.instance().makePersistent(archive);
         stream.setFirstItemKey(null);
         PM.instance().deletePersistentAll(items);
         // create next task to check this stream
         enqueueTask(streamId, seq + 1);
     }
 }
