package ru.mishapp.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import ru.mishapp.dto.PeriodicChangeRuleDTO;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.Chat;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ChatService;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.PeriodicChangeService;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PeriodicChangeDialog extends Dialog {

	private final PeriodicChangeRuleService service;
	private final Runnable saveCallback;
	private final PeriodicChangeService periodicChangeService;
	private final AccountService accountService;
	private final ChatService chatService;
	private PeriodicChangeRule periodicChangeRule;

	// Поля формы
	private final ComboBox<String> periodicChangeIdField = new ComboBox<>("Тип платежа");
	private final ComboBox<String> targetAccountIdField = new ComboBox<>("Счет");
	private final TextField nameField = new TextField("Name");
	private final IntegerField sumField = new IntegerField("Amount");
	private final ComboBox<Type> typeComboBox = new ComboBox<>("Type");
	private final IntegerField passField = new IntegerField("Pass");
	private final DatePicker nextDayPicker = new DatePicker("Next Day");
	private final Checkbox activeCheckbox = new Checkbox("Active");
	private final DatePicker endDatePicker = new DatePicker("End Date");
	private final ComboBox<String> chatCheckBox = new ComboBox<>("Чат");

	// Кнопки
	private final Button saveButton = new Button("Save");
	private final Button cancelButton = new Button("Cancel");

	public PeriodicChangeDialog(PeriodicChangeRuleService service,
	                            Runnable saveCallback,
	                            PeriodicChangeService periodicChangeService,
	                            AccountService accountService,
	                            ChatService chatService) {
		this.service = service;
		this.saveCallback = saveCallback;
		this.periodicChangeService = periodicChangeService;
		this.accountService = accountService;
		this.chatService = chatService;

		configureFields();
		configureButtons();
		configureLayout();
		bindData();
	}

	private void configureFields() {
		// Настройка валидации и внешнего вида полей
		List<String> periodicChangeList = periodicChangeService.readAll().stream()
				.map(PeriodicChange::getName)
				.collect(toList());
		periodicChangeIdField.setRequired(true);
		periodicChangeIdField.setItems(periodicChangeList);
		periodicChangeIdField.setErrorMessage("Periodic Change ID is required");
		periodicChangeIdField.setWidthFull();

		List<String> accountList = accountService.readAllAccounts().stream()
				.map(Account::getName)
				.toList();

		targetAccountIdField.setItems(accountList);
		targetAccountIdField.setRequired(true);
		targetAccountIdField.setErrorMessage("Target Account ID is required");
		targetAccountIdField.setWidthFull();

		nameField.setRequired(true);
		nameField.setErrorMessage("Name is required");
		nameField.setWidthFull();

		sumField.setRequired(true);
		sumField.setMin(1);
		sumField.setErrorMessage("Amount must be greater than 0");
		sumField.setWidthFull();

		typeComboBox.setRequired(true);
		typeComboBox.setItems(Type.values());
		typeComboBox.setErrorMessage("Type is required");
		typeComboBox.setWidthFull();

		passField.setRequired(true);
		passField.setMin(0);
		passField.setErrorMessage("Pass must be at least 0");
		passField.setWidthFull();

		nextDayPicker.setRequired(true);
		nextDayPicker.setErrorMessage("Next day is required");
		nextDayPicker.setMin(LocalDate.now());
		nextDayPicker.setWidthFull();

		activeCheckbox.setValue(true);
		activeCheckbox.setWidthFull();

		endDatePicker.setMin(LocalDate.now());
		endDatePicker.setWidthFull();

		// Обработка зависимостей полей
		activeCheckbox.addValueChangeListener(e -> {
			if (!e.getValue()) {
				endDatePicker.setRequired(true);
				endDatePicker.setErrorMessage("End date is required when inactive");
			} else {
				endDatePicker.setRequired(false);
				endDatePicker.clear();
			}
		});

		List<String> chats = chatService.getAllChats().stream().map(Chat::getName).toList();
		chatCheckBox.setItems(chats);
		chatCheckBox.setRequired(true);
		chatCheckBox.setWidthFull();
		chatCheckBox.setErrorMessage("Chat is required");
	}

	private void configureButtons() {
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> save());

		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		cancelButton.addClickListener(e -> close());

		// Разрешаем сохранение по Enter
		addDialogCloseActionListener(e -> cancelButton.click());
	}

	private void configureLayout() {
		// Создание формы
		FormLayout formLayout = new FormLayout();
		formLayout.add(
				periodicChangeIdField, targetAccountIdField,
				nameField, sumField,
				typeComboBox, passField,
				nextDayPicker, activeCheckbox,
				endDatePicker, chatCheckBox
		);
		formLayout.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1),
				new FormLayout.ResponsiveStep("500px", 2)
		);
		formLayout.setWidth("600px");

		// Создание футера с кнопками
		HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setWidthFull();

		// Добавление в диалог
		add(formLayout, buttonLayout);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(true);
	}

	private void bindData() {
		if (periodicChangeRule != null) {
			// Режим редактирования
//			periodicChangeIdField.setValue(periodicChange.getPeriodicChangeId());
//			targetAccountIdField.setValue(periodicChange.getTargetAccountId());
			nameField.setValue(periodicChangeRule.getName());
			sumField.setValue(periodicChangeRule.getSum());
			typeComboBox.setValue(periodicChangeRule.getType());
			passField.setValue(periodicChangeRule.getPass());
			nextDayPicker.setValue(periodicChangeRule.getNextDay());
			activeCheckbox.setValue(periodicChangeRule.isActive());
			endDatePicker.setValue(periodicChangeRule.getEndDate());

			setHeaderTitle("Редактирование платежа");
		} else {
			// Режим создания
			nextDayPicker.setValue(LocalDate.now());
			setHeaderTitle("Добавление нового платежа");
		}
	}

	private void save() {
		// Валидация формы
		if (!isValid()) {
			return;
		}

		try {
			PeriodicChangeRuleDTO dto = new PeriodicChangeRuleDTO(
					nameField.getValue(), periodicChangeIdField.getValue(), targetAccountIdField.getValue(),
					sumField.getValue(), typeComboBox.getValue().getDescription(), passField.getValue(), nextDayPicker.getValue(),
					true, endDatePicker.getValue()

			);
			Chat chat = chatService.getChatByName(chatCheckBox.getValue());
			periodicChangeRule = service.create(dto, chat.getChatId());

			Notification.show(
					periodicChangeRule.getId() == null ? "Created successfully" : "Updated successfully",
					3000,
					Notification.Position.MIDDLE
			);

			saveCallback.run();
			close();

		} catch (Exception e) {
			Notification.show("Error: " + e.getMessage(), 5000,
					Notification.Position.MIDDLE);
		}
	}

	private boolean isValid() {
		boolean valid = true;

		if (periodicChangeIdField.isEmpty()) {
			periodicChangeIdField.setInvalid(true);
			valid = false;
		}

		if (targetAccountIdField.isEmpty()) {
			targetAccountIdField.setInvalid(true);
			valid = false;
		}

		if (nameField.isEmpty()) {
			nameField.setInvalid(true);
			valid = false;
		}

		if (sumField.isEmpty() || sumField.getValue() < 1) {
			sumField.setInvalid(true);
			valid = false;
		}

		if (typeComboBox.isEmpty()) {
			typeComboBox.setInvalid(true);
			valid = false;
		}

		if (passField.isEmpty() || passField.getValue() < 1) {
			passField.setInvalid(true);
			valid = false;
		}

		if (nextDayPicker.isEmpty()) {
			nextDayPicker.setInvalid(true);
			valid = false;
		}

		if (!activeCheckbox.getValue() && endDatePicker.isEmpty()) {
			endDatePicker.setInvalid(true);
			valid = false;
		}

		return valid;
	}
}
