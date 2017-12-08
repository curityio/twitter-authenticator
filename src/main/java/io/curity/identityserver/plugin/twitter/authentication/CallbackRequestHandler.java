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
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import io.curity.identityserver.plugin.authentication.CodeFlowOAuthClient;
import io.curity.identityserver.plugin.authentication.OAuthClient;
import io.curity.identityserver.plugin.twitter.config.TwitterAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.util.Optional;

import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN_SECRET;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.SCREEN_NAME;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.USER_ID;

public class CallbackRequestHandler
        implements AuthenticatorRequestHandler<CallbackGetRequestModel> {
    private final ExceptionFactory _exceptionFactory;
    private final OAuthClient _oauthClient;
    private final TwitterAuthenticatorPluginConfig _config;
    private final OAuth10aService _service;
    private final SessionManager _sessionManager;
    private final OAuth1RequestToken _requestToken;

    public CallbackRequestHandler(ExceptionFactory exceptionFactory,
                                  AuthenticatorInformationProvider provider,
                                  Json json,
                                  TwitterAuthenticatorPluginConfig config) {
        _exceptionFactory = exceptionFactory;
        _oauthClient = new CodeFlowOAuthClient(exceptionFactory, provider, json, config.getSessionManager());
        _config = config;
        _sessionManager = _config.getSessionManager();
        _service = new ServiceBuilder(_config.getClientId())
                .apiSecret(_config.getClientSecret())
                .callback(_oauthClient.getCallbackUrl())
                .build(TwitterApi.instance());
        _requestToken = new OAuth1RequestToken(_sessionManager.get(OAUTH_TOKEN).getValue().toString(), _sessionManager.get(OAUTH_TOKEN_SECRET).getValue().toString());
    }

    @Override
    public CallbackGetRequestModel preProcess(Request request, Response response) {
        if (request.isGetRequest()) {
            return new CallbackGetRequestModel(request);
        } else {
            throw _exceptionFactory.methodNotAllowed();
        }
    }

    @Override
    public Optional<AuthenticationResult> get(CallbackGetRequestModel requestModel,
                                              Response response) {
        _oauthClient.redirectToAuthenticationOnError(requestModel.getRequest(), _config.id());
        try {
            OAuth1AccessToken accessToken = _service.getAccessToken(_requestToken, requestModel.getOAuthVerifier());

            Attributes subjectAttributes = Attributes.of(Attribute.of(USER_ID, accessToken.getParameter(USER_ID)), Attribute.of(SCREEN_NAME, accessToken.getParameter(SCREEN_NAME)));
            Attributes contextAttributes = Attributes.of(Attribute.of(OAUTH_TOKEN, accessToken.getToken()), Attribute.of(OAUTH_TOKEN_SECRET, accessToken.getTokenSecret()));

            AuthenticationAttributes attributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(accessToken.getParameter(USER_ID), subjectAttributes),
                    ContextAttributes.of(contextAttributes));
            AuthenticationResult authenticationResult = new AuthenticationResult(attributes);
            return Optional.ofNullable(authenticationResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            _oauthClient.redirectToAuthenticationOnError(ex.getMessage(), "", _config.id());
        }
        return Optional.empty();
    }

    @Override
    public Optional<AuthenticationResult> post(CallbackGetRequestModel requestModel,
                                               Response response) {
        throw _exceptionFactory.methodNotAllowed();
    }

}
