import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import {
  type ElectronApplication,
  _electron as electron,
  expect,
  type Page,
  test,
} from '@playwright/test'

let app: ElectronApplication
let page: Page
let userDataDir: string

test.beforeAll(async () => {
  // Use a fresh temp directory so the app always starts in first-run state
  userDataDir = fs.mkdtempSync(path.join(os.tmpdir(), 'uniso-e2e-'))

  app = await electron.launch({
    args: [
      '--no-sandbox',
      path.join(__dirname, '../out/main/index.js'),
      `--user-data-dir=${userDataDir}`,
    ],
    env: {
      ...process.env,
      NODE_ENV: 'test',
    },
  })

  page = await app.firstWindow()
  await page.waitForLoadState('domcontentloaded')
})

test.afterAll(async () => {
  await app?.close()
  // Clean up temp directory
  if (userDataDir) {
    fs.rmSync(userDataDir, { recursive: true, force: true })
  }
})

test.describe('Initial launch flow', () => {
  test('shows telemetry consent dialog on first run', async () => {
    // First-run flow should show telemetry consent
    await expect(page.locator('text=Data Collection')).toBeVisible({ timeout: 10_000 })
  })

  test('proceeds to tutorial after telemetry choice', async () => {
    // Deny telemetry to proceed
    await page.locator('button', { hasText: "Don't allow" }).click()

    // Tutorial welcome screen should appear
    await expect(page.locator('text=Welcome to Uniso')).toBeVisible({ timeout: 5_000 })
  })

  test('can navigate through tutorial steps', async () => {
    // Step 1: Welcome → click Next
    await page.locator('button', { hasText: 'Next' }).click()
    await expect(page.locator('text=Add an Account')).toBeVisible()

    // Step 2: Add Account → click Next
    await page.locator('button', { hasText: 'Next' }).click()
    await expect(page.locator('text=Switch Accounts')).toBeVisible()

    // Step 3: Switch Accounts → click Next
    await page.locator('button', { hasText: 'Next' }).click()
    await expect(page.locator("text=You're All Set!")).toBeVisible()
  })

  test('completes tutorial and shows main screen', async () => {
    // Click "Get Started" on final step
    await page.locator('button', { hasText: 'Get Started' }).click()

    // Main screen should show empty state placeholder
    await expect(page.locator('text=No account selected')).toBeVisible({ timeout: 5_000 })
  })
})

test.describe('Main screen', () => {
  test('sidebar is visible', async () => {
    await expect(page.locator('button', { hasText: '+' })).toBeVisible()
  })

  test('add account dialog opens and shows all services', async () => {
    await page.locator('button', { hasText: '+' }).click()

    // Dialog title and subtitle
    await expect(page.locator('text=Add Account')).toBeVisible({ timeout: 5_000 })
    await expect(page.locator('text=Select a service')).toBeVisible()

    // All 6 supported services should be listed
    const expectedServices = ['X', 'Instagram', 'Facebook', 'YouTube', 'Bluesky', 'Twitch']
    for (const name of expectedServices) {
      await expect(page.locator(`text=${name}`)).toBeVisible()
    }

    // Close dialog by clicking overlay background
    await page.mouse.click(1, 1)
    await expect(page.locator('text=Add Account')).not.toBeVisible({ timeout: 3_000 })
  })
})

