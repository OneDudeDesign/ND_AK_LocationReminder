package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    //create lateinit variables: need DB and Repository, repository needs Dao
    private lateinit var database: RemindersDatabase
    private lateinit var testRepository: RemindersLocalRepository
    private lateinit var dao: RemindersDao

    //reminder to use in testing
    private val reminder = ReminderDTO("reminder title","reminder description",
        "Somewhere", 100.10, 55.50)
    private val reminder2 = ReminderDTO("reminder title2","reminder description2",
        "Somewhere2", 101.10, 45.50)
    private val reminder3 = ReminderDTO("reminder title3","reminder description3",
        "Somewhere3", 102.10, 65.50)

    //set rules
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    //use mainCoroutineRule to use the same dispatcher throughout
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //setup before tests
    @Before
    fun initializeDbRepositoryAndDao() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
        testRepository = RemindersLocalRepository(dao,Dispatchers.Main)
    }

    //cleanup after (close imdb)
    @After
    fun closeDatabase(){
        database.close()
    }

    //TESTS*****TESTS
    //test getting all reminders via the repository successfully
    @Test
    fun getRemindersReturnsSuccess() = mainCoroutineRule.runBlockingTest {
        //Given - save 3 reminders
        testRepository.saveReminder(reminder)
        testRepository.saveReminder(reminder2)
        testRepository.saveReminder(reminder3)

        //When - retrieve the reminders (response is result so pull the data from the result
        val retrievedResult = testRepository.getReminders() as Result.Success<List<ReminderDTO>>
        val retrievedReminders = retrievedResult.data

        //Then - Assert success
        assertThat(retrievedReminders.size, `is`(3))
    }
    //test saving a reminder (consequently this also tests getting a reminder by ID
    @Test
    fun saveReminderSucceeds() = mainCoroutineRule.runBlockingTest {
        //Given - save a reminders
        testRepository.saveReminder(reminder)


        //When - retrieve the reminder
        val retrievedResult = testRepository.getReminder(reminder.id) as Result.Success<ReminderDTO>
        val retrievedReminder = retrievedResult.data

        //Then - Assert that the reminder was the one expected
        assertThat(retrievedReminder.id, `is`(reminder.id))
        assertThat(retrievedReminder.title, `is`(reminder.title))
        assertThat(retrievedReminder.location, `is`(reminder.location))
        assertThat(retrievedReminder.description, `is`(reminder.description))
        assertThat(retrievedReminder.latitude, `is`(reminder.latitude))
        assertThat(retrievedReminder.longitude, `is`(reminder.longitude))
    }

    //test that retrieving a non-existent reminder returns the error message
    @Test
    fun getReminderFailsWithErrorMessage() = mainCoroutineRule.runBlockingTest {
        //Given - save a reminder and delete it
        testRepository.saveReminder(reminder)
        testRepository.deleteAllReminders()


        //When - retrieve the reminder
        val retrievedResult = testRepository.getReminder(reminder.id) as Result.Error


        //Then - Assert that the error message was correct
        assertThat(retrievedResult.message, `is`("Reminder not found!"))

    }

    //test deleting all reminders via the repository successfully
    @Test
    fun deleteAllRemindersSucceeds() = mainCoroutineRule.runBlockingTest {
        //Given - save 3 reminders and delete them
        testRepository.saveReminder(reminder)
        testRepository.saveReminder(reminder2)
        testRepository.saveReminder(reminder3)
        testRepository.deleteAllReminders()

        //When - retrieve the reminders (response is result so pull the data from the result)
        val retrievedResult = testRepository.getReminders() as Result.Success<List<ReminderDTO>>
        val retrievedReminders = retrievedResult.data

        //Then - Assert that the list is empty
        assertThat(retrievedReminders, `is`(empty()))
    }

    //test deleting a reminder via the repository succeeds
    @Test
    fun deleteSingleReminderSucceeds() = mainCoroutineRule.runBlockingTest {
        //Given - save 3 reminders and delete them
        testRepository.saveReminder(reminder)
        testRepository.saveReminder(reminder2)
        testRepository.saveReminder(reminder3)
        testRepository.deleteReminder(reminder2.id)

        //When - retrieve the reminders (response is result so pull the data from the result)
        val retrievedResult = testRepository.getReminders() as Result.Success<List<ReminderDTO>>
        val retrievedReminders = retrievedResult.data

        //Then - Assert that the reminder deleted is not in the retrieved result
        for (element in retrievedReminders) {
            assertThat(element.id, `is`(not(equalTo(reminder2.id))))
        }
    }

}