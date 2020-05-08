 package work;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ExpFkQueryGenerator 
 {
 	private static ExpFkQueryGenerator instance_;
 	private static Object syncObject_= new Object(); 
 	private static Object syncTemplateObject_= new Object(); 
 
 	private ExpFkXmlQueryXO template;
 
 	// Synchronization
 	public static ExpFkQueryGenerator getInstance() {
 
 		if (instance_ == null) 
 		{
 			synchronized(syncObject_) 
 			{
 				if (instance_ == null) 
 				{
 					instance_ = new ExpFkQueryGenerator();
 				}
 			}
 
 		}
 		return instance_;
 	}
 	public void setTemplate(ExpFkXmlQueryXO template)
 	{
 		if (this.template == null) 
 		{
 			synchronized(syncTemplateObject_) 
 			{
 				if (this.template == null) 
 				{
 					this.template = template;
 				}	
 			}
 		}
 	}
 	// Logic
 	public boolean isQueryTemplateLoaded()
 	{
 		return template != null;
 	}
 	
 	public String genereateQuery(GmExpPozTO data)
 	{
 		ExpFKQuery query = new ExpFKQuery(template.elemStr);
 		query.sign = data.zmienZnak() ? "-" : "";
 	
 		//SYMBOL KONTA
 		query.append(query.select, query.sep);
 		addDiList(query, data.getSymbolKontaList(), template.elemBud);
 		query.append(query.select, " as strSymbol_Kontr");
 		
 		//KONTO SZCZEGOLOWE
 		query.append(query.select, query.sep);
 		if(data.getKartotekaList()!=null)
 		{
 			addSelectiveDiList(query, data.getKosztSzczegolowyList(), template.elemBud);
 		}
 		else
 		{
 			query.append(query.select, "null");
 		}
 		query.append(query.select, " as strKonto_Szcz");
 		
 		// KARTOTEKA
 		query.append(query.select, query.sep);
 		if(data.getKartotekaList()!=null)
 		{
 			addSelectiveDiList(query, data.getKartotekaList(), template.elemBud);
 		}
 		else
 		{
 			query.append(query.select, "null");
 		}
 		query.append(query.select, " as strKarto_Exp");
 		
 	
 	
 		//tmpQuery.select += "as strKonto_Exp";
 
 
 
 		return query.toString();
 	}
 	
 
 	private void addDiList(ExpFKQuery query, List<DictItem> data, List<ExpFkXmlQueryElemXO> tList)
 	{
 		ExpFkXmlQueryElemXO elemXO = null;
 		
 		for (DictItem dI : data) 
 		{
 			elemXO = findXO(dI.getCode(), tList);
 			addXO(query, elemXO, dI);
 		}
 
 	}
 	private void addSelectiveDiList(ExpFKQuery query, List<DictItem> data, List<ExpFkXmlQueryElemXO> tList)
 	{
 		ExpFkXmlQueryElemXO elemXO = null;
 		
 		for (DictItem dI : data) 
 		{
 			elemXO = findXO(dI.getCode(), tList);
 			
 			if(!query.elements.contains(elemXO))
 			{
 				addXO(query, elemXO, dI);
 			}
 			else
 			{
 				addSelectiveXO(query, elemXO, dI);
 			}
 		}
 
 	}
 	private ExpFkXmlQueryElemXO findXO(String id, List<ExpFkXmlQueryElemXO> data)
 	{
 		for (ExpFkXmlQueryElemXO expFkXmlQueryElemXO : data) {
 			
 			if(expFkXmlQueryElemXO.getId().equals(id))
 			{
 				return expFkXmlQueryElemXO;
 			}
 		}
 		return null;
 	}
 	private void addSelectiveXO(ExpFKQuery query, ExpFkXmlQueryElemXO elemXO, DictItem dI)
 	{
 		String value = dI.getDaneDod1();
 		
 		if(elemXO.getSelect() != null)
 		{	
 			query.append(query.select, query.sSep);
 			addElem(query, query.select, elemXO.getSelect(), value);
 		}
 	}
 	private void addXO(ExpFKQuery query, ExpFkXmlQueryElemXO elemXO, DictItem dI)
 	{
 		String value = dI.getDaneDod1();
 		
 		if(elemXO.getFrom() != null)
 		{
 			query.addFromElements(elemXO.getFrom(), query.sep);
 		}
 		if(elemXO.getSelect() != null)
 		{	
 			query.append(query.select, query.sSep);
 			addElem(query, query.select, elemXO.getSelect(), value);
 		}
 		if(elemXO.getWhere() != null)
 		{	
 			query.append(query.where, query.wSep);
 			addElem(query, query.where, elemXO.getWhere(), value);
 		}
 		if(elemXO.getGroup() != null)
 		{
 			query.append(query.group, query.sep);
 			addElem(query, query.group, elemXO.getGroup(), value);
 		}
 		if(elemXO.getHaving() != null)
 		{
 			query.append(query.having, query.sep);
 			addElem(query, query.having, elemXO.getHaving(), value);
 		}
 		if(elemXO.getOrdered() != null)
 		{
 			query.append(query.ordered, query.sep);
 			addElem(query, query.ordered, elemXO.getOrdered(), value);
 		}
 	}
 	private void addElem(ExpFKQuery query, StringBuilder str, StrExpFkXO strXO, String value)
 	{
 		if(strXO.getSpecialStr() == "#")
 		{
 			value = query.sign;
 		}
 		
 		query.appendElem(str, strXO, value);
 	}
 //	private 
 	
 	private class ExpFKQuery
 	{
 		private final String ls = System.getProperty("line.separator");
 		private final String sep = " , ";
 		private final String sSep = " || ";
 		private final String wSep = "AND";
 		
 		private String sign;
 		private ExpFkXmlQueryElemXO root;
 		private List<ExpFkXmlQueryElemPartXO> fromElems;//unique elements
 		private List<ExpFkXmlQueryElemXO> elements;//unique elements
 		
 		private StringBuilder select;
 		private StringBuilder into;
 		private StringBuilder from;
 		private StringBuilder where;
 		private StringBuilder group;
 		private StringBuilder having; 
 		private StringBuilder ordered; 
 		
 		private StringBuilder result;
 
 		private ExpFKQuery(ExpFkXmlQueryElemXO root)
 		{
 		   this.root = root;
 		   elements = new ArrayList<ExpFkXmlQueryElemXO>();
 		   elements.add(root);
 		   
 		   select = new StringBuilder();
 		   appendElem(select, root.getSelect(), null);
 		  
 		   into = new StringBuilder();
 		   appendElem(into, root.getInto(), null);
 		   
 		   from = new StringBuilder();
 		   fromElems = new  ArrayList<ExpFkXmlQueryElemPartXO>();
 		   addFromElements(root.getFrom(), sep);
 		   
 		   where = new StringBuilder();
 		   appendElem(where, root.getWhere(), null);
 		   
 		   group = new StringBuilder();
 		   //appendElem(group, root.getGroup(), null);
 		   
 		   having = new StringBuilder();
 		   //appendElem(having, root.getHaving(), null);
 		   
 		   ordered = new StringBuilder();
 		   //appendElem(ordered, root.getOrdered(), null);
 		} 
 		
 		private void append(StringBuilder str, String data)
 		{
 			str.append(data);
 		}
 		private void appendElem(StringBuilder str, StrExpFkXO data, String value)
 		{
 			System.out.println( data.getValue());
 			String strValue = data.getValue();
 			
 			if(value != null)
 			{
 				strValue  = data.getValue().replaceAll(data.getSpecialStr(), value);
 			}
 				
 			str.append(strValue);
 		}
 		private void addFromElements(List<ExpFkXmlQueryElemPartXO> data, String separator)
 		{
 			for (ExpFkXmlQueryElemPartXO expFkXmlQueryElemPartXO : data)
 			{
 				if(!fromElems.contains(expFkXmlQueryElemPartXO))
 				{
 					fromElems.add(expFkXmlQueryElemPartXO);
 					
 					from.append(separator);
 					from.append(expFkXmlQueryElemPartXO.getStrData());
 				}
 			}
 		}	
 		public void compile()
 		{
 			result = new StringBuilder();
 			result.append(select);
 			result.append(ls);
			
 			result.append(into);
 			result.append(ls);
			
 			result.append("FROM");
 			result.append(from.substring(2));
 			result.append(ls);
			
 			result.append(where);
 			
 			if(group.length() > 0)
 			{
 				result.append(ls);
 				result.append(root.getGroup().getValue() +" "+ group.substring(2));
 			}
 			if(having.length() > 0)
 			{
 				result.append(ls);
 				result.append(root.getHaving().getValue() +" "+ having.substring(2));
 			}
 			if(ordered.length() > 0)
 			{
 				result.append(ls);
 				result.append(root.getOrdered().getValue() +" "+ ordered.substring(2));
 			}
 		}
 		@Override
 		public String toString()
 		{
 			compile();
 			return result.toString(); 
 		}
 	}
 
 }
