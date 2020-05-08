 package org.gwaspi.model;
 
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 @Entity
 @Table(name = "operationMetadata")
 @NamedQueries({
 	@NamedQuery(
 		name = "operationMetadata_fetchById",
 		query = "SELECT om FROM OperationMetadata om WHERE om.id = :id"),
 	@NamedQuery(
 		name = "operationMetadata_fetchByNetCDFName",
 		query = "SELECT om FROM OperationMetadata om WHERE om.matrixCDFName = :netCDFName"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixIdOperationId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.id = :operationId"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixIdParentOperationId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.parentOperationId = :parentOperationId"),
 })
 public class OperationMetadata implements Serializable {
 
 	private int id;
 	private int parentMatrixId;
 	private int parentOperationId;
 	private String opName;
 	private String netCDFName;
 	private String description;
 	private String pathToMatrix;
 	private OPType gtCode;
 	private int opSetSize;
 	private int implicitSetSize;
 	private int studyId;
 	private Date creationDate;
 
 	protected OperationMetadata() {
 
 		this.id = Integer.MIN_VALUE;
 		this.parentMatrixId = Integer.MIN_VALUE;
 		this.parentOperationId = Integer.MIN_VALUE;
 		this.opName = "";
 		this.netCDFName = "";
 		this.description = "";
 		this.pathToMatrix = "";
 		this.gtCode = null;
 		this.opSetSize = Integer.MIN_VALUE;
 		this.implicitSetSize = Integer.MIN_VALUE;
 		this.studyId = Integer.MIN_VALUE;
 		this.creationDate = new Date();
 	}
 
 	public OperationMetadata(
 			int id,
 			int parentMatrixId,
 			int parentOperationId,
 			String opName,
 			String netCDFName,
 			String description,
 			String pathToMatrix,
 			OPType gtCode,
 			int opSetSize,
 			int implicitSetSize,
 			int studyId,
 			Date creationDate
 			)
 	{
 		this.id = id;
 		this.parentMatrixId = parentMatrixId;
 		this.parentOperationId = parentOperationId;
 		this.opName = opName;
 		this.netCDFName = netCDFName;
 		this.description = description;
 		this.pathToMatrix = pathToMatrix;
 		this.gtCode = gtCode;
 		this.opSetSize = opSetSize;
 		this.implicitSetSize = implicitSetSize;
 		this.studyId = studyId;
		this.creationDate = (creationDate == null)
				? null : (Date) creationDate.clone();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final OperationMetadata other = (OperationMetadata) obj;
 		if (this.getOPId() != other.getOPId()) {
 			return false;
 		}
 		if (this.getStudyId() != other.getStudyId()) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 3;
 		hash = 17 * hash + this.getOPId();
 		hash = 17 * hash + this.getStudyId();
 		return hash;
 	}
 
 	@Id
 	@Column(
 		name       = "id",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getOPId() {
 		return id;
 	}
 
 	protected void setOPId(int id) {
 		this.id = id;
 	}
 
 	@Id
 	@Column(
 		name       = "parentMatrixId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParentMatrixId() {
 		return parentMatrixId;
 	}
 
 	protected void setParentMatrixId(int parentMatrixId) {
 		this.parentMatrixId = parentMatrixId;
 	}
 
 	@Id
 	@Column(
 		name       = "studyId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getStudyId() {
 		return studyId;
 	}
 
 	protected void setStudyId(int studyId) {
 		this.studyId = studyId;
 	}
 
 	@Column(
 		name       = "opName",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getOPName() {
 		return opName;
 	}
 
 	protected void setOPName(String opName) {
 		this.opName = opName;
 	}
 
 	@Column(
 		name       = "netCDFName",
 		length     = 255,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMatrixCDFName() {
 		return netCDFName;
 	}
 
 	protected void setMatrixCDFName(String netCDFName) {
 		this.netCDFName = netCDFName;
 	}
 
 	@Column(
 		name       = "gtCode",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public OPType getGenotypeCode() {
 		return gtCode;
 	}
 
 	protected void setGenotypeCode(OPType gtCode) {
 		this.gtCode = gtCode;
 	}
 
 	@Column(
 		name       = "opSetSize",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getOpSetSize() {
 		return opSetSize;
 	}
 
 	protected void setOpSetSize(int opSetSize) {
 		this.opSetSize = opSetSize;
 	}
 
 	@Column(
 		name       = "pathToMatrix",
 		length     = 1023,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getPathToMatrix() {
 		return pathToMatrix;
 	}
 
 	protected void setPathToMatrix(String pathToMatrix) {
 		this.pathToMatrix = pathToMatrix;
 	}
 
 	@Column(
 		name       = "description",
 		length     = 255,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getDescription() {
 		return description;
 	}
 
 	protected void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Id
 	@Column(
 		name       = "parentOperationId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParentOperationId() {
 		return parentOperationId;
 	}
 
 	protected void setParentOperationId(int parentOperationId) {
 		this.parentOperationId = parentOperationId;
 	}
 
 	@Column(
 		name       = "implicitSetSize",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getImplicitSetSize() {
 		return implicitSetSize;
 	}
 
 	protected void setImplicitSetSize(int implicitSetSize) {
 		this.implicitSetSize = implicitSetSize;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(
 		name       = "creationDate",
 		unique     = false,
		nullable   = true,
 		insertable = true,
 		updatable  = false
 		)
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	protected void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 }
