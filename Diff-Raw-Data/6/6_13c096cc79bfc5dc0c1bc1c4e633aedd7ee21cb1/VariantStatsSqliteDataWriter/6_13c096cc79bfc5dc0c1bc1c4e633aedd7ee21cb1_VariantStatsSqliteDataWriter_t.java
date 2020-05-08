 package org.opencb.variant.lib.io.variant.writers;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectWriter;
 import org.apache.commons.io.FilenameUtils;
 import org.bioinfo.commons.utils.StringUtils;
 import org.opencb.variant.lib.core.formats.*;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.sql.*;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aaleman
  * Date: 8/30/13
  * Time: 1:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class VariantStatsSqliteDataWriter implements VariantStatsDataWriter {
 
     private String dbName;
     private Connection con;
     private Statement stmt;
     private PreparedStatement pstmt;
     private boolean createdSampleTable;
     private VariantAnalysisInfo vi;
 
 
     public VariantStatsSqliteDataWriter(String dbName) {
 
         this.dbName = dbName;
         this.stmt = null;
         this.pstmt = null;
         this.createdSampleTable = false;
         this.vi = new VariantAnalysisInfo();
     }
 
     @Override
     public boolean open() {
 
         try {
             Class.forName("org.sqlite.JDBC");
             con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
             con.setAutoCommit(false);
 
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.err.println(e.getClass().getName() + ": " + e.getMessage());
             return false;
         } catch (SQLException e) {
             e.printStackTrace();
             System.err.println(e.getClass().getName() + ": " + e.getMessage());
             return false;
 
         }
 
         return true;
     }
 
     @Override
     public boolean close() {
 
         try {
             con.close();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     @Override
     public boolean pre() {
         String globalStatsTable = "CREATE TABLE IF NOT EXISTS global_stats (" +
                 "name TEXT," +
                 " title TEXT," +
                 " value TEXT," +
                 "PRIMARY KEY (name));";
         String variant_stats = "CREATE TABLE IF NOT EXISTS variant_stats (" +
                 "id_variant INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "chromosome TEXT, " +
                 "position INT64, " +
                 "allele_ref TEXT, " +
                 "allele_alt TEXT, " +
                 "id TEXT, " +
                 "maf DOUBLE, " +
                 "mgf DOUBLE," +
                 "allele_maf TEXT, " +
                 "genotype_maf TEXT, " +
                 "miss_allele INT, " +
                 "miss_gt INT, " +
                 "mendel_err INT, " +
                 "is_indel INT, " +
                 "cases_percent_dominant DOUBLE, " +
                 "controls_percent_dominant DOUBLE, " +
                 "cases_percent_recessive DOUBLE, " +
                 "controls_percent_recessive DOUBLE);";
         String sample_stats = "CREATE TABLE IF NOT EXISTS sample_stats(" +
                 "name TEXT, " +
                 "mendelian_errors INT, " +
                 "missing_genotypes INT, " +
                 "homozygotesNumber INT, " +
                 "PRIMARY KEY (name));";
 
         String variantTable = "CREATE TABLE IF NOT EXISTS variant (" +
                 "id_variant INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "chromosome TEXT, " +
                 "position INT64, " +
                 "id TEXT, " +
                 "ref TEXT, " +
                 "alt TEXT, " +
                 "qual DOUBLE, " +
                 "filter TEXT, " +
                 "info TEXT, " +
                 "format TEXT);";
 
         String sampleTable = "CREATE TABLE IF NOT EXISTS sample(" +
                 "name TEXT PRIMARY KEY);";
 
         String sampleInfoTable = "CREATE TABLE IF NOT EXISTS sample_info(" +
                 "id_sample_info INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "id_variant INTEGER, " +
                 "sample_name TEXT, " +
                 "allele_1 INTEGER, " +
                 "allele_2 INTEGER, " +
                 "data TEXT, " +
                 "FOREIGN KEY(id_variant) REFERENCES variant(id_variant)," +
                 "FOREIGN KEY(sample_name) REFERENCES sample(name));";
         String variantInfoTable = "CREATE TABLE IF NOT EXISTS variant_info(" +
                 "id_variant_info INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "id_variant INTEGER, " +
                 "key TEXT, " +
                 "value TEXT, " +
                 "FOREIGN KEY(id_variant) REFERENCES variant(id_variant));";
 
         String variantEffectTable = "CREATE TABLE IF NOT EXISTS variant_effect(" +
                 "id_variant_effect INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "chromosome	TEXT, " +
                 "position INT64, " +
                 "reference_allele TEXT, " +
                 "alternative_allele TEXT, " +
                 "feature_id TEXT, " +
                 "feature_name TEXT, " +
                 "feature_type TEXT, " +
                 "feature_biotype TEXT, " +
                 "feature_chromosome TEXT, " +
                 "feature_start INT64, " +
                 "feature_end INT64, " +
                 "feature_strand TEXT, " +
                 "snp_id TEXT, " +
                 "ancestral TEXT, " +
                 "alternative TEXT, " +
                 "gene_id TEXT, " +
                 "transcript_id TEXT, " +
                 "gene_name TEXT, " +
                 "consequence_type TEXT, " +
                 "consequence_type_obo TEXT, " +
                 "consequence_type_desc TEXT, " +
                 "consequence_type_type TEXT, " +
                 "aa_position INT64, " +
                 "aminoacid_change TEXT, " +
                 "codon_change TEXT); ";
 
         try {
             stmt = con.createStatement();
 
             stmt.execute(globalStatsTable);
             stmt.execute(variant_stats);
             stmt.execute(sample_stats);
             stmt.execute(variantTable);
             stmt.execute(variantInfoTable);
             stmt.execute(sampleTable);
             stmt.execute(sampleInfoTable);
             stmt.execute(variantEffectTable);
 
             stmt.close();
 
             con.commit();
         } catch (SQLException e) {
             System.err.println("PRE: " + e.getClass().getName() + ": " + e.getMessage());
             return false;
         }
 
         return true;
     }
 
     @Override
     public boolean post() {
 
         try {
 
             stmt = con.createStatement();
             stmt.execute("CREATE INDEX variant_stats_chromosome_position_idx ON variant_stats (chromosome, position);");
             stmt.execute("CREATE INDEX variant_chromosome_position_idx ON variant (chromosome, position);");
             stmt.execute("CREATE INDEX variant_effect_chromosome_position_idx ON variant_effect (chromosome, position);");
             stmt.execute("CREATE INDEX variant_pass_idx ON variant (filter);");
             stmt.execute("CREATE INDEX variant_id_idx ON variant (id);");
             stmt.execute("CREATE INDEX sample_name_idx ON sample (name);");
             stmt.execute("CREATE INDEX sample_info_id_variant_idx ON sample_info (id_variant);");
             stmt.execute("CREATE INDEX variant_info_id_variant_key_idx ON variant_info (id_variant, key);");
             stmt.close();
             con.commit();
 
         } catch (SQLException e) {
             System.err.println("POST: " + e.getClass().getName() + ": " + e.getMessage());
             return false;
 
         }
 

        String jsonName = FilenameUtils.getFullPath(dbName) + FilenameUtils.getBaseName(dbName) + ".json";
        System.out.println("jsonName = " + jsonName);
         ObjectMapper jsonObjectMapper = new ObjectMapper();
         ObjectWriter jsonObjectWriter = jsonObjectMapper.writerWithDefaultPrettyPrinter();
 
         try {
             String res = jsonObjectWriter.writeValueAsString(vi);
             PrintWriter out = new PrintWriter(jsonName);
//            System.out.println(res);
             out.println(res);
             out.close();
         } catch (JsonProcessingException | FileNotFoundException e) {
             e.printStackTrace();
         }
 
         return true;
     }
 
     @Override
     public boolean writeVariantStats(List<VcfVariantStat> data) {
 
         String sql = "INSERT INTO variant_stats (chromosome, position, allele_ref, allele_alt, id, maf, mgf, allele_maf, genotype_maf, miss_allele, miss_gt, mendel_err, is_indel, cases_percent_dominant, controls_percent_dominant, cases_percent_recessive, controls_percent_recessive) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
         boolean res = true;
 
         try {
             pstmt = con.prepareStatement(sql);
 
             for (VcfVariantStat v : data) {
                 pstmt.setString(1, v.getChromosome());
                 pstmt.setLong(2, v.getPosition());
                 pstmt.setString(3, v.getRefAlleles());
                 pstmt.setString(4, StringUtils.join(v.getAltAlleles(), ","));
                 pstmt.setString(5, v.getId());
                 pstmt.setDouble(6, v.getMaf());
                 pstmt.setDouble(7, v.getMgf());
                 pstmt.setString(8, v.getMafAllele());
                 pstmt.setString(9, v.getMgfAllele());
                 pstmt.setInt(10, v.getMissingAlleles());
                 pstmt.setInt(11, v.getMissingGenotypes());
                 pstmt.setInt(12, v.getMendelinanErrors());
                 pstmt.setInt(13, (v.getIndel() ? 1 : 0));
                 pstmt.setDouble(14, v.getCasesPercentDominant());
                 pstmt.setDouble(15, v.getControlsPercentDominant());
                 pstmt.setDouble(16, v.getCasesPercentRecessive());
                 pstmt.setDouble(17, v.getControlsPercentRecessive());
 
                 pstmt.execute();
 
             }
             con.commit();
             pstmt.close();
         } catch (SQLException e) {
             System.err.println("VARIANT_STATS: " + e.getClass().getName() + ": " + e.getMessage());
             res = false;
         }
 
 
         return res;
     }
 
     @Override
     public boolean writeGlobalStats(VcfGlobalStat globalStats) {
         boolean res = true;
         float titv = 0;
         float pass = 0;
         float avg = 0;
         try {
             String sql;
             stmt = con.createStatement();
 
             sql = "INSERT INTO global_stats VALUES ('NUM_VARIANTS', 'Number of variants'," + globalStats.getVariantsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_variants", globalStats.getVariantsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_SAMPLES', 'Number of samples'," + globalStats.getSamplesCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_samples", globalStats.getSamplesCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_BIALLELIC', 'Number of biallelic variants'," + globalStats.getBiallelicsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_biallelic", globalStats.getBiallelicsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_MULTIALLELIC', 'Number of multiallelic variants'," + globalStats.getMultiallelicsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_multiallelic", globalStats.getMultiallelicsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_SNPS', 'Number of SNP'," + globalStats.getSnpsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_snps", globalStats.getSnpsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_INDELS', 'Number of indels'," + globalStats.getIndelsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_indels", globalStats.getIndelsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_TRANSITIONS', 'Number of transitions'," + globalStats.getTransitionsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_transitions", globalStats.getTransitionsCount());
             sql = "INSERT INTO global_stats VALUES ('NUM_TRANSVERSSIONS', 'Number of transversions'," + globalStats.getTransversionsCount() + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("num_transversions", globalStats.getTransversionsCount());
             if (globalStats.getTransversionsCount() > 0) {
                 titv = globalStats.getTransitionsCount() / (float) globalStats.getTransversionsCount();
             }
             sql = "INSERT INTO global_stats VALUES ('TITV_RATIO', 'Ti/TV ratio'," + titv + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("titv_ratio", titv);
             if (globalStats.getVariantsCount() > 0) {
                 pass = globalStats.getPassCount() / (float) globalStats.getVariantsCount();
                 avg = globalStats.getAccumQuality() / (float) globalStats.getVariantsCount();
             }
 
             sql = "INSERT INTO global_stats VALUES ('PERCENT_PASS', 'Percentage of PASS'," + (pass * 100) + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("percent_pass", pass * 100);
 
             sql = "INSERT INTO global_stats VALUES ('AVG_QUALITY', 'Average quality'," + avg + ");";
             stmt.executeUpdate(sql);
             vi.addGlobalStats("avg_quality", avg);
 
             con.commit();
             stmt.close();
 
             ;
 
         } catch (SQLException e) {
             System.err.println("GLOBAL_STATS: " + e.getClass().getName() + ": " + e.getMessage());
             res = false;
         }
 
         return res;
     }
 
     @Override
     public boolean writeSampleStats(VcfSampleStat sampleStat) {
         String sql = "INSERT INTO sample_stats VALUES(?,?,?,?);";
         SampleStat s;
         String name;
         boolean res = true;
         try {
             pstmt = con.prepareStatement(sql);
 
             for (Map.Entry<String, SampleStat> entry : sampleStat.getSamplesStats().entrySet()) {
                 s = entry.getValue();
                 name = entry.getKey();
 
                 pstmt.setString(1, name);
                 pstmt.setInt(2, s.getMendelianErrors());
                 pstmt.setInt(3, s.getMissingGenotypes());
                 pstmt.setInt(4, s.getHomozygotesNumber());
 
                 pstmt.execute();
 
             }
             con.commit();
             pstmt.close();
         } catch (SQLException e) {
             System.err.println("SAMPLE_STATS: " + e.getClass().getName() + ": " + e.getMessage());
             res = false;
         }
 
 
         return res;
     }
 
     @Override
     public boolean writeSampleGroupStats(VcfSampleGroupStat sampleGroupStats) {
         return false;
     }
 
     @Override
     public boolean writeVariantGroupStats(VcfVariantGroupStat groupStats) {
         return false;
     }
 
     @Override
     public boolean writeVariantIndex(List<VcfRecord> data) {
         String sql, sqlSampleInfo, sqlInfo;
         PreparedStatement pstmtSample, pstmtInfo;
         String sampleName;
         String sampleData;
         int allele_1, allele_2;
         Genotype g;
         int id;
         boolean res = true;
 
         PreparedStatement pstmt;
         if (!createdSampleTable && data.size() > 0) {
             try {
                 sql = "INSERT INTO sample (name) VALUES(?);";
                 pstmt = con.prepareStatement(sql);
                 VcfRecord v = data.get(0);
                 for (Map.Entry<String, Integer> entry : v.getSampleIndex().entrySet()) {
                     vi.addSample(entry.getKey());
                     pstmt.setString(1, entry.getKey());
                     pstmt.execute();
                 }
 
                 pstmt.close();
                 con.commit();
                 createdSampleTable = true;
             } catch (SQLException e) {
                 System.err.println("SAMPLE: " + e.getClass().getName() + ": " + e.getMessage());
                 res = false;
             }
         }
 
         sql = "INSERT INTO variant (chromosome, position, id, ref, alt, qual, filter, info, format) VALUES(?,?,?,?,?,?,?,?,?);";
         sqlSampleInfo = "INSERT INTO sample_info(id_variant, sample_name, allele_1, allele_2, data) VALUES (?,?,?,?,?);";
         sqlInfo = "INSERT INTO variant_info(id_variant, key, value) VALUES (?,?,?);";
 
         try {
 
             pstmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
             pstmtSample = con.prepareStatement(sqlSampleInfo);
             pstmtInfo = con.prepareStatement(sqlInfo);
 
             for (VcfRecord v : data) {
 
                 pstmt.setString(1, v.getChromosome());
                 pstmt.setInt(2, v.getPosition());
                 pstmt.setString(3, v.getId());
                 pstmt.setString(4, v.getReference());
                 pstmt.setString(5, StringUtils.join(v.getAltAlleles(), ","));
                 pstmt.setDouble(6, (v.getQuality().equals(".") ? 0 : Double.valueOf(v.getQuality())));
                 pstmt.setString(7, v.getFilter());
                 pstmt.setString(8, v.getInfo());
                 pstmt.setString(9, v.getFormat());
 
                 pstmt.execute();
                 ResultSet rs = pstmt.getGeneratedKeys();
                 if (rs.next()) {
                     id = rs.getInt(1);
                     for (Map.Entry<String, Integer> entry : v.getSampleIndex().entrySet()) {
                         sampleName = entry.getKey();
                         sampleData = v.getSamples().get(entry.getValue());
                         g = v.getSampleGenotype(sampleName);
 
                         allele_1 = (g.getAllele1() == null) ? -1 : g.getAllele1();
                         allele_2 = (g.getAllele2() == null) ? -1 : g.getAllele2();
 
                         pstmtSample.setInt(1, id);
                         pstmtSample.setString(2, sampleName);
                         pstmtSample.setInt(3, allele_1);
                         pstmtSample.setInt(4, allele_2);
                         pstmtSample.setString(5, sampleData);
                         pstmtSample.execute();
 
                     }
 
                     if (!v.getInfo().equals(".")) {
                         String[] infoFields = v.getInfo().split(";");
                         for (String elem : infoFields) {
                             String[] fields = elem.split("=");
                             pstmtInfo.setInt(1, id);
                             pstmtInfo.setString(2, fields[0]);
                             pstmtInfo.setString(3, fields[1]);
                             pstmtInfo.execute();
                         }
                     }
                 } else {
                     res = false;
                 }
             }
 
             pstmt.close();
             pstmtSample.close();
             pstmtInfo.close();
             con.commit();
 
         } catch (SQLException e) {
             System.err.println("VARIANT/SAMPLE_INFO: " + e.getClass().getName() + ": " + e.getMessage());
             res = false;
         }
 
         return res;
     }
 
     @Override
     public boolean writeVariantEffect(List<VariantEffect> batchEffect) {
 
         String sql = "INSERT INTO variant_effect(chromosome	, position , reference_allele , alternative_allele , " +
                 "feature_id , feature_name , feature_type , feature_biotype , feature_chromosome , feature_start , " +
                 "feature_end , feature_strand , snp_id , ancestral , alternative , gene_id , transcript_id , gene_name , " +
                 "consequence_type , consequence_type_obo , consequence_type_desc , consequence_type_type , aa_position , " +
                 "aminoacid_change , codon_change) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
 
         boolean res = true;
 
         try {
             pstmt = con.prepareStatement(sql);
 
             for (VariantEffect v : batchEffect) {
                 pstmt.setString(1, v.getChromosome());
                 pstmt.setInt(2, v.getPosition());
                 pstmt.setString(3, v.getReferenceAllele());
                 pstmt.setString(4, v.getAlternativeAllele());
                 pstmt.setString(5, v.getFeatureId());
                 pstmt.setString(6, v.getFeatureName());
                 pstmt.setString(7, v.getFeatureType());
                 pstmt.setString(8, v.getFeatureBiotype());
                 pstmt.setString(9, v.getFeatureChromosome());
                 pstmt.setInt(10, v.getFeatureStart());
                 pstmt.setInt(11, v.getFeatureEnd());
                 pstmt.setString(12, v.getFeatureStrand());
                 pstmt.setString(13, v.getSnpId());
                 pstmt.setString(14, v.getAncestral());
                 pstmt.setString(15, v.getAlternative());
                 pstmt.setString(16, v.getGeneId());
                 pstmt.setString(17, v.getTranscriptId());
                 pstmt.setString(18, v.getGeneName());
                 pstmt.setString(19, v.getConsequenceType());
                 pstmt.setString(20, v.getConsequenceTypeObo());
                 pstmt.setString(21, v.getConsequenceTypeDesc());
                 pstmt.setString(22, v.getConsequenceTypeType());
                 pstmt.setInt(23, v.getAaPosition());
                 pstmt.setString(24, v.getAminoacidChange());
                 pstmt.setString(25, v.getCodonChange());
 
                 pstmt.execute();
 
                 vi.addConsequenceType(v.getConsequenceTypeObo());
                 vi.addBiotype(v.getFeatureBiotype());
 
             }
             con.commit();
             pstmt.close();
         } catch (SQLException e) {
             System.err.println("VARIANT_EFFECT: " + e.getClass().getName() + ": " + e.getMessage());
             res = false;
         }
         return res;
     }
 }
