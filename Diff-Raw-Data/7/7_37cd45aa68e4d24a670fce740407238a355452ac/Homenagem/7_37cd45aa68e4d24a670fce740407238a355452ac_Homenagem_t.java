 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.Valid;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.Page;
 import com.avaje.ebean.Query;
 
 @Entity
 public class Homenagem extends Model {
 
 	@Id
 	public Long id;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	public Homenagem homenagemPai;
 
 	@Valid
 	@OneToMany(mappedBy = "homenagemPai")
 	public List<Homenagem> homenagensFilhas;
 
 	@Required
 	@Lob
 	public String numeroRegistro;
 
 	@Required
 	public Homenageado homenageado;
 
 	@Lob
 	public String descricao;
 
 	@Required
 	@ManyToOne
 	public TipoHomenagem tipoHomenagem;
 
 	@ManyToOne
 	public Pais pais;
 
 	@ManyToOne
 	public Estado estado;
 
 	@Required
 	@ManyToOne
 	public Cidade cidade;
 
 	@Lob
 	public String outraCidade;
 
 	@Lob
 	public String resumo;
 
 	@Lob
 	public String local;
 
 	@Lob
 	public String localizacao;
 
 	@Lob
 	public String precedencia;
 
 	@Lob
 	public String objeto;
 
 	@Lob
 	public String prateleira;
 
 	@Lob
 	public String material;
 
 	@Lob
 	public String altura;
 
 	@Lob
 	public String largura;
 
 	@Lob
 	public String comprimento;
 
 	@Lob
 	public String profundidade;
 
 	@Lob
 	public String quemEntregou;
 
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date dataRecebimento;
 
 	@Valid
 	@OneToMany(mappedBy = "homenagem", cascade = CascadeType.REMOVE)
 	public List<HomenagemImagem> imagens;
 	
	public String getDescricao(Integer maxLength) {
		if(maxLength != null && descricao != null && descricao.length() > maxLength) {
			return descricao.substring(0, maxLength) + "\u2026";
		}
		return descricao;
	}
	
 	public String getCidadeCompleto() {
 		String cidadeCompleto = null;
 		
 		if(outraCidade != null && !outraCidade.trim().isEmpty()) {
 			cidadeCompleto = outraCidade;
 			
 			if(estado != null) {
 				if(!cidadeCompleto.trim().isEmpty()) {
 					cidadeCompleto = cidadeCompleto + " - ";
 				}
 				cidadeCompleto = cidadeCompleto + estado.abreviacao;
 			}
 			
 			if(pais != null) {
 				if(!cidadeCompleto.trim().isEmpty()) {
 					cidadeCompleto = cidadeCompleto + " - ";
 				}
 				cidadeCompleto = cidadeCompleto + pais.abreviacao;
 			}
 		} else if(cidade != null) {
 			cidadeCompleto = cidade.nome + " - " + cidade.estado.abreviacao + " - " + cidade.estado.pais.abreviacao;
 		}
 		
 		return cidadeCompleto;
 	}
 
 	public static Model.Finder<Long, Homenagem> find = new Finder<Long, Homenagem>(Long.class, Homenagem.class);
 
 	public static Page<Homenagem> page(int page, int pageSize, String sortBy, String order, String filter) {
 		return find.where().ilike("descricao", "%" + filter + "%").orderBy(sortBy + " " + order).findPagingList(pageSize)
 				.getPage(page);
 	}
 
 	public static Page<Homenagem> page(int page, int pageSize, String sortBy, String order, HomenagemFilter homenagemFilter) {
 		Query<Homenagem> homenagemQuery = findFiltered(sortBy, order, homenagemFilter);
 
 		return homenagemQuery.findPagingList(pageSize).getPage(page);
 	}
 
 	public static List<Homenagem> list(String sortBy, String order, HomenagemFilter homenagemFilter) {
 		Query<Homenagem> homenagemQuery = findFiltered(sortBy, order, homenagemFilter);
 
 		return homenagemQuery.findList();
 	}
 
 	public static Query<Homenagem> findFiltered(String sortBy, String order, HomenagemFilter homenagemFilter) {
 		ExpressionList<Homenagem> queryEl = find.where();
 
 		if (homenagemFilter != null) {
 			if (homenagemFilter.numeroRegistro != null && !homenagemFilter.numeroRegistro.isEmpty()) {
 				queryEl.ilike("numeroRegistro", "%" + homenagemFilter.numeroRegistro + "%");
 			}
 
 			if (homenagemFilter.descricao != null && !homenagemFilter.descricao.isEmpty()) {
 				queryEl.ilike("descricao", "%" + homenagemFilter.descricao + "%");
 			}
 
 			if (homenagemFilter.homenageado != null) {
 				queryEl.eq("homenageado", homenagemFilter.homenageado);
 			} else {
 				queryEl.eq("homenageado", Homenageado.MARCONI);
 			}
 
 			if (homenagemFilter.tipoHomenagem != null) {
 				queryEl.eq("tipoHomenagem", homenagemFilter.tipoHomenagem);
 			}
 
 			if (homenagemFilter.dataRecebimentoInicio != null && homenagemFilter.dataRecebimentoFim != null) {
 				queryEl.between("dataRecebimento", homenagemFilter.dataRecebimentoInicio, homenagemFilter.dataRecebimentoFim);
 			} else if (homenagemFilter.dataRecebimentoInicio != null) {
 				queryEl.ge("dataRecebimento", homenagemFilter.dataRecebimentoInicio);
 			} else if (homenagemFilter.dataRecebimentoFim != null) {
 				queryEl.le("dataRecebimento", homenagemFilter.dataRecebimentoFim);
 			}
 		}
 
 		return queryEl.orderBy("homenageado asc, " + sortBy + " " + order);
 	}
 }
