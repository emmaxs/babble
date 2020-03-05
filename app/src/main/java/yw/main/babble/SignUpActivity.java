package yw.main.babble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class SignUpActivity extends AppCompatActivity {

    private EditText new_email;
    private EditText new_password;
    private Button signupButton;
    private FirebaseAuth firebaseAuth;

    // error stuff
    private final String SIGNUP_ERROR_TITLE = "Something went wrong!";
    private final String SIGNUP_ERROR_BLANK = "You must enter an email and a password.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // initializing FB authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // setting instance vars
        new_email = (EditText)findViewById(R.id.edit_email);
        new_password = (EditText)findViewById(R.id.edit_password);
        signupButton = (Button)findViewById(R.id.signupButton);

        signupButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String email = new_email.getText().toString();
                String password = new_password.getText().toString();

                // if a field is empty, throw up a dialog
                if (email.equals("") || password.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(SIGNUP_ERROR_BLANK)
                            .setTitle(SIGNUP_ERROR_TITLE)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>(){
                                public void onSuccess(AuthResult result){
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    // don't want to be able to reach signup by pressing back from main
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);

                            /* want to tell them why it failed
                             * e.g., password is too weak (less than 6 chars)
                             */
                            // wrong pass
                            if (e instanceof FirebaseAuthInvalidCredentialsException){
                                builder.setMessage("Please enter a valid email address!");
                            }
                            // wrong user
                            else if (e instanceof FirebaseAuthUserCollisionException){
                                builder.setMessage("This user has already registered for Babble!");
                            }
                            else if (e instanceof FirebaseAuthWeakPasswordException){
                                builder.setMessage("Please enter a password longer than 6 characters!");
                            }
                            else{
                                builder.setMessage(e.getLocalizedMessage());
                            }

                            // build the fail dialog
                            builder.setTitle(SIGNUP_ERROR_TITLE)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            }
        });
    }

}
