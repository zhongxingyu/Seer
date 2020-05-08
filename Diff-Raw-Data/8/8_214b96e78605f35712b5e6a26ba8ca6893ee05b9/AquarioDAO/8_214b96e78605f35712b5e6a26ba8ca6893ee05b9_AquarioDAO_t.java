 package dao;
 
import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import entidade.Aquario;
 
 public class AquarioDAO extends HibernateDaoSupport {
 	
 	public List<Aquario> buscarTodos(){
 		return getHibernateTemplate().loadAll(Aquario.class);
 	}
 	
 	public List<Aquario> buscarTodosResumido(){
 		
 		//List<Aquario> listTemp = getHibernateTemplate().loadAll(Aquario.class);
 		List<String> listTemp = getHibernateTemplate().find("select distinct o.dataMedicao from Aquario o");
 		
 		System.out.println(listTemp);
 		List<Aquario> aux1 = null;
 		List<Aquario> aux2 = null;
 		
 		if(listTemp==null || listTemp.size()<=0)
 			return null;
 		int count = 0;
 		float agua,tampa,amb;
 		int nivSump,nivRepo;
 		Aquario aquario = null;
 
 		aux2 = new ArrayList<Aquario>();
 		for(int i = 0; i < listTemp.size(); i++){
 			
 			aux1 = buscaData(listTemp.get(i));
 			agua = 0.0f; tampa = 0.0f; amb = 0.0f;
 			nivSump = 1000; nivRepo = 1000;
 			count = aux1.size();
 			for(int j = 0; j < aux1.size(); j++){
 				agua+=aux1.get(j).getTempAgua();
 				tampa+=aux1.get(j).getTempTampa();
 				amb+=aux1.get(j).getTempAmb();
 				if(nivSump > aux1.get(j).getNivelSump())
 					nivSump = aux1.get(j).getNivelSump();
 				if(nivRepo > aux1.get(j).getNivelRepo())
 					nivRepo = aux1.get(j).getNivelRepo();
 				
 			}
 			aquario = new Aquario();
 			aquario.setId(Long.decode(""+i));
 			aquario.setDataMedicao(listTemp.get(i));
 			aquario.setHoraMedicao("-");
 			aquario.setNivelRepo(nivRepo);
 			aquario.setNivelSump(nivSump);
			aquario.setTempAgua(Float.parseFloat((new DecimalFormat("0.00").format(agua/(float)count))));
			aquario.setTempAmb(Float.parseFloat((new DecimalFormat("0.00").format(amb/(float)count))));
			aquario.setTempTampa(Float.parseFloat((new DecimalFormat("0.00").format(tampa/(float)count))));
 			aux2.add(aquario);
 				
 		}
 		//System.out.println(aux2);
 		return aux2;
 	}
 	
 	public Aquario buscarPorId(Long id){
 		return getHibernateTemplate().get(Aquario.class, id);		
 	}
 	
 	public void gravar(Aquario projeto){
 		getHibernateTemplate().saveOrUpdate(projeto);
 	}
 	
 	public void excluir(Aquario projeto){
 		getHibernateTemplate().delete(projeto);
 	}
 	
 	public List<Aquario>buscaData(Aquario aquario){
 		return (List<Aquario>)getHibernateTemplate().find("FROM Aquario a WHERE a.dataMedicao = ?",aquario.getDataMedicao());
 	}
 	
 	public List<Aquario>buscaData(String dataMedicao){
 		return (List<Aquario>)getHibernateTemplate().find("FROM Aquario a WHERE a.dataMedicao = ?",dataMedicao);
 	}
 	
 		
 }
