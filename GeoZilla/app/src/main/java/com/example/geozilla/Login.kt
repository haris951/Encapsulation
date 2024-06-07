package com.example.geozilla
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.geozilla.activities.ForgotPassword
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
class Login : AppCompatActivity() {
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth= FirebaseAuth.getInstance()
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient=GoogleSignIn.getClient(this,gso)
        findViewById<Button>(R.id.button4).setOnClickListener{
            googleSignIn();
        }
    }
    private fun googleSignIn() {
val signInClient=googleSignInClient.signInIntent
    launcher.launch(signInClient)
    }
private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
    result ->
    if (result.resultCode == Activity.RESULT_OK){
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        manageResults(task);
    }
}
    private fun manageResults(task: Task < GoogleSignInAccount>) {
        val account: GoogleSignInAccount? = task.result
        if (account !=null){
            val credential = GoogleAuthProvider.getCredential(account.idToken,null)
            auth.signInWithCredential(credential).addOnCompleteListener{
                if (task.isSuccessful){
                    val intent =Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"Account Created",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    fun openmain5(view:View){
        val intent = Intent(this, ForgotPassword::class.java)
        startActivity(intent)
    }
    fun openmain6(view: View){
        val intent = Intent (this,MainActivity::class.java)
        startActivity(intent)
    }
    }










