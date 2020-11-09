package org.koitharu.kotatsu.ui.search

import android.app.SearchManager
import android.content.ContentResolver
import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.net.Uri
import android.provider.SearchRecentSuggestions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.BuildConfig
import org.koitharu.kotatsu.R

class MangaSuggestionsProvider : SearchRecentSuggestionsProvider() {

	init {
		setupSuggestions(
			AUTHORITY,
			MODE
		)
	}

	private class SearchSuggestionAdapter(context: Context, cursor: Cursor) : CursorAdapter(
		context, cursor,
		FLAG_REGISTER_CONTENT_OBSERVER
	) {

		override fun newView(context: Context, cursor: Cursor?, parent: ViewGroup?): View {
			return LayoutInflater.from(context)
				.inflate(R.layout.item_search_complete, parent, false)
		}

		override fun bindView(view: View, context: Context, cursor: Cursor) {
			if (view !is TextView) return
			view.text = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY))
		}

		override fun convertToString(cursor: Cursor?): CharSequence {
			return cursor?.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY))
				.orEmpty()
		}
	}

	companion object {

		private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.MangaSuggestionsProvider"
		private const val MODE = DATABASE_MODE_QUERIES

		private val uri = Uri.Builder()
			.scheme(ContentResolver.SCHEME_CONTENT)
			.authority(AUTHORITY)
			.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
			.build()

		private val projection = arrayOf("_id", SearchManager.SUGGEST_COLUMN_QUERY)

		fun saveQueryAsync(context: Context, query: String) {
			GlobalScope.launch(Dispatchers.IO) {
				saveQuery(context, query)
			}
		}

		fun saveQuery(context: Context, query: String) {
			runCatching {
				SearchRecentSuggestions(
					context,
					AUTHORITY,
					MODE
				).saveRecentQuery(query, null)
			}.onFailure {
				if (BuildConfig.DEBUG) {
					it.printStackTrace()
				}
			}
		}

		suspend fun clearHistory(context: Context) = withContext(Dispatchers.IO) {
			SearchRecentSuggestions(
				context,
				AUTHORITY,
				MODE
			).clearHistory()
		}

		suspend fun getItemsCount(context: Context) = withContext(Dispatchers.IO) {
			getCursor(context)?.use { it.count } ?: 0
		}

		private fun getCursor(context: Context): Cursor? {
			return context.contentResolver?.query(uri, projection, null, arrayOf(""), null)
		}

		@Deprecated("Need async implementation")
		fun getSuggestionAdapter(context: Context): CursorAdapter? = getCursor(
			context
		)?.let { cursor ->
			SearchSuggestionAdapter(context, cursor).also {
				it.setFilterQueryProvider { q ->
					context.contentResolver?.query(
						uri,
						projection,
						" ?",
						arrayOf(q?.toString().orEmpty()),
						null
					)
				}
			}
		}
	}
}