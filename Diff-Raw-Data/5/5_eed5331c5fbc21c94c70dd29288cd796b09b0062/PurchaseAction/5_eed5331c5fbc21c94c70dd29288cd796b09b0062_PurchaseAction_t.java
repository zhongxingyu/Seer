 package com.xone.action.app.purchase;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.action.base.LogicAction;
 import com.xone.model.hibernate.entity.ImageUploaded;
 import com.xone.model.hibernate.entity.Overhead;
 import com.xone.model.hibernate.entity.Product;
 import com.xone.model.hibernate.entity.Purchase;
 import com.xone.model.utils.MyDateUtils;
 import com.xone.model.utils.MyModelUtils;
 import com.xone.service.app.OverheadService;
 import com.xone.service.app.PurchaseService;
 import com.xone.service.app.utils.AppConstants;
 import com.xone.service.app.utils.MyBeanUtils;
 import com.xone.service.app.utils.MyBeanUtils.CopyRules;
 
 public class PurchaseAction extends LogicAction {
 	
 	private static final long serialVersionUID = 8114006617561140444L;
 	
 	@Autowired
 	protected PurchaseService purchaseService;
 	
 	@Autowired
 	protected OverheadService overheadService;
 	
 	protected Purchase purchase = new Purchase();
 	protected Overhead overhead = new Overhead();
 	protected ImageUploaded imageUploaded;
 	protected List<Purchase> list = new ArrayList<Purchase>();
 	protected String imageUploadPath;
 	
 	public String listAll() {
 		return SUCCESS;
 	}
 	
 	public String listSales() {
 		return SUCCESS;
 	}
 	
 	public String listGroups() {
 		return SUCCESS;
 	}
 	
 	public String indexAdd() {
 		return SUCCESS;
 	}
 	
 	/**
 	 * 置顶申请
 	 * @return
 	 */
 	public String doTopApply() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("refId", getOverhead().getRefId().toString());
 		params.put("overheadType", getOverhead().getOverheadType());
 		Overhead overhead = getOverheadService().findByMap(params);
 		if (null != overhead && null != overhead.getId()) {
 			return SUCCESS;
 		}
 		Long userId = getUserId();
 		getOverhead().setOverheadType(Overhead.OverheadType.PURCHASE.getValue());
 		getOverhead().setUserApply(userId);
 		getOverhead().setDateApply(new Date());
 		getOverhead().setUserCreated(userId);
 		getOverheadService().save(getOverhead());
 		return SUCCESS;
 	}
 	
 	public String create() {
 		getPurchase().setUserCreated(getUserId());
 		getPurchase().setCheckStatus(Purchase.CheckStatus.WAITING.getValue());
 		List<ImageUploaded> images = super.createImageByParams(getImageUploadPath(), ImageUploaded.RefType.PURCHASE);
 		setPurchase(getPurchaseService().save(getPurchase(), images));
 		return SUCCESS;
 	}
 	
 	/**
 	 * 查询产品顶置列表
 	 * @return
 	 */
 	public String listOverheadItems() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("overheadType", Overhead.OverheadType.PURCHASE.getValue());
 		params.put("checkStatus", Overhead.CheckStatus.PASSED.getValue());
 		params.put("flagDeleted", Overhead.FlagDeleted.NORMAL.getValue());
 		List<Overhead> list = getOverheadService().findAllByMap(params);
 		if (null != list && !list.isEmpty()) {
 			List<Long> ids = new ArrayList<Long>();
 			for (Overhead o : list) {
 				ids.add(o.getRefId());
 			}
 			params.clear();
 			params.put("checkStatus", Product.CheckStatus.PASSED.getValue());
 			params.put("flagDeleted", Product.FlagDeleted.NORMAL.getValue());
 			List<Purchase> pList = getPurchaseService().findAllByIds(ids, params);
 			if (null != pList && !pList.isEmpty()) {
 				getList().addAll(pList);
 			}
 		}
 		return SUCCESS;
 	}
 	
 	public String listItems() {
 		Map<String, String> map = getRequestMap();
 		Map<String, Object> params = new HashMap<String, Object>();
 		int length = MyModelUtils.parseInt(map.get("itemcount"), 0);
 		if (null == getPurchase()) {
 			setPurchase(new Purchase());
 		}
 		if (length >= AppConstants.LIST_ITEM_LENGTH) {
 			getMapValue().put("ITEM_TOO_LONG", "YES");
 		} else {
 			if ("down".equals(map.get("itemaction")) && null != getPurchase().getDateCreated()) {
 				params.put("gtDateCreated", MyDateUtils.format(getPurchase().getDateCreated()));
 			} else if ("up".equals(map.get("itemaction")) && null != getPurchase().getDateCreated()) {
 				params.put("ltDateCreated", MyDateUtils.format(getPurchase().getDateCreated()));
 			}
 			if (!StringUtils.isBlank(getPurchase().getPurchaseName())) {
 				params.put("purchaseName", getPurchase().getPurchaseName());
 			}
			if (!StringUtils.isBlank(map.get("exIds"))) {
				params.put("exIds", Arrays.asList(map.get("exIds").split(",")));
			}
 			params.put("userLevels", getLogicUserLevel());
 			params.put("checkStatus", Purchase.CheckStatus.PASSED.getValue());
 			params.put("flagDeleted", Purchase.FlagDeleted.NORMAL.getValue());
 			setList(getPurchaseService().findAllByMap(params));
 		}
 		return SUCCESS;
 	}
 	
 	public String listAllForUser() {
 		return SUCCESS;
 	}
 	
 	public String listItemsForUser() {
 		Map<String, String> map = getRequestMap();
 		Map<String, String> params = new HashMap<String, String>();
 		if ("down".equals(map.get("itemaction"))) {
 			params.put("gtDateCreated", MyDateUtils.format(getPurchase().getDateCreated()));
 		} else if ("up".equals(map.get("itemaction"))) {
 			params.put("ltDateCreated", MyDateUtils.format(getPurchase().getDateCreated()));
 		}
 		params.put("userCreated", String.valueOf(getUserId()));
 		setList(getPurchaseService().findAllByMapForUser(params));
 		return SUCCESS;
 	}
 	
 	public String updateItem() {
 		Long id = getPurchase().getId();
 		if (null != id) {
 			Purchase p = findById(id);
 			if (null == p || null == p.getId()) {
 				getMapValue().put("msg", "无此记录.");
 				return ERROR;
 			}
 			if (Purchase.CheckStatus.PASSED.getValue().equals(p.getCheckStatus())) {
 				getMapValue().put("msg", "已经通过审核的信息不能进行更新操作");
 				return ERROR;
 			}
 			setPurchase(p);
 			return SUCCESS;
 		}
 		getMapValue().put("msg", "缺失数据标识.");
 		return ERROR;
 	}
 	
 	protected Purchase findById(Long id) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("id", String.valueOf(id));
 		return purchaseService.findByMap(params);
 	}
 	
 	public String update() {
 		Purchase pu = getPurchase();
 		if (null == pu || null == pu.getId()) {
 			getMapValue().put("msg", "无此记录.");
 			return ERROR;
 		}
 		pu.setUserUpdated(getUserId());
 		Purchase entity = findById(pu.getId());
 		if (Purchase.CheckStatus.PASSED.getValue().equals(entity.getCheckStatus())) {
 			getMapValue().put("msg", "已经通过审核的信息不能进行更新操作");
 			return ERROR;
 		}
 		MyBeanUtils.copyProperties(pu, entity, Purchase.class, new String[] {
 			"ids"
 		}, new CopyRules() {
 			@Override
 			public boolean myCopyRules(Object value) {
 				return (null != value);
 			}
 		});
 		List<ImageUploaded> imageUploadeds = super.createImageByParams(getImageUploadPath(), ImageUploaded.RefType.PURCHASE);
 		entity = getPurchaseService().update(entity, imageUploadeds, pu.getIds());
 		setPurchase(entity);
 		return SUCCESS;
 	}
 	
 	public String item() {
 		return SUCCESS;
 	}
 	
 	public String itemForUser() {
 		return SUCCESS;
 	}
 	
 	public String itemDetails() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("id", String.valueOf(getPurchase().getId()));
 		params.put("checklist", "purchaseCheckList");
 		setPurchase(getPurchaseService().findByMap(params));
 		return SUCCESS;
 	}
 
 	public String getImageUploadPath() {
 		return imageUploadPath;
 	}
 
 	public void setImageUploadPath(String imageUploadPath) {
 		this.imageUploadPath = imageUploadPath;
 	}
 
 	public List<Purchase> getList() {
 		return list;
 	}
 
 	public void setList(List<Purchase> list) {
 		this.list = list;
 	}
 
 	public Purchase getPurchase() {
 		return purchase;
 	}
 
 	public void setPurchase(Purchase purchase) {
 		this.purchase = purchase;
 	}
 
 	public OverheadService getOverheadService() {
 		return overheadService;
 	}
 
 	public void setOverheadService(OverheadService overheadService) {
 		this.overheadService = overheadService;
 	}
 
 	public Overhead getOverhead() {
 		return overhead;
 	}
 
 	public void setOverhead(Overhead overhead) {
 		this.overhead = overhead;
 	}
 
 	public ImageUploaded getImageUploaded() {
 		return imageUploaded;
 	}
 
 	public void setImageUploaded(ImageUploaded imageUploaded) {
 		this.imageUploaded = imageUploaded;
 	}
 
 	public PurchaseService getPurchaseService() {
 		return purchaseService;
 	}
 
 	public void setPurchaseService(PurchaseService purchaseService) {
 		this.purchaseService = purchaseService;
 	}
 	
 }
