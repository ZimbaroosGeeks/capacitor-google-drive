export interface GoogleDrivePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  uploadFile():any;
}
