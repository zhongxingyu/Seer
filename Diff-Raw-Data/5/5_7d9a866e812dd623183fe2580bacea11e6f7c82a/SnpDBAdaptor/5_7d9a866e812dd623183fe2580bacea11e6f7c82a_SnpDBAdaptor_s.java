 package org.bioinfo.infrared.lib.api;
 
 import java.util.List;
 
 import org.bioinfo.infrared.core.cellbase.ConsequenceType;
 import org.bioinfo.infrared.core.cellbase.Snp;
 import org.bioinfo.infrared.core.cellbase.SnpToTranscript;
 import org.bioinfo.infrared.lib.common.Position;
 import org.bioinfo.infrared.lib.common.Region;
 
 public interface SnpDBAdaptor extends FeatureDBAdaptor {
 
 	
 	@Override
 	public List<Snp> getAll();
 
 	
 	public List<Snp> getAllBySnpId(String snpId);
 
 	public List<List<Snp>> getAllBySnpIdList(List<String> snpIdList);
 
 	public List<Snp> getAllByGeneId(String externalId);
 	
 	public List<List<Snp>> getAllByGeneIdList(List<String> externalIds);
 	
 	public List<Snp> getAllByEnsemblGeneId(String ensemblGeneId);
 	
 	public List<List<Snp>> getAllByEnsemblGeneIdList(List<String> ensemblGeneIds);
 	
 	public List<Snp> getAllByEnsemblTranscriptId(String ensemblTranscriptId);
 	
 	public List<List<Snp>> getAllByEnsemblTranscriptIdList(List<String> ensemblTranscriptIds);
 
 	public List<String> getAllIdsByRegion(String chromosome, int start, int end);
 
 	
 	public List<ConsequenceType> getAllConsequenceTypes();
 	
 	public List<ConsequenceType> getAllConsequenceTypesBySnpId(String snpId);
 	
 	public List<SnpToTranscript> getAllSnpToTranscriptsBySnpId(String snpId);
 	
 	public List<SnpToTranscript> getAllSnpToTranscriptsByTranscriptId(String transcriptId);
	
	public List<List<ConsequenceType>> getAllConsequenceTypesBySnpIdList(List<String> snpId);
 
 	public List<String> getAllIdsBySOConsequenceType(String soConsequenceType);
 	
 	public List<List<String>> getAllIdsBySOConsequenceTypeList(List<String> soConsequenceTypeList);
 	
 	public List<Snp> getAllBySOConsequenceType(String soConsequenceType);
 	
 	public List<Snp> getAllBySOConsequenceTypeList(List<String> soConsequenceTypeList);
 	
 	public List<Snp> getAllByEnsemblConsequenceType(String ensemblConsequenceType);
 	
 	public List<Snp> getAllByEnsemblConsequenceTypeList(List<String> ensemblConsequenceTypeList);
 	
 	
 	public List<Snp> getAllByPosition(String chromosome, int position);
 	
 	public List<Snp> getAllByPosition(Position position);
 	
 	public List<List<Snp>> getAllByPositionList(List<Position> positionList);
 	
 	
 	public List<Snp> getAllByRegion(String chromosome);
 	
 	public List<Snp> getAllByRegion(String chromosome, int start);
 	
 	public List<Snp> getAllByRegion(String chromosome, int start, int end);
 	
 	public List<Snp> getAllByRegion(String chromosome, int start, int end, List<String> consequenceTypeList);
 	
 	public List<Snp> getAllByRegion(Region region);
 	
 	public List<Snp> getAllByRegion(Region region, List<String> consequenceTypeList);
 	
 	public List<List<Snp>> getAllByRegionList(List<Region> regionList);
 	
 	public List<List<Snp>> getAllByRegionList(List<Region> regionList, List<String> consequenceTypeList);
 	
 	public List<Snp> getAllByCytoband(String chromosome, String cytoband);
 	
 	
 	public List<Snp> getAllFilteredByConsequenceType(List<String> snpIds, String consequence);
 
 	public List<Snp> getAllFilteredByConsequenceType(List<String> snpIds, List<String> consequenceTypes);
 
 	public void writeAllFilteredByConsequenceType(String consequence, String outfile);
 	
 }
