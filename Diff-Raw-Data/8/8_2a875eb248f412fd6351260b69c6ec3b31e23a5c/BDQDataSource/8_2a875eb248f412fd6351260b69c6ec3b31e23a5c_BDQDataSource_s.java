 package edu.wustl.cab2b.client.ui.viewresults;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource;
 import edu.wustl.cab2b.client.ui.controls.LazyTable.CacheInterface;
 import edu.wustl.cab2b.client.ui.controls.LazyTable.Page;
 import edu.wustl.cab2b.client.ui.controls.LazyTable.PageDimension;
 import edu.wustl.cab2b.client.ui.controls.LazyTable.PageInfo;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import static edu.wustl.cab2b.common.ejb.EjbNamesConstants.UTILITY_BEAN;
 import edu.wustl.cab2b.common.BusinessInterface;
 import edu.wustl.cab2b.common.ejb.utility.UtilityBusinessInterface;
 import edu.wustl.cab2b.common.ejb.utility.UtilityHomeInterface;
 import edu.wustl.cab2b.common.queryengine.result.I3DDataRecord;
 import edu.wustl.cab2b.common.queryengine.result.IPartiallyInitialized3DRecord;
 import edu.wustl.cab2b.common.queryengine.result.IPartiallyInitializedRecord;
 import edu.wustl.cab2b.common.queryengine.result.I3DDataRecord.LazyParams;
 
 /**
  * BDQDataSource
  * @author rahul_ner
  */
 public class BDQDataSource extends AbstractLazyDataSource<IPartiallyInitialized3DRecord<?, ?>> {
 
     /**
      * this is required to find dimension of the cube
      */
     private IPartiallyInitialized3DRecord<?, ?> uninitializedRecord;
 
     /**
      * Constructor
      * @param uninitializedRecord uninitialized record
      * @param pageDimension page dimension
      * @param cache
      */
     public BDQDataSource(
             IPartiallyInitialized3DRecord<?, ?> uninitializedRecord,
             PageDimension pageDimension,
             CacheInterface<?> cache) {
         super(pageDimension, cache);
         this.uninitializedRecord = uninitializedRecord;
     }
 
     /** 
      * Returns row count
      * @return row count 
      * @see edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource#getRowCount()
      */
     public int getRowCount() {
         return uninitializedRecord.getDim3Labels().length;
     }
 
     /**      
      * Returns column count
      * @return column count 
      * @see edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource#getColumnCount()
      */
     public int getColumnCount() {
         return uninitializedRecord.getDim1Labels().length * uninitializedRecord.getDim2Labels().length;
     }
 
     /**
      * @param columnNo column number
      * @return column name
      * @see edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource#getColumnName(int)
      */
     public String getColumnName(int columnNumber) {
         int dim2Size = uninitializedRecord.getDim2Labels().length;
 
         int dim1Index = columnNumber / dim2Size;
         int dim2Index = columnNumber - dim1Index;
 
         return currentPage.getData().getDim2Labels()[dim2Index] + "_"
                 + currentPage.getData().getDim1Labels()[dim1Index];
 
     }
 
     /**
      * @param rowNo row number 
      * @param columnNo column number
      * @return Data
      * @see edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource#extractDataFromPage(int, int)
      */
     public Object extractDataFromPage(int rowNumber, int columnNumber) {
         int dim2Size = currentPage.getData().getDim2Labels().length;
 
         int dim1Index = columnNumber / dim2Size;
         int dim2Index = columnNumber - dim1Index;
         int dim3Index = rowNumber;
         return currentPage.getData().getCube()[dim1Index][dim2Index][dim3Index];
     }
 
     /**
      * @param pageInfo Page Information
      * @return IPartiallyInitialized3DRecord
      * @see edu.wustl.cab2b.client.ui.controls.LazyTable.AbstractLazyDataSource
      * #fetchPageData(edu.wustl.cab2b.client.ui.controls.LazyTable.PageInfo)
      */
     public Page<IPartiallyInitialized3DRecord<?, ?>> fetchPageData(PageInfo pageInfo) {
         List<LazyParams.Range> rangeList = getRanges(uninitializedRecord.getCube(), pageInfo.getStartX(),
                                                      pageInfo.getStartY());
         LazyParams lazyParams = new I3DDataRecord.LazyParams(rangeList);
         try {
             UtilityBusinessInterface utilityBean = getUtilityBean();
            IPartiallyInitializedRecord<?, ?> record = utilityBean.getView(uninitializedRecord.handle(),lazyParams);
            IPartiallyInitialized3DRecord<?, ?> newRecord = (IPartiallyInitialized3DRecord<?, ?>) record;
             return new Page<IPartiallyInitialized3DRecord<?, ?>>(pageInfo, newRecord);
         } catch (RemoteException e) {
             CommonUtils.handleException(e, NewWelcomePanel.getMainFrame(), true, true, true, false);
         }
         return null;
     }
 
     /**
      * @param cube
      * @param startRow
      * @param startColumn
      * @return
      */
     private List<LazyParams.Range> getRanges(Object[][][] cube, int startRow, int startColumn) {
 
         int dimi = cube.length;
         int dimj = cube[0].length;
         int dimk = cube[0][0].length;
 
         List<LazyParams.Range> ranges = new ArrayList<LazyParams.Range>();
 
         int dx = (startRow + pageDimension.getNoOfRows());
         int dy = (startColumn + pageDimension.getNoOfColumns());
 
         if (dy > dimi * dimj) {
             dy = dimi * dimj;
         }
 
         int starti = startColumn / dimj;
         int endi = dy / dimj;
 
         int startj = startColumn - (starti * dimj);
         int endj = dy - ((endi - 1) * dimj);
 
         int startk = startRow;
         int endk = dx > dimk ? dimk : dx;
 
         boolean moreThanOnePage = (endi - starti) > 1;
         boolean moreThanTwoPages = (endi - starti) > 2;
         boolean endFullPage = (endj == dimj);
 
         ranges.add(new LazyParams.Range(starti, starti + 1, startj, moreThanOnePage ? dimj : endj, startk, endk));
 
         if (moreThanOnePage) {
             ranges.add(new LazyParams.Range(starti + 1, endFullPage ? endi : (endi - 1), 0,
                     moreThanTwoPages ? dimj : endj, startk, endk));
 
             if (moreThanTwoPages && !endFullPage) {
                 ranges.add(new LazyParams.Range(endi - 1, endi, 0, endj, startk, endk));
             }
         }
 
         return ranges;
     }
 
     /**
      * Method to get bio-data cube range for the given columnNo
      * @param columnNo
      * @return
      */
     private LazyParams.Range getCloumnRage(int columnNo) {
         Object[][][] cube = uninitializedRecord.getCube();
         int dimj = cube[0].length;
         int dimk = cube[0][0].length;
         int starti = columnNo / dimj;
         int endi = starti + 1;
         int startj = columnNo - (starti * dimj);
         int endj = startj + 1;
         int startk = 0;
         int endk = dimk;
         return new LazyParams.Range(starti, endi, startj, endj, startk, endk);
     }
 
     /**
      * @param selectedColumns selectedColumns
      * @return IPartiallyInitialized3DRecord
      */
     public IPartiallyInitialized3DRecord<?, ?> getColumnsData(int[] selectedColumns) {
 
         List<LazyParams.Range> rangeList = new ArrayList<LazyParams.Range>();
         for (int i = 0; i < selectedColumns.length; i++) {
             rangeList.add(getCloumnRage(selectedColumns[i]));
         }
         LazyParams lazyParams = new I3DDataRecord.LazyParams(rangeList);
 
         try {
             UtilityBusinessInterface utilityBean = getUtilityBean();
            return (IPartiallyInitialized3DRecord<?, ?>) utilityBean.getView(uninitializedRecord.handle(),lazyParams);
         } catch (RemoteException e) {
             CommonUtils.handleException(e, NewWelcomePanel.getMainFrame(), true, true, true, false);
         }
 
         return null;
     }
 
     /**
      * @return Unique Record Values
      */
     public List<TreeSet<Comparable<?>>> getUniqueRecordValues() {
         List<TreeSet<Comparable<?>>> recordValues = null;
         Set<AttributeInterface> allAttributes = uninitializedRecord.getAttributes();
         if (!allAttributes.isEmpty()) {
             Long entityId = allAttributes.iterator().next().getEntity().getId();
             try {
                 UtilityBusinessInterface utilityBusinessInterface = getUtilityBean();
                 recordValues = utilityBusinessInterface.getUniqueRecordValues(entityId);
             } catch (RemoteException e) {
                 CommonUtils.handleException(e, NewWelcomePanel.getMainFrame(), true, true, true, false);
             }
         }
         return recordValues;
     }
     private UtilityBusinessInterface getUtilityBean() {
         BusinessInterface bus =  CommonUtils.getBusinessInterface(UTILITY_BEAN,UtilityHomeInterface.class);
         return (UtilityBusinessInterface)bus;
     }
 }
