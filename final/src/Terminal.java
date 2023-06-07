import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Terminal {
    private File currentDir;
    private TextArea code;

    public Terminal(TextArea code) {
        this.code = code;
    }

    public VBox createTerminal() {
        currentDir = new File(System.getProperty("user.dir"));
        VBox terminalContainer = new VBox();
        TextArea terminalOutput = new TextArea();
        //terminalOutput.setText(currentDir.getAbsolutePath());
        terminalOutput.appendText(currentDir.getAbsolutePath() + "\n");
        terminalOutput.setStyle("-fx-control-inner-background: #1E2127; -fx-text-fill: #98C379; -fx-text-box-border: #1E2127; -fx-focus-color: transparent");
        terminalOutput.setEditable(false);
        TextField inputField = new TextField();
        inputField.setStyle("-fx-control-inner-background: #1E2127; -fx-text-fill: #61AFEF; -fx-text-box-border: #1E2127; -fx-focus-color: transparent");

        //when stuff in terminal execute command thingy
        inputField.setOnKeyPressed((t) -> {
            if(t.getCode() == KeyCode.ENTER) {
                terminalOutput.appendText(currentDir.getAbsolutePath() + " " + inputField.getText() + "\n");
                if(inputField.getText().equalsIgnoreCase("clear")) terminalOutput.clear();
                else if(inputField.getText().substring(0, 2).equalsIgnoreCase("cd")) {
                    String target = inputField.getText().substring(3).trim();
                    if (target.equals("..")) {
                        File parentDirectory = currentDir.getParentFile();
                        if (parentDirectory != null && parentDirectory.isDirectory()) {
                            currentDir = parentDirectory;
                        } else {
                            terminalOutput.appendText("Invalid directory\n");
                        }
                    } else {
                        File newDirectory = new File(currentDir, target);
                        if (newDirectory.isDirectory()) {
                            currentDir = newDirectory;
                        } else {
                            terminalOutput.appendText("Invalid directory\n");
                        }
                    }
                    terminalOutput.appendText(currentDir.getAbsolutePath() + "\n");
                }
                else if(inputField.getText().equalsIgnoreCase("compile")) {
                    // try {
                    //     String className;
                    //     int classKeywordIndex = code.getText().indexOf("class ");
                    //     int classNameStartIndex = classKeywordIndex + 6; // Length of "class " is 6
                    //     int classNameEndIndex = code.getText().indexOf(" ", classNameStartIndex);
                    //     if (classNameEndIndex == -1) {
                    //         classNameEndIndex = code.getText().indexOf("{", classNameStartIndex);
                    //     }
                    //     if (classNameEndIndex == -1) {
                    //         classNameEndIndex = code.getText().indexOf("\n", classNameStartIndex);
                    //     }
                    //     if (classNameEndIndex == -1) {
                    //         classNameEndIndex = code.getText().length();
                    //     }
                    //     className = code.getText().substring(classNameStartIndex, classNameEndIndex);

                    //     File temp = new File(className + ".java");
                    //     temp.deleteOnExit();

                    //     try (PrintWriter writer = new PrintWriter(temp)) {
                    //         writer.write(code.getText());
                    //     } catch (Exception e) {
                    //         terminalOutput.appendText("Error writing code to temporary file: " + e.getMessage() + "\n");
                    //         return;
                    //     }
                        
                    //     ProcessBuilder processBuilder = new ProcessBuilder("javac", temp.getAbsolutePath());
                    //     processBuilder.redirectErrorStream(true);
                    //     Process process = processBuilder.start();
                    //     BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    //     String line;
                        
                    //     while ((line = reader.readLine()) != null) {
                    //         terminalOutput.appendText(line + "\n");
                    //     }

                    //     int exitCode = process.waitFor();
                    //     if (exitCode == 0) terminalOutput.appendText("Compilation Successful");
                    //     else terminalOutput.appendText("Command failed with exit code: " + exitCode + "\n");

                    // } catch(Exception e) {
                    //     Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
                    //     terminalOutput.appendText("Error compiling code: " + e.getMessage() + "\n");
                    // }
                    compile(terminalOutput);
                }
                else if(inputField.getText().equalsIgnoreCase("run")) {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder("java", compile(terminalOutput).getAbsolutePath());
                        processBuilder.redirectErrorStream(true);
                        Process process = processBuilder.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        
                        while ((line = reader.readLine()) != null) {
                            terminalOutput.appendText(line + "\n");
                        }
                    } catch(Exception e) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
                    }

                }
                else {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder(inputField.getText());
                        processBuilder.redirectErrorStream(true);
                        Process process = processBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        
                        while ((line = reader.readLine()) != null) {
                            terminalOutput.appendText(line + "\n");
                        }

                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            terminalOutput.appendText("Command failed with exit code: " + exitCode + "\n");
                        }
                    } catch (Exception e) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
                        terminalOutput.appendText(inputField.getText() + "\n");
                    }
                }
                inputField.clear();
            }
        });

        terminalContainer.getChildren().addAll(terminalOutput, inputField);
        return terminalContainer;
    }

    public Task<Void> printResults(Process process) {
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String str = "";
                    while((str = br.readLine()) != null) updateMessage(str);

                } catch(Exception e) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
                }
                return null;
            }
        };
        return task;
    }

    public File compile(TextArea terminalOutput) {
        try {
            String className;
            int classKeywordIndex = code.getText().indexOf("class ");
            int classNameStartIndex = classKeywordIndex + 6;
            int classNameEndIndex = code.getText().indexOf(" ", classNameStartIndex);
            if (classNameEndIndex == -1) {
                classNameEndIndex = code.getText().indexOf("{", classNameStartIndex);
            }
            if (classNameEndIndex == -1) {
                classNameEndIndex = code.getText().indexOf("\n", classNameStartIndex);
            }
            if (classNameEndIndex == -1) {
                classNameEndIndex = code.getText().length();
            }
            className = code.getText().substring(classNameStartIndex, classNameEndIndex);

            File temp = new File(className + ".java");
            temp.deleteOnExit();

            try (PrintWriter writer = new PrintWriter(temp)) {
                writer.write(code.getText());
            } catch (Exception e) {
                terminalOutput.appendText("Error writing code to temporary file: " + e.getMessage() + "\n");
                return null;
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder("javac", temp.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                terminalOutput.appendText(line + "\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) terminalOutput.appendText("Compilation Successful" + "\n");
            else terminalOutput.appendText("Command failed with exit code: " + exitCode + "\n");
            return temp;
        } catch(Exception e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
            terminalOutput.appendText("Error compiling code: " + e.getMessage() + "\n");
        }
        return null;
    }
}
