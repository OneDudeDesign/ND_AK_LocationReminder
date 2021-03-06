REVIEWER, YOU NEED TO SETUP API KEY PLEASE
the google api ke needs to go into the google_maps_api.xml file

fyi there are some extraneous comments throught the code where I may want to add a future feature or tried to document wehat I was doing
for later use, this is a challenging project that I will want to review later for use in other apps.
THANK YOU FOR YOUR TIME REVIEWING

#Attributions
- Blood
-sweat
- tears
- more tears
-hair from pulling

Accessing exact location: https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient

permissions adapted from previous projects wander and treasure hunt along with developer.android.com and new Android 11 permissions video
geofencing adaptation from treasurehunt

You are here graphic:
"https://www.vecteezy.com/free-vector/you-are-here"

onBackPressedCallback in ReminderDescription activity adapted from Medium article: https://medium.com/@pavan.careers5208/onbackpresseddispatcher-android-2a771f25bd44
Testing in general adapted from the lessons and codelabs
recyclerview testing code adapted from https://medium.com/@_rpiel/recyclerview-and-espresso-a-complicated-story-3f6f4179652e
posting a value to the viewmodel in espresso test adapted from https://stackoverflow.com/questions/53304347/mutablelivedata-cannot-invoke-setvalue-on-a-background-thread-from-coroutine
checking toast visibility adapted from https://www.pluralsight.com/guides/testing-in-android-with-espresso-part-2

Fragment testing with scenario https://developer.android.com/reference/kotlin/androidx/fragment/app/testing

Adapted Custom EspressoMatcher for checking the TextviewCompound drawable from : 
    https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f
    https://gist.github.com/RyanBurnsworth/9bf15ebd29c321b4e5517b98f5142b99 

Viewmodel testing setting config to SDK28 and stopping koin learned from knowledgebase review
    

# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.

## Getting Started

1. Clone the project to your local machine.
2. Open the project using Android Studio.

### Dependencies

```
1. A created project on Firebase console.
2. A create a project on Google console.
```

### Installation

Step by step explanation of how to get a dev environment running.

```
1. To enable Firebase Authentication:
        a. Go to the authentication tab at the Firebase console and enable Email/Password and Google Sign-in methods.
        b. download `google-services.json` and add it to the app.
2. To enable Google Maps:
    a. Go to APIs & Services at the Google console.
    b. Select your project and go to APIs & Credentials.
    c. Create a new api key and restrict it for android apps.
    d. Add your package name and SHA-1 signing-certificate fingerprint.
    c. Enable Maps SDK for Android from API restrictions and Save.
    d. Copy the api key to the `google_maps_api.xml`
3. Run the app on your mobile phone or emulator with Google Play Services in it.
```

## Testing

Right click on the `test` or `androidTest` packages and select Run Tests

### Break Down Tests

Explain what each test does and why

```
1.androidTest
        //TODO: Students explain their testing here.
2. test
        //TODO: Students explain their testing here.
```

## Project Instructions
    DONE1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
    DONE with Firebase2. Create a Register screen to allow a user to register using an email address or a Google account.
    3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
    4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
    5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
        a. title
        b. description
        c. selected location
    6. Reminder data should be saved to local storage.
    7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
    8. Provide testing for the ViewModels, Coroutines and LiveData objects.
    9. Create a FakeDataSource to replace the Data Layer and test the app in isolation.
    10. Use Espresso and Mockito to test each screen of the app:
        a. Test DAO (Data Access Object) and Repository classes.
        b. Add testing for the error messages.
        c. Add End-To-End testing for the Fragments navigation.


## Student Deliverables:

1. APK file of the final project.
2. Git Repository with the code.

## Built With

* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
* [JobIntentService](https://developer.android.com/reference/androidx/core/app/JobIntentService) - Run background service from the background application, Compatible with >= Android O.

## License
