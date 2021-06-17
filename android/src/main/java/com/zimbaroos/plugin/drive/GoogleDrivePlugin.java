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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;


@CapacitorPlugin(name = "GoogleDrive")
public class GoogleDrivePlugin extends Plugin {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_FILE_REQUEST = 100;

    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";
    JSObject ret = new JSObject();




    GoogleSignInClient googleSignInClient;
    LoadToast loadToast;


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
//
//  /**
//   * Showing Alert Dialog with Settings option
//   * Navigates user to app settings
//   * NOTE: Keep proper title and message depending on your app
//   */
//  private void showSettingsDialog() {
//    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//    builder.setTitle("Need Permissions");
//    builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
//    builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialog, int which) {
//        dialog.cancel();
//        openSettings();
//      }
//    });
//    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialog, int which) {
//        dialog.cancel();
//      }
//    });
//    builder.show();
//
//  }
//
//  // navigating user to app settings
//  private void openSettings() {
//    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//    Uri uri = Uri.fromParts("package", getPackageName(), null);
//    intent.setData(uri);
//    startActivityForResult(intent, 101);
//  }


    @ActivityCallback
    public void onSignIn(PluginCall call,ActivityResult result) {
        Log.i("Sign In", result.getData().toString());
        handleSignInResult(result.getData());

    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
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

                    showMessage("Sign-In Success");
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                        showMessage("Unable to sign in.");
                    }
                });
    }

    // This method will get call when user click on sign-in button
    @PluginMethod
    public void signIn(PluginCall call) {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(call, googleSignInClient.getSignInIntent(), "onSignIn");
    }


    @PluginMethod
    // This method will get call when user click on create folder button
    public void createFolder(PluginCall call) {
        if (mDriveServiceHelper != null) {

            // check folder present or not
            mDriveServiceHelper.isFolderPresent()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String id) {
                            if (id.isEmpty()){
                                mDriveServiceHelper.createFolder()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String fileId) {
                                                Log.e(TAG, "folder id: "+fileId );
                                                folderId=fileId;
                                                showMessage("Folder Created with id: "+fileId);
                                                ret.put("folder" , fileId);
                                                call.resolve(ret);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "Couldn't create file.", exception);
                                                call.reject(exception.getLocalizedMessage());
                                            }
                                        });
                            }else {
                                folderId=id;
                                showMessage("Folder already present");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            showMessage("Couldn't create file..");
                            Log.e(TAG, "Couldn't create file..", exception);
                        }
                    });
        }
    }
    @PluginMethod
    // This method will get call when user click on folder data button
    public void getFolderData(PluginCall call) {
        if (mDriveServiceHelper != null) {

            mDriveServiceHelper.getFolderFileList()
                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                        @Override
                        public void onSuccess(FileList fileList) {
                            showMessage("Success");
                            Log.e(TAG, "onSuccess: result: "+fileList );
                            ret.put("result", fileList);
                            call.resolve(ret);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage("Fail");
                            Log.e(TAG, "onfail: err: "+e.toString() );
                            call.reject(e.getLocalizedMessage());
                        }
                    });
        }
    }
    @PluginMethod
    // This method will get call when user click on upload file button
    public void uploadFile(PluginCall call) {
                    String filePath = call.getString("filePath");
                    String mimeType = call.getString("mimeType");
        // Get the Uri of the selected file
        String selectedFilePath = "/storage/emulated/0/Movies/ScreenRecord/20201217120339.mp4";


        if(selectedFilePath != null && !selectedFilePath.equals("")){
            if (mDriveServiceHelper != null) {
                requestForStoragePermission(call);
                mDriveServiceHelper.uploadFileToGoogleDrive(filePath,mimeType)
                        .addOnSuccessListener(new OnSuccessListener<com.google.api.services.drive.model.File>() {
                            @Override
                            public void onSuccess(com.google.api.services.drive.model.File result) {
                                showMessage("File uploaded ...!!");
                                ret.put("File",result);
                                call.resolve(ret);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showMessage("Couldn't able to upload file, error: "+e);
                                call.reject(e.getLocalizedMessage());
                            }
                        });
            }
        }else{
            Toast.makeText(getContext(),"Cannot upload file to server",Toast.LENGTH_SHORT).show();
        }
    }
    @PluginMethod
    // This method will get call when user click on sign-out button
    public void signOut(PluginCall call) {
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            showMessage("Sign-Out");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            showMessage("Unable to sign out.");
                            Log.e(TAG, "Unable to sign out.", exception);
                            call.reject(exception.getLocalizedMessage());
                        }
                    });
        }
    }
    @PluginMethod
    public void dawnloadFile(PluginCall call){
        requestForStoragePermission(call);
//        String fileStorePath = "/storage/emulated/0/Example_Download";
        String fileName = call.getString("fileName");
        String fileId = call.getString("fileId");
        String fileStorePath = call.getString("dawnloadPath");
        mDriveServiceHelper.downloadFile(new java.io.File(fileStorePath, fileName), fileId)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {

                        if (result)
                            showMessage("Successfully downloaded file ...!!");
                        else
                            showMessage("Not Able to downloaded file ...!!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "onFailure: error: "+e.getMessage());
                        showMessage("Got error while downloading file.");
                        call.reject(e.getLocalizedMessage());
                    }
                });

    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(getContext(),message, Toast.LENGTH_SHORT).show();
    }
}
