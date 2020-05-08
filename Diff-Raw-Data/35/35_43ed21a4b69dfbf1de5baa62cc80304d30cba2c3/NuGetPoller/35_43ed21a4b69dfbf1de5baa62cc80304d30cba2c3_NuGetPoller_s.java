 package com.tw.go.plugin.nuget.apimpl;
 
 import com.thoughtworks.go.plugin.api.logging.Logger;
 import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
 import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
 import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
 import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
 import com.thoughtworks.go.plugin.api.response.Result;
 import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
 import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
 import com.tw.go.plugin.nuget.Feed;
 import com.tw.go.plugin.nuget.NuGetFeedDocument;
 import com.tw.go.plugin.nuget.NuGetParams;
 import com.tw.go.plugin.nuget.config.NuGetPackageConfig;
 import com.tw.go.plugin.nuget.config.NuGetRepoConfig;
 import com.tw.go.plugin.util.HttpRepoURL;
 import com.tw.go.plugin.util.RepoUrl;
 
 public class NuGetPoller implements PackageMaterialPoller {
     private static Logger LOGGER = Logger.getLoggerFor(NuGetPoller.class);
 
     public PackageRevision getLatestRevision(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
         LOGGER.info(String.format("getLatestRevision called with packageId %s, for repo: %s",
                 packageConfig.get(NuGetPackageConfig.PACKAGE_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));
         validateConfig(repoConfig, packageConfig);
         NuGetPackageConfig nuGetPackageConfig = new NuGetPackageConfig(packageConfig);
         NuGetParams params = new NuGetParams(
                 new NuGetRepoConfig(repoConfig).getRepoUrl(),
                 nuGetPackageConfig.getPackageId(),
                 nuGetPackageConfig.getPollVersionFrom(),
                 nuGetPackageConfig.getPollVersionTo(), null, nuGetPackageConfig.isIncludePreRelease());
         PackageRevision packageRevision = poll(params);
         LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                 packageRevision.getRevision(), packageRevision.getTimestamp()));
         return packageRevision;
     }
 
     public PackageRevision latestModificationSince(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig, PackageRevision previouslyKnownRevision) {
         LOGGER.info(String.format("latestModificationSince called with packageId %s, for repo: %s",
                 packageConfig.get(NuGetPackageConfig.PACKAGE_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));
         validateConfig(repoConfig, packageConfig);
         NuGetPackageConfig nuGetPackageConfig = new NuGetPackageConfig(packageConfig);
         NuGetParams params = new NuGetParams(
                 new NuGetRepoConfig(repoConfig).getRepoUrl(),
                 nuGetPackageConfig.getPackageId(),
                 nuGetPackageConfig.getPollVersionFrom(),
                 nuGetPackageConfig.getPollVersionTo(),
                 previouslyKnownRevision, nuGetPackageConfig.isIncludePreRelease());
         PackageRevision updatedPackage = poll(params);
         if (updatedPackage == null) {
             LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
             return null;
         }
         LOGGER.info(String.format("latestModificationSince returning with %s, %s",
                 updatedPackage.getRevision(), updatedPackage.getTimestamp()));
         if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
             LOGGER.warn(String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                     updatedPackage.getRevision(), updatedPackage.getTimestamp(), previouslyKnownRevision.getRevision(), previouslyKnownRevision.getTimestamp()));
         return updatedPackage;
     }
 
     @Override
     public Result checkConnectionToRepository(RepositoryConfiguration repoConfigs) {
         Result response = new Result();
         NuGetRepoConfig nuGetRepoConfig = new NuGetRepoConfig(repoConfigs);
         RepoUrl repoUrl = nuGetRepoConfig.getRepoUrl();
         if (repoUrl.isHttp()) {
             try {
                 repoUrl.checkConnection(((HttpRepoURL) repoUrl).getUrlStrWithTrailingSlash() + "$metadata");
             } catch (Exception e) {
                 response.withErrorMessages(e.getMessage());
             }
         } else {
             repoUrl.checkConnection();
         }
         return response;
     }
 
     @Override
     public Result checkConnectionToPackage(PackageConfiguration packageConfigs, RepositoryConfiguration repoConfigs) {
         Result response = checkConnectionToRepository(repoConfigs);
        if (!response.isSuccessful()) return response;
         PackageRevision packageRevision = getLatestRevision(packageConfigs, repoConfigs);
         response.withSuccessMessages("Found " + packageRevision.getRevision());
         return response;
     }
 
     private void validateConfig(RepositoryConfiguration repoConfig, PackageConfiguration packageConfig) {
         ValidationResult errors = new PluginConfig().isRepositoryConfigurationValid(repoConfig);
         errors.addErrors(new PluginConfig().isPackageConfigurationValid(packageConfig, repoConfig).getErrors());
         if (!errors.isSuccessful()) {
             StringBuilder stringBuilder = new StringBuilder();
             for (ValidationError validationError : errors.getErrors()) {
                 stringBuilder.append(validationError.getMessage()).append("; ");
             }
             String errorString = stringBuilder.toString();
             throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
         }
     }
 
     public PackageRevision poll(NuGetParams params) {
         String url = params.getQuery();
         LOGGER.info(url);
        return new NuGetFeedDocument(new Feed(url).download()).getPackageRevision(params.isLastVersionKnown());
     }
 }
