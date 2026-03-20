import { session } from 'electron'

const CHROME_UA =
  'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36'

const MASK_ELECTRON_JS = `
  // Hide webdriver flag (Electron sets this to true by default)
  Object.defineProperty(navigator, 'webdriver', {
    get: () => false,
    configurable: true,
  });

  // Mask userAgentData
  if (navigator.userAgentData) {
    Object.defineProperty(navigator, 'userAgentData', {
      get: () => ({
        brands: [
          { brand: "Chromium", version: "146" },
          { brand: "Google Chrome", version: "146" },
          { brand: "Not?A_Brand", version: "99" }
        ],
        mobile: false,
        platform: "macOS",
        getHighEntropyValues: (hints) => Promise.resolve({
          brands: [
            { brand: "Chromium", version: "146.0.0.0" },
            { brand: "Google Chrome", version: "146.0.0.0" },
            { brand: "Not?A_Brand", version: "99.0.0.0" }
          ],
          mobile: false,
          platform: "macOS",
          platformVersion: "15.3.0",
          architecture: "arm",
          model: "",
          uaFullVersion: "146.0.0.0",
          fullVersionList: [
            { brand: "Chromium", version: "146.0.0.0" },
            { brand: "Google Chrome", version: "146.0.0.0" },
            { brand: "Not?A_Brand", version: "99.0.0.0" }
          ]
        })
      })
    });
  }

  // Chrome runtime object
  if (!window.chrome) {
    window.chrome = {};
  }
  if (!window.chrome.runtime) {
    window.chrome.runtime = {};
  }

  // Chrome-like Permissions API behavior
  if (navigator.permissions) {
    const origQuery = navigator.permissions.query.bind(navigator.permissions);
    navigator.permissions.query = (params) => {
      if (params.name === 'notifications') {
        return Promise.resolve({ state: Notification.permission, onchange: null });
      }
      return origQuery(params);
    };
  }

  // Plugins
  Object.defineProperty(navigator, 'plugins', {
    get: () => [
      { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer' },
      { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai' },
      { name: 'Native Client', filename: 'internal-nacl-plugin' }
    ]
  });

  // Languages (ensure consistent)
  Object.defineProperty(navigator, 'languages', {
    get: () => ['en-US', 'en'],
  });

  // Hide Electron-specific globals
  delete window.process;
  delete window.require;
  delete window.module;
  delete window.exports;
  delete window.__dirname;
  delete window.__filename;
`

export function getOrCreateSession(accountId: string): Electron.Session {
  const partition = `persist:account-${accountId}`
  const ses = session.fromPartition(partition)
  setupSession(ses)
  return ses
}

function setupSession(ses: Electron.Session): void {
  ses.setUserAgent(CHROME_UA)

  ses.webRequest.onBeforeSendHeaders((details, callback) => {
    const headers = { ...details.requestHeaders }
    if (headers['Sec-CH-UA'] || headers['sec-ch-ua']) {
      const chromeUA = '"Chromium";v="146", "Google Chrome";v="146", "Not?A_Brand";v="99"'
      headers['Sec-CH-UA'] = chromeUA
      headers['sec-ch-ua'] = chromeUA
    }
    callback({ requestHeaders: headers })
  })
}

export function getMaskElectronJs(): string {
  return MASK_ELECTRON_JS
}
