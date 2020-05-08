 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojos;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author ashutoshsingh
  */
 @Entity
 @Table(name = "items_pharma")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "ItemsPharma.findAll", query = "SELECT i FROM ItemsPharma i"),
     @NamedQuery(name = "Items.findByItemsPharmaNameLike", query = "SELECT i FROM ItemsPharma i WHERE i.description LIKE  :description"),
     @NamedQuery(name = "ItemsPharma.findNextId", query = "SELECT MAX(i.id) FROM ItemsPharma i"),
     @NamedQuery(name = "ItemsPharma.findById", query = "SELECT i FROM ItemsPharma i WHERE i.id = :id"),
     @NamedQuery(name = "ItemsPharma.findByDM", query = "SELECT i FROM ItemsPharma i WHERE i.dM = :dM"),
     @NamedQuery(name = "ItemsPharma.findByMake", query = "SELECT i FROM ItemsPharma i WHERE i.make = :make"),
     @NamedQuery(name = "ItemsPharma.findByBatch", query = "SELECT i FROM ItemsPharma i WHERE i.batch = :batch"),
     @NamedQuery(name = "ItemsPharma.findByExpDate", query = "SELECT i FROM ItemsPharma i WHERE i.expDate = :expDate"),
     @NamedQuery(name = "ItemsPharma.findByDesc", query = "SELECT i FROM ItemsPharma i WHERE i.description = :desc"),
     @NamedQuery(name = "ItemsPharma.findByPack", query = "SELECT i FROM ItemsPharma i WHERE i.pack = :pack"),
     @NamedQuery(name = "ItemsPharma.findByMrp", query = "SELECT i FROM ItemsPharma i WHERE i.mrp = :mrp"),
     @NamedQuery(name = "ItemsPharma.findByRateFraction", query = "SELECT i FROM ItemsPharma i WHERE i.rateFraction = :rateFraction")})
 public class ItemsPharma implements Serializable {
     @Basic(optional = false)
     @Column(name = "stock_strips")
     private int stockStrips;
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "itemPharmaId")
     private List<SaleBillPharmaItem> saleBillPharmaItemList;
     
     @Basic(optional = false)
     @Column(name = "pack")
     private String pack;
     
     @Basic(optional = false)
     @Column(name = "vat")
     private float vat = 5;
     private static final long serialVersionUID = 1L;
     
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
 
     @Basic(optional = false)
     @Column(name = "d_m_")
     private String dM;
 
     @Basic(optional = false)
     @Column(name = "make")
     private String make;
     
     @Basic(optional = false)
     @Column(name = "batch")
     private String batch;
 
     @Temporal(TemporalType.DATE)
     @Basic(optional = false)
     @Column(name = "exp_date")
             private Date expDate;
 
     @Basic(optional = false)
     @Column(name = "description")
     private String description;
     
     @Basic(optional = false)
     @Column(name = "mrp")
         private float mrp;
     
     @Basic(optional = false)
     @Column(name = "rate_fraction")
     private float rateFraction;
 
     
     @Transient
     private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
     
     public ItemsPharma() {
         
     }
 
     public ItemsPharma(Integer id) {
         this.id = id;
     }
 
     public ItemsPharma(Integer id, String dM, String make, String batch, String expDate, String description, String pack, float mrp, float rateFraction, float vat ) throws ParseException {
         this.id = id;
         this.dM = dM;
         this.make = make;
         this.batch = batch;
         this.expDate = dateFormatter.parse(expDate);
         this.description = description;
         this.pack = pack;
         this.mrp = mrp;
         this.rateFraction = rateFraction;
         this.vat = vat;
     }
 
 
         public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
         public String getDM() {
         return dM;
     }
 
     public void setDM(String dM) {
         this.dM = dM;
     }
 
         public String getMake() {
         return make;
     }
 
     public void setMake(String make) {
         this.make = make;
     }
 
         public String getBatch() {
         return batch;
     }
 
     public void setBatch(String batch) {
         this.batch = batch;
     }
 
         public Date getExpDate() {
         return expDate;
     }
         
         public String getExpDateString(){
             return dateFormatter.format(expDate);
         }
 
     public void setExpDate(Date expDate) {
         this.expDate = expDate;
     }
 
 
     public String getDesc() {
         return description;
     }
 
     public void setDesc(String description) {
         this.description = description;
     }
 
         public float getMrp() {
         return mrp;
     }
 
     public void setMrp(float mrp) {
         this.mrp = mrp;
     }
 
         public float getRateFraction() {
         return rateFraction;
     }
 
     public void setRateFraction(float rateFraction) {
         this.rateFraction = rateFraction;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof ItemsPharma)) {
             return false;
         }
         ItemsPharma other = (ItemsPharma) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return description+"-"+batch;
     }
     
 //    public ItemsPharmaProperty getPropertyObj(){
 //        
 //        return new ItemsPharmaProperty(id,  dM, make, batch, dateFormatter.format(expDate), description, pack, mrp,rateFraction,vat);
 //    }
 
         public float getVat() {
         return vat;
     }
 
     public void setVat(float vat) {
         this.vat = vat;
     }
 
         public String getPack() {
            
            try {
                Integer.parseInt(pack);
                return pack + " tab.";
            }catch(Exception e){
                return pack;
            }        
       
     }
 
     public void setPack(String pack) {
         this.pack = pack;
     }
 
     @XmlTransient
     public List<SaleBillPharmaItem> getSaleBillPharmaItemList() {
         return saleBillPharmaItemList;
     }
 
     public void setSaleBillPharmaItemList(List<SaleBillPharmaItem> saleBillPharmaItemList) {
         this.saleBillPharmaItemList = saleBillPharmaItemList;
     }
 
     public int getStockStrips() {
         return stockStrips;
     }
 
     public void setStockStrips(int stockStrips) {
         this.stockStrips = stockStrips;
     }
     
   
 }
