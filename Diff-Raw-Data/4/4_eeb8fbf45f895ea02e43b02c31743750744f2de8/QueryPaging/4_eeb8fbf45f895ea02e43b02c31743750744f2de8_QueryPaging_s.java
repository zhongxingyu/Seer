 package org.romaframework.frontend.domain.crud;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.romaframework.aspect.core.annotation.AnnotationConstants;
 import org.romaframework.aspect.core.feature.CoreFieldFeatures;
 import org.romaframework.aspect.persistence.Query;
 import org.romaframework.aspect.view.ViewCallback;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.annotation.ViewField;
 import org.romaframework.core.Roma;
 import org.romaframework.core.domain.entity.ComposedEntity;
 import org.romaframework.core.entity.EntityHelper;
 import org.romaframework.core.repository.GenericRepository;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaHelper;
 
 public class QueryPaging<T extends ComposedEntity<E>, E> implements PagingListener, ViewCallback {
 
 	@ViewField(label = "", render = ViewConstants.RENDER_OBJECTEMBEDDED, position = "form://paging")
 	protected CRUDPaging						paging;
 
 	@ViewField(render = ViewConstants.RENDER_TABLE, label = "", selectionField = "selected", position = "form://elements", enabled = AnnotationConstants.FALSE)
 	protected List<T>								elements;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected List<T>								selected	= new ArrayList<T>();
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected Query									query;
 
 	protected GenericRepository<E>	repository;
 
 	protected SchemaClass						entityClass;
 
 	protected SchemaClass						listableClass;
 
 	protected QueryPaging() {
 		this(10);
 	}
 
 	protected QueryPaging(int pageElements) {
 		paging = new CRUDPaging(this, pageElements);
 		List<SchemaClass> generics = SchemaHelper.getSuperclassGenericTypes(Roma.schema().getSchemaClass(this));
 		listableClass = generics.get(0);
 		entityClass = generics.get(1);
 		repository = Roma.repository(entityClass);
 	}
 
 	public QueryPaging(Class<T> listableClass, Class<E> entityClass) {
 		this(listableClass, entityClass, CRUDPaging.DEF_PAGE_ELEMENTS);
 	}
 
 	public QueryPaging(Class<T> listableClass, Class<E> entityClass, int pageElements) {
 		this(Roma.schema().getSchemaClass(listableClass), Roma.schema().getSchemaClass(entityClass), pageElements);
 	}
 
 	public QueryPaging(SchemaClass listableClass, SchemaClass entityClass, int pageElements) {
 		paging = new CRUDPaging(this, pageElements);
 		this.listableClass = listableClass;
 		this.entityClass = entityClass;
 		repository = Roma.repository(entityClass);
 	}
 
 	public void loadAllPages() {
 		query.setRangeFrom(0, query.getTotalItems());
 		executeQuery();
 	}
 
 	public void loadPage(int iFrom, int iTo) {
 		if (query == null) {
 			return;
 		}
 		query.setRangeFrom(iFrom, iTo);
 		executeQuery();
 	}
 
 	protected void search(Query query) {
 		this.query = query;
 		query.setRangeFrom(0, paging.getPageElements());
 		executeQuery();
 		if (paging != null) {
			paging.setTotalItems(query.getTotalItems());
 			paging.setCurrentPage(1);
 			Roma.fieldChanged(this, "paging");
 		}
 	}
 
 	protected void executeQuery() {
 		List<E> els = repository.findByCriteria(query);
 		try {
 			elements = new ArrayList<T>();
 			for (E o : els) {
 				elements.add(createListable(o));
 			}
 			Roma.fieldChanged(this, "elements");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	protected T createListable(E instance) throws Exception {
 		return (T) EntityHelper.createObject(instance, listableClass);
 	}
 
 	public void onShow() {
 		Roma.setFeature(this, "elements", CoreFieldFeatures.EMBEDDED_TYPE, listableClass);
 		Roma.setFeature(this, "selected", CoreFieldFeatures.EMBEDDED_TYPE, listableClass);
 	}
 
 	public void onDispose() {
 	}
 
 	public void selectAll() {
 		setSelected(new ArrayList<T>(getElements()));
 		Roma.fieldChanged(this, "elements");
 	}
 
 	public void deselectAll() {
 		setSelected(new ArrayList<T>());
 		Roma.fieldChanged(this, "elements");
 	}
 
 	public CRUDPaging getPaging() {
 		return paging;
 	}
 
 	public void setPaging(CRUDPaging paging) {
 		this.paging = paging;
 	}
 
 	public List<T> getElements() {
 		return elements;
 	}
 
 	public void setElements(List<T> elements) {
 		this.elements = elements;
 	}
 
 	public List<T> getSelected() {
 		return selected;
 	}
 
 	public void setSelected(List<T> selected) {
 		this.selected = selected;
 	}
 
 }
