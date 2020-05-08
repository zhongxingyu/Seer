 package com.atlassian.plugin.osgi.factory.transform;
 
 /**
 * Performs a stage in the transformation from a JAR to an OSGi bundle
  *
  * @since 2.2.0
  */
 public interface TransformStage
 {
     /**
      * Transforms the jar by operating on the context
      *
      * Any transformation stage that generates Spring beans must ensure no collision of bean ids or names or aliases
      * by calling {@link TransformContext#trackBean(String, String)} whenever a new bean id or name or alias is created.
      *
      * @param context The transform context to operate on
      * @throws PluginTransformationException If the stage cannot be performed and the whole operation should be aborted
      */
    void execute(TransformContext context) throws PluginTransformationException;
 }
