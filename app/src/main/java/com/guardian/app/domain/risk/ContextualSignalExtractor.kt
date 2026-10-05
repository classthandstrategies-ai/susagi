package com.guardian.app.domain.risk

import com.guardian.app.domain.risk.extraction.ContextualSignalExtractor as CanonicalContextualSignalExtractor

/**
 * Backward-compatible bridge to [com.guardian.app.domain.risk.extraction.ContextualSignalExtractor].
 *
 * Preserves compatibility for callers referencing `com.guardian.app.domain.risk.ContextualSignalExtractor`.
 */
typealias ContextualSignalExtractor = CanonicalContextualSignalExtractor
typealias LocalContextualExtractor = CanonicalContextualSignalExtractor
