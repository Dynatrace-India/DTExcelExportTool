package models

data class MetricExcel(
    var url: String,
    var mzId: String,
    var mzName: String,
    var timestamp: String? = null,
    var licenceUnits: String? = null,
    var demUnits: String? =null,
    var dduUnits: String? = null
    )
