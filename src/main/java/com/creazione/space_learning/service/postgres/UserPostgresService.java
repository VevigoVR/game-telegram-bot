package com.creazione.space_learning.service.postgres;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.UserP;
import com.creazione.space_learning.repository.UserRepository;
import com.creazione.space_learning.service.redis.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPostgresService {
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final ResourcePostgresService resourcePostgresService;
    private final BuildingPostgresService buildingPostgresService;

    private UserP toPostgresObject(UserDto userDto) {
        return new UserP(userDto.getId(),
                userDto.getTelegramId(),
                userDto.getName(),
                userDto.getBuildings() == null ? new HashSet<>() : new HashSet<>(userDto.getBuildings()),
                userDto.getResources() == null ? new HashSet<>() : new HashSet<>(resourcePostgresService.toPostgresObjectList(userDto.getResources())),
                userDto.getBoosters() == null ? new HashSet<>() : new HashSet<>(userDto.getBoosters()),
                userDto.getPlayerScore(),
                userDto.getReferrer(),
                userDto.getTotalReferrals(),
                userDto.getNotices() == null ? new HashSet<>() : new HashSet<>(userDto.getNotices()),
                userDto.isSuperAggregate(),
                userDto.isPost(),
                userDto.getUpdatedAt(),
                userDto.getCreatedAt()
        );
    }

    private UserDto toGameObject(UserP userP) {
        return new UserDto(userP.getId(),
                userP.getTelegramId(),
                userP.getName(),
                userP.getBuildings() == null ? new ArrayList<>() : new ArrayList<>(userP.getBuildings()),
                userP.getResources() == null ? new ArrayList<>() : new ArrayList<>(resourcePostgresService.toGameObjectList(userP.getResources())),
                userP.getBoosters() == null ? new ArrayList<>() : new ArrayList<>(userP.getBoosters()),
                userP.getPlayerScore(),
                userP.getReferrer(),
                userP.getTotalReferrals(),
                userP.getNotices() == null ? new ArrayList<>() : new ArrayList<>(userP.getNotices()),
                userP.isSuperAggregate(),
                userP.isPost(),
                userP.getUpdatedAt(),
                userP.getCreatedAt()
        );
    }

    private List<UserP> toPostgresObjectList(List<UserDto> userDtos) {
        return userDtos.stream()
                .map(this::toPostgresObject)
                .collect(Collectors.toList());
    }

    private List<UserDto> toGameObjectList(List<UserP> userPList) {
        return userPList.stream()
                .map(this::toGameObject)
                .collect(Collectors.toList());
    }

    private Page<UserDto> toGameObjectListFromPage(Page<UserP> userPList) {
        return userPList.map(this::toGameObject);
    }

    public Page<UserDto> findUsersWithResourcesAndRecentBuildingUpdates(Pageable pageable) {
        return toGameObjectListFromPage(userRepository.findUsersWithResourcesAndRecentBuildingUpdates(pageable));
    }

    public UserDto findFullUserByTelegramId(Long telegramId) {
        UserDto userDto = userCacheService.getUser(telegramId);
        if (userDto != null) {
            Long id = userDto.getId();
            userDto.setResources(resourcePostgresService.findResourcesById(id, telegramId));
            userDto.setBuildings(buildingPostgresService.getBuildings(id, telegramId));
            userDto.setBoosters(DataSet.getBoosterService().findAllIBByUserIdToList(id, telegramId));
            return userDto;
        }

        Optional<UserP> userEntity = userRepository.findFullUserByTelegramId(telegramId);
        if (userEntity.isPresent()) {
            userCacheService.cacheFullUser(toGameObject(userEntity.get()));
            return toGameObject(userEntity.get());
        } else {
            return null;
        }
    }

    public UserDto saveFull(UserDto userDto) {
        UserP user = userRepository.save(toPostgresObject(userDto));
        UserDto userDtoResult = toGameObject(user);
        userCacheService.cacheFullUser(userDtoResult);
        return userDtoResult;
    }

    public UserDto saveFullWithoutCache(UserDto userDto) {
        return toGameObject(userRepository.save(toPostgresObject(userDto)));
    }

    public UserDto findById(Long userId) {
        Optional<UserP> userEntity = userRepository.findById(userId);
        return userEntity.map(this::toGameObject).orElse(null);
    }

    public Page<UserDto> findAllUsers(Pageable pageable) {
        return toGameObjectListFromPage(userRepository.findAll(pageable));
    }

    public Page<Long> findAllUserIds(Pageable pageable) {
        return userRepository.findAllUserIds(pageable);
    }

    public Page<UserDto> findAll(Pageable pageable) {
        return toGameObjectListFromPage(userRepository.findAll(pageable));
    }

    public UserDto findUserWithResourcesById(Long userId) {
        Optional<UserP> userEntity = userRepository.findUserWithResourcesById(userId);
        return userEntity.map(this::toGameObject).orElse(null);
    }

    // WARNING СОХРАНЕНИЕ В ЦИКЛЕ, НУЖНО ОПТИМИЗИРОВАТЬ!!!
    public void saveAll(List<UserDto> userDtos) {
        for (UserDto userDto : userDtos) {
            userCacheService.deleteFullUser(userDto.getTelegramId());
        }
        userRepository.saveAll(toPostgresObjectList(userDtos));
    }

    public int updateNameById(Long id, String name) {
        return userRepository.updateNameById(id, name);
    }

    public Optional<Long> findTelegramIdByUserId(Long userId) {
        return userRepository.findTelegramIdById(userId);
    }
}