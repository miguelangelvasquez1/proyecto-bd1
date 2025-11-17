package com.tienda.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;

public class CalendarController implements Initializable {

    @FXML private Label lblClock;
    @FXML private Label lblMonthYear;
    @FXML private Label lblSelectedDate;
    @FXML private Label lblDayOfYear;
    @FXML private Label lblWeekOfYear;
    @FXML private Label lblDaysInMonth;
    @FXML private GridPane weekDaysGrid;
    @FXML private GridPane daysGrid;
    @FXML private Button btnPreviousMonth;
    @FXML private Button btnNextMonth;
    @FXML private Button btnPreviousYear;
    @FXML private Button btnNextYear;
    @FXML private Button btnToday;
    @FXML private Button btnBack;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        
        // Iniciar el reloj
        startClock();
        
        // Renderizar el calendario
        renderCalendar();
        
        // Actualizar información adicional
        updateFooterInfo();
    }

    @FXML
    private void handleBack() {
        try {
            // Detener el reloj antes de salir
            stopClock();
            
            // Obtener el Stage actual
            Stage currentStage = (Stage) btnBack.getScene().getWindow();
            
            // Cargar la vista del Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainWindow.fxml"));
            Parent root = loader.load();
            
            
            // Cambiar la escena
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Home");
            
        } catch (IOException e) {
            System.err.println("Error al volver al Home: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar alerta de error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo volver al Home");
            alert.setContentText("Detalle: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Inicia el reloj digital
     */
    private void startClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            lblClock.setText(currentTime.format(formatter));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Renderiza el calendario completo
     */
    private void renderCalendar() {
        // Actualizar el título del mes y año
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
        String monthYear = currentYearMonth.format(formatter);
        // Capitalizar primera letra
        monthYear = monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1);
        lblMonthYear.setText(monthYear);
        
        // Actualizar fecha seleccionada
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String selectedDateStr = selectedDate.format(dateFormatter);
        selectedDateStr = selectedDateStr.substring(0, 1).toUpperCase() + selectedDateStr.substring(1);
        lblSelectedDate.setText("Fecha seleccionada: " + selectedDateStr);
        
        // Renderizar días de la semana
        renderWeekDays();
        
        // Renderizar días del mes
        renderDays();
        
        // Actualizar información del footer
        updateFooterInfo();
    }

    /**
     * Renderiza los días de la semana (Dom, Lun, Mar, etc.)
     */
    private void renderWeekDays() {
        weekDaysGrid.getChildren().clear();
        
        String[] daysOfWeek = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.getStyleClass().add("week-day-label");
            
            // Destacar fin de semana
            if (i == 0 || i == 6) {
                dayLabel.getStyleClass().add("weekend-label");
            }
            
            weekDaysGrid.add(dayLabel, i, 0);
        }
    }

    /**
     * Renderiza los días del mes
     */
    private void renderDays() {
        daysGrid.getChildren().clear();
        
        // Configurar las filas dinámicamente
        daysGrid.getRowConstraints().clear();
        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(16.66);
            row.setMinHeight(80);
            daysGrid.getRowConstraints().add(row);
        }
        
        // Primer día del mes
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Domingo = 0
        
        // Días del mes anterior (para llenar espacios vacíos)
        YearMonth previousMonth = currentYearMonth.minusMonths(1);
        int daysInPreviousMonth = previousMonth.lengthOfMonth();
        int startDay = daysInPreviousMonth - dayOfWeek + 1;
        
        int row = 0;
        int col = 0;
        
        // Días del mes anterior (grises)
        for (int i = 0; i < dayOfWeek; i++) {
            LocalDate date = previousMonth.atDay(startDay + i);
            VBox dayCell = createDayCell(date, true);
            daysGrid.add(dayCell, col, row);
            col++;
        }
        
        // Días del mes actual
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date, false);
            
            daysGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
        
        // Días del mes siguiente (grises)
        YearMonth nextMonth = currentYearMonth.plusMonths(1);
        int remainingCells = (6 * 7) - (dayOfWeek + daysInMonth);
        for (int day = 1; day <= remainingCells && row < 6; day++) {
            LocalDate date = nextMonth.atDay(day);
            VBox dayCell = createDayCell(date, true);
            
            daysGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Crea una celda de día
     */
    private VBox createDayCell(LocalDate date, boolean isOtherMonth) {
        VBox cell = new VBox(5);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.getStyleClass().add("day-cell");
        
        // Estilos para días de otros meses
        if (isOtherMonth) {
            cell.getStyleClass().add("other-month-day");
        }
        
        // Estilos para el día actual
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("current-day");
        }
        
        // Estilos para día seleccionado
        if (date.equals(selectedDate)) {
            cell.getStyleClass().add("selected-day");
        }
        
        // Estilos para fin de semana
        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        if (dayOfWeek == 0 || dayOfWeek == 6) {
            cell.getStyleClass().add("weekend-day");
        }
        
        // Número del día
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setFont(Font.font("System", FontWeight.BOLD, 18));
        dayNumber.getStyleClass().add("day-number");
        
        // Nombre del día (solo para día actual y seleccionado)
        if (date.equals(LocalDate.now()) || date.equals(selectedDate)) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            Label dayNameLabel = new Label(dayName);
            dayNameLabel.setFont(Font.font("System", 10));
            dayNameLabel.getStyleClass().add("day-name");
            cell.getChildren().addAll(dayNameLabel, dayNumber);
        } else {
            cell.getChildren().add(dayNumber);
        }
        
        // Evento de clic
        cell.setOnMouseClicked(event -> handleDayClick(date));
        
        return cell;
    }

    /**
     * Maneja el clic en un día
     */
    private void handleDayClick(LocalDate date) {
        selectedDate = date;
        
        // Si el día clickeado es de otro mes, navegar a ese mes
        if (date.getMonth() != currentYearMonth.getMonth()) {
            currentYearMonth = YearMonth.of(date.getYear(), date.getMonth());
        }
        
        renderCalendar();
    }

    /**
     * Actualiza la información del footer
     */
    private void updateFooterInfo() {
        // Día del año
        int dayOfYear = selectedDate.getDayOfYear();
        lblDayOfYear.setText("Día del año: " + dayOfYear);
        
        // Semana del año
        WeekFields weekFields = WeekFields.of(new Locale("es", "ES"));
        int weekOfYear = selectedDate.get(weekFields.weekOfWeekBasedYear());
        lblWeekOfYear.setText("Semana: " + weekOfYear);
        
        // Días en el mes
        int daysInMonth = currentYearMonth.lengthOfMonth();
        lblDaysInMonth.setText("Días en este mes: " + daysInMonth);
    }

    @FXML
    private void handlePreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handlePreviousYear() {
        currentYearMonth = currentYearMonth.minusYears(1);
        renderCalendar();
    }

    @FXML
    private void handleNextYear() {
        currentYearMonth = currentYearMonth.plusYears(1);
        renderCalendar();
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        renderCalendar();
    }
    
    /**
     * Detener el reloj cuando se cierra la ventana
     */
    public void stopClock() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}
