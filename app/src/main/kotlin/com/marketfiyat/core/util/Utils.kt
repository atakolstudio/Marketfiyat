package com.marketfiyat.core.util

enum class UnitType(val displayName: String, val shortName: String) {
    GRAM("Gram", "g"),
    KILOGRAM("Kilogram", "kg"),
    MILLILITRE("Mililitre", "ml"),
    LITRE("Litre", "L"),
    PIECE("Adet", "adet");

    companion object {
        fun fromString(value: String): UnitType {
            return entries.find { it.name == value } ?: GRAM
        }
    }
}

object UnitPriceCalculator {

    fun calculateUnitPrices(
        price: Double,
        quantity: Double,
        unit: UnitType
    ): UnitPrices {
        return when (unit) {
            UnitType.GRAM -> {
                val perKg = if (quantity > 0) (price / quantity) * 1000 else null
                val per100g = if (quantity > 0) (price / quantity) * 100 else null
                UnitPrices(perKg = perKg, per100g = per100g)
            }
            UnitType.KILOGRAM -> {
                val perKg = if (quantity > 0) price / quantity else null
                val per100g = perKg?.let { it / 10 }
                UnitPrices(perKg = perKg, per100g = per100g)
            }
            UnitType.MILLILITRE -> {
                val perLitre = if (quantity > 0) (price / quantity) * 1000 else null
                UnitPrices(perLitre = perLitre)
            }
            UnitType.LITRE -> {
                val perLitre = if (quantity > 0) price / quantity else null
                UnitPrices(perLitre = perLitre)
            }
            UnitType.PIECE -> {
                val perPiece = if (quantity > 0) price / quantity else null
                UnitPrices(perPiece = perPiece)
            }
        }
    }

    fun getComparableUnitPrice(
        price: Double,
        quantity: Double,
        unit: UnitType
    ): Double? {
        val prices = calculateUnitPrices(price, quantity, unit)
        return prices.perKg ?: prices.perLitre ?: prices.perPiece
    }
}

data class UnitPrices(
    val perKg: Double? = null,
    val per100g: Double? = null,
    val perLitre: Double? = null,
    val perPiece: Double? = null
)

object NotificationChannels {
    const val PRICE_ALERT = "price_alert"
    const val BACKUP = "backup"
}

object Constants {
    const val PRICE_ANALYSIS_WINDOW_DAYS = 180L // 6 months
    const val MIN_PRICES_FOR_ANALYSIS = 2
    const val SIGNIFICANT_PRICE_CHANGE_PERCENT = 5.0
    const val MAX_BARCODE_CACHE_AGE_DAYS = 30L
    const val BACKUP_WORKER_TAG = "backup_worker"
    const val PRICE_ALARM_WORKER_TAG = "price_alarm_worker"
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_CHART_DATA_POINTS = 30
}

fun Double.formatPrice(): String {
    return if (this == kotlin.math.floor(this)) {
        "%.0f TL".format(this)
    } else {
        "%.2f TL".format(this)
    }
}

fun Double.formatPercent(): String {
    return "%.1f%%".format(this)
}

fun Double.roundToTwoDecimals(): Double {
    return kotlin.math.round(this * 100) / 100.0
}
