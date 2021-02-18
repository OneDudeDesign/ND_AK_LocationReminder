package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderdetails.ReminderDetailViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.Matchers.not
import org.junit.After
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
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in our testing.
     */
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
            single {
                ReminderDetailViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
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
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unRegisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }



    private val reminder = ReminderDTO("reminder title","reminder description",
        "Somewhere", 100.10, 55.50)
    private val reminder2 = ReminderDTO("reminder title2","reminder description2",
        "Somewhere2", 101.10, 45.50)
    private val reminder3 = ReminderDTO("reminder title3","reminder description3",
        "Somewhere3", 102.10, 65.50)


//    TODO: add End to End testing to the app
    @Test
    fun confirmReminderDetailsOnListClick() = runBlocking{
       //set initial state
        repository.saveReminder(reminder)

    //Start up reminders view
    val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
    dataBindingIdlingResource.monitorActivity(activityScenario)

    //check the details when list item clicked
    //click the entered
    onView(withText("Somewhere")).perform(click())
    onView(withId(R.id.rdf_title)).check(matches(withText("reminder title")))
    onView(withId(R.id.rdf_description)).check(matches(withText("reminder description")))
    onView(withId(R.id.rdf_location)).check(matches(withText("Somewhere")))

    activityScenario.close()

    }

    @Test
    fun confirmItemRemainsOnSave() = runBlocking{
        //set initial state with 3 reminders
        repository.saveReminder(reminder)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)

        //Start up reminders view
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click reminder2
       onView(withText("Somewhere2")).perform(click())
        onView(withId(R.id.rdf_title)).check(matches(withText("reminder title2")))
        onView(withId(R.id.rdf_description)).check(matches(withText("reminder description2")))
        onView(withId(R.id.rdf_location)).check(matches(withText("Somewhere2")))
        //click the save button
        onView(withId(R.id.rdf_btn_back)).perform(click())
        //check that the item is still in the list in the recyclerview
        onView(withId(R.id.remindersRecyclerView))
            .check(matches(hasDescendant(withText("Somewhere2"))))

        activityScenario.close()

    }


    @Test
    fun confirmItemIsDeleted() = runBlocking{
        //set initial state with 3 reminders
        repository.saveReminder(reminder)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)

        //Start up reminders view
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click reminder2
        onView(withText("Somewhere2")).perform(click())
        onView(withId(R.id.rdf_title)).check(matches(withText("reminder title2")))
        onView(withId(R.id.rdf_description)).check(matches(withText("reminder description2")))
        onView(withId(R.id.rdf_location)).check(matches(withText("Somewhere2")))
        //click the delete button
        onView(withId(R.id.rdf_btn_remove)).perform(click())
        //check that the item is still in the list in the recyclerview
        onView(withId(R.id.remindersRecyclerView))
            .check(matches(not(hasDescendant(withText("Somewhere2")))))

        activityScenario.close()

    }

    @Test
    fun addNewReminderSuccess() = runBlocking{
        //set initial state with a reminder
        repository.saveReminder(reminder)


        //Start up reminders view
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click fab
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        //cannot test the map fragment poi selection so fake the LatLng and location data to the viewmodel
        val saveReminderVm = SaveReminderViewModel(appContext,repository)
        saveReminderVm.reminderSelectedLocationStr.postValue(reminder.location)
        saveReminderVm.latitude.postValue(reminder.latitude)
        saveReminderVm.longitude.postValue(reminder.longitude)
        //saveReminderVm.locationSelectedVM.postValue(true)


        Timber.i("Viewmodel data is: %s %s %s %s",
            saveReminderVm.reminderSelectedLocationStr.value,
            saveReminderVm.latitude.value, saveReminderVm.longitude.value,
            saveReminderVm.locationSelectedVM.value)
        //go back
        //this should go back to SaveReminderFragment without popping the error toast as locationselectedVM is set to true
        //it does not work the data shows in the log as null
        onView(withId(R.id.btn_save_map_location)).perform(click())
        //attempt to save fails because the app was blocked from going back toi the save screen due to null values
        Thread.sleep(5000) //sleep just to slow down to see whats happening do not leave in
        onView(withId(R.id.saveReminder)).perform(click())
        Thread.sleep(5000)

        activityScenario.close()


    }



}
