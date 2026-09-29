package com.karakoc.sofra.chatgptapi;

import com.karakoc.sofra.security.UserPrincipal;
import com.karakoc.sofra.services.CarServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parser")
@RequiredArgsConstructor
public class ParserController {

    private final ChatgptApi chatgptApi;
    private final CarServiceRepository carServiceRepository;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public ChatgptApi.ParserResult parse(
            @RequestBody String serviceHistory,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        List<ChatgptApi.TrackedService> services =
                carServiceRepository
                        .findAllByCreatorUserId(user.getUserId())
                        .stream()
                        .map(service ->
                                new ChatgptApi.TrackedService(
                                        service.getId(),
                                        service.getName()
                                )
                        )
                        .toList();

        System.out.println(
                "Gonderilen servis sayisi: " + services.size()
        );

        ChatgptApi.ParserQuery query =
                new ChatgptApi.ParserQuery(
                        serviceHistory,
                        services
                );

        return chatgptApi.parse(query);
    }
}