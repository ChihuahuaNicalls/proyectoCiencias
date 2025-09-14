package ciencias.Research;

import ciencias.ResearchController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.Label;

public class SecuencialController {
    private ResearchController researchController;
    @FXML
    private MenuButton rangeSec;

    @FXML
    private Spinner<Integer> numberDigits;

    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;

    @FXML
    private Label titleSec;
    @FXML
    private Label functionSec;

    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;

    @FXML
    private ListView<String> miViewList;

    @FXML
    private Button createButton;
    @FXML
    private Button reiniciarButton;
    @FXML
    private Button insertButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;


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
    private void searchItem(){
    }
    @FXML
    private void eliminateItem(){
    }
    @FXML
    private void undoAction(){
    }
    @FXML
    private void redoAction(){
    }
    @FXML
    private void saveArray(){
    }
    @FXML
    private void loadArray(){
    }
}

