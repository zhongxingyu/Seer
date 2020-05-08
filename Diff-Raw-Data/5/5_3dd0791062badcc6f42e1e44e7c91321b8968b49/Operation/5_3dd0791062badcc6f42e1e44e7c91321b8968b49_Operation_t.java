 
 package org.mule.modules.jive.api;
 
 import org.mule.modules.jive.CustomOp;
 
 import org.apache.commons.lang.StringUtils;
 
 /**The complete list of operations the user can execute.*/
 public enum Operation 
 {
 
     /**
      * Creates a new avatar for a user using the specified byte array as the contents
      * of the avatar image.
      */
     AVATAR_CREATE_AVATAR(EntityType.AVATAR, EntityTypes.BASE_URI),
     /** Deletes an avatar from the system. */
     AVATAR_DELETE_AVATAR(CustomOp.AVATAR_DELETE),
     /**
      * Returns the active avatar for the specified user, or the SystemDefaultAvatar
      * if the user does not have an active avatar specified.
      */
     AVATAR_GET_ACTIVE_AVATAR(EntityType.AVATAR),
     /** Returns an avatar by its ID. */
     AVATAR_GET_AVATAR(CustomOp.AVATAR_GET_AVATAR_BY_ID),
     /** Used to acquire a count of all the avatars for a specific user. */
     AVATAR_GET_AVATAR_COUNT(EntityType.AVATAR),
     /** Returns a list of avatars for the specified user. */
     AVATAR_GET_AVATARS(CustomOp.AVATAR_GET_AVATARS),
     /** Returns a list of all of the global avatars. */
     AVATAR_GET_GLOBAL_AVATARS(EntityType.AVATAR),
     /** Returns the maximum allowable height for an avatar image. */
     AVATAR_GET_MAX_ALLOWABLE_HEIGHT(CustomOp.AVATAR_GET_MAX_ALLOWABLE_HEIGHT),
     /** Returns the maximum allowable width for an avatar image. */
     AVATAR_GET_MAX_ALLOWABLE_WIDTH(CustomOp.AVATAR_GET_MAX_ALLOWABLE_WIDTH),
     /**
      * Returns the maximum number of avatars a user is allowed to have; returns -1
      * when there is no limit.
      */
     AVATAR_GET_MAX_USER_AVATARS(EntityType.AVATAR),
     /** Returns a count of all the avatars that require moderation. */
     AVATAR_GET_MODERATION_AVATAR_COUNT(EntityType.AVATAR),
     /** Returns a collection of all of the avatars that require moderation. */
     AVATAR_GET_MODERATION_AVATARS(EntityType.AVATAR),
     /** Returns true if the system should attempt to resize avatar images. */
     AVATAR_IS_ALLOW_IMAGE_RESIZE(CustomOp.AVATAR_IS_ALLOW_IMAGE_RESIZE),
     /** Returns true if the avatars feature is enabled; false otherwise. */
     AVATAR_IS_AVATARS_ENABLED(EntityType.AVATAR),
     /** Returns whether or not user avatars will be moderated. */
     AVATAR_IS_MODERATE_USER_AVATARS(EntityType.AVATAR),
     /** Returns true if users can create their own avatars; false otherwise. */
     AVATAR_IS_USER_AVATARS_ENABLED(EntityType.AVATAR),
     /** Sets the specified avatar as the user's avatar. */
     AVATAR_SET_ACTIVE_AVATAR(EntityType.AVATAR),
     /** Set whether the system should attempt to resize avatar images. */
     AVATAR_SET_ALLOW_IMAGE_RESIZE(CustomOp.AVATAR_SET_ALLOW_IMAGE_RESIZE),
     /** Sets the maximum allowable height for an avatar image. */
     AVATAR_SET_MAX_ALLOWABLE_HEIGHT(CustomOp.AVATAR_SET_MAX_ALLOWABLE_HEIGHT),
     /** Sets the maximum allowable width for an avatar image. */
     AVATAR_SET_MAX_ALLOWABLE_WIDTH(CustomOp.AVATAR_SET_MAX_ALLOWABLE_WIDTH),
     /** Sets the maximum number of avatars a user can have. */
     AVATAR_SET_MAX_USER_AVATARS(EntityType.AVATAR),
     /** Sets whether or not user-create avatars will be moderated. */
     AVATAR_SET_MODERATE_USER_AVATARS(EntityType.AVATAR),
     /** Sets whether or not users can create their own custom avatars. */
     AVATAR_SET_USER_AVATARS_ENABLED(EntityType.AVATAR),
     /** Adds a new comment to the specified content object. */
  //  COMMENT_ADD_COMMENT(CustomOp.COMMENT_ADD), FIXME
     /** Adds a new comment to the specified parent comment. */
  //  COMMENT_ADD_COMMENT_TO_COMMENT(CustomOp.COMMENT_ADD_COMMENT_TO_COMMENT), FIXME
     /** Deletes all comments on the object. */
     COMMENT_DELETE_ALL_COMMENTS(EntityType.COMMENT, EntityTypes.BASE_URI),
     /** Deletes a comment in the object. */
     COMMENT_DELETE_COMMENT(EntityType.COMMENT),
     /** Deletes a comment in the object. */
     COMMENT_DELETE_COMMENT_RECURSIVE(EntityType.COMMENT),
     /** Returns the specified comment. */
     COMMENT_GET_COMMENT(EntityType.COMMENT),
     /**
      * Returns the number of comments on the content specified by objectType and
      * objectID.
      */
     COMMENT_GET_COMMENT_COUNT(EntityType.COMMENT),
     /**
      * Returns the number of comments on the content specified by objectType and
      * objectID, where the number is constrained by the filter.
      */
     COMMENT_GET_COMMENT_COUNT_WITH_FILTER(EntityType.COMMENT),
     /**
      * Returns a list of the comments on the content specified by objectType and
      * objectID.
      */
     COMMENT_GET_COMMENTS(EntityType.COMMENT),
     /**
      * Returns a count of all the comments in all content that has been authored by
      * the specified user.
      */
     COMMENT_GET_USER_CONTENT_COMMENT_COUNT(EntityType.COMMENT),
     /**
      * Returns a count of all the comments in all content which has been authored by
      * the supplied user, with the count constrained by the specified filter.
      */
     COMMENT_GET_USER_CONTENT_COMMENT_COUNT_WITH_FILTER(EntityType.COMMENT),
     /**
      * Returns a list of all the comments in all content which has been authored by
      * the supplied user.
      */
     COMMENT_GET_USER_CONTENT_COMMENTS(EntityType.COMMENT),
     /**
      * Returns a list of all the comments in all content which has been authored by
      * the supplied user.
      */
     COMMENT_GET_USER_CONTENT_COMMENTS_WITH_FILTER(EntityType.COMMENT),
     /** Updates an existing comment. */
     COMMENT_UPDATE_COMMENT(EntityType.COMMENT),
     /** Adds an attachment to the specified blog post. */
     BLOG_ADD_ATTACHMENT_TO_BLOG_POST(EntityType.BLOG),
     /** Adds an image to the specified blog post. */
     BLOG_ADD_IMAGE_TO_BLOG_POST(EntityType.BLOG),
     /** Creates a new blog. */
     BLOG_CREATE_BLOG(EntityType.BLOG),
     /** Creates a new blog post. */
     BLOG_CREATE_BLOG_POST(EntityType.BLOG),
     /**
      * Permanently deletes a blog and all of the posts and comments associated with
      * it.
      */
     BLOG_DELETE_BLOG(EntityType.BLOG),
     /**
      * Permanently deletes a blog post and all of the comments associated with the
      * it.
      */
     BLOG_DELETE_BLOG_POST(EntityType.BLOG),
     /** Returns an array of attachments for the specified blog post. */
     BLOG_GET_ATTACHMENTS_BY_BLOG_POST_ID(EntityType.BLOG),
     /** Returns a blog by blog name. */
     BLOG_GET_BLOG_BY_NAME(EntityType.BLOG),
     /** Returns a blog by its ID. */
     BLOG_GET_BLOG_BY_ID(EntityType.BLOG),
     /** Returns the total number of blogs on this system. */
     BLOG_GET_BLOG_COUNT(EntityType.BLOG),
     /**
      * Returns the total number of blogs on this system that match the criteria
      * specified by the filter.
      */
     BLOG_GET_BLOG_COUNT_BY_RESULTFILTER(EntityType.BLOG),
     /** Returns the count of all blogs associated with the specified user. */
     BLOG_GET_BLOG_COUNT_FOR_USER(EntityType.BLOG),
     /** Returns a blog post by its ID. */
     BLOG_GET_BLOG_POST(EntityType.BLOG),
     /**
      * Returns the number of blog posts on the system, by default only includes blog
      * posts where status = WSBlogPost.STATUS_PUBLISH and publish date less than now.
      */
     BLOG_GET_BLOG_POST_COUNT_1(EntityType.BLOG),
     /** Returns the number of blog posts on the system. */
     BLOG_GET_BLOG_POST_COUNT_2(EntityType.BLOG),
     /** Returns all the blog posts that match the criteria specified by the filter. */
     BLOG_GET_BLOG_POSTS(EntityType.BLOG),
     /**
      * Returns all the blogs on this system whose display name is LIKE the given
      * query.
      */
     BLOG_GET_BLOGS_BY_DISPLAY_NAME(EntityType.BLOG),
     /** Returns all blogs associated with the specified user. */
     BLOG_GET_BLOGS_FOR_USER(EntityType.BLOG),
     /** Returns the number of comments on blog posts in the system. */
     BLOG_GET_COMMENT_COUNT(EntityType.BLOG),
     /**
      * Returns the number of blog post comments that match the criteria specified by
      * the filter.
      */
     BLOG_GET_COMMENT_COUNT_BY_RESULTFILTER(EntityType.BLOG),
     /**
      * Returns all the blog post comments that match the criteria specified by the
      * filter.
      */
     BLOG_GET_COMMENTS(EntityType.BLOG),
     /** Returns an array of images that are attached to the specified blog post. */
     BLOG_GET_IMAGES_BY_BLOG_POST_ID(EntityType.BLOG),
     /** Returns a comma-delimited list of available pings for the system. */
     BLOG_GET_PING_SERVICES(EntityType.BLOG),
     /** Returns up to ten of the most recent blogs created on this system. */
     BLOG_GET_RECENT_BLOGS(EntityType.BLOG),
     /** Returns all of the tags for blogs in the system. */
     BLOG_GET_TAGS(EntityType.BLOG),
     /** Returns all tags for blogs as constrained by the specified filter. */
     BLOG_GET_TAGS_BY_RESULTFILTER(EntityType.BLOG),
     /** Returns true if the blogs feature is turned on. */
     BLOG_IS_BLOGS_ENABLED(EntityType.BLOG),
     /** Returns true if the comments feature is turned on. */
     BLOG_IS_COMMENTS_ENABLED(EntityType.BLOG),
     /** Returns true if the pings feature is turned on. */
     BLOG_IS_PINGS_ENABLED(EntityType.BLOG),
     /**
      * Returns true if the system has been configured to allow users to override the
      * ping URIs configured for the system.
      */
     BLOG_IS_PINGS_OVERRIDE_ENABLED(EntityType.BLOG),
     /** Returns true if the trackbacks feature is turned on. */
     BLOG_IS_TRACKBACKS_ENABLED(EntityType.BLOG),
     /** Publishes a blog post with the specified subject and body. */
     BLOG_PUBLISH_BLOG_POST(EntityType.BLOG),
     /** Removes the attachment with the supplied ID. */
     BLOG_REMOVE_ATTACHMENT(EntityType.BLOG),
     /** Enables or disables the blogs feature. */
     BLOG_SET_BLOGS_ENABLED(EntityType.BLOG),
     /** Enables or disables the comments feature system wide. */
     BLOG_SET_COMMENTS_ENABLED(EntityType.BLOG),
     /** Sets the comma-delimited list of available pings for the system. */
     BLOG_SET_PING_SERVICES(EntityType.BLOG),
     /** Enables or disables the pings feature system wide. */
     BLOG_SET_PINGS_ENABLED(EntityType.BLOG),
     /**
      * Configures the system to allow users to override the ping URIs configured for
      * all blogs.
      */
     BLOG_SET_PINGS_OVERRIDE_ENABLED(EntityType.BLOG),
     /** Enables or disables the trackbacks feature system wide. */
     BLOG_SET_TRACKBACKS_ENABLED(EntityType.BLOG),
     /** Updates the specified blog post with what's contained in blogPost. */
     BLOG_UPDATE_BLOG_POST(EntityType.BLOG),
     /** Uploads a new attachment to the specified blog post. */
     BLOG_UPLOAD_ATTACHMENT_TO_BLOG_POST(EntityType.BLOG),
     /**
      * Returns <tt>true</tt> if the specified user has one or more blogs;
      * <tt>false</tt> if the user does not have a blog.
      */
     BLOG_USER_HAS_BLOGS(EntityType.BLOG),
     /** Creates a new community as a sub-community of the specified community. */
     COMUNITYSERVICE_CREATE_COMMUNITY(EntityType.COMMUNITY),
     /** Deletes the specified community. */
     COMUNITYSERVICE_DELETE_COMMUNITY(EntityType.COMMUNITY),
     /** Delete the specified property from the specified community. */
     COMUNITYSERVICE_DELETE_PROPERTY(EntityType.COMMUNITY),
     /** Returns a WSCommunity by its ID. */
     COMUNITYSERVICE_GET_COMMUNITY(EntityType.COMMUNITY),
     /**
      * Returns document IDs for all published documents in the specified community,
      * excluding its sub-communities.
      */
     COMUNITYSERVICE_GET_DOCUMENT_IDS(EntityType.COMMUNITY),
     /** Returns the extended properties for the specified community. */
     COMUNITYSERVICE_GET_PROPERTIES(EntityType.COMMUNITY),
     /** Returns the specified extended property for a community. */
     COMUNITYSERVICE_GET_PROPERTY(EntityType.COMMUNITY),
     /***/
     COMUNITYSERVICE_GET_RECURSIVE_COMMUNITIES(EntityType.COMMUNITY),
     /**
      * Returns all sub-communities that are descendants of the specified parent
      * community.
      */
     COMUNITYSERVICE_GET_RECURSIVE_COMMUNITIES_BY_PARENT(EntityType.COMMUNITY),
     /** Returns a count of sub-communities of the specified community. */
     COMUNITYSERVICE_GET_RECURSIVE_COMMUNITY_COUNT(EntityType.COMMUNITY),
     /** Returns the immediate sub-communities of the specified parent community. */
     COMUNITYSERVICE_GET_SUB_COMMUNITIES(EntityType.COMMUNITY),
     /** Sets an extended propery for the specified community. */
     COMUNITYSERVICE_SET_PROPERTY(EntityType.COMMUNITY),
     /** Updates the specified community with <em>community</em>. */
     COMUNITYSERVICE_UPDATE_COMMUNITY(EntityType.COMMUNITY),
     /** Adds a new attachment to the specified document. */
     DOCUMENTSERVICE_ADD_ATTACHMENT_TO_DOCUMENT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Adds a new attachment to the specified document. */
     DOCUMENTSERVICE_ADD_ATTACHMENT_TO_DOCUMENT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Adds the specified user as an author of a document. */
     DOCUMENTSERVICE_ADD_AUTHOR(EntityType.DOCUMENT),
     /** Adds the specified user as a document approver for an entire community. */
     DOCUMENTSERVICE_ADD_DOCUMENT_APPROVER_ON_COMMUNITY(EntityType.DOCUMENT),
     /** Adds a user as an approver for the specified document. */
     DOCUMENTSERVICE_ADD_DOCUMENT_APPROVER_ON_DOCUMENT(EntityType.DOCUMENT),
     /** Adds a new image to the specified document. */
     DOCUMENTSERVICE_ADD_IMAGE_TO_DOCUMENT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Adds a new image to the document with the specified internal ID. */
     DOCUMENTSERVICE_ADD_IMAGE_TO_DOCUMENT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Creates a new binary document in the specified community. */
     DOCUMENTSERVICE_CREATE_BINARY_DOCUMENT(EntityType.DOCUMENT),
     /** Creates a new binary document in the specified container. */
     DOCUMENTSERVICE_CREATE_BINARY_DOCUMENT_IN_CONTAINER(EntityType.DOCUMENT),
     /** Creates a new document in the specified community. */
     DOCUMENTSERVICE_CREATE_DOCUMENT(EntityType.DOCUMENT),
     /** Create a new document in the specified container. */
     DOCUMENTSERVICE_CREATE_DOCUMENT_IN_CONTAINER(EntityType.DOCUMENT),
     /** Deletes a document. */
     DOCUMENTSERVICE_DELETE_DOCUMENT(EntityType.DOCUMENT),
     /** Deletes the specified property from the specified document. */
     DOCUMENTSERVICE_DELETE_DOCUMENT_PROPERTY(EntityType.DOCUMENT),
     /**
      * Returns a list of WSApprovalStatus that show which users have approved the
      * specified document and which haven't.
      */
     DOCUMENTSERVICE_GET_APPROVAL_STATUS_FOR_DOCUMENT(EntityType.DOCUMENT),
     /** Returns the number of attachments on the specified document. */
     DOCUMENTSERVICE_GET_ATTACHMENT_COUNT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /**
      * Returns the number of attachments on the document with the specified internal
      * ID.
      */
     DOCUMENTSERVICE_GET_ATTACHMENT_COUNT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Returns the attachments for the specified document. */
     DOCUMENTSERVICE_GET_ATTACHMENTS_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /**
      * Returns the attachments for the document with the specified internal document
      * ID.
      */
     DOCUMENTSERVICE_GET_ATTACHMENTS_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Returns the users who are allowed to edit the document. */
     DOCUMENTSERVICE_GET_AUTHORS(EntityType.DOCUMENT),
     /** Returns the content of the specified binary document. */
     DOCUMENTSERVICE_GET_BINARY_DOCUMENT_CONTENT(EntityType.DOCUMENT),
     /**
      * Returns all of the users who must approve new documents before they can be
      * published.
      */
     DOCUMENTSERVICE_GET_DOCUMENT_APPROVERS_ON_COMMUNITY(EntityType.DOCUMENT),
     /**
      * Returns all of the users who must approve the specified document before it can
      * be published.
      */
     DOCUMENTSERVICE_GET_DOCUMENT_APPROVERS_ON_DOCUMENT(EntityType.DOCUMENT),
     /** Returns the specified document from a community. */
     DOCUMENTSERVICE_GET_DOCUMENT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Returns the specified version of a document. */
     DOCUMENTSERVICE_GET_DOCUMENT_BY_DOCUMENT_IDAND_VERSION(EntityType.DOCUMENT),
     /** Returns the specified document. */
     DOCUMENTSERVICE_GET_DOCUMENT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Returns the specified version of a document. */
     DOCUMENTSERVICE_GET_DOCUMENT_BY_INTERNAL_DOC_IDAND_VERSION(EntityType.DOCUMENT),
     /** Returns the extended properties for the specified document. */
     DOCUMENTSERVICE_GET_DOCUMENT_PROPERTIES(EntityType.DOCUMENT),
     /** Returns the value of the specified extended property for a document. */
     DOCUMENTSERVICE_GET_DOCUMENT_PROPERTY(EntityType.DOCUMENT),
     /** Returns all of the documents for the specified community. */
     DOCUMENTSERVICE_GET_DOCUMENTS_BY_COMMUNITY(EntityType.DOCUMENT),
     /**
      * Returns all of the documents in the specified community and constrained by the
      * specified filter.
      */
     DOCUMENTSERVICE_GET_DOCUMENTS_BY_COMMUNITY_AND_FILTER(EntityType.DOCUMENT),
     /** Returns the number of images on the specified document. */
     DOCUMENTSERVICE_GET_IMAGE_COUNT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Returns the number of images on the document with the specified internal ID. */
     DOCUMENTSERVICE_GET_IMAGE_COUNT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /** Acquire the images for a document by the document id. */
     DOCUMENTSERVICE_GET_IMAGES_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Returns the images for the document specified by its internal ID. */
     DOCUMENTSERVICE_GET_IMAGES_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /**
      * Returns the most popular documents from across all communities according to
      * ratings, document views and time passed since the document was created.
      */
     DOCUMENTSERVICE_GET_POPULAR_DOCUMENTS(EntityType.DOCUMENT),
     /**
      * Returns the most popular documents in the given community according to
      * ratings, document views and time passed since the document was created.
      */
     DOCUMENTSERVICE_GET_POPULAR_DOCUMENTS_BY_COMMUNITY(EntityType.DOCUMENT),
     /**
      * Returns the most popular documents in the given community according to
      * ratings, document views and time passed since the document was created in the
      * specified languages.
      */
     DOCUMENTSERVICE_GET_POPULAR_DOCUMENTS_BY_LANGUAGE(EntityType.DOCUMENT),
     /** Returns the user who authored the document. */
     DOCUMENTSERVICE_GET_USER(EntityType.DOCUMENT),
     /**
      * Returns a list of the documents that the specified user must approve before
      * they can be published.
      */
     DOCUMENTSERVICE_GET_USER_APPROVAL_DOCUMENTS(EntityType.DOCUMENT),
     /** Returns true if the comments feature is turned on. */
     DOCUMENTSERVICE_IS_COMMENTS_ENABLED(EntityType.DOCUMENT),
     /** Returns true if the trackbacks feature is turned on. */
     DOCUMENTSERVICE_IS_TRACKBACKS_ENABLED(EntityType.DOCUMENT),
     /** Moves a document from its current community to another one. */
     DOCUMENTSERVICE_MOVE_DOCUMENT(EntityType.DOCUMENT),
     /** Moves a document from its current container to another. */
     DOCUMENTSERVICE_MOVE_DOCUMENT_TO_CONTAINER(EntityType.DOCUMENT),
     /** Publishes the specified binary document in the specified community. */
     DOCUMENTSERVICE_PUBLISH_BINARY_DOCUMENT(EntityType.DOCUMENT),
     /** Publishes the specified binary document in the specified container. */
     DOCUMENTSERVICE_PUBLISH_BINARY_DOCUMENT_IN_CONTAINER(EntityType.DOCUMENT),
     /** Publishes the specified document in the specified community. */
     DOCUMENTSERVICE_PUBLISH_DOCUMENT(EntityType.DOCUMENT),
     /** Publishes the specified document in the specified container. */
     DOCUMENTSERVICE_PUBLISH_DOCUMENT_IN_CONTAINER(EntityType.DOCUMENT),
     /** Removes the specified attachment. */
     DOCUMENTSERVICE_REMOVE_ATTACHMENT(EntityType.DOCUMENT),
     /** Removes the specified user as an author of the specified document. */
     DOCUMENTSERVICE_REMOVE_AUTHOR(EntityType.DOCUMENT),
     /** Enables or disables the comments feature system wide. */
     DOCUMENTSERVICE_SET_COMMENTS_ENABLED(EntityType.DOCUMENT),
     /** Sets the specified extended property for a document. */
     DOCUMENTSERVICE_SET_DOCUMENT_PROPERTY(EntityType.DOCUMENT),
     /** Enables or disables the trackbacks feature system wide. */
     DOCUMENTSERVICE_SET_TRACKBACKS_ENABLED(EntityType.DOCUMENT),
     /** Updates the specified document with the <em>document</em> parameter value. */
     DOCUMENTSERVICE_UPDATE_DOCUMENT(EntityType.DOCUMENT),
     /** Uploads a file as an attachment to the specified document. */
     DOCUMENTSERVICE_UPLOAD_ATTACHMENT_TO_DOCUMENT_BY_DOCUMENT_ID(EntityType.DOCUMENT),
     /** Uploads a file as an attachment to the document specified internal ID. */
     DOCUMENTSERVICE_UPLOAD_ATTACHMENT_TO_DOCUMENT_BY_INTERNAL_DOC_ID(EntityType.DOCUMENT),
     /**
      * Adds the entitlements specified by the supplied mask to the user for the given
      * container and content type.
      */
     ENTITLEMENTSERVICE_ADD_GROUP_ENTITLEMENT(EntityType.ENTITLEMENT),
     /**
      * Adds the entitlements specified by the supplied mask to the user for the given
      * container and content type.
      */
     ENTITLEMENTSERVICE_ADD_USER_ENTITLEMENT(EntityType.ENTITLEMENT),
     /** Returns all the entitlement masks available in the system. */
     ENTITLEMENTSERVICE_GET_ENTITLEMENT_MASKS(EntityType.ENTITLEMENT),
     /**
      * Returns true if the supplied user has any of the entitlements specified by the
      * given mask for the supplied container and content type
      */
     ENTITLEMENTSERVICE_IS_USER_ENTITLED(EntityType.ENTITLEMENT),
     /**
      * Removes the entitlements specified by the supplied mask from the user for the
      * given container and content type.
      */
     ENTITLEMENTSERVICE_REMOVE_GROUP_ENTITLEMENT(EntityType.ENTITLEMENT),
     /**
      * Removes the entitlements specified by the supplied mask from the user for the
      * given container and content type.
      */
     ENTITLEMENTSERVICE_REMOVE_USER_ENTITLEMENT(EntityType.ENTITLEMENT),
     /** Adds an attachment to the specified message. */
     FORUMSERVICE_ADD_ATTACHMENT_TO_MESSAGE(EntityType.FORUM),
     /** Adds an image to the specified. */
     FORUMSERVICE_ADD_IMAGE_TO_MESSAGE(EntityType.FORUM),
     /** Creates a new message on the specified thread. */
     FORUMSERVICE_CREATE_MESSAGE(EntityType.FORUM),
     /** Creates a new message that is a direct response to the specified message. */
     FORUMSERVICE_CREATE_REPLY_MESSAGE(EntityType.FORUM),
     /** Creates a new thread in the specified community. */
     FORUMSERVICE_CREATE_THREAD(EntityType.FORUM),
     /** Creates a new thread in the specified container. */
     FORUMSERVICE_CREATE_THREAD_IN_CONTAINER(EntityType.FORUM),
     /** Deletes the specified message. */
     FORUMSERVICE_DELETE_MESSAGE(EntityType.FORUM),
     /** Deletes the specified message, optionally also deleting its child messages. */
     FORUMSERVICE_DELETE_MESSAGE_AND_CHILDREN(EntityType.FORUM),
     /** Deletes the specified property from a message. */
     FORUMSERVICE_DELETE_MESSAGE_PROPERTY(EntityType.FORUM),
     /** Deletes the specified thread. */
     FORUMSERVICE_DELETE_THREAD(EntityType.FORUM),
     /** Deletes the specified property from the specified thread. */
     FORUMSERVICE_DELETE_THREAD_PROPERTY(EntityType.FORUM),
     /** Returns attachments for the specified message. */
     FORUMSERVICE_GET_ATTACHMENTS_BY_MESSAGE_ID(EntityType.FORUM),
     /**
      * Returns the specified child message of the specified parent, given the child
      * message's index in the array of children.
      */
     FORUMSERVICE_GET_CHILD(EntityType.FORUM),
     /**
      * Returns the number of the specified parent's child messages; 0 if the node has
      * no children.
      */
     FORUMSERVICE_GET_CHILD_COUNT(EntityType.FORUM),
     /** Returns all the messages that are children of the specified parent. */
     FORUMSERVICE_GET_CHILDREN(EntityType.FORUM),
     /** Returns the specified message. */
     FORUMSERVICE_GET_FORUM_MESSAGE(EntityType.FORUM),
     /** Returns the specified thread.. */
     FORUMSERVICE_GET_FORUM_THREAD(EntityType.FORUM),
     /** Returns the images that are attached to the specified message. */
     FORUMSERVICE_GET_IMAGES_BY_MESSAGE_ID(EntityType.FORUM),
     /**
      * Returns the index of the specified message one of the specified parent's
      * children.
      */
     FORUMSERVICE_GET_INDEX_OF_CHILD(EntityType.FORUM),
     /** Returns the number of messages in the specified community. */
     FORUMSERVICE_GET_MESSAGE_COUNT_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns the number of messages in the specified community, as constrained by
      * the specified filter.
      */
     FORUMSERVICE_GET_MESSAGE_COUNT_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /** Returns the number of messages in the specified thread. */
     FORUMSERVICE_GET_MESSAGE_COUNT_BY_THREAD_ID(EntityType.FORUM),
     /**
      * Returns the number of messages in the specified thread, as constrained by the
      * specified filter.
      */
     FORUMSERVICE_GET_MESSAGE_COUNT_BY_THREAD_IDAND_FILTER(EntityType.FORUM),
     /**
      * Returns the number of messages that are in the specified community, as
      * constrained by the specified filter.
      */
     FORUMSERVICE_GET_MESSAGE_COUNTS_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /**
      * Returns the depth-level of the specified message in the message tree
      * hierarchy.
      */
     FORUMSERVICE_GET_MESSAGE_DEPTH(EntityType.FORUM),
     /** Returns IDs for all messages in the specified community. */
     FORUMSERVICE_GET_MESSAGE_IDS_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns IDs for all messages in the specified community, constrained by the
      * specified filter.
      */
     FORUMSERVICE_GET_MESSAGE_IDS_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /** Returns IDs of all messages in the specified thread. */
     FORUMSERVICE_GET_MESSAGE_IDS_BY_THREAD_ID(EntityType.FORUM),
     /**
      * Returns IDs for all of the messages in the thread, as constrained by the
      * specified filter.
      */
     FORUMSERVICE_GET_MESSAGE_IDS_BY_THREAD_IDAND_FILTER(EntityType.FORUM),
     /** Returns the extended properties for the specified message. */
     FORUMSERVICE_GET_MESSAGE_PROPERTIES(EntityType.FORUM),
     /** Returns the value of the specified extended property for a message. */
     FORUMSERVICE_GET_MESSAGE_PROPERTY(EntityType.FORUM),
     /** Returns all of the messages in the specified community. */
     FORUMSERVICE_GET_MESSAGES_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns all of the messages in the specified community, as constrained by the
      * specified filter.
      */
     FORUMSERVICE_GET_MESSAGES_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /** Returns the messages in the specified thread. */
     FORUMSERVICE_GET_MESSAGES_BY_THREAD_ID(EntityType.FORUM),
     /**
      * Returns all of the messages in the specified, constrained by the the specified
      * filter.
      */
     FORUMSERVICE_GET_MESSAGES_BY_THREAD_IDAND_FILTER(EntityType.FORUM),
     /** Returns the parent of the specified message. */
     FORUMSERVICE_GET_PARENT(EntityType.FORUM),
     /** Returns the IDs for all the popular threads in the system. */
     FORUMSERVICE_GET_POPULAR_THREADS(EntityType.FORUM),
     /** Returns the popular threads in the specified community. */
     FORUMSERVICE_GET_POPULAR_THREADS_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns the total number of the specified paren'ts children, recursively; 0 if
      * there are no children.
      */
     FORUMSERVICE_GET_RECURSIVE_CHILD_COUNT(EntityType.FORUM),
     /**
      * Returns all descendants (child messages and all of their sub-children) of the
      * specified parent.
      */
     FORUMSERVICE_GET_RECURSIVE_CHILDREN(EntityType.FORUM),
     /** Returns all messages in the specified thread in depth-first order. */
     FORUMSERVICE_GET_RECURSIVE_MESSAGES(EntityType.FORUM),
     /**
      * Returns the root message of the specified thread; null if thread has no nodes.
      */
     FORUMSERVICE_GET_ROOT_MESSAGE(EntityType.FORUM),
     /** Returns the number of threads in the specified community. */
     FORUMSERVICE_GET_THREAD_COUNT_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns the number of threads in the specified community, as constrained by
      * the specified filter.
      */
     FORUMSERVICE_GET_THREAD_COUNT_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /** Returns all the extended properties for the specified thread. */
     FORUMSERVICE_GET_THREAD_PROPERTIES(EntityType.FORUM),
     /**
      * Returns the value of the specified extended property for the specified thread.
      */
     FORUMSERVICE_GET_THREAD_PROPERTY(EntityType.FORUM),
     /** Returns all of the IDs for threads the specified community. */
     FORUMSERVICE_GET_THREADS_BY_COMMUNITY_ID(EntityType.FORUM),
     /**
      * Returns all of the IDs for threads the specified community, as constrained by
      * the specified result filter.
      */
     FORUMSERVICE_GET_THREADS_BY_COMMUNITY_IDAND_FILTER(EntityType.FORUM),
     /** Returns message properties without applying filters to them first. */
     FORUMSERVICE_GET_UNFILTERED_MESSAGE_PROPERTIES(EntityType.FORUM),
     /** Returns true if the specified message has a parent message. */
     FORUMSERVICE_HAS_PARENT(EntityType.FORUM),
     /** Returns true if the specified message has no child messages. */
     FORUMSERVICE_IS_LEAF(EntityType.FORUM),
     /** Moves the specified thread to the specified community. */
     FORUMSERVICE_MOVE_THREAD(EntityType.FORUM),
     /** Moves the specified thread to the specified container. */
     FORUMSERVICE_MOVE_THREAD_TO_CONTAINER(EntityType.FORUM),
     /** Removes the specified attachment. */
     FORUMSERVICE_REMOVE_ATTACHMENT(EntityType.FORUM),
     /** Sets an extended property for the specified message. */
     FORUMSERVICE_SET_MESSAGE_PROPERTY(EntityType.FORUM),
     /** Sets an extended property for the specified thread. */
     FORUMSERVICE_SET_THREAD_PROPERTY(EntityType.FORUM),
     /** Updates the subject and body of the specified message. */
     FORUMSERVICE_UPDATE_FORUM_MESSAGE(EntityType.FORUM),
     /** Uploads a new attachment to the specified message. */
     FORUMSERVICE_UPLOAD_ATTACHMENT_TO_MESSAGE(EntityType.FORUM),
     /** Makes the specified user an administrator of the specified group. */
     GROUPSERVICE_ADD_ADMINISTRATOR_TO_GROUP(EntityType.GROUP),
     /** Adds the specified user to the specified group. */
     GROUPSERVICE_ADD_MEMBER_TO_GROUP(EntityType.GROUP),
     /** Creates a new user group. */
     GROUPSERVICE_CREATE_GROUP(EntityType.GROUP),
     /** Deletes the group with the specified ID. */
     GROUPSERVICE_DELETE_GROUP(EntityType.GROUP),
     /** Deletes an extended property from the specified group. */
     GROUPSERVICE_DELETE_PROPERTY(EntityType.GROUP),
     /** Returns the count of administrators for the specified group. */
     GROUPSERVICE_GET_ADMINISTRATOR_COUNT(EntityType.GROUP),
     /** Returns a user group by its ID. */
     GROUPSERVICE_GET_GROUP(EntityType.GROUP),
     /** Returns an array of IDs for all users that administer the specified group. */
     GROUPSERVICE_GET_GROUP_ADMINS(EntityType.GROUP),
     /** Returns a user group by its name. */
     GROUPSERVICE_GET_GROUP_BY_NAME(EntityType.GROUP),
     /** Returns a count of all groups in the system. */
     GROUPSERVICE_GET_GROUP_COUNT(EntityType.GROUP),
     /** Returns the user information for members of the specified group. */
     GROUPSERVICE_GET_GROUP_MEMBERS(EntityType.GROUP),
     /** Returns the names for all the groups in the system. */
     GROUPSERVICE_GET_GROUP_NAMES(EntityType.GROUP),
     /**
      * Returns names for groups beginning at startIndex and until the number results
      * equals numResults.
      */
     GROUPSERVICE_GET_GROUP_NAMES_BOUNDED(EntityType.GROUP),
     /** Returns the group IDs for all groups in the system. */
     GROUPSERVICE_GET_GROUPS(EntityType.GROUP),
     /** Returns the specified group's extended properties. */
     GROUPSERVICE_GET_PROPERTIES(EntityType.GROUP),
     /** Returns the names of groups that the specified user belongs to. */
     GROUPSERVICE_GET_USER_GROUP_NAMES(EntityType.GROUP),
     /** Returns the IDs of groups that a user belongs to. */
     GROUPSERVICE_GET_USER_GROUPS(EntityType.GROUP),
     /** Returns true if this GroupService is read-only. */
     GROUPSERVICE_IS_READ_ONLY(EntityType.GROUP),
     /** Removes the specified user as an administrator from the specified group. */
     GROUPSERVICE_REMOVE_ADMINISTRATOR_FROM_GROUP(EntityType.GROUP),
     /** Removes the specified user from the specified group. */
     GROUPSERVICE_REMOVE_MEMBER_FROM_GROUP(EntityType.GROUP),
     /** Sets a new extended property on the specified group. */
     GROUPSERVICE_SET_PROPERTY(EntityType.GROUP),
     /** Updates the specified group. */
     GROUPSERVICE_UPDATE_GROUP(EntityType.GROUP),
     /** Configures the connection between SBS and the XMPP server. */
     IMSERVICE_CONFIGURE_COMPONENT(EntityType.IMSERVICE),
     /** Generates a new nonce that can be used to SSO from Openfire. */
     IMSERVICE_GENERATE_NONCE(EntityType.IMSERVICE),
     /**
      * Tests the user's credentials to ensure that they have the required
      * permissions.
      */
     IMSERVICE_TEST_CREDENTIALS(EntityType.IMSERVICE),
     /** Returns a list of installed plugins. */
     PLUGINSERVICE_GET_PLUGIN_INFO(EntityType.PLUGIN),
     /** Installs the specified plugin. */
     PLUGINSERVICE_INSTALL_PLUGIN(EntityType.PLUGIN),
     /** Unistalls the specified plugin. */
     PLUGINSERVICE_UNINSTALL_PLUGIN(EntityType.PLUGIN),
     /** Add a guest vote for an option in the poll. */
     POLLSERVICE_ADD_ANONYMOUS_VOTE(EntityType.POLL),
     /** Add a new option to the poll. */
     POLLSERVICE_ADD_OPTION(EntityType.POLL),
     /** Add a user vote for an option in the poll. */
     POLLSERVICE_ADD_USER_VOTE(EntityType.POLL),
     /** Change a guest vote. */
     POLLSERVICE_CHANGE_ANONYMOUS_VOTE(EntityType.POLL),
     /** Change a user vote. */
     POLLSERVICE_CHANGE_USER_VOTE(EntityType.POLL),
     /** Creates a new poll. */
     POLLSERVICE_CREATE_POLL(EntityType.POLL),
     /** Remove an option from the poll. */
     POLLSERVICE_DELETE_OPTION(EntityType.POLL),
     /** Deletes the specified poll. */
     POLLSERVICE_DELETE_POLL(EntityType.POLL),
     /** Returns a count of all active polls in the system. */
     POLLSERVICE_GET_ACTIVE_POLL_COUNT(EntityType.POLL),
     /** Returns a count of all active polls of a given type and object ID. */
     POLLSERVICE_GET_ACTIVE_POLL_COUNT_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns an iterable of active polls in the system. */
     POLLSERVICE_GET_ACTIVE_POLLS(EntityType.POLL),
     /**
      * Returns a list of active polls associated with the object specified by the
      * objectType and objectID.
      */
     POLLSERVICE_GET_ACTIVE_POLLS_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns a count of all guests user votes for all options in the poll. */
     POLLSERVICE_GET_ANONYMOUS_VOTE_COUNT(EntityType.POLL),
     /** Returns a count of all user votes for the specified option in the poll. */
     POLLSERVICE_GET_ANONYMOUS_VOTE_COUNT_BY_INDEX(EntityType.POLL),
     /**
      * Returns a list of option indexes corresponding to the anonymous votes, or an
      * empty array if the user has not voted.
      */
     POLLSERVICE_GET_ANONYMOUS_VOTE_INDICES(EntityType.POLL),
     /**
      * Returns a list of uniqueID's for guests who have voted for the option at the
      * given index.
      */
     POLLSERVICE_GET_ANONYMOUS_VOTES(EntityType.POLL),
     /**
      * Returns an Iterator of uniqueID's for guests who have voted for the option at
      * the given index.
      */
     POLLSERVICE_GET_ANONYMOUS_VOTES_BY_INDEX(EntityType.POLL),
     /** Returns a count of all live polls in the system. */
     POLLSERVICE_GET_LIVE_POLL_COUNT(EntityType.POLL),
     /** Returns a count of all live polls of a given type and object ID. */
     POLLSERVICE_GET_LIVE_POLL_COUNT_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns a List of live polls in the system. */
     POLLSERVICE_GET_LIVE_POLLS(EntityType.POLL),
     /**
      * Returns an iterable of live polls associated with the object specified by the
      * objectType and objectID.
      */
     POLLSERVICE_GET_LIVE_POLLS_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns the specified poll. */
     POLLSERVICE_GET_POLL(EntityType.POLL),
     /** Returns a count of all polls, both active and inactive. */
     POLLSERVICE_GET_POLL_COUNT(EntityType.POLL),
     /**
      * Returns a count of polls, both active and inactive, associated with the object
      * specified by the objectType and objectID.
      */
     POLLSERVICE_GET_POLL_COUNT_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns a list of all polls, both active and inactive. */
     POLLSERVICE_GET_POLLS(EntityType.POLL),
     /**
      * Returns a list of polls, both active and inactive, associated with the object
      * specified by the objectType and objectID.
      */
     POLLSERVICE_GET_POLLS_BY_OBJECT_TYPE_AND_OBJECT_ID(EntityType.POLL),
     /** Returns a count of all user votes for all options in the poll. */
     POLLSERVICE_GET_USER_VOTE_COUNT(EntityType.POLL),
     /** Returns a count of all user votes for the specified option in the poll. */
     POLLSERVICE_GET_USER_VOTE_COUNT_BY_INDEX(EntityType.POLL),
     /**
      * Returns a list of option indexes corresponding to the user votes, or an empty
      * array if the user has not voted.
      */
     POLLSERVICE_GET_USER_VOTE_INDICES(EntityType.POLL),
     /**
      * Returns a List of User objects for users who have voted for any options in the
      * poll.
      */
     POLLSERVICE_GET_USER_VOTES(EntityType.POLL),
     /**
      * Returns a list of User objects for users who have voted for the option at the
      * given index.
      */
     POLLSERVICE_GET_USER_VOTES_BY_INDEX(EntityType.POLL),
     /**
      * Returns a count of all votes (both guest and user votes) for all options in
      * the poll.
      */
     POLLSERVICE_GET_VOTE_COUNT(EntityType.POLL),
     /**
      * Returns a count of all votes (both guest and user votes) for the specified
      * option in the poll.
      */
     POLLSERVICE_GET_VOTE_COUNT_BY_INDEX(EntityType.POLL),
     /**
      * Returns true if the guest associated with the uniqueID has previously voted in
      * the poll, false otherwise.
      */
     POLLSERVICE_HAS_ANONYMOUS_VOTED(EntityType.POLL),
     /**
      * Returns true if the user specified has previously voted in the poll, false
      * otherwise.
      */
     POLLSERVICE_HAS_USER_VOTED(EntityType.POLL),
     /** Returns true if the mode specified is enabled for the poll, false otherwise. */
     POLLSERVICE_IS_MODE_ENABLED(EntityType.POLL),
     /** Remove a guest vote. */
     POLLSERVICE_REMOVE_ANONYMOUS_VOTE(EntityType.POLL),
     /** Remove a user vote. */
     POLLSERVICE_REMOVE_USER_VOTE(EntityType.POLL),
     /** Sets a mode to be enabled or disabled for the poll. */
     POLLSERVICE_SET_MODE(EntityType.POLL),
     /** Sets the text of the option at the specified index. */
     POLLSERVICE_SET_OPTION(EntityType.POLL),
     /** Moves the option's index. */
     POLLSERVICE_SET_OPTION_INDEX(EntityType.POLL),
     /** Update the poll. */
     POLLSERVICE_UPDATE_POLLSERVICE(EntityType.POLL),
     /** Creates a new private message folder for the specified user. */
     PRIVATE_MESSAGE_SERVICE_CREATE_FOLDER(EntityType.PRIVATE_MESSAGE),
     /** Creates a new private message from the specified user. */
     PRIVATE_MESSAGE_SERVICE_CREATE_MESSAGE(EntityType.PRIVATE_MESSAGE),
     /** Deletes the specified message folder. */
     PRIVATE_MESSAGE_SERVICE_DELETE_FOLDER(EntityType.PRIVATE_MESSAGE),
     /**
      * Deletes a private message from the specified folder by moving it to the trash
      * folder.
      */
     PRIVATE_MESSAGE_SERVICE_DELETE_MESSAGE(EntityType.PRIVATE_MESSAGE),
     /** Returns the specified message folder for a user. */
     PRIVATE_MESSAGE_SERVICE_GET_FOLDER(EntityType.PRIVATE_MESSAGE),
     /** Returns a list of message folders for the specified user. */
     PRIVATE_MESSAGE_SERVICE_GET_FOLDERS(EntityType.PRIVATE_MESSAGE),
     /** Returns the specified private message. */
     PRIVATE_MESSAGE_SERVICE_GET_MESSAGE(EntityType.PRIVATE_MESSAGE),
     /** Returns the total number of private messages a user has in their mailbox. */
     PRIVATE_MESSAGE_SERVICE_GET_MESSAGE_COUNT(EntityType.PRIVATE_MESSAGE),
     /** Returns the number of messages in the specified folder. */
     PRIVATE_MESSAGE_SERVICE_GET_MESSAGE_COUNT_FOR_FOLDER(EntityType.PRIVATE_MESSAGE),
     /**
      * Returns all the messages in the specified folder sorted by date in descending
      * order.
      */
     PRIVATE_MESSAGE_SERVICE_GET_MESSAGES(EntityType.PRIVATE_MESSAGE),
     /** Returns the total number of unread messages a user has in their mailbox. */
     PRIVATE_MESSAGE_SERVICE_GET_UNREAD_MESSAGE_COUNT(EntityType.PRIVATE_MESSAGE),
     /**
      * Returns the total number of unread private messages a user has in the
      * specified folder.
      */
     PRIVATE_MESSAGE_SERVICE_GET_UNREAD_MESSAGE_COUNT_FOR_FOLDER(EntityType.PRIVATE_MESSAGE),
     /**
      * Returns <tt>true</tt> if private messaging is enabled; <tt>false</tt>
      * otherwise.
      */
     PRIVATE_MESSAGE_SERVICE_IS_PRIVATE_MESSAGES_ENABLED(EntityType.PRIVATE_MESSAGE),
     /** Moves a private message to another message folder. */
     PRIVATE_MESSAGE_SERVICE_MOVE_MESSAGE(EntityType.PRIVATE_MESSAGE),
     /**
      * Saves a message as a draft by storing it in the sender's <tt>Drafts</tt>
      * folder.
      */
     PRIVATE_MESSAGE_SERVICE_SAVE_MESSAGE_AS_DRAFT(EntityType.PRIVATE_MESSAGE),
     /** Sends a private message to another user. */
     PRIVATE_MESSAGE_SERVICE_SEND_MESSAGE(EntityType.PRIVATE_MESSAGE),
     /**
      * access, update, and remove profile fields data. All user profile data is
      * managed via the ProfileService.
      */
     PROFILE_FIELD_SERVICE_DEFINES_METHODS_USED_TO_CREATE(EntityType.PROFILE_FIELD),
     /** Creates a new profile field. */
     PROFILE_FIELD_SERVICE_CREATE_PROFILE_FIELD(EntityType.PROFILE_FIELD),
     /** Removes a profile field from the system. */
     PROFILE_FIELD_SERVICE_DELETE_PROFILE_FIELD(EntityType.PROFILE_FIELD),
     /** Edits the profile field data. */
     PROFILE_FIELD_SERVICE_EDIT_PROFILE_FIELD(EntityType.PROFILE_FIELD),
     /** Edits the objects for a profile field. */
     PROFILE_FIELD_SERVICE_EDIT_PROFILE_FIELD_OPTIONS(EntityType.PROFILE_FIELD),
     /** Returns a list of all default profile fields. */
     PROFILE_FIELD_SERVICE_GET_DEFAULT_FIELDS(EntityType.PROFILE_FIELD),
     /** Gets a profile field object by its id. */
     PROFILE_FIELD_SERVICE_GET_PROFILE_FIELD(EntityType.PROFILE_FIELD),
     /** Gets the list of all profile fields in the system. */
     PROFILE_FIELD_SERVICE_GET_PROFILE_FIELDS(EntityType.PROFILE_FIELD),
     /** Sets the index of the profile field. */
     PROFILE_FIELD_SERVICE_SET_INDEX(EntityType.PROFILE_FIELD),
     /** Returns the the first 5 users that are similar to the specified user. */
     PROFILE_SEARCH_SERVICE_GET_SIMILAR_USER_RESULTS(EntityType.PROFILE_SEARCH),
     /** Returns true if the profile search feature is turned on. */
     PROFILE_SEARCH_SERVICE_IS_SEARCH_ENABLED(EntityType.PROFILE_SEARCH),
     /** Returns the users that correspond to the search query. */
     PROFILE_SEARCH_SERVICE_SEARCH(EntityType.PROFILE_SEARCH),
     /**
      * Returns the users that correspond to the search query beginning at startIndex
      * and until the number results equals numResults.
      */
     PROFILE_SEARCH_SERVICE_SEARCH_BOUNDED(EntityType.PROFILE_SEARCH),
     /** access, update, and remove user profile data. */
     PROFILESERVICE_MANAGES_USER_PROFILE_DATA_DEFINES_METHODS_USED_TO_CREATE(EntityType.PROFILE),
     /** Adds a new profile entry to a user. */
     PROFILESERVICE_ADD_PROFILE(EntityType.PROFILE),
     /** Removes all user profile data associated with a particular profile field. */
     PROFILESERVICE_DELETE_PROFILE_BY_ID(EntityType.PROFILE),
     /** Removes all user profile data associated with a particular user. */
     PROFILESERVICE_DELETE_PROFILE_BY_USER_ID(EntityType.PROFILE),
     /** Deletes all status information for the specified user. */
     PROFILESERVICE_DELETE_USER_STATUS(EntityType.PROFILE),
     /** Retrieves the current status for the specified user. */
     PROFILESERVICE_GET_CURRENT_STATUS(EntityType.PROFILE),
     /**
      * Gets a map of user profile values for a particular user mapped to their
      * corresponding <tt>ProfileField</tt> id.
      */
     PROFILESERVICE_GET_PROFILE(EntityType.PROFILE),
     /** Returns the profile image for a user. */
     PROFILESERVICE_GET_PROFILE_IMAGE(EntityType.PROFILE),
     /** Returns a list of recent user status updates sorted newest first. */
     PROFILESERVICE_GET_RECENT_STATUS_UPDATES(EntityType.PROFILE),
     /**
      * Returns a list of recent status updates for the specified user sorted newest
      * first.
      */
     PROFILESERVICE_GET_RECENT_STATUS_UPDATES_FOR_USER(EntityType.PROFILE),
     /** Returns the max length permitted for a status message. */
     PROFILESERVICE_GET_STATUS_MESSAGE_MAX_LENGTH(EntityType.PROFILE),
     /**
      * Returns a list of today's recent status updates for the specified user sorted
      * newest first.
      */
     PROFILESERVICE_GET_TODAYS_STATUS_UPDATES(EntityType.PROFILE),
     /** Retrieves the user status corresponding to the given status ID */
     PROFILESERVICE_GET_USER_STATUS(EntityType.PROFILE),
     /**
      * Returns a list of yesterday's recent status updates for the specified user
      * sorted newest first.
      */
     PROFILESERVICE_GET_YESTERDAYS_STATUS_UPDATES(EntityType.PROFILE),
     /** Returns true if the user status manager is enabled, false otherwise. */
     PROFILESERVICE_IS_STATUS_UPDATES_ENABLED(EntityType.PROFILE),
     /** Set the current status for a user */
     PROFILESERVICE_SET_CURRENT_STATUS(EntityType.PROFILE),
     /** Sets a array of profile values for a particular user. */
     PROFILESERVICE_SET_PROFILE(EntityType.PROFILE),
     /** Set a new profile page image for the specified user. */
     PROFILESERVICE_SET_PROFILE_IMAGE(EntityType.PROFILE),
     /** Creates a new project as a child of the parent container. */
     PROJECTSERVICE_CREATE_PROJECTSERVICE(EntityType.PROJECT),
     /** Deletes a project and all of its content. */
     PROJECTSERVICE_DELETE_PROJECTSERVICE(EntityType.PROJECT),
     /** Returns a list of checkpoints for the specified project. */
     PROJECTSERVICE_GET_CHECK_POINTS(EntityType.PROJECT),
     /** Returns the specified project. */
     PROJECTSERVICE_GET_PROJECT_BY_ID(EntityType.PROJECT),
     /***/
     PROJECTSERVICE_GET_PROJECT_COUNT(EntityType.PROJECT),
     /** Returns a list of the projects. */
     PROJECTSERVICE_GET_PROJECTS(EntityType.PROJECT),
     /** Returns the count of unique users that own tasks in the specified project. */
     PROJECTSERVICE_GET_USER_COUNT(EntityType.PROJECT),
     /** Sets the lists of checkpoints for the project. */
     PROJECTSERVICE_SET_CHECK_POINTS(EntityType.PROJECT),
     /** Saves project changes and broadcasts changes across the cluster. */
     PROJECTSERVICE_UPDATE_PROJECTSERVICE(EntityType.PROJECT),
     /** Add a rating to the JiveContentObject. */
     RATINGSSERVICE_ADD_RATING(EntityType.RATINGS),
     /** Create a new rating with the specified attributes. */
     RATINGSSERVICE_CREATE_RATING(EntityType.RATINGS),
     /** Returns the count of currently available ratings. */
     RATINGSSERVICE_GET_AVAILABLE_RATING_COUNT(EntityType.RATINGS),
     /** Returns an iterable of Rating objects that list all the available ratings. */
     RATINGSSERVICE_GET_AVAILABLE_RATINGS(EntityType.RATINGS),
     /**
      * A convenience method which returns a geometric mean average of all the ratings
      * given to the JiveContentObject.
      */
     RATINGSSERVICE_GET_MEAN_RATING(EntityType.RATINGS),
     /**
      * Returns the rating associated with the user, or null if this user hasn't rated
      * the JiveContentObject.
      */
     RATINGSSERVICE_GET_RATING(EntityType.RATINGS),
     /** Returns the total number of ratings given to the JiveContentObject. */
     RATINGSSERVICE_GET_RATING_COUNT(EntityType.RATINGS),
     /** Retrieve the rating with the specified score. */
     RATINGSSERVICE_GET_RATING_FROM_SCORE(EntityType.RATINGS),
     /** Returns an Iterable of all the ratings given to the JiveContentObject. */
     RATINGSSERVICE_GET_RATINGS(EntityType.RATINGS),
     /** Returns whether the user has rated the JiveContentObject or not. */
     RATINGSSERVICE_HAS_RATED(EntityType.RATINGS),
     /** Returns true if the rating feature is turned on. */
     RATINGSSERVICE_IS_RATINGS_ENABLED(EntityType.RATINGS),
     /** Remove the specified rating from the list of currently available ratings. */
     RATINGSSERVICE_REMOVE_RATING(EntityType.RATINGS),
     /** Enables or disables the ratings feature. */
     RATINGSSERVICE_SET_RATINGS_ENABLED(EntityType.RATINGS),
     /**
      * REFERENCES_ARE_USE_TO_CREATE_A_RELATIONSHIP between one kind of jive object to
      * another. For instance a blog may reference a specific thread.
      */
     /** Creates a reference between the refering object and the references. */
     REFERENCESERVICE_ADD_REFERENCE(EntityType.REFERENCE),
     /** Removes all references from the specified referer. */
     REFERENCESERVICE_DELETE_ALL_REFERENCES(EntityType.REFERENCE),
     /** Causes this object to not be referred by any other objects. */
     REFERENCESERVICE_DELETE_ALL_REFERERS(EntityType.REFERENCE),
     /** Delete the reference between a referer and the refered content. */
     REFERENCESERVICE_DELETE_REFERENCE(EntityType.REFERENCE),
     /** Get all objects that the specfied jiveObject referers too. */
     REFERENCESERVICE_GET_REFERENCES(EntityType.REFERENCE),
     /** Get a list of all the objects that refer to this specified object. */
     REFERENCESERVICE_GET_REFERERS(EntityType.REFERENCE),
     /** Returns the number of possible results for the specified query. */
     SEARCHSERVICE_COUNT_QUICK_MESSAGE_SEARCH_RESULTS_BY_COMMUNITY_ID(EntityType.SEARCH),
     /** Returns the number of possible results for the specified query. */
     SEARCHSERVICE_COUNT_QUICK_SEARCH_RESULTS(EntityType.SEARCH),
     /** Returns the number of possible results for the specified query. */
     SEARCHSERVICE_COUNT_SEARCH_RESULTS(EntityType.SEARCH),
     /** Returns the number of possible results for the specified query by category. */
     SEARCHSERVICE_COUNT_SEARCH_RESULTS_BY_COMMUNITY_ID(EntityType.SEARCH),
     /** Provides the ability to do quick search queries based on the provided string. */
     SEARCHSERVICE_QUICK_MESSAGE_SEARCH_BY_COMMUNITY_ID(EntityType.SEARCH),
     /** Provides the ability to do quick search queries based on the provided string. */
     SEARCHSERVICE_QUICK_SEARCH(EntityType.SEARCH),
     /**
      * Provides the ability to create complex search queries with the ability to
      * change sorting, filtering, etc.
      */
     SEARCHSERVICE_SEARCH_SEARCHSERVICE(EntityType.SEARCH),
     /**
      * Provides the ability to create complex search queries with the ability to
      * change sorting, filtering, etc. all by category.
      */
     SEARCHSERVICE_SEARCH_BY_COMMUNITIES(EntityType.SEARCH),
     /**
      * Add the user with the specified userID to the social group with the specified
      * socialGroupID.
      */
     SOCIAL_GROUP_SERVICE_ADD_MEMBER(EntityType.SOCIAL_GROUP),
     /** Creates a new social group. */
     SOCIAL_GROUP_SERVICE_CREATE_SOCIAL_GROUP(EntityType.SOCIAL_GROUP),
     /** Delete the social group with the specified id. */
     SOCIAL_GROUP_SERVICE_DELETE_SOCIAL_GROUP(EntityType.SOCIAL_GROUP),
     /** Returns a count of all members in a group */
     SOCIAL_GROUP_SERVICE_GET_MEMBER_COUNT(EntityType.SOCIAL_GROUP),
     /** Returns an array of all members of a particular social group. */
     SOCIAL_GROUP_SERVICE_GET_MEMBERS(EntityType.SOCIAL_GROUP),
     /** Returns a WSSocialGroup by its ID. */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUP(EntityType.SOCIAL_GROUP),
     /** Returns a WSSocialGroup by its displayName. */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUP_BY_NAME(EntityType.SOCIAL_GROUP),
     /** Returns a count of all social groups in the system. */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUP_COUNT(EntityType.SOCIAL_GROUP),
     /**
      * Returns an array of all the social group names for all the social groups in
      * the system.
      */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUP_NAMES(EntityType.SOCIAL_GROUP),
     /**
      * Returns an array of the social group names beginning at startIndex and until
      * the number results equals numResults.
      */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUP_NAMES_BOUNDED(EntityType.SOCIAL_GROUP),
     /**
      * Returns an array of all the social group IDs for all the social groups in the
      * system.
      */
     SOCIAL_GROUP_SERVICE_GET_SOCIAL_GROUPS(EntityType.SOCIAL_GROUP),
     /** Returns an array of social group names that an entity belongs to. */
     SOCIAL_GROUP_SERVICE_GET_USER_SOCIAL_GROUP_NAMES(EntityType.SOCIAL_GROUP),
     /** Returns an array of all the social group IDs that a user belongs too. */
     SOCIAL_GROUP_SERVICE_GET_USER_SOCIAL_GROUPS(EntityType.SOCIAL_GROUP),
     /**
      * Remove the user with the specified id from the social group with the specified
      * id.
      */
     SOCIAL_GROUP_SERVICE_REMOVE_MEMBER(EntityType.SOCIAL_GROUP),
     /** Returns an array of WSSocialGroup filtered. */
     SOCIAL_GROUP_SERVICE_SEARCH_SOCIAL_GROUPS(EntityType.SOCIAL_GROUP),
     /** Update the following social group in the system. */
     SOCIAL_GROUP_SERVICE_UPDATE_SOCIAL_GROUP(EntityType.SOCIAL_GROUP),
     /** Rewards points to a user. */
     STATUS_LEVEL_SERVICE_ADD_POINTS(EntityType.STATUS_LEVEL),
     /** Rewards points to a user. */
     STATUS_LEVEL_SERVICE_ADD_POINTS_2(EntityType.STATUS_LEVEL),
     /** Creates a new points based StatusLevel. */
     STATUS_LEVEL_SERVICE_CREATE_STATUS_LEVEL(EntityType.STATUS_LEVEL),
     /** Creates a new group based StatusLevel Level */
     STATUS_LEVEL_SERVICE_CREATE_STATUS_LEVEL_2(EntityType.STATUS_LEVEL),
     /** Deletes a statusLevel level from the system */
     STATUS_LEVEL_SERVICE_DELETE_STATUS_LEVEL(EntityType.STATUS_LEVEL),
     /** Returns all of the WSStatusLevelScenario objects. */
     STATUS_LEVEL_SERVICE_GET_ALL_STATUS_LEVEL_SCENARIOS(EntityType.STATUS_LEVEL),
     /**
      * If there is a status level associated with the group passed in then the status
      * level will be returned, Otherwise null will be returned.
      */
     STATUS_LEVEL_SERVICE_GET_GROUP_STATUS_LEVEL(EntityType.STATUS_LEVEL),
     /** Returns an array of all group based status levels in the system. */
     STATUS_LEVEL_SERVICE_GET_GROUP_STATUS_LEVELS(EntityType.STATUS_LEVEL),
     /** Returns an array of system wide leaders. */
     STATUS_LEVEL_SERVICE_GET_LEADERS(EntityType.STATUS_LEVEL),
     /** Returns an array of system wide leaders. */
     STATUS_LEVEL_SERVICE_GET_LEADERS_2(EntityType.STATUS_LEVEL),
     /** Returns an Iterable of leaders for a specific community */
     STATUS_LEVEL_SERVICE_GET_LEADERS_BY_COMMUNITY(EntityType.STATUS_LEVEL),
     /** Returns an Iterable of leaders for a specific container */
     STATUS_LEVEL_SERVICE_GET_LEADERS_BY_CONTAINER(EntityType.STATUS_LEVEL),
     /** Returns an Iterable of leaders for a specific community */
     STATUS_LEVEL_SERVICE_GET_LEADERS_3(EntityType.STATUS_LEVEL),
     /** Returns an Iterable of leaders for a specific container */
     STATUS_LEVEL_SERVICE_GET_LEADERS_4(EntityType.STATUS_LEVEL),
     /** Returns the point level for a user system wide. */
     STATUS_LEVEL_SERVICE_GET_POINT_LEVEL(EntityType.STATUS_LEVEL),
     /** Returns the status level points for a user in regards to a specific community */
     STATUS_LEVEL_SERVICE_GET_POINT_LEVEL_2(EntityType.STATUS_LEVEL),
     /** Returns the status level points for a user in regards to a specific container */
     STATUS_LEVEL_SERVICE_GET_POINT_LEVEL_3(EntityType.STATUS_LEVEL),
     /**
      * Returns an array of point based status levels in the system sorted by point
      * range.
      */
     STATUS_LEVEL_SERVICE_GET_POINT_STATUS_LEVELS(EntityType.STATUS_LEVEL),
     /** Used to acquire a specific status level object from the system */
     STATUS_LEVEL_SERVICE_GET_STATUS_LEVEL(EntityType.STATUS_LEVEL),
     /** Used to get a status level by a point value. */
     STATUS_LEVEL_SERVICE_GET_STATUS_LEVEL_BY_POINTS(EntityType.STATUS_LEVEL),
     /** Returns a WSStatusLevelScenario by its code. */
     STATUS_LEVEL_SERVICE_GET_STATUS_LEVEL_SCENARIO_BY_CODE(EntityType.STATUS_LEVEL),
     /** Returns a WSStatusLevelScenario by its code. */
     STATUS_LEVEL_SERVICE_GET_STATUS_LEVEL_SCENARIO_BY_CODE_2(EntityType.STATUS_LEVEL),
     /**
      * Returns the system wide status level for specific user, will return null if
      * there is no status level for this user.
      */
     STATUS_LEVEL_SERVICE_GET_USER_STATUS_LEVEL(EntityType.STATUS_LEVEL),
     /** Returns true if status levels are enabled in the system */
     STATUS_LEVEL_SERVICE_IS_STATUS_LEVELS_ENABLED(EntityType.STATUS_LEVEL),
     /** Sets whether status levels should be enabled in the system. */
     STATUS_LEVEL_SERVICE_SET_STATUS_LEVELS_ENABLED(EntityType.STATUS_LEVEL),
     /**
      * Update the points and whether or not this scenario is included in status level
      * results.
      */
     STATUS_LEVEL_SERVICE_UPDATE_STATUS_LEVEL_SCENARIO(EntityType.STATUS_LEVEL),
     /** Deletes a Jive System Property. */
     SYSTEM_PROPERTIES_SERVICE_DELETE_PROPERTY(EntityType.SYSTEM_PROPERTIES),
     /** Obtains all Jive System Properties. */
     SYSTEM_PROPERTIES_SERVICE_GET_PROPERTIES(EntityType.SYSTEM_PROPERTIES),
     /** Saves a name/value pair as a Jive System Property. */
     SYSTEM_PROPERTIES_SERVICE_SAVE_PROPERTY(EntityType.SYSTEM_PROPERTIES),
     /** Associates a tag with this object */
     TAGSERVICE_ADD_TAG(EntityType.TAG),
     /** Creates a tag in the database. */
     TAGSERVICE_CREATE_TAG(EntityType.TAG),
     /** Returns a tag given a tag ID. */
     TAGSERVICE_GET_TAG_BY_ID(EntityType.TAG),
     /** Returns a tag by tag name. */
     TAGSERVICE_GET_TAG_BY_NAME(EntityType.TAG),
     /** Return an Iterable for all the tags associated with this manager. */
     TAGSERVICE_GET_TAGS(EntityType.TAG),
     /** Disassociates this object with all tags. */
     TAGSERVICE_REMOVE_ALL_TAGS(EntityType.TAG),
     /** Disassociates this object with the given tag. */
     TAGSERVICE_REMOVE_TAG(EntityType.TAG),
     /** Creates a new task within the provided project. */
     TASKSERVICE_CREATE_TASKSERVICE(EntityType.TASK),
     /** Creates a new personal task. */
     TASKSERVICE_CREATE_PERSONAL_TASK(EntityType.TASK),
     /** Delete a task. */
     TASKSERVICE_DELETE_TASKSERVICE(EntityType.TASK),
     /** Returns a count of all the incomplete tasks in the system. */
     TASKSERVICE_GET_TASK_COUNT(EntityType.TASK),
     /**
      * Returns a count of all the incomplete tasks in the system that match the
      * provided result filter.
      */
     TASKSERVICE_GET_TASK_COUNT_WITH_FILTER(EntityType.TASK),
     /** Retrieves the task with the given ID. */
     TASKSERVICE_GET_TASKBY_ID(EntityType.TASK),
     /** Returns a system wide iterator of incomplete tasks. */
     TASKSERVICE_GET_TASKS(EntityType.TASK),
     /** Returns all the tasks of a project. */
     TASKSERVICE_GET_TASKS_BY_PROJECT(EntityType.TASK),
     /** Returns an iterator of tasks that match the provided result filter. */
     TASKSERVICE_GET_TASKS_WITH_FILTER(EntityType.TASK),
     /** Returns all the incomplete tasks of a user. */
     TASKSERVICE_GET_UNCOMPLETE_TASKS_BY_USER_ID(EntityType.TASK),
     /** Persist updates to a task to storage. */
     TASKSERVICE_UPDATE_TASKSERVICE(EntityType.TASK),
     /**
      * Creates a new user account from a minimum of information: username, password,
      * and email address.
      */
     USERSERVICE_CREATE_USER(EntityType.USER),
     /** Creates a new user account from user data. */
     USERSERVICE_CREATE_USER_WITH_USER(EntityType.USER),
     /** Deletes an extended property from the specified user account. */
     USERSERVICE_DELETE_USER_PROPERTY(EntityType.USER),
     /** Disables a user account. */
     USERSERVICE_DISABLE_USER(EntityType.USER),
     /** Enables a user account. */
     USERSERVICE_ENABLE_USER(EntityType.USER),
     /** Returns the user specified by ID. */
     USERSERVICE_GET_USER(EntityType.USER),
     /** Returns the user account corresponding to the specified email address. */
     USERSERVICE_GET_USER_BY_EMAIL_ADDRESS(EntityType.USER),
     /** Returns the user specified by username (case-insensitive). */
     USERSERVICE_GET_USER_BY_USERNAME(EntityType.USER),
     /** Returns the number of users in the system. */
     USERSERVICE_GET_USER_COUNT(EntityType.USER),
     /** Returns all of the userNames in the system. */
     USERSERVICE_GET_USER_NAMES(EntityType.USER),
     /** Returns the extended properties for the specified user account. */
     USERSERVICE_GET_USER_PROPERTIES(EntityType.USER),
     /** Returns the IDs of the first 1000 users. */
     USERSERVICE_GET_USERS(EntityType.USER),
     /**
      * Returns the IDs of users begining at startIndex and until the number results
      * equals numResults.
      */
     USERSERVICE_GET_USERS_BOUNDED(EntityType.USER),
     /** Returns true if this UserService is read-only. */
     USERSERVICE_IS_READ_ONLY(EntityType.USER),
     /** Changes the specified user's password. */
     USERSERVICE_SET_PASSWORD(EntityType.USER),
     /** Sets an extended property for the specified user. */
     USERSERVICE_SET_USER_PROPERTY(EntityType.USER),
     /** Updates information for the specified user. */
     USERSERVICE_UPDATE_USER(EntityType.USER),
     /** Creates a new video in the specified container. */
     VIDEOSERVICE_CREATE_VIDEO(EntityType.VIDEO),
     /** Deletes the specified video. */
     VIDEOSERVICE_DELETE_VIDEO(EntityType.VIDEO),
     /** Returns the specified video. */
     VIDEOSERVICE_GET_VIDEO(EntityType.VIDEO),
     /** Returns a list of videos in the specified container. */
     VIDEOSERVICE_GET_VIDEOS_FOR_CONTAINER(EntityType.VIDEO),
     /** Updates the specified video. */
     VIDEOSERVICE_UPDATE_VIDEO(EntityType.VIDEO),
     /** Create a watch on a community for the specified user. */
     WATCHSERVICE_CREATE_COMMUNITY_WATCH(EntityType.WATCH),
     /** Create a watch on a thread for the specified user. */
     WATCHSERVICE_CREATE_THREAD_WATCH(EntityType.WATCH),
     /** Create a watch on a user for the specified user. */
     WATCHSERVICE_CREATE_USER_WATCH(EntityType.WATCH),
     /** Delete the specified watch. */
     WATCHSERVICE_DELETE_WATCH(EntityType.WATCH),
     /** Deletes all watches that a user has. */
     WATCHSERVICE_DELETE_WATCHES(EntityType.WATCH),
     /** Returns a watch on a particular community, or null if there isn't a watch. */
     WATCHSERVICE_GET_COMMUNITY_WATCH(EntityType.WATCH),
     /**
      * Return the count of all community watches in a particular communityID for the
      * given userID.
      */
     WATCHSERVICE_GET_COMMUNITY_WATCH_COUNT(EntityType.WATCH),
     /**
      * Returns an array of IDs for all the community objects a user is watching in a
      * community.
      */
     WATCHSERVICE_GET_COMMUNITY_WATCHES(EntityType.WATCH),
     /**
      * Returns the number of days that a watched object can remain inactive before
      * watches on that object are deleted.
      */
     WATCHSERVICE_GET_DELETE_DAYS(EntityType.WATCH),
     /** Returns a watch on a particular thread, or null if there isn't a watch. */
     WATCHSERVICE_GET_THREAD_WATCH(EntityType.WATCH),
     /** Returns a count of all watches that a userID has of a particular type. */
     WATCHSERVICE_GET_TOTAL_WATCH_COUNT(EntityType.WATCH),
     /** Returns a watch on a particular user, or null if there isn't a watch. */
     WATCHSERVICE_GET_USER_WATCH(EntityType.WATCH),
     /**
      * Returns an array of Watch objects for a particular object type that the given
      * user is watching.
      */
     WATCHSERVICE_GET_WATCH_LIST(EntityType.WATCH),
     /** Returns all the users who are watching this objectType and objectID. */
     WATCHSERVICE_GET_WATCH_USERS(EntityType.WATCH),
     /** Returns true if the user is watching the specified community. */
     WATCHSERVICE_IS_COMMUNITY_WATCHED(EntityType.WATCH),
     /** Returns true if the user is watching the specified thread. */
     WATCHSERVICE_IS_THREAD_WATCHED(EntityType.WATCH),
     /** Returns true if the user is watching the specified user. */
     WATCHSERVICE_IS_USER_WATCHED(EntityType.WATCH),
     /**
      * Sets the number of days that a watched object can remain inactive before
      * watches on that object are deleted.
      */
     WATCHSERVICE_SET_DELETE_DAYS(EntityType.WATCH);
 
     private static final int BASE_URI = 1;
 
     private final String resourceUri;
     private EntityType entityType = null;
     private String protocol;
 
     private Operation(final EntityType type)
     {
         final String[] split = StringUtils.split(this.toString(), '_');
         final StringBuffer str = new StringBuffer();
         str.append(split[1].toLowerCase());
         for (int i = 2; i < split.length; i++)
         {
             str.append(StringUtils.capitalize(split[i].toLowerCase()));
         }
         this.resourceUri = str.toString();
 
         setProtocolFromName();
 
         this.entityType = type;
     }
 
     private Operation(final CustomOp customType)
     {
         // this.resourceUri =UriFactory.generateCustomUri(customType);
         this.resourceUri = null;
         this.protocol = customType.getMethod();
     }
 
     private Operation(final EntityType type, final int strategy)
     {
         this.entityType = type;
         if (strategy == BASE_URI)
         {
             this.resourceUri = this.entityType.generateBaseUri();
         }
         else
         {
             this.resourceUri = "";
         }
         setProtocolFromName();
     }
 
     public String getResourceUri()
     {
         return resourceUri;
     }
 
     public String getProtocol()
     {
         return protocol;
     }
 
     /**
      * @return The root tag xml element name for this custom operation.
      */
     public final String getRootTagElementName()
     {
         final String[] split = StringUtils.split(this.toString(), '_');
         final StringBuffer res = new StringBuffer();
         res.append(split[1].toLowerCase());
         if (split.length > 2)
         {
             for (int i = 2; i < split.length; i++)
             {
                 res.append(StringUtils.capitalize(split[i].toLowerCase()));
             }
         }
         return res.toString();
     }
 
     private void setProtocolFromName()
     {
         final String[] split = StringUtils.split(this.toString(), '_');
         if (split[1].equals("ADD") || split[1].equals("CREATE"))
         {
             this.protocol = "POST";
         }
         else if (split[1].equals("REMOVE"))
         {
             this.protocol = "DELETE";
         }
         else if (split[1].equals("GET") || split[1].equals("IS"))
         {
             this.protocol = "GET";
         }
         else if (split[1].equals("PUT"))
         {
             this.protocol = "PUT";
         }
         else
         {
             this.protocol = "";
         }
     }
 }
