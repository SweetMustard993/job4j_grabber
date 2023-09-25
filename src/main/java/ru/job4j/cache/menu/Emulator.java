package ru.job4j.cache.menu;

import ru.job4j.cache.DirFileCache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Emulator {

    private final Scanner in;

    private final String menuText;

    public Emulator(Scanner in) {
        this.in = in;
        String sep = System.lineSeparator();
        menuText = "Выберите действие номер действия:" + sep + "1.Прочитать из кэша" + sep + "2.Добавить в кэш"
                + sep + "3.Выйти из программы";
    }

    public void init() {
        String dir = askDir();
        final DirFileCache fileCache = new DirFileCache(dir);
        boolean run = true;
        while (run) {
            System.out.println(menuText);
            String action = in.nextLine();
            if (action.length() > 1) {
                System.out.println("Введено не верное значение.");
                continue;
            }
            if (!action.chars().allMatch(Character::isDigit)) {
                System.out.println("Введенное значение не является числом.");
                continue;
            }
            int numberAction = Integer.parseInt(action);
            if (numberAction == 1) {
                String valueFromCache = getAction(fileCache);
                System.out.println(valueFromCache);
            } else if (numberAction == 2) {
                putAction(fileCache);
            } else if (numberAction == 3) {
                run = false;
            } else {
                System.out.println("Введен неверный номер.");
            }
        }
    }

    private String getAction(DirFileCache fileCache) {
        System.out.println("Укажите название файла:");
        String askName = in.nextLine();
        return fileCache.get(askName);
    }

    private void putAction(DirFileCache fileCache) {
        System.out.println("Укажите название файла:");
        String askName = in.nextLine();
        fileCache.get(askName);
        System.out.println("Файл добавлен в кэш.");
    }

    private String askDir() {
        boolean invalid = true;
        String dir = new String();
        while (invalid) {
            System.out.println("Укажите путь директории для кэширования:");
            dir = in.nextLine();
            Path dirPath = Paths.get(dir);
            if (!dirPath.isAbsolute()) {
                System.out.println("Указанный текст не является путем.");
                continue;
            }
            if (!Files.isDirectory(dirPath)) {
                System.out.println("Указанный путь не является директорией.");
                continue;
            }
            System.out.println("Установлена директория: " + dirPath.normalize());
            invalid = false;
        }
        return dir;
    }

    public static void main(String[] args) {
        Emulator emulatorDirFileCache = new Emulator(new Scanner(System.in));
        emulatorDirFileCache.init();
    }
}
