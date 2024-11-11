package com.example.poomagnet.mangaRepositoryManager

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

enum class Tag(val full_name: String){
    Romance("Romance"),
    Magical_Girls("Magical Girls"),
    Boys_Love("Boys Love"),
    Animals("Animals"),
    Monsters("Monsters"),
    Demons("Demons"),
    Medical("Medical"),
    Cooking("Cooking"),
    Shota("Shota"),
    Full_Color("Full Color"),
    Aliens("Aliens"),
    Doujinshi("Doujinshi"),
    Wuxia("Wuxia"),
    Fantasy("Fantasy"),
    Incest("Incest"),
    Thriller("Thriller"),
    Crime("Crime"),
    Survival("Survival"),
    Office_Workers("Office Workers"),
    Sci_Fi("Sci Fi"),
    Gyaru("Gyaru"),
    Ghosts("Ghosts"),
    Villainess("Villainess"),
    Post_Apocalyptic("Post Apocalyptic"),
    Vampires("Vampires"),
    Video_Games("Video Games"),
    Magic("Magic"),
    Crossdressing("Crossdressing"),
    Police("Police"),
    Sports("Sports"),
    Music("Music"),
    Military("Military"),
    Time_Travel("Time Travel"),
    Reincarnation("Reincarnation"),
    Action("Action"),
    Self_Published("Self Published"),
    Isekai("Isekai"),
    Martial_Arts("Martial Arts"),
    Official_Colored("Official Colored"),
    Loli("Loli"),
    Four_Koma("Four Koma"),
    Horror("Horror"),
    Superhero("Superhero"),
    Drama("Drama"),
    Tragedy("Tragedy"),
    Delinquents("Delinquents"),
    Adventure("Adventure"),
    Harem("Harem"),
    Zombies("Zombies"),
    Mecha("Mecha"),
    Supernatural("Supernatural"),
    Mystery("Mystery"),
    Reverse_Harem("Reverse Harem"),
    Sexual_Violence("Sexual Violence"),
    School_Life("School Life"),
    Anthology("Anthology"),
    Slice_of_Life("Slice of Life"),
    Long_Strip("Long Strip"),
    Comedy("Comedy"),
    Web_Comic("Web Comic"),
    Virtual_Reality("Virtual Reality"),
    Gore("Gore"),
    Oneshot("Oneshot"),
    Mafia("Mafia"),
    Adaptation("Adaptation"),
    Girls_Love("Girls Love"),
    Monster_Girls("Monster Girls"),
    Award_Winning("Award Winning"),
    Historical("Historical"),
    Psychological("Psychological"),
    Ninja("Ninja"),
    Traditional_Games("Traditional Games"),
    Philosophical("Philosophical"),
    Samurai("Samurai"),
    Genderswap("Genderswap"),
    Fan_Colored("Fan Colored");

    companion object {
        fun fromValue(value: String): Tag? {
            return entries.find { it.full_name.lowercase() == value.lowercase() }
        }
    }

    override fun toString(): String {
        return super.toString().replace("_", " ")
    }
}

class TagDeserializer : JsonDeserializer<Tag> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Tag {
        val value = json.asString
        return Tag.fromValue(value) ?: throw JsonParseException("Unknown tag: $value")
    }
}


enum class Ordering(val msg: String){
    Title("order[title]"),
    Year("order[year]"),
    Created_At("order[createdAt]"),
    Updated_At("order[updatedAt]"),
    Latest_Chapter("order[latestUploadedChapter]"),
    Followed_Count("order[followedCount]"),
    Relevance("order[relevance]");

    override fun toString(): String {
        return super.toString().replace("_", " ")
    }
}

enum class Demographic(val msg: String){
    Shounen("shounen"),
    Shoujo("shoujo"),
    Josei("josei"),
    Seinen("seinen")
}

enum class ContentRating(val msg: String){
    Safe("safe"),
    Suggestive("suggestive"),
    Erotica("erotica"),
    Pornographic("pornographic")
}