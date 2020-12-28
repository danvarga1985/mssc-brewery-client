package com.danvarga.msscbreweryclient.web.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// Http Client config - Apache impl

@Component
public class BlockingRestTemplateCustomizer implements RestTemplateCustomizer {

    // Externalized properties (application.properties)
    private final Integer maxTotalConnections;
    private final Integer defaultMaxPerRoute;
    private final Integer connectionRequestTimeout;
    private final Integer socketTimeout;

    // Externalized properties accessed with SpEL.
    public BlockingRestTemplateCustomizer(@Value("${mssc.maxtotalconnections}") Integer maxTotalConnections,
                                          @Value("${mssc.defaultmaxperroute}") Integer defaultMaxPerRoute,
                                          @Value("${mssc.connectionrequesttimeout}") Integer connectionRequestTimeout,
                                          @Value("${mssc.sockettimeout}") Integer socketTimeout) {
        
        this.maxTotalConnections = maxTotalConnections;
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.socketTimeout = socketTimeout;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // If the request takes more than 3s -> error.
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.setRequestFactory(this.clientHttpRequestFactory());
    }
}
