package itunes.gallery;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {

    protected Stage stage;

    /**
     * The entry point for the gallery application.
     *
     * @param newStage new stage which will be used in {@code ImageGallery}
     */
    public void start(Stage newStage) {
        stage = newStage;
        VBox pane = new VBox();
        Scene scene = new Scene(pane);

        ImageGallery imageGallery = new ImageGallery();

        pane.setVgrow(imageGallery, Priority.ALWAYS);
        pane.getChildren().addAll(imageGallery);

        stage.setMaxWidth(755);
        stage.setMaxHeight(800);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

} // GalleryApp