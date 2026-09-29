package com.karakoc.sofra.exceptions.general;

import com.karakoc.sofra.exceptions.RestException;
import com.karakoc.sofra.exceptions.config.AppConfig;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends RestException {
    @Getter
    private final HttpStatus httpStatus;

    // Tek parametreli constructor
    public ForbiddenException(String msg) {
        super(msg);
        this.httpStatus = HttpStatus.FORBIDDEN;
    }

    // İki parametreli constructor
    public ForbiddenException(String prodMessage, String devMessage) {
        super("Initializing message...");

        // Aktif profili AppConfig'ten alıyoruz
        String activeProfile = AppConfig.getActiveProfile();

        // Aktif profile göre mesajı seçiyoruz
        String finalMessage = "dev".equals(activeProfile) ? devMessage : prodMessage;

        // Mesajı ayarlıyoruz
        this.setMessage(finalMessage);

        this.httpStatus = HttpStatus.FORBIDDEN;
    }
}
