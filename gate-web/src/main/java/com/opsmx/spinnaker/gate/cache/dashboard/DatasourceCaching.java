/*
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.spinnaker.gate.cache.dashboard;

import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface DatasourceCaching {

  @CachePut(
      value = "datasource",
      key = "#datasourceKey",
      cacheManager = "concurrentMapCacheManager")
  Map<String, Object> populateDatasourceCache(String datasourceKey, Map<String, Object> datasource);

  @CacheEvict(
      value = "datasource",
      key = "#datasourceKey",
      cacheManager = "concurrentMapCacheManager")
  void evictRecord(String datasourceKey);

  @Cacheable(
      value = "datasource",
      key = "#datasourceKey",
      cacheManager = "concurrentMapCacheManager")
  Map<String, Object> getRecord(String datasourceKey);
}
