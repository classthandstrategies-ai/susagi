const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.SUPABASE_URL || '';
const supabaseAnonKey = process.env.SUPABASE_ANON_KEY || process.env.SUPABASE_PUBLISHABLE_KEY || '';
const supabaseServiceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY || '';

let authClient = null;
let adminClient = null;
let isConfigured = false;

if (supabaseUrl && supabaseAnonKey && supabaseServiceRoleKey) {
    try {
        // Client for token verification and auth operations
        authClient = createClient(supabaseUrl, supabaseAnonKey, {
            auth: {
                persistSession: false,
                autoRefreshToken: false
            }
        });

        // Privileged client with service_role key for database persistence
        adminClient = createClient(supabaseUrl, supabaseServiceRoleKey, {
            auth: {
                persistSession: false,
                autoRefreshToken: false
            }
        });

        isConfigured = true;
        console.log('[Supabase] Initialized auth and admin service-role clients');
    } catch (err) {
        console.warn('[Supabase] Initialization failed:', err.message);
        isConfigured = false;
        authClient = null;
        adminClient = null;
    }
} else {
    console.warn('[Supabase] Configuration missing (SUPABASE_URL, SUPABASE_ANON_KEY, or SUPABASE_SERVICE_ROLE_KEY). Running in unconfigured fail-closed mode.');
}

module.exports = {
    getAuthClient: () => authClient,
    getAdminClient: () => adminClient,
    isConfigured: () => isConfigured
};
