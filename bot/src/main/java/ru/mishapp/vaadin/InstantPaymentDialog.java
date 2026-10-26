package ru.mishapp.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableRunnable;
import lombok.Getter;
import ru.mishapp.Constants;
import ru.mishapp.entity.Account;
import ru.mishapp.services.AccountService;

import java.time.LocalDate;

public class InstantPaymentDialog extends ResponsiveDialog {

    @Getter
    private enum PaymentDirection {
        EXPENSE("Расход"),
        INCOME("Доход");

        private final String label;

        PaymentDirection(String label) {
            this.label = label;
        }

    }

    private final Account account;
    private final AccountService accountService;
    private final SerializableRunnable onSaved;

    private final TextField nameField = new TextField("Название платежа");
    private final RadioButtonGroup<PaymentDirection> directionField =
            new RadioButtonGroup<>("Тип операции");
    private final IntegerField sumField = new IntegerField("Сумма, ₽");

    public InstantPaymentDialog(Account account, AccountService accountService, SerializableRunnable onSaved) {
        super(520);
        this.account = account;
        this.accountService = accountService;
        this.onSaved = onSaved;

        setHeaderTitle("Разовый платёж");
        setHeight("auto");
        initDialog();
    }

    private void initDialog() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidthFull();

        Span accountSpan = new Span("Счёт: " + account.getName());
        Span dateSpan = new Span("Дата: " + LocalDate.now().format(Constants.DAY));

        VerticalLayout info = new VerticalLayout(accountSpan, dateSpan);
        info.setPadding(false);
        info.setSpacing(false);
        info.setWidthFull();
        info.getStyle()
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        nameField.setRequired(true);
        nameField.setMaxLength(256);
        nameField.setPlaceholder("Например, продукты");
        nameField.setWidthFull();

        directionField.setItems(PaymentDirection.values());
        directionField.setItemLabelGenerator(PaymentDirection::getLabel);
        directionField.setValue(PaymentDirection.EXPENSE);
        directionField.setWidthFull();

        sumField.setRequired(true);
        sumField.setMin(1);
        sumField.setStep(1);
        sumField.setWidthFull();

        Button saveButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(_ -> save());

        Button cancelButton = new Button("Отмена");
        cancelButton.addClickListener(_ -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        mainLayout.add(info, nameField, directionField, sumField, buttons);
        add(mainLayout);
    }

    private void save() {
        if (nameField.isEmpty() || sumField.isEmpty() || directionField.isEmpty()) {
            Notification.show("Заполните название, тип операции и сумму",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            Notification.show("Название платежа не может быть пустым",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        int amount = sumField.getValue();
        if (amount <= 0) {
            Notification.show("Сумма платежа должна быть больше нуля",
                    3000, Notification.Position.MIDDLE);
            return;
        }
        int sum = directionField.getValue() == PaymentDirection.EXPENSE ? -amount : amount;

        try {
            accountService.registerPayment(account.getId(), name, sum);
        } catch (RuntimeException e) {
            Notification.show("Не удалось сохранить платёж: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
            return;
        }

        close();
        if (onSaved != null) {
            onSaved.run();
        }
        Notification.show("Платёж сохранён", 3000, Notification.Position.TOP_CENTER);
    }
}
