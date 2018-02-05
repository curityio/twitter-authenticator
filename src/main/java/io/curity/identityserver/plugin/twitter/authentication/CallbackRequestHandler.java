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
import io.curity.identityserver.plugin.twitter.config.TwitterAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN_SECRET;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.SCREEN_NAME;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.USER_ID;
import static io.curity.identityserver.plugin.twitter.descriptor.TwitterAuthenticatorPluginDescriptor.CALLBACK;

public class CallbackRequestHandler
        implements AuthenticatorRequestHandler<CallbackGetRequestModel>
{
    private static final Logger _logger = LoggerFactory.getLogger(CallbackRequestHandler.class);
    
    private final ExceptionFactory _exceptionFactory;
    private final OAuth10aService _service;
    private final SessionManager _sessionManager;
    private final OAuth1RequestToken _requestToken;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;


    public CallbackRequestHandler(TwitterAuthenticatorPluginConfig _config)
    {
        _exceptionFactory = _config.getExceptionFactory();
        _sessionManager = _config.getSessionManager();
        _authenticatorInformationProvider = _config.getAuthenticatorInformationProvider();
        _service = new ServiceBuilder(_config.getClientId())
                .apiSecret(_config.getClientSecret())
                .callback(createRedirectUri())
                .build(TwitterApi.instance());
        _requestToken = new OAuth1RequestToken(_sessionManager.get(OAUTH_TOKEN).getValue().toString(), _sessionManager.get(OAUTH_TOKEN_SECRET).getValue().toString());
    }

    @Override
    public CallbackGetRequestModel preProcess(Request request, Response response)
    {
        if (request.isGetRequest())
        {
            return new CallbackGetRequestModel(request);
        } else
        {
            throw _exceptionFactory.methodNotAllowed();
        }
    }

    @Override
    public Optional<AuthenticationResult> get(CallbackGetRequestModel requestModel,
                                              Response response)
    {
        handleError(requestModel);
        try
        {
            OAuth1AccessToken accessToken = _service.getAccessToken(_requestToken, requestModel.getOAuthVerifier());

            Attributes subjectAttributes = Attributes.of(Attribute.of(USER_ID, accessToken.getParameter(USER_ID)), Attribute.of(SCREEN_NAME, accessToken.getParameter(SCREEN_NAME)));
            Attributes contextAttributes = Attributes.of(Attribute.of(OAUTH_TOKEN, accessToken.getToken()), Attribute.of(OAUTH_TOKEN_SECRET, accessToken.getTokenSecret()));

            AuthenticationAttributes attributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(accessToken.getParameter(USER_ID), subjectAttributes),
                    ContextAttributes.of(contextAttributes));
            AuthenticationResult authenticationResult = new AuthenticationResult(attributes);
            return Optional.ofNullable(authenticationResult);
        } catch (Exception ex)
        {
            _logger.info("Login failed with twitter, reason :  {}", ex.getMessage());
            throw _exceptionFactory.externalServiceException("Login failed with twitter, reason : " + ex.getMessage());

        }
    }

    private void handleError(CallbackGetRequestModel requestModel)
    {
        if (!Objects.isNull(requestModel.getError()))
        {
            if ("access_denied".equals(requestModel.getError()))
            {
                _logger.debug("Got an error from LinkedIn: {} - {}", requestModel.getError(), requestModel.getErrorDescription());

                throw _exceptionFactory.redirectException(
                        _authenticatorInformationProvider.getAuthenticationBaseUri().toASCIIString());
            }

            _logger.warn("Got an error from LinkedIn: {} - {}", requestModel.getError(), requestModel.getErrorDescription());

            throw _exceptionFactory.externalServiceException("Login with LinkedIn failed");
        }
    }


    private String createRedirectUri()
    {
        try
        {
            URI authUri = _authenticatorInformationProvider.getFullyQualifiedAuthenticationUri();

            return new URL(authUri.toURL(), authUri.getPath() + "/" + CALLBACK).toString();
        } catch (MalformedURLException e)
        {
            throw _exceptionFactory.internalServerException(ErrorCode.INVALID_REDIRECT_URI,
                    "Could not create redirect URI");
        }
    }

    @Override
    public Optional<AuthenticationResult> post(CallbackGetRequestModel requestModel,
                                               Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

}
