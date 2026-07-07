package com.example.fixedexpeneses.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixedexpeneses.domain.model.AmountBehavior
import com.example.fixedexpeneses.domain.model.InstallmentTransaction
import com.example.fixedexpeneses.domain.model.PaymentStatus
import com.example.fixedexpeneses.domain.model.RecurringMonthlyTransaction
import com.example.fixedexpeneses.domain.model.Transaction
import com.example.fixedexpeneses.domain.model.TransactionType
import com.example.fixedexpeneses.domain.repository.AppPreferencesRepository
import com.example.fixedexpeneses.domain.repository.InstallmentTransactionRepository
import com.example.fixedexpeneses.domain.repository.RecurringMonthlyTransactionRepository
import com.example.fixedexpeneses.domain.repository.TransactionRepository
import com.example.fixedexpeneses.domain.usecase.GenerateMonthlyTransactionsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val recurringMonthlyTransactionRepository: RecurringMonthlyTransactionRepository,
    private val installmentTransactionRepository: InstallmentTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val generateMonthlyTransactionsUseCase: GenerateMonthlyTransactionsUseCase
) : ViewModel() {
    private val initialYearMonth = currentYearMonth()
    private val firstAvailableYearMonth = appPreferencesRepository
        .getOrCreateFirstAvailableYearMonth(initialYearMonth)
    private val lastAvailableYearMonth = initialYearMonth.plusMonths(FUTURE_MONTH_LIMIT)

    private val availableMonths = yearMonthRange(
        from = firstAvailableYearMonth,
        to = lastAvailableYearMonth
    ).map { yearMonth ->
        MonthSelectorOption(
            yearMonth = yearMonth,
            label = yearMonth.toMonthYearLabel()
        )
    }
    private val selectedYearMonth = MutableStateFlow(initialYearMonth)
    private val baseTransactionsChanged = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1
    )

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedYearMonth = initialYearMonth,
            currentYearMonth = initialYearMonth,
            firstAvailableYearMonth = firstAvailableYearMonth,
            availableMonths = availableMonths
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeBaseTransactions()
        generateTransactionsWhenNeeded()
        observeMonthlyTransactions()
    }

    fun onMonthSelected(yearMonth: Int) {
        if (availableMonths.none { it.yearMonth == yearMonth }) return

        selectedYearMonth.value = yearMonth
        _uiState.update {
            it.copy(selectedYearMonth = yearMonth)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            runCatching {
                val transaction = transactionRepository.getById(id)
                if (transaction != null) {
                    appPreferencesRepository.markGeneratedTransactionDeleted(
                        recurringMonthlyTransactionId = transaction.recurringMonthlyTransactionId,
                        installmentTransactionId = transaction.installmentTransactionId,
                        referenceMonth = transaction.referenceMonth,
                        referenceYear = transaction.referenceYear
                    )
                }
                transactionRepository.deleteById(id)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Não foi possível excluir a transação."
                    )
                }
            }
        }
    }

    fun toggleTransactionPaymentStatus(id: Long) {
        viewModelScope.launch {
            runCatching {
                val transaction = transactionRepository.getById(id) ?: return@runCatching
                val nextStatus = when (transaction.status) {
                    PaymentStatus.PAID -> PaymentStatus.PENDING
                    PaymentStatus.PENDING -> PaymentStatus.PAID
                }

                transactionRepository.update(
                    transaction.copy(
                        status = nextStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Não foi possível atualizar o status."
                    )
                }
            }
        }
    }

    private fun observeBaseTransactions() {
        viewModelScope.launch {
            combine(
                recurringMonthlyTransactionRepository.observeAll(),
                installmentTransactionRepository.observeAll()
            ) { _, _ -> Unit }
                .collect {
                    baseTransactionsChanged.emit(Unit)
                }
        }
    }

    private fun generateTransactionsWhenNeeded() {
        viewModelScope.launch {
            combine(selectedYearMonth, baseTransactionsChanged) { yearMonth, _ -> yearMonth }
                .collectLatest { yearMonth ->
                    generateTransactions(yearMonth)
                }
        }
    }

    private suspend fun generateTransactions(yearMonth: Int) {
        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }

        runCatching {
            generateMonthlyTransactionsUseCase(
                referenceMonth = yearMonth.referenceMonth(),
                referenceYear = yearMonth.referenceYear()
            )
        }.onSuccess {
            _uiState.update { it.copy(isGenerating = false) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    errorMessage = error.message ?: "Não foi possível montar o mês."
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMonthlyTransactions() {
        viewModelScope.launch {
            selectedYearMonth
                .flatMapLatest { yearMonth ->
                    combine(
                        transactionRepository.observeByMonth(
                            referenceMonth = yearMonth.referenceMonth(),
                            referenceYear = yearMonth.referenceYear()
                        ),
                        recurringMonthlyTransactionRepository.observeAll(),
                        installmentTransactionRepository.observeAll()
                    ) { transactions, recurringTransactions, installmentTransactions ->
                        MonthlyTransactionsSnapshot(
                            yearMonth = yearMonth,
                            transactions = transactions,
                            recurringTransactions = recurringTransactions,
                            installmentTransactions = installmentTransactions
                        )
                    }
                }
                .collect { snapshot ->
                    val items = snapshot.transactions.map { transaction ->
                        transaction.toHomeItem(
                            selectedYearMonth = snapshot.yearMonth,
                            recurringTransactions = snapshot.recurringTransactions,
                            installmentTransactions = snapshot.installmentTransactions
                        )
                    }
                    val totalIncomeInCents = items
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amountInCents }
                    val totalExpenseInCents = items
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amountInCents }

                    _uiState.update {
                        it.copy(
                            selectedYearMonth = snapshot.yearMonth,
                            transactions = items,
                            totalIncomeInCents = totalIncomeInCents,
                            totalExpenseInCents = totalExpenseInCents,
                            balanceInCents = totalIncomeInCents - totalExpenseInCents
                        )
                    }
                }
        }
    }

    private fun Transaction.toHomeItem(
        selectedYearMonth: Int,
        recurringTransactions: List<RecurringMonthlyTransaction>,
        installmentTransactions: List<InstallmentTransaction>
    ): HomeTransactionItem {
        val recurringTransaction = recurringMonthlyTransactionId?.let { id ->
            recurringTransactions.firstOrNull { it.id == id }
        }
        val installmentTransaction = installmentTransactionId?.let { id ->
            installmentTransactions.firstOrNull { it.id == id }
        }
        val sourceDescription = when {
            recurringTransaction != null -> recurringTransaction.sourceDescription()
            recurringMonthlyTransactionId != null -> "Fixa"
            installmentTransaction != null -> installmentTransaction.installmentDescriptionAt(selectedYearMonth)
            else -> "Avulsa"
        }

        return HomeTransactionItem(
            id = id,
            type = type,
            name = name,
            amountInCents = amountInCents,
            dueDay = dueDay,
            paymentMethod = paymentMethod,
            status = status,
            sourceDescription = sourceDescription
        )
    }

    private fun RecurringMonthlyTransaction.sourceDescription(): String =
        when (amountBehavior) {
            AmountBehavior.FIXED -> "Fixa"
            AmountBehavior.VARIABLE -> "Fixa variável"
        }

    private fun InstallmentTransaction.installmentDescriptionAt(yearMonth: Int): String {
        val number = installmentNumberAt(yearMonth) ?: return "Parcelada"
        val amountBehaviorLabel = when (amountBehavior) {
            AmountBehavior.FIXED -> "Parcelada"
            AmountBehavior.VARIABLE -> "Parcelada variável"
        }
        return "$amountBehaviorLabel $number/${installmentCount()}"
    }

    private fun InstallmentTransaction.installmentNumberAt(yearMonth: Int): Int? {
        if (yearMonth < yearMonthFrom || yearMonth > yearMonthTo) return null

        val number = yearMonthRange(yearMonthFrom, yearMonth).size
        val count = installmentCount()
        return number.takeIf { it in 1..count }
    }

    private fun InstallmentTransaction.installmentCount(): Int =
        yearMonthRange(yearMonthFrom, yearMonthTo).size

    private data class MonthlyTransactionsSnapshot(
        val yearMonth: Int,
        val transactions: List<Transaction>,
        val recurringTransactions: List<RecurringMonthlyTransaction>,
        val installmentTransactions: List<InstallmentTransaction>
    )

    private companion object {
        const val FUTURE_MONTH_LIMIT = 12
    }
}
