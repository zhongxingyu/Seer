 package org.openrdf.alibaba.decor.support;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.openrdf.alibaba.decor.Decoration;
 import org.openrdf.alibaba.decor.Representation;
 import org.openrdf.alibaba.decor.TextPresentation;
 import org.openrdf.alibaba.decor.TextPresentationImportBehaviour;
 import org.openrdf.alibaba.decor.base.TextPresentationBase;
 import org.openrdf.alibaba.decor.helpers.Context;
 import org.openrdf.alibaba.exceptions.AlibabaException;
 import org.openrdf.alibaba.exceptions.BadRequestException;
 import org.openrdf.alibaba.pov.Display;
 import org.openrdf.alibaba.pov.Intent;
 import org.openrdf.alibaba.pov.Perspective;
 import org.openrdf.alibaba.pov.PerspectiveOrSearchPattern;
 import org.openrdf.alibaba.pov.ReferencePerspective;
 import org.openrdf.alibaba.pov.SearchPattern;
 import org.openrdf.alibaba.vocabulary.DCR;
 import org.openrdf.concepts.rdfs.Class;
 import org.openrdf.concepts.rdfs.Resource;
 import org.openrdf.elmo.ElmoManager;
 import org.openrdf.elmo.ElmoQuery;
 import org.openrdf.elmo.Entity;
 import org.openrdf.elmo.annotations.rdf;
 
 @rdf(DCR.NS + "Presentation")
 public class TextPresentationImportSupport extends TextPresentationBase
 		implements TextPresentationImportBehaviour {
 	static class Lookup implements Entity {
 		private Map<String, Object> values = new HashMap<String, Object>();
 
 		public Set<String> getDisplayNames() {
 			return values.keySet();
 		}
 
 		public boolean containsDisplay(String name) {
 			return values.containsKey(name);
 		}
 
 		public Object getValue(String name) {
 			return values.get(name);
 		}
 
 		public void setValues(Display display, Collection<?> list) {
 			String name = display.getPovName();
 			if (list.isEmpty()) {
 				values.put(name, null);
 			} else {
 				assert list.size() == 1;
 				values.put(name, list.toArray()[0]);
 			}
 		}
 
 		public ElmoManager getElmoManager() {
 			return null;
 		}
 
 		public QName getQName() {
 			return null;
 		}
 
 		@Override
 		public String toString() {
 			return values.toString();
 		}
 	}
 
 	public TextPresentationImportSupport(TextPresentation presentation) {
 		super(presentation);
 	}
 
 	public void importPresentation(PerspectiveOrSearchPattern spec,
 			Entity target, Context ctx) throws AlibabaException, IOException {
 		presentation(spec, target, ctx);
 	}
 
 	@Override
 	protected void resources(PerspectiveOrSearchPattern spec,
 			Representation rep, List<Display> displays,
 			Collection<?> resources, Context ctx) throws AlibabaException,
 			IOException {
 		Map<String, Object> b = ctx.getBindings();
 		Decoration decor = rep.getPovDecoration();
 		if (decor.isBefore(b)) {
 			decor.before(b);
 			List<Object> pool = new ArrayList<Object>(resources);
 			List<Object> found = new ArrayList<Object>(pool.size());
 			do {
 				Object resource = importResource(spec, rep, displays, pool, ctx);
 				found.add(resource);
 				if (decor.isAfter(b))
 					break;
 				decor.separation(b);
 			} while (true);
 			decor.after(b);
 			resources.retainAll(found);
 			if (resources.size() != found.size()) {
 				resources.clear();
 				Collection coll = resources;
 				coll.addAll(found);
 			}
 		} else {
 			decor.empty(b);
 			resources.clear();
 		}
 	}
 
 	private Object importResource(PerspectiveOrSearchPattern spec,
 			Representation rep, List<Display> displays, List<Object> avail,
 			Context ctx) throws AlibabaException, IOException {
 		if (spec instanceof ReferencePerspective) {
 			ReferencePerspective ref = (ReferencePerspective) spec;
 			Lookup lookup = new Lookup();
 			resource(rep, displays, lookup, ctx);
 			return lookup(ref.getPovLookup(), lookup, ctx);
 		}
 		Decoration displayDecor = rep.getPovDisplayDecoration();
 		Object resource = findNextMatch(avail, displayDecor, ctx);
 		if (resource == null) {
 			resource = create(spec.getPovRepresents(), ctx);
 		}
 		resource(rep, displays, resource, ctx);
 		return resource;
 	}
 
 	private Object lookup(SearchPattern search, Lookup lookup, Context ctx)
 			throws AlibabaException, BadRequestException {
 		Set<String> filters = lookup.getDisplayNames();
 		String queryString = search.getSparqlQueryString(filters, null);
 		ElmoQuery<?> query = ctx.getElmoManager().createQuery(queryString);
 		for (Display binding : search.getBindings(filters, null)) {
 			String name = binding.getPovName();
 			if (!lookup.containsDisplay(name))
 				throw new BadRequestException(name);
 			Object value = lookup.getValue(name);
 			query.setParameter(name, value);
 		}
 		return query.getSingleResult();
 	}
 
 	private Object findNextMatch(List<Object> pool, Decoration decor,
 			Context ctx) throws AlibabaException, IOException {
 		Iterator<Object> iter = pool.iterator();
 		while (iter.hasNext()) {
 			Object resource = iter.next();
 			ctx.bind("resource", resource);
 			if (decor.isBefore(ctx.getBindings())) {
 				iter.remove();
 				return resource;
 			}
 		}
 		return null;
 	}
 
 	private Object create(Set<Class> represents, Context ctx) {
 		ElmoManager manager = ctx.getElmoManager();
 		Resource resource = manager.designate(Resource.class);
 		resource.setRdfTypes(represents);
		return manager.designateEntity(Resource.class, resource);
 	}
 
 	@Override
 	protected void display(Representation rep, Display display,
 			Object resource, Context ctx) throws AlibabaException, IOException {
 		Decoration decor = rep.findDecorationFor(display);
 		Collection<?> values = getValueOf(display, resource);
 		if (decor.isBefore(ctx.getBindings())) {
 			decor.before(ctx.getBindings());
 			if (display.getPovPerspective() != null) {
 				perspectiveDisplay(decor, display, resource, values, ctx);
 			} else if (display.getPovSearchPattern() != null) {
 				searchDisplay(decor, display, values, ctx);
 			} else {
 				literalDisplay(decor, display, resource, values, ctx);
 			}
 			decor.after(ctx.getBindings());
 		} else {
 			decor.empty(ctx.getBindings());
 			values.clear();
 			setValuesOf(display, resource, values);
 		}
 	}
 
 	private void perspectiveDisplay(Decoration decor, Display display,
 			Object resource, Collection<?> values, Context ctx)
 			throws AlibabaException, IOException {
 		Perspective spec = display.getPovPerspective();
 		Intent intent = ctx.getIntent();
 		if (decor.isSeparation()) {
 			Iterator<?> iter = values.iterator();
 			while (iter.hasNext()) {
 				Set<?> singleton = Collections.singleton(iter.next());
 				ctx.setIntent(spec.getPovPurpose());
 				resources(spec, singleton, ctx);
 				ctx.setIntent(intent);
 				if (iter.hasNext()) {
 					decor.separation(ctx.getBindings());
 				}
 			}
 		} else {
 			ctx.setIntent(spec.getPovPurpose());
 			resources(spec, values, ctx);
 			ctx.setIntent(intent);
 			setValuesOf(display, resource, values);
 		}
 	}
 
 	private void searchDisplay(Decoration decor, Display display,
 			Collection<?> values, Context ctx) throws AlibabaException,
 			IOException {
 		SearchPattern sp = display.getPovSearchPattern();
 		Iterator<?> iter = values.iterator();
 		while (iter.hasNext()) {
 			// FIXME what about value?
 			ElmoQuery<?> query = sp.createElmoQuery(ctx.getFilter(), ctx
 					.getOrderBy());
 			resources(sp, query.getResultList(), ctx);
 			if (iter.hasNext()) {
 				decor.separation(ctx.getBindings());
 			}
 		}
 	}
 
 	private void literalDisplay(Decoration decor, Display display,
 			Object resource, Collection<?> values, Context ctx)
 			throws AlibabaException, IOException {
 		values.clear();
 		decor.values(values, ctx.getBindings());
 		setValuesOf(display, resource, values);
 	}
 
 	private Collection<?> getValueOf(Display display, Object resource)
 			throws AlibabaException {
 		if (resource instanceof Lookup)
 			return new ArrayList<Object>();
 		return display.getValuesOf(resource);
 	}
 
 	private void setValuesOf(Display display, Object resource,
 			Collection<?> values) throws AlibabaException {
 		if (resource instanceof Lookup) {
 			Lookup lookup = (Lookup) resource;
 			lookup.setValues(display, values);
 		} else {
 			display.setValuesOf(resource, values);
 		}
 	}
 }
