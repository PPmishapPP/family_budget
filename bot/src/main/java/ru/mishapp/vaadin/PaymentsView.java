package ru.mishapp.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ChatService;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.PeriodicChangeService;

@RequiredArgsConstructor
@Route(value = "categories", layout = MainLayout.class)
public class PaymentsView extends VerticalLayout {

	private Grid<PeriodicChangeRule> grid;
	private final PeriodicChangeRuleService periodicChangeRuleService;
	private final PeriodicChangeService periodicChangeService;
	private final AccountService accountService;
	private final ChatService chatService;

	@PostConstruct
	public void configure() {
		this.grid = new Grid<>(PeriodicChangeRule.class);
		configureHeader();
		configureGrid();
		add(grid);
		setSizeFull();
		// Заголовок страницы
	}

	private void configureHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);
		header.setAlignItems(Alignment.CENTER);
		header.add(new H2("План платежей"));
		Button addButton = new Button("Добавить");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e ->
				new PeriodicChangeDialog(periodicChangeRuleService, this::updateGrid, periodicChangeService, accountService, chatService).open());
		header.add(addButton);
		add(header);
	}

	private void updateGrid() {
		grid.setItems(periodicChangeRuleService.readAll());
	}

	private void configureGrid() {
		// Убираем стандартные колонки
		grid.removeAllColumns();
		// Колонка с названием
		grid.addColumn(PeriodicChangeRule::getName)
				.setHeader("Название")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

		grid.addColumn(PeriodicChangeRule::getSum)
				.setHeader("Сумма")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

		grid.addComponentColumn(item -> {
			HorizontalLayout layout = new HorizontalLayout();
			layout.setAlignItems(Alignment.CENTER);
			layout.setSpacing(true);

			Span badge = new Span(item.getType().getDescription());
			layout.add(badge);
			return layout;
		}).setHeader("Тип").setWidth("120px");

		grid.addColumn(PeriodicChangeRule::getNextDay)
				.setHeader("Следующий платеж")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

		grid.addComponentColumn(item -> new Span(item.isActive() ? "Активно" : "Неактивно"))
				.setHeader("Активность").setWidth("120px");

		grid.addColumn(PeriodicChangeRule::getEndDate)
				.setHeader("Последний платеж")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
				GridVariant.LUMO_COMPACT,
				GridVariant.LUMO_COLUMN_BORDERS);
		grid.setItems(periodicChangeRuleService.readAll());
	}
}
