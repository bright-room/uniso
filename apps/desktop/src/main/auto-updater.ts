import { ipcMain } from 'electron'
import log from 'electron-log'
import { autoUpdater, type UpdateInfo } from 'electron-updater'

autoUpdater.logger = log
autoUpdater.autoDownload = false
autoUpdater.autoInstallOnAppQuit = true

let mainSidebarWebContents: Electron.WebContents | null = null

export function initAutoUpdater(sidebarWebContents: Electron.WebContents): void {
  mainSidebarWebContents = sidebarWebContents

  autoUpdater.on('update-available', (info: UpdateInfo) => {
    mainSidebarWebContents?.send('update-available', {
      version: info.version,
      releaseNotes: info.releaseNotes,
    })
  })

  autoUpdater.on('update-not-available', () => {
    mainSidebarWebContents?.send('update-not-available')
  })

  autoUpdater.on('download-progress', (progress) => {
    mainSidebarWebContents?.send('update-download-progress', {
      percent: progress.percent,
    })
  })

  autoUpdater.on('update-downloaded', () => {
    mainSidebarWebContents?.send('update-downloaded')
  })

  autoUpdater.on('error', (err) => {
    log.error('Auto-updater error:', err)
    mainSidebarWebContents?.send('update-error', err.message)
  })

  // IPC handlers
  ipcMain.handle('check-for-updates', async () => {
    try {
      const result = await autoUpdater.checkForUpdates()
      return result?.updateInfo ?? null
    } catch (err) {
      log.error('Check for updates failed:', err)
      return null
    }
  })

  ipcMain.handle('download-update', async () => {
    try {
      await autoUpdater.downloadUpdate()
    } catch (err) {
      log.error('Download update failed:', err)
    }
  })

  ipcMain.handle('install-update', () => {
    autoUpdater.quitAndInstall()
  })
}

export function checkForUpdatesInBackground(): void {
  autoUpdater.checkForUpdates().catch((err) => {
    log.error('Background update check failed:', err)
  })
}
