package homework.lesson1.sportobject;

import homework.lesson1.ActionsObject;
import homework.lesson1.Sports;

public class Wall  implements Sports {

    private long height;

    public Wall() {
    }

    public Wall(long height) {
        this.height = height;
    }



    @Override
    public boolean doAction(ActionsObject actionsObject){
        return actionsObject.jump(height);
    }



}
