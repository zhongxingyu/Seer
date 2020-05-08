 /**
  * 
  */
 package de.enwida.web.db.model;
 
 import java.io.File;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import de.enwida.web.model.User;
 import de.enwida.web.utils.Constants;
 
 /**
  * This entity maintains details about files uploaded
  * 
  * @author Jitin
  * 
  */
 @Entity
 @Table(name = Constants.UPLOADED_FILE_TABLE_NAME, schema = Constants.UPLOADED_FILE_TABLE_SCHEMA_NAME)
 @SequenceGenerator(name = "FILE_SEQ", initialValue = 1, allocationSize = 1, sequenceName = Constants.UPLOADED_FILE_SEQUENCE_NAME, schema = Constants.UPLOADED_FILE_SEQUENCE_SCHEMA_NAME)
 public class UploadedFile implements Serializable, Comparable<UploadedFile> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2646705236947755657L;
 	public static final String ID = "ID";
 	public static final String PREVIOUS_FILE_ID = "PREVIOUS_FILE_ID";
 	public static final String DISPLAY_FILE_NAME = "DISPLAY_FILE_NAME";
 	public static final String FILE_NAME = "FILE_NAME";
 	public static final String UPLOAD_DATE = "UPLOAD_DATE";
 	public static final String MODIFICATION_DATE = "MODIFICATION_DATE";
 	public static final String FORMAT = "FORMAT";
 	public static final String FILE_PATH = "FILE_PATH";
 	public static final String REVISION = "REVISION";
 	public static final SimpleDateFormat formatter = new SimpleDateFormat(
 			Constants.DISPLAY_DATE_FORMAT);
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_SEQ")
 	@Column(name = ID)
 	private long id;
 
 	@Column(name = DISPLAY_FILE_NAME, unique = false, nullable = false, length = 255)
 	private String displayFileName;
 
 	@Column(name = FILE_NAME, unique = true, nullable = false, length = 255)
 	private String fileName;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = User.USER_ID)
 	private User uploader;
 
 	@OneToOne(mappedBy = "file")
 	private UserLinesMetaData metaData;
 
 	@Column(name = UPLOAD_DATE, nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date uploadDate;
 
 	@Column(name = MODIFICATION_DATE)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date modificationDate;
 
 	@Column(name = FORMAT, length = 10)
 	private String format;
 
 	@Column(name = FILE_PATH, length = 256)
 	private String filePath;
 
 	@Column(name = REVISION)
 	private int revision;
 
 	@OneToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = PREVIOUS_FILE_ID)
 	private UploadedFile previousFile;
 
 	@Transient
 	private File actualFile;
 
 	@Transient
 	private File manifestFile;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getDisplayFileName() {
 		return displayFileName;
 	}
 
 	public void setDisplayFileName(String displayFileName) {
 		this.displayFileName = displayFileName;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	@Transient
 	public User getUploader() {
 		return uploader;
 	}
 
 	public void setUploader(User uploader) {
 		this.uploader = uploader;
 	}
 
 	public UserLinesMetaData getMetaData() {
 		return metaData;
 	}
 
 	public void setMetaData(UserLinesMetaData metaData) {
 		this.metaData = metaData;
 	}
 
 	public String getDisplayUploadDate() {
 		return formatter.format(uploadDate);
 	}
 	public Date getUploadDate() {
 		return uploadDate;
 	}
 
 	public void setUploadDate(Date uploadDate) {
 		this.uploadDate = uploadDate;
 	}
 
 	public Date getModificationDate() {
 		return modificationDate;
 	}
 
 	public void setModificationDate(Date modificationDate) {
 		this.modificationDate = modificationDate;
 	}
 
 	public String getFormat() {
 		return format;
 	}
 
 	public void setFormat(String format) {
 		this.format = format;
 	}
 
 	public String getFilePath() {
 		return filePath;
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 	public int getRevision() {
 		return revision;
 	}
 
 	public void setRevision(int revision) {
 		this.revision = revision;
 	}
 
 	public UploadedFile getPreviousFile() {
 		return previousFile;
 	}
 
 	public void setPreviousFile(UploadedFile previousFile) {
 		this.previousFile = previousFile;
 	}
 
 	public File getActualFile() {
 		if (actualFile == null) {
 			actualFile = new File(this.filePath);
 		}
 		return actualFile;
 	}
 
 	public File getManifestFile() {
		if (actualFile == null) {
			actualFile = new File(this.filePath + ".mfst");
 		}
		return actualFile;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result
 				+ ((uploadDate == null) ? 0 : uploadDate.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		UploadedFile other = (UploadedFile) obj;
 		if (id != other.id)
 			return false;
 		if (uploadDate == null) {
 			if (other.uploadDate != null)
 				return false;
 		} else if (!uploadDate.equals(other.uploadDate))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "UploadedFile [id="
 				+ id
 				+ ", "
 				+ (displayFileName != null ? "displayFileName="
 						+ displayFileName + ", " : "")
 				+ (fileName != null ? "fileName=" + fileName + ", " : "")
 				+ (uploader != null ? "uploader=" + uploader.getUserName()
 						+ ", " : "")
 				+ (uploadDate != null ? "uploadDate=" + uploadDate + ", " : "")
 				+ (modificationDate != null ? "modificationDate="
 						+ modificationDate + ", " : "")
 				+ (format != null ? "format=" + format + ", " : "")
 				+ (filePath != null ? "filePath=" + filePath + ", " : "")
 				+ "revision=" + revision + "]";
 	}
 
 	@Override
 	public int compareTo(UploadedFile o) {
 		// Sort based on upload date descending order
 		return o.getUploadDate().compareTo(this.uploadDate);
 	}
 }
