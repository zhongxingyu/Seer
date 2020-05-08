 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.cachelist;
 
 import com.google.code.geobeagle.LocationControlBuffered.GpsDisabledLocation;
 import com.google.code.geobeagle.activity.cachelist.presenter.ActionAndTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.AdapterCachesSorter;
import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh.ActionManager;
 import com.google.code.geobeagle.activity.cachelist.presenter.DistanceUpdater;
 import com.google.code.geobeagle.activity.cachelist.presenter.LocationAndAzimuthTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.LocationTolerance;
 import com.google.code.geobeagle.activity.cachelist.presenter.SqlCacheLoader;
 import com.google.code.geobeagle.activity.cachelist.presenter.ToleranceStrategy;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Provider;
 
 class ActionManagerProvider implements Provider<ActionManager> {
     private final GpsDisabledLocation gpsDisabledLocation;
     private final AdapterCachesSorter adapterCachesSorter;
     private final DistanceUpdater distanceUpdater;
     private final SqlCacheLoader sqlCacheLoader;
 
     public ActionManagerProvider(GpsDisabledLocation gpsDisabledLocation,
             AdapterCachesSorter adapterCachesSorter, DistanceUpdater distanceUpdater,
             SqlCacheLoader sqlCacheLoader) {
         this.gpsDisabledLocation = gpsDisabledLocation;
         this.adapterCachesSorter = adapterCachesSorter;
         this.distanceUpdater = distanceUpdater;
         this.sqlCacheLoader = sqlCacheLoader;
     }
 
     @Inject
     public ActionManagerProvider(Injector injector) {
         this.gpsDisabledLocation = injector.getInstance(GpsDisabledLocation.class);
         this.adapterCachesSorter = injector.getInstance(AdapterCachesSorter.class);
         this.distanceUpdater = injector.getInstance(DistanceUpdater.class);
         this.sqlCacheLoader = injector.getInstance(SqlCacheLoader.class);
     }
 
     @Override
     public ActionManager get() {
 
         final ToleranceStrategy sqlCacheLoaderTolerance = new LocationTolerance(500,
                gpsDisabledLocation, 10000);
         final ToleranceStrategy adapterCachesSorterTolerance = new LocationTolerance(6,
                gpsDisabledLocation, 5000);
         final LocationTolerance distanceUpdaterLocationTolerance = new LocationTolerance(1,
                 gpsDisabledLocation, 1000);
         final ToleranceStrategy distanceUpdaterTolerance = new LocationAndAzimuthTolerance(
                 distanceUpdaterLocationTolerance, 720);
 
         final ActionAndTolerance[] actionAndTolerances = new ActionAndTolerance[] {
                 new ActionAndTolerance(sqlCacheLoader, sqlCacheLoaderTolerance),
                 new ActionAndTolerance(adapterCachesSorter, adapterCachesSorterTolerance),
                 new ActionAndTolerance(distanceUpdater, distanceUpdaterTolerance)
         };
 
         return new ActionManager(actionAndTolerances);
     }
 }
