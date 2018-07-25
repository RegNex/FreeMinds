package co.etornam.freeminds;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackandphantom.circularimageview.CircleImage;
import com.mikepenz.itemanimators.ScaleUpAnimator;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import co.etornam.freeminds.authenticate.SignupActivity;
import co.etornam.freeminds.model.Post;

import static co.etornam.freeminds.util.DateConventer.calculateTimePeriod;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    FloatingActionButton actionText,actionImage;
    FloatingActionsMenu fab;
    RecyclerView mRecyclerView;
    FirebaseAuth mAuth;
    FirebaseFirestore mDatabase,mUser,mPost;
    String uid;
    Bitmap compressedImageFile;
    private FirestoreRecyclerAdapter<Post, PostViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.multiple_actions_left);
        mRecyclerView = findViewById(R.id.recyclerList);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setItemAnimator(new ScaleUpAnimator());
        actionText = findViewById(R.id.fabText);
        actionImage = findViewById(R.id.fabImage);
        actionText.setIcon(R.drawable.ic_mode_edit_black_24dp);
        actionImage.setIcon(R.drawable.ic_insert_photo_black_24dp);

        mAuth = FirebaseAuth.getInstance();
        initUserAuth();
        mDatabase = FirebaseFirestore.getInstance();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);
        final TextView userName = hView.findViewById(R.id.headerUsername);
        final CircleImage userImage = hView.findViewById(R.id.headerImg);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.setVisibility(View.GONE);
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.setVisibility(View.VISIBLE);
                    }
                }else {
                    if (!fab.isShown()){
                        fab.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mDatabase.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    String username = doc.get("username").toString();
                    String userphoto = doc.get("imageUrl").toString();
                    userName.setText(username);
                    Picasso.get().load(userphoto).noFade().into(userImage);
                }else{
                    userName.setText(getResources().getString(R.string.app_name));
                    Picasso.get().load(String.valueOf(getResources().getDrawable(R.drawable.ic_android_black_24dp))).noFade().into(userImage);
                }
            }
        });


        actionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),PostTextActivity.class));
                finish();
            }
        });

        actionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),PictureBlogActivity.class));
                finish();
            }
        });


        Query query = mDatabase.collection("Posts").orderBy("datePosted",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        adapter = new FirestoreRecyclerAdapter<Post, PostViewHolder>(options) {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onBindViewHolder(@NonNull final PostViewHolder holder, final int position, @NonNull Post model) {
                final DocumentSnapshot r = getSnapshots().getSnapshot(position);
                holder.setIsRecyclable(false);
                Date date = new Date();
                SimpleDateFormat nwDateFt = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
                holder.postDesc.setText(model.getPostText());
                holder.txtImgDesc.setText(model.getImgDesc());
                final String userId = model.getCurrent_userId();
                if (model.getThumbnail().isEmpty()){
                    holder.imageLayout.setVisibility(View.GONE);
                }else{
                    holder.imageLayout.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getThumbnail()).noFade().into(holder.mImage);
                }
                DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
                DateTime time1 = formatter.parseDateTime(nwDateFt.format(date));
                DateTime time2 = formatter.parseDateTime(model.getTimeStamp());
                Duration duration = new Duration(time2,time1);
                Log.d(TAG, "onBindViewHolder: date "+duration.getStandardSeconds());
                Log.d(TAG, "onBindViewHolder: formatted "+calculateTimePeriod(duration.getStandardSeconds()));
                holder.postTime.setText(calculateTimePeriod(Math.abs(duration.getStandardSeconds())));

                Log.d(TAG, "onBindViewHolder: "+model.getTimeStamp());
                if (userId.equalsIgnoreCase(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())){
                    holder.postSender.setText("You");
                    holder.postSender.setTextColor(getResources().getColor(R.color.colorPrimary));
                    mDatabase.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                String senderImg = doc.get("imageUrl").toString();
                                Picasso.get().load(senderImg)
                                        .noFade()
                                        .resize(50, 50)
                                        .into(holder.senderImg);
                            }
                        }
                    });
                }else {
                    mDatabase.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                String senderName = doc.get("username").toString();
                                String senderImg = doc.get("imageUrl").toString();
                                holder.postSender.setText(senderName);
                                Picasso.get().load(senderImg).noFade().into(holder.senderImg);
                            }else{
                                holder.postSender.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
                                //  Picasso.get().load().noFade().into(holder.senderImg);
                            }
                        }
                    });
                }

                //likes count
                mDatabase.collection("Posts").document(r.getId()).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            int count = queryDocumentSnapshots.size();
                            if (count == 1){
                                holder.txtLikesCount.setText(count + " like");
                            }else{
                                holder.txtLikesCount.setText(count + " likes");
                            }

                        }else{
                            holder.txtLikesCount.setText("No like yet");
                        }
                    }
                });

                //comments count
                mDatabase.collection("Posts").document(r.getId()).collection("Comment").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                   if (!queryDocumentSnapshots.isEmpty()){
                       int count = queryDocumentSnapshots.size();
                       if (count == 1){
                           holder.txtCommentCount.setText(count + " comment");
                       }else{
                           holder.txtCommentCount.setText(count + " comments");
                       }
                   }else{
                       holder.txtCommentCount.setText("No Comment");
                   }
                    }
                });

                mDatabase.collection("Posts").document(r.getId()).collection("Likes").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    holder.likesBtn.setImageDrawable(getDrawable(R.drawable.ic_sentiment_very_satisfied_black_24dp));
                }else{
                    holder.likesBtn.setImageDrawable(getDrawable(R.drawable.ic_sentiment_dissatisfied_black_24dp));
                }
                    }
                });

                holder.likesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.collection("Posts").document(r.getId()).collection("Likes").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()){
                                    Map<String, Object> objectsMap = new HashMap<>();
                                    objectsMap.put("timeStamp", FieldValue.serverTimestamp());
                                    mDatabase.collection("Posts").document(r.getId()).collection("Likes").document(mAuth.getCurrentUser().getUid()).set(objectsMap);
                                }else{
                                    mDatabase.collection("Posts").document(r.getId()).collection("Likes").document(mAuth.getCurrentUser().getUid()).delete();

                                }
                            }
                        });

                holder.btnExtra.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                        popupMenu.inflate(R.menu.content_other_menu);

                        // Force icons to show
                        Object menuHelper;
                        Class[] argTypes;
                        try {
                            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                            fMenuHelper.setAccessible(true);
                            menuHelper = fMenuHelper.get(popupMenu);
                            argTypes = new Class[] { boolean.class };
                            menuHelper.getClass().getDeclaredMethod("setForceShowIcon",
                                    argTypes).invoke(menuHelper, true);
                        } catch (Exception e) {
                            // Possible exceptions are NoSuchMethodError and
                            // NoSuchFieldError
                            //
                            // In either case, an exception indicates something is wrong
                            // with the reflection code, or the
                            // structure of the PopupMenu class or its dependencies has
                            // changed.
                            //
                            // These exceptions should never happen since we’re shipping the
                            // AppCompat library in our own apk,
                            // but in the case that they do, we simply can’t force icons to
                            // display, so log the error and
                            // show the menu normally.

                            Log.w(TAG, "error forcing menu icons to show", e);
                            popupMenu.show();
                            return;
                        }
                        popupMenu.show();
                    }

                });




                    }
                });

                holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), CommentActivity.class);
                        i.putExtra("KEY",r.getId());
                        startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.single_row, group, false);
                return new PostViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.d(TAG, "onError: "+e.getMessage());
            }
        };
