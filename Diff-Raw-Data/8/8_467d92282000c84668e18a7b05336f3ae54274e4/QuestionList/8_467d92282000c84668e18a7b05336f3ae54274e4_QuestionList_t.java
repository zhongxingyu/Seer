 package leca.modelo;
 /**
  * @package leca.modelo
  * Classes do modelo principal do LECA, gerenciam e representam o conteúdo.
  */
 import java.util.ArrayList;
 import java.util.Collections;
 
 import leca.exceptions.IncompleteDataException;
 import leca.exceptions.MinimumQuestionsException;
 import leca.xmlconverter.QuestionConverter;
 import leca.xmlconverter.QuestionListConverter;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 /**
  * É a classe que contém uma lista de questões.
  *
  */
 public class QuestionList {
 	
 	public final static int AVALIACAO_IMEDIATA = 0; ///< Ao assinalar a resposta, já é informado se está correta.
 	public final static int AVALIACAO_FINAL = 1; ///< Ao responder todas as questões é informado o número de acertos.
 	public final static int AVALIACAO_ALUNO = 2; ///< O aluno escolhe qual o modo de avaliação deseja. A aplicação de interpretação deverá tratar essa escolha para o funcionamento correto.
 	
 	public String Titulo; ///< Titulo da lista de questões. Obrigatório.
 	public String Tema; ///< Tema geral das questões. Obrigatório.
 	public String Autor; ///< Autor da lista. Obrigatório.
 	public String Obs; ///< Outras observações
 	
 	public String Idioma;///< Idioma é setado automaticamente pela interface de criação.
 	public String Data; ///< Data de modificação da lista de questões, usado para comparar atualizações. É feita automaticamente pela interface do sistema.
 	
 	/// A lista de objetos do tipo Questão
 	private ArrayList<Question> ListaQuestoes;
 
 	private boolean Permuta; ///< Permite que as questões sejam exibidas em ordem aleatória.
 	
 	/**
 	 *  Método de avaliação, podendo ser AVALIACAO_ALUNO, AVALIACAO_IMEDIATA, AVALIACAO_FINAL.	
 	 * 	@see AVALIACAO_IMEDIATA AVALIACAO_ALUNO AVALIACAO_FINAL
 	 */
 	private int Avaliacao; 
 	
 	
 	/**
 	 * Construtor do objeto Lista de Questões
 	 */
 	public QuestionList() { 
 		Titulo = "";
 		Tema = "";
 		Autor = "";
 		Data = "";
 		Obs = "";
 		Idioma = "";
 		//VersaoPlataforma = versaoPlataforma;
 		ListaQuestoes = new ArrayList<Question>();
 		Permuta = false;
 		Avaliacao = AVALIACAO_ALUNO;
 	}
 	
 	/**
 	 * Verifica se é possivel permutar as questões da lista.
 	 * @return true se sim, e false se não.
 	 */
 	public boolean getPermuta() {
 		return Permuta;
 	}
 
 
 	/**
 	 * Atribui a permissão de permutar as questões na lista para exibição.
 	 * @param permuta true para permitir e false para impedir.
 	 */
 	public void setPermuta(boolean permuta) {
 		Permuta = permuta;
 	}
 	
 	/**
 	 * Usa o método Collections.shuffle para permutar a ordem das questões. 
 	 * Util ara serem exibidas para o usuário responder com uma ordem diferente.
 	 * Só ocorre a permutação se houver permissão pela variável booleana Permuta.
 	 * Permuta as alternativas de cada questão, dependendo da permissão de permutação de cada questão.
 	 * @see Permuta permutarAlternativas
 	 */
 	public void permutarQuestoes(){
 		// permuta a lista de questões, deve ser usado somente na interpretação para trocar a ordem que as questões serão exibidas
 		if(Permuta){
 			Collections.shuffle(ListaQuestoes);
 		}
 		for(Question q : ListaQuestoes)
 		{
 			q.permutarAlternativas();
 		}
 	}
 	
 	/*
 	public String getTitulo() {
 		return Titulo;
 	}
 
 	public void setTitulo(String titulo) {
 		Titulo = titulo;
 	}
 
 	public String getTema() {
 		return Tema;
 	}
 
 	public void setTema(String tema) {
 		Tema = tema;
 	}
 
 	public String getAutor() {
 		return Autor;
 	}
 
 	public void setAutor(String autor) {
 		Autor = autor;
 	}
 
 	public String getData() {
 		return Data;
 	}
 
 	public void setData(String data) {
 		Data = data;
 	}
 
 	public String getObs() {
 		return Obs;
 	}
 
 	public void setObs(String obs) {
 		Obs = obs;
 	}
 
 	public String getIdioma() {
 		return Idioma;
 	}
 
 	public void setIdioma(String idioma) {
 		Idioma = idioma;
 	}
 
 	public int getVersaoPlataforma() {
 		return VersaoPlataforma;
 	}
 
 	public void setVersaoPlataforma(int versaoPlataforma) {
 		VersaoPlataforma = versaoPlataforma;
 	}*/
 	
 	/**
 	 * Apaga todas as questões na lista.
 	 */
 	public void limparLista(){
 		ListaQuestoes.clear();
 	}
 	
 	/**
 	 * Adiciona uma questão à lista de questões.
 	 * @param q Questão a ser adicionada.
 	 */
 	public void addLista(Question q){
 		ListaQuestoes.add(q);
 	}
 	
 	/**
 	 * Move uma questão para cima na lista de questões.
 	 * Se tentar mover de uma posição invalida ou para uma posição invalida será lançada exceção.
 	 * @param pos a posição da questão que será movida.
 	 * @throws IndexOutOfBoundsException
 	 */
 	public void moveUp(int pos){
 		Collections.swap(ListaQuestoes, pos, pos-1);
 	}
 
 	/**
 	 * Move uma questão para baixo na lista de questões.
 	 * Se tentar mover de uma posição invalida ou para uma posição invalida será lançada exceção.
 	 * @param pos a posição da questão que será movida.
 	 * @throws IndexOutOfBoundsException
 	 */
 	public void moveDown(int pos){
 		Collections.swap(ListaQuestoes, pos, pos+1);
 	}
 	
 	/**
 	 * Remove uma questão da lista. 
 	 * @param pos a posição da questão que será removida da lista.
 	 */
 	public void removerQuestao(int pos){
 		ListaQuestoes.remove(pos);
 	}
 	
 	/**
 	 * Converte a Lista de Questões para XML.
 	 * Este método chama o verificarListaQuestao e lança suas exceções.
 	 * @return String do XML
 	 * @throws MinimumQuestionsException 
 	 * @throws IncompleteDataException 
 	 * @see verificarListaQuestao
 	 */
 	public String toXML() throws IncompleteDataException, MinimumQuestionsException {
 		verificarListaQuestao();
 		XStream xStream = new XStream(new DomDriver());
 		xStream.registerConverter(new QuestionListConverter());
 		xStream.registerConverter(new QuestionConverter());
 		xStream.alias("ListaQuestoes", QuestionList.class);
 		
 		String xml = null;
 		try{
 			xml = xStream.toXML(this); 
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return xml;
 	}
 	
 	/**
 	 * Converte um XML para Lista de Questões
 	 * @param toXML String do XML
 	 * @return O objeto Lista de Questões
 	 */
 	public static QuestionList fromXML(String toXML) {
 		QuestionList qlr = null;
 		
 		XStream xStream = new XStream(new DomDriver());
 		xStream.registerConverter(new QuestionListConverter());
 		xStream.registerConverter(new QuestionConverter());
 		xStream.alias("ListaQuestoes", QuestionList.class);
 		
 		Object obj = null;
 		
 		try{
 			obj = xStream.fromXML(toXML);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		if(obj != null && obj instanceof QuestionList)
 		{
 			qlr = (QuestionList) obj;
 		}
 		return qlr;
 	}
 	
 	/**
 	 * Cria uma ArrayList contendo cada Questão formatada pelo seu toString.
 	 * Ex: 1 : Primeira questão.
 	 * @return A ArrayList de String contendo as questões.
 	 */
 	public ArrayList<String> getArrayListStringQuestions(){
 		ArrayList<String> list = new ArrayList<String>();
 		int i=1;
 		for(Question q : ListaQuestoes){
 			list.add(i++ + " : " + q.toString());
 		}
 		return list;
 	}
 
 	/**
 	 * @return O método de Avaliação usado para responder as questões desta lista.
 	 * @attention Um interpretador de questões deverá usar este método para verificar se o método não é à escolha do aluno e permitir que ele escolha qual modo de avaliação irá usar.
 	 */
 	public int getAvaliacao() {
 		return Avaliacao;
 	}
 
 	/**
 	 * @param avaliacao seta o método de avaliação. Deve ser uma das constantes de AVALACIAO desta classe.
 	 * @see AVALIACAO_ALUNO
 	 * @see AVALIACAO_FINAL
 	 * @see AVALIACAO_IMEDIATA
 	 */
 	public void setAvaliacao(int avaliacao) {
 		Avaliacao = avaliacao;
 	}
 	
 	/**
 	 * Verifica se a Lista de Questões está corretamente criada.
 	 * Deve ser usado na criação da lista de questões.
 	 * Lança exceções de acordo com o erro. Se os campos obrigatórios não tiverem preenchidos, a exceção conterá o nome do campo que está incompleto.
 	 * @throws IncompleteDataException 
 	 * @throws MinimumQuestionsException 
 	 * @attention Este método deve ser chamado pela GUI para garantir que a questão esta corretamente preenchida.
 	 */
 	public void verificarListaQuestao() throws IncompleteDataException, MinimumQuestionsException{
 		if(Titulo.equals("")){
 			throw new IncompleteDataException("Titulo");
 		}
 		else if(Tema.equals("")){
 			throw new IncompleteDataException("Tema");
 		}
 		else if(Autor.equals("")){
 			throw new IncompleteDataException("Autor");
 		}
 		else if(ListaQuestoes.isEmpty()){
 			throw new MinimumQuestionsException();
 		}
 	}
 	
 	/**
 	 * Obtem a ArrayList da lista de questão.
 	 * @return ArrayList de Question
 	 * @warning É usado somente pelo conversor e para carregar um XML de User.
 	 */
 	public ArrayList<Question> getArrayListQuestion(){
 		return ListaQuestoes;
 	}
 	
 	/**
 	 * Atribui IDs para as questões na ordem que estão na lista.
 	 */
 	//TODO para uso futuro.
 	public void setIds(){
 		int i = 0;
 		for(Question q : ListaQuestoes){
 			q.ID = i++;
 		}
 	}
 	
 	/**
 	 * Obtem o numero de questões na lista.
 	 * @return inteiro que representa o numero de questões na lista.
 	 */
 	public int size(){
 		return ListaQuestoes.size();
 	}
 	
 	/**
 	 * Pega uma questão da lista de questões.
 	 * Deve ser usado para obter questões da lista.
 	 * @param pos posição da questão na lista.
 	 * @return a questão na posição desejada da lista.
 	 */
 	public Question getQuestion(int pos){
 		return ListaQuestoes.get(pos);
 	}
 }
