package views;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class firstCard {
	public static void Display(String first) {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.initStyle(StageStyle.UNDECORATED);
		GridPane cardInfo = new GridPane();
		Scene info = new Scene(cardInfo,200,200);
		Label firstPlayer = new Label();
		Button hide = new Button();
		hide.setText("Hide");
		hide.setOnAction(e->window.close());
		cardInfo.setBackground(Background.fill(Color.BLACK));
		firstPlayer.setText(first);
		firstPlayer.setTextFill(Color.YELLOWGREEN);
		cardInfo.add(firstPlayer, 0, 0);
		cardInfo.add(hide, 10, 10);
		window.setScene(info);
		window.showAndWait();
		
		
	}
}