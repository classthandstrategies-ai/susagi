const { firestore } = require('./firebase');

/**
 * Checks whether trustedUid is an authorized, enabled trusted contact of protectedUid.
 *
 * Rules:
 * - self-verification forbidden (protectedUid === trustedUid -> false)
 * - arbitrary UID targeting forbidden
 * - missing or disabled relationship -> false
 * - canonical Firestore path: users/{protectedUid}/trustedContacts/{trustedUid}
 */
async function isTrustedContact({ protectedUid, trustedUid, db = firestore }) {
    if (!protectedUid || !trustedUid) {
        return false;
    }

    const cleanProtected = String(protectedUid).trim();
    const cleanTrusted = String(trustedUid).trim();

    // Self-verification forbidden
    if (cleanProtected === cleanTrusted) {
        return false;
    }

    if (!db) {
        return false;
    }

    try {
        const contactRef = db
            .collection('users')
            .doc(cleanProtected)
            .collection('trustedContacts')
            .doc(cleanTrusted);

        const snapshot = await contactRef.get();
        if (!snapshot.exists) {
            return false;
        }

        const data = snapshot.data();
        return Boolean(data && data.enabled === true);
    } catch (err) {
        console.error('[TrustedContacts] Error checking relationship:', err.message);
        return false;
    }
}

/**
 * Retrieves the trusted contact relationship document if it exists.
 */
async function getTrustedContact({ protectedUid, trustedUid, db = firestore }) {
    if (!protectedUid || !trustedUid || !db) return null;

    const cleanProtected = String(protectedUid).trim();
    const cleanTrusted = String(trustedUid).trim();

    if (cleanProtected === cleanTrusted) return null;

    try {
        const contactRef = db
            .collection('users')
            .doc(cleanProtected)
            .collection('trustedContacts')
            .doc(cleanTrusted);

        const snapshot = await contactRef.get();
        if (!snapshot.exists) return null;
        return snapshot.data();
    } catch (err) {
        console.error('[TrustedContacts] Error reading contact:', err.message);
        return null;
    }
}

/**
 * Helper to record or update a trusted contact relationship (for administration or tests).
 * Canonical fields:
 *   trustedUserId, displayName, relationship, enabled, createdAt, updatedAt
 */
async function setTrustedContact({
    protectedUid,
    trustedUid,
    displayName = '',
    relationship = 'Family',
    enabled = true,
    db = firestore
}) {
    if (!protectedUid || !trustedUid) {
        throw new Error('Both protectedUid and trustedUid are required');
    }

    const cleanProtected = String(protectedUid).trim();
    const cleanTrusted = String(trustedUid).trim();

    if (cleanProtected === cleanTrusted) {
        const error = new Error('Self-verification is forbidden: user cannot be their own trusted contact');
        error.statusCode = 400;
        throw error;
    }

    if (!db) {
        const error = new Error('Database service unavailable: Firestore is not configured');
        error.statusCode = 503;
        throw error;
    }

    const contactRef = db
        .collection('users')
        .doc(cleanProtected)
        .collection('trustedContacts')
        .doc(cleanTrusted);

    const existingDoc = await contactRef.get();
    const now = Date.now();
    let createdAt = now;

    if (existingDoc.exists) {
        const data = existingDoc.data();
        if (data && typeof data.createdAt === 'number') {
            createdAt = data.createdAt;
        }
    }

    const record = {
        trustedUserId: cleanTrusted,
        displayName: String(displayName).trim(),
        relationship: String(relationship).trim(),
        enabled: Boolean(enabled),
        createdAt,
        updatedAt: now
    };

    await contactRef.set(record, { merge: true });
    return record;
}

module.exports = {
    isTrustedContact,
    getTrustedContact,
    setTrustedContact
};
