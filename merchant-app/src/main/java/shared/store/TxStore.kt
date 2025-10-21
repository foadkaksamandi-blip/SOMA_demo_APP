package shared.store

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TxStore(context: Context) {

    private val prefs = context.getSharedPreferences("soma_store", Context.MODE_PRIVATE)

    data class Balances(
        var main: Long = 5_000_000L,
        var subsidy: Long = 0L,
        var emergency: Long = 0L,
        var cbdc: Long = 0L
    )

    data class Tx(
        val txId: String,
        val amount: Long,
        val ts: Long,
        val from: String,
        val to: String,
        val method: String,
        val type: String,
        val status: String
    )

    fun getBalances(): Balances {
        val json = prefs.getString("balances", null) ?: return Balances()
        return try {
            val o = JSONObject(json)
            Balances(
                main = o.optLong("main", 5_000_000L),
                subsidy = o.optLong("subsidy", 0L),
                emergency = o.optLong("emergency", 0L),
                cbdc = o.optLong("cbdc", 0L)
            )
        } catch (_: Exception) {
            Balances()
        }
    }

    fun saveBalances(bal: Balances) {
        val o = JSONObject()
            .put("main", bal.main)
            .put("subsidy", bal.subsidy)
            .put("emergency", bal.emergency)
            .put("cbdc", bal.cbdc)
        prefs.edit().putString("balances", o.toString()).apply()
    }

    fun addTx(tx: Tx) {
        val arr = JSONArray(prefs.getString("tx_list", "[]"))
        val o = JSONObject()
            .put("txId", tx.txId)
            .put("amount", tx.amount)
            .put("ts", tx.ts)
            .put("from", tx.from)
            .put("to", tx.to)
            .put("method", tx.method)
            .put("type", tx.type)
            .put("status", tx.status)
        arr.put(o)
        prefs.edit().putString("tx_list", arr.toString()).apply()
    }

    fun getTxList(): List<Tx> {
        val res = mutableListOf<Tx>()
        val arr = JSONArray(prefs.getString("tx_list", "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            res.add(
                Tx(
                    txId = o.optString("txId"),
                    amount = o.optLong("amount"),
                    ts = o.optLong("ts"),
                    from = o.optString("from"),
                    to = o.optString("to"),
                    method = o.optString("method"),
                    type = o.optString("type"),
                    status = o.optString("status")
                )
            )
        }
        return res
    }

    fun add(type: String, amount: Long): Balances {
        val b = getBalances()
        when (type) {
            "main" -> b.main += amount
            "subsidy" -> b.subsidy += amount
            "emergency" -> b.emergency += amount
            "cbdc" -> b.cbdc += amount
        }
        saveBalances(b)
        return b
    }

    fun hasSufficient(type: String, amount: Long): Boolean {
        val b = getBalances()
        return when (type) {
            "main" -> b.main >= amount
            "subsidy" -> b.subsidy >= amount
            "emergency" -> b.emergency >= amount
            "cbdc" -> b.cbdc >= amount
            else -> false
        }
    }
}
