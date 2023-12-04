package utils

import com.google.gson.Gson
import models.ManagementZone
import models.MetricExcel
import models.MetricResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.json.JSONObject
import java.io.FileOutputStream
import java.net.URL

object Utils {
    private fun parseMZJson(responseBody: String?): List<ManagementZone> {
        return responseBody?.let {
            val jsonObject = JSONObject(it)
            val valuesArray = jsonObject.optJSONArray("values")
            val dataList = mutableListOf<ManagementZone>()
            for (i in 0..<valuesArray.length()) {
                val valueObject = valuesArray.getJSONObject(i)
                val id = valueObject.optString("id")
                val name = valueObject.optString("name")
                val dataObject = ManagementZone(id, name)
                dataList.add(dataObject)
            }
            dataList
        } ?: emptyList()
    }

    fun fetchMZs(url: String, token: String): List<ManagementZone> {
        println(url)
        println("Fetching management zones.")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${url}/api/config/v1/managementZones")
            .header("Authorization", "Api-Token $token")
            .build()
        val response = client.newCall(request).execute()
        return if (response.isSuccessful) {
            val responseBody = response.body?.string()
            println("Fetching management zones completed.")
            parseMZJson(responseBody)
        } else {
            println("Error: ${response.code} - ${response.message}")
            response.close()
            emptyList()
        }
    }

    fun fetchLicences(url: String, token: String, mzId: String): MetricResponse? {
        try {
            val client = OkHttpClient()
            val params = mapOf(
                "metricSelector" to "builtin:billing.full_stack_monitoring.usage_per_host:splitBy():value:sort(value(sum,descending)):limit(1)",
                "resolution" to "1h",
                "mzSelector" to "mzId(${mzId})"
            )
            val urlWithParams = buildUrlWithParams("${url}/api/v2/metrics/query", params)
            val request = Request.Builder()
                .url(urlWithParams)
                .header("Authorization", "Api-Token $token")
                .build()
            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Gson().fromJson(responseBody, MetricResponse::class.java)
            } else {
                println("Error: ${response.code} - fetch Licences failed")
                response.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun fetchDEM(url: String, token: String, mzName: String): MetricResponse? {
        try {
            val client = OkHttpClient()
            val params = mapOf(
                "metricSelector" to "%28%28%28builtin%3Abilling.apps.web.sessionsWithReplayByApplication%3Afilter%28and%28eq%28Type%2CBilled%29%29%29%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.apps.web.sessionsWithoutReplayByApplication%3Afilter%28and%28eq%28Type%2CBilled%29%29%29%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.25%29%20%2B%20%28builtin%3Abilling.apps.mobile.sessionsWithReplayByApplication%3Afilter%28and%28eq%28Type%2CBilled%29%29%29%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.apps.mobile.sessionsWithoutReplayByApplication%3Afilter%28and%28eq%28Type%2CBilled%29%29%29%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.25%29%20%2B%20%28builtin%3Abilling.synthetic.actions%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.synthetic.requests%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.1%29%20%2B%20%28builtin%3Abilling.apps.mobile.userActionPropertiesByMobileApplication%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.01%29%20%2B%20%28builtin%3Abilling.apps.web.userActionPropertiesByApplication%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.01%29%20%2B%20%28builtin%3Abilling.synthetic.external%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%20%2A%200.1%29%29%29",
                "from" to "1684866600000",
                "to" to "1701283442086",
                "mzSelector" to "mzName(${mzName})"
            )
            val urlWithParams = buildUrlWithParams("${url}/api/v2/metrics/query", params)
            val request = Request.Builder()
                .url(urlWithParams)
                .header("Authorization", "Api-Token $token")
                .build()
            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Gson().fromJson(responseBody, MetricResponse::class.java)
            } else {
                println("Error: ${response.code} - fetch DEM failed")
                response.close()
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun fetchDEMDDU(url: String, token: String, mzId: String): MetricResponse? {
        try {
            val client = OkHttpClient()
            val params = mapOf(
                "metricSelector" to "%28%28builtin%3Abilling.ddu.metrics.total%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.ddu.log.total%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.ddu.serverless.total%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.ddu.events.total%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%20%2B%20%28builtin%3Abilling.ddu.traces.total%3AsplitBy%28%29%3Asum%3Adefault%280%29%3Afold%28sum%29%29%29",
                "from" to "1684866600000",
                "to" to "1701283442086",
                "mzSelector" to "mzId(${mzId})"
            )
            val urlWithParams = buildUrlWithParams("${url}/api/v2/metrics/query", params)
            val request = Request.Builder()
                .url(urlWithParams)
                .header("Authorization", "Api-Token $token")
                .build()
            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Gson().fromJson(responseBody, MetricResponse::class.java)
            } else {
                println("Error: ${response.code} - fetch DEM DUU failed")
                response.close()
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun buildUrlWithParams(baseUrl: String, params: Map<String, String>): String {
        val urlBuilder = StringBuilder(baseUrl)
        if (params.isNotEmpty()) {
            urlBuilder.append("?")
            params.forEach { (key, value) ->
                urlBuilder.append("$key=$value&")
            }
            urlBuilder.deleteCharAt(urlBuilder.length - 1)
        }
        return urlBuilder.toString()
    }



    fun exportMetricsToExcel(metrics: List<List<MetricExcel>>, filePath: String) {
        println("Exporting to excel.")
        print("Progress: 0%")
        val workbook: Workbook = WorkbookFactory.create(true)
        metrics.forEachIndexed { index, metricExcels ->
            val sheet: Sheet = workbook.createSheet(parseHostName(metricExcels[0].url))
            val headerRow: Row = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("URL")
            headerRow.createCell(1).setCellValue("MZ ID")
            headerRow.createCell(2).setCellValue("MZ Name")
            headerRow.createCell(3).setCellValue("Timestamp")
            headerRow.createCell(4).setCellValue("Licence Units")
            headerRow.createCell(5).setCellValue("DEM Units")
            headerRow.createCell(6).setCellValue("DDU Units")
            metricExcels.forEachIndexed { rowIndex, metric ->
                val dataRow: Row = sheet.createRow(rowIndex + 1)
                dataRow.createCell(0).setCellValue(metric.url)
                dataRow.createCell(1).setCellValue(metric.mzId)
                dataRow.createCell(2).setCellValue(metric.mzName)
                dataRow.createCell(3).setCellValue(metric.timestamp ?: "0.0")
                dataRow.createCell(4).setCellValue(metric.licenceUnits ?: "0.0")
                dataRow.createCell(5).setCellValue(metric.demUnits ?: "0.0")
                dataRow.createCell(6).setCellValue(metric.dduUnits ?: "0.0")
            }
            val percentageCompleted = ((index + 1).toDouble() / metrics.size) * 100
            print("\rProgress: %.2f%%".format(percentageCompleted))
        }
        workbook.write(FileOutputStream(filePath))
        workbook.close()
        println("\nExcel export completed to $filePath.")
        println("\n-------------------------------------------")
    }

    private fun parseHostName(inputUrl: String): String? {
        return try {
            val url = URL(inputUrl)
            url.host.split(".")[0]
        } catch (e: Exception) {
            println("Error parsing URL: ${e.message}")
            null
        }
    }

    fun nanoToMinutes(nanoTime: Long): Double {
        return nanoTime.toDouble() / 6000000000.0
    }
}