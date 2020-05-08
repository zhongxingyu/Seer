 package org.ocha.hdx.persistence.entity.ckan;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.ForeignKey;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 
 @Entity
 @Table(name = "hdx_dataserie_to_curated_dataset", uniqueConstraints = @UniqueConstraint(columnNames = { "source_id", "indicator_type_id" }))
@SequenceGenerator(name = "hdx_dataserie_to_curated_dataset_seq", sequenceName = "hdx_dataserie_to_curated_dataset_seq")
 public class DataSerieToCuratedDataset {
 
 	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "hdx_dataserie_to_curated_dataset_seq")
 	@Column(name = "id", nullable = false)
 	private long id;
 
 	@ManyToOne
 	@ForeignKey(name = "fk__dataserie_to_curated_data_to_source")
 	@JoinColumn(name = "source_id", nullable = false)
 	private Source source;
 
 	@ManyToOne
 	@ForeignKey(name = "fk__dataserie_to_curated_data_to_indicator_type")
 	@JoinColumn(name = "indicator_type_id", nullable = false)
 	private IndicatorType indicatorType;
 
 	/**
 	 * The name of the dataset(kind cof id) in CKAN can be arbitrary to get pretty urls, not convention based Needed to communicate changes to CKAN
 	 * 
 	 * ascii chars only
 	 */
 	@Column(name = "ckan_dataset_name", nullable = true, updatable = true)
 	private String ckanDatasetName;
 
 	@Column(name = "ckan_dataset_id", nullable = true, updatable = true)
 	private String ckanDatasetId;
 
 	@Column(name = "last_metadata_update", nullable = true, updatable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date lastMetadataUpdate;
 
 	@Column(name = "last_metadata_push", nullable = true, updatable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date lastMetadataPush;
 
 	@Column(name = "last_data_update", nullable = true, updatable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date lastDataUpdate;
 
 	@Column(name = "last_data_push", nullable = true, updatable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date lastDataPush;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 	}
 
 	public Source getSource() {
 		return source;
 	}
 
 	public void setSource(final Source source) {
 		this.source = source;
 	}
 
 	public IndicatorType getIndicatorType() {
 		return indicatorType;
 	}
 
 	public void setIndicatorType(final IndicatorType indicatorType) {
 		this.indicatorType = indicatorType;
 	}
 
 	public String getCkanDatasetName() {
 		return ckanDatasetName;
 	}
 
 	public void setCkanDatasetName(final String ckanDatasetName) {
 		this.ckanDatasetName = ckanDatasetName;
 	}
 
 	public String getCkanDatasetId() {
 		return ckanDatasetId;
 	}
 
 	public void setCkanDatasetId(final String ckanDatasetId) {
 		this.ckanDatasetId = ckanDatasetId;
 	}
 
 	public Date getLastMetadataUpdate() {
 		return lastMetadataUpdate;
 	}
 
 	public void setLastMetadataUpdate(final Date lastMetadataUpdate) {
 		this.lastMetadataUpdate = lastMetadataUpdate;
 	}
 
 	public Date getLastMetadataPush() {
 		return lastMetadataPush;
 	}
 
 	public void setLastMetadataPush(final Date lastMetadataPush) {
 		this.lastMetadataPush = lastMetadataPush;
 	}
 
 	public Date getLastDataUpdate() {
 		return lastDataUpdate;
 	}
 
 	public void setLastDataUpdate(final Date lastDataUpdate) {
 		this.lastDataUpdate = lastDataUpdate;
 	}
 
 	public Date getLastDataPush() {
 		return lastDataPush;
 	}
 
 	public void setLastDataPush(final Date lastDataPush) {
 		this.lastDataPush = lastDataPush;
 	}
 
 }
