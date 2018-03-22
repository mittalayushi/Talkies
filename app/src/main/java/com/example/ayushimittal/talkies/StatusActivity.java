package com.example.ayushimittal.talkies;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private TextInputLayout mstatus;
    private Button msavebtn;
    private ProgressDialog mprogressDialog;

    private DatabaseReference mstatusDatabase;
    private FirebaseUser mcurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mcurrentUser.getUid();

        mstatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        //progress
        mprogressDialog = new ProgressDialog(this);

        mstatus = (TextInputLayout) findViewById(R.id.status_input);
        msavebtn = (Button) findViewById(R.id.status_save_btn);

        mstatus.getEditText().setText(status_value);

        msavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mprogressDialog.setTitle("Saving Changes");
                mprogressDialog.setMessage("Please wait, while we save the changes");
                mprogressDialog.setCanceledOnTouchOutside(false);
                mprogressDialog.show();

                String status = mstatus.getEditText().getText().toString();
                mstatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mprogressDialog.dismiss();

                        } else {
                            Toast.makeText(StatusActivity.this, "There was some error in saving changes, please try again", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
    }
}
