 package com.spartango.hicgraph.deploy;
 
 import com.spartango.ensembl.model.Genome;
 import com.spartango.ensembl.raw.EnsemblParser;
 import com.spartango.hicgraph.analysis.cluster.Clusterer;
 import com.spartango.hicgraph.analysis.cluster.CoefficientClusterer;
 import com.spartango.hicgraph.analysis.stat.StatisticGatherer;
 import com.spartango.hicgraph.data.filters.IntraCompartmentFilter;
 import com.spartango.hicgraph.data.gene.GeneBinner;
 import com.spartango.hicgraph.data.raw.HiCDataSource;
 import com.spartango.hicgraph.data.raw.HiCParser;
 import com.spartango.hicgraph.graph.BinaryGraphBuilder;
 import com.spartango.hicgraph.graph.GraphBuilder;
 
 public class Main {
 
     public static final String RAWDATA_PREFIX   = "/Volumes/DarkIron/HiC Data/raw/";
    public static final String DATA_PATH        = RAWDATA_PREFIX + "all_reads.txt";
     public static final String ENSEMBL_DB_PATH  = RAWDATA_PREFIX
                                                   + "Homo_sapiens.GRCh37.65.gtf";
     public static final String COMPARTMENT_PATH = RAWDATA_PREFIX
                                                   + "HiCCompartment_";
 
    public static final String OUTPUT_PATH      = "/Volumes/DarkIron/HiC Data/data/";
 
     public static void main(String[] args) {
         // Setup data source
         HiCDataSource dataSource = new HiCParser(DATA_PATH);
 
         Genome humanGenome = EnsemblParser.buildGenome(ENSEMBL_DB_PATH,
                                                        COMPARTMENT_PATH, 100000);
 
         GeneBinner binner = new GeneBinner(50000, humanGenome);
         //HiCDataSource dataSource = new ControlDataSource(21392274);
 
         // Setup Graph Pipe
         GraphBuilder builder = new BinaryGraphBuilder();
         builder.addFilter(new IntraCompartmentFilter(humanGenome));
 
         // Add data sources
 
         // Clusterer
        Clusterer clusterer = new CoefficientClusterer(.65);
 
         StatisticGatherer gatherer = new StatisticGatherer(OUTPUT_PATH);
 
         // Assemble pipes
         dataSource.addConsumer(binner);
         binner.addConsumer(builder);
 
         builder.addConsumer(clusterer);
         clusterer.addConsumer(gatherer);
 
         // Start pipes
         dataSource.startReading();
 
         // Let them take care of themselves
         while (true) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 }
