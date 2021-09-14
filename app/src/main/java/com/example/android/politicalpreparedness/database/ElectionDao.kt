package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Query("select * from election_table")
    fun getAll(): LiveData<List<Election>>

    @Query("select * from election_table where id = :id")
    suspend fun get(id: Int): Election?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(election: Election)

    @Query("delete from election_table")
    suspend fun clear()

    @Delete
    suspend fun delete(election: Election)
}