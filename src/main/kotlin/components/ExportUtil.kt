package components

import models.ManagementZone
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import utils.Utils
import java.io.FileOutputStream

fun exportManagementZoneSplits() {
    println("Enter the tenant URL")
    val url = readLine().takeIf { it?.isNotBlank() == true }
    println("Enter the token")
    val token = readLine().takeIf { it?.isNotBlank() == true }
    if (url != null && token != null) {
        println("Processing. Please wait")
        val mzData = Utils.fetchMZs(url, token)
        val filteredList = mzData.filter { it.name.contains("APP", ignoreCase = true) }

        println("Enter the excel output path.")
        val filePath = readLine().takeIf { it?.isNotBlank() == true }
        println("Enter the excel fileName without .xlsx extension.")
        val fileName = readLine().takeIf { it?.isNotBlank() == true }

        if (!filePath.isNullOrBlank() || ! fileName.isNullOrBlank()) {
            exportMZSplitToExcel(filteredList, "$filePath/$fileName.xlsx")
            println("Completed excel exported to $filePath/$fileName.xlsx\n")
        } else {
            println("Provide the correct details.")
        }
    } else {
        println("Provide the correct details.")
    }
}

private fun exportMZSplitToExcel(data: List<ManagementZone>, fileName: String) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Management Name Splits")

    with(sheet.createRow(0)) {
        createCell(0).setCellValue("ID")
        createCell(1).setCellValue("Name")
    }

    for ((rowIndex, item) in data.withIndex()) {
        val row = sheet.createRow(rowIndex + 1)
        val (firstPart, lastPart) = item.name.split("_")

        with(row) {
            createCell(0).setCellValue(firstPart)
            createCell(1).setCellValue(lastPart)
        }
    }

    FileOutputStream(fileName).use {
        workbook.write(it)
    }
}