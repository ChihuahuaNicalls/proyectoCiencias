package ciencias;

import java.io.IOException;
import javafx.fxml.FXML;

public class StartController {

    @FXML
    private void toResearch() throws IOException {
        App.setRoot("research");
    }

    @FXML
    private void toGraphs() throws IOException {
    }

    @FXML
    private void exit() throws IOException {
        System.exit(0);
    }
}