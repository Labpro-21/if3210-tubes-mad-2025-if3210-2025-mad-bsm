data class MonthlySummaryCapsule(
    val month: String,
    val totalMinutes: Int?,
    val topArtist: String?,
    val topSong: String?,
    val topArtistImageRes: Int? = null,
    val topSongImageRes: Int? = null,
    val isEmpty: Boolean = false
)

