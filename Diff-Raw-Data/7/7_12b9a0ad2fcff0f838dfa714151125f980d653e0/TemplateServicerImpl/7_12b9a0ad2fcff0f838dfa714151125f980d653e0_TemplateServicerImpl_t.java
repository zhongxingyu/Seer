 package com.l5m.!REPLACE_STYLE!.engine.servicer;
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.l5m.common.bean.DataSourceBeanBase;
 import com.l5m.common.bean.GeneralAvailabilityModule;
 import com.l5m.common.util.dcnds.QHTCommonUtil;
 import com.l5m.customtags.beans.BaseServicerParaBean;
 import com.l5m.customtags.tags.utils.GenerateTagUtil;
 import com.l5m.!REPLACE_STYLE!.engine.worker.UserCompanyWorker;
 /**
  * make sure code as clear as possible.
  * don't make another one think.
  * if you can't do it, get away.
  * SortBeanListUtil can sort bean list.
  * GroupBeanListUtil can group a bean list to map, or .....
  * Use DAO Util to retrieve data, update...  utils. 
  * don't write sql in servicer
  * 
  * @author Ronghai
  *
  */
 public class !REPLACE_ME_FILE!ServicerImpl extends BaseServicerParaBean.AbstractBaseServicer{
     private static final long serialVersionUID = 1L;
     public static enum PANEL {
          REPORT("REPORT")  ,  
          Help("Help"),  DataAvailability("Data Availability")
         ; //Total Day/Prime * LSD/C3
         private final String label;
         private boolean enable;
         PANEL(){
             this.label = this.name();
             this.enable = true;
         }
         PANEL(String label){
             this.label = label;
             this.enable = true;
         }
         PANEL(String label,boolean enable){
             this.label = label;
             this.enable = enable;
         }
         public String getLabel() {
             return label;
         }
         public boolean isEnable() {
             return enable;
         }
         public int panelIndex(){
             return this.ordinal();
         }
     }
     
     
     
      
     @Override
     public void clearCache() {
         super.clearCache();
         this.servicerParamBean.setPanelIndex(PANEL.Help.panelIndex()); 
          
         System.gc(); 
        }
  
  
     @Override
     public void handlePaginator(int panelIndex) {
         if (this.servicerParamBean.getPaginatorStateBean(panelIndex) == null) {
             this.servicerParamBean.setPaginatorStateBean(GenerateTagUtil.initPaginatorStateBean("PaginatorStateBeanTag"), panelIndex);
             this.servicerParamBean.getPaginatorStateBean(panelIndex).setRowsPerPage(1);
         }
         
         
          if(panelIndex ==  PANEL.DataAvailability.panelIndex()  || panelIndex ==  PANEL.Help.panelIndex() ){
          }else{
              int size = 0;  
             this.servicerParamBean.getPaginatorStateBean(panelIndex).setCollectionSize(size);  
          }
           
         if (!this.servicerParamBean.getPaginatorStateBean(panelIndex).checkCurrentPageIndexIsLegal()) {
             this.servicerParamBean.getPaginatorStateBean(panelIndex).setCurrentPageIndex(0);
         } 
     }
      
     @Override
     public void init( ) {
         super.init();  
         try {
             this.servicerParamBean = new BaseServicerParaBean(this.dh, this.companyId, this.groupId , this.userId, this.userCompanyId, PANEL.values().length);
             this.servicerParamBean.initDisplayAndSortBy(); 
             try{
                 this.initTimeSpan();
             }catch(Exception e){
                 e.printStackTrace();
                 this.servicerParamBean.initTimeSpan();
             }  
         }catch(Exception e){
             e.printStackTrace();
         } 
      
         //How many tabes.... init here
         this.servicerParamBean.initTabbedPanel("SOURCE"); 
         
           
         /*
          String[] columns = new String[]{"COLUMNS"};  
          //decimal
         this.servicerParamBean.setReportDecimalControlList(GenerateTagUtil.initDecimal(columns, new int[]{3,3,0,0,0,0}), PANEL.REPORT.panelIndex()); 
          
         
         //display
         this.servicerParamBean.setColumnsMap(QHTCommonUtil.array2Map(columns), PANEL.REPORT.panelIndex());
         Set<String> set = new HashSet<String>();
         for (String s : columns) { set.add(s); } 
          this.servicerParamBean.setSelectedColumns(set, PANEL.REPORT.panelIndex());
         this.servicerParamBean.setDisplayController( GenerateTagUtil.getSelectedValueBoolean( this.servicerParamBean.getColumnsMap(   PANEL.REPORT.panelIndex() ) , set) , PANEL.REPORT.panelIndex());
         
         //sort
         this.servicerParamBean.setSortingKeysModuleStateBean(GenerateTagUtil.initSortKeyModule(columns, "sortKey", new int[]{0}, new boolean[]{true}), PANEL.REPORT.panelIndex());
         */
      } 
 
      
  
     
   
     public void initGeneralAvailabilityModule() {
         DataSourceBeanBase dataSourceBean = null;
         try {
             dataSourceBean = UserCompanyWorker.getDataSourceBean(dh, companyId);
         } catch (SQLException e1) {
             e1.printStackTrace();
         } catch (ClassNotFoundException e1) {
             e1.printStackTrace();
         }
         this.session.setAttribute(UserCompanyWorker.DATA_SOURCE_BEAN, dataSourceBean);
         boolean[] displaySetting = new boolean[GeneralAvailabilityModule.GROUP_COUNT];
         if (dataSourceBean != null) {
             displaySetting[GeneralAvailabilityModule.GROUP_MP] = dataSourceBean.hasMP() ;
             displaySetting[GeneralAvailabilityModule.GROUP_TNS] = dataSourceBean.hasTNS()  ;
             displaySetting[GeneralAvailabilityModule.GROUP_MIT] = true;
             displaySetting[GeneralAvailabilityModule.GROUP_ACM_MIT] = true;
             if (dataSourceBean.hasMXM()) {
                 displaySetting[GeneralAvailabilityModule.GROUP_MXM] = true;
             }
         }
         try {
             this.generalAvailabilityModule = new GeneralAvailabilityModule(this.dh, this.groupId, companyId, dataSourceBean, displaySetting);
         } catch (SQLException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
  
       
     
 
     
 
 
 
 
     @SuppressWarnings("unchecked")
     private void initTimeSpan() throws ClassNotFoundException, SQLException {  
         this.servicerParamBean.initTimeSpan(); 
      }
 
   
     
     public void retrieveData() throws SQLException, ClassNotFoundException, CloneNotSupportedException { 
          // don't write sql in this method,
         // when retrieve something from database, you can create a DAO Util. Use a better name to  identifine it, and another programmer aslo can use the DAO Util.
         // all parameters passed into DAO Utill, should be wrapped in a parameterBean. if you add an additional parameter, you don't need modify/adjust the method.
         // you can add a new attribute in your parameter bean, and use it in DAOUtil
         // 
         
     }
      
     
     @Override
     public void updateDisplay(){
          // nothing to do, here, don't use this method to update display or something.
         // 
         // for flat report, you can use FlatViewUtil to generate.
         //
         
     }
     
     @Override
     public void sort(int ...panelIndexes){ 
         if( panelIndexes == null || panelIndexes .length == 0){
             panelIndexes = new int[]{this.getPanelIndex() } ;
         }
        /*for( int p : panelIndexes){
             this.servicerParamBean.sort (p);     
        }*/
         this.servicerParamBean.parseSort(panelIndexes);
         //use SortBeanListUtil to sort bean list.
         // Generelly application has two different data struct.  bean list or dataTable. 
         // every application can convert to bean list or dataTable.
         // don't use map to store result except map is better.
         
     }
       
     
     @Override
     public void processFilter() {  
     }
  
      
     
     
 }
