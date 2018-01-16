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
import io.curity.identityserver.plugin.twitter.config.TwitterAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.RedirectStatusCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN_SECRET;
import static io.curity.identityserver.plugin.twitter.descriptor.TwitterAuthenticatorPluginDescriptor.CALLBACK;

public class TwitterAuthenticatorRequestHandler implements AuthenticatorRequestHandler<Request>
{
    private static final Logger _logger = LoggerFactory.getLogger(TwitterAuthenticatorRequestHandler.class);

    private final TwitterAuthenticatorPluginConfig _config;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final ExceptionFactory _exceptionFactory;

    public TwitterAuthenticatorRequestHandler(TwitterAuthenticatorPluginConfig config)
    {
        _config = config;
        _exceptionFactory = config.getExceptionFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
    }

    @Override
    public Optional<AuthenticationResult> get(Request request, Response response)
    {
        _logger.debug("GET request received for authentication");

        OAuth10aService service = new ServiceBuilder(_config.getClientId())
                .apiSecret(_config.getClientSecret())
                .callback(createRedirectUri())
                .build(TwitterApi.instance());
        String authorizationEndpoint = "";
        try
        {
            final OAuth1RequestToken requestToken = service.getRequestToken();
            authorizationEndpoint = service.getAuthorizationUrl(requestToken);
            _config.getSessionManager().put(Attribute.of(OAUTH_TOKEN, requestToken.getToken()));
            _config.getSessionManager().put(Attribute.of(OAUTH_TOKEN_SECRET, requestToken.getTokenSecret()));

        } catch (Exception e)
        {
            e.printStackTrace();
        }


        _logger.debug("Redirecting to {}", authorizationEndpoint);

        throw _exceptionFactory.redirectException(authorizationEndpoint,
                RedirectStatusCode.MOVED_TEMPORARILY);
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
    public Optional<AuthenticationResult> post(Request request, Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

    @Override
    public Request preProcess(Request request, Response response)
    {
        return request;
    }
}
