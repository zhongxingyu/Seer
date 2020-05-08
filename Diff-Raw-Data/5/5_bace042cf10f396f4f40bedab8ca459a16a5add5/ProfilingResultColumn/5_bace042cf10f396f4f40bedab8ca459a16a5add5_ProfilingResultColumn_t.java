 package com.neutrino.models.metadata;
 
 import com.neutrino.profiling.MetadataSchema;
 import com.neutrino.profiling.StagingSchema;
 import com.neutrino.models.configuration.ProfilingTemplate;
 import play.data.format.Formats;
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.Map;
 
 @Entity
 @Table(name="ProfilingResultColumn")
 public class ProfilingResultColumn extends Model {
     @Id
     @Column(name="ID")
     public Integer id;
 
     @NotNull
     @Column(name="ProfilingTemplateID")
     public Integer profilingTemplateId;
 
     @Column(name="TableName", length = 64)
     public String tableName;
 
     @Column(name="ColumnName", length = 64)
     public String columnName;
 
     @NotNull
     @Column(name="TotalCount")
     public Integer totalCount;
 
     @NotNull
     @Column(name="DistinctCount")
     public Integer distinctCount;
 
     @NotNull
     @Column(name="NullCount")
     public Integer nullCount;
 
     @NotNull
    @Column(name="PercentagePopulated", precision = 3, scale=2)
     public BigDecimal percentagePopulated;
 
     @NotNull
    @Column(name="PercentageUnique", precision = 3, scale=2)
     public BigDecimal percentageUnique;
 
     @NotNull
     @Column(name="MinimumLength")
     public Integer minimumLength;
 
     @NotNull
     @Column(name="MaximumLength")
     public Integer maximumLength;
 
     @Column(name="MinimumValue", length = 512)
     public String minimumValue;
 
     @Column(name="MaximumValue", length = 512)
     public String maximumValue;
 
     @ManyToOne
     @NotNull
     @JoinColumn(name="DataColumnID")
     public DataColumn dataColumn;
 
     @Column(name="CreationTimestamp")
     @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
     public Date creationTimestamp;
 
     public DataColumn getDataColumn() {
         return dataColumn;
     }
 
     public void setDataColumn(DataColumn dataColumn) {
         this.dataColumn = dataColumn;
     }
 
 
     public static void addResult(MetadataSchema mtd, StagingSchema stg, ProfilingTemplate template, DataColumn col, Map<String, String> results) {
         ProfilingResultColumn res = new ProfilingResultColumn();
         col.getResultsColumns().add(res);
 
         for (String k:results.keySet()) {
             System.out.println("DEBUG PRofiing results: "+k+" -> "+ results.get(k));
         }
         try {
             res.totalCount = Integer.valueOf(results.get("TotalCount"));
         }catch (NumberFormatException e) {
             res.totalCount = 0;
         }
         try {
             res.distinctCount = Integer.valueOf(results.get("DistinctCount"));
         }catch (NumberFormatException e) {
             res.distinctCount = 0;
         }
 
         try {
             res.nullCount = Integer.valueOf(results.get("NullCount"));
         }catch (NumberFormatException e) {
             res.nullCount = 0;
         }
 
         res.percentagePopulated = new BigDecimal(results.get("PercentagePopulated"));
         res.percentageUnique = new BigDecimal(results.get("PercentageUnique"));
 
         try {
             res.minimumLength = Integer.valueOf(results.get("MinimumLength"));
         }catch (NumberFormatException e) {
             res.minimumLength = 0;
         }
 
         try {
             res.maximumLength = Integer.valueOf(results.get("MaximumLength"));
         }catch (NumberFormatException e) {
             res.maximumLength = 0;
         }
 
         res.minimumValue = results.get("MinimumValue");
         res.maximumValue = results.get("MaximumValue");
 
         res.columnName = col.name;
         res.profilingTemplateId = template.id;
         res.tableName = stg.dataSetTableName();
         res.setDataColumn(col);
         res.save(mtd.server().getName());
         col.save(mtd.server().getName());
     }
 }
