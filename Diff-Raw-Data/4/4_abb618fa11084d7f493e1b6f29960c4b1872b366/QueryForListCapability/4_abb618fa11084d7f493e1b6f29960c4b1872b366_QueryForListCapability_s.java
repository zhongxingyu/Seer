 package com.jayway.forest.reflection;
 
 import com.jayway.forest.di.DependencyInjectionSPI;
 import com.jayway.forest.roles.FieldComparator;
 import com.jayway.forest.roles.Linkable;
 import com.jayway.forest.roles.Resource;
 
 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.*;
 import java.util.*;
 
 public class QueryForListCapability extends QueryCapability {
 
     private DependencyInjectionSPI dependencyInjectionSPI;
     private Map<Class, Transformer> transformers;
 
     public QueryForListCapability(DependencyInjectionSPI dependencyInjectionSPI, Map<Class, Transformer> transformers, Resource resource, Method method, String name, String documentation) {
         super(resource, method, name, documentation);
         this.dependencyInjectionSPI = dependencyInjectionSPI;
         this.transformers = transformers;
     }
 
     @Override
     public PagedSortedListResponse get(HttpServletRequest request) {
         // setup Paging and Sorting parameter
         UrlParameter urlParameter = new UrlParameter(request.getParameterMap());
 
         PagingParameter paging = pagingParameter( urlParameter );
         dependencyInjectionSPI.addRequestContext( PagingParameter.class, paging );
 
         Type type = method.getGenericReturnType();
         Class listElementClass = Object.class;
         Transformer transformer = null;
         if ( type instanceof ParameterizedType ) {
             Type listElementType = ((ParameterizedType) type).getActualTypeArguments()[0];
             // no nested lists yet
             if ( !(listElementType instanceof ParameterizedType) ) {
                 listElementClass = (Class) listElementType;
             }
            transformer = transformers.get( listElementClass );
         }
 
         SortingParameter sorting = sortingParameter( urlParameter );
         dependencyInjectionSPI.addRequestContext( SortingParameter.class, sorting );
 
         List<?> list = (List<?>) super.get(request);
 
         boolean sortingParameterFound = false;
         if ( listElementClass == Linkable.class || transformer != null || urlParameter.sortBy() != null) {
             sortingParameterFound = true;
             // If there is a transformer, we have to transform all element before sorting can be done
             transformList( list, transformer );
         }
 
         PagedSortedListResponse response = new PagedSortedListResponse();
         response.setName( name() );
 
         // infer the sortBy links
         List<String> sortingParameters = new LinkedList<String>();
         if ( list != null && !list.isEmpty() ) {
             inferSortParameters( sortingParameters, list.get(0).getClass() );
 
             for (String sort : sortingParameters) {
                 response.addOrderByAsc( sort, name() + urlParameter.linkSortBy(sort, true));
                 response.addOrderByDesc(sort, name() + urlParameter.linkSortBy(sort, false));
             }
         }
 
         if (!sorting.isTouched() && sortingParameterFound) {
             // the resource has not handled sorting so do it here
             Collections.sort(list, new FieldComparator(sorting.iterator()));
         }
 
 
         if ( paging.isTouched() ) {
             // the resource has handled the paging
             // so just copy the values to the pagedSortedListResponse
 
             response.setPage( paging.getPage() );
             response.setList( list );
             response.setPageSize( paging.getPageSize() );
             response.setTotalElements( paging.getTotalElements() );
             response.setTotalPages( calculateTotalPages(response.getTotalElements(), response.getPageSize()) );
             if ( paging.getPage() < response.getTotalPages() ) {
                 response.setNext( name() + urlParameter.linkTo( paging.getPage()+1) );
             }
             if ( paging.getPage() > 1 ) {
                 response.setPrevious( name() + urlParameter.linkTo( paging.getPage()-1) );
             }
         } else {
             // assume resource has not used the parameters
             // to split the result list according to the
             // passed in paging parameters
             Integer page = paging.getPage();
             Integer pageSize = paging.getPageSize();
 
             // build response
             response.setPage(page);
             response.setPageSize(pageSize);
             response.setTotalElements( list.size() );
             response.setTotalPages(  calculateTotalPages(response.getTotalElements(), response.getPageSize()) );
 
             int actualListSize = list.size();
             int maxIndex = page * pageSize;
             int minIndex = ( page - 1 )*pageSize;
             if (actualListSize >= minIndex) {
                 List<Object> resultList = new ArrayList<Object>();
                 for ( int i=minIndex; i<actualListSize && i<maxIndex; i++ ) {
                     resultList.add(list.get(i));
                 }
                 response.setList( resultList );
 
                 if ( maxIndex < actualListSize ) {
                     response.setNext( name() + urlParameter.linkTo( paging.getPage()+1) );
                 }
                 if ( minIndex > 0 ) {
                     response.setPrevious( name() + urlParameter.linkTo( paging.getPage()-1) );
                 }
             }
         }
 
         return response;
     }
 
     private void inferSortParameters(List<String> sortingParameters, Class<?> clazz) {
         if ( clazz != Object.class ) {
             for (Field field : clazz.getDeclaredFields()) {
                 if ( Modifier.isStatic( field.getModifiers() )) continue;
                 if ( Modifier.isFinal( field.getModifiers())) continue;
                 sortingParameters.add( field.getName() );
             }
             inferSortParameters( sortingParameters, clazz.getSuperclass() );
         }
     }
 
     private int calculateTotalPages( int totalElements, int pageSize ) {
         return 1+(int)Math.ceil(totalElements / pageSize );
     }
 
     private void transformList(List list, Transformer transformer) {
         if ( list != null && list.size() > 0 && transformer != null ) {
             for ( int i=0; i<list.size(); i++ ) {
                 list.add(i, transformer.transform(list.remove(i)));
             }
         }
     }
 
     private PagingParameter pagingParameter( UrlParameter urlParameter ) {
         // todo set as a property of the application
         Integer pageSize = 10;
         if ( urlParameter.pageSize() != null ) {
             pageSize = urlParameter.pageSize();
         }
         return new PagingParameter( urlParameter.page(), pageSize );
     }
 
     private SortingParameter sortingParameter( UrlParameter urlParameter ) {
         String sortBy = urlParameter.sortBy();
         if ( sortBy == null ) {
             // default for the Linkable, will not be used it list type is not of Linkable
             sortBy = "name";
         }
         return new SortingParameter( sortBy );
     }
 
 }
