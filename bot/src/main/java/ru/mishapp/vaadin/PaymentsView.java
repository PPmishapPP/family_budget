package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.Chat;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.enumiration.Type;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ChatService;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.PeriodicChangeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@RequiredArgsConstructor
@Route(value = "payments", layout = MainLayout.class)
public class PaymentsView extends VerticalLayout {

	private final PeriodicChangeRuleService periodicChangeRuleService;
	private final ChatService chatService;
	private final ForecastService service;
	private final PeriodicChangeService periodicChangeService;
	private final AccountService accountService;

	private final Binder<EditableItem> binder = new Binder<>(EditableItem.class);
	private final List<PeriodicChangeRuleDto> itemsToDelete = new ArrayList<>();
	private final Button calculateButton = new Button("Выполнить прогноз платежей");
	private final Button editButton = new Button("Режим редактирования");
	private final Button cancelButton = new Button("Отменить изменения");
	private final Button addButton = new Button("Добавить", new Icon(VaadinIcon.PLUS));
	private final Button saveButton = new Button("Сохранить");
	private final Button calculateIncomeButton = new Button("Расчет дохода");

	// Текущий выбранный чат
	private Chat selectedChat;
	private Grid<EditableItem> grid;
	private VerticalLayout panel;
	private List<EditableItem> items = new ArrayList<>();
	private List<EditableItem> originalItems = new ArrayList<>();
	private boolean editMode = false;

	@PostConstruct
	public void configure() {
		this.grid = new Grid<>(EditableItem.class);
		configureHeader();
		createChatButtonsPanel();
		createToolbar();
		configureGrid();
		add(grid);
		add(createActionButtons());
		setSizeFull();
		getElement().executeJs(
				"const style = document.createElement('style');" +
						"style.textContent = '" +
						"vaadin-grid::part(marked-for-deletion) { " +
						"  background-color: var(--lumo-error-color-10pct) !important; " +
						"  opacity: 0.8; " +
						"}" +
						"';" +
						"document.head.appendChild(style);"
		);
	}

