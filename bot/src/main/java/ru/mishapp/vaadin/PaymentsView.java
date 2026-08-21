package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
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
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.enumiration.Type;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.RuleExecuteService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static ru.mishapp.Constants.RUB;

import jakarta.annotation.security.PermitAll;

@PermitAll
@RequiredArgsConstructor
@Route(value = "payments", layout = MainLayout.class)
public class PaymentsView extends VerticalLayout {

    private final PeriodicChangeRuleService periodicChangeRuleService;
    private final ForecastService service;
    private final AccountService accountService;
    private final RuleExecuteService executeService;

    private final Binder<EditableItem> binder = new Binder<>(EditableItem.class);
    private final List<PeriodicChangeRuleDto> itemsToDelete = new ArrayList<>();
    private final Button calculateButton = new Button("Выполнить прогноз платежей");
    private final Button editButton = new Button("Режим редактирования");
    private final Button cancelButton = new Button("Отменить изменения");
    private final Button addButton = new Button("Добавить", new Icon(VaadinIcon.PLUS));
    private final Button saveButton = new Button("Сохранить");
    private final Button calculateIncomeButton = new Button("Расчет дохода");

    // Текущий выбранный чат
    private Account selectedAccount;
    private Grid<EditableItem> grid;
    private VerticalLayout panel;
    private List<EditableItem> items = new ArrayList<>();
    private List<EditableItem> originalItems = new ArrayList<>();
    private boolean editMode = false;
    private Span balanceSpan;

