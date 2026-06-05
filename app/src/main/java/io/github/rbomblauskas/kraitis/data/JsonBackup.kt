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
