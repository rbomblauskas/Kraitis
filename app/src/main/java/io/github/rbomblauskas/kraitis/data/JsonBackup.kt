package io.github.rbomblauskas.kraitis.data

import org.json.JSONArray
import org.json.JSONObject

fun exportJson(items: List<ClothingItem>, events: List<WearEvent>): String {
    val root = JSONObject()
    root.put("version", 1)

    val itemsArr = JSONArray()
    items.forEach { item ->
        val o = JSONObject()
        o.put("id", item.id)
        o.put("name", item.name)
        o.put("category", item.category.name)
        o.put("condition", item.condition.name)
        item.priceCents?.let { o.put("priceCents", it) }
        o.put("status", item.status.name)
        item.photoPath?.let { o.put("photoPath", it) }
        o.put("season", item.season.name)
        o.put("sentimental", item.sentimental)
        o.put("createdAt", item.createdAt)
        itemsArr.put(o)
    }
    root.put("items", itemsArr)

    val eventsArr = JSONArray()
    events.forEach { event ->
        val o = JSONObject()
        o.put("id", event.id)
        o.put("itemId", event.itemId)
        o.put("wornAt", event.wornAt)
        eventsArr.put(o)
    }
    root.put("wearEvents", eventsArr)

    return root.toString(2)
}

// throws on broken json or unknown enum values, caller decides what to show
fun parseBackup(json: String): Pair<List<ClothingItem>, List<WearEvent>> {
    val root = JSONObject(json)

    val itemsArr = root.getJSONArray("items")
    val items = (0 until itemsArr.length()).map { i ->
        val o = itemsArr.getJSONObject(i)
        ClothingItem(
            id = o.getLong("id"),
            name = o.getString("name"),
            category = ClothingCategory.valueOf(o.getString("category")),
            condition = ClothingCondition.valueOf(o.getString("condition")),
            priceCents = if (o.has("priceCents")) o.getLong("priceCents") else null,
            status = ClothingStatus.valueOf(o.getString("status")),
            photoPath = if (o.has("photoPath")) o.getString("photoPath") else null,
            season = Season.valueOf(o.getString("season")),
            sentimental = o.getBoolean("sentimental"),
            createdAt = o.getLong("createdAt")
        )
    }

    val eventsArr = root.getJSONArray("wearEvents")
    val events = (0 until eventsArr.length()).map { i ->
        val o = eventsArr.getJSONObject(i)
        WearEvent(
            id = o.getLong("id"),
            itemId = o.getLong("itemId"),
            wornAt = o.getLong("wornAt")
        )
    }

    return items to events
}
