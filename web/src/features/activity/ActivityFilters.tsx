"use client";

import React from "react";
import { RiskFilter, ChannelFilter, SortOrder } from "./activityTypes";
import { cn } from "@/lib/utils";

interface ActivityFiltersProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  riskFilter: RiskFilter;
  onRiskFilterChange: (risk: RiskFilter) => void;
  channelFilter: ChannelFilter;
  onChannelFilterChange: (channel: ChannelFilter) => void;
  sortBy: SortOrder;
  onSortByChange: (sort: SortOrder) => void;
  totalResults: number;
}

const RISK_OPTIONS: { label: string; value: RiskFilter }[] = [
  { label: "All risks", value: "ALL" },
  { label: "Critical", value: "CRITICAL" },
  { label: "High", value: "HIGH" },
  { label: "Caution", value: "CAUTION" },
  { label: "Low", value: "LOW" },
];

const CHANNEL_OPTIONS: { label: string; value: ChannelFilter }[] = [
  { label: "All", value: "ALL" },
  { label: "Calls", value: "CALL" },
  { label: "Messages", value: "MESSAGE" },
  { label: "Links", value: "LINK" },
  { label: "Verifications", value: "VERIFICATION" },
];

export const ActivityFilters: React.FC<ActivityFiltersProps> = ({
  searchQuery,
  onSearchChange,
  riskFilter,
  onRiskFilterChange,
  channelFilter,
  onChannelFilterChange,
  sortBy,
  onSortByChange,
  totalResults,
}) => {
  return (
    <section aria-label="Activity search and filters" className="space-y-3">
      {/* Search Input */}
      <div className="relative">
        <label htmlFor="activity-search" className="sr-only">
          Search activity
        </label>
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-muted">
          <svg
            className="w-4 h-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
            />
          </svg>
        </div>
        <input
          id="activity-search"
          type="search"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search activity by title, phone, or name..."
          className="w-full pl-10 pr-10 min-h-[44px] rounded-xl bg-surface border border-subtle focus:border-default focus:ring-2 focus:ring-brandLight text-xs sm:text-sm text-primary placeholder:text-muted transition-colors shadow-sm"
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => onSearchChange("")}
            className="absolute inset-y-0 right-0 pr-3.5 min-w-[40px] flex items-center justify-center text-muted hover:text-primary transition-colors cursor-pointer text-xs"
            aria-label="Clear search"
          >
            Clear
          </button>
        )}
      </div>

      {/* Filter Pills and Sort Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
        {/* Channel Pills */}
        <div
          role="group"
          aria-label="Filter by category"
          className="flex flex-wrap items-center gap-1.5"
        >
          {CHANNEL_OPTIONS.map((opt) => {
            const isSelected = channelFilter === opt.value;
            return (
              <button
                key={opt.value}
                type="button"
                onClick={() => onChannelFilterChange(opt.value)}
                className={cn(
                  "min-h-[36px] px-3 py-1.5 rounded-full text-xs font-medium transition-colors cursor-pointer border",
                  isSelected
                    ? "bg-brand text-white border-brand"
                    : "bg-surface text-secondary hover:text-primary border-subtle"
                )}
              >
                {opt.label}
              </button>
            );
          })}
        </div>

        {/* Secondary Controls: Risk Pills & Sort / Count */}
        <div className="flex flex-wrap items-center gap-3">
          <div
            role="group"
            aria-label="Filter by risk"
            className="flex flex-wrap items-center gap-1.5"
          >
            {RISK_OPTIONS.map((opt) => {
              const isSelected = riskFilter === opt.value;
              return (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => onRiskFilterChange(opt.value)}
                  className={cn(
                    "min-h-[36px] px-2.5 py-1 rounded-full text-[11px] font-medium transition-colors cursor-pointer border",
                    isSelected
                      ? "bg-surfaceElevated text-primary border-default font-semibold"
                      : "bg-transparent text-muted hover:text-secondary border-transparent"
                  )}
                >
                  {opt.label}
                </button>
              );
            })}
          </div>

          <div className="flex items-center gap-2 text-xs text-muted pl-2 border-l border-subtle">
            <button
              type="button"
              onClick={() => onSortByChange(sortBy === "NEWEST" ? "OLDEST" : "NEWEST")}
              className="text-secondary hover:text-primary transition-colors cursor-pointer"
            >
              {sortBy === "NEWEST" ? "Newest" : "Oldest"}
            </button>
            <span>({totalResults})</span>
          </div>
        </div>
      </div>
    </section>
  );
};
