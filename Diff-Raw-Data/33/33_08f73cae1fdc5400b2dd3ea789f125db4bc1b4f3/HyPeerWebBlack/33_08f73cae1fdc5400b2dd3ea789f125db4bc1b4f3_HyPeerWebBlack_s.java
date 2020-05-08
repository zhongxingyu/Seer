 package black;
 
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import hypeerweb.HyPeerWeb;
 import hypeerweb.Node;
 
 public class HyPeerWebBlack extends TestCase{
 
     private static final int MAX_SIZE = 3;
     
     private static final String DATABASE_ONE = "HyPeerWebBlack1.db";
     private static final String DATABASE_TWO = "HyPeerWebBlack2.db";
     
     @BeforeClass
     public static void setUpBeforeClass() {
         HyPeerWeb.getSingleton().reload(DATABASE_TWO);
         HyPeerWeb.getSingleton().clear();
         HyPeerWeb.getSingleton().saveToDatabase();
         HyPeerWeb.getSingleton().reload(DATABASE_ONE);
         HyPeerWeb.getSingleton().clear();
         HyPeerWeb.getSingleton().saveToDatabase();
     }
 
     @After
     public void tearDown() throws Exception {
         HyPeerWeb.getSingleton().reload(DATABASE_TWO);
         HyPeerWeb.getSingleton().clear();
         HyPeerWeb.getSingleton().saveToDatabase();
         HyPeerWeb.getSingleton().reload(DATABASE_ONE);
         HyPeerWeb.getSingleton().clear();
         HyPeerWeb.getSingleton().saveToDatabase();
     }
 
     @Test
     public void testGetSingleton() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         web = HyPeerWeb.getSingleton();
         assertTrue(web.size() == 1);
         web.removeFromHyPeerWeb();
         web = HyPeerWeb.getSingleton();
         assertTrue(web.size() == 0);
         web.addNode(new Node(1));
         web.clear();
         web = HyPeerWeb.getSingleton();
         assertTrue(web.size() == 0);
     }

     @Test
     public void testClear() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         assertTrue(web.size() == 0);
         web.clear();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(i), web.getNode(0));
         }
         assertTrue(web.size() == MAX_SIZE);
         web.clear();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(i), web.getNode(0));
         }
         assertTrue(web.size() == MAX_SIZE);
         for(int i = MAX_SIZE; web.size() >= 2; i--){// Two comes from the precondition
             assertTrue(web.size() == i);
             web.removeFromHyPeerWeb();
         }
         web.clear();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         for(Random rand = new Random(); web.size() < MAX_SIZE;
                 web.addToHyPeerWeb(new Node(0), web.getNode(0))){
             int size = web.size();
             if(rand.nextBoolean()){
                 web.addToHyPeerWeb(new Node(0), web.getNode(0));
                 assertTrue(web.size() == size + 1);
             }
             else if(web.size() >= 2){// Two comes from the precondition.
                 web.removeFromHyPeerWeb();
                 assertTrue(web.size() == size - 1);
             }
         }
         web.clear();
         assertTrue(web.size() == 0);
     }
 
     @Test
     public void testSize() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         web.addNode(new Node(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(i), web.getNode(0));
         }
         assertTrue(web.size() == MAX_SIZE);
         for(int i = MAX_SIZE - 1; web.size() >= 2; i--){// Two comes from the precondition
             web.removeFromHyPeerWeb();
             assertTrue(web.size() == i);
         }
         assertTrue(web.size() == 1);
         for(Random rand = new Random(); web.size() < MAX_SIZE;
                 web.addToHyPeerWeb(new Node(0), web.getNode(0))){
             int size = web.size();
             if(rand.nextBoolean()){
                 web.addToHyPeerWeb(new Node(0), web.getNode(0));
                 assertTrue(web.size() == size + 1);
             }
             else if(web.size() >= 2){// Two comes from the precondition.
                 web.removeFromHyPeerWeb();
                 assertTrue(web.size() == size - 1);
             }
         }
     }
 
     @Test
     public void testReloadString() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         web.reload(DATABASE_ONE);
         web.saveToDatabase();
         web.addNode(new Node(0));
         web.reload(DATABASE_ONE);
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         web.saveToDatabase();
         web.reload(DATABASE_ONE);
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(0), web.getNode(0));
             web.saveToDatabase();
             web.reload(DATABASE_ONE);
         }
         assertTrue(web.size() == MAX_SIZE);
         web.reload(DATABASE_TWO);
         web.saveToDatabase();
         web.addNode(new Node(0));
         web.reload(DATABASE_TWO);
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         web.saveToDatabase();
         web.reload(DATABASE_TWO);
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(0), web.getNode(0));
             web.saveToDatabase();
             web.reload(DATABASE_TWO);
         }
         assertTrue(web.size() == MAX_SIZE);
         web.reload(DATABASE_ONE);
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testGetNode() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         Node node = new Node(0);
         web.addNode(node);
         assertTrue(node == web.getNode(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             node = new Node(Math.abs(rand.nextInt()));
             web.addToHyPeerWeb(node, web.getNode(rand.nextInt(web.size())));
             assertTrue(node == web.getNode(i));
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testGetNodeAsserts() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         try{
             web.getNode(-1);
             fail("HyPeerWeb.getNode accepted -1 on size 0.");
         }
         catch(AssertionError e) { }
         try{
             web.getNode(0);
             fail("HyPeerWeb.getNode accepted 0 on size 0.");
         }
         catch(AssertionError e) { }
         try{
             web.getNode(1);
             fail("HyPeerWeb.getNode accepted 1 on size 0.");
         }
         catch(AssertionError e) { }
         Node node = new Node(0);
         web.addNode(node);
         assertTrue(node == web.getNode(0));
         try{
             web.getNode(-1);
             fail("HyPeerWeb.getNode accepted -1 on size 1.");
         }
         catch(AssertionError e) { }
         try{
             web.getNode(1);
             fail("HyPeerWeb.getNode accepted 1 on size 1.");
         }
         catch(AssertionError e) { }
     }

     @Test
     public void testReload() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         web.clear();
         web.saveToDatabase();
         web.addNode(new Node(0));
        web.reload();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         web.saveToDatabase();
        web.reload();
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(0), web.getNode(0));
             web.saveToDatabase();
            web.reload();
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testSaveToDatabase() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         web.clear();
         web.saveToDatabase();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
        web.reload();
         assertTrue(web.size() == 0);
         web.addNode(new Node(0));
         web.saveToDatabase();
        web.reload();
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(0), web.getNode(0));
             web.saveToDatabase();
            web.reload();
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testAddNode() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         for(int i = 0; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addNode(new Node(Math.abs(rand.nextInt())));
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testAddNodeAsserts() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         web.addNode(new Node(0));
         try{
             web.addNode(new Node(0));
             fail("HyPeerWeb.addNode accepted 0 when 0 is in the HyPeerWeb.");
         }
         catch(AssertionError e) { }
     }
 
     @Test
     public void testContains() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         for(int i = 0; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             Node node = new Node(Math.abs(rand.nextInt()));
             web.addNode(node);
             assertTrue(web.contains(node));
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testAddToHyPeerWeb() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         web.addToHyPeerWeb(new Node(0), null);
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(Math.abs(rand.nextInt())), 
                     web.getNode(rand.nextInt(web.size())));
         }
         assertTrue(web.size() == MAX_SIZE);
     }
 
     @Test
     public void testAddToHyPeerWebAsserts() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         try{
             web.addToHyPeerWeb(null, new Node(0));
             fail("HyPeerWeb.addToHyPeerWeb accepted null for newNode. Size = 0");
         }
         catch(AssertionError e) { }
         try{
             web.addToHyPeerWeb(Node.NULL_NODE, new Node(0));
             fail("HyPeerWeb.addToHyPeerWeb accepted NULL_NODE for newNode. Size = 0");
         }
         catch(AssertionError e) { }
         web.addNode(new Node(0));
         try{
             web.addToHyPeerWeb(null, web.getNode(0));
             fail("HyPeerWeb.addToHyPeerWeb accepted null for newNode. Size = 1");
         }
         catch(AssertionError e) { }
         try{
             web.addToHyPeerWeb(Node.NULL_NODE, web.getNode(0));
             fail("HyPeerWeb.addToHyPeerWeb accepted NULL_NODE for newNode. Size = 1");
         }
         catch(AssertionError e) { }
         try{
             web.addToHyPeerWeb(new Node(-1), null);
             fail("HyPeerWeb.addToHyPeerWeb accepted null for startNode.");
         }
         catch(AssertionError e) { }
         try{
             web.addToHyPeerWeb(new Node(-1), Node.NULL_NODE);
             fail("HyPeerWeb.addToHyPeerWeb accepted NULL_NODE for startNode.");
         }
         catch(AssertionError e) { }
     }
 
     @Test
     public void testRemoveFromHyPeerWeb() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         web.addNode(new Node(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(Math.abs(rand.nextInt())), 
                     web.getNode(rand.nextInt(web.size())));
         }
         assertTrue(web.size() == MAX_SIZE);
         for(int i = web.size() - 1; web.size() >= 2; i--){
             web.removeFromHyPeerWeb();
             assertTrue(web.size() == i);
         }
     }
 
     @Test
     public void testRemoveFromHyPeerWebInt() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         Random rand = new Random();
         web.addNode(new Node(0));
         for(int i = 1; i < MAX_SIZE; i++){
             assertTrue(web.size() == i);
             web.addToHyPeerWeb(new Node(Math.abs(rand.nextInt())), 
                     web.getNode(rand.nextInt(web.size())));
         }
         assertTrue(web.size() == MAX_SIZE);
         for(int i = web.size() - 1; web.size() >= 2; i--){
             for(int j = 0; j < web.size(); j++){
                 web.removeFromHyPeerWeb(j);
                 assertTrue(web.size() == i);
                 web.addToHyPeerWeb(new Node(0), web.getNode(0));
             }
             web.removeFromHyPeerWeb(web.size() - 1);
         }
     }
 
     @Test
     public void testRemoveFromHyPeerWebIntAsserts() {
         HyPeerWeb web = HyPeerWeb.getSingleton();
         try{
             web.removeFromHyPeerWeb(-1);
             fail("HyPeerWeb.removeFromHyPeerWeb accepted -1 for id.");
         }
         catch(AssertionError e) { }
         try{
             web.removeFromHyPeerWeb(-1);
             fail("HyPeerWeb.removeFromHyPeerWeb accepted 0 for id. Size = 0");
         }
         catch(AssertionError e) { }
     }
 }
