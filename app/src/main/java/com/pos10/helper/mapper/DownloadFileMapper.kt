package com.pos10.helper.mapper

import com.pos10.db.entity.DownloadedFileEntity
import com.pos10.model.remote.DownloadFileResponse

object DownloadFileMapper {

    // 🔽 API Data -> Entity
    fun mapToEntity(file: DownloadFileResponse.Data, requestId: String,requestType:String): DownloadedFileEntity {
        return DownloadedFileEntity(
            requestId = requestId,
            filePath = file.filePath,
            originalFileName = file.originalFileName,
            uploadType = file.uploadType,
            requestType = requestType,
            id = file.id
            )
    }

    // 🔽 API Data List -> Entity List
    fun mapListToEntityList(
        files: List<DownloadFileResponse.Data>,
        requestId: String,
        requestType: String

    ): List<DownloadedFileEntity> {
        return files.map { mapToEntity(it, requestId,requestType) }
    }

    // 🔼 Entity -> API Data
    fun mapEntityToModel(entity: DownloadedFileEntity): DownloadFileResponse.Data {
        return DownloadFileResponse.Data(
            filePath = entity.filePath,
            originalFileName = entity.originalFileName,
            uploadType = entity.uploadType,
            id = entity.id
        )
    }

    // 🔼 Entity List -> API Data List
    fun mapEntityListToModelList(list: List<DownloadedFileEntity>): List<DownloadFileResponse.Data> {
        return list.map { mapEntityToModel(it) }
    }

    // 🔼 Entity List -> Full API Response (optional, if needed)
    fun mapEntityListToResponse(
        list: List<DownloadedFileEntity>,
        info: DownloadFileResponse.Info
    ): DownloadFileResponse {
        return DownloadFileResponse(
            data = ArrayList(mapEntityListToModelList(list)),
            info = info
        )
    }
}
