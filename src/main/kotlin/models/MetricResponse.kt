package models

data class MetricResponse(
    val totalCount: Int,
    val nextPageKey: String?,
    val resolution: String,
    val result: List<Result>
)

data class Result(
    val metricId: String,
    val dataPointCountRatio: Double,
    val dimensionCountRatio: Double,
    val data: List<Data>?
)

data class Data(
    val dimensions: List<String>,
    val dimensionMap: Map<String, Any>,
    val timestamps: List<Long?>?,
    val values: List<Double?>?
)
