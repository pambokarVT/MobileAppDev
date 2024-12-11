package edu.vt.cs5254.fancygallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.vt.cs5254.fancygallery.api.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "GalleryViewModel"

class MainViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()

    private val _galleryItems: MutableStateFlow<List<GalleryItem>> =
        MutableStateFlow(emptyList())
    val galleryItems: StateFlow<List<GalleryItem>>
        get() = _galleryItems.asStateFlow()

    fun reloadGalleryItems() {
        viewModelScope.launch {
            _galleryItems.value = emptyList() // Clear the list
            _galleryItems.update {
                loadPhotos() // Fetch new photos
            }
        }
    }

    private suspend fun loadPhotos(): List<GalleryItem> {
        return try {
            val items = photoRepository.fetchPhotos(99)
            Log.d("GalleryViewModel", "Items received: $items")
            items
        } catch (ex: Exception) {
            Log.e("GalleryViewModel", "Failed to fetch gallery items", ex)
            emptyList()
        }
    }

    init {
        viewModelScope.launch {
            _galleryItems.value = loadPhotos()
        }
    }
}