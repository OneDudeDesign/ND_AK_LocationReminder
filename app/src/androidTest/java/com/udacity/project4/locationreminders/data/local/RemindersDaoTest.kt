package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase

//    DONE: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeDatabase(){
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun insertReminderandGetById() = runBlockingTest {
        //Given: Insert a reminder
        val reminder = ReminderDTO("reminder title","reminder description",
            "Somewhere", 100.10, 55.50)
        database.reminderDao().saveReminder(reminder)

        //WHEN get the reminder using the reminder id
        val retrievedReminder = database.reminderDao().getReminderById(reminder.id)

        //THEN: assertions should be as inserted
        assertThat(retrievedReminder as ReminderDTO, notNullValue())
        assertThat(retrievedReminder.id, `is`(reminder.id))
        assertThat(retrievedReminder.title, `is`(reminder.title))
        assertThat(retrievedReminder.description, `is`(reminder.description))
        assertThat(retrievedReminder.location, `is`(reminder.location))
        assertThat(retrievedReminder.latitude, `is`(reminder.latitude))
        assertThat(retrievedReminder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertRemindersandDeleteAll() = runBlockingTest {
        //Given: Insert reminders
        val reminder = ReminderDTO("reminder title","reminder description",
            "Somewhere", 100.10, 55.50)
        val reminder2 = ReminderDTO("reminder title2","reminder description2",
            "Somewhere2", 101.10, 45.50)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)

        //WHEN delete the reminders and then retrieve the list from the DB
        database.reminderDao().deleteAllReminders()
        val reminderList = database.reminderDao().getReminders()

        //THEN: Assert that the returned list is empty
        assertThat(reminderList.isEmpty(), `is`(true))
    }
    @Test
    fun insertRemindersandDeleteOne() = runBlockingTest {
        //Given: Insert reminders
        val reminder = ReminderDTO("reminder title","reminder description",
            "Somewhere", 100.10, 55.50)
        val reminder2 = ReminderDTO("reminder title2","reminder description2",
            "Somewhere2", 101.10, 45.50)
        val reminder3 = ReminderDTO("reminder title3","reminder description3",
            "Somewhere3", 102.10, 65.50)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        //WHEN delete reminder3 and confirm it is not in the list
        database.reminderDao().deleteReminder(reminder3.id)
        val reminderList = database.reminderDao().getReminders()

        //THEN: Assert that the returned list is empty

        for (element in reminderList) {
            assertThat(element.id,`is`(not(equalTo(reminder3.id))))
        }
    }

    @Test
    fun insertRemindersandOverwrite() = runBlockingTest {
        //Given: Insert reminders
        val reminder = ReminderDTO("reminder title","reminder description",
            "Somewhere", 100.10, 55.50)
        database.reminderDao().saveReminder(reminder)

        //WHEN insert reminder with different details
        val reminderId = reminder.id
        val changedReminder = ReminderDTO("New Title","New Description", "Somewhere Else", 200.0, 100.0, reminderId)
        database.reminderDao().saveReminder(changedReminder)
        val reminderList = database.reminderDao().getReminders()


        //THEN: Assert that the returned list is only one and title changed
        assertThat(reminderList.size, `is`(1))
        assertThat(reminderList[0].title, `is`("New Title"))
        assertThat(reminderList[0].id, `is`(reminderId))
    }

}