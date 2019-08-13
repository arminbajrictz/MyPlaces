package com.example.myplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUsers extends AppCompatActivity {

    EditText emailEditText,passwordEditText,repeatPasswordEditText;
    ProgressBar loading;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;

    public void switchToSignIn (View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void registerWithEmailAndPass(final String email, String password) {
        loading.setVisibility(View.VISIBLE);
        mAuth=FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterUsers.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),"User successfully created\nVerification email sent.",Toast.LENGTH_SHORT).show();
                    mAuth.getCurrentUser().sendEmailVerification();

                    UserModel user = new UserModel();
                    user.setEmail(mAuth.getCurrentUser().getEmail());
                    user.setNumLocationsAdded(0);
                    user.setMarkerPriority(0);

                    mDataBase = FirebaseDatabase.getInstance();
                    mRef = mDataBase.getReference("Users");
                    mRef.child(mAuth.getCurrentUser().getUid()).setValue(user);


                    mAuth.signOut();
                    loading.setVisibility(View.INVISIBLE);
                }else {
                    if (isValidEmailAddress(email)) {

                        if (passwordEditText.length()<6) {
                            Toast myToast = Toast.makeText(getApplicationContext(),"Password must contains \nat least 6 characters.",Toast.LENGTH_SHORT);
                        } else {
                            Toast.makeText(getApplicationContext(), "User already registred with \n"+email, Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "Email not valid.", Toast.LENGTH_SHORT).show();
                    }
                    loading.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public boolean passwordMatch (String pass1,String pass2 ) {
        if (pass1.equals(pass2)) {
            return true;
        }else {
            return false;
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void creatNewUser (View view) {
        isEmpty();
        if (isEmpty()) {
            Toast.makeText(getApplicationContext(),"Plase fill all fields",Toast.LENGTH_SHORT).show();
        } else {

            if (passwordMatch(passwordEditText.getText().toString(),repeatPasswordEditText.getText().toString())) {

                    registerWithEmailAndPass(emailEditText.getText().toString(),passwordEditText.getText().toString());

            }else {
                Toast.makeText(getApplicationContext(),"Password doesn't match!",Toast.LENGTH_SHORT).show();

            }

        }
    }

    public boolean isEmpty () {
        if (TextUtils.isEmpty(emailEditText.getText()) ||
                TextUtils.isEmpty(passwordEditText.getText()) ||
                TextUtils.isEmpty(repeatPasswordEditText.getText())) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_users);

        emailEditText = (EditText) findViewById(R.id.emailEditText2);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        repeatPasswordEditText = (EditText) findViewById(R.id.passwordRepeatText);
        loading = (ProgressBar)findViewById(R.id.loadingProgressBar);
        loading.getIndeterminateDrawable().setColorFilter(0xFFD95642,
                android.graphics.PorterDuff.Mode.MULTIPLY);
        loading.setVisibility(View.INVISIBLE);

    }
}
