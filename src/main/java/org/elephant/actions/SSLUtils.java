package org.elephant.actions;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtils
{
    static SSLContext sslContext;

    public static SSLContext getSSLContextWithoutCertificateValidation() throws NoSuchAlgorithmException, KeyManagementException
    {
        if ( sslContext == null )
        {
            TrustManager[] trustAllCertificates = new TrustManager[] {
                    new X509TrustManager()
                    {
                        public X509Certificate[] getAcceptedIssuers()
                        {
                            return null;
                        }

                        public void checkClientTrusted( X509Certificate[] certs, String authType )
                        {}

                        public void checkServerTrusted( X509Certificate[] certs, String authType )
                        {}
                    }
            };
            sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, trustAllCertificates, new java.security.SecureRandom() );
        }
        return sslContext;
    }
}
