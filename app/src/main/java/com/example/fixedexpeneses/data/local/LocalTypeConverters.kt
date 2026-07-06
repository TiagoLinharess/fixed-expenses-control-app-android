package com.example.fixedexpeneses.data.local

import androidx.room.TypeConverter
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.PaymentMethod
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.TransactionType

class LocalTypeConverters {
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toAmountBehavior(value: String): AmountBehavior = AmountBehavior.valueOf(value)

    @TypeConverter
    fun fromAmountBehavior(value: AmountBehavior): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let(PaymentMethod::valueOf)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name
}
