import components.exportManagementZoneSplits
import components.exportMetricsData
import components.showMenu

fun main(args: Array<String>) {
    println("-------Dynatrace Excel Export Tool---------")
    println("-------Developed by Ashish Zingade---------")
    println("-------------------------------------------")

    while (true) {
        showMenu()

        val inputOption = readLine()

        when (inputOption) {
            "1" -> exportManagementZoneSplits()
            "2" -> exportMetricsData()
            "9" -> break
            else -> println("Invalid option. Please try again.")
        }
    }
}