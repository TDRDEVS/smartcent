import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcent.DatabaseHelper
import com.example.smartcent.R

class ExpenseAdapter(
    private val context: AppCompatActivity,
    private val expenses: List<DatabaseHelper.Expense>
) : ArrayAdapter<DatabaseHelper.Expense>(context, 0, expenses) {

    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
        val view = convertView ?: context.layoutInflater.inflate(R.layout.expense_list_item, parent, false)

        val expense = expenses[position]
        val textView = view.findViewById<TextView>(R.id.expenseText)
        val imageView = view.findViewById<ImageView>(R.id.expenseImage)

        textView.text = "${expense.date} | ${expense.description} | R${expense.amount} | ${expense.category}"

        if (!expense.photoUri.isNullOrEmpty()) {
            try {
                imageView.setImageURI(Uri.parse(expense.photoUri))
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }

        return view
    }
}