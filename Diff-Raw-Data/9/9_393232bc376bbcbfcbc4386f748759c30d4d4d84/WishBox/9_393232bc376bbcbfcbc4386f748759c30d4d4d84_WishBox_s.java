 package com.ngdb.web.pages;
 
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import com.ngdb.entities.Population;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.shop.Wish;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.Category;
 
 public class WishBox {
 
 	@Property
 	private Wish wish;
 
 	@Property
 	private List<Wish> wishes;
 
 	private Category category;
 
 	@Inject
 	private com.ngdb.entities.WishBox wishBox;
 
 	@Inject
 	private Population population;
 
 	private Long id;
 
 	void onActivate(String category, String value) {
 		if (isNotBlank(category)) {
 			this.category = Category.valueOf(Category.class, category);
 			if (StringUtils.isNumeric(value)) {
 				id = Long.valueOf(value);
 			}
 		}
 	}
 
 	@SetupRender
 	public void setupRender() {
 		if (category == null || category == Category.none) {
 			this.wishes = wishBox.findAllWishes();
 		} else {
 			switch (category) {
 			case byUser:
 				User user = population.findById(id);
 				this.wishes = new ArrayList<Wish>(user.getWishes());
 				break;
 			}
 		}
 		Collections.sort(this.wishes);
 	}
 
 	public String getViewPage() {
		if (wish.getArticle() instanceof Game) {
 			return "article/game/gameView";
 		}
 		return "article/hardware/hardwareView";
 	}
 
 	static int evenOdd = 0;
 
 	public String getRowClass() {
 		evenOdd++;
 		if (evenOdd % 2 == 0) {
 			return "odd";
 		}
 		return "even";
 	}
 }
