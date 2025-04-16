package com.vlr.gsheetsync.feature.sheets.domain.model


abstract class GSyncResult<out Data> {
    data class Success<out Data>(val data: Data, val message: String? = null): GSyncResult<Data>()
    class Loading(val loading: Boolean): GSyncResult<Nothing>()
    class Error(val message: IGSyncError): GSyncResult<Nothing>()
}