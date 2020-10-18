package org.koitharu.kotatsu.ui.list.favourites.categories.select

import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_category_checkable.*
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.FavouriteCategory
import org.koitharu.kotatsu.ui.base.list.BaseViewHolder

class CategoryCheckableHolder(parent: ViewGroup) :
	BaseViewHolder<FavouriteCategory, Boolean>(parent, R.layout.item_category_checkable) {

	override fun onBind(data: FavouriteCategory, extra: Boolean) {
		checkedTextView.text = data.title
		checkedTextView.isChecked = extra
	}
}