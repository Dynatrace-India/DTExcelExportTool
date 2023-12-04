package components

import models.ManagementZone
import models.MetricExcel
import models.Tenants
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import utils.Utils
import java.io.File
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val sheetList = mutableListOf<List<MetricExcel>>()

fun exportMetricsData() {
    println("Enter the tenant list excel file path")
    val filePath = readLine().takeIf { it?.isNotBlank() == true }

    if (filePath != null) {
        println("Enter the excel output path")
        val outputFilePath = readLine().takeIf { it?.isNotBlank() == true }
        println("Enter the excel fileName without .xlsx")
        val fileName = readLine().takeIf { it?.isNotBlank() == true }

        beginExport(filePath, "$outputFilePath/$fileName.xlsx")
    } else {
        println("Invalid filepath.")
    }
}

private fun beginExport(filePath: String, outputFilePath: String) {
    println("Parsing list of tenants from excel.")
    val file = File(filePath)
    val workbook = XSSFWorkbook(file)
    val sheet = workbook.getSheetAt(0)
    val tenantList = mutableListOf<Tenants>()
    for (row in sheet) {
        val url = row.getCell(0).stringCellValue
        val token = row.getCell(1).stringCellValue
        tenantList.add(Tenants(url, token))
    }
    getMZ(tenantList, outputFilePath)
}

private fun getMZ(tenantList: List<Tenants>, outputFilePath: String) {
    tenantList.take(1).forEach { tenant ->
        println("\n-------------------------------------------")
        if (tenant.url.isEmpty())
            return
        val startTime = System.nanoTime()
        fetchMetrics(Utils.fetchMZs(tenant.url, tenant.token), tenant.url, tenant.token)
        val endTime = System.nanoTime()
        println("Time Consumes: ${Utils.nanoToMinutes(endTime - startTime)}")
        println("\n-------------------------------------------")
    }
    Utils.exportMetricsToExcel(sheetList, outputFilePath)
}

private fun fetchMetrics(mzList: List<ManagementZone>, url: String, token: String) {
    if (mzList.isEmpty()) {
        println("No management zones retrieved.")
        return
    }
    print("Fetching licence metrics.\n")
    print("Progress: 0%")

    val metricExcelList = mzList.mapIndexed { index, zone ->
        print("\rProgress: %.2f%%".format((index + 1).toDouble() / mzList.size * 100))
        val metricExcel = MetricExcel(url, zone.id, zone.name)

            val licenceResponse =  Utils.fetchLicences(url, token, zone.id)
            val demResponse =  Utils.fetchDEM(url, token, zone.name)
            val dduResponse =  Utils.fetchDEMDDU(url, token, zone.id)

            //Licence API
            licenceResponse?.result?.let {
                val data = it.getOrNull(0)?.data.orEmpty()
                metricExcel.apply {
                    licenceUnits = data.getOrNull(0)?.values?.let { licenceValue->
                        // Check if values[1] is available, use it; otherwise, use values[0] if available
                        licenceValue.getOrNull(1)?.toString() ?: licenceValue.getOrNull(0)?.toString() ?: "0.0"
                    } ?: "0.0"
                    timestamp = data.getOrNull(0)?.timestamps?.getOrNull(1)?.let { timestamp->
                        convertTimestampToDayMonthYear(timestamp)
                    } ?: "NA"
                }
            }

            //DEM Units Api
            demResponse?.result?.let {
                val data = it.getOrNull(0)?.data.orEmpty()
                metricExcel.apply {
                    demUnits = (data.getOrNull(0)?.values?.getOrNull(0)?.toBigDecimal()?.setScale(2, RoundingMode.HALF_DOWN) ?: "0.0").toString()
                }
            }

            //DDU Units Api
            dduResponse?.result?.let {
                val data = it.getOrNull(0)?.data.orEmpty()
                metricExcel.apply {
                    dduUnits = (data.getOrNull(0)?.values?.getOrNull(0)?.toBigDecimal()?.setScale(2, RoundingMode.HALF_DOWN) ?: "0.0").toString()
                }
                metricExcel
            }

    }.filterNotNull()
    if (metricExcelList.isNotEmpty())
        sheetList.add(metricExcelList)
    println("\nFetching licence metrics completed.")
}

private fun convertTimestampToDayMonthYear(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    return localDateTime.format(formatter)
}

