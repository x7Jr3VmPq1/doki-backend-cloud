package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.VideoFavorites;
import com.megrez.result.Result;
import com.megrez.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public Result<Void> add(@CurrentUser Integer uid,
                            @RequestParam Integer vid) {

        return favoriteService.add(uid, vid);
    }

    @DeleteMapping
    public Result<Void> del(@CurrentUser Integer uid,
                            @RequestParam Integer vid) {
        return favoriteService.del(uid, vid);
    }

}
