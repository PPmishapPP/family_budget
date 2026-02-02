package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ru.mishapp.entity.Chat;
import ru.mishapp.entity.Media;
import ru.mishapp.services.ChatService;
import ru.mishapp.services.MediaService;

import java.util.List;

@Route(value = "media", layout = MainLayout.class)
public class MediaView extends VerticalLayout {

	private final ChatService chatService;
	private final MediaService mediaService;
	private VerticalLayout panel;
	private final Grid<Media> mediaGrid = new Grid<>(Media.class);

	// Текущий выбранный чат
	private Chat selectedChat;

	public MediaView(ChatService chatService, MediaService mediaService) {
		this.chatService = chatService;
		this.mediaService = mediaService;

		setSizeFull();
		setPadding(true);
		setSpacing(true);

		createHeader();
		createChatButtonsPanel();
		createMediaGrid();
	}

	private void createHeader() {
		H3 header = new H3("🎬 Управление фильмами и сериалами");
		header.getStyle()
				.set("margin", "0");

		add(header);
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
		loadMediaForChat(chat.getChatId());
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

		// Переключатели фильтров
		Button allButton = new Button("Все");
		Button moviesButton = new Button("Фильмы", new Icon(VaadinIcon.FILM));
		Button seriesButton = new Button("Сериалы", new Icon(VaadinIcon.PLAY_CIRCLE));

		allButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		moviesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		seriesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		// Выделение активного фильтра
		allButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		// Обработчики
		allButton.addClickListener(e -> {
			if (selectedChat != null) {
				loadMediaForChat(selectedChat.getChatId());
				updateActiveFilter(allButton, moviesButton, seriesButton);
			}
		});

		moviesButton.addClickListener(e -> {
			if (selectedChat != null) {
				List<Media> movies = mediaService.getMoviesByChatId(selectedChat.getId());
				mediaGrid.setItems(movies);
				updateActiveFilter(moviesButton, allButton, seriesButton);
			}
		});

		seriesButton.addClickListener(e -> {
			if (selectedChat != null) {
				List<Media> series = mediaService.getSeriesByChatId(selectedChat.getId());
				mediaGrid.setItems(series);
				updateActiveFilter(seriesButton, allButton, moviesButton);
			}
		});

		filterPanel.add(allButton, moviesButton, seriesButton);

		return filterPanel;
	}

	private void updateActiveFilter(Button active, Button... others) {
		active.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
		active.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		for (Button other : others) {
			other.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
			other.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		}
	}

	private void configureMediaGrid() {
		mediaGrid.setSizeFull();
		mediaGrid.setAllRowsVisible(true);

		// Убираем стандартные колонки
		mediaGrid.removeAllColumns();

		// Колонка с названием
		mediaGrid.addColumn(Media::getName)
				.setHeader("Название")
				.setSortable(true)
				.setAutoWidth(true)
				.setFlexGrow(1);

		// Колонка с типом
		mediaGrid.addComponentColumn(item -> {
			HorizontalLayout layout = new HorizontalLayout();
			layout.setAlignItems(Alignment.CENTER);
			layout.setSpacing(true);

			Span badge = new Span(item.getType().getName());
			layout.add(badge);
			return layout;
		}).setHeader("Тип").setWidth("120px");

		mediaGrid.addComponentColumn(item -> {
			HorizontalLayout layout = new HorizontalLayout();
			layout.setAlignItems(Alignment.CENTER);
			layout.setSpacing(true);

			Span badge = new Span(item.getStatus().getName());
			layout.add(badge);
			return layout;
		}).setHeader("Статус").setWidth("120px");

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
		}).setHeader("Рейтинг").setWidth("150px");

		// Колонка действий
		mediaGrid.addComponentColumn(item -> {
			Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
			deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
			deleteButton.addClickListener(e -> {
				// Удаление элемента
				// mediaService.deleteMediaItem(item.getId());
				// loadMediaForChat(selectedChat.getChatId());
			});

			Button editButton = new Button(new Icon(VaadinIcon.EDIT));
			editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

			HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
			actions.setSpacing(true);
			return actions;
		}).setHeader("Действия").setWidth("130px");
	}

	private void loadMediaForChat(Long chatId) {
		if (chatId != null) {
			List<Media> mediaItems = mediaService.getMediaByChatId(chatId);
			mediaGrid.setItems(mediaItems);
		}
	}
}
