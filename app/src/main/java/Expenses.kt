data class Expenses(
    val date: String,
    val description: String,
    val amount: Double,
    val category: String,
    val photoUri: String? = null
)

object ExpenseData {
    val expenseList = ArrayList<Expenses>()
}