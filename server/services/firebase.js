const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

let isInitialized = false;
let isMockMode = true;

const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
    path.join(__dirname, '../firebase-service-account.json');

try {
    if (admin.apps && admin.apps.length > 0) {
        isInitialized = true;
        isMockMode = false;
    } else if (fs.existsSync(serviceAccountPath)) {
        const serviceAccount = require(serviceAccountPath);
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount)
        });
        isInitialized = true;
        isMockMode = false;
        console.log('[Firebase] Admin SDK initialized successfully with service account');
    } else {
        console.warn('[Firebase] firebase-service-account.json not found; running in development mock mode');
        isMockMode = true;
    }
} catch (error) {
    console.warn('[Firebase] Initialization error:', error.message);
    isMockMode = true;
}

const auth = isInitialized && !isMockMode ? admin.auth() : null;
const firestore = isInitialized && !isMockMode ? admin.firestore() : null;
const messaging = isInitialized && !isMockMode ? admin.messaging() : null;

module.exports = {
    admin,
    auth,
    firestore,
    messaging,
    isInitialized,
    isMockMode
};
