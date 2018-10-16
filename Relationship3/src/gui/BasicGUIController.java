package gui;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class BasicGUIController {
	@FXML
	private Button startButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button loadButton;
	@FXML
	private Button savePDF;
	@FXML
	private Button saveSVG;
	@FXML
	private ColorPicker selectColor;
	@FXML
	private ComboBox<String> mouseModeSelection;
	@FXML
	private AnchorPane graphDisplayPane;
	@FXML
	private ComboBox<String> layoutSelection;
	@FXML
	private CheckBox highlight;
	@FXML
	private CheckBox keepPainting;
	@FXML
	private CheckBox gradientEdges;
	@FXML
	private CheckBox qCurve;
	@FXML
	private CheckBox cCurve;
	@FXML
	private CheckBox oCurve;
	@FXML
	private CheckBox sCurve;
	@FXML
	private MenuBar menuBar;
	
	VisualizationInstance visualizationObject;
	ObservableList<String> mouseModeList = FXCollections.observableArrayList("PICKING", "TRANSFORMING");
	ObservableList<String> layoutList = FXCollections.observableArrayList("Static", "Circle", "Kamada Kawai",
			"Fruchterman Reingold", "Self Organizing Map");
	//VisualizationInstance visualizationObject = new VisualizationInstance();
	SwingNode swingNode = new SwingNode();

	@FXML
	private void initialize() {
		mouseModeSelection.setValue("TRANSFORMING");
		mouseModeSelection.setItems(mouseModeList);
		layoutSelection.setValue("Static");
		layoutSelection.setItems(layoutList);
		// javafx.scene.paint.Color fxColor = new
		// javafx.scene.paint.Color(255.0,0.0,0.0,0.0); //Red as deafult
		selectColor.setValue(javafx.scene.paint.Color.RED);
		qCurve.setSelected(true);
	}

	private void updatePane() {
		graphDisplayPane.getChildren().remove(swingNode);
		swingNode.setContent(visualizationObject.getScrollPanel());
		graphDisplayPane.getChildren().add(swingNode);
	}
	@FXML
	private void openGraph() {
		FileChooser fc = new FileChooser();
		File selectedFile = fc.showOpenDialog(null);
		if (selectedFile != null) {
			
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Open File");
			alert.setHeaderText("You must select a TXT file.");
			alert.show();
		}
	}
	@FXML
	private void startGraph() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("Concept Map TXT", "*.txt");
		fileChooser.getExtensionFilters().add(fileExtension);
		File selectedFile = fileChooser.showOpenDialog(null);
		visualizationObject = new VisualizationInstance(selectedFile.getAbsolutePath());
		swingNode.setContent(visualizationObject.getScrollPanel());
		graphDisplayPane.getChildren().add(swingNode);
	}

	@FXML
	private void selectMouseMode() {
		if (mouseModeSelection.getValue().equals("PICKING")) {
			visualizationObject.setMouseMode(1);
		}
		if (mouseModeSelection.getValue().equals("TRANSFORMING")) {
			visualizationObject.setMouseMode(0);
		}
	}

	@FXML
	private void selectLayout() {
		if (layoutSelection.getValue().equals("Static")) {
			visualizationObject.setLayoutNumber(0);
		}
		if (layoutSelection.getValue().equals("Circle")) {
			visualizationObject.setLayoutNumber(1);
		}
		if (layoutSelection.getValue().equals("Kamada Kawai")) {
			visualizationObject.setLayoutNumber(2);
		}
		if (layoutSelection.getValue().equals("Self Organizing Map")) {
			visualizationObject.setLayoutNumber(3);
		}
		if (layoutSelection.getValue().equals("Fruchterman Reingold")) {
			visualizationObject.setLayoutNumber(4);
		}
	}

	@FXML
	private void save() {
		layoutSelection.setValue("Static");
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("XML", "*.xml");
		fileChooser.getExtensionFilters().add(fileExtension);
		File selectedFile = fileChooser.showSaveDialog(null);
		visualizationObject.saveGraph(selectedFile.getAbsolutePath());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Saving Process");
		alert.setHeaderText("The graph was successfully saved.");
		alert.show();
	}

	@FXML
	private void load() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("XML", "*.xml");
		fileChooser.getExtensionFilters().add(fileExtension);
		File selectedFile = fileChooser.showOpenDialog(null);
		visualizationObject.loadGraph(selectedFile.getAbsolutePath());
		updatePane();
	}

	@FXML
	private void changeColor() {
		visualizationObject.setVertexColor(selectColor.getValue(), keepPainting.isSelected());
	}

	@FXML
	private void saveGraphInPDF() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("PDF", "*.pdf");
		fileChooser.getExtensionFilters().add(fileExtension);
		File selectedFile = fileChooser.showSaveDialog(null);
		visualizationObject.saveInPDF(selectedFile.getAbsolutePath());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Saving in PDF Process");
		alert.setHeaderText("The graph was successfully saved.");
		alert.show();
	}

	@FXML
	private void saveGraphInSVG() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("SVG", "*.svg");
		fileChooser.getExtensionFilters().add(fileExtension);
		File selectedFile = fileChooser.showSaveDialog(null);
		visualizationObject.saveInSVG(selectedFile.getAbsolutePath());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Saving in SVG Process");
		alert.setHeaderText("The graph was successfully saved.");
		alert.show();
	}

	@FXML
	private void highlight() {
		visualizationObject.highlightRelations(highlight.isSelected());
	}

	@FXML
	private void applyGradientEdges() {
		visualizationObject.gradientEdges(gradientEdges.isSelected());
	}

	@FXML
	private void changeEdgeShapeQ() {
		cCurve.setSelected(false);
		oCurve.setSelected(false);
		sCurve.setSelected(false);
		visualizationObject.changeEdgeShape(qCurve.isSelected(), cCurve.isSelected(), oCurve.isSelected(),
				sCurve.isSelected());
	}

	@FXML
	private void changeEdgeShapeC() {
		qCurve.setSelected(false);
		oCurve.setSelected(false);
		sCurve.setSelected(false);
		visualizationObject.changeEdgeShape(qCurve.isSelected(), cCurve.isSelected(), oCurve.isSelected(),
				sCurve.isSelected());
	}

	@FXML
	private void changeEdgeShapeO() {
		qCurve.setSelected(false);
		cCurve.setSelected(false);
		sCurve.setSelected(false);
		visualizationObject.changeEdgeShape(qCurve.isSelected(), cCurve.isSelected(), oCurve.isSelected(),
				sCurve.isSelected());
	}

	@FXML
	private void changeEdgeShapeS() {
		qCurve.setSelected(false);
		cCurve.setSelected(false);
		oCurve.setSelected(false);
		visualizationObject.changeEdgeShape(qCurve.isSelected(), cCurve.isSelected(), oCurve.isSelected(),
				sCurve.isSelected());
	}
}
