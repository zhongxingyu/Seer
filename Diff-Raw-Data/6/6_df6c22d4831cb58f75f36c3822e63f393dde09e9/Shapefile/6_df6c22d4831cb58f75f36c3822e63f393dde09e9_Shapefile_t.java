 package com.rockontrol.yaogan.model;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 @Entity(name = "shapefiles")
 @NamedQueries({
       @NamedQuery(name = "Shapefile.getAvailableTimesOfPlace", query = "select distinct shootTime from com.rockontrol.yaogan.model.Shapefile"
             + " where placeId = :placeId"),
       @NamedQuery(name = "Shapefile.getShapefilesByPlaceAndTime", query = "from com.rockontrol.yaogan.model.Shapefile"
             + " where placeId = :placeId and shootTime = :time"),
      @NamedQuery(name = "Shapefile.getShapefileByPTC", query = "from com.rockontrol.yaogan.model.Shapefile "
            + "where placeId = :placeId and shootTime = :time and category = :category") })
 public class Shapefile {
 
    public enum Category {
       FILE_REGION_BOUNDARY("kq", "边界"), FILE_LAND_TYPE("tdly", "土地利用"), FILE_LAND_COLLAPSE(
             "dbtx", "地表塌陷"), FILE_LAND_FRACTURE("dlf", "地裂缝"), FILE_LAND_SOIL("trqs",
             "土壤侵蚀"), FILE_HIG_DEF("gqyg", "高清遥感");
 
       /**
        * 名称
        */
       private final String name;
 
       /**
        * 类型 前台页面使用的一个标识
        */
       private final String type;
 
       private Category(String type, String name) {
          this.type = type;
          this.name = name;
       }
 
       public String getType() {
          return this.type;
       }
 
       public String getName() {
          return this.name;
       }
    }
 
    private Long _id;
    private Long _placeId;
    private String _fileName;
    private String _filePath;
    private String _wmsUrl;
    private String _shootTime;
    private Category _category;
    private Place place;
    private Date _uploadTime;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
       return _id;
    }
 
    public void setId(Long id) {
       this._id = id;
    }
 
    @Column(name = "place_id")
    public Long getPlaceId() {
       return _placeId;
    }
 
    public void setPlaceId(Long placeId) {
       this._placeId = placeId;
    }
 
    @Column(length = 512, unique = true, nullable = false)
    public String getFileName() {
       return _fileName;
    }
 
    public void setFileName(String fileName) {
       this._fileName = fileName;
    }
 
    @Column(length = 1024)
    public String getFilePath() {
       return _filePath;
    }
 
    public void setFilePath(String filePath) {
       this._filePath = filePath;
    }
 
    @Column(length = 128)
    public String getWmsUrl() {
       return _wmsUrl;
    }
 
    public void setWmsUrl(String wmsUrl) {
       this._wmsUrl = wmsUrl;
    }
 
    @Column(length = 64)
    public String getShootTime() {
       return _shootTime;
    }
 
    public void setShootTime(String shootTime) {
       this._shootTime = shootTime;
    }
 
    @Enumerated(EnumType.STRING)
    public Category getCategory() {
       return _category;
    }
 
    public void setCategory(Category category) {
       this._category = category;
    }
 
    public void setCategory(String category) {
       if (Category.FILE_LAND_COLLAPSE.equals(category)) {
          this._category = Category.FILE_LAND_COLLAPSE;
       } else if (Category.FILE_LAND_FRACTURE.equals(category)) {
          this._category = Category.FILE_LAND_FRACTURE;
       } else if (Category.FILE_LAND_SOIL.equals(category)) {
          this._category = Category.FILE_LAND_SOIL;
       } else if (Category.FILE_LAND_TYPE.equals(category)) {
          this._category = Category.FILE_LAND_TYPE;
       } else if (Category.FILE_REGION_BOUNDARY.equals(category)) {
          this._category = Category.FILE_REGION_BOUNDARY;
       } else {
          this._category = Category.FILE_HIG_DEF;
       }
    }
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    public Place getPlace() {
       return place;
    }
 
    public void setPlace(Place place) {
       this.place = place;
    }
 
    public void setUploadTime(Date date) {
       this._uploadTime = date;
    }
 
    @Column(name = "upload_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getUploadTime() {
       return this._uploadTime;
    }
 
    // just a placeholder
    public void setTypeString(String str) {
       // nothing to do
    }
 
    public String getTypeString() {
       switch (this._category) {
       case FILE_LAND_COLLAPSE:
          return "地塌陷";
       case FILE_LAND_SOIL:
          return "土壤侵蚀";
       case FILE_LAND_FRACTURE:
          return "地裂缝";
       case FILE_LAND_TYPE:
          return "地类";
       case FILE_REGION_BOUNDARY:
          return "边界";
       case FILE_HIG_DEF:
          return "高清遥感";
       }
       // will never go to here
       return "未知";
    }
 }
