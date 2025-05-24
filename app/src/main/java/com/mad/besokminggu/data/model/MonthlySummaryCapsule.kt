data class MonthlySummaryCapsule(
    val month: String,
    val totalMinutes: Int?,
    val topArtist: String?,
    val topSong: String?,
    val topArtistCover: String? = null,
    val topSongCover: String? = null,
    val isEmpty: Boolean = false
)