test.describe('Settings screen', () => {
  test('opens settings via gear button', async () => {
    // The settings button is an SVG gear icon button in the sidebar bottom area
    await page.locator('button svg circle').first().click()

    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible({ timeout: 5_000 })
  })

  test('displays all sections', async () => {
    await expect(page.locator('h2', { hasText: 'General' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'Privacy' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'Keyboard Shortcuts' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'Application Info' })).toBeVisible()
  })

  test('shows language selector with correct options', async () => {
    const select = page.locator('select')
    await expect(select).toBeVisible()

    // Verify both language options exist
    await expect(select.locator('option[value="en"]')).toHaveText('English')
    await expect(select.locator('option[value="ja"]')).toHaveText('日本語')
  })

  test('shows telemetry toggle', async () => {
    const toggle = page.locator('[role="switch"]')
    await expect(toggle).toBeVisible()
    // Telemetry was denied in initial flow, so should be off
    await expect(toggle).toHaveAttribute('aria-checked', 'false')
  })

  test('can toggle telemetry on and off', async () => {
    const toggle = page.locator('[role="switch"]')

    // Turn on
    await toggle.click()
    await expect(toggle).toHaveAttribute('aria-checked', 'true')

    // Turn back off
    await toggle.click()
    await expect(toggle).toHaveAttribute('aria-checked', 'false')
  })

  test('shows keyboard shortcuts', async () => {
    await expect(page.locator('kbd', { hasText: 'Ctrl+Tab' })).toBeVisible()
    await expect(page.locator('kbd', { hasText: 'Ctrl+Shift+Tab' })).toBeVisible()
    await expect(page.locator('kbd', { hasText: 'Ctrl+R' })).toBeVisible()
    await expect(page.locator('kbd', { hasText: 'Ctrl+Shift+R' })).toBeVisible()
    await expect(page.locator('kbd', { hasText: 'Ctrl+N' })).toBeVisible()
    await expect(page.locator('kbd', { hasText: 'Ctrl+,' })).toBeVisible()
  })

  test('shows version info', async () => {
    const version = await app.evaluate(({ app: electronApp }) => electronApp.getVersion())
    await expect(page.locator(`text=${version}`)).toBeVisible()
  })

  test('show tutorial button re-opens tutorial', async () => {
    await page.locator('button', { hasText: 'Show Tutorial Again' }).click()

    // Tutorial should reappear
    await expect(page.locator('text=Welcome to Uniso')).toBeVisible({ timeout: 5_000 })

    // Complete tutorial to return to main screen
    await page.locator('button', { hasText: 'Next' }).click()
    await page.locator('button', { hasText: 'Next' }).click()
    await page.locator('button', { hasText: 'Next' }).click()
    await page.locator('button', { hasText: 'Get Started' }).click()

    await expect(page.locator('text=No account selected')).toBeVisible({ timeout: 5_000 })
  })

  test('closes settings via close button', async () => {
    // Re-open settings
    await page.locator('button svg circle').first().click()
    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible({ timeout: 5_000 })

    // Close
    await page.locator('button', { hasText: 'Close' }).click()
    await expect(page.locator('h1', { hasText: 'Settings' })).not.toBeVisible({ timeout: 3_000 })
  })
})

test.describe('Internationalization', () => {
  test('switching language to Japanese updates UI text', async () => {
    // Open settings
    await page.locator('button svg circle').first().click()
    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible({ timeout: 5_000 })

    // Change language to Japanese
    await page.locator('select').selectOption('ja')

    // Settings title should now be in Japanese
    await expect(page.locator('h1', { hasText: '設定' })).toBeVisible({ timeout: 5_000 })
    await expect(page.locator('h2', { hasText: '一般' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'プライバシー' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'キーボードショートカット' })).toBeVisible()
    await expect(page.locator('h2', { hasText: 'アプリケーション情報' })).toBeVisible()
  })

  test('switching back to English restores UI text', async () => {
    await page.locator('select').selectOption('en')

    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible({ timeout: 5_000 })
    await expect(page.locator('h2', { hasText: 'General' })).toBeVisible()

    // Close settings
    await page.locator('button', { hasText: 'Close' }).click()
  })
})

test.describe('Keyboard shortcuts', () => {
  // Electron globalShortcut is OS-level and cannot be triggered by
  // Playwright keyboard events. Send the IPC message directly instead.

  test('Ctrl+N opens add account dialog', async () => {
    await app.evaluate(({ webContents }) => {
      for (const wc of webContents.getAllWebContents()) {
        wc.send('shortcut-add-account')
      }
    })

    await expect(page.locator('text=Add Account')).toBeVisible({ timeout: 5_000 })
    await expect(page.locator('text=Select a service')).toBeVisible()

    // Close dialog by clicking overlay background
    await page.mouse.click(1, 1)
    await expect(page.locator('text=Select a service')).not.toBeVisible({ timeout: 3_000 })
  })

  test('Ctrl+, opens settings', async () => {
    await app.evaluate(({ webContents }) => {
      for (const wc of webContents.getAllWebContents()) {
        wc.send('shortcut-settings')
      }
    })

    await expect(page.locator('h1', { hasText: 'Settings' })).toBeVisible({ timeout: 5_000 })

    // Close
    await page.locator('button', { hasText: 'Close' }).click()
    await expect(page.locator('h1', { hasText: 'Settings' })).not.toBeVisible({ timeout: 3_000 })
  })
})
