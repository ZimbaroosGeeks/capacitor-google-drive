export interface GoogleDrivePlugin {
  // echo(options: { value: string }): Promise<{ value: string }>;
  // uploadFileFromDevice(options: {fileName:string,filePath:string,mimeType:string}) : Promise<returnInfo>;
signIn():Promise<any>;
getFolderData():Promise<any>;
uploadFile(options: {filePath:string,mimeType:string}):Promise<any>;
signOut():Promise<any>;
dawnloadFile(options: {fileId:string,fileName:string,fileStorePath:string}):Promise<any>;
deleteFile(options: {fileId:string}):Promise<any>;

}


