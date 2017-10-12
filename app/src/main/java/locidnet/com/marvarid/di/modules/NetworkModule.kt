package locidnet.com.marvarid.di.modules

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import dagger.Module
import dagger.Provides
import locidnet.com.marvarid.resources.utils.log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.net.CacheResponse
import java.util.concurrent.TimeUnit


@Module(includes = arrayOf(ContextModule::class))
class NetworkModule {

    @Provides
    fun getFile(ctx: Context):File {
        val fName = "posts"
        val f = File(ctx.externalCacheDir,fName)

        if (!f.exists()) f.mkdirs()

        return f
    }



    @Provides
    fun getCacheFIle(cacheFile:File):Cache = Cache(cacheFile,10*1024)

    @Provides
    fun logging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String?) {

            log.d("MARVARID OKHTTP: ${message}")
            }

        })
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }


    @Provides
    fun okhttpClient(cache: Cache,context:Context):OkHttpClient{

        val client:OkHttpClient =  OkHttpClient.Builder()

                .connectTimeout(7, TimeUnit.MINUTES)
                .readTimeout   (7, TimeUnit.MINUTES)
                .writeTimeout  (7, TimeUnit.MINUTES)
                .addNetworkInterceptor({
                    chain ->
                    val request = chain.request()
                            .newBuilder()

                            .removeHeader("Pragma")
                            .build()
                    val response = chain.proceed(request)
                    log.d("Marvarid Get online request header ${request.headers()}")

                    val cachControl = response.header("Cache-Control")
                    log.d("Marvarid Get online response header ${cachControl}")




                        response.newBuilder()
                                .removeHeader("Cache-Control")
                                .removeHeader("Pragma")
                                .addHeader("Cache-Control", "public, max-age=${60 * 60 * 24 * 28}")
                                .build()



                })
                .addInterceptor({
                    chain ->
                    val infoInternet:NetworkInfo?
                            = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

                    var request = chain.request().newBuilder()
                            .removeHeader("Pragma")
                            .build()

                    if (infoInternet == null || !infoInternet.isConnected){
                        request = request.newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .addHeader("Cache-Control","public, only-if-cached, max-stale=${60 * 60 * 24 * 28}")
                                .build()
                     }
                    log.d("Marvarid Get offline request header ${request.headers()}")
                    log.d("Marvarid Get offline request header cached ${request.cacheControl()!!.onlyIfCached()}")
                    chain.proceed(request)



                })
                .cache(cache)

                .build()

        return client
    }
}