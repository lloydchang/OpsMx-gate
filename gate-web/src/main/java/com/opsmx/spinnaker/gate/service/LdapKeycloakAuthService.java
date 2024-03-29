/*
 * Copyright 2023 OpsMx, Inc.
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

package com.opsmx.spinnaker.gate.service;

import com.opsmx.spinnaker.gate.model.AuthProviderModel;
import com.opsmx.spinnaker.gate.model.KeycloakProperties;
import com.opsmx.spinnaker.gate.util.KeycloakAuthUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("${feature.auth-provider.flag:false}")
public class LdapKeycloakAuthService implements KeycloakAuthService {

  private static final String LDAP = "ldap";
  private static final String VENDOR = "vendor";
  private static final String COMPONENT_ID = "componentId";

  @Autowired private KeycloakProperties ldapConfigProps;

  @Autowired private KeycloakAuthUtils keycloakAuthUtils;

  @Value("${keycloak.ldapName:ldap}")
  private String ldapName;

  /**
   * @param authProviderModel
   * @return
   */
  @Override
  public AuthProviderModel create(AuthProviderModel authProviderModel) {
    authProviderModel.getConfig().putAll(ldapConfigProps.getProps());
    MultivaluedHashMap<String, String> config =
        hashMapToMultivaluedHashMap(authProviderModel.getConfig());
    keycloakAuthUtils.addLdapComponent(config);
    keycloakAuthUtils.addLdapGroupMapper(authProviderModel);
    keycloakAuthUtils.addGroupMembershipProtocolMapper();

    return authProviderModel;
  }

  private MultivaluedHashMap<String, String> hashMapToMultivaluedHashMap(
      Map<String, String> config) {
    if (config.containsKey(VENDOR)) {
      config.put(VENDOR, KeycloakAuthUtils.getVendor(config.get(VENDOR)));
    }
    MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
    Optional.of(config).orElse(new HashMap<>()).forEach(map::add);
    return map;
  }

  /**
   * @param authProviderModel
   * @return
   */
  @Override
  public AuthProviderModel update(AuthProviderModel authProviderModel) {
    authProviderModel.getConfig().putAll(ldapConfigProps.getProps());
    authProviderModel.getConfig().remove(COMPONENT_ID);
    MultivaluedHashMap<String, String> config =
        hashMapToMultivaluedHashMap(
            filterUserSearchAndConnectionSettings(authProviderModel.getConfig()));
    log.debug("updating LDAP component : {}", config);
    keycloakAuthUtils.updateLdapComponent(config);
    keycloakAuthUtils.updateLdapGroupMapper(authProviderModel);
    return authProviderModel;
  }

  private Map<String, String> filterUserSearchAndConnectionSettings(Map<String, String> config) {
    return config.entrySet().stream()
        .filter(
            map ->
                !map.getKey().equalsIgnoreCase("groupsDn")
                    && !map.getKey().equalsIgnoreCase("membershipLdapAttribute")
                    && !map.getKey().equalsIgnoreCase("groupNameLdapAttribute")
                    && !map.getKey().equalsIgnoreCase("groupObjectClasses")
                    && !map.getKey().equalsIgnoreCase("membershipUserLdapAttribute")
                    && !map.getKey().equalsIgnoreCase("membershipAttributeType")
                    && !map.getKey().equalsIgnoreCase("userRolesRetrieveStrategy")
                    && !map.getKey().equalsIgnoreCase("memberofLdapAttribute")
                    && !map.getKey().equalsIgnoreCase("mode")
                    && !map.getKey().equalsIgnoreCase("groupsLdapFilter"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public String getAuthType() {
    return LDAP;
  }

  @Override
  public AuthProviderModel get() {
    ComponentRepresentation ldap = keycloakAuthUtils.getLdapComponent();

    if (ldap == null) {
      return keycloakAuthUtils.getDefaultModel(LdapKeycloakAuthService.LDAP);
    }
    Map<String, String> config = multivaluedHashMapToHashMap(ldap.getConfig());
    // We need this id to validate the authentication.
    config.put(COMPONENT_ID, ldap.getId());
    ComponentRepresentation ldapGroupMapper = keycloakAuthUtils.getLdapGroupMapper(ldap);
    if (ldapGroupMapper != null) {
      Map<String, String> groupConfig = multivaluedHashMapToHashMap(ldapGroupMapper.getConfig());
      config.putAll(keycloakAuthUtils.mapLdapGroupFields(groupConfig));
    }
    AuthProviderModel model = new AuthProviderModel();
    model.setName(ldap.getName());
    model.setConfig(config);
    return model;
  }

  private Map<String, String> multivaluedHashMapToHashMap(
      MultivaluedHashMap<String, String> config) {
    Map<String, String> map = new HashMap<>();
    Optional.ofNullable(config)
        .orElse(new MultivaluedHashMap<>())
        .forEach(
            (key, value1) -> {
              String value =
                  Optional.ofNullable(value1).orElse(new ArrayList<>()).stream()
                      .findFirst()
                      .orElse("");
              map.put(key, value);
            });
    if (map.containsKey(VENDOR)) {
      map.put(VENDOR, KeycloakAuthUtils.getVendorDisplayName(map.get(VENDOR)));
    }
    return map;
  }

  @Override
  public void delete() {
    keycloakAuthUtils.deleteLdapComponent();
  }

  @Override
  public AuthProviderModel toggle(String action) {
    MultivaluedHashMap<String, String> ldapConfig = keycloakAuthUtils.toggleLdap(action);
    AuthProviderModel authProviderModel = new AuthProviderModel();
    authProviderModel.setName(ldapName);
    authProviderModel.setConfig(multivaluedHashMapToHashMap(ldapConfig));
    log.debug("Successfully toggled LDAP with the action : {}", action);
    return authProviderModel;
  }
}
