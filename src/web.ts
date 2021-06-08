import { WebPlugin } from '@capacitor/core';

import type { GoogleDrivePlugin } from './definitions';

export class GoogleDriveWeb extends WebPlugin implements GoogleDrivePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  uploadFile() {
    return 'Its Working!';
  }
}
