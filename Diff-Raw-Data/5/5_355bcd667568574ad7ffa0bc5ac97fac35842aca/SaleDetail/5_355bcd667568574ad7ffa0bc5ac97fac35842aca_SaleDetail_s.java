 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.model.sale;
 
 import com.structureeng.persistence.model.AbstractTenantModel;
 import com.structureeng.persistence.model.product.ProductPrice;
 
 import com.google.common.base.Preconditions;
 
 import java.math.BigDecimal;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 /**
  * Defines the sale of a {@code RetailProduct}.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  */
 @Entity
 @Table(name = "saleDetail", uniqueConstraints = {
         @UniqueConstraint(name = "saleDetailTenantPK",
                columnNames = {"idTenant", "idSale", "idRetailProduct"})})
 public class SaleDetail extends AbstractTenantModel<Long> {
 
     @Id
     @Column(name = "idSaleDetail")
     private Long id;
 
     @JoinColumn(name = "idSale", referencedColumnName = "idSale", nullable = false)
     @ManyToOne(cascade = CascadeType.REFRESH)
     private Sale sale;
 
    @JoinColumn(name = "idRetailProduct", referencedColumnName = "idRetailProduct",
             nullable = false)
     @ManyToOne(cascade = CascadeType.REFRESH)
     private ProductPrice retailProduct;
 
     @Column(name = "lineOrder", nullable = false)
     private int lineOrder;
 
     @Column(name = "quantity", nullable = false, precision = 13, scale = 4)
     private BigDecimal quantity;
 
     @Column(name = "price", nullable = false, precision = 13, scale = 4)
     private BigDecimal price;
 
     @Column(name = "tax", nullable = false, precision = 13, scale = 4)
     private BigDecimal tax;
 
     @Column(name = "total", nullable = false, precision = 13, scale = 4)
     private BigDecimal total;
 
     @Override
     public Long getId() {
         return id;
     }
 
     @Override
     public void setId(Long id) {
         this.id = Preconditions.checkNotNull(id);
     }
 
     public Sale getSale() {
         return sale;
     }
 
     public void setSale(Sale sale) {
         this.sale = Preconditions.checkNotNull(sale);
     }
 
     public ProductPrice getRetailProduct() {
         return retailProduct;
     }
 
     public void setRetailProduct(ProductPrice retailProduct) {
         this.retailProduct = Preconditions.checkNotNull(retailProduct);
     }
 
     public int getLineOrder() {
         return lineOrder;
     }
 
     public void setLineOrder(int lineOrder) {
         this.lineOrder = Preconditions.checkNotNull(lineOrder);
     }
 
     public BigDecimal getQuantity() {
         return quantity;
     }
 
     public void setQuantity(BigDecimal quantity) {
         this.quantity = Preconditions.checkNotNull(quantity);
     }
 
     public BigDecimal getPrice() {
         return price;
     }
 
     public void setPrice(BigDecimal price) {
         this.price = Preconditions.checkNotNull(price);
     }
 
     public BigDecimal getTax() {
         return tax;
     }
 
     public void setTax(BigDecimal tax) {
         this.tax = Preconditions.checkNotNull(tax);
     }
 
     public BigDecimal getTotal() {
         return total;
     }
 
     public void setTotal(BigDecimal total) {
         this.total = Preconditions.checkNotNull(total);
     }
 }
