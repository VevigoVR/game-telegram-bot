package com.creazione.space_learning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaginationDto {
    int page;
    int size;
    int limit;
    int totalPages = 1;
    List<InlineKeyboardButton> buttons = new ArrayList<>();
}
