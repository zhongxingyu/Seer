package pt.piskeirinho.multi_actor_harvester_with_remoting.config_and_utils;
 
 import java.util.Arrays;
 import java.util.List;
 
 public final class Configs {
 	public static final List<String> repositoriesURLs = Arrays.asList(
 //			"http://arca.igc.gulbenkian.pt/oaiextended/request",
 			"http://repositorio.esepf.pt/oai/request",
 			"http://digituma.uma.pt/oaiextended/request",
 			// "http://iconline.ipleiria.pt/oaiextended/request",
 			"http://iconline.ipleiria.pt/oaiextended/driver",
 			"http://repositorio.insa.pt/oaiextended/request",
 //			"http://repositorio.hff.min-saude.pt/oaiextended/request",
 			"http://repositorio.ipsantarem.pt/oaiextended/request");
 	public static final String METADATA_PREFIX = "oai_dc";
 	public static final String SET_SPEC = null;
 	public static final int DETAULT_TIMEOUT = 10000;
 	public static final int DEFAULT_RETRIES = 3;
 }
