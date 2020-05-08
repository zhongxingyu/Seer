 package ufpb.br.projeto;
 
 /**
  * Essa classe sera a fachada do jogo.
  * 
  * @author jonathas Firmo
  * 
  */
 public class Jogo {
 
 	private String tabuleiro[] = new String[] { null, null, null, null };
 	private int posicaoPersonagem;
 	private int valorDado;
 	private boolean resultado;
 	private int score;
 	private boolean iniciouJogo = false;
 	private boolean definirPersonagemX;
 	private boolean contemSurpresa = false;
 
 	/**
 	 * Esse metodo informar quando o jogo termina.
 	 * 
 	 * @return true caso tenha terminado e false caso contrario.
 	 * 
 	 */
 
 	public boolean acabou() {
 		if (getPosicaoPersonagem() == 3) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isEscolhaPersonagemX() {
 		return definirPersonagemX;
 	}
 
 	public void setEscolhaPersonagemX(boolean b) {
 		if (iniciouJogo) {
 			throw new ExcecaoJogoTabuleiro("O jogo j foi iniciado!");
 		}
 
 		this.definirPersonagemX = b;
 	}
 
 	/**
 	 * Esse metodo faz o lanamento do dado.
 	 * 
 	 * @return retorna um valor inteiro gerado pelo dado.
 	 */
 	public int lancarDado() {
 
 		if (acabou()) {
 			throw new ExcecaoJogoTabuleiro("O jogo j foi acabado!");
 		}
 
 		if (!definirPersonagemX) {
 			throw new ExcecaoJogoTabuleiro(" O Personagem no foi definido!");
 		}
 
 		iniciouJogo = true;
 		return valorDado = 1;
 
 	}
 
 	/**
 	 * Esse metodo propoe um desafio ao personagem.
 	 * 
 	 * @param questao
 	 *            propoe uma pergunta ao personagem.
 	 * @param alternativas
 	 *            possui uma lista de alternativa para escolha do personagem.
 	 * @param gabarito
 	 *            contem a alternativa correta da pergunta.
 	 * @param resposta
 	 *             a alternativa escolhida pelo personagem.
 	 * @return
 	 */
 	public boolean desafio(String questao, String alternativas[],
 			String gabarito, String resposta) {
 
 		if (resposta.equals(gabarito)) {
 			resultado = true;
 			posicaoPersonagem += valorDado;
 
 		}
		resultado = false;
 		adicionarPontuacao(resultado);
 		return resultado;
 	}
 
 	/**
 	 * Esse metodo adiciona a pontuao no score do personagem,
 	 * 
 	 * se o personagem acerta a questao ele ganha 3 pts. se o personagem nao
 	 * estiver no inicio do tabuleiro e errar a questao ele perde 1 pt. se o
 	 * personagem estiver no inicio do tabuleiro ele nao tera nenhum ponto, logo
 	 * retorna sem fazer nada.
 	 * 
 	 * @param resultado
 	 *            informa se o personagem acertou ou errou o desafio.
 	 */
 	private void adicionarPontuacao(boolean resultado) {
 		if (resultado == true)
 			score += 3;
 		if (score != 0 && resultado == false) {
 			score -= 1;
 		}
 		if (score == 0 && resultado == false)
 			return;
 	}
 
 	public boolean isRespostaPersonagemX() {
 		return resultado;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		if (score < 0) {
 			throw new ExcecaoJogoTabuleiro("Valor irregular no score!");
 		}
 
 		this.score = score;
 
 	}
 
 	public int getPosicaoPersonagem() {
 		return posicaoPersonagem;
 	}
 
 	/**
 	 * Esse metodo move o personagem no tabuleiro
 	 * 
 	 * @param posicao
 	 *            informa para qual casa o personagem tem que ir.
 	 */
 	public void moverPersonagemX(int posicao) {
 		String escolha = (definirPersonagemX) ? "X" : "Y";
 
 		if (posicao < 0 || posicao >= tabuleiro.length) {
 			throw new ExcecaoJogoTabuleiro("Posicao irregular!");
 		}
 		this.tabuleiro[posicao] = escolha;
 
 	}
 
 	/**
 	 * Esse metodo propoe uma surpresa para o personagem
 	 * 
 	 * @param valorSurpresa
 	 *            possui um valor que sera atribuido ao score do personagem
 	 * @return true se o valor for positivo e false caso seja negativo.
 	 */
 	public boolean surpresa(int valorSurpresa) {
 		boolean saida = false;
 
 		if (valorSurpresa > 0) {
 			saida = true;
 		}
 		contemSurpresa = true;
 		return saida;
 	}
 
 	/**
 	 * Esse metodo mostra se contem uma surpresa.
 	 * 
 	 * @return true se contem e false caso contrario.
 	 */
 	public boolean isSurpresa() {
 		return contemSurpresa;
 	}
 
 	/**
 	 * Esse metodo defini se tera uma surpresa ou nao.
 	 * 
 	 * @param contem
 	 *            recebe o valor true para existencia da surpresa e false caso
 	 *            contrario.
 	 */
 	public void setSurpresa(boolean contem) {
 		this.contemSurpresa = contem;
 
 	}
 
 }
