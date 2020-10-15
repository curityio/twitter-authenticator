package io.curity.identityserver.plugin.twitter.authentication;

import com.github.scribejava.core.builder.api.DefaultApi10a;

// Based on https://raw.githubusercontent.com/scribejava/scribejava/master/scribejava-apis/src/main/java/com/github/scribejava/apis/TwitterApi.java
// Licensed under MIT
final class TwitterApi extends DefaultApi10a
{
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String REQUEST_TOKEN_RESOURCE = "api.twitter.com/oauth/request_token";
    private static final String ACCESS_TOKEN_RESOURCE = "api.twitter.com/oauth/access_token";

    public static TwitterApi instance()
    {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint()
    {
        return "https://" + ACCESS_TOKEN_RESOURCE;
    }

    @Override
    public String getRequestTokenEndpoint()
    {
        return "https://" + REQUEST_TOKEN_RESOURCE;
    }

    @Override
    public String getAuthorizationBaseUrl()
    {
        return AUTHORIZE_URL;
    }

    private static class InstanceHolder
    {
        private static final TwitterApi INSTANCE = new TwitterApi();
    }
}
