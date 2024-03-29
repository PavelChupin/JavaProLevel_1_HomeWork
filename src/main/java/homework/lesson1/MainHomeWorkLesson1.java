package homework.lesson1;

import homework.lesson1.object.ActionsObject;
import homework.lesson1.object.Cat;
import homework.lesson1.object.Person;
import homework.lesson1.object.Robot;
import homework.lesson1.sportobject.RunLine;
import homework.lesson1.sportobject.Sports;
import homework.lesson1.sportobject.Wall;

import java.util.ArrayList;
import java.util.List;

public class MainHomeWorkLesson1 {

    public static void main(String[] args) {

        List<ActionsObject> actionsObjects = new ArrayList<ActionsObject>();
        actionsObjects.add(new Person(100, 200));
        actionsObjects.add(new Person(200, 400));
        actionsObjects.add(new Cat(100, 200));
        actionsObjects.add(new Cat(200, 400));
        actionsObjects.add(new Robot(100, 200));
        actionsObjects.add(new Robot(200, 400));

        List<Sports> sports = new ArrayList<Sports>();
        sports.add(new Wall(100));
        sports.add(new Wall(300));
        sports.add(new RunLine(200));
        sports.add(new RunLine(300));


        for (ActionsObject o : actionsObjects) {
            //Проходим беговую дорожку
            for (Sports r : sports) {
                if (!r.doAction(o)) {
                    break;
                }
            }
        }


    }

}
