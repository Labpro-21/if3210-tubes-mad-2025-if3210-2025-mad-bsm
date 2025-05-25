import java.text.SimpleDateFormat
import java.util.Locale

data class MonthlySummaryCapsule(
    val month: String,
    val totalMinutes: Int?,
    val topArtist: String?,
    val topSong: String?,
    val topArtistsList: List<String> = emptyList(),
    val topSongsList: List<String> = emptyList(),
    val topArtistCover: String? = null,
    val topSongCover: String? = null,
    val isEmpty: Boolean = false

)

fun MonthlySummaryCapsule.getMonthKeyFormat(): String {
    return try {
        val inputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val parsedDate = inputFormat.parse(this.month)
        outputFormat.format(parsedDate!!)
    } catch (e: Exception) {
        this.month
    }
}

