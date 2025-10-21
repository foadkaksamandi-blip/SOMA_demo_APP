package shared.utils

import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

/**
 * DateUtils — تاریخ شمسی + تولید کد تراکنش آفلاین
 * خروجی‌ها:
 *  - nowJalaliDate(): تاریخ شمسی کوتاه 1402/07/01
 *  - nowJalaliDateTime(): تاریخ/ساعت شمسی 1402/07/01 12:34:56
 *  - generateTxId(): SOMA-YYMMDD-HHMMSS-XXXX-CC
 */
object DateUtils {

    /** تاریخ شمسی فعلی به صورت YYYY/MM/DD */
    fun nowJalaliDate(): String {
        val c = Calendar.getInstance()
        val res = gregorianToJalali(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
        return "%04d/%02d/%02d".format(Locale.US, res[0], res[1], res[2])
    }

    /** تاریخ و ساعت شمسی فعلی به صورت YYYY/MM/DD HH:MM:SS */
    fun nowJalaliDateTime(): String {
        val c = Calendar.getInstance()
        val res = gregorianToJalali(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        val s = c.get(Calendar.SECOND)
        return "%04d/%02d/%02d %02d:%02d:%02d".format(Locale.US, res[0], res[1], res[2], h, m, s)
    }

    /** تولید کد تراکنش: SOMA-YYMMDD-HHMMSS-XXXX-CC (CC = mod97) */
    fun generateTxId(): String {
        val c = Calendar.getInstance()
        val res = gregorianToJalali(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
        val yy = (res[0] % 100)
        val MM = res[1]
        val dd = res[2]
        val HH = c.get(Calendar.HOUR_OF_DAY)
        val mm = c.get(Calendar.MINUTE)
        val ss = c.get(Calendar.SECOND)
        val rnd = (1000..9999).random()

        val core = "%02d%02d%02d%02d%02d%02d%04d".format(Locale.US, yy, MM, dd, HH, mm, ss, rnd)
        val cc = mod97(core)
        return "SOMA-%02d%02d%02d-%02d%02d%02d-%04d-%02d".format(Locale.US, yy, MM, dd, HH, mm, ss, rnd, cc)
    }

    private fun mod97(s: String): Int {
        var acc = 0L
        for (ch in s) {
            acc = (acc * 10 + (ch.code - 48)) % 97
        }
        return acc.toInt()
    }

    /**
     * تبدیل میلادی به جلالی — پیاده‌سازی سبک (JDN مبنا)
     * خروجی: [year, month, day]
     */
    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val g_d_m = intArrayOf(0, 31, if (isLeapGregorian(gy)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1
        var g_day_no = 365 * gy2 + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400)
        for (i in 1..gm2) g_day_no += g_d_m[i]
        g_day_no += gd2
        var j_day_no = g_day_no - 79
        val j_np = j_day_no / 12053
        j_day_no %= 12053
        var jy = 979 + 33 * j_np + 4 * (j_day_no / 1461)
        j_day_no %= 1461
        if (j_day_no >= 366) {
            jy += (j_day_no - 1) / 365
            j_day_no = (j_day_no - 1) % 365
        }
        val jm = if (j_day_no < 186) 1 + j_day_no / 31 else 7 + (j_day_no - 186) / 30
        val jd = 1 + if (j_day_no < 186) (j_day_no % 31) else ((j_day_no - 186) % 30)
        return intArrayOf(jy, jm, jd)
    }

    private fun isLeapGregorian(y: Int): Boolean {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
    }
}
