package com.ntnu.imt3673.imt3673_lab4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * Authentication - Handles all user authentication using Firebase Auth UI.
 */
public class Authentication {

    private MainActivity           activity;
    private boolean                anonymous;
    private List<AuthUI.IdpConfig> providers;

    /**
     * Authentication
     * @param context Current activity context
     */
    public Authentication(final Context context) {
        this.activity = (MainActivity)context;

        // A list of supported authentication providers
        this.providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.EmailBuilder().build()
        );
    }

    /**
     * Send the user to the main screen if authentication was successful,
     * otherwise send the user to the login screen.
     */
    public void authenticate(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.LOGIN_INTENT) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == Activity.RESULT_OK) {
                this.activity.updateUI(this.getUser());
            } else {
                Toast.makeText(this.activity, R.string.error_login, Toast.LENGTH_LONG).show();
                Log.w("LAB4_AUTHENTICATION_FAILED", response.getError());
                this.activity.updateUI(null);
            }
        }
    }

    /**
     * Returns the currently authenticated user.
     */
    public FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Try to sign the user in using Firebase Auth UI.
     * Starts a new activity and returns with the results.
     */
    public void login() {
        this.anonymous = false;

        // Create and start the login intent using Firebase Auth UI
        this.activity.startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(this.providers).build(),
            Constants.LOGIN_INTENT
        );
    }

    /**
     * Try to sign an anonymous user in using Firebase Auth.
     */
    public void loginAnonymously() {
        this.anonymous = true;

        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(
            this.activity,
            (@NonNull Task<AuthResult> task) -> {
                if (task.isSuccessful()) {
                    activity.updateUI(this.getUser());
                } else {
                    Toast.makeText(this.activity, R.string.error_login, Toast.LENGTH_LONG).show();
                    Log.w("LAB4_LOGIN_ANONYMOUSLY_FAILED", task.getException());
                    activity.updateUI(null);
                }
            }
        );
    }

    /**
     * Sign the user out and return to the login screen.
     */
    public void logout() {
        if (this.anonymous) {
            FirebaseAuth.getInstance().signOut();
            activity.updateUI(null);
        } else {
            AuthUI.getInstance()
            .signOut(this.activity)
            .addOnCompleteListener((@NonNull Task<Void> task) -> {
                activity.updateUI(null);
            });
        }
    }

}
