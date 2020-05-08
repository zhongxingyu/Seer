 package com.ngdb.web.pages;
 
 import static com.google.common.collect.Collections2.filter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
import com.ngdb.Predicates;
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.Population;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.entities.shop.ShopItem;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.Category;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 
 public class Market {
 
 	@Inject
 	private com.ngdb.entities.Market market;
 
 	@Inject
 	private ArticleFactory articleFactory;
 
 	@Inject
 	private Population population;
 
 	private Category category;
 
 	@Inject
 	private CurrentUser currentUser;
 
 	private Long id;
 
 	@Property
 	private String username;
 
 	private List<Platform> platforms;
 
 	@Property
 	private ShopItem shopItem;
 
 	@Property
 	private Platform platform;
 
 	private List<Origin> origins;
 
 	@Property
 	private Origin origin;
 
 	@Inject
 	private ReferenceService referenceService;
 
 	void onActivate() {
 		this.origins = referenceService.getOrigins();
 		this.platforms = referenceService.getPlatforms();
 	}
 
 	void onActivate(String filter, String value) {
 		if (StringUtils.isNotBlank(filter)) {
 			this.category = Category.valueOf(Category.class, filter);
 			if (StringUtils.isNumeric(value)) {
 				this.id = Long.valueOf(value);
 				if (category == Category.byUser) {
 					username = population.findById(id).getLogin();
 				}
 			}
 		}
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public User getUser() {
 		return currentUser.getUser();
 	}
 
 	public void setUser(User user) {
 		this.id = user.getId();
 		this.category = Category.byUser;
 	}
 
 	public Collection<ShopItem> getShopItems() {
 		List<ShopItem> shopItems = new ArrayList<ShopItem>(findAllByOriginAndPlatform());
 		if (category != null) {
 			switch (category) {
 			case byArticle:
 				Article article = articleFactory.findById(id);
 				shopItems = filterBy(shopItems, article);
 				break;
 			case byUser:
 				User user;
 				if (id == null) {
 					user = currentUser.getUser();
 				} else {
 					user = population.findById(id);
 				}
 				shopItems = new ArrayList<ShopItem>(filterBy(shopItems, user));
 				break;
 			}
 		}
		shopItems = new ArrayList<ShopItem>(filter(shopItems, Predicates.shopItemsForSale));
 		Collections.sort(shopItems);
 		return shopItems;
 	}
 
 	private Collection<ShopItem> filterBy(Collection<ShopItem> shopItems, final User user) {
 		return Collections2.filter(shopItems, new Predicate<ShopItem>() {
 			@Override
 			public boolean apply(ShopItem input) {
 				return input.getSeller().getId().equals(user.getId());
 			}
 		});
 	}
 
 	private List<ShopItem> filterBy(Collection<ShopItem> shopItems, final Article article) {
 		return new ArrayList<ShopItem>(filter(shopItems, new Predicate<ShopItem>() {
 			@Override
 			public boolean apply(ShopItem input) {
 				return input.getArticle().getId().equals(article.getId());
 			}
 		}));
 	}
 
 	private Collection<ShopItem> findAllByOriginAndPlatform() {
 		Collection<ShopItem> shopItems = market.findAllByOriginAndPlatform(origin, platform);
 		return shopItems;
 	}
 
 	public List<Origin> getOrigins() {
 		return origins;
 	}
 
 	public List<Platform> getPlatforms() {
 		return platforms;
 	}
 
 }
