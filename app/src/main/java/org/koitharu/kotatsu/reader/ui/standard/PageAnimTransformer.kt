package org.koitharu.kotatsu.reader.ui.standard

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class PageAnimTransformer : ViewPager2.PageTransformer {

	override fun transformPage(page: View, position: Float) {
		page.apply {
			val pageWidth = width
			when {
				position < -1 -> alpha = 0f
				position <= 0 -> { // [-1,0]
					alpha = 1f
					translationX = 0f
					translationZ = 0f
					scaleX = 1 + FACTOR * position
					scaleY = 1f
				}
				position <= 1 -> { // (0,1]
					alpha = 1f
					translationX = pageWidth * -position
					translationZ = -1f
					scaleX = 1f
					scaleY = 1f
				}
				else -> alpha = 0f
			}
		}
	}

	private companion object {

		const val FACTOR = 0.1f
	}
}