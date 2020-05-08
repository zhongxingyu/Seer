 package edu.mit.cci.turksnet.plugins;
 
 
 import edu.mit.cci.snatools.util.jung.DefaultJungEdge;
 import edu.mit.cci.snatools.util.jung.DefaultJungGraph;
 import edu.mit.cci.snatools.util.jung.DefaultJungNode;
 import edu.mit.cci.turksnet.Experiment;
 import edu.mit.cci.turksnet.Node;
 import edu.mit.cci.turksnet.SessionLog;
 import edu.mit.cci.turksnet.Session_;
 import edu.mit.cci.turksnet.Worker;
 import edu.mit.cci.turksnet.util.GraphGenerator;
 import edu.mit.cci.turksnet.util.NodeStatus;
 import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.graph.util.Pair;
 import org.apache.commons.collections15.Factory;
 import org.apache.log4j.Logger;
 import org.apache.sling.commons.json.JSONArray;
 import org.apache.sling.commons.json.JSONException;
 import org.apache.sling.commons.json.JSONObject;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.InputStream;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: jintrone
  * Date: 4/28/11
  * Time: 7:45 AM
  */
 
 
 //an experiment maintains settings
 //a session is run within an expriment.  it is a case.
 
 public class LoomStandalonePlugin implements Plugin {
 
     public static final String PROP_STORY = "story";
     private static final String PROP_PRIVATE_TILES = "private_tile_count";
     private static final String PROP_NET_DEGREE = "network_degree";
     private static final String PROP_DEBUG_WAIT = "debug_wait";
 
 
     private static Logger logger = Logger.getLogger(LoomStandalonePlugin.class);
 
 
     public Session_ createSession(Experiment experiment, List<Worker> workers, boolean force) throws SessionCreationException {
         logger.info("Creating session with " + workers.size() + " workers");
         //@TODO add logic for checking worker history and making sure there is an available session
         Session_ session = setupSession(experiment);
         logger.info("Created session " + session.getId());
         try {
             createGraph(session, workers.size());
         } catch (Exception e) {
             e.printStackTrace();
             throw new SessionCreationException("Error generating graph");
         }
         initNodes(session);
 
         for (int i = 0; i < session.getAvailableNodes().size(); i++) {
             session.assignNodeToTurker(workers.get(i));
         }
         logger.debug("Attempting to flush new session " + session.getId());
         session.flush();
 
         //session.merge();
         return session;
     }
 
     private Session_ setupSession(Experiment e) {
         Session_ session = new Session_(e.getId());
         session.setCreated(new Date());
         session.setIteration(-1);
         List<Session_> known = new ArrayList<Session_>();
         String nextSession = e.getNextSession();
         for (Session_ s : e.getSessions()) {
             if (s.getStatus() != Session_.Status.ABORTED) {
                 known.add(s);
             }
         }
         if (e.getSessionProps() != null) {
             try {
 
 
                 JSONArray sessions = null;
                 sessions = new JSONArray(e.getSessionProps());
                 Set<String> sessionids = new HashSet<String>();
                 for (Session_ s : known) {
                     sessionids.add(s.getProperty(PROP_SESSION_ID));
                 }
 
                 if (nextSession!=null && sessionids.contains(nextSession)) {
                     nextSession = null;
                 }
 
                 int i = 0;
                boolean flag = false;
                 for (; i < sessions.length(); i++) {
                     JSONObject props = sessions.getJSONObject(i);
                     String sessionid = props.getString(PROP_SESSION_ID);
                     if ((nextSession==null || sessionid.equals(nextSession)) && !sessionids.contains(sessionid)) {
                         session.setProperties(props.toString());
                        flag = true;
                         break;
                     }
                 }
                if (!flag) {
                     logger.error("No sessions available! (using specific session properties)");
                     return null;
                 }
 
 
             } catch (JSONException e1) {
                 e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         } else if (e.getProperty(Plugin.PROP_SESSION_COUNT) != null) {
             if (Integer.parseInt(e.getProperty(Plugin.PROP_SESSION_COUNT)) >= known.size()) {
                 logger.error("No sessions available! (using prop session count)");
                 return null;
             }
         }
 
         List<String> sessionprops = new ArrayList<String>();
         Collections.addAll(sessionprops, PROP_NODE_COUNT, PROP_STORY, PROP_ITERATION_COUNT, PROP_NET_DEGREE, PROP_GRAPH_TYPE, PROP_TURN_LENGTH_SECONDS, PROP_PRIVATE_TILES);
         for (String p : sessionprops) {
             if (session.getProperty(p) == null) {
                 session.updateProperty(p, e.getProperty(p));
             }
 
         }
         session.updateProperty(PROP_STORY, processStory(session.getProperty(PROP_STORY)));
         session.setStatus(Session_.Status.WAITING);
         session.persist();
         session.flush();
         return session;
     }
 
     @Override
     public boolean checkDone(Session_ s) {
         for (Node n : s.getAvailableNodes()) {
             if (NodeStatus.valueOf(n.getStatus()) == NodeStatus.ACCEPTING_INPUT) {
                 return false;
             }
         }
         return (s.getIteration() >= Integer.parseInt(s.getProperty(PROP_ITERATION_COUNT).toString()));
 
     }
 
     @Override
     public void processResults(Node n, String results) {
 
         logger.debug("Receiving " + results + " from " + n.getWorker().getUsername());
         Map<String, String> storymap = getStoryMap(n.getSession_().getProperty(PROP_STORY));
         n.setPublicData_(internalFormatData(storymap, results));
         n.addScore(getScore(n.getSession_().getProperty(PROP_STORY), n.getPublicData_()));
         if (n.getSession_().getExperiment().getProperty(PROP_DEBUG_WAIT) != null) {
             try {
                 Thread.sleep(Long.parseLong(n.getSession_().getExperiment().getProperty(PROP_DEBUG_WAIT)));
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
 
 
     }
 
     private String internalFormatData(Map<String, String> story, String results) {
         StringBuilder builder = new StringBuilder();
         String[] items = results.split(";");
 
         String sep = "";
         for (String i : items) {
             builder.append(sep).append(i).append("=").append(story.get(i));
             sep = "&";
         }
         String s = builder.toString();
 
         return s;
     }
 
     private static int getNewId(Set<Integer> set) {
         Random rnd = new Random();
         int n;
         do {
             n = rnd.nextInt(1000);
 
         } while (set.contains(n));
         set.add(n);
         return n;
     }
 
 
     private String processStory(String story) {
         StringBuilder builder = new StringBuilder();
         Pattern pat = Pattern.compile("(\\d+)=([\\w\\s]+)");
         String sep = "";
         Set<Integer> ids = new HashSet<Integer>();
         for (String p : story.split("&")) {
             p = p.trim();
             builder.append(sep);
             Matcher m = pat.matcher(p);
             if (m.matches()) {
                 if (!ids.contains(m.group(1))) {
                     ids.add(Integer.getInteger(m.group(1)));
                     builder.append(p);
                 } else {
                     int nid = getNewId(ids);
                     builder.append(nid).append("=").append(m.group(2));
                 }
             } else {
                 int nid = getNewId(ids);
                 builder.append(nid).append("=").append(p);
 
             }
             sep = "&";
         }
         return builder.toString();
     }
 
 
     public String configureApplicationString(String appname) throws Exception {
         InputStream stream = getClass().getResourceAsStream("/loomStandalone.fragment.html");
         String result = edu.mit.cci.turkit.util.U.slurp(stream, "UTF8");
         result = result.replace("${applicationName}", appname);
         return result;
     }
 
     @Override
     public String getQualificationApp() throws Exception {
         return configureApplicationString("Qualifications");
     }
 
     @Override
     public String getTrainingApp() throws Exception {
         return configureApplicationString("Training");
     }
 
     @Override
     public String getLoginApp() throws Exception {
         return configureApplicationString("LoginRegister");
     }
 
 
     private String extractStoryFromTraining(JSONObject obj) throws JSONException {
         StringBuilder builder = new StringBuilder();
         for (int i = 1; i <= obj.length(); i++) {
             if (i > 1) builder.append("&");
             builder.append(i).append("='").append(obj.getString(i + ""));
         }
         return builder.toString();
     }
 
 
     @Override
     public JSONObject addTrainingData(Worker w, Experiment e, Map parameterMap) throws JSONException {
 
          JSONArray trainingdata = new JSONArray(e.getTrainingProps());
          JSONObject result = null;
         int trainingItem = Integer.parseInt(((String[]) parameterMap.get("trainingitem"))[0]);
         if (trainingItem >= trainingdata.length()) {
             result = new JSONObject();
             result.put("status", "error");
             return result;
         }
           commitTrainingData(w, trainingItem,e,  parameterMap);
 
 
 
 
 
         result = trainingdata.getJSONObject(trainingItem);
         if (result.getString("type").equals("full")) {
             String story = extractStoryFromTraining(result.getJSONObject("story"));
             JSONObject obj = new JSONObject(w.getTraining());
             result = scoreTraining(obj.getString(trainingItem+""), story);
         }
         result.put("status", "ok");
         return result;
     }
 
     @Transactional
     private void commitTrainingData(Worker w, int trainingItem, Experiment e, Map parameterMap) throws JSONException {
         String current = w.getTraining();
         String incoming = ((String[]) parameterMap.get("data"))[0];
         logger.debug("Set training data:" + incoming);
         if (current == null || current.isEmpty()) {
             JSONObject obj = new JSONObject();
             obj.put(trainingItem+"",incoming);
             w.setTraining(obj.toString());
         } else {
             JSONObject obj = new JSONObject(w.getTraining());
             if (obj.has(""+trainingItem)) {
                 String existing = obj.getString(""+trainingItem);
                 obj.put(""+trainingItem,existing+"&"+incoming);
 
             } else {
                obj.put(""+trainingItem,incoming);
             }
            w.setTraining(obj.toString());
         }
         w.flush();
     }
 
 
     @Override
     public JSONObject getTrainingData(Worker w, Experiment e, Map parameterMap) throws JSONException {
         JSONObject result = new JSONObject();
         JSONArray trainingdata = null;
         try {
             trainingdata = new JSONArray(e.getTrainingProps());
         } catch (JSONException e1) {
             e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 
             result.put("status", "error");
             return result;
 
         }
         int trainingItem = Integer.parseInt(((String[]) parameterMap.get("trainingitem"))[0]);
         if (trainingItem >= trainingdata.length()) {
             result.put("status", "done");
         } else {
             result = trainingdata.getJSONObject(trainingItem);
         }
         return result;
     }
 
 
     public JSONObject scoreTraining(String trainingdata, String story) throws JSONException {
 
         JSONObject result = new JSONObject();
         List<Integer> storyorder = getStoryOrder(story);
 
         List<Float> scores = new ArrayList<Float>();
         float total = 0f;
 
         if (trainingdata != null && !trainingdata.isEmpty()) {
             for (String interim: trainingdata.split("&")) {
                 List<Integer> order = new ArrayList<Integer>();
                 for (String s : interim.split(";")) {
                     order.add(Integer.parseInt(s));
                 }
                 float f = (float) Math.floor(100 * score(storyorder, order));
                 total += f;
                 scores.add(f);
             }
         }
 
         result.put("roundScore", scores.isEmpty() ? 0 : scores.get(scores.size() - 1));
         result.put("cumulativeScore", scores.isEmpty() ? 0 : total);
         return result;
 
     }
 
 
     @Override
     public String getApplicationBody(Node n) throws Exception {
         return configureApplicationString("StoryHitProvider");
     }
 
     @Override
     @Transactional
     public void automateNodeTurn(Node n) throws ClassNotFoundException, JSONException, IllegalAccessException, InstantiationException {
         n = Node.findNode(n.getId());
         List<String> data = new ArrayList<String>(Arrays.asList(n.getPublicData_().split("&")));
 
         for (Node neighbor : n.getIncoming()) {
             List<String> ndata = new ArrayList<String>(Arrays.asList(neighbor.getPublicData_().split("&")));
             ndata.removeAll(data);
             data.addAll(ndata);
 
         }
         StringBuilder builder = new StringBuilder();
         String sep = "";
         for (String elt : data) {
             builder.append(sep).append(elt.split("=")[0]);
             sep = ";";
         }
 
         n.getSession_().getRunner().updateNode(n, builder.toString());
     }
 
     @Override
     public long getTurnLength(Session_ session) {
         try {
             return session.getPropertiesAsJson().getLong(PROP_TURN_LENGTH_SECONDS);
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return 0;
     }
 
 
     private void createGraph(Session_ s, Integer nodecount) throws GraphCreationException {
 
         Graph<DefaultJungNode, DefaultJungEdge> graph = null;
         String graphtype = s.getProperty(PROP_GRAPH_TYPE);
         int degree = Integer.parseInt(s.getProperty(PROP_NET_DEGREE));
 
         nodecount = Math.min(nodecount, Integer.parseInt(s.getProperty(PROP_NODE_COUNT)));
         if (nodecount == 1) {
             DefaultJungGraph g = new DefaultJungGraph();
             DefaultJungNode node = DefaultJungNode.getFactory().create();
             g.addVertex(node);
             graph = g;
         } else if ("lattice".equals(graphtype)) {
             if (Math.floor(Math.sqrt(nodecount.doubleValue())) < Math.sqrt(nodecount.doubleValue())) {
                 logger.warn("Requested number of nodes must be a perfect square for lattice networks");
             }
             Lattice2DGenerator<DefaultJungNode, DefaultJungEdge> generator = new Lattice2DGenerator<DefaultJungNode, DefaultJungEdge>(
                     DefaultJungGraph.getFactory(),
                     DefaultJungNode.getFactory(),
                     DefaultJungEdge.getFactory(), (int) Math.sqrt(nodecount.doubleValue()), true);
             graph = (DefaultJungGraph) generator.create();
 
         } else if ("connected".equals(graphtype)) {
             DefaultJungGraph g = DefaultJungGraph.getFactory().create();
             Factory<DefaultJungNode> nfact = DefaultJungNode.getFactory();
             Factory<DefaultJungEdge> efact = DefaultJungEdge.getFactory();
 
             for (int i = 0; i < nodecount; i++) {
                 g.addVertex(nfact.create());
             }
 
             List<DefaultJungNode> nodes = new ArrayList<DefaultJungNode>(g.getVertices());
             for (int i = 0; i < nodes.size(); i++) {
                 for (int j = 0; j < nodes.size(); j++) {
                     if (i == j) continue;
                     else {
                         g.addEdge(efact.create(), nodes.get(i), nodes.get(j));
                     }
                 }
             }
 
             graph = g;
 
 
         } else if ("ring".equals(graphtype)) {
             DefaultJungGraph g = DefaultJungGraph.getFactory().create();
             Factory<DefaultJungNode> nfact = DefaultJungNode.getFactory();
             Factory<DefaultJungEdge> efact = DefaultJungEdge.getFactory();
 
             for (int i = 0; i < nodecount; i++) {
                 g.addVertex(nfact.create());
             }
 
             List<DefaultJungNode> nodes = new ArrayList<DefaultJungNode>(g.getVertices());
             for (int i = 0; i < nodes.size(); i++) {
                 for (int j = 1; j <= degree; j++) {
                     g.addEdge(efact.create(), nodes.get(i), nodes.get((i + j) % nodecount));
                 }
             }
             graph = g;
 
         } else if ("bowtie".equals(graphtype)) {
             graph = GraphGenerator.generateBowtie(nodecount);
         } else if ("bowtie-circle".equals(graphtype)) {
             nodecount = (nodecount / 6) * 6;
 
             graph = GraphGenerator.generateBowtieCircle(nodecount);
         } else if ("wheel".equals(graphtype)) {
             nodecount = (nodecount / 2) * 2;
             graph = GraphGenerator.generateWheel(nodecount, degree);
         } else if ("square-tesselation".equals(graphtype)) {
             if (nodecount / 2 * 2 != nodecount) {
                 nodecount--;
             }
             graph = GraphGenerator.generateSquareLatticeGraph(nodecount, degree);
         } else if ("ring-lattice".equals(graphtype)) {
             graph = GraphGenerator.generateLatticeGraph(nodecount, degree);
 
         } else {
             throw new GraphCreationException("Graph type not supported");
         }
 
 
         Map<DefaultJungNode, Node> nodes = new HashMap<DefaultJungNode, Node>();
 
         for (DefaultJungNode vertex : graph.getVertices()) {
             Node node = new Node();
             node.setSession_(s);
             s.addNode(node);
             nodes.put(vertex, node);
             node.persist();
         }
 
 
         for (DefaultJungEdge edge : graph.getEdges()) {
             Pair<DefaultJungNode> eps = graph.getEndpoints(edge);
             nodes.get(eps.getSecond()).getIncoming().add(nodes.get(eps.getFirst()));
             if (graph.getDefaultEdgeType() == EdgeType.UNDIRECTED) {
                 nodes.get(eps.getFirst()).getIncoming().add(nodes.get(eps.getSecond()));
             }
         }
 
 
     }
 
 
     private void initNodes(Session_ session) {
         Set<Node> nodes = session.getAvailableNodes();
         logger.info("Assigned " + nodes.size() + " for session " + session.getId());
         int numtiles = Integer.valueOf(session.getProperty(PROP_PRIVATE_TILES));
         String[] story = session.getProperty(PROP_STORY).split("&");
         List<Node> rndnodes = new ArrayList<Node>(nodes);
         List<String> rndstory = new ArrayList<String>(Arrays.asList(story));
         Collections.shuffle(rndnodes);
         Collections.shuffle(rndstory);
         int i = 0;
         for (Node n : rndnodes) {
             for (int elt = 0; elt < numtiles; i++) {
                 i %= story.length;
                 if (n.getPublicData_() != null && n.getPublicData_().contains(rndstory.get(i))) {
                     continue;
                 }
                 n.setPublicData_((n.getPublicData_() == null ? "" : n.getPublicData_() + "&") + rndstory.get(i));
                 elt++;
             }
             logger.debug("Node " + n.getId() + ":" + n.getPublicData_());
             // n.persist();
         }
         for (Node n : nodes) {
             n.setPrivateData_(n.getPublicData_());
         }
 
 
     }
 
     @Override
     public String getHitCreation(Session_ session, String rooturl) {
 
         return null;
 
     }
 
     public static Float getScore(String story, String publicData) {
         List<Integer> truth = getStoryOrder(story);
         return score(truth, getStoryOrder(publicData));
     }
 
     public static List<Float> getSessionScores(List<SessionLog> logs) {
 
 
         List<Float> result = new ArrayList<Float>();
         if (logs == null || logs.size() == 0) {
             return result;
         }
         Session_ session = logs.get(0).getSession_();
         List<Integer> truth = getStoryOrder(session.getProperty(PROP_STORY));
         for (SessionLog log : logs) {
             result.add(score(truth, getStoryOrder(log.getNodePublicData())));
         }
         logger.debug("Resulting scores are: " + result);
         return result;
     }
 
 
     public Map<String, Object> getScoreInformation(Node n) {
         Map<String, Object> scoreinfo = new HashMap<String, Object>();
         Float f[] = n.getScores();
         scoreinfo.put("roundScore", f.length == 0 ? 0 : Math.floor(100 * f[f.length - 1]));
         float total = 0;
         for (float fi : f) {
             total += (100 * fi);
         }
         scoreinfo.put("cumulativeScore", f.length == 0 ? 0 : Math.floor(total));
 
         return scoreinfo;
     }
 
 
     @Override
     public Map<String, Object> getFinalInfo(Node n) {
         String story = n.getSession_().getProperty(PROP_STORY);
         Map<String, Object> result = getScoreInformation(n);
         result.put("answer", new JSONArray(getStoryAsList(story)));
         return result;
 
     }
 
     //TODO: Please please improve me
     public int getRemainingSessionCount(Experiment e) {
         List<Session_> known = new ArrayList<Session_>();
         for (Session_ s : e.getSessions()) {
             if (s.getStatus() != Session_.Status.ABORTED) {
                 known.add(s);
             }
         }
         if (e.getSessionProps() != null) {
            return getRemainingSessionNames(e).size();
 
         } else if (e.getProperty(Plugin.PROP_SESSION_COUNT) != null) {
             return Math.max(0, Integer.parseInt(e.getProperty(Plugin.PROP_SESSION_COUNT)) - known.size());
         }
         return 0;
     }
 
     @Override
     public Set<String> getRemainingSessionNames(Experiment e) {
        Set<String> available = new HashSet<String>();
         if (e.getSessionProps() != null) {
             try {
 
                 JSONArray pending = null;
                 pending = new JSONArray(e.getSessionProps());
 
                 for (int i = 0; i < pending.length(); i++) {
 
                     JSONObject props = pending.getJSONObject(i);
                     if (props.has(PROP_SESSION_ID)) {
                         available.add(props.getString(PROP_SESSION_ID));
                     }
 
                 }
 
             } catch (JSONException e1) {
                 e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
         for (Session_ s : e.getSessions()) {
             if (s.getStatus() != Session_.Status.ABORTED) {
                if (s.getProperty(PROP_SESSION_ID)!=null) {
                    available.remove(s.getProperty(PROP_SESSION_ID));
                }
             }
         }
         return available;
 
     }
 
 
 
 
     @Override
     public Destination getDestinationForEvent(Worker w, Event e) {
         if (e == Event.LOGIN || e == Event.REGISTER) {
             if (w.getQualifications() == null) {
                 return Destination.QUALIFICATIONS;
             } else if (w.getTraining() == null) {
                 return Destination.TRAINING;
             } else return Destination.WAITING;
         }
         if (e == Event.QUALIFICATIONS_SUBMITTED) {
             return Destination.TRAINING;
         }
         if (e == Event.TRAINING_SUBMITTED) {
             return Destination.WAITING;
         }
         return Destination.WAITING;
     }
 
     private static List<Integer> getStoryOrder(String story) {
         List<Integer> result = new ArrayList<Integer>();
         logger.debug("Extracting story from " + story);
         Pattern pat = Pattern.compile("(\\d+)=.+");
         if (story != null) {
             for (String p : story.split("&")) {
                 p = p.trim();
                 Matcher m = pat.matcher(p);
                 if (m.matches()) {
                     result.add(Integer.parseInt(m.group(1)));
                 }
             }
         }
         logger.debug("Got story order: " + result);
         return result;
     }
 
     private static List<String> getStoryAsList(String story) {
         List<String> result = new ArrayList<String>();
         logger.debug("Extracting story from " + story);
         Pattern pat = Pattern.compile("\\d+=(.+)");
         if (story != null) {
             for (String p : story.split("&")) {
                 p = p.trim();
                 Matcher m = pat.matcher(p);
                 if (m.matches()) {
                     result.add(m.group(1));
                 }
             }
         }
         return result;
     }
 
 
     private static Map<String, String> getStoryMap(String story) {
         Map<String, String> result = new HashMap<String, String>();
         Pattern pat = Pattern.compile("(\\d+)=(.+)");
         if (story != null) {
             for (String p : story.split("&")) {
                 p = p.trim();
                 Matcher m = pat.matcher(p);
                 if (m.matches()) {
                     result.put(m.group(1), m.group(2));
                 }
             }
         }
         return result;
     }
 
     public static Float score(List<Integer> truth, List<Integer> sample) {
         logger.debug("Checking truth:" + truth + " against sample:" + sample);
         Map<Integer, Integer> tmap = new HashMap<Integer, Integer>();
         int i = 0;
         for (Integer t : truth) {
             tmap.put(t, i++);
         }
         tmap.keySet().retainAll(sample);
 
 
         int last = -1;
         int accountedFor = 0;
         for (Integer s : sample) {
             if (last > -1) {
                 if (tmap.get(last) < tmap.get(s)) {
                     accountedFor++;
                 }
             }
             last = s;
 
         }
         return accountedFor / (float) (truth.size() - 1);
 
 
     }
 
     public static void main(String[] args) {
         String truth = "3:there was a fox and a bear;209:who were friends;87:one day they decided to catch a chicken for supper;262:they decided to go together;849:because neither one wanted to be left alone;369:and they both liked fried chicken;";
         String sample = "262:they decided to go together;849:because neither one wanted to be left alone;369:and they both liked fried chicken;3:there was a fox and a bear;209:who were friends;";
         System.err.println(score(getStoryOrder(truth), getStoryOrder(sample)));
 
     }
 
 
 }
