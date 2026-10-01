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

const RISK_OPTIONS: { label: string; value: RiskFilter; color?: string }[] = [
  { label: "All Risks", value: "ALL" },
  { label: "Low", value: "LOW", color: "bg-risk-low" },
  { label: "Caution", value: "CAUTION", color: "bg-risk-caution" },
  { label: "High", value: "HIGH", color: "bg-risk-high" },
  { label: "Critical", value: "CRITICAL", color: "bg-risk-critical" },
];

const CHANNEL_OPTIONS: { label: string; value: ChannelFilter }[] = [
  { label: "All Channels", value: "ALL" },
  { label: "Calls", value: "CALL" },
  { label: "Messages", value: "MESSAGE" },
  { label: "Links", value: "LINK" },
  { label: "Verification", value: "VERIFICATION" },
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
    <section aria-label="Activity search and filters" className="space-y-4">
      {/* Search Input */}
      <div className="relative">
        <label htmlFor="activity-search" className="sr-only">
          Search activity records
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
          placeholder="Search by title, phone, claimed identity, or keyword..."
          className="w-full pl-10 pr-10 min-h-[48px] rounded-xl bg-surface border border-subtle focus:border-default focus:ring-2 focus:ring-brandLight text-sm text-primary placeholder:text-muted transition-colors"
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => onSearchChange("")}
            className="absolute inset-y-0 right-0 pr-3.5 min-w-[48px] flex items-center justify-center text-muted hover:text-primary transition-colors cursor-pointer"
            aria-label="Clear search query"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>

      {/* Filter Controls Row */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Risk Level Filter Pills */}
        <div
          role="group"
          aria-label="Filter by risk level"
          className="flex flex-wrap items-center gap-2"
        >
          {RISK_OPTIONS.map((opt) => {
            const isSelected = riskFilter === opt.value;
            return (
              <button
                key={opt.value}
                type="button"
                aria-pressed={isSelected}
                onClick={() => onRiskFilterChange(opt.value)}
                className={cn(
                  "inline-flex items-center gap-2 px-3.5 min-h-[48px] rounded-xl text-xs font-semibold transition-colors cursor-pointer focus-visible:ring-2 focus-visible:ring-brandLight",
                  isSelected
                    ? "bg-surfaceHighlight text-primary border border-default shadow-sm"
                    : "bg-surface text-secondary hover:text-primary border border-subtle hover:border-default"
                )}
              >
                {opt.color && (
                  <span
                    className={cn("w-2 h-2 rounded-full", opt.color)}
                    aria-hidden="true"
                  />
                )}
                <span>{opt.label}</span>
              </button>
            );
          })}
        </div>

        {/* Channel & Sorting Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Channel Filter Pills */}
          <div
            role="group"
            aria-label="Filter by communication channel"
            className="flex flex-wrap items-center gap-1.5"
          >
            {CHANNEL_OPTIONS.map((ch) => {
              const isSelected = channelFilter === ch.value;
              return (
                <button
                  key={ch.value}
                  type="button"
                  aria-pressed={isSelected}
                  onClick={() => onChannelFilterChange(ch.value)}
                  className={cn(
                    "px-3 min-h-[48px] inline-flex items-center rounded-xl text-xs font-mono font-medium transition-colors cursor-pointer",
                    isSelected
                      ? "bg-brandSoft text-brand border border-brand/30"
                      : "text-muted hover:text-secondary bg-surface border border-subtle"
                  )}
                >
                  {ch.label}
                </button>
              );
            })}
          </div>

          {/* Sort Order Selector */}
          <div className="flex items-center gap-1 pl-2 border-l border-subtle">
            <span className="text-[11px] text-muted font-mono">Sort:</span>
            <button
              type="button"
              onClick={() => onSortByChange(sortBy === "NEWEST" ? "OLDEST" : "NEWEST")}
              className="text-xs font-semibold text-secondary hover:text-primary px-3 min-h-[48px] inline-flex items-center rounded-xl bg-surface border border-subtle hover:border-default transition-colors cursor-pointer focus-visible:ring-2 focus-visible:ring-brandLight"
              aria-label={`Sort order: ${sortBy === "NEWEST" ? "Newest First" : "Oldest First"}`}
            >
              {sortBy === "NEWEST" ? "Newest ↓" : "Oldest ↑"}
            </button>
          </div>
        </div>
      </div>

      {/* Results and helper feedback */}
      <div className="flex items-center justify-between text-xs text-muted font-mono pt-1">
        <span>
          Showing {totalResults} {totalResults === 1 ? "record" : "records"}
        </span>
        <span>Presentation filtering of loaded records</span>
      </div>
    </section>
  );
};
