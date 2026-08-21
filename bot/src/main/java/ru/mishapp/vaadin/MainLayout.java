package ru.mishapp.vaadin;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "")
public class MainLayout extends AppLayout {
	public MainLayout() {
		DrawerToggle toggle = new DrawerToggle();

		H1 title = new H1("Семейный бот");
		title.getStyle().set("font-size", "1.125rem").set("margin", "0");

		SideNav nav = getSideNav();
		nav.getStyle().set("margin", "var(--vaadin-gap-s)");

		Scroller scroller = new Scroller(nav);

		addToDrawer(scroller);
		addToNavbar(toggle, title);
	}

	private SideNav getSideNav() {
		SideNav nav = new SideNav();

		SideNavItem predictionLink = new SideNavItem("План платежей", PaymentsView.class,
				VaadinIcon.MONEY.create());
		SideNavItem mediaLink = new SideNavItem("Фильмы/сериалы", MediaView.class,
				VaadinIcon.MOVIE.create());
		nav.addItem(predictionLink, mediaLink);
		return nav;
	}
}