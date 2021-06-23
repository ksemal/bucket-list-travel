# Bucket list travel app
## Details and Description
### Functionality
Bucket list travel app alows you:
* to add a place you visited
* to fill in details about a place: title, description, date, location, image
* to update the details or delete a place from the list with the swipe guestures
* to show details and map for each place in the list

### App is using
* 4 activities: 
    * MainActivity - Main screen where user is able to see the list of places and click on the add button to create a new one
    * AddPlaceActivity - Screen where user enters information about new place (title, description, date, location, image)
    * PlaceDetailActivity - When user clicks on a place from the main screen it will move him to the activity with details about this place
    * MapActivity - View map of a particular place from PlaceDetailActivity
* Dexter - Requests permissions:
    * to read and write to external storage in order to add/get image from a device
    * to access camera to make a photo for place details
    * to access current user location to automatically populate field "Location"
* Coroutines: asynchronous function handles getting current location and showing it on the screen without blocking the main thread
* SQLiteDatabase to save places locally so they are percictent on the screen. Implemented database handle for the following CRUD operations: add place, update place, delete place, get list of all the places in DB
* RecyclerView for list of places with item click listener interface
* Custom swipe guesture class extended from ItemTouchHelper.SimpleCallback
* Maps SDK for Android and Places API to get map view, determine and show location of a place with marker and zoom

### Challenges

* Coroutines implementation in order to get current location asynchronous

### Screenshots

#### Create new traval place flow, edit/remove travel place
https://user-images.githubusercontent.com/42688915/123001702-039f2200-d37f-11eb-8c2a-45b2c1b45e4f.mov

#### View travel place details, view place map and search location in google places API
https://user-images.githubusercontent.com/42688915/123003477-3fd38200-d381-11eb-9cf1-1aa8df5ae1f3.mov

