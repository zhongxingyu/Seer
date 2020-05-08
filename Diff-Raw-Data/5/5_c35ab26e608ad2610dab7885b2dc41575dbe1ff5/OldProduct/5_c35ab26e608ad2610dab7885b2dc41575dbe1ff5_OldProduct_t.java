 package org.giavacms.catalogue10importer.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.giavacms.base.model.attachment.Document;
 import org.giavacms.base.model.attachment.Image;
 
 @Entity
 @Table(name = "Product_Old")
 public class OldProduct
          implements Serializable
 {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String preview;
    private String description;
    private OldCategory category;
    private String dimensions;
    private String code;
    List<Document> documents;
    List<Image> images;
    private boolean active = true;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId()
    {
       return this.id;
    }
 
    public void setId(Long id)
    {
       this.id = id;
    }
 
    public String getName()
    {
       return this.name;
    }
 
    public void setName(String name)
    {
       this.name = name;
    }
 
    @Lob
    @Column(length = 1024)
    public String getPreview()
    {
       return this.preview;
    }
 
    public void setPreview(String preview)
    {
       this.preview = preview;
    }
 
    @Lob
    @Column(length = 1024)
    public String getDescription()
    {
       return this.description;
    }
 
    public void setDescription(String description)
    {
       this.description = description;
    }
 
    @ManyToOne
    public OldCategory getCategory()
    {
       if (this.category == null)
          this.category = new OldCategory();
       return this.category;
    }
 
    public void setCategory(OldCategory category)
    {
       this.category = category;
    }
 
    @OneToMany(fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
   @JoinTable(name = "Product_Old_Document", joinColumns = { @javax.persistence.JoinColumn(name = "Product_id") }, inverseJoinColumns = { @javax.persistence.JoinColumn(name = "documents_id") })
    public List<Document> getDocuments()
    {
       if (this.documents == null)
          this.documents = new ArrayList<Document>();
       return this.documents;
    }
 
    public void setDocuments(List<Document> documents)
    {
       this.documents = documents;
    }
 
    public void addDocument(Document document)
    {
       getDocuments().add(document);
    }
 
    @Transient
    public int getDocSize()
    {
       return getDocuments().size();
    }
 
    @Transient
    public Image getImage()
    {
       if ((getImages() != null) && (getImages().size() > 0))
          return (Image) getImages().get(0);
       return null;
    }
 
    @OneToMany(fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
   @JoinTable(name = "Product_Old_Image", joinColumns = { @javax.persistence.JoinColumn(name = "Product_id") }, inverseJoinColumns = { @javax.persistence.JoinColumn(name = "images_id") })
    public List<Image> getImages()
    {
       if (this.images == null)
          this.images = new ArrayList<Image>();
       return this.images;
    }
 
    public void setImages(List<Image> images)
    {
       this.images = images;
    }
 
    public void addImage(Image image)
    {
       getImages().add(image);
    }
 
    @Transient
    public int getImgSize()
    {
       return getImages().size();
    }
 
    public String getCode()
    {
       return this.code;
    }
 
    public void setCode(String code)
    {
       this.code = code;
    }
 
    public String getDimensions()
    {
       return this.dimensions;
    }
 
    public void setDimensions(String dimensions)
    {
       this.dimensions = dimensions;
    }
 
    public boolean isActive()
    {
       return this.active;
    }
 
    public void setActive(boolean active)
    {
       this.active = active;
    }
 
    public String toString()
    {
       return "Product [id=" + this.id + ", name=" + this.name + ", preview=" + this.preview + ", description="
                + this.description + ", category=" + this.category.getName() + ", dimensions=" + this.dimensions
                + ", code=" + this.code + ", active=" + this.active + "]";
    }
 }
