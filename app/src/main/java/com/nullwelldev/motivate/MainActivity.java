package com.nullwelldev.motivate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;

    private DatabaseReference mDataBase;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mProcessFavorite = false;
    private DatabaseReference mDatabaseFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    //User not logged-in

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//user won't be able to go back to previous page
                    startActivity(loginIntent);
                }
            }
        };

        mDataBase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseFavorite = FirebaseDatabase.getInstance().getReference().child("Favorites");

        mDataBase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseFavorite.keepSynced(true);

        mBlogList = (RecyclerView) findViewById(R.id.blog_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        checkUserExist();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDataBase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setFavoriteBtn(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_LONG).show();
                    }
                });

                //set onClickListener for favorite btn
                viewHolder.mFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessFavorite = true;

                        mDatabaseFavorite.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessFavorite && mAuth.getCurrentUser() != null) {

                                    //Check if favorite is pressed
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        //remove favorite - by removing value

                                        mDatabaseFavorite.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessFavorite = false;

                                    } else {

                                        mDatabaseFavorite.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                        mProcessFavorite = false;

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });
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
                    //runs real-time
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity
                                .class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//user won't be able to go back to previous page
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton mFavoriteBtn;
        DatabaseReference mDatabaseFavorite;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mFavoriteBtn = (ImageButton) mView.findViewById(R.id.favorite_btn);

            mDatabaseFavorite = FirebaseDatabase.getInstance().getReference().child("Favorites");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseFavorite.keepSynced(true);
        }

        public void setFavoriteBtn(final String post_key) {

            mDatabaseFavorite.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(mAuth.getCurrentUser() != null) {

                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                            mFavoriteBtn.setImageResource(R.mipmap.ic_favorite_black_48dp);
                            //mFavoriteBtn.setImageResource(R.mipmap.ic_favorite_border_black_48dp);
                        } else {
                            mFavoriteBtn.setImageResource(R.mipmap.ic_favorite_border_black_48dp);
                            //mFavoriteBtn.setImageResource(R.mipmap.ic_favorite_black_48dp);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc) {

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx, String image) {

            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(image).into(post_image);
        }

        public void setUsername(String username) {

            TextView post_desc = (TextView) mView.findViewById(R.id.post_username);
            post_desc.setText(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();

        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        //verify if user is logged in or not using auth listener
        mAuth.signOut();
    }
}