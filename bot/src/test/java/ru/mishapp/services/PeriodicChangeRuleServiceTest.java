package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.records.PaymentRuleCommand;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodicChangeRuleServiceTest {

    @Mock
    private PeriodicChangeRuleRepository repository;

    @Mock
    private PeriodicChangeRuleMapper mapper;

    @InjectMocks
    private PeriodicChangeRuleService service;

    private final LocalDate nextDay = LocalDate.of(2026, 12, 1);

    @Test
    void shouldCreateActiveRuleWithZeroPass() {
        when(repository.save(any(PeriodicChangeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodicChangeRule result = service.create(10L, new PaymentRuleCommand(
                "  Интернет  ", -500, Type.MONTHLY, nextDay, null, null));

        ArgumentCaptor<PeriodicChangeRule> captor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
        verify(repository).save(captor.capture());
        PeriodicChangeRule saved = captor.getValue();

        assertEquals("Интернет", saved.getName());
        assertEquals(10L, saved.getTargetAccountId());
        assertEquals(0, saved.getPass());
        assertTrue(saved.isActive());
        assertEquals(Type.MONTHLY, saved.getType());
        assertEquals("Интернет", result.getName());
    }

    @Test
    void shouldPreservePassWhenUpdatingRule() {
        PeriodicChangeRule existing = rule(7L, 3, true);
        when(repository.findByIdAndTargetAccountId(7L, 10L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PeriodicChangeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodicChangeRule result = service.update(10L, 7L, new PaymentRuleCommand(
                "Интернет", -600, Type.MONTHLY, nextDay, false,
                LocalDate.of(2027, 12, 1)));

        ArgumentCaptor<PeriodicChangeRule> captor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
        verify(repository).save(captor.capture());
        PeriodicChangeRule saved = captor.getValue();

        assertEquals(7L, saved.getId());
        assertEquals(3, saved.getPass());
        assertFalse(saved.isActive());
        assertEquals(-600, saved.getSum());
        assertEquals(LocalDate.of(2027, 12, 1), saved.getEndDate());
        assertFalse(result.isActive());
    }

    @Test
    void shouldDeleteOwnedRule() {
        PeriodicChangeRule existing = rule(7L, 0, true);
        when(repository.findByIdAndTargetAccountId(7L, 10L)).thenReturn(Optional.of(existing));

        service.delete(10L, 7L);

        verify(repository).deleteById(7L);
    }

    @Test
    void shouldRejectMissingRuleOnDelete() {
        when(repository.findByIdAndTargetAccountId(7L, 10L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.delete(10L, 7L));
    }

    @Test
    void shouldMapLegacyRuleDtosToApiDtos() {
        when(repository.findAllRuleDtos(10L)).thenReturn(List.of(
                new PeriodicChangeRuleDto(7L, "Интернет", -500, "MONTHLY", 2,
                        nextDay, true, null)));

        List<PeriodicChangeRuleDto> result = service.findAll(10L);

        assertEquals(1, result.size());
        assertEquals(Type.MONTHLY.name(), result.getFirst().type());
        assertEquals(2, result.getFirst().pass());
    }

    private PeriodicChangeRule rule(Long id, int pass, boolean active) {
        return PeriodicChangeRule.builder()
                .id(id)
                .targetAccountId(10L)
                .name("Интернет")
                .sum(-500)
                .type(Type.MONTHLY)
                .pass(pass)
                .nextDay(nextDay)
                .active(active)
                .build();
    }
}
