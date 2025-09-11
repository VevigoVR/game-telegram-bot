package com.creazione.space_learning.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Getter
@Setter
@NoArgsConstructor
public class Answer {
    private SendMessage sendMessage;
    private SendPhoto sendPhoto;
    private AnswerCallbackQuery answerCallbackQuery;
    private EditMessageReplyMarkup editMessageReplyMarkup;
    private EditMessageText newTxt;
    private DeleteMessage deleteMessage;
    private EditMessageCaption editMessageCaption;
}
