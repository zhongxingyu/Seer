 package com.datastax.demo.portfolio.controller;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.util.*;
 
 import com.datastax.demo.portfolio.Portfolio;
 import com.datastax.demo.portfolio.Position;
 
 import org.apache.cassandra.hadoop.CassandraProxyClient;
import org.apache.cassandra.hadoop.CassandraProxyClient.ConnectionStrategy;
 import org.apache.cassandra.thrift.*;
 import org.apache.cassandra.utils.ByteBufferUtil;
 import org.apache.thrift.TException;
 
 public class PortfolioMgrHandler implements com.datastax.demo.portfolio.PortfolioMgr.Iface
 {
     private static final ColumnParent           pcp     = new ColumnParent("Portfolios");
     private static final SliceRange             range   = new SliceRange(ByteBufferUtil.EMPTY_BYTE_BUFFER,
                                                          ByteBufferUtil.EMPTY_BYTE_BUFFER, false, 1000);
     private static final SlicePredicate         sp      = new SlicePredicate().setSlice_range(range);
     private static final ColumnParent           scp     = new ColumnParent("Stocks");
     
     private static final ColumnParent           lcp     = new ColumnParent("HistLoss");
     private static final SlicePredicate         lcols   = new SlicePredicate();
     private static final ByteBuffer             lossCol = ByteBufferUtil.bytes("loss");
     private static final ByteBuffer             lossDateCol = ByteBufferUtil.bytes("worst_date");
     
     
     private static final ColumnParent           hcp      = new ColumnParent("StockHist");
     private static final SliceRange             hrange   = new SliceRange(ByteBufferUtil.EMPTY_BYTE_BUFFER,
             ByteBufferUtil.EMPTY_BYTE_BUFFER, true, 20);
     private static final SlicePredicate         hsp      = new SlicePredicate().setSlice_range(hrange);
 
     
     static
     {
         lcols.addToColumn_names(lossDateCol);
         lcols.addToColumn_names(lossCol);
 
     }
     
     private ThreadLocal<Cassandra.Iface> clients_ = new ThreadLocal<Cassandra.Iface>();
 
     public List<Portfolio> get_portfolios(String start_key, int limit) throws TException
     {
         KeyRange kr = new KeyRange(limit).setStart_key(start_key.getBytes()).setEnd_key(ByteBufferUtil.EMPTY_BYTE_BUFFER);
 
         try
         {
            List<KeySlice> kslices = getClient().get_range_slices(pcp, sp, kr, ConsistencyLevel.ONE);            
 
             List<Portfolio> portfolios = new ArrayList<Portfolio>();
             for (KeySlice ks : kslices)
             {
                 Portfolio p = new Portfolio();
                 p.setName(new String(ks.getKey()));
 
                 Map<ByteBuffer, Long> tickerLookup = new HashMap<ByteBuffer, Long>();
                 List<ByteBuffer> tickers = new ArrayList<ByteBuffer>();
 
                 for (ColumnOrSuperColumn cosc : ks.getColumns())
                 {
                     tickers.add(cosc.getColumn().name);
                     tickerLookup.put(cosc.getColumn().name, ByteBufferUtil.toLong(cosc.getColumn().value));
                 }
 
                 Map<ByteBuffer, List<ColumnOrSuperColumn>> prices = getClient().multiget_slice(tickers, scp, sp,
                         ConsistencyLevel.ONE);
 
                 double total = 0;
                 double basis = 0;
                 
                 Random r = new Random(Long.valueOf(new String(ks.getKey())));
                 
                 for (Map.Entry<ByteBuffer, List<ColumnOrSuperColumn>> entry : prices.entrySet())
                 {
                     if (!entry.getValue().isEmpty())
                     {                                              
                         Double price = Double.valueOf(ByteBufferUtil.string(entry.getValue().get(0).column.value));
                         Position s = new Position(ByteBufferUtil.string(entry.getKey()), price, tickerLookup.get(entry.getKey()));
                         p.addToConstituents(s);
                         total += price * tickerLookup.get(entry.getKey());
                         basis += r.nextDouble() * 100 * tickerLookup.get(entry.getKey());
                     }
                 }
 
                 p.setPrice(total);
                 p.setBasis(basis);
 
                 portfolios.add(p);
             }
 
             addLossInformation(portfolios);
             addHistInformation(portfolios);
             
             return portfolios;
         }
         catch (Exception e)
         {
             throw new TException(e);
         }
     }
 
     private Cassandra.Iface getClient()
     {
 
         Cassandra.Iface client = clients_.get();
 
         if (client == null)
         {
             try
             {
                client = CassandraProxyClient.newProxyConnection("localhost", 9160, true, ConnectionStrategy.RANDOM);
 
                 client.set_keyspace("PortfolioDemo");
             }
             catch (Exception e)
             {
                 throw new RuntimeException(e);
             }
             
             clients_.set(client);
         }
 
         return client;
     }
 
     private void addLossInformation(List<Portfolio> portfolios)
     {
         
         Map<ByteBuffer, Portfolio> portfolioLookup = new HashMap<ByteBuffer, Portfolio>();
         List<ByteBuffer> portfolioNames = new ArrayList<ByteBuffer>();
         
         for(Portfolio p : portfolios)
         {
             ByteBuffer name = ByteBufferUtil.bytes(p.name);
             portfolioLookup.put(name, p);  
             portfolioNames.add(name);
         }
         
         
         try
         {
            Map<ByteBuffer,List<ColumnOrSuperColumn>> result =  getClient().multiget_slice(portfolioNames, lcp, lcols, ConsistencyLevel.ONE);
         
            for(Map.Entry<ByteBuffer, List<ColumnOrSuperColumn>> entry : result.entrySet())
            {
                Portfolio portfolio = portfolioLookup.get(entry.getKey());
                
                if(portfolio == null)
                    continue;
                
                for(ColumnOrSuperColumn col : entry.getValue())
                {
                    if(col.getColumn().name.equals(lossCol))
                        portfolio.setLargest_10day_loss(Double.valueOf(ByteBufferUtil.string(col.getColumn().value)));
               
                    if(col.getColumn().name.equals(lossDateCol))
                        portfolio.setLargest_10day_loss_date(ByteBufferUtil.string(col.getColumn().value));                            
                }
            }
         
         }
         catch (InvalidRequestException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (UnavailableException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (TimedOutException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (TException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (CharacterCodingException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         
     }
 
     
     private void addHistInformation(List<Portfolio> portfolios)
     {
         
         
         for(Portfolio p : portfolios)
         {
             ByteBuffer name = ByteBufferUtil.bytes(p.name);
             List<ByteBuffer> tickers = new ArrayList<ByteBuffer>();
 
             
             for( Position position : p.constituents)
             {
                 tickers.add(ByteBufferUtil.bytes(position.ticker));
             }
         
             try
             {
                 Map<ByteBuffer,List<ColumnOrSuperColumn>> result =  getClient().multiget_slice(tickers, hcp, hsp, ConsistencyLevel.ONE);
         
                 Map<String, Double> histPrices = new LinkedHashMap<String,Double>();
            
                 for(Map.Entry<ByteBuffer, List<ColumnOrSuperColumn>> entry : result.entrySet())
                 {
                                   
                     for(ColumnOrSuperColumn col : entry.getValue())
                     {
                         Double price = histPrices.get(ByteBufferUtil.string(col.column.name));
                    
                         if(price == null)                      
                             price = 0.0;
                         
                    
                         price =+ Double.valueOf(ByteBufferUtil.string(col.column.value));                   
                     
                         histPrices.put(ByteBufferUtil.string(col.column.name), price);                                       
 
                     }
                
                 }
                 
                 p.setHist_prices(Arrays.asList(histPrices.values().toArray(new Double[]{})));              
             
                       
             }
             catch (InvalidRequestException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (UnavailableException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (TimedOutException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (TException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (CharacterCodingException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }      
     }
     
 }
