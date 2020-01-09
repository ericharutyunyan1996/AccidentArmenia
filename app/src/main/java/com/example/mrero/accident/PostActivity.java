
package com.example.mrero.accident;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;

public class PostActivity extends AppCompatActivity {

    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String PICTURE_URI_KEY = "PICTURE_URI_KEY";

    private static final int Gallery_Request = 1;
    private static final int Map_Request = 2;

    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitBtn;
    private Uri mImageUri = null;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUser;
    private AppCompatButton mapBtn;

    Intent intent;

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Editable title = mPostTitle.getText();
        if (title != null) {
            outState.putString(TITLE_KEY, title.toString());
        } else {
            outState.putString(TITLE_KEY, null);
        }
        Editable descr = mPostDesc.getText();
        if (descr != null) {
            outState.putString(DESCRIPTION_KEY, descr.toString());
        } else {
            outState.putString(DESCRIPTION_KEY, null);
        }

        if (mImageUri != null) {
            outState.putString(PICTURE_URI_KEY, mImageUri.toString());
        } else {
            outState.putString(PICTURE_URI_KEY, null);
        }

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPostTitle.setText(savedInstanceState.getString(TITLE_KEY));
        mPostDesc.setText(savedInstanceState.getString(DESCRIPTION_KEY));

        String uri = savedInstanceState.getString(PICTURE_URI_KEY);
        if (uri != null) {
            mSelectImage.setImageURI(Uri.parse(uri));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getSupportActionBar().setTitle("Post");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mCurrentUser.getUid());
        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);
        mPostTitle = (EditText) findViewById(R.id.titleField);

        mapBtn = (AppCompatButton) findViewById(R.id.mapPoint);

        mPostDesc = (EditText) findViewById(R.id.descField);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);
        mProgress = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Accident");
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Request);

            }
        });
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, MapActivity.class);
                startActivityForResult(intent, Map_Request);
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Posting");

        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null) {
            mProgress.show();
            StorageReference filepath = mStorage.child("Accident_images")
                    .child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            final DatabaseReference newPost = mDatabase.push();

                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newPost.child("title").setValue(title_val);
                                    newPost.child("desc").setValue(desc_val);
                                    newPost.child("image").setValue(downloadUrl.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("address").setValue(intent.getStringExtra("address"));
                                    newPost.child("lat").setValue(intent.getDoubleExtra("lat", 0.0));
                                    newPost.child("lng").setValue(intent.getDoubleExtra("lng", 0.0));
                                    newPost.child("date").setValue(Calendar.getInstance().getTimeInMillis());
                                    newPost.child("username")
                                            .setValue(dataSnapshot.child("name").getValue())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(PostActivity.this,
                                                                MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(PostActivity.this, "Error",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mProgress.dismiss();

                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Request && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mSelectImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == Map_Request && data != null) {
            intent = data;
            mapBtn.setText(data.getStringExtra("address"));
        }
    }
}
