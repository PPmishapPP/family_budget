package ru.mishapp.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PeriodicChangeRuleMapper {

	private final AccountRepository accountRepository;
	private final PeriodicChangeRepository periodicChangeRepository;

	public PeriodicChangeRule toEntity(PeriodicChangeRuleDto dto, long chatId) {
		PeriodicChangeRule.PeriodicChangeRuleBuilder builder = PeriodicChangeRule.builder();
		Optional<Account> targetAccount = accountRepository.findByNameAndChatId(dto.targetAccountName(), chatId);
		if (targetAccount.isEmpty()) {
			throw new IllegalArgumentException("Не существует счёта с именем " + dto.targetAccountName());
		}
		builder.targetAccountId(targetAccount.get().getId());

		Optional<PeriodicChange> periodicChange = periodicChangeRepository.findByNameAndChatId(dto.periodicChangeName(), chatId);
		if (periodicChange.isEmpty()) {
			throw new IllegalArgumentException("Не существует периодического изменения " + dto.periodicChangeName());
		}
		builder.periodicChangeId(periodicChange.get().getId());

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
		return dtos.stream()
				.map(dto -> toEntity(dto, chatId))
				.toList();
	}
}