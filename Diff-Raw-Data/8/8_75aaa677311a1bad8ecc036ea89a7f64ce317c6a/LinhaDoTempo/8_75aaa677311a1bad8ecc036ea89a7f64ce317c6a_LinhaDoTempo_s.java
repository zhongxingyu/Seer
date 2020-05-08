 package blog;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import tempo.IntervaloDeTempo;
 import tempo.UnidadeDeTempo;
 import exception.InvalidLinkException;
 /**
  * Classe que representa uma linha do tempo de um {@link Usuario}
  */
 public class LinhaDoTempo {
 
 	private ArrayList<Link> links;
 	private Map<String,Integer> postagensDoSite;
 
 	public LinhaDoTempo(){
 		links = new ArrayList<Link>();
 		postagensDoSite = new HashMap<String, Integer>();
 	}
 
 	/**
 	 * Posta um link no blog
 	 * @param endereco o endereco do link
 	 * @throws InvalidLinkException exceo caso o link seja invlido
 	 */
 	public void posta(String endereco) throws InvalidLinkException{
 			links.add(0,new Link(endereco));
 			String dominio = getDominioDoSite(endereco);
 			postagensDoSite.put(dominio,
 			postagensDoSite.get(dominio)!= null? postagensDoSite.get(dominio) + 1: 1);
 	}
 
 	private String getDominioDoSite(String endereco) {
 		String dominio = endereco.split("/")[2];
 		if(dominio.startsWith("www.")){
 			dominio = dominio.substring(4);
 		}
 		return dominio;
 	}
 
 	/**
 	 * Retorna o o tempo medio entre as postagens do usuario
 	 * @return o {@link IntervaloDeTempo} medio entre as postagens do usuario
 	 */
 	public IntervaloDeTempo getTempoMedioEntrePostagens(){
 
 		if (links.size() < 1){
 			return null;
 		}else{
 			Calendar dataUltimaPostagem = links.get(0).getDataDePostagem();
 			Calendar dataPrimeiraPostagem= links.get(links.size()-1).getDataDePostagem();
 			return calculaTempoMedio(dataPrimeiraPostagem,dataUltimaPostagem);
 		}
 	}
 
 	
 	private IntervaloDeTempo calculaTempoMedio(Calendar dataPrimeiraPostagem,
 			Calendar dataUltimaPostagem) {
 		int numeroDePostagens = links.size() -1;
 		double milisegundosPorDia = 1000*60*60*24;
 		double tempoEntreDatasMiliSec = dataUltimaPostagem.getTimeInMillis() - dataPrimeiraPostagem.getTimeInMillis();
 		double tempoMedioEntrePostagens = tempoEntreDatasMiliSec/(milisegundosPorDia*numeroDePostagens);
 		if(tempoMedioEntrePostagens >= 1f){
 			return new IntervaloDeTempo(tempoMedioEntrePostagens,UnidadeDeTempo.Dias);
 		}else {
 			tempoMedioEntrePostagens *= 24;
 			if(tempoMedioEntrePostagens >= 1f){
 				return new IntervaloDeTempo(tempoMedioEntrePostagens, UnidadeDeTempo.Horas);
 			}else{
 				tempoMedioEntrePostagens *= 60;
 				if(tempoMedioEntrePostagens >= 1f){
 					return new IntervaloDeTempo(tempoMedioEntrePostagens, UnidadeDeTempo.Minutos);
 				}else{
 					tempoMedioEntrePostagens *= 60;
 					return new IntervaloDeTempo(tempoMedioEntrePostagens, UnidadeDeTempo.Segundos);
 				}
 			}
 		}
 
 	}
 
 	public void posta(String endereco, int dia, int mes, int ano, int hora, int minuto) {
		links.add(new Link(endereco, dia, mes, ano, hora, minuto));	
 	}
 	/**
 	 * Retorna o site mais postado pelo usuario atual
 	 * @return o site mais postado pelo usuario
 	 */
 	public String getSiteMaisPostado() {
 		String siteMaisPostado= "";
 		int maximoDePostagens = 0;
 		for(String key : postagensDoSite.keySet()){
 			if(postagensDoSite.get(key) > maximoDePostagens){
 				siteMaisPostado = key;
 				maximoDePostagens = postagensDoSite.get(key);
 			}
 		}
 		return siteMaisPostado;
 	}
 	/**
 	 * Retorna uma lista contendo ate 10 dos links mais novos postados pelo usuario
 	 * @return uma {@link List} de {@link List}
 	 */
 	public List<Link> getUltimosLinksPostados(){
 		if(links.size() < 10){
 			return links.subList(0, links.size());
 		}else{
 			return links.subList(0, 10);
 		}
 	}
 
 }
