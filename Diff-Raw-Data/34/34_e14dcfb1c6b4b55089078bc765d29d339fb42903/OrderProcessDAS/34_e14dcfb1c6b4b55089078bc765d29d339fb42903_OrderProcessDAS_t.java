 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.sapienter.jbilling.server.order.db;
 
import java.util.List;

import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 
 public class OrderProcessDAS extends AbstractDAS<OrderProcessDTO> {
    
    //used to check of the order has any invoices (non deleted not cancelled)
    public List<Integer> findActiveInvoicesForOrder(Integer orderId) {

        String hql = "select pr.invoice.id" +
                     "  from OrderProcessDTO pr " +
                     "  where pr.purchaseOrder.id = :orderId" +
                     "    and pr.invoice.deleted = 0" + 
                     "    and pr.isReview = 0";

        List<Integer> data = getSession()
                        .createQuery(hql)
                        .setParameter("orderId", orderId)
                        .setComment("OrderProcessDAS.findActiveInvoicesForOrder " + orderId)
                        .list();
        return data;
    }
    
 }
