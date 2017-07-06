package com.donetr.chat.donefirebaseexample.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import com.donetr.chat.donefirebaseexample.R;
import com.donetr.chat.donefirebaseexample.adapter.UserAdapter;
import com.donetr.chat.donefirebaseexample.custome_view.progressbares.CustomLoaderDialog;
import com.donetr.chat.donefirebaseexample.global.Constant;
import com.donetr.chat.donefirebaseexample.model.UserModel;
import com.donetr.chat.donefirebaseexample.viewholders.FirebaseUserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * Created by Bekir TURA on 5/7/17.
 */
public class UserList extends AppCompatActivity {
    private static final String TAG = "UserList";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private ArrayList<UserModel> arrayUser;
    private UserAdapter userAdapter;
    private RecyclerView my_recycler_view;
    private LinearLayoutManager mLayoutManager;
    private CustomLoaderDialog customeLoaderDialog;
    private FirebaseRecyclerAdapter<UserModel, FirebaseUserViewHolder> mFirebaseAdapter;
    public static int from = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_userlist_activity);

        initObjects();
        setUpinitialData();
       /* getUserList();*/
    }

    private void setUpinitialData() {
        DatabaseReference databaseReference = database.getReference().child("users");
        Query query = databaseReference.limitToFirst(50); // for the first 50 user from users node
        if (customeLoaderDialog == null)
            customeLoaderDialog = new CustomLoaderDialog(this);
        customeLoaderDialog.show(false);
        setUpFirebaseAdapter(query);

    }

    private void initObjects() {
        mAuth = FirebaseAuth.getInstance();
        Constant.USER_ID = mAuth.getCurrentUser().getUid();
        my_recycler_view = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        my_recycler_view.setLayoutManager(mLayoutManager);
        database = FirebaseDatabase.getInstance();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            from = bundle.getInt("from");// from = 1 -> ChatConversationList , from = 2 -> GroupChatConversation
            getSupportActionBar().setTitle("Select Freind");
        }
    }

    private void getUserList() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null) {
            final DatabaseReference firebase = database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("status");
            firebase.setValue("online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (database != null) {
            final DatabaseReference firebase = database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("status");
            firebase.setValue("offline");
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*getMenuInflater().inflate(R.menu.user_activity, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);*/
        return true;
    }


    private void setUpFirebaseAdapter(Query query) {

        mFirebaseAdapter = new FirebaseRecyclerAdapter<UserModel, FirebaseUserViewHolder>
                (UserModel.class, R.layout.row_user_list, FirebaseUserViewHolder.class, query) {
            @Override
            protected void populateViewHolder(FirebaseUserViewHolder viewHolder, UserModel model, int position) {
                customeLoaderDialog.hide();
                viewHolder.bindUser(model);
            }
        };

        my_recycler_view.setHasFixedSize(true);
        my_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        my_recycler_view.setAdapter(mFirebaseAdapter);

    }

}
