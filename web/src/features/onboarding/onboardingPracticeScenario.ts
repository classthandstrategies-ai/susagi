/**
 * Dedicated onboarding-only educational scenario object.
 *
 * CRITICAL ISOLATION RULES:
 * - DO NOT import or use riskService, activityService, or verificationService.
 * - Must NOT include real or simulated call IDs, session IDs, activity IDs,
 *   real timestamps, or runtime transport state.
 * - This is strictly an in-memory educational object for safe walkthrough.
 */

export interface PracticeScenarioData {
  readonly isPractice: true;
  readonly riskLevel: "HIGH";
  readonly title: string;
  readonly scenarioText: string;
  readonly headline: string;
  readonly explanation: string;
  readonly concerns: readonly string[];
  readonly safetyGuidance: string;
  readonly notice: string;
}

export const PRACTICE_SCENARIO: PracticeScenarioData = {
  isPractice: true,
  riskLevel: "HIGH",
  title: "PRACTICE SCENARIO",
  scenarioText:
    "A caller says they're from your bank and asks for the OTP you just received.",
  headline: "They're asking for your OTP.",
  explanation:
    "Banks should not require you to share an OTP over a phone call.",
  concerns: [
    "One-time password requested",
    "Urgency / account threat",
  ],
  safetyGuidance: "Do not share your OTP, PIN or password.",
  notice: "No real call is active.",
};
