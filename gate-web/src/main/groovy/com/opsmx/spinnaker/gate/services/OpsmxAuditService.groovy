/*
 * Copyright 2021 OpsMx, Inc.
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

package com.opsmx.spinnaker.gate.services

import retrofit.http.Body
import retrofit.http.POST
import retrofit.http.Path

interface OpsmxAuditService {

  @POST("/auditservice/{version}/{type}/{source}/{source1}")
  Object postAuditService1(@Path('version') String version,
                               @Path('type') String type,
                               @Path('source') String source,
                               @Path('source1') String source1,
                               @Body Object data)

}
