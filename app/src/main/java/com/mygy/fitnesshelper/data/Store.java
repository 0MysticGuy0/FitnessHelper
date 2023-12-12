package com.mygy.fitnesshelper.data;

public class Store {
    public static String auth;
    public static final SportActivity walking;

    static{
        auth = "###bvr###";
        walking = new SportActivity("Ходьба",300);
        new SportActivity("Бег",620);
        new SportActivity("Приседания",600);
        new SportActivity("Обруч",580);
        new SportActivity("Скакалка",725);
        new SportActivity("Прыжки",760);
        new SportActivity("Велотренажёр",700);
        new SportActivity("Езда на велосипеде",500);
        new SportActivity("Плавание",700);
        new SportActivity("Танцы",300);
        new SportActivity("Йога",280);
        new SportActivity("Катание на коньках",420);
        new SportActivity("Футбол",400);
        new SportActivity("Волейбол",230);
        new SportActivity("Баскетбол",350);
        new SportActivity("Хоккей",280);
        new SportActivity("Теннис",370);
        new SportActivity("Борьба",1000);
        new SportActivity("Аэробика",340);
    }
}
