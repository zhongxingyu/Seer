 package br.com.unifacs.view;
 
 import java.io.Serializable;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
 
 import org.primefaces.model.chart.CartesianChartModel;
 import org.primefaces.model.chart.ChartSeries;
 
 import br.com.unifacs.bo.LancamentoBo;
 import br.com.unifacs.bo.LancamentoBoImpl;
 import br.com.unifacs.dao.CustomQueryDao;
 import br.com.unifacs.dao.DaoException;
 
 @ManagedBean(name="graficoMb")
@ViewScoped
 public class GraficoDespesaReceitaMb implements Serializable {
 
 	private CartesianChartModel categoryModel;
 	private LancamentoBo bo;
 	private String ano;
 	
 	public GraficoDespesaReceitaMb() {
 		bo = new LancamentoBoImpl();
 		criarGrafico();
 	}
 	
 	private void criarGrafico(){
 		
 		categoryModel = new CartesianChartModel();
 		
 		ChartSeries receita = new ChartSeries();
 		receita.setLabel("Receitas");
 		ChartSeries despesa = new ChartSeries();
 		despesa.setLabel("Despesas");
 		
 		Object[] receitas = null;
 		Object[] despesas = null;
 		
 		try {
 			receitas = CustomQueryDao.getTotalReceitaAnual(2012);
 		} catch (DaoException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		try {
 			despesas = CustomQueryDao.getTotalDespesaAnual(2012);
 		} catch (DaoException e) {
 			e.printStackTrace();
 			return;
 		}		
 		
 		receita.set("jan", (Number) receitas[0]);
 		receita.set("fev", (Number) receitas[1]);
 		receita.set("mar", (Number) receitas[2]);
 		receita.set("abr", (Number) receitas[3]);
 		receita.set("mai", (Number) receitas[4]);
 		receita.set("jun", (Number) receitas[5]);
 		receita.set("jul", (Number) receitas[6]);
 		receita.set("ago", (Number) receitas[7]);
 		receita.set("set", (Number) receitas[8]);
 		receita.set("out", (Number) receitas[9]);
 		receita.set("nov", (Number) receitas[10]);
 		receita.set("dez", (Number) receitas[11]);
 		
 		despesa.set("jan", (Number) despesas[0]);
 		despesa.set("fev", (Number) despesas[1]);
 		despesa.set("mar", (Number) despesas[2]);
 		despesa.set("abr", (Number) despesas[3]);
 		despesa.set("mai", (Number) despesas[4]);
 		despesa.set("jun", (Number) despesas[5]);
 		despesa.set("jul", (Number) despesas[6]);
 		despesa.set("ago", (Number) despesas[7]);
 		despesa.set("set", (Number) despesas[8]);
 		despesa.set("out", (Number) despesas[9]);
 		despesa.set("nov", (Number) despesas[10]);
 		despesa.set("dez", (Number) despesas[11]);
 		/*for(Lancamento l:lancamentos){
 			if(l.getDespesa().equals("S")){
 				despesa.set(l.getDataPgto().getMonth(), l.getValorPgto());
 			} else {
 				receita.set(l.getDataPgto().getMonth(), l.getValorPgto());
 			}
 		}*/
 		
 		categoryModel.addSeries(receita);
 		categoryModel.addSeries(despesa);	
 			
 	}
 	
 	public CartesianChartModel getCategoryModel() {
 		return categoryModel;
 	}
 	public void setCategoryModel(CartesianChartModel categoryModel) {
 		this.categoryModel = categoryModel;
 	}
 
 	public String getAno() {
 		return ano;
 	}
 
 	public void setAno(String ano) {
 		this.ano = ano;
 	}
 	
 	
 
 }
