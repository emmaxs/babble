package yw.main.babble.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import yw.main.babble.LoginActivity;
import yw.main.babble.R;
import yw.main.babble.font.FontDrawActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String string) {

        addPreferencesFromResource(R.xml.settings_prefs);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // open intent for Password Change
        Preference preference1 = findPreference("pref1");
        preference1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // pop up a dialog asking for old password
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
                builder.setTitle("Enter your old password.");
                EditText editText = new EditText(getActivity());
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(editText);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                        // need to reauthenticate -- only need user to input password
                        String email = firebaseUser.getEmail();
                        String pass = editText.getText().toString();

                        //
                        AuthCredential authCredential = EmailAuthProvider
                                .getCredential(email, pass);

                        // now try reauthentication
                        firebaseUser.reauthenticate(authCredential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        // if task successful, need yet another alert dialog
                                        if (task.isSuccessful()) {

                                            // pop up a dialog asking for new password
                                            AlertDialog.Builder sub_builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
                                            sub_builder.setTitle("Enter a new password.");
                                            EditText sub_editText = new EditText(getActivity());
                                            sub_editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                            sub_builder.setView(sub_editText);

                                            sub_builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                                                public void onClick(DialogInterface dialog, int which){
                                                    String new_pass = sub_editText.getText().toString();
                                                    firebaseUser.updatePassword(new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("tag", "Password updated");
                                                                Toast.makeText(getActivity(),
                                                                        "Success! Password updated.",
                                                                        Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Log.d("tag2", "Error password not updated");
                                                                Toast.makeText(getActivity(),
                                                                        "Something went wrong.",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            });

                                            sub_builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){
                                                public void onClick(DialogInterface dialog, int which){
                                                    dialog.cancel();
                                                }
                                            });

                                            AlertDialog sub_dialog = sub_builder.create();
                                            sub_dialog.show();

                                        } else {
                                            // just make a toast for wrong password
                                            Log.d("tag", "Error auth failed");
                                            Toast.makeText(getActivity(), "Wrong password entered.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        // open intent for Account delete
        Preference preference2 = findPreference("deleteAccount");
        preference2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO
                // make alert dialog, on ok click delete account

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
                builder.setTitle(" Are you sure about deleting your Babble account?");

                // User clicked yes
                builder.setPositiveButton("YES",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Prompt the user to re-provide their sign-in credentials
                        // i.e. make another dialog asking for password
                        AlertDialog.Builder sub_builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
                        sub_builder.setTitle("Enter your password.");
                        EditText editText = new EditText(getActivity());
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        sub_builder.setView(editText);

                        sub_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete user
                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                // get email and password for credentials
                                String user_email = firebaseUser.getEmail();
                                String pass = editText.getText().toString();
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(user_email, pass);

                                firebaseUser.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                firebaseUser.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("tag", "account deleted");
                                                                    Toast.makeText(getContext(),
                                                                            "Success.",
                                                                            Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                                else{
                                                                    Toast.makeText(getContext(),
                                                                            "Something went wrong. Try again.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });

                        sub_builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog sub_dialog = sub_builder.create();
                        sub_dialog.show();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        // Log out
        Preference preference3 = findPreference("logOut");
        preference3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // sign out
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                loadLoginScreen();
                return true;
            }
        });

        // open intent for default_font draw
        Preference preference4 = findPreference("openFontDraw");
        preference4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), FontDrawActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // open intent for themes change
        Preference preference5 = findPreference("changeTheme");
        preference5.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: themes
                Intent intent = new Intent(getActivity(), ThemeChangeActivity.class);
                startActivity(intent);
                return true;
            }
        });

    }

    // method to open the login activity
    // leah, 2.21.20
    private void loadLoginScreen() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        // so that user cannot press back button and get back to main activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}