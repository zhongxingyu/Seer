 package test;
 
 import java.util.HashSet;
 import java.util.List;
 
 import model.GameType;
 import model.Review;
 import dao.ReviewDAO;
 
 public class ConvertDatabaseToARFF {
 	
 	public static void main(String[] args) {
 		ReviewDAO dao = new ReviewDAO();
 		
 		List<Review> reviews = dao.listAllReviews();
 		
 		// produtora
 		HashSet<String> produtoras = new HashSet<String>();
 		HashSet<Integer> anos = new HashSet<Integer>();
 		HashSet<String> tipos = new HashSet<String>();
 		HashSet<Integer> idades = new HashSet<Integer>();
 		for (Review review : reviews) {
 			produtoras.add(review.getProducer().replace(" ", "-"));
 			anos.add(review.getYear());
 			tipos.add(review.getGameType().getName().replace(" ", "-"));
 			idades.add(review.getAge());
 		}
 		
 		System.out.println("@RELATION Jogos");
 		System.out.println("");
 		System.out.println("@attribute nameGame string");
 		System.out.println("@attribute produtora " + produtoras);
 		System.out.println("@attribute ano " + anos);
 		System.out.println("@attribute multiplayer {true,false}");
 		System.out.println("@attribute tipo " + tipos);
 		System.out.println("@attribute grafico numeric");
 		System.out.println("@attribute jogabilidade numeric");
 		System.out.println("@attribute diversao numeric");
 		System.out.println("@attribute audio numeric");
 		System.out.println("@attribute notaFinal numeric");
 		System.out.println("@attribute idade " + idades);
 		System.out.println("");
 		System.out.println("@data");
 		
 		for (Review r : reviews) {
			System.out.println(r.getGame().getName().replace(" ", "-").replace("{", "").replace("}", "").replace(",", "") + "," 
 					+ r.getProducer().replace(" ", "-") + ","
 					+ r.getYear() + ","
 					+ r.isMultiplayer() + ","
 					+ r.getGameType().getName().replace(" ", "-") + ","
 					+ r.getGradeGraphic() + ","
 					+ r.getGradeJogability() + ","
 					+ r.getGradeFun() + ","
 					+ r.getGradeSound() + ","
					+ r.getGradeContent() + ","
 					+ r.getAge()
 					);
 			
 		}
 		
 		
 		
 	}
 
 }
