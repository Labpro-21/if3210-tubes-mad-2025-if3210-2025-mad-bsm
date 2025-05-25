package com.mad.besokminggu.viewModels

import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.repositories.UnprotectedRepository
import com.mad.besokminggu.network.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnlineSongsViewModel @Inject constructor(
    private val unprotectedRepository: UnprotectedRepository,
): BaseViewModel() {

    private val _song = MutableLiveData<ApiResponse<OnlineSong>>()
    val song: MutableLiveData<ApiResponse<OnlineSong>>
        get() = _song

    fun getSongById(id: Int, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _song,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.getSongById(id)
    }
}