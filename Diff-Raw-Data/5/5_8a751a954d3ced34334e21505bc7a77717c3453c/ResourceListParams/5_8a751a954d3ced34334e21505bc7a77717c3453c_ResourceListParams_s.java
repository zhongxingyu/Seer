 package org.ow2.sirocco.apis.rest.cimi.tools;
 
 import org.ow2.sirocco.apis.rest.cimi.sdk.QueryParams;
 
 import com.beust.jcommander.Parameter;
 
 public class ResourceListParams extends ResourceSelectExpandParams {
     @Parameter(names = "-first", description = "First index of entity to return")
    private Integer first = -1;
 
     @Parameter(names = "-last", description = "Last index of entity to return")
    private Integer last = -1;
 
     @Parameter(names = "-filter", description = "Filter expression")
     private String filter;
 
     public Integer getFirst() {
         return this.first;
     }
 
     public Integer getLast() {
         return this.last;
     }
 
     public String getFilter() {
         return this.filter;
     }
 
     public ResourceListParams(final String... defaultSelectValue) {
         super(defaultSelectValue);
     }
 
     @Override
     public QueryParams.Builder buildQueryParams() {
         return super.buildQueryParams().first(this.first).last(this.last).filter(this.filter);
     }
 
 }
