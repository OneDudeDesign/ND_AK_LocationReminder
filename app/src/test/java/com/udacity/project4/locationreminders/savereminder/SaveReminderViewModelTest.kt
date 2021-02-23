package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    //DONE: provide testing to the SaveReminderViewModel and its live data objects

    //set the datasource,viewmodel, and context variables
    private lateinit var datasource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var context: Application

    //Entry to use for testing:
    private val reminder = ReminderDataItem(
        "Lunch Time?", "Grub at Zareen's",
        "Zareen's", 37.42674563370432, -122.14408099651337
    )
    private val nullTitleReminder = ReminderDataItem(
        null, "Grub at Zareen's",
        "Zareen's", 37.42674563370432, -122.14408099651337
    )
    private val emptyTitleReminder = ReminderDataItem(
        "", "Grub at Zareen's",
        "Zareen's", 37.42674563370432, -122.14408099651337
    )
    private val nullLocationReminder = ReminderDataItem(
        "Lunch Time?", "Grub at Zareen's",
        null, 37.42674563370432, -122.14408099651337
    )
    private val emptyLocationReminder = ReminderDataItem(
        "Lunch Time?", "Grub at Zareen's",
        "", 37.42674563370432, -122.14408099651337
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initializeTestParameters() {
        stopKoin()
        //setup the viewmodel for testing
        context = ApplicationProvider.getApplicationContext()
        datasource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(context, datasource)
    }

    @Test
    fun whenSaveClickedViewmodelShowsLoadingThenSnackBarThenNavigatesBack() =
        mainCoroutineRule.runBlockingTest {
            //pause the dispatcher to see loading status
            mainCoroutineRule.pauseDispatcher()
            //save a reminder via the viewmodel
            saveReminderViewModel.validateAndSaveReminder(reminder) //shows the method flow through validate and then save works
            //check showLoading live data is true
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
            //resume the dispatcher and check the showloading is false
            mainCoroutineRule.resumeDispatcher()
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
            //check that showSnackbar has a value and it is correct
            assertThat(
                saveReminderViewModel.showSnackBar.getOrAwaitValue().isNotEmpty(),
                `is`(true)
            )
            assertThat(
                saveReminderViewModel.showSnackBar.getOrAwaitValue() ==
                        context.getString(R.string.reminder_saved) +
                        " " + reminder.location, `is`(true)
            )

        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenTitleIsNull() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(nullTitleReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenTitleIsEmpty() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(emptyTitleReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenLocationIsNull() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(nullLocationReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenLocationIsEmpty() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(emptyLocationReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location,
                `is`(true)
            )
            assert(!returnValue)
        }
}