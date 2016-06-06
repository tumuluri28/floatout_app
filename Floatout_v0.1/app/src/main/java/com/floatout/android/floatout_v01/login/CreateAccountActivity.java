package com.floatout.android.floatout_v01.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.EditText;

import com.floatout.android.floatout_v01.R;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth.getInstance();

        initializeScreen();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        return true;
    }

    public void initializeScreen(){

        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);




    }

}
