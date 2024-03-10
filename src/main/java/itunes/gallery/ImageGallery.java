package itunes.gallery;

import com.google.gson.Gson;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates {@code ImageGallery} which extends {@code VBox}.
 */
public class ImageGallery extends VBox {

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2) // uses HTTP protocol version 2 where possible
            .followRedirects(HttpClient.Redirect.NORMAL) // always redirects, except to HTTP
            .build(); // builds and returns a HttpClient object
    private HttpRequest request;
    private volatile ItunesResponse itunesResponse;

    protected static final String DEFAULT_IMG = "resources/default.png";

    protected static final int DEF_HEIGHT = 150;
    protected static final int DEF_WIDTH = 150;

    protected HBox toolBar;
    protected HBox urlBar;
    protected Label urlLabel;
    protected Button btnPlayPause;
    protected Label searchLabel;
    protected TextField searchField;
    protected ComboBox genres;
    protected Button updateImagesButton;
    protected List<String> unusedImageURLs = new ArrayList<String>();
    protected TilePane tile;
    protected String[] usedImageURLs = new String[20];
    protected ImageView[] imageViewsOnScreen = new ImageView[20];
    protected HBox progressTab;
    protected ProgressBar progressBar;
    protected Label courtesyLabel;
    protected KeyFrame keyFrame;
    protected Timeline timeline;
    protected double currentProgress;
    protected ImageView imgView;
    protected String urlString;
    protected boolean enablePlayMode;
    protected boolean alertShown = false;

    /**
     * Creates the constructor for ImageGallery.
     */
    public ImageGallery() {
        super();
        tile = new TilePane();
        tile.setPrefColumns(5);
        tile.setPrefRows(4);
        createProgressTab();
        urlBar = new HBox();
        this.urlLabel = new Label();
        this.urlLabel.setText("Type in a term, select a media type, then click the button.");
        urlBar.getChildren().addAll(urlLabel);
        urlBar.setAlignment(Pos.CENTER_LEFT);
        createToolBar();
        this.btnPlayPause.setDisable(true);
        runNow(() -> playMode(false));
        Image defaultImage = new Image("file:" + DEFAULT_IMG);
        imgView = new ImageView(defaultImage);
        imgView.setFitWidth(750);
        imgView.setFitHeight(600);
        for (int i = 0; i < 20; i++) {
            ImageView imageView = new ImageView(defaultImage);
            imageView.setFitWidth(DEF_WIDTH);
            imageView.setFitHeight(DEF_HEIGHT);
            imageViewsOnScreen[i] = imageView;
            tile.getChildren().add(imageViewsOnScreen[i]);
        } // for
        this.btnPlayPause.setOnAction(e -> {
            if (this.btnPlayPause.getText().equals("Pause")) {
                this.btnPlayPause.setText("Play");
                this.enablePlayMode = false;
                runNow(() -> playMode(this.enablePlayMode));
            } else if (this.btnPlayPause.getText().equals("Play")) {
                this.btnPlayPause.setText("Pause");
                this.enablePlayMode = true;
                runNow(() -> playMode(this.enablePlayMode));
            } // if
        });
        updateImagesButton.setOnAction(e -> {
            this.enablePlayMode = false;
            runNow(() -> {
                playMode(this.enablePlayMode);
                updateImages(searchField.getText());
            });
            if (this.btnPlayPause.isDisable()) {
                runNow(() -> btnPlayPause.setDisable(false));
            } // if
            updateUrlLabel("Getting images...");
            this.btnPlayPause.setText("Play");
            this.btnPlayPause.setDisable(true);
        });
        this.getChildren().addAll(toolBar, urlBar, tile, progressTab);
    } // ImageGallery

    /**
     * Downloads images based on {@code searchQuery}.
     *
     * @param searchQuery input of the search
     * @return response response to the search
     */
    public HttpResponse<String> fetchImages(String searchQuery) {
        String searchURL = "https://itunes.apple.com/search?term=";
        String urlEncodedValue = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        HttpResponse<String> response = null;
        searchURL += urlEncodedValue + "&media=" +
                     genres.getSelectionModel().getSelectedItem().toString() + "&limit=200";
        urlEncodedValue = URLEncoder.encode(searchURL, StandardCharsets.UTF_8);
        this.urlString = searchURL;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(searchURL))
                    .build();
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpResponse<String> finalResponse = response;
            if (response.statusCode() != 200) { // check response
                Platform.runLater(() -> createAlert("java.io.IOException", finalResponse.toString(),
                                                    urlString));
                alertShown = true;
                updateUrlLabel("Last attempt to get images failed...");
                return null;
            } // if
        } catch (IOException e) {
            Platform.runLater(() -> createAlert("java.io.IOException", e.getMessage(), urlString));
        } catch (InterruptedException e) {
            Platform.runLater(() -> createAlert("java.lang.IllegalArgumentException",
                                                e.getMessage(), urlString));
        } // try
        return response;
    } // fetchImages

    /**
     * Creates a URL bar.
     *
     * @param urlString string of the URL
     */
    public void createURLBar(String urlString) {
        urlBar = new HBox();
        urlLabel = new Label(urlString);
        urlBar.getChildren().addAll(urlLabel);
        urlBar.setAlignment(Pos.CENTER_LEFT);
    } // createURLBar

    /**
     * Creates a toolbar.
     */
    public void createToolBar() {
        toolBar = new HBox(8);
        this.btnPlayPause = new Button("Play");
        searchLabel = new Label("Search:");
        searchField = new TextField("daft punk");
        updateImagesButton = new Button("Get Images");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        toolBar.getChildren().addAll(btnPlayPause, searchLabel, searchField, genres,
                                     updateImagesButton);
        toolBar.setAlignment(Pos.CENTER_LEFT);
    } // createToolBar

    /**
     * Creates a progress tab.
     */
    public void createProgressTab() {
        progressTab = new HBox(15);
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(450);
        progressBar.setProgress(0.0);
        courtesyLabel = new Label("Images provided by iTunes Search API.");
        courtesyLabel.setAlignment(Pos.CENTER_RIGHT);
        progressTab.getChildren().addAll(progressBar, courtesyLabel);
        progressTab.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        List<String> mediaTypes = new ArrayList<>(
                Arrays.asList("movie",
                        "podcast",
                        "music",
                        "musicVideo",
                        "audiobook",
                        "shortFilm",
                        "tvShow",
                        "software",
                        "ebook",
                        "all"));

        genres = new ComboBox<>();
        genres.getItems().addAll(mediaTypes);
        genres.setValue("music");
    } // createProgressTab

    /**
     * Constructs a URL List based on {@code response}.
     *
     * @param response response to a search
     * @return imageURLList images listed according to URL
     */
    public List<String> getURLStrings(HttpResponse<String> response) {
        if (response == null) {
            return Collections.emptyList();
        } // if
        Gson gson = new Gson();
        String responseBody = response.body().trim();
        if (responseBody.startsWith("{")) {
            itunesResponse = gson.fromJson(responseBody, ItunesResponse.class);
            ItunesResult[] results = itunesResponse.results;

            List<String> imageURLList = new ArrayList<String>();
            for (ItunesResult itunesResult : results) {
                imageURLList.add(itunesResult.artworkUrl100);
            } // for

            return imageURLList;
        } else {
            return Collections.emptyList();
        } // else
    } // getURLStrings

    /**
     * Creates alerts for error messages.
     *
     * @param headerErrorMessage header of the error message
     * @param contentErrorMessage content of the error message
     * @param url URL of the search query
     */
    public void createAlert(String headerErrorMessage, String contentErrorMessage, String url) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
        alert.getDialogPane().setPrefHeight(300);
        alert.getDialogPane().setPrefWidth(600);
        alert.setTitle("Error");
        String formattedContent = "URI: " + url + "\n\nException: " + headerErrorMessage + ": "
                                  + contentErrorMessage;
        alert.setContentText(formattedContent);
        alert.showAndWait();
    } // createAlert

    /**
     * Updates the current images displayed on screen whenever the "Update Image" button
     * is pressed by gathering a new set of images from the specified search query and
     * displaying those images instead.
     *
     * @param searchQuery the search query that the user entered in the search
     */
    public void updateImages(String searchQuery) {
        HttpResponse<String> response = fetchImages(searchQuery);
        if (response == null) {
            return;
        } // if
        List<String> tempList = getURLStrings(response);
        List<String> newList = uniqueList(tempList);
        if (newList.size() < 21) {
            String contentText = newList.size() +
                " distinct results found, but 21 or more are needed.";
            if (!alertShown) {
                Platform.runLater(() -> createAlert("java.lang.IllegalArgumentException",
                                                    contentText, urlString));
                alertShown = true;
                updateUrlLabel("Last attempt to get images failed...");
                this.btnPlayPause.setDisable(true);
            } // if
            return;
        } else {
            Set<String> usedURLsSet = new HashSet<>();
            alertShown = false;
            boolean appHasAlreadySetUp = false;
            if (unusedImageURLs != null) {
                unusedImageURLs.clear(); // Resets unused image URL list.
                currentProgress = 0.0;
                progressBar.setProgress(currentProgress);
                appHasAlreadySetUp = true;
            } // if
            unusedImageURLs.addAll(newList);
            Runnable runnable;
            for (int i = 0; i < 20; i++) {
                usedImageURLs[i] = unusedImageURLs.get(i);
                Image currentImage = new Image(usedImageURLs[i], DEF_HEIGHT,
                                               DEF_WIDTH, false, false);
                imageViewsOnScreen[i] = new ImageView(currentImage);
                if (appHasAlreadySetUp) {
                    Platform.runLater(() -> {
                        currentProgress += 0.05;
                        progressBar.setProgress(currentProgress);
                    });
                } // if
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Platform.runLater(() -> createAlert("java.lang.InterruptedException",
                                                        e.getMessage(), urlString));
                } // try
            } // for
            Platform.runLater(() -> tile.getChildren().clear());
            for (int i = 0; i < 20; i++) {
                runnable = createRunnable(i);
                Platform.runLater(runnable);
            } // for
            unusedImageURLs.subList(0, 20).clear();
            updateUrlLabel(this.urlString);
            this.btnPlayPause.setDisable(false);
        } // if
    } // updateImages

    /**
     * Checks if any URLs are duplicate.
     *
     * @param tempList list used to find any URL duplicates
     * @return uniqueReponses unique responses found
     */
    public List<String> uniqueList(List<String> tempList) {
        List<String> uniqueResponses = new ArrayList<>();
        List<String> duplicateResponses = new ArrayList<>();
        for (String url : tempList) {
            if (!uniqueResponses.contains(url)) {
                uniqueResponses.add(url);
            } else {
                duplicateResponses.add(url);
            } // if
        } // for
        return uniqueResponses;
    } // uniqueList

    /**
     * Creates and returns runnable.
     *
     * @param i index of image
     * @return tile new image
     */
    public Runnable createRunnable(int i) {
        return () -> {
            tile.getChildren().add(imageViewsOnScreen[i]); // Adds images.
            VBox.setVgrow(tile, Priority.ALWAYS);
        };
    } // createRunnable

    /**
     * Determines if a condition is in playable mode.
     *
     * @param enablePlayMode true if the app can be played
     */
    public void playMode(boolean enablePlayMode) {
        if (enablePlayMode) {
            EventHandler<ActionEvent> handler = event -> {
                int randomNonDisplayedImage = 0;
                if (!unusedImageURLs.isEmpty()) {
                    randomNonDisplayedImage = new Random().nextInt(unusedImageURLs.size());
                } // if
                int randomDisplayedImage = new Random().nextInt(usedImageURLs.length);
                unusedImageURLs.add(usedImageURLs[randomDisplayedImage]);
                Image newImage = new Image(unusedImageURLs.get(randomNonDisplayedImage));
                imageViewsOnScreen[randomDisplayedImage].setImage(newImage);
                imageViewsOnScreen[randomDisplayedImage].setFitWidth(DEF_WIDTH);
                imageViewsOnScreen[randomDisplayedImage].setFitHeight(DEF_HEIGHT);
                usedImageURLs[randomDisplayedImage] =
                    unusedImageURLs.get(randomNonDisplayedImage);
                unusedImageURLs.remove(randomNonDisplayedImage);
            };
            startRandomlyReplacingImage(handler);
            this.timeline = new Timeline(new KeyFrame(Duration.seconds(2), handler));
            this.timeline.setCycleCount(Timeline.INDEFINITE);
            this.timeline.play();
        } else {
            if (this.timeline != null) {
                this.timeline.pause();
            } // if
        } // if
    } // playMode


    /**
     * Replaces images randomly.
     *
     * @param handler event handler
     */
    public void startRandomlyReplacingImage(EventHandler<ActionEvent> handler) {
        if (timeline == null) {
            keyFrame = new KeyFrame(Duration.seconds(2), handler);
            timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().add(keyFrame);
        } // if
    } // startRandmlyReplacingImage

    /**
     * Updates the text in URL label.
     *
     * @param newText new Text that the label will update to
     */
    public void updateUrlLabel(String newText) {
        Platform.runLater(() -> this.urlLabel.setText(newText));
    } // updateUrlLabel

    /**
     * Runs the {@code target}.
     *
     * @param target object which will be run
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runNow
} // ImageGallery
