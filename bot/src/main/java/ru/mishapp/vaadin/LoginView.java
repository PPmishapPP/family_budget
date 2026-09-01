package ru.mishapp.vaadin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Вход")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

	private final LoginForm login = new LoginForm();

	public LoginView() {
		addClassName("login-view");
		setSizeFull();
		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);

		// На десктопе не даём форме растянуться на весь экран,
		// на мобильных она занимает доступную ширину
		setWidth("100%");
		setMaxWidth("480px");
		getStyle().set("margin", "0 auto");

		login.setAction("login");
		login.setForgotPasswordButtonVisible(false);
		login.getStyle().set("width", "100%");

		add(new H1("Семейный бот"), login);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			login.setError(true);
		}
	}
}
