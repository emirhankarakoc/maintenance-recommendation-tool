package com.karakoc.sofra.exceptions.general;

import com.karakoc.sofra.exceptions.RestException;
import com.karakoc.sofra.exceptions.config.AppConfig;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class NotfoundException extends RestException {

    @Getter
    private final HttpStatus httpStatus;

    // Tek parametreli constructor: Hem production hem de development için aynı mesaj kullanılır
    public NotfoundException(String message) {
        super(message);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    // İki parametreli constructor: Production ve development mesajı alır, aktif profile göre seçer
    public NotfoundException(String prodMessage, String devMessage) {
        // Başlangıçta geçici bir mesaj set ediyoruz
        super("Initializing message...");

        // Aktif profili AppConfig'ten alıyoruz
        String activeProfile = AppConfig.getActiveProfile();

        // Aktif profile göre mesajı seçiyoruz
        String finalMessage = "dev".equals(activeProfile) ? devMessage : prodMessage;

        // Mesajı ayarlıyoruz
        this.setMessage(finalMessage);

        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
