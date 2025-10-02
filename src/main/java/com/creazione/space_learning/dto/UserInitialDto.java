package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.game_entity.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInitialDto {
    private UserDto userDto;
    private String query;
    private int messageId;
    private long chatId;
    private boolean status;
    private boolean update;
}
