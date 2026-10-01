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
