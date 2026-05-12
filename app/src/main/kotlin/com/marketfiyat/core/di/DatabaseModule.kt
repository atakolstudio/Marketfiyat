package com.marketfiyat.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marketfiyat.core.data.local.MarketFiyatDatabase
import com.marketfiyat.core.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MarketFiyatDatabase {
        lateinit var db: MarketFiyatDatabase
        db = Room.databaseBuilder(
            context,
            MarketFiyatDatabase::class.java,
            MarketFiyatDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(database: SupportSQLiteDatabase) {
                    super.onCreate(database)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        db.marketDao().insertMarkets(MarketFiyatDatabase.DEFAULT_MARKETS)
                    }
                }
            })
            .build()
        return db
    }

    @Provides
    fun provideProductDao(database: MarketFiyatDatabase): ProductDao =
        database.productDao()

    @Provides
    fun provideProductPriceDao(database: MarketFiyatDatabase): ProductPriceDao =
        database.productPriceDao()

    @Provides
    fun provideMarketDao(database: MarketFiyatDatabase): MarketDao =
        database.marketDao()

    @Provides
    fun provideShoppingListDao(database: MarketFiyatDatabase): ShoppingListDao =
        database.shoppingListDao()

    @Provides
    fun provideBarcodeCacheDao(database: MarketFiyatDatabase): BarcodeCacheDao =
        database.barcodeCacheDao()
}
