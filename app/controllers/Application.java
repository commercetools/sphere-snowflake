package controllers;

import play.mvc.Result;
import sphere.ShopController;

public class Application extends ShopController {

    public static Result move(String path) {
        return movedPermanently("/" + path);
    }

}