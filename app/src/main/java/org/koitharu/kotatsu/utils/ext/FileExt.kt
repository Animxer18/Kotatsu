package org.koitharu.kotatsu.utils.ext

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import org.koitharu.kotatsu.R
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Suppress("NOTHING_TO_INLINE")
inline fun File.sub(name: String) = File(this, name)

fun File.takeIfReadable() = takeIf { it.exists() && it.canRead() }

fun ZipFile.readText(entry: ZipEntry) = getInputStream(entry).bufferedReader().use {
	it.readText()
}

fun File.computeSize(): Long = listFiles()?.sumByLong { x ->
	if (x.isDirectory) {
		x.computeSize()
	} else {
		x.length()
	}
} ?: 0L

inline fun File.findParent(predicate: (File) -> Boolean): File? {
	var current = this
	while (!predicate(current)) {
		current = current.parentFile ?: return null
	}
	return current
}

fun File.getStorageName(context: Context): String = runCatching {
	val manager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		manager.getStorageVolume(this)?.getDescription(context)?.let {
			return@runCatching it
		}
	}
	when {
		Environment.isExternalStorageEmulated(this) -> context.getString(R.string.internal_storage)
		Environment.isExternalStorageRemovable(this) -> context.getString(R.string.external_storage)
		else -> null
	}
}.getOrNull() ?: context.getString(R.string.other_storage)

fun Uri.toFileOrNull() = if (scheme == "file") path?.let(::File) else null