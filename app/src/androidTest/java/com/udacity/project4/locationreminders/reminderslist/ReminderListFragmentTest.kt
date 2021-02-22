package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.TextViewDrawableMatcher.Companion.withDrawable
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    private lateinit var testDataSource: ReminderDataSource
    private lateinit var appContext: Application

    //reminder to use in testing
    private val reminder = ReminderDTO(
        "reminder title", "reminder description",
        "Somewhere", 100.10, 55.50
    )
    private val reminder2 = ReminderDTO(
        "reminder title2", "reminder description2",
        "Somewhere2", 101.10, 45.50
    )
    private val reminder3 = ReminderDTO(
        "reminder title3", "reminder description3",
        "Somewhere3", 102.10, 65.50
    )

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in our testing.
     */
    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our datasource
        testDataSource = get()

        //clear the data to start fresh
        runBlocking {
            testDataSource.deleteAllReminders()
        }
    }

    //initial test no data

    @Test
    fun fragmentDisplayedNoDataIndicated() {
        //Given - scenario with navcontroller and no data
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //when - we have the fragment
        scenario.onFragment {}

        //then - assert that the no data indicators are displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withText(appContext.getString(R.string.no_data))).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(withDrawable(R.drawable.ic_no_data)))

    }

    //test with data

    @Test
    fun fragmentDisplayedwithDataItems() {
        //Given - scenario with navcontroller and data items

        runBlocking {
            testDataSource.saveReminder(reminder)
            testDataSource.saveReminder(reminder2)
            testDataSource.saveReminder(reminder3)
        }
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //when - we have the fragment
        scenario.onFragment {}

        //then - assert that the no data indicators are displayed
        onView(withText("reminder title")).check(matches(isDisplayed()))
        onView(withText("reminder title2")).check(matches(isDisplayed()))
        onView(withText("reminder title3")).check(matches(isDisplayed()))
        onView(withText("reminder description")).check(matches(isDisplayed()))
        onView(withText("reminder description2")).check(matches(isDisplayed()))
        onView(withText("reminder description3")).check(matches(isDisplayed()))
        onView(withText("Somewhere")).check(matches(isDisplayed()))
        onView(withText("Somewhere2")).check(matches(isDisplayed()))
        onView(withText("Somewhere3")).check(matches(isDisplayed()))

    }

    //test clicking fab
    @Test
    fun fabClickNavigatesToSaveReminderFragment() {
        //given fragment and nav controller
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        //when we have fragment and nav controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //then clicking fab navigates
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    //test clicking item
    @Test
    fun itemClickNavigatesToReminderDetailFragment() {
        //given fragment, reminderDataItem, and nav controller
        runBlocking { testDataSource.saveReminder(reminder)}
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        val reminderDataItem = ReminderDataItem(reminder.title,reminder.description,reminder.location,reminder.latitude,reminder.longitude,reminder.id)

        //When we have the fragment
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //Then when item clicked we navigate to ReminderDetails
        onView(withText("reminder title")).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToReminderDetailFragment(reminderDataItem))
    }

}