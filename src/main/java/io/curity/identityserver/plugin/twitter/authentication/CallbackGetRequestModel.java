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

import se.curity.identityserver.sdk.web.Request;

import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_TOKEN;
import static io.curity.identityserver.plugin.twitter.authentication.Constants.OAUTH_VERIFIER;

public class CallbackGetRequestModel {
    private String _oauthToken;
    private String _oauthVerifier;
    private Request _request;

    public CallbackGetRequestModel(Request request) {
        _oauthToken = request.getParameterValueOrError(OAUTH_TOKEN);
        _oauthVerifier = request.getParameterValueOrError(OAUTH_VERIFIER);
        _request = request;
    }

    public String getOAuthToken() {
        return _oauthToken;
    }

    public String getOAuthVerifier() {
        return _oauthVerifier;
    }

    public Request getRequest() {
        return _request;
    }
}
