export interface GoogleDrivePlugin {
  // echo(options: { value: string }): Promise<{ value: string }>;
  // uploadFileFromDevice(options: {fileName:string,filePath:string,mimeType:string}) : Promise<returnInfo>;
signIn():any;
getFolderData():any;
uploadFile(options: {filePath:string,mimeType:string}):Promise<any>;
signOut():any;
dawnloadFile(options: {fileId:string,fileName:string,fileStorePath:string}):Promise<any>;
deleteFile(options: {fileId:string}):Promise<any>;

}


