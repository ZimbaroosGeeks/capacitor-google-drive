import { WebPlugin } from '@capacitor/core';

import type { GoogleDrivePlugin } from './definitions';

export class GoogleDriveWeb extends WebPlugin implements GoogleDrivePlugin {

  async signIn():Promise<any>{
  }
  async getFolderData():Promise<any>{
  }
  async uploadFile(options:{filePath:string,mimeType:string}):Promise<any>{
    console.log('ECHO', options);
    return options;
  }
  async signOut():Promise<any>{
  }
  async dawnloadFile(options:{ fileId:string,fileName:string,fileStorePath:string}): Promise<any> {
    console.log('ECHO', options);
    return options;
  }
  async deleteFile(options:{ fileId:string}): Promise<any> {
    console.log('ECHO', options);
    return options;
  }



 
}
