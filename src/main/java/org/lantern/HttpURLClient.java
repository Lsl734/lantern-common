package org.lantern;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Base class for users of HttpURLConnetion that may use a proxy.
 */
public abstract class HttpURLClient {
    private final Proxy proxy;
    private volatile SSLContextSource sslContextSource;

    protected HttpURLClient() {
        this(null);
    }

    protected HttpURLClient(InetSocketAddress proxyAddress) {
        if (proxyAddress != null) {
            this.proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
        } else {
            this.proxy = null;
        }
    }

    public void setSslContextSource(SSLContextSource sslContextSource) {
        this.sslContextSource = sslContextSource;
    }

    protected HttpURLConnection newConn(String url) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
        conn.setConnectTimeout(50000);
        conn.setReadTimeout(120000);
        boolean useCustomSslContext = sslContextSource != null;
        boolean isSSL = conn instanceof HttpsURLConnection;
        if (isSSL && useCustomSslContext) {
            SSLContext sslContext = sslContextSource.getContext(url);
            HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
            SSLSocketFactory sf =
                    new NoSSLv2SocketFactory(sslContext.getSocketFactory());
            httpsConn.setSSLSocketFactory(sf);
        }
        return conn;
    }

    public interface SSLContextSource {
        SSLContext getContext(String url);
    }
}
