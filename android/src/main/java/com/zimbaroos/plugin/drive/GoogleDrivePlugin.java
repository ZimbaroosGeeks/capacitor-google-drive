package com.zimbaroos.plugin.drive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

@CapacitorPlugin(name = "GoogleDrive")
public class GoogleDrivePlugin extends Plugin {
    private static final String TAG = "drive-quickstart";
    private GoogleSignInClient mGoogleSignInClient;
    private DriveServiceHelper driveServiceHelper;

    private GoogleDrive implementation = new GoogleDrive();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }
    @PluginMethod
    public void uploadFile(PluginCall call){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity() , gso);
        SignIn(call);
    }
    @PluginMethod
    private void SignIn(PluginCall call) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(call, signInIntent , "pickSignInResult");
    }

    @ActivityCallback
    private void  pickSignInResult(PluginCall call, ActivityResult result){
        Log.i(TAG,result.toString());
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        handleSignInResult(task);
    }


    @PluginMethod
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "Sign in successfully ");
            Log.i(TAG, "Sign in successfully "+account.getEmail()+account.getRequestedScopes());
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            Drive googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("DriveExample")
                    .build();
            driveServiceHelper = new DriveServiceHelper(googleDriveService);
            createFile();

            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }
    @PluginMethod
    public void createFile() {
        try {
            Log.i(TAG,"I am here");
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading to google drive");
            progressDialog.setMessage("PLeas Wait");
            progressDialog.show();
            String filePath = "/storage/emulated/0/Downloads/mypdf.pdf";

//        File ff = Environment.getExternalStorageDirectory(Environment.DIRECTORY_DOWNLOADS);
//        String filePath = Environment.DIRECTORY_DOWNLOADS + "/mypdf.pdf";

            driveServiceHelper.createFile().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Uploaded Successfully", Toast.LENGTH_LONG).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.i(TAG,e.toString());
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        catch (Exception ex){

        }
    }

}
