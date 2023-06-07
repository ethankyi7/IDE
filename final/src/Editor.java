import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Editor {
    private Stage primaryStage;
    private int autoIndent = 0;
    private int cursorPosition;

    public Editor(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 900, Color.BLACK);
        root.setStyle("-fx-background-color: #2f3542; -fx-border-color: #282C34");

        //create the text area where you can actually write the code
        TextArea textSpace = new TextArea();
        textSpace.setPrefColumnCount(15);
        // textSpace.setPrefWidth(1195);
        textSpace.setStyle("-fx-control-inner-background: #282C34; -fx-text-fill: #ABB2BF; -fx-font-size: 24; -fx-pref-width: 945px; -fx-text-box-border: #282C34; -fx-background-insets: 0;");
        textSpace.setPadding(new Insets(0));

        //root.setRight(textSpace);

        VBox editorContainer = new VBox();
        editorContainer.getChildren().add(textSpace);

        ScrollPane scrollPane = new ScrollPane(textSpace);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));
        scrollPane.setStyle("-fx-background-color: transparent;");

        ListView<Integer> numList = new ListView<Integer>();
        numList.setStyle("<font-style>: Monospaced; -fx-padding: -1;");

        textSpace.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                cursorPosition = textSpace.getCaretPosition();
                
                System.out.println(cursorPosition);
                String upToCode = textSpace.getText().substring(0, cursorPosition);
                String[] precede = upToCode.split(" ");
                int openBraceCounter = 0;
                int closeBraceCounter = 0;
                for(int i = 0; i < precede.length; i++) {
                    if(precede[i].equals("{")) openBraceCounter++;
                    if(precede[i].equals("}")) closeBraceCounter++;
                }
                autoIndent = openBraceCounter - closeBraceCounter;
                System.out.println("autoIndent: " + autoIndent);
                System.out.print("[");
                for(int i = 0; i < precede.length; i++) {
                    System.out.print(precede[i] + " ");
                }
                System.out.println("]");
            }
        });

        textSpace.setOnKeyPressed((t) -> {
            if(t.getCode() == KeyCode.BACK_SPACE && textSpace.getText().length() > 0) {
                cursorPosition = textSpace.getCaretPosition() - 1;
                if (cursorPosition < 0) {
                    cursorPosition = 0;
                }
            }
            else if(t.getCode() == KeyCode.ENTER) {
                StringBuilder indent = new StringBuilder();
                for(int i = 0; i < autoIndent; i++) {
                    indent.append("      ");
                }
                int caretPosition = textSpace.getCaretPosition();
                if(textSpace.getText().substring(caretPosition, caretPosition + 1).equals("}")) textSpace.insertText(caretPosition, indent.toString() + "\n");
                else textSpace.insertText(caretPosition, indent.toString());
                textSpace.positionCaret(caretPosition + indent.length());
            }
        });

        textSpace.setOnKeyTyped(t -> {
            if (t.getCharacter().equals("(")) {
                textSpace.insertText(textSpace.getCaretPosition(), ")");
                textSpace.positionCaret(textSpace.getCaretPosition() - 1);
            }
            if (t.getCharacter().equals("{")) {
                textSpace.insertText(textSpace.getCaretPosition(), "}");
                textSpace.positionCaret(textSpace.getCaretPosition() - 1);
            }
            if (t.getCharacter().equals("[")) {
                textSpace.insertText(textSpace.getCaretPosition(), "]");
                textSpace.positionCaret(textSpace.getCaretPosition() - 1);
            }
        });

        textSpace.textProperty().addListener((obs, oldText, newText) -> {
            String[] lines = newText.split("\n");
            numList.getItems().clear();
            for (int i = 1; i <= lines.length; i++) {
                numList.getItems().add(i);
            }
        });

        scrollPane.vvalueProperty().bindBidirectional(textSpace.scrollTopProperty());
        HBox lineNumbers = new HBox(numList);
        lineNumbers.setAlignment(Pos.CENTER);
        lineNumbers.setPadding(new Insets(0, 0, 0, 0));
                
        lineNumbers.setStyle("-fx-pref-width: 55px; -fx-control-inner-background: #373D49; -fx-text-fill: #FFFFFF; -fx-font-size: 20; -fx-border-color: #373D49;");
        textSpace.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> scrollPane.getHeight() - scrollPane.getPadding().getTop() - scrollPane.getPadding().getBottom(), scrollPane.heightProperty(), scrollPane.paddingProperty()));
        
        root.setRight(scrollPane);
        root.setLeft(lineNumbers);
        
        Terminal terminal = new Terminal(textSpace);
        root.setBottom(terminal.createTerminal());

        return scene;
    }
}