 package dk.statsbiblioteket.mediaplatform.ingest.channelarchivingrequester.web;
 
 import dk.statsbiblioteket.mediaplatform.ingest.model.ChannelArchiveRequest;
 import dk.statsbiblioteket.mediaplatform.ingest.model.WeekdayCoverage;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.ChannelArchiveRequestDAO;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.ChannelArchiveRequestDAOIF;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.ChannelArchiveRequestService;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.ChannelArchiveRequestServiceIF;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.ServiceException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  */
 public class ChannelArchiveRequestCRUDServlet extends HttpServlet {
 
     public static final String FROM_DATE = "fromDate";
     public static final String TO_DATE = "toDate";
     public static final String FROM_TIME_HOURS = "fromTimeHours";
     public static final String TO_TIME_HOURS = "toTimeHours";
     public static final String FROM_TIME_MINUTES = "fromTimeMinutes";
     public static final String TO_TIME_MINUTES = "toTimeMinutes";
     public static final String CHANNEL = "channel";
     public static final String COVERAGE = "coverage";
     public static final String SUBMIT_ACTION = "action";
     public static final String Id = "ID";
 
     public static final String CREATE = "create";
     public static final String UPDATE = "update";
     public static final String DELETE = "delete";
 
     public static final SimpleDateFormat JAVA_DATE_FORMAT =  new SimpleDateFormat("yyyy-MM-dd");
 
     private static ChannelArchiveRequestServiceIF service = null;
 
     @Override
     public void init() throws ServletException {
         super.init();
         service = new ChannelArchiveRequestService();
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String action = req.getParameter(SUBMIT_ACTION);
         String channel = req.getParameter(CHANNEL);
         if (channel == null || channel.trim().length() == 0) {
             req.setAttribute("error", "The SB Channel Name must not be empty.");
             doForward(req, resp);
             return;
         }
         String coverageS = req.getParameter(COVERAGE);
         String fromTimeHours = req.getParameter(FROM_TIME_HOURS);
         String fromTimeMinutes = req.getParameter(FROM_TIME_MINUTES);
         String toTimeHours = req.getParameter(TO_TIME_HOURS);
         String toTimeMinutes = req.getParameter(TO_TIME_MINUTES);
         String fromDateS = req.getParameter(FROM_DATE);
         if (fromDateS == null || fromDateS.equals("")) {
             long nowL = (new Date()).getTime();
             long fourWeeksAgoL = nowL - 28*24*3600L*1000L;
             Date fromDate = new Date(fourWeeksAgoL);
             fromDateS = JAVA_DATE_FORMAT.format(fromDate);
         }
         String toDateS = req.getParameter(TO_DATE);
         if (toDateS == null || toDateS.equals("")) {
             toDateS = "3000-01-01";
         }
         //For a create action, the id is null
         String idS = req.getParameter(Id);
 
         ChannelArchiveRequest caRequest = new ChannelArchiveRequest();
         caRequest.setsBChannelId(channel);
         try {
             Date fromDate = JAVA_DATE_FORMAT.parse(fromDateS);
             fromDate.setHours(0);
             fromDate.setMinutes(0);
             fromDate.setSeconds(0);
             Date toDate = JAVA_DATE_FORMAT.parse(toDateS);
             toDate.setHours(0);
             toDate.setMinutes(0);
             toDate.setSeconds(0);
             if (fromDate.after(toDate)) {
                 req.setAttribute("error", "fromDate " + fromDateS + " must not be after toDate " + toDateS);
                 doForward(req, resp);
                 return;
             }
             caRequest.setFromDate(fromDate);
             caRequest.setToDate(toDate);
         } catch (ParseException e) {
             req.setAttribute("error", e);
             doForward(req, resp);
             return;
         }
         try {
             Date fromTime = new Date(0);
             fromTime.setHours(Integer.parseInt(fromTimeHours));
             fromTime.setMinutes(Integer.parseInt(fromTimeMinutes));
             Date toTime = new Date(0);
             toTime.setHours(Integer.parseInt(toTimeHours));
             toTime.setMinutes(Integer.parseInt(toTimeMinutes));
             if (toTime.getHours() == 0 && toTime.getMinutes() == 0) {
                 toTime.setDate(2);
                 toTime.setMonth(0);
                toTime.setYear(0);
             }
             if (fromTime.after(toTime)) {
                 req.setAttribute("error", "fromTime "
                         + fromTimeHours + ":" + fromTimeMinutes + "  is after toTime " +
                        toTimeHours + ":" + toTimeMinutes + " (" + fromTime + "," + toTime + ")" );
                 doForward(req, resp);
                 return;
             }
             caRequest.setToTime(toTime);
             caRequest.setFromTime(fromTime);
         } catch (NumberFormatException e) {
             req.setAttribute("error", e);
             doForward(req, resp);
             return;
         }
         caRequest.setWeekdayCoverage(WeekdayCoverage.valueOf(coverageS));
         if (action.equals(CREATE)) {
             try {
                 service.insert(caRequest);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         } else if (action.equals(UPDATE)) {
             caRequest.setId(Long.parseLong(idS));
             try {
                 service.update(caRequest);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         } else if (action.equals(DELETE)) {
             caRequest.setId(Long.parseLong(idS));
             try {
                 service.delete(caRequest);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         }
         doForward(req, resp);
     }
 
     private void doForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         req.setAttribute("page_attr", "archiving_requests.jsp");
         req.getSession().getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
     }
 
 
 }
