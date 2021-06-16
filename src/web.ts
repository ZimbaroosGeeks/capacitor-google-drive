import { WebPlugin } from '@capacitor/core';

import type { GoogleDrivePlugin } from './definitions';

export class GoogleDriveWeb extends WebPlugin implements GoogleDrivePlugin {

  async signIn():Promise<any>{
  }

  async createFolder():Promise<any>{
  }
  async getFolderData():Promise<any>{
  }
  async uploadFile():Promise<any>{
  }
  async signOut():Promise<any>{
  }
  async dawnloadFile(options: { fileId:string,fileName:string }): Promise<any> {
    console.log('ECHO', options);
    return options;
  }



 
}
