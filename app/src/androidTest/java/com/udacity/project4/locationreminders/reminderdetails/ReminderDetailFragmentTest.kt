package com.udacity.project4.locationreminders.reminderdetails

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest

class ReminderDetailFragmentTest : AutoCloseKoinTest(){

    private lateinit var testDataSource: ReminderDataSource
    private lateinit var appContext: Application

    //reminder to use in testing
    private val reminder = ReminderDTO(
        "Lunch Time?", "Grub at Zareen's",
        "Zareen's", 37.42674563370432, -122.14408099651337
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeTestParameters() {
        stopKoin()//stop the original app koin before
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            single {
                ReminderDetailViewModel(
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
//I would think you need to stop koin after each test like the viewmodels testing but causes stack overflow
//    @After
//    fun stopKoin(){
//        stopKoin()
//    }

    //Check for all items visible
    @Test
    fun allItemsAreVisibleForEnteredReminder(){
        //Given a reminder inserted, the bundle args and the fragment
        runBlocking { testDataSource.saveReminder(reminder) }
        val reminderDataItem = ReminderDataItem(reminder.title,reminder.description,reminder.location,reminder.latitude,reminder.longitude,reminder.id)

        val scenario = launchFragmentInContainer<ReminderDetailFragment>(bundleOf("EXTRA_ReminderDataItem" to reminderDataItem),R.style.AppTheme)

        //when - we have the fragment
        scenario.onFragment {}

        //Then verify all the details are shown
        onView(withId(R.id.rdf_location_head)).check(matches(isDisplayed()))
        onView(withId(R.id.rdf_title_head)).check(matches(isDisplayed()))
        onView(withId(R.id.rdf_description_head)).check(matches(isDisplayed()))
        onView(withId(R.id.rdf_btn_back)).check(matches(isDisplayed()))
        onView(withId(R.id.rdf_btn_remove)).check(matches(isDisplayed()))
        onView(withId(R.id.rdf_tv_title)).check(matches(withText(containsString(appContext.getString(R.string.reminder_detail_heading)))))
        onView(withId(R.id.rdf_location)).check(matches(withText(containsString(reminder.location))))
        onView(withId(R.id.rdf_title)).check(matches(withText(containsString(reminder.title))))
        onView(withId(R.id.rdf_description)).check(matches(withText(containsString(reminder.description))))


    }

    @Test
    fun clickingRemoveDeletesItemAndNavigatesBackToList () {
        //Given a reminder inserted, the bundle args and the fragment
        runBlocking { testDataSource.saveReminder(reminder) }
        val reminderDataItem = ReminderDataItem(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id
        )

        val scenario = launchFragmentInContainer<ReminderDetailFragment>(
            bundleOf("EXTRA_ReminderDataItem" to reminderDataItem),
            R.style.AppTheme
        )
        val navController = mock(NavController::class.java)

        //when we have fragment and nav controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //test that clicking on the remove button generates the actions we want
        onView(withId(R.id.rdf_btn_remove)).perform(click())
         //test that the navcontroller receives a popbackstack command
        verify(navController).popBackStack()
        //note: it will not navigate on screen as we are testing the fragment but the toast for removal notification will pop so test for it
        onView(withText(appContext.getString(R.string.reminder_removed)+ " " + reminder.location))
            .check(matches(withEffectiveVisibility( Visibility.VISIBLE)))

    }

    @Test
    fun clickingSaveNavigatesBackToList () {
        //Given a reminder inserted, the bundle args and the fragment
        runBlocking { testDataSource.saveReminder(reminder) }
        val reminderDataItem = ReminderDataItem(
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.id
        )

        val scenario = launchFragmentInContainer<ReminderDetailFragment>(
            bundleOf("EXTRA_ReminderDataItem" to reminderDataItem),
            R.style.AppTheme
        )
        val navController = mock(NavController::class.java)

        //when we have fragment and nav controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //test that clicking on save button navigates back to the list
        onView(withId(R.id.rdf_btn_back)).perform(click())
        verify(navController).popBackStack()

    }

}