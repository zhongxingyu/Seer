 package iotc;
 
 import iotc.common.UPnPException;
 import iotc.db.*;
 import iotc.event.DBEventListener;
 import iotc.event.DBEventListenerManager;
 import iotc.event.UPnPEventListener;
 import iotc.medium.SMediumMap;
 import iotc.test.ExpEventListener;
 import org.apache.commons.collections15.multimap.MultiHashMap;
 import org.hibernate.classic.Session;
 import org.itolab.morihit.clinkx.UPnPRemoteDevice;
 import org.itolab.morihit.clinkx.UPnPRemoteStateVariable;
 
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: atsushi-o
  * Date: 13/01/16
  * Time: 17:04
  */
 public class VariableChecker implements UPnPEventListener, DBEventListener {
     private HashMap<String, Sensor> sensor;
     private MultiHashMap<String, Term> terms;
     private Set<Integer> fireFlags;
     private ExpEventListener listener;
     private static final Logger LOG;
     private static final Pattern TERM_PATTERN;
     private static final ResourceBundle rb;
 
     static {
         LOG = Logger.getLogger(VariableChecker.class.getName());
         TERM_PATTERN = Pattern.compile("([-+]?\\d+\\.?[\\d]*) ?([=<>]) ?([-+]?\\d+\\.?[\\d]*)");
         rb = ResourceBundle.getBundle("iotc.i18n.VariableChecker");
     }
 
     public VariableChecker() {
         sensor = new HashMap();
         terms = new MultiHashMap();
         fireFlags = new HashSet();
         DBEventListenerManager.getInstance().addListener(this, "Term");
     }
 
     private String generateKey(UPnPRemoteStateVariable upprsv) {
         UPnPRemoteDevice upprd = upprsv.getRemoteService().getRemoteDevice();
         String key = upprd.getUDN()+upprsv.getName();
         return key;
     }
 
     @Override
     public void onUpdateValue(UPnPRemoteStateVariable upprsv) {
         String key = generateKey(upprsv);
         Sensor s;
 
         s = sensor.get(key);
         if (s == null) {
             try {
                 s = EntityMapUtil.upnpToDB(upprsv);
                 Session ses = HibernateUtil.getSessionFactory().openSession();
                 ses.beginTransaction();
                 s = (Sensor)ses.load(iotc.db.Sensor.class, s.getId());
                 Set<Term> t = s.getTerms();
                 sensor.put(key, s);
                 terms.putAll(key, t);
                 ses.close();
             } catch (UPnPException ex) {
                 LOG.log(Level.FINE, "Ignore UPnP value change", ex);
                 return;
             }
         }
 
         if (!terms.containsKey(key)) return;
         for (Term t : terms.get(key)) {
             //TODO: 複数の変数に対応できるように要修正
             String ts = t.getTerm();
             LOG.log(Level.FINE, "Check term: {0}", ts);
            ts = ts.replaceAll("([^: <>=.\\d][^: <>=.]+)(::[^: <>=.]+)*", String.valueOf(upprsv.getValue()));
             Matcher m = TERM_PATTERN.matcher(ts);
             if (m.matches()) {
                 boolean b = false;
                 switch (m.group(2)) {
                     case "=":
                         b = (Double.valueOf(m.group(1)) == Double.valueOf(m.group(3)));
                         break;
                     case "<":
                         b = (Double.valueOf(m.group(1)) < Double.valueOf(m.group(3)));
                         break;
                     case ">":
                         b = (Double.valueOf(m.group(1)) > Double.valueOf(m.group(3)));
                         break;
                 }
                 LOG.log(Level.FINE, "{0} = {1}", new Object[]{ts, b});
                 if (b) {
                     if (fireFlags.contains(t.getId())) {
                        LOG.log(Level.INFO, "This term was already fired.");
                         continue;
                     }
                     // Execute action
                     Log l = new Log();
 
                     SMediumMap.get(iotc.medium.Twitter.class).send(null, t.getUser(), MessageFormat.format(rb.getString("NotificationMessage"), ts));
                     if (listener != null) {
                         listener.onCommandComplete(String.valueOf(System.currentTimeMillis()));
                     }
                     fireFlags.add(t.getId());
                 } else {
                     fireFlags.remove(t.getId());
                 }
             }
         }
     }
 
     @Override public void onDetectNewDevice(UPnPRemoteDevice device) {}
     @Override public void onDetectKnownDevice(Device device) {}
     @Override public void onFailDevice(Device device) {}
 
     private String generateKey(Term t) {
         String key = null;
         for (Sensor st : (Set<Sensor>)t.getSensors()) {
             try {
                 key = generateKey(EntityMapUtil.dbToUPnP(st));
             } catch (UPnPException e) {
                 LOG.log(Level.WARNING, "Failed to generate key: no such UPnP device or variable");
                 return null;
             }
         }
         return key;
     }
 
     @Override
     public void onCreate(String entityName, Object entity) {
        LOG.log(Level.INFO, "New term record: {0}", entity);
         Term t = (Term)entity;
         String key = generateKey(t);
         if (key != null) terms.put(key, t);
     }
 
     @Override
     public void onDelete(String entityName, Object entity) {
         Term t = (Term)entity;
         String key = generateKey(t);
         if (key != null) {
             for (Iterator<Term> it = terms.iterator(key); it.hasNext();) {
                 Term ts = it.next();
                 if (ts.getId() == t.getId()) {
                     it.remove();
                     break;
                 }
             }
         }
     }
 
     @Override
     public void onUpdate(String entityName, Object entity) {
         Term t = (Term)entity;
         String key = generateKey(t);
         if (key != null) {
             for (Iterator<Term> it = terms.iterator(key); it.hasNext();) {
                 Term ts = it.next();
                 if (ts.getId() == t.getId()) {
                     it.remove();
                     break;
                 }
             }
             terms.put(key, t);
         }
     }
 
     public void addExpEventListener(ExpEventListener l) {
         this.listener = l;
     }
 }
