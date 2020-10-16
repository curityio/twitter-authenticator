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
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.http.HttpRequest;
import se.curity.identityserver.sdk.http.HttpResponse;
import se.curity.identityserver.sdk.service.WebServiceClient;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public final class ScribeJavaHttpClientAdapter implements HttpClient
{
    private final WebServiceClient _webServiceClient;

    private static final Map<Integer, String> _statusMessageByStatusCode;

    static
    {
        Map<Integer, String> statusMessageByStatusCode = new HashMap<>(50);

        statusMessageByStatusCode.put(200, "OK");
        statusMessageByStatusCode.put(201, "Created");
        statusMessageByStatusCode.put(202, "Accepted");
        statusMessageByStatusCode.put(203, "Non-Authoritative Information");
        statusMessageByStatusCode.put(204, "No Content");
        statusMessageByStatusCode.put(205, "Reset Content");
        statusMessageByStatusCode.put(206, "Partial Content");
        statusMessageByStatusCode.put(300, "Multiple Choices");
        statusMessageByStatusCode.put(301, "Moved Permanently");
        statusMessageByStatusCode.put(302, "Found");
        statusMessageByStatusCode.put(303, "See Other");
        statusMessageByStatusCode.put(304, "Not Modified");
        statusMessageByStatusCode.put(305, "Use Proxy");
        statusMessageByStatusCode.put(306, "Switch Proxy");
        statusMessageByStatusCode.put(307, "Temporary Redirect");
        statusMessageByStatusCode.put(308, "Permanent Redirect");
        statusMessageByStatusCode.put(400, "Bad Request");
        statusMessageByStatusCode.put(401, "Unauthorized");
        statusMessageByStatusCode.put(402, "Payment Required");
        statusMessageByStatusCode.put(403, "Forbidden");
        statusMessageByStatusCode.put(404, "Not Found");
        statusMessageByStatusCode.put(405, "Method Not Allowed");
        statusMessageByStatusCode.put(406, "Not Acceptable");
        statusMessageByStatusCode.put(407, "Proxy Authentication Required");
        statusMessageByStatusCode.put(408, "Request Timeout");
        statusMessageByStatusCode.put(409, "Conflict");
        statusMessageByStatusCode.put(410, "Gone");
        statusMessageByStatusCode.put(411, "Length Required");
        statusMessageByStatusCode.put(412, "Precondition Failed");
        statusMessageByStatusCode.put(413, "Payload Too Large");
        statusMessageByStatusCode.put(414, "URI Too Long");
        statusMessageByStatusCode.put(415, "Unsupported Media Type");
        statusMessageByStatusCode.put(416, "Range Not Satisfiable");
        statusMessageByStatusCode.put(417, "Expectation Failed");
        statusMessageByStatusCode.put(420, "Enhance Your Calm");
        statusMessageByStatusCode.put(421, "Misdirected Request");
        statusMessageByStatusCode.put(425, "Too Early");
        statusMessageByStatusCode.put(426, "Upgrade Required");
        statusMessageByStatusCode.put(428, "Precondition Required");
        statusMessageByStatusCode.put(429, "Too Many Requests");
        statusMessageByStatusCode.put(431, "Request Header Fields Too Large");
        statusMessageByStatusCode.put(451, "Unavailable For Legal Reasons");
        statusMessageByStatusCode.put(500, "Internal Server Error");
        statusMessageByStatusCode.put(501, "Not Implemented");
        statusMessageByStatusCode.put(502, "Bad Gateway");
        statusMessageByStatusCode.put(503, "Service Unavailable");
        statusMessageByStatusCode.put(504, "Gateway Timeout");
        statusMessageByStatusCode.put(505, "HTTP Version Not Supported");
        statusMessageByStatusCode.put(506, "Variant Also Negotiates");
        statusMessageByStatusCode.put(510, "Not Extended");
        statusMessageByStatusCode.put(511, "Network Authentication Required");

        _statusMessageByStatusCode = Collections.unmodifiableMap(statusMessageByStatusCode);
    }

    private ScribeJavaHttpClientAdapter(WebServiceClient webServiceClient)
    {
        _webServiceClient = webServiceClient;
    }

    public static HttpClient from(
            WebServiceClientFactory webServiceClientFactory,
            se.curity.identityserver.sdk.service.HttpClient httpClient)
    {
        WebServiceClient webServiceClient = webServiceClientFactory.create(httpClient).withHost(TwitterApi.BASE_URL);

        return new ScribeJavaHttpClientAdapter(webServiceClient);
    }

    public static HttpClient from(WebServiceClientFactory webServiceClientFactory)
    {
        WebServiceClient webServiceClient = webServiceClientFactory.create(URI.create(TwitterApi.BASE_URL));

        return new ScribeJavaHttpClientAdapter(webServiceClient);
    }

    @Override
    public Response execute(@Nullable String userAgent,
                            Map<String, String> headers,
                            Verb httpVerb,
                            String completeUrl,
                            byte[] bodyContents)
    {
        int startPos = completeUrl.indexOf("/", "https://".length() + 1);
        String path = completeUrl.substring(startPos);

        HttpRequest.Builder builder = _webServiceClient.withPath(path).request();

        headers.forEach(builder::header);

        builder.accept("application/json");

        if (userAgent != null)
        {
            builder.header("UserAgent", userAgent);
        }

        builder.body(HttpRequest.fromByteArray(bodyContents));

        HttpRequest request = builder.method(httpVerb.name());

        HttpResponse response = request.response();

        Map<String, String> adaptedHeaders = response.headers()
                .map()
                .entrySet()
                .stream()
                .map(it -> new AbstractMap.SimpleEntry<>(it.getKey(), String.join(",", it.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String body = response.body(HttpResponse.asString());

        String message = _statusMessageByStatusCode.get(response.statusCode());

        return new Response(response.statusCode(), message, adaptedHeaders, body);
    }

    @Override
    public Response execute(String userAgent,
                            Map<String, String> headers,
                            Verb httpVerb,
                            String completeUrl,
                            String bodyContents)
    {
        return execute(userAgent, headers, httpVerb, completeUrl, bodyContents.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Response execute(String userAgent,
                            Map<String, String> headers,
                            Verb httpVerb,
                            String completeUrl,
                            File bodyContents)
    {
        throw new UnsupportedOperationException("file upload is not support");
    }

    @Override
    public Response execute(String userAgent,
                            Map<String, String> headers,
                            Verb httpVerb,
                            String completeUrl,
                            MultipartPayload bodyContents)
    {
        throw new UnsupportedOperationException("Multi-part body content is not support");
    }

    @Override
    public void close()
    {
        // Void
    }

    @Override
    public <T> Future<T> executeAsync(String userAgent,
                                      Map<String, String> headers,
                                      Verb httpVerb,
                                      String completeUrl,
                                      byte[] bodyContents,
                                      OAuthAsyncRequestCallback<T> callback,
                                      OAuthRequest.ResponseConverter<T> converter)
    {
        throw new UnsupportedOperationException("async execution is not support");
    }

    @Override
    public <T> Future<T> executeAsync(String userAgent,
                                      Map<String, String> headers,
                                      Verb httpVerb,
                                      String completeUrl,
                                      MultipartPayload bodyContents,
                                      OAuthAsyncRequestCallback<T> callback,
                                      OAuthRequest.ResponseConverter<T> converter)
    {
        throw new UnsupportedOperationException("async execution is not support");
    }

    @Override
    public <T> Future<T> executeAsync(String userAgent,
                                      Map<String, String> headers,
                                      Verb httpVerb,
                                      String completeUrl,
                                      String bodyContents,
                                      OAuthAsyncRequestCallback<T> callback,
                                      OAuthRequest.ResponseConverter<T> converter)
    {
        throw new UnsupportedOperationException("async execution is not support");
    }

    @Override
    public <T> Future<T> executeAsync(String userAgent,
                                      Map<String, String> headers,
                                      Verb httpVerb,
                                      String completeUrl,
                                      File bodyContents,
                                      OAuthAsyncRequestCallback<T> callback,
                                      OAuthRequest.ResponseConverter<T> converter)
    {
        throw new UnsupportedOperationException("async execution is not support");
    }
}
