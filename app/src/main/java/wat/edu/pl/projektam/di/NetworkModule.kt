package wat.edu.pl.projektam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import wat.edu.pl.projektam.BuildConfig
import wat.edu.pl.projektam.data.remote.api.AuthApiService
import wat.edu.pl.projektam.data.remote.api.UserApiService
import wat.edu.pl.projektam.data.remote.api.WorkoutApiService
import wat.edu.pl.projektam.data.remote.interceptor.AuthInterceptor
import wat.edu.pl.projektam.util.Constants
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // SHA-256 certyfikatu serwera — uzupełnij po wygenerowaniu certyfikatu SSL na backendzie.
    // Jak uzyskać: openssl s_client -connect 10.0.2.2:8443 | openssl x509 -pubkey -noout |
    //              openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | base64
    private const val CERT_PIN = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    private const val SERVER_HOST = "10.0.2.2"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(SERVER_HOST, CERT_PIN)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // Pinning aktywny — pamiętaj o uzupełnieniu CERT_PIN przed uruchomieniem z backendem.
            .certificatePinner(certificatePinner)
            .connectTimeout(Constants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideWorkoutApiService(retrofit: Retrofit): WorkoutApiService =
        retrofit.create(WorkoutApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)
}
