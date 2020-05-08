 package edu.mit.wi.haploview;
 
 
 public interface Constants {
 
     //main jframe setup stuff & labels.
     public static final String TITLE_STRING = "Haploview v2.05";
     public static final String READ_GENOTYPES = "Open genotype data";
     public static final String READ_MARKERS = "Load marker data";
     public static final String READ_ANALYSIS_TRACK = "Load analysis track";
     public static final String READ_BLOCKS_FILE = "Load block definitions";
 
     public static final String EXPORT_TEXT = "Export current tab to text";
     public static final String EXPORT_PNG = "Export current tab to PNG";
     public static final String EXPORT_OPTIONS = "Export options";
 
     public static final String CLEAR_BLOCKS = "Clear all blocks";
     public static final String CUST_BLOCKS = "Customize Block Definitions";
 
     public static final String VIEW_DPRIME = "LD Plot";
     public static final String VIEW_HAPLOTYPES = "Haplotypes";
     public static final String VIEW_LOD = "Genotyping completeness";
     public static final String VIEW_CHECK_PANEL = "Check Markers";
     public static final String VIEW_TDT = "Association";
 
     //main frame tab numbers
     public static final int VIEW_D_NUM = 0;
     public static final int VIEW_HAP_NUM = 1;
     public static final int VIEW_CHECK_NUM = 2;
     public static final int VIEW_TDT_NUM = 3;
 
     //export modes
     public static final int PNG_MODE = 0;
     public static final int TXT_MODE = 1;
     public static final int COMPRESSED_PNG_MODE = 2;
     public static final int TABLE_TYPE = 0;
     public static final int LIVE_TYPE = 1;
 
     //block defs
     public static final int BLOX_GABRIEL = 0;
     public static final int BLOX_4GAM = 1;
     public static final int BLOX_SPINE = 2;
     public static final int BLOX_CUSTOM = 3;
     public static final int BLOX_ALL = 4;
     public static final int BLOX_NONE = 5;
 
     //filetypes
     static final int GENO = 0;
     static final int INFO = 1;
     static final int HAPS = 2;
     static final int PED = 3;
     static final int DCC = 4;
 
     static final String HELP_OUTPUT = TITLE_STRING + " command line options\n" +
                         "-h, -help                     print this message\n" +
                         "-n                            command line output only\n" +
                         "-q                            quiet mode- doesnt print any warnings or information to screen\n" +
                         "-p <pedfile>                  specify an input file in pedigree file format\n" +
                         "         pedfile specific options (nogui mode only): \n" +
                         "         --showcheck       displays the results of the various pedigree integrity checks\n" +
                         "         --skipcheck       skips the various pedfile checks\n" +
                         //"         --ignoremarkers <markers> ignores the specified markers.<markers> is a comma\n" +
                         //"                                   seperated list of markers. eg. 1,5,7,19,25\n" +
                         "-a <hapmapfile>               specify an input file in HapMap format\n" +
                         "-l <hapsfile>                 specify an input file in .haps format\n" +
                         "-i <infofile>                 specify a marker info file\n" +
                         "-b <batchfile>                batch mode. batchfile should contain a list of files either all genotype or alternating genotype/info\n" +
                         "-k <blockfile>                blocks file, one block per line, will force output for these blocks\n" +
                         "-d                            outputs dprime to <inputfile>.DPRIME\n" +
                         "-c                            outputs marker checks to <inputfile>.CHECK\n" +
                         "                              note: -d  and -c default to no blocks output. use -o to also output blocks\n" +
                         "-o <GAB,GAM,SPI,ALL>          output type. Gabriel, 4 gamete, spine output or all 3. default is Gabriel.\n" +
                         "-m <distance>                 maximum comparison distance in kilobases (integer). default is 500";
 }
