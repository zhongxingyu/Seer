 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.neighbours.gpeh;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.preferences.NeoCorePreferencesConstants;
 import org.amanzi.neo.core.utils.GpehReportUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.apache.log4j.Logger;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 /**
  * <p>
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class IntraMatrixProvider extends AbstractGpehExportProvider {
     private static final Logger LOGGER = Logger.getLogger(IntraMatrixProvider.class);
     protected Long computeTime;
     protected Iterator<CellInfo> rowIter;
     protected RrcModel model;
 
     protected RrcModelHandler modelHandler;
 
     public IntraMatrixProvider(Node dataset, Node network, GraphDatabaseService service, CallTimePeriods period, LuceneIndexService luceneService) {
         super(dataset, network, GpehRelationshipType.RRC, period, service, luceneService);
         computeTime = startTime;
 
     }
     @Override
     protected void init() {
         super.init();
         defineHandler();
         defineModel();
         defineRowIterator();
         loadModel();
     }
 
     /**
      *
      */
     protected void defineRowIterator() {
         rowIter = new RowIterator(statRoot);
     }
 
     /**
      *
      */
     protected void loadModel() {
         model.load(network, rowIter, service, luceneService);
     }
 
     /**
      *
      */
     protected void defineModel() {
         model = new RrcModel<RrcModelHandler>(modelHandler);
     }
 
 
     protected void defineHandler() {
         modelHandler = new IntraModelHandler(period,service);
     }
 
     @Override
     public boolean isValid() {
         return true;
     }
 
     @Override
     public boolean hasNextLine() {
         while (computeTime < minMax.getRight() || computeTime == startTime) {
             if (modelHandler.getComputeTime() != computeTime) {
                 modelHandler.setTime(computeTime);
                 model.clearIter();
             }
             while (!modelHandler.haveData()) {
                 if (!model.defineNextData()) {
                     break;
                 }
             }
             if (modelHandler.haveData()) {
                 return true;
             }
             computeTime = period.addPeriod(computeTime);
         }
         return false;
     }
 
     @Override
     public String getDataName() {
         return "INTRA-FREQUENCY ICDM";
     }
 
     @Override
     // hasNextLine() must be used before calling of this method!
     public List<Object> getNextLine() {
         if (!modelHandler.haveData()) {
             hasNextLine();
         }
         List<Object> result = modelHandler.formLine();
         modelHandler.clearData();
         return result;
     }
 
     @Override
     protected void createHeader() {
         headers = new LinkedList<String>();
         headers.add("Serving cell name");
         headers.add("Serving PSC");
         headers.add("Interfering cell name");
         headers.add("Interfering PSC");
         headers.add("Defined NBR");
         headers.add("Distance");
         headers.add("Tier Distance");
         headers.add("# of MR for best cell");
         headers.add("# of MR for Interfering cell");
         headers.add("EcNo Delta1");
         headers.add("EcNo Delta2");
         headers.add("EcNo Delta3");
         headers.add("EcNo Delta4");
         headers.add("EcNo Delta5");
         headers.add("RSCP Delta1");
         headers.add("RSCP Delta2");
         headers.add("RSCP Delta3");
         headers.add("RSCP Delta4");
         headers.add("RSCP Delta5");
         headers.add("Position1");
         headers.add("Position2");
         headers.add("Position3");
         headers.add("Position4");
         headers.add("Position5");
     }
 
     public static class RowIterator implements Iterator<CellInfo> {
         protected Iterator<Relationship> bestCellIterator;
         protected Iterator<Relationship> interferenceCellIterator;
         protected Relationship bestCellRel;
         protected Node servingCell;
         private Integer ci;
         private Integer rnc;
 
         public RowIterator(Node statRoot) {
             bestCellIterator = statRoot.getRelationships(Direction.OUTGOING).iterator();
             interferenceCellIterator = getemptyIterator();
         }
 
         /**
          * @return
          */
         protected Iterator<Relationship> getemptyIterator() {
             return Collections.<Relationship> emptyList().iterator();
 
         }
 
         @Override
         public boolean hasNext() {
             if (interferenceCellIterator.hasNext()) {
                 return true;
             } else if (!bestCellIterator.hasNext()) {
                 return false;
             }
             defineIterator();
             return interferenceCellIterator.hasNext();
         }
 
         protected void defineIterator() {
             while (!interferenceCellIterator.hasNext() && bestCellIterator.hasNext()) {
                 bestCellRel = bestCellIterator.next();
                 interferenceCellIterator = bestCellRel.getEndNode().getRelationships(Direction.OUTGOING).iterator();
             }
             String[] ciRnc = bestCellRel.getType().name().split("_");
             ci = Integer.valueOf(ciRnc[0]);
             rnc = Integer.valueOf(ciRnc[1]);
         }
 
         @Override
         public CellInfo next() {
             if (!interferenceCellIterator.hasNext() && bestCellIterator.hasNext()) {
                 defineIterator();
             }
             Relationship interfRel = interferenceCellIterator.next();
             String psc = interfRel.getType().name();
             return new CellInfo(ci, rnc, psc, bestCellRel.getEndNode(), interfRel.getEndNode());
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
     }
 
     /**
      * <p>
      * Information about best cell and interference cell
      * </p>
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public static class CellInfo {
         private Integer ci;
         private Integer rnc;
         private String psc;
         private Node bestCellInfo;
         private Node interfCellInfo;
 
         /**
          * Instantiates a new cell info.
          * 
          * @param ci the ci
          * @param rnc the rnc
          * @param psc the psc
          * @param bestCellInfo the best cell info
          * @param interfCellInfo the interf cell info
          */
         public CellInfo(Integer ci, Integer rnc, String psc, Node bestCellInfo, Node interfCellInfo) {
             super();
             this.ci = ci;
             this.rnc = rnc;
             this.psc = psc;
             this.bestCellInfo = bestCellInfo;
             this.interfCellInfo = interfCellInfo;
         }
 
         public Node getBestCellInfo() {
             return bestCellInfo;
         }
 
         public Node getInterfCellInfo() {
             return interfCellInfo;
         }
 
         public Integer getCi() {
             return ci;
         }
 
         public void setCi(Integer ci) {
             this.ci = ci;
         }
 
         public Integer getRnc() {
             return rnc;
         }
 
         public void setRnc(Integer rnc) {
             this.rnc = rnc;
         }
 
         public String getPsc() {
             return psc;
         }
 
         public void setPsc(String psc) {
             this.psc = psc;
         }
 
     }
 
     public static class RrcModel<M extends RrcModelHandler> {
         protected Integer maxRange;
         protected final M modelHandler;
 
         protected String scrCodeIndName;
 
         protected Map<CellNodeInfo, Set<InterfCellInfo>> cache = new LinkedHashMap<CellNodeInfo, Set<InterfCellInfo>>();
 
         protected Iterator<CellNodeInfo> bestCellIterator;
 
         protected Iterator<InterfCellInfo> interfCellIter;
 
         protected CellNodeInfo bestCellInfo;
 
         public RrcModel(M modelHandler) {
             this.modelHandler = modelHandler;
         }
 
         /**
          * Load.
          * 
          * @param network the network
          * @param rowIter the row iter
          * @param service the service
          * @param luceneService the lucene service
          */
         public void load(Node network, Iterator<CellInfo> rowIter, GraphDatabaseService service, LuceneIndexService luceneService) {
             scrCodeIndName = NeoUtils.getLuceneIndexKeyByProperty(network, GpehReportUtil.PRIMARY_SCR_CODE, NodeTypes.SECTOR);
             Transaction tx = NeoUtils.beginTx(service);
             try {
                 while (rowIter.hasNext()) {
                     CellInfo cell = rowIter.next();
                     Node bestCell = NeoUtils.findSector(network, cell.getCi(), String.valueOf(cell.getRnc()), luceneService, service);
                     if (bestCell == null) {
                         LOGGER.warn(String.format("Data not included in statistics! Not found sector with ci=%s, rnc=%s", cell.getCi(), cell.getRnc()));
                         continue;
                     }
                     CellNodeInfo bci = findInCache(bestCell, cache.keySet());
                     if (bci == null) {
                         bci = new CellNodeInfo(bestCell, cell.getBestCellInfo());
                         if (!bci.setupLocation()) {
                             LOGGER.warn(String.format("Data not included in statistics! Not found location for best cell %s", bestCell));
                             continue;
                         }
                         cache.put(bci, new LinkedHashSet<InterfCellInfo>());
                     }
                     Set<InterfCellInfo> cacheInt = cache.get(bci);
                     InterfCellInfo sector = findInCache(bestCell, cacheInt);
                     if (sector == null) {
                         sector = findClosestSector(bci, cell, network, service, luceneService);
                         if (sector == null) {
                             continue;
                         }
                         cacheInt.add(sector);
                     }
                 }
             } finally {
                 tx.finish();
             }
         }
 
         /**
          * Find in cache.
          * 
          * @param <E> the element type
          * @param bestCell the best cell
          * @param cache the cache
          * @return the e
          */
         protected <E extends CellNodeInfo> E findInCache(Node bestCell, Collection<E> cache) {
             for (E info : cache) {
                 if (info.getCellSector().equals(bestCell)) {
                     return info;
                 }
             }
             return null;
         }
 
         /**
          * Find closest sector.
          * 
          * @param bestCell the best cell
          * @param cell the cell
          * @param network the network
          * @param service the service
          * @param luceneService the lucene service
          * @return the interf cell info
          */
         private InterfCellInfo findClosestSector(CellNodeInfo bestCell, CellInfo cell, Node network, GraphDatabaseService service, LuceneIndexService luceneService) {
             if (bestCell.getLat() == null || bestCell.getLon() == null) {
                 LOGGER.debug("bestCell " + bestCell.getCellSector() + " do not have location");
                 return null;
             }
             InterfCellInfo result = null;
             IndexHits<Node> nodes = luceneService.getNodes(scrCodeIndName, String.valueOf(cell.getPsc()));
             for (Node sector : nodes) {
                 if (result == null) {
                     result = new InterfCellInfo(sector, cell.getInterfCellInfo(), cell.getPsc());
                     if (result.setupLocation()) {
                         // TODO check correct distance! maybe use CRS for this
 
                         result.setDistance(calculateDistance(bestCell, result));
                         if (result.getDistance() > getMaxRange()) {
                            LOGGER.debug("sector " + result + " have too big distance: " + bestCell.getDistance());
                             result = null;
                         }
                     } else {
                         LOGGER.debug("sector " + result + " do not have location");
                         result = null;
                     }
                 } else {
                     InterfCellInfo candidate = new InterfCellInfo(sector, cell.getInterfCellInfo(), cell.getPsc());
                     if (candidate.setupLocation()) {
                         candidate.setDistance(calculateDistance(bestCell, candidate));
                         if (candidate.getDistance() < result.getDistance()) {
                             result = candidate;
                         }
                     }
                 }
             }
             return result;
         }
         /**
         *
         * @return
         */
        public double getMaxRange() {
            if (maxRange==null){
                maxRange = NeoCorePlugin.getDefault().getPreferenceStore().getInt(NeoCorePreferencesConstants.MAX_SECTOR_DISTANSE);
            }
            return maxRange;
        }
         public boolean defineNextData() {
             modelHandler.clearData();
             while (interfCellIter.hasNext() || bestCellIterator.hasNext()) {
                 while (!interfCellIter.hasNext() && bestCellIterator.hasNext()) {
                     bestCellInfo = bestCellIterator.next();
                     interfCellIter = cache.get(bestCellInfo).iterator();
                 }
                 if (!interfCellIter.hasNext()) {
                     return false;
                 }
                 if (modelHandler.setData(bestCellInfo, interfCellIter.next())) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * @param computeTime
          */
         public void clearIter() {
             modelHandler.clearData();
             bestCellIterator = cache.keySet().iterator();
             interfCellIter = Collections.<InterfCellInfo> emptySet().iterator();
 
         }
 
         /**
          * Calculate distance.
         * 
          * @param bestCell the best cell
          * @param result the candidate
          * @return the distance between sectors
          */
         private Double calculateDistance(CellNodeInfo bestCell, CellNodeInfo result) {
            // TODO refactor use CRS of network
            return Math.sqrt(Math.pow(bestCell.getLat() - result.getLat(), 2) + Math.pow(bestCell.getLon() - result.getLon(), 2));
         }
     }
 
 
 }
