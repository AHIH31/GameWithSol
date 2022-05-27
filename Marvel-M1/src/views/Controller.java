package views;


import engine.Game;
import engine.Player;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Controller{
	Player firstPlayer;
	Player secondPlayer;
	Game currentGame;
	public void Play(TextField first, TextField second, Stage window) {
		if((first.getLength()!=0 && !first.getText().equals("Enter your Name")) && (second.getLength()!=0 && !second.getText().equals("Enter your Name"))){
			firstPlayer = new Player(first.getText());
			secondPlayer = new Player(second.getText());
		}
		else {
			//error window
		}
	}
}