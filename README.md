# Political Preparedness

Political Preparedness is a mobile application that helps users stay informed about civic data and make informed decisions during elections. The app integrates with an API to retrieve candidate information and ballot data based on addresses or geolocation. Users can mark preferred candidates and store their selections. The app also provides user authentication and data storage. The UI is designed to be intuitive and visually appealing. The project includes testing and welcomes contributions.

## Features

- Retrieve civic data using addresses or geolocation
- Mark preferred candidates and store selections
- User authentication and data storage
- Intuitive and visually appealing UI
- Testing for reliability and stability
- Open to contributions

## Dependencies

The app has the following dependencies:

- [Retrofit](https://square.github.io/retrofit/) to make api calls to an HTTP web service.
- [Moshi](https://github.com/square/moshi) which handles the deserialization of the returned JSON to Kotlin data objects.
- [Glide](https://bumptech.github.io/glide/) to load and cache images by URL.
- [Room](https://developer.android.com/training/data-storage/room) for local database storage.
  
It leverages the following components from the Jetpack library:

- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- [Data Binding](https://developer.android.com/topic/libraries/data-binding/) with binding adapters
- [Navigation](https://developer.android.com/topic/libraries/architecture/navigation/) with the SafeArgs plugin for parameter passing between fragments

## Installation

1. Clone the repository: `git clone [repository URL]`.
2. Open the project in Android Studio.
3. Build and run the app on your preferred device.
  
* NOTE: In order for this project to pull data, you will need to add your API Key to the project as a value in the CivicsHttpClient. You can generate an API Key from the [Google Developers Console](https://console.developers.google.com/)
