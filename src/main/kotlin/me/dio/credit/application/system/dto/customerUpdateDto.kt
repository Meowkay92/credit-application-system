package me.dio.credit.application.system.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal

data class customerUpdateDto(
    @NotEmpty(message = "Invalid Input") val firstName: String,
    @NotEmpty(message = "Invalid Input") val lastName: String,
    @NotNull(message = "Invalid Input") val income: BigDecimal,
    @NotEmpty(message = "Invalid Input") val zipCode: String,
    @NotEmpty(message = "Invalid Input") val street: String
) {
    fun toEntity(customer: Customer): Customer {
        customer.firstName = this.firstName
        customer.lastName = this.lastName
        customer.income = this.income
        customer.address.zipCode = this.zipCode
        customer.address.street = this.street
        return customer
    }
}
