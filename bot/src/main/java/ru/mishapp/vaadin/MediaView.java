package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import ru.mishapp.dto.MediaDto;
import ru.mishapp.entity.Account;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.MediaService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route(value = "media", layout = MainLayout.class)
public class MediaView extends VerticalLayout {

    private final AccountService accountService;
    private final MediaService mediaService;

    private final Grid<MediaDto> mediaGrid = new Grid<>(MediaDto.class);
    private final Button addButton = new Button("Добавить", new Icon(VaadinIcon.PLUS));
    // Переключатели фильтров
    private final Button allButton = new Button("Все");
    private final Button moviesButton = new Button("Фильмы", new Icon(VaadinIcon.FILM));
    private final Button seriesButton = new Button("Сериалы", new Icon(VaadinIcon.PLAY_CIRCLE));
    private VerticalLayout panel;
    private List<MediaDto> originalItems = new ArrayList<>();
    private Binder<MediaDto> binder;
    private MediaDto editingItem = null;
    // Текущий выбранный чат
    private Account selectedAccount;

    public MediaView(AccountService accountService, MediaService mediaService) {
        this.accountService = accountService;
        this.mediaService = mediaService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createToolbar();
        createAccountButtonsPanel();
        createMediaGrid();
        createFooter();
    }

    private void createHeader() {
        H3 header = new H3("🎬 Управление фильмами и сериалами");
        header.getStyle()
                .set("margin", "0");

        add(header);
    }

