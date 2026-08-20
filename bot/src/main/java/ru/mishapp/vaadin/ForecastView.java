package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ForecastView extends Dialog {

    private final ForecastService service;
    private final List<PeriodicChangeRuleDto> dtos;
    private final Grid<CalcItem> resultsGrid = new Grid<>();
    private DatePicker datePicker;

    public ForecastView(ForecastService service, List<PeriodicChangeRuleDto> dtos) {
        this.service = service;
        this.dtos = dtos;
        initDialog();
    }

    private void initDialog() {
        setWidth("900px");
        setHeight("700px");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
        setDraggable(true);
        setResizable(true);

        // Создаем основное содержимое
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setSizeFull();

        // Добавляем компоненты
        mainLayout.add(createHeader(),
                createDatePickerSection(),
                createResultsGrid(),
                createActionButtons()
        );
        mainLayout.setFlexGrow(1, resultsGrid);

        add(mainLayout);
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Расчет прогнозного баланса");
        title.getStyle()
                .set("margin", "0");

        Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> close());

        header.add(title, closeButton);
        return header;
    }

    private Component createDatePickerSection() {
        VerticalLayout dateSection = new VerticalLayout();
        dateSection.setSpacing(true);
        dateSection.setPadding(false);

        // Информационная панель
        HorizontalLayout infoPanel = new HorizontalLayout();
        infoPanel.setWidthFull();
        infoPanel.setAlignItems(FlexComponent.Alignment.CENTER);
        infoPanel.setSpacing(true);

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("16px");
        infoIcon.getStyle().set("color", "var(--lumo-primary-color)");

        Span infoText = new Span("Выберите дату для расчета прогноза баланса");
        infoText.getStyle()
                .set("font-weight", "500")
                .set("font-size", "var(--lumo-font-size-m)");

        infoPanel.add(infoIcon, infoText);

        // Датапикер
        HorizontalLayout datePickerLayout = new HorizontalLayout();
        datePickerLayout.setWidthFull();
        datePickerLayout.setAlignItems(FlexComponent.Alignment.END);
        datePickerLayout.setSpacing(true);

        datePicker = new DatePicker("Дата окончания расчета");
        datePicker.setLocale(new Locale("ru"));
        datePicker.setPlaceholder("дд.мм.гггг");
        datePicker.setWidth("250px");
        datePicker.setRequired(true);
        datePicker.setMin(LocalDate.now().plusDays(1));
        datePicker.setMax(LocalDate.now().plusYears(3));
        datePicker.setClearButtonVisible(true);

        // Кнопка "Сегодня + месяц"
        Button monthButton = new Button("+1 месяц", e -> {
            datePicker.setValue(LocalDate.now().plusMonths(1).withDayOfMonth(1));
        });
        monthButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Кнопка "Сегодня + 1 год"
        Button oneYearButton = new Button("+1 год", e -> {
            datePicker.setValue(LocalDate.now().plusYears(1).withDayOfMonth(1));
        });
        oneYearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Кнопка "Сегодня + 2 года"
        Button twoYearsButton = new Button("+2 года", e -> {
            datePicker.setValue(LocalDate.now().plusYears(2).withDayOfMonth(1));
        });
        twoYearsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        datePickerLayout.add(datePicker, monthButton, oneYearButton, twoYearsButton);

        dateSection.add(infoPanel, datePickerLayout);
        return dateSection;
    }

    private Component createResultsGrid() {
        resultsGrid.removeAllColumns();

        // Колонка с датой
        resultsGrid.addColumn(item -> item.day().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .setHeader("Дата")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Колонка с операцией
        resultsGrid.addColumn(item -> item.rule().name())
                .setHeader("Операция")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Колонка с суммой операции
        resultsGrid.addComponentColumn(item -> new Span(formatSum(item.rule().sum())))
                .setHeader("Сумма")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Колонка с балансом
        resultsGrid.addColumn(item -> formatBalance(item.balance()))
                .setHeader("Баланс")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Настройка внешнего вида
        resultsGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_COLUMN_BORDERS
        );

        resultsGrid.setHeight("300px");
        resultsGrid.setVisible(false); // Скрываем до расчета

        return resultsGrid;
    }

    private String formatBalance(int balance) {
        return String.format("%,d₽", balance).replace(',', ' ');
    }

    private String formatSum(int sum) {
        String sign = sum < 0 ? "-" : "";
        return String.format("%s%,d₽", sign, Math.abs(sum)).replace(',', ' ');
    }

    private Component createActionButtons() {
        Button calculateButton;
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setSpacing(true);

        // Кнопка расчета
        calculateButton = new Button("Рассчитать прогноз", VaadinIcon.CALC.create());
        calculateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        calculateButton.setEnabled(false);
        calculateButton.addClickListener(e -> calculateForecast());

        // Кнопка отмены
        Button cancelButton = new Button("Отмена", VaadinIcon.CLOSE.create());
        cancelButton.addClickListener(e -> close());

        // Активация кнопки при выборе даты
        datePicker.addValueChangeListener(e -> {
            calculateButton.setEnabled(e.getValue() != null);
        });

        buttons.add(calculateButton, cancelButton);
        buttons.setFlexGrow(1, calculateButton);

        return buttons;
    }

    private void calculateForecast() {
        if (datePicker.getValue() == null) {
            Notification.show("Выберите дату для расчета",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        LocalDate targetDate = datePicker.getValue();

        // Показываем диалог загрузки
        Dialog loadingDialog = createLoadingDialog();
        loadingDialog.open();

        // Асинхронный расчет
        UI.getCurrent().access(() -> {
            try {
                // Выполняем расчет
                List<CalcItem> results = service.calculateForecast(dtos, targetDate);

                loadingDialog.close();

                if (results.isEmpty()) {
                    Notification.show("Нет данных для расчета на выбранную дату",
                            3000, Notification.Position.MIDDLE);
                } else {
                    // Показываем результаты
                    showResults(results, targetDate);
                }

            } catch (Exception e) {
                loadingDialog.close();
                Notification.show("Ошибка расчета: " + e.getMessage(),
                        5000, Notification.Position.BOTTOM_END);
            }
        });
    }

    private Dialog createLoadingDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("200px");

        Span loadingText = new Span("Выполняется расчет прогноза...");
        loadingText.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)");

        Span detailsText = new Span("Обработка данных...");
        detailsText.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        layout.add(loadingText, progressBar, detailsText);
        dialog.add(layout);

        return dialog;
    }

    private void showResults(List<CalcItem> results, LocalDate targetDate) {
        // Обновляем заголовок диалога
        setHeaderTitle(String.format("Прогноз до %s",
                targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

        // Устанавливаем данные в таблицу
        resultsGrid.setItems(results);
        resultsGrid.setVisible(true);

        // Прокручиваем к результатам
        resultsGrid.scrollToStart();
    }
}