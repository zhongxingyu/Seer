 package com.mns.mojoinvest.server.pipeline.quote;
 
 import com.google.appengine.tools.pipeline.FutureValue;
 import com.google.appengine.tools.pipeline.Job1;
 import com.google.appengine.tools.pipeline.Value;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.dao.FundDao;
 import com.mns.mojoinvest.server.pipeline.GenericPipelines;
 import com.mns.mojoinvest.server.pipeline.PipelineHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 
 public class ISharesQuoteFetcherControlJob extends Job1<String, String> {
 
     private static final Logger log = Logger.getLogger(ISharesQuoteFetcherControlJob.class.getName());
 
 
     @Override
     public Value<String> run(String sessionId) {
 
         List<FutureValue<String>> quotesUpdated = new ArrayList<FutureValue<String>>();
 
         FundDao dao = PipelineHelper.getFundDao();
         for (Fund fund : dao.list()) {
            quotesUpdated.add(futureCall(new ISharesQuoteFetcherJob(), immediate(fund.getFundId()),
                    immediate(sessionId)));
         }
 
         return futureCall(new GenericPipelines.MergeListJob(), futureList(quotesUpdated));
     }
 }
