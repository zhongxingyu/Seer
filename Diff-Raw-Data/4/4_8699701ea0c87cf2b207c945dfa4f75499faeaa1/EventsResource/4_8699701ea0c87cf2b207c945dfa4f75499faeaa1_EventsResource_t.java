 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  * 
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.zep.rest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.protobufs.ProtobufConstants;
 import org.zenoss.protobufs.util.Util.TimestampRange;
 import org.zenoss.protobufs.zep.Zep.EventDetailSet;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventQuery;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdate;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdateRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdateResponse;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.EventTagSeveritiesSet;
 import org.zenoss.protobufs.zep.Zep.FilterOperator;
 import org.zenoss.protobufs.zep.Zep.NumberRange;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventStoreDao;
 import org.zenoss.zep.index.EventIndexDao;
 import org.zenoss.zep.index.EventIndexer;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 
 @Path("1.0/events")
 public class EventsResource {
     private static final Logger logger = LoggerFactory.getLogger(EventsResource.class);
 
     private int queryLimit = ZepConstants.DEFAULT_QUERY_LIMIT;
     private EventIndexer eventIndexer;
     private EventStoreDao eventStoreDao;
     private EventIndexDao eventSummaryIndexDao;
     private EventIndexDao eventArchiveIndexDao;
 
     public void setQueryLimit(int limit) {
         if (limit > 0) {
             this.queryLimit = limit;
         }
         else {
             logger.warn("Invalid query limit: {}, using default: {}", limit, queryLimit);
         }
     }
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     public void setEventIndexer(EventIndexer eventIndexer) {
         this.eventIndexer = eventIndexer;
     }
 
     public void setEventSummaryIndexDao(EventIndexDao eventSummaryIndexDao) {
         this.eventSummaryIndexDao = eventSummaryIndexDao;
     }
 
     public void setEventArchiveIndexDao(EventIndexDao eventArchiveIndexDao) {
         this.eventArchiveIndexDao = eventArchiveIndexDao;
     }
 
     private static Set<String> getQuerySet(MultivaluedMap<String, String> params, String name) {
         final Set<String> set;
         final List<String> l = params.get(name);
         if (l == null) {
             set = Collections.emptySet();
         } else {
             set = new HashSet<String>(l.size());
             set.addAll(l);
         }
         return set;
     }
 
     private static int getQueryInteger(MultivaluedMap<String, String> params, String name, int defaultValue) {
         final String strVal = params.getFirst(name);
         return (strVal != null) ? Integer.valueOf(strVal) : defaultValue;
     }
 
     static <T extends Enum<T>> EnumSet<T> getQueryEnumSet(MultivaluedMap<String, String> params, String name,
             Class<T> enumClass, String prefix) {
         final EnumSet<T> set = EnumSet.noneOf(enumClass);
         final List<String> values = params.get(name);
         if (values != null) {
             for (String value : values) {
                 value = value.toUpperCase();
                 if (!value.startsWith(prefix)) {
                     value = prefix + value;
                 }
                 set.add(Enum.valueOf(enumClass, value));
             }
         }
 
         return set;
     }
 
     @POST
     @Path("search")
     @Consumes({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response createSavedSearch(EventQuery query, @Context UriInfo ui) throws URISyntaxException, ZepException {
         return createSavedSearchInternal(this.eventSummaryIndexDao, query, ui);
     }
 
     @POST
     @Path("archive/search")
     @Consumes({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response createArchiveSavedSearch(EventQuery query, @Context UriInfo ui) throws URISyntaxException, ZepException {
         return createSavedSearchInternal(this.eventArchiveIndexDao, query, ui);
     }
 
     public Response createSavedSearchInternal(EventIndexDao indexDao, EventQuery query, @Context UriInfo ui)
             throws URISyntaxException, ZepException {
         // Make sure index is up to date with latest events prior to creating the saved search
         indexFully();
         String uuid = indexDao.createSavedSearch(query);
         return Response.created(new URI(ui.getRequestUri().toString() + '/' + uuid)).build();
     }
 
     @GET
     @Path("search/{searchUuid}")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response listSavedSearch(@PathParam("searchUuid") String searchUuid,
                                     @QueryParam("offset") String offsetStr,
                                     @QueryParam("limit") String limitStr) throws ZepException {
         return listSavedSearchInternal(this.eventSummaryIndexDao, searchUuid, offsetStr, limitStr);
     }
 
     @GET
     @Path("archive/search/{searchUuid}")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response listArchiveSavedSearch(@PathParam("searchUuid") String searchUuid,
                                     @QueryParam("offset") String offsetStr,
                                     @QueryParam("limit") String limitStr) throws ZepException {
         return listSavedSearchInternal(this.eventArchiveIndexDao, searchUuid, offsetStr, limitStr);
     }
 
     private Response listSavedSearchInternal(EventIndexDao indexDao, String searchUuid, String offsetStr,
                                              String limitStr) throws ZepException {
         int offset = 0;
         if (offsetStr != null) {
             offset = Integer.parseInt(offsetStr);
             if (offset < 0) {
                 throw new ZepException("Invalid offset: " + offsetStr);
             }
         }
         int limit = queryLimit;
         if (limitStr != null) {
             limit = Integer.parseInt(limitStr);
             if (limit > queryLimit) {
                 limit = queryLimit;
             }
         }
         Response response;
         try {
             response = Response.ok(indexDao.savedSearch(searchUuid, offset, limit)).build();
         } catch (ZepException e) {
             if (e.getLocalizedMessage() != null && e.getLocalizedMessage().startsWith("ZEP0001E")) {
                 response = Response.status(Status.NOT_FOUND).entity(e.getLocalizedMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
             }
             else {
                 throw e;
             }
         }
         return response;
     }
 
     @DELETE
     @Path("search/{searchUuid}")
     public Response deleteSavedSearch(@PathParam("searchUuid") String searchUuid) throws ZepException {
         return deleteSavedSearchInternal(this.eventSummaryIndexDao, searchUuid);
     }
 
     @DELETE
     @Path("archive/search/{searchUuid}")
     public Response deleteArchiveSavedSearch(@PathParam("searchUuid") String searchUuid) throws ZepException {
         return deleteSavedSearchInternal(this.eventArchiveIndexDao, searchUuid);
     }
 
     private Response deleteSavedSearchInternal(EventIndexDao indexDao, String searchUuid) throws ZepException {
         String uuid = indexDao.deleteSavedSearch(searchUuid);
         if (uuid == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
         return Response.status(Status.NO_CONTENT).build();
     }
 
     @PUT
     @Path("search/{searchUuid}")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventSummaryUpdateResponse updateEvents(@PathParam("searchUuid") String searchUuid,
                                                    EventSummaryUpdateRequest request) throws ZepException {
         if (request.hasEventQueryUuid() && !searchUuid.equals(request.getEventQueryUuid())) {
             throw new ZepException(String.format("Mismatched search UUIDs: '%s' != '%s'", searchUuid,
                     request.getEventQueryUuid()));
         }
         EventSummaryResult result = this.eventSummaryIndexDao.savedSearchUuids(searchUuid, request.getOffset(),
                 request.getLimit());
         List<String> uuids = new ArrayList<String>(result.getEventsCount());
         for (EventSummary summary : result.getEventsList()) {
             uuids.add(summary.getUuid());
         }
         int numUpdated = this.eventStoreDao.update(uuids, request.getUpdateFields());
        if (numUpdated > 0) {
            indexFully();
        }
         EventSummaryUpdateResponse.Builder response = EventSummaryUpdateResponse.newBuilder();
         if (result.hasNextOffset()) {
             EventSummaryUpdateRequest.Builder requestBuilder = EventSummaryUpdateRequest.newBuilder(request);
             // Must set limit again in case initial limit is greater than configured maximum
             requestBuilder.setOffset(result.getNextOffset()).setLimit(result.getLimit());
             requestBuilder.setEventQueryUuid(searchUuid);
             response.setNextRequest(requestBuilder.build());
         }
         response.setTotal(result.getTotal());
         response.setUpdated(numUpdated);
         return response.build();
     }
 
     @GET
     @Path("{eventUuid}")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response getEventSummaryByUuid(@PathParam("eventUuid") String eventUuid) throws ZepException {
         EventSummary summary = eventStoreDao.findByUuid(eventUuid);
         if (summary == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
 
         return Response.ok(summary).build();
     }
 
     private void indexFully() throws ZepException {
         final long now = System.currentTimeMillis();
         int numIndexed;
         while ((numIndexed = eventIndexer.index(now)) > 0) {
             logger.debug("Indexed {} events", numIndexed);
         }
     }
 
     @PUT
     @Path("{eventUuid}")
     @Consumes({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response updateEventSummaryByUuid(@PathParam("eventUuid") String uuid, EventSummaryUpdate update)
             throws ZepException {
 
         EventSummary summary = eventStoreDao.findByUuid(uuid);
         if (summary == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
 
         int numRows = eventStoreDao.update(summary.getUuid(), update);
         if (numRows > 0) {
             indexFully();
         }
 
         return Response.noContent().build();
     }
 
     @POST
     @Path("{eventUuid}/notes")
     @Consumes({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response addNote(@PathParam("eventUuid") String eventUuid, EventNote note) throws ZepException {
 
         EventSummary summary = eventStoreDao.findByUuid(eventUuid);
         if (summary == null) {
             return Response.status(Status.NOT_FOUND).build();
         }
 
         int numRows = eventStoreDao.addNote(summary.getUuid(), note);
         if (numRows == 0) {
             return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
 
         return Response.noContent().build();
     }
 
     @POST
     @Path("/")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventSummaryResult listEventIndex(EventSummaryRequest request)
             throws ZepException {
         return this.eventSummaryIndexDao.list(request);
     }
 
     @POST
     @Path("archive")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventSummaryResult listEventIndexArchive(EventSummaryRequest request)
             throws ZepException {
         return this.eventArchiveIndexDao.list(request);
     }
 
     @GET
     @Path("/")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventSummaryResult listEventIndexGet(@Context UriInfo ui)
             throws ParseException, ZepException {
         return this.eventSummaryIndexDao.list(eventSummaryRequestFromUriInfo(ui));
     }
 
     @GET
     @Path("archive")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventSummaryResult listEventIndexArchiveGet(@Context UriInfo ui)
             throws ParseException, ZepException {
         return this.eventArchiveIndexDao.list(eventSummaryRequestFromUriInfo(ui));
     }
 
     @POST
     @Path("tag_severities")
     @Produces({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public EventTagSeveritiesSet getEventTagSeverities(EventFilter filter) throws ZepException {
         return this.eventSummaryIndexDao.getEventTagSeverities(filter);
     }
 
     @POST
     @Path("{eventUuid}/details")
     @Consumes({ MediaType.APPLICATION_JSON, ProtobufConstants.CONTENT_TYPE_PROTOBUF })
     public Response updateEventDetails(@PathParam("eventUuid") String eventUuid, EventDetailSet details) throws ZepException {
 
         int numRows = eventStoreDao.updateDetails(eventUuid, details);
         if (numRows == 0) {
             return Response.status(Status.NOT_FOUND).build();
         }
 
         return Response.noContent().build();
     }
 
     private EventSummaryRequest eventSummaryRequestFromUriInfo(UriInfo info) throws ParseException {
         /* Read all params from query */
         MultivaluedMap<String, String> queryParams = info.getQueryParameters();
         logger.debug("Query Parameters: {}", queryParams);
         final int limit = getQueryInteger(queryParams, "limit", queryLimit);
         final int offset = getQueryInteger(queryParams, "offset", 0);
         final Set<String> sorts = getQuerySet(queryParams, "sort");
 
         /* Build event request */
         final EventSummaryRequest.Builder reqBuilder = EventSummaryRequest
                 .newBuilder();
         reqBuilder.setEventFilter(createEventFilter(queryParams, false));
         reqBuilder.setExclusionFilter(createEventFilter(queryParams, true));
 
         if (limit < 0) {
             throw new IllegalArgumentException("Invalid limit: " + limit);
         }
         reqBuilder.setLimit(limit);
 
         if (offset < 0) {
             throw new IllegalArgumentException("Invalid offset: " + offset);
         }
         reqBuilder.setOffset(offset);
 
         for (String sort : sorts) {
             String[] sortParts = sort.split(",", 2);
 
             EventSort.Builder eventSort = EventSort.newBuilder();
             eventSort.setField(EventSort.Field.valueOf(sortParts[0]));
 
             if (sortParts.length == 2) {
                 eventSort.setDirection(EventSort.Direction.valueOf(sortParts[1]));
             }
 
             reqBuilder.addSort(eventSort);
         }
 
         return reqBuilder.build();
     }
 
     static EventFilter createEventFilter(MultivaluedMap<String, String> queryParams, boolean isExclusion)
             throws ParseException {
         String prefix = (isExclusion) ? "ex_" : "";
 
         final EnumSet<EventSeverity> severities = getQueryEnumSet(queryParams,
                 prefix + "severity", EventSeverity.class, "SEVERITY_");
         final EnumSet<EventStatus> status = getQueryEnumSet(queryParams,
                 prefix + "status", EventStatus.class, "STATUS_");
         final Set<String> eventClass = getQuerySet(queryParams, prefix + "event_class");
         final TimestampRange firstSeen = parseRange(queryParams.getFirst(prefix + "first_seen"));
         final TimestampRange lastSeen = parseRange(queryParams.getFirst(prefix + "last_seen"));
         final TimestampRange statusChange = parseRange(queryParams.getFirst(prefix + "status_change"));
         final TimestampRange updateTime = parseRange(queryParams.getFirst(prefix + "update_time"));
         final NumberRange count = convertCount(queryParams.getFirst(prefix + "count"));
         final Set<String> fingerprint = getQuerySet(queryParams, prefix + "fingerprint");
         final Set<String> element_identifier = getQuerySet(queryParams, prefix + "element_identifier");
         final Set<String> element_sub_identifier = getQuerySet(queryParams, prefix + "element_sub_identifier");
         final Set<String> uuids = getQuerySet(queryParams, prefix + "uuid");
         final Set<String> summary = getQuerySet(queryParams, prefix + "event_summary");
         final Set<String> current_user = getQuerySet(queryParams, prefix + "current_user");
         final Set<String> tagUuids = getQuerySet(queryParams, prefix + "tag_uuids");
         final String tagUuidsOp = queryParams.getFirst(prefix + "tag_uuids_op");
         // TODO: EventDetailFilter
 
         /* Build event filter */
         final EventFilter.Builder filterBuilder = EventFilter.newBuilder();
         filterBuilder.addAllFingerprint(fingerprint);
         filterBuilder.addAllSeverity(severities);
         filterBuilder.addAllStatus(status);
         filterBuilder.addAllEventClass(eventClass);
         filterBuilder.addAllElementIdentifier(element_identifier);
         filterBuilder.addAllElementSubIdentifier(element_sub_identifier);
         filterBuilder.addAllUuid(uuids);
         filterBuilder.addAllEventSummary(summary);
         filterBuilder.addAllCurrentUserName(current_user);
 
         if (firstSeen != null) {
             filterBuilder.addFirstSeen(firstSeen);
         }
         if (lastSeen != null) {
             filterBuilder.addLastSeen(lastSeen);
         }
         if (statusChange != null) {
             filterBuilder.addStatusChange(statusChange);
         }
         if (updateTime != null) {
             filterBuilder.addUpdateTime(updateTime);
         }
         if (count != null) {
             filterBuilder.addCountRange(count);
         }
         if (!tagUuids.isEmpty()) {
             FilterOperator op = FilterOperator.OR;
             if (tagUuidsOp != null) {
                 op = FilterOperator.valueOf(tagUuidsOp.toUpperCase());
             }
             EventTagFilter.Builder tagFilterBuilder = EventTagFilter.newBuilder();
             tagFilterBuilder.addAllTagUuids(tagUuids);
             tagFilterBuilder.setOp(op);
             filterBuilder.addTagFilter(tagFilterBuilder.build());
         }
 
         return filterBuilder.build();
     }
 
     static TimestampRange parseRange(String range) throws ParseException {
         if (range == null) {
             return null;
         }
         final TimestampRange.Builder builder = TimestampRange.newBuilder();
         final int solidusIndex = range.indexOf('/');
 
         if (solidusIndex != -1) {
             final String start = range.substring(0, solidusIndex);
             final String end = range.substring(solidusIndex + 1);
             final long startTs = parseISO8601(start);
             final long endTs = parseISO8601(end);
             if (startTs > endTs) {
                 throw new IllegalArgumentException(start + " > " + end);
             }
             builder.setStartTime(startTs).setEndTime(endTs);
         } else {
             builder.setStartTime(parseISO8601(range));
         }
         return builder.build();
     }
 
     static long parseISO8601(String str) throws ParseException {
         final SimpleDateFormat fmt;
         if (str.indexOf('.') > 0) {
             fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         } else {
             fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         }
         fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
         return fmt.parse(str).getTime();
     }
 
     static NumberRange convertCount(String count) {
         if (count == null || count.isEmpty()) {
             return null;
         }
         Integer from = null, to = null;
         final int colon = count.indexOf(':');
         // RANGE in form of FROM:TO
         if (colon != -1) {
             String strFrom = count.substring(0, colon);
             String strTo = count.substring(colon+1);
             if (!strFrom.isEmpty()) {
                 from = Integer.parseInt(strFrom);
             }
             if (!strTo.isEmpty()) {
                 to = Integer.parseInt(strTo);
             }
         }
         // [GT|GTEQ|LT|LTEQ|EQ]NUM
         else {
             switch (count.charAt(0)) {
                 case '>':
                     if (count.charAt(1) == '=') {
                         from = Integer.parseInt(count.substring(2));
                     } else {
                         from = Integer.parseInt(count.substring(1)) + 1;
                     }
                     break;
                 case '<':
                     if (count.charAt(1) == '=') {
                         to = Integer.parseInt(count.substring(2));
                     } else {
                         to = Integer.parseInt(count.substring(1)) - 1;
                     }
                     break;
                 case '=':
                     from = to = Integer.parseInt(count.substring(1));
                     break;
                 default:
                     from = to = Integer.parseInt(count);
                     break;
             }
         }
 
         if (from == null && to == null) {
             return null;
         }
         if (from != null && to != null && from > to) {
             throw new IllegalArgumentException("Count from > to: " + from + "," + to);
         }
 
         final NumberRange.Builder rangeBuilder = NumberRange.newBuilder();
         if (from != null) {
             if (from < 0) {
                 throw new IllegalArgumentException("Count number out of range: " + from);
             }
             rangeBuilder.setFrom(from);
         }
         if (to != null) {
             if (to < 0) {
                 throw new IllegalArgumentException("Count number out of range: " + to);
             }
             rangeBuilder.setTo(to);
         }
         return rangeBuilder.build();
     }
 }
