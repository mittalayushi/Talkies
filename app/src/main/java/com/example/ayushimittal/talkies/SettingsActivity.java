package com.example.ayushimittal.talkies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mcurrentUser;

    private CircleImageView mdisplayImage;
    private TextView mName, mStatus;
    private Button mchangeStatus, mchangeImage;

    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mdisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mchangeImage = (Button) findViewById(R.id.changeImageBtn);
        mchangeStatus = (Button) findViewById(R.id.changeStatusBtn);

        mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mcurrentUser.getUid();

        mImageStorage= FirebaseStorage.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = "";
                String status = "";
                try {
                    name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    status = dataSnapshot.child("status").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    mName.setText(name);
                    mStatus.setText(status);

                    if (!image.equals("default"))
                    {
                        Toast.makeText(SettingsActivity.this,"not default",Toast.LENGTH_LONG).show();
                        Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dummy_user).into(mdisplayImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(SettingsActivity.this,"Ayushi",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError() {
                                Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.dummy_user).into(mdisplayImage);
                            }
                        });

                    }



                } catch (Exception e) {
                    Log.d("hey", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mchangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });
        mchangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK&& resultCode==RESULT_OK)
        {
            Uri ImageUri=data.getData();

            CropImage.activity(ImageUri).setAspectRatio(1,1).start(this);

        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK)
            {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we upload and process your image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String current_uid=mcurrentUser.getUid();


                Uri resultUri=result.getUri();
                File thumb_Filepath= new File(resultUri.getPath());

                final Bitmap thumb_bitmap=new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToBitmap(thumb_Filepath);

                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                final byte[] thumb_byte=byteArrayOutputStream.toByteArray();


                StorageReference filePath=mImageStorage.child("profile_images").child(current_uid+".jpeg");
                final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_uid+".jpeg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {


                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            final String download_url=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask=thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_download_url=thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){
 
                                        Map update_hashMap=new HashMap();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_download_url);

                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful())
                                                {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this,"Success Uploading",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(SettingsActivity.this,"Error in uploading thumbnail",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(SettingsActivity.this,"Error Uploading",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error=result.getError();
            }

        }
    }

}