/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.content.SearchRecentSuggestionsProvider

class SearchHistoryProvider : SearchRecentSuggestionsProvider() {
    companion object {
        val AUTHORITY: String = SearchHistoryProvider::class.java.name
        const val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}