package ru.mishapp.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.services.PeriodicChangeService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PeriodicChangeRuleMapper {

	private final AccountRepository accountRepository;
	private final PeriodicChangeService periodicChangeService;

	public PeriodicChangeRule toEntity(PeriodicChangeRuleDto dto, Account account, Map<String, PeriodicChange> periodicChangeMap) {
		PeriodicChangeRule.PeriodicChangeRuleBuilder builder = PeriodicChangeRule.builder();
		builder.targetAccountId(account.getId());

		if (!periodicChangeMap.containsKey(dto.periodicChangeName())) {
			throw new IllegalArgumentException("Не существует периодического изменения " + dto.periodicChangeName());
		}
		builder.periodicChangeId(periodicChangeMap.get(dto.periodicChangeName()).getId());

		Type type = Type.valueOf(dto.type());
		builder.id(dto.id());
		builder.type(type);
		builder.name(dto.name());
		builder.sum(dto.sum());
		builder.pass(dto.pass());
		builder.nextDay(dto.nextDay());
		builder.active(dto.active());
		builder.endDate(dto.endDate());
		return builder.build();
	}

	public List<PeriodicChangeRule> toEntityList(List<PeriodicChangeRuleDto> dtos, long chatId) {
		Account account = accountRepository.findByNameAndChatId("Безопасное место для денег", chatId).orElseThrow(
				() -> new IllegalArgumentException("Не существует счёта с именем 'Безопасное место для денег'")
		);
		Map<String, PeriodicChange> periodicChangeMap = periodicChangeService.findAll(chatId);
		return dtos.stream()
				.map(dto -> toEntity(dto, account, periodicChangeMap))
				.toList();
	}
}