# iTunes Gallery App
The project develops a GUI application in Java using JavaFX 17 that interacts with the iTunes Search API to display a gallery of images based on user search queries. Users can enter search terms, retrieve relevant images from the iTunes API, and view them in a visually appealing gallery format. The application utilizes concepts such as inheritance, polymorphism, and interfaces to implement its functionality.

## Design Overview
The project follows a structured approach to GUI development, emphasizing class design and modularity. It leverages JavaFX for building the graphical user interface and promotes code reusability, readability, and maintainability through proper class hierarchy and componentization.

## Functionality
`GalleryDriver`:
- Serves as the main entry point for the Gallery application.
- Launches the JavaFX application by invoking the `launch` method and passing the `GalleryApp` class as an argument.
- Handles `UnsupportedOperationException` by displaying a message and suggesting potential solutions if a display problem occurs.

`GalleryApp`:
- Represents the main application class for the Gallery application.
- Extends the JavaFX `Application` class to manage the application lifecycle and UI components.
- Displays a window containing the gallery of images using a vertical `VBox` layout.
- Instantiates and configures UI components such as buttons, text fields, and progress bars.
- Provides functionality to update the gallery with images fetched from the iTunes Search API based on user input.
- Utilizes custom UI components and layouts to create a visually appealing and responsive user interface.
- Implements error handling to gracefully handle exceptions and display error messages to the user.

`ImageGallery`:
- Represents a UI component for displaying a gallery of images fetched from the iTunes Search API.
- Extends the JavaFX `VBox` class to organize its visual elements in a vertical layout.
- Includes a `TilePane` to organize the displayed images in a grid layout.
- Provides functionality to fetch images from the iTunes Search API based on user-defined search queries.
- Utilizes HTTP requests through `HttpClient` to communicate with the iTunes Search API endpoint.
- Implements a progress bar to indicate the status of image retrieval and updates it dynamically as images are fetched.
- Supports a play mode feature, allowing continuous replacement of displayed images with new ones fetched randomly from the API.
- Handles errors gracefully by displaying alert dialogs to inform the user in case of exceptions during the image retrieval process.
- Displays the current URL being queried to the iTunes Search API in a label to provide transparency to the user about the ongoing search query.
- Utilizes multithreading to perform network operations asynchronously, ensuring a responsive user interface during image retrieval.

`ItunesResponse`:
- Represents a response from the iTunes Search API.
- Provides instance variables for storing information such as the number of results and an array of `ItunesResult` objects.
- Used by the Gson library to deserialize JSON responses from the iTunes Search API into Java objects.

`ItunesResult`:
- Represents a result in a response from the iTunes Search API.
- Contains instance variables for storing information such as the wrapper type, kind, and artwork URL of a result.
- Used by the Gson library to deserialize JSON objects within the response body into Java objects.

## File Structure and Content
```
itunes-gallery-app/
├── compile.sh
├── deps.sh
├── pom.xml
├── README.md
├── resources
│   ├── default.png
│   ├── mockup_thumb.png
│   └── screenshot.png
├── run.sh
└── src
    └── main
        └── java
            ├── itunes
            │   └── gallery
            │       ├── GalleryApp.java
            │       ├── GalleryDriver.java
            │       ├── ImageGallery.java
            │       ├── ItunesResponse.java
            │       └── ItunesResult.java
            └── module-info.java
```