	private void configureHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);
		header.setAlignItems(Alignment.CENTER);
		header.add(new H2("План платежей"));
		add(header);
	}

	private void createToolbar() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setJustifyContentMode(JustifyContentMode.END);
		toolbar.setAlignItems(Alignment.END);

		// Кнопка переключения режима редактирования
		editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		editButton.addClickListener(e -> toggleEditMode());
		editButton.setEnabled(false);

		cancelButton.addClickListener(e -> cancelChanges());
		cancelButton.setEnabled(false);

		// Кнопка добавления
		addButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		addButton.setEnabled(false); // По умолчанию выключена
		addButton.addClickListener(e -> addNewRow());

		saveButton.addClickListener(e -> save());
		saveButton.setEnabled(false);

		toolbar.add(editButton, cancelButton, addButton, saveButton);
		add(toolbar);
	}

	private void toggleEditMode() {
		editMode = !editMode;

		// Обновляем состояние кнопок
		addButton.setEnabled(editMode);
		updateButtonStates();

		if (editMode) {
			editButton.setText("Режим просмотра");
			editButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
			editButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

			Notification.show("Режим редактирования включен. Двойной клик для изменения.",
					3000, Notification.Position.BOTTOM_START);

		} else {
			editButton.setText("Режим редактирования");
			editButton.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
			editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

			// Отменяем активное редактирование
			grid.getEditor().cancel();

			Notification.show("Режим редактирования выключен",
					2000, Notification.Position.BOTTOM_START);
		}

		// Обновляем доступность редакторов
		grid.getDataProvider().refreshAll();
	}

	private void deleteEntity(EditableItem item) {
		if (item.isNew()) {
			// Просто удаляем из списка, в БД еще нет
			items.remove(item);
		} else {
			// Помечаем на удаление или удаляем сразу
			// Например, добавляем в список на удаление
			itemsToDelete.add(item.getOriginalDto());
			item.setMarkedForDeletion(true);
		}
		grid.setItems(items);
		grid.getDataProvider().refreshAll();
		updateButtonStates();
	}

	private void addNewRow() {
		// Создаем новый EditableItem с пустыми значениями
		EditableItem newItem = createEmptyEditableItem();

		// Добавляем в список
		items.addFirst(newItem); // Добавляем в начало списка

		// Обновляем грид
		grid.setItems(items);

		// Открываем редактор для новой строки
		grid.getEditor().editItem(newItem);

		updateButtonStates();
	}

	private EditableItem createEmptyEditableItem() {
		// Создаем новый DTO с пустыми/дефолтными значениями
		PeriodicChangeRuleDto emptyDto = new PeriodicChangeRuleDto(null, "", "", "",
				0, Type.MONTHLY.toString(), 0, null, true, null);

		// Создаем EditableItem с одинаковыми current и original (пустыми)
		EditableItem editableItem = new EditableItem(emptyDto);
		editableItem.setNew(true);
		return editableItem;
	}

	private void save() {
		// Сохраняем только измененные и новые элементы
		List<PeriodicChangeRuleDto> itemsToSave = items.stream()
				.filter(item -> item.isModified() || item.isNew())
				.map(EditableItem::getCurrentDto)
				.toList();
		periodicChangeRuleService.saveAll(itemsToSave, selectedChat.getChatId());

		periodicChangeRuleService.deleteAll(itemsToDelete, selectedChat.getChatId());

		// После сохранения обновляем оригинальные значения
		loadDataForChat(selectedChat.getChatId());

		Notification.show("Изменения сохранены", 3000, Notification.Position.TOP_CENTER);

		updateButtonStates();
	}

	private void configureGrid() {
		grid.getEditor().setBinder(binder);
		grid.getEditor().setBuffered(false);
		// Убираем стандартные колонки
		grid.removeAllColumns();
		// Колонка с названием
		grid.addColumn(item ->
		{
			String name = item.getCurrentDto().name();
			if (item.isModified() && !name.equals(item.originalDto.name())) {
				name = "✎ " + name;
			}
			return name;
		})
				.setHeader("Название")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(item -> {
					TextField nameField = new TextField();
					nameField.setValue(item.getCurrentDto().name());
					nameField.setWidthFull();

					// Сохраняем изменения
					nameField.addValueChangeListener(e -> {
						item.setCurrentName(e.getValue());
						grid.getDataProvider().refreshItem(item);
						updateButtonStates();
					});

					return nameField;
				});

		grid.addColumn(item -> String.valueOf(item.getCurrentDto().periodicChangeName()))
				.setHeader("Категория платежа")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createPeriodicChangeNameEditor);

		grid.addColumn(item -> String.valueOf(item.getCurrentDto().targetAccountName()))
				.setHeader("Счёт")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createAccountNameEditor);

		grid.addColumn(item -> {
					int currentSum = item.currentDto.sum();
					String sum = String.valueOf(currentSum);
					if (item.isModified() && currentSum != item.originalDto.sum()) {
						// Показываем измененное значение с иконкой
						sum = sum + " ✎";
					}
					return sum;
				})
				.setHeader("Сумма")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(item -> {
					IntegerField sumField = new IntegerField();
					sumField.setValue(item.currentDto.sum());
					sumField.setStep(1);
					sumField.setMin(0);
					sumField.setWidthFull();
					sumField.setEnabled(editMode); // Только в режиме редактирования

					// Сохраняем изменение при потере фокуса
					sumField.addBlurListener(e -> {
						Integer newValue = sumField.getValue();
						if (newValue != null && !newValue.equals(item.currentDto.sum())) {
							item.setCurrentSum(newValue);
							grid.getDataProvider().refreshItem(item);
							updateButtonStates();
						}
					});

					// Сохраняем по Enter
					sumField.addKeyPressListener(Key.ENTER, e -> {
						Integer newValue = sumField.getValue();
						if (newValue != null) {
							item.setCurrentSum(newValue);
							grid.getDataProvider().refreshItem(item);
							updateButtonStates();
							grid.getEditor().cancel();
						}
					});

					return sumField;
				});

		grid.addComponentColumn(item -> {
					Type type = Type.valueOf(item.currentDto.type());
					String description = type.getDescription();
					if (item.isModified() && !Type.valueOf(item.originalDto.type()).equals(type)) {
						description = description + " ✎";
					}
					return new Span(description);
				}).setHeader("Тип")
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createTypeEditor);

		grid.addColumn(item -> item.getCurrentDto().nextDay())
				.setHeader("Следующий платеж")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createNextDayEditor);

		grid.addComponentColumn(item -> {
					boolean active = item.currentDto.active();
					String flag = active ? "Активно" : "Неактивно";
					if (item.isModified() && item.originalDto.active() != active) {
						flag = "✎ " + flag;
					}
					return new Span(flag);
				})
				.setHeader("Активность")
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createActiveCheckBox);

		grid.addColumn(item -> item.getCurrentDto().endDate())
				.setHeader("Последний платеж")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setEditorComponent(this::createEndDayEditor);

		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
				GridVariant.LUMO_COMPACT,
				GridVariant.LUMO_COLUMN_BORDERS);


		// Включаем редактирование по двойному клику
		grid.addItemClickListener(event -> {
			if (editMode && event.getColumn() != null) {
				grid.getEditor().editItem(event.getItem());
			}
		});

		grid.addComponentColumn(item -> {
					Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
					deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR,
							ButtonVariant.LUMO_SMALL);

					if (!editMode || item.isMarkedForDeletion()) {
						deleteButton.setEnabled(false);
					}

					deleteButton.addClickListener(e -> {
						ConfirmDialog dialog = new ConfirmDialog();
						dialog.setHeader("Подтверждение удаления");
						dialog.setText(String.format("Вы уверены, что хотите удалить \"%s\"?",
								item.getCurrentDto().name()));

						dialog.setCancelable(true);
						dialog.setCancelText("Отмена");

						dialog.setConfirmText("Удалить");
						dialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());

						dialog.addConfirmListener(confirmEvent -> {
							deleteEntity(item);
						});

						dialog.open();
					});
					return deleteButton;
				})
				.setHeader("Действия")
				.setAutoWidth(true)
				.setFlexGrow(1);

		grid.setPartNameGenerator(item -> {
			if (item.isMarkedForDeletion()) {
				return "marked-for-deletion";
			}
			return "";
		});
	}

	private Component createActiveCheckBox(EditableItem item) {
		Checkbox activeCheckbox = new Checkbox();
		// Устанавливаем текущее значение из элемента
		activeCheckbox.setValue(item.currentDto.active());

		// Добавляем слушатель для сохранения изменений
		activeCheckbox.addValueChangeListener(event -> {
			// Обновляем значение в DTO
			item.setIsActive(event.getValue());

			// Обновляем отображение в гриде
			grid.getDataProvider().refreshItem(item);
		});

		// Опционально: делаем чекбокс доступным только в режиме редактирования
		activeCheckbox.setEnabled(editMode); // если у вас есть такой флаг

		return activeCheckbox;
	}

	private Component createPeriodicChangeNameEditor(EditableItem item) {
		ComboBox<PeriodicChange> comboBox = new ComboBox<>();

		// Загружаем данные из сервиса
		List<PeriodicChange> categories = periodicChangeService.readAll(selectedChat.getChatId());
		comboBox.setItems(categories);

		// Настраиваем отображение
		comboBox.setItemLabelGenerator(PeriodicChange::getName);
		comboBox.setPlaceholder("Выберите категорию");
		comboBox.setWidthFull();

		// Устанавливаем текущее значение
		if (item.getCurrentDto().periodicChangeName() != null) {
			categories.stream()
					.filter(cat -> cat.getName().equals(item.getCurrentDto().periodicChangeName()))
					.findFirst()
					.ifPresent(comboBox::setValue);
		}

		// Редактируемо только для новых записей
		comboBox.setReadOnly(!item.isNew());

		// Визуально показываем, что поле только для чтения
		if (!item.isNew()) {
			comboBox.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
			comboBox.getStyle().set("pointer-events", "none");
		}

		// Сохраняем изменения
		comboBox.addValueChangeListener(e -> {
			if (!comboBox.isReadOnly() && e.getValue() != null) {
				PeriodicChange selected = e.getValue();
				item.setCurrentPeriodicChangeName(selected.getName());
				grid.getDataProvider().refreshItem(item);
				updateButtonStates();
			}
		});

		return comboBox;
	}

	private Component createAccountNameEditor(EditableItem item) {
		ComboBox<Account> comboBox = new ComboBox<>();

		// Загружаем данные из сервиса
		List<Account> accounts = accountService.findAllByChatId(selectedChat.getChatId());
		comboBox.setItems(accounts);

		// Настраиваем отображение
		comboBox.setItemLabelGenerator(Account::getName);
		comboBox.setPlaceholder("Выберите аккаунт");
		comboBox.setWidthFull();

		// Устанавливаем текущее значение
		if (item.getCurrentDto().targetAccountName() != null) {
			accounts.stream()
					.filter(acc -> acc.getName().equals(item.getCurrentDto().targetAccountName()))
					.findFirst()
					.ifPresent(comboBox::setValue);
		}

		// Редактируемо только для новых записей
		comboBox.setReadOnly(!item.isNew());

		// Визуально показываем, что поле только для чтения
		if (!item.isNew()) {
			comboBox.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
			comboBox.getStyle().set("pointer-events", "none");
		}

		// Сохраняем изменения
		comboBox.addValueChangeListener(e -> {
			if (!comboBox.isReadOnly() && e.getValue() != null) {
				Account selected = e.getValue();
				item.setAccountName(selected.getName());
				grid.getDataProvider().refreshItem(item);
				updateButtonStates();
			}
		});

		return comboBox;
	}

	private ComboBox<Type> createTypeEditor(EditableItem item) {
		ComboBox<Type> combo = new ComboBox<>();
		combo.setItems(Type.values());
		combo.setItemLabelGenerator(Type::getDescription);
		combo.setValue(Type.valueOf(item.currentDto.type()));
		combo.setWidthFull();
		combo.addValueChangeListener(e -> {
			if (e.isFromClient() && e.getValue() != null) {
				Type newType = e.getValue();
				if (!newType.name().equals(item.currentDto.type())) {
					item.setCurrentType(newType.name());
					grid.getDataProvider().refreshItem(item);
					updateButtonStates();

					// Автоматически закрываем редактор после выбора
					UI.getCurrent().access(() -> {
						grid.getEditor().cancel();
					});
				}
			}
		});
		// Закрытие по клику вне или Escape
		combo.addBlurListener(e -> {
			grid.getEditor().cancel();
		});
		return combo;
	}

	private DatePicker createNextDayEditor(EditableItem item) {
		DatePicker datePicker = new DatePicker();
		datePicker.setLocale(new Locale("ru"));
		datePicker.setPlaceholder("дд.мм.гггг");
		datePicker.setWidthFull();
		datePicker.setEnabled(editMode);
		// Минимальная дата - сегодня или завтра (в зависимости от логики)
		datePicker.setMin(LocalDate.now());
		if (editMode) {
			datePicker.addFocusListener(e -> {
				if (editMode && !datePicker.isOpened()) {
					datePicker.setOpened(true);
				}
			});
		}
		// Устанавливаем текущее значение
		datePicker.setValue(item.getCurrentDto().nextDay());

		// Обработка изменений
		datePicker.addValueChangeListener(e -> {
			if (e.isFromClient() && e.getValue() != null) {
				LocalDate newDate = e.getValue();
				LocalDate currentDate = item.getCurrentDto().nextDay();

				if (!newDate.equals(currentDate)) {
					item.setNextDay(newDate);
					grid.getDataProvider().refreshItem(item);
					updateButtonStates();

					// Автоматически закрываем редактор после выбора
					UI.getCurrent().access(() -> {
						grid.getEditor().cancel();
					});
				}
			}
		});

		// Закрытие редактора
		datePicker.addBlurListener(e -> {
			grid.getEditor().cancel();
		});

		return datePicker;
	}

	private DatePicker createEndDayEditor(EditableItem item) {
		DatePicker datePicker = new DatePicker();
		datePicker.setLocale(new Locale("ru"));
		datePicker.setPlaceholder("дд.мм.гггг");
		datePicker.setWidthFull();
		datePicker.setEnabled(editMode);
		// Минимальная дата - сегодня или завтра (в зависимости от логики)
		datePicker.setMin(LocalDate.now());
		if (editMode) {
			datePicker.addFocusListener(e -> {
				if (editMode && !datePicker.isOpened()) {
					datePicker.setOpened(true);
				}
			});
		}
		// Устанавливаем текущее значение
		datePicker.setValue(item.getCurrentDto().endDate());

		// Обработка изменений
		datePicker.addValueChangeListener(e -> {
			if (e.isFromClient() && e.getValue() != null) {
				LocalDate newDate = e.getValue();
				LocalDate currentDate = item.getCurrentDto().nextDay();

				if (!newDate.equals(currentDate)) {
					item.setEndDate(newDate);
					grid.getDataProvider().refreshItem(item);
					updateButtonStates();

					// Автоматически закрываем редактор после выбора
					UI.getCurrent().access(() -> {
						grid.getEditor().cancel();
					});
				}
			}
		});

		// Закрытие редактора
		datePicker.addBlurListener(e -> {
			grid.getEditor().cancel();
		});

		return datePicker;
	}

	private Component createActionButtons() {
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		calculateButton.addClickListener(e ->
				new ForecastView(service,
						items.stream()
								.map(EditableItem::toDto)
								.collect(Collectors.toList()),
						selectedChat.getChatId()).open());
		calculateButton.setEnabled(selectedChat != null);
		buttons.add(calculateButton);
		calculateIncomeButton.setEnabled(selectedChat != null);
		calculateIncomeButton.addClickListener(e ->
				new IncomeView(service, accountService,
						selectedChat.getChatId()).open());
		buttons.add(calculateIncomeButton);
		return buttons;
	}

	private void cancelChanges() {
		// Восстанавливаем оригинальные значения
		items = new ArrayList<>(originalItems);

		// Обновляем таблицу
		grid.setItems(items);

		// Сбрасываем кнопки
		updateButtonStates();

		// Отменяем активное редактирование
		grid.getEditor().cancel();

		Notification.show("Изменения отменены",
				3000, Notification.Position.BOTTOM_END);
	}

	private void updateButtonStates() {
		boolean hasChanges = items.stream().anyMatch(e -> e.isModified() || e.isNew() || e.isMarkedForDeletion());
		cancelButton.setEnabled(hasChanges);
		saveButton.setEnabled(hasChanges);
		editButton.setEnabled(selectedChat != null && isNotEmpty(originalItems));
		calculateButton.setEnabled(selectedChat != null && isNotEmpty(originalItems));
		calculateIncomeButton.setEnabled(selectedChat != null && isNotEmpty(originalItems));
	}

	private void createChatButtonsPanel() {
		panel = new VerticalLayout();
		panel.setPadding(false);
		panel.setSpacing(false);

		H3 panelTitle = new H3("💬 Чаты");
		panelTitle.getStyle()
				.set("margin-bottom", "0.5em")
				.set("margin-top", "1em")
				.set("font-size", "16px");
		panel.add(panelTitle);

		// Используем HorizontalLayout с переносом строк
		HorizontalLayout chatsLayout = new HorizontalLayout();
		chatsLayout.setWidthFull();
		chatsLayout.setSpacing(true);
		chatsLayout.setPadding(false);

		// Автоматическая ширина элементов
		chatsLayout.getStyle()
				.set("gap", "8px") // Расстояние между элементами
				.set("align-items", "center");

		List<Chat> chats = chatService.getAllChats();

		for (Chat chat : chats) {
			Button chatButton = createUltraCompactButton(chat);
			chatsLayout.add(chatButton);
		}
		panel.add(chatsLayout);
		add(panel);
	}

	private Button createUltraCompactButton(Chat chat) {
		Button button = new Button(chat.getName());
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		button.addClickListener(e -> selectChat(chat));
		ComponentUtil.setData(button, "chat", chat);

		return button;
	}

	private void selectChat(Chat chat) {
		// Обновляем выбранный чат
		selectedChat = chat;

		// Перерисовываем все кнопки для обновления стилей
		updateChatButtonsStyle();

		// Загружаем медиа для выбранного чата
		loadDataForChat(chat.getChatId());
		updateButtonStates();
	}

	private void updateChatButtonsStyle() {
		// Обновляем стили всех кнопок
		for (Component component : panel.getChildren().toArray(Component[]::new)) {
			if (component instanceof HorizontalLayout chatsLayout) {
				for (Component innerComponent : chatsLayout.getChildren().toArray(Component[]::new)) {
					if (innerComponent instanceof Button button) {
						Chat chat = getChatFromButton(button);
						if (chat != null) {
							button.removeThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
							if (chat.equals(selectedChat)) {
								button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
							} else {
								button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
							}
						}
					}
				}
			}
		}
	}

	private Chat getChatFromButton(Button button) {
		return (Chat) ComponentUtil.getData(button, "chat");
	}

	private void loadDataForChat(Long chatId) {
		if (chatId != null) {
			List<PeriodicChangeRuleDto> dtos = periodicChangeRuleService.findAllPeriodicChangeRuleDtos(chatId);
			// Преобразуем в редактируемые объекты
			items = dtos.stream()
					.map(EditableItem::new)
					.collect(Collectors.toList());
			// Сохраняем оригинальную копию

			originalItems = items.stream()
					.map(EditableItem::new) // глубокое копирование
					.collect(Collectors.toList());

			ListDataProvider<EditableItem> dataProvider = new ListDataProvider<>(items);
			grid.setItems(dataProvider);
			// Добавляем слушатель изменений данных
			dataProvider.addDataProviderListener(event -> {
				updateButtonStates();
			});
		}
	}

	// Внутренний класс для редактируемых элементов
	@Getter
	@Setter
	private static class EditableItem {
		private final PeriodicChangeRuleDto originalDto;
		private PeriodicChangeRuleDto currentDto;
		private boolean modified = false;
		private boolean isNew = false;
		private boolean markedForDeletion = false;

		public EditableItem(PeriodicChangeRuleDto dto) {
			this.originalDto = dto;
			this.currentDto = dto;
		}

		public EditableItem(EditableItem other) {
			this.originalDto = other.originalDto;
			this.currentDto = other.currentDto;
			this.modified = other.modified;
			this.isNew = other.isNew;
			this.markedForDeletion = other.markedForDeletion;
		}

		public PeriodicChangeRuleDto toDto() {
			return currentDto;
		}

		public void setCurrentSum(int sum) {
			if (sum != currentDto.sum()) {
				this.currentDto = currentDto.withSum(sum);
				this.modified = (sum != originalDto.sum());
			}
		}

		public void setCurrentType(String type) {
			if (!type.equals(currentDto.type())) {
				this.currentDto = currentDto.withType(type);
				this.modified = !type.equals(originalDto.type());
			}
		}

		public void setNextDay(LocalDate newNextDay) {
			if (!newNextDay.equals(currentDto.nextDay())) {
				this.currentDto = currentDto.withNextDay(newNextDay);
				this.modified = !newNextDay.equals(originalDto.nextDay());
			}
		}

		public void setEndDate(LocalDate newEndDate) {
			if (!newEndDate.equals(currentDto.endDate())) {
				this.currentDto = currentDto.withEndDate(newEndDate);
				this.modified = !newEndDate.equals(originalDto.endDate());
			}
		}

		public void setCurrentName(String newName) {
			if (!newName.equals(currentDto.name())) {
				this.currentDto = currentDto.withName(newName);
				this.modified = !newName.equals(originalDto.name());
			}
		}

		public void setCurrentPeriodicChangeName(String periodicChangeName) {
			this.currentDto = currentDto.withPeriodicChangeName(periodicChangeName);
			this.modified = !periodicChangeName.equals(originalDto.targetAccountName());
		}

		public void setAccountName(String accountName) {
			this.currentDto = currentDto.withTargetAccountName(accountName);
			this.modified = !accountName.equals(originalDto.targetAccountName());
		}

		public void setIsActive(boolean isActive) {
			this.currentDto = currentDto.withActive(isActive);
			this.modified = isActive != originalDto.active();
		}
	}
}
