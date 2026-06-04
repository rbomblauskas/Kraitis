package io.github.rbomblauskas.kraitis.domain

import io.github.rbomblauskas.kraitis.data.ClothingCondition
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.data.Season
import java.util.Calendar

private const val DAY_MS = 24L * 60 * 60 * 1000

// thresholds picked by feel, easy to tune later
private const val RECENT_WEAR_DAYS = 30
private const val NEVER_WORN_NUDGE_DAYS = 30
private const val REWEAR_AFTER_DAYS = 60
private const val LET_GO_AFTER_DAYS = 150
private const val REPAIR_WORTH_CENTS = 3000L

data class Decision(
    val action: ClothingStatus,
    val reasons: List<String>
)

fun costPerWearCents(priceCents: Long?, wearCount: Int): Long? = when {
    priceCents == null -> null
    wearCount == 0 -> priceCents
    else -> priceCents / wearCount
}

// rough split for lithuania, warm months are april-september
fun currentSeason(now: Long): Season {
    val month = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.MONTH) + 1
    return if (month in 4..9) Season.WARM else Season.COLD
}

fun decideNextAction(
    item: ClothingItem,
    wearCount: Int,
    lastWornAt: Long?,
    now: Long = System.currentTimeMillis()
): Decision {
    val daysSinceWorn = lastWornAt?.let { ((now - it) / DAY_MS).toInt() }
    // never worn items are judged from the day they were added
    val daysUnused = daysSinceWorn ?: ((now - item.createdAt) / DAY_MS).toInt()
    val unusedText =
        if (lastWornAt == null) "never worn since adding" else "not worn for $daysUnused days"

    if (item.condition == ClothingCondition.DAMAGED) {
        return if (item.sentimental || (item.priceCents ?: 0) >= REPAIR_WORTH_CENTS) {
            Decision(
                ClothingStatus.REPAIR,
                listOf(
                    "condition is damaged",
                    if (item.sentimental) "sentimental item, worth fixing"
                    else "price is high enough that repair pays off"
                )
            )
        } else {
            Decision(
                ClothingStatus.DONATE,
                listOf("condition is damaged", "cheap item, repair is not worth it")
            )
        }
    }

    // was sent to repair and the condition is not damaged anymore
    if (item.status == ClothingStatus.REPAIR) {
        return Decision(
            ClothingStatus.REWEAR,
            listOf("repair looks done", "start wearing it again")
        )
    }

    if (item.season != Season.ALL_YEAR && item.season != currentSeason(now)) {
        return Decision(
            ClothingStatus.ACTIVE,
            listOf("${item.season.label} item, off season now", "decide again when its season starts")
        )
    }

    if (daysSinceWorn != null && daysSinceWorn <= RECENT_WEAR_DAYS) {
        return Decision(
            ClothingStatus.ACTIVE,
            listOf("worn $daysSinceWorn days ago", "still in normal rotation")
        )
    }

    if (daysUnused >= LET_GO_AFTER_DAYS) {
        if (item.sentimental) {
            return Decision(
                ClothingStatus.ARCHIVED,
                listOf(unusedText, "sentimental, keep it stored instead of selling")
            )
        }
        if (sellReady(item)) {
            val cpw = costPerWearCents(item.priceCents, wearCount)
            return Decision(
                ClothingStatus.SELL,
                listOfNotNull(
                    unusedText,
                    "condition is ${item.condition.label} and price is known, easy to sell",
                    cpw?.let { "cost per wear so far is ${it / 100}.${(it % 100).toString().padStart(2, '0')}" }
                )
            )
        }
        return Decision(
            ClothingStatus.DONATE,
            listOf(unusedText, "worn out or no price set, donating is simpler than selling")
        )
    }

    if (daysUnused >= REWEAR_AFTER_DAYS) {
        return Decision(
            ClothingStatus.REWEAR,
            listOf(unusedText, "give it one more chance before letting it go")
        )
    }

    return Decision(ClothingStatus.ACTIVE, listOf("nothing stands out, keep using it"))
}

private fun sellReady(item: ClothingItem): Boolean =
    item.priceCents != null &&
        (item.condition == ClothingCondition.NEW || item.condition == ClothingCondition.GOOD)
