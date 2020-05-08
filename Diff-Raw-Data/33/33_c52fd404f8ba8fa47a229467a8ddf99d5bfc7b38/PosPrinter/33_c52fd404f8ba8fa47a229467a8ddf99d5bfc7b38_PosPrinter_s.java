 /******************************************************************************
 * Copyright (C) 2013 ShenZhen 1000funs Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳千方百味公司开发研制。
 ******************************************************************************/
 package com.magiccube.core.util.tools;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.print.Doc;
 import javax.print.DocFlavor;
 import javax.print.DocPrintJob;
 import javax.print.PrintService;
 import javax.print.PrintServiceLookup;
 import javax.print.SimpleDoc;
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.Size2DSyntax;
 import javax.print.attribute.standard.MediaPrintableArea;
 
 import com.magiccube.common.model.EnvironmentInfoVO;
 import com.magiccube.order.model.OrderFoodView;
 import com.magiccube.order.model.OrderView;
 import com.magiccube.order.model.PlateVO;
 
 /**
  * 
  *
  * @author 黄科林
  * @since jdk6.0
  * @version  2013-3-25 黄科林
  */
 public class PosPrinter {
 	
 	public static String LOGO_PATH = EnvironmentInfoVO.WEBROOT+"web/img/logo.png";
 	
 	// web调用打印
 	public static void doPrint(int index, String bf) {
 	   try {
 	    DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
 	    
 	    //使用默认打印机，如果默认打印机不是POS打印机，请通过名称查找。    
 	    PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
 	    // job
 	    DocPrintJob job = printer.createPrintJob();
 	    
 	    byte[] buf1 = bf.getBytes();
 	    byte[] data3 = new byte[buf1.length];
 	    
 	    
 	    System.arraycopy(buf1,0,data3,0,buf1.length);
 	    //System.arraycopy(buf2,0,data3,buf1.length,buf2.length);
 	    //System.arraycopy(buf3,0,data3,buf1.length+buf2.length,buf3.length);
 	    
 	    InputStream stream = new ByteArrayInputStream(data3);
 	    Doc doc = new SimpleDoc(stream, flavor, null);
 	    Doc doc2 = new SimpleDoc(new FileInputStream(LOGO_PATH), DocFlavor.INPUT_STREAM.PNG, null);
 
 	    
 	    // 获得打印属性
 		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
 		// 每一次默认打印一页
 		//pras.add(new Copies(1));
 		MediaPrintableArea mp = new MediaPrintableArea(0f, 20f, 100f, 40f, Size2DSyntax.MM);// 5f, 5f, 100f, 40f
 		pras.add(mp);
 		DocPrintJob job2 = printer.createPrintJob();
 	    job2.print(doc2, pras);
 	    
 	    // print
 	    job.print(doc, null);
 	    
 	   } catch (Exception e) {
 		   e.printStackTrace();
 	   }
 	}
 	
 	public static void print(OrderView orderView){
 		StringBuffer sbPrinter = new StringBuffer();
 		sbPrinter.append(PosPrinter.getHead(orderView));
 		sbPrinter.append(PosPrinter.getContent(orderView));
 		sbPrinter.append(PosPrinter.getTail(orderView));
 		
 		//doPrint(1, sbPrinter.toString());
 		
 		DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
 		try {
		    System.out.println(sbPrinter.toString());
 			
 			// 使用默认打印机，如果默认打印机不是POS打印机，请通过名称查找。    
 		    PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
 		    
 		    if (printer != null) {
 				DocPrintJob job = printer.createPrintJob();
 				// 获得打印属性
 				PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
 				Doc doc2 = new SimpleDoc(new FileInputStream(LOGO_PATH), DocFlavor.INPUT_STREAM.PNG, null);
 				MediaPrintableArea mp = new MediaPrintableArea(0f, 0f, 90f, 20f, Size2DSyntax.MM);// 5f, 5f, 100f, 40f
 				pras.add(mp);
 				DocPrintJob job2 = printer.createPrintJob();
 				job2.print(doc2, pras);
 				
 				byte[] buf1 = sbPrinter.toString().getBytes();
 			    byte[] data3 = new byte[buf1.length];
 			    System.arraycopy(buf1,0,data3,0,buf1.length);
 			    
 				InputStream stream = new ByteArrayInputStream(data3);
 				Doc doc = new SimpleDoc(stream, flavor, null);
 				job.print(doc, null);
 			}
 		}catch (Exception e) {
		    e.printStackTrace();
 		}
 		
 	}
 	
 	private static String getHead(OrderView orderView){
 		StringBuffer bf = new StringBuffer(128);
 		String strPhone = orderView.getContact() != null ? orderView.getContact():orderView.getPhone();
 		bf.append("          千方百味     　               \n");
 		bf.append("        你的饭你做主     　　　 \n");
 		bf.append("*******************************\n");
 		bf.append("订单："+orderView.getId()+"\n");
 		bf.append("地址："+orderView.getAddress()+"\n");
 		bf.append("电话："+strPhone+"\n");
 		bf.append("期望时间："+orderView.getExceptTime()+"\n");
 		bf.append("*******************************\n");
 		return bf.toString();
 	}
 	
 	private static String getContent(OrderView orderView){
 		StringBuffer bf = new StringBuffer(256);
 		List<PlateVO> plates = orderView.getPlateList();
 		for (int i = 0; i < plates.size(); i++) {
 			PlateVO plateVO = plates.get(i);
 			bf.append("餐盘"+(i+1)+"\r\n");
 			List<OrderFoodView> foodList = plateVO.getFoodList();
 			for (int j = 0; j < foodList.size(); j++) {
 				OrderFoodView food = foodList.get(j);
 				bf.append(PosPrinter.getFormatFoodName(food.getFood())+""+PosPrinter.formateMoney(food.getPrice())+"    "+food.getAmount()+""+"\r\n");
 			}
 			bf.append("----------------餐盘"+(i+1)+"合计:"+plateVO.getPrice()+"\r\n");
 		}
 		bf.append("\t\t总计金额："+orderView.getTotalPrice()+"\r\n");
 		return bf.toString();
 		
 	}
 	
 	private static String getFormatFoodName(String name){
 		int length = StringUtil.getStringLength(name);
 		StringBuffer bf = new StringBuffer(32);
 		if (length < 20) {
 			bf.append(name);
 			while (StringUtil.getStringLength(bf.toString()) < 20) {
 				bf.append(" ");
 			}
 		}
 //		System.out.println(bf.toString());
 //		System.out.println(bf.toString().length());
 		return bf.toString();
 	}
 	
 	private static String getTail(OrderView orderView){
 		StringBuffer bf = new StringBuffer(128);
 		bf.append("操作员：admin\r\n");
 		bf.append("出单时间："+DateTimeFormatUtils.formatDateTime(new Date())+"\r\n");
 		bf.append("       欢迎您下次再来做主      　　\r\n");
 		bf.append("         1000funs.com      　　\r\n");
 		
 		return bf.toString();
 	}
 	
 	private static String formateMoney(double d){
 		return String.format("%1$6s", String.valueOf(d));
 	}
 	
 	public static void main(String[] args) {
 		OrderView orderView = new OrderView();
 		orderView.setId(198198);
 		orderView.setAddress("福田区莲花路香丽大厦一楼");
 		orderView.setPhone("13888888888");
 		orderView.setExceptTime("12点钟");
 		
 		List<PlateVO> plateList = new ArrayList<PlateVO>();
 		
 		PlateVO plate1 = new PlateVO();
 		List<OrderFoodView> foodList = new ArrayList<OrderFoodView>();
 		OrderFoodView food1 = new OrderFoodView();
 		food1.setFood("农家小炒肉套餐");
 		food1.setPrice(14.00);
 		food1.setAmount(2);
 		
 		OrderFoodView food2 = new OrderFoodView();
 		food2.setFood("萝卜牛腩");
 		food2.setPrice(10.00);
 		food2.setAmount(1);
 		
 		OrderFoodView food3 = new OrderFoodView();
 		food3.setFood("米饭");
 		food3.setPrice(2.00);
 		food3.setAmount(1);
 		
 		foodList.add(food1);
 		foodList.add(food2);
 		foodList.add(food3);
 		plate1.setFoodList(foodList);
 //		plate1.setPrice(40.00);
 		
 		PlateVO plate2 = new PlateVO();
 		List<OrderFoodView> foodList2 = new ArrayList<OrderFoodView>();
 		OrderFoodView food12 = new OrderFoodView();
 		food12.setFood("相汁排骨套餐饭");
 		food12.setPrice(18.00);
 		food12.setAmount(2);
 		
 		OrderFoodView food22 = new OrderFoodView();
 		food22.setFood("西红柿鸡蛋");
 		food22.setPrice(6.00);
 		food22.setAmount(1);
 		
 		OrderFoodView food32 = new OrderFoodView();
 		food32.setFood("米饭");
 		food32.setPrice(2.00);
 		food32.setAmount(1);
 		
 		foodList2.add(food12);
 		foodList2.add(food22);
 		foodList2.add(food32);
 		plate2.setFoodList(foodList2);
 //		plate2.setPrice(44.00);
 		
 		plateList.add(plate1);
 		plateList.add(plate2);
 		
 		orderView.setPlateList(plateList);
 		orderView.setTotalPrice(84.00);
 		
 		PosPrinter.print(orderView);
 		
 	}
 }
