import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import java.security.KeyStore
import javax.net.ssl.*


interface CatFactCallback {
    fun onSuccess(response: String)
    fun onError(exception: Exception)
}


fun createCustomTrustManager(): Array<TrustManager> {
    val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            // Implement your custom client certificate validation logic here
        }

        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            // Implement your custom server certificate validation logic here
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    }
    return arrayOf(trustManager)
}

fun createSSLContext(): SSLContext {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, createCustomTrustManager(), java.security.SecureRandom())
    return sslContext
}

fun applyCustomTrustManager() {
    val sslContext = createSSLContext()
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyCustomTrustManager()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerLink = findViewById<TextView>(R.id.register_link)

        fun fetchCatFact(callback: CatFactCallback) {
            thread {
                try {
                    val url = URL("https://catfact.ninja/fact")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use { it.readText() }
                    callback.onSuccess(response)
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            var ss= "asdf"
           // it.context.startActivity(intent)
            fetchCatFact(object : CatFactCallback {
                override fun onSuccess(response: String) {
                    ss = response;
                    val intent = Intent(it.context, DisplayActivity::class.java).apply {
                        putExtra("USERNAME", username)
                        putExtra("PASSWORD", ss)
                    }
                    startActivity(intent)

                }

                override fun onError(exception: Exception) {
                    ss = exception.message.toString();
                    val intent = Intent(it.context, DisplayActivity::class.java).apply {
                        putExtra("USERNAME", username)
                        putExtra("PASSWORD", ss)
                    }
                    startActivity(intent)
                }
            })

        }

        registerLink.setOnClickListener {
            // Handle the click event, e.g., navigate to the registration screen
            // val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            // startActivity(intent)
        }
    }
}
