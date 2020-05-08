 package ai.ilikeplaces.logic.hotspots;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.logic.validators.unit.BoundingBox;
 import ai.ilikeplaces.util.Loggers;
 import ai.ilikeplaces.util.SmartLogger;
 import com.google.gdata.data.geo.impl.W3CPoint;
 import net.sf.oval.Validator;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * This class calculates hotspots on a given bounding box.
  * <p/>
  * It takes a list of geocordinates along with their common names,
  * and analyzes the hotspots and the "mode" name for that spot.
  * <p/>
  * <p/>
  * It also takes an optional block size(per hotspot).
  * <p/>
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Oct 10, 2010
  * Time: 2:35:54 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class HotspotAnalyzer {
 
     public Set<Rawspot> rawspots;
 
     public BoundingBox bb;
 
     private BoundingBox[][] mesh;
 //    private Map<Integer, Map<Integer, BoundingBox>> mesh;
 
     private Hotspot[][] hotspots;
 //    private Map<Integer, Map<Integer, Hotspot>> hotspots;
 
    public double[] blockWH;//= new double[]{0.000184401869775, 0.000133145585763375};
 
     private Validator v = new Validator();
     private int arrayWSize;
     private int arrayHSize;
 
     final SmartLogger sl = new SmartLogger(Loggers.LEVEL.DEBUG, "HotspotAnalyzer", 5000, "started", true);
     private static final String BYTES = " Bytes";
     private static final String GENERATE_HOTSPOTS_RAWSPOTS_AVERAGE_DONE_DONE = "generateHotspots rawspots average done done - ";
     private static final String GENERATE_HOTSPOTS_RAWSPOTS_ADDITION_DONE = "generateHotspots rawspots addition done - ";
     private static final String GENERATE_HOTSPOTS_INTERNALS_INIT_DONE = "generateHotspots internals init done - ";
     private static final String INIT_MESH_POST_MESH_HOTSPOT_INIT = "initMesh Post mesh hotspot init - ";
     private static final String INIT_MESH_PRE_CONTRUCTION = "initMesh Pre Contruction - ";
     private static final String POST_CONTRUCTION = "Post Contruction - ";
     private static final String PRE_CONTRUCTION = "Pre Contruction - ";
 
     //lat 0.0005325823430535 / 4 = 0.000133145585763375
     //lng 0.0007376074791 / 4 =  0.000184401869775
 
     //6.876754135813088,79.93997275829315,6.8772867181561415,79.94071036577225
 
     public HotspotAnalyzer(final Set<Rawspot> rawspots, final BoundingBox bb) {
         sl.appendToLogMSG(PRE_CONTRUCTION + memory());
         this.rawspots = rawspots;
         this.bb = bb;
         sl.appendToLogMSG(POST_CONTRUCTION + memory());
     }
 
     private void initMesh() {
         sl.appendToLogMSG(INIT_MESH_PRE_CONTRUCTION + memory());
 
         final double widthStart = bb.getObjectAsValid()[0].getObjectAsValid().getLongitude();
         final double widthEnd = bb.getObjectAsValid()[1].getObjectAsValid().getLongitude();
 
         final double heightStart = bb.getObjectAsValid()[0].getObjectAsValid().getLatitude();
         final double heightEnd = bb.getObjectAsValid()[1].getObjectAsValid().getLatitude();
 
 
         final double width = widthEnd - widthStart;
         final double height = heightEnd - heightStart;
 
        blockWH = new double[]{width / 12, height / 12};
 
 //        arrayWSize = (int) Math.ceil(width / blockWH[0]);
 //        arrayHSize = (int) Math.ceil(height / blockWH[1]);
 
         arrayWSize = (int) Math.ceil(width / blockWH[0]);
         arrayHSize = (int) Math.ceil(height / blockWH[1]);
 
         mesh = new BoundingBox[arrayWSize][arrayHSize];
         hotspots = new Hotspot[arrayWSize][arrayHSize];
 
 //        sl.appendToLogMSG("initMesh Pre MESH Contruction - " + memory());
 //        mesh = new HashMap<Integer, Map<Integer, BoundingBox>>(arrayWSize) {{
 //            for (int i = 0; i < arrayWSize; i++) {
 //                put(i, new HashMap<Integer, BoundingBox>(arrayHSize));
 //            }
 //        }};
 //        sl.appendToLogMSG("initMesh Pre HOTSPOT Contruction - " + memory());
 //        hotspots = new HashMap<Integer, Map<Integer, Hotspot>>(arrayWSize) {{
 //            for (int i = 0; i < arrayWSize; i++) {
 //                put(i, new HashMap<Integer, Hotspot>(arrayHSize));
 //            }
 //        }};
 
         sl.appendToLogMSG(INIT_MESH_POST_MESH_HOTSPOT_INIT + memory());
 
         double currentLatitude, currentLongitude;
 
         currentLongitude = widthStart;
         for (int i = 0; i < arrayWSize; i++) {
 
             currentLatitude = heightStart;
             for (int j = 0; j < arrayHSize; j++) {
 
                 mesh[i][j] = new BoundingBox().setObj(currentLatitude, currentLongitude, currentLatitude + blockWH[1], currentLongitude + blockWH[0]);
                 mesh[i][j].validateThrow(v);
 
 //                mesh.get(i).put(j, (BoundingBox)
 //                        new BoundingBox()
 //                                .setObj(currentLatitude, currentLongitude, currentLatitude + blockWH[1], currentLongitude + blockWH[0])
 //                                .getSelfAsValid());//new
 
                 currentLatitude += blockWH[1];
             }
 
             currentLongitude += blockWH[0];
         }
         sl.appendToLogMSG("initMesh done - " + memory());
     }
 
     private synchronized void generateHotspots() {
         sl.appendToLogMSG("generateHotspots init - " + memory());
 
         final BBWithSpots[][] allocatedrawspots = new BBWithSpots[arrayWSize][arrayHSize];
 
 //        final Map<Integer, Map<Integer, BBWithSpots>> allocatedrawspots = new HashMap<Integer, Map<Integer, BBWithSpots>>(arrayWSize) {{
 //            for (int i = 0; i < arrayWSize; i++) {
 //                put(i, new HashMap<Integer, BBWithSpots>(arrayHSize));
 //            }
 //        }};
 
         for (int i = 0; i < mesh.length; i++) {
             for (int j = 0; j < mesh[i].length; j++) {
                 allocatedrawspots[i][j] = (BBWithSpots) new BBWithSpots().setPair(mesh[i][j], new RawspotElasticArray());
             }
         }
 
 //        for (int i = 0; i < arrayWSize; i++) {
 //            for (int j = 0; j < arrayHSize; j++) {
 //                allocatedrawspots.get(i).put(j, (BBWithSpots) new BBWithSpots().setPair(mesh.get(i).get(j), new RawspotElasticArray()));
 //            }
 //        }
 
         sl.appendToLogMSG(GENERATE_HOTSPOTS_INTERNALS_INIT_DONE + memory());
 
         for (final Rawspot location : rawspots) {
             for (int i = 0; i < mesh.length; i++) {
                 for (int j = 0; j < mesh[i].length; j++) {
                     if (mesh[i][j].bounds(location.getCoordinates())) {
                         allocatedrawspots[i][j].getValue().addEntries(location);
                     }
                 }
             }
         }
 
 
 //        for (final Rawspot location : rawspots) {
 //            for (int i = 0; i < arrayWSize; i++) {
 //                for (int j = 0; j < arrayHSize; j++) {
 //                    if (mesh.get(i).get(j).bounds(location.getCoordinates())) {
 //                        allocatedrawspots.get(i).get(j).getValue().addEntries(location);
 //                    }
 //                }
 //            }
 //        }
         sl.appendToLogMSG(GENERATE_HOTSPOTS_RAWSPOTS_ADDITION_DONE + memory());
 
         for (int i = 0; i < allocatedrawspots.length; i++) {
             for (int j = 0; j < allocatedrawspots[i].length; j++) {
                 hotspots[i][j] = new Hotspot(allocatedrawspots[i][j].spotAverage(), allocatedrawspots[i][j].modeName(), allocatedrawspots[i][j].getValue().array.length);
             }
         }
 
 //        for (int i = 0; i < allocatedrawspots.size(); i++) {
 //            for (int j = 0; j < allocatedrawspots.get(i).size(); j++) {
 //                hotspots.get(i).put(j, new Hotspot(allocatedrawspots.get(i).get(j).spotAverage(), allocatedrawspots.get(i).get(j).modeName(), allocatedrawspots.get(i).get(j).getValue().array.length));
 //            }
 //        }
 
         sl.complete(GENERATE_HOTSPOTS_RAWSPOTS_AVERAGE_DONE_DONE + memory());
     }
 
     private synchronized void refresh() {
         initMesh();
         generateHotspots();
     }
 
 
 //    public synchronized Map<Integer, Map<Integer, Hotspot>> getHotspots() {
 //        refresh();
 //        return hotspots;
 //    }
 
     public synchronized Hotspot[][] getHotspots() {
         refresh();
         return hotspots;
     }
 
     public static void main(String args[]) {
         final Set<Rawspot> rs = new HashSet<Rawspot>();
         //tl
         rs.add(new Rawspot(new W3CPoint(6.877182864646137, 79.94012966752052), "a1 of californina"));
         rs.add(new Rawspot(new W3CPoint(6.877180201735318, 79.94016319513321), "a2 of california"));
         rs.add(new Rawspot(new W3CPoint(6.877192184833859, 79.94016319513321), "a3 of colombo"));
         rs.add(new Rawspot(new W3CPoint(6.877192184833859, 79.94012966752052), "a4 of matara"));
         rs.add(new Rawspot(new W3CPoint(6.877182864646137, 79.94016319513321), "a5 in galle"));
         rs.add(new Rawspot(new W3CPoint(6.877182864646137, 79.94016319513321), "a6 of course"));
         rs.add(new Rawspot(new W3CPoint(6.877182864646137, 79.94016319513321), "a7 not really"));
         rs.add(new Rawspot(new W3CPoint(6.877182864646137, 79.94016319513321), "a8 just a"));
 
         //middle
         rs.add(new Rawspot(new W3CPoint(6.876984477749757, 79.9405038356781), "m1 just m"));
         rs.add(new Rawspot(new W3CPoint(6.876984477749757, 79.9405038356781), "m2 is m?"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "m5 pizza 4 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "Pizza 1 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "pizza 2 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "PIZZa HUT 2"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "pizza 2 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "pizza 3 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "pizza 3 hut"));
         rs.add(new Rawspot(new W3CPoint(6.877064365100539, 79.94048774242401), "pizza 3 hut 2"));
         rs.add(new Rawspot(new W3CPoint(6.8770377359851045, 79.94051322340965), "hotel pizza hut"));
         rs.add(new Rawspot(new W3CPoint(6.8770377359851045, 79.94051322340965), "namu"));
         rs.add(new Rawspot(new W3CPoint(6.876997792309147, 79.94052529335022), "m7"));
         rs.add(new Rawspot(new W3CPoint(6.876997792309147, 79.94052529335022), "m8 kamak na"));
 
 
         final BoundingBox bb = new BoundingBox().setObj("6.876754135813088,79.93997275829315,6.8772867181561415,79.94071036577225");
 
         memory();
 
         final HotspotAnalyzer hsa = new HotspotAnalyzer(rs, (BoundingBox) bb.validateThrowAndGetThis());
         memory();
 //        final Map<Integer, Map<Integer, Hotspot>> hotspots = hsa.getHotspots();
         final Hotspot[][] hotspots = hsa.getHotspots();
 //        memory();
 //        for (final Integer place : hotspots.keySet()) {
 //            for (final Integer innerplace : hotspots.get(place).keySet()) {
 //                if (hotspots.get(place).get(innerplace).getCoordinates() != null) {
 //                    System.out.println("Hotspot:" + hotspots.get(place).get(innerplace).getCommonName() + ":" + hotspots.get(place).get(innerplace).getCoordinates().getLatitude() + "," + hotspots.get(place).get(innerplace).getCoordinates().getLongitude());
 //                }
 //            }
 //        }
 
         for (int i = 0; i < hotspots.length; i++) {
             for (int j = 0; j < hotspots[i].length; j++) {
                 final W3CPoint coords = hotspots[i][j].getCoordinates();
 
                 if (coords != null) {
                     System.out.println("Hotspot:" + hotspots[i][j].getCommonName() + ":" + coords.getLatitude() + "," + coords.getLongitude());
                 }
             }
         }
 
         memory();
 
     }
 
     private static String memory() {
         return Runtime.getRuntime().freeMemory() + BYTES;
     }
 
 }
