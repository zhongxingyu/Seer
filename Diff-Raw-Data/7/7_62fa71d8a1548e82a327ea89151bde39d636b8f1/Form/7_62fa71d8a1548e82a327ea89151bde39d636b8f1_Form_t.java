 package org.openxdata.designer.util;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.pivot.collections.ArrayList;
 import org.apache.pivot.collections.List;
 import org.apache.pivot.collections.ListListener;
 import org.apache.pivot.collections.Sequence;
 import org.apache.pivot.util.ListenerList;
 import org.fcitmuk.epihandy.Condition;
 import org.fcitmuk.epihandy.DynamicOptionDef;
 import org.fcitmuk.epihandy.FormDef;
 import org.fcitmuk.epihandy.OptionDef;
 import org.fcitmuk.epihandy.PageDef;
 import org.fcitmuk.epihandy.SkipRule;
 import org.fcitmuk.epihandy.ValidationRule;
 import org.openxdata.designer.idgen.DefaultIdGenerator;
 import org.openxdata.designer.idgen.ScarceIdGenerator;
 
 /**
  * 
  * @author brent
  * 
  */
 public class Form extends org.fcitmuk.epihandy.FormDef implements List<Page> {
 
 	private ScarceIdGenerator idGen = new DefaultIdGenerator(1, Short.MAX_VALUE);
 
 	// Question are assumed unique to the form
 	private ScarceIdGenerator questionIdGen = new DefaultIdGenerator(1,
 			Short.MAX_VALUE);
 
 	@SuppressWarnings("unchecked")
 	public Form(FormDef formDef) {
 
 		super(formDef);
 
 		// Patch up pages into alternative model representation
 		Vector<PageDef> pages = (Vector<PageDef>) getPages();
 		if (pages != null) {
 			for (int i = 0; i < pages.size(); i++) {
 				PageDef pageDef = pages.elementAt(i);
 				Page page = new Page(questionIdGen, pageDef);
 				pages.setElementAt(page, i);
 				idGen.reserveId(page.getPageNo());
 			}
 		}
 
 		// Patch up dynamic options
 		Hashtable<Short, DynamicOptionDef> dynOptionMap = (Hashtable<Short, DynamicOptionDef>) getDynamicOptions();
 		if (dynOptionMap != null) {
 			for (Map.Entry<Short, DynamicOptionDef> entry : dynOptionMap
 					.entrySet()) {
 				Hashtable<Short, Vector<OptionDef>> optionMap = entry
 						.getValue().getParentToChildOptions();
 				for (Map.Entry<Short, Vector<OptionDef>> optionEntry : optionMap
 						.entrySet()) {
 					Vector<OptionDef> options = optionEntry.getValue();
 					for (int i = 0; i < options.size(); i++)
 						options.set(i, new Option(options.get(i)));
 				}
 			}
 		}
 
 		// Patch up question id references that changed
 		Map<Short, Short> globalIdMap = new HashMap<Short, Short>();
 		for (Page p : this) {
 			globalIdMap.putAll(p.getModifiedQuestionIds());
 		}
 
 		if (dynOptionMap != null) {
			Hashtable<Short, DynamicOptionDef> renamedOptionMap = (Hashtable<Short, DynamicOptionDef>) getDynamicOptions();
 			for (Map.Entry<Short, DynamicOptionDef> entry : dynOptionMap
 					.entrySet()) {
 
 				Short sourceQuestionId = entry.getKey();
 				DynamicOptionDef optionDef = entry.getValue();
 
 				boolean sourceMoved = globalIdMap.containsKey(sourceQuestionId);
 				boolean targetMoved = globalIdMap.containsKey(optionDef
 						.getQuestionId());
 
 				if (sourceMoved && targetMoved) {
 					optionDef.setQuestionId(globalIdMap.get(optionDef
 							.getQuestionId()));
 					renamedOptionMap.put(globalIdMap.get(entry.getKey()),
 							optionDef);
 					dynOptionMap.remove(entry.getKey());
 				} else if (sourceMoved) {
 					renamedOptionMap.put(globalIdMap.get(entry.getKey()),
 							entry.getValue());
 					dynOptionMap.remove(entry.getKey());
 				} else if (targetMoved) {
 					optionDef.setQuestionId(globalIdMap.get(optionDef
 							.getQuestionId()));
 				}
 			}
			dynOptionMap.putAll(renamedOptionMap);
 		}
 
 		for (ValidationRule validationRule : (Vector<ValidationRule>) getValidationRules()) {
 			if (globalIdMap.keySet().contains(validationRule.getQuestionId())) {
 				validationRule.setQuestionId(globalIdMap.get(validationRule
 						.getQuestionId()));
 				for (Condition condition : (Vector<Condition>) validationRule
 						.getConditions()) {
 					if (globalIdMap.keySet()
 							.contains(condition.getQuestionId())) {
 						condition.setQuestionId(globalIdMap.get(condition
 								.getQuestionId()));
 					}
 				}
 			}
 		}
 
 		for (SkipRule skipRule : (Vector<SkipRule>) getSkipRules()) {
 			for (Condition condition : (Vector<Condition>) skipRule
 					.getConditions()) {
 				if (globalIdMap.containsKey(condition.getQuestionId())) {
 					condition.setQuestionId(globalIdMap.get(condition
 							.getQuestionId()));
 				}
 			}
 			Vector<Short> actionTargets = skipRule.getActionTargets();
 			for (int i = 0; i < actionTargets.size(); i++) {
 				if (globalIdMap.containsKey(actionTargets.get(i))) {
 					actionTargets.set(i, globalIdMap.get(actionTargets.get(i)));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void addPage() {
 		@SuppressWarnings("unchecked")
 		Vector<PageDef> pages = (Vector<PageDef>) getPages();
 		synchronized (pages) {
 			short pageId = (short) idGen.nextId();
 			Page newPage = new Page("Page" + pageId, pageId,
 					new Vector<Question>());
 			newPage.setQuestionIdGen(questionIdGen);
 			add(newPage); // Method sends notifications
 		}
 	}
 
 	public int remove(Page item) {
 
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		for (int i = 0; i < pages.size(); i++) {
 			if (item.equals(pages.get(i))) {
 				Page removedPage = pages.remove(i);
 				idGen.makeIdAvailable(removedPage.getPageNo());
 				listenerList.itemsRemoved(this, i, new ArrayList<Page>(
 						removedPage));
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public Page get(int index) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		return (Page) pages.get(index);
 	}
 
 	public int indexOf(Page item) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		return pages.indexOf(item);
 	}
 
 	public boolean isEmpty() {
 		return getPages().isEmpty();
 	}
 
 	class PageComparator implements Comparator<Page> {
 
 		public int compare(Page o1, Page o2) {
 			if (o1 == o2 && o1 == null)
 				return 0;
 
 			if (o1 == null)
 				return -1;
 
 			if (o2 == null)
 				return 1;
 
 			return o1.toString().compareTo(o2.toString());
 		}
 
 	}
 
 	private Comparator<Page> comparator = new PageComparator();
 
 	public Comparator<Page> getComparator() {
 		return comparator;
 	}
 
 	public Iterator<Page> iterator() {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		return pages.iterator();
 	}
 
 	public int add(Page item) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		int index = -1;
 		synchronized (pages) {
 			index = pages.size();
 			pages.add(item);
 			idGen.reserveId(item.getPageNo());
 			listenerList.itemInserted(this, index);
 			return index;
 		}
 	}
 
 	public void insert(Page item, int index) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		pages.insertElementAt(item, index);
 		idGen.reserveId(item.getPageNo());
 		listenerList.itemInserted(this, index);
 	}
 
 	public Page update(int index, Page item) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		Page exiled = null;
 		synchronized (pages) {
 			exiled = pages.get(index);
 			idGen.makeIdAvailable(exiled.getPageNo());
 			pages.setElementAt(item, index);
 			idGen.reserveId(item.getPageNo());
 		}
 		listenerList.itemUpdated(this, index, exiled);
 		return exiled;
 	}
 
 	public Sequence<Page> remove(int index, int count) {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		Sequence<Page> removedPages = new ArrayList<Page>();
 		for (int i = 0; i < count && index + i < pages.size(); i++) {
 			Page removedPage = pages.remove(index);
 			removedPages.add(removedPage);
 			idGen.makeIdAvailable(removedPage.getPageNo());
 		}
 		listenerList.itemsRemoved(this, index, removedPages);
 		return removedPages;
 	}
 
 	public void clear() {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		for (Page page : this) {
 			idGen.makeIdAvailable(page.getPageNo());
 		}
 		pages.clear();
 	}
 
 	public int getLength() {
 		@SuppressWarnings("unchecked")
 		Vector<Page> pages = (Vector<Page>) getPages();
 		return pages.size();
 	}
 
 	public void setComparator(Comparator<Page> comparator) {
 		this.comparator = comparator;
 	}
 
 	private ListListenerList<Page> listenerList = new ListListenerList<Page>();
 
 	public ListenerList<ListListener<Page>> getListListeners() {
 		return listenerList;
 	}
 }
