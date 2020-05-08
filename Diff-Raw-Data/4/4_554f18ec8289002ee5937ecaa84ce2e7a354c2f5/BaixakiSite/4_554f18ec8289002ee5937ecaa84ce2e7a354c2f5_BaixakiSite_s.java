 package test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Game;
 import model.Review;
 
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.safari.SafariDriver;
 import org.testng.Assert;
 
 import dao.ReviewDAO;
 
 public class BaixakiSite extends BaseTest {
 
 	@Test
 	public void test(){
 		html = new SafariDriver();
 		dao = new ReviewDAO();
 
 		html.get("http://www.baixakijogos.com.br/analises");
 
 		pause(3000);
 
 		ArrayList<String> links = getReviewLinks();
 
 		System.out.println(links);
 		
 
 		for (String link : links) {
 
 			String linkDados = link.replace("/analise", "");
 			// abrir a página
 			html.get(linkDados);
 	
 			
 			// desenvolvedora / produtora
 			WebElement info = html.findElement(By.className("gameinfo"));
 			
 			String producer = info.findElements(By.tagName("li")).get(0).findElement(By.tagName("a")).getText();
 			
 			// ano 
 			String[] syear = info.findElements(By.tagName("li")).get(2).getText().split(" "); 
 			int year =  Integer.parseInt(syear[syear.length - 1]);
 			
 			// multiplayer
			int numberOfPlayers = Integer.parseInt(info.findElements(By.tagName("li")).get(6).getText().split(" ")[0].replace("-", ""));
 			boolean isMultiplayer = numberOfPlayers > 1;
 			
 			String nameGame = getFromHtmlNameGame();
 			
 			Review review = new Review();
 
 			Game game = new Game();
 			game.setName(nameGame);
 			
 			
 			review.setGame(game);
 			review.setProducer(producer);
 			review.setYear(year);
 			review.setMultiplayer(isMultiplayer);
 			
 			// verifica se existe o jogo com o nome dado
 			boolean getGrades = true;
 			Review reviewDatabase = dao.selectReviewByNameGame(nameGame);
 			if (reviewDatabase != null) {
 				//jogo existe
 				
 				review.setId(reviewDatabase.getId());
 				
 				// se nao tiver nota iremos pegar todas as notas
 				// só estou verificando o grafico, jogabilidade e som
 				// pq na base existem várias linhas que n tem 
 				// esses 3 preenchidos
 				if (!(reviewDatabase.getGradeGraphic() == -1 &&
 						reviewDatabase.getGradeJogability() == -1 &&
 						reviewDatabase.getGradeSound() == -1)) {
 					
 					getGrades = false;
 					
 					review.setUrl(reviewDatabase.getUrl());
 					review.setGameType(reviewDatabase.getGameType());
 					
 					review.setGradeContent(reviewDatabase.getGradeContent());
 					
 					review.setGradeFun(reviewDatabase.getGradeFun());
 					review.setGradeGraphic(reviewDatabase.getGradeGraphic());
 					review.setGradeJogability(reviewDatabase.getGradeJogability());
 					review.setGradeSound(reviewDatabase.getGradeSound());
 					
 				}
 			}
 			
 			if (getGrades) {
 				// pega as notas
 				int[] notas = getGrades(link);
 				
 				review.setGradeGraphic(notas[0]);
 				review.setGradeJogability(notas[1]);
 				review.setGradeSound(notas[2]);
 				review.setGradeFun(notas[3]);
 				review.setGradeContent(notas[4]);
 				
 				// atualiza a url
 				review.setUrl(linkDados);
 			}
 			
 			// enfim ... salva o jogo
 			saveReview(review);
 		}
 
 	}
 	
 	public int[] getGrades(String link) {
 		int[] grades = {-1, -1, -1, -1, -1};
 		
 		
 		html.get(link);
 		pause(3000);
 		
 		// grafico / visual
 		WebElement wgrades = html.findElement(By.className("grades"));
 		int n0 = Integer.parseInt(wgrades.findElements(By.tagName("li")).get(0).findElement(By.xpath("//span[2]/span")).getText().split(" ")[1]);
 		grades[0] = n0 / 10;
 		
 		// jogabilidade
 		n0 = Integer.parseInt(wgrades.findElements(By.tagName("li")).get(1).findElement(By.xpath("//span[2]/span")).getText().split(" ")[1]);
 		grades[1] = n0 / 10; 
 		
 		// som
 		n0 = Integer.parseInt(wgrades.findElements(By.tagName("li")).get(2).findElement(By.xpath("//span[2]/span")).getText().split(" ")[1]);
 		grades[2] = n0 / 10; 
 		
 		// diversao
 		n0 = Integer.parseInt(wgrades.findElements(By.tagName("li")).get(3).findElement(By.xpath("//span[2]/span")).getText().split(" ")[1]);
 		grades[3] = n0 / 10; 
 		
 		// conteudo
 		grades[4] = Integer.parseInt(html.findElement(By.xpath("//span[@class='num rating']")).getText());
 				
 		
 			
 		return grades;
 		
 	}
 
 
 	@Override
 	public ArrayList<String> getReviewLinks() {
 
 		ArrayList<String> links = new ArrayList<String>();
 
 		String btproximo = "//a[@class='next_page']";
 		try {
 			do {
 
 
 				System.out.println("Pega uma pagina ... ");
 
 				List<WebElement> elinks = html.findElements(By.xpath("//li[contains(@id,'review_')]/a[@class='tit color' and contains(@href,'/analise')]"));
 
 				for (WebElement webElement : elinks) {
 					links.add(webElement.getAttribute("href"));
 				}
 
 				html.findElement(By.xpath(btproximo)).click();
 				pause(4000);
 			} while (html.findElement(By.xpath(btproximo)) != null);
 		} catch(NoSuchElementException e) {
 			System.out.println("Pegou todas as paginas de UOL ... eu espero");
 		}
 
 
 		return links;
 	}
 
 	@Override
 	public String getFromHtmlNameGame() {
 
 		String name = html.findElement(By.className("gametitle")).getText().toLowerCase();
 		name = name.concat(" ");
 		// substituindo a numeracao
 		name = name.replaceAll(" 2 ", " ii ");
 		name = name.replaceAll(" 3 ", " iii ");
 		name = name.replaceAll(" 4 ", " iv ");
 		name = name.replaceAll(" 5 ", " v ");
 
 		// retirando dois pontos e traco
 		name = name.replaceAll(":", "");
 		name = name.replaceAll("-", "");
 		name = name.replaceAll("'", "");
 		name = name.replaceAll("&", "and");
 
 		System.out.println("Nome: " + name);
 		// se o ultimo caracter for um espaco, remove
 		if (name.toCharArray()[name.length()-1] == ' ') {
 			name = name.substring(0, name.length()-1);
 		}
 		
 		return name;
 	}
 
 }