    @PostConstruct
    public void configure() {
        this.grid = new Grid<>(EditableItem.class);
        configureHeader();
        createAccountButtonsPanel();
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

        getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '" +
                        "vaadin-grid::part(payment-due-row) { " +
                        "  background-color: var(--lumo-success-color-50pct) !important; " +
                        "  opacity: 0.8; " +
                        "}" +
                        "';" +
                        "document.head.appendChild(style);"
        );
    }

    public void updateBalance() {
        if (selectedAccount != null) {
            AccountHistory lastTarget = accountService.findLastByAccountId(selectedAccount.getId());
            if (lastTarget != null) {
                balanceSpan.setText("Текущий баланс: " + RUB.format(lastTarget.getBalance()) + " ₽");
            } else {
                balanceSpan.setText("Текущий баланс: 0 ₽");
            }
        }
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

        // Контейнер для левой части (с балансом)
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(Alignment.CENTER);
        leftSection.setSpacing(true);
        leftSection.getStyle().set("margin-right", "auto");

        // Текст
        Span balanceIcon = new Span("💰 ");

        balanceSpan = new Span("Здесь будет баланс");
        balanceSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        leftSection.add(balanceIcon, balanceSpan);

        // Кнопка переключения режима редактирования
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(_ -> toggleEditMode());
        editButton.setEnabled(false);

        cancelButton.addClickListener(_ -> cancelChanges());
        cancelButton.setEnabled(false);

        // Кнопка добавления
        addButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addButton.setEnabled(false); // По умолчанию выключена
        addButton.addClickListener(_ -> addNewRow());

        saveButton.addClickListener(_ -> save());
        saveButton.setEnabled(false);

        toolbar.add(leftSection, editButton, cancelButton, addButton, saveButton);
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
        PeriodicChangeRuleDto emptyDto = new PeriodicChangeRuleDto(null,  "", null,
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

        periodicChangeRuleService.saveAll(itemsToSave);
        periodicChangeRuleService.deleteAll(itemsToDelete);

        // После сохранения обновляем оригинальные значения
        loadDataForAccount(selectedAccount.getId());

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
                        grid.getEditor().editItem(item);
                    });

                    return nameField;
                });

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
                .setComparator((item1, item2) -> {
                    // Сортируем по числовому значению, игнорируя иконку ✎
                    int sum1 = item1.currentDto.sum();
                    int sum2 = item2.currentDto.sum();
                    return Integer.compare(sum1, sum2);
                })
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

                    sumField.addValueChangeListener(e -> {
                        Integer newValue = sumField.getValue();
                        if (newValue != null) {
                            item.setCurrentSum(newValue);
                            grid.getDataProvider().refreshItem(item);
                            updateButtonStates();
                            grid.getEditor().editItem(item);
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
                .setSortable(true)
                .setComparator((item1, item2) -> {
                    // Сортируем по enum значению, а не по описанию
                    Type type1 = Type.valueOf(item1.currentDto.type());
                    Type type2 = Type.valueOf(item2.currentDto.type());
                    return type1.compareTo(type2); // Сортировка по enum (порядок объявления)
                    // Или по описанию: return type1.getDescription().compareTo(type2.getDescription());
                })
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


//		// Включаем редактирование по двойному клику
        grid.addItemClickListener(event -> {
            if (editMode && event.getColumn() != null) {
                grid.getEditor().editItem(event.getItem());
            }
        });

        grid.addComponentColumn(item -> {
                    HorizontalLayout actionsLayout = new HorizontalLayout();
                    actionsLayout.setSpacing(true);
                    actionsLayout.setPadding(false);
                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_SMALL);

                    if (!editMode || item.isMarkedForDeletion()) {
                        deleteButton.setEnabled(false);
                    }

                    deleteButton.addClickListener(_ -> {
                        ConfirmDialog dialog = new ConfirmDialog();
                        dialog.setHeader("Подтверждение удаления");
                        dialog.setText(String.format("Вы уверены, что хотите удалить \"%s\"?",
                                item.getCurrentDto().name()));

                        dialog.setCancelable(true);
                        dialog.setCancelText("Отмена");

                        dialog.setConfirmText("Удалить");
                        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());

                        dialog.addConfirmListener(_ -> deleteEntity(item));

                        dialog.open();
                    });

                    // Кнопка "Выполнить"
                    Button executeButton = new Button(new Icon(VaadinIcon.PLAY));
                    executeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                    executeButton.setTooltipText("Выполнить");

                    if (editMode || item.isMarkedForDeletion() || item.isNew()) {
                        executeButton.setEnabled(false);
                    }

                    executeButton.addClickListener(_ -> {
                        executePayment(item); // Ваш метод выполнения платежа
                    });

                    actionsLayout.add(deleteButton, executeButton);
                    return actionsLayout;
                })
                .setHeader("Действия")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setPartNameGenerator(item -> {
            if (item.isMarkedForDeletion()) {
                return "marked-for-deletion";
            }

            LocalDate nextDay = item.getCurrentDto().nextDay();
            LocalDate today = LocalDate.now();
            if (nextDay != null && !nextDay.isAfter(today)) {
                return "payment-due-row";
            }
            return null;
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
            grid.getEditor().editItem(item);
        });
        // Опционально: делаем чекбокс доступным только в режиме редактирования
        activeCheckbox.setEnabled(editMode); // если у вас есть такой флаг

        return activeCheckbox;
    }

    private ComboBox<Type> createTypeEditor(EditableItem item) {
        ComboBox<Type> comboBox = new ComboBox<>();
        comboBox.setItems(Type.values());
        comboBox.setItemLabelGenerator(Type::getDescription);
        comboBox.setValue(Type.valueOf(item.currentDto.type()));
        comboBox.setWidthFull();

        if (editMode) {
            comboBox.addFocusListener(e -> {
                if (editMode && !comboBox.isOpened()) {
                    comboBox.setOpened(true);
                }
            });
        }

        comboBox.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                Type newType = e.getValue();
                if (!newType.name().equals(item.currentDto.type())) {
                    item.setCurrentType(newType.name());
                    grid.getDataProvider().refreshItem(item);
                    updateButtonStates();
                    grid.getEditor().editItem(item);
                }
            }
        });

        return comboBox;
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
                    grid.getEditor().editItem(item);
                }
            }
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
                    grid.getEditor().editItem(item);
                }
            }
        });

        return datePicker;
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

    private void executePayment(EditableItem item) {
        executeService.ruleExecute(item.getOriginalDto());
        // После сохранения обновляем оригинальные значения
        loadDataForAccount(selectedAccount.getId());
        updateBalance();
    }

    private Component createActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        List<PeriodicChangeRuleDto> rules = items.stream()
                .map(EditableItem::toDto)
                .toList();
        calculateButton.addClickListener(
                _ -> new ForecastView(service, rules).open());
        calculateButton.setEnabled(selectedAccount != null);
        buttons.add(calculateButton);
        calculateIncomeButton.setEnabled(selectedAccount != null);
        calculateIncomeButton.addClickListener(
                _ -> new IncomeView(service, selectedAccount.getId()).open());
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
        editButton.setEnabled(selectedAccount != null && isNotEmpty(originalItems));
        calculateButton.setEnabled(selectedAccount != null && isNotEmpty(originalItems));
        calculateIncomeButton.setEnabled(selectedAccount != null && isNotEmpty(originalItems));
    }

    private void createAccountButtonsPanel() {
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
        HorizontalLayout accountLayout = new HorizontalLayout();
        accountLayout.setWidthFull();
        accountLayout.setSpacing(true);
        accountLayout.setPadding(false);

        // Автоматическая ширина элементов
        accountLayout.getStyle()
                .set("gap", "8px") // Расстояние между элементами
                .set("align-items", "center");

        for (Account account : accountService.readAllAccounts()) {
            accountLayout.add(createUltraCompactButton(account));
        }
        panel.add(accountLayout);
        add(panel);
    }

    private Button createUltraCompactButton(Account account) {
        Button button = new Button(account.getName());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        button.addClickListener(_ -> selectAccount(account));
        ComponentUtil.setData(button, "account", account);

        return button;
    }

    private void selectAccount(Account account) {
        // Обновляем выбранный чат
        selectedAccount = account;

        // Перерисовываем все кнопки для обновления стилей
        updateAccountButtonsStyle();

        // Загружаем медиа для выбранного чата
        loadDataForAccount(account.getId());
        updateButtonStates();
        updateBalance();
    }

    private void updateAccountButtonsStyle() {
        // Обновляем стили всех кнопок
        for (Component component : panel.getChildren().toArray(Component[]::new)) {
            if (component instanceof HorizontalLayout accountsLayout) {
                for (Component innerComponent : accountsLayout.getChildren().toArray(Component[]::new)) {
                    if (innerComponent instanceof Button button) {
                        Account account = getAccountFromButton(button);
                        if (account != null) {
                            button.removeThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
                            if (account.equals(selectedAccount)) {
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

    private Account getAccountFromButton(Button button) {
        return (Account) ComponentUtil.getData(button, "account");
    }

    private void loadDataForAccount(Long accountId) {
        if (accountId != null) {
            List<PeriodicChangeRuleDto> dtos = periodicChangeRuleService.findAllPeriodicChangeRuleDtos(accountId);
            // Преобразуем в редактируемые объекты
            items = dtos.stream()
                    .map(EditableItem::new)
                    .sorted(Comparator.comparing(item -> item.getCurrentDto().nextDay()))
                    .collect(Collectors.toList());

            // Сохраняем оригинальную копию
            originalItems = items.stream()
                    .map(EditableItem::new) // глубокое копирование
                    .collect(Collectors.toList());

            ListDataProvider<EditableItem> dataProvider = new ListDataProvider<>(items);
            grid.setItems(dataProvider);
            // Добавляем слушатель изменений данных
            dataProvider.addDataProviderListener(_ -> updateButtonStates());
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

        public void setIsActive(boolean isActive) {
            this.currentDto = currentDto.withActive(isActive);
            this.modified = isActive != originalDto.active();
        }
    }
}
