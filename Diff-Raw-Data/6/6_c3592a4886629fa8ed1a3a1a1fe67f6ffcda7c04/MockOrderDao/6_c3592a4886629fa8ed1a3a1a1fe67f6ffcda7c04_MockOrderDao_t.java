 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.module.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import jp.dip.komusubi.lunch.model.Order;
 
 public class MockOrderDao implements OrderDao {
 
 	@Override
 	public Order find(Integer pk) {
 		return null;
 	}
 
 	@Override
 	public List<Order> findAll() {
 		return null;
 	}
 
 	@Override
 	public Integer persist(Order instance) {
 		return null;
 	}
 
 	@Override
 	public void remove(Order instance) {
 
 	}
 
 	@Override
 	public void update(Order instance) {
 
 	}
 
 	@Override
 	public List<Order> findByUserAndDate(Integer userId, Date date) {
 		return null;
 	}
 
 	@Override
 	public List<Order> findByUser(Integer userIde) {
 		return null;
 	}
 
 	@Override
 	public List<Order> findByProduct(String productId) {
 		return null;
 	}
 
 	@Override
 	public List<Order> findByUserAndProductAndDate(Integer userId, String productId, Date date) {
 		return null;
 	}
 
 	@Override
 	public List<Order> findByGroupId(String groupId) {
 		return null;
 	}
 
    @Override
    public List<Order> findByGroupIdAndDate(Integer groupId, Date date, boolean summary) {
        // TODO Auto-generated method stub
        return null;
    }

 }
