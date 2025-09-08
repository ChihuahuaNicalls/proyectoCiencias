package ciencias.Research;

import ciencias.ResearchController;
import javafx.fxml.FXML;

public class BinariaController {
    private ResearchController researchController;

    @FXML
    private void initialize(){
    }
    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }
    public void initData() {
        if (researchController == null)
            return;
    }
    @FXML
    private void crearArray() {
    }
    @FXML
    private void addToArray() {
    }
    @FXML
    private void reiniciar(){
    }
    @FXML
    private void enableTrunc(){
    }
    @FXML
    private void elegirTrunc(){
    }
    @FXML
    private void searchItem(){
    }
    @FXML
    private void modifyItem(){
    }
    @FXML
    private void eliminateItem(){
    }
    @FXML
    private void saveArray(){
    }
    @FXML
    private void loadArray(){
    }
}
