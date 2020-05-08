 package org.test.parse;
 
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.NodeClassFilter;
 import org.htmlparser.filters.OrFilter;
 import org.htmlparser.tags.TableColumn;
 import org.htmlparser.tags.TableRow;
 import org.htmlparser.tags.TableTag;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 
 public class HtmlTableParser {
 	
 	public static void parseTable(String content,int [] filterCols,String [] filters,int ...cols) {
 		NodeFilter tableFilter = new NodeClassFilter(TableTag.class);
 		Node[] nodes = htmlToNode(content, new NodeFilter[] { tableFilter });
 		
 		for (Node node : nodes) {
 			TableTag table = (TableTag)node;
 			TableRow[] rows = table.getRows();
 			
 			//遍历每行
             for (int r=8; r<rows.length-1; r++) {
                 TableRow tr = rows[r];
                 TableColumn[] td = tr.getColumns();
                 for (int fCols = 0;fCols < filterCols.length;fCols++) {
					if(td[filterCols[fCols]].toString().indexOf(filters[fCols]) > -1 || td[filterCols[fCols]].getChildrenHTML().indexOf(filters[fCols]) > -1){
 						for (int dispCols : cols) {
							System.out.print(" "+td[dispCols].toPlainTextString().trim() + " ");
 						}
 						System.out.println();
 					}
 				}
             }
 		}
 	}
 	
 	private static Node[] htmlToNode(String content, NodeFilter[] filters) {
 		Parser parser = Parser.createParser(content, "GBK");
 		NodeList nodeList = null;
 		OrFilter orFilter = new OrFilter();
 		orFilter.setPredicates(filters);
 		try {
 			nodeList = parser.parse(orFilter);
 		} catch (ParserException e) {
 			e.printStackTrace();
 		}
 		if (nodeList != null) {
 			return nodeList.toNodeArray();
 		} else {
 			return null;
 		}
 	}
 	
 	public static int [] strArrToIntArr(String [] strArr){
 		int array[] = new int[strArr.length];
 		for(int i=0;i<strArr.length;i++){  
 			try{
 				array[i]=Integer.parseInt(strArr[i].replace("'", ""));   
 			} catch (Exception e){
 				
 			}
 		}
 		
 		return array;
 	}
 	
 	public static void run(String[] args) {
 
 		if(args.length < 6){
 			Common.logln("列数据从0开始");
 			Common.logln("参数2为文件路径");
 			Common.logln("参数3为文件编码");
 			Common.logln("参数4为文件哪列需要过滤，可放多个'2,2'");
 			Common.logln("参数5为文件哪列需要过滤的内容，和需要过滤的列对应，可放多个'STOP JOURNEY,START JOURNEY'");
 			Common.logln("参数6为文件过滤后需要哪些列显示，可放多个'0,2,5'");
 			Common.logln("例子:java -jar parseTable.jar -f E://1.xls GBK \"2,2\" \"STOP JOURNEY,START JOURNEY\" \"0,2,5\"");
 			
 			return;
 		}
 		
 		String filePath = args[1];
 		String fileEncoding = args[2];
 		String filterCols = args[3];
 		String filterCont = args[4];
 		String dispCols = args[5];
 		
 		int [] fCols = strArrToIntArr(filterCols.split(","));
 		String [] filters = filterCont.split(",");
 		parseTable(Common.readResource(filePath,fileEncoding),fCols,filters,strArrToIntArr(dispCols.split(",")));
 	}
 }
