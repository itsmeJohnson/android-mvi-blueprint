package com.mvi.blueprint.data.di

import android.content.Context
import androidx.room.Room
import com.mvi.blueprint.data.database.OrderDatabase
import com.mvi.blueprint.data.database.dao.OrderDao
import com.mvi.blueprint.data.repository.OrderRepositoryImpl
import com.mvi.blueprint.domain.repository.OrderRepository
import com.mvi.blueprint.domain.usecase.BatchApproveOrdersUseCase
import com.mvi.blueprint.domain.usecase.GetOrderDetailUseCase
import com.mvi.blueprint.domain.usecase.ObserveOrdersUseCase
import com.mvi.blueprint.domain.usecase.RefreshOrdersUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private const val DATABASE_NAME = "enterprise_orders.db"

    @Provides
    @Singleton
    fun provideOrderDatabase(
        @ApplicationContext context: Context
    ): OrderDatabase {
        return Room.databaseBuilder(
            context,
            OrderDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideOrderDao(database: OrderDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideObserveOrdersUseCase(repository: OrderRepository): ObserveOrdersUseCase {
        return ObserveOrdersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideBatchApproveOrdersUseCase(repository: OrderRepository): BatchApproveOrdersUseCase {
        return BatchApproveOrdersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRefreshOrdersUseCase(repository: OrderRepository): RefreshOrdersUseCase {
        return RefreshOrdersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetOrderDetailUseCase(repository: OrderRepository): GetOrderDetailUseCase {
        return GetOrderDetailUseCase(repository)
    }
}
