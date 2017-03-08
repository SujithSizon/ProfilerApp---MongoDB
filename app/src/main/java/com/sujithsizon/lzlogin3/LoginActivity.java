package com.sujithsizon.lzlogin3;

/**
 * Created by sujith on 10/2/17.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;




public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "zxczxczxczxc" ;
    public String name;
    public String middlename;
    public String surname;
    public String userid;
    public String profilelink;
    public String imageUrl;
    public String emailid;
    public String gender;
    public String birthday;
    ArrayList<String> dset = new ArrayList<String>();
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private SignInButton mGoogleSignInButton;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;




    String scope = "https://www.googleapis.com/auth/userinfo.email+profile";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                nextActivity(newProfile);
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                final Profile profile = Profile.getCurrentProfile();
                nextActivity(profile);

                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("JSON",response.toString());
                                try {

                                    emailid=object.getString("email");
                                    gender=object.getString("gender");
                                    birthday=object.getString("birthday");
                                    name = object.getString("first_name");
                                    surname = object.getString("last_name");

                                    Profile profile2 = Profile.getCurrentProfile();
                                    //userid = profile2.getId();
                                    profilelink = profile2.getLinkUri().toString();
                                    imageUrl = profile2.getProfilePictureUri(200,200).toString();

                                    dset.add(name);
                                    dset.add(surname);
                                    dset.add(profilelink);
                                    dset.add(emailid);
                                    dset.add(gender);
                                    dset.add(birthday);
                                    dset.add("userid not available");
                                    dset.add(imageUrl);


                                    new DownloadFilesTask().execute(dset);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,gender,birthday,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException e) {
            }
        };
        loginButton.setReadPermissions("user_friends");
        loginButton.setReadPermissions("public_profile");
        loginButton.setReadPermissions("email");
        loginButton.setReadPermissions("user_birthday");
        loginButton.registerCallback(callbackManager, callback);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();

        mGoogleSignInButton = (SignInButton)findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.google_sign_in_button: signIn();
                        break;
                }
            }
        });

    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        nextActivity(profile);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    protected void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);


            //if (!mGoogleApiClient.hasConnectedApi(Plus.API)) {
                //Person person  = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                if (acct!= null) {

                    //Log.i(TAG, "About Me: " + person.getAboutMe());
                    //Integer personGender = person.getGender();
                    //String personBirthday = person.getBirthday();
                    //Log.i(TAG, "Current Location: " + person.getCurrentLocation());
                    //Log.i(TAG, "Language: " + person.getLanguage());
                    String personName = acct.getDisplayName();
                    String personEmail = acct.getEmail();
                    String personId = acct.getId();
                    String personPhoto = acct.getPhotoUrl().toString();

                    dset.add(personName);
                    dset.add("last name not available");
                    dset.add("profilelink not available");
                    dset.add(personEmail);
                    dset.add("grender not available");
                    dset.add("bdaynot available");
                    dset.add(personId);
                    dset.add(personPhoto);

                    new DownloadFilesTask().execute(dset);
                } else {Log.e(TAG, "Error!");
                }
            //} else {Log.e(TAG, "Google+ not connected");
            //}

        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void nextActivity(Profile profile){
        if(profile != null){
            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            main.putExtra("name", profile.getName());
            main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
            startActivity(main);
        }
    }

    private class DownloadFilesTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        protected ArrayList<String> doInBackground(ArrayList<String>... passing) {
            ArrayList<String> passed = passing[0];

                String uri = "mongodb://10.0.3.2:27017";
                MongoClientURI mongoClientURI=new MongoClientURI(uri);
                MongoClient mongoClient = new MongoClient(mongoClientURI);
                MongoDatabase database = mongoClient.getDatabase("user9");
                MongoCollection mongoCollection = database.getCollection("lzlo");

                Document doc = new Document("firstname", passed.get(0))
                        .append("surname", passed.get(1))
                        .append("profilelink", passed.get(2))
                        .append("emailid", passed.get(3))
                        .append("Gender", passed.get(4))
                        .append("Birthday", passed.get(5))
                        .append("UserID", passed.get(6))
                        .append("photoUrl", passed.get(7));
                mongoCollection.insertOne(doc);
                mongoClient.close();
            return null;
        }
    }

}