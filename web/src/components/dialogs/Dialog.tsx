"use client";

import React from "react";
import { Modal } from "./Modal";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";

interface DialogProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm?: () => void;
  confirmVariant?: "brand" | "danger" | "neutral";
}

export const Dialog: React.FC<DialogProps> = ({
  isOpen,
  onClose,
  title,
  message,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  onConfirm,
  confirmVariant = "brand",
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <p className="text-sm text-secondary leading-relaxed mb-6">{message}</p>

      <div className="flex flex-col-reverse sm:flex-row sm:justify-end gap-3">
        <SecondarySafetyAction label={cancelLabel} onClick={onClose} />
        {onConfirm && (
          <PrimarySafetyAction
            label={confirmLabel}
            variant={confirmVariant}
            onClick={() => {
              onConfirm();
              onClose();
            }}
          />
        )}
      </div>
    </Modal>
  );
};
