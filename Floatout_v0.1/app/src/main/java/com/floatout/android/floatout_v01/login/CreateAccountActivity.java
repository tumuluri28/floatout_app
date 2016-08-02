package com.floatout.android.floatout_v01.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.floatout.android.floatout_v01.MainActivity;
import com.floatout.android.floatout_v01.R;
import com.floatout.android.floatout_v01.model.User;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();

    private ProgressDialog mAuthProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference ref;

    private Button createAccountButton;

    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;

    private TextView signInText;

    private String userName,email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        grabInputData();

        mAuth =FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userName = mEditTextUsernameCreate.getText().toString();
                email = mEditTextEmailCreate.getText().toString();
                password = mEditTextPasswordCreate.getText().toString();

                boolean validUserName = isUserNameValid(userName);
                boolean validEmail = isEmailValid(email);
                boolean validPassword = isPasswordValid(password);

                if(!validEmail || !validUserName || !validPassword) return;

                Log.d(LOG_TAG, "im being clicked ");
                userName.trim();
                email.trim();
                password.trim();

                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null) {
                            String uid = user.getUid();
                            createUserInFirebaseDatabase(uid);
                            Toast.makeText(CreateAccountActivity.this, "Account Created!", Toast.LENGTH_SHORT)
                                    .show();
                        }else{
                            Toast.makeText(CreateAccountActivity.this, "Email ID exists. Try Login!", Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(LOG_TAG, "userId " + user.getUid());

                    Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        return true;
    }

    public void grabInputData(){
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);

        createAccountButton = (Button) findViewById(R.id.button_create_account);

        signInText = (TextView) findViewById(R.id.user_sign_in);
    }

    @Override
    protected void onResume() { super.onResume();}

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean isUserNameValid(String userName){
        if(userName.equals("")){
            mEditTextUsernameCreate
                    .setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email){
        boolean isGoodEmail=
                (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail){
            mEditTextEmailCreate.setError(String.format(getString(R.string.invalid_email_address)));
            return false;
        }
        return isGoodEmail;
    }

    private boolean isPasswordValid(String password){
        if(password.length() < 6){
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_password_short));
            return false;
        }
        return true;
    }

    private void createUserInFirebaseDatabase(String uid){
        final DatabaseReference userLocation = ref.child(uid);

        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(userName, email, timestampJoined);
                    userLocation.setValue(newUser);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
