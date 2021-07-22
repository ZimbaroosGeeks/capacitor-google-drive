package com.zimbaroos.plugin.drive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;


@CapacitorPlugin(name = "GoogleDrive")
public class GoogleDrivePlugin extends Plugin {
    private static final String TAG = "MainActivity";
//
//    private static final int REQUEST_CODE_SIGN_IN = 1;
//    private static final int PICK_FILE_REQUEST = 100;

    static GoogleDriveServiceHelper mDriveServiceHelper;
    //static String folderId="";
//    JSObject ret = new JSObject();
    GoogleSignInClient googleSignInClient;


    //     Read/Write permission
    private void requestForStoragePermission(PluginCall call) {
        Dexter.withContext(getContext())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            signIn(call);
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
//                  showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    @ActivityCallback
    public void onSignIn(PluginCall call,ActivityResult result) {
        Log.i("Sign In", result.getData().toString());
        handleSignInResult(call, result.getData());
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     */
    private void handleSignInResult(PluginCall call, Intent result) {
        try {
            GoogleSignIn.getSignedInAccountFromIntent(result)
                    .addOnSuccessListener(googleAccount -> {
                        try {
                            Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                            // Use the authenticated account to sign in to the Drive service.
                            GoogleAccountCredential credential =
                                    GoogleAccountCredential.usingOAuth2(
                                            getContext(), Collections.singleton(DriveScopes.DRIVE_APPDATA));
                            credential.setSelectedAccount(googleAccount.getAccount());
                            Drive googleDriveService =
                                    new Drive.Builder(
                                            AndroidHttp.newCompatibleTransport(),
                                            new GsonFactory(),
                                            credential)
                                            .setApplicationName("Drive API Migration")
                                            .build();

                            // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                            // Its instantiation is required before handling any onClick actions.
                            mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService);
                            Log.i("SignIn","SignIn Successfull");
                            JSObject response = new JSObject();
                            response.put("isSignIn", true);
                            response.put("SignIn Account", googleAccount.getEmail());
                            call.resolve(response);
                        } catch (Exception e) {
                            call.reject(e.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Unable to sign in.", exception);
                            call.reject(exception.toString());
                        }
                    });
        } catch (Exception e) {
            call.reject(e.toString());
        }
    }

    // This method will get call when user click on sign-in button
    @PluginMethod
    public void signIn(PluginCall call) {
        Log.d(TAG, "Requesting sign-in");
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                        .requestScopes(new Scope(DriveScopes.DRIVE_READONLY))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(call, googleSignInClient.getSignInIntent(), "onSignIn");
    }

    @PluginMethod
    public void getFolderData(PluginCall call) {
        JSObject ret = new JSObject();
        if (mDriveServiceHelper != null) {

            mDriveServiceHelper.getFolderFileList()
                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                        @Override
                        public void onSuccess(FileList fileList) {
                            Log.e(TAG, "onSuccess: result: "+fileList );
                            ret.put("result", fileList);
                            call.resolve(ret);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onfail: err: "+e.toString() );
                            call.reject(e.toString());
                        }
                    });
        }
    }
    @PluginMethod
    // This method will get call when user click on upload file button
    public void uploadFile(PluginCall call) {
        String filePath = call.getString("filePath");
        String mimeType = call.getString("mimeType");
        JSObject ret = new JSObject();
        if(filePath != null && !filePath.equals("")){
            if (mDriveServiceHelper != null) {
                requestForStoragePermission(call);
                mDriveServiceHelper.uploadFileToGoogleDrive(filePath,mimeType)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String result) {
                                Log.i("File","File uploaded Successfully");
                                Log.i("res",result);
                                ret.put("File",result);
                                call.resolve(ret);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Fail",e.toString());
                                call.reject(e.toString());
                            }
                        });
            }
        }else{
//            Toast.makeText(getContext(),"Cannot upload file to server",Toast.LENGTH_SHORT).show();
            Log.i("Fail","Cannot upload file to server");
            call.reject("Fail to upload file");
        }
    }
    @PluginMethod
    // This method will get call when user click on sign-out button
    public void signOut(PluginCall call) {
        JSObject ret = new JSObject();
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Log.i("Sign out","Sign out Successfully");
                            ret.put("Sign Out",true);
                            call.resolve(ret);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Unable to sign out.", exception);
                            call.reject(exception.getLocalizedMessage());
                        }
                    });
        }
    }
    @PluginMethod
    public void dawnloadFile(PluginCall call) {
        JSObject ret = new JSObject();
        requestForStoragePermission(call);
//        String fileStorePath = "/storage/emulated/0/Example_Download";
        String fileName = call.getString("fileName");
        String fileId = call.getString("fileId");
        String fileStorePath = call.getString("fileStorePath");
//I will create a folder if not exist
        File file = new File(fileStorePath);
        if (!file.exists()) {
            file.mkdir();
        }

        if (mDriveServiceHelper != null) {
            mDriveServiceHelper.downloadFile(new java.io.File(fileStorePath, fileName), fileId)
                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {

                            if (result) {
                                Log.i("File", "Successfully downloaded file ...!!");
                                ret.put("file downloaded successfully",result);
                                call.resolve(ret);
                            }

                            else {
                                Log.i("File", "Not Able to downloaded file ...!!");
                                call.reject("Not Able to downloaded file ...!!");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "onFailure: error: " + e.getMessage());
                            call.reject(e.toString());
                        }
                    });

        }
    }
    

    @PluginMethod
    public void deleteFile(PluginCall call){
        JSObject ret = new JSObject();
        requestForStoragePermission(call);
//        String fileStorePath = "/storage/emulated/0/Example_Download";
        String fileId = call.getString("fileId");
        if(mDriveServiceHelper != null) {
            mDriveServiceHelper.deleteFolderFile(fileId).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    ret.put("result", aBoolean);
                    call.resolve(ret);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            call.reject(e.toString());
                        }
                    });
        }

    }
    
}
