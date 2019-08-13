package com.example.myplaces;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText,passwordEditText;
    private ProgressBar loading;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText=findViewById(R.id.emailEditText);
        passwordEditText= findViewById(R.id.passwordEditText);
        loading= findViewById(R.id.loadingProgressBarSign);
        loading.getIndeterminateDrawable().setColorFilter(0xFFD95642,
                android.graphics.PorterDuff.Mode.MULTIPLY);
        loading.setVisibility(View.INVISIBLE);

        setUpFirebaseAuth();

    }


    public void signUserWithInfo(String email,String password) {
        mAuth = FirebaseAuth.getInstance();
        loading.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loading.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"Wrong email or password",Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void registerActivity (View view) {
        Intent intent = new Intent(getApplicationContext(),RegisterUsers.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        if (isEmpty()) {
            Toast.makeText(getApplicationContext(),"Plase fill all fields",Toast.LENGTH_SHORT).show();
        }else {
            signUserWithInfo(emailEditText.getText().toString(),passwordEditText.getText().toString());
        }
    }

    public boolean isEmpty () {
        return TextUtils.isEmpty(emailEditText.getText()) ||
                TextUtils.isEmpty(passwordEditText.getText());
    }

    public void setUpFirebaseAuth () {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null) {
                    if (user.isEmailVerified()) {
                        Toast.makeText(getApplicationContext(),"Hi! Welcome back!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),HomeDashBoard.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(getApplicationContext(),"Please verify your email",Toast.LENGTH_SHORT).show();
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                    }
                }else {
                    Log.i("signed out","signed out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener!=null) {
            mAuth = FirebaseAuth.getInstance();
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
