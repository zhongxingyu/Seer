 /*
* $Id: PedFile.java,v 1.25 2004/09/21 19:26:58 jcbarret Exp $
 * WHITEHEAD INSTITUTE
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This software and its documentation are copyright 2002 by the
 * Whitehead Institute for Biomedical Research.  All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever.  The Whitehead Institute can not be responsible for its
 * use, misuse, or functionality.
 */
 package edu.mit.wi.pedfile;
 
 
 import edu.mit.wi.haploview.Chromosome;
 import edu.mit.wi.haploview.Options;
 
 import java.util.*;
 //import edu.mit.wi.haploview.Chromosome;
 
 /**
  * Handles input and storage of Pedigree files
  *
  * this class is not thread safe (untested).
  * modified from original Pedfile and checkdata classes by Hui Gong
  * @author Julian Maller
  */
 public class PedFile {
     private Hashtable families;
     private Vector axedPeople = new Vector();
     private Vector axedFamilies = new Vector();
 
     /*
     * stores the familyIDs and individualIDs of all individuals found by parse()
     * in order. this is useful for outputting Pedigree information to a file of another type.
     * the information is stored as an array of two strings.
     * also used in parsing hapmap data
     */
     private Vector order;
     private Vector results = null;
     private String[][] hminfo;
 
     private static Hashtable hapMapTranslate;
 
     public PedFile(){
 
         //hardcoded hapmap info
         this.families = new Hashtable();
 
         hapMapTranslate = new Hashtable(90,1);
         hapMapTranslate.put("NA10846", "1334 NA10846 NA12144 NA12145 1 0" );
         hapMapTranslate.put("NA12144", "1334 NA12144 0 0 1 0");
         hapMapTranslate.put("NA12145", "1334 NA12145 0 0 2 0");
         hapMapTranslate.put("NA10847", "1334 NA10847 NA12146 NA12239 2 0" );
         hapMapTranslate.put("NA12146", "1334 NA12146 0 0 1 0");
         hapMapTranslate.put("NA12239", "1334 NA12239 0 0 2 0");
         hapMapTranslate.put("NA07029", "1340 NA07029 NA06994 NA07000 1 0" );
         hapMapTranslate.put("NA06994", "1340 NA06994 0 0 1 0");
         hapMapTranslate.put("NA07000", "1340 NA07000 0 0 2 0");
         hapMapTranslate.put("NA07019", "1340 NA07019 NA07022 NA07056 2 0" );
         hapMapTranslate.put("NA07022", "1340 NA07022 0 0 1 0");
         hapMapTranslate.put("NA07056", "1340 NA07056 0 0 2 0");
         hapMapTranslate.put("NA07048", "1341 NA07048 NA07034 NA07055 1 0" );
         hapMapTranslate.put("NA07034", "1341 NA07034 0 0 1 0");
         hapMapTranslate.put("NA07055", "1341 NA07055 0 0 2 0");
         hapMapTranslate.put("NA06991", "1341 NA06991 NA06993 NA06985 2 0" );
         hapMapTranslate.put("NA06993", "1341 NA06993 0 0 1 0");
         //hapMapTranslate.put("NA06993.dup", "dup NA06993.dup 0 0 1 0");
         hapMapTranslate.put("NA06985", "1341 NA06985 0 0 2 0");
         hapMapTranslate.put("NA10851", "1344 NA10851 NA12056 NA12057 1 0" );
         hapMapTranslate.put("NA12056", "1344 NA12056 0 0 1 0");
         hapMapTranslate.put("NA12057", "1344 NA12057 0 0 2 0");
         hapMapTranslate.put("NA07348", "1345 NA07348 NA07357 NA07345 2 0" );
         hapMapTranslate.put("NA07357", "1345 NA07357 0 0 1 0");
         hapMapTranslate.put("NA07345", "1345 NA07345 0 0 2 0");
         hapMapTranslate.put("NA10857", "1346 NA10857 NA12043 NA12044 1 0" );
         hapMapTranslate.put("NA12043", "1346 NA12043 0 0 1 0");
         hapMapTranslate.put("NA12044", "1346 NA12044 0 0 2 0");
         hapMapTranslate.put("NA10859", "1347 NA10859 NA11881 NA11882 2 0" );
         hapMapTranslate.put("NA11881", "1347 NA11881 0 0 1 0");
         hapMapTranslate.put("NA11882", "1347 NA11882 0 0 2 0");
         hapMapTranslate.put("NA10854", "1349 NA10854 NA11839 NA11840 2 0" );
         hapMapTranslate.put("NA11839", "1349 NA11839 0 0 1 0");
         hapMapTranslate.put("NA11840", "1349 NA11840 0 0 2 0");
         hapMapTranslate.put("NA10856", "1350 NA10856 NA11829 NA11830 1 0" );
         hapMapTranslate.put("NA11829", "1350 NA11829 0 0 1 0");
         hapMapTranslate.put("NA11830", "1350 NA11830 0 0 2 0");
         hapMapTranslate.put("NA10855", "1350 NA10855 NA11831 NA11832 2 0" );
         hapMapTranslate.put("NA11831", "1350 NA11831 0 0 1 0");
         hapMapTranslate.put("NA11832", "1350 NA11832 0 0 2 0");
         hapMapTranslate.put("NA12707", "1358 NA12707 NA12716 NA12717 1 0" );
         hapMapTranslate.put("NA12716", "1358 NA12716 0 0 1 0");
         hapMapTranslate.put("NA12717", "1358 NA12717 0 0 2 0");
         hapMapTranslate.put("NA10860", "1362 NA10860 NA11992 NA11993 1 0" );
         hapMapTranslate.put("NA11992", "1362 NA11992 0 0 1 0");
         hapMapTranslate.put("NA11993", "1362 NA11993 0 0 2 0");
        // hapMapTranslate.put("NA11993.dup", "dup NA11993.dup 0 0 2 0");
         hapMapTranslate.put("NA10861", "1362 NA10861 NA11994 NA11995 2 0" );
         hapMapTranslate.put("NA11994", "1362 NA11994 0 0 1 0");
         hapMapTranslate.put("NA11995", "1362 NA11995 0 0 2 0");
         hapMapTranslate.put("NA10863", "1375 NA10863 NA12264 NA12234 2 0" );
         hapMapTranslate.put("NA12264", "1375 NA12264 0 0 1 0");
         hapMapTranslate.put("NA12234", "1375 NA12234 0 0 2 0");
         hapMapTranslate.put("NA10830", "1408 NA10830 NA12154 NA12236 1 0" );
         hapMapTranslate.put("NA12154", "1408 NA12154 0 0 1 0");
         hapMapTranslate.put("NA12236", "1408 NA12236 0 0 2 0");
         hapMapTranslate.put("NA10831", "1408 NA10831 NA12155 NA12156 2 0" );
         hapMapTranslate.put("NA12155", "1408 NA12155 0 0 1 0");
         hapMapTranslate.put("NA12156", "1408 NA12156 0 0 2 0");
         //hapMapTranslate.put("NA12156.dup", "dup NA12156.dup 0 0 2 0");
         hapMapTranslate.put("NA10835", "1416 NA10835 NA12248 NA12249 1 0" );
         hapMapTranslate.put("NA12248", "1416 NA12248 0 0 1 0");
        // hapMapTranslate.put("NA12248.dup", "dup NA1248.dup 0 0 1 0");
         hapMapTranslate.put("NA12249", "1416 NA12249 0 0 2 0");
         hapMapTranslate.put("NA10838", "1420 NA10838 NA12003 NA12004 1 0" );
         hapMapTranslate.put("NA12003", "1420 NA12003 0 0 1 0");
         //hapMapTranslate.put("NA12003.dup", "dup NA12003.dup 0 0 1 0");
         hapMapTranslate.put("NA12004", "1420 NA12004 0 0 2 0");
         hapMapTranslate.put("NA10839", "1420 NA10839 NA12005 NA12006 2 0" );
         hapMapTranslate.put("NA12005", "1420 NA12005 0 0 1 0");
         hapMapTranslate.put("NA12006", "1420 NA12006 0 0 2 0");
         hapMapTranslate.put("NA12740", "1444 NA12740 NA12750 NA12751 2 0" );
         hapMapTranslate.put("NA12750", "1444 NA12750 0 0 1 0");
         hapMapTranslate.put("NA12751", "1444 NA12751 0 0 2 0");
         hapMapTranslate.put("NA12752", "1447 NA12752 NA12760 NA12761 1 0" );
         hapMapTranslate.put("NA12760", "1447 NA12760 0 0 1 0");
         hapMapTranslate.put("NA12761", "1447 NA12761 0 0 2 0");
         hapMapTranslate.put("NA12753", "1447 NA12753 NA12762 NA12763 2 0" );
         hapMapTranslate.put("NA12762", "1447 NA12762 0 0 1 0");
         hapMapTranslate.put("NA12763", "1447 NA12763 0 0 2 0");
         hapMapTranslate.put("NA12801", "1454 NA12801 NA12812 NA12813 1 0" );
         hapMapTranslate.put("NA12812", "1454 NA12812 0 0 1 0");
         hapMapTranslate.put("NA12813", "1454 NA12813 0 0 2 0");
         hapMapTranslate.put("NA12802", "1454 NA12802 NA12814 NA12815 2 0" );
         hapMapTranslate.put("NA12814", "1454 NA12814 0 0 1 0");
         hapMapTranslate.put("NA12815", "1454 NA12815 0 0 2 0");
         hapMapTranslate.put("NA12864", "1459 NA12864 NA12872 NA12873 1 0" );
         hapMapTranslate.put("NA12872", "1459 NA12872 0 0 1 0");
         hapMapTranslate.put("NA12873", "1459 NA12873 0 0 2 0");
         hapMapTranslate.put("NA12865", "1459 NA12865 NA12874 NA12875 2 0" );
         hapMapTranslate.put("NA12874", "1459 NA12874 0 0 1 0");
         hapMapTranslate.put("NA12875", "1459 NA12875 0 0 2 0");
         hapMapTranslate.put("NA12878", "1463 NA12878 NA12891 NA12892 2 0" );
         hapMapTranslate.put("NA12891", "1463 NA12891 0 0 1 0");
         hapMapTranslate.put("NA12892", "1463 NA12892 0 0 2 0");
         hapMapTranslate.put("NA18526", "chi1 NA18526 0 0 2 0");
         hapMapTranslate.put("NA18524", "chi2 NA18524 0 0 1 0");
         hapMapTranslate.put("NA18529", "chi3 NA18529 0 0 2 0");
         hapMapTranslate.put("NA18558", "chi4 NA18558 0 0 1 0");
         hapMapTranslate.put("NA18532", "chi5 NA18532 0 0 2 0");
         hapMapTranslate.put("NA18561", "chi6 NA18561 0 0 1 0");
         hapMapTranslate.put("NA18942", "jap1 NA18942 0 0 2 0");
         hapMapTranslate.put("NA18940", "jap2 NA18940 0 0 1 0");
         hapMapTranslate.put("NA18951", "jap3 NA18951 0 0 2 0");
         hapMapTranslate.put("NA18943", "jap4 NA18943 0 0 1 0");
         hapMapTranslate.put("NA18947", "jap5 NA18947 0 0 2 0");
         hapMapTranslate.put("NA18944", "jap6 NA18944 0 0 1 0");
         hapMapTranslate.put("NA18562", "chi7 NA18562 0 0 1 0");
         hapMapTranslate.put("NA18537", "chi8 NA18537 0 0 2 0");
         hapMapTranslate.put("NA18603", "chi9 NA18603 0 0 1 0");
         hapMapTranslate.put("NA18540", "chi10 NA18540 0 0 2 0");
         hapMapTranslate.put("NA18605", "chi11 NA18605 0 0 1 0");
         hapMapTranslate.put("NA18542", "chi12 NA18542 0 0 2 0");
         hapMapTranslate.put("NA18945", "jap7 NA18945 0 0 1 0");
         hapMapTranslate.put("NA18949", "jap8 NA18949 0 0 2 0");
         hapMapTranslate.put("NA18948", "jap9 NA18948 0 0 1 0");
         hapMapTranslate.put("NA18952", "jap10 NA18952 0 0 1 0");
         hapMapTranslate.put("NA18956", "jap11 NA18956 0 0 2 0");
         hapMapTranslate.put("NA18545", "chi13 NA18545 0 0 2 0");
         hapMapTranslate.put("NA18572", "chi46 NA18572 0 0 1 0");
         hapMapTranslate.put("NA18547", "chi15 NA18547 0 0 2 0");
         hapMapTranslate.put("NA18609", "chi16 NA18609 0 0 1 0");
         hapMapTranslate.put("NA18550", "chi17 NA18550 0 0 2 0");
         hapMapTranslate.put("NA18608", "chi18 NA18608 0 0 1 0");
         hapMapTranslate.put("NA18964", "jap12 NA18964 0 0 2 0");
         hapMapTranslate.put("NA18953", "jap13 NA18953 0 0 1 0");
         hapMapTranslate.put("NA18968", "jap14 NA18968 0 0 2 0");
         hapMapTranslate.put("NA18959", "jap15 NA18959 0 0 1 0");
         hapMapTranslate.put("NA18969", "jap16 NA18969 0 0 2 0");
         hapMapTranslate.put("NA18960", "jap17 NA18960 0 0 1 0");
         hapMapTranslate.put("NA18552", "chi19 NA18552 0 0 2 0");
         hapMapTranslate.put("NA18611", "chi20 NA18611 0 0 1 0");
         hapMapTranslate.put("NA18555", "chi21 NA18555 0 0 2 0");
         hapMapTranslate.put("NA18564", "chi22 NA18564 0 0 2 0");
         hapMapTranslate.put("NA18961", "jap18 NA18961 0 0 1 0");
         hapMapTranslate.put("NA18972", "jap19 NA18972 0 0 2 0");
         hapMapTranslate.put("NA18965", "jap20 NA18965 0 0 1 0");
         hapMapTranslate.put("NA18973", "jap21 NA18973 0 0 2 0");
         hapMapTranslate.put("NA18966", "jap22 NA18966 0 0 1 0");
         hapMapTranslate.put("NA18975", "jap23 NA18975 0 0 2 0");
         hapMapTranslate.put("NA18566", "chi23 NA18566 0 0 2 0");
         hapMapTranslate.put("NA18563", "chi24 NA18563 0 0 1 0");
         hapMapTranslate.put("NA18570", "chi25 NA18570 0 0 2 0");
         hapMapTranslate.put("NA18612", "chi26 NA18612 0 0 1 0");
         hapMapTranslate.put("NA18571", "chi27 NA18571 0 0 2 0");
         hapMapTranslate.put("NA18620", "chi28 NA18620 0 0 1 0");
         hapMapTranslate.put("NA18976", "jap24 NA18976 0 0 2 0");
         hapMapTranslate.put("NA18967", "jap25 NA18967 0 0 1 0");
         hapMapTranslate.put("NA18978", "jap26 NA18978 0 0 2 0");
         hapMapTranslate.put("NA18970", "jap27 NA18970 0 0 1 0");
         hapMapTranslate.put("NA18980", "jap28 NA18980 0 0 2 0");
         hapMapTranslate.put("NA18995", "jap29 NA18995 0 0 1 0");
         hapMapTranslate.put("NA18621", "chi29 NA18621 0 0 1 0");
         hapMapTranslate.put("NA18594", "chi30 NA18594 0 0 2 0");
       //  hapMapTranslate.put("NA18594.dup", "dup 0 0 0 0 0");
       //  hapMapTranslate.put("NA18603.dup", "dup 0 0 0 0 0");
       //  hapMapTranslate.put("NA18609.dup", "dup 0 0 0 0 0");
       //  hapMapTranslate.put("NA18951.dup", "dup 0 0 0 0 0");
       //  hapMapTranslate.put("NA18995.dup", "dup 0 0 0 0 0");
         hapMapTranslate.put("NA18622", "chi31 NA18622 0 0 1 0");
         hapMapTranslate.put("NA18573", "chi32 NA18573 0 0 2 0");
         hapMapTranslate.put("NA18623", "chi33 NA18623 0 0 1 0");
         hapMapTranslate.put("NA18576", "chi34 NA18576 0 0 2 0");
         hapMapTranslate.put("NA18971", "jap30 NA18971 0 0 1 0");
         hapMapTranslate.put("NA18981", "jap31 NA18981 0 0 2 0");
         hapMapTranslate.put("NA18974", "jap32 NA18974 0 0 1 0");
         hapMapTranslate.put("NA18987", "jap33 NA18987 0 0 2 0");
         hapMapTranslate.put("NA18990", "jap34 NA18990 0 0 1 0");
         hapMapTranslate.put("NA18991", "jap35 NA18991 0 0 2 0");
         hapMapTranslate.put("NA18577", "chi35 NA18577 0 0 2 0");
         hapMapTranslate.put("NA18624", "chi36 NA18624 0 0 1 0");
         hapMapTranslate.put("NA18579", "chi37 NA18579 0 0 2 0");
         hapMapTranslate.put("NA18632", "chi38 NA18632 0 0 1 0");
         hapMapTranslate.put("NA18582", "chi39 NA18582 0 0 2 0");
         hapMapTranslate.put("NA18633", "chi40 NA18633 0 0 1 0");
         hapMapTranslate.put("NA18994", "jap36 NA18994 0 0 1 0");
         hapMapTranslate.put("NA18992", "jap37 NA18992 0 0 2 0");
         hapMapTranslate.put("NA18997", "jap38 NA18997 0 0 2 0");
         hapMapTranslate.put("NA18996", "jap39 NA18996 0 0 1 0");
         hapMapTranslate.put("NA18635", "chi41 NA18635 0 0 1 0");
         hapMapTranslate.put("NA18592", "chi42 NA18592 0 0 2 0");
         hapMapTranslate.put("NA18636", "chi43 NA18636 0 0 1 0");
         hapMapTranslate.put("NA18593", "chi44 NA18593 0 0 2 0");
         hapMapTranslate.put("NA18637", "chi45 NA18637 0 0 1 0");
         hapMapTranslate.put("NA19000", "jap40 NA19000 0 0 1 0");
         hapMapTranslate.put("NA18998", "jap41 NA18998 0 0 2 0");
         hapMapTranslate.put("NA19005", "jap42 NA19005 0 0 1 0");
         hapMapTranslate.put("NA18999", "jap43 NA18999 0 0 2 0");
         hapMapTranslate.put("NA19007", "jap44 NA19007 0 0 1 0");
         hapMapTranslate.put("NA19003", "jap45 NA19003 0 0 2 0");
         hapMapTranslate.put("NA18500", "Yoruba004 NA18500 NA18501 NA18502 1 0");
         hapMapTranslate.put("NA18501", "Yoruba004 NA18501 0 0 1 0");
         hapMapTranslate.put("NA18502", "Yoruba004 NA18502 0 0 2 0");
         hapMapTranslate.put("NA18503", "Yoruba005 NA18503 NA18504 NA18505 1 0");
         hapMapTranslate.put("NA18504", "Yoruba005 NA18504 0 0 1 0");
         hapMapTranslate.put("NA18505", "Yoruba005 NA18505 0 0 2 0");
         hapMapTranslate.put("NA18506", "Yoruba009 NA18506 NA18507 NA18508 1 0");
         hapMapTranslate.put("NA18507", "Yoruba009 NA18507 0 0 1 0");
         hapMapTranslate.put("NA18508", "Yoruba009 NA18508 0 0 2 0");
         hapMapTranslate.put("NA18860", "Yoruba012 NA18860 NA18859 NA18858 1 0");
         hapMapTranslate.put("NA18859", "Yoruba012 NA18859 0 0 1 0");
         hapMapTranslate.put("NA18858", "Yoruba012 NA18858 0 0 2 0");
         hapMapTranslate.put("NA18515", "Yoruba013 NA18515 NA18516 NA18517 1 0");
         hapMapTranslate.put("NA18516", "Yoruba013 NA18516 0 0 1 0");
         hapMapTranslate.put("NA18517", "Yoruba013 NA18517 0 0 2 0");
         hapMapTranslate.put("NA18521", "Yoruba016 NA18521 NA18522 NA18523 1 0");
         hapMapTranslate.put("NA18522", "Yoruba016 NA18522 0 0 1 0");
         hapMapTranslate.put("NA18523", "Yoruba016 NA18523 0 0 2 0");
         hapMapTranslate.put("NA18872", "Yoruba017 NA18872 NA18871 NA18870 1 0");
         hapMapTranslate.put("NA18871", "Yoruba017 NA18871 0 0 1 0");
         hapMapTranslate.put("NA18870", "Yoruba017 NA18870 0 0 2 0");
         hapMapTranslate.put("NA18854", "Yoruba018 NA18854 NA18853 NA18852 1 0");
         hapMapTranslate.put("NA18853", "Yoruba018 NA18853 0 0 1 0");
         hapMapTranslate.put("NA18852", "Yoruba018 NA18852 0 0 2 0");
         hapMapTranslate.put("NA18857", "Yoruba023 NA18857 NA18856 NA18855 1 0");
         hapMapTranslate.put("NA18856", "Yoruba023 NA18856 0 0 1 0");
         hapMapTranslate.put("NA18855", "Yoruba023 NA18855 0 0 2 0");
         hapMapTranslate.put("NA18863", "Yoruba024 NA18863 NA18862 NA18861 1 0");
         hapMapTranslate.put("NA18862", "Yoruba024 NA18862 0 0 1 0");
         hapMapTranslate.put("NA18861", "Yoruba024 NA18861 0 0 2 0");
         hapMapTranslate.put("NA18914", "Yoruba028 NA18914 NA18913 NA18912 1 0");
         hapMapTranslate.put("NA18913", "Yoruba028 NA18913 0 0 1 0");
         hapMapTranslate.put("NA18912", "Yoruba028 NA18912 0 0 2 0");
         hapMapTranslate.put("NA19094", "Yoruba040 NA19094 NA19092 NA19093 2 0");
         hapMapTranslate.put("NA19092", "Yoruba040 NA19092 0 0 1 0");
         hapMapTranslate.put("NA19093", "Yoruba040 NA19093 0 0 2 0");
         hapMapTranslate.put("NA19103", "Yoruba042 NA19103 NA19101 NA19102 1 0");
         hapMapTranslate.put("NA19101", "Yoruba042 NA19101 0 0 1 0");
         hapMapTranslate.put("NA19102", "Yoruba042 NA19102 0 0 2 0");
         hapMapTranslate.put("NA19139", "Yoruba043 NA19139 NA19138 NA19137 1 0");
         hapMapTranslate.put("NA19138", "Yoruba043 NA19138 0 0 1 0");
         hapMapTranslate.put("NA19137", "Yoruba043 NA19137 0 0 2 0");
         hapMapTranslate.put("NA19202", "Yoruba045 NA19202 NA19200 NA19201 2 0");
         hapMapTranslate.put("NA19200", "Yoruba045 NA19200 0 0 1 0");
         hapMapTranslate.put("NA19201", "Yoruba045 NA19201 0 0 2 0");
         hapMapTranslate.put("NA19173", "Yoruba047 NA19173 NA19171 NA19172 1 0");
         hapMapTranslate.put("NA19171", "Yoruba047 NA19171 0 0 1 0");
         hapMapTranslate.put("NA19172", "Yoruba047 NA19172 0 0 2 0");
         hapMapTranslate.put("NA19205", "Yoruba048 NA19205 NA19203 NA19204 1 0");
         hapMapTranslate.put("NA19203", "Yoruba048 NA19203 0 0 1 0");
         hapMapTranslate.put("NA19204", "Yoruba048 NA19204 0 0 2 0");
         hapMapTranslate.put("NA19211", "Yoruba050 NA19211 NA19210 NA19209 1 0");
         hapMapTranslate.put("NA19210", "Yoruba050 NA19210 0 0 1 0");
         hapMapTranslate.put("NA19209", "Yoruba050 NA19209 0 0 2 0");
         hapMapTranslate.put("NA19208", "Yoruba051 NA19208 NA19207 NA19206 1 0");
         hapMapTranslate.put("NA19207", "Yoruba051 NA19207 0 0 1 0");
         hapMapTranslate.put("NA19206", "Yoruba051 NA19206 0 0 2 0");
         hapMapTranslate.put("NA19161", "Yoruba056 NA19161 NA19160 NA19159 1 0");
         hapMapTranslate.put("NA19160", "Yoruba056 NA19160 0 0 1 0");
         hapMapTranslate.put("NA19159", "Yoruba056 NA19159 0 0 2 0");
         hapMapTranslate.put("NA19221", "Yoruba058 NA19221 NA19223 NA19222 2 0");
         hapMapTranslate.put("NA19223", "Yoruba058 NA19223 0 0 1 0");
         hapMapTranslate.put("NA19222", "Yoruba058 NA19222 0 0 2 0");
         hapMapTranslate.put("NA19120", "Yoruba060 NA19120 NA19119 NA19116 1 0");
         hapMapTranslate.put("NA19119", "Yoruba060 NA19119 0 0 1 0");
         hapMapTranslate.put("NA19116", "Yoruba060 NA19116 0 0 2 0");
         hapMapTranslate.put("NA19142", "Yoruba071 NA19142 NA19141 NA19140 1 0");
         hapMapTranslate.put("NA19141", "Yoruba071 NA19141 0 0 1 0");
         hapMapTranslate.put("NA19140", "Yoruba071 NA19140 0 0 2 0");
         hapMapTranslate.put("NA19154", "Yoruba072 NA19154 NA19153 NA19152 1 0");
         hapMapTranslate.put("NA19153", "Yoruba072 NA19153 0 0 1 0");
         hapMapTranslate.put("NA19152", "Yoruba072 NA19152 0 0 2 0");
         hapMapTranslate.put("NA19145", "Yoruba074 NA19145 NA19144 NA19143 1 0");
         hapMapTranslate.put("NA19144", "Yoruba074 NA19144 0 0 1 0");
         hapMapTranslate.put("NA19143", "Yoruba074 NA19143 0 0 2 0");
         hapMapTranslate.put("NA19129", "Yoruba077 NA19129 NA19128 NA19127 2 0");
         hapMapTranslate.put("NA19128", "Yoruba077 NA19128 0 0 1 0");
         hapMapTranslate.put("NA19127", "Yoruba077 NA19127 0 0 2 0");
         hapMapTranslate.put("NA19132", "Yoruba101 NA19132 NA19130 NA19131 2 0");
         hapMapTranslate.put("NA19130", "Yoruba101 NA19130 0 0 1 0");
         hapMapTranslate.put("NA19131", "Yoruba101 NA19131 0 0 2 0");
         hapMapTranslate.put("NA19100", "Yoruba105 NA19100 NA19098 NA19099 2 0");
         hapMapTranslate.put("NA19098", "Yoruba105 NA19098 0 0 1 0");
         hapMapTranslate.put("NA19099", "Yoruba105 NA19099 0 0 2 0");
         hapMapTranslate.put("NA19194", "Yoruba112 NA19194 NA19192 NA19193 1 0");
         hapMapTranslate.put("NA19192", "Yoruba112 NA19192 0 0 1 0");
         hapMapTranslate.put("NA19193", "Yoruba112 NA19193 0 0 2 0");
         hapMapTranslate.put("NA19240", "Yoruba117 NA19240 NA19239 NA19238 2 0");
         hapMapTranslate.put("NA19239", "Yoruba117 NA19239 0 0 1 0");
         hapMapTranslate.put("NA19238", "Yoruba117 NA19238 0 0 2 0");
     }
 
     /**
      * gets the order Vector
      * @return
      */
     public Vector getOrder() {
         return order;
     }
 
     /**
      *
      * @return enumeration containing a list of familyID's in the families hashtable
      */
     public Enumeration getFamList(){
         return this.families.keys();
     }
     /**
      *
      * @param familyID id of desired family
      * @return Family identified by familyID in families hashtable
      */
     public Family getFamily(String familyID){
         return (Family)this.families.get(familyID);
     }
 
     /**
      *
      * @return the number of Family objects in the families hashtable
      */
     public int getNumFamilies(){
         return this.families.size();
     }
 
     /**
      * this method iterates through each family in Hashtable families and adds up
      * the number of individuals in total across all families
      * @return the total number of individuals in all the family objects in the families hashtable
      */
     public int getNumIndividuals(){
         Enumeration famEnum = this.families.elements();
         int total =0;
         while (famEnum.hasMoreElements()) {
             Family fam = (Family) famEnum.nextElement();
             total += fam.getNumMembers();
         }
         return total;
     }
 
     /**
      * finds the first individual in the first family and returns the number of markers for that individual
      * @return the number of markers
      */
     public int getNumMarkers(){
         Enumeration famList = this.families.elements();
         int numMarkers = 0;
         while (famList.hasMoreElements()) {
             Family fam = (Family) famList.nextElement();
             Enumeration indList = fam.getMemberList();
             Individual ind = null;
             while(indList.hasMoreElements()){
                 try{
                     ind = fam.getMember((String)indList.nextElement());
                 }catch(PedFileException pfe){
                 }
                 numMarkers = ind.getNumMarkers();
                 if(numMarkers > 0){
                     return numMarkers;
                 }
             }
         }
         return 0;
     }
 
 
     /**
      * takes in a pedigree file in the form of a vector of strings and parses it.
      * data is stored in families in the member hashtable families
      * @param pedigrees a Vector of strings containing one pedigree line per string
      */
     public void parseLinkage(Vector pedigrees) throws PedFileException {
         int colNum = -1;
         boolean withOptionalColumn = false;
         int numLines = pedigrees.size();
         Individual ind;
 
         this.order = new Vector();
 
         for(int k=0; k<numLines; k++){
             StringTokenizer tokenizer = new StringTokenizer((String)pedigrees.get(k), "\n\t\" \"");
             //reading the first line
             if(colNum < 1){
                 //only check column number count for the first nonblank line
                 colNum = tokenizer.countTokens();
                 if(colNum%2==1) {
                     withOptionalColumn = true;
                 }
             }
             if(colNum != tokenizer.countTokens()) {
                 //this line has a different number of columns
                 //should send some sort of error message
                 //TODO: add something which stores number of markers for all lines and checks that they're consistent
                 throw new PedFileException("line number mismatch in pedfile. line " + (k+1));
             }
 
             ind = new Individual(tokenizer.countTokens());
 
             if(tokenizer.hasMoreTokens()){
 
                 ind.setFamilyID(tokenizer.nextToken().trim());
                 ind.setIndividualID(tokenizer.nextToken().trim());
                 ind.setDadID(tokenizer.nextToken().trim());
                 ind.setMomID(tokenizer.nextToken().trim());
                 try {
                     //TODO: affected/liability should not be forced into Integers!
                     ind.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                     ind.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                     if(withOptionalColumn) {
                         ind.setLiability(Integer.parseInt(tokenizer.nextToken().trim()));
                     }
                 }catch(NumberFormatException nfe) {
                     throw new PedFileException("Pedfile error: invalid gender or affected status on line " + (k+1));
                 }
 
                 while(tokenizer.hasMoreTokens()){
                     try {
                         int allele1 = Integer.parseInt(tokenizer.nextToken().trim());
                         int allele2 = Integer.parseInt(tokenizer.nextToken().trim());
                         if(allele1 <0 || allele1 > 4 || allele2 <0 || allele2 >4) {
                             throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1)
                                     + ".\n all genotypes must be 0-4.");
                         }
                         byte[] markers = new byte[2];
                         markers[0] = (byte)allele1;
                         markers[1]= (byte)allele2;
                         ind.addMarker(markers);
                     }catch(NumberFormatException nfe) {
                         throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1) );
                     }
                 }
 
                 //check if the family exists already in the Hashtable
                 Family fam = (Family)this.families.get(ind.getFamilyID());
                 if(fam == null){
                     //it doesnt exist, so create a new Family object
                     fam = new Family(ind.getFamilyID());
                 }
                 fam.addMember(ind);
                 this.families.put(ind.getFamilyID(),fam);
 
                 String[] indFamID = new String[2];
                 indFamID[0] = ind.getFamilyID();
                 indFamID[1] = ind.getIndividualID();
                 this.order.add(ind);
 
             }
         }
     }
 
     public void parseHapMap(Vector rawLines) throws PedFileException {
         int colNum = -1;
         int numLines = rawLines.size();
         Individual ind;
 
         this.order = new Vector();
 
         //sort first
         Vector lines = new Vector();
         Hashtable sortHelp = new Hashtable(numLines-1,1.0f);
         long[] pos = new long[numLines-1];
         lines.add(rawLines.get(0));
         for (int k = 1; k < numLines; k++){
             StringTokenizer st = new StringTokenizer((String) rawLines.get(k));
             //strip off 1st 3 cols
             st.nextToken();st.nextToken();st.nextToken();
             pos[k-1] = new Long(st.nextToken()).longValue();
             sortHelp.put(new Long(pos[k-1]),rawLines.get(k));
         }
         Arrays.sort(pos);
         for (int i = 0; i < pos.length; i++){
             lines.add(sortHelp.get(new Long(pos[i])));
         }
 
         //enumerate indivs
         StringTokenizer st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
         int numMetaColumns = 0;
         boolean doneMeta = false;
         while(!doneMeta && st.hasMoreTokens()){
             String thisfield = st.nextToken();
             numMetaColumns++;
             //first indiv ID will be a string beginning with "NA"
             if (thisfield.startsWith("NA")){
                 doneMeta = true;
             }
         }
         numMetaColumns--;
 
         st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
         for (int i = 0; i < numMetaColumns; i++){
             st.nextToken();
         }
         Vector namesIncludingDups = new Vector();
         StringTokenizer dt;
         while (st.hasMoreTokens()){
             ind = new Individual(numLines);
             String name = st.nextToken();
             namesIncludingDups.add(name);
             if (name.endsWith("dup")){
                 //skip dups (i.e. don't add 'em to ind array)
                 continue;
             }
             String details = (String)hapMapTranslate.get(name);
             if (details == null){
                 throw new PedFileException("Hapmap data format error: " + name);
             }
             dt = new StringTokenizer(details, "\n\t\" \"");
             ind.setFamilyID(dt.nextToken().trim());
             ind.setIndividualID(dt.nextToken().trim());
             ind.setDadID(dt.nextToken().trim());
             ind.setMomID(dt.nextToken().trim());
             try {
                 ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                 ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
             }catch(NumberFormatException nfe) {
                 throw new PedFileException("File error: invalid gender or affected status for indiv " + name);
             }
 
             //check if the family exists already in the Hashtable
             Family fam = (Family)this.families.get(ind.getFamilyID());
             if(fam == null){
                 //it doesnt exist, so create a new Family object
                 fam = new Family(ind.getFamilyID());
             }
             fam.addMember(ind);
             this.families.put(ind.getFamilyID(),fam);
 
             String[] indFamID = new String[2];
             indFamID[0] = ind.getFamilyID();
             indFamID[1] = ind.getIndividualID();
             this.order.add(ind);
         }
 
         //start at k=1 to skip header which we just processed above.
         hminfo = new String[numLines-1][];
         for(int k=1;k<numLines;k++){
             StringTokenizer tokenizer = new StringTokenizer((String)lines.get(k));
             //reading the first line
             if(colNum < 0){
                 //only check column number count for the first line
                 colNum = tokenizer.countTokens();
             }
             if(colNum != tokenizer.countTokens()) {
                 //this line has a different number of columns
                 //should send some sort of error message
                 //TODO: add something which stores number of markers for all lines and checks that they're consistent
                 throw new PedFileException("Line number mismatch in input file. line " + (k+1));
             }
 
             if(tokenizer.hasMoreTokens()){
                 hminfo[k-1] = new String[2];
                 for (int skip = 0; skip < numMetaColumns; skip++){
                     //meta-data crap
                     String s = tokenizer.nextToken().trim();
 
                     //get marker name, chrom and pos
                     if (skip == 0){
                         hminfo[k-1][0] = s;
                     }
                     if (skip == 2){
                         String dc = Chromosome.getDataChrom();
                         if (dc != null){
                            if (dc.equalsIgnoreCase(s)){
                                 throw new PedFileException("Hapmap file format error on line " + (k+1) +
                                         ":\n The file appears to contain multiple chromosomes:" +
                                         "\n" + dc + ", " + s);
                             }
                         }else{
                             Chromosome.setDataChrom(s);
                         }
                     }
                     if (skip == 3){
                         hminfo[k-1][1] = s;
                     }
                 }
                 int index = 0;
                 int indexIncludingDups = -1;
                 while(tokenizer.hasMoreTokens()){
                     String alleles = tokenizer.nextToken();
 
                     indexIncludingDups++;
                     //we've skipped the dups in the ind array, so we skip their genotypes
                     if (((String)namesIncludingDups.elementAt(indexIncludingDups)).endsWith("dup")){
                         continue;
                     }
 
                     ind = (Individual)order.elementAt(index);
                     int allele1=0, allele2=0;
                     if (alleles.substring(0,1).equals("A")){
                         allele1 = 1;
                     }else if (alleles.substring(0,1).equals("C")){
                         allele1 = 2;
                     }else if (alleles.substring(0,1).equals("G")){
                         allele1 = 3;
                     }else if (alleles.substring(0,1).equals("T")){
                         allele1 = 4;
                     }
                     if (alleles.substring(1,2).equals("A")){
                         allele2 = 1;
                     }else if (alleles.substring(1,2).equals("C")){
                         allele2 = 2;
                     }else if (alleles.substring(1,2).equals("G")){
                         allele2 = 3;
                     }else if (alleles.substring(1,2).equals("T")){
                         allele2 = 4;
                     }
                     byte[] markers = new byte[2];
                     markers[0] = (byte)allele1;
                     markers[1]= (byte)allele2;
                     ind.addMarker(markers);
                     index++;
                 }
             }
         }
     }
 
     public Vector check() throws PedFileException{
         //before we perform the check we want to prune out individuals with too much missing data
         //or trios which contain individuals with too much missing data
         Vector indList = getOrder();
         Individual currentInd;
         Family currentFamily;
 
         //deal with individuals who are missing too much data
         for(int x=0; x < indList.size(); x++){
             currentInd = (Individual)indList.elementAt(x);
             currentFamily = getFamily(currentInd.getFamilyID());
             double numMissing = 0;
             int numMarkers = currentInd.getNumMarkers();
             for (int i = 0; i < numMarkers; i++){
                 byte[] thisMarker = currentInd.getMarker(i);
                 if (thisMarker[0] == 0 || thisMarker[1] == 0){
                     numMissing++;
                 }
             }
             if (numMissing/numMarkers > Options.getMissingThreshold()){
                 //this person is missing too much data so remove him and then deal
                 //with his family connections
                 order.removeElement(currentInd);
                 axedPeople.add(currentInd.getIndividualID());
                 if (currentFamily.getNumMembers() > 1){
                     //there are more people in this family so deal with relatives appropriately
                     if (currentInd.hasEitherParent()){
                         //I have parents, so kick out any of my kids.
                         Enumeration peopleinFam = currentFamily.getMemberList();
                         while (peopleinFam.hasMoreElements()){
                             Individual nextMember = currentFamily.getMember((String)peopleinFam.nextElement());
                             if (nextMember.getDadID().equals(currentInd.getIndividualID()) ||
                                     nextMember.getMomID().equals(currentInd.getIndividualID())){
                                 order.removeElement(nextMember);
                                 currentFamily.removeMember(nextMember.getIndividualID());
                             }
                         }
                     }else{
                         //I have no parents but need to check if my spouse does
                         String spouseID = "";
                         Enumeration peopleinFam = currentFamily.getMemberList();
                         while (peopleinFam.hasMoreElements()){
                             Individual nextMember = currentFamily.getMember((String)peopleinFam.nextElement());
                             if (nextMember.getDadID().equals(currentInd.getIndividualID()))
                                 spouseID = nextMember.getMomID();
                             if (nextMember.getMomID().equals(currentInd.getIndividualID()))
                                 spouseID = nextMember.getDadID();
                         }
                         if (!spouseID.equals("")){
                             if (currentFamily.getMember(spouseID).hasEitherParent()){
                                 //remove my kids and leave my spouse alone
                                peopleinFam = currentFamily.getMemberList();
                                 while (peopleinFam.hasMoreElements()){
                                     Individual nextMember = currentFamily.getMember((String)peopleinFam.nextElement());
                                     if (nextMember.getDadID().equals(currentInd.getIndividualID()) ||
                                             nextMember.getMomID().equals(currentInd.getIndividualID())){
                                         order.removeElement(nextMember);
                                         currentFamily.removeMember(nextMember.getIndividualID());
                                     }
                                 }
                             }else{
                                 //knock off my spouse and make my first kid a founder (i.e. "0" for parents)
                                 //and remove any other kids
                                 order.removeElement(currentFamily.getMember(spouseID));
                                 currentFamily.removeMember(spouseID);
                                 peopleinFam = currentFamily.getMemberList();
                                 boolean oneFound = false;
                                 while (peopleinFam.hasMoreElements()){
                                     Individual nextMember = currentFamily.getMember((String)peopleinFam.nextElement());
                                     if (nextMember.getDadID().equals(currentInd.getIndividualID()) ||
                                             nextMember.getMomID().equals(currentInd.getIndividualID())){
                                         if (oneFound){
                                             order.removeElement(nextMember);
                                             currentFamily.removeMember(nextMember.getIndividualID());
                                         }else{
                                             nextMember.setDadID("0");
                                             nextMember.setMomID("0");
                                             oneFound = true;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 currentFamily.removeMember(currentInd.getIndividualID());
                 if (currentFamily.getNumMembers() == 0){
                     //if everyone in a family is gone, we remove it from the list
                     families.remove(currentInd.getFamilyID());
                     axedFamilies.add(currentInd.getFamilyID());
                 }
             }
         }
         indList = getOrder();
         for (int x = 0; x < indList.size(); x++){
             //after we've done all that go through and set the boolean for each person who has any kids
             currentInd = (Individual)indList.elementAt(x);
             currentFamily = getFamily(currentInd.getFamilyID());
             Enumeration peopleinFam = currentFamily.getMemberList();
             while (peopleinFam.hasMoreElements()){
                 Individual nextMember = currentFamily.getMember((String)peopleinFam.nextElement());
                 if (nextMember.getMomID().equals(currentInd.getIndividualID()) ||
                         nextMember.getDadID().equals(currentInd.getIndividualID())){
                     currentInd.setHasKids(true);
                     break;
                 }
             }
         }
 
         CheckData cd = new CheckData(this);
         Vector results = cd.check();
         /*int size = results.size();
         for (int i = 0; i < size; i++) {
         MarkerResult markerResult = (MarkerResult) results.elementAt(i);
         System.out.println(markerResult.toString());
         }*/
         this.results = results;
         return results;
     }
 
     public String[][] getHMInfo() {
         return hminfo;
     }
 
     public Vector getResults() {
         return results;
     }
 }
 
 
