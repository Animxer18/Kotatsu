package org.koitharu.kotatsu.ui.list.remote

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import moxy.ktx.moxyPresenter
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.MangaFilter
import org.koitharu.kotatsu.core.model.MangaSource
import org.koitharu.kotatsu.ui.list.MangaListFragment
import org.koitharu.kotatsu.ui.search.SearchActivity
import org.koitharu.kotatsu.utils.ext.withArgs

class RemoteListFragment : MangaListFragment<Unit>() {

	private val presenter by moxyPresenter(factory = ::RemoteListPresenter)

	private val source by arg<MangaSource>(ARG_SOURCE)

	override fun onRequestMoreItems(offset: Int) {
		presenter.loadList(source, offset)
	}

	override fun getTitle(): CharSequence? {
		return source.title
	}

	override fun onFilterChanged(filter: MangaFilter) {
		presenter.applyFilter(source, filter)
		super.onFilterChanged(filter)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.opt_remote, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.action_search_internal -> {
			context?.startActivity(SearchActivity.newIntent(requireContext(), source, null))
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	companion object {

		private const val ARG_SOURCE = "provider"

		fun newInstance(provider: MangaSource) = RemoteListFragment().withArgs(1) {
			putParcelable(ARG_SOURCE, provider)
		}
	}
}