package com.donetr.chat.donefirebaseexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.donetr.chat.donefirebaseexample.R;
import com.donetr.chat.donefirebaseexample.custome_view.progressbares.CustomLoaderDialog;
import com.donetr.chat.donefirebaseexample.global.Prefs;
import com.donetr.chat.donefirebaseexample.model.UserModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private AccessToken accessToken;
    private ImageView imgLoginFB;
    private CallbackManager callbackManager;
    private CustomLoaderDialog customLoaderDialog;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String TAG = "LoginActivity";
    private FirebaseDatabase database;
    //Firebase and GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private boolean isUserExist = true;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initObjects();
        initListners();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    private void initObjects() {
        database = FirebaseDatabase.getInstance();
        imgLoginFB = (ImageView) findViewById(R.id.imgLoginFB);
        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();

    }

    private void initListners() {
        final boolean[] chatListCalled = {false};
        imgLoginFB.setOnClickListener(this);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && isUserExist && !chatListCalled[0]) {
                    // User is signed in
                    chatListCalled[0] = true;
                    Intent intent = new Intent(LoginActivity.this, ChatConversationList.class);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v == imgLoginFB) {

            signIn();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



    private void checkForUserAndSignuUp(final FirebaseUser currentUser) {

        final DatabaseReference firebase = database.getReference().child("users").child(currentUser.getUid());
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                customLoaderDialog.hide();
                // firebase.removeEventListener(this);
                if (snapshot.getValue() != null) {
                    /*GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {};
                    Map<String, String> hashMap = snapshot.getValue(genericTypeIndicator );

*/
                    try {
                        Map<String, String> hashMap = snapshot.getValue(HashMap.class);
                        Prefs.setUserId(LoginActivity.this, currentUser.getUid());
                        Prefs.setUSERNAME(LoginActivity.this, getValuesWithValid(hashMap, "displayName"));
                        Prefs.setEMAIL(LoginActivity.this, getValuesWithValid(hashMap, "email"));
                        Prefs.setPhotoUri(LoginActivity.this, getValuesWithValid(hashMap, "profileImageUri"));
                    } catch (Exception e) {

                    }
                    isUserExist = true;
                    if (customLoaderDialog != null)
                        customLoaderDialog.hide();
                    Intent intent = new Intent(LoginActivity.this, ChatConversationList.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (customLoaderDialog != null)
                        customLoaderDialog.hide();
                    isUserExist = false;
                    UserModel userModel = new UserModel("" + currentUser.getUid(), "offline", "" + currentUser.getDisplayName(), "", "Android", "" + currentUser.getPhotoUrl(), 0);
                    userModel.setEmail(currentUser.getEmail());
                    firebase.setValue(userModel);
                    Prefs.setUserId(LoginActivity.this, currentUser.getUid());
                    Prefs.setUSERNAME(LoginActivity.this, currentUser.getDisplayName());
                    Prefs.setEMAIL(LoginActivity.this, currentUser.getEmail());
                    Prefs.setPhotoUri(LoginActivity.this, currentUser.getPhotoUrl() + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                customLoaderDialog.hide();
                isUserExist = false;
                UserModel userModel = new UserModel("" + currentUser.getUid(), "offline", "" + currentUser.getDisplayName(), "", "Android", "" + currentUser.getPhotoUrl(), 0);
                userModel.setEmail(currentUser.getEmail());
                Prefs.setUserId(LoginActivity.this, currentUser.getUid());
                Prefs.setUSERNAME(LoginActivity.this, currentUser.getDisplayName());
                Prefs.setEMAIL(LoginActivity.this, currentUser.getEmail());
                Prefs.setPhotoUri(LoginActivity.this, currentUser.getPhotoUrl() + "");
                firebase.setValue(userModel);
            }

        });
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            checkForUserAndSignuUp(mAuth.getCurrentUser());
                        } else {
                            final DatabaseReference firebase = database.getReference().child("users").child(mAuth.getCurrentUser().getUid());
                            UserModel userModel = new UserModel("" + mAuth.getCurrentUser().getUid(), "offline", "" + mAuth.getCurrentUser().getDisplayName(), "", "Android", "" +  mAuth.getCurrentUser().getPhotoUrl(), 0);
                            userModel.setEmail(acct.getEmail());
                            firebase.setValue(userModel);
                            Intent intent = new Intent(LoginActivity.this, ChatConversationList.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private String getValuesWithValid(Map<String, String> hashMap, String displayName) {
        if (hashMap.containsKey("" + displayName) && hashMap.get("" + displayName).length() > 0) {
            return hashMap.get("" + displayName) + "";
        } else {
            return "";
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
