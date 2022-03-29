//package com.youngsbook.common.network
//
//import android.R
//import com.youngsbook.ui.login.LoginActivity
//import com.youngsbook.ui.main.MainActivity
//import okhttp3.OkHttpClient
//import java.io.IOException
//import java.io.InputStream
//import java.security.*
//import java.security.cert.Certificate
//import java.security.cert.CertificateException
//import java.security.cert.CertificateFactory
//import java.security.cert.X509Certificate
//import javax.net.ssl.SSLContext
//import javax.net.ssl.TrustManagerFactory
//import javax.net.ssl.X509TrustManager
//
//
//public class SelfSigningHelper {
//    private var sslContext: SSLContext? = null
//    private var tmf: TrustManagerFactory? = null
//    private fun SelfSigningHelper() {
//        setUp()
//    }
//
//    // 싱글턴으로 생성
//    private object SelfSigningClientBuilderHolder {
//        val INSTANCE = SelfSigningHelper()
//    }
//
//    fun getInstance(): SelfSigningHelper {
//        return SelfSigningClientBuilderHolder.INSTANCE
//    }
//
//    fun setUp() {
//        val cf: CertificateFactory
//        val ca: Certificate
//        val caInput: InputStream
//        try {
//            cf = CertificateFactory.getInstance("X.509")
//            // Application을 상속받는 클래스에
//// Context 호출하는 메서드 ( getAppContext() )를
//// 생성해 놓았음
//            caInput = MainActivity().applicationContext.resources.openRawResource(R.raw.youngsBook_crt)
//            ca = cf.generateCertificate(caInput)
//            System.out.println("ca=" + (ca as X509Certificate).getSubjectDN())
//            // Create a KeyStore containing our trusted CAs
//            val keyStoreType: String = KeyStore.getDefaultType()
//            val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
//            keyStore.load(null, null)
//            keyStore.setCertificateEntry("ca", ca)
//            // Create a TrustManager that trusts the CAs in our KeyStore
//            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
//            tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
//            tmf.init(keyStore)
//            // Create an SSLContext that uses our TrustManager
//            sslContext = SSLContext.getInstance("TLS")
//            sslContext.init(null, tmf.getTrustManagers(), SecureRandom())
//            caInput.close()
//        } catch (e: KeyStoreException) {
//            e.printStackTrace()
//        } catch (e: CertificateException) {
//            e.printStackTrace()
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } catch (e: KeyManagementException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun setSSLOkHttp(builder: OkHttpClient.Builder): OkHttpClient.Builder {
//        builder.sslSocketFactory(
//            getInstance().sslContext?.getSocketFactory(),
//            getInstance().tmf?.getTrustManagers()?.get(0) as X509TrustManager
//        )
//        return builder
//    }
//}