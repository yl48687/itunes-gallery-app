/**
 * Provides the <strong>itunes-gallery</strong> application.
 */
module itunes.gallery {
    requires transitive java.logging;
    requires transitive java.net.http;
    requires transitive javafx.controls;
    requires transitive com.google.gson;
    opens itunes.gallery;
} // module
