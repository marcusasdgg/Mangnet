package com.example.poomagnet.mangaRepositoryManager

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SimpleDateAdapter : JsonDeserializer<SimpleDate>, JsonSerializer<SimpleDate> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SimpleDate {
        val jsonObject = json?.asJsonObject
        return SimpleDate(
            jsonObject?.get("year")?.asInt ?: 0,
            jsonObject?.get("month")?.asInt ?: 0,
            jsonObject?.get("day")?.asInt ?: 0
        )
    }

    override fun serialize(src: SimpleDate, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("year", src.year)
        jsonObject.addProperty("month", src.month)
        jsonObject.addProperty("day", src.day)
        return jsonObject
    }
}

class SlimChapterAdapter : JsonDeserializer<SlimChapter>, JsonSerializer<SlimChapter> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SlimChapter {
        val jsonObject = json?.asJsonObject
        return SlimChapter(
            id = jsonObject?.get("id")?.asString ?: "",
            name = jsonObject?.get("name")?.asString ?: "",
            chapter = jsonObject?.get("chapter")?.asDouble ?: 0.0,
            volume = jsonObject?.get("volume")?.asDouble ?: 0.0,
            mangaId = jsonObject?.get("mangaId")?.asString ?: "",
            imageUrl = jsonObject?.get("imageUrl")?.asString ?: "",
            mangaName = jsonObject?.get("mangaName")?.asString ?: ""
        )
    }

    override fun serialize(src: SlimChapter, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", src.id)
        jsonObject.addProperty("name", src.name)
        jsonObject.addProperty("chapter", src.chapter)
        jsonObject.addProperty("volume", src.volume)
        jsonObject.addProperty("mangaId", src.mangaId)
        jsonObject.addProperty("imageUrl", src.imageUrl)
        jsonObject.addProperty("mangaName", src.mangaName)
        return jsonObject
    }
}

class ChapterContentsAdapter : JsonDeserializer<ChapterContents>, JsonSerializer<ChapterContents> {
    override fun serialize(src: ChapterContents, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()

        when (src) {
            is ChapterContents.Downloaded -> {
                jsonObject.addProperty("type", "Downloaded")
                jsonObject.add("imagePaths", context.serialize(src.imagePaths))
                jsonObject.addProperty("ifDone", src.ifDone)
            }
            is ChapterContents.Online -> {
                jsonObject.addProperty("type", "Online")
                jsonObject.add("imagePaths", context.serialize(src.imagePaths))
                jsonObject.addProperty("ifDone", src.ifDone)
            }
        }

        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChapterContents {
        val jsonObject = json.asJsonObject
        val imagePathsType = object : TypeToken<List<String>>() {}.type
        val imagePaths: List<String> = context.deserialize(jsonObject.get("imagePaths"), imagePathsType)
        val ifDone = jsonObject.get("ifDone").asBoolean

        return when (jsonObject.get("type").asString) {
            "Downloaded" -> ChapterContents.Downloaded(imagePaths, ifDone)
            "Online" -> ChapterContents.Online(imagePaths, ifDone)
            else -> throw JsonParseException("Unknown ChapterContents type")
        }
    }
}
