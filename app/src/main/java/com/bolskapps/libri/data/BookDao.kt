package com.bolskapps.libri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Room stores [ReadingStatus] as its enum name so the column stays readable in a DB dump. */
class Converters {
    @TypeConverter
    fun fromStatus(status: ReadingStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ReadingStatus = ReadingStatus.fromName(value)
}

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    /** One query per shelf, matching the tab it backs. */
    @Query("SELECT * FROM books WHERE status = :status ORDER BY addedAt DESC")
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>>

    /** "In Progress" — most recently touched first, so the dashboard picks a sensible hero. */
    @Query("SELECT * FROM books WHERE status = 'READING' ORDER BY addedAt DESC")
    fun getReadingBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'TO_READ' ORDER BY addedAt DESC")
    fun getToReadBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'FINISHED' ORDER BY addedAt DESC")
    fun getFinishedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookStream(id: Int): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    /** Prevents adding the same Open Library work twice. */
    @Query("SELECT * FROM books WHERE openLibraryKey = :key LIMIT 1")
    suspend fun getBookByOpenLibraryKey(key: String): Book?

    /** Books finished inside a window — the numerator of the yearly reading goal. */
    @Query(
        """
        SELECT * FROM books
        WHERE status = 'FINISHED' AND finishedAt >= :from AND finishedAt < :until
        ORDER BY finishedAt DESC
        """
    )
    fun getBooksFinishedBetween(from: Long, until: Long): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)
}
