const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

let isInitialized = false;
let customMessagingTransport = null;

// Support safe external configuration via FIREBASE_SERVICE_ACCOUNT_PATH or GOOGLE_APPLICATION_CREDENTIALS.
// Credential files should live OUTSIDE the repository.
const externalPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
    process.env.GOOGLE_APPLICATION_CREDENTIALS;

// Only fall back to local file if it exists, but never require it in repository.
const localDefaultPath = path.join(__dirname, '../firebase-service-account.json');
const credentialPath = (externalPath && fs.existsSync(externalPath)) ? externalPath
    : (fs.existsSync(localDefaultPath) ? localDefaultPath : null);

try {
    if (admin.apps && admin.apps.length > 0) {
        isInitialized = true;
    } else if (credentialPath) {
        const serviceAccount = JSON.parse(fs.readFileSync(credentialPath, 'utf8'));
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount)
        });
        isInitialized = true;
        console.log('[FCM] Firebase Admin SDK initialized successfully for messaging');
    } else {
        isInitialized = false;
    }
} catch (error) {
    console.warn('[FCM] Firebase messaging initialization error:', error.message);
    isInitialized = false;
}

/**
 * Returns the active Firebase Messaging client or the injected mock transport.
 * In normal runtime with missing credentials, returns null (unconfigured).
 */
function getMessagingClient() {
    if (customMessagingTransport) {
        return customMessagingTransport;
    }
    if (isInitialized && admin.messaging) {
        return admin.messaging();
    }
    return null;
}

/**
 * Injects a custom messaging transport for testing.
 */
function setMockMessagingTransport(transport) {
    customMessagingTransport = transport;
}

module.exports = {
    getMessagingClient,
    setMockMessagingTransport,
    isInitialized: () => isInitialized,
    isConfigured: () => isInitialized || customMessagingTransport !== null,
    isMockMode: () => customMessagingTransport !== null
};
