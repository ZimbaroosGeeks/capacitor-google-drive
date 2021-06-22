package com.zimbaroos.plugin.drive;


import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//import static com.zimbaroos.plugin.drive.GoogleDrivePlugin.folderId;

public class GoogleDriveServiceHelper {

    private static final String TAG = "GoogleDriveService";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public GoogleDriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }


    /**
     * Get all the file present in the user's  Drive appDataFolder.
     */
    public Task<FileList> getFolderFileList() {

        return Tasks.call(mExecutor, () -> {
            FileList result = mDriveService.files().list()
//                    .setQ("mimeType = '" + SHEET_MIME_TYPE + "' and trashed=false and parents = '" + folderId + "' ")
                    .setSpaces("appDataFolder")
                    .execute();

            Log.e(TAG, "getFolderFileList: folderFiles: "+result );
            return result;
        });
    }


    /**
     * Upload the file to the user's Drive appDataFolder.
     */
    public Task<String> uploadFileToGoogleDrive(String path,String mimeType) {

        return Tasks.call(mExecutor, () -> {

            Log.e(TAG, "uploadFileToGoogleDrive: path: "+path );
            java.io.File filePath = new java.io.File(path);

            File fileMetadata = new File();
            fileMetadata.setName(filePath.getName());
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));
            fileMetadata.setMimeType(mimeType);

            FileContent mediaContent = new FileContent(mimeType, filePath);
            File file = mDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());

            return file.getId();
        });
    }

    /**
     * Download file from the user's Drive
     */
    public Task<Boolean> downloadFile(final java.io.File fileSaveLocation, final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                OutputStream outputStream = new FileOutputStream(fileSaveLocation);
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return true;
            }
        });
    }

    /**
     * delete file from the user's Drive
     */
    public Task<Boolean> deleteFolderFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                if (fileId != null) {
                    mDriveService.files().delete(fileId).execute();
                    return true;
                }
                return false;
            }
        });
    }

}
