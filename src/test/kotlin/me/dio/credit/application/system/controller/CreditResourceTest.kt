package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.dto.CreditViewList
import me.dio.credit.application.system.dto.CustomerDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired lateinit var customerService: CustomerService

    @Autowired lateinit var creditResource: CreditResource

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    public lateinit var customer: Customer

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup(){
        customerRepository.deleteAll()
        creditRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        customerRepository.deleteAll()
        creditRepository.deleteAll()
    }

    @Test
    fun `should create credit`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCredit()
        var valueAsString: String =objectMapper.writeValueAsString(credit)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallment").value(15))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `must reject outdated credit`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCreditOutdated()
        var valueAsString: String =objectMapper.writeValueAsString(credit)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect { MockMvcResultMatchers.status().isBadRequest }
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should reject out numberOfInstallments`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCreditOutnumberOfInstallments()
        var valueAsString: String =objectMapper.writeValueAsString(credit)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect { MockMvcResultMatchers.status().isBadRequest }
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return list of  credits`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCredit()
        creditRepository.save(credit.toEntity())

        var credit2: CreditDto = buildCredit2()
        creditRepository.save(credit2.toEntity())

        var creditView: ResponseEntity<List<CreditViewList>> =
            creditResource.findAllByCustomerId(1L)

        Assertions.assertThat(creditView.body?.size ?: 0).isEqualTo(2)

    }

    @Test
    fun `should return creditView by credit code and customerId`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCredit()
        var creditView =  creditRepository.save(credit.toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get(URL+"/${creditView.creditCode}?customerId=${this.customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should fail by credit code and customerId`() {
        customerRepository.save(builderCustomerDto().toEntity())
        this.customer = customerService.findById(1L)

        var credit: CreditDto = buildCredit()
        var creditView =  creditRepository.save(credit.toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get(URL+"/${creditView.creditCode}?customerId=2")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class java.lang.IllegalArgumentException"))
            .andDo(MockMvcResultHandlers.print())
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(100.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusMonths(2L),
        numberOfInstallments: Int = 15,
        customer: Customer = this.customer
    )= CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customer.id!!
    )

    private fun buildCredit2(
        creditValue: BigDecimal = BigDecimal.valueOf(440.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusMonths(1L),
        numberOfInstallments: Int = 4,
        customer: Customer = this.customer
    )= CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customer.id!!
    )

    private fun buildCreditOutdated(
        creditValue: BigDecimal = BigDecimal.valueOf(100.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusMonths(1L),
        numberOfInstallments: Int = 55,
        customer: Customer = this.customer
    )= CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customer.id!!
    )

    private fun buildCreditOutnumberOfInstallments(
        creditValue: BigDecimal = BigDecimal.valueOf(100.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusMonths(5L),
        numberOfInstallments: Int = 15,
        customer: Customer = this.customer
    )= CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customer.id!!
    )

    private fun builderCustomerDto(
        firstName: String = "Cami",
        lastName: String = "Cavalcante",
        cpf: String = "28475934625",
        email: String = "camila@email.com",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        password: String = "1234",
        zipCode: String = "000000",
        street: String = "Rua da Cami, 123",
    ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        income = income,
        password = password,
        zipCode = zipCode,
        street = street
    )

}