 package com.ben.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ParseException;
 import org.json.simple.JSONObject;
 
 
 
 public class mainPnL {
 
 	 ResultSet rs = null;
 	 Connection con = null;
 	    Statement st = null;
 	   
 	   
 	    String url = "jdbc:mysql://localhost:3306/Stocks";
 	    String user = "root";
 	    String password = "root";
 	
 	
 	public String Value_pie_json(Boolean live) throws SQLException
 	{
 		JSONObject obj=new JSONObject();
 		  JSONObject obj_cols_1=new JSONObject();
 		  JSONObject obj_cols_2=new JSONObject();
 		  obj_cols_1.put("id","");
 		  obj_cols_1.put("label","Stock");
 		  obj_cols_1.put("type","string");
 		  
 		  obj_cols_2.put("id","");
 		  obj_cols_2.put("label","$Value");
 		  obj_cols_2.put("type","number");
 		  
 		  LinkedList l_cols = new LinkedList();
 		  
 		  l_cols.add(obj_cols_1);
 		  l_cols.add(obj_cols_2);
 		  obj.put("cols", l_cols);
 		  try
 		  {
 			  
 		rs = LoadData("Select distinct Ticker from CurrentHoldings");
 		  }
 		  catch (Exception e)
 		  {
 			  System.out.println(e.toString());
 			  
 			  
 		  }
 		String Ticker;
 		String LastPx="";
 		String Qty;
 		String AvgPx;
 	
 		Double Value;
 		GoogleScrape gs = new GoogleScrape();
 		List<String> Tickers = new ArrayList<String>();
 		 LinkedList l_final = new LinkedList();
 		while (rs.next())
 		{
 			//Ticker = (rs.getString(1));
 			Tickers.add(rs.getString(1));
 		}
 		for (String name : Tickers)
 		{	
 		Ticker = (name);
 	
 		Qty = LoadData_str("Select Max(Quantity) from CurrentHoldings where Ticker = '"+Ticker+"' limit 1");
 		
 		if (live==true)
 		{
 		LastPx = LoadData_str("select LastPx from PnL where Ticker='"+Ticker+"' order by date desc limit 1" );
 //  	LastPx = gs.getLast(Ticker);
   		
 		Value = Double.valueOf(LastPx)*Double.valueOf(Qty);
 		
 		}
 		else
 		{
 		AvgPx = LoadData_str("Select AvgPx from CurrentHoldings where Ticker = '"+Ticker+"'");	
 		Value = Double.valueOf(AvgPx)*Double.valueOf(Qty);	
 		}
 		
 		
 		
 		  JSONObject obj2=new JSONObject();
 		  JSONObject obj3=new JSONObject();
 		  JSONObject obj4=new JSONObject();
 		  JSONObject obj_col=new JSONObject();
 		  
 		  obj3.put("v", Ticker);
 		  obj3.put("f", null);
 		  obj4.put("v",Value);
 		  obj4.put("f", null);
 		 
 		  LinkedList l1 = new LinkedList();
 		  LinkedHashMap m1 = new LinkedHashMap();
 		  l1.add(obj3);
 		  l1.add(obj4);
 		  m1.put("c",l1);
 		  
 		  l_final.add(m1);
 		}
 		obj.put("rows",l_final);
 		 
 		System.out.println("Pie Chart: "+ obj.toJSONString());
 		
 		  return obj.toJSONString();
 		
 	}
 	public String Value_Line_json() throws SQLException
 	{
 		List<String> Tickers = new ArrayList<String>();
 		rs = LoadData("Select distinct Ticker from CurrentHoldings");
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj=new JSONObject();
 		while (rs.next())
 		{
 			//Ticker = (rs.getString(1));
 			Tickers.add(rs.getString(1));
 		}
 		for (String name : Tickers)
 		{
 		
 		  JSONObject obj_cols_1=new JSONObject();
 		  
 		  obj_cols_1.put("id","");
 		  obj_cols_1.put("label",name);
 		  obj_cols_1.put("type","number");
 		  
 		  l_cols.add(obj_cols_1);
 		}
 		JSONObject obj_cols_1=new JSONObject();
 		 obj_cols_1.put("id","");
 		  obj_cols_1.put("label","Total");
 		  obj_cols_1.put("type","number");
 		  l_cols.add(obj_cols_1);
 		
 		
 		JSONObject obj_cols_2=new JSONObject();
 		  
 		  
 		  obj_cols_2.put("id","");
 		  obj_cols_2.put("label","dates");
 		  obj_cols_2.put("type","string");
 		  
 		 
 		  l_cols.add(0,obj_cols_2);
 		  
 		  obj.put("cols", l_cols);
 		 		  
 		  
 		  try
 		  {
 			  
 		rs = LoadData("Select distinct date  from PNL");
 		  }
 		  catch (Exception e)
 		 {
 			 System.out.println(e.toString());
 		 
 			 
 		 }
 		String Ticker;
 		String LastPx;
 		String Qty;
 		String PnL;
 		Double Value;
 		GoogleScrape gs = new GoogleScrape();
 		List<String> Date_list = new ArrayList<String>();
 		 LinkedList l_final = new LinkedList();
 		 
 		 
 		while (rs.next())
 		
 		{
 			//Ticker = (rs.getString(1));
 			Date_list.add(rs.getString(1));
 		}
 		
 		
 		
 		  
 		
 		  
 		for (String date : Date_list)
 		{
 			 LinkedList l1 = new LinkedList();
 			for (String ticker : Tickers)
 			{
 		
 			Ticker = (ticker);
 		//	LastPx = gs.getLast(Ticker);
 			try
 			{
 						PnL = LoadData_str("Select PL from PnL where date = '"+date+"' and Ticker = '"+ticker+"'");
 			}
 			catch (Exception e)
 			{
 				PnL = "0";
 			}
 						
 					  
 		
 		 
 		  
 		  JSONObject obj_col=new JSONObject();
 		  JSONObject obj_val=new JSONObject(); 
 		
 		  obj_val.put("v",Double.valueOf(PnL));
 		  obj_val.put("f", null);
 		 
 		  l1.add(obj_val);
 		 
 		 
 			}
 			
 			
 			//Total PNL calculation
 			String TotalPnL;
 			try
 			{
 						TotalPnL = LoadData_str("Select SUM(PL) from PnL where date = '"+date+"'");
 			}
 			catch (Exception e)
 			{
 				TotalPnL = "0";
 			}
 			
 			
 			
 			JSONObject obj_val=new JSONObject(); 
 			
 			 obj_val.put("v",Double.valueOf(TotalPnL));
 			  obj_val.put("f", null);
 			
 			  l1.add(obj_val);
 			  
 			  
 			  //End
 			  
 			  
 			
 			  JSONObject obj_date=new JSONObject();
 			  LinkedHashMap m1 = new LinkedHashMap();
 			obj_date.put("v", date);
 			obj_date.put("f",null);
 			
 			 l1.add(0,obj_date);
 			
 			
 			 m1.put("c",l1);
 			 l_final.add(m1);
 			 
 			//  l1.clear();
 			 // m1.clear();
 		}
 		 
 		obj.put("rows",l_final);
 		 System.out.println(obj);
 		 
 		  return obj.toJSONString();
 		
 	}
 	public String FXTable() throws SQLException
 	{
 		
 		//Columns
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj=new JSONObject();
 		JSONObject obj_cols_1=new JSONObject();
 		  JSONObject obj_cols_2=new JSONObject();
 		  JSONObject obj_cols_3=new JSONObject();
 		  JSONObject obj_cols_4=new JSONObject();
 		  obj_cols_1.put("id","");
 		  obj_cols_1.put("label","GBP");
 		  obj_cols_1.put("type","number");
 		  
 		  obj_cols_2.put("id","");
 		  obj_cols_2.put("label","USD");
 		  obj_cols_2.put("type","number");
 		
 		  obj_cols_3.put("id","");
 		  obj_cols_3.put("label","AvgFX");
 		  obj_cols_3.put("type","number");
 		  
 		  obj_cols_4.put("id","");
 		  obj_cols_4.put("label","FX_PnL");
 		  obj_cols_4.put("type","number");
 		  
 		  
 	//	  obj_cols_4.put("id","");
 	//	  obj_cols_4.put("label","BreakevenFx");
 	//	  obj_cols_4.put("type","number");
 		  
 		  l_cols.add(obj_cols_1);
 		  l_cols.add(obj_cols_2);
 		  l_cols.add(obj_cols_3);
 		  l_cols.add(obj_cols_4);
 		  obj.put("cols", l_cols);
 	//rows
 		  
 		  String USD_IN = LoadData_str("Select (select sum(dollar_value) from FX where Direction ='IN') - (select sum(dollar_value) from FX where Direction ='OUT') as difference"); 
 			
 		  String GBP_IN = LoadData_str("Select (select sum(pound_value) from FX where Direction ='IN') - (select sum(pound_value) from FX where Direction ='OUT') as difference"); 
 		
 		  Double Fx_Rate = Double.valueOf(USD_IN) / Double.valueOf(GBP_IN);
 		  
 		  Bloomberg_scrape BS = new Bloomberg_scrape();
 		  
 		  String FX_last = BS.getFX();
 		  DecimalFormat df = new DecimalFormat("#.##");
 		  
 		  String FX_change = df.format(((Double.valueOf(Fx_Rate)/ Double.valueOf(FX_last))-1)*100);
 		  String FX_pct;
 		 /*
 		  if (Double.valueOf(Fx_Rate) > Double.valueOf(FX_last))
 		  {
 			  FX_pct = Fx_last+" ("+ FX_change+")";
 		  
 		  }
 		  else
 		  {
 			  FX_pct = Fx_last+" ("+ FX_change+")";  
 			  
 		  }
 		 **/
 		  
 		  JSONObject obj_row1=new JSONObject(); 
 			JSONObject obj_row2=new JSONObject(); 
 			JSONObject obj_row3=new JSONObject(); 
 			JSONObject obj_row4=new JSONObject();
 			 obj_row1.put("v",GBP_IN);
 			  obj_row1.put("f", null);
 			  obj_row2.put("v",USD_IN);
 			  obj_row2.put("f", null);
 			  obj_row3.put("v",Fx_Rate);
 			  obj_row3.put("f", null);
 			  obj_row4.put("v",FX_last+" ("+FX_change+")");
 			  obj_row4.put("f", null);
 			  
 			  
 			  
 			  
 	//	  obj_row4.put("v",d_Change);
 	//		  obj_row4.put("f", null);
 			  
 			  LinkedList l1_rows = new LinkedList();
 			  l1_rows.add(obj_row1);
 			  l1_rows.add(obj_row2);
 			  l1_rows.add(obj_row3);
 			  l1_rows.add(obj_row4);
 			  LinkedHashMap m1 = new LinkedHashMap();
 			  LinkedList l_final = new LinkedList();
 			  m1.put("c",l1_rows);
 			  l_final.add(m1);
 			  obj.put("rows",l_final);
 				 System.out.println(obj);
 				 
 				  return obj.toJSONString();  
 	}
 	public String Table_holdings() throws SQLException
 	{
 	
 		//Column names
 			LinkedList l_cols = new LinkedList();
 			JSONObject obj=new JSONObject();
 						
 		  JSONObject obj_cols_1=new JSONObject();
 		  JSONObject obj_cols_2=new JSONObject();
 		  JSONObject obj_cols_3=new JSONObject();
 		  JSONObject obj_cols_4=new JSONObject();
 		  JSONObject obj_cols_5=new JSONObject();
 		  JSONObject obj_cols_6=new JSONObject();
 		  JSONObject obj_cols_7=new JSONObject();
 		  obj_cols_1.put("id","");
 		  obj_cols_1.put("label","Ticker");
 		  obj_cols_1.put("type","String");
 		  
 		  obj_cols_2.put("id","");
 		  obj_cols_2.put("label","Qty");
 		  obj_cols_2.put("type","number");
 		  
 		  obj_cols_3.put("id","");
 		  obj_cols_3.put("label","AvgPx");
 		  obj_cols_3.put("type","number");
 		  
 		  obj_cols_4.put("id","");
 		  obj_cols_4.put("label","LastPx");
 		  obj_cols_4.put("type","number");
 		  
 		  obj_cols_5.put("id","");
 		  obj_cols_5.put("label","UPnLvLast");
 		  obj_cols_5.put("type","number");
 		  
 		  obj_cols_6.put("id","");
 		  obj_cols_6.put("label","%");
 		  obj_cols_6.put("type","number");
 		  
 		  obj_cols_7.put("id","");
 		  obj_cols_7.put("label","Date");
 		  obj_cols_7.put("type","String");
 		  
 		  l_cols.add(obj_cols_1);
 		  l_cols.add(obj_cols_2);
 		  l_cols.add(obj_cols_3);
 		  l_cols.add(obj_cols_4);
 		  l_cols.add(obj_cols_5);
 		  l_cols.add(obj_cols_6);
 		  l_cols.add(obj_cols_7);
 		  obj.put("cols", l_cols);
 		//End Columns 		  
 		
 		  rs = LoadData("Select distinct Ticker from CurrentHoldings");
 		  ArrayList<String> l_Tickers = new ArrayList<String>();
 		  LinkedList l_final = new LinkedList();
 		  while (rs.next())
 			{
 				l_Tickers.add(rs.getString(1));
 			}
 		 
 		  String Qty;
 		  String AvgPx;
 		  String LastPx;
 		  String UPnLvLast;
 		  String Pcnt;
 		  String Date;
 		  for (String name : l_Tickers)
 			{
 			  LinkedList l1_rows = new LinkedList();
 				rs = LoadData("Select Quantity, AvgPx from CurrentHoldings where Ticker ='"+name+"'");
 				rs.next();
 			
 				Qty=rs.getString(1);
 				AvgPx=rs.getString(2);
 				rs = LoadData("Select LastPx, PL,PL_Percent,date from pnl where Ticker ='"+name+"' order by date desc limit 1");
 				rs.next();
 				LastPx=rs.getString(1);
 				UPnLvLast=rs.getString(2);
 				Pcnt=rs.getString(3);
 				Date=rs.getString(4);
 				
 				JSONObject obj_row1=new JSONObject(); 
 				JSONObject obj_row2=new JSONObject(); 
 				JSONObject obj_row3=new JSONObject(); 
 				JSONObject obj_row4=new JSONObject(); 
 				JSONObject obj_row5=new JSONObject(); 
 				JSONObject obj_row6=new JSONObject(); 
 				JSONObject obj_row7=new JSONObject(); 
 				  obj_row1.put("v",name);
 				  obj_row1.put("f", null);
 				  obj_row2.put("v",Qty);
 				  obj_row2.put("f", null);
 				  obj_row3.put("v",AvgPx);
 				  obj_row3.put("f", null);
 				  obj_row4.put("v",LastPx);
 				  obj_row4.put("f", null);
 				  obj_row5.put("v",UPnLvLast);
 				  obj_row5.put("f", null);
 				  obj_row6.put("v",Pcnt);
 				  obj_row6.put("f", Pcnt+"%");
 				  obj_row7.put("v",Date);
 				  obj_row7.put("f", null);
 				  
 				  
 				  l1_rows.add(obj_row1);
 				  l1_rows.add(obj_row2);
 				  l1_rows.add(obj_row3);
 				  l1_rows.add(obj_row4);
 				  l1_rows.add(obj_row5);
 				  l1_rows.add(obj_row6);
 				  l1_rows.add(obj_row7);
 				
 				  LinkedHashMap m1 = new LinkedHashMap();
 									
 					
 					 m1.put("c",l1_rows);
 					 l_final.add(m1);
 				  
 				  
 			}
 		  obj.put("rows",l_final);
 			 System.out.println(obj);
 			 
 			  return obj.toJSONString();
 		
 	}
 	
 	public String Table_PL_history() throws SQLException
 	{
 		//Column names
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj=new JSONObject();
 					
 	  JSONObject obj_cols_1=new JSONObject();
 	  JSONObject obj_cols_2=new JSONObject();
 	  JSONObject obj_cols_3=new JSONObject();
 	  JSONObject obj_cols_4=new JSONObject();
 	  JSONObject obj_cols_5=new JSONObject();
 	  JSONObject obj_cols_6=new JSONObject();
 	  JSONObject obj_cols_7=new JSONObject();
 	  obj_cols_1.put("id","");
 	  obj_cols_1.put("label","Ticker");
 	  obj_cols_1.put("type","string");
 	  
 	  obj_cols_2.put("id","");
 	  obj_cols_2.put("label","$P/L");
 	  obj_cols_2.put("type","number");
 	  
 	
 	  
 	  l_cols.add(obj_cols_1);
 	  l_cols.add(obj_cols_2);
 	//  l_cols.add(obj_cols_3);
 	 // l_cols.add(obj_cols_4);
 	  //l_cols.add(obj_cols_5);
 	 // l_cols.add(obj_cols_6);
 	 // l_cols.add(obj_cols_7);
 	  obj.put("cols", l_cols);
 	//End Columns 		  
 	
 	  rs = LoadData("Select distinct Ticker from holdingshistory");
 	  ArrayList<String> l_Tickers = new ArrayList<String>();
 	  
 	  LinkedList l_final = new LinkedList();
  	  while (rs.next())
 		{
 			l_Tickers.add(rs.getString(1));
 		}
 	 
 	  String Qty;
 	  String Buy_Px;
 	  String Sell_Px;
 	  Double RPnL;
 	  String Pcnt;
 	  String Date;
 	  for (String name : l_Tickers)
 		{
		 
 			rs = LoadData("Select Quantity, Px from holdingshistory where Ticker ='"+name+"' and Direction ='B' order by Date asc");
 			ArrayList<String> l_Tickers_qty = new ArrayList<String>();  
 			ArrayList<String> l_Tickers_px = new ArrayList<String>();  
 			while (rs.next())
 				{
 					l_Tickers_qty.add(rs.getString(1));
 					l_Tickers_px.add(rs.getString(2));
 				}
 			 for (int i=0;i< l_Tickers_qty.size();i++)
 			 {	
				 LinkedList l1_rows = new LinkedList();
 				Qty=l_Tickers_qty.get(i);
 				Buy_Px=l_Tickers_px.get(i);
 				 ResultSet rs_sell = null;
 				rs_sell = LoadData("Select Px from holdingshistory where Ticker ='"+name+"' and Direction ='S' and Quantity ='"+Qty+"'");
 				if (rs_sell.next())
 				{
 					//				rs_sell.next();
 				Sell_Px = rs_sell.getString(1);
 				
 				RPnL = Double.valueOf(Qty) *( Double.valueOf(Sell_Px)-Double.valueOf(Buy_Px));
 				
 			
 				JSONObject obj_row1=new JSONObject(); 
 				JSONObject obj_row2=new JSONObject(); 
 				  obj_row1.put("v",name);
 				  obj_row1.put("f", null);
 				  obj_row2.put("v",RPnL);
 				  obj_row2.put("f", null);
 				  
 			
 			  l1_rows.add(obj_row1);
 			  l1_rows.add(obj_row2);
 			  
 			
 			  LinkedHashMap m1 = new LinkedHashMap();
 								
 				
 				 m1.put("c",l1_rows);
 				 l_final.add(m1);
 				}
 				else
 				{}
 			}
 			  
 		}
 	  obj.put("rows",l_final);
 		 System.out.println(obj);
 		 
 		  return obj.toJSONString();
 		
 		
 		
 	}
 
 	
 	public String LoadData_str(String Message) throws SQLException
 	{
 		LogOutput(Message);
 		 PreparedStatement pst = null;
 		
 		 con = DriverManager.getConnection(url, user, password);
 	//	 st = con.createStatement();
      //    rs = st.executeQuery("SELECT VERSION()");	
     //     rs.next();
 	//	 System.out.println(rs.getString(1));
 		 
          pst = con.prepareStatement(Message);
          rs = pst.executeQuery();
          rs.next();
       //   while (rs.next()) {
       //       System.out.print(rs.getString(1));
       //       System.out.print(": ");
          //    System.out.println(rs.getString(2));
       //   }
 		return rs.getString(1);
 	}
 	public ResultSet LoadData(String Message) throws SQLException
 	{
 		LogOutput(Message);
 		 PreparedStatement pst = null;
 		
 		 con = DriverManager.getConnection(url, user, password);
 	//	 st = con.createStatement();
     //     rs = st.executeQuery("SELECT VERSION()");	
     //     rs.next();
 //		 System.out.println(rs.getString(1));
          pst = con.prepareStatement(Message);
          rs = pst.executeQuery();
 		
       //   while (rs.next()) {
       //       System.out.print(rs.getString(1));
       //       System.out.print(": ");
          //    System.out.println(rs.getString(2));
       //   }
          
 		return rs;
 	}
 	public void LogOutput(String Message)
 	{
 		
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		DateFormat dateFormat_log = new SimpleDateFormat("yyyy.MM.dd");
 		Date date = new Date();
 		System.out.println(dateFormat.format(date)+" : "+Message);
  		//System.out.printf("%D %R : ",date + Message);
 	/*
 		try {
 			 
 		
  
 			File file = new File("/home/pi/logs/"+dateFormat_log.format(date)+".PiFinance.log.txt");
 			//File file = new File("c:\\"+dateFormat_log.format(date)+".PiFinance.log.txt");
 			// if file doesnt exists, then create it
 			if (!file.exists()) {
 				file.createNewFile();
 			}
  
 			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.write(dateFormat.format(date)+" : "+Message+"\n");
 			bw.close();
  
 		
  
 		} catch (IOException e) {
 			System.out.println(e.toString());
 		}
 		*/
 		
 	}
 	
 }