/*        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });*/
        mRecyclerView.setAdapter(adapter);
    }

    //ViewHolder for our Firebase UI
    public static class PostViewHolder extends RecyclerView.ViewHolder{
        TextView postDesc;
        TextView postTime;
        TextView postSender;
        TextView txtLikesCount;
        TextView txtImgDesc;
        TextView txtCommentCount;
        CircleImage senderImg;
        ImageView mImage;
        ImageButton likesBtn;
        ImageButton commentBtn;
        ImageButton btnExtra;
        RelativeLayout imageLayout;

        PostViewHolder(View v) {
            super(v);
            postDesc = v.findViewById(R.id.postText);
            mImage = v.findViewById(R.id.postImage);
            postSender = v.findViewById(R.id.txtUsername);
            senderImg = v.findViewById(R.id.imgUser);
            postTime = v.findViewById(R.id.txtUploadTime);
            likesBtn = v.findViewById(R.id.btnLikes);
            txtLikesCount = v.findViewById(R.id.txtLikes);
            commentBtn = v.findViewById(R.id.btnComments);
            txtCommentCount = v.findViewById(R.id.txtComments);
            txtImgDesc = v.findViewById(R.id.imgDesc);
            imageLayout = v.findViewById(R.id.imageLayout);
            btnExtra = v.findViewById(R.id.btnExtra);

        }
    }

    private void initUserAuth() {
        if (mAuth.getCurrentUser() != null){
            uid = mAuth.getCurrentUser().getUid();
        }else{
            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_privacy) {

        } else if (id == R.id.nav_home) {

        } else if (id == R.id.nav_post) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
           if (mAuth.getCurrentUser() != null){
               mAuth.signOut();
               startActivity(new Intent(getApplicationContext(),SignupActivity.class));
               finish();
               Toast.makeText(this, "See you soon!", Toast.LENGTH_SHORT).show();
           }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initUserAuth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUserAuth();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initUserAuth();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }
}
