package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    //set the datasource,viewmodel, and context variables
    private lateinit var datasource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var context: Application

    //Entry to use for testing:
    private val reminder = ReminderDTO(
        "Lunch Time?", "Grub at Zareen's",
        "Zareen's", 37.42674563370432, -122.14408099651337
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
        remindersListViewModel = RemindersListViewModel(context, datasource)
    }
    @After
    fun resetForcedErrorInDatasource(){
        datasource.setForcedErrorFalse()
    }

    @Test
    fun verifyLoadingIndicatorAppearsAndDissapears() = mainCoroutineRule.runBlockingTest {
        //given a reminder to load
        datasource.saveReminder(reminder)

        //pause the dispatcher so that we can test loading indicator
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        //check that the loading indicator live data value is true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        //restart the dispatcher
        mainCoroutineRule.resumeDispatcher()
        //check that the loading indicator live data is false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
    @Test
    fun verifyAfterLoadingListIsNotEmpty() = mainCoroutineRule.runBlockingTest {
        //given a reminder to load
        datasource.saveReminder(reminder)

        remindersListViewModel.loadReminders()
        //check that the list live data value is true
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(), `is`(true))

    }
    @Test
    fun verifyAfterDeletingRemindersListIsEmpty() = mainCoroutineRule.runBlockingTest {
        //given a reminder to load
        datasource.saveReminder(reminder)
        datasource.deleteAllReminders()

        remindersListViewModel.loadReminders()
        //check that the list live data value is false
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(), `is`(false))

    }

    @Test
    fun verifySnackBarGetsForcedErrorMessage() = mainCoroutineRule.runBlockingTest {
        //given a reminder and a forced error
        datasource.saveReminder(reminder)
        datasource.setForcedErrorTrue()

        remindersListViewModel.loadReminders()
        //check that the showsnackbar live data value is not empty and  has the forced error message
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue().isNotEmpty(), `is`(true))
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue() == "getReminders failed(forced)", `is`(true))

    }

    @Test
    fun verifyShowNoDataIsTrueIfListIsNullOrEmpty() = mainCoroutineRule.runBlockingTest {
        //given a reminder and then clearing
        datasource.saveReminder(reminder)
        datasource.deleteAllReminders()


        remindersListViewModel.loadReminders()
        //check that the showNoData live data is true
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun verifyShowNoDataIsFalseGivenList() = mainCoroutineRule.runBlockingTest {
        //given a reminder and then clearing
        datasource.saveReminder(reminder)


        remindersListViewModel.loadReminders()
        //check that the showNoData live data is true
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))

    }

}