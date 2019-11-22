package com.example.innovacceems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 7;
    SignInButton button;

    private boolean isNotLoggedIn;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser currentUser;

    String userUID;
    public static String userEmail;
    public static String userName;

    private static String TAG = "LoginActivity";

    public Context context;
    public static Visitor visitor;

    CollectionReference visitorDB = FirebaseFirestore.getInstance().collection("dbVisitors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        context = this;

        visitor = new Visitor();

        //TODO: Remove this
        mAuth.signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        currentUser = mAuth.getCurrentUser();
        updateUI();
        getSupportActionBar().hide();
        if (isNotLoggedIn) {
            setContentView(R.layout.activity_main);
            button = findViewById(R.id.sign_in_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
        } else {
            switchAct();
        }
    }

    private void updateUI() {
        currentUser = mAuth.getCurrentUser();
        isNotLoggedIn = currentUser == null;
    }

    private void switchAct() {
//        finish();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userUID = currentUser.getUid();
            userName = currentUser.getDisplayName();
            userEmail = currentUser.getEmail();
            visitorDB.whereEqualTo("visID", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isComplete()) {
                        Log.v(TAG, task.getResult().size()+"");
                        boolean isCheckedIn = false;
                        for(QueryDocumentSnapshot doc: task.getResult()) {
                            visitor = doc.toObject(Visitor.class);
                            isCheckedIn = visitor.isCheckedIn;
                        }
                        if(isCheckedIn) {
                            Intent intent = new Intent(context, CheckOut.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(context, CheckIn.class);
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(context, CheckIn.class);
                        startActivity(intent);
                    }
                }
            });

        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "sign in failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.v(TAG, "firebaseAuthWithGoogle: " + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.v(TAG, "signInWithCredential:success");
                            updateUI();
                            switchAct();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure " + task.getException());
// Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }
}