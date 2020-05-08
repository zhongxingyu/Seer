 package org.bioinfo.infrared.regulatory.dbsql;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import org.bioinfo.db.handler.BeanArrayListHandler;
 import org.bioinfo.infrared.common.dbsql.DBConnector;
 import org.bioinfo.infrared.common.dbsql.DBManager;
 import org.bioinfo.infrared.common.feature.FeatureList;
 import org.bioinfo.infrared.regulatory.JasparTfbs;
import org.bioinfo.infrared.variation.SNP;
 
 public class JasparTfbsDBManager extends DBManager {
 	
 	private static final String SELECT_FIELDS = " j.tf_factor_name, g.stable_id, j.relative_start, j.relative_end, j.chromosome, j.absolute_start, j.absolute_end, j.strand, j.score, j.sequence ";
 	public static final String GET_BY_SNP_ID = "select "+SELECT_FIELDS+" from snp s, snp2jaspar_tfbs s2j, jaspar_tfbs j, gene g where s.name = ? and s.snp_id=s2j.snp_id and s2j.jaspar_tfbs_id=j.jaspar_tfbs_id and j.gene_id=g.gene_id";
 	
 	public JasparTfbsDBManager(DBConnector dBConnector) {
 		super(dBConnector);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<JasparTfbs> getAllByGeneId(String geneId) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureListById("select "+SELECT_FIELDS+" from jaspar_tfbs j, gene g where g.stable_id= ? and j.gene_id=g.gene_id", geneId, new BeanArrayListHandler(JasparTfbs.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<JasparTfbs> getAllBySnpId(String snpId) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureListById(GET_BY_SNP_ID, snpId, new BeanArrayListHandler(JasparTfbs.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<FeatureList<JasparTfbs>> getAllBySnpIds(List<String> snpIds) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getListOfFeatureListByIds(GET_BY_SNP_ID, snpIds, new BeanArrayListHandler(JasparTfbs.class));
 	}
 	@SuppressWarnings("unchecked")
 	public FeatureList<JasparTfbs> getAll() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList("select "+SELECT_FIELDS+" from jaspar_tfbs j, gene g where j.gene_id=g.gene_id", new BeanArrayListHandler(JasparTfbs.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<JasparTfbs> getAllByLocation(String chromosome, int position) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList("select "+SELECT_FIELDS+" from jaspar_tfbs j, gene g where j.gene_id=g.gene_id and j.chromosome= '"+chromosome+"' and "+position+">=j.absolute_start and "+position+"<=j.absolute_end ", new BeanArrayListHandler(JasparTfbs.class));
 	}
	
	@SuppressWarnings("unchecked")
	public List<FeatureList<SNP>> getSnpsByIds(List<String> oregannoIds) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		return getListOfFeatureListByIds("select s.* from jaspar_tfbs j, snp2jaspar_tfbs s2j, snp s where j.tf_factor_name= ? and j.jaspar_tfbs_id=s2j.jaspar_tfbs_id and s2j.snp_id=s.snp_id", oregannoIds, new BeanArrayListHandler(SNP.class));
	}
	
 }
