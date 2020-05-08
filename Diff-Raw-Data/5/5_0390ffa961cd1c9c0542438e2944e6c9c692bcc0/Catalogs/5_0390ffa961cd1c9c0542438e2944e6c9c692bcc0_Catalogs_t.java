 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xml.catalog2;
 
 import org.trancecode.annotation.Nullable;
 import org.trancecode.core.AbstractImmutableHashCodeObject;
 import org.trancecode.core.AbstractImmutableObject;
 import org.trancecode.core.collection.TubularIterables;
 import org.trancecode.core.function.TubularFunctions;
 import org.trancecode.io.UriFunctions;
 import org.trancecode.io.Uris;
 
 import java.net.URI;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 
 /**
  * Utility methods related to {@link Catalog}.
  * 
  * @author Herve Quiroz
  * @version $Revision$
  */
 public final class Catalogs
 {
 	private Catalogs()
 	{
 		// No instantiation
 	}
 
 
 	public static Function<CatalogQuery, URI> defaultCatalog()
 	{
 		return DefaultCatalog.INSTANCE;
 	}
 
 
 	private static class DefaultCatalog implements Function<CatalogQuery, URI>
 	{
 		public static DefaultCatalog INSTANCE = new DefaultCatalog();
 
 
 		private DefaultCatalog()
 		{
 			// Singleton
 		}
 
 
 		@Override
 		public URI apply(final CatalogQuery query)
 		{
 			if (query.systemId != null)
 			{
 				return Uris.createUri(query.systemId);
 			}
 
 			return Uris.resolve(query.href, query.base);
 		}
 	}
 
 
 	public static Function<CatalogQuery, URI> routingCatalog(final Function<CatalogQuery, URI>... catalogEntries)
 	{
 		return routingCatalog(ImmutableList.of(catalogEntries));
 	}
 
 
 	public static Function<CatalogQuery, URI> routingCatalog(final Iterable<Function<CatalogQuery, URI>> catalogEntries)
 	{
 		return new RoutingCatalog(catalogEntries);
 	}
 
 
 	private static class RoutingCatalog extends AbstractImmutableObject implements Function<CatalogQuery, URI>
 	{
 		private final Iterable<Function<CatalogQuery, URI>> catalogEntries;
 
 
 		public RoutingCatalog(final Iterable<Function<CatalogQuery, URI>> catalogEntries)
 		{
 			super(catalogEntries);
 			Preconditions.checkNotNull(catalogEntries);
 			this.catalogEntries = catalogEntries;
 		}
 
 
 		@Override
 		public URI apply(final CatalogQuery query)
 		{
 			return Iterables.find(TubularIterables.applyFunctions(catalogEntries, query), Predicates.notNull());
 		}
 	}
 
 
 	public static Function<CatalogQuery, URI> setBaseUri(
 		final URI baseUri, @Nullable final Function<CatalogQuery, URI> catalog)
 	{
 		if (baseUri == null)
 		{
 			return catalog;
 		}
 
 		return Functions.compose(UriFunctions.resolveUri(baseUri), catalog);
 	}
 
 
 	public static Function<CatalogQuery, URI> addCache(final Function<CatalogQuery, URI> catalog)
 	{
 		return TubularFunctions.cache(catalog);
 	}
 
 
 	public static Function<CatalogQuery, URI> rewriteSystem(final String systemIdStartString, final String rewritePrefix)
 	{
 		return new RewriteSystem(systemIdStartString, rewritePrefix);
 	}
 
 
 	private static class RewriteSystem extends AbstractImmutableHashCodeObject implements Function<CatalogQuery, URI>
 	{
 		private final String systemIdStartString;
 		private final String rewritePrefix;
 
 
 		public RewriteSystem(final String systemIdStartString, final String rewritePrefix)
 		{
 			super(systemIdStartString, rewritePrefix);
 			this.systemIdStartString = systemIdStartString;
 			this.rewritePrefix = rewritePrefix;
 		}
 
 
 		@Override
 		public URI apply(final CatalogQuery query)
 		{
 			if (query.systemId != null && query.systemId.startsWith(systemIdStartString))
 			{
 				final String suffix = query.systemId.substring(systemIdStartString.length());
 				return Uris.createUri(rewritePrefix + suffix);
 			}
 
 			return null;
 		}
 	}
 
 
	public static Function<CatalogQuery, URI> rewriteUri(final String uriStartString, final String rewritePrefix)
 	{
		return new RewriteUri(uriStartString, rewritePrefix);
 	}
 
 
 	private static class RewriteUri extends AbstractImmutableHashCodeObject implements Function<CatalogQuery, URI>
 	{
 		private final String uriStartString;
 		private final String rewritePrefix;
 
 
 		public RewriteUri(final String uriStartString, final String rewritePrefix)
 		{
 			super(uriStartString, rewritePrefix);
 			this.uriStartString = uriStartString;
 			this.rewritePrefix = rewritePrefix;
 		}
 
 
 		@Override
 		public URI apply(final CatalogQuery query)
 		{
 			final String uriString;
 			if (query.href != null || query.base != null)
 			{
 				uriString = Uris.resolve(query.href, query.base).toString();
 			}
 			else
 			{
 				uriString = query.systemId;
 			}
 
 			if (uriString != null && uriString.startsWith(uriStartString))
 			{
 				final String suffix = uriString.substring(uriStartString.length());
 				return Uris.createUri(rewritePrefix + suffix);
 			}
 
 			return null;
 		}
 	}
 
 
 	public static Function<CatalogQuery, URI> group(
 		final URI baseUri, final Iterable<Function<CatalogQuery, URI>> catalogs)
 	{
 		return setBaseUri(baseUri, routingCatalog(catalogs));
 	}
 }
