package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.TransferBuildingResult;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.service.postgres.BuildingPostgresService;
import com.creazione.space_learning.service.postgres.ResourcePostgresService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferResourceService {
    private final ResourceService resourceService;
    private final BuildingPostgresService buildingPostgresService;
    private final ResourcePostgresService resourcePostgresService;

    public String transferResource(UserDto user, int iBuilding) {
        Instant date = Instant.now();
        // Производит вычисление, не сохраняет
        DataSet.getResourceService().calculateQuantityChanges(user, date);

        BuildingP building = user.getBuildings().get(iBuilding);
        if (building == null) {
            System.out.println("нет объекта");
        } else {
            System.out.println("тип здания: " + building.getName().name());
        }
        TransferBuildingResult calculateTransfer = resourceService.calculateTransfer(building.getResourcesInBuilding());
        building.setResourcesInBuilding(calculateTransfer.getRemainingAmount());
        ResourceDto resourceDto = ResourceList.createResource(building.getProduction().name(), calculateTransfer.getTransferredAmount());
        resourceDto.setUserId(user.getId());
        resourceService.addResourceOrIncrement(user.getResources(), resourceDto);
        buildingPostgresService.saveAll(user.getBuildings(), user.getTelegramId());
        resourcePostgresService.saveAll(user.getResources(), user.getTelegramId());

        return "\n" + Emoji.STAR2 + "<b>Ресурсы отправлены на склад!</b>\n\n";
    }
}
