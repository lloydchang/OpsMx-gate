/*
 * Copyright 2021 OpsMx, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.gate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${message-broker.enabled:true}")
@ConditionalOnProperty(value = "message-broker.endpoint.name", havingValue = "rabbitmq")
public class RabbitMQConfig implements CamelRouteConfig {

  @Autowired private MessageBrokerProperties messageBrokerProperties;
  private static final String isdExchange = "isd.userLoginDetails";
  private String directUserActivity = "userLoginDetails";

  @Override
  public String getUserActivityQueueEndPoint() {
    return messageBrokerProperties.getEndpoint().getName()
        + ":"
        + isdExchange
        + "?queue="
        + directUserActivity
        + "&autoDelete=false&routingKey="
        + directUserActivity
        + "&declare=true&durable=true&exchangeType=direct&hostname="
        + messageBrokerProperties.getHost()
        + "&portNumber="
        + messageBrokerProperties.getPort()
        + "&username="
        + messageBrokerProperties.getUsername()
        + "&password="
        + messageBrokerProperties.getPassword();
  }
}
