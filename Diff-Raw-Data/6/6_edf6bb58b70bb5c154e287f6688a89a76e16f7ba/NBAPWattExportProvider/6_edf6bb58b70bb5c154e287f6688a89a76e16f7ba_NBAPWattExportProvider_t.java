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
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.amanzi.awe.neighbours.gpeh.Calculator3GPPdBm.ValueType;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.apache.log4j.Logger;
 import org.hsqldb.lib.StringUtil;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 
 
 /**
  * <p>
  *Export provider for Nbap reports in Watt
  * </p>
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class NBAPWattExportProvider extends ExportProvider3GPP {
     private  static final Logger LOGGER = Logger.getLogger(NBAPWattExportProvider.class);
     protected Integer power;
 
     /**
      * Instantiates a new nBAP watt export provider.
      *
      * @param dataset the dataset
      * @param network the network
      * @param service the service
      * @param value3gpp the value3gpp
      * @param statRelation the stat relation
      * @param period the period
      * @param dataname the dataname
      * @param luceneService the lucene service
      */
     public NBAPWattExportProvider(Node dataset, Node network, GraphDatabaseService service, ValueType value3gpp, RelationshipType statRelation, CallTimePeriods period,
             String dataname, LuceneIndexService luceneService) {
         super(dataset, network, service, value3gpp, statRelation, period, dataname, luceneService);
     }
 
     @Override
     protected void createHeader() {
         super.createHeader();
         headers.add(1, "maximumTransmissionPower(W)");
     }
 
     @Override
     protected void createArrayHeader() {
         for (double i = 0.1; i <1; i += 0.1) {
             headers.add(String.format("%1.1f",i));
         }
         for (int i = 1; i <= 100; i ++) {
             headers.add(String.valueOf(i));
         }
     }
     @Override
     public List<Object> getNextLine() {
         Pair<Node, int[]> values = model.next();
         List<Object> result=new LinkedList<Object>();
         String name = (String)values.getLeft().getProperty("userLabel", "");
         if (StringUtil.isEmpty(name)) {
             name = NeoUtils.getNodeName(values.getLeft(), service);
         }
         result.add(name);
         power = (Integer)values.getLeft().getProperty("maximumTransmissionPower", null);
         result.add(power);
         Calendar calendar=Calendar.getInstance();
         calendar.setTimeInMillis(computeTime);
         result.add(dateFormat.format(calendar.getTime()));
         result.add(dateFormat2.format(calendar.getTime()));
         result.add(period.getId());
         int[] array=values.getRight();
         processArray(result, array); 
         return result;
     }
 
     @Override
     protected void processArray(List<Object> result, int[] array) {
         int startElem = result.size();
         for (int i = 0; i < 110; i++) {
             result.add(0);
         }
         if (power == null) {
             return;
         }
         double maxTrPowWatt = Math.pow(10, -3) * Math.pow(10, power / 100);
             for (int i = value3gpp.getMin3GPP(); i <= value3gpp.getMax3GPP(); i++) {
                 int txpower = (int)Math.ceil(maxTrPowWatt * value3gpp.getRightBound(i)/ 1000);// (txpower=TxPower                                                                           // i - 0-1000
                //analyse txower*10
                txpower=txpower*10;                                                          // i/1000);
                 if (txpower < 0 || txpower > 1000) {
                     LOGGER.error(String.format("Cell %s. Wrong TxPower %s", "", txpower / 10));
                     continue;
                 }
                 int value =array[i];
                 if (value != 0) {
                     int ind = 0;
                     if (txpower < 10) {
                         ind = startElem + txpower;
                     } else {
                         ind = startElem + 8 + (int)Math.ceil(txpower / 10);
                     }
                    result.set(ind, (Integer)result.get(ind) + value);
                 }
 
             }
     }
 }
