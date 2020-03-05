package yw.main.babble.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    SharedPreferences sharedPreferences;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private String password_old;
    private String email;

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

                // TODO: try dialog fragment
                // makeAlertDialog();
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                // need to reauthenticate -- only need user to input password
                email = firebaseUser.getEmail();

                //
                AuthCredential authCredential = EmailAuthProvider
                        .getCredential(email, "password");

                // now must reauthenticate
                firebaseUser.reauthenticate(authCredential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser.updatePassword("something").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("tag", "Password updated");
                                            } else {
                                                Log.d("tag2", "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d("tag", "Error auth failed");
                                }
                            }
                        });
                return true;
            }
        });

        // open intent for Account delete
        Preference preference2 = findPreference("pref2");
        preference2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO
                // make alert dialog, on ok click delete account

                /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert);
                builder.setTitle("Are you sure you want to delete your Babble account?");

                builder.setPositiveButton("YES",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete user
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        AuthCredential credential = EmailAuthProvider
                                .getCredential("", "");

                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        user.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("tag", "account deleted");
                                                        }
                                                    }
                                                });
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
                dialog.show();*/
                return true;
            }
        });

        // Log out
        Preference preference3 = findPreference("pref3");
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

        // open intent for font draw
        Preference preference4 = findPreference("pref4");
        preference4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), FontDrawActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // TODO
        // open intent for themes change
        Preference preference5 = findPreference("pref5");
        preference5.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: themes
                Intent intent = new Intent(getActivity(), ThemeChangeActivity.class);
                startActivity(intent);

                /*ThemeChangeFragment themeFrag= new ThemeChangeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawer_layout,themeFrag,"tag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();*/
                return true;
            }
        });

    }

    public void makeAlertDialog(){
        // put a dialog box here asking for password
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert);
        builder.setTitle("Enter your old password.");
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);

        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                password_old = editText.getText().toString();
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