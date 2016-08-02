package com.floatout.android.floatout_v01;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.floatout.android.floatout_v01.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountManagement extends AppCompatActivity {

    private Button logOutButton,goBack,updatePassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mAuth =FirebaseAuth.getInstance();

        logOutButton = (Button) findViewById(R.id.logoutbutton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(AccountManagement.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        goBack = (Button) findViewById(R.id.goback);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountManagement.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        updatePassword = (Button) findViewById(R.id.updatepassword);
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordUpdateDialog();
            }
        });
    }

    protected void showPasswordUpdateDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(AccountManagement.this);
        View promptView = layoutInflater.inflate(R.layout.update_password_custom_dialog,null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountManagement.this);
        alertDialogBuilder.setView(promptView);

        final EditText passwordUpdate = (EditText) promptView.findViewById(R.id.updatepasswordedittext);
        String pass = passwordUpdate.getText().toString();
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = passwordUpdate.getText().toString();
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.updatePassword(newPassword);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
