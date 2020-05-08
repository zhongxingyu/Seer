 /*
  * Copyright 2002-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.amqp.rabbit.stocks.handler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.amqp.rabbit.stocks.domain.MockStock;
 import org.springframework.amqp.rabbit.stocks.domain.TradeRequest;
 import org.springframework.amqp.rabbit.stocks.domain.TradeResponse;
 import org.springframework.amqp.rabbit.stocks.service.CreditCheckService;
 import org.springframework.amqp.rabbit.stocks.service.ExecutionVenueService;
 import org.springframework.amqp.rabbit.stocks.service.TradingService;
 import org.springframework.util.StringUtils;
 
 /**
  * POJO handler that receives trade requests and sends back a trade response. Main application logic sits here which
  * coordinates between {@link ExecutionVenueService}, {@link CreditCheckService}, and {@link TradingService}.
  * 
  * @author Mark Pollack
  * 
  */
 public class ServerHandler {
 
 	private ExecutionVenueService executionVenueService;
 
 	private CreditCheckService creditCheckService;
 
 	private TradingService tradingService;
 
 	public ServerHandler(ExecutionVenueService executionVenueService, CreditCheckService creditCheckService,
 			TradingService tradingService) {
 		this.executionVenueService = executionVenueService;
 		this.creditCheckService = creditCheckService;
 		this.tradingService = tradingService;
 	}
 
 	public TradeResponse handleMessage(TradeRequest tradeRequest) {
 		TradeResponse tradeResponse;
 		List<String> errors = new ArrayList<String>();
 		if (validRequest(tradeRequest, errors)) {
 			if (creditCheckService.canExecute(tradeRequest, errors)) {
 				tradeResponse = executionVenueService.executeTradeRequest(tradeRequest);
				tradingService.processTrade(tradeRequest, tradeResponse);
 			} else {
 				tradeResponse = createErrorTradeResponse(tradeRequest, errors);
 
 			}
 		} else {
 			tradeResponse = createErrorTradeResponse(tradeRequest, errors);
 		}
 		return tradeResponse;
 	}
 	
 	private boolean validRequest(TradeRequest tradeRequest, List<String> errors) {
 			if (MockStock.getStockByName(tradeRequest.getTicker())!=null) {
 				return true;
 			} else {
 				errors.add("Invalid Ticker [" + tradeRequest.getTicker() +"]");
 			}
 			return false;
 	}
 
 	private TradeResponse createErrorTradeResponse(TradeRequest tradeRequest, List<String> errors) {
 		TradeResponse tradeResponse;
 		tradeResponse = new TradeResponse();
 		tradeResponse.setError(true);
 		tradeResponse.setErrorMessage(StringUtils.arrayToCommaDelimitedString(errors.toArray()));
 		tradeResponse.setAccountName(tradeRequest.getAccountName());
 		tradeResponse.setBuyRequest(tradeRequest.isBuyRequest());
 		tradeResponse.setOrderType(tradeRequest.getOrderType());
 		tradeResponse.setRequestId(tradeRequest.getRequestId());
 		tradeResponse.setPrice(tradeRequest.getPrice());
 		tradeResponse.setQuantity(tradeRequest.getQuantity());
 		tradeResponse.setTicker(tradeRequest.getTicker());
 		return tradeResponse;
 	}
 
 }
