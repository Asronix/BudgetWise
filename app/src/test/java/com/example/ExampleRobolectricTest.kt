package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BudgetWise", appName)
    }

    @Test
    fun `savings goal progress fraction calculation`() {
        val goal = SavingsGoalEntity(
            id = 1,
            title = "Trip to Europe",
            targetAmount = 2000.0,
            currentAmount = 1000.0,
            targetDateMillis = System.currentTimeMillis() + 86400000L
        )
        assertEquals(0.5f, goal.progressFraction, 0.001f)
        assertEquals(false, goal.isCompleted)

        val completedGoal = goal.copy(currentAmount = 2000.0)
        assertEquals(1.0f, completedGoal.progressFraction, 0.001f)
        assertTrue(completedGoal.isCompleted)
    }

    @Test
    fun `transaction entity creation`() {
        val tx = TransactionEntity(
            id = 10,
            title = "Grocery Run",
            amount = 65.50,
            type = TransactionType.EXPENSE.name,
            category = "Groceries",
            dateMillis = 1000L
        )
        assertEquals("EXPENSE", tx.type)
        assertEquals(65.50, tx.amount, 0.001)
    }
}
