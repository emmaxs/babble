package yw.main.babble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    // textviews and firebase authentication
    protected EditText email;
    protected EditText password;
    protected Button logIn;
    protected TextView signUp;
    private FirebaseAuth firebaseAuth;

    // error message and title for login error dialog
    private final String LOGIN_ERROR_TITLE = "Login failed.";
    private final String WRONG_LOGIN_ERROR = "Password or email is incorrect.";
    private final String LOGIN_ERROR = "Must enter a value in both fields!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialixing the firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // setting up our instance variables
        email = (EditText) findViewById(R.id.edit_email);
        password = (EditText) findViewById(R.id.edit_password);
        signUp = (TextView) findViewById(R.id.signUpText);
        logIn = (Button) findViewById(R.id.loginButton);

        // click listener for login button
        // if user leaves a field blank, make failure dialog
        // if user completes email and password correctly, progress to main activity
        // if user puts in wrong info, make failure dialog
        logIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email_text = email.getText().toString();
                String password_text = password.getText().toString();

                // if either field is empty altogether, send error dialog
                if(email_text.equals("") || password_text.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(LOGIN_ERROR)
                            .setTitle(LOGIN_ERROR_TITLE)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                // else, check if right password/email or not
                else{
                    firebaseAuth.signInWithEmailAndPassword(email_text, password_text)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>(){
                                public void onSuccess(AuthResult result){
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    // again, don't want to be able to reach login from main
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                            /* want to tell them why it failed
                             * easy if password is wrong, harder if user is wrong
                             */
                            // wrong pass
                            if (e instanceof FirebaseAuthInvalidCredentialsException){
                                builder.setMessage("Wrong password entered!");
                            }
                            // wrong user
                            else if (e instanceof FirebaseAuthInvalidUserException){
                                String which_error = ((FirebaseAuthInvalidUserException)e).getErrorCode();

                                if (which_error.equals("ERROR_USER_NOT_FOUND")){
                                    builder.setMessage("User not found!");
                                }
                                else if (which_error.equals("ERROR_USER_DISABLED")){
                                    builder.setMessage("User account is disabled!");
                                }
                                else if (which_error.equals("ERROR_USER_TOKEN_EXPIRED")){
                                    builder.setMessage("User token has expired!"+
                                                    "User credentials may have been changed on another device.");
                                }
                                else{
                                    builder.setMessage(e.getLocalizedMessage());
                                }
                            }

                            // build the fail dialog
                            builder.setTitle(LOGIN_ERROR_TITLE)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            }
        });

        // if the user clicks sign up
        signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });



    }

    // if user is already logged in, dont want to launch again
    // make a new account
    // if login successful, go to main activity and finihs() this one
}
