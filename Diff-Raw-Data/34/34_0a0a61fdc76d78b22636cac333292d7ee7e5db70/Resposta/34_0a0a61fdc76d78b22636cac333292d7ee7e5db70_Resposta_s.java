 package controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import dao.RespostaDAO;
 

 /**
  * Resposta
  * 
  * @author Daniel Bonfim <daniel.fb88@gmail.com>
  * @since 01-09-2012
  * 
  */
 public class Resposta {
 	private Integer id_resposta;
 	private Pergunta pergunta = new Pergunta();
 	private String descricao;
 	private Boolean correta;
 	private String observacao;
 
 	private RespostaDAO respostaDAO = new RespostaDAO();
 
 	public Resposta() {
 
 	}
 
 	/**
 	 * Resposta
 	 * 
 	 * @param id_resposta
 	 * @param pergunta
 	 * @param descricao
 	 * @param correta
 	 * @param observacao
 	 */
 	public Resposta(Integer id_resposta, Pergunta pergunta, String descricao,
 			Boolean correta, String observacao) {
 		super();
 		this.id_resposta = id_resposta;
 		this.pergunta = pergunta;
 		this.descricao = descricao;
 		this.correta = correta;
 		this.observacao = observacao;
 	}
 
 	/**
 	 * Constroi e carrega o objeto com um Map que possua suas chaves iguais aos
 	 * nomes das colunas do banco, referente a este objeto
 	 * 
 	 * @param map
 	 * @param carregarRelacionamentos
 	 */
 	public Resposta(Map<String, Object> map, boolean carregarRelacionamentos) {
 		this.carregarObjeto(map, carregarRelacionamentos);
 	}
 
 	/**
 	 * Carrega objeto baseado no HashMap de Entrada. As chaves do Map devem ser
 	 * iguais ao nome dos campos da tabela.
 	 * 
 	 * @param map
 	 * @param carregarRelacionamentos
 	 */
 	private void carregarObjeto(Map<String, Object> map, boolean carregarRelacionamentos) {
 
 		this.id_resposta = (Integer) map.get("id_resposta");
 		this.pergunta.setId_pergunta((Integer) map.get("id_pergunta"));
 		this.descricao = (String) map.get("descricao");
 		this.correta = (((Integer) map.get("correta")) == 1) ? true : false;
 		this.observacao = (String) map.get("observacao");
 
 		if (carregarRelacionamentos)
 			this.pergunta.carregar(carregarRelacionamentos);
 	}
 
 	/**
 	 * Adicionar
 	 * 
 	 * @return
 	 */
 	public boolean adicionar() {
		int respostaCorreta = (correta != null && correta) ? 1 : 0;
 
 		return this.respostaDAO.adicionar(pergunta.getId_pergunta(), descricao,
 				respostaCorreta, observacao) > 0;
 	}
 
 	/**
 	 * Carregar
 	 * 
 	 * @param carregarRelacionamentos
 	 * @return
 	 */
 	public boolean carregar(boolean carregarRelacionamentos) {
 		Map<String, Object> map = this.respostaDAO.buscarPorId(this.id_resposta);
 
 		if (map != null) {
 			this.carregarObjeto(map, carregarRelacionamentos);
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Carregar por Id
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public boolean carregarPorId(int id, boolean carregarRelacionamentos) {
 		this.id_resposta = id;
 		return this.carregar(carregarRelacionamentos);
 	}
 
 	/**
 	 * Editar
 	 * 
 	 * @return
 	 */
 	public boolean editar() {
		int respostaCorreta = (correta != null && correta) ? 1 : 0;
 
 		return this.respostaDAO.editar(id_resposta, pergunta.getId_pergunta(),
 				descricao, respostaCorreta, observacao) > 0;
 	}
 
 	/**
 	 * Excluir
 	 * 
 	 * @return
 	 */
 	public boolean excluir() {
 		return this.respostaDAO.excluir(id_resposta) > 0;
 	}
 
 	/**
 	 * Listar
 	 * 
 	 * @param carregarRelacionamentos
 	 * @return
 	 */
 	public List<Resposta> listar(boolean carregarRelacionamentos) {
		// TODO: ISSO NAO DEVE SER FEITO AQUI. DEVE SER FEITO NO DAO!!!! ESSE É O MOTIVO DO ERRO
		int respostaCorreta = (correta != null && correta) ? 1 : 0;
		
 		// buscando a lista de Mapa recuperando pelos parametros
 		List<Map<String, Object>> listMap = this.respostaDAO.listarPor(
 				pergunta.getId_pergunta(), descricao, respostaCorreta, observacao);
 
 		List<Resposta> list = new ArrayList<Resposta>();
 
 		for (Map<String, Object> map : listMap) {
 			list.add(new Resposta(map, carregarRelacionamentos));
 		}
 
 		return list;
 	}
 
 	/**
 	 * @return the id_resposta
 	 */
 	public Integer getId_resposta() {
 		return id_resposta;
 	}
 
 	/**
 	 * @param id_resposta
 	 *            the id_resposta to set
 	 */
 	public void setId_resposta(Integer id_resposta) {
 		this.id_resposta = id_resposta;
 	}
 
 	/**
 	 * @return the pergunta
 	 */
 	public Pergunta getPergunta() {
 		return pergunta;
 	}
 
 	/**
 	 * @param pergunta
 	 *            the pergunta to set
 	 */
 	public void setPergunta(Pergunta pergunta) {
 		this.pergunta = pergunta;
 	}
 
 	/**
 	 * @return the descricao
 	 */
 	public String getDescricao() {
 		return descricao;
 	}
 
 	/**
 	 * @param descricao
 	 *            the descricao to set
 	 */
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 	/**
 	 * @return the correta
 	 */
 	public Boolean getCorreta() {
 		return correta;
 	}
 
 	/**
 	 * @param correta
 	 *            the correta to set
 	 */
 	public void setCorreta(Boolean correta) {
 		this.correta = correta;
 	}
 
 	/**
 	 * @return the observacao
 	 */
 	public String getObservacao() {
 		return observacao;
 	}
 
 	/**
 	 * @param observacao
 	 *            the observacao to set
 	 */
 	public void setObservacao(String observacao) {
 		this.observacao = observacao;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((correta == null) ? 0 : correta.hashCode());
 		result = prime * result
 				+ ((descricao == null) ? 0 : descricao.hashCode());
 		result = prime * result
 				+ ((id_resposta == null) ? 0 : id_resposta.hashCode());
 		result = prime * result
 				+ ((observacao == null) ? 0 : observacao.hashCode());
 		result = prime * result
 				+ ((pergunta == null) ? 0 : pergunta.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Resposta other = (Resposta) obj;
 		if (correta == null) {
 			if (other.correta != null)
 				return false;
 		} else if (!correta.equals(other.correta))
 			return false;
 		if (descricao == null) {
 			if (other.descricao != null)
 				return false;
 		} else if (!descricao.equals(other.descricao))
 			return false;
 		if (id_resposta == null) {
 			if (other.id_resposta != null)
 				return false;
 		} else if (!id_resposta.equals(other.id_resposta))
 			return false;
 		if (observacao == null) {
 			if (other.observacao != null)
 				return false;
 		} else if (!observacao.equals(other.observacao))
 			return false;
 		if (pergunta == null) {
 			if (other.pergunta != null)
 				return false;
 		} else if (!pergunta.equals(other.pergunta))
 			return false;
 		return true;
 	}
 
 }
