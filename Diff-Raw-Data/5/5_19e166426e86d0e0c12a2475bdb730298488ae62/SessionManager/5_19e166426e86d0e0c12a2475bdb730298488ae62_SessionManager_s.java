 /*
  * SessionManager.java
  *
  * Copyright (C) 2009 Nicola Roberto Viganò
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * SessionManager.java
  *
  * Created on 26 gennaio 2007, 21.11
  */
 
 package gestionecassa.server;
 
 import gestionecassa.exceptions.NotExistingSessionException;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import org.apache.log4j.Logger;
 
 /**
  * This class is a specialized sessions manager. It's responsible for sessions'
  * life, and for intelligent using-reusing of session ids.
  *
  * @author ben
  */
 class SessionManager {
 
     /**
      * List of the opened sessions
      */
     private Map<Integer, SessionRecord> sessions;
 
     /**
      * List of session ids we could reuse.
      */
     private Queue<Integer> recycleIds;
 
     /**
      * clock ticks, onto which we make actions when they reach predefined values
      */
     private int numTick;
 
     /**
      * Semaphore for the list of opened sessions
      *
      * NOTE: it's randozed to avoid the JVM to make optimizations, which could
      * lead the threads to share the same semaphore.
      */
     private static final String sessionListSemaphore =
             new String("SessionsSemaphore" + System.currentTimeMillis());
 
     /**
      * Logger that takes account of logging messages.
      */
     private Logger logger;
 
     /**
      * Max value of numTick.
      */
     final static int toc = 10;
     
     /**
      * Creates a new instance of SessionManager
      */
     SessionManager(Logger logger) {
         sessions = new TreeMap<Integer, SessionRecord>();
         recycleIds = new PriorityQueue<Integer>();
         this.logger = logger;
         this.numTick = 0;
     }
 
     /**
      * Updates time and acts on predefined intervals
      */
     void tick() {
         if (numTick < SessionManager.toc) {
             numTick++;
         } else {
             numTick = 0;
             updateTimeElapsed();
         }
     }
     
     /** This method cyclicly updates passing of time, since the last
      * "keepAlive" call from the client, and determinates the
      * timeout, when necessary. Timeout implies that the connection
      * is no more useful, and it is erased. (set to -1)
      */
     private void updateTimeElapsed() {
 //        logger.debug("faccio il check delle sessioni attive.");
         synchronized (sessionListSemaphore) {
            for (SessionRecord elem : sessions.values()) {
                 /*se supera il timeout distrugge il thread e rimuove la sessione*/
 //                logger.debug("elemento con session id: "+
 //                        elem.sessionId + "ed elapsedtime: "+elem.timeElapsed);
                 if (elem.timeElapsed++ > 5) {
 //                    logger.debug("eliminato sess con id: " + elem.sessionId);
                     invalidateSession(elem);
                 }
             }
             cleanupSessions();
         }
 //        logger.debug("finito il check delle sessioni attive.");
     }
 
     /**
      * Removes invalidated sessions on the tail of the sessions list
      */
     private void cleanupSessions() {
         synchronized (sessionListSemaphore) {
             for (int i = (sessions.size()-1);
                 i >= 0 && sessions.get(i) != null && sessions.get(i).sessionId == -1;
                 i--)
             {
                 sessions.remove(i);
                 recycleIds.remove(i);
             }
         }
     }
 
     /**
      * This method destoryes a record in the sessions' list.
      * @param   session     the session to destroy.
      */
     private void invalidateSession(SessionRecord session) {
         synchronized (sessionListSemaphore) {
             recycleIds.add(session.sessionId);
             
             session.sessionId = -1;
             session.user = null;
             session.serviceThread.stopThread();
         }
         logger.debug("Invalidata la sessione scaduta o terminata");
     }
 
     /**
      * Safe session retriver.
      * If the session is not found, or is invalidated, it gracefully fails
      * throwing an exception.
      *
      * @param sessionID The id of the session needed
      * @return The <code>SessionRecord</code> corresponding to the id
      * @throws NotExistingSessionException In case the id is no more associated to any session
      */
     private SessionRecord getSession(int sessionID) throws NotExistingSessionException {
         synchronized (sessionListSemaphore) {
             SessionRecord record = sessions.get(sessionID);
             if (record != null && record.sessionId >= 0) {
                 return record;
             } else {
                 throw new NotExistingSessionException("Nessuna sessione con" +
                         " id: " + sessionID);
             }
         }
     }
 
     /**
      * Returns a session of the same user if present, otherwise throws an exception
      * @param record
      * @return
      * @throws NotExistingSessionException
      */
     private SessionRecord getSession(SessionRecord record) throws NotExistingSessionException {
         synchronized (sessionListSemaphore) {
             for (SessionRecord tempRecord : sessions.values()) {
                 if (tempRecord.equals(record)) {
                     return tempRecord;
                 }
             }
             /* if not found */
             throw new NotExistingSessionException("Nessuna sessione come " +
                         "quella indicata");
         }
     }
 
     /**
      * This method finds the first free session id in sessions list, and then
      * adds the given session, assigning it that session id.
      *
      * It assumes there are no duplicates, so you need to externally verify this
      * session is unique.
      *
      * @param newRecord  the record to verify.
      * @return new sessionId.
      */
     int newSession(SessionRecord newRecord) {
         /*vedo se esistono posti intermedi liberi.
           infatti se un thread implode lascia uno spazio libero.*/
         int id = 0;
         synchronized (sessionListSemaphore) {
             if (recycleIds.size() > 0) {
                 id = recycleIds.poll();
             } else {
                 id = sessions.size();
             }
 
             newRecord.sessionId = id;
             sessions.put(id, newRecord);
         }
         return id;
     }
 
     /**
      * Verifies whether an already existing session is in the sessions list
      * @param record The session to verify
      * @return <code>true</code> if the session is already in the sessions list
      */
     boolean isSessionAlreadyOpen(SessionRecord record) {
         synchronized (sessionListSemaphore) {
             return sessions.containsValue(record);
         }
     }
 
     /**
      * Method that tell's the server that the client still
      * lives and is connected.
      * @param sessionID
      * @throws NotExistingSessionException
      */
     void keepAlive(int sessionID) throws NotExistingSessionException {
         synchronized (sessionListSemaphore) {
             getSession(sessionID).timeElapsed = 0;
         }
     }
 
     /**
      * Method that tell's to the thread to shut down.
      * @param sessionID The session id, of the session to invalidate.
      * @throws NotExistingSessionException In case the id is no more associated to any session
      */
     void closeService(int sessionID) throws NotExistingSessionException {
         invalidateSession(getSession(sessionID));
     }
 
     /**
      * Kicks off, if necessary, the given session.
      * This helps in case we trust our clients that reconnect when we expect
      * them be still alive.
      *
      * @param record Session to kick off
      */
     void kickOff(SessionRecord record) {
         try {
             synchronized (sessionListSemaphore) {
                 if (sessions.containsValue(record)) {
                         invalidateSession(getSession(record));
                 }
             }
         } catch (NotExistingSessionException ex) {
             logger.debug("no session found, but expected", ex);
         }
     }
 
     /**
      * Closes all the open sessions to terminate the service
      */
     void temrinate() {
         synchronized (sessionListSemaphore) {
             for (Integer sessID : sessions.keySet()) {
                 try {
                     if ( !recycleIds.contains(sessID)) {
                         closeService(sessID);
                     }
                 } catch (NotExistingSessionException ex) {
                     logger.warn("While closing, found phantom sessions!", ex);
                 }
             }
             cleanupSessions();
         }
     }
 
     /**
      * Getter used in testing
      * @return a copy of the map sessions
      */
     Map<Integer, SessionRecord> getSessions() {
         return new TreeMap<Integer, SessionRecord>(sessions);
     }
 
     /**
      * Getter used in testing
      * @return a copy of the queue recycleIds
      */
     Queue<Integer> getRecycleIds() {
         return new PriorityQueue<Integer>(recycleIds);
     }
 }
