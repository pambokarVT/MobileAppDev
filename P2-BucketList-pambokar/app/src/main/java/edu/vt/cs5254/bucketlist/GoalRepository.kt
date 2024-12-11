package edu.vt.cs5254.bucketlist

import android.content.Context
import androidx.room.Room
import edu.vt.cs5254.bucketlist.database.GoalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "goal-database"
class GoalRepository @OptIn(DelicateCoroutinesApi::class)
private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope ) {

    private val database = Room.databaseBuilder(
        context,
        GoalDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset(DATABASE_NAME)
        .build()

    //GoalListFragment
    fun getGoals(): Flow<List<Goal>> {
        val flowMultiMap = database.goalDao().getGoals()
//        val multiMap = database.goalDao().getGoals() //multimap: List<Goal, List<GoalNote>>
        return flowMultiMap.map { multiMap ->
            multiMap.keys.map {
                it.apply { notes = multiMap.getValue(it)}
            }
        }
    }

    //GoalDetailFragment
    suspend fun getGoal(id: UUID): Goal = database.goalDao().getGoalAndNotes(id)

    fun updateGoal(goal: Goal) {
        coroutineScope.launch {
            database.goalDao().updateGoalAndNotes(goal)
        }
    }

    suspend fun addGoal(goal: Goal) {
        database.goalDao().addGoal(goal)
    }

    suspend fun deleteGoal(goal: Goal) {
        database.goalDao().deleteGoalAndNotes(goal)
    }

    companion object {
        private var INSTANCE: GoalRepository? = null

        fun initialize(context: Context) {
            check(INSTANCE == null) { "GoalRepository is ALREADY INITIALIZED" }
            INSTANCE = GoalRepository(context)
        }

        fun get() : GoalRepository {
            return checkNotNull(INSTANCE) { "GoalRepository MUST BE initialized" }
        }
    }

}