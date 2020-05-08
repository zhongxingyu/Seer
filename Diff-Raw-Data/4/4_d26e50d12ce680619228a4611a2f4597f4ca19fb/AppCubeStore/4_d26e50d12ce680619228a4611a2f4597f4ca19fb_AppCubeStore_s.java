 package io.analytica.hcube.plugins.store.memory;
 
 import io.analytica.hcube.HCubeStore;
 import io.analytica.hcube.cube.HCube;
 import io.analytica.hcube.cube.HCubeBuilder;
 import io.analytica.hcube.dimension.HCategory;
 import io.analytica.hcube.dimension.HCubeKey;
 import io.analytica.hcube.dimension.HTime;
 import io.analytica.hcube.query.HQuery;
 import io.analytica.hcube.query.HQueryUtil;
 import io.analytica.hcube.result.HSerie;
 import io.vertigo.kernel.lang.Assertion;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 final class AppCubeStore {
 	private static final class QueueItem {
 		final HCubeKey cubeKey;
 		final HCube cube;
 
 		QueueItem(HCubeKey cubeKey, HCube cube) {
 			Assertion.checkNotNull(cubeKey);
 			Assertion.checkNotNull(cube);
 			//---------------------------------------------------------------------
 			this.cubeKey = cubeKey;
 			this.cube = cube;
 		}
 	}
 
 	private static final int QUEUE_SIZE = 5000;
 	private final List<QueueItem> queue;
 	private final Map<HCubeKey, HCube> store;
 	//---------------------------------------------------------------------
 	private final Set<HCategory> rootCategories;
 	private final Map<HCategory, Set<HCategory>> categories;
 	private final String appName;
 
 	AppCubeStore(final String appName) {
 		Assertion.checkArgNotEmpty(appName);
 		//---------------------------------------------------------------------
 		this.appName = appName;
 		queue = new ArrayList<>();
 		store = new HashMap<>();
 		//---------------------------------------------------------------------
 		rootCategories = new HashSet<>();
 		categories = new HashMap<>();
 	}
 
 	void push(final HCubeKey cubeKey, final HCube cube) {
 		Assertion.checkNotNull(cubeKey);
 		Assertion.checkNotNull(cube);
 		//---------------------------------------------------------------------
 		addCategory(cubeKey.getCategory());
 
 		//populate a queue
 		queue.add(new QueueItem(cubeKey, cube));
 		if (queue.size() > QUEUE_SIZE) {
 			flushQueue();
 		}
 	}
 
 	//flushing queue into store
 	private void flushQueue() {
 		for (final QueueItem item : queue) {
 			for (final HCubeKey upCubeKeys : item.cubeKey.drillUp()) {
 				merge(upCubeKeys, item.cube);
 			}
 		}
 		queue.clear();
 		printStats();
 	}
 
 	//On construit un nouveau cube  partir de l'ancien(peut tre null) et du nouveau.
 	private void merge(final HCubeKey cubeKey, final HCube cube) {
 
 		final HCube oldCube = store.get(cubeKey);
 		final HCube newCube;
 		if (oldCube != null) {
 			newCube = new HCubeBuilder().withMetrics(cube.getMetrics()).withMetrics(oldCube.getMetrics()).build();
 		} else {
 			newCube = cube;
 		}
 		store.put(cubeKey, newCube);
 	}
 
 	private void printStats() {
 		System.out.println("memStore : " + store.size() + " cubes");
 	}
 
 	Map<HCategory, HSerie> findAll(final HQuery query, final HCubeStore cubeStore) {
 		Assertion.checkNotNull(query);
 		Assertion.checkNotNull(cubeStore);
 		//---------------------------------------------------------------------
 		flushQueue();
 		//On itre sur les sries indexes par les catgories de la slection.
 		final Map<HCategory, HSerie> cubeSeries = new HashMap<>();
 
 		for (final HCategory category : HQueryUtil.findCategories(appName, query.getCategorySelection(), cubeStore)) {
			final Map<HTime, HCube> cubes = new HashMap<>();
 
 			for (HTime currentTime : HQueryUtil.findTimes(query.getTimeSelection())) {
 				final HCubeKey cubeKey = new HCubeKey(currentTime, category/*, null*/);
 				final HCube cube = store.get(cubeKey);
 				//---
 				//2 stratgies possibles : on peut choisir de retourner tous les cubes ou seulement ceux avec des donnes
 				cubes.put(currentTime, cube == null ? new HCubeBuilder().build() : cube);
 				/*if (cube != null) {
 					cubes.add(new HCubeBuilder(cubeKey).build());
 				}*/
 				//---
 				currentTime = currentTime.getDimension().next(currentTime.inMillis());
 			}
 			//A nouveau on peut choisir de retourner toutes les series ou seulement celles avec des donnes 
 			//if (!cubes.isEmpty()) {
 			cubeSeries.put(category, new HSerie(category, cubes));
 			//}
 		}
 		printStats();
 		return cubeSeries;
 	}
 
 	Set<HCategory> getAllSubCategories(HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 		Set<HCategory> set = categories.get(category);
 		return set == null ? Collections.<HCategory> emptySet() : Collections.unmodifiableSet(set);
 	}
 
 	Set<HCategory> getAllRootCategories() {
 		return Collections.unmodifiableSet(rootCategories);
 	}
 
 	private void addCategory(HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 		HCategory currentCategory = category;
 		HCategory parentCategory;
 		boolean drillUp;
 		do {
 			parentCategory = currentCategory.drillUp();
 			//Optim :Si la catgorie existe dj alors sa partie gauche aussi !!
 			//On dispose donc d'une info pour savoir si il faut remonter 
 			drillUp = doPut(parentCategory, currentCategory);
 			currentCategory = parentCategory;
 		} while (drillUp);
 	}
 
 	private boolean doPut(HCategory parentCategory, HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 		if (parentCategory == null) {
 			//category est une catgorie racine
 			rootCategories.add(category);
 			return false;
 		}
 		//category n'est pas une catgorie racine
 		Set<HCategory> set = categories.get(parentCategory);
 		if (set == null) {
 			set = new HashSet<>();
 			categories.put(parentCategory, set);
 		}
 		return set.add(category);
 	}
 
 	/** {@inheritDoc} */
 	/*@Override
 	public synchronized String toString() {
 		final StringBuilder sb = new StringBuilder();
 		for (final HCube cube : store.values()) {
 			sb.append(cube);
 			sb.append("\r\n");
 		}
 		return sb.toString();
 	}*/
 
 	long count() {
 		flushQueue();
 		return store.size();
 	}
 
 }
