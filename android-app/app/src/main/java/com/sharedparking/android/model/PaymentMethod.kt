package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 支付方式枚举
 */
enum class PaymentMethod(val value: String, val displayName: String) {
    @SerializedName("alipay")
    ALIPAY("alipay", "支付宝"),

    @SerializedName("wechat")
    WECHAT("wechat", "微信支付"),

    @SerializedName("credit_card")
    CREDIT_CARD("credit_card", "信用卡"),

    @SerializedName("wallet")
    WALLET("wallet", "钱包余额");

    companion object {
        fun fromValue(value: String): PaymentMethod {
            return when (value) {
                "alipay" -> ALIPAY
                "wechat" -> WECHAT
                "credit_card" -> CREDIT_CARD
                "wallet" -> WALLET
                else -> ALIPAY
            }
        }

        fun getDisplayName(value: String): String {
            return fromValue(value).displayName
        }
    }
}