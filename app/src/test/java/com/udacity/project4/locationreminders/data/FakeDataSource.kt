package com.udacity.project4.locationreminders.data

import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminderList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    DONE: Create a fake data source to act as a double to the real data source

    //need a variable to force an error in testing
    private var forcedError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //Done("Return the reminders")
        //set the returns for Success or Failure using result class (like in Repository)
        //force the error condition to get an error so check if it is set
        if (forcedError) //force the error and it returns out
            return Result.Error("getReminders failed(forced)")
        //otherwise
        reminderList?.let {
            return Result.Success(ArrayList(it))
        } //and a standard error if the list is empty
        return Result.Error("Reminder list is empty")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //DONE("save the reminder")
        reminderList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //DONE("return the reminder with the id")
        if (forcedError)
            return Result.Error("getReminder failed(forced)")

        reminderList?.forEach {
            return when (id) {
                it.id -> Result.Success(it)
                else -> Result.Error("Reminder was not found")
            }
        }
        return Result.Error("Reminder was not found")
    }

    override suspend fun deleteAllReminders() {
        //DONE("delete all the reminders")
        reminderList?.clear()
    }

    override suspend fun deleteReminder(id: String) {
        //DONE("Not yet implemented")
        reminderList?.forEach {
            when (id) {
                it.id -> reminderList!!.remove(it)
                else -> return
            }
        }
        return
    }

    //functions to set the forced return value true or false
    fun setForcedErrorTrue() {
        forcedError = true
    }

    fun setForcedErrorFalse() {
        forcedError = false
    }

}