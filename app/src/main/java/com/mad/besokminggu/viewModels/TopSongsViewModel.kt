package com.mad.besokminggu.viewModels;

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.repositories.UnprotectedRepository
import com.mad.besokminggu.network.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TopSongsViewModel @Inject constructor(
    private val unprotectedRepository: UnprotectedRepository,
): BaseViewModel() {

    private val _topSongs = MutableLiveData<ApiResponse<List<OnlineSong>>>()
    val topSongs: MutableLiveData<ApiResponse<List<OnlineSong>>>
        get() = _topSongs

    fun getTopSongsGlobal(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _topSongs,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.topSongsGlobal()
    }

    fun getTopSongsCountry(country: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _topSongs,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.topSongsCountry(country)
    }
}
