package com.neverno.neverq.di

import android.content.Context
import androidx.room.Room
import com.neverno.neverq.core.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "neverq.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideKitchenOrderDao(db: AppDatabase) = db.kitchenOrderDao()

    @Provides
    fun providePosProductDao(db: AppDatabase) = db.posProductDao()
}
