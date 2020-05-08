 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.dip.komusubi.lunch.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import org.komusubi.common.util.Resolver;
 
 /**
  * order line.
  * @author jun.ozeki
  * @since 2011/11/20
  */
 public class OrderLine implements Serializable {
 
 	/**
 	 * order line primary key.
 	 * @author jun.ozeki
 	 * @since 2011/11/23
 	 */
 	public static class OrderLineKey implements Serializable {
 
 		private static final long serialVersionUID = -9158460336546810565L;
 		private int orderId;
 		private int no;
 
 		public OrderLineKey() {
 			this(0, 0);
 		}
 		
 		public OrderLineKey(int orderId, int no) {
 			this.orderId = orderId;
 			this.no = no;
 		}
 		
 		public int getNo() {
 			return no;
 		}
 		
 		public int getOrderId() {
 			return orderId;
 		}
 
 		@Override
         public String toString() {
             StringBuilder builder = new StringBuilder();
             builder.append("OrderLineKey [orderId=").append(orderId).append(", no=").append(no)
                     .append("]");
             return builder.toString();
         }
 
 	}
 	private static final long serialVersionUID = 4918522454881089820L;
 	private Product product;
 	private OrderLineKey primaryKey;
 	private int quantity;
 	private int fixedAmount;
 	private Date datetime;
 	private boolean cancel;
 		
     public OrderLine() {
 		this(new OrderLineKey());
 	}
 
     public OrderLine(OrderLineKey primaryKey) {
 		this.primaryKey = primaryKey;
 		quantity = 1;
 	}
 	
 	public int getAmount() {
 		// FIXME fixed amount は確定後の注文金額なので加算（追加注文）のみ可能。
 		// その場合のProduct の修正不可とする対応が必要。
 //		int amount = fixedAmount;
 		int amount = 0;
 		if (getProduct() != null && isCancel() != true)
 			amount += getProduct().getAmount() * getQuantity();
 		return amount;
 	}
 
 	public Date getDatetime() {
 		return datetime;
 	}
 
 	public OrderLineKey getPrimaryKey() {
 		return primaryKey;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public int increment(int i) {
 		setQuantity(getQuantity() + i);
 		return quantity;
 	}
 
 	public boolean isCancel() {
         return cancel;
     }
 
 	public OrderLine setAmount(int fixedAmount) {
 		this.fixedAmount = fixedAmount;
 		return this;
 	}
 	
 	public OrderLine setCancel(boolean cancel) {
         this.cancel = cancel;
         return this;
     }
 
 	public OrderLine setDatetime(Date datetime) {
 		this.datetime = datetime;
 		return this;
 	}
 
	public OrderLine setOrderLineKey(OrderLineKey primaryKey) {
         this.primaryKey = primaryKey;
        return this;
     }
 
 	public OrderLine setProduct(Product product) {
 		this.product = product;
 		return this;
 	}
 	
 	public OrderLine setQuantity(int quantity) {
 		if (quantity <= 0)
 			throw new IllegalArgumentException("quantity MUST not be under zero: " + quantity);
 		this.quantity = quantity;
 		return this;
 	}
 
 	public ReceiptLine toReceiptLine() {
 	    if (this.product == null) throw new IllegalStateException("product must NOT be null.");
 	    if (this.primaryKey == null) throw new IllegalStateException("OrderLine key is must NOT be null.");
 	    ReceiptLine receiptLine = new ReceiptLine()
 	                                .setAmount(getAmount())
                 	                .setProduct(getProduct())
                 	                // FIXME change date resolver 
                 	                .setDatetime(new Date())
                 	                .setQuantity(getQuantity());
 	    return receiptLine;
 	}
 	
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("OrderLine [product=").append(product).append(", primaryKey=")
                 .append(primaryKey).append(", quantity=").append(quantity).append(", fixedAmount=")
                 .append(fixedAmount).append(", datetime=").append(datetime).append(", cancel=")
                 .append(cancel).append("]");
         return builder.toString();
     }
 }
