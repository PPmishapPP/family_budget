package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.Chat;
import ru.mishapp.repository.ChatRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRepository chatRepository;

	public List<Chat> getAllChats() {
		return StreamSupport.stream(chatRepository.findAll().spliterator(), false)
				.collect(Collectors.toList());
	}

	public Chat getChatByChatId(Long chatId) {
		return chatRepository.findByChatId(chatId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Нет чата с таким chatId: %s", chatId)));
	}

	public Chat getChatByName(String name) {
		return chatRepository.findByName(name).orElseThrow();
	}
}

