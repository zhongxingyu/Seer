 package biz.thaicom.eBudgeting.repositories;
 
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
 import org.springframework.data.jpa.repository.Modifying;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.PagingAndSortingRepository;
 
 import biz.thaicom.eBudgeting.models.hrx.Organization;
 import biz.thaicom.eBudgeting.models.pln.Objective;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveName;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveType;
 
 public interface ObjectiveRepository extends PagingAndSortingRepository<Objective, Long>, JpaSpecificationExecutor<Objective>{
 	public List<Objective> findByTypeId(Long id);
 
 	public List<Objective> findByParentIdAndFiscalYearAndParent_Name(Long id, Integer fiscalYear, String parentName);
 
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.name = 'ROOT' " +
 			"")
 	public List<Objective> findRootFiscalYear();
 	
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.name='ROOT' and fiscalYear=?1 " +
 			"")
 	public Objective findRootOfFiscalYear(Integer fiscalYear);
 	
 	
 	
 //	@Query("" +  
 //			"SELECT objective, proposal " +
 //			"FROM BudgetProposal proposal " +
 //			"	RIGHT OUTER JOIN proposal.owner owner with owner.id = ?2 " +
 //			"	RIGHT OUTER JOIN proposal.forObjective objective with objective.fiscalYear = ?1 " +
 //			"WHERE " +
 //			"	" +
 //			"	 objective.parent.id = ?3 ")
 	@Query("" +  
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"	LEFT OUTER JOIN objective.proposals proposal with proposal.owner.id = ?2 " +
 			"WHERE objective.parent.id = ?3 and objective.fiscalYear = ?1 " +
 			"ORDER BY objective.index asc ")
 	public List<Objective> findByObjectiveBudgetProposal(Integer fiscalYear, Long onwerId, Long objectiveId);
 
 	
 	
 	@Query("" +  
 			"SELECT distinct objective " +
 			"FROM Objective objective" +
 			"	INNER JOIN FETCH objective.parent parent " +
 			"	INNER JOIN FETCH objective.type type " +
 			"	LEFT OUTER JOIN FETCH objective.budgetTypes budgetTypes " +
 			"	LEFT OUTER JOIN objective.proposals proposal with proposal.owner.id = ?2 " +
 			"WHERE objective.fiscalYear = ?1 AND (objective.parentPath like ?3 OR objective.parentPath is null) " +
 			"ORDER BY objective.index asc ")
 	public List<Objective> findFlatByObjectiveBudgetProposal(
 			Integer fiscalYear, Long ownerId, String parentPathLikeString);
 	
 	
 	@Query("" +  
 			"SELECT distinct objective " +
 			"FROM Objective objective" +
 			"	INNER JOIN FETCH objective.parent parent " +
 			"	INNER JOIN FETCH objective.type type " +
 			"	LEFT OUTER JOIN FETCH objective.budgetTypes budgetTypes " +
 			"	LEFT OUTER JOIN objective.objectiveProposals proposal with proposal.owner.id = ?2 " +
 			"WHERE objective.fiscalYear = ?1 AND (objective.parentPath like ?3 OR objective.parentPath is null) " +
 			"ORDER BY objective.index asc ")	
 	public List<Objective> findFlatByObjectiveObjectiveBudgetProposal(
 			Integer fiscalYear, Long ownerId, String parentPathLikeString);
 	
 	
 	@Query("" +  
 			"SELECT distinct objective " +
 			"FROM Objective objective" +
 			"	LEFT OUTER JOIN FETCH objective.parent parent " +
 			"	INNER JOIN FETCH objective.type type " +
 			"	LEFT OUTER JOIN FETCH objective.budgetTypes budgetTypes " +
 			"	LEFT OUTER JOIN objective.proposals proposal " +
 			"WHERE objective.fiscalYear = ?1 AND (objective.parentPath like ?2 OR objective.parentPath is null) " +
 			"ORDER BY objective.index asc ")
 	public List<Objective> findFlatByObjectiveBudgetProposal(
 			Integer fiscalYear, String parentPathLikeString);
 	
 
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.parentPath like ?1")
 	public List<Objective> findAllDescendantOf(String parentPathLikeString);
 	
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.id in (?1) " +
 			"ORDER BY objective.parentPath DESC ")
 	public List<Objective> findAllObjectiveByIds(List<Long> ids);
 	
 	
 	@Query("" +  
 			"SELECT objective " +
 			"FROM Objective objective" +
 			"	INNER JOIN FETCH objective.type type " +
 			"	LEFT OUTER JOIN FETCH objective.budgetTypes budgetTypes " +
 			"WHERE objective.parent.id = ?1  " +
 			"ORDER BY objective.id asc ")
 	public List<Objective> findChildrenWithParentAndTypeAndBudgetType(
 			Long id);
 
 	@Modifying
 	@Query("update Objective objective " +
 			"set index = index-1 " +
 			"where index > ?1 and objective.parent = ?2 ")
 	public int reIndex(Integer deleteIndex, Objective parent);
 
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"	INNER JOIN FETCH objective.type type " +
 			"	LEFT OUTER JOIN FETCH objective.parent parent " +
 			"	LEFT OUTER JOIN FETCH objective.units unit " +
 			"WHERE objective.fiscalYear=?1 " +
 			"	AND objective.type.id=?2 " +
			"ORDER BY objective.index asc, objective.id asc ")
 	public List<Objective> findAllByFiscalYearAndType_id(Integer fiscalYear,
 			Long typeId);
 	
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.fiscalYear=?1 " +
 			"	AND objective.type.id=?2 " )
 	public Page<Objective> findPageByFiscalYearAndType_id(Integer fiscalYear,
 			Long typeId, Pageable pageable);
 	
 	@Query("" +
 			"SELECT max(o.code) " +
 			"FROM Objective o " +
 			"WHERE o.type=? AND o.fiscalYear=?2 ")
 	public String findMaxCodeOfTypeAndFiscalYear(ObjectiveType type,
 			Integer fiscalYear);
 	
 	@Query("" +
 			"SELECT max(o.lineNumber) " +
 			"FROM Objective o " +
 			"WHERE o.fiscalYear=?1 ")
 	public Integer findMaxLineNumberFiscalYear(Integer fiscalYear);
 	
 	@Modifying
 	@Query("update Objective objective " +
 			"set lineNumber = lineNumber + ?3  " +
 			"where fiscalYear =?1 AND lineNumber >= ?2 ")
 	public Integer insertFiscalyearLineNumberAt(Integer fiscalYear, Integer lineNumer, Integer amount);
 
 	@Modifying
 	@Query("update Objective objective " +
 			"set lineNumber = lineNumber - ?3  " +
 			"where fiscalYear =?1 AND lineNumber > ?2 ")
 	public Integer removeFiscalyearLineNumberAt(Integer fiscalYear, Integer lineNumer, Integer amount);
 	
 	@Query("" +
 			"SELECT max(o.lineNumber) " +
 			"FROM Objective o " +
 			"WHERE o.parent = ?1  ")
 	public Integer findMaxLineNumberChildrenOf(Objective parent);
 	
 	
 	
 	public List<Objective> findAllByFiscalYearAndParentPathLike(Integer fiscalYear, String parentPath);
 
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective " +
 			"WHERE objective.type in (?1) and objective.parent is null ")
 		public List<Objective> findAvailableChildrenOfObjectiveType(
 			Set<ObjectiveType> childrenSet);
 
 	public Objective findOneByFiscalYearAndName(Integer fiscalYear,
 			String string);
 
 	public List<Objective> findAllByObjectiveName(ObjectiveName on);
 
 	
 	@Query("" +
 			"SELECT objective " +
 			"FROM Objective objective" +
 			"	INNER JOIN FETCH objective.proposals proposal " +
 			"	INNER JOIN FETCH proposal.owner owner " +
 			"	INNER JOIN FETCH proposal.budgetType budgetType " +
 			"WHERE objective.fiscalYear = ?1 AND objective.type.id = ?2 ")
 	public List<Objective> findAllByTypeIdAndFiscalYearInitBudgetProposal(
 			Integer fiscalYear, long typeId);
 
 
 
 
 
 }
