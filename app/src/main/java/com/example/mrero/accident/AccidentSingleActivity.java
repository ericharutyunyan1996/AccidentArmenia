
package com.example.mrero.accident;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class AccidentSingleActivity extends AppCompatActivity {
    private String mPost_key = null;
    private DatabaseReference mDatabase;
    private ImageView mAccidentSingleImage;
    //    private TextView mAccidentSingleTitle;
//    private TextView mAccidentSingleDesc;
    private FirebaseAuth mAuth;
    private String post_uid;
    private Button mSingleRemoveBtn;
    private ImageButton shareButton;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_single);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1596294793206523~6720473494");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mprogress=new ProgressDialog(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar_event_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        shareButton = (ImageButton) findViewById(R.id.share_btn);
        facebookSdkInitilaize();
        shareDialog = new ShareDialog(this);


        mPost_key = getIntent().getExtras().getString("accident_id");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Accident");
        mAuth = FirebaseAuth.getInstance();
        mAccidentSingleImage = (ImageView) findViewById(R.id.singleBlogImage);


//        mAccidentSingleDesc = (TextView) findViewById(R.id.singleBlogDec);
//        mAccidentSingleTitle = (TextView) findViewById(R.id.singleBlogTitle);
        // mSingleRemoveBtn = (Button) findViewById(R.id.singleRemoveBtn);


        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
//                String post_title = (String) dataSnapshot.child("title").getValue();
//                String post_desc = (String) dataSnapshot.child("desc").getValue();
                final String post_image = (String) dataSnapshot.child("image").getValue();
                post_uid = (String) dataSnapshot.child("uid").getValue();
//                mAccidentSingleTitle.setText(post_title);
//                mAccidentSingleDesc.setText(post_desc);
                Glide.with(AccidentSingleActivity.this).load(post_image)
                        .into(mAccidentSingleImage);
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mprogress.setMessage("Opening");
                        mprogress.show();
                        if (ShareDialog.canShow(SharePhotoContent.class)) {


                            final Bitmap[] image = new Bitmap[1];
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    Looper.prepare();
                                    try {
                                        image[0] = Glide.with(AccidentSingleActivity.this).load(post_image).asBitmap()
                                                .into(-1, -1)
                                                .get();
                                    } catch (final ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    return null;

                                }

                                @Override
                                protected void onPostExecute(Void dummy) {
                                    if (null != image[0]) {
                                        SharePhoto photo = new SharePhoto.Builder()
                                                .setBitmap(image[0])
                                                .build();
                                        SharePhotoContent content = new SharePhotoContent.Builder()
                                                .addPhoto(photo)
                                                .build();

                                        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
                                        mprogress.dismiss();
                                    }
                                }
                            }.execute();



                        }
                        else {
                            Toast.makeText(AccidentSingleActivity.this, "Something Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void ShareDialog(Bitmap imagepath) {
        SharePhoto photo = new SharePhoto.Builder().setBitmap(imagepath).setCaption("").build();
        SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
        shareDialog.show(content);
    }

    private void facebookSdkInitilaize() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAuth.getCurrentUser().getUid().equals(post_uid)) {
            getMenuInflater().inflate(R.menu.remove, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_remove) {
            mDatabase.child(mPost_key).removeValue();
            Intent mainIntent = new Intent(AccidentSingleActivity.this, MainActivity.class);
            startActivity(mainIntent);
        }
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
