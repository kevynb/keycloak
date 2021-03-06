/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PersistentAuthenticatedClientSessionAdapter implements AuthenticatedClientSessionModel {

    private final PersistentClientSessionModel model;
    private final RealmModel realm;
    private final ClientModel client;
    private UserSessionModel userSession;

    private PersistentClientSessionData data;

    public PersistentAuthenticatedClientSessionAdapter(AuthenticatedClientSessionModel clientSession) {
        data = new PersistentClientSessionData();
        data.setAction(clientSession.getAction());
        data.setAuthMethod(clientSession.getProtocol());
        data.setNotes(clientSession.getNotes());
        data.setProtocolMappers(clientSession.getProtocolMappers());
        data.setRedirectUri(clientSession.getRedirectUri());
        data.setRoles(clientSession.getRoles());

        model = new PersistentClientSessionModel();
        model.setClientId(clientSession.getClient().getId());
        model.setUserId(clientSession.getUserSession().getUser().getId());
        model.setUserSessionId(clientSession.getUserSession().getId());
        model.setTimestamp(clientSession.getTimestamp());

        realm = clientSession.getRealm();
        client = clientSession.getClient();
        userSession = clientSession.getUserSession();
    }

    public PersistentAuthenticatedClientSessionAdapter(PersistentClientSessionModel model, RealmModel realm, ClientModel client, UserSessionModel userSession) {
        this.model = model;
        this.realm = realm;
        this.client = client;
        this.userSession = userSession;
    }

    // Lazily init data
    private PersistentClientSessionData getData() {
        if (data == null) {
            try {
                data = JsonSerialization.readValue(model.getData(), PersistentClientSessionData.class);
            } catch (IOException ioe) {
                throw new ModelException(ioe);
            }
        }

        return data;
    }

    // Write updated model with latest serialized data
    public PersistentClientSessionModel getUpdatedModel() {
        try {
            String updatedData = JsonSerialization.writeValueAsString(getData());
            this.model.setData(updatedData);
        } catch (IOException ioe) {
            throw new ModelException(ioe);
        }

        return this.model;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public ClientModel getClient() {
        return client;
    }

    @Override
    public UserSessionModel getUserSession() {
        return userSession;
    }

    @Override
    public void setUserSession(UserSessionModel userSession) {
        this.userSession = userSession;
    }

    @Override
    public String getRedirectUri() {
        return getData().getRedirectUri();
    }

    @Override
    public void setRedirectUri(String uri) {
        getData().setRedirectUri(uri);
    }

    @Override
    public int getTimestamp() {
        return model.getTimestamp();
    }

    @Override
    public void setTimestamp(int timestamp) {
        model.setTimestamp(timestamp);
    }

    @Override
    public String getAction() {
        return getData().getAction();
    }

    @Override
    public void setAction(String action) {
        getData().setAction(action);
    }

    @Override
    public Set<String> getRoles() {
        return getData().getRoles();
    }

    @Override
    public void setRoles(Set<String> roles) {
        getData().setRoles(roles);
    }

    @Override
    public Set<String> getProtocolMappers() {
        return getData().getProtocolMappers();
    }

    @Override
    public void setProtocolMappers(Set<String> protocolMappers) {
        getData().setProtocolMappers(protocolMappers);
    }

    @Override
    public String getProtocol() {
        return getData().getAuthMethod();
    }

    @Override
    public void setProtocol(String method) {
        getData().setAuthMethod(method);
    }

    @Override
    public String getNote(String name) {
        PersistentClientSessionData entity = getData();
        return entity.getNotes()==null ? null : entity.getNotes().get(name);
    }

    @Override
    public void setNote(String name, String value) {
        PersistentClientSessionData entity = getData();
        if (entity.getNotes() == null) {
            entity.setNotes(new HashMap<>());
        }
        entity.getNotes().put(name, value);
    }

    @Override
    public void removeNote(String name) {
        PersistentClientSessionData entity = getData();
        if (entity.getNotes() != null) {
            entity.getNotes().remove(name);
        }
    }

    @Override
    public Map<String, String> getNotes() {
        PersistentClientSessionData entity = getData();
        if (entity.getNotes() == null || entity.getNotes().isEmpty()) return Collections.emptyMap();
        return entity.getNotes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AuthenticatedClientSessionModel)) return false;

        AuthenticatedClientSessionModel that = (AuthenticatedClientSessionModel) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    protected static class PersistentClientSessionData {

        @JsonProperty("authMethod")
        private String authMethod;

        @JsonProperty("redirectUri")
        private String redirectUri;

        @JsonProperty("protocolMappers")
        private Set<String> protocolMappers;

        @JsonProperty("roles")
        private Set<String> roles;

        @JsonProperty("notes")
        private Map<String, String> notes;

        @JsonProperty("action")
        private String action;

        public String getAuthMethod() {
            return authMethod;
        }

        public void setAuthMethod(String authMethod) {
            this.authMethod = authMethod;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public Set<String> getProtocolMappers() {
            return protocolMappers;
        }

        public void setProtocolMappers(Set<String> protocolMappers) {
            this.protocolMappers = protocolMappers;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }

        public Map<String, String> getNotes() {
            return notes;
        }

        public void setNotes(Map<String, String> notes) {
            this.notes = notes;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

    }
}
