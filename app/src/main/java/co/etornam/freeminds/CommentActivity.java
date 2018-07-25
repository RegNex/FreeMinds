package co.etornam.freeminds;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mikepenz.itemanimators.ScaleUpAnimator;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import co.etornam.freeminds.authenticate.SignupActivity;
import co.etornam.freeminds.model.Comment;

import static co.etornam.freeminds.util.DateConventer.calculateTimePeriod;

public class CommentActivity extends AppCompatActivity {
private RecyclerView mRecyclerView;
private FloatingActionButton sendBtn;
private EditText edtComment;
private FirebaseFirestore mDatabase,mComment;
private FirebaseAuth firebaseAuth;
private FirebaseUser firebaseUser;
private String key;
    private FirestoreRecyclerAdapter<Comment, CommentViewHolder> adapter;
ProgressDialog progressDialog;
    DocumentSnapshot r;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mRecyclerView = findViewById(R.id.recyclerView);
        sendBtn = findViewById(R.id.sendFab);
        edtComment = findViewById(R.id.edtComment);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setItemAnimator(new ScaleUpAnimator());
        mDatabase = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending...");
        firebaseUser = firebaseAuth.getCurrentUser();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            key = extras.getString("KEY");
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                progressDialog.show();
                SimpleDateFormat dateFt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String userComment = edtComment.getText().toString();
                if (!TextUtils.isEmpty(edtComment.getText().toString())) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("timeStamp", FieldValue.serverTimestamp());
                    objectMap.put("comment", userComment);
                    objectMap.put("time", dateFt.format(date));
                    objectMap.put("currentId", firebaseUser.getUid());
                    mDatabase.collection("Posts").document(key).collection("Comment").add(objectMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CommentActivity.this, "Comment Sent!", Toast.LENGTH_SHORT).show();
                                        edtComment.setText("");
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(CommentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                } else {
                    edtComment.setError("Enter a comment");
                }
            }
        });


        Query query = mDatabase.collection("Posts").document(key).collection("Comment").orderBy("timeStamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class).build();

        adapter = new FirestoreRecyclerAdapter<Comment, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder holder, int position, @NonNull Comment model) {
                r = getSnapshots().getSnapshot(position);

                holder.setIsRecyclable(false);

                Date date = new Date();
                SimpleDateFormat nwDateFt = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
                DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
                DateTime time1 = formatter.parseDateTime(nwDateFt.format(date));
                DateTime time2 = formatter.parseDateTime(model.getTime());
                Duration duration = new Duration(time2,time1);
                holder.commentTime.setText(calculateTimePeriod(Math.abs(duration.getStandardSeconds())));
                holder.commentText.setText(model.getComment());

                if (model.currentId.equalsIgnoreCase(Objects.requireNonNull(firebaseUser.getUid()))) {
                    holder.username.setText("You");
                    holder.username.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    mDatabase.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                String senderName = doc.get("username").toString();
                                holder.username.setText(senderName);
                            }else{
                                holder.username.setText(Objects.requireNonNull(firebaseUser.getEmail()));
                            }
                        }
                    });
                }
                holder.cardViewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new FancyAlertDialog.Builder(CommentActivity.this)
                                .setTitle("Delete Comment")
                                .setBackgroundColor(Color.parseColor("#f90202"))  //Don't pass R.color.colorvalue
                                .setMessage("Do you really want to Delete this comment ?")
                                .setNegativeBtnText("No")
                                .setPositiveBtnBackground(Color.parseColor("#f90202"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnText("Sure")
                                .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                                .setAnimation(Animation.POP)
                                .isCancellable(true)
                                .setIcon(R.drawable.ic_delete_forever_black_24dp,Icon.Visible)
                                .OnPositiveClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
                                        deleteComment();
                                    }
                                })
                                .OnNegativeClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
                                        Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .build();
                    }
                });

            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment_row, parent, false);
                return new CommentViewHolder(view);
            }
        };
    }
    adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
    }

    //delete comment
    private void deleteComment() {
        Toast.makeText(this, "key is : "+r.getId(), Toast.LENGTH_SHORT).show();
    }

    //ViewHolder for our Firebase UI
    public static class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView commentTime;
        TextView commentText;
        TextView username;
        RelativeLayout cardViewLayout;

        CommentViewHolder(View v) {
            super(v);
            cardViewLayout = v.findViewById(R.id.commentLayout);
            username = v.findViewById(R.id.txtUser);
            commentText = v.findViewById(R.id.txtComment);
            commentTime = v.findViewById(R.id.txtPostedTime);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        if (firebaseUser == null){
            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }
}
