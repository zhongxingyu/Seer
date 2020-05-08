 package controllers;
 
 import static play.data.Form.form;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.persistence.EntityExistsException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import models.Comissao;
 import models.DeputadoComissao;
 import models.DeputadoFederal;
 import models.DeputadoFederalExercicio;
 import models.DeputadoFederalGasto;
 import models.Empresa;
 import models.ProjetoDeLei;
 import models.TotalData;
 import models.TotalTipo;
 import models.util.ComissaoSimple;
 import models.util.WordFrequency;
 import play.data.DynamicForm;
 import play.db.jpa.JPA;
 import play.db.jpa.Transactional;
 import play.libs.WS;
 import play.libs.F.Promise;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Deputados extends Controller {
 
 	public static final String hostCamaraFederal = "http://www.camara.gov.br";
 	public static final String pathObterDeputados = "/SitCamaraWS/Deputados.asmx/ObterDeputados";
 	// http://www.camara.gov.br/SitCamaraWS/Deputados.asmx/ObterDetalhesDeputados?ideCadastro=XXXX&numLegislatura=
 	public static final String pathObterDeputadosDetails = "/SitCamaraWS/Deputados.asmx/ObterDetalhesDeputado";
 	
 	public static final String pathObterDeputadosGastos = "./data/AnoAtual.xml";
 	
 	private static final Long timeout = 20000L;
 	
 	@Transactional
     public static Result getAllData() {
 		
 		try {
 			getProfileData();
 			JPA.em().getTransaction().commit();
 			
 			JPA.em().getTransaction().begin();
 			getExpensesData();
 			JPA.em().getTransaction().commit();
 			
 			JPA.em().getTransaction().begin();
 			getTotalsData();
 			JPA.em().getTransaction().commit();
 
 			JPA.em().getTransaction().begin();
 			processProjetosDeLei();
 			JPA.em().getTransaction().commit();
 
 			JPA.em().getTransaction().begin();
 			processCompaniesData();
 			
 			return ok("Completed. See log to details.");
 		} catch (Exception e) {
 			e.printStackTrace();
 			return badRequest(e.getLocalizedMessage());
 		}
 	}
 
 	/**
      * Uses SAX instead of DOM because file is huge.
      * @return
      */
     public static void getProfileData() {
     	String url = hostCamaraFederal + pathObterDeputados;
     	ArrayList<DeputadoFederal> deputados = new ArrayList<DeputadoFederal>();
     	
     	try {
     	
     		Promise<WS.Response> result = WS.url(url).get();
     		InputStream is = new ByteArrayInputStream(result.get(timeout).asByteArray());
     		
     		System.out.println("Processando deputados...");
     		
     		SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 		
 			DeputadoXMLHandler dhandler = new DeputadoXMLHandler(deputados);
 			saxParser.parse(is, dhandler);
 			System.out.println("Processando detalhes dos deputados...");
 			
 			//For each deputado, get details and insert
 			for(DeputadoFederal df: deputados){
 				try {
 					//System.out.println("Registrando detalhes de " + df.getNome());
 					getDetails(df);
 					JPA.em().persist(df);
 				} catch (Exception e) {
 					System.out.println("Erro pegando detalhes de " + df.getNome() + ": " + e.getMessage());
 					e.printStackTrace();
 				}
 			}
 			
 			System.out.println("Sucesso processando detalhes dos deputados!");
 			
 		} catch (ParserConfigurationException e) {
 			//e.printStackTrace();
 			System.out.println("Error of response parse. " + e.getMessage());
 		} catch (SAXException e) {
 			//e.printStackTrace();
 			System.out.println("Error reading xml response. " + e.getMessage());
 		} catch (IOException e) {
 			//e.printStackTrace();
 			System.out.println("Error saving Deputados's data. " + e.getMessage());
 		}
     }
 	
     private static void getDetails(DeputadoFederal deputado) throws Exception {
     	String url = hostCamaraFederal + pathObterDeputadosDetails;
        	
     	//Auxiliary variables
     	String sDate;
     	Date date;
     	
     	Promise<WS.Response> result = 
     			WS.url(url)
     			.setQueryParameter("ideCadastro", deputado.getIdeCadastro())
     			.setQueryParameter("numLegislatura", " ")
     			.get();
     	
     	Document doc = result.get(timeout).asXml();
     	doc.getDocumentElement().normalize();
     	
     	NodeList nDepList = doc.getElementsByTagName("Deputado");
     	//Sometimes it returns 2 nodes. I dont know why
     	Node depNode = nDepList.item(0);
     	Element depElement = (Element) depNode;
     	
     	//Get Generic Data
     	deputado.setProfissao(depElement.getElementsByTagName("nomeProfissao").item(0).getTextContent());
     	sDate = depElement.getElementsByTagName("dataNascimento").item(0).getTextContent();
     	date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(sDate);
     	deputado.setNascimento(date);
     	
     	
     	//Save Periodo de Exercicio
     	NodeList exList = depElement.getElementsByTagName("periodoExercicio");
     	
     	for (int i = 0; i < exList.getLength(); i++) {
     	
     		Node exNode = exList.item(i);
     		Element exElement = (Element) exNode;
     		
     		DeputadoFederalExercicio exercicio = new DeputadoFederalExercicio(); 
         	exercicio.setIdeCadastro(deputado.getIdeCadastro());
         	//exercicio.setIdHistorico(exElement.getElementsByTagName("idHistoricoExercicioParlamentar").item(0).getTextContent());
         	exercicio.setSituacao(exElement.getElementsByTagName("situacaoExercicio").item(0).getTextContent());
         	
         	sDate = exElement.getElementsByTagName("dataInicio").item(0).getTextContent();
         	date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(sDate);
         	exercicio.setDataInicio(date);
         	
         	sDate = exElement.getElementsByTagName("dataFim").item(0).getTextContent();
         	if (sDate != null && !sDate.trim().equals("")) {
         		date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(sDate);
         		exercicio.setDataFim(date);
         		exercicio.setDescricaoFim(exElement.getElementsByTagName("descricaoCausaFimExercicio").item(0).getTextContent());
         	}
         	
         	// insert exercicio
         	JPA.em().persist(exercicio);
     	}
     	
     	//Save comissoes
     	NodeList comList = depElement.getElementsByTagName("comissao");
     	Node comNode;
     	Element comElement;
     	String nome, sigla;
     	Long orgao;
     	for (int i = 0; i < comList.getLength(); i++) {
     		comNode = comList.item(i);
     		comElement = (Element) comNode;
     		
     		orgao = Long.parseLong(comElement.getElementsByTagName("idOrgaoLegislativoCD").item(0).getTextContent());
     		sigla = comElement.getElementsByTagName("siglaComissao").item(0).getTextContent();
     		nome = comElement.getElementsByTagName("nomeComissao").item(0).getTextContent();
     		Comissao comissao = new Comissao();
     		comissao.setNome(nome);
     		comissao.setSigla(sigla);
     		comissao.setOrgao(orgao);
     		
     		//TODO UGE problem. I gave up and I insert data duplicated on DB
     		//TODO: checar antes se já não existe
     		try {
     			if (!JPA.em().contains(comissao)){
     				JPA.em().persist(comissao);
     			}
     		} catch (EntityExistsException e){
     			System.out.print(".");
     		}
     		
     		DeputadoComissao depCom = new DeputadoComissao();
     		depCom.setSiglaComissao(sigla);
     		depCom.setIdeCadastroDeputado(deputado.getIdeCadastro());
     		//depCom.setDeputadoComissao(deputado.getIdeCadastro()+"#"+sigla);
     		
     		//TODO: checar antes se já não existe
    			//if (!JPA.em().contains(depCom)){ --> não funciona
     		try {
     			if (!JPA.em().contains(depCom)){
     				JPA.em().persist(depCom);
     			}
     		} catch (EntityExistsException e){
     			System.out.print("-");
     			//do nothing
     			//a questão é que o mesmo deputado pode ter participado de uma comissão mais de uma vez
     		}
     	}
     	
 	}
     
     @Transactional
     public static Result startProcessProjetosDeLei(){   	
     	int erros = processProjetosDeLei();
     	return ok("Projetos processados com " + erros + " erros.");
     	
     }
     
 	private static int processProjetosDeLei() {
 		String url = "http://www.camara.gov.br/SitCamaraWS/Proposicoes.asmx/ListarProposicoes";
 		
 		System.out.println("Processando projetos de lei.");
 		
 		String[] tipoLei = {"PL", "PEC"};
 		
 		int erros = 0;
 		
 		for (int ano = 2011; ano <=2013; ano++){
 			for (int i = 0; i < tipoLei.length; i++){
 				try {
 	
 					Promise<WS.Response> result = 
 		    			WS.url(url)
 		    			.setQueryParameter("sigla", tipoLei[i])
 		    			.setQueryParameter("numero", " ")
 		    			.setQueryParameter("ano", ano +"")
 		    			.setQueryParameter("datApresentacaoIni", " ")
 		    			.setQueryParameter("datApresentacaoFim", " ")
 		    			.setQueryParameter("autor", " ")
 		    			.setQueryParameter("parteNomeAutor", " ")
 		    			.setQueryParameter("siglaPartidoAutor", " ")
 		    			.setQueryParameter("siglaUFAutor", " ")
 		    			.setQueryParameter("generoAutor", " ")
 		    			.setQueryParameter("codEstado", " ")
 		    			.setQueryParameter("codOrgaoEstado", " ")
 		    			.setQueryParameter("emTramitacao", " ")
 		    			.get();
 				
 					// Quando não acha, retorna "proposição não encontrada". Vou lançar e pegar uma exceção
 		    		try {
 						InputStream is = new ByteArrayInputStream(result.get(timeout).asByteArray());
 			    		
 			    		SAXParserFactory factory = SAXParserFactory.newInstance();
 						SAXParser saxParser = factory.newSAXParser();
 					
 						List<ProjetoDeLei> projetosDeLei = new ArrayList<ProjetoDeLei>();
 						
 						ProjetoDeLeiXMLHandlerBack dhandler = new ProjetoDeLeiXMLHandlerBack(projetosDeLei);
 						saxParser.parse(is, dhandler);
 						
 						System.out.println(ano + ": " + projetosDeLei.size());
 						for(ProjetoDeLei pdl: projetosDeLei){
 							try {
 								//System.out.println("Registrando detalhes de " + df.getNome());
 								JPA.em().persist(pdl);
 							} catch (Exception e) {
 								System.out.println("Erro salvando projeto de lei " + pdl.getNome() + " de " + pdl.getNomeAutor() + ": " + e.getMessage());
 								erros++;
 							}
 						}
 		    		} catch (Exception e){
 		    			System.out.println("Erro obtendo projetos de lei 1." + e.getMessage());
 		    		}
 				} catch (Exception e){
 					System.out.println("Erro obtendo projetos de lei de lei 2." + e.getMessage());
 					e.printStackTrace();
 					erros++;
 				}
 			}
 		}
 		System.out.println("Projetos de lei processados com " + erros + " erros.");
 		
 		return erros;
 		
 	}
 
 	/**
      * Uses SAX instead of DOM because file is huge.
      * @return
      */
     public static void getExpensesData() {
     	System.out.println("Processando despesas dos deputados...");
     	
     	try {
     		//charset é 'cp852'
     		InputStream is = new FileInputStream(pathObterDeputadosGastos);
     		
     		SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 		
 			GastoDeputadoXMLHandler dhandler = new GastoDeputadoXMLHandler();
 			saxParser.parse(is, dhandler);			
 			System.out.println("Sucesso processando despesas dos deputados!");
 		} catch (ParserConfigurationException e) {
 			//e.printStackTrace();
 			System.out.println("Error of response parse.");
 		} catch (SAXException e) {
 			//e.printStackTrace();
 			System.out.println("Error reading xml response.");
 		} catch (IOException e) {
 			//e.printStackTrace();
 			System.out.println("Error reading file data.");
 		}
 		
 		//return ok ("Sucess");
     }
     
 	/**
 	 * Dados de 2012 e 2013
 	 * @return
 	 */
 	@Transactional
 	public static void getTotalsData() {
 		
 		System.out.println("Processando totais dos Deputados...");
 		
 		//Pega o último dia com registro
 		String mostRecent = (String)JPA.em()
 				.createNativeQuery("Select max(datEmissao) FROM DeputadoFederalGasto WHERE datEmissao < NOW()").getSingleResult();
 		Date mostRecentDate = null;
 		Date olderDate = null;
 		
 		try {
 			mostRecentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(mostRecent);
 			olderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2013-01-01");
 		} catch (ParseException e) {
 			System.out.println("Error getting mostRecentDate. " + e.getLocalizedMessage());
 		}
 		
 		List<DeputadoFederal> depList = JPA.em().createQuery("FROM DeputadoFederal").getResultList();
 		
 		/**
 		 * Salva o gasto medio por dia
 		 */
 		
 		for (DeputadoFederal dep: depList){
 			System.out.println("Calculando gasto de " + dep.getNomeParlamentar());
 			int days = 0;
 			Date dataIni, dataFim;
 			
 			/** TODO Repensar e refazer essa conta do exercício DIREITO */
 			/* List<DeputadoFederalExercicio> exeList =
 					JPA.em().createQuery(" FROM DeputadoFederalExercicio e where e.ideCadastro = :id")
 					.setParameter("id", dep.getIdeCadastro())
 					.getResultList();
 			
 			for (DeputadoFederalExercicio exe: exeList){
 				dataFim = exe.getDataFim();
 				dataIni = exe.getDataInicio();
 				
 				if (dataFim == null) dataFim = mostRecentDate;
 				if (dataFim.getTime() < olderDate.getTime()) continue;
 				
 				if (dataIni.getTime() < olderDate.getTime()) dataIni = olderDate;
 				if (dataIni.getTime() > mostRecentDate.getTime()) continue;
 				
 				days += Days.daysBetween(new DateTime(dataIni), new DateTime(dataFim)).getDays();
 				
 			}*/
 			
 			days = Days.daysBetween(new DateTime(olderDate), new DateTime(mostRecentDate)).getDays();
 			dep.setDiasTrabalhados(days);
 			
 			/////////////////////////
 			Object o = JPA.em()
 					.createNativeQuery("select sum(vlrDocumento) as TotalGasto from DeputadoFederalGasto g where g.nuCarteiraParlamentar = :id")
 					.setParameter("id", dep.getMatricula())
 					.getSingleResult();
 			
 			if (o != null){
 				Double gasto = (Double)o; 
 				dep.setGastoTotal(gasto);
 			
 				dep.setGastoPorDia(gasto/days);
 			}
 			JPA.em().persist(dep);
 		}
 	
 		JPA.em().getTransaction().commit();
 		JPA.em().getTransaction().begin();
 		
 		/**
 		 * Calcula o índice do candidato:
 		 * 100*gasto/max
 		 * Ou seja: o candidato que gastou MAIS tem valor 100, o candidato que gastou NADA tem valor 0 
 		 */
 		
 		Double maxGasto = (Double)JPA.em().createNativeQuery("Select max(gastoPorDia) FROM DeputadoFederal").getSingleResult();
 		Double gasto, depTempIndice;
 	
 		for (DeputadoFederal dep: depList){
 			gasto = dep.getGastoPorDia();
 			
 			depTempIndice = (100 * gasto / maxGasto);
 			
 			dep.setIndice(depTempIndice.intValue());
 			
 			JPA.em().persist(dep);
 		}
 		
 		System.out.println("Sucesso processando totais dos Deputados!");
 	}
 	
 	@Transactional
 	public static Result startProcessCompaniesData(){
 		try {
 			processCompaniesData();
 			return ok("ok. Empresas processadas com sucesso"); 
 		} catch (Exception e) {
 			return badRequest("Erro processando empresas: " + e.getMessage());
 		}
 	}
 	
     private static void processCompaniesData() throws Exception{
     	String query = "select txtCNPJCPF, txtDescricaoEspecificacao, sum(vlrDocumento)" +
     			" from deputadofederalgasto" +
     			" group by txtCNPJCPF" +
     			" order by 3 desc";
     	
     	List<Object> resultList = JPA.em().createNativeQuery(query).getResultList();
 		
 		//List<Empresa> empresas = new LinkedList<Empresa>();
 		Empresa emp;
 		
 		System.out.println("Processando " + resultList.size() + " empresas.");
 		int problemas = 0;
 		int corretos = 0;
 		for (Object result : resultList) {
 			emp = new Empresa();
 			
 		    Object[] items = (Object[]) result;
		    try {
		    	emp.setCnpj((String)items[0]);
		    } catch (Exception e){
		    	//se não tem cnpj, passa batido
		    	continue;
		    }
 		    emp.setNome((String)items[1]);
 		    emp.setFantasia((String)items[1]);
 		    emp.setTotalRecebido((Double)items[2]);
 		    emp.setDadosAtualizados(false);
 		    
 		    //System.out.println(emp);
 		    if ( emp.getCnpj() != null && emp.getCnpj().trim().length() > 5){
 		    	JPA.em().persist(emp);
 		    	corretos++;
 		    } else {
 		    	problemas++;
 		    }
 		    //empresas.add(emp);
 		} 
 		
 		System.out.println("Processadas " + corretos + " empresas. " + problemas + " não tinham CNPJ/CPF e não foram salvas");
 		
 	}
 	
 	@Transactional
 	public static List<DeputadoFederal> getMelhores(){
 		List<DeputadoFederal> depMelhores = JPA.em()
     			.createQuery("FROM DeputadoFederal ORDER BY gastopordia ASC")
     			.setMaxResults(5)
     			.getResultList();
 		return depMelhores;
 	}
 	
 	@Transactional
 	public static List<DeputadoFederal> getPiores(){
 		List<DeputadoFederal> depMelhores = JPA.em()
     			.createQuery("FROM DeputadoFederal ORDER BY gastopordia DESC")
     			.setMaxResults(5)
     			.getResultList();
 		return depMelhores;
 	}
 	
 	public static List<ProjetoDeLei> getProjetosDeLei(DeputadoFederal deputado){		
 		List<ProjetoDeLei> projetos = JPA.em()
 				.createQuery("FROM ProjetoDeLei WHERE cadastroDeputado = :id ORDER BY ano DESC")
 				.setParameter("id", deputado.getIdeCadastro())
 				.getResultList();
 		return projetos;
 	}
 	
     @Transactional
     public static Result deputados() {
     	List<DeputadoFederal> depList = JPA.em().createQuery("FROM DeputadoFederal ORDER BY nomeParlamentar").getResultList();
 
     	// TODO Probably get once and then get 5 best and worst is faster
     	List<DeputadoFederal> depMelhores = JPA.em().createQuery("FROM DeputadoFederal ORDER BY gastopordia asc").setMaxResults(5).getResultList();
     	
     	List<DeputadoFederal> depPiores = JPA.em().createQuery("FROM DeputadoFederal ORDER BY gastopordia desc").setMaxResults(5).getResultList();
     	
     	return ok(views.html.depfederal.render(depList, depMelhores, depPiores, null, null, null));
     }
 	
 	@Transactional
     public static Result show(String id) {		
 		DeputadoFederal deputado;
 		List<TotalTipo> totalTipo;
 		List<TotalData> totalData;
 		List<Object> resultList;
 		
 		//Traz o deputado
 		try {
 			deputado = (DeputadoFederal)JPA.em().createQuery("FROM DeputadoFederal where ideCadastro = :id")
 					.setParameter("id", id).getSingleResult();
 		} catch (Exception e){
 			e.printStackTrace();
 			return badRequest("Error getting Deputado  " + id + ". Message: " + e.getMessage() + " - " + e.getLocalizedMessage());
 		}
 		
 		//traz as comissões
 				
 		
 		//Traz os resultados dos gastos agrupados por tipo de gasto
 		try {
 			// urzeni (o mais ladrao) eh 616
 			String nuCarteiraParlamentar = deputado.getMatricula();
 		
 			String query = 
 					"select txtCNPJCPF, txtDescricao, txtDescricaoEspecificacao," + 
 					" sum(vlrDocumento) as TotalGasto" +
 					" from deputadofederalgasto" + 
 					" where nuCarteiraParlamentar = :id" + 
 					" group by txtCNPJCPF order by 2,3,4 desc";
 			
 			resultList = JPA.em().createNativeQuery(query)
 					.setParameter("id", nuCarteiraParlamentar).getResultList();
 			
 			totalTipo = new LinkedList<TotalTipo>();
 			TotalTipo tt;
 			
 			for (Object result : resultList) {
 				tt = new TotalTipo();
 				
 			    Object[] items = (Object[]) result;
 			    tt.setCnpj((String)items[0]);
 			    tt.setDescricao((String)items[1]);
 			    tt.setDetalhe((String)items[2]);
 			    tt.setTotalGasto((Double)items[3]);
 			    
 			    totalTipo.add(tt);
 			}  
 			
 		} catch (Exception e){
 			e.printStackTrace();
 			return badRequest("Error getting Deputado  " + id + ". Message: " + e.getMessage() + " - " + e.getLocalizedMessage());
 		}
 		
 		//Traz os resultados por data
 		try {
 			String nuCarteiraParlamentar = deputado.getMatricula();
 		
 			String query = 
 					" select numAno, numMes," +
 					" sum(vlrDocumento) as TotalGasto" +
 					" from deputadofederalgasto" +
 					" where nuCarteiraParlamentar = :id" +
 					" group by numAno,numMes order by 1,2";
 			
 			resultList = JPA.em().createNativeQuery(query)
 					.setParameter("id", nuCarteiraParlamentar).getResultList();
 			
 			totalData = new LinkedList<TotalData>();
 			TotalData td;
 			
 			for (Object result : resultList) {
 				td = new TotalData();
 				
 			    Object[] items = (Object[]) result;    
 			    td.setAno((Integer)items[0]);
 			    td.setMes((Integer)items[1]);
 			    td.setTotalGasto((Double)items[2]);
 			    
 			    totalData.add(td);
 			} 
 			
 		} catch (Exception e){
 			e.printStackTrace();
 			return badRequest("Error getting Deputado  " + id + ". Message: " + e.getMessage() + " - " + e.getLocalizedMessage());
 		}
     	
 		List<WordFrequency> words = getWordFrequency(deputado, 30);
 		List<ComissaoSimple> comissoes = getComissoes(deputado);
 		List<ProjetoDeLei> projetos = getProjetosDeLei(deputado);
 		
 		//return ok(views.html.depfederaldetalhe.render(deputado, totalTipo, totalData));
     	return ok(views.html.depfederaldetalhe.render(words, deputado, comissoes, projetos, totalTipo, totalData));
     }
 	
 	private static List<ComissaoSimple> getComissoes(DeputadoFederal deputado) {
 		String query = 
 				"select distinct sigla, nome " +
 				"from deputadocomissao, comissao " +	
 				"where comissao.sigla = deputadocomissao.siglacomissao " +
 				"and ideCadastroDeputado = :id";
 		
 		List<Object> resultList = JPA.em().createNativeQuery(query)
 			.setParameter("id", deputado.getIdeCadastro()).getResultList();
 		
 		LinkedList<ComissaoSimple> comissoes = new LinkedList<ComissaoSimple>();
 		
 		Object[] items;
 		ComissaoSimple comissao;
 		for (Object result : resultList) {
 		    items = (Object[]) result;
 		    comissao = new ComissaoSimple();
 		    comissao.setSigla((String)items[0]);
 		    comissao.setNome((String)items[1]);
 		    comissoes.add(comissao);
 		}
 		return comissoes;
 	}
 
 	public static List<WordFrequency> getWordFrequency(DeputadoFederal deputado, int numberWords){
 		
 		List<WordFrequency> finalList = new ArrayList<WordFrequency>();
 		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
 		
 		List<String> texts = getAllComissoesText(deputado);
 		
 		//put words on map
 		for (String text: texts){
 			String[] words = text.toLowerCase()
 					.replace("(", " ")
 					.replace(")", " ")
 					.replace(".", " ")
 					.replace(",", " ")
 					.split("[-!~\\s]+");
 			//str1.split("[\\W]+");  --> another options. Split on non letters
 			for (String word : words) {
 			    Integer count = wordCounts.get(word);
 			    if (count == null) {
 			        count = 0;
 			    }
 			    if ( word.length() > 2 ) { //only words with +2 letters
 			    	wordCounts.put(word, count + 1);
 			    }
 			}
 		}
 		
 		//clean map
 		removeUnusefulWords(wordCounts);
 		
 		//sort
 		//TODO I dont need a map. Only sort the keys
 		/*TreeSet<WordFrequency> wordsOrdered = new TreeSet<WordFrequency>();
 		long qtd; String word;
 		for (String entry : wordCounts.keySet()) {
 			WordFrequency wf = new WordFrequency();
 			wf.occurrences = wordCounts.get(entry);
 		    wf.word = entry;
 		    wordsOrdered.add(wf);
 		}*/
 		
 		List<WordFrequency> wordsOrdered = new ArrayList<WordFrequency>();
 		for (String entry : wordCounts.keySet()) {
 			WordFrequency wf = new WordFrequency();
 			wf.occurrences = wordCounts.get(entry);
 		    wf.word = entry;
 		    wordsOrdered.add(wf);
 		}
 		
 		Collections.sort(wordsOrdered);
 		
 		//Get the amount requested
 		int wordCount = 0;
 		for (WordFrequency wf: wordsOrdered){
 			finalList.add(wf);
 			if (++wordCount >= numberWords) break;
 		}
 		
 		return finalList;
 	}
 
 	/**
 	 * TODO: Ficar criando o hashset toda hora é custoso
 	 * Talvez seja melhor fazer a verificação só no final mesmo
 	 * @param word
 	 * @return
 	 */
 	private static void removeUnusefulWords(Map<String, Integer> words) {
 		List<String> unusefulWords = new ArrayList<String>();
 		unusefulWords.add("comissão");
 		unusefulWords.add("sobre");
 		unusefulWords.add("dos");
 		unusefulWords.add("das");
 		unusefulWords.add("que");
 		unusefulWords.add("qual");
 		unusefulWords.add("quais");
 		unusefulWords.add("por");
 		unusefulWords.add("para");
 		unusefulWords.add("aos");
 		
 		for (String unuseful: unusefulWords){
 			words.remove(unuseful);
 		}
 		
 	}
 
 	private static List<String> getAllComissoesText(DeputadoFederal deputado) {
 		
 		String query = 
 				"select distinct sigla, nome " +
 				"from comissao, deputadocomissao " +
 				"where comissao.sigla = deputadocomissao.siglacomissao " +
 				"and idecadastrodeputado = :id";
 		List<Object> resultList = JPA.em().createNativeQuery(query)
 			.setParameter("id", deputado.getIdeCadastro()).getResultList();
 		
 		LinkedList<String> strings = new LinkedList<String>();
 		
 		Object[] items;
 		for (Object result : resultList) {
 		    items = (Object[]) result;
 		    strings.add((String)items[0]);
 		    strings.add((String)items[1]);
 		}
 		return strings;
 	}
 
 }
 
 class DeputadoXMLHandler extends DefaultHandler {
 	private boolean bIdeCadastro;
 	private boolean bMatricula;
 	private boolean bIdParlamentar;
 	private boolean bNome;
 	private boolean bNomeParlamentar;
 	private boolean bSexo;
 	private boolean bUf;
 	private boolean bPartido;
 	private boolean bFone;
 	private boolean bEmail;
 	
 	ArrayList<DeputadoFederal> deputados;
 	DeputadoFederal deputado;
 	
 	public DeputadoXMLHandler(ArrayList<DeputadoFederal> deputados) {
 		this.deputados = deputados;
 	}
 
 	public void startElement(String uri, String localName,String qName, 
             Attributes attributes) throws SAXException {
 		
 		if (qName.equalsIgnoreCase("ideCadastro")) {
 			bIdeCadastro = true;
 		} else if (qName.equalsIgnoreCase("matricula")) {
 			bMatricula = true;
 		} else if (qName.equalsIgnoreCase("idParlamentar")) {
 			bIdParlamentar = true;
 		} else if (qName.equalsIgnoreCase("nome")) {
 			bNome = true;
 		} else if (qName.equalsIgnoreCase("nomeParlamentar")) {
 			bNomeParlamentar = true;
 		} else if (qName.equalsIgnoreCase("sexo")) {
 			bSexo = true;
 		} else if (qName.equalsIgnoreCase("uf")) {
 			bUf = true;
 		} else if (qName.equalsIgnoreCase("partido")) {
 			bPartido = true;
 		} else if (qName.equalsIgnoreCase("fone")) {
 			bFone = true;
 		} else if (qName.equalsIgnoreCase("email")) {
 			bEmail = true;
 		}
 	}
 	
 	public void endElement(String uri, String localName,
 			String qName) throws SAXException {
 	}
 	
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (bIdeCadastro) {
 			//Começando novo deputado
 			deputado = new DeputadoFederal();
 			String s = new String(ch, start, length);
 			deputado.setIdeCadastro(s);
 			bIdeCadastro = false;
 		}
 		if (bMatricula) {
 			String s = new String(ch, start, length);
 			deputado.setMatricula(s);
 			bMatricula = false;
 		}
 		if (bIdParlamentar) {
 			String s = new String(ch, start, length);
 			deputado.setIdParlamentar(s);
 			bIdParlamentar = false;
 		}
 		if (bNome) {
 			String s = new String(ch, start, length);
 			deputado.setNome(s);
 			bNome = false;
 		}
 		if (bNomeParlamentar) {
 			String s = new String(ch, start, length);
 			deputado.setNomeParlamentar(s);
 			bNomeParlamentar = false;
 		}
 		if (bSexo) {
 			String s = new String(ch, start, length);
 			deputado.setSexo(s);
 			bSexo = false;
 		}
 		if (bUf) {
 			String s = new String(ch, start, length);
 			deputado.setUf(s);
 			bUf = false;
 		}
 		if (bPartido) {
 			String s = new String(ch, start, length);
 			deputado.setPartido(s);
 			bPartido = false;
 		}
 		if (bFone) {
 			String s = new String(ch, start, length);
 			deputado.setFone(s);
 			bFone = false;
 		}
 		if (bEmail) {
 			String s = new String(ch, start, length);
 			deputado.setEmail(s);
 			bEmail = false;
 			
 			//Acabou o deputado
 			deputados.add(deputado);
 			deputado = new DeputadoFederal();
 			
 		}		
  
 	}
 }
 	
 class GastoDeputadoXMLHandler extends DefaultHandler {
 	private boolean bTxNomeParlamentar;
 	private boolean bNuCarteiraParlamentar;
 	private boolean bNuLegislatura;
 	private boolean bSgUF;
 	private boolean bSgPartido;
 	private boolean bCodLegislatura;
 	private boolean bNumSubCota;
 	private boolean bTxtDescricao;
 	private boolean bNumEspecificacaoSubCota;
 	private boolean bTxtDescricaoEspecificacao;
 	private boolean bTxtFornecedor;
 	private boolean bTxtCNPJCPF;
 	private boolean bTxtNumero;
 	private boolean bIndTipoDocumento;
 	private boolean bDatEmissao;
 	private boolean bVlrDocumento;
 	private boolean bVlrGlosa;
 	private boolean bVlrLiquido;
 	private boolean bNumMes;
 	private boolean bNumAno;
 	private boolean bNumParcela;
 	private boolean bTxtPassageiro;
 	private boolean bTxtTrecho;
 	private boolean bNumLote;
 	private boolean bNumRessarcimento;
 	
 	DeputadoFederalGasto gasto;
 	int count = 0;
 
 	public void startElement(String uri, String localName,String qName, 
             Attributes attributes) throws SAXException {
 		
 		if (qName.equalsIgnoreCase("txNomeParlamentar")) {
 			bTxNomeParlamentar = true;
 		} else if (qName.equalsIgnoreCase("nuCarteiraParlamentar")) {
 			bNuCarteiraParlamentar = true;
 		} else if (qName.equalsIgnoreCase("nuLegislatura")) {
 			bNuLegislatura = true;
 		} else if (qName.equalsIgnoreCase("sgUF")) {
 			bSgUF = true;
 		} else if (qName.equalsIgnoreCase("sgPartido")) {
 			bSgPartido = true;
 		} else if (qName.equalsIgnoreCase("codLegislatura")) {
 			bCodLegislatura = true;
 		} else if (qName.equalsIgnoreCase("numSubCota")) {
 			bNumSubCota = true;
 		} else if (qName.equalsIgnoreCase("txtDescricao")) {
 			bTxtDescricao = true;
 		} else if (qName.equalsIgnoreCase("numEspecificacaoSubCota")) {
 			bNumEspecificacaoSubCota = true;
 		} else if (qName.equalsIgnoreCase("txtDescricaoEspecificacao")) {
 			bTxtDescricaoEspecificacao = true;
 		} else if (qName.equalsIgnoreCase("txtFornecedor")) {
 			bTxtFornecedor = true;
 		} else if (qName.equalsIgnoreCase("txtCNPJCPF")) {
 			bTxtCNPJCPF = true;
 		} else if (qName.equalsIgnoreCase("txtNumero")) {
 			bTxtNumero = true;
 		} else if (qName.equalsIgnoreCase("indTipoDocumento")) {
 			bIndTipoDocumento = true;
 		} else if (qName.equalsIgnoreCase("datEmissao")) {
 			bDatEmissao = true;
 		} else if (qName.equalsIgnoreCase("vlrDocumento")) {
 			bVlrDocumento = true;
 		} else if (qName.equalsIgnoreCase("vlrGlosa")) {
 			bVlrGlosa = true;
 		} else if (qName.equalsIgnoreCase("vlrLiquido")) {
 			bVlrLiquido = true;
 		} else if (qName.equalsIgnoreCase("numMes")) {
 			bNumMes = true;
 		} else if (qName.equalsIgnoreCase("numAno")) {
 			bNumAno = true;
 		} else if (qName.equalsIgnoreCase("numParcela")) {
 			bNumParcela = true;
 		} else if (qName.equalsIgnoreCase("txtPassageiro")) {
 			bTxtPassageiro = true;
 		} else if (qName.equalsIgnoreCase("txtTrecho")) {
 			bTxtTrecho = true;
 		} else if (qName.equalsIgnoreCase("numLote")) {
 			bNumLote = true;
 		} else if (qName.equalsIgnoreCase("numRessarcimento")) {
 			bNumRessarcimento = true;
 		}
 		
 	}
 	
 	public void endElement(String uri, String localName,
 			String qName) throws SAXException {
 	}
 	
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (bTxNomeParlamentar) {
 			//Começando novo deputado
 			gasto = new DeputadoFederalGasto();
 			String s = new String(ch, start, length);
 			gasto.setTxNomeParlamentar(s);
 			bTxNomeParlamentar = false;
 		} else if (bNuCarteiraParlamentar) {
 			String s = new String(ch, start, length);
 			gasto.setNuCarteiraParlamentar(s);
 			bNuCarteiraParlamentar = false;
 		} else if (bNuLegislatura) {
 			String s = new String(ch, start, length);
 			gasto.setNuLegislatura(s);
 			bNuLegislatura = false;
 		} else if (bSgUF) {
 			String s = new String(ch, start, length);
 			gasto.setSgUF(s);
 			bSgUF = false;
 		} else if (bSgPartido) {
 			String s = new String(ch, start, length);
 			gasto.setSgPartido(s);
 			bSgPartido = false;
 		} else if (bCodLegislatura) {
 			String s = new String(ch, start, length);
 			gasto.setCodLegislatura(s);
 			bCodLegislatura = false;
 		} else if (bNumSubCota) {
 			String s = new String(ch, start, length);
 			gasto.setNumSubCota(s);
 			bNumSubCota = false;
 		} else if (bTxtDescricao) {
 			String s = new String(ch, start, length);
 			gasto.setTxtDescricao(s);
 			bTxtDescricao = false;
 		} else if (bNumEspecificacaoSubCota) {
 			String s = new String(ch, start, length);
 			gasto.setNumEspecificacaoSubCota(s);
 			bNumEspecificacaoSubCota = false;
 		} else if (bTxtDescricaoEspecificacao) {
 			String s = new String(ch, start, length);
 			gasto.setTxtDescricaoEspecificacao(s);
 			bTxtDescricaoEspecificacao = false;				
 		} else if (bTxtFornecedor) {
 			String s = new String(ch, start, length);
 			gasto.setTxtFornecedor(s);
 			bTxtFornecedor = false;				
 		} else if (bTxtCNPJCPF) {
 			String s = new String(ch, start, length);
 			gasto.setTxtCNPJCPF(s);
 			bTxtCNPJCPF = false;				
 		} else if (bTxtNumero) {
 			String s = new String(ch, start, length);
 			gasto.setTxtNumero(s);
 			bTxtNumero = false;				
 		} else if (bIndTipoDocumento) {
 			String s = new String(ch, start, length);
 			gasto.setIndTipoDocumento(s);
 			bIndTipoDocumento = false;				
 		} else if (bDatEmissao) {
 			String s = new String(ch, start, length);
 			/*try {
 				gasto.setDatEmissao(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(s));
 			} catch (ParseException e) {
 				e.printStackTrace();
 				throw new SAXException(e);
 			}*/
 			gasto.setDatEmissao(s);
 			bDatEmissao = false;				
 		} else if (bVlrDocumento) {
 			String s = new String(ch, start, length);
 			gasto.setVlrDocumento(Float.valueOf(s));
 			bVlrDocumento = false;				
 		} else if (bVlrGlosa) {
 			String s = new String(ch, start, length);
 			gasto.setVlrGlosa(Float.valueOf(s));
 			bVlrGlosa = false;				
 		} else if (bVlrLiquido) {
 			String s = new String(ch, start, length);
 			gasto.setVlrLiquido(Float.valueOf(s));
 			bVlrLiquido = false;				
 		} else if (bNumMes) {
 			String s = new String(ch, start, length);
 			gasto.setNumMes(Integer.valueOf(s));
 			bNumMes = false;				
 		} else if (bNumAno) {
 			String s = new String(ch, start, length);
 			gasto.setNumAno(Integer.valueOf(s));
 			bNumAno = false;				
 		} else if (bNumParcela) {
 			String s = new String(ch, start, length);
 			try {
 				gasto.setNumParcela(Integer.valueOf(s));
 			} catch (Exception e){
 				gasto.setNumParcela(0);
 			}
 			bNumParcela = false;				
 		} else if (bTxtPassageiro) {
 			String s = new String(ch, start, length);
 			gasto.setTxtPassageiro(s);
 			bTxtPassageiro = false;				
 		} else if (bTxtTrecho) {
 			String s = new String(ch, start, length);
 			gasto.setTxtTrecho(s);
 			bTxtTrecho = false;				
 		} else if (bNumLote) {
 			String s = new String(ch, start, length);
 			gasto.setNumLote(s);
 			bNumLote = false;				
 		} else if (bNumRessarcimento) {
 			String s = new String(ch, start, length);
 			gasto.setNumRessarcimento(s);
 			bNumRessarcimento = false;
 			
 			saveGastoDeputadoFederal(gasto);
 		}		
  
 	}
 
 	private void saveGastoDeputadoFederal(DeputadoFederalGasto gasto) {
 		JPA.em().persist(gasto);
 		count++;
 		if (count%10000 == 0){
 			System.out.println("Salvo " + count);
 		}
 	}
 }
 
 class ProjetoDeLeiXMLHandler extends DefaultHandler {
 	private boolean bId;
 	private boolean bNome;
 	private boolean bSigla;
 	private boolean bAno;
 	private boolean bDatApresentacao;
 	private boolean bTxtEmenta;
 	private boolean bTxtNomeAutor;
 	private boolean bIdecadastro;	
 	
 	List<ProjetoDeLei> projetos;
 	ProjetoDeLei projeto;
 	
 	public ProjetoDeLeiXMLHandler(List<ProjetoDeLei> projetos) {
 		this.projetos = projetos;
 		projeto = new ProjetoDeLei();
 	}
 
 	public void startElement(String uri, String localName,String qName, 
             Attributes attributes) throws SAXException {
 		if (qName.equalsIgnoreCase("id")) {
 				bId = true;
 		} else if (qName.equalsIgnoreCase("nome")) {
 				bNome = true;
 		} else if (qName.equalsIgnoreCase("sigla")) {
 				bSigla = true;
 		} else if (qName.equalsIgnoreCase("ano")) {
 			bAno = true;
 		} else if (qName.equalsIgnoreCase("datApresentacao")) {
 			bDatApresentacao = true;
 		} else if (qName.equalsIgnoreCase("txtEmenta")) {
 			bTxtEmenta = true;
 		} else if (qName.equalsIgnoreCase("txtNomeAutor")) {
 			bTxtNomeAutor = true;
 		} else if (qName.equalsIgnoreCase("idecadastro")) {
 			bIdecadastro = true;
 		}
 	}
 	
 	public void endElement(String uri, String localName,
 			String qName) throws SAXException {
 	}
 	
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (bId) {
 			//Começando novo deputado
 			if (projeto.getId() == 0){
 				projeto = new ProjetoDeLei();
 				String s = new String(ch, start, length);
 				projeto.setId(Long.parseLong(s));
 				bId = false;
 			}
 		}
 		if (bNome) {
 			if (projeto.getNome() != null && projeto.getNome().trim().length() > 0){
 				String s = new String(ch, start, length);
 				projeto.setNome(s.trim());
 				bNome = false;
 			}
 		}
 		if (bSigla) {
 			if (projeto.getSigla() != null && projeto.getSigla().trim().length() > 0){
 				String s = new String(ch, start, length);
 				projeto.setSigla(s.trim());
 				bSigla = false;
 			}
 		}
 		if (bAno) {
 			String s = new String(ch, start, length);
 			projeto.setAno(Integer.parseInt(s));
 			bAno = false;
 		}
 		if (bDatApresentacao) {
 			String s = new String(ch, start, length);
 			projeto.setDataApresentacao(s);
 			bDatApresentacao = false;
 		}
 		if (bTxtEmenta) {
 			String s = new String(ch, start, length);
 			projeto.setEmenta(s);
 			bTxtEmenta = false;
 		}
 		if (bTxtNomeAutor) {
 			String s = new String(ch, start, length);
 			projeto.setNomeAutor(s);
 			bTxtNomeAutor = false;
 		}
 		if (bIdecadastro) {
 			String s = new String(ch, start, length);
 			projeto.setCadastroDeputado(s);
 			bIdecadastro = false;
 			
 			projetos.add(projeto);
 			projeto = new ProjetoDeLei();
 			System.out.println("Projeto: " + projeto);
 		}		
 	}
 }
 
 class ProjetoDeLeiXMLHandlerBack extends DefaultHandler {
 	private boolean bId;
 	private boolean bNome;
 	private boolean bSigla;
 	private boolean bAno;
 	private boolean bDatApresentacao;
 	private boolean bTxtEmenta;
 	private boolean bTxtNomeAutor;
 	private boolean bIdecadastro;
 	
 	//helpers because some elemens have same name
 	private boolean bTipoProposicao;
 	private boolean bOrgaoNumerador;
 	private boolean bApreciacao;
 	private boolean bSituacao;
 	
 	
 	List<ProjetoDeLei> projetos;
 	ProjetoDeLei projeto;
 	
 	public ProjetoDeLeiXMLHandlerBack(List<ProjetoDeLei> projetos) {
 		this.projetos = projetos;
 	}
 
 	public void startElement(String uri, String localName,String qName, 
             Attributes attributes) throws SAXException {
 		
 		if (qName.equalsIgnoreCase("id")) {
 			if (!bTipoProposicao && !bOrgaoNumerador && !bApreciacao && !bSituacao){
 				bId = true;
 			}
 		} else if (qName.equalsIgnoreCase("nome")) {
 			if (!bTipoProposicao && !bOrgaoNumerador){
 				bNome = true;
 			}
 		} else if (qName.equalsIgnoreCase("sigla")) {
 			if (bTipoProposicao){
 				bSigla = true;
 			}
 		} else if (qName.equalsIgnoreCase("ano")) {
 			bAno = true;
 		} else if (qName.equalsIgnoreCase("datApresentacao")) {
 			bDatApresentacao = true;
 		} else if (qName.equalsIgnoreCase("txtEmenta")) {
 			bTxtEmenta = true;
 		} else if (qName.equalsIgnoreCase("txtNomeAutor")) {
 			bTxtNomeAutor = true;
 		} else if (qName.equalsIgnoreCase("idecadastro")) {
 			bIdecadastro = true;
 			
 			//helper
 		} else if (qName.equalsIgnoreCase("tipoProposicao")) {
 			bTipoProposicao = true;
 		} else if (qName.equalsIgnoreCase("orgaoNumerador")) {
 			bOrgaoNumerador = true;
 		} else if (qName.equalsIgnoreCase("Apreciacao")) {
 			bApreciacao = true;
 		} else if (qName.equalsIgnoreCase("situacao")) {
 			bSituacao = true;
 		}
 	}
 	
 	public void endElement(String uri, String localName,
 			String qName) throws SAXException {
 		
 		if (qName.equalsIgnoreCase("tipoProposicao")) {
 			bTipoProposicao = false;
 		} else if (qName.equalsIgnoreCase("orgaoNumerador")) {
 			bOrgaoNumerador = false;
 		} else if (qName.equalsIgnoreCase("Apreciacao")) {
 			bApreciacao = false;
 		} else if (qName.equalsIgnoreCase("situacao")) {
 			bSituacao = false;
 		}
 	}
 	
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (bId) {
 			//Começando novo deputado
 			projeto = new ProjetoDeLei();
 			String s = new String(ch, start, length);
 			projeto.setId(Long.parseLong(s));
 			bId = false;
 		}
 		if (bNome) {
 			String s = new String(ch, start, length);
 			projeto.setNome(s);
 			bNome = false;
 		}
 		if (bSigla) {
 			String s = new String(ch, start, length);
 			projeto.setSigla(s);
 			bSigla = false;
 		}
 		if (bAno) {
 			String s = new String(ch, start, length);
 			projeto.setAno(Integer.parseInt(s));
 			bAno = false;
 		}
 		if (bDatApresentacao) {
 			String s = new String(ch, start, length);
 			projeto.setDataApresentacao(s);
 			bDatApresentacao = false;
 		}
 		if (bTxtEmenta) {
 			String s = new String(ch, start, length);
 			projeto.setEmenta(s);
 			bTxtEmenta = false;
 		}
 		if (bTxtNomeAutor) {
 			String s = new String(ch, start, length);
 			projeto.setNomeAutor(s);
 			bTxtNomeAutor = false;
 		}
 		if (bIdecadastro) {
 			String s = new String(ch, start, length);
 			projeto.setCadastroDeputado(s);
 			bIdecadastro = false;
 			
 			projetos.add(projeto);
 			//System.out.println(projeto);
 			
 			projeto = new ProjetoDeLei();
 			
 			bTipoProposicao = false;
 			bOrgaoNumerador = false;
 			bApreciacao = false;
 			bSituacao = false;
 		}		
 	}
 }
