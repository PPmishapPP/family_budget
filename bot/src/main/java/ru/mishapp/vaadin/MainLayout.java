package ru.mishapp.vaadin;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "")
@CssImport(value = "./styles/responsive.css")
public class MainLayout extends AppLayout {
	public MainLayout(AuthenticationContext authenticationContext) {
		DrawerToggle toggle = new DrawerToggle();

		H1 title = new H1("Семейный бюджет");
		title.getStyle().set("font-size", "1.125rem").set("margin", "0");

		SideNav nav = getSideNav();
		nav.getStyle().set("margin", "var(--vaadin-gap-s)");

		Scroller scroller = new Scroller(nav);

		addToDrawer(scroller);

		Button logout = new Button("Выйти", e -> authenticationContext.logout());
		logout.setIcon(VaadinIcon.SIGN_OUT.create());
		logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Span spacer = new Span();
		spacer.getStyle().set("flex", "1");

		addToNavbar(toggle, title, spacer, logout);
	}

	private SideNav getSideNav() {
		SideNav nav = new SideNav();

		SideNavItem predictionLink = new SideNavItem("План платежей", PaymentsView.class,
				VaadinIcon.MONEY.create());
		SideNavItem mediaLink = new SideNavItem("Фильмы/сериалы", MediaView.class,
				VaadinIcon.MOVIE.create());
		SideNavItem historyLink = new SideNavItem("История платежей", HistoryView.class,
				VaadinIcon.CLOCK.create());
		nav.addItem(predictionLink, mediaLink, historyLink);
		return nav;
	}
}