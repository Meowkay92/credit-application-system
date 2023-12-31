package me.dio.credit.application.system.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import org.hibernate.validator.constraints.br.CPF
import java.math.BigDecimal

data class CustomerDto(
    @NotEmpty(message = "Invalid Input") val firstName: String,
    @NotEmpty(message = "Invalid Input") val lastName: String,
    @CPF(message = "This is invalid CPF")
    val cpf: String,
    @NotNull(message = "Invalid Input") val income: BigDecimal,
    @field:Email(message = "Invalid Email")
    @NotEmpty(message = "Invalid Input") val email: String,
    @NotEmpty(message = "Invalid Input") val password: String,
    @NotEmpty(message = "Invalid Input") val zipCode: String,
    @NotEmpty(message = "Invalid Input") val street: String,
) {
    fun toEntity(): Customer = Customer(
        firstName = this.firstName,
        lastName = this.lastName,
        cpf = this.cpf,
        income = this.income,
        email = this.email,
        password = this.password,
        address = Address(
            zipCode = this.zipCode,
            street = this.street
        )
    )
}
