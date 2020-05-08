 package logic;
 import java.util.ArrayList;
 import java.util.List;
 public class Knowledge {
 	private List<Clause> lista;
 	public List<Clause> getList()
 	{
 		return lista;
 	}
 	public void setClause(Clause n_clause)
 	{
 		lista.add(n_clause);
 	}
 	public List<Clause> getResult(Literal lit)
 	{
		List<Clause> wnioski=new ArrayList();
 		for(int i=0;i<lista.size();i++)
 		{
 			wnioski.add(lista.get(i).getClause(lit));
 		}
 		if(wnioski.size()==0)
 		{
			wnioski.add(new Clause().emptyClause());
 		}
 		return wnioski;
 	}
 
 
 }
