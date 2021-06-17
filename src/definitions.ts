export interface GoogleDrivePlugin {
  // echo(options: { value: string }): Promise<{ value: string }>;
  // uploadFileFromDevice(options: {fileName:string,filePath:string,mimeType:string}) : Promise<returnInfo>;
signIn():any;
createFolder():any;
getFolderData():any;
uploadFile(options: {filePath:string,mimeType:string}):Promise<any>;
signOut():any;
dawnloadFile(options: {fileId:string,fileName:string,fileStorePath:string}):Promise<any>;

}

export interface returnInfo {
      id:string,
      kind:string,
      mimeType:string,
      name:string,
}
