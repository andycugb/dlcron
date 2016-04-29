package com.andycugb.cron.util;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jbcheng on 2016-04-28.
 */
public class HttpUtil {
    // singleton instance
    private static HttpClient httpClient = null;

    // retry strategy ,retryCount <5
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
        public boolean retryRequest(IOException exception, int retryCount, HttpContext httpContext) {
            if (retryCount >= 5) {
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            HttpRequest request =
                    (HttpRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
            if (!idempotent) {
                return true;
            }
            return false;
        }
    };

    /**
     * get thread-safe HttpClient instance.
     * 
     * @return instacne
     * @throws Exception throws exception when error occurs
     */
    private static HttpClient getHttpClient() throws Exception {
        if (httpClient == null) {
            synchronized (HttpUtil.class) {
                if (httpClient == null) {
                    HttpParams params = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(params, 5000);
                    HttpConnectionParams.setSoTimeout(params, 5000);
                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    X509TrustManager trustManager = new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] x509Certificates,
                                String var1) throws CertificateException {

                        }

                        public void checkServerTrusted(X509Certificate[] x509Certificates,
                                String var1) throws CertificateException {

                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    };
                    sslContext.init(null, new TrustManager[] {trustManager}, null);
                    SSLSocketFactory socketFactory =
                            new SSLSocketFactory(sslContext,
                                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    Scheme https = new Scheme("https", 443, socketFactory);
                    schemeRegistry.register(http);
                    schemeRegistry.register(https);
                    PoolingClientConnectionManager pool =
                            new PoolingClientConnectionManager(schemeRegistry);
                    pool.setDefaultMaxPerRoute(2000);
                    pool.setMaxTotal(2000);
                    httpClient = new DefaultHttpClient(pool, params);
                }
            }
        }
        return httpClient;
    }

    /**
     * exec given url.
     * 
     * @param url url to be invoked
     * @return exec status map
     */
    public static Map<String, Object> execRequest(String url) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        HttpGet getMethod = new HttpGet(url);
        try {
            DefaultHttpClient client = (DefaultHttpClient) getHttpClient();
            client.setHttpRequestRetryHandler(requestRetryHandler);
            HttpResponse response = client.execute(getMethod);
            retMap.put("status", response.getStatusLine().getStatusCode());
            retMap.put("entity", EntityUtils.toString(response.getEntity(), "utf-8"));
        } catch (Exception e) {
            Constant.LOG_CRON.error("error when request:" + url + ",by:" + e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return retMap;
    }
}
