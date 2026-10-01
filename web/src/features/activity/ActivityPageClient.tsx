"use client";

import React, { useState, useMemo } from "react";
import { ActivityViewState, RiskFilter, ChannelFilter, SortOrder } from "./activityTypes";
import { ActivityHeader } from "./ActivityHeader";
import { ActivityFilters } from "./ActivityFilters";
import { ActivityList } from "./ActivityList";
import { ActivityEmptyState } from "./ActivityEmptyState";
import { ActivityLoadingSkeleton } from "./ActivityLoadingSkeleton";
import { ActivityErrorState } from "./ActivityErrorState";

interface ActivityPageClientProps {
  initialState: ActivityViewState;
}

export const ActivityPageClient: React.FC<ActivityPageClientProps> = ({
  initialState,
}) => {
  const { status, incidents, errorMessage, isFixtureMode, fixtureKey } = initialState;

  const [searchQuery, setSearchQuery] = useState("");
  const [riskFilter, setRiskFilter] = useState<RiskFilter>("ALL");
  const [channelFilter, setChannelFilter] = useState<ChannelFilter>("ALL");
  const [sortBy, setSortBy] = useState<SortOrder>("NEWEST");

  // Client-side filtering & search on loaded records
  const filteredIncidents = useMemo(() => {
    if (!incidents || incidents.length === 0) return [];

    let list = incidents.filter((incident) => {
      // Risk filter
      if (riskFilter !== "ALL" && incident.riskLevel !== riskFilter) {
        return false;
      }

      // Channel filter
      if (channelFilter !== "ALL" && incident.channel !== channelFilter) {
        return false;
      }

      // Search query filter (matches title, claimedIdentity, source, summary)
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        const matchesTitle = incident.title.toLowerCase().includes(q);
        const matchesSource = incident.source.toLowerCase().includes(q);
        const matchesSummary = incident.summary.toLowerCase().includes(q);
        const matchesIdentity = incident.claimedIdentity
          ? incident.claimedIdentity.toLowerCase().includes(q)
          : false;

        if (!matchesTitle && !matchesSource && !matchesSummary && !matchesIdentity) {
          return false;
        }
      }

      return true;
    });

    // Sorting
    if (sortBy === "OLDEST") {
      list = [...list].reverse();
    }

    return list;
  }, [incidents, riskFilter, channelFilter, searchQuery, sortBy]);

  const resetFilters = () => {
    setSearchQuery("");
    setRiskFilter("ALL");
    setChannelFilter("ALL");
    setSortBy("NEWEST");
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Activity Header */}
      <ActivityHeader
        isFixtureMode={isFixtureMode}
        totalCount={incidents.length}
      />

      {/* Loading State */}
      {status === "LOADING" && <ActivityLoadingSkeleton />}

      {/* Error State */}
      {status === "ERROR" && (
        <ActivityErrorState message={errorMessage} />
      )}

      {/* Normal Runtime Disconnected State */}
      {(status === "UNAVAILABLE" || (status === "EMPTY" && !isFixtureMode)) && (
        <ActivityEmptyState type="DISCONNECTED" />
      )}

      {/* Connected Empty State (Dev fixture ?fixture=empty) */}
      {status === "EMPTY" && isFixtureMode && (
        <ActivityEmptyState type="EMPTY_CONNECTED" />
      )}

      {/* Loaded Incidents View */}
      {status === "LOADED" && (
        <div className="space-y-5">
          <ActivityFilters
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            riskFilter={riskFilter}
            onRiskFilterChange={setRiskFilter}
            channelFilter={channelFilter}
            onChannelFilterChange={setChannelFilter}
            sortBy={sortBy}
            onSortByChange={setSortBy}
            totalResults={filteredIncidents.length}
          />

          {filteredIncidents.length === 0 ? (
            <ActivityEmptyState
              type="NO_SEARCH_RESULTS"
              onResetFilters={resetFilters}
            />
          ) : (
            <ActivityList
              incidents={filteredIncidents}
              fixtureKey={fixtureKey}
            />
          )}
        </div>
      )}
    </div>
  );
};
