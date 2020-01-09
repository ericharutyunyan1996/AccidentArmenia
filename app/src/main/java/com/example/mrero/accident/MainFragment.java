
package com.example.mrero.accident;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;

//    private DatabaseReference mDatabaseLike;
//    private boolean mProcessLike = false;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    // loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    getActivity().finish();
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Accident");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
//        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);
//        mDatabaseLike.keepSynced(true);
        mBlogList = (RecyclerView) view.findViewById(R.id.blog_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(layoutManager);
        checkUserExist();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Accident, AccidentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Accident, AccidentViewHolder>(
                Accident.class, R.layout.blog_row, AccidentViewHolder.class,
                mDatabase) {
            @Override
            protected void populateViewHolder(AccidentViewHolder viewHolder,
                                              final Accident model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setAddress(model.getAddress());
//                viewHolder.setLikebtn(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent accidentsingleIntent = new Intent(getActivity(),
                                AccidentSingleActivity.class);
                        accidentsingleIntent.putExtra("accident_id", post_key);
                        startActivity(accidentsingleIntent);
                    }
                });
//                viewHolder.mLikebtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mProcessLike = true;
//
//                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                if (mProcessLike) {
//                                    if (dataSnapshot.child(post_key)
//                                            .hasChild(mAuth.getCurrentUser().getUid())) {
//                                        mDatabaseLike.child(post_key)
//                                                .child(mAuth.getCurrentUser().getUid())
//                                                .removeValue();
//                                        mProcessLike = false;
//                                    } else {
//                                        mDatabaseLike.child(post_key)
//                                                .child(mAuth.getCurrentUser().getUid())
//                                                .setValue("RandomValue");
//                                        mProcessLike = false;
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//
//                    }
//                });
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        /*
                         * Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                         * setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         * startActivity(setupIntent);
                         */
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    public static class AccidentViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton mLikebtn;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        private CallbackManager callbackManager;
        private ShareDialog shareDialog;

        public AccidentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
//            mLikebtn = (ImageButton) mView.findViewById(R.id.like_btn);
//            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
//            mDatabaseLike.keepSynced(true);


        }

//        public void setLikebtn(final String post_key) {
//            mDatabaseLike.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
//                        mLikebtn.setImageResource(R.drawable.thumb_up);
//                    } else {
//                        mLikebtn.setImageResource(R.drawable.thumb_up_outline);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }

        public void setTitle(String title) {
            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setUsername(String username) {
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }

        public void setAddress(String address) {
            TextView post_address = (TextView) mView.findViewById(R.id.post_address);
            post_address.setText(address);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Glide.with(ctx).load(image).into(post_image);
            // Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image,
            // new Callback() {
            // @Override
            // public void onSuccess() {
            //
            // }
            //
            // @Override
            // public void onError() {
            // Picasso.with(ctx).load(image).into(post_image);
            // }
            // });
        }
    }

}
