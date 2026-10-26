package ru.mishapp.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.services.AccountService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static ru.mishapp.Constants.RUB;

@PermitAll
@Route(value = "history", layout = MainLayout.class)
public class HistoryView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AccountService accountService;
    private final Grid<AccountHistory> historyGrid = new Grid<>();
    private final VerticalLayout panel = new VerticalLayout();

    private Account selectedAccount;

    public HistoryView(AccountService accountService) {
        this.accountService = accountService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createAccountButtonsPanel();
        configureHistoryGrid();
        add(historyGrid);
        setFlexGrow(1, historyGrid);
    }

    private void createHeader() {
        H3 header = new H3("📜 История платежей");
        header.getStyle().set("margin", "0");
        add(header);
    }

    private void createAccountButtonsPanel() {
        panel.setPadding(false);
        panel.setSpacing(false);

        H3 panelTitle = new H3("💬 Счета");
        panelTitle.getStyle()
                .set("margin-bottom", "0.5em")
                .set("margin-top", "1em")
                .set("font-size", "16px");
        panel.add(panelTitle);

        HorizontalLayout accountLayout = new HorizontalLayout();
        accountLayout.setWidthFull();
        accountLayout.setSpacing(true);
        accountLayout.setPadding(false);
        accountLayout.getStyle()
                .set("gap", "8px")
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
        selectedAccount = account;
        updateAccountButtonsStyle();
        loadHistory(account.getId());
    }

    private void updateAccountButtonsStyle() {
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

    private void loadHistory(Long accountId) {
        List<AccountHistory> history = accountService.findHistoryByAccountId(accountId);
        historyGrid.setItems(history);
    }

    private void configureHistoryGrid() {
        historyGrid.removeAllColumns();

        historyGrid.addComponentColumn(item -> {
                    String comment = item.getComment();
                    if (comment == null || comment.isBlank()) {
                        Span empty = new Span("—");
                        empty.getStyle().set("color", "var(--lumo-disabled-text-color)");
                        return empty;
                    }
                    return new Span(comment);
                })
                .setHeader("Наименование")
                .setSortable(true)
                .setComparator(Comparator.comparing(AccountHistory::getComment,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .setAutoWidth(true)
                .setFlexGrow(1);

        historyGrid.addComponentColumn(item -> formatSum(item.getSum()))
                .setHeader("Сумма")
                .setSortable(true)
                .setComparator(Comparator.comparingInt(AccountHistory::getSum))
                .setAutoWidth(true);

        historyGrid.addColumn(item -> item.getDateTime().format(DATE))
                .setHeader("Дата")
                .setSortable(true)
                .setAutoWidth(true);

        historyGrid.addColumn(item -> RUB.format(item.getBalance()))
                .setHeader("Баланс")
                .setSortable(true)
                .setComparator(Comparator.comparingInt(AccountHistory::getBalance))
                .setAutoWidth(true);

        historyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_COLUMN_BORDERS);
    }

    private Span formatSum(int sum) {
        Span sumSpan = new Span(RUB.format(sum));
        if (sum > 0) {
            sumSpan.getStyle().set("color", "var(--lumo-success-text-color)");
        } else if (sum < 0) {
            sumSpan.getStyle().set("color", "var(--lumo-error-text-color)");
        }
        return sumSpan;
    }
}
