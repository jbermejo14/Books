package com.bolskapps.libri.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Insert
    suspend fun insert(session: ReadingSession): Long

    /** Newest first; the dashboard only ever needs a recent window. */
    @Query("SELECT * FROM reading_sessions WHERE recordedAt >= :since ORDER BY recordedAt DESC")
    fun getSessionsSince(since: Long): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY recordedAt DESC")
    fun getSessionsForBook(bookId: Int): Flow<List<ReadingSession>>

    @Query("SELECT COALESCE(SUM(pagesRead), 0) FROM reading_sessions WHERE recordedAt >= :since")
    fun getPagesReadSince(since: Long): Flow<Int>

    @Query("DELETE FROM reading_sessions WHERE bookId = :bookId")
    suspend fun deleteForBook(bookId: Int)
}

@Dao
interface ReadingGoalDao {

    @Query("SELECT * FROM reading_goals WHERE year = :year")
    fun getGoal(year: Int): Flow<ReadingGoal?>

    /** REPLACE so setting a goal twice in the same year updates rather than fails. */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: ReadingGoal)

    @Query("DELETE FROM reading_goals WHERE year = :year")
    suspend fun clear(year: Int)
}