    private void createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setAlignItems(Alignment.END);
        add(toolbar);
    }

    private void createAccountButtonsPanel() {
        panel = new VerticalLayout();
        panel.setPadding(false);
        panel.setSpacing(false);

        H3 panelTitle = new H3("💬 Счета");
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

        Iterable<Account> accounts = accountService.readAllAccounts();

        for (Account account : accounts) {
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
        loadMediaForAccount(account.getId());

        updateAddButtonState();
    }

    private void updateAccountButtonsStyle() {
        // Обновляем стили всех кнопок
        for (Component component : panel.getChildren().toArray(Component[]::new)) {
            if (component instanceof HorizontalLayout accountLayout) {
                for (Component innerComponent : accountLayout.getChildren().toArray(Component[]::new)) {
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

    private void updateActiveFilter(Button active, Button... others) {
        active.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
        active.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        for (Button other : others) {
            other.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            other.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
    }

    private void loadMediaForAccount(Long accountId) {
        //TODO сделать
    }

    private void createMediaGrid() {
        // Панель с таблицей
        VerticalLayout gridPanel = new VerticalLayout();
        gridPanel.setPadding(false);
        gridPanel.setSpacing(false);
        gridPanel.setWidthFull();

        // Панель фильтров
        HorizontalLayout filterPanel = createFilterPanel();
        // Настройка Grid
        configureMediaGrid();

        // Компоновка
        gridPanel.add(filterPanel, mediaGrid);
        gridPanel.setFlexGrow(1, mediaGrid);

        add(gridPanel);
        setFlexGrow(1, gridPanel);
    }

    private HorizontalLayout createFilterPanel() {
        HorizontalLayout filterPanel = new HorizontalLayout();
        filterPanel.setWidthFull();
        filterPanel.setPadding(false);
        filterPanel.setSpacing(true);
        filterPanel.setAlignItems(Alignment.CENTER);

        allButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        moviesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        seriesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Выделение активного фильтра
        allButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Обработчики
        allButton.addClickListener(e -> {
            if (selectedAccount != null) {
                loadMediaForAccount(selectedAccount.getId());
                updateActiveFilter(allButton, moviesButton, seriesButton);
            }
        });

        moviesButton.addClickListener(e -> {
            if (selectedAccount != null) {
                List<MediaDto> movies = mediaService.getMoviesByAccountId(selectedAccount.getId());
                mediaGrid.setItems(movies);
                updateActiveFilter(moviesButton, allButton, seriesButton);
            }
        });

        seriesButton.addClickListener(e -> {
            if (selectedAccount != null) {
                List<MediaDto> series = mediaService.getSeriesAccountId(selectedAccount.getId());
                mediaGrid.setItems(series);
                updateActiveFilter(seriesButton, allButton, moviesButton);
            }
        });

        allButton.setEnabled(selectedAccount != null);
        moviesButton.setEnabled(selectedAccount != null);
        seriesButton.setEnabled(selectedAccount != null);

        filterPanel.add(allButton, moviesButton, seriesButton, addButton);

        return filterPanel;
    }

    private void configureMediaGrid() {
        // Настройка редактора с BUFFERED режимом
        binder = new Binder<>(MediaDto.class);
        mediaGrid.getEditor().setBinder(binder);
        mediaGrid.getEditor().setBuffered(true); // Важно: true для отмены изменений

        // Убираем стандартные колонки
        mediaGrid.removeAllColumns();

        // Колонка с названием
        mediaGrid.addColumn(MediaDto::getName)
                .setHeader("Название")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("name")
                .setEditorComponent(item -> {
                    TextField nameField = new TextField();
                    nameField.setValue(item.getName() != null ? item.getName() : "");
                    nameField.setWidthFull();
                    nameField.setPlaceholder("Введите название");
                    nameField.focus(); // Автофокус для новой записи

                    binder.removeBinding(nameField);
                    binder.forField(nameField)
                            .asRequired("Название обязательно")
                            .bind(MediaDto::getName, MediaDto::setName);

                    return nameField;
                });

        // Колонка с типом
        mediaGrid.addComponentColumn(item -> {
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setAlignItems(Alignment.CENTER);
                    layout.setSpacing(true);

                    Span badge = new Span(item.getType());
                    layout.add(badge);
                    return layout;
                })
                .setHeader("Тип")
                .setWidth("120px")
                .setKey("type")
                .setEditorComponent(item -> {
                    ComboBox<String> typeCombo = new ComboBox<>();
                    typeCombo.setItems(Arrays.stream(MediaType.values()).map(MediaType::getName).toList());
                    typeCombo.setValue(item.getType());
                    typeCombo.setWidthFull();
                    typeCombo.setPlaceholder("Выберите тип");

                    binder.removeBinding(typeCombo);
                    binder.forField(typeCombo)
                            .asRequired("Выберите тип")
                            .bind(MediaDto::getType, MediaDto::setType);

                    return typeCombo;
                });
        ;

        Grid.Column<MediaDto> statusColumn = mediaGrid.addComponentColumn(item -> {
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setAlignItems(Alignment.CENTER);
                    layout.setSpacing(true);

                    MediaStatus status = item.getStatus();
                    Span badge = new Span(status != null ? status.getName() : MediaStatus.PLAN_TO_WATCH.getName());
                    layout.add(badge);
                    return layout;
                })
                .setHeader("Статус")
                .setWidth("120px")
                .setKey("status")
                .setEditorComponent(item -> {
                    Select<MediaStatus> statusSelect = new Select<>();

                    List<MediaStatus> statusValues = item.getStatus() != null
                            ? item.getStatus().getAvailableTransitions()
                            : Arrays.stream(MediaStatus.values()).toList();

                    statusSelect.setItems(statusValues);
                    statusSelect.setValue(item.getStatus());

                    // Для отображения понятных имен
                    statusSelect.setItemLabelGenerator(MediaStatus::getName);

                    statusSelect.setWidthFull();

                    binder.removeBinding(statusSelect);
                    binder.forField(statusSelect)
                            .bind(MediaDto::getStatus, MediaDto::setStatus);

                    return statusSelect;
                });

        // Колонка с рейтингом (визуализация)
        mediaGrid.addComponentColumn(item -> {
                    Integer rating = item.getRating();
                    if (rating == null) {
                        Span noRating = new Span("—");
                        noRating.getStyle()
                                .set("color", "var(--lumo-disabled-text-color)")
                                .set("font-style", "italic");
                        return noRating;
                    }
                    return new Span(rating + "/10");
                })
                .setHeader("Рейтинг")
                .setWidth("150px")
                .setKey("rating")
                .setEditorComponent(item -> {
                    ComboBox<Integer> ratingCombo = new ComboBox<>();
                    List<Integer> ratings = IntStream.rangeClosed(1, 10)
                            .boxed()
                            .collect(Collectors.toList());
                    ratingCombo.setItems(ratings);
                    ratingCombo.setValue(item.getRating());
                    ratingCombo.setPlaceholder("Выберите рейтинг");
                    ratingCombo.setWidthFull();

                    binder.removeBinding(ratingCombo);
                    binder.forField(ratingCombo)
                            .bind(MediaDto::getRating, MediaDto::setRating);

                    return ratingCombo;
                });

        mediaGrid.addComponentColumn(item ->
                {
                    if (item.getDescription() == null) {
                        return new Span("—");
                    }
                    return new Span(item.getDescription());
                })
                .setHeader("Комментарий")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("description")
                .setEditorComponent(
                        item ->
                        {
                            TextField descriptionField = new TextField();
                            descriptionField.setValue(item.getDescription() != null ? item.getDescription() : "");
                            descriptionField.setWidthFull();
                            descriptionField.setPlaceholder("Введите комментарий...");

                            binder.removeBinding(descriptionField);
                            binder.forField(descriptionField)
                                    .bind(MediaDto::getDescription, MediaDto::setDescription);

                            return descriptionField;
                        }
                );

        // Колонка действий
        mediaGrid.addComponentColumn(item -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.setTooltipText("Редактировать");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.setTooltipText("Удалить");

            Button saveButton = new Button(new Icon(VaadinIcon.CHECK));
            saveButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SUCCESS);
            saveButton.setTooltipText("Сохранить");

            Button cancelButton = new Button(new Icon(VaadinIcon.CLOSE));
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            cancelButton.setTooltipText("Отмена");

            // Устанавливаем начальное состояние кнопок
            boolean isEditing = editingItem != null && editingItem.equals(item);
            updateButtonStates(editButton, deleteButton, saveButton, cancelButton, isEditing);

            // Сохраняем оригинальное состояние для отмены
            editButton.addClickListener(e -> {

                if (editingItem != null) {
                    // Если что-то уже редактируется, показываем предупреждение
                    Notification.show("Сначала завершите текущее редактирование",
                            3000, Notification.Position.BOTTOM_END);
                    return;
                }

                editingItem = item;
                mediaGrid.getEditor().editItem(item);
            });

            saveButton.addClickListener(e -> {
                // Сохраняем изменения
                mediaGrid.getEditor().save(); // Важно: вызываем save() у editor
                saveMediaItem(item);
                mediaGrid.getEditor().cancel();
                editingItem = null;
                originalItems = new ArrayList<>();
                loadMediaForAccount(selectedAccount.getId());
                updateAddButtonState();
                Notification.show("Сохранено", 2000, Notification.Position.BOTTOM_END);
            });

            cancelButton.addClickListener(e -> {
                if (item.getId() == null) {
                    mediaGrid.setItems(originalItems);
                    originalItems = new ArrayList<>();
                } else {
                    // Отменяем изменения - возвращаем оригинальные значения
                    mediaGrid.getEditor().cancel(); // Важно: вызываем cancel() у editor
                    editingItem = null;
                }
                mediaGrid.getDataProvider().refreshAll();
                updateAddButtonState();
                Notification.show("Изменения отменены", 2000, Notification.Position.BOTTOM_END);
            });

            deleteButton.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Подтверждение удаления");
                dialog.setText(String.format("Удалить \"%s\"?", item.getName()));
                dialog.setConfirmText("Удалить");
                dialog.setCancelText("Отмена");
                dialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());
                dialog.addConfirmListener(_ -> {
                    mediaService.deleteMediaItem(item.getId());
                    loadMediaForAccount(selectedAccount.getId());
                });
                dialog.open();
            });

            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton, saveButton, cancelButton);
            actions.setSpacing(true);
            actions.setPadding(false);
            return actions;
        }).setHeader("Действия").setWidth("130px");

        mediaGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_COLUMN_BORDERS);
    }

    private void updateButtonStates(Button edit, Button delete, Button save, Button cancel, boolean isEditing) {
        edit.setEnabled(!isEditing);
        delete.setEnabled(!isEditing);
        save.setEnabled(isEditing);
        cancel.setEnabled(isEditing);
    }

    private void saveMediaItem(MediaDto item) {
        //TODO сделать
    }

    private void createFooter() {
        HorizontalLayout buttons = new HorizontalLayout();
        addButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addButton.addClickListener(e -> addNewRow());
        addButton.setEnabled(selectedAccount != null);
        buttons.add(addButton);
        add(buttons);
    }

    private void addNewRow() {
        if (editingItem != null) {
            Notification.show("Сначала завершите редактирование текущей записи",
                    3000, Notification.Position.BOTTOM_END);
            return;
        }
        // Сбрасываем Binder ПЕРЕД созданием новой записи
        resetBinder();

        editingItem = new MediaDto(
                null, "", "", null, null, ""
        );
        // Добавляем в начало списка
        originalItems = new ArrayList<>(mediaGrid.getListDataView().getItems().toList());
        List<MediaDto> currentItems = new ArrayList<>(mediaGrid.getListDataView().getItems().toList());
        currentItems.addFirst(editingItem);
        mediaGrid.setItems(currentItems);
        // ВАЖНО: Устанавливаем объект в новый binder ДО editItem
        binder.setBean(editingItem);
        // Открываем редактор для новой записи
        mediaGrid.getEditor().editItem(editingItem);
        updateAddButtonState();
    }

    private void resetBinder() {
        new Binder<>(MediaDto.class);
        mediaGrid.getEditor().setBinder(binder);
        mediaGrid.getEditor().setBuffered(true);
        reconfigureBindings();
    }

    private void reconfigureBindings() {
        // Пересоздаем все привязки для колонок
        // Можно просто вызвать configureColumns() заново для колонок с редакторами
        mediaGrid.getColumns().forEach(column -> {
            if (column.getEditorComponent() != null) {
                // Принудительно обновляем editor component
                column.setEditorComponent(item -> createEditorComponentForColumn(column, item));
            }
        });
    }

    private Component createEditorComponentForColumn(Grid.Column<MediaDto> column, MediaDto item) {
        // Логика создания компонента в зависимости от колонки
        String columnKey = column.getKey();

        switch (columnKey) {
            case "name" -> {
                TextField nameField = new TextField();
                nameField.setValue(item.getName() != null ? item.getName() : "");
                nameField.setWidthFull();

                binder.forField(nameField)
                        .asRequired("Название обязательно")
                        .bind(MediaDto::getName, MediaDto::setName);

                return nameField;
            }
            case "type" -> {
                ComboBox<String> typeCombo = new ComboBox<>();
                typeCombo.setItems(Arrays.stream(MediaType.values()).map(MediaType::getName).toList());
                typeCombo.setValue(item.getType());
                typeCombo.setWidthFull();
                typeCombo.setPlaceholder("Выберите тип");

                binder.removeBinding(typeCombo);
                binder.forField(typeCombo)
                        .asRequired("Выберите тип")
                        .bind(MediaDto::getType, MediaDto::setType);

                return typeCombo;
            }
            case "status" -> {
                Select<MediaStatus> statusSelect = new Select<>();

                List<MediaStatus> statusValues = item.getStatus() != null
                        ? item.getStatus().getAvailableTransitions()
                        : Arrays.stream(MediaStatus.values()).toList();

                statusSelect.setItems(statusValues);
                statusSelect.setValue(item.getStatus());

                // Для отображения понятных имен
                statusSelect.setItemLabelGenerator(MediaStatus::getName);

                statusSelect.setWidthFull();

                binder.removeBinding(statusSelect);
                binder.forField(statusSelect)
                        .bind(MediaDto::getStatus, MediaDto::setStatus);

                return statusSelect;
            }
            case "rating" -> {
                ComboBox<Integer> ratingCombo = new ComboBox<>();
                List<Integer> ratings = IntStream.rangeClosed(1, 10)
                        .boxed()
                        .collect(Collectors.toList());
                ratingCombo.setItems(ratings);
                ratingCombo.setValue(item.getRating());
                ratingCombo.setPlaceholder("Выберите рейтинг");
                ratingCombo.setWidthFull();

                binder.removeBinding(ratingCombo);
                binder.forField(ratingCombo)
                        .bind(MediaDto::getRating, MediaDto::setRating);

                return ratingCombo;
            }
            case "description" -> {
                TextField descriptionField = new TextField();
                descriptionField.setValue(item.getDescription() != null ? item.getDescription() : "");
                descriptionField.setWidthFull();
                descriptionField.setPlaceholder("Введите комментарий...");

                binder.removeBinding(descriptionField);
                binder.forField(descriptionField)
                        .bind(MediaDto::getDescription, MediaDto::setDescription);

                return descriptionField;
            }
        }

        return new Span("");
    }

    private void updateAddButtonState() {
        addButton.setEnabled(originalItems.isEmpty());
        allButton.setEnabled(selectedAccount != null);
        moviesButton.setEnabled(selectedAccount != null);
        seriesButton.setEnabled(selectedAccount != null);
    }
}
