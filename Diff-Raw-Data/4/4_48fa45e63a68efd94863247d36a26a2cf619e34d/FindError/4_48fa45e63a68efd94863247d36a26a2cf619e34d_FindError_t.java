 /*
     FindError.java
     2012 Ⓒ ReadStackCorrector, developed by Chien-Chih Chen (rocky@iis.sinica.edu.tw), 
     released under Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0) 
     at: https://github.com/ice91/ReadStackCorrector
 */
 
 package Corrector;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.RunningJob;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 
 
 public class FindError extends Configured implements Tool
 {
 	private static final Logger sLogger = Logger.getLogger(FindError.class);
 
 	public static class FindErrorMapper extends MapReduceBase
     implements Mapper<LongWritable, Text, Text, Text>
 	{
 		public static int K = 0;
         public static int IDX = 0;
 		public static int TRIM5 = 0;
 		public static int TRIM3 = 0;
 
 		public void configure(JobConf job)
 		{
 			//K = Integer.parseInt(job.get("K"));
             IDX = Integer.parseInt(job.get("IDX"));
 		}
 
 		public void map(LongWritable lineid, Text nodetxt,
 				        OutputCollector<Text, Text> output, Reporter reporter)
 		                throws IOException
 		{
             Node node = new Node();
 			node.fromNodeMsg(nodetxt.toString());
             
             Map<String, String> group_sub = new HashMap<String, String>();
             //slide the split K-mer windows for each read in both strands
             int end = node.len() - IDX;
             for (int i = 0; i < end; i++)
             {
                 String window_tmp = node.str().substring(i, i+IDX);
                 //\\
                 String window_tmp_r = Node.rc(node.str().substring(i, i+IDX));
                 if (window_tmp.compareTo(window_tmp_r) < 0) {
                     String prefix_half_tmp = window_tmp.substring(0, 20);
                     String suffix_half_tmp = window_tmp.substring(20);
                     String prefix_half = Node.str2dna(prefix_half_tmp);
                     String suffix_half = Node.str2dna(suffix_half_tmp);
                     String group_id = prefix_half;
                     int f_pos = i;
                     if ( !window_tmp.matches("A*") && !window_tmp.matches("T*") ){
                         if (group_sub.containsKey(group_id)) {
                             // dir, pos, suffix
                             String sub = group_sub.get(group_id);
                             sub = sub + "|" + "f" + "!" + f_pos + "!" + suffix_half;
                             group_sub.put(group_id, sub);
                         } else {
                             String sub = "f" + "!" + f_pos + "!" + suffix_half;
                             group_sub.put(group_id, sub);
                         }
                     }
                 } else if (window_tmp_r.compareTo(window_tmp) < 0) {
                    String prefix_half_tmp_r = window_tmp_r.substring(0, 20);
                    String suffix_half_tmp_r = window_tmp_r.substring(20);
                     String prefix_half_r = Node.str2dna(prefix_half_tmp_r);
                     String suffix_half_r = Node.str2dna(suffix_half_tmp_r);
                     String group_id = prefix_half_r;
                     int r_pos = end - i;
                     String Qscore_reverse = new StringBuffer(node.Qscore_1()).reverse().toString();
                     if ( !window_tmp_r.matches("A*") && !window_tmp_r.matches("T*") ){
                         if (group_sub.containsKey(group_id)) {
                             String sub = group_sub.get(group_id);
                             sub = sub + "|" + "r" + "!" + r_pos + "!" + suffix_half_r;
                             group_sub.put(group_id, sub);
                         } else {
                             String sub = "r" + "!" + r_pos + "!" + suffix_half_r;
                             group_sub.put(group_id, sub);
                         }
                     }
                 }
                 //\\
             }
              for(String id : group_sub.keySet()) {
                 String sub = group_sub.get(id);
                 output.collect(new Text(id), new Text(node.getNodeId() + "\t" + node.str_raw() + "\t" + node.Qscore_1() + "\t" + sub));
             }
 		}
 	}
 
 	public static class FindErrorReducer extends MapReduceBase
 	implements Reducer<Text, Text, Text, Text>
 	{
         private static int IDX = 0;
         private static long HighKmer = 0;
 
 		public void configure(JobConf job) {
             IDX = Integer.parseInt(job.get("IDX"));
             HighKmer = Long.parseLong(job.get("UP_KMER"));
 		}
 
         public class ReadInfo
 		{
 			public String id;
             boolean dir;
 			public short pos;
             //public short len;
             //public String seq;
             public byte[] seq;
             //public String qv;
             public byte[] int_qv;
 
 			public ReadInfo(String id1, String dir1, short pos1, /*short len1*/String seq1, String qv1) throws IOException
 			{
 				id = id1;
                 pos = pos1;
                 //len = len1;
                 if (dir1.equals("f")) {
                     dir = true;
                     //seq = Node.dna2str(seq1).toCharArray();
                     seq = Node.dna2str(seq1).getBytes();
                     int_qv = new byte[qv1.length()];
                     for(int i=0; i < int_qv.length; i++){
                         int_qv[i] = (byte)((int)qv1.charAt(i)-33);
                     }
                 } else {
                     dir = false;
                     //seq = Node.rc(Node.dna2str(seq1)).toCharArray();
                     seq = Node.rc(Node.dna2str(seq1)).getBytes();
                     int_qv = new byte[qv1.length()];
                     for(int i=0; i < int_qv.length; i++){
                         int_qv[i] = (byte)((int)qv1.charAt(int_qv.length-1-i)-33);
                     }
                 }
 			}
 
             public String toString()
 			{
                 return id + "!" + dir + "|" + pos + "|" /*+ seq*/ ;   
 			}
 		}
         
         /*public class SeqInfo {
             public String seq;
             public String qv;
             public SeqInfo(String seq1, String qv1) {
                 seq = seq1;
                 qv = qv1;
             }
         }*/
         
         class ReadComparator_right implements Comparator {
             public int compare(Object element1, Object element2) {
                 ReadInfo obj1 = (ReadInfo) element1;
                 ReadInfo obj2 = (ReadInfo) element2;
                 if ((int) ( (obj1.seq.length- obj1.pos) - (obj2.seq.length - obj2.pos) ) > 0) {
                     return -1;
                 } else if ((int) ( (obj1.seq.length - obj1.pos) - (obj2.seq.length - obj2.pos) ) < 0) {
                     return 1;
                 } else {
                     if ( obj1.id.compareTo(obj2.id) < 0) {
                         return -1;
                     } else {
                         return 1;
                     }
                 }
             }
         }
         
         class ReadComparator implements Comparator {
             public int compare(Object element1, Object element2) {
                 ReadInfo obj1 = (ReadInfo) element1;
                 ReadInfo obj2 = (ReadInfo) element2;
                 if ((int) ( obj1.pos - obj2.pos ) > 0) {
                     return -1;
                 } else if ((int) ( obj1.pos - obj2.pos ) < 0) {
                     return 1;
                 } else {
                     if ( obj1.id.compareTo(obj2.id) < 0) {
                         return -1;
                     } else {
                         return 1;
                     }
                 }
             }
         }
         
 		public void reduce(Text prefix, Iterator<Text> iter,
 						   OutputCollector<Text, Text> output, Reporter reporter)
 						   throws IOException
 		{
             List<String> code_list = new ArrayList<String>();
 			List<ReadInfo> readlist;
             //List<String> ReadID_list = new ArrayList<String>();
             Map<String, List<ReadInfo>> ReadStack_list = new HashMap<String, List<ReadInfo>>();
             //Map<String, String> id_seq = new HashMap<String, String>();
             //Map<String, String> id_qv = new HashMap<String, String>();
             //Map<String, SeqInfo> id_seq = new HashMap<String, SeqInfo>();
             List<String> H_Kmer = new ArrayList<String>();
             
             while(iter.hasNext())
 			{
 				String msg = iter.next().toString();
 				String [] vals = msg.split("\t");
                 String id = vals[0];
                 String seq = vals[1];
                 String qv = vals[2];
                 String [] subs = vals[3].split("\\|");
                 for(int i=0; i < subs.length; i++) {
                     String [] sub = subs[i].split("!");
                     String dir = sub[0];
                     String pos = sub[1];
                     String suffix = sub[2];
                     /*String DNAseq = Node.dna2str(seq);
                     String DNAqv = qv;
                     if (!id_seq.containsKey(id)) {
                         id_seq.put(id, new SeqInfo(DNAseq, DNAqv));
                     }*/
                     ReadInfo read_item = new ReadInfo(id,dir,Short.parseShort(pos), seq, qv);
                     //ReadInfo read_item = new ReadInfo(id,dir,Short.parseShort(pos), (short)DNAseq.length());
                     String window_tmp = Node.dna2str(prefix.toString()) + Node.dna2str(suffix);
                     String window = Node.str2dna(window_tmp);
                     if (!H_Kmer.contains(window)) {
                         if (ReadStack_list.containsKey(window)) {
                             readlist = ReadStack_list.get(window);
                             if (readlist.size()+1 > HighKmer) {
                                 H_Kmer.add(window);
                                 ReadStack_list.remove(window);
                             } else {
                                 readlist.add(read_item);
                                 ReadStack_list.put(window, readlist);
                             }
                         } else {
                             readlist = new ArrayList<ReadInfo>();
                             readlist.add(read_item);
                             ReadStack_list.put(window, readlist);
                         }
                     }
                 }
                 // duplicate reverse complement reads
                 /*String window_r_tmp = Node.rc(window_tmp);
                 String window_r = Node.str2dna(window_r_tmp);
                 int end = Node.dna2str(vals[3]).length() - IDX;
                 String Qscore_reverse = new StringBuffer(vals[4]).reverse().toString();
                 read_item = new ReadInfo(vals[0], Node.flip_dir(vals[1]), end - Integer.parseInt(vals[2]), Node.str2dna(Node.dna2str(vals[3])), Qscore_reverse);
                 if (!H_Kmer.contains(window_r)){
                     if (ReadStack_list.containsKey(window_r)) {
                         readlist = ReadStack_list.get(window_r);
                         if (readlist.size()+1 > HighKmer) {
                             H_Kmer.add(window_r);
                             ReadStack_list.remove(window_r);
                         } else {
                             readlist.add(read_item);
                             ReadStack_list.put(window_r, readlist);
                         }
                     } else {
                         readlist = new ArrayList<ReadInfo>();
                         readlist.add(read_item);
                         ReadStack_list.put(window_r, readlist);
                     }
                 }*/
 			}
             
             // for all ReadStack
             Node node = new Node("MSG");
             node.setstr_raw("X");
             node.setCoverage(1);   
             Map<String, StringBuffer> outcode_list = new HashMap<String, StringBuffer>();
             
             for(String RS_idx : ReadStack_list.keySet())
             {// for each readstack
                 
             readlist = ReadStack_list.get(RS_idx);     
             //\\
             if (readlist.size() <= 5 ) {
                 continue;
             }
             //\\\
             
             Collections.sort(readlist, new ReadComparator_right());
             int right_len = readlist.get(0).seq.length - IDX - readlist.get(0).pos;
             Collections.sort(readlist, new ReadComparator());
             int left_len = readlist.get(0).pos;
             ReadInfo[] readarray = readlist.toArray(new ReadInfo[readlist.size()]);
             readlist.clear();
             
             //\\\\\\\
             //\\DEBUG
             /*output.collect(new Text(node.getNodeId()), new Text( "[" + Node.dna2str(RS_idx) + "]" + RS_idx));
             for(int i=0; i < readarray.length; i++){
                 //\\ DEBUG
                 String start_pos="";
                 for(int j=0; j < (left_len - readarray[i].pos); j++) {
                     start_pos = start_pos + " ";
                 }
                 output.collect(new Text(node.getNodeId()), new Text(start_pos + new String(readarray[i].seq)+ " " + readarray[i].dir + " " + readarray[i].id));
             }*/
             //\\\\\\\ debug
             //--- for each column
             
             int[] array = new int[6];
             int[] lose = new int[4];
             
             //\\
             //String debug = "";
             //\\            
             //  left range
             int majority = 2;
             int reads_threshold = 6;    
             for(int j=left_len - 1; j >= 0; j--) {
                 char consensus = 'N';
                 for(int k=0; k < 6; k++) {
                     array[k] = 0;
                     if( k < 4) {
                         lose[k] = 0;
                     }
                 } 
                 //\\
                 //debug = "";
                 //\\
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     if (readitem.pos-left_len+j < 0) {
                         //debug = debug + " ";
                         continue;
                     }
                     array[4] = array[4] + 1;
                     int quality_value = readitem.int_qv[readitem.pos-left_len+j];
                     char base_char = (char)readitem.seq[readitem.pos-left_len+j];
                     /*int quality_value;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(readarray[i].pos-left_len+j) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(readarray[i].pos-left_len+j);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(readarray[i].pos-left_len+j) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(readarray[i].pos-left_len+j);
                     }*/
                     if (quality_value < 0) {
                         quality_value = 0;
                     } else if (quality_value > 40) {
                         quality_value = 40;
                     }
                     if (base_char == 'A') {
                         array[0] = array[0] + quality_value;
                         if (quality_value >= 20)
                             lose[0] = lose[0]+1;
                     } else if (base_char == 'T') {
                         array[1] = array[1] + quality_value;
                         if (quality_value >= 20)
                             lose[1] = lose[1]+1;
                     } else if (base_char == 'C') {
                         array[2] = array[2] + quality_value;
                         if (quality_value >= 20)
                             lose[2] = lose[2]+1;
                     } else if (base_char == 'G') {
                         array[3] = array[3] + quality_value;
                         if (quality_value >= 20)
                             lose[3] = lose[3]+1;
                     }
                     //\\
                     //debug = debug + base_char;
                     //\\
                 }
                 //\\
                 //output.collect(new Text(node.getNodeId()), new Text("{" + debug + "}"));
                 //\\
                 if ( array[0] > array[1] && array[0] > array[2] && array[0] > array[3]) {
                     consensus = 'A';
                     array[5] = array[0];
                 } else if (array[1] > array[0] && array[1] > array[2] && array[1] > array[3]) {
                     consensus = 'T';
                     array[5] = array[1];
                 } else if (array[2] > array[0] && array[2] > array[1] && array[2] > array[3]) {
                     consensus = 'C';
                     array[5] = array[2];
                 } else if (array[3] > array[0] && array[3] > array[1] && array[3] > array[2]) {
                     consensus = 'G';
                     array[5] = array[3];
                 } else {
                     consensus = 'N';
                     array[5] = array[0];
                 }
                 int support = 0;
                 if (lose[0] >= majority ){
                 	support = support +1;
                 }
                 if (lose[1] >= majority ){
                 	support = support +1;
                 }
                 if (lose[2] >= majority ){
                 	support = support +1;
                 }
                 if (lose[3] >= majority ){
                 	support = support +1;
                 }
                 if (support >= 2 || array[4] < reads_threshold) {
                     break; //branch
                 }
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     if (readitem.pos-left_len+j < 0) {
                         continue;
                     }
                     String id = readitem.id;
                     /*int quality_value;
                     int quality_value_r;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(readarray[i].pos-left_len+j) - 33;
                         quality_value_r = (int)id_seq.get(readarray[i].id).qv.charAt(readarray[i].len-1-(readarray[i].pos-left_len+j)) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(readarray[i].pos-left_len+j);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(readarray[i].pos-left_len+j) -33;
                         quality_value_r = (int)qv_reverse.charAt(readarray[i].len-1-(readarray[i].pos-left_len+j)) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(readarray[i].pos-left_len+j);
                     }*/
                     int quality_value = readitem.int_qv[readitem.pos-left_len+j];
                     int quality_value_r = readitem.int_qv[readitem.seq.length-1-(readitem.pos-left_len+j)];
                     char base_char = (char)readitem.seq[readitem.pos-left_len+j];
                     int pos = 0;
                     char chr = 'X';
                     
                     if (consensus == base_char) {
                         // Comfirmation
                         boolean confirm = false;
                         if (lose[0] >= 2 && base_char == 'A' && (lose[1] < 2 && lose[2] < 2 && lose[3] < 2)) {
                             confirm = true;
                         }
                         if (lose[1] >= 2 && base_char == 'T' && (lose[0] < 2 && lose[2] < 2 && lose[3] < 2)) {
                             confirm = true;
                         }
                         if (lose[2] >= 2 && base_char == 'C' && (lose[0] < 2 && lose[1] < 2 && lose[3] < 2)) {
                             confirm = true;
                         }
                         if (lose[3] >= 2 && base_char == 'G' && (lose[0] < 2 && lose[1] < 2 && lose[2] < 2)) {
                             confirm = true;
                         }
                         if (confirm ) {
                             boolean submit = true;
                             if (readitem.dir && quality_value < 20){
                                 pos = readitem.pos-left_len+j;
                             } else if (!readitem.dir && quality_value_r < 20) {
                                 pos = (readitem.seq.length-1-(readitem.pos-left_len+j));
                             } else {
                                 submit = false;
                             }
                             if (submit) {
                                 //\\
                                 //output.collect(new Text(id), new Text(pos+":LC"));
                                 //\\
                                 if (outcode_list.containsKey(id)){
                                     StringBuffer sb = outcode_list.get(id);
                                     if (pos >= sb.length()){
                                         for(int k=sb.length(); k<=pos; k++) {
                                             sb.append("X");
                                         }
                                         sb.setCharAt(pos, 'N');
                                     } else {
                                         sb.setCharAt(pos, 'N');
                                     }
                                     outcode_list.put(id, sb);
                                     reporter.incrCounter("Brush", "confirm_char", 1);
                                 } else {
                                     StringBuffer sb = new StringBuffer();
                                     for(int k=0; k <= pos; k++) {
                                         sb.append("X");
                                     }
                                     sb.setCharAt(pos, 'N');
                                     outcode_list.put(id, sb);
                                     reporter.incrCounter("Brush", "confirm_char", 1);
                                 }
                             }
                         }
                         //\\\\
                     } else {
                         if (consensus != 'N') {
                             //\\\
                             float A_ratio = (float)array[0]/(float)array[5];
                             float T_ratio = (float)array[1]/(float)array[5];
                             float C_ratio = (float)array[2]/(float)array[5];
                             float G_ratio = (float)array[3]/(float)array[5];
                             if (consensus == 'A' && (T_ratio > 0.25 || C_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'T' && (A_ratio > 0.25 || C_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'C' && (A_ratio > 0.25 || T_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'C' && (A_ratio > 0.25 || T_ratio > 0.25 || C_ratio > 0.25)) {
                                 break;
                             }
                             //\\
                             if( base_char == 'A' && ( lose[0] >= 2 || (float)array[0]/(float)array[5] > 0.25f)) {
                                 break;
                             }
                             if( base_char == 'T' && ( lose[1] >= 2 || (float)array[1]/(float)array[5] > 0.25f)) {
                                 break;
                             }
                             if( base_char == 'C' && ( lose[2] >= 2 || (float)array[2]/(float)array[5] > 0.25f)) {
                                 break;
                             }
                             if( base_char == 'G' && ( lose[3] >= 2 || (float)array[3]/(float)array[5] > 0.25f)) {
                                 break;
                             }
                             if (readitem.dir){
                                 pos = readitem.pos-left_len+j;
                                 chr = consensus;
                             } else if (!readitem.dir) {
                                 pos = readitem.seq.length-1-(readitem.pos-left_len+j);
                                 chr = Node.rc(consensus+"").charAt(0);
                             }
                             //\\
                             //output.collect(new Text(id), new Text(pos+":L"));
                             //\\
                             if (outcode_list.containsKey(id)){
                                 StringBuffer sb = outcode_list.get(id);
                                 if (pos >= sb.length()){
                                     for(int k=sb.length(); k<=pos; k++) {
                                         sb.append("X");
                                     }
                                     sb.setCharAt(pos, chr);
                                 } else {
                                     if (sb.charAt(pos) == 'X') {
                                         sb.setCharAt(pos, chr);
                                     } else if (sb.charAt(pos) != chr) {
                                         sb.setCharAt(pos, 'N');
                                     }
                                 }
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "fix_char", 1);
                             } else {
                                 StringBuffer sb = new StringBuffer();
                                 for(int k=0; k <= pos; k++) {
                                     sb.append("X");
                                 }
                                 sb.setCharAt(pos, chr);
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "fix_char", 1);
                             }
                             //\\\\\\\\\\\\\\
                         }
                     }
                 }
             }
             // IDX range  
             for(int j=0; j < IDX; j++) {
                 for(int k=0; k < 6; k++) {
                     array[k] = 0;
                     if( k < 4) {
                         lose[k] = 0;
                     }
                 } 
                 //\\
                 //debug = "";
                 //\\
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     /*int quality_value;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(j+readarray[i].pos) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(j+readarray[i].pos);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(j+readarray[i].pos) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(j+readarray[i].pos);
                     }*/
                     int quality_value = (int)readitem.int_qv[j+readitem.pos];
                     char base_char = (char)readitem.seq[j+readitem.pos];
                     
                     if (quality_value < 0) {
                         quality_value = 0;
                     } else if (quality_value > 40) {
                         quality_value = 40;
                     }
                     array[4] = array[4]+1;
                     if (base_char == 'A') {
                     	array[0] = array[0] + quality_value;
                         if  (quality_value >= 20)
                             lose[0] = lose[0]+1;
                     } else if (base_char == 'T') { 
                     	array[1]= array[1] + quality_value;
                     	if  (quality_value >= 20)
                             lose[1] = lose[1]+1;
                     } else if (base_char == 'C') { 
                     	array[2]= array[2] + quality_value;
                     	if  (quality_value >= 20)
                             lose[2] = lose[2]+1;
                     } else if (base_char == 'G') {
                     	array[3]= array[3] + quality_value;
                     	if  (quality_value >= 20)
                             lose[3] = lose[3]+1;
                     }
                     //\\
                     //debug = debug + base_char;
                     //\\
                 }
                 //\\
                 //output.collect(new Text(node.getNodeId()), new Text("[" + debug + "]"));
                 //\\
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     String id = readitem.id;
                     /*int quality_value;
                     int quality_value_r;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(j+readarray[i].pos) - 33;
                         quality_value_r = (int)id_seq.get(readarray[i].id).qv.charAt(readarray[i].len-1-(j+readarray[i].pos)) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(j+readarray[i].pos);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(j+readarray[i].pos) -33;
                         quality_value_r = (int)qv_reverse.charAt(readarray[i].len-1-(j+readarray[i].pos)) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(j+readarray[i].pos);
                     }*/
                     char base_char = (char)readitem.seq[j+readitem.pos];
                     int quality_value = readitem.int_qv[j+readitem.pos];
                     int quality_value_r = readitem.int_qv[readitem.seq.length-1-(j+readitem.pos)];
                     int pos = 0;
                     boolean confirm = false;
                     if (base_char == 'A' && lose[0] >= 2 ) {
                         confirm = true;
                     }
                     if (base_char== 'T' && lose[1] >= 2 ) {
                         confirm = true;
                     }
                     if (base_char == 'C' && lose[2] >= 2 ) {
                         confirm = true;
                     }
                     if (base_char == 'G' && lose[3] >= 2 ) {
                         confirm = true;
                     }
                     if (confirm ) {
                         boolean submit=true;
                         if (readitem.dir  && quality_value < 20){
                             pos = j+readitem.pos;
                         } else if (!readitem.dir && quality_value_r < 20) {
                             pos = (readitem.seq.length-1-(j+readitem.pos));
                         } else {
                             submit = false;
                         }
                         if (submit) {
                             //\\
                             //output.collect(new Text(id), new Text(pos+":IC"));
                             //\\
                             if (outcode_list.containsKey(id)){
                                 StringBuffer sb = outcode_list.get(id);
                                 if (pos >= sb.length()){
                                     for(int k=sb.length(); k<=pos; k++) {
                                         sb.append("X");
                                     }
                                     sb.setCharAt(pos, 'N');
                                 } else {
                                     sb.setCharAt(pos, 'N');
                                 }
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "confirm_char", 1);
                             } else {
                                 StringBuffer sb = new StringBuffer();
                                 for(int k=0; k <= pos; k++) {
                                     sb.append("X");
                                 }
                                 sb.setCharAt(pos, 'N');
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "confirm_char", 1);
                             }
                         }
                         //\\\\\\\\\\\\\\\\\\\\\\\\\\
                     }
                 }
             }
             //  right range 
             //for(int j=readarray[i].pos + IDX; j < readarray[i].seq.length; j++) {
             for(int j=0; j < right_len; j++) {
                 char consensus = 'N';
                 for(int k=0; k < 6; k++) {
                     array[k] = 0;
                     if (k < 4) {
                         lose[k] = 0;
                     }
                 } 
                 //\\
                 //debug = "";
                 //\\
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     if (j+readitem.pos + IDX >= readitem.seq.length) {
                         //debug = debug + " ";
                         continue;
                     }
                     array[4] = array[4] + 1;
                     /*int quality_value;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(j+readarray[i].pos + IDX) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(j+readarray[i].pos + IDX);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(j+readarray[i].pos + IDX) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(j+readarray[i].pos + IDX);
                     }*/
                     int quality_value = readitem.int_qv[j+readarray[i].pos + IDX];
                     char base_char = (char)readitem.seq[j+readarray[i].pos + IDX];
                     if (quality_value < 0) {
                         quality_value = 0;
                     } else if (quality_value > 40) {
                         quality_value = 40;
                     }
                     if (base_char == 'A') {
                         array[0] = array[0] + quality_value;
                         if (quality_value >= 20)
                             lose[0] = lose[0]+1;
                     } else if (base_char == 'T') {
                         array[1] = array[1] + quality_value;
                         if (quality_value >= 20)
                             lose[1] = lose[1]+1;
                     } else if (base_char == 'C') {
                         array[2] = array[2] + quality_value;
                         if (quality_value >= 20)
                             lose[2] = lose[2]+1;
                     } else if (base_char == 'G') {
                         array[3] = array[3] + quality_value;
                         if (quality_value >= 20)
                             lose[3] = lose[3]+1;
                     }
                     //debug = debug + base_char;
                 }
                 //\\
                 //output.collect(new Text(node.getNodeId()), new Text("(" + debug + ")"));
                 //\\
                 if ( array[0] > array[1] && array[0] > array[2] && array[0] > array[3]) {
                     consensus = 'A';
                     array[5] = array[0];
                 } else if (array[1] > array[0] && array[1] > array[2] && array[1] > array[3]) {
                     consensus = 'T';
                     array[5] = array[1];
                 } else if (array[2] > array[0] && array[2] > array[1] && array[2] > array[3]) {
                     consensus = 'C';
                     array[5] = array[2];
                 } else if (array[3] > array[0] && array[3] > array[1] && array[3] > array[2]) {
                     consensus = 'G';
                     array[5] = array[3];
                 } else {
                     consensus = 'N';
                     array[5] = array[0];
                 }
                 int support = 0;
                 if (lose[0] >= majority ){
                 	support = support +1;
                 }
                 if (lose[1] >= majority ){
                 	support = support +1;
                 }
                 if (lose[2] >= majority ){
                 	support = support +1;
                 }
                 if (lose[3] >= majority ){
                 	support = support +1;
                 }
                 if (support >= 2 || array[4] < reads_threshold) {
                     break; //branch
                 }
                 for(int i=0; i < readarray.length; i++) {
                     ReadInfo readitem = readarray[i];
                     if (j+readitem.pos + IDX >= readitem.seq.length) {
                         continue;
                     }
                     String id = readitem.id;
                     /*int quality_value;
                     int quality_value_r;
                     char base_char;
                     if (readarray[i].dir) {
                         quality_value = (int)id_seq.get(readarray[i].id).qv.charAt(j+readarray[i].pos + IDX) - 33;
                         quality_value_r = (int)id_seq.get(readarray[i].id).qv.charAt(readarray[i].len-1-(j+readarray[i].pos + IDX)) - 33;
                         base_char = id_seq.get(readarray[i].id).seq.charAt(j+readarray[i].pos + IDX);
                     } else {
                         String qv_reverse = new StringBuffer(id_seq.get(readarray[i].id).qv).reverse().toString();
                         quality_value = (int)qv_reverse.charAt(j+readarray[i].pos + IDX) -33;
                         quality_value_r = (int)qv_reverse.charAt(readarray[i].len-1-(j+readarray[i].pos + IDX)) -33;
                         base_char = Node.rc(id_seq.get(readarray[i].id).seq).charAt(j+readarray[i].pos + IDX);
                     }*/
                     char base_char = (char)readitem.seq[j+readitem.pos + IDX];
                     int quality_value = readitem.int_qv[j+readitem.pos + IDX];
                     int quality_value_r = readitem.int_qv[readitem.seq.length-1-(j+readitem.pos + IDX)];
                     int pos = 0;
                     char chr = 'X';
                     if (consensus == base_char){
                         // Comfirmation
                         boolean confirm = false;
                         if (base_char == 'A' && ( lose[1] < 2 && lose[2] < 2 && lose[3] < 2) && lose[0] >= 2 ) {
                             confirm = true;
                         }
                         if (base_char == 'T' && ( lose[0] < 2 && lose[2] < 2 && lose[3] < 2) && lose[1] >= 2 ) {
                             confirm = true;
                         }
                         if (base_char == 'C' && ( lose[0] < 2 && lose[1] < 2 && lose[3] < 2) && lose[2] >= 2 ) {
                             confirm = true;
                         }
                         if (base_char == 'G' && ( lose[0] < 2 && lose[1] < 2 && lose[2] < 2) && lose[3] >= 2 ) {
                             confirm = true;
                         }
                         
                         if (confirm ) {
                             boolean submit=true;
                             if (readitem.dir && quality_value < 20){
                                 pos = j+readitem.pos + IDX;
                             } else if (!readitem.dir && quality_value_r < 20) {
                                 pos = readitem.seq.length-1-(j+readitem.pos + IDX);
                             } else {
                                 submit = false;
                             }
                             if (submit) {
                                 //\\
                                 //output.collect(new Text(id), new Text(pos+":RC"));
                                 //\\
                                 if (outcode_list.containsKey(id)){
                                     StringBuffer sb = outcode_list.get(id);
                                     if (pos >= sb.length()){
                                         for(int k=sb.length(); k<=pos; k++) {
                                             sb.append("X");
                                         }
                                         sb.setCharAt(pos, 'N');
                                     } else {
                                         sb.setCharAt(pos, 'N');
                                     }
                                     outcode_list.put(id, sb);
                                     reporter.incrCounter("Brush", "confirm_char", 1);
                                 } else {
                                     StringBuffer sb = new StringBuffer();
                                     for(int k=0; k <= pos; k++) {
                                         sb.append("X");
                                     }
                                     sb.setCharAt(pos, 'N');
                                     outcode_list.put(id, sb);
                                     reporter.incrCounter("Brush", "confirm_char", 1);
                                 }
                             }
                             //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
                         }
                     } else {
                         if (consensus != 'N') {
                             //\\
                             float A_ratio = (float)array[0]/(float)array[5];
                             float T_ratio = (float)array[1]/(float)array[5];
                             float C_ratio = (float)array[2]/(float)array[5];
                             float G_ratio = (float)array[3]/(float)array[5];
                             if (consensus == 'A' && (T_ratio > 0.25 || C_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'T' && (A_ratio > 0.25 || C_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'C' && (A_ratio > 0.25 || T_ratio > 0.25 || G_ratio > 0.25)) {
                                 break;
                             }
                             if (consensus == 'G' && (A_ratio > 0.25 || T_ratio > 0.25 || C_ratio > 0.25)) {
                                 break;
                             }
                             //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
                             if (base_char == 'A' && ( lose[0] >= 2 || (float)array[0]/(float)array[5] > 0.25f) ) {
                                 break;
                             }
                             if (base_char == 'T' && ( lose[1] >= 2 || (float)array[1]/(float)array[5] > 0.25f )) {
                                 break;
                             }
                             if (base_char == 'C' && ( lose[2] >= 2 || (float)array[2]/(float)array[5] > 0.25f )) {
                                 break;
                             }
                             if (base_char == 'G' && ( lose[3] >= 2 || (float)array[3]/(float)array[5] > 0.25f )) {
                                 break;
                             }
                             //\\
                             if (readitem.dir){
                                 pos = j+readitem.pos + IDX;
                                 chr = consensus;
                             } else if (!readitem.dir) {
                                 pos = (readitem.seq.length-1-(j+readitem.pos + IDX));
                                 chr = Node.rc(consensus+"").charAt(0);
                             }
                             //\\
                             //output.collect(new Text(id), new Text(pos+":R"));
                             //\\
                             if (outcode_list.containsKey(id)){
                                 StringBuffer sb = outcode_list.get(id);
                                 if (pos >= sb.length()){
                                     for(int k=sb.length(); k<=pos; k++) {
                                         sb.append("X");
                                     }
                                     sb.setCharAt(pos, chr);
                                 } else {
                                     if (sb.charAt(pos) == 'X') {
                                         sb.setCharAt(pos, chr);
                                     } 
                                 }
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "fix_char", 1);
                             } else {
                                 StringBuffer sb = new StringBuffer();
                                 for(int k=0; k <= pos; k++) {
                                     sb.append("X");
                                 }
                                 sb.setCharAt(pos, chr);
                                 outcode_list.put(id, sb);
                                 reporter.incrCounter("Brush", "fix_char", 1);
                             }
                             //\\\\\\\\\\\\\\\\\\\\\\
                         }
                     }
                 }
             }
             } // for each readstack
             
             
             boolean export = false;
             for(String read_id : outcode_list.keySet())
             {
                 String msg="";
                 msg = Node.str2code(outcode_list.get(read_id).toString());
                 node.addConfirmations(read_id, msg);
                 //output.collect(new Text(read_id), new Text(Node.UPDATEMSG + "\t" + msg));
                 export = true;
             }
             if (export) {
                 output.collect(new Text(node.getNodeId()), new Text(node.toNodeMsg()));
             }
 		}
 	}
 
 
 
 	public RunningJob run(String inputPath, String outputPath, int idx) throws Exception
 	{
 		sLogger.info("Tool name: FindError");
 		sLogger.info(" - input: "  + inputPath);
 		sLogger.info(" - output: " + outputPath);
 
 		JobConf conf = new JobConf(FindError.class);
 		conf.setJobName("FindError " + inputPath + " " + Config.K);
         conf.setLong("IDX", idx);
         
 		Config.initializeConfiguration(conf);
 
 		FileInputFormat.addInputPath(conf, new Path(inputPath));
 		FileOutputFormat.setOutputPath(conf, new Path(outputPath));
 
 		conf.setInputFormat(TextInputFormat.class);
 		conf.setOutputFormat(TextOutputFormat.class);
 
 		conf.setMapOutputKeyClass(Text.class);
 		conf.setMapOutputValueClass(Text.class);
 
 		conf.setOutputKeyClass(Text.class);
 		conf.setOutputValueClass(Text.class);
 
 		conf.setMapperClass(FindErrorMapper.class);
 		conf.setReducerClass(FindErrorReducer.class);
 
 		//delete the output directory if it exists already
 		FileSystem.get(conf).delete(new Path(outputPath), true);
 
 		return JobClient.runJob(conf);
 	}
 
 	public int run(String[] args) throws Exception
 	{
 		String inputPath  = "";
 		String outputPath = "";
 		Config.K = 21;
 
 		long starttime = System.currentTimeMillis();
 
 		run(inputPath, outputPath, 1);
 
 		long endtime = System.currentTimeMillis();
 
 		float diff = (float) (((float) (endtime - starttime)) / 1000.0);
 
 		System.out.println("Runtime: " + diff + " s");
 
 		return 0;
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		int res = ToolRunner.run(new Configuration(), new FindError(), args);
 		System.exit(res);
 	}
 }
 
