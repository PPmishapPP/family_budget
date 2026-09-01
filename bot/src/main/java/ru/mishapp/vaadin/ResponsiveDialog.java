package ru.mishapp.vaadin;

import com.vaadin.flow.component.dialog.Dialog;

/**
 * Базовый класс диалога с адаптивным поведением:
 * на десктопе — окно заданной ширины (но не шире экрана),
 * на мобильных — занимает весь экран (см. responsive.css).
 */
public abstract class ResponsiveDialog extends Dialog {

    protected ResponsiveDialog(int desktopWidthPx) {
        addClassName("responsive-dialog");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(false);
        setResizable(false);

        // Никогда не шире экрана и не выше viewport
        setWidth("min(" + desktopWidthPx + "px, 100vw)");
        setHeight("min(700px, 90vh)");
    }
}
