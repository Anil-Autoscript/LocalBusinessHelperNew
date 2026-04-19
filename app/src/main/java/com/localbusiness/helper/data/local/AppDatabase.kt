package com.localbusiness.helper.data.local

import android.content.Context
import androidx.room.*
import com.localbusiness.helper.data.local.dao.CustomerDao
import com.localbusiness.helper.data.local.dao.OrderDao
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.OrderStatus
import com.localbusiness.helper.data.local.entity.PaymentStatus

@Database(
    entities = [Customer::class, Order::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "local_business_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus =
        try { OrderStatus.valueOf(value) } catch (e: Exception) { OrderStatus.PENDING }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus =
        try { PaymentStatus.valueOf(value) } catch (e: Exception) { PaymentStatus.UNPAID }
}
