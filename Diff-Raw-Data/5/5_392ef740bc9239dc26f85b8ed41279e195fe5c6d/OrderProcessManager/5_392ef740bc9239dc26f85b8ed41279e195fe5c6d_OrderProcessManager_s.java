 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.tudelft.ewi.st.atlantis.tudelft.v1.services.orderprocessorservice.impl;
 
 import nl.tudelft.ewi.st.atlantis.tudelft.external.v1.types.RemoteQuoteData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.services.GetQSLocationsRequest;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.services.GetQSLocationsResponse;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.services.configurationservice.gen.SharedConfigurationServiceV1Consumer;
 import nl.tudelft.ewi.st.atlantis.v1.services.GetQuotesRequest;
 import nl.tudelft.ewi.st.atlantis.v1.services.GetQuotesResponse;
 import nl.tudelft.ewi.st.atlantis.v1.services.quoteservice.gen.SharedQuoteServiceV1Consumer;
 import nl.tudelft.stocktrader.Holding;
 import nl.tudelft.stocktrader.Order;
 import nl.tudelft.stocktrader.Quote;
 import nl.tudelft.stocktrader.dal.DAOException;
 import nl.tudelft.stocktrader.dal.DAOFactory;
 import nl.tudelft.stocktrader.dal.OrderDAO;
 import nl.tudelft.stocktrader.util.StockTraderUtility;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.math.BigDecimal;
 import java.net.URL;
 import java.util.Calendar;
 
 public class OrderProcessManager {
 	private static final Log logger = LogFactory
 			.getLog(OrderProcessManager.class);
 
 	private OrderDAO orderDAO = null;
 
 	public void processAndCompleteOrder(Order order)
 			throws DAOException {
 
 		if (logger.isDebugEnabled()) {
 			logger
 					.debug("ProcessOrder.processAndCompleteOrder(OrderDataModel) \nOrderID :"
 							+ order.getOrderID()
 							+ "\nOrderType :"
 							+ order.getOrderType()
 							+ "\nSymbol :"
 							+ order.getSymbol()
 							+ "\nQuantity :"
 							+ order.getQuantity()
 							+ "\nOrder Status :"
 							+ order.getOrderStatus()
 							+ "\nOrder Open Date :"
 							+ order.getOpenDate()
 							+ "\nCompletionDate :"
 							+ order.getCompletionDate());
 		}
 
 		try {
 			DAOFactory factory = DAOFactory
 					.getFactory();
 
 			orderDAO = factory.getOrderDAO();
 			orderDAO.beginTransaction();
 
 			BigDecimal total = null;
 			int holdingId = -1;
 
 			//Quote quote = orderDAO.getQuoteForUpdate(order.getSymbol());
 			/* Tiago: making this more dynamic. query config service for a
 			 * quote service, then use quote service to get quote data
 			 */
 			
			SharedConfigurationServiceV1Consumer consumer = new SharedConfigurationServiceV1Consumer("OrderProcessingService","production");
 			
 			GetQSLocationsResponse response = consumer.getQSLocations(new GetQSLocationsRequest());
 			
 			if (response.getLocations().size() == 0) {
 				System.out.println("Unable to locate a quote service");
 				return;
 			}
 			
 			/* Got the QS Endpoint */
 			String qsEndpoint = response.getLocations().get(0).getServiceURL();
 
			SharedQuoteServiceV1Consumer qsConsumer = new SharedQuoteServiceV1Consumer("OrderProcessingService", "production");
 			
 			GetQuotesRequest request = new GetQuotesRequest();
 			request.getSymbols().add(order.getSymbol());
 			
 			qsConsumer.getService().setServiceLocation(new URL(qsEndpoint));
 			
 			GetQuotesResponse qsResponse = qsConsumer.getQuotes(request);
 			
 			if (qsResponse.getQuoteData().size() == 0) {
 				System.out.println("Unable to locate a quote entry for the symbol :"
 									+ order.getSymbol());
 				return;
 			}
 			
 			RemoteQuoteData quoteData = qsResponse.getQuoteData().get(0);
 			
 			BigDecimal price = BigDecimal.valueOf(quoteData.getValue());
 			
 			order.setPrice(price);
 
 			if (StockTraderUtility.ORDER_TYPE_BUY.equals(order.getOrderType())) {
 				holdingId = orderDAO.createHolding(order);
 				BigDecimal orderQuantity = BigDecimal.valueOf(order
 						.getQuantity());
 				BigDecimal orderPrice = order.getPrice();
 				total = orderQuantity.multiply(orderPrice);
 
 				BigDecimal orderFee = order.getOrderFee();
 				if (orderFee != null) {
 					total = total.add(orderFee);
 				}
 
 			} else if (StockTraderUtility.ORDER_TYPE_SELL.equals(order
 					.getOrderType())) {
 				holdingId = sellHolding(order);
 				if (holdingId == -1) {
 					return;
 				}
 
 				BigDecimal orderQuantity = BigDecimal.valueOf(-1
 						* order.getQuantity());
 				BigDecimal orderPrice = order.getPrice();
 				total = orderQuantity.multiply(orderPrice);
 
 				BigDecimal orderFee = order.getOrderFee();
 				if (total != null) {
 					total = total.add(orderFee);
 				}
 			}
 			orderDAO.updateAccountBalance(order.getAccountId(), total);
 			orderDAO.updateStockPriceVolume(order.getQuantity(), quoteData);
 			order.setOrderStatus(StockTraderUtility.ORDER_STATUS_CLOSED);
 			order.setCompletionDate(Calendar.getInstance());
 			order.setHoldingId(holdingId);
 			orderDAO.closeOrder(order);
 			orderDAO.commitTransaction();
 
 		} catch (Exception e) {
 			try {
 				orderDAO.rollbackTransaction();
 				logger.error("", e);
 			} catch (DAOException e1) {
 				logger.error("", e1);
 			}
 		} finally {
 			if (orderDAO != null) {
 				try {
 					orderDAO.close();
 				} catch (DAOException e) {
 					logger.error("", e);
 				}
 			}
 		}
 	}
 
 	private int sellHolding(Order order) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("ProcessOrder.sellHolding(OrderDataModel) \nOrderID :"
 					+ order.getOrderID() + "\nOrderType :"
 					+ order.getOrderType() + "\nSymbol :" + order.getSymbol()
 					+ "\nQuantity :" + order.getQuantity() + "\nOrder Status :"
 					+ order.getOrderStatus() + "\nOrder Open Date :"
 					+ order.getOpenDate() + "\nCompletionDate :"
 					+ order.getCompletionDate());
 		}
 
 		Holding holding = orderDAO.getHoldingForUpdate(order.getOrderID());
 
 		if (holding == null) {
 			// TODO : DAOException ..
 			throw new RuntimeException(
 					"Unable to locate a holding entry for orderID :"
 							+ order.getOrderID());
 		}
 		order.setAccountId(holding.getAccountID());
 
 		// There are three distinct business cases here, each needs different
 		// treatment:
 		// a) Quantity requested is less than total shares in the holding --
 		// update holding.
 		// b) Quantity requested is equal to total shares in the holding --
 		// delete holding.
 		// c) Quantity requested is greater than total shares in the holding --
 		// delete holding, update order table.
 
 		if (order.getQuantity() < holding.getQuantity()) {
 			orderDAO.updateHolding(holding.getHoldingID(), order.getQuantity());
 
 		} else if (holding.getQuantity() == order.getQuantity()) {
 			orderDAO.deleteHolding(holding.getHoldingID());
 
 		} else if (order.getQuantity() > holding.getQuantity()) {
 			// We now need to back-update the order record quantity to reflect
 			// fact not all shares originally requested were sold since the
 			// holding had less shares in it, perhaps due to other orders placed
 			// against that holding that completed before this one. So we will
 			// sell the remaining shares, but need to update the final order to
 			// reflect this.
 			orderDAO.deleteHolding(holding.getHoldingID());
 			order.setQuantity(holding.getQuantity());
 			order.setAccountId(holding.getAccountID());
 			orderDAO.updateOrder(order);
 		}
 		return holding.getHoldingID();
 	}
 }
