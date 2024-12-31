package com.sanchat.api.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/map")
@CrossOrigin(origins = "*")
public class MapController {

    @GetMapping("/mapData")
    public String getMapData() {
        return "map 데이터 테스팅 입니다.";
    }

    @PostMapping("/PostMapData")
    public void postMapData(@RequestBody String data){
        System.out.println("react 에서 받아온 데이터" + data);
    }

}
