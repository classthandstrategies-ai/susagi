"use client";

import React, { useState } from "react";
import { GuardianCircleViewState, GuardianDialogMode, GuardianFormData } from "./guardianTypes";
import { GuardianContact } from "@/types/guardian";
import { GuardianCircleHeader } from "./GuardianCircleHeader";
import { GuardianList } from "./GuardianList";
import { GuardianEmptyState } from "./GuardianEmptyState";
import { GuardianManagementDialog } from "./GuardianManagementDialog";

interface GuardianCirclePageClientProps {
  initialState: GuardianCircleViewState;
}

export const GuardianCirclePageClient: React.FC<GuardianCirclePageClientProps> = ({
  initialState,
}) => {
  const { status, circle, errorMessage, isFixtureMode } = initialState;

  // Local in-memory state for development fixture interaction testing (Step 6)
  const [guardians, setGuardians] = useState<GuardianContact[]>(circle.guardians);
  const [dialogMode, setDialogMode] = useState<GuardianDialogMode>("NONE");
  const [selectedGuardian, setSelectedGuardian] = useState<GuardianContact | null>(null);

  // In-memory add/edit in fixture mode
  const handleSaveFixtureGuardian = (formData: GuardianFormData) => {
    if (!isFixtureMode) return;

    if (formData.id) {
      // Edit existing
      setGuardians((prev) =>
        prev.map((g) =>
          g.id === formData.id
            ? {
                ...g,
                name: formData.name,
                relationship: formData.relationship,
                phone: formData.phone,
              }
            : g
        )
      );
    } else {
      // Add new in-memory
      const initials = formData.name
        .split(" ")
        .map((p) => p[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);

      const newGuardian: GuardianContact = {
        id: `g-fixture-${Date.now()}`,
        name: formData.name,
        relationship: formData.relationship,
        phone: formData.phone,
        isPrimary: guardians.length === 0,
        canVerifyIdentity: true,
        status: "ACTIVE",
        avatarInitials: initials || "TC",
        lastActive: "Added in session",
      };

      setGuardians((prev) => [newGuardian, ...prev]);
    }
  };

  // In-memory delete in fixture mode
  const handleDeleteFixtureGuardian = (id: string) => {
    if (!isFixtureMode) return;
    setGuardians((prev) => prev.filter((g) => g.id !== id));
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <GuardianCircleHeader
        isFixtureMode={isFixtureMode}
        guardianCount={guardians.length}
        onOpenAddDialog={() => {
          setSelectedGuardian(null);
          setDialogMode("ADD");
        }}
        onOpenEducationDialog={() => setDialogMode("EDUCATION")}
      />

      {/* Loading State */}
      {status === "LOADING" && (
        <div role="status" aria-label="Loading guardian circle" className="grid grid-cols-1 md:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 rounded-2xl bg-surface border border-subtle p-5" />
          ))}
          <span className="sr-only">Loading guardian circle...</span>
        </div>
      )}

      {/* Error State */}
      {status === "ERROR" && (
        <div className="rounded-2xl bg-surface border border-risk-caution/30 p-8 text-center space-y-3">
          <h2 className="text-lg font-bold text-primary">Guardian Service Unavailable</h2>
          <p className="text-sm text-secondary">{errorMessage || "Could not retrieve guardian circle."}</p>
        </div>
      )}

      {/* Normal Runtime Disconnected State */}
      {(status === "UNAVAILABLE" || (status === "EMPTY" && !isFixtureMode)) && (
        <GuardianEmptyState
          type="DISCONNECTED"
          onOpenEducationDialog={() => setDialogMode("EDUCATION")}
        />
      )}

      {/* Empty Connected State in Fixture Mode */}
      {status === "EMPTY" && isFixtureMode && (
        <GuardianEmptyState
          type="EMPTY_CONNECTED"
          onOpenEducationDialog={() => setDialogMode("EDUCATION")}
        />
      )}

      {/* Loaded Guardian Circle View */}
      {status === "LOADED" && (
        <div className="space-y-6">
          {/* Family Secret Word Security Banner */}
          {circle.securityPassphraseConfigured && (
            <div className="rounded-2xl bg-surfaceElevated border border-subtle p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-brandSoft text-brand flex items-center justify-center shrink-0">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-primary">
                    Family Secret Word Configured
                  </h3>
                  <p className="text-xs text-secondary leading-relaxed">
                    Protects against voice synthesis by requiring a pre-shared passphrase.
                  </p>
                </div>
              </div>
              <span className="text-xs font-mono font-bold px-2.5 py-1 rounded bg-brandSoft text-brand border border-brand/20 self-start sm:self-auto">
                ANCHOR ACTIVE
              </span>
            </div>
          )}

          {guardians.length === 0 ? (
            <GuardianEmptyState
              type="EMPTY_CONNECTED"
              onOpenEducationDialog={() => setDialogMode("EDUCATION")}
            />
          ) : (
            <GuardianList
              guardians={guardians}
              isFixtureMode={isFixtureMode}
              onEditGuardian={(g) => {
                setSelectedGuardian(g);
                setDialogMode("EDIT");
              }}
              onRemoveGuardian={(g) => {
                setSelectedGuardian(g);
                setDialogMode("REMOVE");
              }}
            />
          )}
        </div>
      )}

      {/* Management & Education Dialogs */}
      <GuardianManagementDialog
        mode={dialogMode}
        onClose={() => {
          setDialogMode("NONE");
          setSelectedGuardian(null);
        }}
        selectedGuardian={selectedGuardian}
        isFixtureMode={isFixtureMode}
        onSaveFixtureGuardian={handleSaveFixtureGuardian}
        onDeleteFixtureGuardian={handleDeleteFixtureGuardian}
      />
    </div>
  );
};
