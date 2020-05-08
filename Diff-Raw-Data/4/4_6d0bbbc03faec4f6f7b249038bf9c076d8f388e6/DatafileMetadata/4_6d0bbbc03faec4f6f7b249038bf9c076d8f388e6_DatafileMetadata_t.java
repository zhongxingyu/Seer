 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.grabber;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.annotations.GenericGenerator;
 
 
 /**
  * This class represents the administrative metadata for a file captured
  * by the FileGrabber.
  * 
  * @author scrawley
  */
 @Entity
 @Table(name = "DATAFILE_METADATA",
         uniqueConstraints=@UniqueConstraint(columnNames={"capturedFilePathname"}))
 public class DatafileMetadata {
     private String sourceFilePathname;
     private String facilityFilePathname;
     private Date captureTimestamp;
     private Date fileWriteTimestamp;
     private String capturedFilePathname;
     private String mimeType;
     private Long id;
     private long fileSize;    
     
     public DatafileMetadata() {
         super();
     }
     
     public DatafileMetadata(
             String sourceFilePathname, String facilityFilePathname,
            String capturedFilePathname, Date fileWriteTimestamp, 
            Date captureTimestamp, String mimeType, long fileSize) {
         super();
         this.sourceFilePathname = sourceFilePathname;
         this.facilityFilePathname = facilityFilePathname;
         this.capturedFilePathname = capturedFilePathname;
         this.captureTimestamp = captureTimestamp;
         this.fileWriteTimestamp = fileWriteTimestamp;
         this.mimeType = mimeType;
         this.fileSize = fileSize;
     }
     
     @Id
     @GeneratedValue(generator = "increment")
     @GenericGenerator(name = "increment", strategy = "increment")
     @JsonIgnore
     public Long getId() {
         return id;
     }
     
     @Temporal(TemporalType.TIMESTAMP)
     public Date getCaptureTimestamp() {
         return captureTimestamp;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     public Date getFileWriteTimestamp() {
         return fileWriteTimestamp;
     }
 
     public String getSourceFilePathname() {
         return sourceFilePathname;
     }
 
     public String getCapturedFilePathname() {
         return capturedFilePathname;
     }
 
     public void setSourceFilePathname(String sourceFilePathname) {
         this.sourceFilePathname = sourceFilePathname;
     }
 
     public String getFacilityFilePathname() {
         return facilityFilePathname;
     }
 
     public void setFacilityFilePathname(String facilityFilePathname) {
         this.facilityFilePathname = facilityFilePathname;
     }
 
     public void setCaptureTimestamp(Date captureTimestamp) {
         this.captureTimestamp = captureTimestamp;
     }
 
     public void setFileWriteTimestamp(Date fileWriteTimestamp) {
         this.fileWriteTimestamp = fileWriteTimestamp;
     }
 
     public void setCapturedFilePathname(String capturedFilePathname) {
         this.capturedFilePathname = capturedFilePathname;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getMimeType() {
         return mimeType;
     }
 
     public void setMimeType(String mimeType) {
         this.mimeType = mimeType;
     }
 
     public long getFileSize() {
         return fileSize;
     }
 
     public void setFileSize(long fileSize) {
         this.fileSize = fileSize;
     }
 }
