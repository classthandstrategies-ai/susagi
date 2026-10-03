const { getAdminClient } = require('./supabase');

/**
 * Checks whether trustedUid is an authorized, enabled trusted contact of protectedUid.
 *
 * Rules:
 * - self-verification forbidden (protectedUid === trustedUid -> false)
 * - arbitrary UID targeting forbidden
 * - missing or disabled relationship -> false
 * - canonical table: trusted_contacts (protected_user_id, trusted_user_id)
 */
async function isTrustedContact({ protectedUid, trustedUid, db = getAdminClient() }) {
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
        if (typeof db.from === 'function') {
            const { data, error } = await db
                .from('trusted_contacts')
                .select('enabled')
                .eq('protected_user_id', cleanProtected)
                .eq('trusted_user_id', cleanTrusted)
                .maybeSingle();

            if (error || !data) {
                return false;
            }

            return Boolean(data.enabled === true);
        }
        return false;
    } catch (err) {
        console.error('[TrustedContacts] Error checking relationship:', err.message);
        return false;
    }
}

/**
 * Retrieves the trusted contact relationship document if it exists.
 */
async function getTrustedContact({ protectedUid, trustedUid, db = getAdminClient() }) {
    if (!protectedUid || !trustedUid || !db) return null;

    const cleanProtected = String(protectedUid).trim();
    const cleanTrusted = String(trustedUid).trim();

    if (cleanProtected === cleanTrusted) return null;

    try {
        if (typeof db.from === 'function') {
            const { data, error } = await db
                .from('trusted_contacts')
                .select('*')
                .eq('protected_user_id', cleanProtected)
                .eq('trusted_user_id', cleanTrusted)
                .maybeSingle();

            if (error || !data) return null;

            return {
                protectedUserId: data.protected_user_id,
                trustedUserId: data.trusted_user_id,
                displayName: data.display_name,
                relationship: data.relationship,
                enabled: data.enabled,
                createdAt: typeof data.created_at === 'string' ? Number(data.created_at) : data.created_at,
                updatedAt: typeof data.updated_at === 'string' ? Number(data.updated_at) : data.updated_at
            };
        }
        return null;
    } catch (err) {
        console.error('[TrustedContacts] Error reading contact:', err.message);
        return null;
    }
}

/**
 * Helper to record or update a trusted contact relationship (for administration or tests).
 * Canonical fields:
 *   protectedUserId, trustedUserId, displayName, relationship, enabled, createdAt, updatedAt
 */
async function setTrustedContact({
    protectedUid,
    trustedUid,
    displayName = '',
    relationship = 'Family',
    enabled = true,
    db = getAdminClient()
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
        const error = new Error('Database service unavailable: Supabase is not configured');
        error.statusCode = 503;
        throw error;
    }

    const existing = await getTrustedContact({ protectedUid: cleanProtected, trustedUid: cleanTrusted, db });
    const now = Date.now();
    const createdAt = existing && typeof existing.createdAt === 'number' ? existing.createdAt : now;

    const record = {
        protected_user_id: cleanProtected,
        trusted_user_id: cleanTrusted,
        display_name: String(displayName).trim(),
        relationship: String(relationship).trim(),
        enabled: Boolean(enabled),
        created_at: createdAt,
        updated_at: now
    };

    if (typeof db.from === 'function') {
        const { error } = await db.from('trusted_contacts').upsert(record, { onConflict: 'protected_user_id,trusted_user_id' });
        if (error) {
            console.error('[TrustedContacts] Upsert failed:', error.message);
            const err = new Error(error.message);
            err.statusCode = 500;
            throw err;
        }
    } else {
        const err = new Error('Unsupported database interface');
        err.statusCode = 503;
        throw err;
    }

    return {
        protectedUserId: cleanProtected,
        trustedUserId: cleanTrusted,
        displayName: record.display_name,
        relationship: record.relationship,
        enabled: record.enabled,
        createdAt,
        updatedAt: now
    };
}

module.exports = {
    isTrustedContact,
    getTrustedContact,
    setTrustedContact
};
