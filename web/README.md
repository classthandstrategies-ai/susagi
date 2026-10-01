# SuSagi Web Companion

Interactive, responsive web companion for the SuSagi Autonomous Scam Defense System.

Built with Next.js App Router, React 19, TypeScript, and Tailwind CSS.

## Product Philosophy & Visual Direction

- **Calm, Premium, Banking-Grade**: Restrained dark slate/obsidian palette (`#0B0E14` void, `#111622` base, `#182030` surface).
- **Clear Semantic Risk Hierarchy**:
  - `LOW`: Calm emerald green (`#10B981`)
  - `CAUTION`: Alert warm amber (`#F59E0B`)
  - `HIGH`: Elevated warning orange (`#F97316`)
  - `CRITICAL`: Emergency crimson red (`#EF4444`, strictly reserved for urgent confirmed threats)
- **CRITICAL RULE**: Red is NEVER the default product color. No cyber-neon, terminal, or hacker aesthetics.
- **Cognitive Clarity Under Stress**: Adheres to the principle:
  `WHAT IS HAPPENING` → `WHY IT IS RISKY` → `WHAT THE USER SHOULD DO NEXT`.

## Core UI Architecture

- **No Secrets Required**: Core Product UI renders cleanly without requiring any secret API keys or backend credentials.
- **Honest Backend Contracts**: Backend-dependent functionality (such as remote cryptographic identity verification) is represented through typed frontend service contracts (`services/verificationService.ts`) and resolves honestly to `UNAVAILABLE` state rather than faking network success.
- **Strict Separation of Concerns**: Frontend components receive semantic states. The frontend NEVER calculates authoritative risk scores or applies numeric thresholds.
- **Isolated Development Fixtures**: Development-only risk scenarios (`?fixture=low|caution|high|critical`) are isolated under `src/fixtures/` and explicitly marked with `DEVELOPMENT FIXTURE — NOT RUNTIME BACKEND DATA`.

## Local Development

```bash
# Navigate to web workspace
cd web

# Install dependencies
npm install

# Start local development server
npm run dev
```

Visit [http://localhost:3000](http://localhost:3000).

## Verification & Build Scripts

```bash
# Type check TypeScript without emitting files
npm run typecheck

# Lint with ESLint
npm run lint

# Production build
npm run build
```

## Vercel Deployment

Deployable directly on Vercel by setting:

- **Root Directory**: `web`
- **Framework Preset**: `Next.js`
- **Build Command**: `next build` (default)
- **Output Directory**: `.next` (default)
- **Environment Variables**: None required for core UI.

## Routes Structure

- `/` — Companion Home & System Defense State
- `/protect` — Protection Shields & Detection Capabilities
- `/live` — Live Call Defense (Adaptive Telemetry & Signal Stream)
- `/activity` — Security Activity Log & Scam History
- `/activity/[incidentId]` — Detailed Incident Breakdown & Audio Timeline
- `/guardians` — Trusted Guardian Circle & Identity Network
- `/verification` — Out-of-band Identity Verification Requester
- `/verification/respond` — Verification Challenge Responder
- `/settings` — Host Device Permissions & Pairing Configuration
