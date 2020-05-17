package org.koitharu.kotatsu.ui.reader.wetoon

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.android.synthetic.main.fragment_reader_webtoon.*
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.MangaPage
import org.koitharu.kotatsu.ui.reader.ReaderState
import org.koitharu.kotatsu.ui.reader.base.AbstractReader
import org.koitharu.kotatsu.ui.reader.base.BaseReaderAdapter
import org.koitharu.kotatsu.ui.reader.base.GroupedList
import org.koitharu.kotatsu.utils.ext.doOnCurrentItemChanged
import org.koitharu.kotatsu.utils.ext.findCenterViewPosition
import org.koitharu.kotatsu.utils.ext.firstItem
import org.koitharu.kotatsu.utils.ext.withArgs

class WebtoonReaderFragment : AbstractReader(R.layout.fragment_reader_webtoon) {

	private val scrollInterpolator = AccelerateDecelerateInterpolator()
	private var paginationListener: ListPaginationListener? = null

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		paginationListener = ListPaginationListener(2, this)
		recyclerView.setHasFixedSize(true)
		recyclerView.adapter = adapter
		recyclerView.addOnScrollListener(paginationListener!!)
		recyclerView.doOnCurrentItemChanged(::notifyPageChanged)
	}

	override fun onCreateAdapter(dataSet: GroupedList<Long, MangaPage>): BaseReaderAdapter {
		return WebtoonAdapter(dataSet, loader)
	}

	override fun onDestroyView() {
		paginationListener = null
		super.onDestroyView()
	}

	override val itemsCount: Int
		get() = adapter?.itemCount ?: 0

	override fun getCurrentItem(): Int {
		return recyclerView.findCenterViewPosition()
	}

	override fun setCurrentItem(position: Int, isSmooth: Boolean) {
		if (isSmooth) {
			recyclerView.smoothScrollToPosition(position)
		} else {
			recyclerView.firstItem = position
		}
	}

	override fun switchPageBy(delta: Int) {
		recyclerView.smoothScrollBy(
			0,
			(recyclerView.height * 0.9).toInt() * delta,
			scrollInterpolator
		)
	}

	override fun getCurrentPageScroll(): Int {
		return (recyclerView.findViewHolderForAdapterPosition(getCurrentItem()) as? WebtoonHolder)
			?.getScrollY() ?: 0
	}

	override fun restorePageScroll(position: Int, scroll: Int) {
		recyclerView.post {
			val holder = recyclerView.findViewHolderForAdapterPosition(position) ?: return@post
			(holder as WebtoonHolder).restoreScroll(scroll)
		}
	}

	companion object {

		fun newInstance(state: ReaderState) = WebtoonReaderFragment().withArgs(1) {
			putParcelable(ARG_STATE, state)
		}
	}
}