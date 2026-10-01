# SuSagi Web Companion

Interactive, responsive web companion for the SuSagi Autonomous Scam Defense System.

Built with Next.js App Router (Turbopack), React 19, TypeScript, and Tailwind CSS.

## Product Philosophy & Visual Direction

- **Calm, Premium, Banking-Grade**: Restrained dark slate/obsidian palette (`#0B0E14` void, `#111622` base, `#182030` surface).
- **Clear Semantic Risk Hierarchy**:
  - `LOW`: Calm emerald green (`#10B981`)
  - `CAUTION`: Alert warm amber (`#F59E0B`)
  - `HIGH`: Elevated warning orange (`#F97316`)
  - `CRITICAL`: Emergency crimson red (`#EF4444`, reserved for CRITICAL risk states and destructive/safety-critical actions)
- **CRITICAL RULE**: Red is NEVER the default product color. No cyber-neon, terminal, or hacker aesthetics.
- **Cognitive Clarity Under Stress**: Adheres to the principle:
  `WHAT IS HAPPENING` → `WHY IT IS RISKY` → `WHAT THE USER SHOULD DO NEXT`.
- **Accessibility & Ergonomics**:
  - Minimum 48px touch targets for all primary buttons, navigation items, filter chips, and dialog actions.
  - WCAG AAA contrast ratios across all semantic tokens against void background.
  - Web-local reduce-motion switch with immediate DOM application and local storage persistence.

## Core Architecture & Service Boundaries

- **No Secrets Required**: The web companion builds, starts, and runs cleanly without requiring proprietary cloud secrets, backend credentials, or API keys.
- **Honest Runtime State**: In production runtime without connected backend services, every route honestly declares its operational state (e.g., "Device service not connected", "Guardian service not connected", "Identity Verification Service Unavailable"). It never fabricates simulated telemetry or fake connection success.
- **Strict Separation of Concerns**: Frontend components consume semantic contracts (`RiskLevel`, `ProtectiveAction`, `VerificationStatus`). The frontend **NEVER** calculates authoritative risk scores or applies numeric thresholds.
- **Strict Development Fixture Isolation**: Fixtures under `src/fixtures/` and the interactive DevFixtureBar are strictly guarded by `process.env.NODE_ENV === "development"`. In production mode (`next start` / Vercel), query parameters such as `?fixture=...` are completely ignored, and no fixture badges or fixture data leak into production runtime.
- **Android Host Relationship**: SuSagi Web Companion acts as an auxiliary monitoring and out-of-band verification interface. Native telephony interception, real-time acoustic ML scoring, and call termination execute exclusively on the host Android device (baseline checkpoint: `95f09cc9c512ac4d35f17c6f8b94d7528ded5053`).

## Local Development & Production Testing

```bash
# Navigate to web workspace
cd web

# Install dependencies
npm install

# Start local development server (with dev fixtures enabled)
npm run dev

# Run full TypeScript validation
npm run typecheck

# Lint with ESLint
npm run lint

# Compile optimized production build
npm run build

# Run production server (zero fixtures, true companion runtime)
npm run start
```

## Vercel Deployment Readiness

Deployable directly on Vercel:

- **Root Directory**: `web`
- **Framework Preset**: `Next.js`
- **Build Command**: `next build`
- **Output Directory**: `.next`
- **Node.js Version**: 18.x or 20.x
- **Environment Variables**: None required for core UI.

## Routes Structure

- `/` — Companion Home & System Defense State
- `/protect` — Protection Shields & Detection Capabilities
- `/live` — Live Call Defense (Adaptive Telemetry & Signal Stream)
- `/activity` — Security Activity Ledger & Incident History
- `/activity/[incidentId]` — Detailed Incident Breakdown & Audio Timeline
- `/guardians` — Trusted Guardian Circle & Identity Network
- `/verification` — Out-of-band Identity Verification Requester
- `/verification/respond` — Verification Challenge Responder
- `/settings` — Companion Operational Status, Accessibility Preferences, Privacy Disclosures & About Metadata
