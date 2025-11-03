package com.tienda.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CalculatorController {
    
    @FXML private TextField display;
    
    private double operand1 = 0;
    private String operator = "";
    private boolean startNewNumber = true;
    
    public void initialize() {
        display.setText("0");
    }
    
    @FXML
    private void numberPressed(ActionEvent event) {
        Button button = (Button) event.getSource();
        String number = button.getUserData().toString();
        
        if (startNewNumber) {
            display.setText(number);
            startNewNumber = false;
        } else {
            if (!display.getText().equals("0")) {
                display.setText(display.getText() + number);
            } else {
                display.setText(number);
            }
        }
    }
    
    @FXML
    private void decimal() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }
    
    @FXML
    private void add() {
        processOperator("+");
    }
    
    @FXML
    private void subtract() {
        processOperator("-");
    }
    
    @FXML
    private void multiply() {
        processOperator("×");
    }
    
    @FXML
    private void divide() {
        processOperator("÷");
    }
    
    private void processOperator(String newOperator) {
        if (!operator.isEmpty() && !startNewNumber) {
            equals();
        }
        
        operand1 = Double.parseDouble(display.getText());
        operator = newOperator;
        startNewNumber = true;
    }
    
    @FXML
    private void equals() {
        if (!operator.isEmpty()) {
            double operand2 = Double.parseDouble(display.getText());
            double result = 0;
            
            switch (operator) {
                case "+":
                    result = operand1 + operand2;
                    break;
                case "-":
                    result = operand1 - operand2;
                    break;
                case "×":
                    result = operand1 * operand2;
                    break;
                case "÷":
                    if (operand2 != 0) {
                        result = operand1 / operand2;
                    } else {
                        display.setText("Error");
                        clearAll();
                        return;
                    }
                    break;
            }
            
            // Formatear resultado
            if (result == Math.floor(result)) {
                display.setText(String.valueOf((long) result));
            } else {
                display.setText(String.format("%.8f", result).replaceAll("0+$", "").replaceAll("\\.$", ""));
            }
            
            operator = "";
            startNewNumber = true;
        }
    }
    
    @FXML
    private void clearAll() {
        display.setText("0");
        operand1 = 0;
        operator = "";
        startNewNumber = true;
    }
    
    @FXML
    private void clearEntry() {
        display.setText("0");
        startNewNumber = true;
    }
    
    @FXML
    private void backspace() {
        String text = display.getText();
        if (text.length() > 1) {
            display.setText(text.substring(0, text.length() - 1));
        } else {
            display.setText("0");
            startNewNumber = true;
        }
    }
}
