package com.pos10.view

import com.pos10.model.remote.GetWorkListResponse

object SelectedRequestHolder {
    var selectedItemList: GetWorkListResponse.Data.Wo? = null
    var selectedSUbWorkItemList: GetWorkListResponse.Data.Wo.WoRequest? = null
    var selectedItemCheckList: List<GetWorkListResponse.Data.Checklist>? = null

    fun clearRequest() {
       selectedItemList=null
        selectedItemCheckList=null
    }
}