 package bwdreader;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author xvit
  */
 
     
 
 /*
 
  * 
  
  * CGElement (1536)
  *  Описание деталей
  *      ..............B, pNbr, code, label, info, customer, ppsorder
  *      bNbr
  * CBWPlan (25920)
  *  Описание файлов-страниц плана резки
  *      id, label, filename, border
  *          CGBlockRef
  *              relatedTo, tFlag, offset, N [128].........
  *              relatedTo
  *      id
  *      id
  
  
  */
 
 
     class BDWField{
         public String fieldName;
         public int startByte;
         public String value;
 
     public BDWField(String name, int start, String val) {
         fieldName = name;
         startByte = start;
         value = val;
     }
 
 
 
         public String toString(){
             return fieldName + "." + startByte + "=\"" + value + "\"";
         }
     }
 
     class partStat{
         String name;
         String customer, group;
         int count;
         int ix;
     }
     
     class realPart{
         
         String name, customer, group;
         byte[] oid_data  = new byte[16];
         byte[] code_data =  new byte[48];
         byte[] group_data =  new byte[48];
         byte[] customer_data =  new byte[48];
         byte[] offset_data = new byte[128];
         boolean used = false;
         int ix = -1;
         int count = 0;
         
         int    oid_start = -1, part_end = -1, code_start = -1, offset_start, customer_start, group_start;
         
         public byte[] getOID(){
             byte[] result = new byte[4];
             
             for (int i = 0; i < 4; ++i){
                 result[i] = oid_data[i+4];
             }
 
 //            if (result[1] == 0 && result[0] !=0) {
 //                result[1] = result[0];
 //                result[0] = 0;
 //            }
 //
             return result;
         }
 
         public byte[] getOffsetID(){
             byte[] result = new byte[4];
 
             if (
                     offset_data[14] == 0 &&
                     offset_data[15] == 0 &&
                     offset_data[16] == 0 &&
                     offset_data[17] == 0                    
                     ){
                 result[0] = offset_data[18];
                 result[1] = offset_data[19];
                 result[2] = offset_data[20];
                 result[3] = offset_data[21];
                 
             }
             else
             {
                 result[0] = offset_data[14];
                 result[1] = offset_data[15];
                 result[2] = offset_data[16];
                 result[3] = offset_data[17];
             }
                 
             return result;
             
 //            boolean end0group = false;
 //            byte prev_byte = -1;
 //            
 //            int ix = -1;
 //          
 //            for (int i = 8; i < offset_data.length; i++){
 //                //if (offset_data[i] != 0)
 //                if (prev_byte == 0 && offset_data[i] != 0) end0group = true;
 //                
 //                if (end0group) {
 //                    
 //                    for (int j=0; j < 4; j++){
 //                        result[j] = offset_data[i+j];                        
 //                    }
 //
 ////                    if (result[1] == 0 && result[2] == 0 && result[3] == 0) {
 ////                        result[1] = result[0];
 ////                        result[0] = 0;
 ////                    }
 //
 //                    return result;
 //                    
 //                    
 //                }
 //                
 //                prev_byte = offset_data[i];
 //            }
             
 
         }
         
         
         
     }
     
     class realPlan{
         String filename;
         String info;
         int    cycleCount;
         byte[] filename_data = new byte[64];
         byte[] cycleCount_data = new byte[16];
 
         int    filename_start, cycleCount_start;
         Vector<realPart> parts = new Vector<realPart>();
         Vector<partStat> stat = new Vector<partStat>();
 
         public void addPartStat(String name, int ix){
             int find_ix_part = -1;
             int find_ix_stat = -1;
 
             for (int i = 0; i < parts.size() - 1; i++){
                if (name.equals(parts.get(i).name)){
                    find_ix_part = i;
                    break;
                }
 
             }
             
             for (int i = 0; i < stat.size(); i++){
                 if (stat.get(i).name.equals(name)){
                     find_ix_stat = i;
                     break;
                 }
                     
             }
             
             if (find_ix_part < 0) return;
             
               partStat ps = null;// = partStat();
             if (find_ix_stat < 0) {
                 ps = new partStat();
                 ps.name = name;
               //  ps.customer =
                 ps.ix = ix;
                 ps.count = 1;    
                 stat.add(ps);
             }
             else
             {
                 ps = stat.get(find_ix_stat);
                 ps.name = name;
                 ps.ix = ix;
                 ps.count++;
             }
 
 
         }
 
     }
     
     class BDWPart {
 
         public String lastWord;
         public String currentCommand;
         public Vector<BDWField> fields;
         public Vector<BDWField> stat;
 
     public String getField(String name, int size, RandomAccessFile stream){
         for (int i = 0; i < fields.size(); i++){
             if (fields.get(i).fieldName.equals(name)){
                 try {
                     String out = "";
                     stream.seek(fields.get(i).startByte);
                     for (int j =0; j < size; j++){
                         out = out + String.valueOf(stream.readChar());
                     }
                     return out;
                 } catch (IOException ex) {
                     Logger.getLogger(BDWPart.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         return "";
     }
 
     public BDWPart() {
         fields = new Vector();
         stat = new Vector();
 
     }
     
     
     protected int inStatExists(String p){
         
         for (int i = 0; i < stat.size(); i++)
             if (stat.get(i).fieldName.equals(p)) return i;        
         
         return -1;
     }
 
     public void makeStat(){
 
         stat = new Vector<BDWField>();
         for (int i = 0; i < fields.size(); i++){
             String p = fields.get(i).fieldName;
 
             int ix = inStatExists(p);
 
             if (ix > -1) {
                 stat.get(ix).startByte++;
             }
             else
                 stat.add(new BDWField(p, 1, ""));
 
         }
     }
 
     public void addToPropertie(String pname, String pvalue, int start){
 
         if (pname == null || pvalue == null) return;
 
         //String oldvalue="";
 
 //        if (fields.getProperty(pname) != null) oldvalue = fields.getProperty(pname) + " ";
 
 
         if (pname.equals("relatedTo") || pname.equals("offset")) System.out.println(start + "> " +pname + " \"" + pvalue + "\"");
         
         
         if (fields.size() > 0) {
             BDWField last = fields.get(fields.size() - 1);
             
             if (last.fieldName.equals(pname)) last.value = last.value + " " + pvalue;
             
                 else fields.add(new BDWField(pname, start, pvalue));
             
             
         }
         else {
             fields.add(new BDWField(pname, start, pvalue));
           //  stat.add(new BDWField(pname, 1, pvalue));
         }
         
         //fields.setProperty(pname, oldvalue  + pvalue);
 
     }
         
         
 
     }
     class BDWFile extends BDWPart{
         
         
     }
 
 public class BDWHacker {
    static String version = "2011.05.03";
     static String author = "Fedotov Viktor (c) 2011 <vtwww@yandex.ru>";
     
 
     
     public String BDWFilename;
     public Vector<BDWPart> parts;
     public Vector<BDWFile> files;
     public RandomAccessFile stream;
     public Vector<String> str;
     public Vector str_i;
     public Vector<realPlan> plans;
     public int tryFoundLevel;
     public boolean  BTW = false;
     public String[] partReservedWords = {
         "pNbr", "code", "label", "info", "customer", "positioningtime", "cuttingtime", "areaeffective",
         "allowCC^", "ppsorder", "info1", "info2", "info3", "createBy",
         "createDate", "group", "substance", "notes", "turnAngle",
         "CFDateTime", "changedBy", "filler^", "createdBy", "changeDate", "NormalLength", "PulsateLength",
         "CutTime", "Technology", "ParameterPath", "PositioningTime",
         "CuttingTime", "PiercingTime", "TotalTime", "id", "filename", "border", "oid", "relatedTo", "offset","tFlag",
         "PositioningLength", "printlabel^", "appName", "priority", "lastfiledateY", "need", "priority",
         "measure", "technology", "turnangleincrement", "commoncutallow", "areaoutercontours",
         "piercingtime", "EngraveLength", "NormalPiercingCount", "thickness", "commoncutallowed^",
         "centerofgravityd", "PulsatePiercingCount", "positioningway", "offsetd", "cyclecount", "cycleCount9", "cycleCount+", "refIdCounter"
 
     };
     private Vector<realPart> realParts;
 
     public BDWHacker() {
     }
 
     public BDWHacker(String file) {
         BDWFilename = file;
         try {
             stream = new RandomAccessFile(file, "r");
             if (file.toUpperCase().indexOf("BTW") > 1) BTW = true;
             analyze();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(BDWHacker.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public boolean isRealSymbol(byte b){
         //if ((b == 9)) return true;
 
         if (b < 32) return false;
         if ((b >=127)&&(b<=159)) return false;
 
         return true;
     }
 
     public boolean inArg(String[] arg, String s){
 
         for (int i =0; i < arg.length; i++){
             if (arg[i].equals(s)) return true;
         }
 
         return false;
     }
     
     public int findFirstString(String value, int start){
         for (int i = start; i < str.size(); i++){
             if (str.get(i).equals(value)) return i;
         }
         return -1;
     }
 
     public int findPrevousString(String value, int end){
         for (int i = end; i > -1; i--){
             if (str.get(i).equals(value)) return i;
         }
 
         return -1;
     }
     
     public void out(String s){
         System.out.println(s);
     }
 
     public void view(){
         out("Part list("+ realParts.size()+")");
         for (int i = 0; i < realParts.size(); i++){
             String u = "(PART_NOT_USED) ";
             if (realParts.get(i).used) u = "";
             out("" + (i+1) + ". " + u + realParts.get(i).name + "(customer: \""+realParts.get(i).customer+"\", group:\"" + realParts.get(i).group +"\" )" + "; oid [" +
                     realParts.get(i).getOID()[0] + ", " +
                     realParts.get(i).getOID()[1] + ", " +
                     realParts.get(i).getOID()[2] + ", " +
                     realParts.get(i).getOID()[3] + "]"
                     );
         }
 
         out("");
         out("Plans("+ plans.size() +")");
         for (int i = 0; i < plans.size(); i++){
             String err = "";
             if (plans.get(i).cycleCount == 0) err = "CYCLECOUNT_NOT_FOUND";
             out((i+1) + ". " + plans.get(i).filename + " (cyclecount: "+plans.get(i).cycleCount+") " + "/"+plans.get(i).info+"/ (" + plans.get(i).parts.size() +")" + err );
             for (int j = 0; j < plans.get(i).parts.size(); j++){
                 out("    " + (j+1) + ". " + plans.get(i).parts.get(j).name + "(customer: \"" + plans.get(i).parts.get(j).customer +"\", group: \"" + plans.get(i).parts.get(j).group  + "\")"+ "; offset [" +
                         plans.get(i).parts.get(j).getOffsetID()[0] + ", " +
                         plans.get(i).parts.get(j).getOffsetID()[1] + ", " +
                         plans.get(i).parts.get(j).getOffsetID()[2] + ", "+
                         plans.get(i).parts.get(j).getOffsetID()[3] + "]"
                         );
             }
         }
 
     }
 
     public void writeData01( String dataFieldName, int length){
 
         //перебираем поток всех найденных строк
         out("<h2>" + dataFieldName + "</h2>");
        out("<table border=1 cellpadding=5><tr bgcolor=silver><td>num<td>addr");
 
         for (int i = 0; i < length; i++){
             out("<td>" + (i));
         }
        out("</tr>");
 
         int counter = 1;
         for (int i = 0; i < str.size(); i++){
             if (str.get(i).equals(dataFieldName)){
                 try {
                     int start = Integer.valueOf(str_i.get(i).toString());
                     out("<tr><td>" + counter++ + "<td bgcolor=yellow>" + start);
                     stream.seek(start);
 
                     for (int j = 0; j < length; j++){
                              byte B = stream.readByte();
                     String color = "";
                        String symbol = "";
                        if (B == 0) color=" bgcolor=silver";
                        if (B == 64) color=" bgcolor=green";
                        if (B > 32 && B < 128) {
                            symbol = "<br><b>" + String.valueOf((char) B) + "</b>";
 //                           word = word + String.valueOf((char) B);
                        }
 //
                        out("<td "+color+">" + B + symbol);
 
                     }
 
                     //фильтр нужного поля
                 } catch (IOException ex) {
                    // Logger.getLogger(BysReaderView.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
 
 
                 //фильтр нужного поля
 
             }
         }
         out("</table>");
       //  out(html.getText());
 
     }
 
     public void printHtmlDump(){
  //out("DUMPINGGGGGGGGGG");
       //  TStrings ini = new TStrings();
 
        out("<html>");
        out("<title>"+BDWFilename+"</title>");
        out("<h1>"+BDWFilename+"</h1>");
 
        out("");
        out("<pre>");
 
        view();
        out("</pre>");
        out("</pre>");
        out("</pre>");
        out("<hr>");
 
 
         writeData01( "oid", 25);
       //  writeData01( "customer", 25);
       //  writeData01( "group", 25);
         writeData01( "id", 16);
         writeData01( "cyclecount", 16);
         writeData01( "cycleCount9", 16);
         writeData01( "cycleCount+", 16);
         writeData01( "refIdCounter", 48);
         writeData01( "code", 48);
 
         writeData01( "filename", 40);
 
         writeData01( "offset", 150);
         out("</html>");
 
        // out(ini.getText());
     }
 
     public String ejectFileName(String filename){
 
         char[] arg = filename.toCharArray();
         String result = "";
 
         for (int i = arg.length-1; i >= 0; i--){
 
         }
 
         return "";
     }
 
     public void print_tabbed01(){
 
         StringTokenizer st = new StringTokenizer(BDWFilename, File.separator);
 
         
 
         //String[] path = BDWFilename.split ( String.valueOf( File.separatorChar));
        // out ("DEBUG separator= \"" + path.toString() + "\"");
         String bwd_name = "";
 
         while (st.hasMoreTokens()) bwd_name = st.nextToken();
 
 
 
     char div = (char) 9;
 
     for (int i = 0; i < plans.size(); i++){
         String last_part = "";
         Properties stati = new Properties();
         
         
         
             for (int j = 0; j < plans.get(i).parts.size(); j++){
                 String j_str = String.valueOf(plans.get(i).parts.get(j).ix);
                 if (stati.getProperty(j_str) == null) 
                     stati.setProperty(j_str, "1");
                 else
                 {
                     int prev = Integer.valueOf(stati.getProperty(j_str)) + 1;
                     stati.setProperty(j_str, String.valueOf(prev));
                 }
              
             //    int ix = plans.get(i).stat.get(j).ix - 1;
                 //Имя джоба/ или имя файла	Имя плана
                 //Количество циклов плана
                 //Артикуль	Кол-во деталей в плане	Клиент	Группа
 
                 //out("DEBUG: " + ix);
             }
        //    stati.list(System.out);
         
         Object[] arg = stati.keySet().toArray();
         for (int j = 0; j < arg.length; j++){
             int part_ix = Integer.valueOf(arg[j].toString());
             //int part_ix = Integer.valueOf(stati.getProperty(String.valueOf(arg[j])));
                 out(
                         bwd_name + div +
                         plans.get(i).filename + div +
                         plans.get(i).cycleCount + div +
                         realParts.get(part_ix).name + div +
                         stati.getProperty(arg[j].toString()) + div +
                         realParts.get(part_ix).customer + div +
                         realParts.get(part_ix).group
                         );                 
         }
         
            // out("DEBUG: plan_end");
 
         }
     }
 
     public void printIni(){
 
         TStrings ini = new TStrings();
 
         ini.items.add("[BWD_FILE]");
         ini.items.add("BWD_PATH=\"" + BDWFilename + "\"");
         //ini.items.add("BWD_PATH=" + );
         ini.items.add("BWD_PLANS_TOTAL=" + plans.size());
 
         int total_parts = 0;
 
         for (int i=0; i < plans.size(); i++){
             total_parts = total_parts + plans.get(i).parts.size();
         }
 
         ini.items.add("BWD_PARTS_TOTAL=" + total_parts);
         
         
 
         for (int i = 0; i < plans.size(); i++){
             ini.items.add("\n["+plans.get(i).filename+"]");
             ini.items.add("BWD_PLAN_INFO=\""+plans.get(i).info + "\"");
             ini.items.add("BWD_PARTS_IN_PLAN="+plans.get(i).parts.size());
 
             Properties list = new Properties();
             //out("===========" + plans.get(i).filename);
             for (int j = 0; j < plans.get(i).parts.size(); j++){
                 realPart rp = plans.get(i).parts.get(j);
               //  out("===========" + rp.name);
                 if (list.getProperty(rp.name) != null) {
                     int prev_value= Integer.valueOf(list.getProperty(rp.name)) + 1;
                     list.setProperty(rp.name, String.valueOf( prev_value));
                 }
                 else list.setProperty(rp.name, String.valueOf(1));
             }
 
             Object[] arg = list.keySet().toArray();
 
             for (int j = 0; j < arg.length; j++){
                 ini.items.add(arg[j].toString() + "=" +list.getProperty(arg[j].toString()));
             }
             
         }
 
         out(ini.getText());
         
     }
     
     public int tryFindPart(byte[] offset){
         
         
         //Наблюдаемая закономерноть первой детали
         //в оффсете 75,0,0,0 а в оид: 79,0,*,*
         
         if (offset[0] == 75 && offset[1] == 0) return 0;
         
         //Попытка найти/сопоставить пару идентификаторов из "offset" в "oid"
         /*Если offset байты 3 и 4 равны 0, например, [a,b,0,0] то 
          * oid байты будут так: [a+1,b,*,*] - здесь 3 и 4 байты не учитываем
          * 
          * если же все байты в offset ненули то ищем по такой формуле:
          * offset [a,b,c,  d] [-1 127 -64 -119 ]
          * oid    [a,b,c+1,d] [-1 127 -63 -119 ]
          *          
          * неизвестно как поведет данная взаимозвось на грани значения типа BYTE
          */
         
 //        
             for (int i = 0; i < realParts.size(); i++){
                 byte[] oid = realParts.get(i).getOID();
 
                 //[*,0,0,0]
                 if (offset[1] == 0 && offset[2] == 0 && offset[3] == 0){
                     if (offset[0] + 4 == oid[0]) return i;
 
 
                     
                     
                 } //[*,0,0,0]
 
                 //[*,*,0,0]
                 if (offset[2] == 0 && offset[3] == 0)
                 {    
                     if (    offset[0] == oid[0]-1 &&
                             offset[1] == oid[1]
                         )       return i;
 
 
                     /*000018/00009567.BWD
                      * 4. (NOT_USED) Stroi 011; oid [1, 1, 87, 37]
                      *  19. NOT_FOUND; offset       [1, 0, 0, 0]
                      */
                     if (    offset[0] == oid[0] &&
                             offset[1] == oid[1]-1
                         )       return i;
 
 
                     /*
                      * 000020/00010433.BWD
                      * 2. (NOT_USED) 2480/150; oid [-128, 0, 47, 25]
                      * 1. NOT_FOUND; offset [127, 0, 0, 0]
                      */
                     if (offset[0] == 255 + oid[0]) return i;
                     
                     
                     /* 000021/00010967.BWD
                      * 2. (NOT_USED) Sfinks; oid [ 0, 1, -43, 42]
                      *  1. NOT_FOUND; offset     [-1, 0,   0,  0] 
                      */
                     if (    offset[0] == oid[0]-1 &&
                             offset[1] == oid[1]-1
                         )       return i;                       
                     
                 }//[*, *, 0, 0]
 
 
                 //offset[X   ,0,*,*]
                 //   oid[X+1, 0,*,*]
                 if (
                         offset[1]   == 0 &&
                         oid   [1]   == 0 &&
                         oid   [0]-1 == offset[0]
                         )   return i;
                 
                 /*000017/00008983.BWD
                  * 
                  * 3. (NOT_USED) ssu004.01.04.01; oid [-37, 1, 39,  28]
                  *  16. NOT_FOUND;             offset [-38, 1, 21, -65]
                  */
                 if (
                         offset[0] == oid[0] - 1 &&
                         offset[1] == oid[1]
                         ) return i;
 
                 /*
                  * 000018/00009476.BWD
                  * 20. (NOT_USED) 191255; oid [-1, 127, 24, -127]
                  *                                      ||
                  *  12. NOT_FOUND;     offset [-1, 127, 23, -127]
                  *
                  */
                 if (
                         offset[0] == oid[0] &&
                         offset[1] == oid[1] &&
                         offset[2] == oid[2] -1 &&
                         offset[3] == oid[3]
                         ) return i;
 
                      /*000018/00009625.BWD
                      * 1. OCM och;       oid [27, 0, -104, 37]
                      *                       |4|
                      * 28. NOT_FOUND; offset [23, 0, -1, -1]
                      * 29. OCM och; offset [23, 0, 0, 0]
                      *
                      */
                 if (
                         offset[0] == oid[0] - 4 &&
                         offset[1] == 0 &&
                         oid   [1] == 0 )
                     return i;
 
                 /*000050/00026024.BWD
                  * 33. (NOT_USED) 021.03.110.062; oid [-1, 127,  0, -109]
                  *                                              -1    -1
                  *   4. NOT_FOUND;             offset [-1, 127, -1, -110]
                  */
                 if (
                         offset[0] == oid[0] &&
                         offset[1] == oid[1] &&
                         offset[2] == oid[2]-1 &&
                         offset[3] == oid[3]-1
                         ) return i;
 
                 /*000042/00021557.BWD
                  *  7. (NOT_USED) 010.03.130.104; oid [-128, 3, -12, 57]
                  *
                  *  8. NOT_FOUND;              offset [ 127, 3, -85, 76]
                  */
                 if (
                         offset[0] == 255 + oid[0] &&
                         offset[1] == oid[1]) return i;
 
                 /*
                  *
                     1. Kosour 7v; oid           [79, 0, -14, 56]
                     2. Kosour 6v; oid           [-48, 3, -15, 56]
                     3. Kosour 8v; oid           [71, 7, -13, 56]
                     4. Kosour 5v; oid           [-66, 10, -16, 56]
                     5. Kosour 4v; oid           [61, 14, -17, 56]
                     6. Kosour 3v; oid           [-76, 17, -18, 56]
                     7. Kosour 2v; oid           [49, 21, -19, 56]
                     8. Kosour 1v_1; oid         [-88, 24, -20, 56]
                     9. Kosour 1v; oid           [31, 28, -21, 56]
                     10. Kosour 5v_1; oid        [-116, 31, -10, 56]
                     11. Kosour 3v_1; oid        [-4, 34, -8, 56]
                     12. Kosour 7v_1; oid        [85, 38, -9, 56]
                     13. 004344; oid             [-82, 41, -8, 35]
                     14. 004345; oid             [-16, 43, -7, 35]
                     15. 004350; oid             [50, 46, -2, 35]
                     16. (NOT_USED) 004354; oid  [116, 48, 2, 36]
                     17. 004356; oid             [-74, 50, 4, 36]
                     18. 004357; oid             [-8, 52, 5, 36]
                     19. 004359; oid             [58, 55, 18, 36]
                     20. 004360; oid             [124, 57, 19, 36]
                     21. 004361; oid             [-66, 59, 20, 36]
                     22. 11001; oid              [0, 62, 89, 56]
                     23. 11003; oid              [66, 64, 90, 56]
                     24. 11005; oid              [-124, 66, 91, 56]
                     25. 11008; oid              [-58, 68, 92, 56]
                     26. 11009; oid              [8, 71, 93, 56]
                     27. 004348_1 ; oid          [74, 73, 98, 56]
                     28. Plat300_100; oid        [-116, 75, 1, 57]
                     29. Plat250_150; oid        [68, 76, 0, 57]
                     30. Plat250_100; oid        [-4, 76, -1, 56]
                  *
                  *    55. NOT_FOUND; offset     [-1, 61, -92, 76]
                  */
                  if (
                         offset[0] == oid[0]-1 &&
                         offset[1] == oid[1]-1
                      )       return i;
 
 
 
             } 
         
         
         
   
         
         return -1;        
     }
 
     public char getRusByte(byte b){
 
         if (b > 0) return(char)  b;
         if (256 + b < 192)  return(char)  b;
 
         //c 192-256
         String rus = "АБВГДЕЖЗИЙКЛМНОП" +
                      "РСТУФХЦЧШЩЪЫЬЭЮЯ" +
                      "абвгдежзийклмноп" +
                      "рстуфхцчшщъыьэюя";
 
         char[] ar = rus.toCharArray();
 
 
         return ar[256 - 192 + b];
     }
 
     public int getPart_ix(String name){
 
         for (int i = 0; i<realParts.size(); i++)
 
             if (realParts.get(i).name.equals(name)) return i;
 
         return -1;
     }
 
 
 
     public void analyze(){
         parts = new Vector<BDWPart>();
         files = new Vector<BDWFile>();
         str = new Vector<String>();
         str_i = new Vector();
         
         plans = new Vector<realPlan>();
         realParts = new Vector<realPart>();
         
         try {
             String word = "";
             Byte prev_byte = -1;
             for (int i = 0; i < stream.length(); i++) {
                 byte B = stream.readByte();
 
                 if (isRealSymbol(B)) word = word +(char)B;
 
                 if (!isRealSymbol(B) && isRealSymbol(prev_byte)){
                     str.add(word);
                     str_i.add(i);
 
                  //   if (word.indexOf("ZT") >= 0) out("DEBUG: ZT" + word + " ADDRESS#" + i);
 
                     word = "";
                 }
 
                 prev_byte = B;
             }
 
            // System.out.println("Load words: " + str.size());
             
             //загружаем список идентификаторов деталей
             for (int i = 0; i < str.size(); i++){
 
                 if (str.get(i).equals("filename")){
                     
                     
                     int filename_ix = i;
                     int next_filename_ix = findFirstString("filename", i+1);
                     if (next_filename_ix < 0) next_filename_ix = str.size();
                     realPlan r = new realPlan(); 
                     r.info = str.get(i-1);
                     r.filename_start = Integer.valueOf(str_i.get(i).toString());
                     r.filename = "";
                        stream.seek(r.filename_start);
                     stream.read(r.filename_data);
                     boolean find_non_symbol = false;
 
                     int s1 = 7;
                     if (r.filename_data[10] == 13)  s1 = 11;
 
 
 
                     /*
                      * Байты определяющие начало имена плана (filename)
                      * Если 10-тый байт 13, 19 или 14 код ASCII то старт считывания перемещяецца
                      * на 11 байт
                      *
                      * 10-й байт всему ГОЛОВА
                      * 2011-03-22
                      */
                     if (r.filename_data[10] == 19 ) s1 = 11;
                     if (r.filename_data[10] == 14 ) s1 = 11;
                    if (r.filename_data[10] == 15 ) s1 = 11;
                     /*2011-04-06*/
                     if (r.filename_data[10] == 16 ) s1 = 11;
 
 
                         for (int j = s1; j < r.filename_data.length; j++){
                             if (r.filename_data[j] < 30) find_non_symbol = true;
                             if (find_non_symbol == false)
                                 r.filename = r.filename + String.valueOf(getRusByte(r.filename_data[j]));
                         }
 
 
                     //if (! BTW )
                     int cycle_ix = 0;// = findFirstString("cyclecount", i);
                     if ( ! BTW )  cycle_ix = findFirstString("cyclecount", i);
                     else { 
                         cycle_ix = findFirstString("cycleCount9", i); 
                         if (cycle_ix < 0) cycle_ix = findFirstString("cycleCount+", i);
                     
                     }
 
                  //   out ("DEBUGGG cycle_ix: " + cycle_ix);
 
                     if (cycle_ix >= 0) {
                         r.cycleCount_start= Integer.valueOf(str_i.get(cycle_ix).toString());
                         stream.seek(r.cycleCount_start);
                         stream.read(r.cycleCount_data);
                     }
 
                     r.cycleCount = 0;
                     
                     if (
                             r.cycleCount_data[6] != 0 &&
                             r.cycleCount_data[7] == 0 &&
                             r.cycleCount_data[8] == 0 &&
                             r.cycleCount_data[9] == 0                            
                             )
                      r.cycleCount = r.cycleCount_data[6];
                     
                     
                     if (
                             r.cycleCount_data[10] != 0 &&
                             r.cycleCount_data[11] == 0 &&
                             r.cycleCount_data[12] == 0 &&
                             r.cycleCount_data[13] == 0
                             )
                      r.cycleCount = r.cycleCount_data[10];
                     
                     
                     
                   //  out("======= filename_start=" + r.filename_start);
                     
                  
                     
                     plans.add(r);
                     for (int j=filename_ix; j < next_filename_ix; j++){
                         if (str.get(j).equals("offset")){
                             realPart p1 = new realPart();
                             p1.offset_start = Integer.valueOf(str_i.get(j).toString());
                             stream.seek(p1.offset_start);
                             stream.read(p1.offset_data);
                             p1.name = "PART_NOT_FOUND";
                             //Идентифицируем деталь                           
                             
                             //Добавляем деталь в план
                             int try_part_ix = tryFindPart(p1.getOffsetID());
 
                             if (try_part_ix > -1) { 
                                 p1.name = realParts.get(try_part_ix).name;
                                 p1.customer = realParts.get(try_part_ix).customer;
                                 p1.group = realParts.get(try_part_ix).group;
                                 realParts.get(try_part_ix).used =true;
                                 p1.ix = try_part_ix;
                             
                             }
 
                             r.parts.add(p1);
                             r.addPartStat(p1.name, try_part_ix);
                         }
                     }
                     
                     
                 }
 
                 if (str.get(i).equals("oid")){
                     
                     realPart p = new realPart();
                     p.oid_start =
                             Integer.valueOf(
                             str_i.get(i).toString()
                             );
                     stream.seek(p.oid_start);
                     //byte[] arg;
                     //byte[] arg = new byte[12];
                     stream.read(p.oid_data);
                   //  out("========oid: " + p.oid_start);
 
                     /*
                      Part list(9)
                     1. ZT dno(customer: "111111", group:"PGM10" ); oid [79, 0, -70, 119]
                     2. ZT PGM10 kreplenie perednee(customer: "111111", group:"PGM10" ); oid [-29, 0, -69, 119]
                     3. PGM10 petla1(customer: "111111", group:"PGM" ); oid [115, 1, -68, 119]
                     4. PGM10 prijim(customer: "111111", group:"PGM" ); oid [-1, 1, -67, 119]
                      * если имя детали начинаеццо на ZT первые 3 символа почемуто пропадают
                      * Вариант искать ближайший code с конца
                      */
                     int next_oid = findFirstString("oid", i);
                     if (next_oid == -1) next_oid = findFirstString("filename", i);
 
                     //int code_ix = findFirstString("code", i);
                     int code_ix = findPrevousString("code", next_oid);
                     
                     
                     int group_ix = findFirstString("group", i+1);
                     int customer_ix = findFirstString("customer", i+1);
                   //  out("code_ix=" + code_ix);
 
                     if (group_ix > 0) {
                         p.group_start = Integer.valueOf(str_i.get(group_ix).toString());
                         stream.seek(p.group_start);
                         stream.read(p.group_data);
 
                         boolean find_non_symbol = false;
                         p.group = "";
 
                         int s1 = 7;
 
                         if (p.group_data[9] == 0) s1 =11;
 
                         for (int j = s1; j < p.group_data.length; j++){
                             int b = p.group_data[j];
                           //  if (b < 0) b = 255 + b;
                             if (b <= 30 && b >= 0) find_non_symbol = true;
                             if (find_non_symbol == false)
 
                             p.group = p.group + String.valueOf( (char) b);
                         }
                     }
 
                     if (customer_ix > 0) {
                         p.customer_start = Integer.valueOf(str_i.get(customer_ix).toString());
                         stream.seek(p.customer_start);
                         stream.read(p.customer_data);
 
                         boolean find_non_symbol = false;
                         p.customer = "";
 
                         int s1 = 7;
 
                         if (p.customer_data[9] == 0) s1 =11;
 
                         for (int j = s1; j < p.customer_data.length; j++){
                             int b = p.customer_data[j];
                           //  if (b < 0) b = 255 + b;
                             if (b <= 30 && b >= 0) find_non_symbol = true;
                             if (find_non_symbol == false)
 
                             p.customer = p.customer + String.valueOf( (char) b);
                         }
                     }
 
 
 
                     if (code_ix > 0) {
                         p.code_start = Integer.valueOf(str_i.get(code_ix).toString());
                         stream.seek(p.code_start);
                         stream.read(p.code_data);
                         
                         boolean find_non_symbol = false;
                         p.name = "";
 
                         int s1 = 7;
 
                         if (p.code_data[9] == 0) s1 =11;
 
                         for (int j = s1; j < p.code_data.length; j++){
                             byte b = p.code_data[j];
                           //  if (b < 0) b = 255 + b;
                             if (b <= 30 && b >= 0) find_non_symbol = true;
                             if (find_non_symbol == false)
                                 
                             p.name = p.name + String.valueOf( getRusByte( b));
                         }
                     }
 
 
 
                     realParts.add(p);
 
                 }
                         
             }
             
            
 //
 //
 //            Boolean start_parts = false, start_part = false, start_files = false;
 //
 //
 //            BDWPart part = null;
 //            BDWFile bfile = null;
 //            String prev_word = "";
 //            for (int i = 0; i < str.size(); i++){
 //                word = str.get(i);
 //                if (word.equals("CGElement")) {
 //                    start_parts = true;
 //                    System.out.println("Read parts...");
 //                }
 //
 //                if (word.equals("CBWPlan")) {
 //                    parts.add(part);
 //                   // part.fields.list(System.out);
 //                    System.out.println("Parts is complete");
 //
 //                    start_parts = false; start_files = true;
 //
 //                }
 //
 //                if (start_files){
 //                    if (word.equals("id")){
 //                         System.out.println("Filename: detected");
 //
 //                         if (bfile != null){
 //                             files.add(bfile);
 //                           //  bfile.fields.list(System.out);
 //                         }
 //
 //                         bfile = new BDWFile();
 //
 //
 //                    }
 //                    else
 //                    {
 //                        if (inArg(partReservedWords, word))
 //                        {
 //                             if (inArg(partReservedWords, prev_word) && ! prev_word.equals(word))
 //                               bfile.addToPropertie(prev_word, "", Integer.valueOf(""+str_i.get(i)));
 //
 //                            bfile.currentCommand = word;// + "." + str_i.get(i);
 //
 //                        }
 //                        else
 //                        {
 //
 //                            if (bfile != null) {
 //                                if (bfile.currentCommand != null && word != null)
 //                                {
 //
 //                                    bfile.addToPropertie(bfile.currentCommand, word, Integer.valueOf(""+str_i.get(i)));
 //
 //                                }
 //                            }
 //
 //
 //
 //                        }
 //                    }
 //                }
 //
 //                if (start_parts){
 //                    //Читаем детали
 //                    
 //                    //if (word.equals("code")) {
 //                    if (word.equals("pNbr")) {
 //                        //Новая деталь, добавляем в вектор
 //
 //                        if (part != null) {
 //                            parts.add(part);
 //                           // part.fields.list(System.out);
 //                        }
 //
 //                        System.out.println();
 //                        System.out.println("Part: " + (parts.size()+1));
 //                        part = new BDWPart();
 //                        
 //                        
 //                    }
 //                    else
 //                    {
 //                        if (inArg(partReservedWords, word))
 //                        {
 //                            if (part != null) 
 //                            {
 //                               if (inArg(partReservedWords, prev_word) && ! prev_word.equals(word))
 //                               part.addToPropertie(prev_word, "", Integer.valueOf(""+str_i.get(i)));
 //                                
 //                                part.currentCommand = word;// + "." + str_i.get(i);
 //                                
 //                             
 //                            }
 //
 //                        }
 //                        else
 //                        {
 //
 //                            if (part != null) {
 //                                if (part.currentCommand != null && word != null)
 //                                {
 //   
 //                                    part.addToPropertie(part.currentCommand, word, Integer.valueOf("" + str_i.get(i)) );
 //                                    
 //                                }
 //                            }
 //                            
 //                            
 //
 //                        }
 //
 //
 //
 //                      //  if (part != null)  part.lastWord = word;
 //                    }
 //                }
 //                
 //                prev_word=word;
 //
 //            }
 //            files.add(bfile);
 
         } catch (IOException ex) {
             Logger.getLogger(BDWHacker.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
     
 
 }
