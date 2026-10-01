"use client";

import React, { useState } from "react";
import { Modal } from "@/components/dialogs/Modal";
import { GuardianContact } from "@/types/guardian";
import { GuardianDialogMode, GuardianFormData } from "./guardianTypes";

interface GuardianManagementDialogProps {
  mode: GuardianDialogMode;
  onClose: () => void;
  selectedGuardian?: GuardianContact | null;
  isFixtureMode: boolean;
  onSaveFixtureGuardian?: (data: GuardianFormData) => void;
  onDeleteFixtureGuardian?: (id: string) => void;
}

interface GuardianFormContentProps {
  selectedGuardian?: GuardianContact | null;
  isEdit: boolean;
  isFixtureMode: boolean;
  onSave?: (data: GuardianFormData) => void;
  onClose: () => void;
}

const GuardianFormContent: React.FC<GuardianFormContentProps> = ({
  selectedGuardian,
  isEdit,
  isFixtureMode,
  onSave,
  onClose,
}) => {
  const [name, setName] = useState(selectedGuardian?.name || "");
  const [relationship, setRelationship] = useState(selectedGuardian?.relationship || "");
  const [phone, setPhone] = useState(selectedGuardian?.phone || "");
  const [error, setError] = useState<string | null>(null);
  const [runtimeBlockedNotice, setRuntimeBlockedNotice] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      setError("Please provide a name for this contact.");
      return;
    }

    if (!phone.trim()) {
      setError("Please provide a phone number.");
      return;
    }

    // Normal production runtime check
    if (!isFixtureMode) {
      setRuntimeBlockedNotice(
        "Guardian service is not connected. This contact cannot be saved yet."
      );
      return;
    }

    // Development fixture mode update in memory
    if (onSave) {
      onSave({
        id: selectedGuardian?.id,
        name: name.trim(),
        relationship: relationship.trim() || "Trusted Contact",
        phone: phone.trim(),
        isPrimary: selectedGuardian?.isPrimary || false,
      });
      onClose();
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {runtimeBlockedNotice && (
        <div
          role="alert"
          className="rounded-xl bg-risk-caution-soft border border-risk-caution/30 p-3.5 text-xs text-risk-caution leading-relaxed space-y-1"
        >
          <div className="font-semibold">Service not connected</div>
          <div>{runtimeBlockedNotice}</div>
        </div>
      )}

      {isFixtureMode && (
        <div className="rounded-xl bg-surfaceElevated border border-subtle p-3 text-xs text-secondary leading-relaxed">
          Fixture preview: Changes update in-memory only.
        </div>
      )}

      {error && (
        <div
          role="alert"
          className="text-xs text-risk-critical font-medium bg-risk-critical-soft p-2.5 rounded-lg border border-risk-critical/20"
        >
          {error}
        </div>
      )}

      <div className="space-y-1.5">
        <label htmlFor="guardian-name" className="text-xs font-medium text-secondary">
          Full name *
        </label>
        <input
          id="guardian-name"
          type="text"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            setError(null);
          }}
          placeholder="e.g. Aarav Sharma"
          required
          className="w-full min-h-[44px] px-3.5 rounded-xl bg-surfaceElevated border border-subtle focus:border-default focus:ring-2 focus:ring-brandLight text-sm text-primary placeholder:text-muted transition-colors"
        />
      </div>

      <div className="space-y-1.5">
        <label htmlFor="guardian-relationship" className="text-xs font-medium text-secondary">
          Relationship
        </label>
        <input
          id="guardian-relationship"
          type="text"
          value={relationship}
          onChange={(e) => setRelationship(e.target.value)}
          placeholder="e.g. Sister, Son, Dad"
          className="w-full min-h-[44px] px-3.5 rounded-xl bg-surfaceElevated border border-subtle focus:border-default focus:ring-2 focus:ring-brandLight text-sm text-primary placeholder:text-muted transition-colors"
        />
      </div>

      <div className="space-y-1.5">
        <label htmlFor="guardian-phone" className="text-xs font-medium text-secondary">
          Phone number *
        </label>
        <input
          id="guardian-phone"
          type="tel"
          value={phone}
          onChange={(e) => {
            setPhone(e.target.value);
            setError(null);
          }}
          placeholder="+91 9XXXX XXXXX"
          required
          className="w-full min-h-[44px] px-3.5 rounded-xl bg-surfaceElevated border border-subtle focus:border-default focus:ring-2 focus:ring-brandLight text-sm text-primary font-mono placeholder:text-muted transition-colors"
        />
      </div>

      <div className="flex flex-col sm:flex-row gap-3 pt-2">
        <button
          type="submit"
          className="flex-1 min-h-[44px] px-5 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-white font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
        >
          {isEdit ? "Update contact" : "Save contact"}
        </button>
        <button
          type="button"
          onClick={onClose}
          className="flex-1 min-h-[44px] px-5 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-secondary hover:text-primary font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
        >
          Cancel
        </button>
      </div>
    </form>
  );
};

