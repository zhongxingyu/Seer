 package ee.ut.math.tvt.salessystem.domain.data;
 
 
 import java.sql.Time;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "SALE")
 public class OrderHeader implements Cloneable, DisplayableItem {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
 	@Column(name = "DATE")
 	private Date date;
 
 	@Column(name = "TIME")
 	private Time time;
     
 	@Column(name = "SUMMA")
     private double sum;
     
 	@OneToMany(mappedBy = "orderHeader")
 	private Set<SoldItem> solditems;
 
 	public Set<SoldItem> getOrderDetail() {
		return solditems;
 	}
 
 	public void setOrderDetail(Set<SoldItem> solditems) {
 		this.solditems = solditems;
 	}
 
 	public OrderHeader(Long id, Date date, Time time, double sum,
 			Set<SoldItem> solditems) {
         this.id = id;
         this.date = date;
         this.time = time;
         this.sum = sum;
 		this.solditems = solditems;
     }
     
     /**
      * Constructs new  <code>OrderHeader</code>.
      */
     public OrderHeader() {
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }    
     
 	public Date getDate() {
         return date;
     }
 
 	public void setDate(Date date) {
         this.date = date;
     }
 
 	public Time getTime() {
         return time;
     }
 
 	public void setTime(Time time) {
         this.time = time;
     }
 
     public double getSum() {
        return sum;
     }
 
    public void setSum(double sum) {
        this.sum = sum;
     }
 
     
     public String toString() {
 		return id + " " + new SimpleDateFormat("dd.MM.yyyy").format(date) + " "
 				+ time + " " + sum;
     }
 
     /**
      * Method for querying the value of a certain column when HeaderOrder are shown
      * as table rows in the SalesSystemTableModel. The order of the columns is:
      * id, date, time, sum.
      */
     public Object getColumn(int columnIndex) {
         switch(columnIndex) {
             case 0: return id;
 		case 1:
 			return new SimpleDateFormat("dd.MM.yyyy").format(date);
             case 2: return time;
             case 3: return new Double(sum);
             default: throw new RuntimeException("invalid column!");
         }
     }    
     
     public Object clone() {
     	OrderHeader item =
             new OrderHeader(getId(), getDate(), getTime(), getSum(), getOrderDetail());
         return item;
     }
 		
 }
