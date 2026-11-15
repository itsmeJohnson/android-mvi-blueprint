package com.mvi.blueprint.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mvi.blueprint.data.database.dao.OrderDao
import com.mvi.blueprint.data.database.entity.OrderEntity

@Database(
    entities = [OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OrderDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