export const GuardianManagementDialog: React.FC<GuardianManagementDialogProps> = ({
  mode,
  onClose,
  selectedGuardian,
  isFixtureMode,
  onSaveFixtureGuardian,
  onDeleteFixtureGuardian,
}) => {
  if (mode === "NONE") return null;

  // REMOVE CONFIRMATION MODAL
  if (mode === "REMOVE" && selectedGuardian) {
    const handleConfirmDelete = () => {
      if (isFixtureMode && onDeleteFixtureGuardian) {
        onDeleteFixtureGuardian(selectedGuardian.id);
        onClose();
      }
    };

    return (
      <Modal isOpen={true} onClose={onClose} title="Remove trusted person?">
        <div className="space-y-4">
          <p className="text-sm text-secondary leading-relaxed">
            Are you sure you want to remove <strong>{selectedGuardian.name}</strong> from your trusted circle?
          </p>

          <div className="flex flex-col sm:flex-row gap-3 pt-2">
            <button
              type="button"
              onClick={handleConfirmDelete}
              className="flex-1 min-h-[44px] px-5 py-2.5 rounded-xl bg-risk-critical hover:bg-red-700 text-white font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
            >
              Confirm removal
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex-1 min-h-[44px] px-5 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-secondary hover:text-primary font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
            >
              Cancel
            </button>
          </div>
        </div>
      </Modal>
    );
  }

  // EDUCATION MODAL
  if (mode === "EDUCATION") {
    return (
      <Modal isOpen={true} onClose={onClose} title="How Guardian Circle works">
        <div className="space-y-4">
          <p className="text-sm text-secondary leading-relaxed">
            When something feels unusual, SuSagi can help you ask a trusted person to confirm a sensitive request through a connected verification service.
          </p>

          <div className="space-y-3">
            <div className="rounded-xl bg-surfaceElevated p-3.5 border border-subtle space-y-1">
              <h4 className="text-xs font-semibold text-primary">1. Trusted person confirmation</h4>
              <p className="text-xs text-secondary leading-relaxed">
                If an incoming caller pressures you for credentials or emergency money, you can challenge their claim by asking a trusted contact to verify it.
              </p>
            </div>

            <div className="rounded-xl bg-surfaceElevated p-3.5 border border-subtle space-y-1">
              <h4 className="text-xs font-semibold text-primary">2. Trusted-person verification</h4>
              <p className="text-xs text-secondary leading-relaxed">
                When connected to SuSagi verification services, a trusted person can help confirm a sensitive request.
              </p>
            </div>

            <div className="rounded-xl bg-surfaceElevated p-3.5 border border-subtle space-y-1">
              <h4 className="text-xs font-semibold text-primary">3. Complete audit history</h4>
              <p className="text-xs text-secondary leading-relaxed">
                All verification requests and responses are recorded in your Activity history for reference.
              </p>
            </div>
          </div>

          <div className="pt-2">
            <button
              type="button"
              onClick={onClose}
              className="w-full min-h-[44px] px-5 py-2.5 rounded-xl bg-brand text-white font-medium text-xs hover:bg-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
            >
              Got it
            </button>
          </div>
        </div>
      </Modal>
    );
  }

  const title = mode === "EDIT" ? "Edit trusted person" : "Add trusted person";

  return (
    <Modal isOpen={true} onClose={onClose} title={title}>
      <GuardianFormContent
        key={`${mode}-${selectedGuardian?.id || "new"}`}
        selectedGuardian={selectedGuardian}
        isEdit={mode === "EDIT"}
        isFixtureMode={isFixtureMode}
        onSave={onSaveFixtureGuardian}
        onClose={onClose}
      />
    </Modal>
  );
};
