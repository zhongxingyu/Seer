 package com.chezhu.web;
 
 import java.util.List;
 
 
 import com.chezhu.dao.BrandService;
 import com.chezhu.dao.StyleService;
 import com.chezhu.dao.model.Brand;
 import com.chezhu.dao.model.Style;
 import com.chezhu.util.ContextUtil;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class StyleAction extends ActionSupport {
 	
 	private int id;
 	
 	private int brandId;
 	private String brandName;
 	private String styleName;
 	private String styleMotor;
 	private String styleOutter;
 	private BrandService brandService = ContextUtil.getBean(BrandService.class, "brandService");
 	private StyleService styleService = ContextUtil.getBean(StyleService.class, "styleService");
 	
 	
 	public String getStyleMotor() {
 		return styleMotor;
 	}
 
 	public void setStyleMotor(String styleMotor) {
 		this.styleMotor = styleMotor;
 	}
 
 	public String getStyleOutter() {
 		return styleOutter;
 	}
 
 	public void setStyleOutter(String styleOutter) {
 		this.styleOutter = styleOutter;
 	}
 
 	public BrandService getBrandService() {
 		return brandService;
 	}
 
 	public void setBrandService(BrandService brandService) {
 		this.brandService = brandService;
 	}
 
 	public StyleService getStyleService() {
 		return styleService;
 	}
 
 	public void setStyleService(StyleService styleService) {
 		this.styleService = styleService;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 
 	public int getBrandId() {
 		return brandId;
 	}
 
 	public void setBrandId(int brandId) {
 		this.brandId = brandId;
 	}
 
 	public String getBrandName() {
 		return brandName;
 	}
 
 	public void setBrandName(String brandName) {
 		this.brandName = brandName;
 	}
 
 	public String getStyleName() {
 		return styleName;
 	}
 
 	public void setStyleName(String styleName) {
 		this.styleName = styleName;
 	}
 
 	public String add() throws Exception {
 		Style style = new Style();
 		style.setBid(brandId);
 		Brand brand = brandService.fetch(brandId);
 		style.setName(styleName);
 		style.setOutter(styleOutter);
 		style.setMotor(styleMotor);
		style.setFullName(brand.getName() + " " + styleName + " " + styleOutter + " " + styleMotor);
 		styleService.addStyle(style);
 		
 		brand = brandService.fetchLinks(brandId);
 		List<Brand> brands = brandService.getAllBrands();
 		List<Style> styles = brand.getStyles();
 		
 		ActionContext ctx = ActionContext.getContext();
 		ctx.put("brands", brands);
 		ctx.put("styles", styles);
 		
 		return SUCCESS;
 	}
 	
 	public String init() throws Exception {
 		List<Brand> brands = brandService.getAllBrands();
 		Brand brand = brands.get(0);
 		brand = brandService.fetchLinks(brand.getId());
 		brandId = brand.getId();
 			
 		ActionContext ctx = ActionContext.getContext();
 		ctx.put("brands", brands);
 		ctx.put("styles", brand.getStyles());
 		
 		return SUCCESS;
 	}
 	
 	public String change() throws Exception {
 		List<Brand> brands = brandService.getAllBrands();
 		Brand brand = brandService.fetchLinks(brandId);
 			
 		ActionContext ctx = ActionContext.getContext();
 		ctx.put("brands", brands);
 		ctx.put("styles", brand.getStyles());
 		
 		return SUCCESS;
 	}
 	
 	public String delete() throws Exception {
 		Style style = styleService.fetch(id);
 		brandId = style.getBid();
 		styleService.delete(id);
 		
 		List<Brand> brands = brandService.getAllBrands();
 		Brand brand = brandService.fetchLinks(brandId);
 		
 		ActionContext ctx = ActionContext.getContext();
 		ctx.put("brands", brands);
 		ctx.put("styles", brand.getStyles());
 		
 		return SUCCESS;
 	}
 	
 	public String view() throws Exception {
 		Style style = styleService.fetch(id);
 		styleMotor = style.getMotor();
 		styleOutter = style.getOutter();
 		brandId = style.getBid();
 		Brand brand = brandService.fetch(brandId);
 		brandName = brand.getName();
 		styleName = style.getName();
 		
 		return INPUT;
 	}
 	
 	public String save() throws Exception {
 		Style style = styleService.fetch(id);
 		Brand brand = brandService.fetch(brandId);
 		style.setName(styleName);
 		style.setMotor(styleMotor);
 		style.setOutter(styleOutter);
		style.setFullName(brand.getName() + " " + styleName + " " + styleOutter + " " + styleMotor);
 		styleService.updateStyle(style);
 		
 		brandId = style.getBid();
 		
 		brand = brandService.fetchLinks(brandId);
 		List<Brand> brands = brandService.getAllBrands();
 		List<Style> styles = brand.getStyles();
 		
 		ActionContext ctx = ActionContext.getContext();
 		ctx.put("brands", brands);
 		ctx.put("styles", styles);
 		
 		return SUCCESS;
 	}
 }
