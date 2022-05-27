package views;

import java.io.File;

import engine.Game;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Main extends Application{
	
	Stage window;
	Scene names;
	Scene welcome;
	Button exit;
	Button playBtn;
	Button startGame;
	TextField firstField;
	TextField secondField;
	Label first;
	Label second;
	Button showFirstCard;
	public void start(Stage primaryStage) {
		try {
			Media media = new Media(getClass().getResource("movie.mp4").toExternalForm());
			MediaPlayer mediaPlayer = new MediaPlayer(media);   
			window = primaryStage;
			window.initStyle(StageStyle.UNDECORATED);
		    Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
			GridPane root = new GridPane();
			Scene screen = new Scene(root,screenSize.getWidth(),screenSize.getHeight());
			screen.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			root.setVgap(10);
			root.setBackground(Background.fill(Color.BLACK));
			root.setHgap(8);
			window.setScene(screen);
			//Game a = new Game();
			mediaPlayer.setAutoPlay(true);  
			MediaView mediaView = new MediaView (mediaPlayer);
			mediaView.fitWidthProperty().bind(window.widthProperty());
			mediaView.fitHeightProperty().bind(window.heightProperty());
			mediaView.setOnMouseClicked(e->window.setScene(welcome));
			mediaView.setPreserveRatio(true);
			root.getChildren().add(mediaView);
			mediaPlayer.setOnEndOfMedia(new Runnable() {
		        public void run() {
		        	window.setScene(welcome);
		        }
			});
			
			GridPane play = new GridPane();
			play.setVgap(10);
			play.setHgap(8);
			welcome = new Scene(play,screenSize.getWidth(),screenSize.getHeight());
			Image img = new Image(getClass().getResource("wallpaper.jpg").toExternalForm());
			Image img2 = new Image(getClass().getResource("button.png").toExternalForm());
			Image exitImg = new Image(getClass().getResource("exitimage.png").toExternalForm());
			BackgroundImage bImg = new BackgroundImage(img,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
			Background bGround = new Background(bImg);
	        ImageView imageView = new ImageView(img2);
	        ImageView imageViewExit= new ImageView(exitImg);
	        imageView.setFitWidth(4);
	        imageView.setScaleX(3);
	        imageView.setScaleY(3);
	        imageView.setFitHeight(4);
	        imageViewExit.setFitWidth(4);
	        imageViewExit.setScaleX(3);
	        imageViewExit.setScaleY(3);
	        imageViewExit.setFitHeight(4);
			play.setBackground(bGround);
			startGame = new Button("Start Game", imageView);
			startGame.setOnAction(e->{
				window.setScene(names);
				mediaPlayer.setVolume(10);
			});
			startGame.setScaleX(4);
			startGame.setBorder(Border.stroke(Color.DARKGOLDENROD));
			startGame.setScaleY(4);
			startGame.setBackground(Background.fill(Color.BLACK));
			startGame.setTextFill(Color.DARKGOLDENROD);
			play.add(startGame,80,90);
			exit = new Button("Exit", imageViewExit);
			exit.setOnAction(e->window.close());
			exit.setScaleX(4);			
			exit.setScaleY(4);
			exit.setBorder(Border.stroke(Color.DARKGOLDENROD));
			exit.setBackground(Background.fill(Color.BLACK));
			exit.setTextFill(Color.DARKGOLDENROD);
			play.add(exit,150,90);
			
			
			
			GridPane layout = new GridPane();
			layout.setAlignment(Pos.CENTER_LEFT);
			layout.setVgap(10);
			layout.setHgap(8);
			names = new Scene(layout,screenSize.getWidth(),screenSize.getHeight());
			layout.setBackground(bGround);
			firstField = new TextField("Enter your Name");
			secondField = new TextField("Enter your Name");
			firstField.setMaxWidth(500);
			secondField.setMaxWidth(500);
			firstField.setScaleX(1.5);
			firstField.setBackground(Background.fill(Color.DARKGREY));
			secondField.setBackground(Background.fill(Color.DARKGREY));

			firstField.setScaleY(1.5);
			secondField.setScaleX(1.5);
			secondField.setScaleY(1.5);
			first = new Label();
			second = new Label();
			first.setText("First Player: ");
			first.setTextFill(Color.WHITE);
			first.setBorder(Border.stroke(Color.DARKGOLDENROD));
			second.setText("Second Player: ");
			second.setTextFill(Color.WHITE);
			second.setBorder(Border.stroke(Color.DARKGOLDENROD));
			first.setScaleX(2);
			first.setScaleY(2);
			first.setBackground(Background.fill(Color.BLACK));
			second.setScaleX(2);
			second.setScaleY(2);
			second.setBackground(Background.fill(Color.BLACK));
			showFirstCard = new Button();
			showFirstCard.setOnAction(e->firstCard.Display(first.getText()));
			showFirstCard.setText("Show first player info");
			ImageView imageView2 = new ImageView(img2);
			playBtn = new Button("Play!", imageView2);
			playBtn.setOnAction(e->{
				window.setScene(names);
				mediaPlayer.setVolume(0);
			});
			imageView2.setFitWidth(4);
		    imageView2.setScaleX(3);
		    imageView2.setScaleY(3);
		    imageView2.setFitHeight(4);
			playBtn.setScaleX(3);
			playBtn.setBorder(Border.stroke(Color.DARKGOLDENROD));
			playBtn.setScaleY(3);
			playBtn.setBackground(Background.fill(Color.BLACK));
			playBtn.setTextFill(Color.DARKGOLDENROD);
			layout.add(firstField,70,50);
			layout.add(secondField,130,50);
			layout.add(first, 70, 47);
			layout.add(second, 130, 47);
			layout.add(playBtn, 100, 60);
			
			window.setFullScreen(true);
			window.setResizable(false);
			window.show();
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}


}