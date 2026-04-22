# Keep Moshi generated adapters
-keep class **JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Keep Retrofit service interfaces
-keep interface com.shayanhaque.politicstracker.data.remote.** { *; }

# Room
-keep class androidx.room.** { *; }
