package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.ForecastCalculator;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class IncomeView  extends Dialog {

	private final ForecastService service;
	private final List<PeriodicChangeRuleDto> dtos;
	private final Grid<CalcItem> resultsGrid = new Grid<>();
	private DatePicker datePicker;
	private Button calculateButton;
	private final long chatId;

	public IncomeView(ForecastService service, List<PeriodicChangeRuleDto> dtos, long chatId) {
		this.service = service;
		this.dtos = dtos;
		this.chatId = chatId;
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
				createResultsGrid()
//				createActionButtons()
		);
		mainLayout.setFlexGrow(1, resultsGrid);

		add(mainLayout);
	}

	private Component createHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		header.setAlignItems(FlexComponent.Alignment.CENTER);

		H2 title = new H2("Расчет прогнозного дохода");
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
		resultsGrid.addColumn(item -> item.rule().getName())
				.setHeader("Операция")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

//		// Колонка с суммой операции
//		resultsGrid.addComponentColumn(item -> new Span(formatSum(item.rule().getSum())))
//				.setHeader("Сумма")
//				.setAutoWidth(true)
//				.setFlexGrow(1);
//
//		// Колонка с балансом
//		resultsGrid.addColumn(item -> formatBalance(item.balance()))
//				.setHeader("Баланс")
//				.setSortable(true)
//				.setAutoWidth(true)
//				.setFlexGrow(1);

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
}
