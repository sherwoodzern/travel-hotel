package com.oracle.camunda.interceptor;

import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

public class HostNameProvider implements ClientRequestInterceptor{

    protected final String key;
    protected final String value;

    public HostNameProvider(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public void intercept(ClientRequestContext requestContext) {
        System.setProperty("sun.net.http.allowsRestrictedHeaders", "true");
        requestContext.addHeader(key, value);
    }
}