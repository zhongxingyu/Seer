 package mpa.manager.control;
 
 import static java.util.Collections.reverse;
 import static java.util.Collections.sort;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mpa.core.container.CoreContainerFactory;
 import mpa.mad.Mad;
 import mpa.mad.MadHome;
 import mpa.mad.Team;
 import mpa.main.loader.ConfigStream;
 import mpa.main.loader.ExpectedLoaderStream;
 import mpa.main.loader.TeamLoaderStream;
 import mpa.manager.Connector;
 import mpa.manager.bean.Mesa;
 import mpa.manager.bean.MpaConfiguracao;
 import mpa.manager.bean.Objectiviano;
 import mpa.manager.repository.MesaRepository;
 import mpa.manager.repository.MpaConfiguracaoRepository;
 import mpa.manager.repository.ObjectivianoRepository;
 import mpa.pair.Pair;
 import mpa.pair.PairHome;
 import mpa.scenary.Scenary;
 import mpa.scenary.ScenaryHome;
 import mpa.search.ScenarySearch;
 
 import org.picocontainer.MutablePicoContainer;
 import org.picocontainer.PicoContainer;
 
 public class MpaControl {
 
     private static List<Objectiviano> objectivianos;
     private static List<MpaConfiguracao> mpaConfiguracoes;
 
     public MpaControl() {
         try {
             MesaRepository.createInstance(Connector.getConnection());
             ObjectivianoRepository.createInstance(Connector.getConnection());
             MpaConfiguracaoRepository.createInstance(Connector.getConnection());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public List<Mesa> getMesas(MpaConfiguracao mpa) throws SQLException {
     	return getMesas(mpa, null);
     }
 
     public List<Mesa> getMesas(MpaConfiguracao mpa, String team) throws SQLException {    	
         MesaRepository repository = MesaRepository.getInstance();
         List<Mesa> mesas = repository.dadoMpa(mpa);
         Collections.sort(mesas);
 
         if (team == null)
         	return mesas;
         
 		List<Mesa> mesasDoTeam = new ArrayList<Mesa>();
 		for (Mesa mesa : mesas) {
 			if (team.equals(mesa.getEquipe()))
 				mesasDoTeam.add(mesa);
 		}
 		
 		return mesasDoTeam;
     }
     
     private Mesa getMaiorMesa(MpaConfiguracao mpa) throws SQLException {
     	return Collections.max(getMesas(mpa));
     }
     
     public String getDevsTeamSeparated(MpaConfiguracao mpa, String selectedTeam, String separator) throws SQLException {
     	if (mpa == null)
     		return "";
     	
     	HashMap<String, String> equipes = new HashMap<String, String>();
     	
     	for (Mesa mesa : getMesas(mpa, selectedTeam)) {
     		String devs = "";
     		if (equipes.containsKey(mesa.getEquipe()))
     			devs = equipes.remove(mesa.getEquipe());
     		
     		equipes.put(mesa.getEquipe(), !"".equals(devs) ? devs + separator + mesa.getDevsString() : mesa.getDevsString());
     	}
 
     	StringBuilder builder = new StringBuilder();
     	for (String equipe : equipes.keySet()) {
     		builder.append(equipe).append("\n".equals(separator) ? ":" + separator : separator).append(equipes.get(equipe)).append("\n");
     	}
     	
     	return builder.toString();
     }
 
     public List<Objectiviano> getObjectivianos() {
         if (objectivianos == null) {
             ObjectivianoRepository repository = ObjectivianoRepository.getInstance();
             objectivianos = repository.todos();
             Collections.sort(objectivianos);
         }
         return objectivianos;
     }
 
     public void updateMesa(Mesa mesa) throws SQLException {
         MesaRepository repository = MesaRepository.getInstance();
         repository.update(mesa);
     }
 
     public Objectiviano objectivianoSelecionado(Objectiviano selected) {
         if (selected == null)
             return selected;
         for (Objectiviano objectiviano : objectivianos)
             if (objectiviano.equals(selected))
                 return objectiviano;
         return null;
     }
 
     public void criaMpaComMesas(Date dataInicio, Date dataFim, String dadosMesas) throws SQLException {
             validaDadosEntrada(dadosMesas);
             MpaConfiguracao mpaConfiguracao = criaMpaConfiguracao(dataInicio, dataFim);
             criaMesas(mpaConfiguracao, 1, dadosMesas);
     }
 
     public void atualizaMpaComMesas(MpaConfiguracao mpa, String dadosMesas) throws SQLException {
     		validaDadosEntrada(dadosMesas);
     		criaMesas(mpa, getMaiorMesa(mpa).getNumero() + 1, dadosMesas);
     }
 
     private void validaDadosEntrada(String dadosMesas) throws SQLException {
         for (String mesaString : dadosMesas.split("\n")) {
         	if (mesaString.contains(":"))
         		continue;
         	
             Pattern pattern = Pattern.compile("\\w+( / \\w+)?");
             Matcher matcher = pattern.matcher(mesaString);
 
             if (!matcher.find())
                throw new IllegalArgumentException("Formato inválido para mesa. (utilize: Dev1 / Dev2)");
 
             String devsSeparados[] = mesaString.split(" / ");
             ObjectivianoRepository devRepository = ObjectivianoRepository.getInstance();
 
             devRepository.getObjectiviano(devsSeparados[0]);
 
             if (devsSeparados.length != 1)
                 devRepository.getObjectiviano(devsSeparados[1]);
         }
     }
 
     private MpaConfiguracao criaMpaConfiguracao(Date dataInicio, Date dataFim) throws SQLException {
         MpaConfiguracao mpaConfiguracao = new MpaConfiguracao(dataInicio, dataFim);
         MpaConfiguracaoRepository mpaConfiguracaorepository = MpaConfiguracaoRepository.getInstance();
         mpaConfiguracaorepository.insert(mpaConfiguracao);
         return mpaConfiguracao;
     }
 
     private void criaMesas(MpaConfiguracao mpa, int numeroMesaInicial, String mesasString) throws SQLException {
 
         ObjectivianoRepository devRepository = ObjectivianoRepository.getInstance();
 
         int numeroMesa = numeroMesaInicial;
         String equipe = "";
         for (String streamLine : mesasString.split("\n")) {
         	if (streamLine.contains(":")) {
         		equipe = streamLine.substring(0, streamLine.length() -1);
         		continue;
         	}
         	
             String devs[] = streamLine.split(" / ");
 
             Objectiviano primeiroObjectiviano = devRepository.getObjectiviano(devs[0].trim());
             Objectiviano segundoObjectiviano = null;
 
             if (devs.length != 1)
                 segundoObjectiviano = devRepository.getObjectiviano(devs[1].trim());
 
             criaMesa(mpa, numeroMesa++, primeiroObjectiviano, segundoObjectiviano, equipe);
         }
     }
 
     public void criaMesa(MpaConfiguracao mpa, int numero, Objectiviano primeiroObjectiviano,
             Objectiviano segundoObjectiviano, String equipe) throws SQLException {
         MesaRepository mesaRepository = MesaRepository.getInstance();
         Mesa mesa = new Mesa(numero, mpa, primeiroObjectiviano, segundoObjectiviano, equipe);
         mesaRepository.insert(mesa);
     }
 
     public List<MpaConfiguracao> getMpasDisponiveis() {
         MpaConfiguracaoRepository mpaConfiguracaorepository = MpaConfiguracaoRepository.getInstance();
         List<MpaConfiguracao> mpas = mpaConfiguracaorepository.todos();
 
         sort(mpas);
         reverse(mpas);
         mpaConfiguracoes = mpas;
         
         return mpas;
     }
     
     public MpaConfiguracao getMpaAtual() {
         for (MpaConfiguracao mpa : mpaConfiguracoes)
             if (mpa.isAtual())
                 return mpa;
         return null;
     }
 
     public MpaConfiguracao getMaiorMpa() {
         return mpaConfiguracoes.get(0);
     }
     
     public List<MpaConfiguracao> getMpasEditaveis() {
     	List<MpaConfiguracao> editaveis = new ArrayList<MpaConfiguracao>();
     	for (MpaConfiguracao mpa : mpaConfiguracoes)
     		if (!mpa.isPassado())
     			editaveis.add(mpa);
     	
     	return editaveis;
     }
 
     public void excluiMesa(Mesa mesa) throws SQLException {
         MesaRepository mesaRepository = MesaRepository.getInstance();
         mesaRepository.delete(mesa);
     }
 
     public void fechaConexao() {
         Connector.closeConnection();
     }
 
     public String gerarNovoMpa(ConfigStream params) throws SQLException {
         
         MutablePicoContainer container = CoreContainerFactory.buildContainer();
         ScenarySearch searcher = loadData(container);
         
         container.addComponent(params);
         
         container.addComponent(TeamLoaderStream.class);
         container.getComponent(TeamLoaderStream.class).load();
         
         container.addComponent(ExpectedLoaderStream.class);
         container.getComponent(ExpectedLoaderStream.class).load();
         
         Scenary scenary = searcher.search();
         
         return getDevsCalculated(container.getComponent(MadHome.class), scenary);
     }
 
     private ScenarySearch loadData(PicoContainer container) throws SQLException {
         ScenarySearch searcher = container.getComponent(ScenarySearch.class);
         MadHome madHome = container.getComponent(MadHome.class);
         PairHome pairHome = container.getComponent(PairHome.class);
         
         for(Objectiviano obj: ObjectivianoRepository.getInstance().todos())
             madHome.createMad(obj.getNome(), new Date());
         
         Mad sozinho = madHome.createMad("Sozinho", new Date());
         
         ScenaryHome scenaryHome = container.getComponent(ScenaryHome.class);
         List<MpaConfiguracao> mpas = getMpasDisponiveis();
         reverse(mpas);
         int c = 1;
         for(MpaConfiguracao mpa: mpas){
             Scenary scenary = scenaryHome.createScenary(c++, mpa.getDataInicio());
             for(Mesa m: MesaRepository.getInstance().dadoMpa(mpa)) {
                 Mad mad1 = madHome.getByName(m.getPrimeiroObjectiviano().getNome());
                 Mad mad2 = m.getSegundoObjectiviano() == null ? sozinho : madHome.getByName(m.getSegundoObjectiviano().getNome());
                 scenary.addPair(pairHome.createPair(mad1, mad2));
             }
         }
         reverse(mpas);
         return searcher;
     }
     
     private String getDevsCalculated(MadHome madHome, Scenary scenary) throws SQLException {
     	String team = "";
     	StringBuilder builder = new StringBuilder();
     	Mad sozinho = madHome.getByName("Sozinho");
        
     	for(Pair pair: scenary.getPairs()) {
         	
 			Mad dev1 = pair.getMads().first();
         	Mad dev2 = pair.getMads().last();
         	
         	String teamAtual = detectTeam(madHome.getTeams(), dev1 != sozinho ? dev1 : dev2);
         	if (teamAtual != null && !team.equals(teamAtual)) {
 				team = teamAtual;
 				builder.append(team).append( ":\n");
 			}
         	
         	if (!pair.getMads().contains(sozinho))
         		builder.append(pair).append("\n");
         	else
         		for (Mad mad : pair.getMads()) if (mad != sozinho) builder.append(mad).append("\n"); 
         }
         return builder.toString();
     }
     
     private String detectTeam(List<Team> teams, Mad dev) {
     	for (Team team : teams)
     		if (team.getMads().contains(dev))
     			return team.getName();
     	return null;
     }
 
 	public Set<String> getTeams(MpaConfiguracao mpa) throws SQLException {
 		List<Mesa> mesas = getMesas(mpa);
 		Set<String> equipes = new HashSet<String>();
 		for (Mesa mesa : mesas) {			
 			equipes.add(mesa.getEquipe());
 		}
 		
 		return equipes;
 	}
 }
