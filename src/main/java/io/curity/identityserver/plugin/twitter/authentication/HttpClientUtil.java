/*
 *  Copyright 2020 Curity AB
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

import com.github.scribejava.core.httpclient.HttpClient;
import io.curity.identityserver.plugin.twitter.config.TwitterAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.Nullable;

import java.util.Optional;

final class HttpClientUtil
{
    private HttpClientUtil() { }

    @Nullable
    static HttpClient getHttpClient(TwitterAuthenticatorPluginConfig config)
    {
        Optional<se.curity.identityserver.sdk.service.HttpClient> maybeHttpClient = config.getHttpClient();

        return maybeHttpClient
                .map(httpClient -> ScribeJavaHttpClientAdapter.from(config.getWebServiceClientFactory(), httpClient))
                .orElseGet(() -> ScribeJavaHttpClientAdapter.from(config.getWebServiceClientFactory()));
    }
}
