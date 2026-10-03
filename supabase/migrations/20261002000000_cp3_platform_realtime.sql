-- CP3 Platform Realtime Migration
-- Migrates device registry, trusted contacts, and authoritative verification sessions to Supabase PostgreSQL.

-- 1. Devices Table
CREATE TABLE IF NOT EXISTS public.devices (
    user_id UUID NOT NULL,
    device_id TEXT NOT NULL,
    fcm_token TEXT NOT NULL,
    platform TEXT NOT NULL CONSTRAINT chk_devices_platform CHECK (platform IN ('android', 'ios', 'web')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    last_seen_at BIGINT NOT NULL,
    PRIMARY KEY (user_id, device_id)
);

CREATE INDEX IF NOT EXISTS idx_devices_user_id ON public.devices (user_id);

-- 2. Trusted Contacts Table
CREATE TABLE IF NOT EXISTS public.trusted_contacts (
    protected_user_id UUID NOT NULL,
    trusted_user_id UUID NOT NULL,
    display_name TEXT,
    relationship TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (protected_user_id, trusted_user_id),
    CONSTRAINT chk_trusted_contacts_no_self CHECK (protected_user_id <> trusted_user_id)
);

CREATE INDEX IF NOT EXISTS idx_trusted_contacts_protected ON public.trusted_contacts (protected_user_id);
CREATE INDEX IF NOT EXISTS idx_trusted_contacts_trusted ON public.trusted_contacts (trusted_user_id);

-- 3. Verification Sessions Table
CREATE TABLE IF NOT EXISTS public.verification_sessions (
    id TEXT PRIMARY KEY,
    protected_user_id UUID NOT NULL,
    trusted_user_id UUID NOT NULL,
    claimed_identity TEXT NOT NULL,
    requested_action TEXT NOT NULL,
    request_summary TEXT NOT NULL,
    risk_score_at_creation INTEGER NOT NULL CONSTRAINT chk_verification_risk_score CHECK (risk_score_at_creation BETWEEN 0 AND 100),
    status TEXT NOT NULL CONSTRAINT chk_verification_status CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED', 'EXPIRED', 'UNAVAILABLE')),
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    responded_at BIGINT NULL,
    response_device_id TEXT NULL,
    version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_verification_sessions_protected ON public.verification_sessions (protected_user_id);
CREATE INDEX IF NOT EXISTS idx_verification_sessions_trusted ON public.verification_sessions (trusted_user_id);
CREATE INDEX IF NOT EXISTS idx_verification_sessions_status ON public.verification_sessions (status);

-- 4. Enable Row Level Security (RLS)
ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.trusted_contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_sessions ENABLE ROW LEVEL SECURITY;

-- 5. RLS Policies: Direct client writes remain completely denied.
-- Only authenticated users may SELECT rows where their auth.uid() matches.

-- devices: authenticated user may read only their own devices
CREATE POLICY devices_select_own
    ON public.devices
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

-- trusted_contacts: authenticated user may read if they are protected or trusted contact
CREATE POLICY trusted_contacts_select_member
    ON public.trusted_contacts
    FOR SELECT
    TO authenticated
    USING (auth.uid() = protected_user_id OR auth.uid() = trusted_user_id);

-- verification_sessions: authenticated user may read if they are protected or trusted contact
CREATE POLICY verification_sessions_select_member
    ON public.verification_sessions
    FOR SELECT
    TO authenticated
    USING (auth.uid() = protected_user_id OR auth.uid() = trusted_user_id);

-- 6. Supabase Realtime Publication
ALTER TABLE public.verification_sessions REPLICA IDENTITY FULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables
        WHERE pubname = 'supabase_realtime'
          AND schemaname = 'public'
          AND tablename = 'verification_sessions'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.verification_sessions;
    END IF;
END $$;
