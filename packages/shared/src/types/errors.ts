export type AppError =
  | { type: 'WebViewLoadFailed'; url: string; errorCode: number; description: string }
  | { type: 'DatabaseError'; message: string }
  | { type: 'SessionRestoreFailed'; message: string }
  | { type: 'UpdateFailed'; message: string }
