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
import com.google.firebase.auth.UserProfileChangeRequest;


public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_signup );
        EditText email_et = (EditText) findViewById( R.id.email_edt_text );
        EditText password_et = (EditText) findViewById( R.id.pass_edt_text );
        EditText name_et = (EditText) findViewById( R.id.dname_text );

        Button signup = (Button) findViewById( R.id.signup_btn );
        Button login = (Button) findViewById( R.id.login_btn );


        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null)
        {
            FirebaseAuth.getInstance().signOut();
        }
        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = email_et.getText().toString();
                String password = password_et.getText().toString();
                String name = name_et.getText().toString();

                if(email==null || password==null)
                {
                    Toast.makeText(SignupActivity.this, "Email or Password is invalid", Toast.LENGTH_LONG).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(SignupActivity.this, "Successfully Registered.\n Please continue to login.", Toast.LENGTH_LONG).show();

                                    Log.d("create account", "createUserWithEmail:success");
                                    FirebaseUser user = auth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("UpdTe", "User profile updated.");
                                                    }
                                                }
                                            });

                                    user.sendEmailVerification();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("create account", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_LONG).show();
                                }

                            }
                        });
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activityChangeIntent = new Intent(SignupActivity.this, login_activity.class);

                SignupActivity.this.startActivity(activityChangeIntent);
            }
        });

    }
}
