package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.exception.AppException;

import java.util.List;

/**
 * Шаблон консольного меню. Подклассы задают заголовок, пункты и действия,
 * а общий цикл «показать меню — выбрать — выполнить — вернуться» написан один раз здесь.
 */
public abstract class Menu {

    protected final ConsoleInput in;

    protected Menu(ConsoleInput in) {
        this.in = in;
    }

    protected abstract String title();

    /** Пункты меню под номерами 1, 2, 3... (пункт 0 добавляется автоматически). */
    protected abstract List<String> items();

    /** Выполняет пункт меню с номером choice (1..N). */
    protected abstract void handle(int choice);

    protected String exitLabel() {
        return "Назад";
    }

    public void run() {
        while (true) {
            print();
            int choice = in.readMenuChoice(items().size());
            if (choice == 0) {
                return;
            }
            safely(() -> handle(choice));
        }
    }

    private void print() {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" " + title());
        System.out.println("========================================");
        List<String> items = items();
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i));
        }
        System.out.println("0. " + exitLabel());
    }

    /** Любая ошибка выполнения действия показывается пользователем, а программа продолжает работать. */
    private void safely(Runnable action) {
        try {
            action.run();
        } catch (InputClosedException e) {
            throw e;
        } catch (AppException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Непредвиденная ошибка: " + e);
        }
    }
}
