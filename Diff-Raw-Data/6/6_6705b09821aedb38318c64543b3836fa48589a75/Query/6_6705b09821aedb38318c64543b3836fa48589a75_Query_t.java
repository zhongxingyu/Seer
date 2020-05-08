 
 package Utilitarios;
 
 import Utilitarios.Data;
 
 import java.sql.*;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 public class Query extends ConexionBd{    
     
     private DefaultTableModel datos; 
     private ResultSet rs = null;
     private Statement s = null;
     private PreparedStatement  pt = null;
     private DefaultComboBoxModel MChoice;
     private Data dt;
     private String _error = "Utilitarios_Query_";
     private String identify="";
 
     public Query(){
     }
     public Query(String identify){
         this.identify = identify;
         System.out.println("VALOR: " + this.identify);
     }
     public void setIdentify(String identify) {
         this.identify = identify;
        System.out.println("VALOR: " + this.identify);
     }
     
     public  PreparedStatement sqlRegister(String Table){
         pt = null;
         try{
             getConexion();
             String query;
             String campos="";
             String values="";
             Statement s = null;
             s = conexion.createStatement();
             rs = s.executeQuery("select * from "+Table +" LIMIT 1 ");
             //Llenado Cabecera Jtable
             ResultSetMetaData meta = rs.getMetaData();
             int nCols = meta.getColumnCount();
             
             for(int i=1;i<=nCols;i++){
                 if(!meta.isAutoIncrement(i)){
                     campos = campos + meta.getColumnName(i);
                     values = values + "?";
                     if(i<nCols){
                         campos = campos + ",";
                         values = values + ","; 
                     }
                 }
             }
             query= "insert into "+Table+" ("+campos+") values("+values+")";
             
             pt  = conexion.prepareStatement(query);
             rs.close();
             return pt;
             
         }
         catch(Exception e){
             System.out.println(_error+"sqlRegister: "+e);
             return pt;
         }
     }
     /*
      * Arma busqueda
      */
     public String sqlSearch(){
         
         String query="";
     
         return query;
     }
      /*
      * Arma actualizacion
      */
     public PreparedStatement sqlUpdate(String Table){
         pt = null;
         
         try{
             getConexion();
             String query;
             String condi="";
             String id = "id";
             Statement s = null;
             s = conexion.createStatement();
             rs = s.executeQuery("select * from "+Table+" LIMIT 1 ");
             //Llenado Cabecera Jtable
             ResultSetMetaData meta = rs.getMetaData();
             int nCols = meta.getColumnCount();
             query = "update "+Table+" set ";
             
             for(int i=1;i<=nCols;i++){
                 if(!meta.isAutoIncrement(i)){
                     for(int x=0;x<Utilitarios.Data.G_EXCLUDE.length;x++){
                         if(meta.getColumnName(i).equals(Utilitarios.Data.G_EXCLUDE[x])){
                             //Este campo ha sido excluido
                         } else {
                             query = query + meta.getColumnName(i)+ "=?,";
                         }
                     }  
                 }
                 else{
                     id =  meta.getColumnName(i);
                 }
             }
             if(id.equals("NMID")){
             id="\"NMID\"";
             }
             query = query + " where "+id+"= ?";
             query = query.replace(", "," ");
             pt  = conexion.prepareStatement(query);
             rs.close();
             return pt;
             
         }
         catch(Exception e){
             System.out.println(_error+"sqlUpdate: "+e);
             return pt;
         }
     }
      /*
      * Arma eliminacion
      */
     public PreparedStatement sqlDelete(String Table){
         pt = null;
         
         try{
             getConexion();
             String query;
             String id = "id";
             Statement s = null;
             s = conexion.createStatement();
             rs = s.executeQuery("select * from "+Table+" LIMIT 1 ");
             //Llenado Cabecera Jtable
             ResultSetMetaData meta = rs.getMetaData();
             int nCols = meta.getColumnCount();
 
             query = "delete from  "+Table+" where  ";
             
             for(int i=1;i<=nCols;i++){
                 if(meta.isAutoIncrement(i)){
                     id =  meta.getColumnName(i);
                 }
             }
             if(id.equals("NMID")){
             id="\"NMID\"";
             }
             query = query + id + "=?";
             pt  = conexion.prepareStatement(query);
             rs.close();
             return pt;
             
         }
         catch(Exception e){
             System.out.println(_error+"sqlDelete: "+e);
             return pt;
         }
     }
     
     /*
      * Armado de la consulta de listado y consulta
      */
     public String getQueryList(String[] args, String Table, String[][] Filter){
         String type;
         String camp;
         String[] temp;
         String[] kargs =  new String[args.length];
         
         for(int i=0;i<args.length;i++){
             kargs[i] = args[i];
         }
         
         String qs = "select ";
         for(int i=0;i<kargs.length;i++)
         {
             temp = kargs[i].split("/");
             if(temp.length>=1){
                 kargs[i] = temp[0];
             }
             qs = qs + kargs[i];
             qs = qs + ",";
         }
         qs = qs +" from "+Table;
         qs = qs.replace(", "," ");
         
         if(Filter.length>0){
             qs = qs + " where ";
             for(int i=0;i<Filter.length;i++)
             {
                 type = Filter[i][0].substring(0, 3);
                 camp = Filter[i][0];
                 camp = camp.substring(4, camp.length());
                 
                 switch(type){
                     case "int":qs = qs + " " + camp + "=" + Filter[i][1] + " ";
                          break;
                     case "equ":qs = qs + " " + camp + "='" + Filter[i][1] + "' ";
                          break;
                     default:qs = qs + " " + Filter[i][0] + " like '%" + Filter[i][1] + "%' ";
                          break;
                 }
                 if (Filter.length != i+1) { 
                     qs = qs + "and";
                 }
             }
         }
         return qs;
     }
     /*
      * Clase generica para realizar consulas en Jtable
      */
     public  DefaultTableModel getAll(String[] args, String Table, String[][] Filter){
         try{
             ResultSet rs_all = null;
             datos = new DefaultTableModel();
             getConexion();
             String id;
             Object[] fila; 
             String[] temp;
             String tbl;
             s = conexion.createStatement();
             String qs = getQueryList(args,Table, Filter);
             System.out.println("Consulta : " + qs);
             rs_all = s.executeQuery(qs);
             //Llenado Cabecera Jtable
             ResultSetMetaData meta = rs_all.getMetaData();
             int nCols = meta.getColumnCount();
             String[] colum = new String[nCols];
             for (int i=0; i<nCols; ++i) {
                 datos.addColumn(meta.getColumnName(i+1));
                 colum[i]=meta.getColumnName(i+1);
                 id = meta.getColumnName(i+1).substring(0, 2);
             }
             //Llenado registro Jtable
             fila = new Object[nCols];
             rs_all = s.executeQuery(qs);
             while(rs_all.next()){
                 for(int i=0; i<nCols; ++i){   
                         temp = args[i].split("/");
                         if(temp.length>1){
                             tbl = temp[1];
                             this.identify = "str_"+temp[2];
                             fila[i] =  idChoice(tbl,colum[i], String.valueOf(rs_all.getObject(i+1)));
                         }else {
                             fila[i] = rs_all.getObject(i+1);
                         }
                 }
                 datos.addRow(fila);
             }
            //Cerrando conexion
            rs_all.close();
            closeConexion(); 
         }
         catch(Exception e){
             System.out.println(_error+"getAll: "+e);
         }
         
         return datos;
         }
     /*
      * Autocarga de los estados activo, inactivo
      */
     public void loadState(JComboBox cmbState, boolean value){
         try{
             dt = new Data();
             MChoice = new DefaultComboBoxModel();
             if(value == false){
                 MChoice.addElement(dt.G_STATES[0]);
                 MChoice.addElement(dt.G_STATES[1]);
             }
             else{
                 MChoice.addElement(dt.G_STATES[1]);
                 MChoice.addElement(dt.G_STATES[0]);
             }
                 
             cmbState.setModel(MChoice);   
         }
         catch(Exception e){
             System.out.println(_error+"loadState: "+e);
         }
     }
     
     public int loadGlobal(int op, JComboBox cmbType, int value){
         int id = 0;
         try{
             dt = new Data();
             int tamaño=0;
             String G_global[];
                 switch(op){
                     case 1:
                         tamaño = dt.G_TYPEHOR.length;
                         G_global = new String[tamaño];
                         G_global = dt.G_TYPEHOR;
                             ;break;
                     case 2: 
                         tamaño = dt.G_DIAS.length;
                         G_global = new String[tamaño];
                         G_global = dt.G_DIAS;
                         ;break;
                     case 3: 
                         tamaño = dt.G_TIPOH.length;
                         G_global = new String[tamaño];
                         G_global = dt.G_TIPOH;
                         ;break;
                     default : G_global = new String[0];
                     break;
                 }
             if(value > 0){
                 MChoice = new DefaultComboBoxModel();
                 MChoice.addElement(G_global[value]);
                 
                 for(int i=1;i<G_global.length;i++){
                     if(value!=i){
                       MChoice.addElement(G_global[i]);  
                     }
                 }
                 cmbType.setModel(MChoice);     
             } else {
                 for(int i=1;i<G_global.length;i++){
                     if(cmbType.getSelectedItem() == G_global[i]){
                         return i;
                     }
                 }
             }
         }
         catch(Exception e){
             System.out.println(_error+"loadType: "+e);
         }
         return id;
     }
      /*
      * Autocarga de los combos
      */
     public void loadChoice(JComboBox cmbChoice, String Tbl, String Campo){
         try{
             getConexion();
             MChoice = new DefaultComboBoxModel();
             s = conexion.createStatement();
             rs = s.executeQuery("select " +Campo+ " from " +Tbl);
             while(rs.next()) {
               MChoice.addElement(rs.getString(Campo));
             }     
             
             cmbChoice.setModel(MChoice);   
             closeConexion(); 
         }
         catch(Exception e){
             System.out.println(_error+"loadChoice: "+e);
         }
     }
        /*
      * Autocarga de los combos
      */
     public void loadChoiceDefault(JComboBox cmbChoice, String Tbl, String Campo, int value){
         try{
             boolean op = false;
             getConexion();
             MChoice = new DefaultComboBoxModel();
             s = conexion.createStatement();
            System.out.println("Identificador : "+this.identify);
             if("".equals(this.identify)){
                 this.identify = getIdentify(Tbl);
                 op = true;
             }
             System.out.println("select " +Campo+ " from " +Tbl + " where " + this.identify + "=" +value);
             rs = s.executeQuery("select " +Campo+ " from " +Tbl + " where " + this.identify + "=" +value);
             while(rs.next()) {
               MChoice.addElement(rs.getString(Campo));
               
             }
             if(op){
                 rs = s.executeQuery("select " +Campo+ " from " +Tbl + " where " + this.identify + "!=" +value);
                 while(rs.next()) {
                   MChoice.addElement(rs.getString(Campo));
                 } 
             }
             this.identify = "";
             cmbChoice.setModel(MChoice);   
             closeConexion(); 
         }
         catch(Exception e){
             System.out.println(_error+"loadChoiceDefault: "+e);
         }
     }
      
      /*
      * Me devuelve el id de 
      */
     public String idChoice(String Tbl, String Campo, String value){
         String campo = "0";
         String type="int";
         try{
             getConexion();
             String identify = "";
             String query="";
             if("".equals(this.identify)){
                 identify = getIdentify(Tbl);
             } else {
                 identify = this.identify;
                 type = identify.substring(0, 3);
                 switch(type){
                     case "int":identify = identify.substring(4, identify.length());
                          break;
                     case "str":identify =  identify.substring(4, identify.length());
                          break;
                     default:type="int";
                         ;break;
                         
                 }
             }
             MChoice = new DefaultComboBoxModel();
             s = conexion.createStatement();
             query  = "select " +identify+ " from " +Tbl+ " where " +Campo+ " = '"+value+"'";
             rs = s.executeQuery(query);
             while(rs.next()) {
                 switch(type){
                     case "int":campo = String.valueOf(rs.getInt(identify));
                          break;
                     case "str":campo =  rs.getString(identify);
                          break;
                 }
             }     
             this.identify = "";
             closeConexion(); 
         }
         catch(Exception e){
             System.out.println(_error+"idChoice: "+e);
         }
         return campo;
     }
     /*
      * Obtener valores de la tabla 
      */
         public  String[] getRecords(String Table,int Id){
         String campos[] = new String[0];
         try{
             getConexion();
             String query;
             String identify="";
             identify = getIdentify(Table);
             if(identify.equals("NMID")){
             identify="\"NMID\"";
             }
             query= "select * from "+Table+" where "+ identify +" = "+Id+"";
             s = conexion.createStatement();
             rs = s.executeQuery(query);
             ResultSetMetaData meta = rs.getMetaData();
             int nCols = meta.getColumnCount();
             campos = new String[nCols+1];
             
             rs.next();
             for(int i=1;i<=nCols;i++){
                     campos[i]=rs.getString(i);
             }
             rs.close();
             closeConexion(); 
         }
         catch(Exception e){
             System.out.println(_error+"getRecords: "+e);
         }
         return campos;
     }
         /*
          * Obetener el indice de una tabla
          */
         private  String getIdentify(String Table){
             String identify="id";
             try{  
                 getConexion();
                 Statement s = null;
 
                 s = conexion.createStatement();
                 rs = s.executeQuery("select * from "+Table +" LIMIT 1 ");
                 //Llenado Cabecera Jtable
                 ResultSetMetaData meta = rs.getMetaData();
                 int nCols = meta.getColumnCount();
                 
                 for(int i=1;i<=nCols;i++){
                     if(meta.isAutoIncrement(i)){
                         identify = meta.getColumnName(i);
                     }
                 }
                 rs.close();
             }
             catch(Exception e){
                 System.out.println(_error+"getIdentify: "+e);
             }
                 
             return identify;
         }
         /*
          * Obtener la cantidad de registros en una tabla
          */
         public  int getCountRegister(String[] args){
             int cant=0;
             try{  
                 System.out.println("1:"+args.length);
                 getConexion();
                 Statement s = null;
                 s = conexion.createStatement();
                 String query="";
                 if(args.length>0){
                     if(args.length==1){
                         query = "select count(*) from "+args[0];
                     }
                     else if(args.length>=3){
                         query = "select count(*) from "+args[0]+" where ";
                         for(int i=1;i<args.length;i++){
                             query = query + args[i]+" = "+args[i+1];
                             if(args.length!=i+2){ 
                                 query = query + " and ";
                             }
                             i++;
                         }
                         
                     }
                     rs = s.executeQuery(query);
                     rs.next();
                     cant = rs.getInt(1);
                 }
                 
             }catch(Exception e){
                 System.out.println(_error+"getCountRegister"+e);
             }
             return cant;
          }
         /*
          * Validar si es integer or String
          */
         public int tipodato(){
             int i=0;
         return i;
         }
         public int gettamColumn(String table,int pos) throws SQLException{
             getConexion();
             Statement s = null;
             s = conexion.createStatement();
             rs = s.executeQuery("select * from "+table +" LIMIT 1 ");
             ResultSetMetaData meta = rs.getMetaData();
             int pres = meta.getPrecision(pos);
             return pres;
             
         }
         
         
         public  DefaultTableModel getFechafilter(String[] args, String Table, String inicio,String fin){
         try{
             datos = new DefaultTableModel();
             getConexion();
             String id;
             Object[] fila; 
             s = conexion.createStatement();
             String qs = "select ";
             for(int i=0;i<args.length;i++){
                 qs = qs + args[i];
                 qs = qs + ",";
             }
             qs = qs +" from "+Table;
             qs = qs.replace(", "," ");
             qs = qs + " where ";
             qs=qs+" (fecha >'"+inicio+"') and (fecha<'"+fin+"')";
             
             
             
             rs = s.executeQuery(qs);
             //Llenado Cabecera Jtable
             ResultSetMetaData meta = rs.getMetaData();
             int nCols = meta.getColumnCount();
             for(int i=0; i<nCols; ++i){    
                 datos.addColumn(meta.getColumnName(i+1));
                 id = meta.getColumnName(i+1).substring(0, 2);
             }
             //Llenado registro Jtable
             fila = new Object[nCols];
             while(rs.next()){
                 for(int i=0; i<nCols; ++i){   
                     fila[i] = rs.getObject(i+1);
                 }
                 datos.addRow(fila);
             }
            //Cerrando conexion
            rs.close();
            closeConexion(); 
            
         }
         catch(Exception e){
             System.out.println(_error+"getFechaFilter: "+e);
         }
         
         return datos;
         }
     }
         
 
