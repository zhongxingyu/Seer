 package edu.mit.cci.turksnet.plugins;
 
 
 import edu.mit.cci.snatools.util.jung.DefaultJungEdge;
 import edu.mit.cci.snatools.util.jung.DefaultJungGraph;
 import edu.mit.cci.snatools.util.jung.DefaultJungNode;
 import edu.mit.cci.turksnet.Experiment;
 import edu.mit.cci.turksnet.Node;
 import edu.mit.cci.turksnet.SessionLog;
 import edu.mit.cci.turksnet.Session_;
 import edu.mit.cci.turksnet.Worker;
 import edu.mit.cci.turksnet.util.NodeStatus;
 import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
 import edu.uci.ics.jung.graph.util.Pair;
 import org.apache.commons.collections15.Factory;
 import org.apache.log4j.Logger;
 import org.apache.sling.commons.json.JSONArray;
 import org.apache.sling.commons.json.JSONException;
 
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
 
 
     private static Logger logger = Logger.getLogger(LoomStandalonePlugin.class);
 
 
     public Session_ createSession(Experiment experiment, List<Worker> workers) throws SessionCreationException {
         //@TODO add logic for checking worker history and making sure there is an available session
         Map<String, String> props = experiment.getPropsAsMap();
         if (workers.size() < Integer.parseInt(experiment.getPropsAsMap().get(PROP_NODE_COUNT))) {
             return null;
         } else {
             Session_ session = new Session_(experiment.getId());
             session.setCreated(new Date());
             session.setIteration(-1);
             session.persist();
             try {
                 createGraph(props, session);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new SessionCreationException("Error generating graph");
             }
             initNodes(session.getAvailableNodes(), props);
 
             for (int i = 0; i < session.getAvailableNodes().size(); i++) {
                 session.assignNodeToTurker(workers.get(i));
             }
             //session.merge();
             return session;
 
         }
 
 
     }
 
 
     @Override
     public boolean checkDone(Session_ s) {
         for (Node n : s.getAvailableNodes()) {
             if (NodeStatus.valueOf(n.getStatus()) == NodeStatus.ACCEPTING_INPUT) {
                 return false;
             }
         }
         return (s.getIteration() >= Integer.parseInt(s.getExperiment().getPropsAsMap().get(PROP_ITERATION_COUNT)));
 
     }
 
     @Override
     public void processResults(Node n, String results) {
         logger.debug("Receiving " + results + " from " + n.getWorker().getUsername());
         StringBuilder builder = new StringBuilder();
         String[] items = results.split(";");
         Map<String, String> storymap = getStoryMap(n.getSession_().getExperiment().getPropsAsMap().get(PROP_STORY));
         String sep = "";
         for (String i : items) {
             builder.append(sep).append(i).append("=").append(storymap.get(i));
             sep = "&";
         }
         String s = builder.toString();
         logger.debug("Setting data: " + s);
         n.setPublicData_(s);
         //n.persist();
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
 
     @Override
     public void preprocessProperties(Experiment experiment) throws ExperimentCreationException {
         StringBuilder builder = new StringBuilder();
         Map<String, String> props = experiment.getPropsAsMap();
         String story = props.get(PROP_STORY);
         Pattern pat = Pattern.compile("(\\d+)=([\\w\\s]+)");
         if (story == null) throw new ExperimentCreationException("No story associated with Loom Experiment");
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
         experiment.updateProperty(PROP_STORY, builder.toString());
     }
 
 
     @Override
     public String getApplicationBody(Node n) throws Exception {
         InputStream stream = getClass().getResourceAsStream("/loomStandalone.fragment.html");
         return edu.mit.cci.turkit.util.U.slurp(stream, "UTF8");
     }
 
     @Override
     public void automateNodeTurn(Node n) throws ClassNotFoundException, JSONException, IllegalAccessException, InstantiationException {
         String[] data = n.getPublicData_().split("&");
         StringBuilder builder = new StringBuilder();
         String sep = "";
         for (String elt : data) {
             builder.append(elt.split("=")[0]).append(sep);
             sep = ";";
         }
 
         n.getSession_().getRunner().updateNode(n, builder.toString());
     }
 
     @Override
     public long getTurnLength(Experiment experiment) {
         return Long.parseLong(experiment.getPropsAsMap().get(PROP_TURN_LENGTH_SECONDS));
     }
 
     private void createGraph(Map<String, String> props, Session_ session) throws GraphCreationException {
         String graphtype = props.get(PROP_GRAPH_TYPE);
         Integer nodecount = Integer.parseInt(props.get(PROP_NODE_COUNT));
         DefaultJungGraph graph = null;
         if (nodecount == 1) {
             graph = new DefaultJungGraph();
             DefaultJungNode node = DefaultJungNode.getFactory().create();
             graph.addVertex(node);
         } else if ("lattice".equals(graphtype)) {
             if (Math.floor(Math.sqrt(nodecount.doubleValue())) < Math.sqrt(nodecount.doubleValue())) {
                 logger.warn("Requested number of nodes must be a perfect square for lattice networks");
             }
             Lattice2DGenerator<DefaultJungNode, DefaultJungEdge> generator = new Lattice2DGenerator<DefaultJungNode, DefaultJungEdge>(
                     DefaultJungGraph.getFactory(),
                     DefaultJungNode.getFactory(),
                     DefaultJungEdge.getFactory(), (int) Math.sqrt(nodecount.doubleValue()), true);
             graph = (DefaultJungGraph) generator.create();
 //        } else if ("erdosrenyi".equals(graphtype)) {
 //            ErdosRenyiGenerator<DefaultJungNode,DefaultJungEdge> generator = new ErdosRenyiGenerator<DefaultJungNode, DefaultJungEdge>(
 //                    DefaultUndirectedJungGraph.getFactory(),
 //                    DefaultJungNode.getFactory(),
 //                    DefaultJungEdge.getFactory(), nodecount.intValue(),1.0f);
 //            graph = (DefaultJungGraph)
 //
 //        }
         } else if ("connected".equals(graphtype)) {
             graph = DefaultJungGraph.getFactory().create();
             Factory<DefaultJungNode> nfact = DefaultJungNode.getFactory();
             Factory<DefaultJungEdge> efact = DefaultJungEdge.getFactory();
 
             for (int i = 0; i < nodecount; i++) {
                 graph.addVertex(nfact.create());
             }
 
             List<DefaultJungNode> nodes = new ArrayList<DefaultJungNode>(graph.getVertices());
             for (int i = 0; i < nodes.size(); i++) {
                 for (int j = 0; j < nodes.size(); j++) {
                     if (i == j) continue;
                     else {
                         graph.addEdge(efact.create(), nodes.get(i), nodes.get(j));
                     }
                 }
             }
 
 
         } else if ("ring".equals(graphtype)) {
             graph = DefaultJungGraph.getFactory().create();
             Factory<DefaultJungNode> nfact = DefaultJungNode.getFactory();
             Factory<DefaultJungEdge> efact = DefaultJungEdge.getFactory();
             int degree = Math.min(Integer.parseInt(props.get(PROP_NET_DEGREE)), nodecount - 1);
             for (int i = 0; i < nodecount; i++) {
                 graph.addVertex(nfact.create());
             }
 
             List<DefaultJungNode> nodes = new ArrayList<DefaultJungNode>(graph.getVertices());
             for (int i = 0; i < nodes.size(); i++) {
                 for (int j = 1; j <= degree; j++) {
                     graph.addEdge(efact.create(), nodes.get(i), nodes.get((i + j) % nodecount));
                 }
             }
 
 
         } else {
             throw new GraphCreationException("Graph type not supported");
         }
 
 
         Map<DefaultJungNode, Node> nodes = new HashMap<DefaultJungNode, Node>();
         for (DefaultJungNode vertex : graph.getVertices()) {
             Node node = new Node();
             node.setSession_(session);
             session.addNode(node);
             nodes.put(vertex, node);
 
 
         }
 
         for (DefaultJungEdge edge : graph.getEdges()) {
             Pair<DefaultJungNode> eps = graph.getEndpoints(edge);
             nodes.get(eps.getSecond()).getIncoming().add(nodes.get(eps.getFirst()));
         }
 
         session.persist();
 
 
     }
 
 
     private void initNodes(Collection<Node> nodes, Map<String, String> props) {
         int numtiles = Integer.valueOf(props.get(PROP_PRIVATE_TILES));
         String[] story = props.get(PROP_STORY).split("&");
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
             logger.debug("Node "+n.getId()+":"+n.getPublicData_());
            // n.persist();
         }
 
 
     }
 
     @Override
     public String getHitCreation(Session_ session, String rooturl) {
 //        Map<String,String> props = session.getExperiment().getPropsAsMap();
 //        String val = props.get(PROP_ASSIGNMENT_VALUE);
 //        Map<String, String> result = new HashMap<String, String>();
 //        result.put("title", props.get(PROP_HIT_TITLE));
 //        result.put("desc", props.get(PROP_HIT_DESCRIPTION));
 //        result.put("url", rooturl + "/session_s/" + session.getId() + "/turk/app");
 //        result.put("autoApprovalDelayInSeconds",props.get(PROP_HIT_AUTO_APPROVAL_DELAY));
 //        result.put("assignmentDurationInSeconds",props.get(PROP_HIT_ASSIGNMENT_DURATION));
 //        result.put("reward", val);
 //        result.put("assignments", props.get(PROP_NODE_COUNT));
 //        result.put("keywords",props.get(PROP_HIT_KEYWORDS));
 //        result.put("height", props.get(PROP_HIT_HEIGHT));
 //        if (session.getQualificationRequirements()!=null ) {
 //            result.put("qualificationRequirements", createQualificationString(session.getQualificationRequirements()));
 //        }
 //        return "("+jsonify(result)+")";
         return null;
 
     }
 
 
     public static List<Float> getSessionScores(Experiment experiment, List<SessionLog> logs) {
         List<Float> result = new ArrayList<Float>();
         List<Integer> truth = getStoryOrder(experiment.getPropsAsMap().get(PROP_STORY));
         for (SessionLog log : logs) {
             result.add(score(truth, getStoryOrder(log.getNodePublicData())));
         }
         logger.debug("Resulting scores are: " + result);
         return result;
     }
 
 
     public Map<String, Object> getScoreInformation(Node n) {
         Experiment e = n.getSession_().getExperiment();
 
         Map<String, Object> scoreinfo = new HashMap<String, Object>();
 
         JSONArray storylist = new JSONArray();
         String story = e.getPropsAsMap().get(PROP_STORY);
         Map<String, String> storymap = getStoryMap(story);
         for (Integer i : getStoryOrder(story)) {
             storylist.put(storymap.get(i + ""));
 
         }
 
         scoreinfo.put("answer", storylist);
         List<SessionLog> logs = new ArrayList<SessionLog>();
         for (SessionLog log : SessionLog.findAllSessionLogs()) {
             if (n.getId().equals(log.getNode().getId()) && log.getType().equals("results")) {
                 logs.add(log);
             }
         }
 
         List<Float> scores = getSessionScores(n.getSession_().getExperiment(), logs);
 
         scoreinfo.put("final_round", Math.floor(100 * (scores.get(scores.size() - 1))));
         float total = 0;
         for (float f : scores) {
             total += f;
         }
 
         scoreinfo.put("average", Math.floor(100 * (total / (scores.size()))));
 
         return scoreinfo;
     }
 
     private static List<Integer> getStoryOrder(String story) {
         List<Integer> result = new ArrayList<Integer>();
         logger.debug("Extracting story from " + story);
        Pattern pat = Pattern.compile("(\\d+)=[\\w\\s]+");
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
 
     private static Map<String, String> getStoryMap(String story) {
         Map<String, String> result = new HashMap<String, String>();
        Pattern pat = Pattern.compile("(\\d+)=([\\w\\s]+)");
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
