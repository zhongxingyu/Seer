 /*
  * Copyright (C) 2013 Universitat Pompeu Fabra
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gwaspi.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.IdClass;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
 import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
 
 @Entity
 @Table(name = "matrixMetadata")
 @IdClass(MatrixKey.class)
 @NamedQueries({
 	@NamedQuery(
 		name = "matrixMetadata_fetchById",
 		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.matrixId = :id"),
 	@NamedQuery(
 		name = "matrixMetadata_fetchByNetCDFName",
 		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.matrixNetCDFName = :netCDFName"),
 	@NamedQuery(
 		name = "matrixMetadata_listKeys",
 		query = "SELECT mm.studyId, mm.matrixId FROM MatrixMetadata mm"),
 	@NamedQuery(
 		name = "matrixMetadata_listIdsByStudyId",
 		query = "SELECT mm.matrixId FROM MatrixMetadata mm WHERE mm.studyId = :studyId"),
 	@NamedQuery(
 		name = "matrixMetadata_listIds",
 		query = "SELECT mm.matrixId FROM MatrixMetadata mm ORDER BY mm.matrixId"),
 	@NamedQuery(
 		name = "matrixMetadata_listByStudyId",
 		query = "SELECT mm FROM MatrixMetadata mm WHERE mm.studyId = :studyId"),
 })
 public class MatrixMetadata implements Serializable {
 
 	private int matrixId;
 	private String matrixFriendlyName;
 	private String matrixNetCDFName;
 	private String pathToMatrix;
 	private ImportFormat technology;
 	private String gwaspiDBVersion;
 	private String description;
 	private GenotypeEncoding gtEncoding;
 	private StrandType strand;
 	private boolean hasDictionray;
 	private int markerSetSize;
 	private int sampleSetSize;
 	private int studyId;
 	private String matrixType; // matrix_type VARCHAR(32) NOT NULL
 	private int parent1MatrixId;
 	private int parent2MatrixId;
 	private String inputLocation;
 	private Date creationDate;
 
 	protected MatrixMetadata() {
 
 		this.matrixId = Integer.MIN_VALUE;
 		this.matrixFriendlyName = "";
 		this.matrixNetCDFName = "";
 		this.pathToMatrix = "";
 		this.technology = ImportFormat.UNKNOWN;
 		this.gwaspiDBVersion = "";
 		this.description = "";
 		this.gtEncoding = null;
 		this.strand = null;
 		this.hasDictionray = false;
 		this.markerSetSize = Integer.MIN_VALUE;
 		this.sampleSetSize = Integer.MIN_VALUE;
 		this.studyId = Integer.MIN_VALUE;
 		this.matrixType = "";
 		this.parent1MatrixId = -1;
 		this.parent2MatrixId = -1;
 		this.inputLocation = "";
 		this.creationDate = new Date();
 	}
 
 	public MatrixMetadata(
 			String matrixFriendlyName,
 			String matrixNetCDFName,
 			String description,
 			GenotypeEncoding gtEncoding,
 			int studyId,
 			int parent1MatrixId,
 			int parent2MatrixId,
 			String inputLocation
 			)
 	{
 		this.matrixId = Integer.MIN_VALUE;
 		this.matrixFriendlyName = matrixFriendlyName;
 		this.matrixNetCDFName = matrixNetCDFName;
 		this.pathToMatrix = "";
 		this.technology = ImportFormat.UNKNOWN;
 		this.gwaspiDBVersion = "";
 		this.description = description;
 		this.gtEncoding = gtEncoding;
 		this.strand = null;
 		this.hasDictionray = false;
 		this.markerSetSize = Integer.MIN_VALUE;
 		this.sampleSetSize = Integer.MIN_VALUE;
 		this.studyId = studyId;
 		this.matrixType = "";
 		this.parent1MatrixId = parent1MatrixId;
 		this.parent2MatrixId = parent2MatrixId;
 		this.inputLocation = inputLocation;
 		this.creationDate = new Date();
 	}
 
 	public MatrixMetadata(
 			int matrixId,
 			String matrixFriendlyName,
 			String matrixNetCDFName,
 			String pathToMatrix,
 			ImportFormat technology,
 			String gwaspiDBVersion,
 			String description,
 			GenotypeEncoding gtEncoding,
 			StrandType strand,
 			boolean hasDictionray,
 			int markerSetSize,
 			int sampleSetSize,
 			int studyId,
 			String matrixType,
 			Date creationDate)
 	{
 		this.matrixId = matrixId;
 		this.matrixFriendlyName = matrixFriendlyName;
 		this.matrixNetCDFName = matrixNetCDFName;
 		this.pathToMatrix = pathToMatrix;
 		this.technology = technology;
 		this.gwaspiDBVersion = gwaspiDBVersion;
 		this.description = description;
 		this.gtEncoding = gtEncoding;
 		this.strand = strand;
 		this.hasDictionray = hasDictionray;
 		this.markerSetSize = markerSetSize;
 		this.sampleSetSize = sampleSetSize;
 		this.studyId = studyId;
 		this.matrixType = matrixType;
 		this.parent1MatrixId = -1;
 		this.parent2MatrixId = -1;
 		this.inputLocation = "";
		this.creationDate = (creationDate == null)
				? null
				: (Date) creationDate.clone();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final MatrixMetadata other = (MatrixMetadata) obj;
 		if (this.getMatrixId() != other.getMatrixId()) {
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
 		hash = 17 * hash + this.getMatrixId();
 		hash = 17 * hash + this.getStudyId();
 		return hash;
 	}
 
 	@Transient
 	public boolean getHasDictionray() {
 		return hasDictionray;
 	}
 
 	public void setHasDictionray(boolean hasDictionray) {
 		this.hasDictionray = hasDictionray;
 	}
 
 	@Id
 	@SequenceGenerator(name = "seqMatrixId")
 	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqMatrixId")
 	@Column(
 		name       = "matrixId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getMatrixId() {
 		return matrixId;
 	}
 
 	protected void setMatrixId(int matrixId) {
 		this.matrixId = matrixId;
 	}
 
 	@Transient
 	public MatrixKey getKey() {
 		return new MatrixKey(getStudyId(), getMatrixId());
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
 		name       = "matrixFriendlyName",
 		length     = 255,
 		unique     = true,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMatrixFriendlyName() {
 		return matrixFriendlyName;
 	}
 
 	protected void setMatrixFriendlyName(String matrixFriendlyName) {
 		this.matrixFriendlyName = matrixFriendlyName;
 	}
 
 	@Transient
 	public ImportFormat getTechnology() {
 		return technology;
 	}
 
 	public void setTechnology(ImportFormat technology) {
 		this.technology = technology;
 	}
 
 	@Transient
 	public String getGwaspiDBVersion() {
 		return gwaspiDBVersion;
 	}
 
 	public void setGwaspiDBVersion(String gwaspiDBVersion) {
 		this.gwaspiDBVersion = gwaspiDBVersion;
 	}
 
 	@Transient
 	public GenotypeEncoding getGenotypeEncoding() {
 		return gtEncoding;
 	}
 
 	public void setGenotypeEncoding(GenotypeEncoding gtEncoding) {
 		this.gtEncoding = gtEncoding;
 	}
 
 	@Transient
 	public int getMarkerSetSize() {
 		return markerSetSize;
 	}
 
 	public void setMarkerSetSize(int markerSetSize) {
 		this.markerSetSize = markerSetSize;
 	}
 
 	@Column(
 		name       = "sampleSetSize",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	@Transient
 	public int getSampleSetSize() {
 		return sampleSetSize;
 	}
 
 	public void setSampleSetSize(int sampleSetSize) {
 		this.sampleSetSize = sampleSetSize;
 	}
 
 	@Transient
 	public String getPathToMatrix() {
 		return pathToMatrix;
 	}
 
 	public void setPathToMatrix(String pathToMatrix) {
 		this.pathToMatrix = pathToMatrix;
 	}
 
 	@Transient
 	public StrandType getStrand() {
 		return strand;
 	}
 
 	public void setStrand(StrandType strand) {
 		this.strand = strand;
 	}
 
 	@Column(
 		name       = "description",
 		length     = 1023,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Column(
 		name       = "matrixNetCDFName",
 		length     = 255,
 		unique     = true,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMatrixNetCDFName() {
 		return matrixNetCDFName;
 	}
 
 	protected void setMatrixNetCDFName(String matrixNetCDFName) {
 		this.matrixNetCDFName = matrixNetCDFName;
 	}
 
 	@Column(
 		name       = "matrixType",
 		length     = 31,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMatrixType() {
 		return matrixType;
 	}
 
 	protected void setMatrixType(String matrixType) {
 		this.matrixType = matrixType;
 	}
 
 	@Column(
 		name       = "parent1MatrixId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParent1MatrixId() {
 		return parent1MatrixId;
 	}
 
 	protected void setParent1MatrixId(int parent1MatrixId) {
 		this.parent1MatrixId = parent1MatrixId;
 	}
 
 	@Column(
 		name       = "parent2MatrixId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParent2MatrixId() {
 		return parent2MatrixId;
 	}
 
 	protected void setParent2MatrixId(int parent2MatrixId) {
 		this.parent2MatrixId = parent2MatrixId;
 	}
 
 	@Column(
 		name       = "inputLocation",
 		length     = 1023,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getInputLocation() {
 		return inputLocation;
 	}
 
 	protected void setInputLocation(String inputLocation) {
 		this.inputLocation = inputLocation;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(
 		name       = "creationDate",
 		unique     = false,
 		nullable   = false,
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
