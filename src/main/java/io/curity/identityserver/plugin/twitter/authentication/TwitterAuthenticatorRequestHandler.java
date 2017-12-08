/*
 *  Copyright 2017 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.curity.identityserver.plugin.twitter.authentication;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import io.curity.identityserver.plugin.authentication.CodeFlowOAuthClient;
import io.curity.identityserver.plugin.authentication.OAuthClient;
import io.curity.identityserver.plugin.twitter.config.TwitterAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN_SECRET;

public class TwitterAuthenticatorRequestHandler implements AuthenticatorRequestHandler<RequestModel> {
    private static final Logger _logger = LoggerFactory.getLogger(TwitterAuthenticatorRequestHandler.class);

    private final TwitterAuthenticatorPluginConfig _config;
    private final OAuthClient _oauthClient;
    private final OAuth10aService service;
    private final ExceptionFactory _exceptionFactory;
    private final SessionManager _sessionManager;

    public TwitterAuthenticatorRequestHandler(TwitterAuthenticatorPluginConfig config,
                                              ExceptionFactory exceptionFactory,
                                              Json json,
                                              AuthenticatorInformationProvider provider) {
        _config = config;
        _oauthClient = new CodeFlowOAuthClient(exceptionFactory, provider, json, config.getSessionManager());
        this._exceptionFactory = exceptionFactory;
        this._sessionManager = config.getSessionManager();
        service = new ServiceBuilder(_config.getClientId())
                .apiSecret(_config.getClientSecret())
                .callback(_oauthClient.getCallbackUrl())
                .build(TwitterApi.instance());
    }

    @Override
    public Optional<AuthenticationResult> get(RequestModel requestModel, Response response) {
        _logger.info("GET request received for authentication authentication");

        String url = "";
        _oauthClient.setServiceProviderId(requestModel.getRequest());
        try {
            final OAuth1RequestToken requestToken = service.getRequestToken();
            url = service.getAuthorizationUrl(requestToken);
            _sessionManager.put(Attribute.of(OAUTH_TOKEN, requestToken.getToken()));
            _sessionManager.put(Attribute.of(OAUTH_TOKEN_SECRET, requestToken.getTokenSecret()));
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        throw _exceptionFactory.redirectException(url);
    }

    @Override
    public Optional<AuthenticationResult> post(RequestModel requestModel, Response response) {
        return Optional.empty();
    }

    @Override
    public RequestModel preProcess(Request request, Response response) {
        return new RequestModel(request);
    }
}
