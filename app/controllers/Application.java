package controllers;

import play.mvc.Result;
import sphere.ShopController;

public class Application extends ShopController {

    public static Result move(String path) {
        return movedPermanently("/" + path);
    }

    public static Result blitz() {
        return ok("42");
    }

}