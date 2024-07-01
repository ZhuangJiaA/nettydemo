package com.sixeco.nettydemo.controller;

import com.sixeco.nettydemo.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class NettyController {

    private final UserService userService;

    public NettyController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/bindUserGroup")
    @ResponseBody
    public void bindUserGroup(@RequestParam("userId") long userId, @RequestParam("groupId") long groupIds) {
        userService.bindUserGroup(userId, groupIds);
    }

}
