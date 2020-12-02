package com.example.cv_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login_activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login_activity );
        EditText email_et = (EditText) findViewById( R.id.email_edt_text_l);
        EditText password_et = (EditText) findViewById( R.id.pass_edt_text_l );

        Button login = (Button) findViewById( R.id.login_btn_l );
        Button signup = (Button) findViewById( R.id.singup_btn_l );

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = email_et.getText().toString();
                String password = password_et.getText().toString();

                Log.d("Login activity", email+password);
                if(email==null || password==null)
                {
                    Toast.makeText(login_activity.this, "Email or Password is invalid", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(login_activity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Login activity", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent activityChangeIntent = new Intent(login_activity.this, MainActivity.class);
                                    login_activity.this.startActivity(activityChangeIntent);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Login activity", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(login_activity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activityChangeIntent = new Intent(login_activity.this, SignupActivity.class);

                login_activity.this.startActivity(activityChangeIntent);
            }
        });

    }
}
