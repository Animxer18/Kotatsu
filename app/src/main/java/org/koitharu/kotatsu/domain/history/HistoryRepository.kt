package org.koitharu.kotatsu.domain.history

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.entity.HistoryEntity
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.core.db.entity.TagEntity
import org.koitharu.kotatsu.core.model.Manga
import org.koitharu.kotatsu.core.model.MangaHistory
import org.koitharu.kotatsu.domain.tracking.TrackingRepository
import java.util.*

class HistoryRepository : KoinComponent {

	private val db: MangaDatabase by inject()
	private val trackingRepository by lazy(::TrackingRepository)

	suspend fun getList(offset: Int, limit: Int = 20): List<Manga> {
		val entities = db.historyDao.findAll(offset, limit)
		return entities.map { it.manga.toManga(it.tags.map(TagEntity::toMangaTag).toSet()) }
	}

	suspend fun addOrUpdate(manga: Manga, chapterId: Long, page: Int, scroll: Float) {
		val tags = manga.tags.map(TagEntity.Companion::fromMangaTag)
		db.withTransaction {
			db.tagsDao.upsert(tags)
			db.mangaDao.upsert(MangaEntity.from(manga), tags)
			db.historyDao.upsert(
				HistoryEntity(
					mangaId = manga.id,
					createdAt = System.currentTimeMillis(),
					updatedAt = System.currentTimeMillis(),
					chapterId = chapterId,
					page = page,
					scroll = scroll
				)
			)
			trackingRepository.upsert(manga)
		}
		notifyHistoryChanged()
	}

	suspend fun getOne(manga: Manga): MangaHistory? {
		return db.historyDao.find(manga.id)?.let {
			MangaHistory(
				createdAt = Date(it.createdAt),
				updatedAt = Date(it.updatedAt),
				chapterId = it.chapterId,
				page = it.page,
				scroll = it.scroll
			)
		}
	}

	suspend fun clear() {
		db.historyDao.clear()
		notifyHistoryChanged()
	}

	suspend fun delete(manga: Manga) {
		db.historyDao.delete(manga.id)
		notifyHistoryChanged()
	}

	/**
	 * Try to replace one manga with another one
	 * Useful for replacing saved manga on deleting it with remove source
	 */
	suspend fun deleteOrSwap(manga: Manga, alternative: Manga?) {
		if (alternative == null || db.mangaDao.update(MangaEntity.from(alternative)) <= 0) {
			db.historyDao.delete(manga.id)
			notifyHistoryChanged()
		}
	}

	companion object {

		private val listeners = HashSet<OnHistoryChangeListener>()

		fun subscribe(listener: OnHistoryChangeListener) {
			listeners += listener
		}

		fun unsubscribe(listener: OnHistoryChangeListener) {
			listeners -= listener
		}

		private suspend fun notifyHistoryChanged() {
			withContext(Dispatchers.Main) {
				listeners.forEach { x -> x.onHistoryChanged() }
			}
		}
	}
}